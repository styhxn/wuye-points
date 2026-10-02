package com.wuye.points.exception;

import lombok.Getter;

import java.util.Optional;

@Getter
public enum ErrorCode {
    // 通用 10xxx
    PARAM_MISSING(10001, "参数缺失", 400),
    PARAM_INVALID(10002, "参数格式错误", 400),
    PARAM_OUT_OF_RANGE(10003, "参数值超出范围", 400),
    ENDPOINT_NOT_FOUND(10004, "接口不存在（路径或方法不匹配）", 404),
    METHOD_NOT_SUPPORTED(10005, "请求方法不支持", 405),
    MEDIA_TYPE_NOT_SUPPORTED(10006, "不支持的媒体类型", 415),
    BODY_PARSE_FAILED(10007, "请求体解析失败", 400),

    // 奖励金/积分发放审批 98xxx
    REWARD_GRANT_NOT_FOUND(98001, "发放申请不存在", 404),

    // 鉴权 20xxx
    TOKEN_MISSING(20001, "Token缺失", 401),
    TOKEN_EXPIRED(20002, "Token过期", 401),
    TOKEN_INVALID(20003, "Token无效", 401),
    PERMISSION_DENIED(20004, "权限不足", 403),
    ACCOUNT_DISABLED(20005, "账号被禁用", 403),

    // 业主 30xxx
    RESIDENT_NOT_FOUND(30001, "业主不存在", 404),
    FAMILY_MEMBER_LIMIT(30002, "家庭人数超限", 400),
    WECHAT_AUTH_FAILED(30003, "微信授权失败", 400),
    MERCHANT_DUPLICATE_BINDING(30004, "该商家已在当前物业下挂接", 400),
    RESIDENT_DUPLICATE_PHONE(30005, "该手机号在当前物业公司下已存在", 400),

    // 积分 40xxx
    POINT_INSUFFICIENT(40001, "积分余额不足", 400),
    POINT_RULE_NOT_FOUND(40002, "积分规则不存在", 404),

    // 物业币 50xxx
    COIN_INSUFFICIENT(50001, "物业币余额不足", 400),
    COIN_EXCHANGE_NOT_ALLOWED(50002, "积分与物业币不可互兑", 400),

    // 商家 60xxx
    MERCHANT_NOT_FOUND(60001, "商家不存在", 404),
    MERCHANT_AUDITING(60002, "商家入驻审核中", 409),
    AUDIT_ALREADY_PROCESSED(60003, "审核已处理", 409),
    PRODUCT_NOT_FOUND(60004, "商品不存在", 404),
    MERCHANT_ALREADY_KICKED(60005, "商家已被踢出", 400),
    MERCHANT_CONFIG_NOT_FOUND(60006, "商家在该物业下未完成挂接配置", 404),

    // 订单 70xxx
    ORDER_NOT_FOUND(70001, "订单不存在", 404),
    ORDER_STATUS_INVALID(70002, "订单状态不允许操作", 409),
    ORDER_CONFLICT(70003, "并发冲突", 409),

    // 楼栋变更 75xxx
    BUILDING_CHANGE_NOT_FOUND(75001, "楼栋变更申请不存在", 404),

    // 物业费 71xxx
    PROPERTY_FEE_NOT_FOUND(71001, "物业费账单不存在", 404),
    PROPERTY_FEE_DUPLICATE_PERIOD(71002, "该账期已存在物业费账单", 409),

    // 履约方式 70xxx（§68.6）
    FULFILLMENT_DEADLINE_PASSED(70021, "已超过履约选择时限，无法再选择", 400),
    FULFILLMENT_NOT_REQUIRED(70022, "本单无需配送，不可选择履约方式", 400),
    FULFILLMENT_NOT_CHOSEN(70023, "请先选择履约方式", 400),
    FULFILLMENT_DELIVERY_INCOMPLETE(70024, "配送未完成，不可完成订单", 400),
    FULFILLMENT_LOCKED(70025, "履约方式已锁定，不可变更", 400),

    // 配送 80xxx
    DELIVERY_GRAB_FAILED(80001, "快递员抢单失败", 400),
    DELIVERY_RULE_NOT_FOUND(80002, "配送规则不存在", 404),
    DELIVERY_RULE_DUPLICATE(80003, "该物业已存在配送规则", 409),

    // 公告 90xxx
    ANNOUNCEMENT_NOT_FOUND(90001, "公告不存在", 404),
    VOTE_ENDED(90002, "投票已结束", 400),
    COLLECT_ALREADY_SUBMITTED(90003, "已提交过收集信息", 400),
    OPINION_BOX_NOT_FOUND(90004, "意见箱不存在", 404),
    OPINION_ALREADY_VOTED(90005, "该家庭已投过票", 400),
    OPINION_MONTHLY_LIMIT(90006, "每家庭每月仅可发表1条意见", 400),
    ACTIVITY_GROUP_NOT_FOUND(90007, "活动组不存在", 404),
    ACTIVITY_ALREADY_SUBSCRIBED(90008, "已订阅该活动组", 400),
    ACTIVITY_ALREADY_MEMBER(90009, "已是活动组成员", 400),
    ACTIVITY_NOT_MEMBER(90010, "不是活动组成员", 400),
    ACTIVITY_LEADER_CANNOT_LEAVE(90011, "组长不能退出活动组", 400),
    MESSAGE_NOT_FOUND(90012, "消息不存在", 404),
    NEIGHBOR_CONTACT_LIMIT_EXCEEDED(90013, "今日邻居私聊次数已达上限", 400),

    // 统筹/板块/个体 91xxx
    COORDINATOR_NOT_FOUND(91001, "统筹不存在", 404),
    SECTOR_LEADER_NOT_FOUND(91002, "板块负责人不存在", 404),
    INDIVIDUAL_LEADER_NOT_FOUND(91003, "个体负责人不存在", 404),

    // 权限 92xxx
    PERMISSION_NOT_FOUND(92001, "权限项不存在", 404),
    PERMISSION_PRESET_NOT_FOUND(92002, "角色预设不存在", 404),
    PERMISSION_CHANGE_LOG_FAILED(92003, "权限变更日志写入失败", 500),

    // 积分池/物业币管控 93xxx
    POINT_POOL_NOT_FOUND(93001, "积分池不存在", 404),
    POINT_POOL_INSUFFICIENT(93002, "积分池余额不足", 400),
    COIN_FROZEN(93003, "物业币已冻结，无法使用", 400),
    COIN_EXPIRED(93004, "物业币已过期", 400),

    // 商家积分/提现 94xxx
    MERCHANT_POINT_PURCHASE_AUDITING(94001, "积分购买审核中", 400),
    MERCHANT_POINT_INSUFFICIENT(94002, "商家积分余额不足", 400),
    MERCHANT_WITHDRAWAL_INVALID(94003, "提现申请不允许操作", 400),
    COIN_WITHDRAWAL_AMOUNT_INVALID(94004, "兑换金额不合法", 400),

    // 文件 95xxx
    FILE_NOT_FOUND(95001, "文件不存在", 404),

    // 社区服务 96xxx
    SERVICE_NOT_FOUND(96001, "服务不存在", 404),
    SERVICE_ORDER_NOT_FOUND(96002, "服务订单不存在", 404),
    SERVICE_ALREADY_SUBSCRIBED(96003, "已订阅该服务", 400),
    SERVICE_UNAVAILABLE(17001, "服务已下架", 400),

    // 物业公司 97xxx
    PROPERTY_COMPANY_NOT_FOUND(97001, "物业公司不存在", 404),

    // 注销账号 30xxx
    ACCOUNT_DELETION_PENDING_ORDER(30006, "存在未完成的订单，请先处理", 400),
    ACCOUNT_DELETION_PENDING_FEE(30007, "存在未缴纳的物业费，请先处理", 400),
    ACCOUNT_DELETION_COIN_BALANCE(30008, "物业币余额不为0，请先处理", 400),
    ACCOUNT_DELETION_FAMILY_MEMBERS(30009, "您是户主，请先移除家庭成员", 400),

    // 社区 96xxx 扩展
    COMMUNITY_POST_TOO_LONG(96010, "帖子内容过长", 400),
    COMMUNITY_POST_NOT_FOUND(96011, "帖子不存在", 404),
    COMMUNITY_NOT_ALLOWED_DELETE(96012, "该帖子不允许删除", 400),
    COMMUNITY_COMMENT_TOO_LONG(96013, "评论内容过长", 400),
    COMMUNITY_COMMENT_NOT_FOUND(96014, "评论不存在", 404),
    COMMUNITY_REPORT_DUPLICATE(96015, "已举报过该内容", 400),
    COMMUNITY_REPORT_NOT_FOUND(96016, "举报记录不存在", 404),
    COMMUNITY_REPORT_ALREADY_HANDLED(96017, "举报已处理", 400),
    COMMUNITY_FORBIDDEN_WORD(96018, "包含敏感词", 400),

    // 服务工单 96xxx
    SERVICE_REQUEST_TIMEOUT(96020, "服务工单已超时", 400),

    // 配送价格区间 / 快递负责人 80xxx
    DELIVERY_PRICE_RANGE_NOT_FOUND(80010, "配送价格区间不存在", 404),
    DELIVERY_PRICE_RANGE_INVALID(80011, "配送价格区间不合法", 400),
    DELIVERY_PRICE_RANGE_OVERLAP(80012, "配送价格区间重叠", 400),
    COURIER_MANAGER_DUPLICATE(80013, "快递负责人已存在", 400),
    COURIER_ALREADY_BOUND(80014, "快递员已绑定其他负责人", 400),
    COURIER_MANAGER_NOT_FOUND(80015, "快递负责人不存在", 404),
    DELIVERY_NOT_COURIER_ORDER(80020, "非配送员承运单，配送员不可抢单/操作", 400),
    DELIVERY_NOT_FOUND(80021, "配送单不存在", 404),

    // 商家扩展 60xxx
    RESIDENT_MERCHANT_DEPOSIT_INVALID(60010, "商家保证金不合法", 400),
    MERCHANT_AD_QUOTA_INSUFFICIENT(60011, "广告额度不足", 400),

    // 提现 94xxx
    WITHDRAWAL_BLOCKED(94005, "提现已被冻结", 400),

    // 物业公司扩展 97xxx
    PROPERTY_OPERATOR_OUT_OF_SCOPE(97002, "物业操作员超出权限范围", 403),

    // 消息 90xxx
    DIRECTED_MSG_COMPANY_DAILY_LIMIT(90014, "定向消息物业每日发送上限", 400),
    DIRECTED_MSG_USER_DAILY_LIMIT(90015, "定向消息用户每日接收上限", 400),

    // 社区食堂 60xxx
    CANTEEN_PURCHASE_PENDING(60020, "额度购买待支付", 400),
    CANTEEN_QUOTA_PURCHASE_NOT_FOUND(60021, "额度购买记录不存在", 404),
    CANTEEN_QUOTA_INSUFFICIENT(60022, "可用额度不足", 400),
    CANTEEN_MERCHANT_NOT_IN_SYSTEM(60023, "食堂商家未在系统内", 404),
    CANTEEN_BALANCE_INSUFFICIENT(60024, "食堂余额不足", 400),
    CANTEEN_NOT_MAIN_MERCHANT(60025, "非社区食堂主商家", 400),
    CANTEEN_BINDING_EXISTS(60026, "绑定关系已存在", 400),
    CANTEEN_BINDING_NOT_FOUND(60027, "绑定关系不存在", 404),
    RESIDENT_MERCHANT_HAS_ORDERS(60028, "该住户商家已有订单", 400),
    RESIDENT_MERCHANT_NOT_PUBLIC(60029, "住户商家未公开", 400),

    // 物业币商城 96xxx 扩展
    COIN_MALL_DISABLED(96030, "物业币商城未开启", 400),
    COIN_BELOW_MIN_AMOUNT(96031, "低于物业币商城起购金额", 400),
    COIN_EXCEED_MAX_RATIO(96032, "超过物业币商城最大占比", 400),
    PREVIEW_EXPIRED(96033, "预览快照已过期", 400),
    CONSULTATION_APPOINTMENT_UNAVAILABLE(96034, "咨询预约不可用", 400),

    // 角色账号 97xxx 扩展
    ROLE_ACCOUNT_HAS_BUSINESS_DATA(97010, "角色账号存在业务数据，不可删除", 400),
    ROLE_ACCOUNT_ALREADY_DELETED(97011, "角色账号已删除", 400),

    // 分账比例校验 97006（2026-09-08 前端问题：商品平台盘一级三档合计必须为 100%）
    SHARE_RATE_PRIMARY_TOTAL_INVALID(97006, "商品平台盘一级分成比例合计必须为100%", 400),

    // 个体负责人申请 9703x（2026-09-08 前端问题：申请列表 / 审核路由）
    INDIVIDUAL_LEADER_APPLICATION_NOT_FOUND(97030, "个体负责人申请不存在", 404),
    INDIVIDUAL_LEADER_APPLICATION_ALREADY_AUDITED(97031, "该申请已审核，不可重复审核", 400),

    // 物业公司关联数据拦截 97033（2026-09-08 前端问题：硬删除需明确关联原因）
    PROPERTY_COMPANY_HAS_RELATED_DATA(97033, "该物业公司存在关联数据，不可删除", 400),

    // 分红/分账校验 92xxx
    DISTRIBUTION_NOT_CONSERVED(92030, "分红金额不守恒", 400),
    DELIVERY_SUBSIDY_SPONSOR_INVALID(92031, "配送补贴出资方不合法", 400),
    DISTRIBUTION_RATE_INVALID(92032, "分红比例不合法", 400),
    DELIVERY_SHARE_RATE_INVALID(92033, "配送分成比例不合法", 400),
    DISTRIBUTION_RATE_TOTAL_INVALID(92034, "分红比例合计不合法", 400),
    INVALID_SHARE_RATE_TOTAL(92035, "分成比例合计不合法", 400),

    // 功能开关 97xxx
    POINT_FEATURE_DISABLED(97020, "积分功能未开启", 400),
    COIN_FEATURE_DISABLED(97021, "物业币功能未开启", 400),

    // 订单验证 93xxx
    ORDER_VERIFICATION_EXPIRED(93030, "订单验证已过期", 400),

    // 系统 99xxx
    WECHAT_API_ERROR(99002, "微信接口异常", 502),
    WECHAT_ECOMMERCE_NOT_CONFIGURED(99003, "电商收付通未配置或未启用", 400),
    ORDER_PAY_UNAVAILABLE(99004, "该商家暂未完成微信进件，请使用积分/物业币支付", 400),
    WECHAT_CERT_NOT_READY(99005, "微信平台证书未就绪，无法加密敏感信息", 502),
    APPLYMENT_NOT_FOUND(99006, "微信进件申请不存在", 404),
    APPLYMENT_STATE_INVALID(99007, "进件申请状态不允许该操作", 400),
    APPLYMENT_ALREADY_EXISTS(99008, "该商家已存在进件申请", 400),
    APPLYMENT_UPLOAD_FAILED(99009, "进件资料上传微信失败", 502),
    TRANSFER_ACCOUNT_NOT_VERIFIED(99010, "收款账号未核验，无法转账", 400),
    TRANSFER_NOT_FOUND(99011, "转账单不存在", 404),
    TRANSFER_AMOUNT_INVALID(99012, "转账金额不合法", 400),
    RECOVERY_NOT_FOUND(99013, "待追回台账记录不存在", 404),

    // 优惠券 60xxx
    COUPON_TEMPLATE_NOT_FOUND(60040, "优惠券模板不存在", 404),
    COUPON_NOT_FOUND(60041, "优惠券不存在", 404),
    COUPON_UNAVAILABLE(60042, "优惠券不可用", 400),
    COUPON_EXPIRED(60043, "优惠券已过期", 400),
    COUPON_ALREADY_USED(60044, "优惠券已使用", 400),
    COUPON_MIN_SPEND_NOT_MET(60045, "订单金额未达到优惠券使用门槛", 400),
    COUPON_SCOPE_MISMATCH(60046, "该优惠券不适用于当前商家", 400),
    COUPON_STOCK_EXHAUSTED(60047, "优惠券已被领完", 400),
    COUPON_CLAIM_LIMIT_EXCEEDED(60048, "已达到每人限领上限", 400),
    COUPON_NOT_CLAIMABLE(60049, "该优惠券不可领取", 400),
    COUPON_TEMPLATE_IN_USE(60050, "优惠券模板已发放，不可修改关键字段", 400),

    SYSTEM_ERROR(99001, "内部服务异常", 500);

    private final int code;
    private final String message;
    private final int httpStatus;

    ErrorCode(int code, String message, int httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public static Optional<ErrorCode> findByCode(int code) {
        for (ErrorCode value : values()) {
            if (value.code == code) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }
}
