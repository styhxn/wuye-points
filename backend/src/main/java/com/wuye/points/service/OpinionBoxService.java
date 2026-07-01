package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.opinion.CreateOpinionBoxRequest;
import com.wuye.points.dto.opinion.OpinionBoxDetailResponse;
import com.wuye.points.dto.opinion.OpinionBoxListItemResponse;
import com.wuye.points.dto.opinion.OpinionBoxResultResponse;
import com.wuye.points.dto.opinion.VoteOpinionBoxRequest;
import com.wuye.points.dto.opinion.VoteOpinionBoxResponse;
import com.wuye.points.entity.OpinionBox;
import com.wuye.points.entity.Resident;
import com.wuye.points.entity.VoteRecord;
import com.wuye.points.enums.ResidentStatus;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.OpinionBoxRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.VoteRecordRepository;
import com.wuye.points.repository.spec.OpinionBoxSpecifications;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OpinionBoxService {

    private static final ZoneId ZONE_SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final Set<String> SORTABLE_FIELDS = Set.of("createdAt", "endTime", "totalVotes");

    private final OpinionBoxRepository opinionBoxRepository;
    private final VoteRecordRepository voteRecordRepository;
    private final ResidentRepository residentRepository;
    private final OpinionBoxConverter opinionBoxConverter;
    private final RoleGuard roleGuard;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional(readOnly = true)
    public PageResponse<OpinionBoxListItemResponse> listOpinionBoxes(
            int page,
            int pageSize,
            String status,
            Boolean mine,
            Boolean voted,
            String sort) {
        roleGuard.requireAnyRole("resident");
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        validateStatus(status);

        Resident resident = getActiveResident(UserContext.currentUserId());
        requireFamily(resident);

        String publisherId = Boolean.TRUE.equals(mine) ? resident.getId() : null;
        String votedFamilyId = Boolean.TRUE.equals(voted) ? resident.getFamilyId() : null;

        Pageable pageable = buildPageable(page, pageSize, sort);
        Specification<OpinionBox> spec = OpinionBoxSpecifications.withFilters(
                resident.getPropertyCompanyId(),
                status,
                publisherId,
                votedFamilyId);

        Page<OpinionBox> boxPage = opinionBoxRepository.findAll(spec, pageable);
        return buildListPage(boxPage, page, pageSize, resident);
    }

    @Transactional(readOnly = true)
    public OpinionBoxDetailResponse getOpinionBox(String id) {
        roleGuard.requireAnyRole("resident");
        validateOpinionBoxId(id);

        Resident resident = getActiveResident(UserContext.currentUserId());
        OpinionBox box = getReadableBox(id, resident);
        return toDetail(box, resident);
    }

    @Transactional(readOnly = true)
    public OpinionBoxResultResponse getOpinionBoxResult(String id) {
        roleGuard.requireAnyRole("resident");
        validateOpinionBoxId(id);

        Resident resident = getActiveResident(UserContext.currentUserId());
        OpinionBox box = getReadableBox(id, resident);
        opinionBoxConverter.refreshStoredStatus(box);

        String resolvedStatus = opinionBoxConverter.resolveStatus(box);
        if (!Boolean.TRUE.equals(box.getResultPublished()) && !"ended".equals(resolvedStatus)) {
            throw new BizException(ErrorCode.VOTE_ENDED, "投票结果尚未公布");
        }

        return opinionBoxConverter.toResult(box);
    }

    @Transactional
    public OpinionBoxDetailResponse createOpinionBox(CreateOpinionBoxRequest request) {
        roleGuard.requireAnyRole("resident");

        Resident resident = getActiveResident(UserContext.currentUserId());
        requireFamily(resident);
        assertMonthlyLimit(resident.getFamilyId());

        LocalDateTime startTime = StringUtils.hasText(request.getStartTime())
                ? parseIsoDateTime(request.getStartTime())
                : LocalDateTime.now();
        LocalDateTime endTime = parseIsoDateTime(request.getEndTime());
        if (!endTime.isAfter(startTime)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "结束时间必须晚于开始时间");
        }

        OpinionBox box = new OpinionBox();
        box.setId(idGenerator.generateId("ob"));
        box.setTitle(request.getTitle().trim());
        box.setContent(request.getContent().trim());
        box.setPublisherId(resident.getId());
        box.setFamilyId(resident.getFamilyId());
        box.setPropertyCompanyId(resident.getPropertyCompanyId());
        box.setStartTime(startTime);
        box.setEndTime(endTime);
        box.setTotalVotes(0);
        box.setSupportCount(0);
        box.setOpposeCount(0);
        box.setAbstainCount(0);
        box.setStatus("active");
        box.setResultPublished(false);
        opinionBoxRepository.save(box);

        return toDetail(box, resident);
    }

    @Transactional
    public VoteOpinionBoxResponse vote(String id, VoteOpinionBoxRequest request) {
        roleGuard.requireAnyRole("resident");
        validateOpinionBoxId(id);
        validateVoteOption(request.getVoteOption());

        Resident resident = getActiveResident(UserContext.currentUserId());
        requireFamily(resident);

        OpinionBox box = opinionBoxRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.OPINION_BOX_NOT_FOUND));
        assertSameProperty(resident, box);
        assertVoteOpen(box);

        if (voteRecordRepository.existsByOpinionBoxIdAndFamilyId(id, resident.getFamilyId())) {
            throw new BizException(ErrorCode.OPINION_ALREADY_VOTED);
        }

        VoteRecord record = new VoteRecord();
        record.setId(idGenerator.generateId("vr"));
        record.setOpinionBoxId(box.getId());
        record.setResidentId(resident.getId());
        record.setFamilyId(resident.getFamilyId());
        record.setVoteOption(request.getVoteOption());

        try {
            voteRecordRepository.save(record);
        } catch (DataIntegrityViolationException ex) {
            throw new BizException(ErrorCode.OPINION_ALREADY_VOTED);
        }

        incrementVoteCount(box, request.getVoteOption());
        opinionBoxConverter.refreshStoredStatus(box);
        opinionBoxRepository.save(box);

        return VoteOpinionBoxResponse.builder()
                .voteRecordId(record.getId())
                .opinionBoxId(box.getId())
                .familyId(resident.getFamilyId())
                .voteOption(record.getVoteOption())
                .totalVotes(defaultCount(box.getTotalVotes()))
                .supportCount(defaultCount(box.getSupportCount()))
                .opposeCount(defaultCount(box.getOpposeCount()))
                .abstainCount(defaultCount(box.getAbstainCount()))
                .votedAt(DateTimeUtil.toIsoUtc(record.getCreatedAt()))
                .build();
    }

    private PageResponse<OpinionBoxListItemResponse> buildListPage(Page<OpinionBox> boxPage,
                                                                     int page,
                                                                     int pageSize,
                                                                     Resident resident) {
        List<OpinionBox> boxes = boxPage.getContent();
        Map<String, Resident> publisherMap = loadPublishers(boxes);
        Map<String, VoteRecord> voteMap = loadFamilyVotes(boxes, resident.getFamilyId());

        List<OpinionBoxListItemResponse> list = boxes.stream()
                .map(box -> {
                    opinionBoxConverter.refreshStoredStatus(box);
                    return opinionBoxConverter.toListItem(
                            box,
                            publisherMap.get(box.getPublisherId()),
                            voteMap.get(box.getId()),
                            false);
                })
                .toList();

        return PageResponse.of(list, page, pageSize, boxPage.getTotalElements());
    }

    private OpinionBoxDetailResponse toDetail(OpinionBox box, Resident viewer) {
        Resident publisher = residentRepository.findById(box.getPublisherId()).orElse(null);
        VoteRecord familyVote = StringUtils.hasText(viewer.getFamilyId())
                ? voteRecordRepository.findByOpinionBoxIdAndFamilyId(box.getId(), viewer.getFamilyId()).orElse(null)
                : null;
        opinionBoxConverter.refreshStoredStatus(box);
        return opinionBoxConverter.toDetail(box, publisher, familyVote);
    }

    private Map<String, Resident> loadPublishers(List<OpinionBox> boxes) {
        List<String> publisherIds = boxes.stream()
                .map(OpinionBox::getPublisherId)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        if (publisherIds.isEmpty()) {
            return Map.of();
        }
        return residentRepository.findAllById(publisherIds).stream()
                .collect(Collectors.toMap(Resident::getId, Function.identity()));
    }

    private Map<String, VoteRecord> loadFamilyVotes(List<OpinionBox> boxes, String familyId) {
        if (!StringUtils.hasText(familyId) || boxes.isEmpty()) {
            return Map.of();
        }
        List<String> boxIds = boxes.stream().map(OpinionBox::getId).toList();
        return voteRecordRepository.findByOpinionBoxIdInAndFamilyId(boxIds, familyId).stream()
                .collect(Collectors.toMap(VoteRecord::getOpinionBoxId, Function.identity()));
    }

    private OpinionBox getReadableBox(String id, Resident resident) {
        OpinionBox box = opinionBoxRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.OPINION_BOX_NOT_FOUND));
        assertSameProperty(resident, box);
        return box;
    }

    private void assertSameProperty(Resident resident, OpinionBox box) {
        if (resident.getPropertyCompanyId() == null
                || !resident.getPropertyCompanyId().equals(box.getPropertyCompanyId())) {
            throw new BizException(ErrorCode.OPINION_BOX_NOT_FOUND);
        }
    }

    private void assertVoteOpen(OpinionBox box) {
        if ("closed".equals(box.getStatus())) {
            throw new BizException(ErrorCode.VOTE_ENDED);
        }
        LocalDateTime now = LocalDateTime.now();
        if (box.getStartTime() != null && box.getStartTime().isAfter(now)) {
            throw new BizException(ErrorCode.VOTE_ENDED, "投票尚未开始");
        }
        if (box.getEndTime() != null && !box.getEndTime().isAfter(now)) {
            throw new BizException(ErrorCode.VOTE_ENDED);
        }
    }

    private void assertMonthlyLimit(String familyId) {
        LocalDate today = LocalDate.now(ZONE_SHANGHAI);
        LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime monthEnd = today.plusMonths(1).withDayOfMonth(1).atStartOfDay();
        long count = opinionBoxRepository.countByFamilyIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                familyId, monthStart, monthEnd);
        if (count >= 1) {
            throw new BizException(ErrorCode.OPINION_MONTHLY_LIMIT);
        }
    }

    private void requireFamily(Resident resident) {
        if (!StringUtils.hasText(resident.getFamilyId())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "未加入家庭，无法操作意见箱");
        }
    }

    private void incrementVoteCount(OpinionBox box, String voteOption) {
        box.setTotalVotes(defaultCount(box.getTotalVotes()) + 1);
        switch (voteOption) {
            case "support" -> box.setSupportCount(defaultCount(box.getSupportCount()) + 1);
            case "oppose" -> box.setOpposeCount(defaultCount(box.getOpposeCount()) + 1);
            case "abstain" -> box.setAbstainCount(defaultCount(box.getAbstainCount()) + 1);
            default -> throw new BizException(ErrorCode.PARAM_INVALID, "voteOption值无效");
        }
    }

    private void validateOpinionBoxId(String id) {
        if (!StringUtils.hasText(id) || !id.startsWith("ob_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "意见箱ID格式不正确");
        }
    }

    private void validateStatus(String status) {
        if (StringUtils.hasText(status) && !RoleGuard.OPINION_BOX_STATUS.contains(status)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "status值无效");
        }
    }

    private void validateVoteOption(String voteOption) {
        if (!StringUtils.hasText(voteOption) || !RoleGuard.VOTE_OPTIONS.contains(voteOption)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "voteOption值无效");
        }
    }

    private Resident getActiveResident(String userId) {
        if (userId == null) {
            throw new BizException(ErrorCode.TOKEN_MISSING);
        }
        Resident resident = residentRepository.findById(userId)
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        if (ResidentStatus.disabled.name().equals(resident.getStatus())
                || ResidentStatus.frozen.name().equals(resident.getStatus())) {
            throw new BizException(ErrorCode.ACCOUNT_DISABLED);
        }
        return resident;
    }

    private LocalDateTime parseIsoDateTime(String value) {
        if (!StringUtils.hasText(value)) {
            throw new BizException(ErrorCode.PARAM_MISSING);
        }
        try {
            return LocalDateTime.ofInstant(Instant.parse(value), ZONE_SHANGHAI);
        } catch (Exception ex) {
            throw new BizException(ErrorCode.PARAM_INVALID, "时间格式不正确");
        }
    }

    private Pageable buildPageable(int page, int pageSize, String sort) {
        int safePage = Math.max(page, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), 100);
        return PageRequest.of(safePage - 1, safePageSize, parseSort(sort));
    }

    private Sort parseSort(String sort) {
        if (!StringUtils.hasText(sort)) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        Sort.Direction direction = Sort.Direction.DESC;
        String field = sort;
        if (sort.startsWith("-")) {
            field = sort.substring(1);
            direction = Sort.Direction.DESC;
        } else if (sort.startsWith("+")) {
            field = sort.substring(1);
            direction = Sort.Direction.ASC;
        }
        if (!SORTABLE_FIELDS.contains(field)) {
            field = "createdAt";
            direction = Sort.Direction.DESC;
        }
        return Sort.by(direction, field);
    }

    private int defaultCount(Integer value) {
        return value != null ? value : 0;
    }
}
