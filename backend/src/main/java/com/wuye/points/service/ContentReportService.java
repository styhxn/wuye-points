package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.community.ContentReportResponse;
import com.wuye.points.dto.community.CreateContentReportRequest;
import com.wuye.points.dto.community.HandleContentReportRequest;
import com.wuye.points.entity.CommunityComment;
import com.wuye.points.entity.CommunityPost;
import com.wuye.points.entity.ContentReport;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.CommunityCommentRepository;
import com.wuye.points.repository.CommunityPostRepository;
import com.wuye.points.repository.ContentReportRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class ContentReportService {

    private final ContentReportRepository reportRepository;
    private final CommunityPostRepository postRepository;
    private final CommunityCommentRepository commentRepository;
    private final ResidentRepository residentRepository;
    private final SnowflakeIdGenerator idGenerator;
    private final CommunityConverter converter;
    private final RoleGuard roleGuard;

    private static final Set<String> REPORT_REASONS = Set.of("spam", "abuse", "politics", "porn", "other");

    public ContentReportResponse createReport(CreateContentReportRequest req) {
        roleGuard.requireAnyRole("resident");
        Resident resident = getViewerResident();
        if (!"post".equals(req.getTargetType()) && !"comment".equals(req.getTargetType())) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        if (req.getReasonType() != null && !req.getReasonType().isBlank()
                && !REPORT_REASONS.contains(req.getReasonType())) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        if (reportRepository.existsByReporterIdAndTargetTypeAndTargetId(
                resident.getId(), req.getTargetType(), req.getTargetId())) {
            throw new BizException(ErrorCode.COMMUNITY_REPORT_DUPLICATE);
        }
        assertReportableTarget(req.getTargetType(), req.getTargetId(), resident);
        ContentReport report = new ContentReport();
        report.setId(idGenerator.generateId("crp_"));
        report.setReporterId(resident.getId());
        report.setReporterName(resident.getName());
        report.setTargetType(req.getTargetType());
        report.setTargetId(req.getTargetId());
        report.setReasonType(req.getReasonType());
        report.setReasonDetail(req.getReasonDetail());
        report.setStatus("pending");
        fillTargetScope(report);
        return converter.toReportResponse(reportRepository.save(report), null, null);
    }

    public Page<ContentReportResponse> listReports(int page, int pageSize, String status) {
        Resident admin = getViewerResident();
        String company = admin.getPropertyCompanyId();
        Specification<ContentReport> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (company != null) {
                predicates.add(cb.equal(root.get("propertyCompanyId"), company));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Pageable pageable = PageRequest.of(Math.max(0, page - 1),
                Math.min(Math.max(pageSize, 1), 50), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ContentReport> result = reportRepository.findAll(spec, pageable);
        return result.map(r -> converter.toReportResponse(r, targetContent(r), targetAuthorName(r)));
    }

    public ContentReportResponse handleReport(String id, HandleContentReportRequest req) {
        Resident admin = getViewerResident();
        ContentReport report = reportRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.COMMUNITY_REPORT_NOT_FOUND));
        if (admin.getPropertyCompanyId() != null
                && (report.getPropertyCompanyId() == null
                    || !admin.getPropertyCompanyId().equals(report.getPropertyCompanyId()))) {
            throw new BizException(ErrorCode.COMMUNITY_REPORT_NOT_FOUND);
        }
        if (!"pending".equals(report.getStatus())) {
            throw new BizException(ErrorCode.COMMUNITY_REPORT_ALREADY_HANDLED);
        }
        if ("accept".equals(req.getAction())) {
            report.setStatus("accepted");
            hideTarget(report.getTargetType(), report.getTargetId());
        } else if ("reject".equals(req.getAction())) {
            report.setStatus("rejected");
        } else {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        report.setHandlerId(admin.getId());
        report.setHandlerName(admin.getName());
        report.setHandleRemark(req.getHandleRemark());
        report.setHandledAt(LocalDateTime.now());
        return converter.toReportResponse(reportRepository.save(report),
                targetContent(report), targetAuthorName(report));
    }

    /** 校验举报对象存在、处于可见状态且属于当前用户小区 */
    private void assertReportableTarget(String targetType, String targetId, Resident resident) {
        if ("post".equals(targetType)) {
            CommunityPost post = postRepository.findById(targetId)
                    .orElseThrow(() -> new BizException(ErrorCode.COMMUNITY_POST_NOT_FOUND));
            if (!"published".equals(post.getStatus())
                    || post.getCommunityId() == null
                    || !post.getCommunityId().equals(resident.getCommunityId())) {
                throw new BizException(ErrorCode.COMMUNITY_POST_NOT_FOUND);
            }
        } else {
            CommunityComment comment = commentRepository.findById(targetId)
                    .orElseThrow(() -> new BizException(ErrorCode.COMMUNITY_COMMENT_NOT_FOUND));
            CommunityPost post = postRepository.findById(comment.getPostId()).orElse(null);
            if (!"published".equals(comment.getStatus())
                    || post == null
                    || !"published".equals(post.getStatus())
                    || post.getCommunityId() == null
                    || !post.getCommunityId().equals(resident.getCommunityId())) {
                throw new BizException(ErrorCode.COMMUNITY_COMMENT_NOT_FOUND);
            }
        }
    }

    private Resident getViewerResident() {
        return residentRepository.findById(UserContext.currentUserId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
    }

    private void fillTargetScope(ContentReport report) {
        if ("post".equals(report.getTargetType())) {
            postRepository.findById(report.getTargetId()).ifPresent(p -> {
                report.setCommunityId(p.getCommunityId());
                report.setPropertyCompanyId(p.getPropertyCompanyId());
            });
        } else {
            commentRepository.findById(report.getTargetId()).ifPresent(c ->
                    postRepository.findById(c.getPostId()).ifPresent(p -> {
                        report.setCommunityId(p.getCommunityId());
                        report.setPropertyCompanyId(p.getPropertyCompanyId());
                    }));
        }
    }

    private void hideTarget(String type, String targetId) {
        if ("post".equals(type)) {
            postRepository.findById(targetId).ifPresent(p -> {
                p.setStatus("hidden");
                postRepository.save(p);
            });
        } else {
            commentRepository.findById(targetId).ifPresent(c -> {
                c.setStatus("deleted");
                commentRepository.save(c);
                postRepository.findById(c.getPostId()).ifPresent(p -> {
                    p.setCommentCount(Math.max(0, (p.getCommentCount() == null ? 0 : p.getCommentCount()) - 1));
                    postRepository.save(p);
                });
            });
        }
    }

    private String targetContent(ContentReport report) {
        if ("post".equals(report.getTargetType())) {
            return postRepository.findById(report.getTargetId()).map(CommunityPost::getContent).orElse(null);
        }
        return commentRepository.findById(report.getTargetId()).map(CommunityComment::getContent).orElse(null);
    }

    private String targetAuthorName(ContentReport report) {
        if ("post".equals(report.getTargetType())) {
            return postRepository.findById(report.getTargetId()).map(CommunityPost::getAuthorName).orElse(null);
        }
        return commentRepository.findById(report.getTargetId()).map(CommunityComment::getAuthorName).orElse(null);
    }
}
