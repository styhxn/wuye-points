package com.wuye.points.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // 通用 10xxx
    PARAM_MISSING(10001, "参数缺失"),
    PARAM_INVALID(10002, "参数格式错误"),
    PARAM_OUT_OF_RANGE(10003, "参数值超出范围"),

    // 鉴权 20xxx
    TOKEN_MISSING(20001, "Token缺失"),
    TOKEN_EXPIRED(20002, "Token过期"),
    TOKEN_INVALID(20003, "Token无效"),
    PERMISSION_DENIED(20004, "权限不足"),
    ACCOUNT_DISABLED(20005, "账号被禁用"),

    // 业主 30xxx
    RESIDENT_NOT_FOUND(30001, "业主不存在"),
    FAMILY_MEMBER_LIMIT(30002, "家庭人数超限"),
    WECHAT_AUTH_FAILED(30003, "微信授权失败"),
    MERCHANT_DUPLICATE_BINDING(30004, "该商家已在当前物业下挂接"),
    RESIDENT_DUPLICATE_PHONE(30005, "该手机号在当前物业公司下已存在"),

    // 积分 40xxx
    POINT_INSUFFICIENT(40001, "积分余额不足"),
    POINT_RULE_NOT_FOUND(40002, "积分规则不存在"),

    // 物业币 50xxx
    COIN_INSUFFICIENT(50001, "物业币余额不足"),
    COIN_EXCHANGE_NOT_ALLOWED(50002, "积分与物业币不可互兑"),

    // 商家 60xxx
    MERCHANT_NOT_FOUND(60001, "商家不存在"),
    MERCHANT_AUDITING(60002, "商家入驻审核中"),
    AUDIT_ALREADY_PROCESSED(60003, "审核已处理"),
    PRODUCT_NOT_FOUND(60004, "商品不存在"),
    MERCHANT_ALREADY_KICKED(60005, "商家已被踢出"),

    // 订单 70xxx
    ORDER_NOT_FOUND(70001, "订单不存在"),
    ORDER_STATUS_INVALID(70002, "订单状态不允许操作"),
    ORDER_CONFLICT(70003, "并发冲突"),

    // 物业费 71xxx
    PROPERTY_FEE_NOT_FOUND(71001, "物业费账单不存在"),

    // 配送 80xxx
    DELIVERY_GRAB_FAILED(80001, "快递员抢单失败"),
    DELIVERY_RULE_NOT_FOUND(80002, "配送规则不存在"),
    DELIVERY_RULE_DUPLICATE(80003, "该物业已存在配送规则"),

    // 公告 90xxx
    ANNOUNCEMENT_NOT_FOUND(90001, "公告不存在"),
    VOTE_ENDED(90002, "投票已结束"),
    COLLECT_ALREADY_SUBMITTED(90003, "已提交过收集信息"),
    OPINION_BOX_NOT_FOUND(90004, "意见箱不存在"),
    OPINION_ALREADY_VOTED(90005, "该家庭已投过票"),
    OPINION_MONTHLY_LIMIT(90006, "每家庭每月仅可发表1条意见"),
    ACTIVITY_GROUP_NOT_FOUND(90007, "活动组不存在"),
    ACTIVITY_ALREADY_SUBSCRIBED(90008, "已订阅该活动组"),
    ACTIVITY_ALREADY_MEMBER(90009, "已是活动组成员"),
    ACTIVITY_NOT_MEMBER(90010, "不是活动组成员"),
    ACTIVITY_LEADER_CANNOT_LEAVE(90011, "组长不能退出活动组"),
    MESSAGE_NOT_FOUND(90012, "消息不存在"),
    NEIGHBOR_CONTACT_LIMIT_EXCEEDED(90013, "今日邻居私聊次数已达上限"),

    // 统筹/板块/个体 91xxx
    COORDINATOR_NOT_FOUND(91001, "统筹不存在"),
    SECTOR_LEADER_NOT_FOUND(91002, "板块负责人不存在"),
    INDIVIDUAL_LEADER_NOT_FOUND(91003, "个体负责人不存在"),

    // 权限 92xxx
    PERMISSION_NOT_FOUND(92001, "权限项不存在"),
    PERMISSION_PRESET_NOT_FOUND(92002, "角色预设不存在"),
    PERMISSION_CHANGE_LOG_FAILED(92003, "权限变更日志写入失败"),

    // 积分池/物业币管控 93xxx
    POINT_POOL_NOT_FOUND(93001, "积分池不存在"),
    POINT_POOL_INSUFFICIENT(93002, "积分池余额不足"),
    COIN_FROZEN(93003, "物业币已冻结，无法使用"),
    COIN_EXPIRED(93004, "物业币已过期"),

    // 商家积分/提现 94xxx
    MERCHANT_POINT_PURCHASE_AUDITING(94001, "积分购买审核中"),
    MERCHANT_POINT_INSUFFICIENT(94002, "商家积分余额不足"),
    MERCHANT_WITHDRAWAL_INVALID(94003, "提现申请不允许操作"),
    COIN_WITHDRAWAL_AMOUNT_INVALID(94004, "兑换金额不合法"),

    // 文件 95xxx
    FILE_NOT_FOUND(95001, "文件不存在"),

    // 社区服务 96xxx
    SERVICE_NOT_FOUND(96001, "服务不存在"),
    SERVICE_ORDER_NOT_FOUND(96002, "服务订单不存在"),
    SERVICE_ALREADY_SUBSCRIBED(96003, "已订阅该服务"),

    // 物业公司 97xxx
    PROPERTY_COMPANY_NOT_FOUND(97001, "物业公司不存在"),

    // 系统 99xxx
    SYSTEM_ERROR(99001, "内部服务异常");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
