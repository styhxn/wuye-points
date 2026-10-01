package com.wuye.points.service;

import com.wuye.points.dto.communityroom.AddRoomRequest;
import com.wuye.points.dto.communityroom.AddRoomsBatchRequest;
import com.wuye.points.dto.communityroom.AvailableRoomItemResponse;
import com.wuye.points.dto.communityroom.AvailableRoomListResponse;
import com.wuye.points.dto.communityroom.RoomListResponse;
import com.wuye.points.dto.communityroom.RoomManageItemResponse;
import com.wuye.points.dto.communityroom.RoomStructureResponse;
import com.wuye.points.dto.communityroom.UpdateRoomRequest;
import com.wuye.points.entity.Community;
import com.wuye.points.entity.CommunityRoom;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.CommunityRepository;
import com.wuye.points.repository.CommunityRoomRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * 社区房号（楼宇结构）服务（A12）。
 *
 * <p>v2 起系统维护独立的房号主表 {@link CommunityRoom}（community_rooms）：
 * <ul>
 *   <li>房号为物理房间一等数据，空房（status='vacant'）可由物业在楼栋管理中新增/删除/锁定；</li>
 *   <li>listAvailableRooms / getRoomStructure 直接读取房号主表，占用姓名由 residents 实时关联；</li>
 *   <li>住户入住/变更/注销时通过 {@link #syncRoom} 同步房号占用状态，保证主表与住户一致；</li>
 *   <li>历史数据未初始化主表时，自动回退到 residents 聚合（legacy 路径），保证平滑过渡。</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class CommunityRoomService {

    private static final String STATUS_VACANT = "vacant";
    private static final String STATUS_OCCUPIED = "occupied";
    private static final String STATUS_LOCKED = "locked";

    private final CommunityRepository communityRepository;
    private final ResidentRepository residentRepository;
    private final CommunityRoomRepository communityRoomRepository;
    private final RoleGuard roleGuard;
    private final SnowflakeIdGenerator idGenerator;

    /* ===================== 只读：楼宇结构 / 可选房号 ===================== */

    @Transactional(readOnly = true)
    public AvailableRoomListResponse listAvailableRooms(String communityId,
                                                       String building,
                                                       String unit) {
        assertCommunityExists(communityId);

        List<CommunityRoom> rooms = loadRooms(communityId, building, unit);
        if (rooms.isEmpty()) {
            return legacyAvailableRooms(communityId, building, unit);
        }

        // 已入住房号，用于剔除被占用的房号
        Map<String, String> occupied = activeResidentNameMap(communityId, building, unit);

        List<AvailableRoomItemResponse> items = new ArrayList<>();
        for (CommunityRoom r : rooms) {
            String key = roomKey(r.getBuilding(), r.getUnit(), r.getRoom());
            if (occupied.containsKey(key) || STATUS_LOCKED.equals(r.getStatus())) {
                continue; // 已占用或锁定的房号不可选
            }
            items.add(AvailableRoomItemResponse.builder()
                    .building(r.getBuilding())
                    .unit(r.getUnit())
                    .floor(r.getFloor())
                    .room(r.getRoom())
                    .isOccupied(false)
                    .residentName(null)
                    .build());
        }

        // 仅传 unit 不传 building 时，派生查询无法按 unit 过滤，这里在结果上补偿
        if (StringUtils.hasText(unit)) {
            String u = normUnit(unit);
            items = items.stream().filter(i -> u.equals(i.getUnit())).collect(Collectors.toList());
        }

        items.sort(Comparator
                .comparing(AvailableRoomItemResponse::getBuilding,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(AvailableRoomItemResponse::getUnit,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(AvailableRoomItemResponse::getFloor,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(AvailableRoomItemResponse::getRoom,
                        Comparator.nullsLast(Comparator.naturalOrder())));

        return AvailableRoomListResponse.builder()
                .communityId(communityId)
                .items(items)
                .totalAvailable(items.size())
                .build();
    }

    @Transactional(readOnly = true)
    public RoomStructureResponse getRoomStructure(String communityId, String building) {
        assertCommunityExists(communityId);

        List<CommunityRoom> rooms = loadRooms(communityId, building, null);
        if (rooms.isEmpty()) {
            return legacyStructure(communityId, building);
        }

        Map<String, String> occupied = activeResidentNameMap(communityId, building, null);

        // unit -> floor -> rooms
        Map<String, Map<String, List<CommunityRoom>>> unitTree =
                new TreeMap<>(Comparator.nullsFirst(Comparator.naturalOrder()));
        for (CommunityRoom r : rooms) {
            String floorKey = parseFloor(r.getRoom());
            unitTree.computeIfAbsent(r.getUnit(), k -> new TreeMap<>(Comparator.nullsFirst(Comparator.naturalOrder())))
                    .computeIfAbsent(floorKey, k -> new ArrayList<>())
                    .add(r);
        }

        List<RoomStructureResponse.UnitStructure> unitList = new ArrayList<>();
        for (Map.Entry<String, Map<String, List<CommunityRoom>>> unitEntry : unitTree.entrySet()) {
            List<RoomStructureResponse.FloorStructure> floorList = new ArrayList<>();
            for (Map.Entry<String, List<CommunityRoom>> floorEntry : unitEntry.getValue().entrySet()) {
                List<RoomStructureResponse.RoomInfo> roomList = new ArrayList<>();
                for (CommunityRoom r : floorEntry.getValue()) {
                    String key = roomKey(r.getBuilding(), r.getUnit(), r.getRoom());
                    boolean isOccupied = occupied.containsKey(key);
                    roomList.add(RoomStructureResponse.RoomInfo.builder()
                            .room(r.getRoom())
                            .isOccupied(isOccupied)
                            .residentName(occupied.get(key))
                            .status(r.getStatus())
                            .build());
                }
                roomList.sort(Comparator.comparing(RoomStructureResponse.RoomInfo::getRoom,
                        Comparator.nullsLast(Comparator.naturalOrder())));
                floorList.add(RoomStructureResponse.FloorStructure.builder()
                        .floor(floorEntry.getKey())
                        .rooms(roomList)
                        .build());
            }
            unitList.add(RoomStructureResponse.UnitStructure.builder()
                    .unit(unitEntry.getKey())
                    .floors(floorList)
                    .build());
        }

        return RoomStructureResponse.builder()
                .communityId(communityId)
                .building(building)
                .units(unitList)
                .build();
    }

    /* ===================== 管理端：房号增删改查 ===================== */

    @Transactional(readOnly = true)
    public RoomListResponse listRooms(String communityId) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        Community community = assertCommunityExists(communityId);
        roleGuard.assertSamePropertyOrPlatform(community.getPropertyCompanyId());

        List<CommunityRoom> rooms =
                communityRoomRepository.findByCommunityIdOrderByBuildingAscUnitAscRoomAsc(communityId);
        Map<String, String> occupied = activeResidentNameMap(communityId, null, null);
        List<RoomManageItemResponse> items = rooms.stream()
                .map(r -> toItem(r, occupied.get(roomKey(r.getBuilding(), r.getUnit(), r.getRoom()))))
                .collect(Collectors.toList());
        return RoomListResponse.builder()
                .communityId(communityId)
                .rooms(items)
                .total(items.size())
                .build();
    }

    @Transactional
    public RoomManageItemResponse addRoom(String communityId, AddRoomRequest req) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        Community community = assertCommunityExists(communityId);
        roleGuard.assertSamePropertyOrPlatform(community.getPropertyCompanyId());

        String building = req.getBuilding().trim();
        String unit = normUnit(req.getUnit());
        String room = req.getRoom().trim();
        if (communityRoomRepository.existsByCommunityIdAndBuildingAndUnitAndRoom(communityId, building, unit, room)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "该房号已存在");
        }
        CommunityRoom entity = CommunityRoom.builder()
                .id(idGenerator.generateId("room"))
                .communityId(communityId)
                .building(building)
                .unit(unit)
                .floor(StringUtils.hasText(req.getFloor()) ? req.getFloor().trim() : null)
                .room(room)
                .status(STATUS_VACANT)
                .build();
        communityRoomRepository.save(entity);
        return toItem(entity, null);
    }

    @Transactional
    public List<RoomManageItemResponse> addRoomsBatch(String communityId, AddRoomsBatchRequest req) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        Community community = assertCommunityExists(communityId);
        roleGuard.assertSamePropertyOrPlatform(community.getPropertyCompanyId());

        String building = req.getBuilding().trim();
        String defaultUnit = normUnit(req.getUnit());
        List<CommunityRoom> toSave = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (AddRoomsBatchRequest.RoomItem item : req.getRooms()) {
            if (item == null || !StringUtils.hasText(item.getRoom())) {
                continue;
            }
            String room = item.getRoom().trim();
            String unit = normUnit(defaultUnit);
            String key = building + "|" + unit + "|" + room;
            if (!seen.add(key)) {
                continue; // 同批去重
            }
            if (communityRoomRepository.existsByCommunityIdAndBuildingAndUnitAndRoom(communityId, building, unit, room)) {
                throw new BizException(ErrorCode.PARAM_INVALID,
                        "房号已存在：" + building + (StringUtils.hasText(unit) ? unit : "") + room);
            }
            toSave.add(CommunityRoom.builder()
                    .id(idGenerator.generateId("room"))
                    .communityId(communityId)
                    .building(building)
                    .unit(unit)
                    .floor(StringUtils.hasText(item.getFloor()) ? item.getFloor().trim() : null)
                    .room(room)
                    .status(STATUS_VACANT)
                    .build());
        }
        if (toSave.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "没有有效的房号可添加");
        }
        communityRoomRepository.saveAll(toSave);
        return toSave.stream().map(r -> toItem(r, null)).collect(Collectors.toList());
    }

    @Transactional
    public RoomManageItemResponse updateRoom(String communityId, String roomId, UpdateRoomRequest req) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        CommunityRoom room = communityRoomRepository.findById(roomId)
                .orElseThrow(() -> new BizException(ErrorCode.PARAM_INVALID, "房号不存在"));
        if (!room.getCommunityId().equals(communityId)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "房号不属于该小区");
        }
        Community community = assertCommunityExists(communityId);
        roleGuard.assertSamePropertyOrPlatform(community.getPropertyCompanyId());

        if (StringUtils.hasText(req.getStatus())) {
            if (!Set.of(STATUS_VACANT, STATUS_LOCKED).contains(req.getStatus())) {
                throw new BizException(ErrorCode.PARAM_INVALID, "status值无效");
            }
            if (STATUS_VACANT.equals(req.getStatus()) && STATUS_OCCUPIED.equals(room.getStatus())) {
                throw new BizException(ErrorCode.PARAM_INVALID, "已入住房间不能手动置为空置");
            }
            room.setStatus(req.getStatus());
        }
        if (StringUtils.hasText(req.getFloor())) {
            room.setFloor(req.getFloor().trim());
        }
        communityRoomRepository.save(room);
        return toItem(room, currentResidentName(room));
    }

    @Transactional
    public void deleteRoom(String communityId, String roomId) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        CommunityRoom room = communityRoomRepository.findById(roomId)
                .orElseThrow(() -> new BizException(ErrorCode.PARAM_INVALID, "房号不存在"));
        if (!room.getCommunityId().equals(communityId)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "房号不属于该小区");
        }
        Community community = assertCommunityExists(communityId);
        roleGuard.assertSamePropertyOrPlatform(community.getPropertyCompanyId());

        // 以防主表状态滞后，按真实入住情况再校验一次
        long active = countActive(room.getCommunityId(), room.getBuilding(), room.getUnit(), room.getRoom());
        if (active > 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "已入住房间不可删除");
        }
        communityRoomRepository.deleteByCommunityIdAndId(communityId, roomId);
    }

    /* ===================== 占用同步（供 ResidentService 调用） ===================== */

    /**
     * 同步某房号的占用状态。
     * <ul>
     *   <li>存在 active 住户 → status=occupied（如主表无记录则自动创建，使空房随入住显现）；</li>
     *   <li>无 active 住户 → 若原状态为 occupied 则回置 vacant，locked 状态保留。</li>
     * </ul>
     */
    @Transactional
    public void syncRoom(String communityId, String building, String unit, String room) {
        if (!StringUtils.hasText(communityId)
                || !StringUtils.hasText(building)
                || !StringUtils.hasText(room)) {
            return;
        }
        String u = normUnit(unit);
        long active = countActive(communityId, building, u, room);
        Optional<CommunityRoom> opt =
                communityRoomRepository.findFirstByCommunityIdAndBuildingAndUnitAndRoom(communityId, building, u, room);
        if (active > 0) {
            CommunityRoom cr = opt.orElseGet(() -> CommunityRoom.builder()
                    .id(idGenerator.generateId("room"))
                    .communityId(communityId)
                    .building(building)
                    .unit(u)
                    .room(room)
                    .build());
            cr.setStatus(STATUS_OCCUPIED);
            cr.setResidentId(firstActiveResidentId(communityId, building, u, room));
            communityRoomRepository.save(cr);
        } else {
            opt.ifPresent(cr -> {
                if (STATUS_OCCUPIED.equals(cr.getStatus())) {
                    cr.setStatus(STATUS_VACANT);
                    cr.setResidentId(null);
                    communityRoomRepository.save(cr);
                }
            });
        }
    }

    /* ===================== 私有辅助方法 ===================== */

    private List<CommunityRoom> loadRooms(String communityId, String building, String unit) {
        if (StringUtils.hasText(building) && StringUtils.hasText(unit)) {
            return communityRoomRepository.findByCommunityIdAndBuildingAndUnitOrderByRoomAsc(communityId, building, unit);
        }
        if (StringUtils.hasText(building)) {
            return communityRoomRepository.findByCommunityIdAndBuildingOrderByUnitAscRoomAsc(communityId, building);
        }
        return communityRoomRepository.findByCommunityIdOrderByBuildingAscUnitAscRoomAsc(communityId);
    }

    /** active 住户（communityId+building+room+unit 等价）姓名映射，key=building|unit|room */
    private Map<String, String> activeResidentNameMap(String communityId, String building, String unit) {
        Specification<Resident> spec = (root, query, cb) -> {
            List<Predicate> list = new ArrayList<>();
            list.add(cb.isNull(root.get("deletedAt")));
            list.add(cb.equal(root.get("communityId"), communityId));
            if (StringUtils.hasText(building)) {
                list.add(cb.equal(root.get("building"), building));
            }
            list.add(cb.equal(root.get("status"), "active"));
            list.add(unitPredicate(cb, root, unit));
            return cb.and(list.toArray(new Predicate[0]));
        };
        List<Resident> active = residentRepository.findAll(spec);
        Map<String, String> map = new LinkedHashMap<>();
        for (Resident r : active) {
            if (!StringUtils.hasText(r.getBuilding()) || !StringUtils.hasText(r.getRoom())) {
                continue;
            }
            String key = roomKey(r.getBuilding(), normUnit(r.getUnit()), r.getRoom());
            map.putIfAbsent(key, r.getName());
        }
        return map;
    }

    private long countActive(String communityId, String building, String unit, String room) {
        Specification<Resident> spec = (root, query, cb) -> {
            List<Predicate> list = new ArrayList<>();
            list.add(cb.isNull(root.get("deletedAt")));
            list.add(cb.equal(root.get("communityId"), communityId));
            list.add(cb.equal(root.get("building"), building));
            list.add(cb.equal(root.get("room"), room));
            list.add(cb.equal(root.get("status"), "active"));
            list.add(unitPredicate(cb, root, unit));
            return cb.and(list.toArray(new Predicate[0]));
        };
        return residentRepository.count(spec);
    }

    private String firstActiveResidentId(String communityId, String building, String unit, String room) {
        Specification<Resident> spec = (root, query, cb) -> {
            List<Predicate> list = new ArrayList<>();
            list.add(cb.isNull(root.get("deletedAt")));
            list.add(cb.equal(root.get("communityId"), communityId));
            list.add(cb.equal(root.get("building"), building));
            list.add(cb.equal(root.get("room"), room));
            list.add(cb.equal(root.get("status"), "active"));
            list.add(unitPredicate(cb, root, unit));
            return cb.and(list.toArray(new Predicate[0]));
        };
        List<Resident> list = residentRepository.findAll(spec, PageRequest.of(0, 1)).getContent();
        return list.isEmpty() ? null : list.get(0).getId();
    }

    private Predicate unitPredicate(jakarta.persistence.criteria.CriteriaBuilder cb,
                                     jakarta.persistence.criteria.Root<Resident> root, String unit) {
        if (StringUtils.hasText(unit)) {
            // 主表 unit 已规范为 ''，住户 unit 可能为 null，故两者等价匹配
            return cb.or(cb.equal(root.get("unit"), unit), cb.equal(root.get("unit"), ""));
        }
        return cb.or(cb.isNull(root.get("unit")), cb.equal(root.get("unit"), ""));
    }

    private String currentResidentName(CommunityRoom room) {
        Map<String, String> m = activeResidentNameMap(room.getCommunityId(), room.getBuilding(), room.getUnit());
        return m.get(roomKey(room.getBuilding(), room.getUnit(), room.getRoom()));
    }

    private RoomManageItemResponse toItem(CommunityRoom r, String residentName) {
        return RoomManageItemResponse.builder()
                .id(r.getId())
                .communityId(r.getCommunityId())
                .building(r.getBuilding())
                .unit(r.getUnit())
                .floor(r.getFloor())
                .room(r.getRoom())
                .status(r.getStatus())
                .residentId(r.getResidentId())
                .residentName(residentName)
                .createdAt(r.getCreatedAt() != null ? DateTimeUtil.toIsoUtc(r.getCreatedAt()) : null)
                .build();
    }

    private Community assertCommunityExists(String communityId) {
        return communityRepository.findById(communityId)
                .orElseThrow(() -> new BizException(ErrorCode.PARAM_INVALID, "小区不存在"));
    }

    private String normUnit(String unit) {
        return StringUtils.hasText(unit) ? unit.trim() : "";
    }

    private String roomKey(String building, String unit, String room) {
        return building + "|" + (unit == null ? "" : unit) + "|" + room;
    }

    private String parseFloor(String room) {
        if (room == null || room.isEmpty()) {
            return "";
        }
        StringBuilder digits = new StringBuilder();
        for (char c : room.toCharArray()) {
            if (Character.isDigit(c)) {
                digits.append(c);
            } else {
                break;
            }
        }
        return digits.length() > 0 ? digits.toString() : "";
    }

    /* ===================== 历史兼容：主表为空时按 residents 聚合 ===================== */

    private AvailableRoomListResponse legacyAvailableRooms(String communityId, String building, String unit) {
        List<Resident> residents = loadResidents(communityId, building, unit);
        Map<String, RoomAggregate> aggMap = aggregateByRoom(residents);

        List<AvailableRoomItemResponse> items = new ArrayList<>();
        for (RoomAggregate agg : aggMap.values()) {
            if (agg.activeCount > 0) {
                continue;
            }
            items.add(AvailableRoomItemResponse.builder()
                    .building(agg.building)
                    .unit(agg.unit)
                    .floor(parseFloor(agg.room))
                    .room(agg.room)
                    .isOccupied(false)
                    .residentName(null)
                    .build());
        }
        items.sort(Comparator
                .comparing(AvailableRoomItemResponse::getBuilding,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(AvailableRoomItemResponse::getUnit,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(AvailableRoomItemResponse::getFloor,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(AvailableRoomItemResponse::getRoom,
                        Comparator.nullsLast(Comparator.naturalOrder())));
        return AvailableRoomListResponse.builder()
                .communityId(communityId)
                .items(items)
                .totalAvailable(items.size())
                .build();
    }

    private RoomStructureResponse legacyStructure(String communityId, String building) {
        List<Resident> residents = loadResidents(communityId, building, null);
        Map<String, RoomAggregate> aggMap = aggregateByRoom(residents);

        Map<String, Map<String, List<RoomAggregate>>> unitTree =
                new TreeMap<>(Comparator.nullsFirst(Comparator.naturalOrder()));
        for (RoomAggregate agg : aggMap.values()) {
            String floorKey = parseFloor(agg.room);
            unitTree.computeIfAbsent(agg.unit != null ? agg.unit : "",
                            k -> new TreeMap<>(Comparator.nullsFirst(Comparator.naturalOrder())))
                    .computeIfAbsent(floorKey, k -> new ArrayList<>())
                    .add(agg);
        }
        List<RoomStructureResponse.UnitStructure> unitList = new ArrayList<>();
        for (Map.Entry<String, Map<String, List<RoomAggregate>>> unitEntry : unitTree.entrySet()) {
            List<RoomStructureResponse.FloorStructure> floorList = new ArrayList<>();
            for (Map.Entry<String, List<RoomAggregate>> floorEntry : unitEntry.getValue().entrySet()) {
                List<RoomStructureResponse.RoomInfo> roomList = new ArrayList<>();
                for (RoomAggregate agg : floorEntry.getValue()) {
                    roomList.add(RoomStructureResponse.RoomInfo.builder()
                            .room(agg.room)
                            .isOccupied(agg.activeCount > 0)
                            .residentName(agg.activeResidentName)
                            .status(agg.activeCount > 0 ? STATUS_OCCUPIED : STATUS_VACANT)
                            .build());
                }
                roomList.sort(Comparator.comparing(RoomStructureResponse.RoomInfo::getRoom,
                        Comparator.nullsLast(Comparator.naturalOrder())));
                floorList.add(RoomStructureResponse.FloorStructure.builder()
                        .floor(floorEntry.getKey())
                        .rooms(roomList)
                        .build());
            }
            unitList.add(RoomStructureResponse.UnitStructure.builder()
                    .unit(unitEntry.getKey())
                    .floors(floorList)
                    .build());
        }
        return RoomStructureResponse.builder()
                .communityId(communityId)
                .building(building)
                .units(unitList)
                .build();
    }

    private List<Resident> loadResidents(String communityId, String building, String unit) {
        Specification<Resident> spec = com.wuye.points.repository.spec.ResidentSpecifications.withFilters(
                null, null, communityId, building, null, null, null, null);
        if (StringUtils.hasText(unit)) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("unit"), unit));
        }
        return residentRepository.findAll(spec);
    }

    private Map<String, RoomAggregate> aggregateByRoom(List<Resident> residents) {
        Map<String, RoomAggregate> map = new LinkedHashMap<>();
        for (Resident r : residents) {
            String building = r.getBuilding();
            String unit = r.getUnit();
            String room = r.getRoom();
            if (!StringUtils.hasText(building) || !StringUtils.hasText(room)) {
                continue;
            }
            String key = building + "|" + (unit != null ? unit : "") + "|" + room;
            RoomAggregate agg = map.computeIfAbsent(key, k -> new RoomAggregate(building, unit, room));
            if ("active".equalsIgnoreCase(r.getStatus())) {
                agg.activeCount++;
                if (agg.activeResidentName == null) {
                    agg.activeResidentName = r.getName();
                }
            }
        }
        return map;
    }

    private static class RoomAggregate {
        final String building;
        final String unit;
        final String room;
        int activeCount = 0;
        String activeResidentName;

        RoomAggregate(String building, String unit, String room) {
            this.building = building;
            this.unit = unit;
            this.room = room;
        }
    }
}
