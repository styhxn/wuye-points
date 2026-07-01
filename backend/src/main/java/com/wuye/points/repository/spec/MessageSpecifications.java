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
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(propertyCompanyId)) {
                predicates.add(cb.equal(root.get("propertyCompanyId"), propertyCompanyId));
            }

            if (!propertyAdminView && StringUtils.hasText(currentUserId)) {
                var fromMe = cb.equal(root.get("fromId"), currentUserId);
                var toMe = cb.equal(root.get("toId"), currentUserId);
                var broadcastToRole = cb.and(
                        cb.equal(root.get("chatType"), "broadcast"),
                        cb.equal(root.get("toRole"), currentRole));
                var broadcastAll = cb.and(
                        cb.equal(root.get("chatType"), "broadcast"),
                        cb.isNull(root.get("toRole")));
                predicates.add(cb.or(fromMe, toMe, broadcastToRole, broadcastAll));
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
