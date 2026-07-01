package com.wuye.points.service;

import com.wuye.points.dto.message.MessageDetailResponse;
import com.wuye.points.dto.message.MessageListItemResponse;
import com.wuye.points.entity.Message;
import com.wuye.points.entity.Resident;
import com.wuye.points.util.DateTimeUtil;
import org.springframework.stereotype.Component;

@Component
public class MessageConverter {

    public MessageListItemResponse toListItem(Message message, Resident sender) {
        return MessageListItemResponse.builder()
                .id(message.getId())
                .fromId(message.getFromId())
                .fromName(sender != null ? sender.getName() : null)
                .fromAvatarUrl(sender != null ? sender.getAvatarUrl() : null)
                .toId(message.getToId())
                .toRole(message.getToRole())
                .messageType(message.getMessageType())
                .content(message.getContent())
                .chatType(message.getChatType())
                .groupId(message.getGroupId())
                .readStatus(message.getReadStatus())
                .propertyCompanyId(message.getPropertyCompanyId())
                .createdAt(DateTimeUtil.toIsoUtc(message.getCreatedAt()))
                .build();
    }

    public MessageDetailResponse toDetail(Message message, Resident sender, Resident receiver) {
        return MessageDetailResponse.builder()
                .id(message.getId())
                .fromId(message.getFromId())
                .fromName(sender != null ? sender.getName() : null)
                .fromAvatarUrl(sender != null ? sender.getAvatarUrl() : null)
                .toId(message.getToId())
                .toName(receiver != null ? receiver.getName() : null)
                .toRole(message.getToRole())
                .messageType(message.getMessageType())
                .content(message.getContent())
                .chatType(message.getChatType())
                .groupId(message.getGroupId())
                .readStatus(message.getReadStatus())
                .propertyCompanyId(message.getPropertyCompanyId())
                .createdAt(DateTimeUtil.toIsoUtc(message.getCreatedAt()))
                .build();
    }
}
