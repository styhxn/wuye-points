package com.wuye.points.repository.spec;

import com.wuye.points.entity.Message;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class MessageSpecifications {

    private MessageSpecifications() {
    }

    public static Specification<Message> withFilters(
            String propertyCompanyId,
            String currentUserId,
            String currentRole,
            boolean propertyAdminView,
            String messageType,
            String chatType,
            String readStatus,
            String fromId) {
        return withFilters(propertyCompanyId, currentUserId,
                currentRole == null ? List.of() : List.of(currentRole),
                propertyAdminView, messageType, chatType, readStatus, fromId);
    }

    /**
     * 2026-10-20 负责人标签化：currentRoles 为「有效角色集合」——
     * 住户库内角色 + 其持有的负责人身份角色码（统筹/板块/个体）。
     * 广播消息按 toRole 命中集合内任一角色即可见，避免负责人角色归一为 resident 后收不到
     * 按原负责人角色群发的消息（向后兼容存量 toRole=coordinator 等广播）。
     */
    public static Specification<Message> withFilters(
            String propertyCompanyId,
            String currentUserId,
            java.util.Collection<String> currentRoles,
            boolean propertyAdminView,
            String messageType,
            String chatType,
            String readStatus,
            String fromId) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(propertyCompanyId)) {
                predicates.add(cb.equal(root.get("propertyCompanyId"), propertyCompanyId));
            }

            if (!propertyAdminView && StringUtils.hasText(currentUserId)) {
                var fromMe = cb.equal(root.get("fromId"), currentUserId);
                var toMe = cb.equal(root.get("toId"), currentUserId);
                var broadcastAll = cb.and(
                        cb.equal(root.get("chatType"), "broadcast"),
                        cb.isNull(root.get("toRole")));
                if (currentRoles == null || currentRoles.isEmpty()) {
                    predicates.add(cb.or(fromMe, toMe, broadcastAll));
                } else {
                    var broadcastToRole = cb.and(
                            cb.equal(root.get("chatType"), "broadcast"),
                            root.get("toRole").in(currentRoles));
                    predicates.add(cb.or(fromMe, toMe, broadcastToRole, broadcastAll));
                }
            }

            if (StringUtils.hasText(messageType)) {
                predicates.add(cb.equal(root.get("messageType"), messageType));
            }
            if (StringUtils.hasText(chatType)) {
                predicates.add(cb.equal(root.get("chatType"), chatType));
            }
            if (StringUtils.hasText(readStatus)) {
                predicates.add(cb.equal(root.get("readStatus"), readStatus));
            }
            if (StringUtils.hasText(fromId)) {
                predicates.add(cb.equal(root.get("fromId"), fromId));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}
