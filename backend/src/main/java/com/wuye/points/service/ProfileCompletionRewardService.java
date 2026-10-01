package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.profilereward.CreateRewardConfigRequest;
import com.wuye.points.dto.profilereward.RewardConfigItemResponse;
import com.wuye.points.dto.profilereward.RewardConfigListResponse;
import com.wuye.points.dto.profilereward.StatsShareResponse;
import com.wuye.points.dto.profilereward.TriggerRewardResponse;
import com.wuye.points.dto.profilereward.UpdateRewardConfigRequest;
import com.wuye.points.entity.ProfileCompletionReward;
import com.wuye.points.entity.ProfileRewardRecord;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.ProfileCompletionRewardRepository;
import com.wuye.points.repository.ProfileRewardRecordRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 信息采集积分激励服务
 */
@Service
@RequiredArgsConstructor
public class ProfileCompletionRewardService {

    private static final String SOURCE_MANUAL = "manual";
    private static final String STATUS_ACTIVE = "active";
    private static final String STATUS_INACTIVE = "inactive";

    /**
     * 家庭完善聚合奖励伪字段：非真实住户字段，触发时校验家庭信息是否完善。
     * 完善判定：有家庭 + 手机号 + 生日 + 是否有子女 + 楼栋/单元/房号均已填写。
     */
    public static final String FIELD_FAMILY_COMPLETED = "family_completed";

    private static final Map<String, Integer> SCOPE_PRIORITY = Map.of(
            "community", 4,
            "property_company", 3,
            "coordinator", 2,
            "platform", 1);

    /** 统计分享 H5 页面基础地址（管理端统计页分享用） */
    @Value("${app.share.stats-url:https://test.yuanxusoftware.top/h5/stats}")
    private String statsShareBaseUrl;

    private final ProfileCompletionRewardRepository rewardConfigRepository;
    private final ProfileRewardRecordRepository rewardRecordRepository;
    private final ResidentRepository residentRepository;
    private final PointService pointService;
    private final RoleGuard roleGuard;
    private final SnowflakeIdGenerator idGenerator;

    /** 自注入以通过代理调用 triggerReward，确保每个字段独立事务 */
    @Lazy
    @org.springframework.beans.factory.annotation.Autowired
    private ProfileCompletionRewardService self;

    @Transactional(readOnly = true)
    public RewardConfigListResponse listConfigs(String scope, String scopeId) {
        roleGuard.requireAnyRole("property_admin", "platform_admin", "coordinator");
        // 统筹仅可查看本物业的 coordinator 作用域配置
        final boolean coordinator = roleGuard.isCoordinator();
        final String queryScope = coordinator ? "coordinator" : scope;
        final String queryScopeId = coordinator ? UserContext.currentPropertyCompanyId() : scopeId;
        List<ProfileCompletionReward> configs;
        if (StringUtils.hasText(queryScope) && StringUtils.hasText(queryScopeId)) {
            configs = rewardConfigRepository.findByScopeAndScopeId(queryScope, queryScopeId);
        } else if (StringUtils.hasText(queryScope)) {
            configs = rewardConfigRepository.findAll().stream()
                    .filter(c -> queryScope.equals(c.getScope()))
                    .toList();
        } else {
            configs = rewardConfigRepository.findAll();
        }
        List<RewardConfigItemResponse> items = configs.stream()
                .map(this::toConfigItem)
                .toList();
        return RewardConfigListResponse.builder()
                .list(items)
                .total(items.size())
                .build();
    }

    @Transactional
    public RewardConfigItemResponse createConfig(CreateRewardConfigRequest request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin", "coordinator");
        validateScope(request.getScope());
        if ("platform".equals(request.getScope()) && !roleGuard.isPlatformAdmin()) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "仅平台管理员可创建平台级奖励配置");
        }
        if (roleGuard.isCoordinator()) {
            if (!"coordinator".equals(request.getScope())) {
                throw new BizException(ErrorCode.PERMISSION_DENIED, "统筹仅可创建 coordinator 作用域配置");
            }
            String pcId = UserContext.currentPropertyCompanyId();
            if (request.getScopeId() != null && !request.getScopeId().equals(pcId)) {
                throw new BizException(ErrorCode.PERMISSION_DENIED, "统筹仅可管理本物业的积分激励配置");
            }
            request.setScopeId(pcId);
        } else if ("property_company".equals(request.getScope()) && !roleGuard.isPlatformAdmin()) {
            String currentPropertyCompanyId = UserContext.currentPropertyCompanyId();
            if (currentPropertyCompanyId == null
                    || !currentPropertyCompanyId.equals(request.getScopeId())) {
                throw new BizException(ErrorCode.PERMISSION_DENIED, "无权创建其他物业的奖励配置");
            }
        }

        ProfileCompletionReward config = new ProfileCompletionReward();
        config.setId(idGenerator.generateId("pcr"));
        config.setScope(request.getScope());
        config.setScopeId(request.getScopeId());
        config.setFieldName(request.getFieldName());
        config.setRewardPoints(request.getRewardPoints());
        config.setOncePerUser(request.getOncePerUser() != null ? request.getOncePerUser() : Boolean.TRUE);
        config.setStatus(STATUS_ACTIVE);
        ProfileCompletionReward saved = rewardConfigRepository.save(config);
        return toConfigItem(saved);
    }

    @Transactional
    public RewardConfigItemResponse updateConfig(String id, UpdateRewardConfigRequest request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin", "coordinator");
        ProfileCompletionReward config = rewardConfigRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.PARAM_INVALID, "奖励配置不存在"));

        if (roleGuard.isCoordinator()) {
            if (!"coordinator".equals(config.getScope())
                    || !UserContext.currentPropertyCompanyId().equals(config.getScopeId())) {
                throw new BizException(ErrorCode.PERMISSION_DENIED, "统筹仅可管理本物业的 coordinator 作用域配置");
            }
        } else if (!roleGuard.isPlatformAdmin()) {
            String currentPropertyCompanyId = UserContext.currentPropertyCompanyId();
            if (!"property_company".equals(config.getScope())
                    || currentPropertyCompanyId == null
                    || !currentPropertyCompanyId.equals(config.getScopeId())) {
                throw new BizException(ErrorCode.PERMISSION_DENIED, "无权修改该奖励配置");
            }
        }

        if (request.getRewardPoints() != null) {
            config.setRewardPoints(request.getRewardPoints());
        }
        if (request.getOncePerUser() != null) {
            config.setOncePerUser(request.getOncePerUser());
        }
        if (StringUtils.hasText(request.getStatus())) {
            if (!STATUS_ACTIVE.equals(request.getStatus()) && !STATUS_INACTIVE.equals(request.getStatus())) {
                throw new BizException(ErrorCode.PARAM_INVALID, "status值无效");
            }
            config.setStatus(request.getStatus());
        }
        ProfileCompletionReward saved = rewardConfigRepository.save(config);
        return toConfigItem(saved);
    }

    @Transactional
    public void deleteConfig(String id) {
        roleGuard.requireAnyRole("property_admin", "platform_admin", "coordinator");
        ProfileCompletionReward config = rewardConfigRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.PARAM_INVALID, "奖励配置不存在"));

        if (roleGuard.isCoordinator()) {
            if (!"coordinator".equals(config.getScope())
                    || !UserContext.currentPropertyCompanyId().equals(config.getScopeId())) {
                throw new BizException(ErrorCode.PERMISSION_DENIED, "统筹仅可删除本物业的 coordinator 作用域配置");
            }
        } else if (!roleGuard.isPlatformAdmin()) {
            String currentPropertyCompanyId = UserContext.currentPropertyCompanyId();
            if (!"property_company".equals(config.getScope())
                    || currentPropertyCompanyId == null
                    || !currentPropertyCompanyId.equals(config.getScopeId())) {
                throw new BizException(ErrorCode.PERMISSION_DENIED, "无权删除该奖励配置");
            }
        }
        rewardConfigRepository.delete(config);
    }

    @Transactional
    public TriggerRewardResponse triggerReward(String residentId, String fieldName) {
        if (!StringUtils.hasText(fieldName)) {
            throw new BizException(ErrorCode.PARAM_MISSING, "字段名不能为空");
        }
        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));

        ProfileCompletionReward config = findApplicableConfig(resident, fieldName);
        if (config == null) {
            return TriggerRewardResponse.builder()
                    .fieldName(fieldName)
                    .rewardedPoints(0)
                    .message("未找到可用的奖励配置")
                    .build();
        }

        if (!STATUS_ACTIVE.equals(config.getStatus())) {
            return TriggerRewardResponse.builder()
                    .fieldName(fieldName)
                    .rewardedPoints(0)
                    .message("该奖励配置已停用")
                    .build();
        }

        // 家庭完善聚合奖励：先校验家庭信息是否完善，未完善不发奖
        if (FIELD_FAMILY_COMPLETED.equals(fieldName) && !isFamilyInfoCompleted(resident)) {
            return TriggerRewardResponse.builder()
                    .fieldName(fieldName)
                    .rewardedPoints(0)
                    .message("家庭信息未完善，无法领取")
                    .build();
        }

        boolean oncePerUser = !Boolean.FALSE.equals(config.getOncePerUser());
        // 仅 oncePerUser=true 时检查是否已领取；oncePerUser=false 允许多条不同 config_id 的记录
        if (oncePerUser && rewardRecordRepository.existsByResidentIdAndFieldName(residentId, fieldName)) {
            return TriggerRewardResponse.builder()
                    .fieldName(fieldName)
                    .rewardedPoints(0)
                    .message("该字段奖励已领取")
                    .build();
        }

        int rewardPoints = config.getRewardPoints() != null ? config.getRewardPoints() : 0;
        if (rewardPoints <= 0) {
            return TriggerRewardResponse.builder()
                    .fieldName(fieldName)
                    .rewardedPoints(0)
                    .message("奖励积分配置为0")
                    .build();
        }

        // 先写记录占位：save 失败（唯一约束冲突）直接返回「已领取」，不发积分
        ProfileRewardRecord record = new ProfileRewardRecord();
        record.setId(idGenerator.generateId("prr"));
        record.setResidentId(residentId);
        record.setFieldName(fieldName);
        record.setRewardPoints(rewardPoints);
        record.setRewardConfigId(config.getId());
        try {
            rewardRecordRepository.save(record);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            return TriggerRewardResponse.builder()
                    .fieldName(fieldName)
                    .rewardedPoints(0)
                    .message("该字段奖励已领取")
                    .build();
        }

        // save 成功才发积分
        pointService.earnPoints(
                resident,
                rewardPoints,
                SOURCE_MANUAL,
                config.getId(),
                "信息采集奖励: " + fieldName,
                residentId);

        return TriggerRewardResponse.builder()
                .fieldName(fieldName)
                .rewardedPoints(rewardPoints)
                .message("奖励已发放")
                .build();
    }

    /** 批量触发奖励，每个字段独立事务（通过 self 代理调用），单字段失败不影响其他字段 */
    public List<TriggerRewardResponse> triggerBatch(String residentId, List<String> fieldNames) {
        if (fieldNames == null || fieldNames.isEmpty()) {
            return List.of();
        }
        return fieldNames.stream()
                .map(fn -> {
                    try {
                        return self.triggerReward(residentId, fn);
                    } catch (Exception e) {
                        return TriggerRewardResponse.builder()
                                .fieldName(fn)
                                .rewardedPoints(0)
                                .message("触发失败：" + e.getMessage())
                                .build();
                    }
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TriggerRewardResponse> listMyRewards() {
        roleGuard.requireAnyRole("resident");
        String currentUserId = UserContext.currentUserId();
        if (currentUserId == null) {
            throw new BizException(ErrorCode.TOKEN_MISSING);
        }
        List<ProfileRewardRecord> records = rewardRecordRepository.findByResidentId(currentUserId);
        return records.stream()
                .map(record -> TriggerRewardResponse.builder()
                        .fieldName(record.getFieldName())
                        .rewardedPoints(record.getRewardPoints())
                        .message(DateTimeUtil.toIsoUtc(record.getCreatedAt()))
                        .build())
                .toList();
    }

    /**
     * 查找居民适用的奖励配置（按 scope 优先级：community > property_company > coordinator > platform）
     */
    private ProfileCompletionReward findApplicableConfig(Resident resident, String fieldName) {
        List<ProfileCompletionReward> configs = rewardConfigRepository.findByFieldName(fieldName);
        if (configs.isEmpty()) {
            return null;
        }
        return configs.stream()
                .filter(c -> isApplicable(c, resident))
                .max(Comparator.comparingInt(c -> SCOPE_PRIORITY.getOrDefault(c.getScope(), 0)))
                .orElse(null);
    }

    private boolean isApplicable(ProfileCompletionReward config, Resident resident) {
        String scope = config.getScope();
        String scopeId = config.getScopeId();
        if (scope == null) {
            return false;
        }
        return switch (scope) {
            case "community" -> StringUtils.hasText(resident.getCommunityId())
                    && resident.getCommunityId().equals(scopeId);
            case "property_company", "coordinator" -> StringUtils.hasText(resident.getPropertyCompanyId())
                    && resident.getPropertyCompanyId().equals(scopeId);
            case "platform" -> true;
            default -> false;
        };
    }

    /**
     * 家庭信息完善判定：有家庭 + 手机号 + 生日 + 是否有子女 + 地址（楼栋/单元/房号）均已填写。
     */
    private boolean isFamilyInfoCompleted(Resident resident) {
        if (!StringUtils.hasText(resident.getFamilyId())) {
            return false;
        }
        if (!StringUtils.hasText(resident.getPhone())) {
            return false;
        }
        if (resident.getBirthday() == null) {
            return false;
        }
        if (resident.getHasChildren() == null) {
            return false;
        }
        return StringUtils.hasText(resident.getBuilding())
                && StringUtils.hasText(resident.getUnit())
                && StringUtils.hasText(resident.getRoom());
    }

    /**
     * 生成信息采集统计分享链接（管理端）。
     * 仅 property_admin / platform_admin 可调用，物业管理员自动限定本物业，平台管理员可指定物业。
     */
    @Transactional(readOnly = true)
    public StatsShareResponse buildStatsShare(String propertyCompanyId) {
        roleGuard.requireAnyRole("property_admin", "platform_admin", "coordinator");
        String scoped = roleGuard.resolveListPropertyCompanyId(propertyCompanyId);
        String query = StringUtils.hasText(scoped) ? "?propertyCompanyId=" + scoped : "";
        String shareUrl = statsShareBaseUrl + query;
        return StatsShareResponse.builder()
                .shareUrl(shareUrl)
                .posterImageUrl(statsShareBaseUrl.replace("/h5/stats", "/api/v1/static/posters/stats.png"))
                .qrcodeUrl(shareUrl + (query.isEmpty() ? "?format=qrcode" : "&format=qrcode"))
                .build();
    }

    private void validateScope(String scope) {
        if (!StringUtils.hasText(scope)) {
            throw new BizException(ErrorCode.PARAM_MISSING, "scope不能为空");
        }
        if (!SCOPE_PRIORITY.containsKey(scope)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "scope值无效");
        }
    }

    private RewardConfigItemResponse toConfigItem(ProfileCompletionReward config) {
        return RewardConfigItemResponse.builder()
                .id(config.getId())
                .scope(config.getScope())
                .scopeId(config.getScopeId())
                .fieldName(config.getFieldName())
                .rewardPoints(config.getRewardPoints())
                .oncePerUser(config.getOncePerUser())
                .status(config.getStatus())
                .createdAt(DateTimeUtil.toIsoUtc(config.getCreatedAt()))
                .updatedAt(DateTimeUtil.toIsoUtc(config.getUpdatedAt()))
                .build();
    }
}
