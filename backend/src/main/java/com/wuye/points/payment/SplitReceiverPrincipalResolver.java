package com.wuye.points.payment;

import com.wuye.points.entity.BuildingLeader;
import com.wuye.points.entity.Coordinator;
import com.wuye.points.entity.CourierManager;
import com.wuye.points.entity.IndividualLeader;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.Resident;
import com.wuye.points.entity.SectorLeader;
import com.wuye.points.repository.BuildingLeaderRepository;
import com.wuye.points.repository.CoordinatorRepository;
import com.wuye.points.repository.CourierManagerRepository;
import com.wuye.points.repository.IndividualLeaderRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.SectorLeaderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Set;

/**
 * 分账个人接收方主体 → 住户账号解析器。
 *
 * <p>分账记录里的 {@code owner_id} 取的是<b>各角色表主键</b>（{@code coo_} / {@code sl_} / {@code il_} /
 * 楼长表 ID / 快递负责人表 ID），不是 {@code residents.id}；只有 {@code RESIDENT} 与
 * {@code PROPERTY_MANAGER} 两种直接用住户 ID。openid 只存在 {@code residents} 上，
 * 所以「按主体自动取 openid」必须先做这一层映射，映射错了就会把分账打到别人的零钱里。</p>
 */
@Component
@RequiredArgsConstructor
public class SplitReceiverPrincipalResolver {

    /** 可用微信 openid 登记为个人接收方的主体类型。 */
    private static final Set<String> PERSONAL_OWNER_TYPES = Set.of(
            "RESIDENT", "COORDINATOR", "SECTOR_LEADER", "INDIVIDUAL_LEADER",
            "BUILDING_LEADER", "COURIER", "PROPERTY_MANAGER", "MERCHANT");

    private final ResidentRepository residentRepository;
    private final CoordinatorRepository coordinatorRepository;
    private final SectorLeaderRepository sectorLeaderRepository;
    private final IndividualLeaderRepository individualLeaderRepository;
    private final BuildingLeaderRepository buildingLeaderRepository;
    private final CourierManagerRepository courierManagerRepository;
    private final MerchantRepository merchantRepository;

    public boolean supportsPersonal(String ownerType) {
        return ownerType != null && PERSONAL_OWNER_TYPES.contains(ownerType);
    }

    /** 该主体类型如何承载身份：用于把「主体不存在」与「没认领微信」两种原因分开提示。 */
    public String describe(String ownerType) {
        return switch (ownerType == null ? "" : ownerType) {
            case "RESIDENT" -> "住户";
            case "COORDINATOR" -> "统筹负责人";
            case "SECTOR_LEADER" -> "板块负责人";
            case "INDIVIDUAL_LEADER" -> "个体负责人";
            case "BUILDING_LEADER" -> "单元楼长";
            case "COURIER" -> "快递负责人";
            case "PROPERTY_MANAGER" -> "物业经理";
            case "MERCHANT" -> "商家店主";
            default -> ownerType;
        };
    }

    /**
     * 解析主体对应的住户账号 ID；主体记录不存在或尚未指向住户返回 null。
     */
    public String resolveResidentId(String ownerType, String ownerId) {
        if (!StringUtils.hasText(ownerId) || !supportsPersonal(ownerType)) {
            return null;
        }
        return switch (ownerType) {
            case "RESIDENT", "PROPERTY_MANAGER" -> ownerId;
            case "COORDINATOR" -> coordinatorRepository.findById(ownerId)
                    .map(Coordinator::getResidentId).orElse(null);
            case "SECTOR_LEADER" -> sectorLeaderRepository.findById(ownerId)
                    .map(SectorLeader::getResidentId).orElse(null);
            case "INDIVIDUAL_LEADER" -> individualLeaderRepository.findById(ownerId)
                    .map(IndividualLeader::getResidentId).orElse(null);
            case "BUILDING_LEADER" -> buildingLeaderRepository.findById(ownerId)
                    .map(BuildingLeader::getResidentId).orElse(null);
            case "COURIER" -> courierManagerRepository.findById(ownerId)
                    .map(CourierManager::getResidentId).orElse(null);
            case "MERCHANT" -> merchantRepository.findById(ownerId)
                    .map(Merchant::getResidentId).orElse(null);
            default -> null;
        };
    }

    /** 载入主体的住户账号；主体缺失或未指向住户时返回 null。 */
    public Resident loadResident(String ownerType, String ownerId) {
        String residentId = resolveResidentId(ownerType, ownerId);
        if (!StringUtils.hasText(residentId)) {
            return null;
        }
        return residentRepository.findById(residentId).orElse(null);
    }
}
