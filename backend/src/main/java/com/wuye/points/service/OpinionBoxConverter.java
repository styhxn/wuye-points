package com.wuye.points.service;

import com.wuye.points.dto.opinion.OpinionBoxDetailResponse;
import com.wuye.points.dto.opinion.OpinionBoxListItemResponse;
import com.wuye.points.dto.opinion.OpinionBoxResultResponse;
import com.wuye.points.entity.OpinionBox;
import com.wuye.points.entity.Resident;
import com.wuye.points.entity.VoteRecord;
import com.wuye.points.util.DateTimeUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Component
public class OpinionBoxConverter {

    private static final int LIST_CONTENT_LIMIT = 200;

    public OpinionBoxListItemResponse toListItem(OpinionBox box,
                                                 Resident publisher,
                                                 VoteRecord familyVote,
                                                 boolean fullContent) {
        return OpinionBoxListItemResponse.builder()
                .id(box.getId())
                .title(box.getTitle())
                .content(formatContent(box.getContent(), fullContent))
                .publisherId(box.getPublisherId())
                .publisherName(publisher != null ? publisher.getName() : null)
                .familyId(box.getFamilyId())
                .startTime(DateTimeUtil.toIsoUtc(box.getStartTime()))
                .endTime(DateTimeUtil.toIsoUtc(box.getEndTime()))
                .totalVotes(defaultCount(box.getTotalVotes()))
                .supportCount(defaultCount(box.getSupportCount()))
                .opposeCount(defaultCount(box.getOpposeCount()))
                .abstainCount(defaultCount(box.getAbstainCount()))
                .status(resolveStatus(box))
                .hasVoted(familyVote != null)
                .myVoteOption(familyVote != null ? familyVote.getVoteOption() : null)
                .resultPublished(Boolean.TRUE.equals(box.getResultPublished()))
                .createdAt(DateTimeUtil.toIsoUtc(box.getCreatedAt()))
                .build();
    }

    public OpinionBoxDetailResponse toDetail(OpinionBox box,
                                               Resident publisher,
                                               VoteRecord familyVote) {
        OpinionBoxListItemResponse listItem = toListItem(box, publisher, familyVote, true);
        return OpinionBoxDetailResponse.builder()
                .id(listItem.getId())
                .title(listItem.getTitle())
                .content(listItem.getContent())
                .publisherId(listItem.getPublisherId())
                .publisherName(listItem.getPublisherName())
                .familyId(listItem.getFamilyId())
                .propertyCompanyId(box.getPropertyCompanyId())
                .startTime(listItem.getStartTime())
                .endTime(listItem.getEndTime())
                .totalVotes(listItem.getTotalVotes())
                .supportCount(listItem.getSupportCount())
                .opposeCount(listItem.getOpposeCount())
                .abstainCount(listItem.getAbstainCount())
                .status(listItem.getStatus())
                .hasVoted(listItem.getHasVoted())
                .myVoteOption(listItem.getMyVoteOption())
                .resultPublished(listItem.getResultPublished())
                .createdAt(listItem.getCreatedAt())
                .updatedAt(DateTimeUtil.toIsoUtc(box.getUpdatedAt()))
                .build();
    }

    public OpinionBoxResultResponse toResult(OpinionBox box) {
        int totalVotes = defaultCount(box.getTotalVotes());
        int supportCount = defaultCount(box.getSupportCount());
        int opposeCount = defaultCount(box.getOpposeCount());
        int abstainCount = defaultCount(box.getAbstainCount());

        BigDecimal supportRate = rate(supportCount, totalVotes);
        BigDecimal opposeRate = rate(opposeCount, totalVotes);
        BigDecimal abstainRate = rate(abstainCount, totalVotes);

        return OpinionBoxResultResponse.builder()
                .opinionBoxId(box.getId())
                .title(box.getTitle())
                .status(resolveStatus(box))
                .totalVotes(totalVotes)
                .supportCount(supportCount)
                .opposeCount(opposeCount)
                .abstainCount(abstainCount)
                .supportRate(supportRate)
                .opposeRate(opposeRate)
                .abstainRate(abstainRate)
                .resultPublished(Boolean.TRUE.equals(box.getResultPublished()))
                .endTime(DateTimeUtil.toIsoUtc(box.getEndTime()))
                .publishedAt(Boolean.TRUE.equals(box.getResultPublished())
                        ? DateTimeUtil.toIsoUtc(box.getUpdatedAt())
                        : null)
                .build();
    }

    public String resolveStatus(OpinionBox box) {
        if ("closed".equals(box.getStatus())) {
            return "closed";
        }
        LocalDateTime now = LocalDateTime.now();
        if (box.getEndTime() != null && box.getEndTime().isBefore(now)) {
            return "ended";
        }
        if (StringUtils.hasText(box.getStatus())) {
            return box.getStatus();
        }
        return "active";
    }

    public void refreshStoredStatus(OpinionBox box) {
        String resolved = resolveStatus(box);
        if ("ended".equals(resolved) && "active".equals(box.getStatus())) {
            box.setStatus("ended");
        }
    }

    private int defaultCount(Integer value) {
        return value != null ? value : 0;
    }

    private BigDecimal rate(int count, int total) {
        if (total <= 0) {
            return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(count)
                .divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP);
    }

    private String formatContent(String content, boolean fullContent) {
        if (!StringUtils.hasText(content) || fullContent || content.length() <= LIST_CONTENT_LIMIT) {
            return content;
        }
        return content.substring(0, LIST_CONTENT_LIMIT) + "...";
    }
}
