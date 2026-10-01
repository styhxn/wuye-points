package com.wuye.points.service;

/**
 * 微信分账异步回调状态机工具（纯静态，无状态）。
 *
 * <p>核心约束：「状态映射的输出集合」与「终态判断的集合」必须同源，避免微信重推风暴下
 * 回调把已完结记录重新改写、或终态检查形同虚设造成重复写库。</p>
 */
public final class WechatSplitNotifyService {

    public static final String STATUS_FROZEN = "frozen";
    public static final String STATUS_FREEZE_FAILED = "freeze_failed";

    private WechatSplitNotifyService() {
    }

    /**
     * 微信分账状态 → 本地状态。
     *
     * <p>PROCESSING / FINISHED 均落到 frozen（仍需调度器按售后窗口调用 finishOrder 完结，
     * 若直接置终态该记录将不再被扫描，自动提现链路会被跳过）；CLOSED 落到 freeze_failed 由补偿调度重试；
     * 未知 / 空安全回退为 frozen。</p>
     */
    public static String mapSplitStatus(String wxStatus) {
        if (wxStatus == null) {
            return STATUS_FROZEN;
        }
        return switch (wxStatus) {
            case "PROCESSING", "FINISHED" -> STATUS_FROZEN;
            case "CLOSED" -> STATUS_FREEZE_FAILED;
            default -> STATUS_FROZEN;
        };
    }

    /** 判断本地状态是否为终态（不再被后续回调改写）。 */
    public static boolean isTerminal(String status) {
        if (status == null) {
            return false;
        }
        return "finished".equals(status)
                || "freeze_failed".equals(status)
                || "reverted".equals(status)
                || "refunded".equals(status);
    }
}
