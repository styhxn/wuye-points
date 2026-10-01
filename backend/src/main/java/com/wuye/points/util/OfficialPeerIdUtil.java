package com.wuye.points.util;

import org.springframework.util.StringUtils;

import java.util.Set;

/**
 * 官方会话稳定 peerId：official_{senderType}_{propertyCompanyId}
 * 例：official_property_pc_demo001
 */
public final class OfficialPeerIdUtil {

    public static final String PREFIX = "official_";
    public static final Set<String> SENDER_TYPES = Set.of(
            "community", "property", "platform", "coordinator");

    private OfficialPeerIdUtil() {
    }

    public static boolean isOfficialPeerId(String peerId) {
        return StringUtils.hasText(peerId) && peerId.startsWith(PREFIX);
    }

    public static String build(String officialSenderType, String propertyCompanyId) {
        if (!StringUtils.hasText(officialSenderType) || !SENDER_TYPES.contains(officialSenderType)) {
            throw new IllegalArgumentException("officialSenderType不合法");
        }
        if (!StringUtils.hasText(propertyCompanyId)) {
            throw new IllegalArgumentException("propertyCompanyId不能为空");
        }
        String id = PREFIX + officialSenderType + "_" + propertyCompanyId;
        if (id.length() > 32) {
            id = id.substring(0, 32);
        }
        return id;
    }

    public static String parseSenderType(String peerId) {
        if (!isOfficialPeerId(peerId)) {
            return null;
        }
        String rest = peerId.substring(PREFIX.length());
        for (String type : SENDER_TYPES) {
            if (rest.startsWith(type + "_")) {
                return type;
            }
        }
        return null;
    }

    public static String parsePropertyCompanyId(String peerId) {
        String type = parseSenderType(peerId);
        if (!StringUtils.hasText(type) || !isOfficialPeerId(peerId)) {
            return null;
        }
        String rest = peerId.substring(PREFIX.length());
        String prefix = type + "_";
        if (!rest.startsWith(prefix)) {
            return null;
        }
        String propertyCompanyId = rest.substring(prefix.length());
        return StringUtils.hasText(propertyCompanyId) ? propertyCompanyId : null;
    }

    public static String displayName(String officialSenderType, String propertyCompanyName) {
        String company = StringUtils.hasText(propertyCompanyName) ? propertyCompanyName : "物业";
        return switch (officialSenderType == null ? "" : officialSenderType) {
            case "community" -> "社区管理处";
            case "property" -> company;
            case "platform" -> "平台官方";
            case "coordinator" -> "统筹";
            default -> "官方消息";
        };
    }
}
