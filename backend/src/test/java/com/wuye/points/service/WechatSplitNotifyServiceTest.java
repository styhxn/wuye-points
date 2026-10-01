package com.wuye.points.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.wuye.points.service.WechatSplitNotifyService.STATUS_FREEZE_FAILED;
import static com.wuye.points.service.WechatSplitNotifyService.STATUS_FROZEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 微信分账回调状态机的回归测试。
 *
 * <p>核心断言是「状态映射的输出集合」与「终态判断的集合」必须同源：
 * 一旦两者漂移，要么回调把已完结的记录重新改写，要么终态检查形同虚设，
 * 在微信重推风暴下造成重复写库。</p>
 */
class WechatSplitNotifyServiceTest {

    @Test
    @DisplayName("PROCESSING 与 FINISHED 均落到 frozen，等待售后窗口调度完结")
    void wxFinishedStillFrozen() {
        // FINISHED 保持 frozen 是有意设计：仍需调度器按售后窗口调用 finishOrder，
        // 若直接置为终态，该记录将不再被扫描，自动提现链路会被跳过。
        assertEquals(STATUS_FROZEN, WechatSplitNotifyService.mapSplitStatus("PROCESSING"));
        assertEquals(STATUS_FROZEN, WechatSplitNotifyService.mapSplitStatus("FINISHED"));
    }

    @Test
    @DisplayName("CLOSED 落到 freeze_failed，由补偿调度重试")
    void wxClosedMapsToFreezeFailed() {
        assertEquals(STATUS_FREEZE_FAILED, WechatSplitNotifyService.mapSplitStatus("CLOSED"));
    }

    @Test
    @DisplayName("未知或空状态安全回退为 frozen")
    void unknownStatusFallsBack() {
        assertEquals(STATUS_FROZEN, WechatSplitNotifyService.mapSplitStatus(null));
        assertEquals(STATUS_FROZEN, WechatSplitNotifyService.mapSplitStatus("WHATEVER"));
    }

    @Test
    @DisplayName("frozen 不是终态，允许后续回调继续更新")
    void frozenIsNotTerminal() {
        assertFalse(WechatSplitNotifyService.isTerminal(STATUS_FROZEN));
    }

    @Test
    @DisplayName("finished / freeze_failed / reverted / refunded 均为终态")
    void terminalStatuses() {
        assertTrue(WechatSplitNotifyService.isTerminal("finished"));
        assertTrue(WechatSplitNotifyService.isTerminal("freeze_failed"));
        assertTrue(WechatSplitNotifyService.isTerminal("reverted"));
        assertTrue(WechatSplitNotifyService.isTerminal("refunded"));
        assertFalse(WechatSplitNotifyService.isTerminal(null));
    }

    @Test
    @DisplayName("mapSplitStatus 的所有可能输出都被终态集合覆盖或明确排除")
    void mappingAndTerminalSetAreConsistent() {
        // frozen 是唯一的非终态输出，保证终态判断不会被绕过
        for (String wx : new String[]{"PROCESSING", "FINISHED", "CLOSED", null, "UNKNOWN"}) {
            String mapped = WechatSplitNotifyService.mapSplitStatus(wx);
            boolean terminal = WechatSplitNotifyService.isTerminal(mapped);
            assertEquals(STATUS_FREEZE_FAILED.equals(mapped), terminal,
                    "状态映射与终态集合不一致: wx=" + wx + " -> " + mapped);
        }
    }
}
