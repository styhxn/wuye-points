package com.wuye.points.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 日志敏感信息脱敏。
 *
 * <p>用途：Controller 请求日志、异常详情、第三方响应体在写入日志前统一脱敏，
 * 避免手机号 / 身份证 / 银行账号 / 令牌落盘造成合规风险（个人信息保护法、等保）。
 *
 * <p>策略：
 * <ol>
 *   <li>键值脱敏：命中敏感 key（password/token/secret/accountNo/...）时整值打码；</li>
 *   <li>模式脱敏：对自由文本中出现的手机号、身份证、银行卡号按正则识别并部分保留。</li>
 * </ol>
 */
public final class SensitiveMasker {

    private static final String MASK_ALL = "******";

    /** 需要整体打码的字段名（不区分大小写，命中即整值替换） */
    private static final Pattern SENSITIVE_KEY = Pattern.compile(
            "(?i)\"(password|passwd|oldPassword|newPassword|token|accessToken|refreshToken"
                    + "|secret|appSecret|apiKey|accessToken|apiV3Key|privateKey|authorization"
                    + "|accountNo|accountNumber|cardNo|bankCard|settleCardNo|idCard|idNumber)\"\\s*:\\s*\"([^\"]*)\"");

    /** 中国大陆手机号 */
    private static final Pattern PHONE = Pattern.compile("(?<!\\d)(1[3-9]\\d{9})(?!\\d)");

    /** 身份证号（15 或 18 位，末位可能为 X） */
    private static final Pattern ID_CARD = Pattern.compile("(?<!\\d)(\\d{6})(?:\\d{8}|\\d{5})(\\d{3}[\\dXx])(?!\\d)");

    /** 银行卡号 / 长数字账号（16~19 位） */
    private static final Pattern BANK_CARD = Pattern.compile("(?<!\\d)(\\d{6})\\d{6,}(\\d{4})(?!\\d)");

    private SensitiveMasker() {
    }

    /**
     * 对任意对象做脱敏后返回用于日志的字符串。
     *
     * <p>非 String 对象先 {@code toString()}，再依次执行键值脱敏与模式脱敏。
     * 入参为 null 时返回 "null"，永不抛异常（日志工具不得影响主流程）。
     */
    public static String mask(Object value) {
        if (value == null) {
            return "null";
        }
        String text = String.valueOf(value);
        if (text.isEmpty()) {
            return text;
        }
        try {
            text = maskByKey(text);
            text = maskByPattern(text);
        } catch (RuntimeException e) {
            return MASK_ALL;
        }
        return text;
    }

    /** 手机号脱敏：138****1234 */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    /** 银行账号脱敏：仅保留后 4 位 */
    public static String maskAccountNo(String accountNo) {
        if (accountNo == null || accountNo.length() <= 4) {
            return accountNo == null ? null : "****";
        }
        return "****" + accountNo.substring(accountNo.length() - 4);
    }

    /**
     * 令牌类脱敏：保留前 8 位用于日志关联，其余打码并附带长度。
     *
     * <p>适用于 previewToken、access_token 等"拿到即可重放"的凭据。
     * 完全打码会让排查无法关联日志，因此保留少量前缀。
     */
    public static String maskSecret(String secret) {
        if (secret == null) {
            return null;
        }
        if (secret.length() <= 8) {
            return "****";
        }
        return secret.substring(0, 8) + "****(len=" + secret.length() + ")";
    }

    private static String maskByKey(String text) {
        Matcher m = SENSITIVE_KEY.matcher(text);
        StringBuilder sb = new StringBuilder(text.length());
        while (m.find()) {
            // 银行账号保留后 4 位便于排查，其余整值打码
            String key = m.group(1).toLowerCase();
            String replacement = key.contains("accountno") || key.contains("cardno")
                    || key.contains("bankcard") || key.contains("settlecardno")
                    ? maskAccountNo(m.group(2))
                    : MASK_ALL;
            m.appendReplacement(sb, Matcher.quoteReplacement("\"" + m.group(1) + "\":\"" + replacement + "\""));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static String maskByPattern(String text) {
        text = PHONE.matcher(text).replaceAll(mr -> mr.group(1).substring(0, 3) + "****" + mr.group(1).substring(7));
        text = ID_CARD.matcher(text).replaceAll(mr -> mr.group(1) + "********" + mr.group(2));
        text = BANK_CARD.matcher(text).replaceAll(mr -> mr.group(1) + "******" + mr.group(2));
        return text;
    }
}
