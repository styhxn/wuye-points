package com.wuye.points.util;

import com.wuye.points.entity.Family;
import com.wuye.points.entity.Resident;
import org.springframework.util.StringUtils;

/**
 * 住户地址工具：拼接用户登记的楼栋+单元+房号作为默认服务地址。
 * 供预约服务（CommunityServiceService）、商品下单（OrderService）、
 * 服务需求（ServiceRequestService）等流程复用，避免各处重复实现。
 */
public final class ResidentAddressUtil {

    private ResidentAddressUtil() {
    }

    /**
     * 拼接默认地址：楼栋+单元+房号（如 "3栋5单元502"），全部为空时返回 null。
     */
    public static String buildDefaultAddress(Resident resident) {
        StringBuilder builder = new StringBuilder();
        if (StringUtils.hasText(resident.getBuilding())) {
            builder.append(resident.getBuilding());
        }
        if (StringUtils.hasText(resident.getUnit())) {
            builder.append(resident.getUnit());
        }
        if (StringUtils.hasText(resident.getRoom())) {
            builder.append(resident.getRoom());
        }
        return builder.length() == 0 ? null : builder.toString();
    }

    /**
     * 判断住户与家庭是否处于同一房源（communityId + building + unit + room）。
     * 比较时忽略 floor（避免 5 / 5楼 / 空 匹配失败），并对楼栋/单元/房号做数字归一化
     * （如 "3栋" 与 "3" 视为一致）。
     */
    public static boolean sameUnitAddress(Resident resident, Family family) {
        if (resident == null || family == null) {
            return false;
        }
        if (!StringUtils.hasText(resident.getCommunityId())
                || !resident.getCommunityId().equals(family.getCommunityId())) {
            return false;
        }
        return samePart(resident.getBuilding(), family.getBuilding())
                && samePart(resident.getUnit(), family.getUnit())
                && samePart(resident.getRoom(), family.getRoom());
    }

    private static boolean samePart(String a, String b) {
        String na = normalizePart(a);
        String nb = normalizePart(b);
        if (na == null && nb == null) {
            return true;
        }
        if (na == null || nb == null) {
            return false;
        }
        return na.equals(nb);
    }

    /**
     * 数字归一化：仅保留数字字符（如 "3栋" -> "3"，"5楼" -> "5"）。
     * 空值/无数字返回 null。
     */
    private static String normalizePart(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String digits = value.replaceAll("\\D", "");
        return digits.isEmpty() ? null : digits;
    }
}
