package com.wuye.points.service;

import com.wuye.points.entity.Family;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.enums.UserType;
import com.wuye.points.repository.FamilyRepository;
import com.wuye.points.repository.ResidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 房号归属校验：一个房号只允许一个家庭。
 *
 * <p>2026-11-06 前端反馈「住户乙无法申请入住甲所在的 4-4-401，而添加家庭成员又要求同房号，
 * 设计冲突」。老板定的口径是<b>乙由甲添加为家庭成员，而不是自己申请入住</b>。要让这条口径成立，
 * 占用判定必须落到后端：此前只有前端选择器（{@code GET /communities/{id}/available-rooms}）在挡，
 * {@code POST /families} 与楼栋变更审批都不查占用，绕过去或并发就能造出两个家庭同房号。</p>
 *
 * <p>放行两类「不算占用」：① 申请人本人；② 已属于同一家庭的成员（同房号本就该是一家人）。
 * 其余同房号的 active 家庭或住户一律拒绝，并把「请由户主添加你为家庭成员」写进报错文案，
 * 让用户知道该走哪条路，而不是只看到「房号不可选」。</p>
 */
@Component
@RequiredArgsConstructor
public class RoomOccupancyGuard {

    /** 冷静期（pending_deletion）账号仍可登录使用，占用判定继续算数，避免到期前被他人抢注。 */
    private static final List<String> OCCUPYING_STATUSES = List.of("active", "pending_deletion");

    private final FamilyRepository familyRepository;
    private final ResidentRepository residentRepository;

    /**
     * @param selfFamilyId    变更场景传当前家庭ID（本家庭成员不算占用）；新建家庭传 null
     * @param selfResidentId  申请人住户ID，本人不算占用
     */
    public void assertRoomNotTaken(String communityId, String building, String unit, String room,
                                   String selfFamilyId, String selfResidentId) {
        if (!StringUtils.hasText(communityId) || !StringUtils.hasText(building) || !StringUtils.hasText(room)) {
            return; // 地址不完整时不判占用，交由各接口自身的必填校验处理
        }

        for (Family other : familyRepository
                .findByCommunityIdAndBuildingAndRoomAndStatusIn(communityId, building, room, OCCUPYING_STATUSES)) {
            if ((selfFamilyId != null && selfFamilyId.equals(other.getId())) || !sameUnit(other.getUnit(), unit)) {
                continue;
            }
            throw new BizException(ErrorCode.PARAM_INVALID, familyOccupiedMessage(other.getOwnerId()));
        }

        for (Resident other : residentRepository
                .findByCommunityIdAndBuildingAndRoomAndStatusIn(communityId, building, room, OCCUPYING_STATUSES)) {
            if (sameResident(other, selfResidentId, selfFamilyId) || !sameUnit(other.getUnit(), unit)) {
                continue;
            }
            // 只有租户构成占用：租户不能建家庭、也不能被添加为家庭成员，families 表表达不了这种占用。
            // 无家庭的普通住户「不算占用」——否则同一房号里的两个人会互相挡住谁都无法建家庭（死锁），
            // 正确路径是先建家庭的人成为户主，另一人由户主添加为成员。
            if (UserType.tenant.name().equals(other.getUserType())) {
                throw new BizException(ErrorCode.PARAM_INVALID,
                        "该房号当前由租住人员居住（" + displayName(other.getName()) + "），不能同时登记业主家庭");
            }
        }
    }

    private boolean sameResident(Resident other, String selfResidentId, String selfFamilyId) {
        if (other.getId().equals(selfResidentId)) {
            return true;
        }
        return selfFamilyId != null && selfFamilyId.equals(other.getFamilyId());
    }

    /** 单元号可能为空串/空格/历史 null，统一按空值等价处理，避免判重时漏掉同房号。 */
    private boolean sameUnit(String left, String right) {
        String l = StringUtils.hasText(left) ? left.trim() : "";
        String r = StringUtils.hasText(right) ? right.trim() : "";
        return l.equals(r);
    }

    private String familyOccupiedMessage(String ownerResidentId) {
        String ownerName = residentRepository.findById(ownerResidentId)
                .map(Resident::getName)
                .map(this::displayName)
                .orElse("该住户");
        return "该房号已有家庭（户主：" + ownerName + "）。如同一住址，请由户主在「家庭成员」中添加你，"
                + "不要再单独申请入住";
    }

    private String displayName(String name) {
        return StringUtils.hasText(name) ? name : "该住户";
    }
}
