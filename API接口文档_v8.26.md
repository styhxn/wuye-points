# 物业积分服务系统 — API 接口文档

> **版本**：v8.36（2026-11-03 第四次修订：🆕 分账接收方**个人 openid 手填通道下线**——§C.3.2 `POST /admin/wechat-split/receivers` 只收 8~20 位微信商户号，传 openid 返回 `10002`；列表/保存响应新增 `accountNoMasked`（恒下发），`accountNo` 仅商户号下发、openid 场景该键缺失；§110.1.2 注明个人接收方已无手填入路。上一版 v8.35 新增 §18.5.1 活动组内楼长只读列表）
> **v8.35**（2026-11-03）：🆕 §18.5.1 `GET /activity-groups/{id}/building-leaders` — 活动组内单元楼长**只读**列表；同时在「组长能力总览」与「权限边界」两处写死「楼长是物业侧身份，组长无设置/撤销权」。
> **v8.34**（2026-11-03 二次修订：🆕 `GET /admin/transactions/detail`（单条流水详情，返回 `sourceTable`/`relatedKeyword`/`links`，见 §106.3.4）与错误码 `97075 TRANSACTION_NOT_FOUND`。文件名由维护者重命名为 `API接口文档_v8.26.md`，**以本行版本号为准**）
> **v8.33**（2026-11-03）：`POST /auth/wechat-login` 新增 `phoneCode`，首次登录强制手机号授权（`30010`/`30011`）；V36 分账链「用户侧先扣档」上线，`calculationVersion` 升为 `distribution_b_v2`，分成预览/明细新增 4 个字段；`/admin/platform-share-rates` 与物业公司配置新增 `userPointShareRate`/`userRewardShareRate`；补齐微信分账接收方主体预检/批量登记、住户微信身份认领 4 个接口；新增 `30010`/`30011`/`94007`/`97072`/`97073`/`97074`/`99016` 等错误码；**错误码以本文件「附录二·错误码权威总表」为准**，历史小节内标注的旧码见「附录三·失效错误码对照」。
> **上一版 v8.32**（2026-10-30）：功能停用错误码改为 `97070`/`97071`；`coin:display_control` 收紧为物业领导专属；参数配置写权限 `config:set` + 分账/手续费仅平台；补 `pointEnabled`/`coinEnabled` 读写与住户端下发；修正 `property_operator` 子角色权限；修正 §14.5/§14.6 历史字段清单。
> **Base URL**：`https://test.yuanxusoftware.top/api/v1`
> **维护方**：连云港元序软件有限公司
> **文档说明**：本文件为**当前最新完整接口文档**（已去除历史版本迭代说明，仅保留最新契约）。

# 目录

- [快速开始](#快速开始)
- [认证与鉴权](#认证与鉴权)
- [统一响应格式](#统一响应格式)
- [分页规范](#分页规范)
- [错误码体系](#错误码速查表)（历史速查）→ **[错误码权威总表（附录二）](#附录二--错误码权威总表以-errorcodejava-为准2026-11-03-全量核对)**
- [限流与幂等性](#限流与幂等性)
- [接口列表](#接口列表)
  - [鉴权模块](#1-鉴权模块-auth)
  - [业主/住户模块](#2-业主住户模块-residents)
    - [楼栋变更申请](#262-楼栋变更申请)
    - [楼栋变更审批（管理端）](#263-楼栋变更审批)
    - [转给物业（积分/物业币）](#264-转给物业)
  - [家庭模块](#3-家庭模块-families)
  - [积分模块](#4-积分模块-points)
  - [物业币模块](#5-物业币模块-property-coins)
  - [商品模块](#6-商品模块-products)
  - [商家模块](#7-商家模块-merchants)
    - [多档配送费 `PUT /merchants/my/delivery-fees`](#721-多档配送费)
  - [订单模块](#8-订单模块-orders)
    - [支付试算 `POST /orders/preview`](#80-订单支付试算)
    - [业主上架商品支付试算 `POST /orders/from-listing/preview`](#800-post-ordersfrom-listingpreview--业主上架商品支付试算)
    - [业主小店下单 `POST /orders/from-listing`](#60101-post-ordersfrom-listing--业主小店下单件数制)
  - [配送模块](#9-配送模块-deliveries)
    - [可用配送员 `GET /deliveries/couriers`](#98-可用配送员)
  - [配送规则模块](#10-配送规则模块-delivery-rules)
    - [管理端送货 `GET /admin/couriers` / `delivery-orders`](#1053-管理端配送员与配送订单)
  - [公告模块](#11-公告模块-announcements)
  - [消息模块](#12-消息模块-messages)
  - [文件模块](#13-文件模块-files)
  - [物业公司模块](#14-物业公司模块-property-companies)
  - [物业费模块](#15-物业费模块-property-fees)
  - [服务订单模块](#16-服务订单模块-service-orders)
  - [社区服务模块](#17-社区服务模块-community-services)
  - [活动组模块](#18-活动组模块-activity-groups)
  - [意见箱模块](#19-意见箱模块-opinion-boxes)
  - [报表模块](#20-报表模块-reports)
  - [分成模块](#21-分成模块-distribution)
  - [商家积分模块](#22-商家积分模块-merchant-points)
    - [积分报价 `GET /merchant/points/quote`](#224-积分报价)
  - [商家提现模块](#23-商家提现模块-merchant-withdrawals)
  - [物业币兑换模块](#24-物业币兑换模块-coin-withdrawals)
  - [特惠推送模块](#25-特惠推送模块-special-offers)
  - [消费过的商家](#26-消费过的商家模块-resident-merchant)
  - [投票记录](#27-投票记录模块-my-votes)
  - [家庭贡献](#28-家庭贡献模块-family-contributions)
  - [活动组订阅状态](#29-活动组订阅状态模块-subscription-status)
  - [邻居搜索](#30-邻居搜索模块-neighbor-search)
  - [账号注销](#31-账号注销模块-account-deletion)
  - [移动端功能性缺口修复（v7.8）](#95-移动端功能性缺口修复v78)
- [管理端 — 公告管理](#32-管理端--公告管理)
  - [管理端 — 物业币提现审核](#33-管理端--物业币提现审核)
  - [管理端 — 商家审核](#34-管理端--商家审核)
  - [管理端 — 操作日志](#35-管理端--操作日志)
  - [管理端 — 积分清零](#36-管理端--积分清零)
  - [管理端 — 个体负责人管理](#37-管理端--个体负责人管理)
  - [管理端 — 线下积分发放](#38-管理端--线下积分发放)
  - [管理端 — 板块负责人管理](#39-管理端--板块负责人管理)
    - [板块负责人工作台](#396-板块负责人工作台)
  - [管理端 — 积分池管理](#40-管理端--积分池管理)
  - [管理端 — 统筹管理](#41-管理端--统筹管理)
  - [管理端 — 平台商家管理](#42-管理端--平台商家管理)
    - [商家-小区距离配置](#421-商家-小区距离配置)
    - [转给物业流水（对账）](#422-转给物业流水对账)
  - [管理端 — 商家踢出](#43-管理端--商家踢出)
  - [管理端 — 仪表盘](#44-管理端--仪表盘)
  - [管理端 — 批量商家配置](#45-管理端--批量商家配置)
  - [管理端 — 商家挂接](#46-管理端--商家挂接)
  - [管理端 — 盈利空间](#47-管理端--盈利空间)
  - [管理端 — 权限管理](#48-管理端--权限管理)
  - [管理端 — 业主物业币管控](#49-管理端--业主物业币管控)
  - [管理端 — 物业币过期管理](#50-管理端--物业币过期管理)
  - [管理端 — 物业公司管理](#51-管理端--物业公司管理)
- [二期 — 定向消息推送](#52-二期--定向消息推送-directed-messages)
  - [二期 — 社区多物业绑定](#53-二期--社区多物业绑定-community-bindings)
  - [二期 — 物业角色层级](#54-二期--物业角色层级-property-operators)
  - [二期 — 商家服务范围](#55-二期--商家服务范围-merchant-service-scope)
  - [二期 — 聊天配额与官方会话](#56-二期--聊天配额与官方会话-chat-quota--official)
  - [二期 — 服务需求智能匹配](#57-二期--服务需求智能匹配-service-requests)
  - [二期 — 咨询板块](#58-二期--咨询板块-consultations)
  - [二期 — 商家广告推送](#59-二期--商家广告推送-merchant-ads)
  - [二期 — 业主商户体系](#60-二期--业主商户体系-resident-merchants)
  - [优惠券模块](#98-优惠券模块)
  - [单元楼长与业主卖货钱包](#99-单元楼长与业主卖货钱包)
  - [物业公司经理（v8.11）](#100-物业公司经理property-managers)
- [三期 — 模块负责人体系](#61-三期--模块负责人体系-sector-leaders)
  - [三期 — 快递负责人体系](#62-三期--快递负责人体系-courier-managers)
  - [三期 — 商家端口改造](#63-三期--商家端口改造-merchant-port)
  - [三期 — 统筹管理](#64-三期--统筹管理-coordinator-workbench)
  - [三期 — 价格审批](#65-三期--价格审批-price-approvals)
  - [三期 — 通知/发布增强](#66-三期--通知发布增强-announcement-enhance)
  - [三期 — 询价](#67-三期--询价-inquiries)
  - [三期 — 活动组增强](#68-三期--活动组增强-activity-group-enhance)
  - [三期 — 物业币商城规则](#69-三期--物业币商城规则-coin-mall-rules)
  - [三期 — 多角色提现](#70-三期--多角色提现-role-withdrawals)
  - [三期 — 平台管理员分成体系](#71-三期--平台管理员分成体系-platform-share)
  - [技工工作台](#72-技工工作台-technicians)
  - [社区食堂模块](#73-社区食堂模块-canteen)
    - [管理端 — 创建/标记社区食堂商家 `POST /admin/canteen/merchants`](#731-标记取消主商家)
  - [管理端 — 手动调整可用充值额度 `POST /admin/canteen/merchants/{merchantId}/quota-adjust`](#7313-手动调整可用充值额度)
    - [管理端 — 绑定子商家 `POST /admin/canteen/bindings`](#732-绑定子商家到主商家)
    - [管理端 — 解绑 `DELETE /admin/canteen/bindings/{id}`](#733-解绑)
    - [管理端 — 调整供货优惠比例 `PUT /admin/canteen/bindings/{id}/rate`](#734-调整供货优惠比例)
    - [管理端 — 绑定列表 `GET /admin/canteen/bindings`](#735-绑定列表)
    - [管理端 — 手续费配置 `GET/PUT /admin/canteen/settings`](#736-手续费配置)
    - [管理端 — 三类流水 `GET /admin/canteen/quota-purchases|recharges|flows`](#737-三类流水查询)
    - [管理端 — 流水导出CSV `GET /admin/canteen/export`](#738-流水导出csv)
    - [商家端 — 购买额度 `POST /merchant/canteen/quota/quote|purchase`](#739-购买充值额度)
    - [商家端 — 额度余额/流水 `GET /merchant/canteen/quota/*`](#7310-额度余额与流水)
    - [商家端 — 给用户充定向余额 `POST /merchant/canteen/recharge`](#7311-给用户充定向余额)
    - [用户端 — 我的定向余额 `GET /canteen/my-balances`](#7312-我的定向余额)
    - [用户端 — 定向余额流水 `GET /canteen/flows`](#7313-我的定向余额流水)
    - [订单支付 — 定向余额抵扣](#7314-订单支付定向余额抵扣)
- [枚举值速查表](#枚举值速查表)
- [错误码速查表](#错误码速查表)
- [v7.0 — 统一响应与观测](#91-统一响应与观测v70-新增)
- [v7.0 — 支付与分账（CBK）](#92-支付与分账cbk)
- [v7.1 — 需求对齐新增接口](#94-需求对齐新增接口v71)
- [业务流程说明](#业务流程说明)
- [WebSocket 接口](#websocket-接口)
- [90. 微信普通分账管理接口](#90-微信普通分账管理接口-cbk-accounts--reconcile)
- [附录](#附录)
- [110. v8.33 新增（2026-11-01～11-03）](#110-v833-新增2026-11-01--11-03)
  - [分账接收方主体预检与批量登记](#1101-微信分账--个人接收方主体预检与批量登记)
  - [管理端 — 住户微信身份认领](#1102-管理端--住户微信身份认领)
  - [微信支付/退款回调地址](#1103-微信支付--退款回调前端不调用供配置核对)
  - [前端页面调整清单（v8.33）](#1104-前端页面调整清单v833)
- [附录二 · 错误码权威总表](#附录二--错误码权威总表以-errorcodejava-为准2026-11-03-全量核对)
- [附录三 · 失效错误码对照](#附录三--失效错误码对照历史文档标注--现行实现)

---



# 快速开始



## 1. 获取 Token

```bash
# 微信小程序登录
curl -X POST https://test.yuanxusoftware.top/api/v1/auth/wechat-login \
  -H "Content-Type: application/json" \
  -d '{
    "code": "wx_login_code_here",
    "propertyCompanyId": "pc_demo001",
    "phoneCode": "0b3xk0003LmQ..."
  }'
```

**响应示例**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJyZXNfYjdtOXgxIiwicm9sZSI6InJlc2lkZW50IiwicHJvcGVydHlDb21wYW55SWQiOiJwY19kZW1vMDAxIiwiZmFtaWx5SWQiOiJmYW1fZGVtbzAwMSIsImlhdCI6MTcxOTc3MDAwMCwiZXhwIjoxNzE5NzczNjAwfQ.xxx",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9.refresh_token_xxx",
    "expiresIn": 3600,
    "tokenType": "Bearer",
    "resident": {
      "id": "res_b7m9x1",
      "name": "张先生",
      "avatarUrl": "https://cdn.example.com/avatar/res_b7m9x1.jpg",
      "phone": "138****1234",
      "role": "resident",
      "propertyCompanyId": "pc_demo001",
      "communityId": "com_demo001",
      "familyId": "fam_demo001",
      "wechatBound": true,
      "newUser": false,
      "status": "active"
    }
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**请求体**：


| 字段                  | 类型       | 必填  | 校验                         | 说明                 |
| ------------------- | -------- | --- | -------------------------- | ------------------ |
| `code`              | `String` | ✅   | `@NotBlank @Size(max=256)` | 微信登录授权码（`wx.login` 取得，**一次性**，用过即失效） |
| `phoneCode`         | `String` | 🆕 首次登录必填 | `@Size(max=256)`          | `<button open-type="getPhoneNumber">` 回调里的 `code`，后端换真实手机号。缺失返回 `30010`，见下方「首次登录强制手机号授权」 |
| `propertyCompanyId` | `String` | ❌   | `@Size(max=32)`            | 物业公司ID。**首次登录必传**（用于把微信身份限定在本物业范围内匹配） |
| `encryptedData`     | `String` | ❌   |                            | ⚠️ 历史遗留字段，后端**已不再读取**；手机号请用 `phoneCode` |
| `iv`                | `String` | ❌   | `@Size(max=24)`            | ⚠️ 历史遗留字段，后端**已不再读取** |

> 🆕 **v8.33 起 `communityId` 不是本接口的入参**（请求体无此字段，传了会被忽略）。小区在登录后由 `PUT /residents/me/community`（§2.6.1）完善。


**首次登录强制手机号授权（v8.33，V36）**：

`openid` 在 `residents` 里查不到记录 = 首次登录，此时**必须**带 `phoneCode`，否则直接返回 `30010` 且**不会新建账号**。

```
收到 code=30010
  → 拉起 <button open-type="getPhoneNumber">（不能静默跳过）
  → 拿到回调 e.detail.code
  → 重新 wx.login 取一个**新的** login code（旧的已作废，不能复用）
  → 再 POST /auth/wechat-login，body 带 phoneCode
```

`30010` 是**流程信号不是错误**，不要按错误弹 toast。

带 `phoneCode` 后按手机号在该物业公司范围内的命中数量分三种结果：

| 命中 | 行为 | 响应 |
| --- | --- | --- |
| 恰好 1 条 | 把 openid 绑到这条已有账号，**不新建记录**（管理端预建的商家/组长/技工/配送员账号即走这条） | 正常登录，`resident.newUser=false` |
| 0 条 | 新建住户账号，**新建即带手机号** | 正常登录，`resident.newUser=true` |
| ≥ 2 条 | 一律拒绝自动绑定（`residents.openid` 是唯一索引，绑错就再也绑不上） | `30011`，须走管理端「微信身份认领」（§110.2） |
| 命中的账号已绑**另一个**微信 | 拒绝 | `10002`，文案「该手机号账号已绑定其他微信身份，请先在管理端做微信身份认领」 |

> `openid` 已命中已有账号时（非首次登录），**不需要** `phoneCode`，行为与旧版一致。


**字段说明**：

**data 字段**：


| 字段             | 类型        | 说明               |
| -------------- | --------- | ---------------- |
| `accessToken`  | `String`  | 访问令牌             |
| `refreshToken` | `String`  | 刷新令牌             |
| `expiresIn`    | `Integer` | 过期时间（秒）          |
| `tokenType`    | `String`  | 令牌类型，固定 `Bearer` |


**data.resident 字段**：


| 字段                  | 类型        | 说明                              |
| ------------------- | --------- | ------------------------------- |
| `id`                | `String`  | 住户ID                            |
| `name`              | `String`  | 姓名                              |
| `avatarUrl`         | `String`  | 头像URL                           |
| `phone`             | `String`  | 手机号（脱敏）                         |
| `role`              | `String`  | 角色                              |
| `propertyCompanyId` | `String`  | 物业公司ID                          |
| `communityId`       | `String`  | 小区ID                            |
| `familyId`          | `String`  | 家庭ID                            |
| `wechatBound`       | `Boolean` | 是否绑定微信                          |
| `newUser`           | `Boolean` | 是否新用户                           |
| `status`            | `String`  | 状态：`active`/`frozen`/`disabled` |


**错误响应**：


| 错误码     | HTTP | 说明     |
| ------- | ---- | ------ |
| `10001` | 400  | `code` 缺失（PARAM_MISSING） |
| `30003` | 400  | 微信授权失败（`code` 无效或已被用过） |
| `30010` | 400  | 🆕 首次登录需授权手机号：**不是错误**，按上方流程拉起 `getPhoneNumber` 后重试 |
| `30011` | 400  | 🆕 该手机号匹配到多个账号，无法自动绑定；文案原样展示，**不要重试**，走管理端认领 |
| `10002` | 400  | 该手机号账号已绑定其他微信身份 / `propertyCompanyId` 格式非法 |
| `20005` | 403  | 账号被禁用  |
| `429001` | 429 | 登录限流：同 IP 5 分钟内超阈值（见 §0.10），响应体只有 `code`/`message`，无 `data` |


---

> **鉴权说明**：以下接口已在代码中配置为 **Auth: Optional**，无需携带 Token 即可访问：
>
> - `GET /announcements`、`GET /announcements/{id}`
> - `GET /property-companies`、`GET /property-companies/{id}`、`GET /property-companies/{id}/communities`
> - `GET /services`、`GET /services/{id}`
> - `GET /activity-groups`、`GET /activity-groups/{id}`
> - `GET /special-offers`、`GET /special-offers/{id}`
> - `POST /auth/wechat-login`、`POST /auth/admin-login`、`POST /auth/refresh-token`
> - `GET /products`、`GET /products/{id}`
> - `GET /merchants`、`GET /merchants/{id}`（排除 `/merchants/my`）
> - `GET /service-categories`
> - `GET /consultations/categories`、`GET /consultations/consultants`、`GET /consultations/consultants/{id}`
> - **WebSocket 路径**：`/ws`、`/ws/**`、`/ws/v1`、`/ws/v1/**` 排除 HTTP `AuthInterceptor`；鉴权在 WebSocket 握手阶段由 Query `token` 完成，**不使用** `Authorization` Header
>
> **Spring Security 说明**：`SecurityFilterChain` 对 HTTP 请求为 `anyRequest().permitAll()`，**不做路径 ACL**。角色与登录态仍由 `AuthInterceptor` 校验；`/admin/**` 额外经 `PermissionInterceptor` 做权限项校验。对接时勿按 Spring Security 过滤器链理解权限。

---

### 0.9 跨域（CORS）与预检请求

**适用场景**：Web 管理端非同源部署、以及**管理端打包成 APK / iOS App**（WebView、Capacitor、Cordova、HBuilder 等）后直连后端。

**服务端行为**：

| 项 | 值 |
| --- | --- |
| 允许来源 | 由环境变量 `CORS_ORIGINS` 控制，**默认 `*`**；配置项 `app.cors.allowed-origins`，逗号分隔，**支持通配符**（如 `https://*.example.com`） |
| 允许方法 | `GET`、`POST`、`PUT`、`PATCH`、`DELETE`、`OPTIONS`、`HEAD` |
| 允许请求头 | `*`（含 `Authorization`、`Content-Type`） |
| 暴露响应头 | `Authorization`、`Content-Disposition`、`X-Content-Type-Options`、`X-Frame-Options` |
| `allowCredentials` | `false`（**全链路 JWT 鉴权，不依赖 Cookie**） |
| 预检缓存 | `Access-Control-Max-Age: 3600` |

**关键约定**：

1. **`OPTIONS` 预检请求全程放行**，由 Security 过滤器链最前端的 `CorsFilter` 直接返回 `200`，**不进入** `AuthInterceptor` / `PermissionInterceptor` / `RateLimitFilter`。因此预检请求**无需携带 `Authorization`**，也不会返回 `40101`。
2. 由于 `allowCredentials=false`，前端**不要**设置 `withCredentials: true`，否则浏览器会因 `Access-Control-Allow-Credentials` 不匹配而拦截响应。
3. App 打包场景下 `Origin` 可能为 `http://localhost`、`capacitor://localhost` 或字面量 `null`，默认配置均已放行；若需收紧，请在部署时显式配置 `CORS_ORIGINS`。

> ⚠️ **App 端排查提示**：若 APK 中登录卡住但 Web 端正常，除 CORS 外请优先确认两点：
> ① Android 9+ 默认禁止明文 HTTP，必须使用 `https://`，或在 `AndroidManifest.xml` 声明 `android:usesCleartextTraffic="true"`；
> ② 打包产物中的 `baseURL` 必须是完整域名（`https://<域名>/api/v1`），不能是相对路径或 `localhost`。

### 0.10 登录限流

`RateLimitFilter` **仅对以下两个登录接口**按客户端 IP 限流，其余接口不限流：

| 接口 | 限流键 |
| --- | --- |
| `POST /auth/admin-login` | `admin_login:<IP>` |
| `POST /auth/wechat-login` | `wechat_login:<IP>` |

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `app.rate-limit.max-attempts` | `10` | 窗口内最大尝试次数 |
| `app.rate-limit.window-ms` | `300000` | 时间窗口（5 分钟） |

- 客户端 IP 依次取 `X-Forwarded-For`（首个）→ `X-Real-IP` → `remoteAddr`，反向代理需正确透传。
- 超限返回 **HTTP 429**，响应体：

```json
{ "code": 429001, "message": "请求过于频繁，请稍后再试" }
```

- 该响应由 Filter 直接写出（不经过全局异常处理器），**但已补齐 `Access-Control-Allow-Origin` 响应头**，跨域场景下前端可正常读取到该错误体而非笼统的 CORS 报错。
- `OPTIONS` 预检**不计入**限流计数。

---



### 1.2 POST `/auth/admin-login` — 管理端登录（手机号+密码）

**Auth**: Optional  
**Description**: 管理端使用手机号和密码登录，适用于物业管理员、平台管理员等角色

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/auth/admin-login \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "13800000000",
    "password": "<初始密码，创建时随机生成>"
  }'
```

**请求体**：


| 字段         | 类型       | 必填  | 校验                                 | 说明  |
| ---------- | -------- | --- | ---------------------------------- | --- |
| `phone`    | `String` | ✅   | `@NotBlank @Pattern("^1\\d{10}$")` | 手机号 |
| `password` | `String` | ✅   | `@NotBlank @Size(min=6, max=32)`   | 密码  |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "expiresIn": 3600,
    "tokenType": "Bearer",
    "resident": {
      "id": "res_pleader01",
      "name": "物业领导-测试",
      "phone": "139****0020",
      "role": "property_admin",
      "propertySubRole": "property_leader",
      "isBuildingLeader": false,
      "isCoordinator": false,
      "coordinatorId": null,
      "isSectorLeader": false,
      "sectorLeaderIds": [],
      "isIndividualLeader": false,
      "individualLeaderId": null,
      "propertyCompanyId": "pc_demo001",
      "status": "active"
    }
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

> **物业侧角色说明**：  
> - 登录鉴权主字段仍是 `role`。物业领导 / 物业管理员的主角色均为 **`property_admin`**。  
> - 细分用 `propertySubRole`：`property_leader` = 物业领导，`property_operator` = 物业管理员（可限楼栋/小区）。  
> - **不要把 `property_leader` / `property_operator` 写入 `role` 字段**；若历史数据误写入 JWT `role`，后端会按 `property_admin` 归并鉴权。

> **🆕 v8.13 负责人身份标签**：登录响应 `resident` 摘要（`/auth/wechat-login`、`/auth/admin-login`）新增 `isCoordinator`/`coordinatorId`、`isSectorLeader`/`sectorLeaderIds`、`isIndividualLeader`/`individualLeaderId`——与 `isBuildingLeader` 同款的「住户子身份」标签：负责人账号 `role` 为 `resident`，前端据这些字段在登录后立即渲染「我的」页负责人工作台入口；同时持有负责人身份的住户仍可正常登录管理端（负责人工作台按业务表 active 记录鉴权）。

**错误响应**：


| 错误码     | HTTP | 说明       |
| ------- | ---- | -------- |
| `30001` | 404  | 手机号或密码错误 |
| `20005` | 403  | 账号被禁用    |


**初始管理员账号**：


| 手机号           | 密码            | 角色 `role`        | 子角色 `propertySubRole` | 说明   |
| ------------- | ------------- | ---------------- | ---------------------- | ---- |
| `<平台管理员手机号>` | `<创建时返回的初始密码>` | `platform_admin` | — | 平台超管 |


**物业角色层级联调账号**（`POST /auth/admin-login`，密码使用创建时返回的 `initialPassword`）：


| 手机号           | 用户ID | `role` | `propertySubRole` | 说明 |
| ------------- | ------ | ------ | ----------------- | ---- |
| `13900000020` | `res_pleader01` | `property_admin` | `property_leader` | 物业领导（全公司权限） |
| `13900000021` | `res_poperator01` | `property_admin` | `property_operator` | 物业管理员（可限小区/楼栋） |


**三角色联调测试账号**（`POST /auth/admin-login`，密码使用创建时返回的 `initialPassword`）：


| 手机号           | 用户ID        | 角色               | 说明        |
| ------------- | ------------ | ---------------- | --------- |
| `13900000008` | `res_mch01`  | `merchant`       | 商家客户端联调   |
| `13900000009` | `res_courier01` | `courier`     | 配送员客户端联调（账号数据已修复） |
| `13900000011` | `res_act01`  | `activity_leader` | 活动组组长联调；所管活动组示例 ID：`ag_admin003` |


> **组长联调提示**：登录后调用 `GET /activity-groups/my-led` 获取管理的活动组列表（§18.2.1）。

> **商家联调提示**：「陈老板的店」`mch_mch01`、组长店 `mch_act01`、技工店 `mch_tech01` 等所有商家均须在 `merchant_property_configs` 中配置 `pc_demo001` 挂接记录，否则下单/支付返回 `60006`。**2026-09-06 起测试服已补齐全部缺失挂接数据**（脚本 `sql/2026-09-06_frontend_issues_fix.sql`），且后端已修复：任命组长/技工、统筹工作台添加商家时同步创建挂接配置。


---



### 1.3 开发模式登录（仅开发环境）

**Auth**: Optional  
**Description**: 开发环境绕过微信验证，使用 `dev_` 前缀的code直接登录

**前置条件**：`app.wechat.skip-api=true`（开发环境默认开启）

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/auth/wechat-login \
  -H "Content-Type: application/json" \
  -d '{
    "code": "dev_13800000001"
  }'
```

**code格式规则**：


| code格式     | 说明             | 示例                |
| ---------- | -------------- | ----------------- |
| `dev_手机号`  | 用指定手机号登录（自动注册） | `dev_13800000001` |
| `dev_任意字符` | 随便一个openid登录   | `dev_abc123`      |


**说明**：

- 开发模式下，code不需要是真实的微信code
- 传 `dev_13800000001` 会用手机号13800000001登录，如果用户不存在会自动创建
- 生产环境（`app.wechat.skip-api=false`）此方式无效

---



### 1.4 POST `/auth/refresh-token` — 刷新 Token

**Auth**: Optional

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/auth/refresh-token \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
  }'
```

**请求体**：


| 字段             | 类型       | 必填  | 校验          | 说明   |
| -------------- | -------- | --- | ----------- | ---- |
| `refreshToken` | `String` | ✅   | `@NotBlank` | 刷新令牌 |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9.new_access_token",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9.new_refresh_token",
    "expiresIn": 3600,
    "tokenType": "Bearer"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**错误响应**：


| 错误码     | HTTP | 说明      |
| ------- | ---- | ------- |
| `20001` | 401  | Token缺失 |
| `20002` | 401  | Token过期 |
| `20003` | 401  | Token无效 |


---



### 1.5 POST `/auth/bind-phone` — 绑定手机号

**Auth**: Required  
**Roles**: `resident`

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/auth/bind-phone \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "code": "phone_auth_code"
  }'
```

**请求体**：


| 字段              | 类型       | 必填  | 校验              | 说明        |
| --------------- | -------- | --- | --------------- | --------- |
| `code`          | `String` | ✅   | `@NotBlank`     | 手机号授权code |
| `encryptedData` | `String` | ❌   |                 | 加密数据      |
| `iv`            | `String` | ❌   | `@Size(max=24)` | 加密向量      |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "phone": "13812345678",
    "bound": true,
    "boundAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 1.6 GET `/auth/profile` — 获取当前用户信息

**Auth**: Required  
**Roles**: `resident`, `property_admin`, `platform_admin`

**请求示例**：

```bash
curl -X GET https://test.yuanxusoftware.top/api/v1/auth/profile \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "res_b7m9x1",
    "openid": "oXXXXXX",
    "unionid": null,
    "phone": "138****1234",
    "name": "张先生",
    "avatarUrl": "https://cdn.example.com/avatar/res_b7m9x1.jpg",
    "gender": 1,
    "age": 35,
    "maritalStatus": "married",
    "hasChildren": true,
    "userType": "owner",
    "role": "resident",
    "propertySubRole": null,
    "isBuildingLeader": false,
    "buildingLeaderId": null,
    "isCoordinator": false,
    "coordinatorId": null,
    "isSectorLeader": false,
    "sectorLeaderIds": [],
    "isIndividualLeader": false,
    "individualLeaderId": null,
    "isActivityLeader": false,
    "activityLeaderId": null,
    "isResidentMerchant": false,
    "residentMerchantId": null,
    "isDistributor": false,
    "distributorId": null,
    "propertyCompanyId": "pc_demo001",
    "propertyName": "演示物业公司",
    "communityId": "com_demo001",
    "communityName": "演示小区",
    "building": "3栋",
    "floor": "5",
    "unit": "5单元",
    "room": "502",
    "familyId": "fam_demo001",
    "pointBalance": 500,
    "coinBalance": 99.50,
    "coinFrozen": false,
    "coinHidden": false,
    "wechatBound": true,
    "status": "active",
    "createdAt": "2026-03-15 10:00:00",
    "updatedAt": "2026-06-28 14:30:00"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

> 物业侧账号（`role=property_admin`）响应额外带 `propertySubRole`：`property_leader`（物业领导）或 `property_operator`（物业管理员）；非物业账号为 `null`。

> **🆕 v8.13 负责人身份标签**：`isCoordinator`/`coordinatorId`、`isSectorLeader`/`sectorLeaderIds`、`isIndividualLeader`/`individualLeaderId` 为「住户子身份」标签——与 `isBuildingLeader` 同款：`role` 仍为 `resident`，身份由 `coordinators`/`sector_leaders`/`individual_leaders` 表 active 记录判定。前端按这些字段在「我的」页渲染负责人工作台入口（一名业主可同时持有多个标签：楼长 + 业主小店 + 一级代理 + 负责人）。`sectorLeaderIds` 为数组（同一住户可负责多个板块），非板块负责人时为空数组。

> **🆕 v8.14「我的」页四图标标签**：`isActivityLeader`/`activityLeaderId`（组长，身份由 `activity_leader_identities` 表 active 记录判定）、`isResidentMerchant`/`residentMerchantId`（业主小店营业中，`resident_merchant_applications.status=active`）、`isDistributor`/`distributorId`（一级代理，名下有 `is_distributor=1` 且 `approved` 载体）。四图标路由规则：**我的订单**（恒显示）；**业主小店/活动组组长/一级代理商**——对应标签为 `true` 时点击进管理页面，为 `false` 时进申请页面（申请状态可再调 `GET /resident-merchants/my`、`GET /activity-leader-applications/my-application`、`GET /distributors/my/application` 查询）。一名住户可同时持有多个标签，图标互不排斥。

**错误响应**：


| 错误码     | HTTP | 说明       |
| ------- | ---- | -------- |
| `20004` | 403  | 权限不足（角色不在 Roles 列表内） |

> **三角色说明**：`merchant`、`courier` 调用本接口将返回 `20004`，**属预期行为**。这两类角色客户端登录后**不应**调用本接口；用户信息与路由所需字段以 `POST /auth/admin-login` 响应中的 `resident` 对象为准（含 `id`、`role`、`propertySubRole`、`propertyCompanyId` 等）。
>
> **🆕 v8.14 组长标签化影响**：`activity_leader` 已降级为住户子身份（标签），组长账号 `role` 为 `resident`，**可以**正常调用本接口（`isActivityLeader=true`）；存量 JWT 中残留的 `activity_leader` 角色值由服务端归一化为 `resident` 后放行。

---



## 2. 业主/住户模块 (Residents)



### 2.1 GET `/residents` — 业主列表（管理员）

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/residents?page=1&pageSize=20&keyword=张&status=active" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：


| 参数                  | 类型       | 必填  | 默认值          | 说明                                |
| ------------------- | -------- | --- | ------------ | --------------------------------- |
| `page`              | `int`    | ❌   | `1`          | 页码                                |
| `pageSize`          | `int`    | ❌   | `20`         | 每页条数                              |
| `keyword`           | `String` | ❌   |              | 搜索关键词（姓名/手机号）                     |
| `communityId`       | `String` | ❌   |              | 小区ID筛选                            |
| `building`          | `String` | ❌   |              | 楼栋筛选                              |
| `status`            | `String` | ❌   |              | 状态筛选：`active`/`frozen`/`disabled` |
| `role`              | `String` | ❌   |              | 角色筛选（`merchant`/`technician`/`activity_leader`/`resident` 等，业务角色账号列表用 `role=merchant` 等） |
| `merchantName`      | `String` | ❌   |              | 按**关联商家名称**搜索（业务角色账号场景）。匹配 `merchants.name`，**不依赖 `residents.role`**，故被踢出（`resident` 已降级）的商家也可通过店铺名找回对应账号 |
| `propertyCompanyId` | `String` | ❌   |              | 物业公司ID（超管筛选）                      |
| `sort`              | `String` | ❌   | `-createdAt` | 排序字段（支持多字段：`sort=+building,+floor,+unit,+room`，跨页由服务端完成） |

> **🆕 v8.13 负责人标签化影响**：统筹/板块/个体负责人任命后 `role` 保持 `resident`，本列表按 `role=resident` 即可筛出（含负责人账号）；负责人身份请在住户详情（§2.7）通过 `isCoordinator`/`isSectorLeader`/`isIndividualLeader` 标签识别。存量数据执行 `sql/manual_upgrade_leader_identity_tags_20261020.sql` 归一化后，`role=coordinator` 等筛选值将不再命中任何账号。


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "res_b7m9x1",
        "name": "张先生",
        "phone": "138****1234",
        "avatarUrl": "https://cdn.example.com/avatar/res_b7m9x1.jpg",
        "gender": 1,
        "age": 35,
        "maritalStatus": "married",
        "hasChildren": true,
        "role": "resident",
        "userType": "owner",
        "propertyCompanyId": "pc_demo001",
        "communityId": "com_demo001",
        "communityName": "演示小区",
        "building": "3栋",
        "floor": "5",
        "unit": "5单元",
        "room": "502",
        "familyId": "fam_demo001",
        "pointBalance": 500,
        "familyPointBalance": 1200,
        "coinBalance": 99.50,
        "coinFrozen": false,
        "status": "active",
        "createdAt": "2026-03-15 10:00:00",
        "hasArrears": true,
        "arrearsCount": 2,
        "arrearsAmount": 150.00,
        "arrearsPeriodStart": "2026-01-01",
        "arrearsPeriodEnd": "2026-06-30"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 1,
      "totalPages": 1
    }
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**字段说明**：


| 字段                         | 类型           | 说明                                                                                                                                                    |
| -------------------------- | ------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------- |
| `list[].id`                | `String`     | 住户ID                                                                                                                                                  |
| `list[].name`              | `String`     | 姓名                                                                                                                                                    |
| `list[].phone`             | `String`     | 手机号（脱敏）                                                                                                                                               |
| `list[].avatarUrl`         | `String`     | 头像URL                                                                                                                                                 |
| `list[].gender`            | `Integer`    | 性别：0未知 1男 2女                                                                                                                                          |
| `list[].age`               | `Integer`    | 年龄                                                                                                                                                    |
| `list[].maritalStatus`     | `String`     | 婚姻状态：single/married/divorced/widowed                                                                                                                  |
| `list[].hasChildren`       | `Boolean`    | 是否有子女                                                                                                                                                 |
| `list[].role`              | `String`     | 主角色：`resident`/`property_admin`/`platform_admin`/`merchant`/`courier`/`coordinator`/`sector_leader`/`individual_leader`/`activity_leader`/`technician`；物业领导/管理员请另看 `propertySubRole`，**不要**把 `property_leader`/`property_operator` 当作主 `role` |
| `list[].userType`          | `String`     | 用户类型：`owner`（业主）/`tenant`（租户）                                                                                                                         |
| `list[].propertyCompanyId` | `String`     | 物业公司ID                                                                                                                                                |
| `list[].communityId`       | `String`     | 小区ID                                                                                                                                                  |
| `list[].communityName`     | `String`     | 小区名称                                                                                                                                                  |
| `list[].building`          | `String`     | 楼栋
| `list[].floor`             | `String`     |  楼层
| `list[].unit`              | `String`     | 单元                                                                                                                                                    |
| `list[].room`              | `String`     | 房号                                                                                                                                                    |
| `list[].familyId`          | `String`     | 家庭ID                                                                                                                                                  |
| `list[].pointBalance`      | `Integer`    | 积分余额
| `list[].familyPointBalance` | `Integer`    |  家庭积分余额（无家庭为 null）
| `list[].coinBalance`       | `BigDecimal` | 物业币余额                                                                                                                                                 |
| `list[].coinFrozen`        | `Boolean`    | 物业币是否冻结                                                                                                                                               |
| `list[].status`            | `String`     | 状态：`active`/`frozen`/`disabled`                                                                                                                       |
| `list[].createdAt`         | `String`     | 注册时间                                                                                                                                                  |
| `list[].hasArrears`        | `Boolean`    |  是否有欠费（paymentStatus NOT IN 'paid','cancelled'）                                                                                                |
| `list[].arrearsCount`      | `Integer`    |  欠费笔数                                                                                                                                         |
| `list[].arrearsAmount`     | `BigDecimal` |  欠费金额合计（amount - paidAmount）
| `list[].arrearsPeriodStart` | `String`     |  欠费最早账期起始日（YYYY-MM-DD）
| `list[].arrearsPeriodEnd`   | `String`     |  欠费最晚账期结束日（YYYY-MM-DD）


---



### 2.2 POST `/residents` — 管理员创建住户

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/residents \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "name": "张三",
    "phone": "13812345678",
    "userType": "owner",
    "role": "resident",
    "propertyCompanyId": "pc_demo001",
    "communityId": "com_demo001",
    "building": "3栋",
    "unit": "5单元",
    "room": "502"
  }'
```

**请求体**：


| 字段                  | 类型        | 必填  | 校验                                 | 说明                                           |
| ------------------- | --------- | --- | ---------------------------------- | -------------------------------------------- |
| `name`              | `String`  | ✅   | `@NotBlank @Size(max=50)`          | 姓名                                           |
| `phone`             | `String`  | ✅   | `@NotBlank @Pattern("^1\\d{10}$")` | 手机号                                          |
| `avatarUrl`         | `String`  | ❌   | `@Size(max=500)`                   | 头像URL                                        |
| `gender`            | `Integer` | ❌   | `@Min(0) @Max(2)`                  | 0未知 1男 2女                                    |
| `age`               | `Integer` | ❌   | `@Min(0) @Max(150)`                | 年龄（传了 `birthday` 时以后端按生日计算为准，忽略该值）                |
| `birthday`          | `String`  | ❌   | `YYYY-MM-DD`，年份 1920–今年        | 生日；后端据此计算年龄（随时间增长）                         |
| `maritalStatus`     | `String`  | ❌   | `@Size(max=20)`                    | 婚姻状态：`single`/`married`/`divorced`/`widowed` |
| `hasChildren`       | `Boolean` | ❌   |                                    | 是否有子女                                        |
| `userType`          | `String`  | ✅   | `@Pattern("owner|tenant")`         | owner业主/tenant租户                             |
| `role`              | `String`  | ✅   |                                    | 角色（仅支持resident）                              |
| `propertyCompanyId` | `String`  | ✅   |                                    | 物业公司ID                                       |
| `communityId`       | `String`  | ✅   |                                    | 小区ID                                         |
| `building`          | `String`  | ❌   | `@Size(max=20)`                    | 楼栋                                           |
| `floor`             | `String`  | ❌   | `@Size(max=20)`                    | 楼层                                           |
| `unit`              | `String`  | ❌   | `@Size(max=20)`                    | 单元                                           |
| `room`              | `String`  | ❌   | `@Size(max=20)`                    | 房号                                           |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "res_a1b2c3d4",
    "name": "张三",
    "phone": "138****5678",
    "avatarUrl": null,
    "gender": null,
    "age": null,
    "maritalStatus": null,
    "hasChildren": null,
    "userType": "owner",
    "role": "resident",
    "propertyCompanyId": "pc_demo001",
    "propertyName": "演示物业公司",
    "communityId": "com_demo001",
    "communityName": "演示小区",
    "building": "3栋",
    "unit": "5单元",
    "room": "502",
    "familyId": null,
    "pointBalance": 0,
    "coinBalance": 0.00,
    "coinFrozen": false,
    "wechatBound": false,
    "status": "active",
    "createdAt": "2026-07-03 10:30:00"
  },
  "timestamp": "2026-07-03 10:30:00"
}
```

**错误响应**：


| 错误码     | HTTP | 说明              |
| ------- | ---- | --------------- |
| `30005` | 409  | 该手机号在当前物业公司下已存在 |


---



### 2.3 GET `/residents/me/points` — 我的积分余额

**Auth**: Required  
**Roles**: `resident`

**请求示例**：

```bash
curl -X GET https://test.yuanxusoftware.top/api/v1/residents/me/points \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "residentId": "res_b7m9x1",
    "pointEnabled": true,
    "pointDisplayEnabled": true,
    "pointBalance": 500,
    "familyId": "fam_demo001",
    "familyPointBalance": 1200,
    "earnedThisMonth": 150,
    "spentThisMonth": 50,
    "expiringSoon": 100,
    "expiryDate": "2026-09-01"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

> **2026-10-30**：`pointEnabled` 已实际下发（本小区是否启用积分）；`pointDisplayEnabled` 仍未实现，前端请勿依赖。

---



### 2.4 GET `/residents/me/coins` — 我的物业币余额

**Auth**: Required  
**Roles**: `resident`

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "residentId": "res_b7m9x1",
    "coinEnabled": true,
    "coinBalance": 99.50,
    "coinFrozen": false,
    "coinHidden": false,
    "coinDisplayEnabled": true,
    "familyId": "fam_demo001",
    "familyCoinBalance": 250.00,
    "earnedThisMonth": 30.00,
    "spentThisMonth": 10.00,
    "expiringSoon": 20.00,
    "expiryDate": "2026-12-01"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

> **2026-10-30**：`coinEnabled` 已实际下发（本小区是否启用物业币）；`coinDisplayEnabled` 为物业币入口展示开关。

---



### 2.5 GET `/residents/me` — 获取当前用户详情

**Auth**: Required  
**Roles**: `resident`, `property_admin`, `platform_admin`

**成功响应**：同 `/auth/profile`，额外包含 `totalConsumption`、`totalOrders`。

> **🆕 v8.13**：响应含负责人身份标签 `isCoordinator`/`coordinatorId`、`isSectorLeader`/`sectorLeaderIds`、`isIndividualLeader`/`individualLeaderId`（住户子身份，`role` 仍为 `resident`，详见 §1.6 说明），与既有 `isDistributor`（一级代理）并列，供「我的」页渲染全部身份入口。

> **🆕 v8.14**：响应新增 `isActivityLeader`/`activityLeaderId`（组长标签，`activity_leader_identities` active 记录判定）、`isResidentMerchant`/`residentMerchantId`（业主小店营业中），与 `isDistributor` 共同构成「我的」页四图标的路由依据（详见 §1.6）。

> **生日/年龄口径**：详情返回 `birthday`（`YYYY-MM-DD`）；有生日时 `age` 由后端按生日实时计算（随时间增长），否则回退库内 `age`。

---



### 2.6 PUT `/residents/me` — 更新当前用户信息

**Auth**: Required  
**Roles**: `resident`

**请求示例**：

```bash
curl -X PUT https://test.yuanxusoftware.top/api/v1/residents/me \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "name": "张三",
    "gender": 1,
    "age": 30
  }'
```

**请求体**：


| 字段              | 类型        | 必填  | 校验                  | 说明                                           |
| --------------- | --------- | --- | ------------------- | -------------------------------------------- |
| `name`          | `String`  | ❌   | `@Size(max=50)`     | 姓名                                           |
| `avatarUrl`     | `String`  | ❌   | `@Size(max=500)`    | 头像URL                                        |
| `gender`        | `Integer` | ❌   | `@Min(0) @Max(2)`   | 0未知 1男 2女                                    |
| `age`           | `Integer` | ❌   | `@Min(0) @Max(150)` | 年龄（传了 `birthday` 时以后端按生日计算为准，忽略该值）            |
| `birthday`      | `String`  | ❌   | `YYYY-MM-DD`，年份 1920–今年 | 生日；后端据此计算年龄（随时间增长）                    |
| `maritalStatus` | `String`  | ❌   | `@Size(max=20)`     | 婚姻状态：`single`/`married`/`divorced`/`widowed` |
| `hasChildren`   | `Boolean` | ❌   |                     | 是否有子女                                        |
| `building`      | `String`  | ❌   | `@Size(max=20)`     | 楼栋（**仅管理端角色可改，住户提交将被忽略**）                   |
| `floor`         | `String`  | ❌   | `@Size(max=20)`     | 楼层（**仅管理端角色可改，住户提交将被忽略**）                   |
| `unit`          | `String`  | ❌   | `@Size(max=20)`     | 单元（**仅管理端角色可改，住户提交将被忽略**）                   |
| `room`          | `String`  | ❌   | `@Size(max=20)`     | 房号（**仅管理端角色可改，住户提交将被忽略**）                   |

> **地址修改规则（2026-08 调整）**：`building`/`unit`/`room` 等地址字段仅管理端角色可修改；住户需变更楼栋请走 §2.6.2 楼栋变更申请，审批通过后由物业写入。


**成功响应**：更新后的用户详情

---

### 2.6.1 PUT `/residents/me/community` — 完善/绑定小区

> **（对应契约审计 A1/A3）**：注册时未选择小区、或需变更所在小区的住户，可通过本接口自主绑定。绑定后论坛、社区等按 `community_id` 过滤的能力方可正常使用。
>
> **（2026-09-10）** 该接口此前**后端未实现**（调用返回 404），现已补齐，行为与本小节契约一致：校验 `communityId` 以 `com_` 开头、小区存在且属于当前住户所属物业公司，通过后写入 `residents.community_id` 并返回住户详情。

**Auth**: Required  
**Roles**: `resident`

**请求示例**：

```bash
curl -X PUT https://test.yuanxusoftware.top/api/v1/residents/me/community \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "communityId": "com_demo001"
  }'
```

**请求体**：


| 字段           | 类型      | 必填  | 校验                    | 说明                                       |
| ------------ | ------- | --- | --------------------- | ---------------------------------------- |
| `communityId` | `String` | ✅   | `@NotBlank @Size(max=32)` | 小区ID（须以 `com_` 开头且存在；与当前住户所属物业不一致时返回 10002） |

**成功响应**：绑定后的用户详情（同 `GET /residents/me`，含 `communityId`/`communityName`）

**错误响应**：


| 错误码   | HTTP | 说明              |
| ----- | ---- | --------------- |
| `10002` | 400  | 小区不存在 / 小区与所属物业不匹配 |
| `20003` | 401  | 未登录 / Token 缺失    |

---



### 2.6.2 楼栋变更申请（住户端）
<a id="262-楼栋变更申请"></a>

> **背景（2026-08 新增）**：住户需要更换楼栋/单元/房号时，提交申请进入**待物业审批**，后端**不会立即修改** `residents` 的楼栋地址；审批通过后才由物业端写入。资料保存（§2.6 `PUT /residents/me`）不再允许住户直接修改已生效楼栋。

#### 2.6.2.1 POST `/residents/me/building-change` — 提交楼栋变更申请

> **（2026-09-10）** 后端同时支持复数路径 `POST /residents/me/building-changes`（等价实现）。前端请统一使用本路径（单数）。

**Auth**: Required  
**Roles**: `resident`

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/residents/me/building-change \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "communityId": "com_demo001",
    "building": "3",
    "unit": "2",
    "floor": "5",
    "room": "501"
  }'
```

**请求体**：

| 字段          | 类型       | 必填  | 校验                  | 说明                                    |
| ----------- | -------- | --- | ------------------- | ------------------------------------- |
| `communityId` | `String` | ✅   | `com_` 开头且存在       | 目标小区ID（须与住户所属物业一致）                    |
| `building`  | `String` | ❌   | `@Size(max=20)`     | 目标楼栋                                  |
| `unit`      | `String` | ❌   | `@Size(max=20)`     | 目标单元                                  |
| `floor`     | `String` | ❌   | `@Size(max=20)`     | 目标楼层                                  |
| `room`      | `String` | ❌   | `@Size(max=20)`     | 目标房号                                  |

**成功响应**（`BuildingChangeResponse`）：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "bcr_xxxxx",
    "status": "pending",
    "communityId": "com_demo001",
    "building": "3",
    "unit": "2",
    "floor": "5",
    "room": "501",
    "rejectReason": null,
    "appliedAt": "2026-08-17T10:00:00Z"
  },
  "timestamp": "2026-08-17 18:00:00"
}
```

**业务规则**：

- 提交后状态为 `pending`，**不修改** `residents.building/unit/floor/room`
- 已有待审批申请（`pending`/`pending_audit`）时重复提交返回 10002「已有待审批的楼栋变更申请」
- 目标小区不属于当前物业时返回 10002

**错误响应**：

| 错误码   | HTTP | 说明                        |
| ----- | ---- | ------------------------- |
| `10002` | 400  | 小区不存在 / 小区不属于当前物业 / 已有待审批申请 |
| `20003` | 401  | 未登录 / Token 缺失              |

---

#### 2.6.2.2 GET `/residents/me/building-change` — 查询最近一次楼栋变更申请

> **（2026-09-10）** 该接口此前**后端未实现**（调用返回 404），现已补齐：返回当前住户最近一次申请（含 `pending` / `approved` / `rejected`），无申请时 `data` 为 `null`。
> 另有 `GET /residents/me/building-changes` 返回**全部**申请列表（按申请时间倒序），两者并存。

**Auth**: Required  
**Roles**: `resident`

**成功响应**：返回当前用户**最近一次**楼栋变更申请（同 §2.6.2.1 响应结构，含 `status`/`rejectReason`）；无申请时 `data` 为 `null`。

`status` 取值：`pending`（待审批）/ `approved`（已通过）/ `rejected`（已驳回），兼容 `pending_audit`。

---



### 2.6.3 楼栋变更审批（管理端）
<a id="263-楼栋变更审批"></a>

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`, `coordinator`, `sector_leader`, `individual_leader`

#### 2.6.3.1 GET `/admin/building-changes` — 楼栋变更申请列表

**Query 参数**：`page` / `pageSize` / `status`（`pending`/`approved`/`rejected`）/ `propertyCompanyId`（超管可指定）

**成功响应**（分页 `BuildingChangeListItemResponse`）：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "bcr_xxxxx",
        "residentId": "res_b7m9x1",
        "residentName": "张先生",
        "residentPhone": "138****1234",
        "currentBuilding": "1栋",
        "currentUnit": "1单元",
        "currentRoom": "101",
        "communityId": "com_demo001",
        "building": "3",
        "unit": "2",
        "floor": "5",
        "room": "501",
        "status": "pending",
        "rejectReason": null,
        "appliedAt": "2026-08-17T10:00:00Z"
      }
    ],
    "pagination": { "page": 1, "pageSize": 20, "total": 1, "totalPages": 1 }
  },
  "timestamp": "2026-08-17 18:00:00"
}
```

#### 2.6.3.2 POST `/admin/building-changes/{id}/approve` — 通过申请

审批通过后**立即写入住户楼栋地址**（`communityId`/`building`/`floor`/`unit`/`room`），申请状态 → `approved`。

#### 2.6.3.3 POST `/admin/building-changes/{id}/reject` — 驳回申请

**Query 参数**：`reason`（必填）——驳回原因，写入申请 `rejectReason`，状态 → `rejected`，住户地址不变。

---



### 2.6.4 住户主动转给物业（积分 / 物业币）
<a id="264-转给物业"></a>

> **（2026-08 新增）**：住户在 App 主动填写积分或物业币数量转给物业：**立即扣减住户个人余额**并记录 `transfer_to_property` 流水（物业侧账本），管理端可对账查看（见 §42.2），**无需审批**。
> 非住户互转、非支付抵扣、非管理端代扣。仅扣**个人余额**（家庭池不参与）。
>
> **（2026-09-10 修复）** 本组接口此前仅挂载在 `POST /points/transfer-to-property` 与 `POST /property-coins/transfer-to-property`，按本文档路径调用返回 **404**。现已补齐文档路径，并**保留旧路径作为兼容别名**；同时统一流水 `source = transfer_to_property`（此前写入 `transfer_property`，导致管理端 §42.2 对账列表查不到数据），历史数据由数据库脚本 `V15__transfer_to_property_source.sql` / `sql/2026-09-10_转给物业接口修复_数据库脚本.sql` 修正。

#### 2.6.4.1 POST `/residents/me/points/transfer-to-property` — 转给物业（积分）

**Auth**: Required  
**Roles**: `resident`

**请求体**：

| 字段          | 类型      | 必填  | 校验                    | 说明   |
| ----------- | ------- | --- | --------------------- | ---- |
| `pointAmount` | `Integer` | ✅   | `@NotNull @Min(1)`     | 转出积分数量（>0） |
| `remark`     | `String` | ❌   | `@Size(max=200)`       | 备注（可选） |

**成功响应**（`TransferToPropertyPointResponse`）：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "recordId": "pt_xxxxx",
    "residentId": "res_b7m9x1",
    "pointAmount": 100,
    "balanceAfter": 400,
    "transferredAt": "2026-08-17T10:00:00Z"
  },
  "timestamp": "2026-08-17 18:00:00"
}
```

**兼容别名**：`POST /points/transfer-to-property`（同一实现，建议前端统一使用本路径）

**业务规则**：

- **即时扣款、无需审批**：成功后立即扣减个人积分余额，并在 `point_records` 写入一条 `point_type=spend`、`source=transfer_to_property` 的流水（物业端 §42.2 对账列表据此过滤）
- 仅扣**个人余额**，家庭池不参与
- 积分功能关闭（`property_companies.point_enabled=false`，§51）时返回 `97070`「积分功能未开启」
- 个人积分余额不足返回 `40001`

**错误响应**：

| 错误码   | HTTP | 说明            |
| ----- | ---- | ------------- |
| `40001` | 400  | 积分余额不足        |
| `97070` | 400  | 积分功能未开启（原文档误写 `40003`，实际返回 `97070`） |
| `10002` | 400  | 参数校验失败（`pointAmount` 为空或 < 1、`remark` 超 200 字） |
| `20003` | 401  | 未登录 / Token 无效 |

#### 2.6.4.2 POST `/residents/me/coins/transfer-to-property` — 转给物业（物业币）

**Auth**: Required  
**Roles**: `resident`

**请求体**：

| 字段          | 类型         | 必填  | 校验                    | 说明   |
| ----------- | ---------- | --- | --------------------- | ---- |
| `coinAmount` | `BigDecimal` | ✅   | `@NotNull @DecimalMin(0.01)` | 转出物业币数量（>0） |
| `remark`     | `String`    | ❌   | `@Size(max=200)`       | 备注（可选） |

**成功响应**（`TransferToPropertyCoinResponse`）：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "recordId": "coin_xxxxx",
    "residentId": "res_b7m9x1",
    "coinAmount": 50.00,
    "balanceAfter": 49.50,
    "transferredAt": "2026-08-17T10:00:00Z"
  },
  "timestamp": "2026-08-17 18:00:00"
}
```

**兼容别名**：`POST /property-coins/transfer-to-property`（同一实现，建议前端统一使用本路径）

**业务规则**：

- **账户边界**：仅扣减**家庭物业币**（家庭池）；**个人物业币不可转赠**（个人名义持有，不从集体划转）。
- **即时扣款、无需审批**：成功后立即从家庭池扣减，并在 `property_coin_records` 写入一条 `coin_type=spend`、`source=transfer_to_property` 的流水（物业端 §42.2 对账列表据此过滤）
- 用户**未加入家庭**返回 `10002`「您尚未加入家庭，无家庭物业币可转赠物业」
- 家庭余额不足返回 `50001`；物业币功能关闭（`property_companies.coin_enabled=false`，§51）时返回 `97071`「物业币功能未开启」；物业币被冻结返回 `93003`

**错误响应**：

| 错误码   | HTTP | 说明            |
| ----- | ---- | ------------- |
| `50001` | 400  | 物业币余额不足       |
| `97071` | 400  | 物业币功能未开启（原文档误写 `50006`，实际返回 `97071`） |
| `93003` | 400  | 物业币已冻结，无法使用  |
| `10002` | 400  | 参数校验失败（`coinAmount` 为空或 < 0.01、`remark` 超 200 字） |
| `20003` | 401  | 未登录 / Token 无效 |

---



### 2.7 GET `/residents/{id}` — 业主详情

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`, `resident`（本人）

**路径参数**：


| 参数   | 类型       | 必填  | 说明   |
| ---- | -------- | --- | ---- |
| `id` | `String` | ✅   | 业主ID |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "res_b7m9x1",
    "openid": "oXXXXXX",
    "unionid": null,
    "phone": "138****1234",
    "name": "张先生",
    "avatarUrl": "https://cdn.example.com/avatar/res_b7m9x1.jpg",
    "gender": 1,
    "age": 35,
    "birthday": "1991-03-15",
    "maritalStatus": "married",
    "hasChildren": true,
    "userType": "owner",
    "role": "resident",
    "propertyCompanyId": "pc_demo001",
    "propertyName": "演示物业公司",
    "communityId": "com_demo001",
    "communityName": "演示小区",
    "building": "3栋",
    "floor": "5",
    "unit": "5单元",
    "room": "502",
    "familyId": "fam_demo001",
    "pointBalance": 500,
    "coinBalance": 99.50,
    "coinFrozen": false,
    "coinHidden": false,
    "wechatBound": true,
    "totalConsumption": 1500.00,
    "totalOrders": 15,
    "status": "active",
    "createdAt": "2026-03-15 10:00:00",
    "updatedAt": "2026-06-28 14:30:00",
    "hasArrears": false,
    "arrearsCount": 0,
    "arrearsAmount": 0.00,
    "isCoordinator": false,
    "coordinatorId": null,
    "isSectorLeader": false,
    "sectorLeaderIds": [],
    "isIndividualLeader": false,
    "individualLeaderId": null
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

> **说明**：详情响应新增 `hasArrears`/`arrearsCount`/`arrearsAmount` 三字段，与列表 §2.1 一致。

> **🆕 v8.13**：详情含负责人身份标签 `isCoordinator`/`coordinatorId`、`isSectorLeader`/`sectorLeaderIds`、`isIndividualLeader`/`individualLeaderId`（住户子身份，`role` 仍为 `resident`，详见 §1.6），`/residents/me` 与 `/residents/{id}` 一致。

---



### 2.8 PUT `/residents/{id}` — 更新业主信息

**Auth**: Required  
**Roles**: `resident`（本人）, `property_admin`, `platform_admin`, `coordinator`, `sector_leader`, `individual_leader`

**请求示例**：

```bash
curl -X PUT https://test.yuanxusoftware.top/api/v1/residents/res_b7m9x1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "name": "张三",
    "gender": 1,
    "age": 30
  }'
```

**路径参数**：


| 参数   | 类型       | 必填  | 说明   |
| ---- | -------- | --- | ---- |
| `id` | `String` | ✅   | 业主ID |


**请求体**：


| 字段              | 类型        | 必填  | 校验                  | 说明                                           |
| --------------- | --------- | --- | ------------------- | -------------------------------------------- |
| `name`          | `String`  | ❌   | `@Size(max=50)`     | 姓名                                           |
| `avatarUrl`     | `String`  | ❌   | `@Size(max=500)`    | 头像URL                                        |
| `gender`        | `Integer` | ❌   | `@Min(0) @Max(2)`   | 0未知 1男 2女                                    |
| `age`           | `Integer` | ❌   | `@Min(0) @Max(150)` | 年龄（传了 `birthday` 时以后端按生日计算为准，忽略该值）            |
| `birthday`      | `String`  | ❌   | `YYYY-MM-DD`，年份 1920–今年 | 生日；后端据此计算年龄（随时间增长）                    |
| `maritalStatus` | `String`  | ❌   | `@Size(max=20)`     | 婚姻状态：`single`/`married`/`divorced`/`widowed` |
| `hasChildren`   | `Boolean` | ❌   |                     | 是否有子女                                        |
| `building`      | `String`  | ❌   | `@Size(max=20)`     | 楼栋（仅管理端角色可改）                               |
| `floor`         | `String`  | ❌   | `@Size(max=20)`     | 楼层（仅管理端角色可改）                               |
| `unit`          | `String`  | ❌   | `@Size(max=20)`     | 单元（仅管理端角色可改）                               |
| `room`          | `String`  | ❌   | `@Size(max=20)`     | 房号（仅管理端角色可改）                               |
| `role`          | `String`  | ❌   | 仅管理端角色可传        | 角色变更（管理端专用），仅接受 `RoleGuard.VALID_ROLES`（`resident`/`merchant`/`courier`/`technician`/`property_admin`/`community_admin` 等）。**v8.14 起不再接受 `activity_leader`/`coordinator`/`sector_leader`/`individual_leader`**——组长与负责人均为住户子身份，只能通过管理端任命或申请审核获得（组长见 §101.2，负责人见 §37.2/§39.2/§37.6），仿楼长（`building_leader` 亦不可通过本字段授予） |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "res_b7m9x1",
    "name": "张三",
    "gender": 1,
    "age": 30,
    "building": "3栋",
    "unit": "5单元",
    "room": "502",
    "updatedAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 2.9 PATCH `/residents/{id}/status` — 冻结/解冻/禁用

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**请求示例**：

```bash
curl -X PATCH https://test.yuanxusoftware.top/api/v1/residents/res_b7m9x1/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "status": "frozen",
    "reason": "异常登录"
  }'
```

**请求体**：


| 字段       | 类型       | 必填  | 校验                          | 说明     |
| -------- | -------- | --- | --------------------------- | ------ |
| `status` | `String` | ✅   | `@NotBlank @Pattern("active | frozen |
| `reason` | `String` | ❌   | `@Size(max=200)`            | 操作原因   |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "res_b7m9x1",
    "status": "frozen",
    "updatedAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 2.10 DELETE `/residents/{id}` — 删除住户（软删除）

> **适用范围（重要）**：本接口是**除「商家 / 活动组长 / 技工 / 配送员」四类业务角色外**的所有账号删除入口，包括：**区域负责人（`sector_leader`）、物业领导（`property_leader`）、统筹 / 板块 / 项目负责人、普通住户** 等，删除为**软删除**，历史业务数据完整保留。
> 四类业务角色账号（`merchant` / `activity_leader` / `technician` / `courier`）请使用 **§31.2.2 `DELETE /admin/role-accounts/{id}`（硬删除）**，勿走本接口。

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`, `coordinator`, `sector_leader`, `individual_leader`

**请求示例**：

```bash
curl -X DELETE https://test.yuanxusoftware.top/api/v1/residents/res_b7m9x1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**路径参数**：


| 参数   | 类型       | 必填  | 说明   |
| ---- | -------- | --- | ---- |
| `id` | `String` | ✅   | 住户ID（**必须使用列表返回的 `residentId`，前缀 `res_`**；不要传区域负责人记录中的其他 id 字段） |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": null,
  "timestamp": "2026-07-01 15:23:33"
}
```

**业务规则**：

- 管理员可直接删除住户，无需冷静期
- 执行软删除：设置 `deletedAt` 时间戳，状态改为 `deleted`
- **删除后该账号在列表查询中自动隐藏**（列表默认过滤已删除记录），前端无需再手动移除
- 物业管理员只能删除本公司下的住户
- 平台管理员可删除任意住户
- **本接口是软删除**：不会物理删除 `residents` 记录，不会因外键约束（订单 / 积分 / 提现等）报错，业务数据完整保留用于审计 / 对账
- 若账号是 `merchant` / `activity_leader` / `technician` / `courier` 业务角色，请改用 §31.2.2 硬删除接口

**错误响应**：


| 错误码     | HTTP | 说明      |
| ------- | ---- | ------- |
| `30001` | 404  | 住户不存在   |
| `40003` | 403  | 无权操作该住户 |
| `10001` | 400  | ID格式不正确（`id` 非 `res_` 开头——说明传了非 `residentId` 的其他字段） |
| — | 403 | 角色无权限（仅 `property_admin` / `platform_admin` / `coordinator` / `sector_leader` / `individual_leader` 可调用） |


---



## 3. 家庭模块 (Families)



### 3.1 POST `/families` — 创建家庭

**Auth**: Required  
**Roles**: `resident`（业主）

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/families \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "name": "张家"
  }'
```

**请求体**：


| 字段            | 类型       | 必填  | 校验                               | 说明   |
| ------------- | -------- | --- | -------------------------------- | ---- |
| `name`        | `String` | ✅   | `@NotBlank @Size(min=1, max=50)` | 家庭名称 |
| `building`    | `String` | ❌   | `@Size(max=20)`                  | **忽略**：家庭地址强制与户主资料一致（2026-08 起不再采用请求体地址） |
| `unit`        | `String` | ❌   | `@Size(max=20)`                  | **忽略**：同上 |
| `room`        | `String` | ❌   | `@Size(max=20)`                  | **忽略**：同上 |
| `communityId` | `String` | ❌   |                                 | **忽略**：同上 |

> **业务规则（2026-08）**：创建家庭时**强制户主地址**——家庭 `communityId`/`building`/`unit`/`room` 一律取自户主 `residents` 资料，请求体传入的地址字段被忽略。
> 户主未完善小区/楼栋/房号时返回 `10002`（提示先到个人资料页提交楼栋申请，经物业审批后生效）；
> 家庭地址与户主资料保持一致，确保家庭加人同房号规则（同一小区+楼栋+单元+房号，忽略楼层）可校验。


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "fam_demo001",
    "name": "张家",
    "ownerId": "res_b7m9x1",
    "ownerName": "张先生",
    "propertyCompanyId": "pc_demo001",
    "communityId": "com_demo001",
    "communityName": "演示小区",
    "building": "3栋",
    "unit": "5单元",
    "room": "502",
    "memberCount": 1,
    "pointBalance": 0,
    "coinBalance": 0.00,
    "voteWeight": 1,
    "auditStatus": "pending",
    "status": "active",
    "createdAt": "2026-07-01 15:23:33",
    "updatedAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**错误响应**：


| 错误码     | HTTP | 说明            |
| ------- | ---- | ------------- |
| `30002` | 409  | 家庭人数超限（最多15人） |


---



### 3.2 GET `/families/me` — 获取我的家庭信息

**Auth**: Required  
**Roles**: `resident`

**成功响应**：


| 字段                       | 类型           | 说明                                                     |
| ------------------------ | ------------ | ------------------------------------------------------ |
| `id`                     | `String`     | 家庭ID                                                   |
| `name`                   | `String`     | 家庭名称                                                   |
| `ownerId`                | `String`     | 户主ID                                                   |
| `ownerName`              | `String`     | 户主姓名                                                   |
| `propertyCompanyId`      | `String`     | 物业公司ID                                                 |
| `communityId`            | `String`     | 小区ID                                                   |
| `communityName`          | `String`     | 小区名称                                                   |
| `building`               | `String`     | 楼栋                                                     |
| `unit`                   | `String`     | 单元                                                     |
| `room`                   | `String`     | 房号                                                     |
| `memberCount`            | `Integer`    | 成员人数                                                   |
| `pointBalance`           | `Integer`    | 家庭积分余额                                                 |
| `coinBalance`            | `BigDecimal` | 家庭物业币余额                                                |
| `voteWeight`             | `Integer`    | 投票权重                                                   |
| `auditStatus`            | `String`     | 审核状态                                                   |
| `status`                 | `String`     | 状态                                                     |
| `members[]`              | `Array`      | 家庭成员列表                                                 |
| `members[].id`           | `String`     | 成员ID                                                   |
| `members[].name`         | `String`     | 成员姓名                                                   |
| `members[].phone`        | `String`     | 手机号（脱敏）                                                |
| `members[].avatarUrl`    | `String`     | 头像URL                                                  |
| `members[].relation`     | `String`     | 关系：`owner`/`spouse`/`child`/`parent`/`sibling`/`other` |
| `members[].isOwner`      | `Boolean`    | 是否户主                                                   |
| `members[].pointBalance` | `Integer`    | 成员积分余额                                                 |
| `members[].coinBalance`  | `BigDecimal` | 成员物业币余额                                                |
| `members[].status`       | `String`     | 状态                                                     |
| `members[].joinedAt`     | `String`     | 加入时间                                                   |
| `createdAt`              | `String`     | 创建时间                                                   |
| `updatedAt`              | `String`     | 更新时间                                                   |


---



### 3.3 PUT `/families/me` — 更新我的家庭信息

**Auth**: Required  
**Roles**: `resident`（仅户主）

**请求示例**：

```bash
curl -X PUT https://test.yuanxusoftware.top/api/v1/families/me \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "name": "张家",
    "building": "3栋",
    "unit": "5单元",
    "room": "502"
  }'
```

**请求体**：


| 字段         | 类型       | 必填  | 校验              | 说明   |
| ---------- | -------- | --- | --------------- | ---- |
| `name`     | `String` | ❌   | `@Size(max=50)` | 家庭名称 |
| `building` | `String` | ❌   | `@Size(max=20)` | 楼栋   |
| `unit`     | `String` | ❌   | `@Size(max=20)` | 单元   |
| `room`     | `String` | ❌   | `@Size(max=20)` | 房号   |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "fam_demo001",
    "name": "张家",
    "building": "3栋",
    "unit": "5单元",
    "room": "502",
    "updatedAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 3.4 GET `/families/{id}/members` — 家庭成员列表

**Auth**: Required  
**Roles**: `resident`（家庭成员）, `property_admin`, `platform_admin`

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "res_b7m9x1",
        "name": "张先生",
        "phone": "138****1234",
        "avatarUrl": "https://cdn.example.com/avatar/res_b7m9x1.jpg",
        "relation": "owner",
        "isOwner": true,
        "pointBalance": 500,
        "familyPointBalance": 1200,
        "coinBalance": 99.50,
        "status": "active",
        "joinedAt": "2026-03-15 10:00:00"
      }
    ]
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 3.5 POST `/families/{id}/members` — 添加家庭成员

**Auth**: Required  
**Roles**: `resident`（仅户主）

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/families/fam_demo001/members \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "phone": "13900001111",
    "name": "李女士",
    "relation": "spouse"
  }'
```

**请求体**：


| 字段         | 类型       | 必填  | 校验                                 | 说明                                                     |
| ---------- | -------- | --- | ---------------------------------- | ------------------------------------------------------ |
| `phone`    | `String` | ✅   | `@NotBlank @Pattern("^1\\d{10}$")` | 手机号                                                    |
| `name`     | `String` | ❌   | `@Size(max=50)`                    | 姓名                                                     |
| `relation` | `String` | ✅   | `@NotBlank`                        | 关系：`owner`/`spouse`/`child`/`parent`/`sibling`/`other` |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "memberId": "res_xxxxxx",
    "familyId": "fam_demo001",
    "name": "李女士",
    "phone": "139****1111",
    "relation": "spouse",
    "isOwner": false,
    "joinedAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**错误响应**：


| 错误码     | HTTP | 说明     |
| ------- | ---- | ------ |
| `30001` | 404  | 业主不存在  |
| `30002` | 409  | 家庭人数超限 |


**业务规则（2026-08 新增）**：

- **只能把同一房源的人加进同一家庭**：被添加成员必须与家庭所在地址一致，即 `communityId + building + unit + room` 四项全部相同
- 比较时**忽略楼层** `floor`（避免 `5` / `5楼` / 空 匹配失败），并对楼栋/单元/房号做数字归一化（如 `3栋` 与 `3` 视为一致）
- 地址不一致返回 `10002`「只能添加同一房号的住户（同一小区+楼栋+单元+房号）」
- 创建家庭（§3.1）时家庭地址即取户主地址，后续加成员按上述规则校验


---



### 3.6 DELETE `/families/{id}/members/{memberId}` — 移除家庭成员

**Auth**: Required  
**Roles**: `resident`（仅户主）

**请求示例**：

```bash
curl -X DELETE "https://test.yuanxusoftware.top/api/v1/families/fam_demo001/members/res_xxxxxx?reason=搬离" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**查询参数**：


| 参数       | 类型       | 必填  | 说明   |
| -------- | -------- | --- | ---- |
| `reason` | `String` | ❌   | 移除原因 |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "familyId": "fam_demo001",
    "memberId": "res_xxxxxx",
    "removedAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**错误响应**：


| 错误码     | HTTP | 说明       |
| ------- | ---- | -------- |
| `30001` | 404  | 家庭或成员不存在 |
| `40003` | 403  | 非户主无权操作  |


---



### 3.7 POST `/families/me/leave` — 退出家庭

**Auth**: Required  
**Roles**: `resident`（**非户主**）

> **与 §3.6 区别**：§3.6 为户主**移除**其他成员；本接口为当前用户**主动退出**家庭。

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/families/me/leave \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**请求体**：无

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "memberId": "res_k3p5n2",
    "familyId": "fam_demo001",
    "removedAt": "2026-07-08T06:00:00.000Z",
    "pointsHandling": "TRANSFER",
    "coinsHandling": "TRANSFER"
  },
  "timestamp": "2026-07-08 14:00:00"
}
```

**响应字段**：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `memberId` | `String` | 退出成员（当前用户）ID |
| `familyId` | `String` | 原家庭 ID |
| `removedAt` | `String` | 退出时间（ISO 8601） |
| `pointsHandling` | `String` | 积分处理方式，固定 `TRANSFER`（按成员数均分带走） |
| `coinsHandling` | `String` | 物业币处理方式，固定 `TRANSFER`（按成员数均分带走） |

**业务规则**：

1. 仅**非户主**可调用；户主须先转让户主或解散家庭。
2. 用户须已加入家庭。
3. 退出时家庭积分与物业币按成员数均分，退出成员带走属于自己的份额。
4. 退出后 `familyId`、`familyRelation` 清空。

**错误响应**：

| 错误码 | HTTP | 说明 |
| --- | --- | --- |
| `20004` | 403 | 户主不可退出家庭 |
| `30001` | 404 | 未加入任何家庭 / 家庭不存在 |
| `10002` | 400 | 家庭至少保留一名成员（仅剩 1 人时不允许退出） |

---



## 4. 积分模块 (Points)



### 4.1 GET `/points/balance` — 获取积分余额

**Auth**: Required  
**Roles**: `resident`

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "pointBalance": 500,
    "familyPointBalance": 1200,
    "familyId": "fam_demo001",
    "todayEarned": 20,
    "todaySpent": 0,
    "expiringPoints": 100,
    "expiringDate": "2026-09-01"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 4.2 GET `/points/family-balance` — 获取家庭积分余额

**Auth**: Required  
**Roles**: `resident`

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "familyId": "fam_demo001",
    "familyName": "张家",
    "pointBalance": 1200,
    "memberCount": 3,
    "members": [
      {
        "id": "res_b7m9x1",
        "name": "张先生",
        "pointBalance": 500,
        "relation": "owner"
      },
      {
        "id": "res_xxxxxx",
        "name": "李女士",
        "pointBalance": 700,
        "relation": "spouse"
      }
    ]
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 4.3 GET `/points/records` — 积分记录列表

**Auth**: Required  
**Roles**: `resident`

**Query 参数**：


| 参数          | 类型       | 必填  | 默认值          | 说明                                           |
| ----------- | -------- | --- | ------------ | -------------------------------------------- |
| `page`      | `int`    | ❌   | `1`          | 页码                                           |
| `pageSize`  | `int`    | ❌   | `20`         | 每页条数                                         |
| `pointType` | `String` | ❌   |              | `earn`/`spend`/`transfer`/`expire`/`pool_in` |
| `source`    | `String` | ❌   |              | 来源筛选                                         |
| `startDate` | `String` | ❌   |              | 开始日期                                         |
| `endDate`   | `String` | ❌   |              | 结束日期                                         |
| `familyId`  | `String` | ❌   |              | 家庭ID                                         |
| `sort`      | `String` | ❌   | `-createdAt` | 排序                                           |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "pt_demo001",
        "residentId": "res_b7m9x1",
        "familyId": "fam_demo001",
        "pointType": "earn",
        "amount": 20,
        "balance": 500,
        "source": "order",
        "sourceId": "ord_demo001",
        "description": "订单消费获得积分",
        "operatorId": null,
        "createdAt": "2026-07-01 14:30:00"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 1,
      "totalPages": 1
    }
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 4.4 GET `/points/expiring` — 即将过期积分

**Auth**: Required  
**Roles**: `resident`

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "expiringPoints": 100,
    "expiringDate": "2026-09-01"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 4.5 POST `/points/transfer` — 家庭内积分转移

**Auth**: Required  
**Roles**: `resident`

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/points/transfer \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "familyId": "fam_demo001",
    "toMemberId": "res_xxxxxx",
    "amount": 100,
    "remark": "给孩子的积分"
  }'
```

**请求体**：


| 字段           | 类型        | 必填  | 校验                 | 说明     |
| ------------ | --------- | --- | ------------------ | ------ |
| `familyId`   | `String`  | ✅   | `@NotBlank`        | 家庭ID   |
| `toMemberId` | `String`  | ✅   | `@NotBlank`        | 目标成员ID |
| `amount`     | `Integer` | ✅   | `@NotNull @Min(1)` | 转移数量   |
| `remark`     | `String`  | ❌   | `@Size(max=200)`   | 备注     |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "recordId": "pt_demo002",
    "fromMemberId": "res_b7m9x1",
    "toMemberId": "res_xxxxxx",
    "amount": 100,
    "fromBalance": 400,
    "toBalance": 100,
    "transferredAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**错误响应**：


| 错误码     | HTTP | 说明     |
| ------- | ---- | ------ |
| `40001` | 400  | 积分余额不足 |


---



### 4.6 POST `/points/deduct-fee` — 积分抵扣物业费

**Auth**: Required  
**Roles**: `resident`

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/points/deduct-fee \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "propertyFeeId": "pf_demo001",
    "points": 100
  }'
```

**请求体**：


| 字段              | 类型        | 必填  | 校验                 | 说明      |
| --------------- | --------- | --- | ------------------ | ------- |
| `propertyFeeId` | `String`  | ✅   | `@NotBlank`        | 物业费账单ID |
| `points`        | `Integer` | ✅   | `@NotNull @Min(1)` | 抵扣积分数量  |
| `familyId`      | `String`  | ❌   |                    | 使用家庭积分池 |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "propertyFeeId": "pf_demo001",
    "pointsUsed": 100,
    "deductedAmount": 1.00,
    "remainingAmount": 99.00,
    "paymentStatus": "partial",
    "residentBalance": 400,
    "familyBalance": 1100,
    "deductedAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**错误响应**：


| 错误码     | HTTP | 说明       |
| ------- | ---- | -------- |
| `40001` | 400  | 积分余额不足   |
| `71001` | 404  | 物业费账单不存在 |


---



### 4.7 ~~POST `/points/redeem`~~ — 积分兑换【已下线】

> **⚠️ 该接口已于 2026-08-01 下线**：PRD P2-01 已取消「积分不再兑换物业币，两套体系独立运作」，且原实现只扣积分、不发放物业币（空转），属于遗留缺陷。前端请勿再调用 `/points/redeem`，积分仅用于订单积分支付、抵扣物业费、活动/服务订阅等。以下为历史说明，仅供追溯：
>
> **口径说明（2026-08-01）**：积分汇率保留可配置，默认口径为 **100积分=1元**（订单支付按商家挂接配置、物业费抵扣按物业级 `point_to_fee_rate`）。历史文档中「1元=1积分」为营销文案，非实际兑换口径。

**Auth**: Required  
**Roles**: `resident`

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/points/redeem \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "points": 200
  }'
```

**请求体**：


| 字段       | 类型        | 必填  | 校验                   | 说明          |
| -------- | --------- | --- | -------------------- | ----------- |
| `points` | `Integer` | ✅   | `@NotNull @Min(100)` | 兑换积分（最少100） |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "pointsUsed": 200,
    "redeemedAmount": 2.00,
    "balanceAfter": 300,
    "familyBalanceAfter": 1000,
    "redeemedAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**错误响应**：


| 错误码     | HTTP | 说明     |
| ------- | ---- | ------ |
| `40001` | 400  | 积分余额不足 |


---



### 4.8 POST `/points/earn` — 获取积分（管理员）

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/points/earn \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "residentId": "res_b7m9x1",
    "pointAmount": 50,
    "source": "manual",
    "description": "活动奖励"
  }'
```

**请求体**：


| 字段            | 类型        | 必填  | 校验                 | 说明   |
| ------------- | --------- | --- | ------------------ | ---- |
| `residentId`  | `String`  | ✅   | `@NotBlank`        | 用户ID |
| `pointAmount` | `Integer` | ✅   | `@NotNull @Min(1)` | 积分数量 |
| `source`      | `String`  | ✅   | `@NotBlank`        | 来源   |
| `description` | `String`  | ❌   | `@Size(max=200)`   | 描述   |
| `sourceId`    | `String`  | ❌   |                    | 来源ID |
| `familyId`    | `String`  | ❌   |                    | 家庭ID |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "recordId": "pt_demo003",
    "residentId": "res_b7m9x1",
    "pointAmount": 50,
    "balanceAfter": 550,
    "familyBalanceAfter": 1250,
    "source": "manual",
    "operatorId": "res_admin01",
    "createdAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 4.9 POST `/points/spend` — 消费积分（管理员）

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/points/spend \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "residentId": "res_b7m9x1",
    "pointAmount": 30,
    "source": "manual",
    "description": "违规扣除"
  }'
```

**请求体**：


| 字段              | 类型        | 必填  | 校验                 | 说明          |
| --------------- | --------- | --- | ------------------ | ----------- |
| `residentId`    | `String`  | ✅   | `@NotBlank`        | 用户ID        |
| `pointAmount`   | `Integer` | ✅   | `@NotNull @Min(1)` | 积分数量        |
| `source`        | `String`  | ✅   | `@NotBlank`        | 来源          |
| `description`   | `String`  | ❌   | `@Size(max=200)`   | 描述          |
| `sourceId`      | `String`  | ❌   |                    | 来源ID        |
| `familyId`      | `String`  | ❌   |                    | 家庭ID        |
| `useFamilyPool` | `Boolean` | ❌   |                    | 是否优先使用家庭积分池 |


**错误响应**：


| 错误码     | HTTP | 说明     |
| ------- | ---- | ------ |
| `40001` | 400  | 积分余额不足 |


---



## 5. 物业币模块 (Property Coins)



### 5.1 GET `/property-coins/balance` — 获取物业币余额

**Auth**: Required  
**Roles**: `resident`

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "coinBalance": 99.50,
    "familyCoinBalance": 250.00,
    "familyId": "fam_demo001",
    "coinFrozen": false,
    "coinHidden": false,
    "coinDisplayEnabled": true,
    "todayEarned": 5.00,
    "todaySpent": 0,
    "expiringSoon": 20.00,
    "expiryDate": "2026-12-01"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 5.2 GET `/property-coins/family-balance` — 获取家庭物业币余额

**Auth**: Required  
**Roles**: `resident`

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "familyId": "fam_demo001",
    "familyName": "张家",
    "coinBalance": 250.00,
    "memberCount": 3,
    "members": [
      {
        "id": "res_b7m9x1",
        "name": "张先生",
        "coinBalance": 99.50,
        "relation": "owner"
      },
      {
        "id": "res_xxxxxx",
        "name": "李女士",
        "coinBalance": 150.50,
        "relation": "spouse"
      }
    ]
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 5.3 GET `/property-coins/records` — 物业币记录列表

**Auth**: Required  
**Roles**: `resident`

**Query 参数**：


| 参数          | 类型       | 必填  | 默认值          | 说明                                                                |
| ----------- | -------- | --- | ------------ | ----------------------------------------------------------------- |
| `page`      | `int`    | ❌   | `1`          | 页码                                                                |
| `pageSize`  | `int`    | ❌   | `20`         | 每页条数                                                              |
| `coinType`  | `String` | ❌   |              | `earn`/`spend`/`recharge`/`freeze`/`unfreeze`/`expire`/`withdraw` |
| `source`    | `String` | ❌   |              | 来源筛选                                                              |
| `startDate` | `String` | ❌   |              | 开始日期                                                              |
| `endDate`   | `String` | ❌   |              | 结束日期                                                              |
| `familyId`  | `String` | ❌   |              | 家庭ID                                                              |
| `sort`      | `String` | ❌   | `-createdAt` | 排序                                                                |


---



### 5.4 GET `/property-coins/expiring` — 即将过期物业币

**Auth**: Required  
**Roles**: `resident`

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "expiringSoon": 20.00,
    "expiryDate": "2026-12-01"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 5.5 POST `/property-coins/recharge` — 充值物业币

**Auth**: Required  
**Roles**: `resident`

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/property-coins/recharge \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "amount": 50.00
  }'
```

**请求体**：


| 字段       | 类型           | 必填  | 校验                             | 说明   |
| -------- | ------------ | --- | ------------------------------ | ---- |
| `amount` | `BigDecimal` | ✅   | `@NotNull @DecimalMin("0.01")` | 充值金额 |
| `openid` | `String`     | ❌   |                                | 用户openid（`wechat` 支付需要；不传则取用户绑定openid） |


**成功响应**（`wechat` 场景）：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "recordId": null,
    "residentId": "res_b7m9x1",
    "amount": 50.00,
    "balanceAfter": null,
    "familyBalanceAfter": null,
    "rechargedAt": null,
    "paymentRecordId": "pr_xxx",
    "payParams": {
      "appId": "wx1234567890abcdef",
      "timeStamp": "1414561699",
      "nonceStr": "abc123",
      "package": "prepay_id=wx123",
      "signType": "MD5",
      "paySign": "xxxx",
      "outTradeNo": "8..."
    }
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

> **`wechat` 说明**：成功响应返回 `paymentRecordId` + `payParams`（`wx.requestPayment`），此时**未到账**（`recordId`/`balanceAfter` 等为 `null`）；支付成功后由回调/查询兜底确认到账。开发模式（真实支付未启用）直接到账：返回 `recordId`/`balanceAfter`/`familyBalanceAfter`/`rechargedAt`，无 `payParams`。存在待支付充值未完成时再次充值返回 `10002`：「存在待支付的充值，请先完成支付或等待超时后再试」。

---



### 5.6 POST `/property-coins/hide` — 隐藏/显示物业币卡片

**Auth**: Required  
**Roles**: `resident`

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/property-coins/hide \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "hidden": true
  }'
```

**请求体**：


| 字段       | 类型        | 必填  | 校验         | 说明                      |
| -------- | --------- | --- | ---------- | ----------------------- |
| `hidden` | `Boolean` | ✅   | `@NotNull` | 是否隐藏：`true`隐藏，`false`显示 |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "residentId": "res_b7m9x1",
    "coinHidden": true,
    "updatedAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 5.7 POST `/property-coins/earn` — 获取物业币（管理员）

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/property-coins/earn \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "residentId": "res_b7m9x1",
    "coinAmount": 10.00,
    "source": "manual",
    "description": "活动奖励"
  }'
```

**请求体**：


| 字段            | 类型           | 必填  | 校验                             | 说明    |
| ------------- | ------------ | --- | ------------------------------ | ----- |
| `residentId`  | `String`     | ✅   | `@NotBlank`                    | 用户ID  |
| `coinAmount`  | `BigDecimal` | ✅   | `@NotNull @DecimalMin("0.01")` | 物业币数量 |
| `source`      | `String`     | ✅   | `@NotBlank`                    | 来源    |
| `description` | `String`     | ❌   | `@Size(max=200)`               | 描述    |
| `sourceId`    | `String`     | ❌   |                                | 来源ID  |

> **入账口径（2026-10-25 起）**：无论住户是否加入家庭，发放金额统一加到**个人物业币余额** `residents.coin_balance`，不再加到家庭池；响应 `balanceAfter` 为发放后个人余额，`familyBalanceAfter` 仅为家庭池余额快照。

---



### 5.8 POST `/property-coins/spend` — 消费物业币（管理员）

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**请求体**：


| 字段              | 类型           | 必填  | 校验                             | 说明          |
| --------------- | ------------ | --- | ------------------------------ | ----------- |
| `residentId`    | `String`     | ✅   | `@NotBlank`                    | 用户ID        |
| `coinAmount`    | `BigDecimal` | ✅   | `@NotNull @DecimalMin("0.01")` | 物业币数量       |
| `source`        | `String`     | ✅   | `@NotBlank`                    | 来源          |
| `description`   | `String`     | ❌   | `@Size(max=200)`               | 描述          |
| `sourceId`      | `String`     | ❌   |                                | 来源ID        |
| `familyId`      | `String`     | ❌   |                                | 家庭ID        |
| `useFamilyPool` | `Boolean`    | ❌   |                                | 是否优先使用家庭物业币 |


**错误响应**：


| 错误码     | HTTP | 说明      |
| ------- | ---- | ------- |
| `50001` | 400  | 物业币余额不足 |
| `93003` | 403  | 物业币已冻结  |


---



### 5.9 POST `/property-coins/freeze` — 冻结物业币

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/property-coins/freeze \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "residentId": "res_b7m9x1",
    "amount": 50.00,
    "reason": "异常操作"
  }'
```

**请求体**：


| 字段           | 类型           | 必填  | 校验                                | 说明           |
| ------------ | ------------ | --- | --------------------------------- | ------------ |
| `residentId` | `String`     | ✅   | `@NotBlank`                       | 用户ID         |
| `amount`     | `BigDecimal` | ❌   | `@DecimalMin("0")`                | 冻结金额（0=全部冻结） |
| `reason`     | `String`     | ✅   | `@NotBlank @Size(min=1, max=200)` | 冻结原因         |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "cfr_demo001",
    "residentId": "res_b7m9x1",
    "residentName": "张先生",
    "action": "freeze",
    "amount": 50.00,
    "reason": "异常操作",
    "operatorId": "res_admin01",
    "operatorName": "管理员",
    "frozenAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 5.10 POST `/property-coins/unfreeze` — 解冻物业币

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/property-coins/unfreeze \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "residentId": "res_b7m9x1",
    "reason": "已核实，解除冻结"
  }'
```

**请求体**：


| 字段               | 类型       | 必填  | 校验                                | 说明             |
| ---------------- | -------- | --- | --------------------------------- | -------------- |
| `residentId`     | `String` | ✅   | `@NotBlank`                       | 用户ID           |
| `frozenRecordId` | `String` | ❌   |                                   | 冻结记录ID（不传解冻全部） |
| `reason`         | `String` | ✅   | `@NotBlank @Size(min=1, max=200)` | 解冻原因           |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "residentId": "res_b7m9x1",
    "unfrozenAmount": 50.00,
    "newBalance": 149.50,
    "unfrozenAt": "2026-07-01 15:23:33",
    "operatorId": "res_admin01"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



## 6. 商品模块 (Products)



### 6.1 GET `/products` — 商品列表

**Auth**: Optional  
**Roles**: `any`

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/products?page=1&pageSize=10&category=外卖" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：


| 参数                  | 类型       | 必填  | 默认值          | 说明                             |
| ------------------- | -------- | --- | ------------ | ------------------------------ |
| `page`              | `int`    | ❌   | `1`          | 页码                             |
| `pageSize`          | `int`    | ❌   | `20`         | 每页条数                           |
| `keyword`           | `String` | ❌   |              | 搜索关键词。同时匹配**商品名称**与**所属商家名称**（如输入"便民"，既返回名称含"便民"的商品，也返回"便民超市"名下所有商品） |
| `merchantId`        | `String` | ❌   |              | 商家ID                           |
| `category`          | `String` | ❌   |              | 分类                             |
| `status`            | `String` | ❌   |              | `active`/`inactive`/`sold_out` |
| `propertyCompanyId` | `String` | 条件   |              | 物业公司ID。平台管理员跨物业查询时必填；已登录用户不传则自动取当前用户所属物业；**未登录时不传则按 `merchantId` 自动推导（须同时传 `merchantId`）**；两者都缺省时返回 `10001 参数缺失` |
| `sort`              | `String` | ❌   | `-createdAt` | 排序                             |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "prd_demo001",
        "merchantId": "mch_demo001",
        "merchantName": "万达超市",
        "name": "可口可乐330ml",
        "description": "经典碳酸饮料",
        "coverUrl": "https://cdn.example.com/product/prd_demo001.jpg",
        "price": 3.50,
        "memberPrice": 3.00,
        "pointPrice": 350,
        "stock": 100,
        "category": "饮料",
        "status": "active",
        "createdAt": "2026-06-01 10:00:00"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 10,
      "total": 1,
      "totalPages": 1
    }
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

> **字段说明（前端必读）**：
> - `name`（商品名称）、`price`（原价）**始终返回**，不会缺失。
> - `memberPrice`（业主价）**仅在携带登录态 `Authorization` 时返回**；未登录时该字段**整个不出现**（不是 null）——这是全局 Jackson 配置 `default-property-inclusion=non_null` 所致，前端取值请用 `item.memberPrice != null` 判断，不要依赖字段存在。
> - `pointPrice`、`category`、`description`、`coverUrl` 可能为空，同样会不出现字段。
> - `merchantName` 需商家记录存在才返回。
> - 跨物业隔离：已登录用户只能看到**自己所属物业公司**下的商品；商家归属其他物业时结果为空。

---



### 6.2 GET `/products/{id}` — 商品详情

**Auth**: Optional  
**Roles**: `any`

**路径参数**：


| 参数   | 类型       | 必填  | 说明   |
| ---- | -------- | --- | ---- |
| `id` | `String` | ✅   | 商品ID |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "prd_demo001",
    "merchantId": "mch_demo001",
    "merchantName": "万达超市",
    "name": "可口可乐330ml",
    "description": "经典碳酸饮料",
    "coverUrl": "https://cdn.example.com/product/prd_demo001.jpg",
    "price": 3.50,
    "memberPrice": 3.00,
    "pointPrice": 350,
    "distanceKm": 1.2,
    "stock": 100,
    "category": "饮料",
    "status": "active",
    "salesCount": 156,
    "rating": 4.8,
    "createdAt": "2026-06-01 10:00:00",
    "updatedAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

> **`distanceKm`（2026-08 更新）**：商品所属商家与**当前用户小区**的距离（公里），**来源于管理端配置**（`/admin/merchants/{merchantId}/community-distances`，见 §42），**不再使用经纬度 Haversine 自动算距**。
> 用户未登录、用户无小区、或该商家×当前小区未配置距离时为 `null`（前端不展示距离）。



### 6.3 POST `/products` — 创建商品

**Auth**: Required  
**Roles**: `merchant`、`activity_leader`

> **说明**：组长（`activity_leader`）首次调用时自动绑定 `merchant_source=group_leader` 的商家店铺，商品进入 `/products` 主商城，收入走该商家钱包并可提现。

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "name": "可口可乐330ml",
    "description": "经典碳酸饮料",
    "price": 3.50,
    "memberPrice": 3.00,
    "pointPrice": 350,
    "stock": 100,
    "category": "饮料"
  }'
```

**请求体**：


| 字段            | 类型           | 必填  | 校验                             | 说明     |
| ------------- | ------------ | --- | ------------------------------ | ------ |
| `name`        | `String`     | ✅   | `@NotBlank @Size(max=100)`     | 商品名称   |
| `description` | `String`     | ❌   |                                | 描述     |
| `coverUrl`    | `String`     | ❌   | `@Size(max=500)`               | 封面图    |
| `price`       | `BigDecimal` | ✅   | `@NotNull @DecimalMin("0.01")` | 价格     |
| `memberPrice` | `BigDecimal` | ❌   | `@DecimalMin("0.01")`          | 业主折扣价  |
| `pointPrice`  | `Integer`    | ❌   | `@Min(1)`                      | 积分兑换价格 |
| `stock`       | `Integer`    | ❌   | `@Min(0)`                      | 库存     |
| `category`    | `String`     | ❌   | `@Size(max=50)`                | 分类     |
| `status`      | `String`     | ❌   |                                | 状态     |


---



### 6.4 PUT `/products/{id}` — 更新商品

**Auth**: Required  
**Roles**: `merchant`、`activity_leader`（仅自己的店铺）

**请求示例**：

```bash
curl -X PUT https://test.yuanxusoftware.top/api/v1/products/prd_demo001 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "name": "可口可乐330ml",
    "price": 3.00,
    "stock": 200
  }'
```

**路径参数**：


| 参数   | 类型       | 必填  | 说明   |
| ---- | -------- | --- | ---- |
| `id` | `String` | ✅   | 商品ID |


**请求体**：


| 字段            | 类型           | 必填  | 校验                    | 说明     |
| ------------- | ------------ | --- | --------------------- | ------ |
| `name`        | `String`     | ❌   | `@Size(max=100)`      | 商品名称   |
| `description` | `String`     | ❌   |                       | 描述     |
| `coverUrl`    | `String`     | ❌   | `@Size(max=500)`      | 封面图    |
| `price`       | `BigDecimal` | ❌   | `@DecimalMin("0.01")` | 价格     |
| `memberPrice` | `BigDecimal` | ❌   | `@DecimalMin("0.01")` | 业主折扣价  |
| `pointPrice`  | `Integer`    | ❌   | `@Min(1)`             | 积分兑换价格 |
| `stock`       | `Integer`    | ❌   | `@Min(0)`             | 库存     |
| `category`    | `String`     | ❌   | `@Size(max=50)`       | 分类     |
| `status`      | `String`     | ❌   |                       | 状态     |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "prd_demo001",
    "name": "可口可乐330ml",
    "price": 3.00,
    "stock": 200,
    "updatedAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 6.5 DELETE `/products/{id}` — 删除商品（软删除）

**Auth**: Required  
**Roles**: `merchant`、`activity_leader`（仅自己的店铺）

**说明**：执行软删除，设置 `status` 为 `inactive` 并记录 `deletedAt` 时间戳。已删除的商品不会在列表查询中返回。

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "prd_demo001",
    "status": "inactive",
    "deletedAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**错误响应**：


| 错误码     | HTTP | 说明      |
| ------- | ---- | ------- |
| `30001` | 404  | 商品不存在   |
| `40003` | 403  | 无权操作该商品 |


---



## 7. 商家模块 (Merchants)



### 7.1 GET `/merchants` — 商家列表

**Auth**: Optional  
**Roles**: `any`

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/merchants?page=1&pageSize=10"
```

**Query 参数**：


| 参数                  | 类型       | 必填  | 默认值          | 说明                                                    |
| ------------------- | -------- | --- | ------------ | ----------------------------------------------------- |
| `page`              | `int`    | ❌   | `1`          | 页码                                                    |
| `pageSize`          | `int`    | ❌   | `20`         | 每页条数                                                  |
| `keyword`           | `String` | ❌   |              | 搜索关键词：匹配**商家名称**，或该商家有**上架商品**的名称命中（如搜「炸」可搜到卖「炸鸡」的商家） |
| `category`          | `String` | ❌   |              | 分类，可选值：外卖/周围商家/自营/农家/外卖小吃/团购/酒店民宿/洗浴汗蒸/按摩足疗/美食/外卖小区/看病买药/休闲玩乐 |
| `merchantLevel`     | `String` | ❌   |              | `official_certified`/`property_certified`/`normal`    |
| `auditStatus`       | `String` | ❌   |              | `pending_audit`/`approved`/`rejected`（管理端用，筛选审核状态）    |
| `applyRole`         | `String` | ❌   |              | 入驻申请身份筛选：`merchant`/`technician`/`activity_leader`（管理端审核列表按身份筛选，如只看技工入驻申请） |
| `merchantType`      | `String` | ❌   |              | 商家角色类型筛选：`goods` 商品商家 / `technician` 技工商家 / `group_leader` 组长商家 / `canteen` 社区食堂商家。不传返回全部；**前端可按返回的 `merchantType` 分区展示（如食堂专区）** |
| `status`            | `String` | ❌   | 全部          | 商家状态筛选：`active`/`inactive`/`kicked`/`quit`（**仅管理端可用**，传此参数需管理员登录；不传返回全部状态）。用于区分「营业中」与「已被踢出/已退出」记录——同一商家被踢出后重新入驻会同时存在 `kicked` 与 `active` 两条记录，属正常业务轨迹 |
| `propertyCompanyId` | `String` | 条件 |              | 物业公司ID。**未登录浏览时必填**（无 Token 上下文，后端无法判定归属物业，不传返回 `10001`）；已登录用户后端取 Token 中的物业归属，可省略（平台管理员可传指定物业） |
| `recommended`       | `Boolean`| ❌   |              | 是否仅返回**官方/物业推荐**商家：`recommended=true`=物业推荐列表；不传=返回全部审核通过商家（官方全部商家、物业公司模块） |
| `sort`              | `String` | ❌   | `-rankOrder` | 排序                                                    |


> **关键词搜索说明**：`keyword` 会同时匹配「商家名称」与「该商家上架商品的名称」，命中任一即返回该商家。商品匹配仅统计**上架（**`active`**）且未删除**的商品，只按商品名称匹配（不含商品描述）。返回结果仍为**商家列表**，不会单独返回商品。

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "mch_demo001",
        "platformMerchantId": "pm_demo001",
        "propertyCompanyId": "pc_demo001",
        "name": "万达超市",
        "description": "超市提供日常用品配送",
        "coverUrls": ["https://cdn.example.com/merchant/mch_demo001.jpg"],
        "videoUrl": null,
        "qrCodeUrl": "https://cdn.example.com/qr/mch_demo001.png",
        "merchantLevel": "official_certified",
        "levelWeight": 100,
        "category": "外卖",
        "businessHours": "08:00-22:00",
        "contactPhone": "138****5678",
        "residentId": "res_mch02",
        "address": "小区东门",
        "deliveryFee": 0.30,
        "freeDeliveryThreshold": 50.00,
        "deliveryFeeTiers": [
          { "minKm": 0, "maxKm": 1, "fee": 3.00, "enabled": true },
          { "minKm": 1, "maxKm": 3, "fee": 5.00, "enabled": true }
        ],
        "applyRole": "merchant",
        "distanceKm": 1.2,
        "memberDiscountPrice": null,
        "coinRebateRate": 0.05,
        "coinRebateEnabled": true,
        "rankOrder": 1,
        "auditStatus": "approved",
        "status": "active",
        "isRecommended": true,
        "recommendedSort": 100,
        "isDistributor": false,
        "createdAt": "2026-03-15 10:00:00"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 10,
      "total": 1,
      "totalPages": 1
    }
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**字段说明**：


| 字段                             | 类型             | 说明                                                      |
| ------------------------------ | -------------- | ------------------------------------------------------- |
| `list[].id`                    | `String`       | 商家ID                                                    |
| `list[].platformMerchantId`    | `String`       | 平台商家ID                                                  |
| `list[].propertyCompanyId`     | `String`       | 物业公司ID                                                  |
| `list[].name`                  | `String`       | 商家名称                                                    |
| `list[].description`           | `String`       | 商家描述                                                    |
| `list[].coverUrls`             | `List<String>` | 封面图URL列表                                                |
| `list[].videoUrl`              | `String`       | 视频URL                                                   |
| `list[].qrCodeUrl`             | `String`       | 二维码URL                                                  |
| `list[].merchantLevel`         | `String`       | 商家等级：`official_certified`/`property_certified`/`normal` |
| `list[].levelWeight`           | `Integer`      | 等级权重                                                    |
| `list[].category`              | `String`       | 分类                                                      |
| `list[].businessHours`         | `String`       | 营业时间                                                    |
| `list[].contactPhone`          | `String`       | 联系电话                                                    |
| `list[].residentId`            | `String`       | 店主住户ID（res_xxx，商家登录/聊天对端账号）                                    |
| `list[].address`               | `String`       | 地址                                                      |
| `list[].deliveryFee`           | `BigDecimal`   | 配送费                                                     |
| `list[].freeDeliveryThreshold` | `BigDecimal`   | 免配送费门槛                                                  |
| `list[].deliveryFeeTiers`      | `List<DeliveryFeeTier>` | 多档配送费配置：`[{minKm,maxKm,fee,enabled}]`（未配置为 `null`）      |
| `list[].applyRole`             | `String`       | 入驻申请身份：`merchant`/`technician`/`activity_leader`            |
| `list[].distanceKm`            | `BigDecimal`   | 距**当前用户小区**距离（公里）；来源于管理端配置（§42），未登录/无小区/未配置时为 `null`（前端不展示） |
| `list[].memberDiscountPrice`   | `BigDecimal`   | 会员折扣价                                                   |
| `list[].coinRebateRate`        | `BigDecimal`   | 物业币返利率（⚠️ V36 起**仍回显但不再参与任何金额计算**，订单返用户物业币已由物业公司级 `userRewardShareRate` 统一控制，建议界面置灰） |
| `list[].coinRebateEnabled`     | `Boolean`      | 是否启用物业币返利（⚠️ 同上，V36 起不影响金额） |
| `list[].rankOrder`             | `Integer`      | 排序权重                                                    |
| `list[].auditStatus`           | `String`       | 审核状态：`pending_audit`/`approved`/`rejected`              |
| `list[].status`                | `String`       | 状态：`active`/`frozen`/`disabled`                         |
| `list[].isRecommended`        | `Boolean`      | 是否官方/物业推荐（用于区分「物业推荐」列表与「官方全部/物业公司模块」） |
| `list[].recommendedSort`      | `Integer`      | 推荐排序（越大越靠前）                                       |
| `list[].isDistributor`       | `Boolean`      | 🆕 2026-10-16 是否一级代理（`merchants.is_distributor`）：管理端「一级代理商家」开通/取消后回读此字段；未开通为 `false`。`GET /merchants/{id}` 详情同名字段一致 |
| `list[].merchantSource`        | `String`       | 商家来源：`platform`（平台）/ `group_leader`（组长店）/ `technician`（技师店）；前端按此字段排除商家 / 展示活动组（组长店为 `group_leader`） |
| `list[].createdAt`             | `String`       | 创建时间                                                    |


---



### 7.2 GET `/merchants/my` — 我的商家

**Auth**: Required  
**Roles**: `merchant`、`activity_leader`（组长店，`merchantSource=group_leader`）、`technician`（技工店，`merchantSource=technician`）

> **组长/技工说明**：组长（`isActivityLeader=true`，`role` 仍为 `resident`）/技工（`technician`）由系统自动绑定自己的店铺（按 `resident_id + 物业 + merchantSource` 解析，优先 `resident_id`，回退手机号）。**组长小店在组长申请审核通过（§101.2）或管理端创建组长账号（`POST /admin/role-accounts`，`role=activity_leader`）时立即自动创建**（免审核：`auditStatus=approved`、`status=active`、`merchant_source=group_leader`、`merchant_type=group_leader`、店铺名=`{姓名或手机号}的小店`、分类=`活动组`、`deliveryFee=0`、`serveAllCommunities=true`、默认抽佣 0.1）；本接口可直接读取店铺配置，含 `deliveryFee` / `freeDeliveryThreshold` / `deliveryScope`。店铺未绑定/不存在时返回 `60001`（商家不存在）。

**请求示例**：

```bash
curl -X GET https://test.yuanxusoftware.top/api/v1/merchants/my \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "mch_demo001",
    "platformMerchantId": "pm_demo001",
    "name": "万达超市",
    "description": "超市提供日常用品配送",
    "coverUrls": ["https://cdn.example.com/merchant/mch_demo001.jpg"],
    "videoUrl": null,
    "qrCodeUrl": "https://cdn.example.com/qr/mch_demo001.png",
    "merchantLevel": "official_certified",
    "levelWeight": 100,
    "category": "外卖",
    "businessHours": "08:00-22:00",
    "contactPhone": "138****5678",
    "residentId": "res_mch02",
    "address": "小区东门",
    "deliveryFee": 0.30,
    "freeDeliveryThreshold": 50.00,
    "deliveryFeeTiers": [
      { "minKm": 0, "maxKm": 1, "fee": 3.00, "enabled": true },
      { "minKm": 1, "maxKm": 3, "fee": 5.00, "enabled": true }
    ],
    "applyRole": "merchant",
    "distanceKm": 1.2,
    "memberDiscountPrice": null,
    "coinRebateRate": 0.05,
    "coinRebateEnabled": true,
    "rankOrder": 1,
    "auditStatus": "approved",
    "status": "active",
    "totalOrders": 120,
    "totalRevenue": 15000.00,
    "pointExchangeRate": 100.00,
    "products": [
      {
        "id": "prd_demo001",
        "name": "可口可乐330ml",
        "price": 3.50,
        "stock": 100,
        "status": "active"
      }
    ],
    "createdAt": "2026-03-15 10:00:00",
    "updatedAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

> **工作台字段口径（v8.28 补齐）**：
> - `totalRevenue`：累计**已完成订单**金额（`order_status='completed'` 的 `totalAmount` 合计；技师店按已完成服务单金额）。不含待支付/待配送/取消/退款，与平台看板、可提现口径一致。**仅商家本人/管理端查询时返回**，公开 `GET /merchants/{id}` 不返回。
> - `totalOrders`：累计订单总数（不含取消/退款，含未完成），与 `totalRevenue` 口径不完全相同；前端卡片建议标注「订单总额（已完成）」。
> - `pointExchangeRate`：商家挂接配置的积分兑换比（1 元 = X 积分）；配置缺失或非正数时返回 `100.0000`，前端可据此本地试算，避免除零。

> ⚠️ **角色限制**：本接口仅对 `merchant` / `activity_leader` / `technician`（即**已通过审核**）角色开放。`resident`（提交入驻申请但**尚未通过**）调用会返回 `403`。住户查询自己"商家入驻申请"进度：**§7.2.2 `GET /merchants/my-application` 当前未实现**，请勿调用；需由后端补实现后再联调。

**错误响应**：

| 错误码 | HTTP | 说明 |
| --- | --- | --- |
| `60001` | 404 | 商家不存在 / 未绑定店铺 |
| `60002` | 409 | 商家入驻审核中，尚未通过 |
| `60005` | 409 | 🔧 **商家已被管理端踢出（`status=kicked`）**，无法继续经营，请联系物业或重新申请入驻 |
| — | 403 | 角色无权限（仅 `merchant` / `activity_leader` / `technician`） |

> 🔧 **修复说明（2026-08-19）**：此前 `GET /merchants/my` 仅校验角色与账号存在性，未校验店铺 `status`；商家被踢出后 `status=kicked`，本接口仍返回旧店铺信息，导致首页"看起来正常"，但添加商品等写操作提示"商家不存在"。现在被踢出会直接返回 `60005`，前端应据此跳转提示页或引导重新入驻。

---

### 7.2.2 GET `/merchants/my-application` — 我的商家入驻申请状态

> ⚠️ **【当前未实现 · 2026-10-27 核对代码】** 后端不存在 `GET /merchants/my-application` 控制器/服务方法（仅保留 `MyMerchantApplicationResponse` DTO）。前端请勿调用；住户端商家入驻申请进度暂无独立查询接口。以下为原设计文档，保留备查。

**Auth**: Required  
**Roles**: `resident`

> 🔧 **修复说明（2026-08-19）**：此前前端在"我的商家"页面误用 §7.2 `GET /merchants/my`，对 `resident` 返回 `403`，导致住户无法读取自己申请状态；提交时收到 `30004`(409) 后又把缺失状态默认成 `pending_audit`，造成"一直待审核但管理端看不到"的假象。本接口直接返回当前住户在本物业下**最新的商家入驻申请真实状态**。

**响应**：见 `MyMerchantApplicationResponse`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `String` | 申请记录ID（`merchant.id`）；从未申请时为 `null` |
| `name` | `String` | 商家名称 |
| `auditStatus` | `String` | `none`（未申请）/ `pending_audit`（待审核）/ `approved`（已通过）/ `rejected`（已驳回）/ `kicked`（已被踢出）/ `closed`（已退出）/ `stopped`（已停用）。**终态优先**：当 `status` 为终态（`kicked`/`closed`/`stopped`）时，本字段返回终态值而非 `approved`，前端不得再展示"已通过" |
| `applyRole` | `String` | `merchant` / `technician` / `activity_leader` |
| `merchantSource` | `String` | `platform` / `technician` / `group_leader` |
| `status` | `String` | 记录状态（`merchant.status`） |
| `processing` | `Boolean` | `true` 表示仍在审核流程中（`pending_audit`） |

**示例**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "mch_xxx",
    "name": "万达超市",
    "auditStatus": "pending_audit",
    "applyRole": "merchant",
    "merchantSource": "platform",
    "status": "active",
    "processing": true
  }
}
```

> 未申请时：`{ "auditStatus": "none", "processing": false, "id": null }`。前端应**以本接口返回的 `auditStatus` 为准展示**，严禁在缺少状态时默认 `pending_audit`。

---



### 7.2.1 PUT `/merchants/my/delivery-fees` — 更新多档配送费
<a id="721-多档配送费"></a>

**Auth**: Required  
**Roles**: `merchant`、`activity_leader`、`technician`（本人）/ `property_admin`, `platform_admin`

> **（2026-08 新增）**：替代原 `deliveryScope`/`distanceType` 的单档配置。下单计价按「用户小区与商家距离」命中**已启用**档位（`minKm ≤ 距离 < maxKm`，`maxKm` 为空视为不限上限）；未命中或未配置时回退 `deliveryFee` 单档配送费。

**请求示例**：

```bash
curl -X PUT https://test.yuanxusoftware.top/api/v1/merchants/my/delivery-fees \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "deliveryFeeTiers": [
      { "minKm": 0, "maxKm": 1, "fee": 3, "enabled": true },
      { "minKm": 1, "maxKm": 3, "fee": 5, "enabled": true }
    ],
    "freeDeliveryEnabled": true,
    "freeDeliveryThreshold": 30
  }'
```

**请求体**：

| 字段                    | 类型                      | 必填  | 说明                                             |
| --------------------- | ----------------------- | --- | ---------------------------------------------- |
| `deliveryFeeTiers`    | `List<DeliveryFeeTier>` | ❌   | 多档配送费：`[{minKm,maxKm,fee,enabled}]`，`minKm` 默认 0，`maxKm` 空=不限 |
| `freeDeliveryEnabled` | `Boolean`               | ❌   | 是否启用满额免配送费                                    |
| `freeDeliveryThreshold` | `BigDecimal`            | ❌   | 满额免配送费门槛（>0）                                  |

**校验**：`deliveryFeeTiers` 入参为「空数组 `[]`」时表示**清空全部档位**（合法，不再拒绝）；非空时至少需一个 `enabled=true` 档位，`minKm ≥ 0`，`maxKm > minKm`（可为空），区间不得重叠且需按 `minKm` 升序。不满足返回 `10002`。

> **支持删除（2026-08-18 变更）**：多档配送费**支持新增、修改、删除**。提交少于历史档位数量的列表不再报错；提交空数组 `[]` 即清空全部档位并回退为单档 `deliveryFee`。`enabled=false` 仍允许（档位保留，停用展示）。`GET /merchants/my`（§7.2）返回完整 `deliveryFeeTiers`（含停用档）。
> **距离来源**：档位按「用户小区与商家距离」命中，该距离为**管理端配置**（§42 `/admin/merchants/{merchantId}/community-distances`），不再使用经纬度 Haversine；未配置距离时不命中多档、回退 `deliveryFee` 单档配送费。

**成功响应**：更新后的商家详情（同 §7.2 `GET /merchants/my`，含 `deliveryFeeTiers`）。

> **兼容回退**：前端也可在 `PUT /merchants/{id}`（§7.5）携带同一 body 字段 `deliveryFeeTiers`，效果一致。

---

### 7.3 GET `/merchants/{id}` — 商家详情

**Auth**: Optional  
**Roles**: `any`

**路径参数**：


| 参数   | 类型       | 必填  | 说明   |
| ---- | -------- | --- | ---- |
| `id` | `String` | ✅   | 商家ID |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "mch_demo001",
    "platformMerchantId": "pm_demo001",
    "name": "万达超市",
    "description": "超市提供日常用品配送",
    "coverUrls": ["https://cdn.example.com/merchant/mch_demo001.jpg"],
    "videoUrl": null,
    "qrCodeUrl": "https://cdn.example.com/qr/mch_demo001.png",
    "merchantLevel": "official_certified",
    "levelWeight": 100,
    "category": "外卖",
    "businessHours": "08:00-22:00",
    "contactPhone": "138****5678",
    "residentId": "res_mch02",
    "address": "小区东门",
    "deliveryFee": 0.30,
    "freeDeliveryThreshold": 50.00,
    "deliveryFeeTiers": [
      { "minKm": 0, "maxKm": 1, "fee": 3.00, "enabled": true },
      { "minKm": 1, "maxKm": 3, "fee": 5.00, "enabled": true }
    ],
    "applyRole": "merchant",
    "distanceKm": 1.2,
    "freeDeliverySponsor": "merchant",
    "freeDeliveryEnabled": true,
    "deliveryScope": "both",
    "memberDiscountPrice": null,
    "coinRebateRate": 0.05,
    "coinRebateEnabled": true,
    "rankOrder": 1,
    "auditStatus": "approved",
    "status": "active",
    "isDistributor": false,
    "merchantSource": "group_leader",
    "createdAt": "2026-03-15 10:00:00",
    "updatedAt": "2026-03-15 10:05:00",
    "products": [
      {
        "id": "prd_demo001",
        "name": "可口可乐330ml",
        "description": "经典碳酸饮料",
        "coverUrl": "https://cdn.example.com/product/prd_demo001.jpg",
        "price": 3.50,
        "memberPrice": 3.00,
        "pointPrice": 350,
        "stock": 100,
        "category": "饮料",
        "status": "active"
      }
    ]
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

> **商家详情页商品列表 `products`（前端必读）**：
> `GET /merchants/{id}` 会**内联返回该商家的商品列表**，商家详情页可直接用它渲染，**无需再单独请求 `GET /products`**。
> 取数为该商家 `status=active`、未软删除的商品，按 `createdAt` 倒序，**最多 20 条**；无商品时返回空数组 `[]`。
>
> | 字段 | 类型 | 说明 |
> | ---- | ---- | ---- |
> | `id` | `String` | 商品ID（`prd_` 前缀） |
> | `name` | `String` | 商品名称 |
> | `description` | `String` | 商品描述，可为空（字段可能不出现） |
> | `coverUrl` | `String` | 封面图，可为空（字段可能不出现） |
> | `price` | `Number` | 原价（元） |
> | `memberPrice` | `Number` | 业主价，**仅携带登录态时返回**，否则字段不出现 |
> | `pointPrice` | `Integer` | 积分兑换价，可为空 |
> | `stock` | `Integer` | 库存 |
> | `category` | `String` | 商品分类，可为空 |
> | `status` | `String` | `active`/`inactive`/`sold_out` |
>
> 说明：`GET /merchants/{id}` 与 `GET /merchants/my` 共用同一 DTO，但**财务字段不一致**：`totalRevenue`、`withdrawableAmount`、`shopWithdrawableAmount`、`accountWithdrawableAmount`、`profitBalance`、`withdrawalBlocked` 仅在商家本人/管理端查询时返回；公开详情不返回（JSON 中省略），请不要从公开详情读取财务数据。

> **补充（v8.28 修正）**：`pointExchangeRate` 在配置缺失或非正数时返回系统默认 `100.0000`（不是 `0`，避免前端本地试算除零）。`totalRevenue` 仅商家本人/管理端返回；`deliveryScope`、`commissionRate`、`totalOrders`、`withdrawableAmount`、`totalWithdrawn`、`withdrawalBlocked` 等字段按各自接口口径返回。



---



### 7.4 POST `/merchants` — 商家入驻申请

**Auth**: Required  
**Roles**: `resident`

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/merchants \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "name": "我的超市",
    "category": "外卖",
    "contactPhone": "13812345678",
    "address": "小区东门",
    "description": "日用品配送",
    "deliveryFee": 0.30,
    "freeDeliveryThreshold": 50.00
  }'
```

**请求体**：


| 字段                      | 类型             | 必填  | 校验                         | 说明      |
| ----------------------- | -------------- | --- | -------------------------- | ------- |
| `name`                  | `String`       | ✅   | `@NotBlank @Size(max=100)` | 商家名称    |
| `category`              | `String`       | 否（技工/组长可不传） | 仅后台校验（`applyRole=merchant` 必填） | 分类：**仅商品商家（`applyRole=merchant`）必填**；技工（`technician`）/活动组组长（`activity_leader`）**可不传，默认 `技工服务` / `活动组`** |
| `contactPhone`          | `String`       | ✅   | `@NotBlank @Size(max=20)`  | 联系电话    |
| `address`               | `String`       | ❌   | `@Size(max=200)`           | 地址      |
| `coverUrls`             | `List<String>` | ❌   |                            | 封面图片URL |
| `videoUrl`              | `String`       | ❌   | `@Size(max=500)`           | 视频URL   |
| `description`           | `String`       | ❌   |                            | 描述      |
| `businessHours`         | `String`       | ❌   | `@Size(max=50)`            | 营业时间    |
| `deliveryFee`           | `BigDecimal`   | ❌   |                            | 配送费     |
| `freeDeliveryThreshold` | `BigDecimal`   | ❌   |                            | 满额免配送费  |
| `freeDeliverySponsor`   | `String`       | ❌   |                            | 满额减免默认承担方：`merchant`/`property`/`platform`（默认 `merchant`） |
| `freeDeliveryEnabled`   | `Boolean`      | ❌   |                            | 是否启用满额减免 |
| `applyRole`             | `String`       | ❌   | `merchant`/`technician`/`activity_leader`（默认 `merchant`） | 入驻申请身份；`merchant_source` 同步为 `platform`/`technician`/`group_leader`，审核通过后居民 `role` 升级为对应值。技工/组长申请可忽略配送费字段，分类可不传（默认 `技工服务`/`活动组`） |
| `intendedRole`          | `String`       | ❌   | 同 `applyRole`             | 意向身份（与 `applyRole` 等价，兼容前端两种命名） |
| `commissionRate`        | `BigDecimal`   | ❌   |                            | 建议抽佣比例  |
| `pointExchangeRate`     | `BigDecimal`   | ❌   |                            | 建议积分兑换比（1元=X积分），默认 `100.00`（100积分=1元），可留空由系统按默认值处理 |
| `deliveryScope`         | `String`       | ❌   | `in_community`/`out_community`/`both`（默认 `both`） | 配送范围 |
| `distanceType`          | `String`       | ❌   | `nearby`/`citywide`/`any`（默认 `any`） | 配送距离类型——由管理端在 `delivery_price_ranges` 中单独配置，此处仅做记录 |
| `serveAllCommunities`   | `Boolean`      | ❌   | 默认 `true` | 是否服务本公司下全部小区 |


**错误响应**：


| 错误码     | HTTP | 说明           |
| ------- | ---- | ------------ |
| `30004` | 409  | 该商家已在当前物业下挂接（重复申请拦截，不会新增记录） |

**30004 响应体（含真实冲突记录）**：返回 `code=30004`、带 `data`，便于前端直接展示真实状态，而非默认成"待审核"。

```json
{
  "code": 30004,
  "errorCode": "MERCHANT_DUPLICATE_BINDING",
  "message": "该物业下已存在相同申请角色/来源的商家记录（id=mch_xxx，auditStatus=approved），请勿重复提交。",
  "data": {
    "id": "mch_xxx",
    "name": "xxx",
    "auditStatus": "approved",
    "applyRole": "merchant",
    "merchantSource": "platform",
    "propertyCompanyId": "pc_xxx",
    "contactPhone": "13812345678"
  }
}
```

> ⚠️ **前端配合项**：收到 30004 时应以 `data.auditStatus` 为真实状态展示，**严禁**在缺少状态时默认成 `pending_audit`，否则会持续误导（表现为"提交后一直待审核"，但数据库实际并无待审记录）。

> 🔧 **重复校验规则（2026-08-19 修正）**：仅当该物业下已存在 `pending_audit` 或 `approved` **且非终态**的同角色/同来源记录时才拦截 409。**终态记录（`status` ∈ `kicked` 被踢出 / `closed` 已退出 / `stopped` 已停用）以及 `rejected`（已驳回申请）均视为已释放、不占用名额，允许住户重新提交入驻申请**——此前被驳回/被踢出/停用后再次提交会一直 409、且前端只能看到默认待审核的死锁。注意：终态商家（被踢出/退出/停用）的 `auditStatus` 仍为 `approved`，其名额释放由 `status` 终态统一判定。

> ℹ️ **住户查询自己申请进度的正确入口**：`GET /merchants/my`（§7.2，商户端接口，对 `resident` 返回 403）**不能**用于住户查询。注意：§7.2.2 `GET /merchants/my-application` **当前后端未实现**，住户暂无法通过接口读取商家入驻申请状态；需后端补实现后再联调。

> 🔧 **后端修复（2026-08-19）**：`POST /merchants` 现在**显式设置** `merchant_source`、`deliveryScope`、`serveAllCommunities`，不再依赖数据库 DEFAULT。此前商品商家申请时 `merchant_source` 未显式赋值，JPA 实体字段为 null，可能在后续查询（如 `isOwnMerchant`、`getMyMerchant`）中引发异常。同时新增 `deliveryScope`（配送范围）、`distanceType`（配送距离类型，管理端配置，此处仅记录）、`serveAllCommunities`（是否服务全部小区）三个请求字段。

---



### 7.5 PUT `/merchants/{id}` — 更新商家信息

**Auth**: Required  
**Roles**: `merchant`（仅自己的商家）、`activity_leader`（仅自己的组长店）、`technician`（仅自己的技工店）、`property_admin`, `platform_admin`

> **组长/技工说明**：`activity_leader`/`technician` 仅能更新自己的店铺（手机号 + 物业 + `merchantSource` 三重匹配，`merchantSource` 不匹配即 `20004` 权限不足）；`merchantSource` / `propertyCompanyId` / `residentId` / `platformMerchantId` 不可修改。组长店默认 `deliveryFee=0`，可在此设置配送费/满额免邮/配送范围。

**请求示例**：

```bash
curl -X PUT https://test.yuanxusoftware.top/api/v1/merchants/mch_demo001 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "name": "万达超市",
    "description": "超市提供日常用品配送",
    "businessHours": "08:00-23:00"
  }'
```

**路径参数**：


| 参数   | 类型       | 必填  | 说明   |
| ---- | -------- | --- | ---- |
| `id` | `String` | ✅   | 商家ID |


**请求体**：


| 字段                      | 类型             | 必填  | 校验               | 说明      |
| ----------------------- | -------------- | --- | ---------------- | ------- |
| `name`                  | `String`       | ❌   | `@Size(max=100)` | 商家名称    |
| `description`           | `String`       | ❌   |                  | 描述      |
| `coverUrls`             | `List<String>` | ❌   |                  | 封面图片URL |
| `videoUrl`              | `String`       | ❌   | `@Size(max=500)` | 视频URL   |
| `businessHours`         | `String`       | ❌   | `@Size(max=50)`  | 营业时间    |
| `contactPhone`          | `String`       | ❌   | `@Size(max=20)`  | 联系电话    |
| `address`               | `String`       | ❌   | `@Size(max=200)` | 地址      |
| `deliveryFee`           | `BigDecimal`   | ❌   |                  | 配送费     |
| `freeDeliveryThreshold` | `BigDecimal`   | ❌   |                  | 满额免配送费阈值 |
| `freeDeliverySponsor`   | `String`       | ❌   |                  | 满额减免默认承担方：`merchant`/`property`/`platform`（默认 `merchant`） |
| `freeDeliveryEnabled`   | `Boolean`      | ❌   |                  | 是否启用满额减免（不得只通过门槛是否为空推断） |
| `deliveryScope`         | `String`       | ❌   |                  | 配送范围：`in_community`/`out_community`/`both`（默认 `both`） |
| `deliveryFeeTiers`      | `List<DeliveryFeeTier>` | ❌   |                  | 多档配送费（与 §7.2.1 `PUT /merchants/my/delivery-fees` 同一结构，兼容回退） |
| `rankOrder`             | `Integer`      | ❌   | 仅管理员（非管理员返回 `20004`） | 排序权重    |
| `isOfficialRecommended` | `Boolean`      | ❌   | 仅管理员    | 是否官方推荐    |
| `recommendedSort`       | `Integer`      | ❌   | 仅管理员    | 推荐排序      |
| `merchantLevel`         | `String`       | ❌   | 仅平台管理员   | 商家分级：`normal`/`property_certified`/`official_certified` |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "mch_demo001",
    "name": "万达超市",
    "businessHours": "08:00-23:00",
    "updatedAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---

### 7.6 满额配送费减免规则（配送链路，`calculationVersion=distribution_b_v2`）

> 配送侧公式在 V36 未变（见 §71.0）；本次改动只在**商品侧**新增「用户积分/奖励金先扣档」。前端做合计校验时请统一用 §71.0 的守恒式，`distribution_b_v1` 的历史单按旧式校验。

**满额判断口径**（最终由后端判断，移动端不得自行计算）：

```
满额判断金额 = 商品优惠后成交小计（不含配送费，抵扣前判断）
金额 ≥ 门槛 → 满足（含恰好等于）
```

**减免后配送结算**（配送劳动不因用户减免而免费）：

```
减免额 deliveryWaiverAmount = originalDeliveryFee − deliveryFee（最低 0）
配送补贴 deliverySubsidyAmount = 减免额
配送结算基数 deliverySettlementBase = 实付配送费 + 补贴
公司配送收入 = 基数 × platformDeliveryShareRate
配送员收入 courierEarning = 基数 − 公司配送收入
```

**承担方优先级**：

1. 订单级运营活动；
2. 物业级商家/品类活动；
3. 商家 `freeDeliverySponsor`；
4. 系统默认 `merchant`。

**规则要点**：

- 商家承担时，`merchantShare = merchantGoodsShare − 配送补贴`（下单时落快照，结算不重复扣款）；
- 满额减免不得让配送员收入归零（补贴注入配送链路）；
- 满额减免不得计入商品抽佣基数；
- 承担方在**创建订单时保存快照**，结算时不得读取最新配置重算。



### 8.0 POST `/orders/preview` — 支付试算（物业币商城规则）
<a id="80-订单支付试算"></a>

**Auth**: Required  
**Roles**: `resident`

按当前物业公司物业币商城规则（§69）试算可抵扣金额与现金应付，下单/支付前调用。

> 🆕 **v8.11 前端约定**：下单支付页**积分入口已移除**，本接口与 `POST /orders/{id}/pay`（§8.2）调用时 `pointUsed` **一律传 `0`**。后端积分支付逻辑保留不变，仅前端不再提供积分抵扣入口。

**请求体**（`PreviewOrderRequest`）：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `merchantId` | `String` | ✅ | 商家 ID |
| `items` | `Array` | ✅ | 商品列表（同创建订单：`productId`、`quantity`） |
| `coinUsed` | `BigDecimal` | ❌ | 期望抵扣的物业币金额 |
| `pointUsed` | `Integer` | ❌ | 期望使用的积分（**前端固定传 0**） |
| `familyId` | `String` | ❌ | 使用家庭物业币池时传入 |
| `userCouponId` | `String` | ❌ | 期望使用的用户优惠券 ID（`ucp_` 前缀，取自 `GET /coupons/available`，见 §61） |

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/orders/preview \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "merchantId": "mch_demo001",
    "items": [
      { "productId": "prd_demo001", "quantity": 2 },
      { "productId": "prd_demo002", "quantity": 1 }
    ],
    "coinUsed": 20.00,
    "pointUsed": 100
  }'
```

**响应**（`PreviewOrderResponse`）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `totalAmount` | `BigDecimal` | 订单应付总额（= `productAmount` + `deliveryFee`） |
| `subtotal` | `BigDecimal` | 商品小计（= `productAmount`） |
| `productAmount` | `BigDecimal` | 商品金额（不含配送费，满额判断口径） |
| `deliveryFee` | `BigDecimal` | 配送费（用户实际支付） |
| `originalDeliveryFee` | `BigDecimal` | 满额减免前基础配送费 |
| `deliveryWaiverAmount` | `BigDecimal` | 满额减免金额（= `originalDeliveryFee − deliveryFee`，最低 0） |
| `deliverySubsidySponsor` | `String` | 减免承担方 `none`/`merchant`/`property`/`platform` |
| `deliverySubsidyAmount` | `BigDecimal` | 配送补贴金额（= 减免额，注入配送链路） |
| `deliverySettlementBase` | `BigDecimal` | 配送结算基数（= `deliveryFee + deliverySubsidyAmount`） |
| `freeDeliveryEligible` | `Boolean` | 是否命中满额减免 |
| `waiverReason` | `String` | 减免原因 `threshold`/`campaign`/`manual`/`none` |
| `maxCoinAllowed` | `BigDecimal` | 当前规则下最大可抵扣物业币 |
| `coinUsed` | `BigDecimal` | 实际可采用的物业币抵扣金额 |
| `couponId` | `String` | 本次试算采用的用户优惠券 ID（未使用优惠券时为 null） |
| `couponAmount` | `BigDecimal` | 优惠券抵扣金额（作用于商品金额，不抵扣配送费；未使用时为 0.00） |
| `pointUsed` | `Integer` | 使用积分 |
| `cashAmount` | `BigDecimal` | 现金应付金额（= `totalAmount` − `couponAmount` − `coinUsed` − `pointUsed` 折算值，最低 0） |
| `reasonIfCapped` | `String` | 若被封顶/限制时的原因说明（可为 null） |
| `directedBalances` | `Array` | 社区食堂可用定向余额列表（**余额>0才返回**；元素：`mainMerchantId`、`mainMerchantName`、`balance`）。空数组表示本单商家不在任何主商家体系内或用户无可用定向余额 |

> **团购单特殊口径**（v6.5）：当订单**所有商品** `category = 团购` 时，整单视为团购单：
> - `deliveryFee = 0`、`originalDeliveryFee = 0`、`deliveryWaiverAmount = 0`、`deliverySubsidyAmount = 0`、`deliverySettlementBase = 0`、`freeDeliveryEligible = false`、`waiverReason = null`、`deliverySubsidySponsor = null`；
> - `totalAmount = productAmount`（不含配送费）；
> - 判断依据为**商品分类**，不是商家入驻分类（"团购店"卖非团购商品仍按非团购单处理）。

**错误响应**（物业币抵扣相关，与 §69 规则联动）：

| 错误码 | HTTP | 说明 |
| --- | --- | --- |
| `50003` | 409 | 该物业未开启物业币抵扣 |
| `50004` | 409 | 未达物业币最低使用金额 |
| `50005` | 409 | 超过物业币最大抵扣比例 |

> **上架商品不可用本接口**：业主上架商品（`rml_`）/一级货源商品（`dpr_`）不在商城 `products` 表，调用本接口会返回 `10002` 商品ID格式不正确。支付页对上架商品请改用 `POST /orders/from-listing/preview`（§8.0.0）。

---

### 8.0.0 POST `/orders/from-listing/preview` — 业主上架商品支付试算

**Auth**: Required  
**Roles**: `resident`

对业主商户上架商品（`listingId`）试算可抵扣金额与现金应付，口径与下单 `POST /orders/from-listing` 一致（商品金额=零售价×件数，运费按**业主小店店铺主体**配送规则——店铺默认配送费 0、无满额减免，后台可调）。上架商品无商城 `productId`，**不可走 `POST /orders/preview`**。仅试算不落库，支付仍以下单后 `POST /orders/{id}/pay` 后端校验为准。

> 🆕 **v8.11（件数制）**：① `quantity` 支持多件购买（默认 1，须 ≤ listing 可售件数 `quantityRemaining`，否则 `60035`）；② 订单归属**业主小店店铺主体**（`merchant_source=resident_shop`），非一级代理；③ **前端约定：下单/支付页积分入口已移除，`pointUsed` 一律传 `0`**（后端积分支付逻辑保留不变）。

**请求体**（`PreviewListingOrderRequest`）：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `listingId` | `String` | ✅ | 业主上架记录 ID（`rml_` 前缀，见 `GET /resident-merchants/public-listings`/公开橱窗） |
| `quantity` | `Integer` | ❌ | 购买件数，默认 1；须 ≥1 且 ≤ 可售件数 |
| `coinUsed` | `BigDecimal` | ❌ | 期望抵扣的物业币金额 |
| `pointUsed` | `Integer` | ❌ | 期望使用的积分（**前端固定传 0**） |
| `familyId` | `String` | ❌ | 使用家庭物业币池时传入 |
| `userCouponId` | `String` | ❌ | 期望使用的用户优惠券 ID（`ucp_` 前缀，见 §61） |

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/orders/from-listing/preview \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "listingId": "rml_abc123",
    "quantity": 1,
    "coinUsed": 20.00,
    "pointUsed": 100
  }'
```

**响应**：字段与 `POST /orders/preview`（§8.0）完全一致（`PreviewOrderResponse`：`totalAmount`/`subtotal`/`productAmount`/`deliveryFee`/`originalDeliveryFee`/`deliveryWaiverAmount`/`deliverySubsidySponsor`/`deliverySubsidyAmount`/`deliverySettlementBase`/`freeDeliveryEligible`/`waiverReason`/`maxCoinAllowed`/`coinUsed`/`couponId`/`couponAmount`/`pointUsed`/`cashAmount`/`reasonIfCapped`/`directedBalances`）。

**错误响应**：

| 错误码 | HTTP | 说明 |
| --- | --- | --- |
| `10002` | 400 | `listingId` 非 `rml_` 前缀 |
| `60004` | 404 | 上架商品/货源商品不存在 |
| `70002` | 409 | 该商品已下架 / 货源商品已停售 |
| `60001` | 404 | 货源商家不存在 |
| `60035` | 400 | 可售件数不足（件数制，v8.11） |
| `60036` | 404 | 业主小店店铺主体不存在（v8.11） |

---

### 8.0.1 POST `/cart/delivery-fee` — 购物车运费预估（配送费重复计算修复）

**Auth**: Required  
**Roles**: `resident`

> **Bug 修复**：购物车每加一份商品就多算一笔运费（前端按商品行累加倍商家整单配送费）。本接口按**整单（商品小计）只计算一次**运费，与商品份数无关。前端购物车应在商品增删改后调用本接口一次获取整单运费，**禁止按商品行累加** `deliveryFee`。

**请求体**（`CartDeliveryFeeRequest`）：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `merchantId` | `String` | ✅ | 商家 ID（需以 `mer_` 开头） |
| `items` | `Array` | ✅ | 购物车商品列表 |
| `items[].productId` | `String` | ✅ | 商品 ID |
| `items[].quantity` | `Integer` | ✅ | 购买数量（≥1） |

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/cart/delivery-fee \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "merchantId": "mch_demo001",
    "items": [
      { "productId": "prd_demo001", "quantity": 2 },
      { "productId": "prd_demo002", "quantity": 3 }
    ]
  }'
```

**响应**（`CartDeliveryFeeResponse`）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `productAmount` | `BigDecimal` | 商品金额（不含配送费，满额判断口径） |
| `deliveryFee` | `BigDecimal` | 整单运费（全单仅计算一次；达到免邮阈值则为 0） |
| `freeDeliveryThreshold` | `BigDecimal` | 免配送费阈值 |
| `freeDeliveryEligible` | `Boolean` | 当前小计是否已满足免邮 |
| `freeDeliveryShortfall` | `BigDecimal` | 距免邮还差多少金额（已满足则为 0） |
| `originalDeliveryFee` | `BigDecimal` | 满额减免前基础配送费（= 商家配送费） |
| `deliveryWaiverAmount` | `BigDecimal` | 满额减免金额 |
| `deliverySubsidySponsor` | `String` | 减免承担方 `platform`/`merchant` |
| `deliverySubsidyAmount` | `BigDecimal` | 平台/商家补贴金额 |
| `deliverySettlementBase` | `BigDecimal` | 配送结算基数（= 实付 + 补贴） |
| `waiverReason` | `String` | 减免原因 `threshold`/`campaign`/`manual`/`none` |
| `calculationVersion` | `String` | 计算版本（`distribution_b_v1`） |

**说明**：商品数量由 `quantity` 体现，运费计算与份数无关；多份商品仅影响小计、可能影响免邮判定。下单/支付试算仍走 `POST /orders/preview` 与 `POST /orders`，其运费同样为整单一次，与购物车一致。

> **团购商品口径**（v6.5）：当购物车**所有商品** `category = 团购` 时，整单视为团购单：`deliveryFee = 0`、`originalDeliveryFee = 0`、`freeDeliveryEligible = false`、`freeDeliveryThreshold = null`、`deliverySubsidySponsor = null`，所有满额减免相关字段归零。购物车内只要有一件非团购商品，即按非团购整单正常计配送费。

> **运费计算一致性修复（双算 / 对账差异）**：原 `DeliveryService.resolveDeliveryFee` 以 `order.getTotalAmount()`（含运费）判定免邮，与订单侧 `OrderService.calculateDeliveryFee` 以 `subtotal`（不含运费）判定免邮的基准不一致，导致免邮阈值落在 `(subtotal, subtotal+运费)` 区间时"订单收运费、配送记为 0"的对账差异。已修复为：配送侧直接复用订单侧权威运费 `order.getDeliveryFee()`，不再二次判定，两套口径统一。

---

### 8.1 POST `/orders` — 创建订单

**Auth**: Required  
**Roles**: `resident`

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "merchantId": "mch_demo001",
    "items": [
      { "productId": "prd_demo001", "quantity": 2 },
      { "productId": "prd_demo002", "quantity": 1 }
    ],
    "address": "3栋5单元502",
    "contactPhone": "13812345678",
    "remark": "请尽快配送"
  }'
```

**请求体**：


| 字段                  | 类型       | 必填  | 校验                 | 说明   |
| ------------------- | -------- | --- | ------------------ | ---- |
| `merchantId`        | `String` | ✅   | `@NotBlank`        | 商家ID |
| `items`             | `List`   | ✅   | `@NotEmpty @Valid` | 商品列表 |
| `items[].productId` | `String` | ✅   | `@NotBlank`        | 商品ID |
| `items[].quantity`  | `int`    | ✅   | `@Min(1)`          | 数量   |
| `address`           | `String` | ❌   | `@Size(max=200)`   | 配送地址 |
| `contactPhone`      | `String` | ❌   | `@Size(max=20)`    | 联系电话 |
| `remark`            | `String` | ❌   | `@Size(max=500)`   | 备注   |
| `familyId`          | `String` | ❌   |                    | 家庭ID |


**错误响应**：


| 错误码     | HTTP | 说明             |
| ------- | ---- | -------------- |
| `60001` | 404  | 商家不存在          |
| `60006` | 404  | 商家在该物业下未完成挂接配置 |


**成功响应**（`CreateOrderResponse`，金额快照与预览一致，后端权威计算）：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "orderId": "ord_demo001",
    "orderNo": "ORD202607010001",
    "merchantId": "mch_demo001",
    "merchantName": "万达超市",
    "productAmount": 9.70,
    "subtotal": 9.70,
    "totalAmount": 10.00,
    "deliveryFee": 0.30,
    "originalDeliveryFee": 0.30,
    "deliveryWaiverAmount": 0.00,
    "deliverySubsidySponsor": "merchant",
    "deliverySubsidyAmount": 0.00,
    "deliverySettlementBase": 0.30,
    "freeDeliveryEligible": false,
    "waiverReason": "none",
    "paymentMethod": null,
    "pointUsed": 0,
    "coinUsed": 0.00,
    "cashAmount": 0.00,
    "orderStatus": "pending",
    "createdAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**响应字段说明**（`CreateOrderResponse`）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `orderId` / `orderNo` | `String` | 订单ID / 订单编号 |
| `merchantId` / `merchantName` | `String` | 商家ID / 名称 |
| `productAmount` | `BigDecimal` | 商品金额（不含配送费，满额判断口径） |
| `subtotal` | `BigDecimal` | 商品小计（= `productAmount`） |
| `totalAmount` | `BigDecimal` | 订单总金额（= `productAmount + deliveryFee`） |
| `deliveryFee` | `BigDecimal` | 配送费（用户实际支付） |
| `originalDeliveryFee` | `BigDecimal` | 满额减免前基础配送费 |
| `deliveryWaiverAmount` | `BigDecimal` | 满额减免金额 |
| `deliverySubsidySponsor` | `String` | 减免承担方 `none`/`merchant`/`property`/`platform` |
| `deliverySubsidyAmount` | `BigDecimal` | 配送补贴金额 |
| `deliverySettlementBase` | `BigDecimal` | 配送结算基数 |
| `freeDeliveryEligible` | `Boolean` | 是否命中满额减免 |
| `waiverReason` | `String` | 减免原因 `threshold`/`campaign`/`manual`/`none` |
| `paymentMethod` | `String` | 支付方式 |
| `pointUsed` / `coinUsed` / `cashAmount` | - | 积分 / 物业币 / 现金应付 |
| `orderStatus` | `String` | 订单状态（`pending`） |

> **团购单创建口径**（v6.5）：当订单**所有商品** `category = 团购` 时，后端在创建订单时即落库：
> - `requires_delivery = false`、`fulfillment_mode = none`（无需配送、不参与履约方式选择）；
> - `delivery_fee = 0` 及所有配送分账/满额减免字段归零，`total_amount = product_amount`；
> - 房号/地址不强制（可空）。
>
> 团购单支付成功后直接进入 `pending_verification`（见 §8.6.1a），不进 `pending_choice`；非团购单维持原配送履约链路不变。
>
> **社区食堂商家单创建口径**（v6.8）：当 `merchant.merchant_type = canteen` 时，后端创建订单即落库 `requires_delivery=false`、`fulfillment_mode=none`、`delivery_fee=0` 及所有配送分账/满额减免字段归零，`total_amount = product_amount`；房号/地址不强制（可空）。支付成功后直接进入 `completed`（见 §68.1），不进 `pending_verification`、不进 `pending_choice`、不生成配送单。

---



### 8.2 POST `/orders/{id}/pay` — 支付订单

**Auth**: Required  
**Roles**: `resident`

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/orders/ord_demo001/pay \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "paymentMethod": "mixed",
    "pointUsed": 100,
    "coinUsed": 5.00
  }'
```

**请求体**：


| 字段              | 类型           | 必填  | 校验          | 说明                                       |
| --------------- | ------------ | --- | ----------- | ---------------------------------------- |
| `paymentMethod` | `String`     | ✅   | `@NotBlank` | `point`/`property_coin`/`wechat`/`mixed` |
| `pointUsed`     | `Integer`    | ❌   | `@Min(0)`   | 使用积分（**v8.11 前端约定：固定传 0**，下单页积分入口已移除，后端逻辑保留） |
| `coinUsed`      | `BigDecimal` | ❌   | `@Min(0)`   | 使用物业币                                    |
| `mainMerchantId` | `String`    | ❌   |             | 社区食堂定向余额所属**主商家体系ID**（`mixed` 且使用定向余额时必传，须在 `POST /orders/preview` 返回的 `directedBalances` 中） |
| `directedBalanceUsed` | `BigDecimal` | ❌ | `@Min(0)` | 社区食堂**定向余额抵扣金额**（`mixed` 且使用定向余额时必传，须 ≤ 该体系余额；与 `mainMerchantId` 成对传入，否则返回 `10002`） |
| `wechatPayCode` | `String`     | ❌   |             | 微信支付code                                 |
| `familyId`      | `String`     | ❌   |             | 使用家庭余额                                   |
| `userCouponId`  | `String`     | ❌   |             | 使用的用户优惠券 ID（`ucp_` 前缀，须为本人持有且 `unused`、满足商家范围/门槛/有效期，见 §61）。支付成功核销；订单取消/超时未支付自动释放 |
| `openid`        | `String`     | ❌   |             | 用户openid（`wechat`/`mixed` 含现金部分时使用；不传则取用户绑定openid） |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "orderId": "ord_demo001",
    "orderNo": "ORD202607010001",
    "orderStatus": "paid",
    "paymentMethod": "mixed",
    "paidAmount": 4.30,
    "pointUsed": 100,
    "coinUsed": 5.00,
    "directedBalanceUsed": 20.00,
    "couponAmount": 10.00,
    "cashAmount": 0.00,
    "paidAt": "2026-07-01 15:24:00",
    "pointEarned": 10,
    "coinEarned": 0.30
  },
  "timestamp": "2026-07-01 15:24:00"
}
```

> 🆕 **V36 起含现金支付的订单也会返积分/物业币**（此前微信支付回调漏了发放，只有积分/物业币支付的订单返）。`pointEarned` / `coinEarned` 按「抽佣基础额 × 用户两档比例」计算，与分账链的先扣档同源；业主第一次微信支付就会看到积分到账，`GET /points/records` 里会出现 `source=order` 的新记录，属正常，不是重复发放。

> **`wechat`/`mixed` 支付说明**：成功响应返回 `paymentRecordId`（`pr_` 支付流水）与 `payParams`（`wx.requestPayment` 调起参数），此时 `orderStatus` 为 `pending`（待微信支付），**属预期，勿当失败**；支付成功后由微信支付回调确认，`orderStatus` 变更为 `paid`。响应中**无** `payParams`（`point`/`property_coin` 或开发模式）时 `orderStatus` 直接为 `paid`。前端轮询 `GET /orders/{id}` 确认 `paid`，60s 未确认可提供「我已支付」兜底按钮（后端调度 30s 内自动补齐）。

**错误响应**：


| 错误码     | HTTP | 说明             |
| ------- | ---- | -------------- |
| `70001` | 404  | 订单不存在          |
| `70002` | 409  | 订单状态不允许操作      |
| `70003` | 409  | 并发冲突           |
| `40001` | 400  | 积分余额不足         |
| `50001` | 400  | 物业币余额不足        |
| `60006` | 404  | 商家在该物业下未完成挂接配置 |
| `80026` | 400  | 所选主商家体系与订单商家不匹配（定向余额越权抵扣） |
| `80025` | 400  | 定向余额不足          |
| `80027` | 400  | 使用了定向余额但未选主商家体系 |
| `60041` | 404  | 优惠券不存在（`userCouponId` 无效或非本人持有） |
| `60042` | 400  | 优惠券不可用（已使用/已锁定/已过期） |
| `60043` | 400  | 优惠券已过期 |
| `60044` | 400  | 优惠券已使用 |
| `60045` | 400  | 未达优惠券使用门槛 |
| `60046` | 400  | 优惠券适用范围不匹配（商家券与订单商家不一致） |
| `99001` | 502  | 微信支付调用失败/未配置。**2026-10-06 起** message 会附带微信侧错误码与信息，形如 `微信支付失败[CODE]：xxx`（如签名错误、appid 与 mchid 不匹配、api-v3-key 长度错误、私钥缺失等），便于直接定位，不再只有笼统的"内部服务异常" |


> **说明**：`60001` 表示商家 ID 本身不存在；`60006` 表示商家存在，但在当前物业公司下缺少 `merchant_property_configs` 挂接记录（抽佣比例、积分兑换比等）。下单与支付均会校验挂接配置。

---



### 8.3 GET `/orders/my` — 我的订单

**Auth**: Required  
**Roles**: `resident`

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/orders/my?page=1&pageSize=10&orderStatus=paid&keyword=可乐" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：


| 参数            | 类型       | 必填  | 默认值          | 说明                                                                           |
| ------------- | -------- | --- | ------------ | ---------------------------------------------------------------------------- |
| `page`        | `int`    | ❌   | `1`          | 页码                                                                           |
| `pageSize`    | `int`    | ❌   | `20`         | 每页条数                                                                         |
| `orderStatus` | `String` | ❌   |              | `pending`/`paid`/`pending_verification`/`verified`/`delivering`/`completed`/`cancelled`/`refunding`/`refunded` |
| `merchantId`  | `String` | ❌   |              | 商家ID                                                                         |
| `startDate`   | `String` | ❌   |              | 开始日期，格式 `yyyy-MM-dd`（含当天起）；也兼容完整 ISO-8601 时间戳                                |
| `endDate`     | `String` | ❌   |              | 结束日期，格式 `yyyy-MM-dd`（**覆盖当天全天**）；也兼容完整 ISO-8601 时间戳                          |
| `keyword`     | `String` | ❌   |              | 关键词搜索，按**订单号**或**商品名**模糊匹配（命中任一即返回），最长 100 字符                                |
| `sort`        | `String` | ❌   | `-createdAt` | 排序                                                                           |


---



### 8.4 GET `/orders` — 订单列表（商家/管理员）

**Auth**: Required  
**Roles**: `merchant`, `property_admin`, `platform_admin`

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/orders?page=1&pageSize=10&orderStatus=paid&startDate=2026-07-01&keyword=ORD202607" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：


| 参数                  | 类型       | 必填  | 默认值          | 说明                                                                           |
| ------------------- | -------- | --- | ------------ | ---------------------------------------------------------------------------- |
| `page`              | `int`    | ❌   | `1`          | 页码                                                                           |
| `pageSize`          | `int`    | ❌   | `20`         | 每页条数                                                                         |
| `orderStatus`       | `String` | ❌   |              | `pending`/`paid`/`delivering`/`completed`/`cancelled`/`refunding`/`refunded` |
| `merchantId`        | `String` | ❌   |              | 商家ID                                                                         |
| `residentId`        | `String` | ❌   |              | 业主ID                                                                         |
| `propertyCompanyId` | `String` | ❌   |              | 物业公司ID                                                                       |
| `startDate`         | `String` | ❌   |              | 开始日期，格式 `yyyy-MM-dd`（含当天起）；也兼容完整 ISO-8601 时间戳                                |
| `endDate`           | `String` | ❌   |              | 结束日期，格式 `yyyy-MM-dd`（**覆盖当天全天**）；也兼容完整 ISO-8601 时间戳                          |
| `keyword`           | `String` | ❌   |              | 关键词搜索，按**订单号**或**商品名**模糊匹配（命中任一即返回），最长 100 字符                                |
| `sort`              | `String` | ❌   | `-createdAt` | 排序                                                                           |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "ord_demo001",
        "orderNo": "ORD202607010001",
        "residentId": "res_b7m9x1",
        "residentName": "张先生",
        "merchantId": "mch_demo001",
        "merchantName": "万达超市",
        "items": [
          {
            "productId": "prd_demo001",
            "productName": "可口可乐330ml",
            "quantity": 2,
            "price": 3.50,
            "subtotal": 7.00
          }
        ],
        "totalAmount": 10.00,
        "productAmount": 9.70,
        "deliveryFee": 0.30,
        "originalDeliveryFee": 0.30,
        "deliveryWaiverAmount": 0.00,
        "deliverySubsidySponsor": "merchant",
        "deliverySubsidyAmount": 0.00,
        "deliverySettlementBase": 0.30,
        "freeDeliveryEligible": false,
        "waiverReason": "none",
        "merchantGoodsShare": 8.73,
        "merchantDeliverySubsidy": 0.00,
        "merchantShare": 8.73,
        "distributionStatus": "distributed",
        "paymentMethod": "混合支付",
        "paymentMethodCode": "mixed",
        "paymentMethodLabel": "混合支付",
        "deliveryId": "dlv_demo001",
        "deliveryStatusCode": "pending",
        "deliveryStatusLabel": "待抢单",
        "pointUsed": 100,
        "coinUsed": 5.00,
        "cashAmount": 0.00,
        "orderStatus": "paid",
        "deliveryAddress": "3栋5单元502",
        "contactPhone": "13800000000",
        "createdAt": "2026-07-01 15:23:33"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 10,
      "total": 1,
      "totalPages": 1
    }
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**列表项字段（补充）**：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `paymentMethod` | `String` | **展示用中文**（微信支付/混合支付等） |
| `paymentMethodCode` | `String` | 支付方式 code |
| `paymentMethodLabel` | `String` | 与 `paymentMethod` 相同 |
| `orderStatus` | `String` | 订单业务状态 **code**（`pending`/`paid`/`delivering`/`completed`/`cancelled`/`refunding`/`refunded`/`refund_rejected`，逻辑判断用此字段） |
| `deliveryId` | `String` | 配送单 ID（无配送单为 `null`：团购/食堂单、商家尚未选择履约方式） |
| `deliveryStatusCode` | `String` | 🆕 **v8.12** 配送单状态 code：`pending`/`accepted`/`delivering`/`merchant_self`/`completed`/`failed`/`property_fallback`（历史值 `delivered` 归一为 `completed`；无配送单为 `null`）。**物流状态，与订单业务状态相互独立** |
| `deliveryStatusLabel` | `String` | 🆕 **v8.12** 配送单状态中文（后端下发：待抢单/已接单/配送中/已完成/配送失败/物业兜底/商家自配待配送/已退款），前端无需再维护映射 |
| `deliveryAddress` | `String` | 配送地址 |
| `contactPhone` | `String` | 联系电话（商家查看本店订单不脱敏） |
| `residentName` | `String` | 下单用户姓名 |
| `productAmount` | `BigDecimal` | 商品金额（不含配送费，满额判断口径） |
| `deliveryFee` | `BigDecimal` | 配送费（用户实际支付） |
| `originalDeliveryFee` | `BigDecimal` | 满额减免前基础配送费 |
| `deliveryWaiverAmount` | `BigDecimal` | 满额减免金额 |
| `deliverySubsidySponsor` | `String` | 减免承担方 `none`/`merchant`/`property`/`platform` |
| `deliverySubsidyAmount` | `BigDecimal` | 配送补贴金额 |
| `deliverySettlementBase` | `BigDecimal` | 配送结算基数（= 配送费 + 补贴） |
| `freeDeliveryEligible` | `Boolean` | 是否命中满额减免 |
| `waiverReason` | `String` | 减免原因 `threshold`/`campaign`/`manual`/`none` |
| `merchantGoodsShare` | `BigDecimal` | 商家商品实得（商家/管理员视角） |
| `merchantDeliverySubsidy` | `BigDecimal` | 商家承担的配送补贴（商家/管理员视角） |
| `merchantShare` | `BigDecimal` | 商家最终实得（商家/管理员视角，已扣配送补贴） |
| `distributionStatus` | `String` | 分账状态 `pending`/`distributed`/`reversed` |
| `familyId` | `String` | 家庭ID |
| `itemCount` | `Integer` | 商品数量 |
| `merchantCoverUrl` | `String` | 商家封面URL |
| `courierId` | `String` | 快递员ID |
| `paidAt` | `String` | 支付时间 |
| `deliveredAt` | `String` | 送达时间 |
| `completedAt` | `String` | 完成时间 |

> `GET /orders/my`（§8.3）列表项字段与上表相同。

---



### 8.5 GET `/orders/{id}` — 订单详情

**Auth**: Required  
**Roles**: `resident`（本人）, `property_admin`, `merchant`, `platform_admin`

**路径参数**：


| 参数   | 类型       | 必填  | 说明   |
| ---- | -------- | --- | ---- |
| `id` | `String` | ✅   | 订单ID |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "ord_demo001",
    "orderNo": "ORD202607010001",
    "residentId": "res_b7m9x1",
    "residentName": "张先生",
    "merchantId": "mch_demo001",
    "merchantName": "万达超市",
    "items": [
      {
        "productId": "prd_demo001",
        "productName": "可口可乐330ml",
        "coverUrl": "https://cdn.example.com/product/prd_demo001.jpg",
        "quantity": 2,
        "price": 3.50,
        "subtotal": 7.00
      },
      {
        "productId": "prd_demo002",
        "productName": "百事可乐330ml",
        "coverUrl": "https://cdn.example.com/product/prd_demo002.jpg",
        "quantity": 1,
        "price": 3.00,
        "subtotal": 3.00
      }
    ],
    "totalAmount": 10.00,
    "productAmount": 9.70,
    "deliveryFee": 0.30,
    "originalDeliveryFee": 0.30,
    "deliveryWaiverAmount": 0.00,
    "deliverySubsidySponsor": "merchant",
    "deliverySubsidyAmount": 0.00,
    "deliverySettlementBase": 0.30,
    "freeDeliveryEligible": false,
    "waiverReason": "none",
    "merchantGoodsShare": 8.73,
    "merchantDeliverySubsidy": 0.00,
    "merchantShare": 8.73,
    "distributionStatus": "distributed",
    "paymentMethod": "混合支付",
    "paymentMethodCode": "mixed",
    "paymentMethodLabel": "混合支付",
    "pointUsed": 100,
    "coinUsed": 5.00,
    "cashAmount": 0.00,
    "deliveryAddress": "3栋5单元502",
    "contactPhone": "13800000000",
    "remark": "请尽快配送",
    "courierId": null,
    "courierName": null,
    "deliveryId": "dlv_demo001",
    "deliveryDeliveredAt": null,
    "deliveryStatusCode": "pending",
    "deliveryStatusLabel": "待抢单",
    "orderStatus": "paid",
    "paidAt": "2026-07-01 15:24:00",
    "completedAt": null,
    "cancelledAt": null,
    "createdAt": "2026-07-01 15:23:33",
    "updatedAt": "2026-07-01 15:24:00"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**详情字段**：与 §8.4 列表项相同，另含 `remark`、`courierName`、`updatedAt`、`version`、`cancelledAt`、`propertyCompanyId`、`deliveryDeliveredAt`（配送单送达时间）等。

> **🆕 v8.12 三层状态口径（业务状态 / 物流状态 / 资金状态分离）**：
> - **业务状态** `orderStatus`（code：`pending`/`paid`/`delivering`/`completed`/…）回答「订单走到哪一步」——订单列表 pill、订单详情 pill 用它；
> - **物流状态** `deliveryStatusCode` + `deliveryStatusLabel` 回答「货送到哪了」——商家卡片配送提示、订单详情「配送状态」行用它；**与 `orderStatus` 相互独立**：配送员完成配送后配送单为 `completed`，而订单仍为 `delivering`（已送达，待居民确认收货）；
> - **资金状态**（分账腿 / 冻结解冻汇总）**只在**商家「结算」Tab 与配送员「到账结算」界面展示，订单列表/详情不再承担资金态表达。
>
> `deliveredAt`（订单级，配送完成时落库）与 `deliveryDeliveredAt`（配送单级）都表示送达时间；无配送单（团购/食堂单、未选履约方式）时 `deliveryId`/`deliveryStatusCode`/`deliveryStatusLabel`/`deliveryDeliveredAt` 均为 `null`。

---


### 8.6 POST `/orders/{id}/cancel` — 取消订单

**Auth**: Required  
**Roles**: `resident`

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/orders/ord_demo001/cancel \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "reason": "不需要了"
  }'
```

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "orderId": "ord_demo001",
    "orderStatus": "cancelled",
    "cancelledAt": "2026-07-01 15:25:00",
    "refundAmount": 4.30,
    "refundPoint": 100,
    "refundCoin": 5.00
  },
  "timestamp": "2026-07-01 15:25:00"
}
```

**资金处理（已支付订单取消）**：
1. 积分/物业币原路退回；已返积分/物业币扣回；库存恢复；配送单失败；分成账目冲正。
2. **现金部分发起微信原路退款**：先退回该订单已冻结（或已完结）的分账（分账退回，资金回笼），再调微信退款 API，支付流水置 `refunding`，由调度轮询落 `refunded`/`refund_failed`。
3. 退款申请失败（如微信余额不足）时整个取消事务回滚，订单保持原状，可重试；纯积分/物业币支付（现金=0）不调微信。

---



### 8.6.1 POST `/orders/{id}/merchant-cancel` — 商家取消订单

> ⚠️ **【当前未实现 · 2026-10-27 核对代码】** 后端不存在 `POST /orders/{id}/merchant-cancel`。商家侧目前没有取消订单入口；请由管理端介入或后端补实现后再联调。以下为原设计文档，保留备查。

**Auth**: Required  
**Roles**: `merchant`

商家因**缺货、无法配送**等原因取消订单。取消原因必填；退款、退库存、退回积分/物业币、失败配送、回滚分成的逻辑与业主取消完全一致。

**请求体**：


| 字段       | 类型       | 必填  | 校验                       | 说明                |
| -------- | -------- | --- | ------------------------ | ----------------- |
| `reason` | `String` | ✅   | `@NotBlank @Size(1,200)` | 取消原因（如：商品缺货、无法配送） |


**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/orders/ord_demo001/merchant-cancel \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "reason": "商品缺货，无法配送"
  }'
```

**成功响应**：结构同业主取消（`orderId`/`orderStatus`/`cancelledAt`/`refundAmount`/`refundPoint`/`refundCoin`）。

**业务规则**：

- 仅可取消**本商家名下**的订单（按商家联系电话 + 物业公司匹配），否则 `403`。
- 仅可取消处于 `paid`**（已支付）或** `delivering`**（配送中）** 的订单，其它状态返回 `70002`。
- 退款按订单所属业主处理，不受该业主账号状态限制。

**错误响应**：


| 错误码     | HTTP | 说明                                   |
| ------- | ---- | ------------------------------------ |
| `10001` | 400  | 取消原因为空（参数校验失败）                       |
| `20004` | 403  | 非本商家订单，无权取消                          |
| `70001` | 404  | 订单不存在                                |
| `70002` | 409  | 订单状态不允许取消（仅 `paid`/`delivering` 可取消） |
| `70003` | 409  | 并发冲突                                 |---

### 8.6.1a POST `/orders/{id}/verify` — 商家核实订单

> ⚠️ **【路径变更 · 2026-10-27 核对代码】** 实际接口为 `POST /merchants/my/orders/{orderId}/verify`（见 §94.16）。`POST /orders/{id}/verify` 不存在；以下业务规则与字段仍适用，请把路径替换为实际接口。

**Auth**: Required  
**Roles**: `merchant`

仅「团购」商品订单支付成功后进入商家核实阶段。判断依据为**订单所有商品 `category=团购`**（非商家入驻分类），与平台全局核实开关解耦。商家在 24 小时内（可管理端配置）进行核实：
- **通过** → 订单进入 `verified` 状态，开始冻结期（默认 7 天），冻结期满自动执行分账结算
- **拒绝** → 订单取消并退款（退积分/物业币/现金 + 恢复库存）
- **超时未核实** → 系统自动视为通过（进入 verified → 冻结期 → 自动分账结算）

**路径参数**：

| 参数   | 类型       | 必填  | 说明    |
| ---- | -------- | --- | ----- |
| `id` | `String` | ✅   | 订单 ID（`ord_` 前缀） |

**请求体**：

| 字段         | 类型       | 必填  | 说明                          |
| ---------- | -------- | --- | --------------------------- |
| `approved` | `boolean` | ✅   | 是否通过（true=通过，false=拒绝）        |
| `reason`   | `String` | ❌   | 核实备注（拒绝时建议填写原因）               |

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/orders/ord_demo001/verify \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "approved": true,
    "reason": "核实通过"
  }'
```

**成功响应（通过）**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "orderId": "ord_demo001",
    "verificationResult": "approved",
    "orderStatus": "verified",
    "freezeEndDate": "2026-08-28T17:21:02Z",
    "verifiedAt": "2026-08-21T17:21:02Z",
    "message": "核实通过，冻结期7天"
  },
  "timestamp": "2026-08-21 17:21:02"
}
```

**成功响应（拒绝）**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "orderId": "ord_demo001",
    "verificationResult": "rejected",
    "orderStatus": "cancelled",
    "freezeEndDate": null,
    "verifiedAt": "2026-08-21T17:21:02Z",
    "message": "核实拒绝，订单已取消并退款"
  },
  "timestamp": "2026-08-21 17:21:02"
}
```

**业务规则**：

- 仅可操作**本商家名下**的订单，否则 `403`。
- 仅可核实处于 `pending_verification` 状态的订单，其它状态返回 `70002`。
- 进入 `pending_verification` 的判定为订单所有商品 `category=团购`（团购单 `requires_delivery=false`、`fulfillment_mode=none`）；非团购单支付后进入 `pending_choice`（配送履约），不会进入本接口可核实范围。
- 核实时限为下单后 24 小时（可管理端配置）；超时未核实订单由系统自动视为通过，进入冻结期与分账结算。
- 通过后订单进入冻结期（默认 7 天），冻结期内住户可申请退货退款。
- 冻结期满后系统自动执行分账结算：物业/商家/统筹/负责人经微信普通分账直接到账，平台佣金留存。

**错误响应**：

| 错误码      | HTTP | 说明                   |
| --------- | ---- | -------------------- |
| `20004`   | 403  | 非本商家订单              |
| `70001`   | 404  | 订单不存在              |
| `70002`   | 409  | 订单状态不允许核实           |
| `99010`   | 409  | 商家核实时限已过           |
| `70003`   | 409  | 并发冲突               |

---

### 8.6.2 POST `/orders/{id}/send-delivery` — 商家发送配送任务

**Auth**: Required  
**Roles**: `merchant`

商家将**已支付**订单手动发布到配送抢单大厅（创建 `pending` 状态配送单）。

> **说明**：`POST /orders/{id}/pay` 支付成功时后端**也会自动**创建配送单。若配送单已存在，本接口**幂等返回 200**（`alreadyExists=true`），不再报错。前端应依据订单上的 `deliveryId` 控制「发送任务 / 开始配送」按钮。
>
> **2026-09-09 修复（v8.4）**：此前该接口**后端未实现**，前端调用返回 `404`（`No handler`）。
> 现已实现，语义等价于 §68.2 选择 `courier_hall`：
> - 仅本商家名下订单；非本商家 `403 20004`；
> - 团购单 / 社区食堂单（`fulfillment_mode=none`）返回 `400 70022`；
> - 订单状态非 `paid` / `delivering` 且非 `pending_choice` 返回 `409 70002`；
> - 已存在配送单时幂等返回 `alreadyExists=true`，不重复创建；
> - 成功后订单置 `delivering`，`fulfillment_mode=courier_hall`，写入 `fulfillment_choice_logs`。
>
> **订单详情 `deliveryId` 此前恒为 `null`**（`OrderDetailResponse.deliveryId` 从未赋值），
> 前端无法据此控制按钮显隐。现已按 `deliveries.order_id` 回填，并新增 `carrierType` 字段。

**路径参数**：


| 参数   | 类型       | 必填  | 说明    |
| ---- | -------- | --- | ----- |
| `id` | `String` | ✅   | 订单 ID（`ord_` 前缀） |


**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/orders/ord_demo001/send-delivery \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "orderId": "ord_demo001",
    "deliveryId": "dlv_demo001",
    "status": "待抢单",
    "statusCode": "pending",
    "statusLabel": "待抢单",
    "alreadyExists": false,
    "sentAt": "2026-07-11 11:00:00"
  },
  "timestamp": "2026-07-11 11:00:00"
}
```

**幂等响应（已有配送单）**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "orderId": "ord_demo001",
    "deliveryId": "dlv_demo001",
    "status": "待抢单",
    "statusCode": "pending",
    "statusLabel": "待抢单",
    "alreadyExists": true,
    "sentAt": "2026-07-01 15:24:00"
  }
}
```


| 字段           | 类型       | 说明                    |
| ------------ | -------- | --------------------- |
| `orderId`    | `String` | 订单 ID                 |
| `deliveryId` | `String` | 配送单 ID（`dlv_` 前缀） |
| `status`     | `String` | **展示用中文**（如「待抢单」） |
| `statusCode` | `String` | 配送状态 code（`pending`/`completed` 等） |
| `statusLabel`| `String` | 与 `status` 相同 |
| `alreadyExists` | `Boolean` | 是否复用已有配送单 |
| `sentAt`     | `String` | 发送/创建时间                  |


**业务规则**：

- 仅可操作**本商家名下**订单，否则 `20004`。
- 订单状态须为 `paid`，否则 `70002`。
- 若该订单已有配送单，**仍返回 200**，`alreadyExists=true`。

**错误响应**：


| 错误码     | HTTP | 说明              |
| ------- | ---- | --------------- |
| `20004` | 403  | 非本商家订单          |
| `70001` | 404  | 订单不存在           |
| `70002` | 409  | 非已支付状态（**不再**因已有配送单报错）    |


---



### 8.7 POST `/orders/{id}/confirm` — 确认收货

**Auth**: Required  
**Roles**: `resident`

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "orderId": "ord_demo001",
    "orderStatus": "completed",
    "completedAt": "2026-07-01 16:00:00",
    "pointEarned": 10,
    "coinEarned": 0.30
  },
  "timestamp": "2026-07-01 16:00:00"
}
```

> **2026-10-06 修复（v8.10）**：确认收货是分账结算的触发点——订单置 `completed` 时生成
> `distribution_records`（含楼长分成）与业主卖货明细。此前骑手送达后订单停在 `delivering`，
> 若买家不确认收货则分成永不落账（商家货款、楼长分成、四级分账均不生成，而骑手配送费已先行入账）。
> 现新增每小时兜底任务 `OrderAutoConfirmScheduler`：对「已送达且超过售后窗口期（复用
> `order.refundWindowDays`，默认 3 天）」仍为 `delivering` 的订单自动确认收货并结算，保证账目闭环。
> 配送员的配送费与时点无关，骑手点「送达」即计入可提现余额。

---



### 8.8 PATCH `/orders/{id}/status` — 更新订单状态

**Auth**: Required  
**Roles**: `merchant`, `courier`

**订单状态流转**：

```
pending → paid → delivering → completed
pending → cancelled
paid → refunding → refunded
```

> **🆕 v8.12 资金终态规则（重要）**：
>
> `completed` 是**资金终态**——一旦置完成即生成分成记录（含楼长分成）并触发微信分账冻结，
> 且按「确认收货 = 资金终态」规则**不再支持线上退货退款**。因此：
>
> | 角色 | 允许的目标状态 | 说明 |
> | --- | --- | --- |
> | `courier` | 仅 `delivering` | 配送员开始配送时推进订单；**不允许置 `completed`**（返回 `70002`）。配送完成请调用 `POST /deliveries/{id}/complete`，订单停留 `delivering` + `deliveredAt`（已送达待确认） |
> | `merchant` | `delivering` / `completed` | 商家可手动完成订单（管理兜底）；普通配送单建议等待居民确认收货 |
>
> **平台配送单的完成路径**（三选一，均落到 `completed` 并结算）：
> 1. 居民确认收货：`POST /orders/{id}/confirm`（resident，主路径）；
> 2. 超期自动确认：已送达（`deliveredAt` 非空）超过售后窗口期（`order.refund-window-days`，默认 3 天）由定时任务自动完成；
> 3. 商家手动完成：本接口 `merchant` 角色置 `completed`。
>
> **商家自配单**：商家确认送达（`POST /deliveries/{id}/merchant-confirm`）时**后端直接级联**完成订单并结算，无需也不应再调本接口。
>
> **团购/食堂单**：不走配送流程，支付后 `pending_verification → verified → 冻结期 → completed`，本接口不参与。
>
> **错误响应（v8.12 新增场景）**：
>
> | 错误码 | HTTP | 说明 |
> | --- | --- | --- |
> | `70002 ORDER_STATUS_INVALID` | 409 | courier 角色尝试置 `completed`（提示改用 `/deliveries/{id}/complete` + 居民确认收货）；或当前订单状态不满足目标状态流转 |

---



## 9. 配送模块 (Deliveries)



### 9.1 GET `/deliveries/pending` — 待抢单列表

**Auth**: Required  
**Roles**: `courier`

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/deliveries/pending?page=1&pageSize=10" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "dlv_demo001",
        "orderId": "ord_demo001",
        "orderNo": "ORD202607010001",
        "merchantId": "mch_demo001",
        "merchantName": "万达超市",
        "merchantAddress": "小区东门",
        "pickupAddress": "小区东门万达超市",
        "deliveryAddress": "3栋5单元502",
        "contactPhone": "138****5678",
    "residentId": "res_mch02",
        "fee": 0.30,
        "originalDeliveryFee": 0.30,
        "deliverySubsidySponsor": "merchant",
        "deliverySubsidyAmount": 0.00,
        "deliverySettlementBase": 0.30,
        "platformDeliveryShare": 0.03,
        "courierEarning": 0.27,
        "courierShare": 0.27,
        "settlementStatus": "pending",
        "status": "pending",
        "timeoutMinutes": 30,
        "createdAt": "2026-07-01 15:24:00"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 10,
      "total": 1,
      "totalPages": 1
    }
  },
  "timestamp": "2026-07-01 15:24:00"
}
```

> **待抢单/可抢单列表项字段**（`PendingDeliveryItemResponse`）：`id`、`orderId`、`orderNo`、`merchantId`、`merchantName`、`merchantAddress`、`pickupAddress`、`deliveryAddress`、`contactPhone`、`residentId`、`fee`（= 用户实际支付配送费）、`originalDeliveryFee`（满额减免前基础配送费）、`deliverySubsidySponsor`、`deliverySubsidyAmount`、`deliverySettlementBase`（= 配送费 + 补贴）、`platformDeliveryShare`（公司配送收入）、`courierEarning`（配送员最终收入 = 基数 − 公司配送收入）、`courierShare`（兼容旧字段，**恒等于** `courierEarning`）、`settlementStatus`（`pending`/`settled`/`reversed`）、`status`、`carrierType`（`courier`/`merchant`）、`merchantDeliveryFeeShare`（仅商家自配单有值）、`timeoutMinutes`、`createdAt`。
>
> **配送侧取数口径（A1，distribution_b_v1）**：`courierEarning` 不再取 `delivery_rules.courier_per_order` 固定每单金额，改为
> `deliverySettlementBase × (1 − platform_delivery_share_rate)`；恒等式 `deliverySettlementBase = platformDeliveryShare + courierEarning` 始终成立。`courier_per_order` / `delivery_courier_per_order` / `courier_share_rate` 列保留但**不参与分账**（仅历史兼容）。

> **待抢单列表查询口径（2026-10-06 修复）**：仅匹配「商家已选择平台配送」的配送单（`delivery.status = pending`）：
> **`order.orderStatus = delivering` AND `order.fulfillmentMode = courier_hall`**（商家已选择 `courier_hall` 或超时兜底为平台配送，等待骑手抢单）。
>
> **不再**匹配 `order.orderStatus = paid`（订单已支付但商家**尚未**选择履约方式）：该阶段订单**不生成配送单**，
> 因此不会出现在待抢大厅，避免「商家选择自配 / 发布抢单大厅」形同虚设。
> 同时排除 `carrier_type = merchant` 的商家自配单。

---



### 9.2 GET `/deliveries/available` — 可抢单列表

同 `/deliveries/pending`

---



### 9.3 GET `/deliveries/my` — 我的配送任务

**Auth**: Required  
**Roles**: `courier`

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/deliveries/my?page=1&pageSize=10&status=accepted&sort=-createdAt" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：


| 参数         | 类型       | 必填  | 默认值          | 说明                                              |
| ---------- | -------- | --- | ------------ | ----------------------------------------------- |
| `page`     | `int`    | ❌   | `1`          | 页码                                              |
| `pageSize` | `int`    | ❌   | `20`         | 每页条数                                            |
| `status`   | `String` | ❌   |              | `accepted`/`delivering`/`completed`/`cancelled`；🆕 v8.12 传 `delivered`（历史遗留写法）时自动归一为 `completed` 查询 |
| `sort`     | `String` | ❌   | `-createdAt` | 排序                                              |


**列表项字段（补充）**：


| 字段                 | 类型             | 说明                              |
| ------------------ | -------------- | ------------------------------- |
| `courierShare`     | `BigDecimal`   | 兼容旧字段，恒等于 `courierEarning`       |
| `proofImageUrls`   | `List<String>` | 完成配送时上传的凭证 URL（仅 `completed` 有值） |
| `completeRemark`   | `String`       | 完成配送备注                          |
| `statusLabel`      | `String`       | 🆕 v8.12 配送状态中文（后端下发）            |
| `orderStatus`      | `String`       | 🆕 v8.12 订单业务状态 code（配送完成后仍为 `delivering`，待居民确认收货） |
| `orderStatusLabel` | `String`       | 🆕 v8.12 订单业务状态中文                  |

> **🆕 v8.12 展示口径**：配送员端卡片右上角标签用 `statusLabel`（配送单状态）；
> 「配送收入」分账腿仅在 `status=completed` 时展示冻结/解冻态。**配送完成不等于订单完成**——
> 完成配送后订单仍为 `delivering`，收入在居民确认收货（或超期自动确认）后才进入分账冻结。


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "dlv_demo001",
        "orderId": "ord_demo001",
        "orderNo": "ORD202607010001",
        "merchantId": "mch_demo001",
        "merchantName": "万达超市",
        "merchantAddress": "小区东门",
        "pickupAddress": "小区东门万达超市",
        "deliveryAddress": "3栋5单元502",
        "contactPhone": "138****5678",
    "residentId": "res_mch02",
        "fee": 0.30,
        "originalDeliveryFee": 0.30,
        "deliverySubsidySponsor": "merchant",
        "deliverySubsidyAmount": 0.00,
        "deliverySettlementBase": 0.30,
        "platformDeliveryShare": 0.03,
        "courierEarning": 0.27,
        "courierShare": 0.27,
        "settlementStatus": "pending",
        "status": "accepted",
        "statusLabel": "已接单",
        "orderStatus": "delivering",
        "orderStatusLabel": "配送中",
        "acceptedAt": "2026-07-01 15:25:00",
        "pickedAt": null,
        "deliveredAt": null,
        "timeoutAt": "2026-07-01 15:55:00",
        "createdAt": "2026-07-01 15:24:00",
        "proofImageUrls": ["https://test.yuanxusoftware.top/api/v1/static/proof/xxx.jpg"],
        "completeRemark": "已送达"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 10,
      "total": 1,
      "totalPages": 1
    }
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 9.4 POST `/deliveries/{id}/grab` — 快递员抢单

**Auth**: Required  
**Roles**: `courier`

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "dlv_demo001",
    "orderId": "ord_demo001",
    "courierId": "res_courier01",
    "status": "accepted",
    "fee": 0.30,
    "originalDeliveryFee": 0.30,
    "deliverySubsidySponsor": "merchant",
    "deliverySubsidyAmount": 0.00,
    "deliverySettlementBase": 0.30,
    "platformDeliveryShare": 0.03,
    "courierEarning": 0.27,
    "courierShare": 0.27,
    "settlementStatus": "pending",
    "acceptedAt": "2026-07-01 15:25:00",
    "timeoutAt": "2026-07-01 15:55:00"
  },
  "timestamp": "2026-07-01 15:25:00"
}
```

**错误响应**：


| 错误码     | HTTP | 说明            |
| ------- | ---- | ------------- |
| `80001` | 409  | 快递员抢单失败（已被抢走） |


---



### 9.5 POST `/deliveries/{id}/complete` — 完成配送

**Auth**: Required  
**Roles**: `courier`（仅接单人）

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/deliveries/dlv_demo001/complete \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "requireProof": true,
    "proofImageUrls": ["https://test.yuanxusoftware.top/api/v1/static/proof/xxx.jpg"],
    "remark": "已送达"
  }'
```

**请求体**：


| 字段               | 类型             | 必填  | 校验               | 说明         |
| ---------------- | -------------- | --- | ---------------- | ---------- |
| `requireProof`   | `Boolean`      | ❌   |                  | 是否凭证模式；`true` 时 `proofImageUrls` **不能为空**（对应「上传凭证后完成」） |
| `proofImageUrls` | `List<String>` | ❌   | `@Size(max=5)`   | 配送凭证图片 URL（最多 5 张）；须先通过 `POST /files/upload` 上传 |
| `remark`         | `String`       | ❌   | `@Size(max=200)` | 完成备注，持久化至 `deliveries.complete_remark` |


**业务规则**：

- **直接完成**：不传 `requireProof` 或传 `false`，可不传 `proofImageUrls`。
- **上传凭证后完成**：须传 `requireProof: true` 且 `proofImageUrls` 至少 1 张，否则 `10002`「凭证模式下必须上传配送凭证」。
- 凭证 URL 以 JSON 数组存入 `deliveries.proof_image_urls`。
- **🆕 v8.12 配送完成 ≠ 订单完成**：本接口只把**配送单**置 `completed` 并落 `deliveredAt`；
  **订单保持 `delivering`**（已送达，待居民确认收货），不触发分成/分账。响应的 `orderStatus` 即为当前订单业务状态（`delivering`）。
- **资金时点**：分成记录生成与微信分账冻结发生在**订单完成**时点（居民确认收货 / 超期自动确认 / 商家手动完成），
  售后窗口（`wechat-split.after-sale-days`）到期后才完结划账（解冻）。配送完成**不解冻、不分账**——
  窗口内用户申请退货时配送费尚未划走，避免平台垫付。
- **幂等保护**：配送单已完成（`completed`）后重复调用返回 `70002`。
- 商家自配单（`carrierType=merchant`）不可调用本接口（`80020`），商家请用 `POST /deliveries/{id}/merchant-confirm`。


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "dlv_demo001",
    "orderId": "ord_demo001",
    "status": "completed",
    "orderStatus": "delivering",
    "orderStatusLabel": "配送中",
    "fee": 0.30,
    "originalDeliveryFee": 0.30,
    "deliverySubsidySponsor": "merchant",
    "deliverySubsidyAmount": 0.00,
    "deliverySettlementBase": 0.30,
    "platformDeliveryShare": 0.03,
    "courierEarning": 0.27,
    "courierShare": 0.27,
    "settlementStatus": "pending",
    "carrierType": "courier",
    "merchantDeliveryFeeShare": null,
    "deliveredAt": "2026-07-01 15:40:00",
    "proofImageUrls": ["https://test.yuanxusoftware.top/api/v1/static/proof/xxx.jpg"],
    "remark": "已送达"
  },
  "timestamp": "2026-07-01 15:40:00"
}
```

> `orderStatus` / `orderStatusLabel` 为 🆕 v8.12 新增：配送员端据此向用户解释「配送已完成，等待居民确认收货后本单收入计入冻结」。

**错误响应**：


| 错误码     | HTTP | 说明                    |
| ------- | ---- | --------------------- |
| `10002` | 400  | `requireProof=true` 但未传凭证 |
| `70002` | 409  | 配送状态不允许完成             |


---



### 9.6 PATCH `/deliveries/{id}/status` — 更新配送状态

**Auth**: Required  
**Roles**: `courier`, `property_admin`

**请求示例**：

```bash
curl -X PATCH https://test.yuanxusoftware.top/api/v1/deliveries/dlv_demo001/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "status": "delivering"
  }'
```

**路径参数**：


| 参数   | 类型       | 必填  | 说明   |
| ---- | -------- | --- | ---- |
| `id` | `String` | ✅   | 配送ID |


**请求体**：


| 字段       | 类型       | 必填  | 校验                              | 说明        |
| -------- | -------- | --- | ------------------------------- | --------- |
| `status` | `String` | ✅   | `@NotBlank @Pattern("delivering | failed")` |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "dlv_demo001",
    "orderId": "ord_demo001",
    "status": "delivering",
    "updatedAt": "2026-07-01 15:30:00"
  },
  "timestamp": "2026-07-01 15:30:00"
}
```

---



### 9.7 POST `/deliveries/{id}/assign` — 物业指派配送

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/deliveries/dlv_demo001/assign \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "courierId": "res_courier01"
  }'
```

---

### 9.8 GET `/deliveries/couriers` — 可用配送员分页（指派用）
<a id="98-可用配送员"></a>

**Auth**: Required  
**Roles**: `merchant`, `property_admin`, `platform_admin`, `coordinator`, `sector_leader`, `individual_leader`

指派配送（§9.7）前查询可用配送员；与管理端 `GET /admin/couriers`（§10.5.3）数据同源。

> **（2026-10-06）** 该接口此前**后端未实现**（调用返回 404），现已补齐：分页查询本物业 `role=courier` 的住户；`status` 按该配送员是否存在进行中配送单（`accepted`/`picked`）判定为 `online` / `offline`；`id` 与 `courierId` 均为其住户 ID（`res_` 前缀）。

**Query 参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `propertyCompanyId` | `String` | ❌ | — | 物业公司 ID（超管筛选） |

**响应** `data.list[]`（`CourierOptionResponse`）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `String` | 记录 ID |
| `courierId` | `String` | 配送员住户 ID（`res_`） |
| `courierName` | `String` | 姓名 |
| `status` | `String` | 状态 |
| `createdAt` | `String` | 创建时间 |

---



## 10. 配送规则模块 (Delivery Rules)

> **路径别名**：`/admin/delivery-rules` 与 `/delivery-rules` **等价**（同一套 CRUD，管理端前缀别名）。下文以 `/delivery-rules*` 书写；调用 `/admin/delivery-rules*` 行为相同。



### 10.1 GET `/delivery-rules` — 配送规则列表

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**Query 参数**：`page`, `pageSize`, `propertyCompanyId`

---



### 10.2 GET `/delivery-rules/{id}` — 配送规则详情

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**路径参数**：


| 参数   | 类型       | 必填  | 说明     |
| ---- | -------- | --- | ------ |
| `id` | `String` | ✅   | 配送规则ID |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "dr_demo001",
    "name": "默认配送规则",
    "baseFee": 0.00,
    "perKgFee": null,
    "freeThreshold": 50.00,
    "maxWeight": null,
    "maxDistance": null,
    "courierShareRate": 1.00,
    "timeoutMinutes": 30,
    "propertyCompanyId": "pc_demo001",
    "createdAt": "2026-06-01 10:00:00",
    "updatedAt": "2026-06-01 10:00:00"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 10.3 POST `/delivery-rules` — 创建配送规则

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/delivery-rules \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "name": "默认配送规则",
    "baseFee": 0.00,
    "timeoutMinutes": 30
  }'
```

**请求体**：


| 字段                  | 类型           | 必填  | 校验                             | 说明      |
| ------------------- | ------------ | --- | ------------------------------ | ------- |
| `name`              | `String`     | ✅   | `@NotBlank @Size(max=100)`     | 规则名称    |
| `baseFee`           | `BigDecimal` | ✅   | `@NotNull @DecimalMin("0.00")` | 起步价     |
| `perKgFee`          | `BigDecimal` | ❌   |                                | 每公斤费用   |
| `freeThreshold`     | `BigDecimal` | ❌   |                                | 免费门槛    |
| `maxWeight`         | `BigDecimal` | ❌   |                                | 最大重量    |
| `maxDistance`       | `BigDecimal` | ❌   |                                | 最大距离    |
| `courierShareRate`  | `BigDecimal` | ❌   |                                | 快递员分成比例（**仅历史兼容，B 方案下配送员收入改由配送费核算，见 §7.6/§21**） |
| `timeoutMinutes`    | `Integer`    | ❌   | `@Min(1)`                      | 超时时间    |
| `propertyCompanyId` | `String`     | ❌   |                                | 物业公司ID  |


**错误响应**：


| 错误码     | HTTP | 说明         |
| ------- | ---- | ---------- |
| `80003` | 409  | 该物业已存在配送规则 |


---



### 10.4 PUT `/delivery-rules/{id}` — 更新配送规则

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**路径参数**：


| 参数   | 类型       | 必填  | 说明     |
| ---- | -------- | --- | ------ |
| `id` | `String` | ✅   | 配送规则ID |


**请求示例**：

```bash
curl -X PUT https://test.yuanxusoftware.top/api/v1/delivery-rules/dr_demo001 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "name": "默认配送规则（更新）",
    "baseFee": 0.50,
    "timeoutMinutes": 25
  }'
```

**请求体**：


| 字段                 | 类型           | 必填  | 校验                    | 说明      |
| ------------------ | ------------ | --- | --------------------- | ------- |
| `name`             | `String`     | ❌   | `@Size(max=100)`      | 规则名称    |
| `baseFee`          | `BigDecimal` | ❌   | `@DecimalMin("0.00")` | 起步价     |
| `perKgFee`         | `BigDecimal` | ❌   |                       | 每公斤费用   |
| `freeThreshold`    | `BigDecimal` | ❌   |                       | 免费门槛    |
| `maxWeight`        | `BigDecimal` | ❌   |                       | 最大重量    |
| `maxDistance`      | `BigDecimal` | ❌   |                       | 最大距离    |
| `courierShareRate` | `BigDecimal` | ❌   |                       | 快递员分成比例（**仅历史兼容，B 方案下配送员收入改由配送费核算**） |
| `timeoutMinutes`   | `Integer`    | ❌   | `@Min(1)`             | 超时时间    |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "dr_demo001",
    "name": "默认配送规则（更新）",
    "baseFee": 0.50,
    "perKgFee": null,
    "freeThreshold": 50.00,
    "maxWeight": null,
    "maxDistance": null,
    "courierShareRate": 1.00,
    "timeoutMinutes": 25,
    "propertyCompanyId": "pc_demo001",
    "createdAt": "2026-06-01 10:00:00",
    "updatedAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



## 10.5 管理端 — 送货管理



### 10.5.1 GET `/admin/deliveries/overview` — 送货管理概览

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/admin/deliveries/overview?propertyCompanyId=pc_demo001" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：


| 参数                  | 类型       | 必填   | 默认值      | 说明     |
| ------------------- | -------- | ---- | -------- | ------ |
| `propertyCompanyId` | `String` | 超管必填 | 从token获取 | 物业公司ID |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "todayStats": {
      "todayOrders": 12,
      "orderGrowth": 3,
      "onlineCouriers": 3,
      "totalCouriers": 5,
      "todayDeliveryFee": 156.50,
      "capacityLoad": 60.0
    },
    "couriers": [
      {
        "id": "res_courier01",
        "name": "吴十",
        "todayCompleted": 5,
        "monthIncome": 1250.00,
        "status": "在线",
        "statusCode": "online",
        "statusLabel": "在线"
      },
      {
        "id": "res_courier02",
        "name": "李四",
        "todayCompleted": 3,
        "monthIncome": 890.00,
        "status": "离线",
        "statusCode": "offline",
        "statusLabel": "离线"
      }
    ],
    "recentDeliveries": [
      {
        "id": "dlv_demo001",
        "time": "2026-07-05 14:30:00",
        "residentName": "吴十",
        "productDesc": "订单#ORD20260705001",
        "fee": 1.00,
        "status": "已完成",
        "statusCode": "completed",
        "statusLabel": "已完成"
      }
    ]
  },
  "timestamp": "2026-07-05 15:00:00"
}
```

**响应字段说明**：


| 字段                                | 类型           | 说明                 |
| --------------------------------- | ------------ | ------------------ |
| `todayStats.todayOrders`          | `long`       | 今日订单数              |
| `todayStats.orderGrowth`          | `long`       | 订单增长率（与昨日对比）       |
| `todayStats.onlineCouriers`       | `long`       | 在线快递员数             |
| `todayStats.totalCouriers`        | `long`       | 总快递员数              |
| `todayStats.todayDeliveryFee`     | `BigDecimal` | 今日配送费总额            |
| `todayStats.capacityLoad`         | `BigDecimal` | 运力负荷百分比            |
| `couriers[].id`                   | `String`     | 快递员ID              |
| `couriers[].name`                 | `String`     | 快递员姓名              |
| `couriers[].todayCompleted`       | `long`       | 今日完成单数             |
| `couriers[].monthIncome`          | `BigDecimal` | 本月收入               |
| `couriers[].status`               | `String`     | **展示用中文**：在线/离线 |
| `couriers[].statusCode`           | `String`     | `online`/`offline` |
| `couriers[].statusLabel`          | `String`     | 与 `status` 相同 |
| `recentDeliveries[].id`           | `String`     | 配送单ID              |
| `recentDeliveries[].time`         | `String`     | 配送时间               |
| `recentDeliveries[].residentName` | `String`     | 快递员姓名              |
| `recentDeliveries[].productDesc`  | `String`     | 商品描述               |
| `recentDeliveries[].fee`          | `BigDecimal` | 配送费                |
| `recentDeliveries[].status`       | `String`     | **展示用中文**（待抢单/已完成等） |
| `recentDeliveries[].statusCode`   | `String`     | 配送状态 code |
| `recentDeliveries[].statusLabel`  | `String`     | 与 `status` 相同 |


---



### 10.5.2 GET `/admin/deliveries/capacity` — 运力效能分析

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/admin/deliveries/capacity?propertyCompanyId=pc_demo001&dimension=24h" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：


| 参数                  | 类型       | 必填   | 默认值      | 说明                   |
| ------------------- | -------- | ---- | -------- | -------------------- |
| `propertyCompanyId` | `String` | 超管必填 | 从token获取 | 物业公司ID               |
| `dimension`         | `String` | ❌    | `24h`    | `24h`（今日）或 `7d`（近7天） |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "dimension": "24h",
    "peakHour": "14:00",
    "hourlyData": [
      { "hour": 8, "label": "08:00", "deliveryCount": 2, "avgResponseMinutes": 12.5 },
      { "hour": 9, "label": "09:00", "deliveryCount": 5, "avgResponseMinutes": 10.0 },
      { "hour": 10, "label": "10:00", "deliveryCount": 3, "avgResponseMinutes": 11.2 },
      { "hour": 11, "label": "11:00", "deliveryCount": 6, "avgResponseMinutes": 9.8 },
      { "hour": 12, "label": "12:00", "deliveryCount": 4, "avgResponseMinutes": 10.5 },
      { "hour": 13, "label": "13:00", "deliveryCount": 7, "avgResponseMinutes": 8.9 },
      { "hour": 14, "label": "14:00", "deliveryCount": 8, "avgResponseMinutes": 8.5 },
      { "hour": 15, "label": "15:00", "deliveryCount": 5, "avgResponseMinutes": 9.2 }
    ]
  },
  "timestamp": "2026-07-05 15:30:00"
}
```

**响应字段说明**：


| 字段                                | 类型           | 说明               |
| --------------------------------- | ------------ | ---------------- |
| `dimension`                       | `String`     | 时间维度（`24h`/`7d`） |
| `peakHour`                        | `String`     | 配送高峰时段           |
| `hourlyData[].hour`               | `int`        | 小时（0-23）         |
| `hourlyData[].label`              | `String`     | 时段标签（如 `14:00`）  |
| `hourlyData[].deliveryCount`      | `long`       | 该时段配送单数          |
| `hourlyData[].avgResponseMinutes` | `BigDecimal` | 平均响应时间（分钟）       |


---

### 10.5.3 GET `/admin/couriers` — 配送员列表（管理端）
<a id="1053-管理端配送员与配送订单"></a>

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

与 §9.8 `GET /deliveries/couriers` 同源；管理端送货页用此路径。

> **（2026-10-06）** 该接口此前**后端未实现**（调用返回 404），现已补齐，实现与 §9.8 同源（`property_admin`/`platform_admin` 可直接查本物业或指定物业）。

**Query 参数**：`page`、`pageSize`、`propertyCompanyId`（同 §9.8）

**响应**：同 §9.8 `CourierOptionResponse` 分页。

---

### 10.5.4 GET `/admin/delivery-orders` — 配送相关订单列表（管理端）

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

分页查询配送相关订单（底层与订单列表同源筛选）。

> **（2026-10-06）** 该接口此前**后端未实现**（调用返回 404），现已补齐：复用订单列表筛选（`OrderService.listOrders`），按 `propertyCompanyId` 做物业隔离。

**Query 参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `orderStatus` | `String` | ❌ | — | 订单状态筛选 |
| `propertyCompanyId` | `String` | ❌ | — | 物业公司 ID |
| `startDate` | `String` | ❌ | — | 开始日期 |
| `endDate` | `String` | ❌ | — | 结束日期 |
| `keyword` | `String` | ❌ | — | 关键词 |
| `sort` | `String` | ❌ | `-createdAt` | 排序 |

**响应**：订单列表项分页（同 §8 订单列表字段）。

---



## 11. 公告模块 (Announcements)



### 11.1 POST `/announcements` — 创建公告

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/announcements \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "title": "停水通知",
    "content": "因水管维修，明日8:00-18:00停水",
    "announcementType": "property_announcement",
    "targetBuildings": ["1栋", "2栋", "3栋"]
  }'
```

**请求体**：


| 字段                  | 类型             | 必填  | 校验                         | 说明                             |
| ------------------- | -------------- | --- | -------------------------- | ------------------------------ |
| `title`             | `String`       | ✅   | `@NotBlank @Size(max=200)` | 标题                             |
| `content`           | `String`       | ✅   | `@NotBlank`                | 内容                             |
| `announcementType`  | `String`       | ✅   | `@NotBlank`                | 公告类型（可传中文，后端自动映射）：`物业公告`→`property`、`社区公告`→`community`、`平台公告/统筹`→`platform`、`系统公告`→`system`、`商家公告`→`merchant`、`活动组公告`→`activity`；亦可直接传英文枚举 |
| `propertyCompanyId` | `String`       | ❌   |                            | 物业公司ID                         |
| `communityId`       | `String`       | ❌   |                            | 小区ID                           |
| `merchantId`        | `String`       | ❌   |                            | 商家ID                           |
| `coverUrls`         | `List<String>` | ❌   |                            | 封面图片                           |
| `targetRoles`       | `List<String>` | ❌   |                            | 目标角色                           |
| `targetBuildings`   | `List<String>` | ❌   |                            | 目标楼栋                           |
| `collectEnabled`    | `Boolean`      | ❌   |                            | 是否启用信息收集                       |
| `collectFields`     | `List<Object>` | ❌   |                            | 收集字段定义                         |
| `status`            | `String`       | ❌   |                            | `draft`/`published`/`archived` |


---



### 11.2 GET `/announcements` — 公告列表

**Auth**: Optional  
**Roles**: `any`

**Query 参数**：


| 参数                 | 类型       | 必填  | 默认值            | 说明   |
| ------------------ | -------- | --- | -------------- | ---- |
| `page`             | `int`    | ❌   | `1`            | 页码   |
| `pageSize`         | `int`    | ❌   | `20`           | 每页条数 |
| `announcementType` | `String` | ❌   |                | 公告类型 |
| `communityId`      | `String` | ❌   |                | 小区ID |
| `merchantId`       | `String` | ❌   |                | 商家ID |
| `readStatus`       | `String` | ❌   |                | 阅读状态 |
| `sort`             | `String` | ❌   | `-publishedAt` | 排序   |


---



### 11.3 GET `/announcements/{id}` — 公告详情

**Auth**: Optional  
**Roles**: `any`

**路径参数**：


| 参数   | 类型       | 必填  | 说明   |
| ---- | -------- | --- | ---- |
| `id` | `String` | ✅   | 公告ID |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "ann_demo001",
    "title": "停水通知",
    "content": "因水管维修，明日8:00-18:00停水",
    "announcementType": "property_announcement",
    "propertyCompanyId": "pc_demo001",
    "communityId": "com_demo001",
    "merchantId": null,
    "coverUrls": [],
    "targetRoles": ["resident"],
    "targetBuildings": ["1栋", "2栋", "3栋"],
    "collectEnabled": true,
    "collectFields": [
      { "name": "needWater", "label": "是否需要储水", "type": "boolean" },
      { "name": "contactPhone", "label": "联系电话", "type": "text" }
    ],
    "status": "published",
    "publishedAt": "2026-07-01 10:00:00",
    "createdAt": "2026-07-01 09:00:00",
    "updatedAt": "2026-07-01 10:00:00"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 11.4 PUT `/announcements/{id}` — 更新公告

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**路径参数**：


| 参数   | 类型       | 必填  | 说明   |
| ---- | -------- | --- | ---- |
| `id` | `String` | ✅   | 公告ID |


**请求示例**：

```bash
curl -X PUT https://test.yuanxusoftware.top/api/v1/announcements/ann_demo001 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "title": "停水通知（更新）",
    "content": "因水管维修，明日8:00-18:00停水，已延长至后天",
    "targetBuildings": ["1栋", "2栋", "3栋", "4栋"]
  }'
```

**请求体**：


| 字段                 | 类型             | 必填  | 校验               | 说明                             |
| ------------------ | -------------- | --- | ---------------- | ------------------------------ |
| `title`            | `String`       | ❌   | `@Size(max=200)` | 标题                             |
| `content`          | `String`       | ❌   |                  | 内容                             |
| `announcementType` | `String`       | ❌   |                  | 公告类型                           |
| `coverUrls`        | `List<String>` | ❌   |                  | 封面图片                           |
| `targetRoles`      | `List<String>` | ❌   |                  | 目标角色                           |
| `targetBuildings`  | `List<String>` | ❌   |                  | 目标楼栋                           |
| `collectEnabled`   | `Boolean`      | ❌   |                  | 是否启用信息收集                       |
| `collectFields`    | `List<Object>` | ❌   |                  | 收集字段定义                         |
| `status`           | `String`       | ❌   |                  | `draft`/`published`/`archived` |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "ann_demo001",
    "title": "停水通知（更新）",
    "content": "因水管维修，明日8:00-18:00停水，已延长至后天",
    "announcementType": "property_announcement",
    "targetBuildings": ["1栋", "2栋", "3栋", "4栋"],
    "status": "published",
    "updatedAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 11.5 DELETE `/announcements/{id}` — 删除公告

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "ann_demo001",
    "status": "archived"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 11.6 POST `/announcements/{id}/collect` — 提交收集/反馈

**Auth**: Required  
**Roles**: `resident`

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/announcements/ann_demo001/collect \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "collectedData": {
      "needWater": true,
      "contactPhone": "13812345678"
    }
  }'
```

**错误响应**：


| 错误码     | HTTP | 说明       |
| ------- | ---- | -------- |
| `90001` | 404  | 公告不存在    |
| `90003` | 409  | 已提交过收集信息 |


---



## 12. 消息模块 (Messages)



### 12.0 消息通知分类规范

`GET /messages` 列表项使用 **`messageType` 作为消息分类字段**，支持 Query 参数 `messageType` 筛选，无需额外 `category` 字段。

#### 12.0.1 `messageType` 与前端 Tab 映射

| `messageType` | 中文含义 | 前端通知 Tab | 是否在「消息通知」页展示 |
| --- | --- | --- | --- |
| `property_message` | 物业通知 | 物业 | ✅ |
| `platform_message` | 平台/统筹通知 | 统筹 | ✅ |
| `system_message` | 系统通知 | 系统 | ✅ |
| `community_message` | 社区通知 | 社区 | ✅ |
| `technician_message` | 技工通知 / 技工群消息 | 技工群消息（技工端）/ 聊天（业主端） | ✅ |
| `resident_message` | 邻里私聊 | — | ❌（在「聊天」模块 / `GET /messages/conversations` 展示） |
| `official_message` | 官方定向/公告会话 | — | ❌（聊天列表 `conversationType=official`） |
| `service_request` | 服务需求协商 | — | ❌（聊天列表 `conversationType=service_request`） |
| `merchant_ad` | 商家广告卡片 | — | ❌（聊天列表 `conversationType=merchant_ad`） |

#### 12.0.2 未读数分类（`GET /messages/unread-count`）

| 响应字段 | 对应 `messageType` / 场景 |
| --- | --- |
| `propertyUnread` | `property_message` |
| `systemUnread` | `system_message` |
| `merchantUnread` | `community_message`（字段名历史遗留，展示为「社区」） |
| `neighborUnread` | `resident_message` + 私聊 |
| `technicianUnread` | `technician_message`（含业主群发维修求助扇出副本；技工端「技工群消息」Tab 角标） |

---



### 12.1 GET `/messages/neighbor-limit` — 获取邻里限制

**Auth**: Required  
**Roles**: `resident`

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "residentId": "res_b7m9x1",
    "dailyLimit": 2,
    "usedToday": 0,
    "remaining": 2,
    "resetAt": "2026-07-02 00:00:00"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 12.2 POST `/messages/neighbor-chat` — 邻里聊天

**Auth**: Required  
**Roles**: `resident`

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/messages/neighbor-chat \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "toId": "res_neighbor01",
    "content": "你好，方便借用一下工具吗？"
  }'
```

**请求体**：


| 字段        | 类型       | 必填  | 校验                                | 说明    |
| --------- | -------- | --- | --------------------------------- | ----- |
| `toId`    | `String` | ✅   | `@NotBlank`                       | 接收方ID |
| `content` | `String` | ✅   | `@NotBlank @Size(min=1, max=500)` | 消息内容  |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "msg_demo001",
    "fromId": "res_b7m9x1",
    "toId": "res_neighbor01",
    "messageType": "resident_message",
    "content": "你好，方便借用一下工具吗？",
    "chatType": "private",
    "readStatus": "unread",
    "createdAt": "2026-07-01 15:23:33",
    "dailyLimitRemaining": 1,
    "usedToday": 1,
    "remaining": 1
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**业务规则**：

- 消息落库的同时会通过 WebSocket 实时推送给接收方（订阅 `/user/queue/messages`），实现即时聊天，详见 `30.4 实时消息推送`。
- **每日次数仅限制「主动发起新会话」**：当与对方**尚无历史私聊记录**时，本次发送计入当日次数；一旦会话已建立（任一方向发过消息），之后继续聊天**不再计次、不受限制**。
- **屏蔽拦截**：若对方已屏蔽我、或我已屏蔽对方，均无法发送（返回 `403`）。
- 仅限同一小区邻居之间私聊。
- 会话记录可通过 `GET /messages/conversations` 与 `GET /messages/conversations/{peerId}` 查看。

**错误响应**：


| 错误码     | HTTP | 说明                              |
| ------- | ---- | ------------------------------- |
| `90013` | 409  | 今日可发起新邻居会话次数已达上限（会话已建立后继续聊天不计次） |
| `20004` | 403  | 对方已屏蔽你 / 你已屏蔽对方，无法发送（权限不足）      |


> 说明：`20004` 为通用「权限不足」错误码，屏蔽拦截会复用该码并返回具体提示信息。

---



### 12.3 POST `/messages/broadcast` — 广播消息（业主向技工群发维修求助）

**Auth**: Required  
**Roles**: `resident`

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/messages/broadcast \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "content": "家里水管漏水，谁能帮忙维修？",
    "toRole": "technician"
  }'
```

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `content` | `String` | ✅ | 消息内容，1–2000 字符 |
| `toRole` | `String` | ❌ | 目标角色，默认 `technician` |

**成功响应**（返回业主侧**发件箱**记录，`toId` 为 `null`）：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "msg_outbox001",
    "fromId": "res_b7m9x1",
    "fromName": "张*华",
    "toId": null,
    "toRole": null,
    "messageType": "technician_message",
    "content": "家里水管漏水，谁能帮忙维修？",
    "chatType": "broadcast",
    "groupId": null,
    "readStatus": "unread",
    "createdAt": "2026-07-11 17:30:00"
  },
  "timestamp": "2026-07-11 17:30:00"
}
```

**业务规则**：

- 仅 `resident` 可调用；默认 `toRole=technician`（业主向技工群发维修求助）。
- 发送前校验目标技工存在：业主有 `communityId` 时，仅向**同小区** `role=technician` 账号扇出；无 `communityId` 时向同物业下全部技工扇出。
- **双写扇出**：除 1 条发件箱（`toId=null`，供业主查看「我发出的」）外，为每个目标技工各写 1 条收件副本（`toId=技工ID`，`groupId=发件箱ID`）。
- 扇出落库后，对每个技工通过 WebSocket 推送至 `/user/queue/messages`（详见 `30.4`）。
- 技工通过 `GET /messages?messageType=technician_message&chatType=broadcast` 查看；未读计入 `GET /messages/unread-count` 的 `technicianUnread`。
- 技工可用 `POST /messages` 私聊回复业主（见 `12.4`）。

**错误响应**：

| 错误码 | HTTP | 说明 |
| --- | --- | --- |
| `10002` | 400 | 当前小区/物业下没有技术员，无法广播 |

---



### 12.4 POST `/messages` — 发送消息

**Auth**: Required  
**Roles**: `resident`, `property_admin`, `platform_admin`, `technician`, `courier`, `merchant`, `coordinator`, `sector_leader`, `individual_leader`, `activity_leader`

**请求示例（技工回复业主）**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/messages \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "toId": "res_b7m9x1",
    "content": "我可以明天上午上门维修",
    "messageType": "technician_message",
    "chatType": "private"
  }'
```

**请求示例（物业/平台发通知）**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/messages \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "toId": "res_neighbor01",
    "content": "你好，请问明天下午有空吗？",
    "messageType": "resident_message",
    "chatType": "private"
  }'
```

**请求体**：


| 字段            | 类型       | 必填  | 校验                                | 说明                                                          |
| ------------- | -------- | --- | --------------------------------- | ----------------------------------------------------------- |
| `toId`        | `String` | 私聊✅ |                                   | 接收方ID                                                       |
| `content`     | `String` | 条件✅ | `@Size(max=2000)`                 | 消息内容；发卡片消息时可省略，`content` 与 `cardType` 至少填一项 |
| `messageType` | `String` | ✅   | `@NotBlank`                       | 消息类型，见 `12.0`；技工回复用 `technician_message` |
| `chatType`    | `String` | ✅   | `@NotBlank`                       | `private` / `group` / `broadcast` |
| `cardType`    | `String` | ❌   | `product`\|`order`               | 卡片类型，与 `cardId` 成对出现 |
| `cardId`      | `String` | ❌   | —                                 | 卡片关联ID（商品ID或订单ID）；商品卡仅限本物业商品，订单卡仅限订单买卖双方 |

**业务规则**：

- `technician` 仅可发送 `messageType=technician_message`（通常 `chatType=private` 回复业主）。
- 指定 `toId` 时，落库后通过 WebSocket 推送给接收方（`/user/queue/messages`）。
- 业主邻里私聊请使用 `POST /messages/neighbor-chat`，勿用本接口发 `resident_message`。
- **卡片消息**：`cardType` 与 `cardId` 必须成对；商品卡仅限本物业商品，订单卡仅限订单买卖双方；卡片内容读取时实时解析，对象删除后自动降级提示；不新增 `messageType`，会话聚合不受影响。
- **商品卡 `cardId` 两种取值**：① `prd_` 前缀 = 商城商品，点击跳 `pages/service/productDetail?id={cardId}`（`GET /products/{id}`）；② `rml_` 前缀 = 分销橱窗商品（业主商户从一级选品上架，见 §60.7），**不能**当 `GET /products/{id}` 使用（非 `prd_` 前缀返回 `10002`），点击应跳橱窗 `pages/shop/detail?residentId={toId}&listingId={cardId}&resellerId={toId}`。发起方取数：商家/组长店用 `GET /products?mine=true`（`prd_`）；业主商户优先 `GET /resident-merchants/listings`（`rml_`，含 `productName/coverUrl/retailPrice` 可直接渲染卡片）。

**请求示例（商家发商品卡片）**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/messages \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "toId": "res_b7m9x1",
    "content": "这款正好适合你～",
    "messageType": "community_message",
    "chatType": "private",
    "cardType": "product",
    "cardId": "p_123"
  }'
```

---
**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "msg_demo002",
    "fromId": "res_b7m9x1",
    "toId": "res_neighbor01",
    "messageType": "resident_message",
    "content": "你好，请问明天下午有空吗？",
    "cardType": null,
    "card": null,
    "chatType": "private",
    "readStatus": "unread",
    "createdAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**成功响应（卡片消息）**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "msg_demo003",
    "fromId": "res_merchant01",
    "toId": "res_b7m9x1",
    "messageType": "community_message",
    "content": "这款正好适合你～",
    "cardType": "product",
    "card": {
      "cardType": "product",
      "cardId": "p_123",
      "title": "精选有机苹果 5斤装",
      "imageUrl": "https://cdn/xxx.jpg",
      "price": 29.90,
      "status": "active"
    },
    "chatType": "private",
    "readStatus": "unread",
    "createdAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 12.5 GET `/messages` — 消息列表

**Auth**: Required  
**Roles**: `resident`, `property_admin`, `platform_admin`, `coordinator`, `sector_leader`, `individual_leader`, `technician`, `courier`, `merchant`

**请求示例（技工 — 技工群消息 Tab）**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/messages?page=1&pageSize=20&messageType=technician_message&chatType=broadcast" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**请求示例（业主 — 邻里私聊）**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/messages?page=1&pageSize=20&messageType=resident_message&chatType=private" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**请求示例（业主 — 我发出的技工群发）**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/messages?page=1&pageSize=20&messageType=technician_message&chatType=broadcast&fromId={当前用户res_id}" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

> 业主查看「我发出的」群发时，列表仅返回发件箱记录（`toId=null`），不含扇出给技工的副本。
> **卡片消息**：列表项/详情额外返回 `cardType` + `card`（结构见 §12.4），非卡片消息两者均为 `null`；会话列表最后一条消息为纯卡片（无文字）时显示 `[商品卡片]`/`[订单卡片]` 占位。

**Query 参数**：


| 参数            | 类型       | 必填  | 默认值          | 说明                |
| ------------- | -------- | --- | ------------ | ----------------- |
| `page`        | `int`    | ❌   | `1`          | 页码                |
| `pageSize`    | `int`    | ❌   | `20`         | 每页条数              |
| `messageType` | `String` | ❌   |              | 消息分类筛选，见 [§12.0](#120-消息通知分类规范)：另含 `official_message`/`service_request`/`merchant_ad` |
| `chatType`    | `String` | ❌   |              | `private`/`group`/`broadcast`/`official` |
| `readStatus`  | `String` | ❌   |              | `read`/`unread`   |
| `fromId`      | `String` | ❌   |              | 发送方ID筛选           |
| `sort`        | `String` | ❌   | `-createdAt` | 排序                |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "msg_demo001",
        "fromId": "res_neighbor01",
        "fromName": "张*华",
        "fromAvatar": "https://cdn.example.com/avatar/res_neighbor01.jpg",
        "toId": "res_b7m9x1",
        "messageType": "resident_message",
        "content": "你好，方便借用一下工具吗？",
        "cardType": null,
        "card": null,
        "chatType": "private",
        "readStatus": "unread",
        "createdAt": "2026-07-01 15:20:00"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 1,
      "totalPages": 1
    }
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 12.6 GET `/messages/{id}` — 消息详情

**Auth**: Required  
**Roles**: `resident`, `property_admin`, `platform_admin`, `technician`, `courier`, `merchant`

**路径参数**：


| 参数   | 类型       | 必填  | 说明   |
| ---- | -------- | --- | ---- |
| `id` | `String` | ✅   | 消息ID |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "msg_demo001",
    "fromId": "res_neighbor01",
    "fromName": "张*华",
    "fromAvatar": "https://cdn.example.com/avatar/res_neighbor01.jpg",
    "toId": "res_b7m9x1",
    "messageType": "resident_message",
    "content": "你好，方便借用一下工具吗？",
    "chatType": "private",
    "readStatus": "unread",
    "createdAt": "2026-07-01 15:20:00"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 12.7 POST `/messages/{id}/read` — 标记已读

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "msg_demo001",
    "readStatus": "read",
    "updatedAt": "2026-07-01 15:24:00"
  },
  "timestamp": "2026-07-01 15:24:00"
}
```

---



### 12.8 GET `/messages/unread-count` — 获取未读消息数

**Auth**: Required  
**Roles**: `resident`, `property_admin`, `platform_admin`, `merchant`, `courier`, `technician`

**请求示例**：

```bash
curl -X GET https://test.yuanxusoftware.top/api/v1/messages/unread-count \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "totalUnread": 5,
    "systemUnread": 2,
    "propertyUnread": 1,
    "merchantUnread": 1,
    "neighborUnread": 1,
    "technicianUnread": 2
  },
  "timestamp": "2026-07-08 14:00:00"
}
```

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `totalUnread` | `Integer` | 未读消息总数 |
| `systemUnread` | `Integer` | 系统通知未读数 |
| `propertyUnread` | `Integer` | 物业通知未读数 |
| `merchantUnread` | `Integer` | 社区通知未读数 |
| `neighborUnread` | `Integer` | 邻里私聊未读数 |
| `technicianUnread` | `Integer` | 技工群消息未读数（`technician_message`，含业主群发扇出副本） |

---



### 12.9 POST `/messages/neighbors/block` — 屏蔽邻居

**Auth**: Required  
**Roles**: `resident`

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/messages/neighbors/block \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{"blockedId": "res_neighbor01"}'
```

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `blockedId` | `String` | ✅ | 要屏蔽的邻居 ID（`res_` 前缀） |

**成功响应**：`action` 为 `blocked`；重复屏蔽仍返回成功。

**业务规则**：不能屏蔽自己；须同物业；屏蔽后 `chatStatus` 为 `blocked`（§30.1）。

---



### 12.10 POST `/messages/neighbors/unblock` — 取消屏蔽邻居

**Auth**: Required  
**Roles**: `resident`

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/messages/neighbors/unblock \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{"blockedId": "res_neighbor01"}'
```

**成功响应**：`action` 为 `unblocked`；未屏蔽过也返回成功。

---



## 13. 文件模块 (Files)



### 13.1 POST `/files/upload` — 单文件上传

**Content-Type**: `multipart/form-data`

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/files/upload \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -F "file=@/path/to/image.jpg" \
  -F "category=avatar"
```

**参数**：


| 参数         | 类型              | 必填  | 说明                                                      |
| ---------- | --------------- | --- | ------------------------------------------------------- |
| `file`     | `MultipartFile` | ✅   | 文件二进制                                                   |
| `category` | `String`        | ✅   | `avatar`/`announcement`/`merchant`/`service`/`activity`/`product`/`delivery_proof`/`withdrawal_proof` |

> **配送员上传配送凭证**：使用 `category=delivery_proof`，上传成功后把返回的 `url` 填入 `POST /deliveries/{id}/complete` 的 `proofImageUrls`。2026-10-25 起 `courier` 已加入上传角色白名单。
>
> **兼容说明**：旧前端传 `category=delivery`、`deliveryProof` 或 `proof` 时，后端会自动归一为 `delivery_proof`；`category=service` 仍可用，但 `service` 分类有“每个上传者累计最多 6 个文件”的历史限制，**配送凭证请改用 `delivery_proof`**（10MB、支持 mp4、不限累计数量，每单最多 5 张由业务字段限制）。
>
> **提现/转账凭证（v8.28 新增）**：财务人工转账凭证使用 `category=withdrawal_proof`（10MB、jpg/jpeg/png/webp、不限累计数量）。**不要再用 `proof`**——`proof` 只是配送端的兼容别名，会被归入 `delivery_proof`，导致财务凭证与配送凭证混桶。


**文件限制**：


| 类别    | 最大大小  | 允许格式                |
| ----- | ----- | ------------------- |
| 头像    | 2 MB  | jpg, png, webp      |
| 公告图片  | 5 MB  | jpg, png, webp      |
| 商家图片  | 5 MB  | jpg, png, webp      |
| 商家视频  | 50 MB | mp4, mov            |
| 服务图片  | 5 MB  | jpg, png, webp      |
| 活动组图片 | 10 MB | jpg, png, webp, mp4 |
| 社区图片   | 10 MB | jpg, jpeg, png, webp |
| 配送凭证   | 10 MB | jpg, jpeg, png, webp, mp4 |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "fileId": "file_demo001",
    "originalName": "image.jpg",
    "storedName": "res_b7m9x1_file_demo001.jpg",
    "url": "https://test.yuanxusoftware.top/api/v1/static/avatar/res_b7m9x1_file_demo001.jpg",
    "thumbnailUrl": null,
    "size": 204800,
    "mimeType": "image/jpeg",
    "category": "avatar"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 13.2 POST `/files/upload-batch` — 批量文件上传

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/files/upload-batch \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -F "files=@/path/to/image1.jpg" \
  -F "files=@/path/to/image2.jpg" \
  -F "category=merchant"
```

---



### 13.3 GET `/files/{id}` — 获取文件详情

**Auth**: Required  
**Roles**: `resident`, `property_admin`, `platform_admin`

**路径参数**：


| 参数   | 类型       | 必填  | 说明   |
| ---- | -------- | --- | ---- |
| `id` | `String` | ✅   | 文件ID |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "file_demo001",
    "originalName": "image.jpg",
    "storedName": "res_b7m9x1_file_demo001.jpg",
    "url": "https://test.yuanxusoftware.top/api/v1/static/avatar/res_b7m9x1_file_demo001.jpg",
    "thumbnailUrl": null,
    "size": 204800,
    "mimeType": "image/jpeg",
    "category": "avatar",
    "uploadedBy": "res_b7m9x1",
    "createdAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 13.4 DELETE `/files/{id}` — 删除文件

**Auth**: Required  
**Roles**: `resident`（仅自己上传的）, `property_admin`, `platform_admin`

**路径参数**：


| 参数   | 类型       | 必填  | 说明   |
| ---- | -------- | --- | ---- |
| `id` | `String` | ✅   | 文件ID |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "file_demo001",
    "deleted": true,
    "deletedAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



## 14. 物业公司模块 (Property Companies)



### 14.1 GET `/property-companies/current` — 获取当前配置

**Auth**: Required  
**Roles**: `resident`, `merchant`, `courier`, `activity_leader`, `technician`

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "pc_demo001",
    "name": "演示物业公司",
    "logoUrl": "https://cdn.example.com/logo/pc_demo001.jpg",
    "contactPhone": "0427-1234567",
    "address": "盘锦市XX区XX路",
    "distributionRates": {
      "propertyRate": 0.30,
      "coordinatorRate": 0.70,
      "sectorLeaderRate": 0.10,
      "individualLeaderRate": 0.00
    },
    "coinDisplayEnabled": true,
    "pointEnabled": true,
    "coinEnabled": true,
    "pointDisplayEnabled": true,
    "coinIssueMode": "auto",
    "coinExpiryDays": 365,
    "deliveryConfig": {
      "baseFee": 0.00,
      "courierShareRate": 1.00
    },
    "withdrawalFeeRate": 0.006,
    "pointToFeeRate": 0.01,
    "twoYearClearEnabled": true,
    "neighborDailyContactLimit": 2
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

> **功能开关（2026-08 新增）**：
> - `pointEnabled`：积分功能是否启用；`false` 时前端隐藏积分的展示与全部相关能力（建设积分、支付抵扣、商家购分/赠分等）
> - `coinEnabled`：物业币功能是否启用；`false` 时隐藏物业币的展示与全部相关能力
> - `pointDisplayEnabled`：积分卡片是否全局展示
> - `coinDisplayEnabled`：物业币卡片是否展示（原有字段）
> - 四项缺省均为 `true`；`GET /residents/me/points`、`GET /residents/me/coins` 同步返回对应开关，前端两种命名都读
>
> **服务端拦截（2026-08，2026-10-30 修正错误码）**：`pointEnabled=false` / `coinEnabled=false` 时，后端**写接口拒绝执行**（不仅靠前端隐藏），包括：支付抵扣（积分抵扣物业费、订单/预约/订阅/服务需求等积分或物业币支付）、转给物业（§2.6.4）、商家购分/赠分（§22）、建设积分调整（§78）、物业币充值/兑换、物业币冻结解冻之外的发放消费等，分别返回 `97070`（本小区未启用积分功能）/ `97071`（本小区未启用物业币功能）。
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 14.2 GET `/property-companies` — 物业公司列表

**Auth**: Optional  
**Roles**: `any`

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/property-companies?page=1&pageSize=20&keyword=演示&status=active" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：


| 参数         | 类型       | 必填  | 默认值          | 说明                  |
| ---------- | -------- | --- | ------------ | ------------------- |
| `page`     | `int`    | ❌   | `1`          | 页码                  |
| `pageSize` | `int`    | ❌   | `20`         | 每页条数                |
| `keyword`  | `String` | ❌   |              | 搜索关键词（名称/电话）        |
| `status`   | `String` | ❌   |              | `active`/`inactive` |
| `sort`     | `String` | ❌   | `-createdAt` | 排序                  |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "pc_demo001",
        "name": "演示物业公司",
        "logoUrl": "https://cdn.example.com/logo/pc_demo001.jpg",
        "contactPhone": "0427-1234567",
        "address": "盘锦市XX区XX路",
        "communityCount": 3,
        "status": "active",
        "createdAt": "2026-01-01 00:00:00"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 1,
      "totalPages": 1
    }
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 14.3 POST `/property-companies` — 创建物业公司

**Auth**: Required  
**Roles**: `platform_admin`

> **2026-09-06 修复说明**：
> 1. 必填项齐全时不再出现 500：公司重名、管理员手机号已占用、小区名称重复等业务问题统一返回 **HTTP 400** + 明确 `message`（`code=10002`）；并发触发的唯一约束冲突也已兜底转为 400。
> 2. 创建成功后初始管理员账号自动写入**随机生成的 14 位初始密码**（BCrypt 落库），可直接 `POST /auth/admin-login` 登录；密码仅在创建响应的 `adminAccount.initialPassword` 中返回一次，详情接口不返回。
> 3. 对外推荐路径：`POST /property-companies`（本节）；`POST /admin/property-companies`（§51.2）为管理端等价入口，两者共用同一套校验与创建逻辑，行为一致。创建接口忽略 Query `propertyCompanyId`，附带时不影响创建。

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/property-companies \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "name": "新物业公司",
    "contactPhone": "0427-9999999",
    "communities": [
      { "name": "新小区", "totalBuildings": 10, "totalUnits": 500 }
    ],
    "adminAccount": {
      "name": "管理员",
      "phone": "13800000000"
    }
  }'
```

**请求体**：

| 字段 | 类型 | 必填 | 校验 | 说明 |
| --- | --- | --- | --- | --- |
| `name` | `String` | ✅ | `@NotBlank` | 物业公司名称（全局唯一，重复返回 400「物业公司名称已存在」） |
| `logoUrl` | `String` | ❌ | | Logo URL |
| `contactPhone` | `String` | ✅ | `@NotBlank` | 公司联系电话 |
| `address` | `String` | ❌ | | 地址 |
| `communities` | `List` | ✅ | `@NotEmpty @Valid` | 小区列表（至少1个；同一请求内小区名不得重复） |
| `communities[].name` | `String` | ✅ | `@NotBlank` | 小区名称 |
| `communities[].address` | `String` | ❌ | | 小区地址 |
| `communities[].totalBuildings` | `Integer` | ❌ | | 楼栋总数（统计字段，不生成房号） |
| `communities[].totalUnits` | `Integer` | ❌ | | 单元总数（统计字段，不生成房号） |
| `adminAccount` | `Object` | ✅ | `@Valid` | 初始管理员账号 |
| `adminAccount.name` | `String` | ✅ | `@NotBlank` | 管理员姓名 |
| `adminAccount.phone` | `String` | ✅ | `@NotBlank` | 管理员手机号（登录账号，全局唯一；已占用返回 400） |
| `adminAccount.email` | `String` | ❌ | | 管理员邮箱 |

**成功响应**（`code=0`）：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "pc_xxxx1234",
    "name": "新物业公司",
    "status": "active",
    "communities": [
      { "id": "com_xxxx1234", "name": "新小区", "totalBuildings": 10, "totalUnits": 500, "status": "active" }
    ],
    "adminAccount": {
      "id": "res_xxxx1234",
      "name": "管理员",
      "phone": "13800000000",
      "role": "property_admin",
      "initialPassword": "A7kP2mQ9xR4tZ6"
    },
    "defaultConfigs": "已自动初始化默认配置项",
    "createdAt": "2026-09-06 10:00:00"
  },
  "timestamp": "2026-09-06 10:00:00"
}
```

**业务错误**（均为 HTTP 400，`code=10002`）：

| message | 触发条件 |
| --- | --- |
| `物业公司名称已存在` | 名称与现有公司重复 |
| `管理员手机号已被使用: 138****0000` | 手机号已被任何账号占用 |
| `小区名称重复: XX` | 同一请求内小区名重复 |
| `数据已存在或违反唯一性约束…` | 并发下触发唯一约束（兜底） |

> 创建成功后自动初始化：默认分成/物业币/配送配置、积分池（`point_pools`）、默认配送规则（`delivery_rules`）。

---



### 14.4 GET `/property-companies/{id}/communities` — 小区列表

**Auth**: Optional  
**Roles**: `any`

**路径参数**：


| 参数   | 类型       | 必填  | 说明     |
| ---- | -------- | --- | ------ |
| `id` | `String` | ✅   | 物业公司ID |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "com_demo001",
        "name": "演示小区",
        "address": "盘锦市XX区XX路1号",
        "totalBuildings": 10,
        "totalUnits": 500,
        "status": "active",
        "createdAt": "2026-01-01 00:00:00"
      }
    ]
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 14.5 GET `/property-companies/{id}` — 物业公司详情

**Auth**: Optional  
**Roles**: `any`

> **2026-10-30 纠偏**：下方响应示例含历史规划字段，实际详情不返回 `deliveryPerKgFee`、`pointExchangeRate`、`pointDisplayEnabled`、`regionalLeaderRate`、`projectLeaderRate`、4 项积分分成等；`pointEnabled` / `coinEnabled` 已于 2026-10-30 补入。实际字段以 §51.3 为准。

**路径参数**：


| 参数   | 类型       | 必填  | 说明     |
| ---- | -------- | --- | ------ |
| `id` | `String` | ✅   | 物业公司ID |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "pc_demo001",
    "name": "演示物业公司",
    "logoUrl": "https://cdn.example.com/logo/pc_demo001.jpg",
    "contactPhone": "0427-1234567",
    "address": "盘锦市XX区XX路",
    "distributionRates": {
      "propertyRate": 0.30,
      "coordinatorRate": 0.70,
      "sectorLeaderRate": 0.10,
      "individualLeaderRate": 0.00
    },
    "coinDisplayEnabled": true,
    "coinIssueMode": "auto",
    "coinExpiryDays": 365,
    "deliveryConfig": {
      "baseFee": 0.00,
      "courierShareRate": 1.00
    },
    "withdrawalFeeRate": 0.006,
    "pointToFeeRate": 0.01,
    "twoYearClearEnabled": true,
    "neighborDailyContactLimit": 2,
    "deliveryPerKgFee": 0.00,
    "pointExchangeRate": 100.00,
    "platformShareRate": 0.05,
    "platformDeliveryShareRate": 0.00,
    "platformWithdrawalFeeShareRate": 1.00,
    "regionalLeaderRate": 0.00,
    "projectLeaderRate": 0.00,
    "autoWithdrawalEnabled": false,
    "autoWithdrawalPeriodDays": 7,
    "coinUseCondition": null,
    "coinPointThreshold": 0,
    "companyAccountBalance": 0.00,
    "residentPointShareRate": 0.0000,
    "merchantPointShareRate": 0.0000,
    "coinPointShareRate": 0.0000,
    "sharedPointShareRate": 0.0000,
    "communities": [
      {
        "id": "com_demo001",
        "name": "演示小区",
        "totalBuildings": 10,
        "totalUnits": 500
      }
    ],
    "status": "active",
    "createdAt": "2026-01-01 00:00:00",
    "updatedAt": "2026-06-28 14:30:00"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

> **说明**：Converter 补全 12 个已定义但未填充的字段（`deliveryPerKgFee`/`pointExchangeRate`/`platformShareRate` 等）；新增 4 个积分分成比例字段。新建物业公司默认 `deliveryPerKgFee=0`、`pointExchangeRate=100`。

---

### 14.6 PUT `/property-companies/{id}` — 更新物业公司（增强）

**Auth**: Required  
**Roles**: `property_admin`（仅本公司）、`platform_admin`

> **2026-10-30 纠偏**：本节旧字段清单已过时。§14.6 与 §51.4 共用同一个 `UpdatePropertyCompanyRequest`；请求体不接收 `deliveryPerKgFee`、`pointDisplayEnabled`、`pointExchangeRate`、`residentPointShareRate` / `merchantPointShareRate` / `coinPointShareRate` / `sharedPointShareRate`、`regionalLeaderRate`、`projectLeaderRate` 等字段，传入会被静默忽略。实际字段、角色与校验以 §51.4 / §51.6 为准；4 项积分分成请走 §65 价格审批。

**路径参数**：


| 参数   | 类型       | 必填  | 说明     |
| ---- | -------- | --- | ------ |
| `id` | `String` | ✅   | 物业公司ID |


**请求示例**：

```bash
curl -X PUT https://test.yuanxusoftware.top/api/v1/property-companies/pc_demo001 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "name": "演示物业公司（更新）",
    "contactPhone": "0427-7654321",
    "address": "盘锦市XX区XX路2号"
  }'
```

**请求体**：


| 字段             | 类型       | 必填  | 校验               | 说明       |
| -------------- | -------- | --- | ---------------- | -------- |
| `name`         | `String` | ❌   | `@Size(max=100)` | 公司名称     |
| `logoUrl`      | `String` | ❌   | `@Size(max=500)` | Logo URL |
| `contactPhone` | `String` | ❌   | `@Size(max=20)`  | 联系电话     |
| `address`      | `String` | ❌   | `@Size(max=200)` | 地址       |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "pc_demo001",
    "name": "演示物业公司（更新）",
    "contactPhone": "0427-7654321",
    "address": "盘锦市XX区XX路2号",
    "updatedAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



## 15. 物业费模块 (Property Fees)



### 15.1 GET `/property-fees/my` — 我的物业费

**Auth**: Required  
**Roles**: `resident`

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/property-fees/my?page=1&pageSize=10&paymentStatus=unpaid" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：


| 参数              | 类型       | 必填  | 默认值        | 说明                                         |
| --------------- | -------- | --- | ---------- | ------------------------------------------ |
| `page`          | `int`    | ❌   | `1`        | 页码                                         |
| `pageSize`      | `int`    | ❌   | `20`       | 每页条数                                       |
| `feeType`       | `String` | ❌   |            | `property`/`parking`/`water`/`electricity` |
| `paymentStatus` | `String` | ❌   |            | `unpaid`/`partial`/`paid`/`overdue`        |
| `period`        | `String` | ❌   |            | 账期                                         |
| `sort`          | `String` | ❌   | `-dueDate` | 排序                                         |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "pf_demo001",
        "residentId": "res_b7m9x1",
        "residentName": "张先生",
        "familyId": "fam_demo001",
        "communityId": "com_demo001",
        "communityName": "演示小区",
        "building": "3栋",
        "room": "502",
        "feeType": "property",
        "feeTypeName": "物业费",
        "period": "2026-Q3",
        "periodStart": "2026-07-01",
        "periodEnd": "2026-09-30",
        "amount": 100.00,
        "paidAmount": 0.00,
        "remainingAmount": 100.00,
        "pointDeducted": 0,
        "coinDeducted": 0.00,
        "paymentStatus": "unpaid",
        "dueDate": "2026-07-31",
        "paidAt": null,
        "autoDeduct": false,
        "overdueDays": 0,
        "lateFee": 0.00,
        "createdAt": "2026-07-01 10:00:00"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 10,
      "total": 1,
      "totalPages": 1
    }
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 15.2 POST `/property-fees/{id}/pay` — 缴纳物业费

**Auth**: Required  
**Roles**: `resident`

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/property-fees/pf_demo001/pay \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "paymentMethod": "mixed",
    "amount": 100.00,
    "pointUsed": 500,
    "coinUsed": 50.00
  }'
```

**请求体**：


| 字段              | 类型           | 必填  | 校验                             | 说明                                            |
| --------------- | ------------ | --- | ------------------------------ | --------------------------------------------- |
| `paymentMethod` | `String`     | ✅   | `@NotBlank`                    | 支付方式：`point`/`property_coin`/`wechat`/`mixed` |
| `amount`        | `BigDecimal` | ✅   | `@NotNull @DecimalMin("0.01")` | 支付金额                                          |
| `pointUsed`     | `Integer`    | ❌   | `@Min(0)`                      | 使用积分                                          |
| `coinUsed`      | `BigDecimal` | ❌   | `@DecimalMin("0.00")`          | 使用物业币                                         |
| `wechatPayCode` | `String`     | ❌   |                                | 微信支付code                                      |
| `familyId`      | `String`     | ❌   |                                | 使用家庭余额                                        |
| `openid`        | `String`     | ❌   |                                | 用户openid（`wechat`/`mixed` 含现金部分时使用；不传则取用户绑定openid） |


**成功响应**（`point`/`property_coin`/开发模式）：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "propertyFeeId": "pf_demo001",
    "paidAmount": 100.00,
    "totalPaidAmount": 100.00,
    "remainingAmount": 0.00,
    "paymentStatus": "paid",
    "paymentMethod": "mixed",
    "pointUsed": 500,
    "coinUsed": 50.00,
    "cashAmount": 0.00,
    "paidAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**成功响应**（`wechat`/`mixed` 含现金部分，返回微信预下单参数）：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "propertyFeeId": "pf_demo001",
    "paidAmount": 0.00,
    "totalPaidAmount": 0.00,
    "remainingAmount": 100.00,
    "paymentStatus": "unpaid",
    "paymentMethod": "wechat",
    "pointUsed": 0,
    "coinUsed": 0.00,
    "cashAmount": 100.00,
    "paidAt": null,
    "paymentRecordId": "pr_2f9c1a",
    "payParams": {
      "appId": "wxa1b2c3d4e5f6",
      "timeStamp": "1735660000",
      "nonceStr": "5K8264ILTKCH16CQ2502SI8ZNMTM67VS",
      "package": "prepay_id=wx201410272009395522657a690389285100",
      "signType": "RSA",
      "paySign": "..."
    }
  },
  "timestamp": "2026-10-08 15:23:33"
}
```

> **`wechat`/`mixed` 含现金部分说明**：成功响应返回 `paymentRecordId` + `payParams`（前端原样透传 `wx.requestPayment`），此时账单 `paymentStatus` 仍为 `unpaid`（待微信支付），**属预期，勿当失败**；支付成功后由回调/查询兜底确认，`paymentStatus` 变更为 `paid`。
>
> **约束**：含现金部分时才走微信预下单（`cashAmount > 0`）；纯 `point`/`property_coin` 即时到账。微信支付未开通时返回 `10002`（fail-closed，不会静默置为已缴）；同一账单已有待支付流水时返回 `70003`（防重复扣款）。

---



### 15.3 GET `/property-fees/{id}` — 物业费详情

**Auth**: Required  
**Roles**: `resident`（本人/家庭）, `property_admin`, `platform_admin`

**路径参数**：


| 参数   | 类型       | 必填  | 说明      |
| ---- | -------- | --- | ------- |
| `id` | `String` | ✅   | 物业费账单ID |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "pf_demo001",
    "residentId": "res_b7m9x1",
    "residentName": "张先生",
    "familyId": "fam_demo001",
    "communityId": "com_demo001",
    "communityName": "演示小区",
    "building": "3栋",
    "room": "502",
    "feeType": "property",
    "feeTypeName": "物业费",
    "period": "2026-Q3",
    "periodStart": "2026-07-01",
    "periodEnd": "2026-09-30",
    "amount": 100.00,
    "paidAmount": 0.00,
    "remainingAmount": 100.00,
    "pointDeducted": 0,
    "coinDeducted": 0.00,
    "paymentStatus": "unpaid",
    "dueDate": "2026-07-31",
    "paidAt": null,
    "autoDeduct": false,
    "overdueDays": 0,
    "lateFee": 0.00,
    "createdAt": "2026-07-01 10:00:00"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 15.4 GET `/property-fees` — 物业费列表（管理员）

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/property-fees?page=1&pageSize=20&feeType=property&paymentStatus=unpaid&communityId=com_demo001" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：


| 参数              | 类型       | 必填  | 默认值        | 说明                                         |
| --------------- | -------- | --- | ---------- | ------------------------------------------ |
| `page`          | `int`    | ❌   | `1`        | 页码                                         |
| `pageSize`      | `int`    | ❌   | `20`       | 每页条数                                       |
| `feeType`       | `String` | ❌   |            | `property`/`parking`/`water`/`electricity` |
| `paymentStatus` | `String` | ❌   |            | `unpaid`/`partial`/`paid`/`overdue`        |
| `period`        | `String` | ❌   |            | 账期                                         |
| `communityId`   | `String` | ❌   |            | 小区ID                                       |
| `sort`          | `String` | ❌   | `-dueDate` | 排序                                         |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "pf_demo001",
        "residentId": "res_b7m9x1",
        "residentName": "张先生",
        "familyId": "fam_demo001",
        "communityId": "com_demo001",
        "communityName": "演示小区",
        "building": "3栋",
        "room": "502",
        "feeType": "property",
        "feeTypeName": "物业费",
        "period": "2026-Q3",
        "periodStart": "2026-07-01",
        "periodEnd": "2026-09-30",
        "amount": 100.00,
        "paidAmount": 0.00,
        "remainingAmount": 100.00,
        "paymentStatus": "unpaid",
        "dueDate": "2026-07-31",
        "createdAt": "2026-07-01 10:00:00"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 1,
      "totalPages": 1
    }
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 15.5 POST `/property-fees` — 创建物业费账单

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/property-fees \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "residentId": "res_b7m9x1",
    "feeType": "property",
    "period": "2026-Q3",
    "periodStart": "2026-07-01",
    "periodEnd": "2026-09-30",
    "amount": 100.00,
    "dueDate": "2026-07-31"
  }'
```

**请求体**：


| 字段            | 类型           | 必填  | 校验                             | 说明                                              |
| ------------- | ------------ | --- | ------------------------------ | ----------------------------------------------- |
| `residentId`  | `String`     | ✅   | `@NotBlank`                    | 住户ID                                            |
| `feeType`     | `String`     | ✅   | `@NotBlank`                    | 费用类型：`property`/`parking`/`water`/`electricity` |
| `period`      | `String`     | ✅   | `@NotBlank`                    | 账期（如 `2026-Q3`、`2026-07`）                       |
| `periodStart` | `String`     | ❌   |                                | 账期起始日 `yyyy-MM-dd`                       |
| `periodEnd`   | `String`     | ❌   |                                | 账期结束日 `yyyy-MM-dd`                       |
| `amount`      | `BigDecimal` | ✅   | `@NotNull @DecimalMin("0.01")` | 应缴金额                                            |
| `communityId` | `String`     | ❌   |                                | 小区ID                                            |
| `dueDate`     | `String`     | ❌   |                                | 截止日期 `yyyy-MM-dd`                               |
| `autoDeduct`  | `Boolean`    | ❌   |                                | 是否自动抵扣积分/物业币                                    |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "pf_demo002",
    "residentId": "res_b7m9x1",
    "residentName": "张先生",
    "feeType": "property",
    "period": "2026-Q3",
    "periodStart": "2026-07-01",
    "periodEnd": "2026-09-30",
    "amount": 100.00,
    "paidAmount": 0,
    "paymentStatus": "unpaid",
    "dueDate": "2026-07-31",
    "createdAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**错误响应**：


| 错误码     | HTTP | 说明          |
| ------- | ---- | ----------- |
| `30001` | 404  | 住户不存在       |
| `71002` | 409  | 该账期已存在物业费账单 |


---



### 15.6 POST `/admin/property-fees/arrears-reminder` — 催缴通知（统一发送）

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

依据**预览快照**（`previewToken`）固定收件人范围发送催缴消息；按 `channels` 分别送达并拆分统计。标准流程：先调 §15.7 预览获取 `previewToken` → 管理员确认 → 调本接口并**原样回传 `previewToken`**。`requestId` 幂等，重复提交同一 `requestId` 不重复发送。同一住户多条欠费合并为一条**个性化催缴内容**（姓名/小区/房屋/账期/金额/截止日）。

**请求体**：

| 字段 | 类型 | 必填 | 校验 | 说明 |
| --- | --- | --- | --- | --- |
| `propertyCompanyId` | String | ❌* | 平台管理员必填，须 `pc_` 开头 | 物业公司ID。`property_admin` 自动取当前物业 |
| `communityId` | String | ❌ | | 社区ID。留空=全公司（以预览快照为准） |
| `building` | String | ❌ | | 楼栋（以预览快照为准） |
| `feeType` | String | ❌ | | 费用类型（以预览快照为准） |
| `templateCode` | String | ❌ | 默认 `property_fee_arrears_reminder_v1` | 业务模板编码（前端只传编码，不传微信模板ID） |
| `previewToken` | String | ✅ | | §15.7 返回的快照令牌；缺失/过期 → `PREVIEW_EXPIRED`。**兼容 `preview_token`（snake_case）**，两者取同一值即可 |
| `channels` | Array | ❌ | 元素 `in_app`/`wechat` | 发送渠道；缺省 `["in_app","wechat"]`。**以请求体为准**：传 `["in_app"]` 则跳过微信模板校验，不发微信 |
| `requestId` | String | ❌ | | 幂等ID；重复提交返回 `ARREARS_REMINDER_DUPLICATE` |
| `title` | String | ✅ | @NotBlank @Size(max=100) | 催缴标题（应用内消息标题） |
| `content` | String | ✅ | @NotBlank @Size(max=1000) | 催缴内容（应用内正文 / 微信温馨提示） |

**请求示例**：

```json
{
  "previewToken": "prev_8f3a...",
  "propertyCompanyId": "pc_demo001",
  "templateCode": "property_fee_arrears_reminder_v1",
  "title": "物业费催缴通知",
  "content": "如已缴费请忽略本通知",
  "channels": ["in_app", "wechat"],
  "requestId": "arrears_20260813_001"
}
```

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "batchId": "batch_20260813abc",
    "recipientCount": 120,
    "notifiedCount": 116,
    "skippedCount": 3,
    "failedCount": 1,
    "channels": {
      "inApp": { "successCount": 120, "failedCount": 0 },
      "wechat": { "successCount": 82, "skippedCount": 37, "failedCount": 1 }
    }
  },
  "timestamp": "2026-08-13 16:00:00"
}
```

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `batchId` | String | 批次ID |
| `recipientCount` | int | 唯一住户数（按 residentId 去重） |
| `notifiedCount` | int | 至少一个渠道发送成功的唯一住户数 |
| `skippedCount` | int | 两个渠道都不具备发送条件的住户数（如仅微信且未绑定） |
| `failedCount` | int | 具备条件但全部发送失败的住户数 |
| `channels.inApp.successCount` / `.failedCount` | int | 应用内消息统计 |
| `channels.wechat.successCount` / `.skippedCount` / `.failedCount` | int | 微信统计（`skippedCount`=未绑定/无 openid 回退应用内） |

> 去重：已存在 `relatedTaskId=arrears_reminder` + `readStatus=unread` 消息的住户跳过。微信不可达时仅发应用内消息，不报错；微信模板字段由后端按 `wechat.mp.template.arrears-reminder-fields` 映射。审计记录（发送人/筛选/模板/各渠道结果）由后端**落库持久化至 `arrears_reminder_audit` 表**；预览快照（`arrears_reminder_preview`）与幂等 `requestId`（`arrears_reminder_request`）同样落库，应用重启不丢失。过期预览快照可经 `DELETE FROM arrears_reminder_preview WHERE expires_at < NOW()` 清理。

> **错误响应（2026-09-07 实测校准）**：本接口不再以全局兜底「数据访问失败」返回 HTTP 500。实际返回的错误码（与后端 `ErrorCode` 枚举一致，历史文档中的 97003/97004/97005/97017 为规划值，**后端从未实现**，请勿按其判等）：
> - `96033 PREVIEW_EXPIRED`（HTTP 400）：`previewToken` 缺失/过期（30 分钟），需重新预览。注意：预览成功≠token 未过期，过期只在此处返回，不会在落库阶段误报 500。
> - `10002 PARAM_INVALID`（HTTP 400）：同一 `requestId` 重复提交，message 为「该 requestId 已发送过，请勿重复提交」。
> - `99001 SYSTEM_ERROR`（HTTP 500）：消息落库 / 审计写入 / 幂等记录写入失败（如数据库约束异常、主键缺失）。出现此码说明范围锁定与消息构造均正常，问题在落库/审计环节，需排查后端 `consumePreview`、`recordAudit`、`markProcessed` 及物业聊天（`relatedTaskId=arrears_reminder`）insert 日志。
>
> > **2026-09-07 校准说明**：此前文档标注的 `97004 PREVIEW_EXPIRED` / `97005 ARREARS_REMINDER_DUPLICATE` / `97017 ARREARS_REMINDER_PERSIST_FAILED` 在后端代码中不存在；实测为 `96033`（预览过期）与 `10002`（重复提交，通用参数错误码）。前端判等请改用数字码 `96033` / `10002`，或按 message 关键字「已发送过」兜底识别重复提交。
> `channels: ["in_app"]` 时跳过微信模板校验，仅写应用内消息 + WebSocket 推送，返回 `code=0` 及 `notifiedCount` 等统计。



---





### 15.7 POST `/admin/property-fees/arrears-reminder/preview` — 催缴人数预览（重做）

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

发送前预览欠费范围与金额，生成**预览快照**返回 `previewToken`（30 分钟有效，落库持久化至 `arrears_reminder_preview` 表），发送时须原样回传以锁定收件人范围（防止预览后数据变化导致范围漂移）。

> **变更说明（v6.4 补丁）**：修复预览快照未落库导致 `PREVIEW_EXPIRED` (97004) 的缺陷——预览方法原先标注 `@Transactional(readOnly = true)`，Hibernate FlushMode 被设为 MANUAL，`savePreview` 的 INSERT 不会被 flush，发送时 `consumePreview` 查不到快照。已改为 `@Transactional`（写事务），预览返回的 token 立即可在表中查到。同时 `expiresAt` 格式从无偏移的 `LocalDateTime.toString()` 改为带时区偏移的 ISO-8601（如 `+08:00`），避免前端误判为 UTC。

**请求体**（仅需筛选条件；`title` / `content` 预览时不校验）：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `propertyCompanyId` | String | ❌* | 同 §15.6 |
| `communityId` | String | ❌ | 社区筛选 |
| `building` | String | ❌ | 楼栋筛选 |
| `feeType` | String | ❌ | 费用类型筛选 |
| `templateCode` | String | ❌ | 业务模板编码 |
| `channels` | Array | ❌ | 发送渠道，元素 `in_app`/`wechat`；缺省 `["in_app","wechat"]`。仅预览也建议与发送时保持一致 |
| `requestId` | String | ❌ | 幂等请求ID；预览阶段可传，发送时用于幂等判重 |

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "templateCode": "property_fee_arrears_reminder_v1",
    "previewToken": "prev_8f3a...",
    "expiresAt": "2026-08-13T14:40:00+08:00",
    "recipientCount": 120,
    "totalArrearsAmount": 85600.50,
    "inAppEligibleCount": 120,
    "wechatEligibleCount": 83,
    "wechatIneligibleCount": 37,
    "duplicateResidentCount": 5
  },
  "timestamp": "2026-08-13 16:00:00"
}
```

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `templateCode` | String | 业务模板编码 |
| `previewToken` | String | 快照令牌，发送时回传 |
| `expiresAt` | String | 快照过期时间（ISO-8601，**带时区偏移**，如 `2026-08-13T14:40:00+08:00`；预览后 30 分钟） |
| `recipientCount` | int | 唯一住户数（按 residentId 去重） |
| `totalArrearsAmount` | BigDecimal | 欠费总额（去重住户合计） |
| `inAppEligibleCount` | int | 应用内可触达（全员） |
| `wechatEligibleCount` | int | 微信可触达（已绑定微信且存在 openid） |
| `wechatIneligibleCount` | int | 微信不可触达（将回退应用内） |
| `duplicateResidentCount` | int | 被去重的欠费记录数（总记录数 - 唯一住户数） |

> 注：规范写为 `GET`，因携带请求体此处用 `POST`。微信可触达仅判断「已绑定微信且有 openid」（无法服务端校验订阅授权），不可达者回退应用内消息。

---

### 15.8 前端对接与联调前确认项

本节为管理员端催缴（§15.6 / §15.7）与分成校验（§14.6 / §65.2）的上线联调清单，供前端对齐。

**A. 微信催缴模板配置（后端联调前完成）**
- 后端 `application.yml` 的 `wechat.mp.template.arrears-reminder`（环境变量 `WECHAT_ARREARS_TEMPLATE_ID`）须配置真实微信订阅消息模板 ID；跳转路径 `wechat.mp.template.arrears-reminder-page`（环境变量 `WECHAT_ARREARS_PAGE_PATH`）；字段映射 `wechat.mp.template.arrears-reminder-fields`（语义字段→微信模板字段，默认 `thing1/thing2/amount1/date1/thing3`，以实际模板为准）。
- 前端只传业务模板编码 `property_fee_arrears_reminder_v1`，**不传微信模板 ID**（防篡改）。
- `channels` 含 `wechat` 但未配置模板 → 返回 `97003 WECHAT_TEMPLATE_NOT_CONFIGURED`（不再静默回退）。

**B. 前端订阅授权（必做，否则收不到微信消息）**
- 微信订阅消息必须由用户主动触发，后端无法代授权。前端在用户手势中调用：
  `wx.requestSubscribeMessage({ tmplIds: [模板ID], success(res){ /* res[模板ID]==='accept' 即订阅成功 */ } })`
- 建议时机：住户首次进入物业费页、或「开启微信催缴提醒」按钮。
- 未授权用户：后端调微信返回 `43101`，自动回退应用内消息（不报错）；前端可在住户端提示「开启微信提醒」引导授权。

**C. 分校验错误码判等（前端）**
- 分成比例合计 ≠ 100% 时后端返回：
  `{ "code": 97002, "errorCode": "INVALID_SHARE_RATE_TOTAL", "message": "业主/商家/物业币/共享积分分成比例合计必须为100%", "data": null }`
- 前端用 **`errorCode === "INVALID_SHARE_RATE_TOTAL"`** 字符串判等（建议勿依赖数字 `97002`，枚举名稳定）。用于高亮 `residentPointShareRate` / `merchantPointShareRate` / `coinPointShareRate` / `sharedPointShareRate` 并提示「四项合计须为 100%」。

**D. 催缴预览 → 发送 流程建议**
- 先 `POST /arrears-reminder/preview`：请求 `propertyCompanyId` / `communityId` / `building` / `feeType`（可选），响应含 `previewToken` 与 `recipientCount` / `totalArrearsAmount` / `wechatEligibleCount` 等，据此展示「预计通知 X 人（微信 Y / 应用内 Z，去重跳过 W）」。
- 管理员确认后 `POST /arrears-reminder`：回传 `previewToken`（也接受 `preview_token` snake_case），补齐 `title` / `content` / `channels` / `requestId`，读取 `notifiedCount` / `skippedCount` / `failedCount` 及 `channels.inApp` / `channels.wechat` 拆分统计。
- `previewToken` 过期（30 分钟）或缺失 → `96033 PREVIEW_EXPIRED`，需重新预览；同一 `requestId` 重复提交 → `10002`（message「该 requestId 已发送过，请勿重复提交」）。
- `channels` 传 `["in_app"]` 时跳过微信模板校验（不会返回 `97003`），仅写应用内消息 + WebSocket 推送。
- **发送接口不再返回「数据访问失败」500**：落库/审计/幂等写入失败返回 `99001 SYSTEM_ERROR`（HTTP 500），业务失败（过期/重复）返回 `96033` / `10002`（2026-09-07 实测校准，原 97003/97004/97005/97017 为文档规划值、后端未实现）。前端判等建议用数字码 `96033` / `10002`，重复提交也可按 message 关键字「已发送过」兜底。
- `expiresAt` 格式为带时区偏移的 ISO-8601（如 `2026-08-13T14:40:00+08:00`），前端可直接用 `new Date(expiresAt)` 解析，无需手动加偏移。

**D2. 97004 PREVIEW_EXPIRED 排查指引（已修复，备查）**
- **根因**：预览方法 `@Transactional(readOnly = true)` 导致 Hibernate FlushMode=MANUAL，`savePreview` 的 INSERT 不会被 flush，预览返回的 token 在 `arrears_reminder_preview` 表中不存在，发送时 `consumePreview` 查不到 → 抛 97004。**已修复**（改为 `@Transactional`）。
- **幂等落库时机**：`markProcessed(requestId)` 仅在发送全部完成后调用，且在同一个事务内。若发送过程中异常导致事务回滚，`requestId` 不会被标记已处理，前端可用同一 `requestId` + 新 `previewToken` 重试。
- **consumePreview 行为**：token 命中后立即 `delete`（同事务内），若后续发送逻辑异常事务回滚，delete 被撤销、token 仍有效；若发送成功事务提交，token 被消费、不可重用。
- **后端日志关键字**：`savePreview 快照已写入数据库` / `consumePreview token=xxx 命中并消费` / `consumePreview token=xxx 在表中不存在` / `consumePreview token=xxx 已过期`。

**D3. 发送接口 500 / 97017 ARREARS_REMINDER_PERSIST_FAILED 排查指引（v6.6 修复，v7.0 改码）**
- **典型现象**：`POST /arrears-reminder/preview` 成功（锁定范围、返回有效 `previewToken`），确认后 `POST /arrears-reminder` 返回 500 + 「数据访问失败」（旧行为），或返回 `97017`（v7.0 起；v6.6~v6.9 为 `97011`）。预览成功≠发送必成功，问题在发送落库/审计环节。
- **根因（已修复）**：审计记录 `arrears_reminder_audit` 主键由 `recordAudit` 从 `buildAuditEntry` 的 `id` 字段读取，旧代码 `buildAuditEntry` 未写入 `id` 且统计字段键名不一致（`inAppFail`/`wechatFail` vs `inAppFailed`/`wechatFailed`），导致 `audit.setId(null)` → 主键为 null 的 INSERT 触发 `DataAccessException` → 被全局异常处理器兜底成「数据访问失败」。现为 `buildAuditEntry` 生成 `audit` 主键、`recordAudit` 增加 null 兜底，并修正统计字段键名。
- **WebSocket 推送隔离**：`webSocketService.sendNotification` 原先在消息保存 try-catch 之外，其异常会直接透传为 500；现已在内部兜底，推送失败不影响落库结果。
- **落库异常转明确错误码**：`markProcessed`（幂等表 `arrears_reminder_request`）与 `recordAudit`（审计表 `arrears_reminder_audit`）的 `DataAccessException` 统一转为 `97017 ARREARS_REMINDER_PERSIST_FAILED`，不再返回「数据访问失败」。
- **`channels: ["in_app"]` 单渠道**：跳过微信模板校验与微信支付链路，仅写应用内消息（`messages` 表，`relatedTaskId=arrears_reminder`）+ WebSocket，应返回 `code=0` 及 `notifiedCount` 统计；住户端物业聊天可见催缴消息。若仍失败，优先查以下后端日志关键字：`consumePreview`、`savePreview`、`催缴发送`、`物业聊天 insert`、以及 `markProcessed`/`recordAudit` 的异常栈。
- **排查顺序**：① 是否 `97017`（落库失败）→ 查 `arrears_reminder_audit` / `arrears_reminder_request` 表结构、约束、字段长度；② 是否 `97004`（token 过期）→ 重新预览（注意旧 500 是误报，非真过期）；③ 是否误入微信/支付链路 → 确认 `channels` 仅含 `in_app` 时不会调用微信模板/支付。

**E. 消息落地路径**
- 住户点击微信催缴消息进入小程序，模板须配置跳转路径（如 `pages/fee/arrears`）。前端确保该页面存在并展示欠费明细。

---

## 16. 服务订单模块 (Service Orders)

> **与商品订单的区别**：本模块仅提供**查询**与**状态更新**（列表/详情/改状态），**没有** `POST /service-orders/{id}/pay` 支付接口。社区服务预约/订阅的扣款在 §17 的 `POST /services/{id}/book` 或 `POST /services/{id}/subscribe` 中**一步完成**；`book`/`subscribe` 成功后才会生成服务订单记录（`so_` 前缀），供 §16 查询。
>
> **技工端**：技工角色请优先使用 §72 `/technicians/my/tasks*`（工单态 `assigned` ↔ 本模块 `confirmed`）。



### 16.1 GET `/service-orders` — 服务订单列表

**Auth**: Required  
**Roles**: `resident`（本人）, `property_admin`, `platform_admin`

**Query 参数**：`page`, `pageSize`, `orderStatus`, `asProvider`, `sort`

---



### 16.2 GET `/service-orders/{id}` — 服务订单详情

**Auth**: Required  
**Roles**: `resident`（本人）, `property_admin`, `platform_admin`

**路径参数**：


| 参数   | 类型       | 必填  | 说明     |
| ---- | -------- | --- | ------ |
| `id` | `String` | ✅   | 服务订单ID |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "so_demo001",
    "serviceId": "svc_demo001",
    "serviceName": "家电维修",
    "serviceCategory": "cleaning_repair",
    "residentId": "res_b7m9x1",
    "residentName": "张先生",
    "communityId": "com_demo001",
    "communityName": "演示小区",
    "address": "3栋5单元502",
    "contactPhone": "138****1234",
    "serviceTime": "2026-07-05 10:00:00",
    "price": 50.00,
    "memberPrice": 40.00,
    "paymentMethod": "wechat",
    "paidAmount": 40.00,
    "orderStatus": "pending",
    "providerId": null,
    "providerName": null,
    "remark": "空调不制冷",
    "createdAt": "2026-07-01 15:23:33",
    "updatedAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 16.3 PATCH `/service-orders/{id}/status` — 更新服务订单状态

**Auth**: Required  
**Roles**: `resident`（本人）, `property_admin`, `platform_admin`, `technician`

**路径参数**：


| 参数   | 类型       | 必填  | 说明     |
| ---- | -------- | --- | ------ |
| `id` | `String` | ✅   | 服务订单ID |


**请求示例**：

```bash
curl -X PATCH https://test.yuanxusoftware.top/api/v1/service-orders/so_demo001/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "orderStatus": "in_progress",
    "providerId": "res_tech001",
    "remark": "已安排技工上门"
  }'
```

**请求体**：


| 字段            | 类型       | 必填  | 校验               | 说明                                                   |
| ------------- | -------- | --- | ---------------- | ---------------------------------------------------- |
| `orderStatus` | `String` | ✅   | `@NotBlank`      | 目标状态：`pending`/`in_progress`/`completed`/`cancelled` |
| `providerId`  | `String` | ❌   |                  | 技工ID（指派时必填）                                          |
| `remark`      | `String` | ❌   | `@Size(max=200)` | 备注                                                   |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "so_demo001",
    "orderStatus": "in_progress",
    "providerId": "res_tech001",
    "providerName": "李技工",
    "updatedAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



## 17. 社区服务模块 (Community Services)

### 预约/订阅与支付（一步式，勿套用商品订单）

| 场景 | 正确接口 | 支付时机 | 常见误用 |
| --- | --- | --- | --- |
| 商品下单 | `POST /orders` → `POST /orders/{id}/pay` | **两段式**：先下单再支付 | — |
| 服务预约（非订阅类） | `POST /services/{id}/book` | **一步式**：请求体带 `paymentMethod`，后端当场扣款并建单 | ❌ `book` 后再调 `POST /service-orders/{id}/pay`（**不存在**） |
| 服务订阅（`isSubscription=true`） | `POST /services/{id}/subscribe` | **一步式**：请求体带 `subType` + `paymentMethod` | ❌ 对订阅类服务调 `book`（返回「请使用 subscribe」） |

> 与活动组订阅（§18.6）、活动组加入（§18.7）相同模式：**弹窗收集参数 → 一次调用文档接口 → 后端扣款**。



### 17.1 GET `/services/my-subscriptions` — 我的订阅列表

**Auth**: Required  
**Roles**: `resident`

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/services/my-subscriptions?page=1&pageSize=20&status=active" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：


| 参数         | 类型       | 必填  | 默认值  | 说明                             |
| ---------- | -------- | --- | ---- | ------------------------------ |
| `page`     | `int`    | ❌   | `1`  | 页码                             |
| `pageSize` | `int`    | ❌   | `20` | 每页条数                           |
| `status`   | `String` | ❌   |      | `active`/`expired`/`cancelled` |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "sub_demo001",
        "serviceId": "svc_demo001",
        "serviceName": "家电维修",
        "serviceCategory": "cleaning_repair",
        "subType": "monthly",
        "price": 40.00,
        "paymentMethod": "wechat",
        "autoRenew": true,
        "status": "active",
        "subscribedAt": "2026-06-15 10:00:00",
        "expiresAt": "2026-07-15 10:00:00"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 1,
      "totalPages": 1
    }
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 17.2 GET `/services` — 服务列表

**Auth**: Optional  
**Roles**: `any`

**Query 参数**：


| 参数            | 类型        | 必填  | 默认值          | 说明                                       |
| ------------- | --------- | --- | ------------ | ---------------------------------------- |
| `page`        | `int`     | ❌   | `1`          | 页码                                       |
| `pageSize`    | `int`     | ❌   | `20`         | 每页条数                                     |
| `category`    | `String`  | ❌   |              | 分类                                       |
| `providerType`| `String`  | ❌   |              | 提供者类型：`merchant`/`resident`/`individual_leader`/`technician` |
| `mine`        | `Boolean` | ❌   |              | 是否只看我的                                   |
| `sort`        | `String`  | ❌   | `+rankOrder` | 排序                                       |


---



### 17.3 POST `/services` — 创建服务

**Auth**: Required  
**Roles**: `merchant`, `individual_leader`, `resident`, `technician`

> **说明**：技工（`technician`）首次调用时自动绑定 `merchant_source=technician` 的商家店铺，`providerType` 固定为 `technician`；服务订单完成后，收入计入该商家钱包并可提现。

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/services \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "name": "家电维修",
    "category": "cleaning_repair",
    "price": 50.00,
    "memberPrice": 40.00,
    "priceUnit": "次"
  }'
```

**请求体**：


| 字段               | 类型             | 必填  | 校验                    | 说明                                                                                                         |
| ---------------- | -------------- | --- | --------------------- | ---------------------------------------------------------------------------------------------------------- |
| `name`           | `String`       | ✅   | `@NotBlank`           | 服务名称                                                                                                       |
| `category`       | `String`       | ✅   | `@NotBlank`           | 服务分类：`cleaning_repair`/`canteen`/`tutoring`/`elderly_care`/`medical`/`pet_medical`/`life_service`/`dating` |
| `description`    | `String`       | ❌   |                       | 服务描述                                                                                                       |
| `coverUrls`      | `List<String>` | ❌   |                       | 封面图URL列表                                                                                                   |
| `price`          | `BigDecimal`   | ❌   | `@DecimalMin("0.00")` | 价格                                                                                                         |
| `memberPrice`    | `BigDecimal`   | ❌   | `@DecimalMin("0.00")` | 会员价                                                                                                        |
| `priceUnit`      | `String`       | ❌   |                       | 价格单位（如 `次`、`月`、`年`）                                                                                        |
| `isSubscription` | `Boolean`      | ❌   |                       | 是否为订阅制服务                                                                                                   |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "svc_demo002",
    "name": "家电维修",
    "category": "cleaning_repair",
    "price": 50.00,
    "memberPrice": 40.00,
    "priceUnit": "次",
    "providerType": "technician",
    "status": "active",
    "createdAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 17.4 POST `/services/{id}/subscribe` — 订阅服务

**Auth**: Required  
**Roles**: `resident`

**请求体**：

| 字段 | 类型 | 必填 | 校验 | 说明 |
| --- | --- | --- | --- | --- |
| `subType` | `String` | ✅ | `@NotBlank` | 订阅类型：`monthly`/`quarterly`/`yearly`，**小写** |
| `paymentMethod` | `String` | ✅ | `@NotBlank` | 支付方式：`property_coin`/`point`/`wechat`/`mixed`（**勿用** `coin`） |
| `pointUsed` | `Integer` | ❌ | — | `mixed` 时使用的积分数 |
| `coinUsed` | `Decimal` | ❌ | — | `mixed` 时使用的物业币数 |
| `autoRenew` | `Boolean` | ❌ | — | 是否自动续订，默认 `false` |
| `openid` | `String` | ❌ | — | 用户openid（`wechat` 支付需要；不传则取用户绑定openid） |

**业务说明**：

- 本接口**一步完成订阅 + 支付**：`property_coin`/`point` 余额充足直接生效（`status=active`）；含现金部分（`wechat`，或 `mixed` 有余款）走微信预下单。
- 含现金部分：先落 `pending_payment` 订阅（**不生效**），成功响应返回 `paymentRecordId` 与 `payParams`，由前端调起 `wx.requestPayment`；**微信回调确认后才置 `active`**。前端勿把 `pending_payment` 当失败，轮询 `GET /services/my-subscriptions` 直到 `active`。
- `mixed` 的积分/物业币部分**当场抵扣**；若现金部分超时未付（默认 30 分钟，`PendingPaymentScheduler` 兜底）订阅置 `cancelled`，已抵扣的**积分/物业币自动退回**，可重新订阅。
- 未收到款项绝不生效：微信支付未配置时调用本接口直接返回 `10002`，不会回退为「直接生效」。

**成功响应**（`wechat` 场景）：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "ss_m8k2n4",
    "serviceId": "svc_demo001",
    "serviceName": "家政保洁",
    "residentId": "res_b7m9x1",
    "subType": "monthly",
    "amount": 200.00,
    "startDate": "2026-01-01T00:00:00.000Z",
    "endDate": "2026-02-01T00:00:00.000Z",
    "autoRenew": true,
    "status": "pending_payment",
    "paymentRecordId": "pr_xxx",
    "payParams": {
      "appId": "wx1234567890abcdef",
      "timeStamp": "1414561699",
      "nonceStr": "abc123",
      "package": "prepay_id=wx123",
      "signType": "MD5",
      "paySign": "xxxx",
      "outTradeNo": "8..."
    }
  }
}
```

> `payParams` 字段原样传给 `wx.requestPayment(...)`；`status=pending_payment` 时订阅尚未生效，`active` 由微信回调/兜底确认后落定。

**错误响应**：

| 错误码 | HTTP | 说明 |
| --- | --- | --- |
| `10002` | 400 | 参数无效（`paymentMethod` 错误、`subType` 无效、混合支付金额不足等） |
| `10002` | 400 | 在线支付未开通（微信支付未配置），请改用物业币或积分 |
| `10001` | 400 | 含现金部分但未绑定 openid，无法调起支付 |
| `70003` | 409 | 订阅支付处理中，请勿重复提交（同一服务已有待支付订阅） |
| `96003` | 409 | 已订阅该服务 |
| `96001` | 404 | 服务不存在 |
| `17001` | 400 | 服务已下架 |


---



### 17.4.1 POST `/services/{id}/unsubscribe` — 取消订阅

**Auth**: Required  
**Roles**: `resident`

**路径参数**：


| 参数   | 类型       | 必填  | 说明   |
| ---- | -------- | --- | ---- |
| `id` | `String` | ✅   | 服务ID |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "ss_demo001",
    "serviceId": "svc_demo001",
    "serviceName": "家电维修",
    "residentId": "res_b7m9x1",
    "subType": "monthly",
    "amount": 40.00,
    "startDate": "2026-06-15",
    "endDate": "2026-07-15",
    "autoRenew": false,
    "status": "cancelled",
    "createdAt": "2026-06-15T10:00:00Z"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**错误响应**：


| 错误码     | HTTP | 说明      |
| ------- | ---- | ------- |
| `30001` | 404  | 服务不存在   |
| `40001` | 400  | 未找到有效订阅 |


---



### 17.5 POST `/services/{id}/book` — 预约服务

**Auth**: Required  
**Roles**: `resident`

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/services/svc_demo001/book \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "serviceTime": "2026-07-05 10:00:00",
    "address": "3栋5单元502",
    "contactPhone": "13812345678",
    "paymentMethod": "wechat"
  }'
```

**请求体**：


| 字段              | 类型       | 必填  | 校验          | 说明                                            |
| --------------- | -------- | --- | ----------- | --------------------------------------------- |
| `address`       | `String` | ❌   | `@Size(max=200)` | 服务地址（不传自动带出：楼栋+单元+房号） |
| `contactPhone`  | `String` | ❌   | `@Size(max=20)` | 联系电话（不传自动带出：注册手机号） |
| `paymentMethod` | `String` | ✅   | `@NotBlank` | 支付方式：`wechat`/`point`/`property_coin`/`mixed`（**勿使用** `coin`） |
| `pointUsed`     | `Integer` | ❌   |             | `mixed` 时使用的积分数                              |
| `coinUsed`      | `Decimal` | ❌   |             | `mixed` 时使用的物业币数                             |
| `serviceTime`   | `String` | ❌   |             | 期望服务时间 `yyyy-MM-dd HH:mm:ss`                  |
| `remark`        | `String` | ❌   |             | 备注                                            |
| `openid`        | `String` | ❌   |             | 用户openid（`wechat`/`mixed` 含现金部分时使用；不传则取用户绑定openid） |


**业务说明**：

- 本接口**同时完成预约与支付**，成功后返回服务订单（`id` 为 `so_` 前缀）；**无需**也**不能**再调用 `POST /service-orders/{id}/pay`（该接口不存在）。
- `address` / `contactPhone` **未传时自动带出**：地址默认拼接用户登记的 `楼栋+单元+房号`，联系电话默认用户注册手机号（仍可传入覆盖）。
- `property_coin` / `point`：余额充足时扣款成功，`orderStatus` 一般为 `confirmed`。
- 含现金部分（`wechat`，或 `mixed` 有余款）：后端创建订单后 `orderStatus` 为 `pending`（待微信支付），成功响应含 `paymentRecordId` 与 `payParams`（前端 `wx.requestPayment`）；**微信回调确认后**才置 `confirmed`。属预期，**勿当作接口失败**。
- `mixed`：须传 `pointUsed`、`coinUsed`，且积分折算 + 物业币 + 现金部分须覆盖应付金额，否则返回 `10002`（混合支付金额不足）。积分/物业币部分**当场抵扣**；若现金部分超时未付（默认 30 分钟，`PendingPaymentScheduler` 兜底）订单置 `cancelled`，已抵扣的**积分/物业币自动退回**，可重新预约。
- 未收到款项绝不确认：微信支付未配置时，含现金部分的请求直接返回 `10002`，不会回退为「直接确认」。
- 仅适用于**非订阅类**服务（`isSubscription=false`）；订阅类请用 §17.4 `subscribe`。

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "so_demo001",
    "serviceId": "svc_demo001",
    "serviceName": "家电维修",
    "orderStatus": "pending",
    "amount": 50.00,
    "paymentMethod": "wechat",
    "serviceTime": "2026-07-05 10:00:00",
    "address": "3栋5单元502",
    "contactPhone": "13812345678",
    "paymentRecordId": "pr_xxx",
    "payParams": { "appId": "wx...", "timeStamp": "...", "nonceStr": "...", "package": "prepay_id=...", "signType": "MD5", "paySign": "...", "outTradeNo": "..." },
    "createdAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**错误响应**：


| 错误码     | HTTP | 说明    |
| ------- | ---- | ----- |
| `10002` | 400  | 参数无效（如 `paymentMethod` 错误、混合支付金额不足、订阅类服务误调 `book`） |
| `10002` | 400  | 在线支付未开通（微信支付未配置），请改用物业币或积分 |
| `10001` | 400  | 含现金部分但未绑定 openid，无法调起支付 |
| `70003` | 409  | 预约支付处理中，请勿重复提交（同一服务已有待支付订单） |
| `40001` | 400  | 积分余额不足（`paymentMethod=point` 或 `mixed` 积分部分） |
| `50001` | 400  | 物业币余额不足（`paymentMethod=property_coin` 或 `mixed` 物业币部分） |
| `30001` | 404  | 服务不存在 |
| `17001` | 400  | 服务已下架 |


**常见联调错误**：


| 现象 | 原因 | 正确写法 |
| --- | --- | --- |
| 四种支付方式均失败 | 前端套用商品流程：`book` 后再调 `/service-orders/{id}/pay` | 仅在弹窗内一次 `POST /services/{id}/book`，请求体含 `paymentMethod` |
| `paymentMethod值无效` | 使用了 `coin` | 使用 `property_coin` |
| 微信「支付失败」 | `orderStatus=pending` 被前端当成错误 | 成功响应中 `pending` 表示待微信支付，非接口失败 |
| `订阅类服务请使用 subscribe` | 对 `isSubscription=true` 的服务调了 `book` | 改调 `POST /services/{id}/subscribe` |


---



### 17.6 PUT `/services/{id}` — 更新服务

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**路径参数**：


| 参数   | 类型       | 必填  | 说明   |
| ---- | -------- | --- | ---- |
| `id` | `String` | ✅   | 服务ID |


**请求示例**：

```bash
curl -X PUT https://test.yuanxusoftware.top/api/v1/services/svc_demo001 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "name": "家电维修（更新）",
    "price": 60.00,
    "memberPrice": 45.00,
    "description": "专业家电维修服务"
  }'
```

**请求体**：


| 字段            | 类型           | 必填  | 校验                    | 说明                  |
| ------------- | ------------ | --- | --------------------- | ------------------- |
| `name`        | `String`     | ❌   | `@Size(max=100)`      | 服务名称                |
| `description` | `String`     | ❌   |                       | 描述                  |
| `coverUrl`    | `String`     | ❌   | `@Size(max=500)`      | 封面图                 |
| `price`       | `BigDecimal` | ❌   | `@DecimalMin("0.01")` | 价格                  |
| `memberPrice` | `BigDecimal` | ❌   | `@DecimalMin("0.01")` | 业主折扣价               |
| `priceUnit`   | `String`     | ❌   | `@Size(max=20)`       | 价格单位                |
| `category`    | `String`     | ❌   |                       | 分类                  |
| `status`      | `String`     | ❌   |                       | `active`/`inactive` |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "svc_demo001",
    "name": "家电维修（更新）",
    "price": 60.00,
    "memberPrice": 45.00,
    "description": "专业家电维修服务",
    "updatedAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 17.7 GET `/services/{id}` — 服务详情

**Auth**: Optional  
**Roles**: `any`

**路径参数**：


| 参数   | 类型       | 必填  | 说明   |
| ---- | -------- | --- | ---- |
| `id` | `String` | ✅   | 服务ID |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "svc_demo001",
    "name": "家电维修",
    "description": "专业家电维修服务，涵盖空调、冰箱、洗衣机等",
    "coverUrl": "https://cdn.example.com/service/svc_demo001.jpg",
    "category": "cleaning_repair",
    "categoryName": "清洁维修",
    "price": 50.00,
    "memberPrice": 40.00,
    "priceUnit": "次",
    "subscriptionRequired": false,
    "monthlyFee": null,
    "yearlyFee": null,
    "status": "active",
    "createdAt": "2026-06-01 10:00:00"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 17.8 DELETE `/services/{id}` — 删除服务

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**路径参数**：


| 参数   | 类型       | 必填  | 说明   |
| ---- | -------- | --- | ---- |
| `id` | `String` | ✅   | 服务ID |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "svc_demo001",
    "status": "inactive",
    "deletedAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



## 18. 活动组模块 (Activity Groups)



### 活动组 ID 格式

路径参数 `{id}` 支持以下前缀：


| 前缀     | 说明             | 示例             |
| ------ | -------------- | -------------- |
| `ag_`  | 种子数据/早期活动组     | `ag_admin002`  |
| `act_` | 用户新建活动组（雪花 ID） | `act_74601000` |




### 加入与订阅流程

```
免费活动组（monthlyFee=0 且 yearlyFee=0）
  └─ POST /activity-groups/{id}/join  → 直接加入

付费活动组（monthlyFee>0 或 yearlyFee>0）
  ├─ 推荐：POST /activity-groups/{id}/subscribe  → 订阅并自动加入
  └─ 或：先 subscribe，再 POST /join（须有效期内已订阅）

退出后再次加入
  └─ POST /leave 为软删除（status=inactive）
  └─ 再次 join/subscribe 时复用原成员记录并重新激活
```

> **注意**：订阅接口 `subType` 使用小写 `monthly`/`yearly`；`paymentMethod` 必填，且须为 `property_coin`/`point`/`wechat`（不可用 `coin`）。



### 活动组组长（activity_leader）能力总览

> **🆕 v8.14 组长标签化**：活动组组长从「与业主住户互斥的独立角色」改为「住户子身份（标签）」——`residents.role` 恒为 `resident`，「是否组长」由 `activity_leader_identities` 表 active 记录判定（与单元楼长、统筹/板块/个体负责人同模式）。组长账号保留全部住户功能（社区论坛、家庭、业主小店申请等），且可与楼长/业主小店/一级代理等身份叠加。存量 `role=activity_leader` 账号已由 `sql/manual_upgrade_shop_profile_leader_tag_forum_ban_20261021.sql` 迁移（建身份记录 + role 归一化）。

**组长如何产生**：

| 方式 | 说明 |
| --- | --- |
| 住户自助申请（🆕 v8.14） | 住户调用 §18.4.1 `POST /activity-leader-applications/apply` 提交申请，管理端 §101.1 审核通过后自动获得组长身份并开通组长小店 |
| 管理端创建账号 | 管理端 §101 角色账号创建 `POST /admin/role-accounts`（`role=activity_leader`）时建 `resident` 账号 + 组长身份 + 组长小店 |
| ~~管理端任命~~ | ~~`PUT /residents/{id}` 改 `role=activity_leader`~~ **v8.14 起不再支持**（`activity_leader` 已移出 role 白名单，与楼长/负责人一致） |
| 创建活动组 | 任意 `resident` 调用 §18.4 `POST /activity-groups` 创建活动组后即成为该组组长（`group.leaderId`=创建者、成员 `role=leader`）；但**账号级组长身份**仍需申请/任命获得（`my-led`、小店等完整能力以身份为准） |

**组长小店开通（免审核）**：

- 触发时机：组长身份创建时（申请审核通过 / 管理端建号）**立即**创建（幂等：已存在则跳过）；历史兜底：组长首次访问商品 / 提现 / 我的商家接口时也会自动创建
- 店铺参数：
  - `merchantSource = group_leader`（组长小店）
  - 店铺名 = `{姓名或手机号}的小店`；分类 `category = 活动组`
  - `auditStatus = approved`（**免审核**）、`status = active`、`deliveryFee = 0`、`serveAllCommunities = true`
  - 默认抽佣 `commissionRate = 0.1000`、积分兑换 `pointExchangeRate = 100`
- 收入结算：组长店商品走 §6 `/products` 主商城，订单完成收入进入组长店钱包，可走 §23 商家提现链路提现

**组长管理活动组接口清单**：

| 能力 | 接口 | 说明 |
| --- | --- | --- |
| 我管理的组列表 | §18.2.1 `GET /activity-groups/my-led` | 仅返回本人担任组长的活动组（🆕 v8.14 补齐落地） |
| 创建活动组 | §18.4 `POST /activity-groups` | 创建者即该组组长 |
| 更新活动组 | §18.10 `PUT /activity-groups/{id}` | 仅本人担任组长的组（否则 `20004`） |
| 成员列表 | §18.5 `GET /activity-groups/{id}/members` | 仅本人担任组长的组（否则 `20004`） |
| 组内楼长列表（只读） | §18.5.1 `GET /activity-groups/{id}/building-leaders` | 🆕 v8.35 只读；**组长无权设置或撤销楼长** |
| 移除成员 | §18.9 `DELETE /activity-groups/{id}/members/{memberId}` | 仅组长；不可移除组长本人（`90011`） |
| 解散活动组 | §18.9.1 `DELETE /activity-groups/{id}` | 仅组长或物业/平台管理员；硬删除，不可恢复 |
| 更新组员简介 | §68.1 `PUT /activity-groups/{id}/members/{memberId}/profile` | 本人或组长可改 |
| 课时价格管理 | §73 `POST/PUT/DELETE /activity-groups/{id}/pricing-tiers` | 组长可维护课时价格 |
| 发布活动公告 | §66.2 `POST /announcements/activity` | 组长可发布活动内容公告 |
| 管理小店商品 | §6.3/§6.4/§6.5 `POST/PUT/DELETE /products` | 仅自己的组长店（手机号+物业+来源三重匹配） |
| 查看/维护小店 | §7.2 `GET /merchants/my`、§7.5 `PUT /merchants/{id}` | 组长店 `merchantSource=group_leader` |
| 商家提现 | §23 `POST/GET /merchant/withdrawals` | 自动定位到自己的组长店 |

**权限边界**：

- 组长仅能查看/管理**本人担任组长**（`leaderId`=本人）的活动组，访问其他活动组返回 `20004`
- **楼长（单元楼长）不属于组长权限范围**：楼长是「小区+单元」维度的物业侧身份（带分成比例），指定入口只有两个——管理端 §99.1.2 `POST /admin/building-leaders`（`property_admin`/`platform_admin`）与在职物业经理 §100.2 `POST /property-managers/my/building-leaders`（限本物业公司）；**撤销只有管理端** §99.1.5 `DELETE /admin/building-leaders/{id}`，经理工作台没有撤销接口。活动组侧**没有**组长写接口，组长侧只有 §18.5.1 只读列表；单条会话对方是否楼长看 §52.7 `leaderRole`
- 组长不可退出自己组长的活动组（`90011` 组长不能退出活动组）；移除成员时也不可移除组长本人
- 组长店身份解析按 `resident_id + 物业 + merchantSource=group_leader` 三重匹配
- 「我的」页入口：`GET /auth/profile` 返回 `isActivityLeader`/`activityLeaderId`，为 `true` 进组长管理页，为 `false` 进申请页（§18.4.1）

### 18.1 GET `/activity-groups/my-subscriptions` — 我的订阅列表

**Auth**: Required  
**Roles**: `resident`, `activity_leader`, `coordinator`, `sector_leader`, `individual_leader`, `property_admin`, `platform_admin`

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/activity-groups/my-subscriptions?page=1&pageSize=20&status=active" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：


| 参数         | 类型       | 必填  | 默认值  | 说明                             |
| ---------- | -------- | --- | ---- | ------------------------------ |
| `page`     | `int`    | ❌   | `1`  | 页码                             |
| `pageSize` | `int`    | ❌   | `20` | 每页条数                           |
| `status`   | `String` | ❌   |      | `active`/`expired`/`cancelled` |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "subscriptionId": "ags_001",
        "groupId": "ag_demo001",
        "groupName": "广场舞队",
        "groupCover": "https://cdn.example.com/activity/ag_demo001.jpg",
        "subType": "monthly",
        "amount": 300.00,
        "startDate": "2026-06-15",
        "endDate": "2026-07-15",
        "daysRemaining": 11,
        "autoRenew": true,
        "memberId": "agm_001",
        "isMember": true,
        "status": "active",
        "paidAt": "2026-06-15T10:00:00",
        "pricingTierId": "agt_xxx",
        "tierName": "一级",
        "tierCode": "level_1"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 1,
      "totalPages": 1
    }
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**响应字段说明**：


| 字段               | 类型           | 说明                                  |
| ---------------- | ------------ | ----------------------------------- |
| `subscriptionId` | `String`     | 订阅记录ID                              |
| `groupId`        | `String`     | 活动组ID                               |
| `groupName`      | `String`     | 活动组名称                               |
| `groupCover`     | `String`     | 活动组封面                               |
| `subType`        | `String`     | 订阅类型：`monthly`/`yearly`             |
| `amount`         | `BigDecimal` | 订阅金额                                |
| `startDate`      | `String`     | 订阅开始日期                              |
| `endDate`        | `String`     | 订阅结束日期                              |
| `daysRemaining`  | `Integer`    | 剩余天数                                |
| `autoRenew`      | `Boolean`    | 是否自动续费                              |
| `memberId`       | `String`     | 成员记录ID（`agm_xxx`），可用于退出/移除操作        |
| `isMember`       | `Boolean`    | 当前是否已是活动组成员                         |
| `status`         | `String`     | 订阅状态：`active`/`expired`/`cancelled` |
| `paidAt`         | `String`     | 支付时间                                |
| `pricingTierId`  | `String`     | 课时价格ID，无课时为 `null`                  |
| `tierName`       | `String`     | 课时名称，无课时为 `null`                    |
| `tierCode`       | `String`     | 课时编码，无课时为 `null`                    |


---



### 18.2 GET `/activity-groups/my-joined` — 我加入的活动组列表

**Auth**: Required  
**Roles**: `resident`, `activity_leader`

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/activity-groups/my-joined?page=1&pageSize=20" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：


| 参数         | 类型    | 必填  | 默认值  | 说明   |
| ---------- | ----- | --- | ---- | ---- |
| `page`     | `int` | ❌   | `1`  | 页码   |
| `pageSize` | `int` | ❌   | `20` | 每页条数 |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "memberId": "agm_001",
        "groupId": "ag_demo001",
        "groupName": "广场舞队",
        "groupCover": "https://cdn.example.com/activity/ag_demo001.jpg",
        "role": "member",
        "memberCount": 25,
        "monthlyFee": 0.00,
        "yearlyFee": 0.00,
        "leaderId": "res_b7m9x1",
        "leaderName": "张先生",
        "status": "active",
        "hasActiveSubscription": true,
        "subscribedAmount": 20.00,
        "joinedAt": "2026-06-15T10:00:00"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 1,
      "totalPages": 1
    }
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**注意**：此接口返回当前用户作为成员（包括组长和普通成员）加入的所有活动组，包含免费加入的组和订阅后加入的组。如需区分订阅信息，请调用 `GET /activity-groups/my-subscriptions`。

**列表项字段（补充）**：


| 字段                      | 类型         | 说明                                   |
| ----------------------- | ---------- | ------------------------------------ |
| `hasActiveSubscription` | `Boolean`  | 当前用户对该组是否有未过期的有效订阅                    |
| `subscribedAmount`      | `Decimal`  | 有效订阅的实付金额（组长改价后仍为历史订阅金额，可与现价 `monthlyFee` 不同） |


> **展示建议**：`hasJoined=true` 时不应仅因 `monthlyFee>0` 显示「需订阅」；已加入成员优先展示「已加入」或「已订阅」。

> **组长说明**：`activity_leader` 若需「我管理的组」列表，**推荐**调用 `GET /activity-groups/my-led`（§18.2.1）；亦可用本接口返回项中的 `leaderId` 与当前用户 ID 比对筛选（依赖成员记录存在）。

---



### 18.2.1 GET `/activity-groups/my-led` — 我管理的活动组列表

**Auth**: Required  
**Roles**: `resident`（持组长身份）、`activity_leader`（存量 JWT）、`property_admin`、`platform_admin`

**Description**: 活动组组长专用（组长管理页）。返回当前登录用户作为 `leaderId` 的活动组列表，**不过滤 status/reviewStatus**（与 `GET /activity-groups?mine=true` 的组长分支一致），响应格式与 `GET /activity-groups` 列表项一致。

> **🆕 v8.14**：本接口补齐落地（此前文档已有但服务端缺失）；Roles 随组长标签化放宽——组长身份由 `activity_leader_identities` 表 active 记录判定（`role` 仍为 `resident`），不再要求 JWT role 为 `activity_leader`。

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/activity-groups/my-led?page=1&pageSize=20&sort=-createdAt" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：


| 参数         | 类型       | 必填  | 默认值          | 说明   |
| ---------- | -------- | --- | ------------ | ---- |
| `page`     | `int`    | ❌   | `1`          | 页码   |
| `pageSize` | `int`    | ❌   | `20`         | 每页条数 |
| `sort`     | `String` | ❌   | `-createdAt` | 排序   |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "ag_admin003",
        "name": "摄影爱好者",
        "description": "社区摄影交流群，定期组织外拍",
        "coverUrl": null,
        "leaderId": "res_act01",
        "leaderName": "活动组长-测试",
        "communityId": "com_demo001",
        "communityName": "演示小区",
        "memberCount": 42,
        "monthlyFee": 0.00,
        "yearlyFee": 0.00,
        "status": "active",
        "hasJoined": false,
        "myRole": null,
        "hasActiveSubscription": false,
        "subscribedAmount": null,
        "createdAt": "2026-07-03 08:02:56"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 1,
      "totalPages": 1
    }
  },
  "timestamp": "2026-07-10 17:00:00"
}
```

**错误响应**：


| 错误码     | HTTP | 说明    |
| ------- | ---- | ----- |
| `20004` | 403  | 权限不足（非 `activity_leader` 角色） |


---



### 18.3 GET `/activity-groups` — 活动组列表

**Auth**: Optional（未登录可浏览）；`mine=true` 或 `activity_leader` 角色调用时 **Auth: Required**  
**Roles**: `any`（含 `resident`、`activity_leader` 及各级管理角色）

**Query 参数**：


| 参数            | 类型        | 必填  | 默认值          | 说明     |
| ------------- | --------- | --- | ------------ | ------ |
| `page`        | `int`     | ❌   | `1`          | 页码     |
| `pageSize`    | `int`     | ❌   | `20`         | 每页条数   |
| `communityId` | `String`  | ❌   |              | 小区ID   |
| `mine`        | `Boolean` | ❌   |              | 是否只看我的（见下方语义说明） |
| `sort`        | `String`  | ❌   | `-createdAt` | 排序     |


**`mine` 参数语义**：


| 当前角色 | `mine=true` 筛选规则 |
| --- | --- |
| `activity_leader` | 按 `leaderId = 当前用户ID` 筛选（**我管理的活动组**） |
| 其他已登录角色 | 按活动组成员关系筛选（**我加入的活动组**） |
| 未登录 | 不可用（须携带 Token） |


> **组长推荐**：活动组组长获取管理列表请优先使用 `GET /activity-groups/my-led`（§18.2.1）。

**列表项公共字段（`GET /activity-groups` / `my-led` / `my-joined` 相关）**：


| 字段                      | 类型        | 说明                          |
| ----------------------- | --------- | --------------------------- |
| `hasActiveSubscription` | `Boolean` | 当前用户是否有未过期有效订阅（需登录才有意义）     |
| `subscribedAmount`      | `Decimal` | 有效订阅实付金额；无订阅时为 `null`        |


---



### 18.4 POST `/activity-groups` — 创建活动组

**Auth**: Required  
**Roles**: `resident`、`activity_leader`、`coordinator`、`sector_leader`、`individual_leader`、`property_admin`、`platform_admin`

> **组长说明**：创建者自动成为该组组长（`group.leaderId`=创建者、成员 `role=leader`）。创建活动组**本身不会**开通组长小店——组长小店在**组长申请审核通过**（§101.2）或管理端任命组长时自动开通（见本模块「活动组组长能力总览」）。组长账号（`role` 仍为 `resident`，持 `isActivityLeader=true` 标签）同样可调用本接口创建新组并成为其组长。

> **2026-09-07 前端问题 F（产品确认）**：创建活动组**即刻生效**，`status=active`、`reviewStatus=approved`，住户端创建后立即可见可用；管理端 `POST /admin/activity-groups/{id}/review` 仅为事后兜底（`approve` 无影响，`reject` 永久硬删该组）。

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/activity-groups \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "name": "广场舞队",
    "description": "社区广场舞活动",
    "monthlyFee": 0.00,
    "yearlyFee": 0.00
  }'
```

---



### 18.5 GET `/activity-groups/{id}/members` — 成员列表

**Auth**: Required  
**Roles**: `resident`, `activity_leader`, `coordinator`, `sector_leader`, `individual_leader`, `property_admin`, `platform_admin`

> **数据范围**：`activity_leader` 仅可查看本人担任组长（`leaderId` 为当前用户）的活动组成员；访问其他活动组返回 `20004`。

**路径参数**：


| 参数   | 类型       | 必填  | 说明                       |
| ---- | -------- | --- | ------------------------ |
| `id` | `String` | ✅   | 活动组ID（`ag_` 或 `act_` 前缀） |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "res_b7m9x1",
        "name": "张先生",
        "avatarUrl": "https://cdn.example.com/avatar/res_b7m9x1.jpg",
        "relation": "owner",
        "isLeader": false,
        "joinedAt": "2026-06-15 10:00:00",
        "status": "active"
      },
      {
        "id": "res_leader01",
        "name": "王组长",
        "avatarUrl": "https://cdn.example.com/avatar/res_leader01.jpg",
        "relation": "owner",
        "isLeader": true,
        "joinedAt": "2026-06-01 10:00:00",
        "status": "active"
      }
    ],
    "total": 2
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---

### 18.5.1 GET `/activity-groups/{id}/building-leaders` — 组内楼长列表（只读，🆕 v8.35）

**Auth**: Required
**Roles**: `resident`, `activity_leader`, `coordinator`, `sector_leader`, `individual_leader`, `property_admin`, `platform_admin`

**Description**: 返回该活动组**活跃成员**中、当前在本物业公司下**启用中**的单元楼长。用于聊天/成员页展示「组里谁是楼长」。

- 楼长身份来自 `building_leaders`（小区+单元+分成比例），与活动组无关；本接口只做**交集展示**，不新增身份、不影响分成。
- **没有**配套的 `POST`/`PUT`/`DELETE`：组长无设置/撤销权。要指定楼长走 §99.1.2（管理端）或 §100.2（在职物业经理，限本物业公司），撤销只有 §99.1.5（管理端）。
- 数据范围：按 `group.propertyCompanyId` 过滤，不按调用者所属物业公司；跨物业公司访问返回 `90007`。
- 不返回 `unit`/`commissionRate`/手机号等物业侧字段（组内成员不应看到彼此的单元门牌与分成）。

**路径参数**：`id` — 活动组ID（`ag_` 或 `act_` 前缀），格式错误返回 `10002`。

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "groupId": "ag_admin002",
    "groupName": "跑步俱乐部",
    "buildingLeaderCount": 1,
    "list": [
      {
        "memberId": "agm_746010001",
        "residentId": "res_b7m9x1",
        "name": "王楼长",
        "avatarUrl": "https://cdn.example.com/avatar/res_b7m9x1.jpg",
        "leaderRole": "building_leader"
      }
    ]
  },
  "timestamp": "2026-11-03 18:50:12"
}
```

> 组内没有楼长时 `list` 为 `[]`、`buildingLeaderCount` 为 `0`（不是 `null`）。
> 全局 `non_null` 序列化：`name`/`avatarUrl` 在住户记录缺失时**整个键不下发**，判空要用 `?.` 而不是 `!== null`。

**错误码**：`10002`（ID 格式）/ `90007`（活动组不存在或不属于本物业公司）/ `20001`（Token 缺失）。

> ⚠️ 本节错误码按 `ErrorCode.java` 实测。§18.5 正文标注的 `20004` 与实际不符（实为 `90007`），待下轮文档核对统一。

---

### 18.4.1 POST `/activity-leader-applications/apply` — 申请成为活动组组长（🆕 v8.14）

**Auth**: Required  
**Roles**: `resident`

**Description**: 住户自主申请成为活动组组长，由管理端（§101.1）审核。审核通过后：组长身份以**标签**形式叠加在住户账号上（`role` 仍为 `resident`），并**自动开通组长小店**（`merchant_source=group_leader`，免审核）。每位住户在同一物业公司下仅可有一个待审核申请；已是组长则拒绝（`97062`）。

**请求体**（`ApplyActivityLeaderRequest`）：

| 字段 | 类型 | 必填 | 校验 | 说明 |
| --- | --- | --- | --- | --- |
| `activityField` | String | ❌ | `@Size(max=50)` | 擅长领域/活动类型（如 文艺/体育/亲子/户外） |
| `remark` | String | ❌ | `@Size(max=200)` | 自我介绍/备注 |

**响应**：`applicationId`, `status`（`pending`）, `activityField`, `remark`, `message`

**请求示例**：
```json
{ "activityField": "亲子活动", "remark": "有 3 年社区活动组织经验" }
```

**成功响应**：
```json
{ "code": 0, "data": {
  "applicationId": "ala_xxx",
  "status": "pending",
  "activityField": "亲子活动",
  "remark": "有 3 年社区活动组织经验",
  "message": "申请已提交，等待物业审核"
} }
```

> **完整链路**（v8.15 已修复审核通过报错）：提交申请 → §101.1 管理端列表 → §101.2 审核。`approved` 后同事务创建 active 组长身份并自动开通组长小店；申请人 `role` 仍为 `resident`，`GET /auth/profile` 的 `isActivityLeader` 变为 `true`，即可进入组长管理页（§18.2.1）。驳回不创建身份/小店，C 端可在 §18.4.2 看到 `rejected` 与原因。

**错误响应**：

| 错误码 | HTTP | 说明 |
| --- | --- | --- |
| `97062` | 400 | 已有待审核申请 / 已是组长 |
| `PARAM_MISSING` | 400 | 未绑定物业公司 |

### 18.4.2 GET `/activity-leader-applications/my-application` — 查询我的组长申请状态（🆕 v8.14）

**Auth**: Required  
**Roles**: `resident`

**Description**: 查询本人最新的组长申请状态，供申请页展示审核进度。

**响应**：`applicationId`, `status`（`none`=未申请 / `pending`=审核中 / `approved`=已通过 / `rejected`=已驳回）, `activityField`, `remark`, `rejectReason`（驳回原因）, `message`

**响应示例**：
```json
{ "code": 0, "data": {
  "applicationId": "ala_xxx",
  "status": "approved",
  "activityField": "亲子活动",
  "remark": "有 3 年社区活动组织经验",
  "rejectReason": null,
  "message": "申请已通过"
} }
```

```json
{ "code": 0, "data": { "applicationId": null, "status": "none", "message": "暂无申请记录" } }
```

> **「我的」页路由**：`GET /auth/profile` 的 `isActivityLeader=true` → 进组长管理页（§18.2.1 `my-led` 等）；`false` → 进申请页（本接口查进度 + §18.4.1 提交）。

---

## 101 管理端 — 活动组组长申请审核（🆕 v8.14）

> 对应 C 端 §18.4.1/§18.4.2。审核通过时**同一事务**内创建组长身份（标签，`role` 仍为 `resident`）+ 自动开通组长小店（`merchant_source=group_leader`，免审核）。组长身份由 `activity_leader_identities` active 记录判定，与楼长/负责人同模式。

### §101.1 GET `/admin/activity-leader-applications` — 组长申请列表

**Roles**: `property_admin`、`platform_admin`

**查询参数**：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| `page` | `int` | ❌ | 1 | 页码 |
| `pageSize` | `int` | ❌ | 20 | 每页条数 |
| `auditStatus` | `String` | ❌ | — | 状态过滤：`pending`/`approved`/`rejected` |
| `propertyCompanyId` | `String` | ❌ | — | 仅 `platform_admin` 跨物业查询时传；**v8.15 起不传即返回全部物业**。`property_admin` 始终强制本物业 |

**响应**：分页 `ActivityLeaderApplicationListItemResponse`

`ActivityLeaderApplicationListItemResponse` 字段：`id`、`residentId`、`residentName`、`residentPhone`(脱敏)、`activityField`、`remark`、`auditStatus`、`rejectReason`、`activityLeaderIdentityId`(审核通过后创建的组长身份记录ID)、`auditedAt`(ISO8601)、`createdAt`(ISO8601)、`propertyCompanyId`

### §101.2 POST `/admin/activity-leader-applications/{applicationId}/audit` — 审核组长申请

**Roles**: `property_admin`、`platform_admin`

**路径参数**：`applicationId`（`ala_` 前缀，必填）

**请求体**：

| 字段 | 类型 | 必填 | 校验 | 说明 |
| --- | --- | --- | --- | --- |
| `auditStatus` | `String` | ✅ | `approved`/`rejected` | 审核结果 |
| `rejectReason` | `String` | 条件必填 | — | `rejected` 时必填 |

**业务说明**：

- `approved`：同一事务内创建组长身份（幂等复用已有 `active` 身份）+ 自动开通组长小店；申请置 `approved`，返回 `activityLeaderIdentityId`。
- `rejected`：记录 `rejectReason`，申请置 `rejected`。
- 申请不存在返回 `97060`；非 `pending` 申请重复审核返回 `97061`；住户账号停用返回 `ACCOUNT_DISABLED`。

**请求示例（通过）**：
```json
{ "auditStatus": "approved" }
```

**请求示例（驳回）**：
```json
{ "auditStatus": "rejected", "rejectReason": "当前活动组名额已满" }
```

**成功响应**：
```json
{ "code": 0, "data": {
  "applicationId": "ala_xxx",
  "auditStatus": "approved",
  "residentId": "res_xxx",
  "residentName": "李阿姨",
  "rejectReason": null,
  "auditedAt": "2026-10-22T01:05:11.000Z",
  "activityLeaderIdentityId": "ali_xxx"
} }
```

**审核通过自动开通的组长小店**：`merchants` 记录 `merchant_source=group_leader`、`merchant_type=group_leader`、`audit_status=approved`、`status=active`；同时创建 `merchant_property_configs` 配置。**v8.15 修复**：此前创建该小店时未赋值 `merchant_type` / `is_canteen_main_merchant`，触发 NOT NULL 约束导致审核 400；现已在实体层补齐默认值，前端无需改动，修复前失败的申请仍为 `pending`，可直接重试。

**成功响应**：`ActivityLeaderApplicationAuditResponse`

`ActivityLeaderApplicationAuditResponse` 字段：`applicationId`、`auditStatus`(`approved`/`rejected`)、`residentId`、`residentName`、`rejectReason`、`auditedAt`(ISO8601)、`activityLeaderIdentityId`(审核通过时创建的组长身份记录ID)

### §101.3 POST `/admin/activity-leaders/{residentId}/revoke` — 撤销组长身份

**Roles**: `property_admin`、`platform_admin`

**路径参数**：`residentId`（`res_` 前缀，必填）

**业务说明**：

- 将该住户 `active` 组长身份记录置 `revoked`，历史业务数据（活动组、小店、订单）保留。
- 无 `active` 组长身份返回 `97063`。

**成功响应**：`ActivityLeaderApplicationAuditResponse`（`auditStatus=revoked`）

> **管理端创建账号路径**：管理端 `POST /admin/role-accounts`（`role=activity_leader`）时建 `resident` 账号 + 组长身份 + 组长小店（与申请审核同款能力），见 §31.2。

---

### 18.6 POST `/activity-groups/{id}/subscribe` — 订阅活动组

**Auth**: Required  
**Roles**: `resident`, `activity_leader`, `coordinator`, `sector_leader`, `individual_leader`, `property_admin`, `platform_admin`

**路径参数**：


| 参数   | 类型       | 必填  | 说明                       |
| ---- | -------- | --- | ------------------------ |
| `id` | `String` | ✅   | 活动组ID（`ag_` 或 `act_` 前缀） |


**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/activity-groups/ag_admin002/subscribe \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "subType": "monthly",
    "paymentMethod": "property_coin",
    "autoRenew": false,
    "pricingTierId": "agt_xxx"
  }'
```

**请求体**：


| 字段              | 类型        | 必填  | 校验          | 说明                                        |
| --------------- | --------- | --- | ----------- | ----------------------------------------- |
| `subType`       | `String`  | ✅   | `@NotBlank` | 订阅类型：`monthly`/`quarterly`/`yearly`，**小写**；有 active 课时时须与课时 `period` 一致 |
| `paymentMethod` | `String`  | ✅   | `@NotBlank` | 支付方式：`property_coin` / `point` / `wechat` |
| `autoRenew`     | `Boolean` | ❌   |             | 是否自动续费，默认 `false`                         |
| `pricingTierId` | `String`  | 条件必填 |             | 课时价格ID（`agt_` 前缀）。活动组存在 active 课时时**必填**；无课时时可不传，走旧 `monthlyFee`/`yearlyFee` |
| `openid`        | `String`  | ❌   |             | 用户openid（`wechat` 支付需要；不传则取用户绑定openid） |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "subscriptionId": "ags_xxx",
    "groupId": "ag_admin002",
    "groupName": "围棋社",
    "subType": "monthly",
    "amount": 300.00,
    "startDate": "2026-07-07",
    "endDate": "2026-08-07",
    "autoRenew": false,
    "status": "active",
    "paidAt": "2026-07-07T05:20:00Z",
    "pricingTierId": "agt_xxx",
    "tierName": "一级",
    "tierCode": "level_1"
  },
  "timestamp": "2026-07-07 13:20:00"
}
```

**响应字段说明**：


| 字段               | 类型           | 说明                   |
| ---------------- | ------------ | -------------------- |
| `subscriptionId` | `String`     | 订阅记录ID（`ags_` 前缀）    |
| `groupId`        | `String`     | 活动组ID                |
| `groupName`      | `String`     | 活动组名称                |
| `subType`        | `String`     | `monthly` / `quarterly` / `yearly` |
| `amount`         | `BigDecimal` | 实付订阅金额（有课时取课时价，无课时取活动组月费/年费）    |
| `startDate`      | `String`     | 订阅开始日期 `yyyy-MM-dd`  |
| `endDate`        | `String`     | 订阅结束日期 `yyyy-MM-dd`  |
| `autoRenew`      | `Boolean`    | 是否自动续费               |
| `status`         | `String`     | 订阅状态：`pending_payment`(待支付) / `active`(生效) / `expired` / `cancelled`    |
| `paidAt`         | `String`     | 支付时间（ISO-8601 UTC）   |
| `pricingTierId`  | `String`     | 课时价格ID，无课时为 `null`   |
| `tierName`       | `String`     | 课时名称，无课时为 `null`     |
| `tierCode`       | `String`     | 课时编码，无课时为 `null`     |
| `paymentRecordId` | `String`    | 支付流水ID（`pr_`，`wechat` 场景返回） |
| `payParams`      | `Object`     | 微信小程序调起支付参数（`wechat` 场景返回，`wx.requestPayment`） |


**业务说明**：

- 订阅成功后会**自动加入**活动组（若当前不是活跃成员）
- `property_coin` / `point`：同步扣款并**立即生效**（`status=active` 且自动入组）
- `wechat`：先落库 `status=pending_payment`（**不生效、不入组、不占用**），成功响应返回 `paymentRecordId` 与 `payParams`（`wx.requestPayment`）由前端调起支付；**微信回调确认后才置 `active` 并自动入组**。前端勿把 `pending_payment` 当失败，轮询 `GET /activity-groups/{id}/subscription-status` 直到 `active`
- 未收到款项绝不生效：微信支付未配置（`wechatPayService.isEnabled()=false`）时调用本接口直接返回 `10002`，不会回退为「直接生效」
- 超时未支付（默认 30 分钟，兜底 `PendingPaymentScheduler`）订阅自动置 `cancelled`，可重新订阅
- 有 active 课时价格的组，必须传 `pricingTierId`，金额取该课时 `price`，`subType` 须与课时 `period` 一致
- 无课时价格的组，仍可走旧 `monthlyFee`/`yearlyFee`；月费/年费均为 0 时调用本接口将返回 `10002`：`该活动组无需订阅`，请直接调用 `join`
- 是否付费组判定：有 active 课时 或 `monthlyFee`/`yearlyFee` > 0

**错误响应**：


| 错误码     | HTTP | 说明                                        |
| ------- | ---- | ----------------------------------------- |
| `10002` | 400  | 参数校验失败（如 `paymentMethod` 为空、`subType` 无效或与课时周期不一致） |
| `10002` | 400  | 该活动组存在 active 课时但未传 `pricingTierId`（请选择课时） |
| `10002` | 400  | 该活动组无需订阅（免费组）                             |
| `40001` | 400  | 积分余额不足（`paymentMethod=point`）             |
| `50001` | 400  | 物业币余额不足（`paymentMethod=property_coin`）    |
| `90007` | 404  | 活动组/课时不存在（`pricingTierId` 不属于该组或已 inactive） |
| `90008` | 409  | 已订阅该活动组（有效期内重复订阅）                         |
| `70003` | 409  | 订阅支付处理中，请勿重复提交（同一住户同一活动组已有待支付订阅） |
| `10001` | 400  | `paymentMethod=wechat` 且未绑定 openid，无法调起支付 |
| `10002` | 400  | 在线支付未开通（微信支付未配置），请改用物业币或积分 |


**常见联调错误**：


| 现象                 | 原因                        | 正确写法                                     |
| ------------------ | ------------------------- | ---------------------------------------- |
| `参数校验失败：支付方式不能为空`  | 未传 `paymentMethod` 或字段名错误 | 请求体包含 `"paymentMethod": "property_coin"` |
| `subType值无效`       | 使用了 `MONTHLY`/`YEARLY` 大写 | 使用小写 `"monthly"` / `"yearly"`            |
| `paymentMethod值无效` | 使用了 `coin`                | 使用 `property_coin`                       |


---



### 18.6.1 POST `/activity-groups/{id}/unsubscribe` — 取消订阅活动组

> ⚠️ **【当前未实现 · 2026-10-27 核对代码】** 后端不存在 `POST /activity-groups/{id}/unsubscribe`（只有 `POST /activity-groups/{id}/subscribe`）。退出活动组请使用 `POST /activity-groups/{id}/leave`。以下为原设计文档，保留备查。

**Auth**: Required  
**Roles**: `resident`

取消当前用户对指定活动组的**有效订阅**（状态置为 `cancelled`）。

> **说明**：本接口**仅取消订阅关系**，不会退出活动组成员身份（如需退出成员请调用 `18.8 /leave`），也**不涉及退款**。

**路径参数**：


| 参数   | 类型       | 必填  | 说明                       |
| ---- | -------- | --- | ------------------------ |
| `id` | `String` | ✅   | 活动组ID（`ag_` 或 `act_` 前缀） |


**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/activity-groups/ag_admin002/unsubscribe \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "groupId": "ag_admin002",
    "subscriptionId": "ags_xxx",
    "subscribed": false,
    "status": "cancelled",
    "endDate": "2026-08-07",
    "cancelledAt": "2026-07-07T05:20:00Z"
  },
  "timestamp": "2026-07-07 13:20:00"
}
```

**响应字段说明**：


| 字段               | 类型        | 说明                     |
| ---------------- | --------- | ---------------------- |
| `groupId`        | `String`  | 活动组ID                  |
| `subscriptionId` | `String`  | 被取消的订阅记录ID（`ags_` 前缀）  |
| `subscribed`     | `Boolean` | 取消后是否仍订阅，恒为 `false`    |
| `status`         | `String`  | 订阅状态，取消后为 `cancelled`  |
| `endDate`        | `String`  | 原订阅到期日期 `yyyy-MM-dd`   |
| `cancelledAt`    | `String`  | 取消时间（ISO-8601 UTC）     |


**错误响应**：


| 错误码     | HTTP | 说明          |
| ------- | ---- | ----------- |
| `90014` | 409  | 未订阅该活动组（无有效期内的订阅记录可取消） |


---



### 18.7 POST `/activity-groups/{id}/join` — 加入活动组

**Auth**: Required  
**Roles**: `resident`, `activity_leader`, `coordinator`, `sector_leader`, `individual_leader`, `property_admin`, `platform_admin`

**路径参数**：


| 参数   | 类型       | 必填  | 说明                       |
| ---- | -------- | --- | ------------------------ |
| `id` | `String` | ✅   | 活动组ID（`ag_` 或 `act_` 前缀） |


**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/activity-groups/act_74601000/join \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**请求体**：无

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "groupId": "act_74601000",
    "groupName": "周末跑步组",
    "memberId": "agm_xxx",
    "residentId": "res_dev001",
    "role": "member",
    "memberCount": 5,
    "joinedAt": "2026-07-07T05:25:00Z"
  },
  "timestamp": "2026-07-07 13:25:00"
}
```

**业务说明**：

- **免费组**：直接加入
- **付费组**：须先有有效期内的订阅记录，否则返回 `70002`：`请先订阅活动组`
- **退出后再次加入**：允许；服务端复用原 `activity_group_members` 记录并重新激活（`status` 从 `inactive` → `active`）

**错误响应**：


| 错误码     | HTTP | 说明                     |
| ------- | ---- | ---------------------- |
| `10002` | 400  | 活动组ID格式不正确             |
| `70002` | 409  | 请先订阅活动组（付费组未订阅）        |
| `90009` | 409  | 已是活动组成员（当前为 active 状态） |
| `90011` | 409  | 组长不能退出活动组              |


---



### 18.8 POST `/activity-groups/{id}/leave` — 离开活动组

**Auth**: Required  
**Roles**: `resident`, `activity_leader`, `coordinator`, `sector_leader`, `individual_leader`, `property_admin`, `platform_admin`

**路径参数**：


| 参数   | 类型       | 必填  | 说明                       |
| ---- | -------- | --- | ------------------------ |
| `id` | `String` | ✅   | 活动组ID（`ag_` 或 `act_` 前缀） |


**请求体**：无

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "groupId": "act_74601000",
    "residentId": "res_dev001",
    "memberCount": 4,
    "leftAt": "2026-07-07T05:30:00Z"
  },
  "timestamp": "2026-07-07 13:30:00"
}
```

**业务说明**：

- 退出为**软删除**（成员记录 `status` 置为 `inactive`，不物理删除）
- 组长不可退出，须转让组长或解散活动组
- 退出后可再次 `join` 或 `subscribe` 重新加入

**错误响应**：


| 错误码     | HTTP | 说明        |
| ------- | ---- | --------- |
| `90010` | 409  | 不是活动组成员   |
| `90011` | 409  | 组长不能退出活动组 |


---



### 18.9 DELETE `/activity-groups/{id}/members/{memberId}` — 移除成员

**Auth**: Required  
**Roles**: `activity_leader`, `resident`（须为活动组组长）, `coordinator`, `sector_leader`, `individual_leader`, `property_admin`, `platform_admin`

> **说明**：`activity_leader` 或 `resident` 调用时，须满足 `group.leaderId === 当前用户ID`，否则返回 `20004`。不可移除组长本人（`90011`）。

**路径参数**：


| 参数         | 类型       | 必填  | 说明    |
| ---------- | -------- | --- | ----- |
| `id`       | `String` | ✅   | 活动组ID |
| `memberId` | `String` | ✅   | 成员ID  |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "activityGroupId": "ag_demo001",
    "memberId": "res_xxxxxx",
    "removedAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 18.9.1 DELETE `/activity-groups/{id}` — 解散活动组（硬删除）

**Auth**: Required  
**Roles**: `activity_leader`（须为该组组长）、`property_admin`、`platform_admin`

> **说明**：彻底删除活动组及其全部成员、帖子相关数据（**物理删除，不可恢复**）。仅以下两类账号可操作：
> - 该活动组的组长（`group.leaderId === 当前用户ID`），无需额外权限；
> - 物业管理员 `property_admin` 或平台管理员 `platform_admin`，且须与活动组同物业（`assertSamePropertyOrPlatform`）。
>
> 注意：采用**硬删除**（用户选择，避免软删除影响后续业务）。调用前无需先移除成员或退订，后端会先清理该组下的订阅记录（`activity_group_subscriptions`）再删除组本体。
>
> **v7.9 修复（405 根因）**：该端点此前在后端**未接线**（仅实现了 `GET/PUT /{id}`），组长调用返回 405「请求方法不支持」。现已新增 `DELETE /activity-groups/{id}` 并实现硬删除逻辑。

**路径参数**：


| 参数   | 类型       | 必填  | 说明                           |
| ---- | -------- | --- | ---------------------------- |
| `id` | `String` | ✅   | 活动组ID（`ag_` 或 `act_` 前缀） |


**请求体**：无

**请求示例**：

```bash
curl -X DELETE "https://test.yuanxusoftware.top/api/v1/activity-groups/act_74601000" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "groupId": "act_74601000",
    "status": "deleted"
  },
  "timestamp": "2026-08-12 13:30:00"
}
```

**业务说明**：

- 操作顺序：先 `DELETE FROM activity_group_subscriptions WHERE group_id=?`，再 `DELETE FROM activity_group_members WHERE group_id=?`、删除价格档位（`ON DELETE CASCADE`），最后 `DELETE FROM activity_groups WHERE id=?`；成员表、价格档位表通过外键 `ON DELETE CASCADE` 自动级联清理（`activity_group_subscriptions` 无级联，故显式先删）
- 组长不可被他人解散：组长本人调用直接通过；非组长须具备 `property_admin`/`platform_admin` 且同物业，否则报错
- 为不可逆操作，前端务必二次确认弹窗

**错误响应**：


| 错误码     | HTTP | 说明                       |
| ------- | ---- | ------------------------ |
| `90007` | 404  | 活动组不存在                   |
| `20004` | 403  | 非组长且非物业/平台管理员（权限不足）      |
| `30001` | 404  | 非组长账号但所属物业与活动组不一致（物业越权） |
| `90011` | 409  | 组内数据校验异常（组长不可被移除等）       |


---

### 18.10 PUT `/activity-groups/{id}` — 更新活动组

**Auth**: Required  
**Roles**: `activity_leader`, `resident`（须为活动组组长）, `coordinator`, `sector_leader`, `individual_leader`, `property_admin`, `platform_admin`

> **说明**：`activity_leader` / `resident` / `coordinator` / `sector_leader` / `individual_leader` 调用时，须满足 `group.leaderId === 当前用户ID`，否则返回 `20004`。
>
> **v7.9 修复（403 根因）**：本端点此前在权限判断中仅 `resident` 走「组长归属校验」分支，其余组长类角色（含 `activity_leader`）落入 else 分支并要求 `property_admin`/`platform_admin`，导致组长本人保存活动组返回 403 / `20004`。现已将 `activity_leader` 等组长类角色纳入归属校验分支，与文档 §18.10 一致。

**路径参数**：


| 参数   | 类型       | 必填  | 说明    |
| ---- | -------- | --- | ----- |
| `id` | `String` | ✅   | 活动组ID |


**请求体**：


| 字段            | 类型           | 必填  | 校验                 | 说明     |
| ------------- | ------------ | --- | ------------------ | ------ |
| `name`        | `String`     | ❌   | `@Size(max=100)`   | 活动组名称  |
| `description` | `String`     | ❌   |                    | 描述     |
| `coverUrl`    | `String`     | ❌   | `@Size(max=500)`   | 封面图URL |
| `monthlyFee`  | `BigDecimal` | ❌   | `@DecimalMin("0")` | 月费     |
| `yearlyFee`   | `BigDecimal` | ❌   | `@DecimalMin("0")` | 年费     |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "ag_demo001",
    "name": "广场舞队",
    "description": "社区广场舞活动",
    "updatedAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 18.11 GET `/activity-groups/{id}` — 活动组详情

**Auth**: Optional（未登录可浏览同物业公开信息）；`activity_leader` 调用时 **Auth: Required**  
**Roles**: `any`

> **数据范围**：`activity_leader` 仅可查看本人担任组长的活动组详情；访问其他活动组返回 `20004`。

**路径参数**：


| 参数   | 类型       | 必填  | 说明    |
| ---- | -------- | --- | ----- |
| `id` | `String` | ✅   | 活动组ID |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "ag_demo001",
    "name": "广场舞队",
    "description": "社区广场舞活动",
    "coverUrl": "https://cdn.example.com/activity/ag_demo001.jpg",
    "leaderId": "res_b7m9x1",
    "leaderName": "张先生",
    "communityId": "com_demo001",
    "communityName": "演示小区",
    "memberCount": 25,
    "subscriberCount": 50,
    "monthlyFee": 0.00,
    "yearlyFee": 0.00,
    "status": "active",
    "hasJoined": true,
    "myRole": "member",
    "hasActiveSubscription": false,
    "subscribedAmount": null,
    "createdAt": "2026-06-01 10:00:00",
    "updatedAt": "2026-06-28 14:30:00"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



## 19. 意见箱模块 (Opinion Boxes)



### 19.1 GET `/opinion-boxes` — 意见箱列表

**Auth**: Required  
**Roles**: `resident`

**Query 参数**：


| 参数         | 类型        | 必填  | 默认值          | 说明                        |
| ---------- | --------- | --- | ------------ | ------------------------- |
| `page`     | `int`     | ❌   | `1`          | 页码                        |
| `pageSize` | `int`     | ❌   | `20`         | 每页条数                      |
| `status`   | `String`  | ❌   |              | `active`/`ended`/`closed` |
| `mine`     | `Boolean` | ❌   |              | 是否只看我创建的                  |
| `voted`    | `Boolean` | ❌   |              | 是否已投票                     |
| `sort`     | `String`  | ❌   | `-createdAt` | 排序                        |


---



### 19.2 POST `/opinion-boxes` — 创建意见箱

**Auth**: Required  
**Roles**: `resident`

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/opinion-boxes \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "title": "是否同意增设健身器材",
    "content": "小区公共区域增设健身器材的提议",
    "endTime": "2026-07-31 23:59:59"
  }'
```

**请求体**：


| 字段          | 类型       | 必填  | 校验                          | 说明                                                                          |
| ----------- | -------- | --- | --------------------------- | --------------------------------------------------------------------------- |
| `title`     | `String` | ✅   | `@NotBlank @Size(max=200)`  | 标题                                                                          |
| `content`   | `String` | ✅   | `@NotBlank @Size(max=2000)` | 内容                                                                          |
| `startTime` | `String` | ❌   |                             | 开始时间，支持格式：`yyyy-MM-dd HH:mm:ss` 或 ISO-8601（`yyyy-MM-ddTHH:mm:ssZ`），默认当前时间   |
| `endTime`   | `String` | ❌   |                             | 结束时间，支持格式：`yyyy-MM-dd HH:mm:ss` 或 ISO-8601（`yyyy-MM-ddTHH:mm:ssZ`），必须晚于开始时间 |


**错误响应**：


| 错误码     | HTTP | 说明            |
| ------- | ---- | ------------- |
| `90006` | 409  | 每家庭每月仅可发表1条意见 |


---



### 19.3 GET `/opinion-boxes/{id}/result` — 投票结果

**Auth**: Required  
**Roles**: `resident`

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "opinionBoxId": "ob_demo001",
    "title": "是否同意增设健身器材",
    "status": "active",
    "totalVotes": 50,
    "supportCount": 35,
    "opposeCount": 10,
    "abstainCount": 5,
    "supportRate": 0.70,
    "opposeRate": 0.20,
    "abstainRate": 0.10,
    "resultPublished": false,
    "endTime": "2026-07-31 23:59:59",
    "publishedAt": null
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 19.4 POST `/opinion-boxes/{id}/vote` — 投票

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/opinion-boxes/ob_demo001/vote \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "voteOption": "support"
  }'
```

**请求体**：


| 字段           | 类型       | 必填  | 校验          | 说明                           |
| ------------ | -------- | --- | ----------- | ---------------------------- |
| `voteOption` | `String` | ✅   | `@NotBlank` | `support`/`oppose`/`abstain` |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "voteRecordId": "vr_demo001",
    "opinionBoxId": "ob_demo001",
    "familyId": "fam_demo001",
    "voteOption": "support",
    "totalVotes": 51,
    "supportCount": 36,
    "opposeCount": 10,
    "abstainCount": 5,
    "votedAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**错误响应**：


| 错误码     | HTTP | 说明      |
| ------- | ---- | ------- |
| `90002` | 409  | 投票已结束   |
| `90005` | 409  | 该家庭已投过票 |


---



### 19.5 GET `/opinion-boxes/{id}` — 意见箱详情

**Auth**: Required  
**Roles**: `resident`

**路径参数**：


| 参数   | 类型       | 必填  | 说明    |
| ---- | -------- | --- | ----- |
| `id` | `String` | ✅   | 意见箱ID |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "ob_demo001",
    "title": "是否同意增设健身器材",
    "content": "小区公共区域增设健身器材的提议",
    "creatorId": "res_b7m9x1",
    "creatorName": "张先生",
    "communityId": "com_demo001",
    "communityName": "演示小区",
    "status": "active",
    "totalVotes": 50,
    "supportCount": 35,
    "opposeCount": 10,
    "abstainCount": 5,
    "supportRate": 0.70,
    "opposeRate": 0.20,
    "abstainRate": 0.10,
    "resultPublished": false,
    "publishedAt": null,
    "endTime": "2026-07-31 23:59:59",
    "createdAt": "2026-07-01 10:00:00"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



## 20. 报表模块 (Reports)



### 20.1 GET `/reports/overview` — 总览报表

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "scope": {
      "propertyCompanyId": "pc_demo001",
      "propertyCompanyName": "演示物业公司"
    },
    "summary": {
      "merchantCount": 15,
      "residentCount": 500,
      "familyCount": 200,
      "totalConsumptionAmount": 50000.00,
      "pointPoolBalance": 10000.00,
      "propertyCoinCirculation": 25000.00,
      "propertyFeeCollectionRate": 0.85,
      "propertyFeeTotalAmount": 100000.00,
      "propertyFeePaidAmount": 85000.00,
      "propertyFeeOverdueAmount": 15000.00,
      "totalOrderCount": 1200
    },
    "comparison": {
      "consumptionGrowthRate": 0.15,
      "orderGrowthRate": 0.20,
      "residentGrowthRate": 0.05
    }
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 20.2 GET `/reports/point-summary` — 积分汇总

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/reports/point-summary?startDate=2026-06-01&endDate=2026-06-30&propertyCompanyId=pc_demo001" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：


| 参数                  | 类型       | 必填  | 默认值 | 说明                |
| ------------------- | -------- | --- | --- | ----------------- |
| `startDate`         | `String` | ❌   |     | 开始日期 `yyyy-MM-dd` |
| `endDate`           | `String` | ❌   |     | 结束日期 `yyyy-MM-dd` |
| `propertyCompanyId` | `String` | ❌   |     | 物业公司ID（超管筛选）      |
| `groupBy`           | `String` | ❌   |     | 分组维度              |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "period": { "startDate": "2026-06-01", "endDate": "2026-06-30" },
    "summary": {
      "totalIssued": 25000,
      "totalRedeemed": 8000,
      "totalExpired": 2000,
      "totalFeeDeducted": 5000,
      "netChange": 10000,
      "activeResidentCount": 380,
      "avgPointsPerResident": 65.79
    },
    "bySource": [
      { "source": "order", "amount": 15000 },
      { "source": "activity", "amount": 5000 },
      { "source": "manual", "amount": 3000 },
      { "source": "merchant_grant", "amount": 2000 }
    ],
    "byMonth": [
      { "month": "2026-01", "issued": 20000, "redeemed": 6000 },
      { "month": "2026-02", "issued": 22000, "redeemed": 7000 },
      { "month": "2026-03", "issued": 25000, "redeemed": 8000 }
    ]
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 20.3 GET `/reports/order-summary` — 订单汇总

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/reports/order-summary?startDate=2026-06-01&endDate=2026-06-30&propertyCompanyId=pc_demo001" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：


| 参数                  | 类型       | 必填  | 默认值 | 说明                |
| ------------------- | -------- | --- | --- | ----------------- |
| `startDate`         | `String` | ❌   |     | 开始日期 `yyyy-MM-dd` |
| `endDate`           | `String` | ❌   |     | 结束日期 `yyyy-MM-dd` |
| `propertyCompanyId` | `String` | ❌   |     | 物业公司ID（超管筛选）      |
| `groupBy`           | `String` | ❌   |     | 分组维度              |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "period": { "startDate": "2026-06-01", "endDate": "2026-06-30" },
    "summary": {
      "totalOrders": 1250,
      "totalRevenue": 62500.00,
      "avgOrderAmount": 50.00,
      "completedOrders": 1100,
      "cancelledOrders": 100,
      "refundingOrders": 50,
      "refundAmount": 2500.00,
      "activeMerchantCount": 15,
      "avgOrdersPerMerchant": 83.33
    },
    "byStatus": [
      { "status": "completed", "count": 1100, "amount": 55000.00 },
      { "status": "cancelled", "count": 100, "amount": 5000.00 },
      { "status": "refunding", "count": 50, "amount": 2500.00 }
    ],
    "topMerchants": [
      { "merchantId": "mch_demo001", "merchantName": "万达超市", "orderCount": 320, "revenue": 16000.00 }
    ]
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 20.4 GET `/reports/distribution-summary` — 分成汇总

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/reports/distribution-summary?startDate=2026-06-01&endDate=2026-06-30&propertyCompanyId=pc_demo001" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：


| 参数                  | 类型       | 必填  | 默认值       | 说明                |
| ------------------- | -------- | --- | --------- | ----------------- |
| `startDate`         | `String` | ❌   |           | 开始日期 `yyyy-MM-dd` |
| `endDate`           | `String` | ❌   |           | 结束日期 `yyyy-MM-dd` |
| `propertyCompanyId` | `String` | ❌   |           | 物业公司ID（超管筛选）      |
| `dimension`         | `String` | ❌   | `summary` | 统计维度              |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "period": { "startDate": "2026-06-01", "endDate": "2026-06-30" },
    "summary": {
      "totalDistributableAmount": 15000.00,
      "totalDistributedAmount": 12000.00,
      "propertyAmount": 4500.00,
      "coordinatorAmount": 10500.00,
      "sectorLeaderAmount": 1050.00,
      "individualLeaderAmount": 525.00
    },
    "byCoordinator": [
      { "coordinatorId": "coo_demo001", "name": "统筹负责人A", "amount": 10500.00, "orderCount": 800 }
    ],
    "bySector": [
      { "sector": "cleaning", "sectorName": "保洁", "amount": 500.00 },
      { "sector": "repair", "sectorName": "维修", "amount": 550.00 }
    ]
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 20.5 GET `/reports/export` — 导出报表

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/reports/export?type=point-summary&format=csv&month=2026-06" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -o report.csv
```

**响应**：文件流（CSV）

---



## 21. 分成模块 (Distribution)



### 21.1 GET `/distribution/records` — 分成记录列表

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`，或持有启用中负责人记录（统筹/板块/个体）的住户（v8.13 标签化）

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/distribution/records?page=1&pageSize=20&startDate=2026-06-01&endDate=2026-06-30&sort=-createdAt" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：


| 参数                   | 类型       | 必填  | 默认值          | 说明        |
| -------------------- | -------- | --- | ------------ | --------- |
| `page`               | `int`    | ❌   | `1`          | 页码        |
| `pageSize`           | `int`    | ❌   | `20`         | 每页条数      |
| `orderId`            | `String` | ❌   |              | 订单ID筛选    |
| `merchantId`         | `String` | ❌   |              | 商家ID筛选    |
| `coordinatorId`      | `String` | ❌   |              | 统筹负责人ID筛选 |
| `sectorLeaderId`     | `String` | ❌   |              | 板块负责人ID筛选 |
| `individualLeaderId` | `String` | ❌   |              | 个体负责人ID筛选 |
| `propertyCompanyId`  | `String` | ❌   |              | 物业公司ID筛选  |
| `startDate`          | `String` | ❌   |              | 开始日期      |
| `endDate`            | `String` | ❌   |              | 结束日期      |
| `sort`               | `String` | ❌   | `-createdAt` | 排序        |


**成功响应**（`DistributionRecordItemResponse`）：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "dr_demo001",
        "orderId": "ord_demo001",
        "orderNo": "ORD202607010001",
        "merchantId": "mch_demo001",
        "merchantName": "万达超市",
        "residentId": "res_b7m9x1",
        "residentName": "张先生",
        "propertyCompanyId": "pc_demo001",
        "totalAmount": 100.00,
        "productAmount": 99.70,
        "commissionRate": 0.20,
        "commissionBaseAmount": 19.94,
        "pointCost": 0.00,
        "coinCost": 0.00,
        "distributableAmount": 19.94,
        "deficitAmount": 0.00,
        "deficitSponsor": "property",
        "platformShareRate": 0.1000,
        "platformShare": 1.99,
        "propertyShare": 11.96,
        "managementPoolAmount": 5.99,
        "coordinatorShare": 2.40,
        "sectorLeaderShare": 1.80,
        "individualLeaderShare": 1.79,
        "userPointShareRate": 0.1000,
        "userRewardShareRate": 0.1000,
        "userPointBudget": 0.50,
        "userRewardBudget": 0.50,
        "originalDeliveryFee": 0.30,
        "deliveryFee": 0.30,
        "deliveryWaiverAmount": 0.00,
        "deliverySubsidySponsor": "merchant",
        "deliverySubsidyAmount": 0.00,
        "deliverySettlementBase": 0.30,
        "merchantGoodsShare": 79.76,
        "merchantDeliverySubsidy": 0.00,
        "merchantAdjustmentAmount": 0.00,
        "merchantShare": 79.76,
        "platformDeliveryShare": 0.03,
        "courierEarning": 0.27,
        "calculationVersion": "distribution_b_v2",
        "status": "distributed",
        "coordinatorId": "coo_demo001",
        "coordinatorName": "统筹A",
        "sectorLeaderId": "sec_demo001",
        "sectorLeaderName": "板块B",
        "individualLeaderId": "ind_demo001",
        "individualLeaderName": "个体C",
        "createdAt": "2026-07-01 15:23:33"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 80,
      "totalPages": 4
    }
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**记录项字段（核心字段，规范§7.2/§8.6）**：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id`/`orderId`/`orderNo` | `String` | 记录ID / 订单ID / 订单编号 |
| `merchantId`/`merchantName` | `String` | 商家ID / 名称 |
| `residentId`/`residentName` | `String` | 住户ID / 姓名 |
| `propertyCompanyId` | `String` | 物业公司ID |
| `totalAmount` | `BigDecimal` | 订单总额（含配送费） |
| `productAmount` | `BigDecimal` | 商品金额（不含配送费） |
| `commissionRate` | `BigDecimal` | 商家抽佣比例快照 |
| `commissionBaseAmount` | `BigDecimal` | 抽佣基础额 = 商品金额 × 抽佣率 |
| `productCommission` | `BigDecimal` | 兼容旧字段，等价于 `commissionBaseAmount` |
| `productBase` | `BigDecimal` | 商品实付 = 商品金额 − 积分成本（物业币为支付资金来源，不扣减） |
| `pointCost`/`coinCost` | `BigDecimal` | 积分成本；物业币支付金额（快照，不作为成本） |
| `userPointShareRate`/`userRewardShareRate` | `BigDecimal` | 🆕 V36 用户积分 / 用户奖励金预算比例快照（占 `commissionBaseAmount`，默认各 `0.1000`）。**V36 之前的历史记录为 `null`** |
| `userPointBudget`/`userRewardBudget` | `BigDecimal` | 🆕 V36 本单从抽佣基础额里先切出的用户积分 / 奖励金预算（元）。**V36 之前的历史记录为 `null`**，按「未启用」渲染，不要当 `0.00` 参与合计 |
| `distributableAmount` | `BigDecimal` | 平台盘 = max(抽佣基础额 − 积分成本 − `userPointBudget` − `userRewardBudget`, 0) |
| `deficitAmount`/`deficitSponsor` | - | 积分成本击穿差额 / 承担方（默认 property；物业币不再参与击穿） |
| `platformShareRate` | `BigDecimal` | 公司平台盘比例快照 |
| `platformShare` | `BigDecimal` | 公司商品平台盘收入 |
| `propertyShare` | `BigDecimal` | 物业商品平台盘收入 |
| `managementPoolAmount` | `BigDecimal` | 管理盘 = 平台盘 − 平台 − 物业（减法吸收分币误差） |
| `sectorGrossAmount` | `BigDecimal` | 板块毛额 = 管理盘 × sectorLeaderRate |
| `coordinatorShare` | `BigDecimal` | 统筹实得 = 管理盘 − 板块毛额 |
| `sectorLeaderShare` | `BigDecimal` | 板块负责人实得 = 板块毛额 − 个体负责人 |
| `individualLeaderShare` | `BigDecimal` | 个体负责人实得 = 板块毛额 × individualLeaderRate |
| `originalDeliveryFee` | `BigDecimal` | 满额减免前基础配送费 |
| `deliveryFee`/`effectiveDeliveryFee` | `BigDecimal` | 实付配送费 |
| `deliveryWaiverAmount` | `BigDecimal` | 满额减免金额 |
| `deliverySubsidySponsor` | `String` | 减免承担方 `none`/`merchant`/`property`/`platform` |
| `deliverySubsidyAmount` | `BigDecimal` | 配送补贴金额 |
| `deliverySettlementBase` | `BigDecimal` | 配送结算基数 = 实付 + 补贴 |
| `merchantGoodsShare` | `BigDecimal` | 商家商品实得 |
| `merchantDeliverySubsidy` | `BigDecimal` | 商家承担的配送补贴（承担方为 merchant 时为补贴额，否则 0） |
| `merchantAdjustmentAmount` | `BigDecimal` | 商家调整额（退款/补差/冲账） |
| `merchantShare` | `BigDecimal` | 商家最终实得 = 商家商品实得 − 商家配送补贴 + 调整额 |
| `platformDeliveryShare` | `BigDecimal` | 公司配送收入 |
| `courierEarning` | `BigDecimal` | 配送员最终收入 = 基数 − 公司配送收入 |
| `courierShare` | `BigDecimal` | 兼容旧字段，等价于 `courierEarning` |
| `calculationVersion` | `String` | 计算版本：`legacy` / `distribution_b_v1` / 🆕 `distribution_b_v2`（V36 起含用户先扣档）。**判断是否新口径优先看本字段，比判 `null` 更稳** |
| `status` | `String` | `pending`/`distributed`/`reversed`/`partially_reversed` |
| `coordinatorId`/`coordinatorName` | `String` | 统筹ID / 姓名 |
| `sectorLeaderId`/`sectorLeaderName` | `String` | 板块负责人ID / 姓名 |
| `individualLeaderId`/`individualLeaderName` | `String` | 个体负责人ID / 姓名 |
| `createdAt` | `String` | 创建时间 |

**守恒校验（结算前强制，规范§11.3）**：

```
commissionBaseAmount = distributableAmount + pointCost + userPointBudget + userRewardBudget + deficitAmount
distributableAmount = platformShare + propertyShare + coordinatorShare + sectorLeaderShare + individualLeaderShare
deliverySettlementBase = platformDeliveryShare + courierEarning
merchantShare = merchantGoodsShare − merchantDeliverySubsidy + merchantAdjustmentAmount
totalAmount = productAmount + deliveryFee
```

> 校验失败返回 `92030`（分红金额不守恒），不会落库。历史单（`distribution_b_v1`）没有 `userPointBudget`/`userRewardBudget`，第一式退化为 `commissionBaseAmount = distributableAmount + pointCost + deficitAmount`。

---



### 21.2 GET `/distribution/stats` — 分成统计

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`，或持有启用中负责人记录（统筹/板块/个体）的住户（v8.13 标签化）

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/distribution/stats?startDate=2026-06-01&endDate=2026-06-30&propertyCompanyId=pc_demo001" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：


| 参数                  | 类型       | 必填  | 默认值       | 说明       |
| ------------------- | -------- | --- | --------- | -------- |
| `startDate`         | `String` | ❌   |           | 开始日期     |
| `endDate`           | `String` | ❌   |           | 结束日期     |
| `dimension`         | `String` | ❌   | `summary` | 统计维度     |
| `propertyCompanyId` | `String` | ❌   |           | 物业公司ID筛选 |


**成功响应**（`DistributionStatsResponse.Summary`）：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "summary": {
      "productAmount": 125000.00,
      "totalOrderAmount": 125800.00,
      "totalDistributableAmount": 15000.00,
      "merchantGoodsAmount": 100000.00,
      "merchantDeliverySubsidyAmount": 2000.00,
      "merchantAmount": 98000.00,
      "platformAmount": 1500.00,
      "platformDeliveryAmount": 1000.00,
      "courierAmount": 9000.00,
      "propertyAmount": 4500.00,
      "managementPoolAmount": 10500.00,
      "coordinatorAmount": 4200.00,
      "sectorLeaderAmount": 3150.00,
      "individualLeaderAmount": 3150.00,
      "buildingLeaderAmount": 300.00,
      "deficitAmount": 0.00,
      "reversalAmount": 0.00
    },
    "byProperty": [
      {
        "propertyCompanyId": "pc_demo001",
        "propertyName": "演示物业公司",
        "amount": 15000.00
      }
    ],
    "byCoordinator": [
      {
        "coordinatorId": "coo_demo001",
        "name": "统筹负责人A",
        "amount": 10500.00
      }
    ],
    "bySector": [
      {
        "sector": "cleaning",
        "sectorName": "保洁",
        "amount": 500.00
      }
    ]
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**summary 字段说明**：

| 字段 | 说明 |
| --- | --- |
| `productAmount` | 商品金额合计（不含配送费） |
| `totalOrderAmount` | 总订单额合计（含配送费） |
| `totalDistributableAmount` | 商品平台盘合计 |
| `merchantGoodsAmount` | 商家商品实得合计 |
| `merchantDeliverySubsidyAmount` | 商家配送补贴合计（商家承担部分） |
| `merchantAmount` | 商家最终实得合计 = 商家商品实得 − 商家配送补贴 |
| `platformAmount` | 公司商品平台盘收入合计 |
| `platformDeliveryAmount` | 公司配送收入合计（与 `platformAmount` 分开统计，不得合并） |
| `courierAmount` | 配送员收入合计 |
| `propertyAmount` | 物业商品平台盘收入合计 |
| `managementPoolAmount` | 管理盘合计（= 统筹 + 板块负责人 + 个体负责人） |
| `coordinatorAmount` | 统筹总金额 |
| `sectorLeaderAmount` | 板块负责人总金额 |
| `individualLeaderAmount` | 个体负责人总金额 |
| `buildingLeaderAmount` | 单元楼长分成合计（从物业分成中定向扣除，不影响统筹/板块/个体链路） |
| `deficitAmount` | 成本击穿差额合计 |
| `reversalAmount` | 冲正金额合计（REVERSED 记录绝对值之和） |

---



### 21.3 GET `/distribution/calculate` — 分账试算（预览，不落库）

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`（假设试算）；订单详情试算可面向移动端角色

支持**两种模式**，返回同一套扁平字段（商品分账链 + 配送分账链，规范§8.5）：

| 模式 | 传参 | 说明 |
| --- | --- | --- |
| ① 订单快照试算 | `orderId=ord_demo001` | 后端读取订单落库快照重算，客户端**不得**提供可篡改金额与比例（推荐移动端订单详情使用） |
| ② 管理端假设试算 | `merchantId` + `productAmount` 等 | 管理端配置预览；响应与正式结算同一计算服务 |

**请求示例①（订单快照试算）**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/distribution/calculate?orderId=ord_demo001" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**请求示例②（管理端假设试算）**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/distribution/calculate?merchantId=mch_demo001&productAmount=100.00&originalDeliveryFee=2.00&deliveryFee=0.00&deliverySubsidySponsor=merchant&pointUsed=0&coinUsed=0.00&propertyCompanyId=pc_demo001" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `orderId` | `String` | ①必填 | | 订单ID（传入时读取订单快照，忽略其余金额参数） |
| `merchantId` | `String` | ②必填 | | 商家ID |
| `productAmount` | `BigDecimal` | ②✅ | | 商品金额（不含配送费） |
| `totalAmount` | `BigDecimal` | ❌ | | 订单总金额（兼容旧调用 = 商品金额 + 配送费；不传 `productAmount` 时用于推导商品金额） |
| `originalDeliveryFee` | `BigDecimal` | ❌ | =deliveryFee | 满额减免前基础配送费 |
| `deliveryFee` | `BigDecimal` | ❌ | `0` | 实付配送费 |
| `deliverySubsidySponsor` | `String` | ❌ | `merchant` | 减免承担方 `none`/`merchant`/`property`/`platform` |
| `pointUsed` | `Integer` | ❌ | `0` | 使用积分数量 |
| `coinUsed` | `BigDecimal` | ❌ | `0` | 使用物业币数量 |
| `propertyCompanyId` | `String` | ❌ | | 物业公司ID |

> `orderId` 与 `merchantId` 至少传一个，否则 `10002`。

**成功响应**（`DistributionCalculateResponse`，扁平字段）：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "totalAmount": 100.00,
    "productAmount": 100.00,
    "commissionRate": 0.20,
    "pointCost": 0.00,
    "coinCost": 0.00,
    "commissionBaseAmount": 20.00,
    "userPointShareRate": 0.1000,
    "userRewardShareRate": 0.1000,
    "userPointBudget": 2.00,
    "userRewardBudget": 2.00,
    "distributableAmount": 16.00,
    "deficitAmount": 0.00,
    "deficitSponsor": "property",
    "platformShareRate": 0.1000,
    "platformShare": 1.60,
    "propertyShare": 9.60,
    "managementPoolAmount": 4.80,
    "sectorGrossAmount": 2.88,
    "coordinatorShare": 1.92,
    "sectorLeaderShare": 1.44,
    "individualLeaderShare": 1.44,
    "buildingLeaderShare": 0.00,
    "originalDeliveryFee": 2.00,
    "deliveryFee": 0.00,
    "deliveryWaiverAmount": 2.00,
    "deliverySubsidySponsor": "merchant",
    "deliverySubsidyAmount": 2.00,
    "deliverySettlementBase": 2.00,
    "merchantGoodsShare": 80.00,
    "merchantDeliverySubsidy": 2.00,
    "merchantAdjustmentAmount": 0.00,
    "merchantShare": 78.00,
    "platformDeliveryShare": 0.20,
    "courierEarning": 1.80,
    "coordinatorId": "coo_demo001",
    "sectorLeaderId": "sec_demo001",
    "individualLeaderId": "ind_demo001",
    "buildingLeaderId": null,
    "calculationVersion": "distribution_b_v2"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**响应字段说明**：

| 字段 | 说明 |
| --- | --- |
| `totalAmount` | 订单总额（= 商品金额 + 实付配送费，兼容旧字段） |
| `productAmount` | 商品金额（不含配送费） |
| `commissionRate` | 商家抽佣比例 |
| `pointCost`/`coinCost` | 积分成本；物业币支付金额（快照，不作为成本） |
| `commissionBaseAmount` | 抽佣基础额 = 商品金额 × 抽佣率 |
| `userPointShareRate`/`userRewardShareRate` | 🆕 V36 用户积分 / 用户奖励金预算比例（占抽佣基础额，物业公司级可配，默认各 `0.1000`） |
| `userPointBudget`/`userRewardBudget` | 🆕 V36 本单从抽佣基础额里**先切**的用户积分 / 奖励金预算（元）。用户积分预算先向下取整到整数积分再折回元，保证「预算 = 实发积分 ÷ 兑换率」一分不差 |
| `distributableAmount` | 平台盘 = max(抽佣基础额 − 积分成本 − `userPointBudget` − `userRewardBudget`, 0) |
| `deficitAmount`/`deficitSponsor` | 积分成本击穿差额 / 承担方（默认 property；物业币是已收现金的支付工具，不参与击穿） |
| `platformShareRate` | 公司平台盘比例快照 |
| `platformShare` | 公司商品平台盘收入 = 平台盘 × platformShareRate |
| `propertyShare` | 物业商品平台盘收入（已扣楼长分成） |
| `managementPoolAmount` | 管理盘 = 平台盘 − 平台 − 物业（减法吸收分币误差） |
| `sectorGrossAmount` | 板块毛额 = 管理盘 × sectorLeaderRate |
| `coordinatorShare` | 统筹实得 = 管理盘 − 板块毛额 |
| `sectorLeaderShare` | 板块负责人实得 = 板块毛额 − 个体负责人 |
| `individualLeaderShare` | 个体负责人实得 = 板块毛额 × individualLeaderRate |
| `buildingLeaderShare` | 单元楼长份额（从物业分成中扣除，未绑定楼长时为 0） |
| `originalDeliveryFee` | 满额减免前基础配送费 |
| `deliveryFee` | 实付配送费 |
| `deliveryWaiverAmount` | 满额减免金额 |
| `deliverySubsidySponsor` | 减免承担方 `none`/`merchant`/`property`/`platform` |
| `deliverySubsidyAmount` | 配送补贴金额 |
| `deliverySettlementBase` | 配送结算基数 = 实付 + 补贴 |
| `merchantGoodsShare` | 商家商品实得 |
| `merchantDeliverySubsidy` | 商家承担的配送补贴（承担方为 merchant 时为补贴额，否则 0） |
| `merchantAdjustmentAmount` | 商家调整额 |
| `merchantShare` | 商家最终实得 |
| `platformDeliveryShare` | 公司配送收入 = 基数 × platformDeliveryShareRate |
| `courierEarning` | 配送员最终收入 = 基数 − 公司配送收入（**正式字段**）；兼容别名 `courierShare` 恒等于本字段 |
| `coordinatorId`/`sectorLeaderId`/`individualLeaderId`/`buildingLeaderId` | 各级负责人ID（`buildingLeaderId` 为预览时按住户楼栋实时解析） |
| `calculationVersion` | 计算版本（V36 起为 `distribution_b_v2`；历史单为 `distribution_b_v1`/`legacy`） |

> **兼容字段**：`productCommission` = `commissionBaseAmount`、`productBase` = 商品实付 = `productAmount − pointCost`（🆕 物业币不再从实付里扣减）、`effectiveDeliveryFee` = `deliveryFee`、`courierShare` = `courierEarning`、`breakdown` 旧结构保留。标准字段语义以本表为准。

> 🆕 **V36 用户侧先扣档（口径示意，100 元订单、抽佣 5%、两档各 10%）**：
>
> ```
> 用户付款 100.00
>  ├─ 95.00 商家自留
>  └─  5.00 抽佣基础额
>           ├─ 0.50 用户积分预算 → 实发 50 积分（100 积分 = 1 元）
>           ├─ 0.50 用户奖励金预算 → 实发 0.50 物业币
>           └─ 4.00 平台盘（distributableAmount）→ 平台 / 物业 / 统筹 一级三档
> ```
>
> 两档是**先扣**，不从物业或统筹任何一方嘴里出，两边按扣完的一起等比少；一级三档合计仍须 100%（`97006`）。
> 用户两档合计 > 100% 时配置接口返回 `10002`；住户拿历史积分抵扣的兑付成本仍单独作为 `pointCost` 扣减，与本档叠加。

---



### 21.4 GET `/admin/distribution-records` — 分成记录（管理端）

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/admin/distribution-records?page=1&pageSize=20&propertyCompanyId=pc_demo001&startDate=2026-06-01&endDate=2026-06-30" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：


| 参数                   | 类型       | 必填  | 默认值          | 说明           |
| -------------------- | -------- | --- | ------------ | ------------ |
| `page`               | `int`    | ❌   | `1`          | 页码           |
| `pageSize`           | `int`    | ❌   | `20`         | 每页条数         |
| `orderId`            | `String` | ❌   |              | 订单ID筛选       |
| `merchantId`         | `String` | ❌   |              | 商家ID筛选       |
| `coordinatorId`      | `String` | ❌   |              | 统筹负责人ID筛选    |
| `sectorLeaderId`     | `String` | ❌   |              | 板块负责人ID筛选    |
| `individualLeaderId` | `String` | ❌   |              | 个体负责人ID筛选    |
| `propertyCompanyId`  | `String` | ❌   |              | 物业公司ID（超管筛选） |
| `startDate`          | `String` | ❌   |              | 开始日期         |
| `endDate`            | `String` | ❌   |              | 结束日期         |
| `sort`               | `String` | ❌   | `-createdAt` | 排序           |


**成功响应**（同 §21.1，`DistributionRecordItemResponse`，字段表见 §21.1）：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "dr_demo001",
        "orderId": "ord_demo001",
        "orderNo": "ORD202607010001",
        "merchantId": "mch_demo001",
        "merchantName": "万达超市",
        "propertyCompanyId": "pc_demo001",
        "totalAmount": 100.00,
        "productAmount": 99.70,
        "distributableAmount": 19.94,
        "platformShare": 1.99,
        "propertyShare": 11.96,
        "managementPoolAmount": 5.99,
        "coordinatorShare": 2.40,
        "sectorLeaderShare": 1.80,
        "individualLeaderShare": 1.79,
        "merchantShare": 79.76,
        "platformDeliveryShare": 0.03,
        "courierEarning": 0.27,
        "userPointShareRate": 0.1000,
        "userRewardShareRate": 0.1000,
        "userPointBudget": 0.50,
        "userRewardBudget": 0.50,
        "calculationVersion": "distribution_b_v2",
        "status": "distributed",
        "createdAt": "2026-07-01 15:23:33"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 80,
      "totalPages": 4
    }
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 21.5 GET `/admin/distribution/statistics` — 分成统计（管理端）

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/admin/distribution/statistics?startDate=2026-06-01&endDate=2026-06-30&propertyCompanyId=pc_demo001" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：


| 参数                  | 类型       | 必填   | 默认值       | 说明                |
| ------------------- | -------- | ---- | --------- | ----------------- |
| `startDate`         | `String` | ✅    |           | 开始日期 `yyyy-MM-dd` |
| `endDate`           | `String` | ✅    |           | 结束日期 `yyyy-MM-dd` |
| `dimension`         | `String` | ❌    | `summary` | 统计维度              |
| `propertyCompanyId` | `String` | 超管必填 |           | 物业公司ID            |


**成功响应**（同 §21.2，`DistributionStatsResponse`，字段表见 §21.2）：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "summary": {
      "productAmount": 125000.00,
      "totalOrderAmount": 125800.00,
      "totalDistributableAmount": 15000.00,
      "merchantGoodsAmount": 100000.00,
      "merchantDeliverySubsidyAmount": 2000.00,
      "merchantAmount": 98000.00,
      "platformAmount": 1500.00,
      "platformDeliveryAmount": 1000.00,
      "courierAmount": 9000.00,
      "propertyAmount": 4500.00,
      "managementPoolAmount": 10500.00,
      "coordinatorAmount": 4200.00,
      "sectorLeaderAmount": 3150.00,
      "individualLeaderAmount": 3150.00,
      "buildingLeaderAmount": 300.00,
      "deficitAmount": 0.00,
      "reversalAmount": 0.00
    },
    "byProperty": [
      { "propertyCompanyId": "pc_demo001", "propertyName": "演示物业公司", "amount": 15000.00 }
    ],
    "byCoordinator": [
      { "coordinatorId": "coo_demo001", "name": "统筹负责人A", "amount": 10500.00 }
    ]
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 21.6 钱包流水账本 `wallet_flows`（规范§7.4 / §9.5 / §10.5）

**说明**：每次分账结算、配送结算、取消/退款冲正都在**同一事务**内写入一条不可变钱包流水（`wallet_flows` 表）。
余额口径仍由订单/配送/分账状态实时聚合（与既有体系一致），本表作为**官方审计账本**；流水不删除、不覆盖，冲正通过负数流水完成。

**表字段**：

| 字段 | 说明 |
| --- | --- |
| `ownerType` | 主体类型：`merchant`/`platform`/`property`/`coordinator`/`sector_leader`/`individual_leader`/`courier` |
| `ownerId` | 主体 ID（公司固定为 `platform`） |
| `orderId` / `deliveryId` / `distributionRecordId` | 关联订单/配送单/分账记录 |
| `entryType` | 入账/冲正类型（下表） |
| `amount` | 正数入账，负数冲正/扣减 |
| `balanceType` | `pending`/`withdrawable`/`frozen`/`receivable`（当前统一 `withdrawable`） |
| `idempotencyKey` | 幂等键，唯一约束保证不重复入账 |
| `refRecordId` | 冲正流水关联的原流水 ID |

**entryType 取值**：

```text
merchant_goods_income       商家商品收入（= merchantGoodsShare）
merchant_delivery_subsidy   商家承担的配送补贴（负流水）
platform_product_share      公司商品平台盘收入（= platformShare）
platform_delivery_share     公司配送收入（= platformDeliveryShare）
property_product_share      物业商品平台盘收入（= propertyShare）
coordinator_share           统筹实得
sector_leader_share         板块负责人实得
individual_leader_share     个体负责人实得
courier_delivery_income     配送员收入（= courierEarning，配送完成后入账）
merchant_delivery_subsidy   商家承担的配送补贴（负流水）
delivery_subsidy_cost       物业/公司活动承担配送补贴时的成本（负流水，承担方 property/platform，§2.5）
cost_deficit                成本击穿（负流水，承担方 property/platform）
distribution_reversal       分账冲正（负流水）
```

**幂等键（§9.5）**：

```text
distribution:settle:{orderId}:{entryType}:{ownerType}:{ownerId}  分账结算（§9.4）
delivery:settle:{deliveryId}                                     配送结算（§9.3）
distribution:reverse:{orderId}:{entryType}:{ownerType}:{ownerId} 冲正（§10.5）
```

**入账时点**：

- **订单完成（确认收货 / 超时自动确认）** → 生成分成记录时：商家商品收入、商家配送补贴扣减、公司/物业/管理盘三级份额、公司配送收入（§9.2）；物业币是已收现金的支付工具，不再计入成本击穿；
- 配送完成 → 配送员收入 `courier_delivery_income`（§9.3）；
- 取消/退款 → 对原流水逐条生成负数 `distribution_reversal`，不删除原流水（§10.5）。

---



## 22. 商家积分模块 (Merchant Points)



### 22.1 POST `/merchant/points/purchase` — 商家购买积分

**Auth**: Required  
**Roles**: `merchant`

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/merchant/points/purchase \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "pointAmount": 1000
  }'
```

**说明（2026-08-01 调整）**：`payAmount`（支付金额）不再作为入参，由后端按商家积分兑换比自动计算：`payAmount = pointAmount / 积分兑换比（1元=X积分）`，例如兑换比 100 时 1000 积分 = 10.00 元。前端仅需传 `pointAmount`，展示支付金额取后端返回值，避免金额被篡改；兑换比在商家挂接配置 `merchant_property_configs.point_exchange_rate` 中调整。

---



### 22.2 GET `/merchant/points/purchases` — 购买记录

**Auth**: Required  
**Roles**: `merchant`

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/merchant/points/purchases?page=1&pageSize=20&auditStatus=approved&sort=-createdAt" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：


| 参数            | 类型       | 必填  | 默认值          | 说明                                    |
| ------------- | -------- | --- | ------------ | ------------------------------------- |
| `page`        | `int`    | ❌   | `1`          | 页码                                    |
| `pageSize`    | `int`    | ❌   | `20`         | 每页条数                                  |
| `auditStatus` | `String` | ❌   |              | `pending_audit`/`approved`/`rejected` |
| `startDate`   | `String` | ❌   |              | 开始日期 `yyyy-MM-dd`                     |
| `endDate`     | `String` | ❌   |              | 结束日期 `yyyy-MM-dd`                     |
| `sort`        | `String` | ❌   | `-createdAt` | 排序                                    |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "mpp_demo001",
        "merchantId": "mch_demo001",
        "merchantName": "万达超市",
        "pointAmount": 1000,
        "payAmount": 10.00,
        "status": "approved",
        "auditRemark": "审核通过",
        "createdAt": "2026-07-01 10:00:00",
        "auditedAt": "2026-07-01 12:00:00"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 5,
      "totalPages": 1
    }
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 22.3 POST `/merchant/points/grant` — 商家赠送积分

**Auth**: Required  
**Roles**: `merchant`

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/merchant/points/grant \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "residentId": "res_b7m9x1",
    "pointAmount": 50,
    "description": "消费赠送"
  }'
```

**请求示例（按手机号定位住户，2026-08 推荐）**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/merchant/points/grant \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "phone": "13800000000",
    "pointAmount": 50,
    "description": "消费赠送"
  }'
```

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `phone` | `String` | ❌（二选一） | 住户手机号，按「手机号 + 当前物业」定位住户（推荐，无需前端持有 residentId） |
| `residentId` | `String` | ❌（二选一） | 住户ID（历史兼容，仍支持） |
| `pointAmount` | `Integer` | ✅ | 赠送积分数量（至少1） |
| `description` | `String` | ❌ | 描述，默认"商家线下消费返积分" |

**业务规则**：
- `phone` 与 `residentId` **至少传一个**；两者都传时以 `phone` 为准
- 手机号在该物业下未找到住户 / 住户与商家不属于同一物业时返回错误
- 商家无需选择购买记录，系统自动从该商家所有已审核通过的购买记录中按时间顺序扣减积分
- 商家积分总余额 = 所有已审核通过的购买记录的 `remainingPoints` 之和
- 该余额同时用于商家积分消费（如 §59.2 用积分支付广告位）：按积分兑换比将应付金额折算为积分后，按购买记录时间顺序（FIFO）扣减，余额不足返回 `94002`
- 余额不足时返回错误

**错误响应**：


| 错误码     | HTTP | 说明       |
| ------- | ---- | -------- |
| `94002` | 400  | 商家积分余额不足 |


---



### 22.4 GET `/merchant/points/quote` — 积分报价

> ⚠️ **【当前未实现 · 2026-10-27 核对代码】** 后端不存在 `GET /merchant/points/quote`。积分兑换比可读取商家挂接配置 `point_exchange_rate`，由前端本地计算展示，或后端补实现后再联调。以下为原设计文档，保留备查。
<a id="224-积分报价"></a>

**Auth**: Required  
**Roles**: `merchant`

> **（2026-08 新增）**：按商家积分兑换比计算「`payAmount` 元可购多少积分」，仅前端展示用，前端不算公式；兑换比来源：商家挂接配置 `merchant_property_configs.point_exchange_rate`，缺省 `100`（100积分=1元）。

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/merchant/points/quote?payAmount=100" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：

| 参数         | 类型         | 必填  | 校验           | 说明     |
| ---------- | ---------- | --- | ------------ | ------ |
| `payAmount` | `BigDecimal` | ✅   | 必须 > 0        | 支付金额（元） |

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "pointAmount": 9500,
    "payAmount": 100
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**计算口径**：`pointAmount = round(payAmount × 积分兑换比)`（四舍五入取整）。

---



## 23. 商家提现模块 (Merchant Withdrawals)



### 23.1 POST `/merchant/withdrawals` — 商家提现申请

**Auth**: Required  
**Roles**: `merchant`、`activity_leader`、`technician`

> **说明**：组长/技工分别按角色自动定位到 `merchant_source=group_leader` / `merchant_source=technician` 的店铺进行提现。技工可提现金额 = 该技工作为 `provider_id` 的已完成 `service_orders` 金额合计 - 已提现金额。

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/merchant/withdrawals \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "amount": 500.00,
    "source": "legacy"
  }'
```

> **2026-10-29 拆账说明**：标准商家本入口只结算「平台内部现金」（直连代收）；服务商/电商渠道的现金已由微信结算到商家特约商户号，不在本入口重复出款。
> 纯物业币/混合支付订单中商家应得、但特约商户号没有现金的部分，走 `GET/POST /merchant/coin-receivables`（§109）。

**错误响应**：


| 错误码     | HTTP | 说明        |
| ------- | ---- | --------- |
| `20004` | 403  | 权限不足（角色非 `merchant`/`activity_leader`/`technician`） |
| `60001` | 404  | 商家店铺不存在 |
| `60002` | 409  | 商家店铺入驻审核中 |
| `97001` | 404  | 物业公司不存在 |
| `10002` | 400  | 参数格式错误 |

> **说明**：申请时即校验对应来源的可结算余额（含 pending/approved 占用），管理端审核通过时再次复核；审核完成只登记状态与打款凭证，不做自动打款。


---



### 23.2 GET `/merchant/withdrawals` — 提现记录

**Auth**: Required  
**Roles**: `merchant`、`activity_leader`、`technician`

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/merchant/withdrawals?page=1&pageSize=20&auditStatus=completed&sort=-createdAt" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：


| 参数            | 类型       | 必填  | 默认值          | 说明                                                |
| ------------- | -------- | --- | ------------ | ------------------------------------------------- |
| `page`        | `int`    | ❌   | `1`          | 页码                                                |
| `pageSize`    | `int`    | ❌   | `20`         | 每页条数                                              |
| `auditStatus` | `String` | ❌   |              | `pending`/`approved`/`rejected`/`completed` |
| `startDate`   | `String` | ❌   |              | 开始日期 `yyyy-MM-dd`                                 |
| `endDate`     | `String` | ❌   |              | 结束日期 `yyyy-MM-dd`                                 |
| `sort`        | `String` | ❌   | `-createdAt` | 排序                                                |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "mw_demo001",
        "merchantId": "mch_demo001",
        "merchantName": "万达超市",
        "amount": 500.00,
        "source": "legacy",
        "feeRate": 0.006,
        "feeAmount": 3.00,
        "actualAmount": 497.00,
        "status": "completed",
        "createdAt": "2026-07-01 10:00:00",
        "completedAt": "2026-07-02 15:00:00"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 3,
      "totalPages": 1
    }
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



## 24. 物业币兑换模块 (Coin Withdrawals)



### 24.1 POST `/coin-withdrawals` — 物业币兑换申请

**Auth**: Required  
**Roles**: `merchant`

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/coin-withdrawals \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "coinAmount": 100.00
  }'
```

**错误响应**：


| 错误码     | HTTP | 说明      |
| ------- | ---- | ------- |
| `50001` | 400  | 物业币余额不足 |
| `93003` | 403  | 物业币已冻结  |
| `94004` | 400  | 兑换金额不合法 |


---



### 24.2 GET `/coin-withdrawals` — 兑换记录

**Auth**: Required  
**Roles**: `merchant`, `platform_admin`, `property_admin`

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/coin-withdrawals?page=1&pageSize=20&auditStatus=completed&sort=-createdAt" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：


| 参数            | 类型       | 必填  | 默认值          | 说明                                                |
| ------------- | -------- | --- | ------------ | ------------------------------------------------- |
| `page`        | `int`    | ❌   | `1`          | 页码                                                |
| `pageSize`    | `int`    | ❌   | `20`         | 每页条数                                              |
| `merchantId`  | `String` | ❌   |              | 商家ID筛选                                            |
| `auditStatus` | `String` | ❌   |              | `pending_audit`/`approved`/`rejected`/`completed` |
| `startDate`   | `String` | ❌   |              | 开始日期 `yyyy-MM-dd`                                 |
| `endDate`     | `String` | ❌   |              | 结束日期 `yyyy-MM-dd`                                 |
| `sort`        | `String` | ❌   | `-createdAt` | 排序                                                |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "cw_demo001",
        "residentId": "res_b7m9x1",
        "residentName": "张先生",
        "coinAmount": 100.00,
        "exchangeAmount": 100.00,
        "status": "completed",
        "createdAt": "2026-07-01 10:00:00",
        "completedAt": "2026-07-02 15:00:00"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 2,
      "totalPages": 1
    }
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



## 25. 特惠推送模块 (Special Offers)



### 25.1 POST `/special-offers` — 创建特惠推送

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`, `sector_leader`, `coordinator`

> **2026-09-08 变更（前端问题：统筹创建特惠 403）**
> §25.1 / §25.4 / §25.5 的写操作此前仅放行 `property_admin` / `platform_admin`，与文档 Roles 不一致。
> 现 `coordinator` 亦可创建 / 更新 / 删除**本物业**特惠，前提是持有 `promotion.push` 权限码
> （统筹标准权限包默认包含）。越权操作其他物业返回 `404 30010`「特惠不存在」；
> 未持有该权限码返回 `403 20004`。
>
> **2026-09-09 变更（v8.4，前端问题：板块负责人创建/删除特惠 403）**
> - `sector_leader` 此前与 `coordinator` 共用权限码分支，最终落到 `requireAdmin()`（只放行
>   `property_admin` / `platform_admin`），导致板块负责人发/改/删特惠一律 `403 20004`。
> - 现 **`sector_leader` 按角色放行，不要求持有 `promotion.push`**（文档 Roles 从未要求）。
>   数据范围收敛到**本人物业公司**（`GET/PUT/DELETE` 只能操作本物业记录，越权返回 `404 90001`「特惠不存在」）。
> - `GET /special-offers/{id}` 详情的 Roles 同步补上 `sector_leader`（此前仅
>   `property_admin`/`platform_admin`/`coordinator`，板块负责人查看自己发的特惠会 403）。
> - §25.7 `publish` / §25.8 `unpublish` 的 Roles 同步补上 `sector_leader`。
> - `publisherRole` 可为 `sector_leader`；住户端可见性不受发布者角色影响（§25.6）。
>
> **2026-09-08 第二轮（v8.3，前端问题：统筹特惠移动端不可见）**
> - **C 端可见性**：创建时 `status` 决定住户端是否可见。默认 `draft`（**仅管理端可见**）；
>   要住户端看到必须传 `published`，或创建后调用 §25.7 `POST /special-offers/{id}/publish`。
> - **状态归一化**：`草稿`→`draft`；`已发布`→`published`；`待发布` / `active` → `published`（历史别名，
>   系统不存在独立的「待发布」态）；`已结束`→`ended`；`已归档`/`已删除`→`archived`。
> - **目标类型归一化**：`全体业主`→`all`、`指定小区`→`building`、`指定商户`→`role`、`指定标签`→`tag`；
>   英文枚举亦可直接传。
> - **时间格式**：`startTime`/`endTime` 支持 ISO8601、`yyyy-MM-dd HH:mm:ss`、`yyyy-MM-dd`
>   （结束时间传纯日期按当天 `23:59:59`）。
> - **配额字段**：新增 `minConsumption` / `totalQuota` / `perUserQuota`（`usedQuota` 为只读，当前恒为 0）。

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/special-offers \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "title": "周末特惠：超市满50减5",
    "content": "周末购物满50元立减5元",
    "targetType": "全体业主",
    "targetTags": "新业主,首套房",
    "merchantId": "mch_demo001",
    "coverUrl": "https://cdn.example.com/special-offers/so_demo001.jpg",
    "discountInfo": "满100减20",
    "minConsumption": 50.00,
    "totalQuota": 100,
    "perUserQuota": 1,
    "startTime": "2026-07-04T00:00:00.000Z",
    "endTime": "2026-07-05T23:59:59.000Z",
    "status": "草稿"
  }'
```

**请求体**：


| 字段            | 类型       | 必填  | 校验                    | 说明                                          |
| ------------- | -------- | --- | --------------------- | ------------------------------------------- |
| `title`       | `String` | ✅   | `@NotBlank @Size(max=200)` | 活动标题                                        |
| `content`     | `String` | ✅   | `@NotBlank`           | 活动内容                                        |
| `targetType`  | `String` | ✅   | `@NotBlank`           | 目标类型（可传中文，后端自动映射）：`全体业主`→`all`、`指定小区`→`building`、`指定商户`→`role`；亦可直接传英文枚举 |
| `targetTags`  | `String` | ❌   |                       | 目标标签，逗号分隔                                    |
| `communityId` | `String` | ❌   |                       | 小区ID（targetType为指定小区时传）                       |
| `merchantId`  | `String` | ❌   |                       | 商家ID（targetType为指定商户时传）                       |
| `coverUrl`    | `String` | ❌   | `@Size(max=500)`     | 封面图URL                                        |
| `discountInfo`| `String` | ❌   |                       | 优惠信息，如"满100减20"                                |
| `minConsumption`| `BigDecimal` | ❌   |                       | 最低消费门槛（不传则无门槛）                             |
| `totalQuota` | `Integer` | ❌   | `@Min(1)`             | 总配额（不传则不限）                                    |
| `perUserQuota`| `Integer` | ❌   | `@Min(1)`             | 每人限领数量（不传则不限）                               |
| `startTime`   | `String` | ❌   |                       | 开始时间，支持多种格式：ISO8601（`2026-07-08T00:00:00.000Z`）、`yyyy-MM-dd HH:mm:ss`（如 `2026-07-08 00:00:00`）、纯日期 `yyyy-MM-dd`（如 `2026-07-08`，默认当天 00:00:00） |
| `endTime`     | `String` | ❌   |                       | 结束时间，支持多种格式：ISO8601（`2026-07-15T23:59:59.000Z`）、`yyyy-MM-dd HH:mm:ss`（如 `2026-07-15 23:59:59`）、纯日期 `yyyy-MM-dd`（如 `2026-07-15`，默认当天 23:59:59） |
| `status`      | `String` | ❌   |                       | 状态（可传中文）：`草稿`→`draft`、`待发布`→`active`、`已发布`→`published`、`已结束`→`ended`；默认 `draft` |


**最简请求示例**（板块负责人工作台）：

```json
{
  "title": "标题",
  "content": "活动内容",
  "targetType": "全体业主",
  "startTime": "2026-07-08T00:00:00.000Z",
  "endTime": "2026-07-15T23:59:59.000Z"
}
```

> **日期格式**：`startTime`/`endTime` 支持 ISO8601（如 `2026-07-08T00:00:00.000Z`）、`yyyy-MM-dd HH:mm:ss`（如 `2026-07-08 00:00:00`）、纯日期 `yyyy-MM-dd`（如 `2026-07-08`）三种格式。


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "so_demo001",
    "title": "周末特惠：超市满50减5",
    "content": "周末购物满50元立减5元",
    "targetType": "全体业主",
    "targetTags": ["新业主", "首套房"],
    "publisherId": "res_admin01",
    "publisherName": "张经理",
    "publisherRole": "property_admin",
    "propertyCompanyId": "pc_demo001",
    "communityId": null,
    "merchantId": "mch_demo001",
    "merchantName": "万达超市",
    "coverUrl": "https://cdn.example.com/special-offers/so_demo001.jpg",
    "discountInfo": "满100减20",
    "minConsumption": 50.00,
    "totalQuota": 100,
    "perUserQuota": 1,
    "usedQuota": 0,
    "startTime": "2026-07-04T00:00:00.000Z",
    "endTime": "2026-07-05T23:59:59.000Z",
    "status": "已发布",
    "createdAt": "2026-07-01T15:23:33.000Z",
    "updatedAt": "2026-07-01T15:23:33.000Z"
  },
  "timestamp": "2026-07-01T15:23:33.000Z"
}
```

---



### 25.2 GET `/special-offers` — 特惠推送列表

**Auth**: Required（路径本身允许匿名，但匿名/无角色会返回 `403 20004`）  
**Roles**: `property_admin`, `platform_admin`, `sector_leader`, `coordinator`, **`resident`**

> **2026-09-08 变更（前端问题：统筹特惠移动端不可见）**
> - 新增 `resident` 放行。住户调用时**按 §25.6 C 端可见规则**过滤，
>   并**忽略** `targetType` / `status` 两个查询参数（C 端固定「本物业 + published + 时间窗内」）。
> - 管理端（`property_admin` / `platform_admin` / `sector_leader` / `coordinator`）行为不变：
>   返回本物业全量，可按 `targetType` / `status` 过滤（参数支持中文与英文枚举）。
> - 响应新增 `publisherName` / `merchantName` / `minConsumption` / `totalQuota` /
>   `perUserQuota` / `usedQuota` / `statusLabel`。

**Query 参数**：


| 参数         | 类型       | 必填  | 默认值 | 说明                                    |
| ---------- | -------- | --- | ---- | ------------------------------------- |
| `page`     | `int`    | ❌   | `1`  | 页码                                    |
| `pageSize` | `int`    | ❌   | `20` | 每页条数（最大100）                             |
| `targetType`| `String`| ❌   |      | 目标类型：`all`/`building`/`role`/`tag`，或中文`全体业主`/`指定小区`/`指定商户`/`指定标签`（**管理端有效**） |
| `status`   | `String` | ❌   |      | 状态：`draft`/`published`/`ended`/`archived`，或中文`草稿`/`已发布`/`已结束`/`已归档`（**管理端有效**） |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "so_demo001",
        "title": "周末特惠：超市满50减5",
        "content": "周末购物满50元立减5元",
        "targetType": "全体业主",
        "targetTags": ["新业主", "首套房"],
        "publisherId": "res_admin01",
        "publisherName": "张经理",
        "publisherRole": "property_admin",
        "merchantId": "mch_demo001",
        "merchantName": "万达超市",
        "coverUrl": "https://cdn.example.com/special-offers/so_demo001.jpg",
        "discountInfo": "满100减20",
        "minConsumption": 50.00,
        "totalQuota": 100,
        "perUserQuota": 1,
        "usedQuota": 30,
        "startTime": "2026-07-04T00:00:00.000Z",
        "endTime": "2026-07-05T23:59:59.000Z",
        "status": "已发布",
        "createdAt": "2026-07-01T15:23:33.000Z"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 1,
      "totalPages": 1
    }
  },
  "timestamp": "2026-07-01T15:23:33.000Z"
}
```

---



### 25.3 GET `/special-offers/{id}` — 特惠推送详情

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`, `coordinator`, **`resident`**

> **2026-09-08 变更**：住户可查看详情，但**仅限本物业 + `status=published` + 时间窗内 + 目标人群匹配**，
> 不满足则返回 `404 90001`「特惠不存在」（与越权一致，不泄露存在性）。

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "so_demo001",
    "title": "周末特惠：超市满50减5",
    "content": "周末购物满50元立减5元",
    "targetType": "全体业主",
    "targetTags": ["新业主", "首套房"],
    "publisherId": "res_admin01",
    "publisherName": "张经理",
    "publisherRole": "property_admin",
    "propertyCompanyId": "pc_demo001",
    "communityId": null,
    "merchantId": "mch_demo001",
    "merchantName": "万达超市",
    "coverUrl": "https://cdn.example.com/special-offers/so_demo001.jpg",
    "discountInfo": "满100减20",
    "minConsumption": 50.00,
    "totalQuota": 100,
    "perUserQuota": 1,
    "usedQuota": 30,
    "startTime": "2026-07-04T00:00:00.000Z",
    "endTime": "2026-07-05T23:59:59.000Z",
    "status": "已发布",
    "createdAt": "2026-07-01T15:23:33.000Z",
    "updatedAt": "2026-07-01T15:23:33.000Z"
  },
  "timestamp": "2026-07-01T15:23:33.000Z"
}
```

---



### 25.4 PUT `/special-offers/{id}` — 更新特惠推送

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`, `sector_leader`, `coordinator`

**请求示例**：

```bash
curl -X PUT https://test.yuanxusoftware.top/api/v1/special-offers/so_demo001 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "title": "周末特惠：超市满100减10",
    "content": "周末购物满100元立减10元",
    "targetType": "全体业主",
    "discountInfo": "满100减10"
  }'
```

**请求体**：同创建接口，所有字段可选。

**成功响应**：更新后的特惠推送详情（同详情接口）

---



### 25.5 DELETE `/special-offers/{id}` — 删除特惠推送

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`, `sector_leader`, `coordinator`

**成功响应**：返回删除后的特惠推送详情（status变为archived）

---

### 25.6 C 端（住户）可见规则 — 必读

**适用接口**：`GET /special-offers`（resident Token）、`GET /special-offers/my`、`GET /special-offers/{id}`（resident Token）

| 条件 | 规则 |
|------|------|
| 物业公司 | **强制** `property_company_id` = 当前住户所属物业公司（由 Token 解析） |
| 状态白名单 | **仅 `published`**。`draft`（草稿）/`ended`（已结束）/`archived`（已归档）住户端一律不可见 |
| 时间窗 | `startTime` 为空 或 `startTime ≤ now`；`endTime` 为空 或 `endTime ≥ now`。两者都为空视为长期有效 |
| `targetType=all`（全体业主） | 本物业全体住户可见 |
| `targetType=building`（指定小区） | `communityId` 为空 → 本物业可见；非空 → 仅该小区住户可见 |
| `targetType=role`（指定商户） | `merchantId` 为空 → 本物业可见；非空 → 仅**在该商户有过已完成订单**的住户可见 |
| `targetType=tag`（指定标签） | 当前无住户标签体系，**按本物业全体可见**处理（后续接入标签后再收紧） |
| `publisherRole` | **不额外过滤**。统筹（`coordinator`）、物业、平台、板块负责人发布的记录一视同仁 |
| 排序 | `createdAt` 倒序 |

**结论（答复前端疑问）**：

1. 统筹创建后移动端看不到，最常见原因是 **`status` 仍为 `draft`**。请提交 `status: "published"`，或创建后调用 `POST /special-offers/{id}/publish`。
2. 已发布但 `endTime` 已过 → C 端不可见（不会返回）。
3. 已发布但 `startTime` 晚于当前时间 → C 端暂不可见（未到开始时间）。
4. 管理端列表**不做**上述过滤，草稿在管理端仍可见；请在草稿旁标注「草稿不会在住户端展示」。

**验收用例**：

| # | 操作 | 期望 |
|---|------|------|
| A | 统筹创建 `status=published`，时间覆盖当前，`targetType=all` | 同物业住户 `GET /special-offers` 可见该 `id` |
| B | 同上但 `status=draft` | 住户端不可见；管理端列表可见 |
| C | `published` 但 `endTime` 已过 | 住户端不可见 |
| D | `targetType=role`（指定商户）且住户未消费该商户 | 住户端不可见 |
| E | 住户 Token 调 `GET /special-offers` | `code=0`，list 含用例 A 的 `id` |

**实际请求示例（用例 A/E，住户 Token）**：

```http
GET /api/v1/special-offers?page=1&pageSize=20
Authorization: Bearer <resident_token>
```

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "so_3bb41000",
        "title": "测试98",
        "content": "测试98",
        "targetType": "all",
        "publisherId": "res_coo01",
        "publisherName": "统筹-张伟",
        "publisherRole": "coordinator",
        "merchantId": null,
        "merchantName": null,
        "minConsumption": null,
        "totalQuota": null,
        "perUserQuota": null,
        "usedQuota": 0,
        "startTime": "2026-09-07 22:53:00",
        "endTime": "2026-09-14 22:53:00",
        "status": "published",
        "statusLabel": "已发布",
        "createdAt": "2026-09-08 06:53:51"
      }
    ],
    "pagination": { "page": 1, "pageSize": 20, "total": 1, "totalPages": 1 }
  }
}
```

---

### 25.6.1 GET `/special-offers/my` — 我的特惠（住户端专属入口）

**Auth**: Required  
**Roles**: `resident`

等价于住户 Token 调用 `GET /special-offers`，按 §25.6 可见规则过滤，参数仅 `page` / `pageSize`。

---

### 25.7 POST `/special-offers/{id}/publish` — 发布特惠

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`, `sector_leader`, `coordinator`（需 `promotion.push`）

> 2026-09-09：`sector_leader` 补入 Roles，与「板块负责人工作台 → 板块特惠 → 立即发布」按钮对齐。

将 `draft` / `ended` 状态的特惠置为 `published`（住户端立即可见）。
已归档（`archived`）记录返回 `400 10002`「已归档的特惠不能发布，请新建」。

**成功响应**：更新后的特惠详情（`status = published`）

---

### 25.8 POST `/special-offers/{id}/unpublish` — 下架特惠

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`, `sector_leader`, `coordinator`（需 `promotion.push`）

> 2026-09-09：`sector_leader` 补入 Roles，与「板块负责人工作台 → 板块特惠 → 下架」按钮对齐。

将特惠置为 `draft`，住户端不再可见，管理端仍可继续编辑。

**成功响应**：更新后的特惠详情（`status = draft`）

---

---



## 26. 消费过的商家模块 (Resident Merchant)



### 26.1 GET `/residents/me/consumed-merchants` — 消费过的商家列表

**Auth**: Required  
**Roles**: `resident`

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/residents/me/consumed-merchants?page=1&pageSize=20" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：


| 参数         | 类型       | 必填  | 默认值               | 说明     |
| ---------- | -------- | --- | ----------------- | ------ |
| `page`     | `int`    | ❌   | `1`               | 页码     |
| `pageSize` | `int`    | ❌   | `20`              | 每页条数   |
| `keyword`  | `String` | ❌   |                   | 商家名称搜索 |
| `sort`     | `String` | ❌   | `-lastConsumedAt` | 排序     |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "merchantId": "mch_demo001",
        "merchantName": "万达超市",
        "merchantCoverUrl": "https://cdn.example.com/merchant/mch_demo001.jpg",
        "category": "外卖",
        "totalOrders": 15,
        "totalConsumption": 750.00,
        "lastConsumedAt": "2026-06-28 14:30:00",
        "lastOrderId": "ord_demo015"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 5,
      "totalPages": 1
    }
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



## 27. 投票记录模块 (My Votes)



### 27.1 GET `/opinion-boxes/my-votes` — 我的投票记录

**Auth**: Required  
**Roles**: `resident`

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/opinion-boxes/my-votes?page=1&pageSize=20" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：


| 参数         | 类型       | 必填  | 默认值        | 说明   |
| ---------- | -------- | --- | ---------- | ---- |
| `page`     | `int`    | ❌   | `1`        | 页码   |
| `pageSize` | `int`    | ❌   | `20`       | 每页条数 |
| `sort`     | `String` | ❌   | `-votedAt` | 排序   |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "voteRecordId": "vr_demo001",
        "opinionBoxId": "ob_demo001",
        "opinionBoxTitle": "是否同意增设健身器材",
        "voteOption": "support",
        "familyId": "fam_demo001",
        "opinionBoxStatus": "active",
        "resultPublished": false,
        "votedAt": "2026-07-01 15:23:33"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 3,
      "totalPages": 1
    }
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



## 28. 家庭贡献模块 (Family Contributions)



### 28.1 GET `/families/me/contributions` — 家庭成员贡献比例

**Auth**: Required  
**Roles**: `resident`

**请求示例**：

```bash
curl -X GET https://test.yuanxusoftware.top/api/v1/families/me/contributions \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "familyId": "fam_demo001",
    "familyName": "张家",
    "totalPointContribution": 1200,
    "totalCoinContribution": 250.00,
    "members": [
      {
        "residentId": "res_b7m9x1",
        "name": "张先生",
        "relation": "owner",
        "pointBalance": 500,
        "familyPointBalance": 1200,
        "coinBalance": 99.50,
        "pointContributionRate": 0.42,
        "coinContributionRate": 0.40,
        "totalOrders": 15,
        "totalConsumption": 750.00
      },
      {
        "residentId": "res_xxxxxx",
        "name": "李女士",
        "relation": "spouse",
        "pointBalance": 700,
        "coinBalance": 150.50,
        "pointContributionRate": 0.58,
        "coinContributionRate": 0.60,
        "totalOrders": 20,
        "totalConsumption": 1200.00
      }
    ],
    "updatedAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



## 29. 活动组订阅状态模块 (Subscription Status)



### 29.1 GET `/activity-groups/{id}/subscription-status` — 订阅状态查询

**Auth**: Required  
**Roles**: `resident`, `activity_leader`, `coordinator`, `sector_leader`, `individual_leader`, `property_admin`, `platform_admin`

**请求示例**：

```bash
curl -X GET https://test.yuanxusoftware.top/api/v1/activity-groups/ag_demo001/subscription-status \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "subscribed": true,
    "subscriptionType": "monthly",
    "startDate": "2026-06-15",
    "endDate": "2026-07-15",
    "status": "active",
    "pricingTierId": "agt_xxx",
    "tierName": "一级",
    "tierCode": "level_1",
    "amount": 300.00
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**响应字段说明**：

| 字段               | 类型           | 说明                                  |
| ---------------- | ------------ | ----------------------------------- |
| `subscribed`     | `Boolean`    | 当前是否在有效期内已订阅；`pending_payment`（微信待支付）为 `false`，请轮询至 `active` |
| `subscriptionType` | `String`   | 订阅类型：`monthly`/`quarterly`/`yearly` |
| `startDate`      | `String`     | 订阅开始日期 `yyyy-MM-dd`                |
| `endDate`        | `String`     | 订阅结束日期 `yyyy-MM-dd`                |
| `status`         | `String`     | 订阅状态：`pending_payment`(待支付)/`active`/`expired`/`cancelled` |
| `pricingTierId`  | `String`     | 课时价格ID，无课时为 `null`                |
| `tierName`       | `String`     | 课时名称，无课时为 `null`                  |
| `tierCode`       | `String`     | 课时编码，无课时为 `null`                  |
| `amount`         | `BigDecimal` | 实付金额                                |

---



## 30. 邻居搜索模块 (Neighbor Search)



### 30.1 GET `/messages/neighbors` — 搜索邻居

**Auth**: Required  
**Roles**: `resident`

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/messages/neighbors?keyword=张&page=1&pageSize=20" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：


| 参数            | 类型       | 必填  | 默认值  | 说明           |
| ------------- | -------- | --- | ---- | ------------ |
| `keyword`     | `String` | ❌   |      | 搜索关键词（姓名/楼栋/单元/房号，不按「几层」解析） |
| `communityId` | `String` | ❌   |      | 小区ID（默认当前小区） |
| `building`    | `String` | ❌   |      | 楼栋筛选         |
| `unit`        | `String` | ❌   |      | 单元筛选（2026-08 新增） |
| `room`        | `String` | ❌   |      | 房号筛选（2026-08 新增） |
| `page`        | `int`    | ❌   | `1`  | 页码           |
| `pageSize`    | `int`    | ❌   | `20` | 每页条数         |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "res_neighbor01",
        "name": "张*华",
        "avatarUrl": "https://cdn.example.com/avatar/res_neighbor01.jpg",
        "building": "3栋",
        "floor": "5",
        "unit": "5单元",
        "room": "501",
        "communityName": "演示小区",
        "chatStatus": "available"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 1,
      "totalPages": 1
    }
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**业务规则**：

- 搜索结果仅显示同一小区的邻居
- 筛选条件为 `communityId + building + unit + room`，**不使用楼层 `floor` 过滤**；关键词匹配姓名/楼栋/单元/房号（拼接地址不含楼层），不按「几层」解析
- 姓名中间字用 `*` 隐藏，保护隐私
- `chatStatus`（聊天状态，按优先级判定）：
  - `blocked`：与对方存在屏蔽关系（我屏蔽了对方，或对方屏蔽了我）
  - `chatted`：与对方已建立会话（历史上有过私聊记录），可直接进入会话继续聊
  - `limited`：尚未聊过，且今日「发起新会话」次数已用完
  - `available`：尚未聊过，且今日仍可发起新会话

---



### 30.2 GET `/messages/conversations` — 会话列表

**Auth**: Required  
**Roles**: `resident`、`activity_leader`、`merchant`、`technician`、`courier`、`coordinator`、`sector_leader`、`individual_leader`、`property_admin`、`platform_admin`

获取当前用户聊天会话列表，按对方聚合（邻居 / 官方 / 服务需求 / 商家广告），显示最后一条消息与未读数，按最后消息时间倒序。默认返回全部类型；可用 `conversationType` 按 Tab 筛选。

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/messages/conversations?page=1&pageSize=20" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."

# 仅官方会话
curl -X GET "https://test.yuanxusoftware.top/api/v1/messages/conversations?conversationType=official" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：


| 参数         | 类型    | 必填  | 默认值  | 说明   |
| ---------- | ----- | --- | ---- | ---- |
| `page`     | `int` | ❌   | `1`  | 页码   |
| `pageSize` | `int` | ❌   | `20` | 每页条数 |
| `conversationType` | `String` | ❌   |  | `neighbor`/`official`/`service_request`/`merchant_ad` |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "peerId": "res_k3p5n2",
        "peerName": "李*华",
        "peerAvatarUrl": "https://cdn.example.com/avatar/res_k3p5n2.jpg",
        "conversationType": "neighbor",
        "officialSenderType": null,
        "officialDisplayName": null,
        "relatedTaskId": null,
        "lastMessage": "好的，明天见",
        "lastMessageTime": "2026-07-07T06:23:33.000Z",
        "lastMessageFromMe": true,
        "unreadCount": 0
      },
      {
        "peerId": "official_property_pc_demo001",
        "peerName": "幸福物业",
        "peerAvatarUrl": null,
        "conversationType": "official",
        "officialSenderType": "property",
        "officialDisplayName": "幸福物业",
        "relatedTaskId": "dmt_demo001",
        "lastMessage": "明日上午9点至12点停水…",
        "lastMessageTime": "2026-07-07T08:00:00.000Z",
        "lastMessageFromMe": false,
        "unreadCount": 1
      }
    ],
    "pagination": { "page": 1, "pageSize": 20, "total": 2, "totalPages": 1 }
  },
  "timestamp": "2026-07-07 14:23:33"
}
```

> **说明**：官方会话 `peerId` 为稳定键 `official_{senderType}_{propertyCompanyId}`（如 `official_property_pc_demo001`）。历史官方消息若 `fromId` 仍为真人 ID，列表会按 `officialSenderType`+物业公司合成 peer；**新发送**的定向消息/公告直达已使用官方 `fromId`。

---



### 30.3 GET `/messages/conversations/{peerId}` — 会话聊天记录

**Auth**: Required  
**Roles**: `resident`、`activity_leader`、`merchant`、`technician`、`courier`、`coordinator`、`sector_leader`、`individual_leader`、`property_admin`、`platform_admin`

获取与指定对方的完整会话记录（分页，按时间倒序，`page=1` 为最新一页）。**打开会话会自动把对方发来的未读消息标记为已读。**  
`peerId` 支持邻居住户（`res_`）或官方会话键（`official_`）。

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/messages/conversations/res_k3p5n2?page=1&pageSize=20" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."

curl -X GET "https://test.yuanxusoftware.top/api/v1/messages/conversations/official_property_pc_demo001?page=1&pageSize=20" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**路径参数**：


| 参数       | 类型       | 必填  | 说明                |
| -------- | -------- | --- | ----------------- |
| `peerId` | `String` | ✅   | 对方会话 ID：`res_` 或 `official_` |


**Query 参数**：


| 参数         | 类型    | 必填  | 默认值  | 说明   |
| ---------- | ----- | --- | ---- | ---- |
| `page`     | `int` | ❌   | `1`  | 页码   |
| `pageSize` | `int` | ❌   | `20` | 每页条数 |


**说明**：返回结构与 `GET /messages` 列表项一致（含 `fromId`/`fromName`/`content`/`readStatus`/`createdAt` 等）。

---



### 30.4 实时消息推送 (WebSocket)

邻居私聊、**业主向技工群发维修求助（扇出副本）**、技工私聊回复业主，在落库的同时会通过 WebSocket 实时推送给接收方。

- **连接端点**：`/api/v1/ws` 或 `/api/v1/ws/v1`（完整 URL：`wss://test.yuanxusoftware.top/api/v1/ws?token=<accessToken>`），握手须在 Query 携带 `token`。
- **鉴权方式**：WebSocket 握手**仅**通过 Query 参数 `token` 传递 **accessToken**（JWT，须含 2 个点）；**勿**在 HTTP Header 传 `Authorization`。`/ws` 相关路径已排除 REST `AuthInterceptor`，避免握手被误拦返回 `20001`/`400`。
- **协议**：STOMP。
- **订阅个人消息队列**：`/user/queue/messages`，收到的对象结构与消息详情一致（含发送者姓名、头像、内容、时间等）。业主群发后，每个技工各自收到自己的扇出副本推送。
- **心跳**：向 `/app/heartbeat` 发送，服务端回推 `/topic/heartbeat.pong`。
- **平台差异**：微信小程序/App 须使用**原生 WebSocket**（`uni.connectSocket`）+ 自行组 STOMP 帧；浏览器 H5 可使用 SockJS + STOMP。`GET /api/v1/ws/info` 仅用于确认 SockJS 服务是否部署，**不等于**小程序原生 WS 已可用。

> 说明：推送失败不影响消息落库，接收方可通过 `GET /messages` 拉取补偿。技工群发请用 `messageType=technician_message&chatType=broadcast`。

---



## 31. 账号注销模块 (Account Deletion)



### 31.1 POST `/residents/me/delete-request` — 申请注销账号

**Auth**: Required  
**Roles**: `resident`

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/residents/me/delete-request \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "reason": "不再居住于此小区",
    "confirmText": "确认注销"
  }'
```

**请求体**：


| 字段            | 类型       | 必填  | 校验               | 说明 |
| ------------- | -------- | --- | ---------------- | ---- |
| `confirmText` | `String` | ✅   | 必须为 `"确认注销"`     | 确认文本 |
| `reason`      | `String` | ❌   | `@Size(max=200)` | 注销原因（DTO 可接收；**当前实现不落库、不回传**） |


**成功响应**（`DeleteAccountResponse`）：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "residentId": "res_b7m9x1",
    "status": "pending",
    "message": "账号注销申请已提交，请在15天冷静期内确认",
    "requestId": "del_demo001",
    "expiresAt": "2026-08-13 15:23:33"
  },
  "timestamp": "2026-07-29 15:23:33"
}
```

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `residentId` | `String` | 住户 ID |
| `status` | `String` | 申请展示态，成功时为 `pending` |
| `message` | `String` | 提示文案 |
| `requestId` | `String` | 注销请求 ID（前缀 `del_`） |
| `expiresAt` | `String` | 冷静期截止时间（本地时间字符串） |

**业务规则**：

- 校验通过后，账号状态立即置为 `pending_deletion`，`deletedAt` 设为 **15 天后**（冷静期截止）
- **当前无撤销注销接口**；冷静期结束后由后续清理流程处理（勿按「随时撤销」对接）
- 以下情况不允许注销：
  - 存在未完成的订单 → `30006`
  - 存在未缴纳的物业费 → `30007`
  - 物业币余额不为 0 → `30008`
  - 是家庭户主且家庭有其他成员 → `30009`

**错误响应**：


| 错误码     | HTTP | 说明 |
| ------- | ---- | ---- |
| `10002` | 400  | `confirmText` 不是「确认注销」等参数错误 |
| `30006` | 409  | 存在未完成的订单 |
| `30007` | 409  | 存在未缴纳的物业费 |
| `30008` | 409  | 物业币余额不为 0 |
| `30009` | 409  | 户主家庭有其他成员 |

> **注意**：`30005` 为「该手机号在当前物业公司下已存在」（见 §2.2 创建住户），**不是**注销错误码。

---


### 31.2 管理端 — 业务角色账号直建 / 直删（商家 / 活动组长 / 技工 / 配送员）

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

管理员可直接为以下四类业务角色创建或删除账号；**物业管理员**与**平台管理员**均可调用本节的直建 / 直删接口（平台管理员可跨物业公司操作，物业管理员受限其管辖物业）。

| 业务角色 | `role` 值 | 创建时自动生成的关联数据 |
| --- | --- | --- |
| 商家 | `merchant` | 门店 `Merchant` + 平台商家 `PlatformMerchant` + 小区绑定，并生成「今日新店」公告 |
| 活动组长 | `activity_leader` | 「{name}的小店」（`group_leader` 小店） |
| 技工 | `technician` | 「{name}的便民小店」（`technician` 小店） |
| 配送员 | `courier` | 仅账号，无关联店铺 |

#### 31.2.1 POST `/admin/role-accounts` — 直接创建业务角色账号

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `role` | `String` | 是 | `merchant` / `activity_leader` / `technician` / `courier` |
| `name` | `String` | 是 | 姓名（≤50） |
| `phone` | `String` | 是 | 手机号（登录账号，同一物业公司下唯一，`^1\d{10}$`） |
| `password` | `String` | 是 | 登录密码，BCrypt 加密存储（6–32） |
| `propertyCompanyId` | `String` | 视角色 | 平台管理员必填；物业管理员可不传（自动使用当前物业） |
| `communityId` | `String` | 否 | 小区 ID |
| `category` | `String` | 否（`merchant` 建议填） | 商家品类，可选值：外卖/周围商家/自营/农家/外卖小吃/团购/酒店民宿/洗浴汗蒸/按摩足疗/美食/外卖小区/看病买药/休闲玩乐 |
| `description` | `String` | 否 | 商家描述 |
| `businessHours` | `String` | 否 | 营业时间，如 `09:00-22:00` |
| `address` | `String` | 否 | 商家地址 |
| `coverUrls` | `List<String>` | 否 | 封面图 URL 列表 |
| `deliveryFee` | `BigDecimal` | 否 | 配送费（默认 0） |
| `commissionRate` | `BigDecimal` | 否 | 抽佣比例（默认 0.1000 = 10%） |
| `pointExchangeRate` | `BigDecimal` | 否 | 积分兑换比（默认 100，即 100 积分 = 1 元） |

**成功响应（CreateRoleAccountResponse）**：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `residentId` | `String` | 新建账号 ID（前缀 `res_`） |
| `name` | `String` | 姓名 |
| `phone` | `String` | 手机号 |
| `role` | `String` | 角色 |
| `propertyCompanyId` | `String` | 归属物业公司 |
| `merchantId` | `String` | 关联店铺 ID（`role=merchant` 时返回，其余角色为空） |
| `status` | `String` | 账号状态，成功时为 `active` |

**业务规则**：
- 同一物业公司下 `phone` 唯一；已存在报 `30005`。
- 账号创建即 `active`，可直接登录。
- 各角色关联店铺按上表自动生成（配送员除外）。

#### 31.2.2 DELETE `/admin/role-accounts/{id}` — 直接删除业务角色账号（硬删除）

> **适用范围（重要）**：本接口**仅**适用于四类业务角色：`merchant` / `activity_leader` / `technician` / `courier`。
> **区域负责人（`sector_leader`）、物业领导（`property_leader`）、统筹 / 板块 / 项目负责人、普通住户** 等非业务角色**不属于**本接口范围，调用会返回「该账号不属于业务角色…请改用 DELETE /residents/{id}」；它们请使用 **§2.10 `DELETE /residents/{id}`（软删除）**。

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**路径参数**：

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `String` | 账号 ID（前缀 `res_`） |

**成功响应（DeleteRoleAccountResponse）**：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `residentId` | `String` | 被删除账号 ID |
| `deleted` | `Boolean` | 固定 `true` |
| `message` | `String` | `账号已彻底删除（含关联店铺）` |

**删除行为（硬删除 / 物理删除）**：
- 彻底删除 `residents` 记录（**非软删除**），关联手机号随之释放，可再次用于新建账号（解决软删除导致的同号重建冲突）。
- 同步删除创建账号时生成的关联店铺链：`Merchant` + `PlatformMerchant` + `MerchantPropertyConfig`（含 `group_leader` / `technician` 小店），**不产生孤儿数据**。
- 配送员账号无关联店铺，仅删除账号主体。
- **若账号已存在业务数据**（订单 / 积分 / 提现 / 维修 / 家庭成员等），外键约束会阻止硬删，返回 `90120`；**应改用 §31.2.3「禁用」或 §31.2.4「软删除」接口**，历史业务数据完整保留（避免误删财务 / 业务记录）。
- **非业务角色账号（区域负责人 / 物业领导 / 普通住户）调用本接口会被拒绝**，返回参数错误并提示改用 §2.10 软删接口。

**错误响应**：

| 错误码 | HTTP | 说明 |
| --- | --- | --- |
| `30001` | 404 | 账号不存在 |
| `90120` | 409 | 账号存在关联业务数据，无法彻底删除；请改用 §31.2.3 禁用 / §31.2.4 软删除接口 |
| `90109` | 403 | 物业管理员超出管辖范围（跨物业公司操作被拒；`platform_admin` 可跨公司） |
| `30005` | 400/409 | 手机号在当前物业公司下已存在（仅创建时） |
| `10001` | 400 | 参数缺失（含 `residentId` 前缀非 `res_`） |
| — | 403 | 角色无权限（仅 `property_admin` / `platform_admin` 可调用） |
| — | 400 | 目标账号非业务角色（区域负责人 / 物业领导 / 普通住户）——请改用 §2.10 `DELETE /residents/{id}` 软删接口 |
| `90121` | 409 | 账号已删除，无法再次操作（禁用 / 软删均会校验） |

#### 31.2.3 POST `/admin/role-accounts/{id}/disable` — 禁用/停用业务角色账号

> **适用场景**：对**存在关联业务数据、无法硬删除（返回 `90120`）**的业务角色账号（merchant / activity_leader / technician / courier），改用本接口**软性停用**——账号不可登录 / 接单，但历史订单 / 积分 / 提现 / 维修 / 家庭成员等数据完整保留（效果同 §2.9 `PATCH /residents/{id}/status` 的禁用，但本接口额外连带停用其名下店铺）。

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**路径参数**：

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `String` | 账号 ID（前缀 `res_`） |

**请求体**：

| 字段 | 类型 | 必填 | 校验 | 说明 |
| --- | --- | --- | --- | --- |
| `reason` | `String` | ✅ | `@NotBlank @Size(max=200)` | 禁用原因（操作审计，如：商家已被踢出且存在业务数据，停用账号） |

**成功响应（DisableRoleAccountResponse）**：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `residentId` | `String` | 账号 ID |
| `status` | `String` | 固定 `disabled` |
| `merchantStatus` | `String` | 关联店铺最终状态：`none`=无店铺；`stopped`=已停用；`kicked`/`rejected` 等=原有状态（未改动，如已被踢出） |
| `message` | `String` | `账号已禁用（关联店铺已停用），历史业务数据完整保留` |

**行为**：
- 账号 `status` 置为 `disabled`，**不可登录、不可接单、不可发起业务**。
- 连带停用其名下**仍在营业（`status=active`）**的店铺，店铺 `status` 置为 `stopped`（新增店铺停用态；接单前置校验 `status=active` 自然排除，店铺停止接单）。店铺**不物理删除**，记录保留便于审计。
- 历史业务数据（订单 / 积分 / 提现 / 维修 / 家庭成员等）**完整保留**。
- 非业务角色账号（区域负责人 / 物业领导 / 普通住户）调用本接口会被拒绝，提示改用 §2.9 `PATCH /residents/{id}/status`。

**错误响应**：

| 错误码 | HTTP | 说明 |
| --- | --- | --- |
| `30001` | 404 | 账号不存在 |
| `90121` | 409 | 账号已删除，无法再次操作 |
| `90109` | 403 | 物业管理员超出管辖范围 |
| `10001` | 400 | 参数缺失（`residentId` 前缀非 `res_`） |
| — | 403 | 角色无权限（仅 `property_admin` / `platform_admin`） |
| — | 400 | 目标账号非业务角色 ——请改用 §2.9 `PATCH /residents/{id}/status` |

#### 31.2.4 POST `/admin/role-accounts/{id}/soft-delete` — 软删除业务角色账号

> **适用场景**：同 §31.2.3。当管理员**不希望物理删除账号、但需要让账号从可见列表消失**时，改用本接口**软删除**（标记删除），效果等同 §2.10 `DELETE /residents/{id}`，但额外连带停用其名下店铺。

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**路径参数**：

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `String` | 账号 ID（前缀 `res_`） |

**成功响应（DeleteRoleAccountResponse）**：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `residentId` | `String` | 被删除账号 ID |
| `deleted` | `Boolean` | 固定 `true` |
| `message` | `String` | `账号已软删除（关联店铺已停用），历史业务数据完整保留` |

**行为**：
- 账号 `status` 置为 `deleted` 并记录 `deletedAt`，列表查询自动过滤，**前端无需手动移除该行**。
- 连带停用其名下**仍在营业（`status=active`）**的店铺（`status=stopped`）。店铺**不物理删除**，记录保留便于审计。
- 与硬删除（§31.2.2）区别：**不物理删除 `residents`、不释放手机号**，因此同手机号不能立即重建账号；历史业务数据完整保留。
- 非业务角色账号调用本接口会被拒绝，提示改用 §2.10 `DELETE /residents/{id}`。

**错误响应**：

| 错误码 | HTTP | 说明 |
| --- | --- | --- |
| `30001` | 404 | 账号不存在 |
| `90121` | 409 | 账号已删除，无法再次操作 |
| `90109` | 403 | 物业管理员超出管辖范围 |
| `10001` | 400 | 参数缺失（`residentId` 前缀非 `res_`） |
| — | 403 | 角色无权限（仅 `property_admin` / `platform_admin`） |
| — | 400 | 目标账号非业务角色 ——请改用 §2.10 `DELETE /residents/{id}` |

#### 31.2.5 前端删除接口分流速查表（交付前端）

> 前端在账号管理页删除账号时，**必须先按列表返回的 `role` 字段判断走哪个删除接口**，并**统一传列表返回的 `residentId`（`res_` 前缀）**。

| 账号类型 | `role` 值 | 删除接口 | 删除方式 | 删除后行为 |
| --- | --- | --- | --- | --- |
| 商家 | `merchant` | `DELETE /admin/role-accounts/{id}` | 硬删除 | 账号 + 关联店铺链（含平台商家 / 小区绑定）彻底删除，手机号释放 |
| 活动组长 | `activity_leader` | `DELETE /admin/role-accounts/{id}` | 硬删除 | 账号 + 「{name}的小店」彻底删除 |
| 技工 | `technician` | `DELETE /admin/role-accounts/{id}` | 硬删除 | 账号 + 「{name}的便民小店」彻底删除 |
| 配送员 | `courier` | `DELETE /admin/role-accounts/{id}` | 硬删除 | 仅删账号主体 |
| 区域负责人 | `sector_leader` | `DELETE /residents/{id}` | 软删除 | 状态置 `deleted` + `deletedAt`，列表自动隐藏，业务数据保留 |
| 物业领导 | `property_leader` | `DELETE /residents/{id}` | 软删除 | 同上 |
| 统筹 / 板块 / 项目负责人 | `coordinator` / `individual_leader` 等 | `DELETE /residents/{id}` | 软删除 | 同上 |
| 普通住户 | `resident` / `tenant` | `DELETE /residents/{id}` | 软删除 | 同上 |

**前端注意事项**：

1. 删除按钮必须绑定**列表返回的 `residentId`**，不能用 `item.id` / 自增主键 / 其他业务 id 代替（后端已校验 `res_` 前缀，传错返回 `10001`「ID格式不正确」）。
2. 只有 `merchant` / `activity_leader` / `technician` / `courier` 四种走 `/admin/role-accounts`；**其余角色全部走 `/residents/{id}`**。
3. 误对区域负责人 / 物业领导调用 `/admin/role-accounts/{id}` 时，后端会返回「该账号不属于业务角色…请改用 DELETE /residents/{id}」，前端可捕获该提示并自动切换到软删接口。
4. 软删成功后列表查询自动过滤已删除账号，前端**无需**手动从表格移除该行。
5. 删除前确认弹窗建议文案：「账号将停用，历史业务数据保留」。
6. **硬删（`DELETE /admin/role-accounts/{id}`）返回 `90120`（存在关联业务数据）时，禁止反复重试**。前端应引导管理员改用 §31.2.3「禁用」或 §31.2.4「软删除」接口，保留历史数据同时让账号停用 / 隐藏。

---

## 32. 管理端 — 公告管理

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

### 32.1 GET `/admin/announcements/{id}/collects` — 公告收集列表

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | `String` | ✅ | 公告ID（前缀 `ann_`） |

**Query 参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `keyword` | `String` | ❌ | | 关键词（住户姓名/手机号） |

> 不支持 `sort` 参数，固定按提交时间倒序。

**成功响应**（`AnnouncementCollectListResponse`，**非普通分页结构**：`data` 内同时返回公告信息、字段定义、回复列表与分页）：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "announcementId": "ann_demo001",
    "announcementTitle": "停水通知",
    "totalResponses": 25,
    "responseRate": 0.85,
    "fields": [
      {
        "key": "needWater",
        "name": "是否需要送水",
        "type": "RADIO",
        "required": true,
        "options": ["是", "否"]
      }
    ],
    "list": [
      {
        "id": "coll_demo001",
        "residentId": "res_b7m9x1",
        "residentName": "张先生",
        "phone": "13900000001",
        "building": "3栋",
        "collectedData": { "needWater": true, "contactPhone": "13800000000" },
        "submittedAt": "2026-07-01T08:00:00Z"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 25,
      "totalPages": 2
    }
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**字段说明**：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `announcementId` | `String` | 公告ID |
| `announcementTitle` | `String` | 公告标题 |
| `totalResponses` | `long` | 总回复数 |
| `responseRate` | `BigDecimal` | 回复率（0~1） |
| `fields` | `Array` | 收集字段定义：`key`/`name`/`type`/`required`/`options` |
| `list[].id` | `String` | 回复记录ID |
| `list[].residentId` | `String` | 住户ID |
| `list[].residentName` | `String` | 住户姓名 |
| `list[].phone` | `String` | 手机号 |
| `list[].building` | `String` | 楼栋 |
| `list[].collectedData` | `Object` | 提交的键值数据 |
| `list[].submittedAt` | `String` | 提交时间（ISO-8601 UTC） |

---

## 33. 管理端 — 物业币提现审核

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

### 33.1 GET `/admin/coin-withdrawals` — 物业币提现审核列表

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`（需权限 `coin.audit`）

**Query 参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `merchantId` | `String` | ❌ | | 商家ID |
| `auditStatus` | `String` | ❌ | | `pending`/`approved`/`rejected`/`completed` |
| `status` | `String` | ❌ | | 与 `auditStatus` 同义，兼容字段 |
| `startDate` | `String` | ❌ | | 开始日期 `yyyy-MM-dd` |
| `endDate` | `String` | ❌ | | 结束日期 `yyyy-MM-dd` |
| `propertyCompanyId` | `String` | ❌ | | 物业公司ID（`platform_admin` 可指定） |

> 不支持 `sort` 参数，固定按 `created_at` 倒序。

**成功响应**（`PageResponse<CoinWithdrawalListItemResponse>`）：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "cw_demo001",
        "merchantId": "mch_demo001",
        "merchantName": "万达超市",
        "coinAmount": 1000.00,
        "cashAmount": 1000.00,
        "feeRate": 0.05,
        "feeAmount": 50.00,
        "actualAmount": 950.00,
        "auditStatus": "pending",
        "createdAt": "2026-07-01T02:00:00Z"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 8,
      "totalPages": 1
    }
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**字段说明**：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `String` | 提现记录ID（`cw_`） |
| `merchantId` | `String` | 商家ID |
| `merchantName` | `String` | 商家名称 |
| `coinAmount` | `BigDecimal` | 物业币金额 |
| `cashAmount` | `BigDecimal` | 折算现金金额 |
| `feeRate` | `BigDecimal` | 手续费率 |
| `feeAmount` | `BigDecimal` | 手续费金额 |
| `actualAmount` | `BigDecimal` | 实到金额 |
| `auditStatus` | `String` | 审核状态 |
| `createdAt` | `String` | 创建时间（ISO-8601 UTC） |

> **旧字段已废弃**：`residentId`/`residentName`/`residentPhone`/`communityName`/`exchangeAmount`/`status` 均不在响应中。

---

### 33.2 POST `/admin/coin-withdrawals/{id}/audit` — 审核物业币提现

**Auth**: Required  
**Roles**: 需权限 `coin.audit`（`platform_admin` 全通，`property_admin` 默认具备）

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | `String` | ✅ | 物业币提现记录ID（前缀 `cw_`） |

**请求体**（`AuditCoinWithdrawalRequest`）：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `auditResult` | `String` | ✅ | 审核结果：`approved` / `rejected` / `completed`；其它值返回 `10002` |
| `transferProof` | `String` | ❌ | 转账凭证URL（仅记录到操作日志，不写入提现表） |
| `rejectReason` | `String` | ❌* | 拒绝原因；`auditResult=rejected` 时**必填**，缺失返回 `10001` |
| `remark` | `String` | ❌ | 备注（仅记录到操作日志） |

**状态流转**：

| 当前状态 | 允许的 `auditResult` | 说明 |
| --- | --- | --- |
| `pending` | `approved` / `rejected` | `pending` 不能直接 `completed`（返回 `10002`）；`rejected` 会退回已扣物业币 |
| `approved` | `completed` / `rejected` | 确认打款完成，或驳回（驳回同样退回物业币） |
| `rejected` / `completed` | — | 终态，再次审核返回 `60003 审核已处理` |

> 该接口仅更新 `audit_status`；响应中的 `auditedAt` 为服务器当前时间（非数据库字段）。

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/admin/coin-withdrawals/cw_demo001/audit  \
  -H "Content-Type: application/json"  \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."  \
  -d '{
    "auditResult": "approved",
    "transferProof": "https://example.com/proof.jpg",
    "remark": "审核通过"
  }'
```

**成功响应**（`AuditCoinWithdrawalResponse`）：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "cw_demo001",
    "auditStatus": "approved",
    "coinAmount": 1000.00,
    "actualAmount": 950.00,
    "auditedAt": "2026-07-01T07:23:33Z",
    "auditorId": "res_admin001"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**响应字段**：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `String` | 提现记录ID |
| `auditStatus` | `String` | 审核后状态 |
| `coinAmount` | `BigDecimal` | 申请物业币金额 |
| `actualAmount` | `BigDecimal` | 实到金额（扣手续费后） |
| `auditedAt` | `String` | 审核时间（ISO-8601 UTC，取当前时间） |
| `auditorId` | `String` | 审核人ID |

---

### 33.3 GET `/admin/coin-withdrawals/summary` — 物业币提现审批汇总（看板）

**Roles**: `property_admin`, `platform_admin`

**查询参数**：`propertyCompanyId`（platform_admin 可指定，否则取当前）

> **（2026-10-06）** 该接口此前**后端未实现**（调用返回 404），现已补齐。
> **口径说明**：`coin_withdrawals` 表**无 `completed_at` 字段**，`completed` 为终态，故「今日完成」按 `updated_at` 落在今日统计。

**响应**（`CoinWithdrawalSummaryResponse`）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `pendingCount` | `Long` | 待审笔数 |
| `pendingAmount` | `BigDecimal` | 待审金额合计（实到金额） |
| `todayCompletedCount` | `Long` | 今日完成笔数（按 `updated_at`，表无 `completed_at`） |
| `todayCompletedAmount` | `BigDecimal` | 今日完成金额合计（按 `updated_at`） |
| `approvedCount` | `Long` | 已通过笔数（approved+completed） |
| `approvedAmount` | `BigDecimal` | 已通过金额合计（实到金额） |

---

## 34. 管理端 — 商家审核

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

### 34.1 GET `/admin/merchant-point-purchases` — 商家积分购买审核列表

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`（需权限 `merchant.audit`）

**Query 参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `merchantId` | `String` | ❌ | | 商家ID |
| `auditStatus` | `String` | ❌ | | `pending`/`pending_audit`/`approved`/`rejected` |
| `status` | `String` | ❌ | | 与 `auditStatus` 同义，兼容字段 |
| `startDate` | `String` | ❌ | | 开始日期 `yyyy-MM-dd` |
| `endDate` | `String` | ❌ | | 结束日期 `yyyy-MM-dd` |
| `propertyCompanyId` | `String` | ❌ | | 物业公司ID（`platform_admin` 可指定） |

> 不支持 `sort` 参数，固定按 `created_at` 倒序。

**成功响应**（`PageResponse<MerchantPointPurchaseListItemResponse>`）：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "mpp_demo001",
        "merchantId": "mch_demo001",
        "merchantName": "万达超市",
        "pointAmount": 1000,
        "payAmount": 10.00,
        "auditStatus": "pending",
        "remainingPoints": 0,
        "auditedAt": null,
        "auditorId": null,
        "createdAt": "2026-07-01T02:00:00Z"
      }
    ],
    "pagination": { "page": 1, "pageSize": 20, "total": 5, "totalPages": 1 }
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**列表项字段**：`id`, `merchantId`, `merchantName`, `pointAmount`（申请积分）, `payAmount`（支付金额）, `auditStatus`, `remainingPoints`（剩余可用积分）, `auditedAt`, `auditorId`, `createdAt`。

> **旧字段 `status` 已废弃**：列表项返回的是 `auditStatus`。

---

### 34.2 POST `/admin/merchant-point-purchases/{id}/audit` — 审核商家积分购买

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`（需权限 `merchant.audit`）

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | `String` | ✅ | 积分购买记录ID（前缀 `mpp_`） |

**请求体**（`AuditMerchantPointPurchaseRequest`）：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `auditResult` | `String` | ✅ | `approved` / `rejected`，其它值返回 `10002` |
| `actualPointAmount` | `Integer` | ❌ | 实际发放积分；`approved` 时不传默认取申请数量，须 > 0 |
| `rejectReason` | `String` | ❌* | 拒绝原因；`rejected` 时必填，缺失返回 `10001` |
| `remark` | `String` | ❌ | 备注（记录到操作日志） |

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/admin/merchant-point-purchases/mpp_demo001/audit  \
  -H "Content-Type: application/json"  \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."  \
  -d '{
    "auditResult": "approved",
    "actualPointAmount": 1000,
    "remark": "审核通过"
  }'
```

**成功响应**（`AuditMerchantPointPurchaseResponse`）：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "mpp_demo001",
    "auditStatus": "approved",
    "pointAmount": 1000,
    "actualPointAmount": 1000,
    "merchantPointBalance": 5000,
    "auditedAt": "2026-07-01T07:23:33Z",
    "auditorId": "res_admin01"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

> `rejected` 时响应中 `actualPointAmount` 为 `null`。仅 `pending` 状态可审核，重复审核返回 `60003`。

---

### 34.3 GET `/admin/merchant-withdrawals` — 商家提现审核列表

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`（需权限 `merchant.audit`）

**Query 参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `merchantId` | `String` | ❌ | | 商家ID |
| `auditStatus` | `String` | ❌ | | `pending`/`approved`/`rejected`/`completed` |
| `status` | `String` | ❌ | | 与 `auditStatus` 同义，兼容字段 |
| `startDate` | `String` | ❌ | | 开始日期 `yyyy-MM-dd` |
| `endDate` | `String` | ❌ | | 结束日期 `yyyy-MM-dd` |
| `propertyCompanyId` | `String` | ❌ | | 物业公司ID（`platform_admin` 可指定） |

> 不支持 `sort` 参数，固定按 `created_at` 倒序。

**成功响应**（`PageResponse<MerchantWithdrawalListItemResponse>`）：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "mw_demo001",
        "merchantId": "mch_demo001",
        "merchantName": "万达超市",
        "amount": 500.00,
        "feeRate": 0.0060,
        "feeAmount": 3.00,
        "actualAmount": 497.00,
        "auditStatus": "pending",
        "auditedAt": null,
        "completedAt": null,
        "createdAt": "2026-07-01T02:00:00Z"
      }
    ],
    "pagination": { "page": 1, "pageSize": 20, "total": 3, "totalPages": 1 }
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**列表项字段**：`id`, `merchantId`, `merchantName`, `amount`, `feeRate`, `feeAmount`, `actualAmount`, `auditStatus`, `auditedAt`, `completedAt`, `createdAt`。

> **旧字段 `status` 已废弃**：列表项返回的是 `auditStatus`。

---

### 34.4 POST `/admin/merchant-withdrawals/{id}/audit` — 审核商家提现

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`（需权限 `merchant.audit`）

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | `String` | ✅ | 提现记录ID（前缀 `mw_`） |

**请求体**（`AuditMerchantWithdrawalRequest`）：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `auditResult` | `String` | ✅ | `approved` / `rejected` / `completed`；其它值返回 `10002` |
| `transferProof` | `String` | ❌ | 转账凭证URL（仅随操作日志留存，不写入提现表） |
| `rejectReason` | `String` | ❌* | 拒绝原因；`rejected` 时必填，缺失返回 `10001` |
| `remark` | `String` | ❌ | 备注（仅记录到操作日志） |

**状态流转**：

| 当前状态 | 允许的 `auditResult` | 说明 |
| --- | --- | --- |
| `pending` | `approved` / `rejected` | `pending` 不能直接 `completed`（`10002`） |
| `approved` | `completed` / `rejected` | 确认打款完成，或驳回 |
| `rejected` / `completed` | — | 终态，再次审核返回 `60003` |

> 审核通过时会再次校验可提现余额（排除当前单），余额不足返回 `60015`；提现资格被冻结返回对应业务错误。

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/admin/merchant-withdrawals/mw_demo001/audit  \
  -H "Content-Type: application/json"  \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."  \
  -d '{
    "auditResult": "approved",
    "transferProof": "https://example.com/proof.jpg",
    "remark": "已转账"
  }'
```

**成功响应**（`AuditMerchantWithdrawalResponse`）：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "mw_demo001",
    "auditStatus": "approved",
    "transferProof": "https://example.com/proof.jpg",
    "auditedAt": "2026-07-01T07:23:33Z",
    "auditorId": "res_admin01",
    "completedAt": null
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---

### 34.5 POST `/admin/merchants/{id}/audit` — 审核商家入驻申请

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/admin/merchants/mch_demo001/audit \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "auditResult": "approved",
    "merchantLevel": "property_certified",
    "category": "外卖",
    "businessHours": "08:00-22:00",
    "deliveryFee": "0.30",
    "freeDeliveryThreshold": "50.00"
  }'
```

**请求体**：


| 字段                      | 类型       | 必填  | 校验               | 说明                                                           |
| ----------------------- | -------- | --- | ---------------- | ------------------------------------------------------------ |
| `auditResult`           | `String` | ✅   | `@NotBlank`      | `approved`（通过）/ `rejected`（拒绝）                               |
| `rejectReason`          | `String` | ❌   | `@Size(max=200)` | 拒绝原因（拒绝时必填）                                                  |
| `remark`                | `String` | ❌   | `@Size(max=200)` | 备注                                                           |
| `merchantLevel`         | `String` | ❌   |                  | 审核时调整商家等级：`official_certified`/`property_certified`/`normal` |
| `category`              | `String` | ❌   | `@Size(max=50)`  | 审核时调整分类                                                      |
| `businessHours`         | `String` | ❌   | `@Size(max=50)`  | 审核时调整营业时间                                                    |
| `deliveryFee`           | `String` | ❌   |                  | 审核时调整配送费                                                     |
| `freeDeliveryThreshold` | `String` | ❌   |                  | 审核时调整满额免配送费                                                  |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "mch_demo001",
    "name": "万达超市",
    "auditStatus": "approved",
    "auditResult": "approved",
    "rejectReason": null,
    "merchantLevel": "property_certified",
    "category": "外卖",
    "operatorId": "res_admin01",
    "operatorName": null,
    "auditedAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**业务规则**：

- 商家入驻申请后状态为 `pending_audit`
- 审核通过：`auditStatus` → `approved`，`status` → `active`；**不改写住户 `residents.role`**（v8.14 起组长/负责人均为住户子身份，身份由业务表判定；组长小店由组长申请审核自动开通，见 §101.2）。商家端登录账号由管理端 `POST /admin/role-accounts` 创建（`role=merchant`/`technician`）。
- 审核拒绝：**拒绝即硬删（2026-08-19 调整）**——商家记录及其关联数据一并物理删除，**不再保留 `rejected` 记录**。删除范围（按外键自子到父）：
  - `merchants` 名下商品（`products`）、特惠推送（`special_offers`）、分账配置（`merchant_property_configs`）
  - `merchants` 本体
  - 对应 `platform_merchants`
  - 住户 `residents.role` 保持 `resident`（从未被升级为商家身份），可随时重新提交入驻申请
  - 审核拒绝后管理端**无法再查询**该申请的驳回记录（无 `rejected` 状态可筛选），如需审计驳回过程请另行留痕
- 审核时可顺带调整商家参数（等级、分类、配送费等）
- 已审核的商家不可重复审核

**错误响应**：


| 错误码     | HTTP | 说明                      |
| ------- | ---- | ----------------------- |
| `60001` | 404  | 商家不存在                   |
| `60003` | 409  | 审核已处理（非pending_audit状态） |
| `20004` | 403  | 权限不足                    |


---



### 34.6 GET `/admin/merchant-withdrawals/summary` — 商家提现审批汇总（看板）

**Roles**: `property_admin`, `platform_admin`

**查询参数**：`propertyCompanyId`（platform_admin 可指定，否则取当前）

> **（2026-10-06）** 该接口此前**后端未实现**（调用返回 404），现已补齐。

**响应**（`MerchantWithdrawalSummaryResponse`）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `pendingCount` | `Long` | 待审笔数 |
| `pendingAmount` | `BigDecimal` | 待审金额合计（申请金额） |
| `todayProcessedCount` | `Long` | 今日已审笔数（按 auditedAt） |
| `todayProcessedAmount` | `BigDecimal` | 今日已审金额合计（实到金额） |
| `approvedCount` | `Long` | 已通过笔数（approved+completed） |
| `approvedAmount` | `BigDecimal` | 已通过金额合计（实到金额） |

---

## 35. 管理端 — 操作日志

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

### 35.1 GET `/admin/operation-logs` — 操作日志列表

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**Query 参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `operatorId` | `String` | ❌ | | 操作人ID |
| `module` | `String` | ❌ | | 操作模块 |
| `action` | `String` | ❌ | | 操作类型 |
| `targetType` | `String` | ❌ | | 目标类型 |
| `targetId` | `String` | ❌ | | 目标ID |
| `startDate` | `String` | ❌ | | 开始日期 `yyyy-MM-dd` |
| `endDate` | `String` | ❌ | | 结束日期 `yyyy-MM-dd` |
| `propertyCompanyId` | `String` | ❌ | | 物业公司ID（`platform_admin` 可指定） |

> 不支持 `sort` 参数，固定按 `created_at` 倒序。

**成功响应**（`PageResponse<OperationLogItemResponse>`）：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "ol_demo001",
        "operatorId": "res_admin01",
        "operatorName": "管理员",
        "operatorRole": "property_admin",
        "propertyCompanyId": "pc_demo001",
        "propertyCompanyName": "演示物业公司",
        "module": "merchant",
        "action": "audit_merchant",
        "targetType": "merchant",
        "targetId": "mch_demo001",
        "beforeData": null,
        "afterData": "{"auditStatus":"approved"}",
        "ipAddress": "192.168.1.100",
        "userAgent": "Mozilla/5.0",
        "createdAt": "2026-07-01T07:23:33Z"
      }
    ],
    "pagination": { "page": 1, "pageSize": 20, "total": 120, "totalPages": 6 }
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**字段说明**：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `String` | 日志ID |
| `operatorId` | `String` | 操作人ID |
| `operatorName` | `String` | 操作人姓名 |
| `operatorRole` | `String` | 操作人角色 |
| `propertyCompanyId` | `String` | 物业公司ID |
| `propertyCompanyName` | `String` | 物业公司名称 |
| `module` | `String` | 操作模块 |
| `action` | `String` | 操作类型 |
| `targetType` | `String` | 目标类型 |
| `targetId` | `String` | 目标ID |
| `beforeData` | `String` | 操作前数据（JSON字符串） |
| `afterData` | `String` | 操作后数据（JSON字符串） |
| `ipAddress` | `String` | 客户端IP |
| `userAgent` | `String` | User-Agent |
| `createdAt` | `String` | 创建时间（ISO-8601 UTC） |

> **旧字段已废弃**：`detail`、`ip` 不在响应中，请改用 `beforeData`/`afterData`、`ipAddress`。

---

## 36. 管理端 — 积分清零

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

### 36.1 POST `/admin/point-clear/run` — 执行积分清零

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**请求体**（`RunPointClearRequest`）：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `clearDate` | `String` | ✅ | 清零基准日期 `yyyy-MM-dd`，不能为空 |
| `dryRun` | `Boolean` | ❌ | 是否仅预览不执行，默认 `false` |
| `residentIds` | `List<String>` | ❌ | 指定住户ID列表；为空则清零全部 |
| `propertyCompanyId` | `String` | ❌ | 物业公司ID；为空则清零全部 |

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/admin/point-clear/run  \
  -H "Content-Type: application/json"  \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."  \
  -d '{
    "clearDate": "2024-01-01",
    "dryRun": true
  }'
```

**成功响应**（`RunPointClearResponse`）：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "taskId": "TASK20240101001",
    "clearDate": "2024-01-01",
    "lookbackDate": "2023-01-01",
    "dryRun": true,
    "processedCount": 50,
    "totalClearedPoints": 10000,
    "clearedToPool": 900.00,
    "status": "completed",
    "executedAt": "2024-01-01 10:00:00"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---

### 36.2 GET `/admin/point-clear/records` — 积分清零记录

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**Query 参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `residentId` | `String` | ❌ | | 住户ID |
| `startDate` | `String` | ❌ | | 开始日期 `yyyy-MM-dd` |
| `endDate` | `String` | ❌ | | 结束日期 `yyyy-MM-dd` |
| `propertyCompanyId` | `String` | ❌ | | 物业公司ID（`platform_admin` 可指定） |

> 不支持 `sort` 参数，固定按 `created_at` 倒序。

**成功响应**（`PageResponse<PointClearRecordItemResponse>`，**按住户逐条记录**）：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "CLR20240101001",
        "residentId": "res_demo001",
        "residentName": "张先生",
        "clearedPoints": 200,
        "equivalentAmount": 20.00,
        "poolInAmount": 18.00,
        "operatorId": "res_admin01",
        "createdAt": "2024-01-01 10:00:00"
      }
    ],
    "pagination": { "page": 1, "pageSize": 20, "total": 3, "totalPages": 1 }
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

> **旧响应已废弃**：`taskId`/`dryRun`/`processedCount`/`totalClearedAmount`/`status`/`executedAt` 属于执行接口（§36.1）的响应，不在本列表接口中。

---

## 37. 管理端 — 个体负责人管理

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

### 37.1 GET `/admin/individual-leaders` — 个体负责人列表

**Auth**: Required  
**Roles**: `platform_admin`、`property_admin`（或持有 `individual.read` 权限的账号）；`sector_leader`（仅本人名下）、`coordinator`（只读）

**Query 参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `sectorLeaderId` | `String` | ❌ | | 板块负责人ID（前缀 `sl_`）；`sector_leader` 登录时强制为本人 |
| `keyword` | `String` | ❌ | | 关键词，模糊匹配负责人姓名、住户姓名、住户手机号 |
| `status` | `String` | ❌ | | `active` / `inactive` |
| `sector` | `String` | ❌ | | 板块代码 `cleaning`/`repair`/`security`/`greening`/`other`（支持中文自动归一） |
| `propertyCompanyId` | `String` | ❌ | | 物业公司ID；仅 `platform_admin` 可指定，其它角色按当前物业公司过滤 |

> 该接口**不支持** `sort` 参数；排序固定为 `created_at` 倒序。

**成功响应**（`PageResponse<IndividualLeaderListItemResponse>`）：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "il_demo001",
        "residentId": "res_b7m9x1",
        "residentName": "张先生",
        "phone": "13900000001",
        "name": "张先生",
        "sectorLeaderId": "sl_demo001",
        "sector": "cleaning",
        "sectorName": "保洁板块",
        "commissionRate": 0.03,
        "totalEarnings": 6800.00,
        "status": "active",
        "appointedAt": "2026-07-01T07:23:33Z"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 10,
      "totalPages": 1
    }
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**响应字段**：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `String` | 个体负责人记录ID |
| `residentId` | `String` | 关联住户ID |
| `residentName` | `String` | 住户姓名 |
| `phone` | `String` | 住户手机号（明文返回，前端按需脱敏展示） |
| `name` | `String` | 负责人显示名 |
| `sectorLeaderId` | `String` | 所属板块负责人ID |
| `sector` | `String` | 板块代码 |
| `sectorName` | `String` | 板块中文名 |
| `commissionRate` | `BigDecimal` | 佣金比例 |
| `totalEarnings` | `BigDecimal` | 累计佣金收入 |
| `status` | `String` | `active` / `inactive` |
| `appointedAt` | `String` | 任命时间（ISO-8601 UTC，取 `created_at`） |

---

### 37.2 POST `/admin/individual-leaders` — 指派个体负责人

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`（另需权限 `individual.manage`；`sector_leader` 仅可指派本人名下）

> **对接注意**：旧示例中的 `sectorId` / `description` **已废弃**。当前实现必填为 `residentId`、`sectorLeaderId`、`sector`。按旧字段发包会返回 `code=10002`、`message=参数校验失败`。

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/admin/individual-leaders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "residentId": "res_b7m9x1",
    "sectorLeaderId": "sl_demo001",
    "sector": "cleaning",
    "name": "张先生",
    "commissionRate": 0.03,
    "status": "active"
  }'
```

**请求体**：


| 字段               | 类型           | 必填  | 校验 / 枚举                                      | 说明 |
| ---------------- | ------------ | --- | --------------------------------------------- | ---- |
| `residentId`     | `String`     | ✅   | `@NotBlank`，前缀 `res_`                         | 业主ID |
| `sectorLeaderId` | `String`     | ✅   | `@NotBlank`，前缀 `sl_`                          | 所属板块负责人ID（不是住户ID） |
| `sector`         | `String`     | ✅   | `@NotBlank`；`cleaning`/`repair`/`security`/`greening`/`other` | 板块编码，须与板块负责人一致 |
| `name`           | `String`     | ❌   |                                               | 个体负责人显示名；空则用业主姓名 |
| `commissionRate` | `BigDecimal` | ❌   | 0~1                                           | 分成比例；空则取物业公司默认 |
| `status`         | `String`     | ❌   | `active`/`inactive`，默认 `active`               | 状态 |
| `startDate`      | `String`     | ❌   |                                               | DTO 保留字段，当前未落库 |
| `endDate`        | `String`     | ❌   |                                               | DTO 保留字段，当前未落库 |
| `remark`         | `String`     | ❌   |                                               | DTO 保留字段，当前未落库 |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "il_demo002",
    "residentId": "res_b7m9x1",
    "residentName": "张先生",
    "phone": "13900000001",
    "name": "张先生",
    "propertyCompanyId": "pc_demo001",
    "sectorLeaderId": "sl_demo001",
    "sector": "cleaning",
    "sectorName": "保洁板块",
    "commissionRate": 0.03,
    "totalEarnings": 0.00,
    "status": "active",
    "appointedAt": "2026-07-01T07:23:33Z",
    "createdAt": "2026-07-01T07:23:33Z",
    "updatedAt": "2026-07-01T07:23:33Z"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

> **🆕 v8.13 负责人标签化**：任命成功后**不再**把该住户的 `residents.role` 改写为 `individual_leader`——role 保持 `resident`，个体负责人身份由 `individual_leaders` 表 active 记录判定（与楼长同模式）。住户的社区论坛/家庭等住户功能不受影响；「我的」页通过 `/auth/profile` 的 `isIndividualLeader` 渲染负责人入口。撤销（§37.4）同理不再回退 role。

---



### 37.3 GET `/admin/individual-leaders/{id}` — 详情

**Auth**: Required  
**Roles**: `platform_admin`、`property_admin`（或持有 `individual.read` 权限）；`sector_leader`（仅本人名下）、`coordinator`（只读）

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | `String` | ✅ | 个体负责人记录ID（前缀 `il_`） |

**成功响应**（`IndividualLeaderDetailResponse`）：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "il_demo001",
    "residentId": "res_b7m9x1",
    "residentName": "张先生",
    "phone": "13900000001",
    "name": "张先生",
    "propertyCompanyId": "pc_demo001",
    "sectorLeaderId": "sl_demo001",
    "sector": "cleaning",
    "sectorName": "保洁板块",
    "commissionRate": 0.03,
    "totalEarnings": 6800.00,
    "status": "active",
    "appointedAt": "2026-07-01T07:23:33Z",
    "createdAt": "2026-07-01T07:23:33Z",
    "updatedAt": "2026-07-01T07:23:33Z"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**响应字段**：在 §37.1 列表字段基础上增加 `propertyCompanyId`（物业公司ID）、`createdAt`、`updatedAt`（ISO-8601 UTC）。

> **旧字段已废弃**：早期文档中的 `residentPhone`、`sectorId`、`coordinatorId`、`coordinatorName`、`propertyCompanyName`、`description` **均不在响应中**。当前板块字段为 `sectorLeaderId` + `sector`，手机号为 `phone`。

---

### 37.4 PUT `/admin/individual-leaders/{id}` — 更新

**Auth**: Required  
**Roles**: `platform_admin`、`property_admin`（或持有 `individual.manage` 权限）；`sector_leader`（仅本人名下）

> **口径说明（2026-10-27 核对代码）**：请求 DTO 共 9 个字段，当前**实际落库 5 个**：`sectorLeaderId`、`sector`、`name`、`commissionRate`、`status`。`residentId`、`startDate`、`endDate`、`remark` 会被反序列化接收但**不写入数据库**（传了不报错、也不生效）。

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | `String` | ✅ | 个体负责人记录ID（前缀 `il_`） |

**请求体**（`UpdateIndividualLeaderRequest`）：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `sectorLeaderId` | `String` | ❌ | 所属板块负责人ID（前缀 `sl_`），须存在且在可写范围内 |
| `sector` | `String` | ❌ | 板块代码，经 `SectorCatalog` 归一校验；空白字符串忽略 |
| `name` | `String` | ❌ | 负责人显示名，自动 trim |
| `commissionRate` | `BigDecimal` | ❌ | 佣金比例，按物业公司配置校验上下限 |
| `status` | `String` | ❌ | `active` / `inactive`，其它值返回 `10002` |
| `residentId` | `String` | ❌ | **（接收但不生效）** 关联住户ID |
| `startDate` | `String` | ❌ | **（接收但不生效）** 生效日期 |
| `endDate` | `String` | ❌ | **（接收但不生效）** 到期日期 |
| `remark` | `String` | ❌ | **（接收但不生效）** 备注 |

> 更新 `sectorLeaderId` 时**不会**校验新板块与 `sector` 是否匹配（仅新增接口 §37.2 校验），前端应同时提交匹配的 `sector`。

**请求示例**：

```bash
curl -X PUT https://test.yuanxusoftware.top/api/v1/admin/individual-leaders/il_demo001  \
  -H "Content-Type: application/json"  \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."  \
  -d '{
    "sectorLeaderId": "sl_demo002",
    "sector": "cleaning",
    "name": "李四",
    "commissionRate": 0.03,
    "status": "active"
  }'
```

**成功响应**：`IndividualLeaderDetailResponse`，字段同 §37.3。

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "il_demo001",
    "residentId": "res_b7m9x1",
    "residentName": "张先生",
    "phone": "13900000001",
    "name": "李四",
    "propertyCompanyId": "pc_demo001",
    "sectorLeaderId": "sl_demo002",
    "sector": "cleaning",
    "sectorName": "保洁板块",
    "commissionRate": 0.03,
    "totalEarnings": 6800.00,
    "status": "active",
    "appointedAt": "2026-07-01T07:23:33Z",
    "createdAt": "2026-07-01T07:23:33Z",
    "updatedAt": "2026-07-01T15:23:33Z"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---

### 37.5 DELETE `/admin/individual-leaders/{id}` — 撤销

**Auth**: Required  
**Roles**: `platform_admin`、`property_admin`（或持有 `individual.manage` 权限）；`sector_leader`（仅本人名下）

将个体负责人置为 `inactive`（软删除，不物理删除记录），**不回退住户 `role`**。

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | `String` | ✅ | 个体负责人记录ID（前缀 `il_`） |

**Query 参数**：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `reason` | `String` | ❌ | 撤销原因；**可选**，当前后端不落库、不校验 |

> 变更说明：`reason` 曾为必填，前端不传会被 Spring 抛 `MissingServletRequestParameterException` 并误报为 `99001 内部服务异常`；现已改为可选，不传可正常撤销。

**成功响应**：返回个体负责人详情（`IndividualLeaderDetailResponse`，字段同 §37.3），`status` 为 `inactive`，**无 `deletedAt` 字段**。

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "il_demo001",
    "residentId": "res_b7m9x1",
    "residentName": "张先生",
    "phone": "13900000001",
    "name": "张先生",
    "propertyCompanyId": "pc_demo001",
    "sectorLeaderId": "sl_demo001",
    "sector": "cleaning",
    "sectorName": "保洁板块",
    "commissionRate": 0.03,
    "totalEarnings": 6800.00,
    "status": "inactive",
    "appointedAt": "2026-07-01T07:23:33Z",
    "createdAt": "2026-07-01T07:23:33Z",
    "updatedAt": "2026-07-01T15:23:33Z"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---

### 37.6 POST `/admin/individual-leaders/applications/{applicationId}/audit` — 审核住户申请

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

> **2026-09-08 变更（前端问题：申请列表 400/404）**：本接口**已实现**。此前
> `GET /admin/individual-leaders/applications` 被 `GET /admin/individual-leaders/{id}` 抢先匹配，
> 返回 `400 10002 ID格式不正确`。现改为独立 Controller 注册**字面量子路径** `/applications`，
> Spring 字面量优先级高于路径变量，天然优先于 `/{id}`，无需前端改路径。
> 另提供兼容别名 `POST /admin/individual-leader-applications/{applicationId}/audit`（已废弃但可用）。

**说明**：审核住户自主提交的个体负责人申请。`approved` 时自动创建 `IndividualLeader` 记录并将住户角色变更为 `individual_leader`。

**路径参数**：

| 参数              | 类型       | 必填 | 说明     |
| --------------- | -------- | -- | ------ |
| `applicationId` | `String` | 是  | 申请ID，前缀 `ila_` |

**请求体**：

| 字段           | 类型       | 必填 | 说明                          |
| ------------ | -------- | -- | --------------------------- |
| `auditStatus` | `String` | 是  | `approved`/`rejected`           |
| `rejectReason`| `String` | 条件 | `rejected` 时必填，不超过200字 |

**成功响应**（审核通过，`data.individualLeader` 非空）：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "applicationId": "ila_xxx",
    "auditStatus": "approved",
    "residentId": "res_xxx",
    "residentName": "张三",
    "sector": "repair",
    "rejectReason": null,
    "auditedAt": "2026-09-08T07:23:33Z",
    "individualLeaderId": "il_xxx",
    "individualLeader": {
      "id": "il_xxx",
      "residentId": "res_xxx",
      "residentName": "张三",
      "sector": "repair",
      "sectorLeaderId": "sl_xxx",
      "status": "active"
    }
  }
}
```

**成功响应**（审核拒绝，`data.individualLeader` 为 `null`）：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "applicationId": "ila_xxx",
    "auditStatus": "rejected",
    "residentId": "res_xxx",
    "residentName": "张三",
    "sector": "repair",
    "rejectReason": "暂不满足条件",
    "auditedAt": "2026-09-08T07:23:33Z",
    "individualLeaderId": null,
    "individualLeader": null
  }
}
```

**错误响应**：

| 错误码 | HTTP | 说明 |
| --- | --- | --- |
| `97030` | 404 | 个体负责人申请不存在 |
| `97031` | 400 | 该申请已审核，不可重复审核 |
| `10001` | 400 | `rejected` 未填 `rejectReason` |
| `10002` | 400 | `applicationId` 格式不正确（非 `ila_` 前缀）/ 该住户已是个体负责人 |
| `20005` | 403 | 申请人账号已停用 |

> **挂载规则**：`approved` 时若该物业该板块下存在启用中的板块负责人，则自动挂靠
> （写入 `individual_leaders.sector_leader_id`）；否则留空，由管理端后续通过
> `PUT /admin/individual-leaders/{id}` 分配。

---

### GET `/admin/individual-leaders/applications` — 个体负责人申请列表（管理端）

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

> **2026-09-08 变更（前端问题：申请列表 400/404）**：本接口**已实现**，独立 Controller 注册字面量路径，
> 优先于 `GET /admin/individual-leaders/{id}`，不再出现 `400 ID格式不正确`。
> 另提供兼容别名 `GET /admin/individual-leader-applications`（已废弃但可用，返回结构完全一致）。

**说明**：获取住户提交的个体负责人申请列表（含待审核/已通过/已拒绝）。支持按审核状态、板块、关键词（姓名/手机号）过滤，按 `createdAt` 倒序。与审核接口 `POST /admin/individual-leaders/applications/{applicationId}/audit` 配套使用。

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/admin/individual-leaders/applications?page=1&pageSize=20&auditStatus=pending" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：

| 参数                | 类型       | 必填  | 默认值 | 说明                                                          |
| ------------------ | -------- | --- | --- | ------------------------------------------------------------ |
| `page`             | `int`    | ❌   | `1`  | 页码                                                         |
| `pageSize`         | `int`    | ❌   | `20` | 每页条数                                                     |
| `auditStatus`      | `String` | ❌   |     | `pending`/`approved`/`rejected`                              |
| `sector`           | `String` | ❌   |     | 申请板块：`cleaning`/`repair`/`security`/`greening`/`other`   |
| `keyword`          | `String` | ❌   |     | 搜索关键词（姓名/手机号）                                       |
| `propertyCompanyId`| `String` | ❌   |     | 物业公司ID（平台管理员可指定；物业管理员限定本物业）                 |

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "ila_xxx",
        "residentId": "res_xxx",
        "residentName": "张三",
        "phone": "13900000001",
        "propertyCompanyId": "pc_demo001",
        "sector": "repair",
        "remark": "申请成为维修负责人",
        "auditStatus": "pending",
        "rejectReason": null,
        "createdAt": "2026-07-01T07:23:33Z",
        "auditedAt": null
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 1,
      "totalPages": 1
    }
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---

## 37.7 住户端 — 个体负责人申请（历史叫法"一级代理申请"，与 §60 货源一级代理无关）

### POST `/individual-leaders/apply` — 申请成为个体负责人

**Auth**: Required  
**Roles**: `resident`

**说明**：住户自主申请成为**个体负责人**（承接物业板块；历史叫法"一级代理申请"，与 §60 的"货源一级代理"`/distributors/*` 是两套完全不同的能力，注意区分）。需登录。每位住户在同一物业公司下仅可有一个 `pending` 状态的申请。

**请求体**：

| 字段     | 类型       | 必填 | 说明                            |
| ------ | -------- | -- | ----------------------------- |
| `sector`| `String` | 是  | 申请板块：`cleaning`/`repair`/`security`/`greening`/`other` |
| `remark`| `String` | 否  | 自我介绍/备注，不超过200字          |

**成功响应**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "applicationId": "ila_xxx",
    "status": "pending",
    "sector": "repair",
    "message": "申请已提交，等待物业审核"
  }
}
```

**错误响应**：

| 错误码 | HTTP | 说明              |
| --- | --- | --------------- |
| `10002` | 400 | sector值不合法      |
| `10002` | 400 | 您已有待审核的申请，请勿重复提交 |
| `10002` | 400 | 您已是个体负责人，无需重复申请 |

---

### GET `/individual-leaders/my-application` — 查询我的最新申请状态

**Auth**: Required  
**Roles**: `resident`

**说明**：住户查询自己最新的个体负责人申请状态。

**成功响应**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "applicationId": "ila_xxx",
    "status": "pending",
    "sector": "repair",
    "message": "申请审核中"
  }
}
```

> `status` 可能值：`none`（无申请）、`pending`（审核中）、`approved`（已通过）、`rejected`（未通过）。
>
> **v8.15 修复**：此前该接口以 `auditStatus=null` 调用派生查询，被翻译为 `audit_status IS NULL`，导致有申请也恒返回 `none`。现按 `createdAt` 倒序返回本人最新一条申请，前端调用方式不变。

---

## 38. 管理端 — 线下积分发放

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

### 38.1 POST `/admin/offline-points/grant` — 线下发放积分

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**请求体**（`GrantOfflinePointRequest`）：

| 字段 | 类型 | 必填 | 校验 | 说明 |
| --- | --- | --- | --- | --- |
| `residentId` | `String` | ✅ | `@NotBlank` | 住户ID |
| `pointAmount` | `Integer` | ✅ | `@NotNull @Min(1)` | 发放积分数量，至少 1 |
| `source` | `String` | ✅ | `@NotBlank` | 积分来源（如「物业奖励」） |
| `description` | `String` | ✅ | `@NotBlank` | 积分说明 |
| `relatedOrderId` | `String` | ❌ | | 关联订单ID |
| `operatorRemark` | `String` | ❌ | | 操作员备注 |

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/admin/offline-points/grant  \
  -H "Content-Type: application/json"  \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."  \
  -d '{
    "residentId": "res_b7m9x1",
    "pointAmount": 100,
    "source": "物业奖励",
    "description": "线下活动奖励",
    "operatorRemark": "活动现场发放"
  }'
```

**成功响应**（`GrantOfflinePointResponse`）：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "recordId": "REC20240101001",
    "residentId": "res_b7m9x1",
    "pointAmount": 100,
    "balanceAfter": 500,
    "familyBalanceAfter": 1500,
    "source": "物业奖励",
    "operatorId": "res_admin01",
    "createdAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---

### 38.2 GET `/admin/offline-points/records` — 线下发放记录

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**Query 参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `residentId` | `String` | ❌ | | 住户ID |
| `operatorId` | `String` | ❌ | | 操作者ID |
| `source` | `String` | ❌ | | 积分来源 |
| `startDate` | `String` | ❌ | | 开始日期 `yyyy-MM-dd` |
| `endDate` | `String` | ❌ | | 结束日期 `yyyy-MM-dd` |
| `propertyCompanyId` | `String` | ❌ | | 物业公司ID（`platform_admin` 可指定） |

> 不支持 `sort` 参数，固定按 `created_at` 倒序。

**成功响应**（`PageResponse<OfflinePointRecordItemResponse>`）：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "REC20240101001",
        "residentId": "res_b7m9x1",
        "residentName": "张先生",
        "pointAmount": 100,
        "balanceAfter": 500,
        "source": "物业奖励",
        "description": "线下活动奖励",
        "operatorId": "res_admin01",
        "operatorName": "管理员",
        "createdAt": "2026-07-01 15:23:33"
      }
    ],
    "pagination": { "page": 1, "pageSize": 20, "total": 15, "totalPages": 1 }
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---

## 39. 管理端 — 板块负责人管理

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`, `coordinator`

> **统筹**：`coordinator` 可列表/指派/更新/撤销**本人名下**板块负责人；不可查看其他统筹名下数据。权限：`sector.manage` 隐含 `sector.read`。

### 39.1 GET `/admin/sector-leaders` — 板块负责人列表

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`, `coordinator`（只读本物业，2026-09-07 前端问题 C 放行；跨物业 ID 返回 20004）, `coordinator`

> **2026-09-08 第二轮（v8.3）**
> - 补齐 `keyword`（姓名/手机号）与 `sector` 过滤参数（此前文档有、实现无）。
> - 列表项新增 `description`、`sectorName`、`propertyCompanyId/Name`、`coordinatorId/Name`、
>   `statusCode`、`statusLabel`。其中 `status` 与 `statusCode` 均为英文码（`active`/`inactive`），
>   `statusLabel` 为中文展示（启用/停用）。

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/admin/sector-leaders?page=1&pageSize=20&keyword=张&status=active" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：


| 参数         | 类型       | 必填  | 默认值          | 说明                  |
| ---------- | -------- | --- | ------------ | ------------------- |
| `page`     | `int`    | ❌   | `1`          | 页码                  |
| `pageSize` | `int`    | ❌   | `20`         | 每页条数                |
| `keyword`  | `String` | ❌   |              | 搜索关键词（姓名/手机号）       |
| `sector`   | `String` | ❌   |              | 板块筛选：`cleaning`/`repair`/`security`/`greening`/`other` |
| `coordinatorId` | `String` | ❌ |            | 统筹负责人ID筛选          |
| `propertyCompanyId` | `String` | ❌ |         | 物业公司ID（平台管理员可跨物业） |
| `status`   | `String` | ❌   |              | `active`/`inactive`（大小写不敏感） |
| `sort`     | `String` | ❌   | `-createdAt` | 排序                  |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "sl_demo001",
        "residentId": "res_b7m9x1",
        "residentName": "张先生",
        "residentPhone": "138****1234",
        "phone": "138****1234",
        "coordinatorId": "coo_demo001",
        "coordinatorName": "统筹负责人A",
        "sector": "cleaning",
        "sectorName": "保洁",
        "propertyCompanyId": "pc_demo001",
        "propertyCompanyName": "演示物业公司",
        "description": "负责保洁板块管理",
        "status": "active",
        "statusCode": "active",
        "statusLabel": "启用",
        "createdAt": "2026-06-01 10:00:00"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 8,
      "totalPages": 1
    }
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**列表项字段（补充）**：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `status` | `String` | 英文状态码 `active`/`inactive`（**与 §39.2 响应保持一致**） |
| `statusCode` | `String` | 与 `status` 相同 |
| `statusLabel` | `String` | 中文展示：启用 / 停用 |
| `sector` | `String` | 板块编码 `cleaning`/`repair`/`security`/`greening`/`other` |
| `sectorName` | `String` | 板块中文名：保洁 / 维修 / 安保 / 绿化 / 其它 |
| `description` | `String` | 板块描述/备注（可为空） |

---

### 39.2 POST `/admin/sector-leaders` — 指派板块负责人

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`、`coordinator`（需 `sector.manage`）

**请求体**（`AppointSectorLeaderRequest`）：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `residentId` | `String` | ✅ | 住户ID（`res_`） |
| `sector` | `String` | ✅ | 板块：`cleaning`/`repair`/`security`/`greening`/`other`，支持中文归一 |
| `coordinatorId` | `String` | ❌ | 统筹负责人ID（`coo_`）；不传时取本物业首个启用统筹；统筹登录时强制为本人 |
| `description` | `String` | ❌ | 板块描述/备注（≤200） |
| `commissionRate` | `BigDecimal` | ❌ | 抽佣比例；不传取物业公司 `sectorLeaderRate` |
| `status` | `String` | ❌ | `active`/`inactive`，默认 `active` |
| `startDate` | `String` | ❌ | **DTO 保留字段，当前不落库** |
| `endDate` | `String` | ❌ | **DTO 保留字段，当前不落库** |
| `remark` | `String` | ❌ | **DTO 保留字段，当前不落库** |

> 同一住户在同一物业同一板块只能有一条记录，重复指派返回 `10002`。`coordinatorId` 不存在/非 active 返回 `41001`。

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/admin/sector-leaders  \
  -H "Content-Type: application/json"  \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."  \
  -d '{
    "residentId": "res_b7m9x1",
    "coordinatorId": "coo_demo001",
    "sector": "cleaning",
    "description": "负责保洁板块管理",
    "commissionRate": 0.10
  }'
```

**成功响应**：`SectorLeaderDetailResponse`，字段同 §39.3。

---

### 39.3 GET `/admin/sector-leaders/{id}` — 详情

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`、`coordinator`（只读本物业，需 `sector.read`）

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | `String` | ✅ | 板块负责人记录ID（`sl_`） |

**成功响应**（`SectorLeaderDetailResponse`）：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "sl_demo001",
    "residentId": "res_b7m9x1",
    "residentName": "张先生",
    "phone": "13900000001",
    "residentPhone": "13900000001",
    "propertyCompanyId": "pc_demo001",
    "propertyCompanyName": "演示物业公司",
    "sector": "cleaning",
    "sectorName": "保洁",
    "description": "负责保洁板块管理",
    "commissionRate": 0.10,
    "coordinatorId": "coo_demo001",
    "coordinatorName": "统筹负责人A",
    "individualLeaderCount": 5,
    "totalEarnings": 12000.00,
    "status": "active",
    "statusCode": "active",
    "statusLabel": "启用",
    "appointedAt": "2026-06-01T10:00:00Z",
    "createdAt": "2026-06-01T10:00:00Z",
    "updatedAt": "2026-06-28T14:30:00Z"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**字段说明**：`residentPhone` 为 `phone` 的兼容别名，值一致；`status`/`statusCode` 均为英文状态码，`statusLabel` 为中文展示；`appointedAt` 取 `created_at`。

---

### 39.4 PUT `/admin/sector-leaders/{id}` — 更新

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`、`coordinator`（需 `sector.manage`，仅本人名下）

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | `String` | ✅ | 板块负责人记录ID（`sl_`） |

**请求体**（`UpdateSectorLeaderRequest`，全部可选）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `sector` | `String` | 板块（五枚举，支持中文归一）；改为某板块前校验同物业同板块是否已有其他启用负责人 |
| `description` | `String` | 板块描述/备注（≤200） |
| `commissionRate` | `BigDecimal` | 抽佣比例，按物业公司配置校验 |
| `coordinatorId` | `String` | 统筹负责人ID；统筹登录时被忽略并强制写为本人 |
| `status` | `String` | `active`/`inactive`（大小写不敏感） |
| `residentId` | `String` | **接收但不生效**（不修改关联住户） |
| `startDate` | `String` | **接收但不生效** |
| `endDate` | `String` | **接收但不生效** |
| `remark` | `String` | **接收但不生效** |

**成功响应**：`SectorLeaderDetailResponse`，字段同 §39.3。

**错误响应**：同板块已有其他启用负责人 → `10002`，message 含冲突对象 ID 与姓名；改为自己当前板块（幂等）不受限制。

---

### 39.5 DELETE `/admin/sector-leaders/{id}` — 撤销

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`、`coordinator`（需 `sector.manage`，仅本人名下）

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | `String` | ✅ | 板块负责人记录ID（`sl_`） |

**Query 参数**：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `reason` | `String` | ❌ | 撤销原因，仅备注，不落库 |

**业务约束**：存在启用中的下级个体负责人时返回 `400 code=10002`，message=「该板块负责人名下仍有 N 名启用中的个体负责人，请先撤销或转移个体负责人后再撤销板块负责人」。

**成功响应**：`SectorLeaderDetailResponse`（字段同 §39.3），`status=inactive`，**无 `deletedAt` 字段**。

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "sl_demo001",
    "residentId": "res_b7m9x1",
    "residentName": "张先生",
    "phone": "13900000001",
    "residentPhone": "13900000001",
    "propertyCompanyId": "pc_demo001",
    "propertyCompanyName": "演示物业公司",
    "sector": "cleaning",
    "sectorName": "保洁",
    "description": "负责保洁板块管理",
    "commissionRate": 0.10,
    "coordinatorId": "coo_demo001",
    "coordinatorName": "统筹负责人A",
    "individualLeaderCount": 0,
    "totalEarnings": 12000.00,
    "status": "inactive",
    "statusCode": "inactive",
    "statusLabel": "停用",
    "appointedAt": "2026-06-01T10:00:00Z",
    "createdAt": "2026-06-01T10:00:00Z",
    "updatedAt": "2026-07-01T07:23:33Z"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---

## 39.6 板块负责人工作台



### GET `/sector-leaders/my` — 获取当前板块负责人信息

**Auth**: Required  
**Roles**: `resident`（须持有启用中的板块负责人记录；v8.13 负责人标签化后不再要求 `role=sector_leader`）

**请求示例**：

```bash
curl -X GET https://test.yuanxusoftware.top/api/v1/sector-leaders/my \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "sl_demo001",
    "residentName": "板块负责人-李娜",
    "phone": "13900000003",
    "propertyCompanyId": "pc_demo001",
    "propertyCompanyName": "万达花园物业",
    "sector": "cleaning",
    "sectorName": "保洁板块",
    "coordinatorId": "coo_demo001",
    "coordinatorName": "统筹-张伟",
    "individualLeaderCount": 3,
    "merchantCount": 5,
    "activeSpecialOfferCount": 2,
    "totalEarnings": 1200.00,
    "withdrawableAmount": 800.00,
    "status": "active"
  },
  "timestamp": "2026-07-07 10:00:00"
}
```

**响应字段说明**：


| 字段                        | 类型           | 说明                     |
| ------------------------- | ------------ | ---------------------- |
| `id`                      | `String`     | 板块负责人ID                |
| `residentName`            | `String`     | 业主姓名                   |
| `phone`                   | `String`     | 手机号                    |
| `propertyCompanyId`       | `String`     | 物业公司ID                 |
| `propertyCompanyName`     | `String`     | 物业公司名称                 |
| `sector`                  | `String`     | 板块编码                   |
| `sectorName`              | `String`     | 板块名称                   |
| `coordinatorId`           | `String`     | 统筹负责人ID                |
| `coordinatorName`         | `String`     | 统筹负责人姓名                |
| `individualLeaderCount`   | `Long`       | 下级个体负责人数量              |
| `merchantCount`           | `Long`       | 板块商家数量                 |
| `activeSpecialOfferCount` | `Long`       | 进行中特惠数量                |
| `totalEarnings`           | `BigDecimal` | 累计收益                   |
| `withdrawableAmount`      | `BigDecimal` |  可提现金额（累计分成-已占用提现） |
| `status`                  | `String`     | 状态：`active`/`inactive` |


---



### 39.7 POST `/sector-leaders/{id}/withdrawals` — 板块负责人申请提现

**Roles**: `resident`（仅本人，且须持有启用中的板块负责人记录；v8.13 标签化）

请求体同 §64.6（`amount`），可提现=累计分成-已占用提现；响应 `withdrawalType`=`sector_leader`。

### 39.8 GET `/sector-leaders/{id}/withdrawals` — 板块负责人提现记录

**Query 参数**：`page`/`pageSize`/`auditStatus`/`startDate`/`endDate`

---

## 40. 管理端 — 积分池管理

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

### 40.1 GET `/admin/point-pools` — 获取积分池余额

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

> **2026-09-07 变更（前端问题 E）**：物业公司尚无 `point_pools` 行时不再返回 404「积分池不存在」，而是返回 **0 值概览**（`balance/totalIn/totalOut = 0`，`updatedAt = null`），前端可直接渲染。配套 SQL 脚本 `sql/2026-09-07_frontend_issues_fix.sql` 已为历史存量公司补建积分池行。仅当 `propertyCompanyId` 不存在时才返回 `97001`。

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "propertyCompanyId": "pc_demo001",
    "propertyCompanyName": "演示物业公司",
    "balance": 10000.00,
    "totalIn": 50000.00,
    "totalOut": 40000.00,
    "equivalentAmount": 10000.00,
    "updatedAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 40.2 GET `/admin/point-pools/records` — 积分池记录列表

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/admin/point-pools/records?page=1&pageSize=20&recordType=diff&startDate=2026-06-01&endDate=2026-06-30" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：


| 参数                  | 类型       | 必填  | 默认值          | 说明                                                             |
| ------------------- | -------- | --- | ------------ | -------------------------------------------------------------- |
| `page`              | `int`    | ❌   | `1`          | 页码                                                             |
| `pageSize`          | `int`    | ❌   | `20`         | 每页条数                                                           |
| `poolType`          | `String` | ❌   |              | `diff`/`expiry_clear`/`fee_clear`/`manual_adjust`/`manual_out` |
| `source`            | `String` | ❌   |              | 来源筛选                                                           |
| `startDate`         | `String` | ❌   |              | 开始日期                                                           |
| `endDate`           | `String` | ❌   |              | 结束日期                                                           |
| `propertyCompanyId` | `String` | ❌   |              | 物业公司ID（超管筛选）                                                   |
| `sort`              | `String` | ❌   | `-createdAt` | 排序                                                             |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "ppr_demo001",
        "recordType": "diff",
        "amount": 5.00,
        "balanceBefore": 9995.00,
        "balanceAfter": 10000.00,
        "description": "订单兑换差额",
        "sourceId": "ord_demo001",
        "operatorId": null,
        "createdAt": "2026-07-01 15:23:33"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 200,
      "totalPages": 10
    }
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 40.3 POST `/admin/point-pools/adjust` — 调整积分池

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**请求体**（`AdjustPointPoolRequest`）：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `amount` | `BigDecimal` | ✅ | 调整金额，正数增加、负数扣减 |
| `description` | `String` | ✅ | 调整说明 |
| `propertyCompanyId` | `String` | ❌ | 物业公司ID（`platform_admin` 可指定；物业侧自动取本物业） |

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/admin/point-pools/adjust \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "amount": 5000.00,
    "description": "充值积分池"
  }'
```

**错误响应**：


| 错误码     | HTTP | 说明      |
| ------- | ---- | ------- |
| `93001` | 404  | 积分池不存在  |
| `93002` | 400  | 积分池余额不足 |


---



## 41. 管理端 — 统筹管理

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

### 41.1 GET `/admin/coordinators` — 统筹负责人列表

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/admin/coordinators?page=1&pageSize=20&keyword=张&status=active" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：


| 参数         | 类型       | 必填  | 默认值          | 说明                  |
| ---------- | -------- | --- | ------------ | ------------------- |
| `page`     | `int`    | ❌   | `1`          | 页码                  |
| `pageSize` | `int`    | ❌   | `20`         | 每页条数                |
| `keyword`  | `String` | ❌   |              | 搜索关键词（姓名/手机号）       |
| `status`   | `String` | ❌   |              | `active`/`inactive` |
| `sort`     | `String` | ❌   | `-createdAt` | 排序                  |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "coo_demo001",
        "residentId": "res_coo01",
        "residentName": "统筹-张伟",
        "residentPhone": "13900000002",
        "phone": "13900000002",
        "propertyCompanyId": "pc_demo001",
        "propertyCompanyName": "演示物业公司",
        "description": "负责统筹管理",
        "commissionRate": 0.7,
        "sectorCount": 3,
        "sectorLeaderCount": 3,
        "individualLeaderCount": 10,
        "merchantCount": 15,
        "totalEarnings": 12500.00,
        "status": "active",
        "createdAt": "2026-07-03 08:09:15",
        "appointedAt": "2026-07-03 08:09:15"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 5,
      "totalPages": 1
    }
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

> **2026-09-06 修复同步**：列表补齐关联字段 —— `residentName`/`residentPhone`（join 住户表）、`propertyCompanyId`/`propertyCompanyName`（join 物业公司表）、`description`（`coordinators.description` 列）、`createdAt`（任命时间，`yyyy-MM-dd HH:mm:ss`）、`merchantCount`（该统筹所属物业公司下挂接商家数，含未挂接=0 一律返回数值不省略）。兼容别名：`phone`=`residentPhone`、`sectorLeaderCount`=`sectorCount`、`appointedAt`=`createdAt`。手机号为完整号码（管理端列表不脱敏，与现网一致）。

---



### 41.2 POST `/admin/coordinators` — 指派统筹负责人

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`（需 `coordinator.manage`）

**请求体**（`AppointCoordinatorRequest`）：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `residentId` | `String` | ✅ | 住户ID（`res_`），须存在且属于当前物业 |
| `commissionRate` | `BigDecimal` | ❌ | 统筹分成比例；不传取物业公司 `coordinatorShareRate` |
| `description` | `String` | ❌ | 统筹描述/备注，写入 `coordinators.description` |
| `status` | `String` | ❌ | `active`/`inactive`，默认 `active` |
| `startDate` | `String` | ❌ | **DTO 保留字段，当前不落库** |
| `endDate` | `String` | ❌ | **DTO 保留字段，当前不落库** |
| `remark` | `String` | ❌ | **DTO 保留字段，当前不落库** |

> 请求体**没有** `propertyCompanyId` 字段；物业公司以住户所属物业为准。同一住户在同一物业只能有一条统筹记录，重复指派返回 `10002`。

**成功响应**：`CoordinatorDetailResponse`，字段同 §41.3。

---

### 41.3 GET `/admin/coordinators/{id}` — 详情

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**路径参数**：


| 参数   | 类型       | 必填  | 说明        |
| ---- | -------- | --- | --------- |
| `id` | `String` | ✅   | 统筹负责人记录ID |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "coo_demo001",
    "residentId": "res_coo01",
    "residentName": "统筹-张伟",
    "residentPhone": "13900000002",
    "phone": "13900000002",
    "propertyCompanyId": "pc_demo001",
    "propertyCompanyName": "演示物业公司",
    "description": "负责统筹管理",
    "commissionRate": 0.7,
    "sectorCount": 3,
    "sectorLeaderCount": 3,
    "individualLeaderCount": 10,
    "merchantCount": 15,
    "totalEarnings": 12500.00,
    "status": "active",
    "createdAt": "2026-07-03 08:09:15",
    "appointedAt": "2026-07-03 08:09:15",
    "updatedAt": "2026-07-03 08:09:15"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

> **2026-09-06 修复同步**：详情与列表同源同字段（§41.1 修复说明同样适用）；`description` 取自 `coordinators.description`，任命（§41.2）与更新（§41.4）均可写入。

---



### 41.4 PUT `/admin/coordinators/{id}` — 更新

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`（需 `coordinator.manage`）

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | `String` | ✅ | 统筹负责人记录ID（`coo_`） |

**请求体**（`UpdateCoordinatorRequest`，全部可选）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `commissionRate` | `BigDecimal` | 统筹分成比例 |
| `status` | `String` | `active`/`inactive` |
| `description` | `String` | 统筹描述/备注 |
| `residentId` | `String` | **接收但不生效**（不修改关联住户） |
| `startDate` | `String` | **接收但不生效** |
| `endDate` | `String` | **接收但不生效** |
| `remark` | `String` | **接收但不生效** |

**成功响应**：`CoordinatorDetailResponse`（完整详情，**不是**精简对象），字段同 §41.3。

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "coo_demo001",
    "residentId": "res_coo01",
    "residentName": "统筹-张伟",
    "residentPhone": "13900000002",
    "phone": "13900000002",
    "propertyCompanyId": "pc_demo001",
    "propertyCompanyName": "演示物业公司",
    "description": "更新后的描述",
    "commissionRate": 0.7,
    "sectorCount": 3,
    "sectorLeaderCount": 3,
    "individualLeaderCount": 10,
    "merchantCount": 15,
    "totalEarnings": 12500.00,
    "status": "active",
    "createdAt": "2026-07-03 08:09:15",
    "appointedAt": "2026-07-03 08:09:15",
    "updatedAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---

### 41.5 DELETE `/admin/coordinators/{id}` — 撤销

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`（需 `coordinator.manage`）

> 存在启用中的下级板块负责人时返回 `400 code=10002`，message=「该统筹名下仍有 N 名启用中的板块负责人，请先撤销或转移板块负责人后再撤销统筹」。

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | `String` | ✅ | 统筹负责人记录ID（`coo_`） |

**Query 参数**：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `reason` | `String` | ❌ | 撤销原因，仅备注，不落库 |

**成功响应**：`CoordinatorDetailResponse`（完整详情，**不是** `{id,status,deletedAt}` 精简对象），`status=inactive`。

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "coo_demo001",
    "residentId": "res_coo01",
    "residentName": "统筹-张伟",
    "residentPhone": "13900000002",
    "phone": "13900000002",
    "propertyCompanyId": "pc_demo001",
    "propertyCompanyName": "演示物业公司",
    "description": "负责统筹管理",
    "commissionRate": 0.7,
    "sectorCount": 0,
    "sectorLeaderCount": 0,
    "individualLeaderCount": 0,
    "merchantCount": 0,
    "totalEarnings": 12500.00,
    "status": "inactive",
    "createdAt": "2026-07-03 08:09:15",
    "appointedAt": "2026-07-03 08:09:15",
    "updatedAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---

## 42. 管理端 — 平台商家管理

**Auth**: Required  
**Roles**: `platform_admin`

### 42.1 GET `/admin/platform-merchants` — 平台商家列表

**Auth**: Required  
**Roles**: `platform_admin`

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/admin/platform-merchants?page=1&pageSize=20&keyword=万达&category=外卖&status=active" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：


| 参数         | 类型       | 必填  | 默认值          | 说明                  |
| ---------- | -------- | --- | ------------ | ------------------- |
| `page`     | `int`    | ❌   | `1`          | 页码                  |
| `pageSize` | `int`    | ❌   | `20`         | 每页条数                |
| `keyword`  | `String` | ❌   |              | 搜索关键词（名称/电话）        |
| `category` | `String` | ❌   |              | 分类                  |
| `status`   | `String` | ❌   |              | `active`/`inactive`/`pending` |
| `linkedPropertyCompanyId` | `String` | ❌ | | 只返回已挂接到指定物业公司的平台商家 |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "pm_demo001",
        "name": "万达超市",
        "category": "外卖",
        "coverUrl": "https://cdn.example.com/merchant/pm_demo001.jpg",
        "contactPhone": "13800138000",
        "address": "朝阳区建国路88号",
        "businessHours": "08:00-22:00",
        "linkedPropertyCount": 3,
        "status": "active",
        "createdAt": "2026-03-15 10:00:00"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 20,
      "totalPages": 1
    }
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 42.2 POST `/admin/platform-merchants` — 创建平台商家

**Auth**: Required  
**Roles**: `platform_admin`

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/admin/platform-merchants \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "name": "新商家",
    "category": "外卖",
    "contactPhone": "13812345678",
    "description": "日用品配送",
    "businessHours": "08:00-22:00"
  }'
```

**请求体**：


| 字段              | 类型       | 必填  | 校验                         | 说明     |
| --------------- | -------- | --- | -------------------------- | ------ |
| `name`          | `String` | ✅   | `@NotBlank @Size(max=100)` | 商家名称   |
| `category`      | `String` | ✅   | `@NotBlank`                | 分类     |
| `contactPhone`  | `String` | ✅   | `@NotBlank @Size(max=20)`  | 联系电话   |
| `description`   | `String` | ❌   |                            | 描述     |
| `coverUrl`      | `String` | ❌   | `@Size(max=500)`           | 封面图URL |
| `businessHours` | `String` | ❌   | `@Size(max=50)`            | 营业时间   |
| `address`       | `String` | ❌   | `@Size(max=200)`           | 地址     |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "pm_demo002",
    "name": "新商家",
    "category": "外卖",
    "contactPhone": "138****5678",
    "residentId": "res_mch02",
    "status": "active",
    "createdAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 42.3 GET `/admin/platform-merchants/{id}` — 详情

**Auth**: Required  
**Roles**: `platform_admin`

**路径参数**：


| 参数   | 类型       | 必填  | 说明     |
| ---- | -------- | --- | ------ |
| `id` | `String` | ✅   | 平台商家ID |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "pm_demo001",
    "name": "万达超市",
    "category": "外卖",
    "contactPhone": "138****5678",
    "residentId": "res_mch02",
    "description": "超市提供日常用品配送",
    "coverUrl": "https://cdn.example.com/merchant/pm_demo001.jpg",
    "businessHours": "08:00-22:00",
    "address": "小区东门",
    "status": "active",
    "linkedProperties": [
      { "propertyCompanyId": "pc_demo001", "propertyName": "演示物业公司", "linkedAt": "2026-04-01 10:00:00" }
    ],
    "createdAt": "2026-03-15 10:00:00",
    "updatedAt": "2026-06-28 14:30:00"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 42.4 PUT `/admin/platform-merchants/{id}` — 更新

**Auth**: Required  
**Roles**: `platform_admin`

**路径参数**：


| 参数   | 类型       | 必填  | 说明     |
| ---- | -------- | --- | ------ |
| `id` | `String` | ✅   | 平台商家ID |


**请求示例**：

```bash
curl -X PUT https://test.yuanxusoftware.top/api/v1/admin/platform-merchants/pm_demo001 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "name": "万达超市（更新）",
    "category": "外卖",
    "businessHours": "07:00-23:00"
  }'
```

**请求体**：


| 字段              | 类型       | 必填  | 校验               | 说明                  |
| --------------- | -------- | --- | ---------------- | ------------------- |
| `name`          | `String` | ❌   | `@Size(max=100)` | 商家名称                |
| `category`      | `String` | ❌   |                  | 分类                  |
| `contactPhone`  | `String` | ❌   | `@Size(max=20)`  | 联系电话                |
| `description`   | `String` | ❌   |                  | 描述                  |
| `coverUrl`      | `String` | ❌   | `@Size(max=500)` | 封面图URL              |
| `businessHours` | `String` | ❌   | `@Size(max=50)`  | 营业时间                |
| `address`       | `String` | ❌   | `@Size(max=200)` | 地址                  |
| `status`        | `String` | ❌   |                  | `active`/`inactive` |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "pm_demo001",
    "name": "万达超市（更新）",
    "category": "外卖",
    "businessHours": "07:00-23:00",
    "updatedAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 42.1 商家-小区距离配置（替代 GPS 算距）
<a id="421-商家-小区距离配置"></a>

> **（2026-08 新增）**：管理端为**商家 × 小区**配置 `distanceKm`（该商家距该小区多少公里），商品/商家列表按当前用户 `communityId` 查表写入 `distanceKm`；**无配置则不展示距离**。
> **不再使用经纬度 Haversine 自动算距**（`merchants`/`communities` 的经纬度字段不再参与距离计算）。下单运费（`/orders/preview`、`/orders`）用同一配置距离命中配送档位。

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`, `coordinator`, `sector_leader`, `individual_leader`

#### 42.1.1 GET `/admin/merchants/{merchantId}/community-distances` — 查询距离配置

返回该商家所属物业下**全部小区**，已配置的带 `distanceKm`（未配置为 `null`）。

```json
{
  "code": 0,
  "message": "success",
  "data": [
    { "communityId": "com_demo001", "communityName": "阳光花园", "distanceKm": 1.20 },
    { "communityId": "com_demo002", "communityName": "翡翠湾", "distanceKm": null }
  ],
  "timestamp": "2026-08-17 18:00:00"
}
```

#### 42.1.2 PUT `/admin/merchants/{merchantId}/community-distances` — 配置距离

**请求体**：

| 字段            | 类型         | 必填  | 说明                                  |
| ------------- | ---------- | --- | ----------------------------------- |
| `items[]`     | `Array`    | ✅   | 距离配置列表                             |
| `items[].communityId` | `String` | ✅   | 小区ID（须属于该商家所在物业）                   |
| `items[].distanceKm` | `BigDecimal` | ✅   | 距离（公里）≥0；传 `null` 表示清除该小区配置（不再展示距离） |

**成功响应**：更新后的配置列表（同 42.1.1 响应结构）。

**错误响应**：

| 错误码   | HTTP | 说明                |
| ----- | ---- | ----------------- |
| `10002` | 400  | 小区不存在 / 不属于该商家所在物业 / 参数不合法 |
| `10003` | 400  | distanceKm 为负数       |
| `20004` | 403  | 权限不足 / 跨物业操作        |

---



### 42.2 转给物业流水（对账）
<a id="422-转给物业流水对账"></a>

> **（2026-08 新增）**：住户「转给物业」的积分/物业币流水（谁、类型、数量、时间、备注），**即时扣款、无需审批**，供物业对账。数据源为 `point_records` / `property_coin_records` 中 `source=transfer_to_property` 的记录（§2.6.4）。

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`, `coordinator`, `sector_leader`, `individual_leader`

#### 42.2.1 GET `/admin/transfer-to-property/points` — 积分转物业流水

**Query 参数**：`page` / `pageSize` / `propertyCompanyId`（超管可指定）/ `residentId`（可选筛选）

#### 42.2.2 GET `/admin/transfer-to-property/coins` — 物业币转物业流水

**Query 参数**：同 42.2.1。

**成功响应**（分页 `TransferToPropertyListItemResponse`）：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "pt_xxxxx",
        "type": "point",
        "residentId": "res_b7m9x1",
        "residentName": "张先生",
        "residentPhone": "138****1234",
        "amount": 100,
        "remark": "捐给物业做活动经费",
        "propertyCompanyId": "pc_demo001",
        "createdAt": "2026-08-17T10:00:00Z"
      }
    ],
    "pagination": { "page": 1, "pageSize": 20, "total": 1, "totalPages": 1 }
  },
  "timestamp": "2026-08-17 18:00:00"
}
```

| 字段                 | 类型         | 说明                        |
| ------------------ | ---------- | ------------------------- |
| `type`             | `String`    | `point` / `coin`           |
| `amount`           | `BigDecimal` | 数量（积分：整数；物业币：两位小数）        |
| `remark`           | `String`    | 备注（缺省为「转给物业」）              |
| `createdAt`        | `String`    | 转出时间（ISO-8601 UTC）          |

---



## 43. 管理端 — 商家踢出

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`, `sector_leader`, `coordinator`

> **板块负责人**：须具备权限码 `merchant.kickout`（角色预设 `sector_leader` 默认包含）；仅能踢出本物业公司下的商家。

### 43.1 POST `/admin/merchants/{id}/kick` — 踢出商家

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/admin/merchants/mch_demo001/kick \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "reason": "商家违规经营",
    "notifyMerchant": true
  }'
```

**请求体**：


| 字段               | 类型        | 必填  | 校验                                | 说明             |
| ---------------- | --------- | --- | --------------------------------- | -------------- |
| `reason`         | `String`  | ✅   | `@NotBlank @Size(min=1, max=200)` | 踢出原因           |
| `notifyMerchant` | `Boolean` | ❌   |                                   | 是否通知商家（默认true） |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "merchantId": "mch_demo001",
    "merchantName": "万达超市",
    "status": "kicked",
    "reason": "商家违规经营",
    "kickedAt": "2026-07-01 15:23:33",
    "operatorId": "res_admin01",
    "notifySent": true
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**业务规则**：

- 被踢出的商家状态变为 `kicked`
- 商家所有商品自动下架
- 未完成的订单需要手动处理
- 商家可在30天后重新申请入驻

**错误响应**：


| 错误码     | HTTP | 说明     |
| ------- | ---- | ------ |
| `60001` | 404  | 商家不存在  |
| `60005` | 409  | 商家已被踢出 |


---



## 44. 管理端 — 仪表盘

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

### 44.1 GET `/admin/dashboard/overview` — 仪表盘总览

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/admin/dashboard/overview?propertyCompanyId=pc_demo001" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：


| 参数                  | 类型       | 必填   | 默认值      | 说明                    |
| ------------------- | -------- | ---- | -------- | --------------------- |
| `propertyCompanyId` | `String` | 超管必填 | 从token获取 | 物业公司ID（超管必传，物业管理员可不传） |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "summary": {
      "totalOrders": 1250,
      "totalRevenue": 62500.00,
      "totalResidents": 520,
      "totalFamilies": 200,
      "totalMerchants": 18,
      "totalCoinIssued": 482000.00,
      "coinConsumed": 120540.00,
      "coinInCirculation": 361460.00
    },
    "trends": {
      "orderGrowthRate": 0.12,
      "revenueGrowthRate": 0.15,
      "newResidentGrowthRate": 0.03
    },
    "topMerchants": [
      {
        "id": "mch_demo001",
        "name": "万达超市",
        "orderCount": 320,
        "revenue": 16000.00
      }
    ],
    "recentActivity": [
      {
        "type": "新业主入驻",
        "typeCode": "new_resident",
        "typeLabel": "新业主入驻",
        "description": "新业主入驻：王五",
        "time": "2026-06-30T14:30:00"
      }
    ]
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**响应字段说明**：


| 字段                             | 类型           | 说明                          |
| ------------------------------ | ------------ | --------------------------- |
| `summary.totalOrders`          | `long`       | 订单总数                        |
| `summary.totalRevenue`         | `BigDecimal` | 已完成订单总营收                    |
| `summary.totalResidents`       | `long`       | 住户总数                        |
| `summary.totalFamilies`        | `long`       | 家庭总数                        |
| `summary.totalMerchants`       | `long`       | 商户总数                        |
| `summary.totalCoinIssued`      | `BigDecimal` | 物业币发行总量                     |
| `summary.coinConsumed`         | `BigDecimal` | 物业币已消费                      |
| `summary.coinInCirculation`    | `BigDecimal` | 物业币流通中                      |
| `trends.orderGrowthRate`       | `BigDecimal` | 订单增长率                       |
| `trends.revenueGrowthRate`     | `BigDecimal` | 营收增长率                       |
| `trends.newResidentGrowthRate` | `BigDecimal` | 新增住户增长率                     |
| `topMerchants`                 | `List`       | 热门商户列表（按订单数排序，最多5个）         |
| `topMerchants[].id`            | `String`     | 商户ID                        |
| `topMerchants[].name`          | `String`     | 商户名称                        |
| `topMerchants[].orderCount`    | `long`       | 订单数                         |
| `topMerchants[].revenue`       | `BigDecimal` | 营收金额                        |
| `recentActivity`               | `List`       | 最近活动列表                      |
| `recentActivity[].type`        | `String`     | **展示用中文**：新订单/新业主入驻 |
| `recentActivity[].typeCode`    | `String`     | `order`/`new_resident` |
| `recentActivity[].typeLabel`   | `String`     | 与 `type` 相同 |
| `recentActivity[].description` | `String`     | 活动描述（中文句子）                        |
| `recentActivity[].time`        | `String`     | 活动时间                        |


---



## 45. 管理端 — 批量商家配置

**Auth**: Required  
**Roles**: `platform_admin`（仅平台管理员；`property_admin` 调用返回 `403 / 20004`）

### 45.1 POST `/admin/platform-merchants/batch-config` — 批量设定商家参数

> **v8.25 契约修正**：请求体为 `items[]` 逐商家配置，**不是**旧的 `merchantIds + config` 结构；响应为 `successCount/failCount/errors`。逐条处理，单条失败不回滚其他商家。

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/admin/platform-merchants/batch-config  \
  -H "Content-Type: application/json"  \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."  \
  -d '{
    "items": [
      {
        "merchantId": "m_xxx",
        "propertyCompanyId": "pc_xxx",
        "commissionRate": 0.15,
        "pointExchangeRate": 1.0,
        "memberDiscountPrice": 9.90,
        "coinRebateRate": 0.05,
        "coinRebateEnabled": true
      }
    ]
  }'
```

**请求体**：

| 字段 | 类型 | 必填 | 校验 | 说明 |
| --- | --- | --- | --- | --- |
| `items` | `List` | ✅ | `@NotNull @Valid` | 配置列表，不能为空 |
| `items[].merchantId` | `String` | ✅ | `@NotBlank`，`m_` 前缀 | 商家 ID |
| `items[].propertyCompanyId` | `String` | ✅ | `pc_` 前缀 | 物业公司 ID（用于定位 `merchant_property_configs`） |
| `items[].commissionRate` | `BigDecimal` | ✅ | `@NotNull @DecimalMin("0")` | 抽佣比例 |
| `items[].pointExchangeRate` | `BigDecimal` | ❌ | | 积分兑换比（1 元 = X 积分） |
| `items[].memberDiscountPrice` | `BigDecimal` | ❌ | | 会员折扣价 |
| `items[].coinRebateRate` | `BigDecimal` | ❌ | | 物业币返现比例（⚠️ V36 起**存库但不再参与任何金额计算**） |
| `items[].coinRebateEnabled` | `Boolean` | ❌ | | 是否启用物业币返现（⚠️ 同上；订单返用户物业币改由 §71 物业公司级 `userRewardShareRate` 控制。建议界面置灰或下掉） |

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "successCount": 1,
    "failCount": 0,
    "errors": []
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**错误响应**：

| 错误码 | HTTP | 说明 |
| --- | --- | --- |
| `20004` | 403 | 非 `platform_admin` |
| `10001` | 400 | `items` 为空 |
| — | 200 | 单条商家 ID/物业公司 ID 格式错误、商家不存在等写入 `errors[]`，不抛整体异常 |

---

## 46. 管理端 — 商家挂接

**Auth**: Required  
**Roles**: `platform_admin`（仅平台管理员；`property_admin` 调用返回 `403 / 20004`）

### 46.1 POST `/admin/platform-merchants/{id}/link` — 从商家库挂接到物业

> **v8.25 契约修正**：配置字段为**扁平字段**，不是旧的 `customConfig` 嵌套；传 `customConfig` 会被忽略。

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/admin/platform-merchants/pm_demo001/link  \
  -H "Content-Type: application/json"  \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."  \
  -d '{
    "propertyCompanyId": "pc_demo001",
    "commissionRate": 0.12,
    "pointExchangeRate": 1.0,
    "memberDiscountPrice": 9.90,
    "coinRebateRate": 0.05,
    "coinRebateEnabled": true,
    "merchantLevel": "normal",
    "rankOrder": 0,
    "deliveryFee": 0.30,
    "freeDeliveryThreshold": 50.00
  }'
```

**请求体**（`LinkMerchantRequest`）：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `propertyCompanyId` | `String` | 条件 | 物业公司 ID；`platform_admin` 未传时取当前物业，仍为空返回 `10001` |
| `commissionRate` | `BigDecimal` | ❌ | 抽佣比例，写入 `merchant_property_configs` |
| `pointExchangeRate` | `BigDecimal` | ❌ | 积分兑换比 |
| `memberDiscountPrice` | `BigDecimal` | ❌ | 会员折扣价 |
| `coinRebateRate` | `BigDecimal` | ❌ | 物业币返现比例 |
| `coinRebateEnabled` | `Boolean` | ❌ | 是否启用物业币返现 |
| `merchantLevel` | `String` | ❌ | 商家等级，默认 `normal` |
| `rankOrder` | `Integer` | ❌ | 排序值，默认 `0` |
| `deliveryFee` | `BigDecimal` | ❌ | 配送费 |
| `freeDeliveryThreshold` | `BigDecimal` | ❌ | 满额免配送费 |

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "merchantId": "m_new001",
    "platformMerchantId": "pm_demo001",
    "propertyCompanyId": "pc_demo001",
    "auditStatus": "approved"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**业务规则**：

- 挂接成功后自动创建本地商家记录，`auditStatus=approved`、`status=active`。
- 同一平台商家不能在同一物业下重复挂接。
- 平台商家资料（名称/描述/封面/分类/营业时间/电话/地址）自动复制到本地商家。

**错误响应**：

| 错误码 | HTTP | 说明 |
| --- | --- | --- |
| `30004` | 409 | 该商家已在当前物业下挂接 |
| `60001` | 404 | 平台商家不存在 |
| `10001` | 400 | `propertyCompanyId` 为空 |
| `20004` | 403 | 非 `platform_admin` |

---

## 47. 管理端 — 盈利空间

> **注意**：盈利空间接口在 `/admin/platform-merchants` 路径下，不是 `/admin/merchants`

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

### 47.1 GET `/admin/platform-merchants/{id}/profit-space` — 盈利空间计算

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/admin/platform-merchants/pm_demo001/profit-space?consumptionAmount=100" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：


| 参数                  | 类型         | 必填  | 默认值   | 说明                                                    |
| ------------------- | ---------- | --- | ----- | ----------------------------------------------------- |
| `consumptionAmount` | `BigDecimal` | ❌   | `100` | 消费金额 = **商品金额**（不含配送费，满额判断口径，规范§8.5.2）                  |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "platformMerchantId": "pm_demo001",
    "platformMerchantName": "万达超市",
    "propertyCompanyId": "pc_demo001",
    "propertyName": "演示物业公司",
    "period": "month",
    "startDate": "2026-08-01",
    "endDate": "2026-08-31",
    "metrics": {
      "totalOrders": 0,
      "totalRevenue": 100.00,
      "totalCommission": 10.00,
      "totalDeliveryFee": 2.00,
      "totalPointCost": 0.00,
      "totalCoinCost": 0.00,
      "netProfit": 1.20,
      "profitMargin": 0.012
    },
    "breakdown": {
      "productAmount": 100.00,
      "commissionBaseAmount": 10.00,
      "merchantGoodsShare": 90.00,
      "merchantDeliverySubsidy": 0.00,
      "merchantShare": 90.00,
      "distributableAmount": 10.00,
      "platformShare": 1.00,
      "propertyShare": 6.00,
      "managementPoolAmount": 3.00,
      "coordinatorShare": 1.20,
      "sectorLeaderShare": 0.90,
      "individualLeaderShare": 0.90,
      "deficitAmount": 0.00,
      "deficitSponsor": null,
      "originalDeliveryFee": 2.00,
      "deliveryFee": 2.00,
      "deliveryWaiverAmount": 0.00,
      "deliverySubsidySponsor": "none",
      "deliverySubsidyAmount": 0.00,
      "deliverySettlementBase": 2.00,
      "platformDeliveryShare": 0.20,
      "courierEarning": 1.80,
      "calculationVersion": "distribution_b_v1"
    },
    "comparison": {
      "lastPeriodRevenue": 0.00,
      "revenueGrowthRate": 0.00,
      "lastPeriodProfit": 0.00,
      "profitGrowthRate": 0.00
    }
  },
  "timestamp": "2026-08-13 15:23:33"
}
```

**业务规则**（对齐《商品分账与满额配送费减免-移动端对齐规范》§4/§5/§15.1，与后端正式结算同一计算服务）：

- 商品金额与配送费为**两条独立链路**，配送费不进入商品抽佣基数、不用于填补商品平台盘（§2.2）；
- 商品链路：抽佣基础额 = 商品金额 × 抽佣比例；商家商品实得 = 商品金额 − 抽佣基础额；平台盘 = max(抽佣基础额 − 积分成本, 0)；
- 配送链路：原配送费取商家配置，命中满额减免时用户配送费=0、补贴注入配送结算基数（配送劳动不免费，§2.4）；
- 公司净收入（`netProfit`）= 商品平台盘份额 `platformShare` + 公司配送收入 `platformDeliveryShare`（§8.6 两条链路分开统计）；
- `merchantShare = merchantGoodsShare − merchantDeliverySubsidy + merchantAdjustmentAmount`（§4.4）。

---



## 48. 管理端 — 权限管理

**Auth**: Required  
**Roles**: `platform_admin`, `property_admin`

### 48.1 GET `/admin/users` — 账号列表（权限配置页）

**Auth**: Required  
**Roles**: `platform_admin`, `property_admin`  
**权限**: `permission.read`

**说明**：`platform_admin` 返回全平台所有账号，`property_admin` 仅返回本公司下的账号。默认只返回具备管理端工作台身份的账号（物业/平台/社区管理员，或持组长/负责人标签的住户）；传 `adminOnly=false` 可查看全部账号。

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/admin/users?keyword=张&adminOnly=true" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：


| 参数        | 类型       | 必填  | 默认值 | 说明            |
| --------- | -------- | --- | --- | ------------- |
| `keyword` | `String` | ❌   |     | 搜索关键词（姓名/手机号） |
| `adminOnly` | `boolean` | ❌ | `true` | 仅返回管理端工作台账号；`false` 返回全部 |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "accounts": [
      {
        "id": "res_admin01",
        "name": "平台管理员",
        "phone": "138****0000",
        "role": "platform_admin",
        "normalizedRole": "platform_admin",
        "roleCodes": ["platform_admin"],
        "adminWorkspace": true,
        "propertyCompanyId": "pc_demo001",
        "status": "active",
        "effectivePermissionCount": 44
      },
      {
        "id": "res_002",
        "name": "物业管理员",
        "phone": "139****1234",
        "role": "property_admin",
        "normalizedRole": "property_admin",
        "roleCodes": ["property_admin"],
        "adminWorkspace": true,
        "propertyCompanyId": "pc_demo001",
        "status": "active",
        "effectivePermissionCount": 40
      },
      {
        "id": "res_leader01",
        "name": "活动组长郑美丽",
        "phone": "137****5678",
        "role": "resident",
        "normalizedRole": "resident",
        "roleCodes": ["activity_leader", "resident"],
        "adminWorkspace": true,
        "propertyCompanyId": "pc_demo001",
        "status": "active",
        "effectivePermissionCount": 2
      }
    ],
    "total": 3
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**字段说明**：


| 字段                                    | 类型       | 说明                              |
| ------------------------------------- | -------- | ------------------------------- |
| `accounts[].id`                       | `String` | 用户ID                            |
| `accounts[].name`                     | `String` | 姓名                              |
| `accounts[].phone`                    | `String` | 手机号（脱敏）                         |
| `accounts[].role`                     | `String` | 库内原始角色；活动组长等标签身份仍为 `resident` |
| `accounts[].normalizedRole`           | `String` | 归一化角色（历史物业子账号为 `property_admin`） |
| `accounts[].roleCodes`                | `List<String>` | 有效角色码集合（含标签身份，如 `activity_leader`） |
| `accounts[].adminWorkspace`           | `Boolean` | 是否具备管理端工作台身份 |
| `accounts[].propertyCompanyId`        | `String` | 所属物业公司ID                        |
| `accounts[].status`                   | `String` | 状态：`active`/`frozen`/`disabled` |
| `accounts[].effectivePermissionCount` | `int`    | 最终权限数量（已剔除当前身份在管理端不可用的平台专属/物业领导专属码） |


---



### 48.2 GET `/admin/permissions` — 权限列表

**Auth**: Required  
**Roles**: `platform_admin`, `property_admin`（或持有 `permission.read` 权限的账号）  
**权限**: `permission.read`

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/admin/permissions" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "permissions": [
      {
        "id": "perm_043",
        "code": "community:read",
        "name": "社区论坛-查看",
        "module": "community",
        "description": "查看本小区帖子与举报列表",
        "isPlatformOnly": false,
        "grantableToTagIdentities": true,
        "status": "active"
      },
      {
        "id": "perm_044",
        "code": "community:manage",
        "name": "社区论坛-管理",
        "module": "community",
        "description": "删帖与处理举报",
        "isPlatformOnly": false,
        "grantableToTagIdentities": true,
        "status": "active"
      },
      {
        "id": "perm_002",
        "code": "merchant:audit",
        "name": "商家审核",
        "module": "merchant",
        "description": "审核商家入驻申请",
        "isPlatformOnly": false,
        "grantableToTagIdentities": true,
        "status": "active"
      },
      {
        "id": "perm_016",
        "code": "config:set",
        "name": "参数配置",
        "module": "config",
        "description": "物业级参数配置",
        "isPlatformOnly": false,
        "grantableToTagIdentities": false,
        "status": "active"
      }
    ]
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

> `grantableToTagIdentities=true` 表示该权限可授予活动组长/负责人/社区管理员等标签身份。规则：除平台专属（`merchant:read/create/update`、`property_company:manage`）和物业领导专属（`config:set`、`permission:read`、`permission:manage`、`property_operator:manage`）外，其余权限均为 `true`。前端应按目标账号身份过滤：标签身份只展示该字段为 `true` 的权限。
>
> 完整“权限码 → 菜单/tab → 接口”矩阵见 `docs/前端对接_管理端权限与工作台_20261029.md`；44 个权限码均已在 `PermissionInterceptor` 中映射到管理端路由。

---



### 48.3 GET `/admin/role-presets` — 角色预设列表

**Auth**: Required  
**Roles**: `platform_admin`、`property_admin`（或持有 `permission.read` 权限的账号）

**Query 参数**：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `scope` | `String` | ❌ | `platform`：仅内置预设（`propertyCompanyId=null`）；`property`：物业公司自建预设（`platform_admin` 返回全部公司，`property_admin` 仅本公司）；不传：`platform_admin` 返回全部，其它角色返回「内置 + 本公司自建」 |

**成功响应**（`RolePresetListResponse`，`presets` 为数组，非分页结构）：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "presets": [
      {
        "id": "rp_demo001",
        "name": "物业管理员",
        "code": "property_admin",
        "description": "物业公司管理员预设",
        "propertyCompanyId": null,
        "isBuiltin": true,
        "permissionCodes": ["permission.resident.read", "permission.merchant.audit", "permission.report.view"],
        "permissionCount": 3,
        "createdAt": "2026-03-15T02:00:00Z"
      }
    ]
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**响应字段**（`RolePresetItemResponse`）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `String` | 预设ID（前缀 `rp_`） |
| `name` | `String` | 预设名称 |
| `code` | `String` | 预设编码（**不是 `role`**） |
| `description` | `String` | 预设描述 |
| `propertyCompanyId` | `String` | 所属物业公司ID；`null` 表示平台内置预设 |
| `isBuiltin` | `Boolean` | 是否内置（`propertyCompanyId=null` 即 `true`） |
| `permissionCodes` | `List<String>` | 权限编码列表 |
| `permissionCount` | `Integer` | 权限数量 |
| `createdAt` | `String` | 创建时间（ISO-8601 UTC） |

---

### 48.4 POST `/admin/role-presets` — 创建角色预设

**Auth**: Required  
**Roles**: `platform_admin`、`property_admin`（或持有 `permission.manage` 权限的账号）

> `property_admin` 创建时自动归属当前物业公司；`platform_admin` 创建的是**平台内置预设**（`propertyCompanyId=null`、`isBuiltin=true`）。

**请求体**（`CreateRolePresetRequest`）：

| 字段 | 类型 | 必填 | 校验 | 说明 |
| --- | --- | --- | --- | --- |
| `name` | `String` | ✅ | `@NotBlank` | 预设名称，自动 trim |
| `code` | `String` | ❌ | 全局唯一 | 预设编码；不传则由名称生成（小写、非字母数字转下划线，重名自动追加 `_1`、`_2`…） |
| `description` | `String` | ❌ | | 预设描述 |
| `permissionCodes` | `List<String>` | ✅ | `@NotEmpty`；每项须存在且可授予 | 权限编码列表 |

> **旧字段已废弃**：请求体**没有** `role` 字段，旧示例中的 `role` 会被忽略。请改用 `code`（可选）。

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/admin/role-presets  \
  -H "Content-Type: application/json"  \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."  \
  -d '{
    "name": "自定义管理员",
    "code": "custom_admin",
    "description": "前端自定义预设",
    "permissionCodes": ["permission.resident.read", "permission.merchant.read", "permission.report.view"]
  }'
```

**成功响应**：`RolePresetItemResponse`，字段同 §48.3。

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "rp_demo003",
    "name": "自定义管理员",
    "code": "custom_admin",
    "description": "前端自定义预设",
    "propertyCompanyId": "pc_demo001",
    "isBuiltin": false,
    "permissionCodes": ["permission.resident.read", "permission.merchant.read", "permission.report.view"],
    "permissionCount": 3,
    "createdAt": "2026-07-01T07:23:33Z"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---

### 48.5 PUT `/admin/role-presets/{id}` — 更新角色预设

**Auth**: Required  
**Roles**: `platform_admin`、`property_admin`（或持有 `permission.manage` 权限的账号）

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | `String` | ✅ | 角色预设ID（前缀 `rp_`） |

**请求体**（`UpdateRolePresetRequest`，全部可选）：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `name` | `String` | ❌ | 预设名称 |
| `code` | `String` | ❌ | 预设编码，需全局唯一，重复返回 `10002` |
| `description` | `String` | ❌ | 预设描述 |
| `permissionCodes` | `List<String>` | ❌ | 权限编码列表；传入即**全量替换** |

**内置预设限制**：`isBuiltin=true` 的预设**仅允许修改 `description`**；同时传 `name` / `code` / `permissionCodes` 会返回 `10002`「内置预设仅可更新描述」。

**请求示例**：

```bash
curl -X PUT https://test.yuanxusoftware.top/api/v1/admin/role-presets/rp_demo003  \
  -H "Content-Type: application/json"  \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."  \
  -d '{
    "name": "自定义管理员（更新）",
    "permissionCodes": ["permission.resident.read", "permission.merchant.read", "permission.report.view", "permission.announcement.manage"]
  }'
```

**成功响应**：`RolePresetItemResponse`，字段同 §48.3（**无 `updatedAt` 字段**）。

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "rp_demo003",
    "name": "自定义管理员（更新）",
    "code": "custom_admin",
    "description": "前端自定义预设",
    "propertyCompanyId": "pc_demo001",
    "isBuiltin": false,
    "permissionCodes": ["permission.resident.read", "permission.merchant.read", "permission.report.view", "permission.announcement.manage"],
    "permissionCount": 4,
    "createdAt": "2026-07-01T07:23:33Z"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---

### 48.6 GET `/admin/users/{id}/permissions` — 获取用户权限

**Auth**: Required  
**Roles**: `platform_admin`, `property_admin`

**路径参数**：


| 参数   | 类型       | 必填  | 说明   |
| ---- | -------- | --- | ---- |
| `id` | `String` | ✅   | 用户ID |


**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/admin/users/res_admin01/permissions" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "accountId": "res_leader01",
    "accountName": "活动组长郑美丽",
    "roleCodes": ["activity_leader", "resident"],
    "appliedPreset": {
      "id": "rp_003",
      "name": "活动组组长",
      "code": "activity_leader"
    },
    "overrides": [
      {
        "permissionCode": "community:read",
        "granted": true,
        "source": "custom",
        "operatorId": "res_admin01",
        "updatedAt": "2026-10-29T08:00:00Z"
      }
    ],
    "effectivePermissions": ["community:read"],
    "effectivePermissionCount": 1,
    "updatedAt": "2026-10-29T08:00:00Z"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

> `appliedPreset` 按 `roleCodes` 命中的预设返回：活动组长为 `rp_003`，统筹为 `rp_001`；不再按原始 `residents.role` 查询（活动组长原始角色是 `resident`，按原始角色会误显示“无预设”）。
>
> `overrides` 是账号级覆盖（grant/revoke），`effectivePermissions` 是“预设 ∪ 授权 − 撤销”的最终结果，并已剔除管理端不可用的平台专属/物业领导专属码。前端不要自行再算一遍，菜单和按钮直接使用该字段。

---



### 48.7 POST `/admin/users/{id}/permissions/grant` — 授予权限

**Auth**: Required  
**Roles**: `platform_admin`、`property_admin`（或持有 `permission.manage` 权限的账号）

> 目标账号必须具备管理端工作台身份（物业/平台/社区管理员，或持组长/负责人标签的住户）。
> 标签身份可授予除平台专属、物业领导专属外的全部权限（如 `merchant:audit`、`promotion:push`、`reports:view`、`delivery:grab`）；
> 平台专属（`merchant:read/create/update`、`property_company:manage`）和物业领导专属（`config:set`、`permission:read/manage`、`property_operator:manage`）返回 `10002 PARAM_INVALID`。
> 普通住户返回 `10002 PARAM_INVALID`。

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/admin/users/res_b7m9x1/permissions/grant \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "permissionCodes": ["merchant:audit", "reports:view"]
  }'
```

**请求体**：


| 字段                | 类型             | 必填  | 说明                |
| ----------------- | -------------- | --- | ----------------- |
| `permissionCodes` | `List<String>` | ✅   | 权限码列表（`模块:动作` 格式） |
| `reason`          | `String`       | ❌   | 授权原因              |


---



### 48.8 POST `/admin/users/{id}/permissions/revoke` — 撤销权限

**Auth**: Required  
**Roles**: `platform_admin`、`property_admin`（或持有 `permission.manage` 权限的账号）

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | `String` | ✅ | 用户（住户）ID，前缀 `res_` |

**请求体**（`RevokePermissionsRequest`）：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `permissionCodes` | `List<String>` | ✅ | 要撤销的权限编码列表，不能为空 |
| `reason` | `String` | ❌ | 撤销原因，写入权限变更日志 |

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/admin/users/res_b7m9x1/permissions/revoke  \
  -H "Content-Type: application/json"  \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."  \
  -d '{
    "permissionCodes": ["merchant:audit"],
    "reason": "员工离职"
  }'
```

---

### 48.9 GET `/admin/permission-change-logs` — 权限变更日志

**Auth**: Required  
**Roles**: `platform_admin`, `property_admin`

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/admin/permission-change-logs?page=1&pageSize=20&userId=res_admin01&startDate=2026-06-01&endDate=2026-06-30" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：


| 参数          | 类型       | 必填  | 默认值          | 说明   |
| ----------- | -------- | --- | ------------ | ---- |
| `page`      | `int`    | ❌   | `1`          | 页码   |
| `pageSize`  | `int`    | ❌   | `20`         | 每页条数 |
| `userId`    | `String` | ❌   |              | 用户ID |
| `startDate` | `String` | ❌   |              | 开始日期 |
| `endDate`   | `String` | ❌   |              | 结束日期 |
| `sort`      | `String` | ❌   | `-createdAt` | 排序   |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "pcl_demo001",
        "residentId": "res_admin01",
        "residentName": "管理员",
        "operatorId": "res_admin02",
        "operatorName": "超级管理员",
        "action": "grant",
        "permissionCode": "coordinator:manage",
        "permissionName": "统筹管理",
        "presetCode": null,
        "beforeValue": "false",
        "afterValue": "true",
        "reason": "需要管理统筹负责人",
        "createdAt": "2026-06-01 10:00:00"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 5,
      "totalPages": 1
    }
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



## 49. 管理端 — 业主物业币管控

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

### 49.1 GET `/admin/coin-freeze-records` — 冻结记录列表

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/admin/coin-freeze-records?page=1&pageSize=20&action=freeze" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：


| 参数                  | 类型       | 必填  | 默认值          | 说明                               |
| ------------------- | -------- | --- | ------------ | -------------------------------- |
| `page`              | `int`    | ❌   | `1`          | 页码                               |
| `pageSize`          | `int`    | ❌   | `20`         | 每页条数                             |
| `residentId`        | `String` | ❌   |              | 业主ID筛选                           |
| `action`            | `String` | ❌   | `freeze`     | `freeze`（冻结记录）/ `unfreeze`（解冻记录） |
| `propertyCompanyId` | `String` | ❌   |              | 物业公司ID（超管必传）                     |
| `sort`              | `String` | ❌   | `-createdAt` | 排序                               |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "cfr_demo001",
        "residentId": "res_b7m9x1",
        "residentName": "张先生",
        "residentPhone": "138****1234",
        "residentBuilding": "3栋",
        "residentRoom": "502",
        "action": "freeze",
        "amount": "50.00",
        "reason": "异常操作",
        "operatorId": "res_admin01",
        "createdAt": "2026-07-01 15:23:33"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 1,
      "totalPages": 1
    }
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 49.2 POST `/admin/residents/{id}/coin/freeze` — 冻结业主物业币

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/admin/residents/res_b7m9x1/coin/freeze \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "amount": 50.00,
    "reason": "异常操作"
  }'
```

**请求体**：


| 字段       | 类型           | 必填  | 校验                                | 说明           |
| -------- | ------------ | --- | --------------------------------- | ------------ |
| `amount` | `BigDecimal` | ✅   | `@NotNull @DecimalMin("0")`       | 冻结金额（0=全部冻结） |
| `reason` | `String`     | ✅   | `@NotBlank @Size(min=1, max=200)` | 冻结原因         |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "cfr_demo001",
    "residentId": "res_b7m9x1",
    "residentName": "张先生",
    "action": "freeze",
    "amount": 50.00,
    "reason": "异常操作",
    "operatorId": "res_admin01",
    "operatorName": "管理员",
    "frozenAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 49.3 POST `/admin/residents/{id}/coin/unfreeze` — 解冻业主物业币

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/admin/residents/res_b7m9x1/coin/unfreeze \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "frozenRecordId": "cfr_demo001",
    "reason": "已核实，解除冻结"
  }'
```

**请求体**：


| 字段               | 类型       | 必填  | 校验                                | 说明             |
| ---------------- | -------- | --- | --------------------------------- | -------------- |
| `frozenRecordId` | `String` | ❌   |                                   | 冻结记录ID（不传解冻全部） |
| `reason`         | `String` | ✅   | `@NotBlank @Size(min=1, max=200)` | 解冻原因           |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "residentId": "res_b7m9x1",
    "unfrozenAmount": 50.00,
    "newBalance": 149.50,
    "unfrozenAt": "2026-07-01 15:23:33",
    "operatorId": "res_admin01"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



## 50. 管理端 — 物业币过期管理

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

### 50.1 POST `/admin/coin-expiry/run` — 执行物业币过期清零

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/admin/coin-expiry/run \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "expiryDate": "2026-07-01",
    "dryRun": false,
    "propertyCompanyId": "pc_demo001"
  }'
```

**请求体**：


| 字段                  | 类型             | 必填  | 校验          | 说明               |
| ------------------- | -------------- | --- | ----------- | ---------------- |
| `expiryDate`        | `String`       | ✅   | `@NotBlank` | 过期日期（yyyy-MM-dd） |
| `dryRun`            | `Boolean`      | ❌   |             | 仅预览不执行（默认false）  |
| `residentIds`       | `List<String>` | ❌   |             | 指定业主ID列表（不传则全部）  |
| `propertyCompanyId` | `String`       | ❌   |             | 物业公司ID（不传则全部）    |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "taskId": "task_demo001",
    "expiryDate": "2026-07-01",
    "dryRun": false,
    "processedCount": 15,
    "totalExpiredAmount": 350.00,
    "clearedToPool": 350.00,
    "status": "completed",
    "executedAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 50.2 GET `/admin/coin-expiry/records` — 物业币过期记录

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**Query 参数**：


| 参数                  | 类型       | 必填  | 默认值  | 说明     |
| ------------------- | -------- | --- | ---- | ------ |
| `page`              | `int`    | ❌   | `1`  | 页码     |
| `pageSize`          | `int`    | ❌   | `20` | 每页条数   |
| `residentId`        | `String` | ❌   |      | 业主ID筛选 |
| `startDate`         | `String` | ❌   |      | 开始日期   |
| `endDate`           | `String` | ❌   |      | 结束日期   |
| `propertyCompanyId` | `String` | ❌   |      | 物业公司ID |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "cer_demo001",
        "residentId": "res_b7m9x1",
        "residentName": "张先生",
        "propertyCompanyId": "pc_demo001",
        "expiredAmount": 20.00,
        "poolInAmount": 20.00,
        "expiredAt": "2026-07-01 00:00:00"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 1,
      "totalPages": 1
    }
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



## 51. 管理端 — 物业公司管理

**Auth**: Required  
**Roles**: `platform_admin`

### 51.1 GET `/admin/property-companies` — 物业公司列表

**Query 参数**：


| 参数         | 类型       | 必填  | 默认值          | 说明                  |
| ---------- | -------- | --- | ------------ | ------------------- |
| `page`     | `int`    | ❌   | `1`          | 页码                  |
| `pageSize` | `int`    | ❌   | `20`         | 每页条数                |
| `keyword`  | `String` | ❌   |              | 搜索关键词（名称/电话）        |
| `status`   | `String` | ❌   |              | `active`/`inactive` |
| `sort`     | `String` | ❌   | `-createdAt` | 排序                  |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "pc_demo001",
        "name": "演示物业公司",
        "logoUrl": "https://cdn.example.com/logo/pc_demo001.png",
        "contactPhone": "138****0001",
        "address": "盘锦市XX区",
        "status": "active",
        "communityCount": 3,
        "stats": {
          "merchantCount": 15,
          "residentCount": 1200
        },
        "createdAt": "2026-01-01 00:00:00",
        "updatedAt": "2026-06-28 14:30:00"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 1,
      "totalPages": 1
    }
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 51.2 POST `/admin/property-companies` — 创建物业公司

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/admin/property-companies \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "name": "新物业公司",
    "contactPhone": "13812345678",
    "address": "盘锦市XX区",
    "communities": [
      { "name": "XX花园", "address": "XX路1号", "totalBuildings": 10, "totalUnits": 200 }
    ],
    "adminAccount": {
      "name": "管理员",
      "phone": "13900001111",
      "email": "admin@example.com"
    }
  }'
```

**请求体**：


| 字段                             | 类型        | 必填  | 校验                 | 说明         |
| ------------------------------ | --------- | --- | ------------------ | ---------- |
| `name`                         | `String`  | ✅   | `@NotBlank`        | 物业公司名称     |
| `logoUrl`                      | `String`  | ❌   |                    | Logo URL   |
| `contactPhone`                 | `String`  | ✅   | `@NotBlank`        | 联系电话         |
| `address`                      | `String`  | ❌   |                    | 地址         |
| `communities`                  | `List`    | ✅   | `@NotEmpty @Valid` | 小区列表（至少1个） |
| `communities[].name`           | `String`  | ✅   | `@NotBlank`        | 小区名称       |
| `communities[].address`        | `String`  | ❌   |                    | 小区地址       |
| `communities[].totalBuildings` | `Integer` | ❌   |                    | 楼栋总数       |
| `communities[].totalUnits`     | `Integer` | ❌   |                    | 单元总数       |
| `adminAccount`                 | `Object`  | ✅   | `@Valid`           | 管理员账号      |
| `adminAccount.name`            | `String`  | ✅   | `@NotBlank`        | 管理员姓名      |
| `adminAccount.phone`           | `String`  | ✅   | `@NotBlank`        | 管理员手机号     |
| `adminAccount.email`           | `String`  | ❌   |                    | 管理员邮箱      |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "pc_demo002",
    "name": "新物业公司",
    "status": "active",
    "communities": [
      { "id": "com_demo002", "name": "XX花园" }
    ],
    "adminAccount": {
      "id": "res_admin02",
      "name": "管理员",
      "phone": "139****1111",
      "initialPassword": "A7kP2mQ9xR4tZ6"
    },
    "defaultConfigs": "已应用默认配置",
    "createdAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

> **2026-09-06 同步**：与 `POST /property-companies`（§14.3）共用同一套校验与创建逻辑：重名/手机号占用返回 400 而非 500；创建成功后初始管理员密码为随机 14 位，仅在 `adminAccount.initialPassword` 返回一次；请求中的 Query `propertyCompanyId` 被忽略。



### 51.3 GET `/admin/property-companies/{id}` — 物业公司详情

**成功响应**：包含完整的物业公司信息、分成比例、物业币配置、小区列表、管理员列表。

### 51.3.1 POST `/admin/property-companies/{id}/admin-accounts/{accountId}/reset-password` — 重置物业管理员密码（v8.28 新增）

**Auth**: Required
**Roles**: `platform_admin`

用于创建物业公司时初始密码丢失的场景。重置成功后旧密码立即失效，同时递增账号 `tokenVersion`，该管理员已签发的 access/refresh token 全部失效，需要重新登录。

**路径参数**：

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| `id` | String | 物业公司 ID（`pc_` 前缀） |
| `accountId` | String | 管理员账号 ID（`residents.id`，从详情 `adminAccounts[].id` 获取） |

**请求体**：无。

**成功响应**：`AdminAccountResponse`，其中 `initialPassword` 为新随机 14 位密码，**仅本次响应返回一次**，请前端立即展示给操作人，不要持久化存储。

```json
{
  "code": 0,
  "data": {
    "id": "res_admin001",
    "name": "张三",
    "phone": "13800138000",
    "role": "property_admin",
    "initialPassword": "A7kP2mQ9xR4tZ6"
  }
}
```

**错误**：

| 错误码 | HTTP | 场景 |
| --- | --- | --- |
| `20004` | 403 | 调用者不是 `platform_admin` |
| `97001` | 404 | 物业公司不存在 |
| `30001` | 404 | 管理员账号不存在、不属于该物业或不是 `property_admin` |

---



### 51.4 PUT `/admin/property-companies/{id}` — 更新物业公司信息

**Auth**: Required  
**Roles**: `property_admin`（仅本公司）、`platform_admin`

> **重要**：§51.4 PUT、§51.5 PATCH `/{id}/status`、§51.6 PATCH `/{id}/config` 三个接口**共用同一个请求 DTO** `UpdatePropertyCompanyRequest`；所有字段均可选，仅更新非 `null` 字段。区别只是调用路径与语义侧重，字段全集见下表。

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | `String` | ✅ | 物业公司ID（`pc_`） |

**请求体**（`UpdatePropertyCompanyRequest`）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `name` | `String` | 名称 |
| `logoUrl` | `String` | Logo URL |
| `contactPhone` | `String` | 联系电话 |
| `address` | `String` | 地址 |
| `status` | `String` | `active`/`inactive`/`pending`；**仅 `platform_admin` 可写**，物业侧传入返回 `20004` |
| `platformShareRate` | `BigDecimal` | 平台（我们公司）分成比例；**仅 `platform_admin` 可写** |
| `propertyShareRate` | `BigDecimal` | 物业公司分成比例（一级三档之一）；**仅 `platform_admin` 可写** |
| `coordinatorShareRate` | `BigDecimal` | 管理盘（统筹）分成比例（一级三档之一）；**仅 `platform_admin` 可写** |
| `sectorLeaderRate` | `BigDecimal` | 板块负责人比例（管理盘内部系数）；**仅 `platform_admin` 可写** |
| `individualLeaderRate` | `BigDecimal` | 个体负责人比例（管理盘内部系数）；**仅 `platform_admin` 可写** |
| `userPointShareRate` | `BigDecimal` | 🆕 V36 用户积分预算比例（占抽佣基础额，0~1，默认 `0.10`）；**仅 `platform_admin` 可写** |
| `userRewardShareRate` | `BigDecimal` | 🆕 V36 用户奖励金预算比例（占抽佣基础额，0~1，默认 `0.10`）；**仅 `platform_admin` 可写** |
| `pointEnabled` | `Boolean` | 积分功能开关；平台或物业领导（本物业）可写 |
| `coinEnabled` | `Boolean` | 物业币功能开关；平台或物业领导（本物业）可写 |
| `coinDisplayEnabled` | `Boolean` | 物业币卡片全局显示 |
| `coinIssueMode` | `String` | `auto`/`manual` |
| `coinExpiryDays` | `Integer` | 物业币有效期天数 |
| `coinFreezeDefault` | `Boolean` | 新用户物业币默认冻结 |
| `deliveryBaseFee` | `BigDecimal` | 配送起步价 |
| `deliveryCourierShareRate` | `BigDecimal` | 快递员配送费分成比例（历史兼容） |
| `deliveryCourierPerOrder` | `BigDecimal` | 快递员每单固定配送费（历史兼容） |
| `withdrawalFeeRate` | `BigDecimal` | 提现手续费率；**仅 `platform_admin` 可写** |
| `pointToFeeRate` | `BigDecimal` | 积分抵扣物业费比率 |
| `twoYearClearEnabled` | `Boolean` | 2 年欠费积分清零开关 |
| `pointTransferEnabled` | `Boolean` | 是否允许积分转物业抵扣物业费 |
| `pointTransferMinYears` | `Integer` | 积分转物业所需最低入住年限 |
| `neighborDailyContactLimit` | `Integer` | 邻居私聊每日限制 |

> **写权限（2026-10-30 前端确认，2026-11-03 补两档）**：
> - `platformShareRate` / `propertyShareRate` / `coordinatorShareRate` / `sectorLeaderRate` / `individualLeaderRate` / 🆕 `userPointShareRate` / 🆕 `userRewardShareRate` / `withdrawalFeeRate` 仅 `platform_admin` 可写；物业领导传入返回 `403 20004`，请在前端置灰。
> - 其他配置字段（含 `pointEnabled` / `coinEnabled` / `coinExpiryDays` / `coinMall*` / `coinUseCondition` / `pointToFeeRate` / `twoYearClearEnabled` / `pointTransfer*` / `neighborDailyContactLimit` 等）要求 `config:set`；物业操作员（`property_operator`，含 `role=property_admin + propertySubRole=property_operator`）不可写。
> - 物业公司名称 / Logo / 联系电话 / 地址等资料字段不要求 `config:set`。

**分成校验**：仅当请求携带 `platformShareRate` / `propertyShareRate` / `coordinatorShareRate` 任一字段时，校验一级三档合计为 `1.0000`（容差 `0.0001`）；失败返回 `400 / 97006`。`sectorLeaderRate` / `individualLeaderRate` 仅各自校验 `[0,1]`，不做相加校验。

🆕 **用户两档（V36）校验**：各自校验 `[0,1]`（越界 `10002`）；两档**合计 > 1.0000** 时返回 `400 / 10002`，文案「用户积分与用户奖励金预算比例合计不得超过100%（当前为 120%）」。这两档不进「一级三档合计 100%」的校验，它们是从抽佣基础额里先切走的，不是平台盘的分配比例。

> **读口径提示**：`GET /admin/property-companies/{id}` 的详情响应（`PropertyCompanyDetailResponse`）**不含** `userPointShareRate` / `userRewardShareRate`；需要读这两个比例请调 §71.1 `GET /admin/platform-share-rates`。

> **旧字段已废弃**：请求体**没有** `residentPointShareRate` / `merchantPointShareRate` / `coinPointShareRate` / `sharedPointShareRate`，传入会被忽略；分成调整请使用上表字段或走 §65 价格审批。

---

### 51.5 PATCH `/admin/property-companies/{id}/status` — 更新物业公司状态

**Auth**: Required  
**Roles**: `platform_admin`（仅平台管理员可变更状态）

> **2026-09-08 变更**：与「删除物业公司」两档语义对齐（见 §94.14）。停用（`inactive`）后该物业不可再经营，
> 住户端 `GET /property-companies/current` 会返回 `20005 账号被禁用`（message=「物业公司已停用」）。
> 本接口**不触发**分账比例三档校验；停用/启用不会因历史比例数据被拦截。
> 停用后：该物业的**管理端账号**调用 `POST /auth/admin-login` 会被拒绝，返回 `403 20005`；平台超管不受影响。

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | `String` | ✅ | 物业公司ID（`pc_`） |

**请求体**：与 §51.4 共用 `UpdatePropertyCompanyRequest`，本接口通常只传：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `status` | `String` | 建议 | `active`/`inactive`；不传则状态不变 |

> 其它字段若一并传入也会按 §51.4 规则生效（`platform_admin` 才可改状态/平台分成比例）。

**成功响应**：`PropertyCompanyDetailResponse`（完整详情，字段见 §51.3）。

---

### 51.6 PATCH `/admin/property-companies/{id}/config` — 更新物业公司配置

**Auth**: Required  
**Roles**: `property_admin`（仅本公司）、`platform_admin`

> **2026-09-08 变更（前端问题：分账比例旧校验「物业+统筹=100%」）**
>
> 商品平台盘**一级三档**合计必须为 100%（容差 `0.0001`）：
>
> ```text
> platformShareRate + propertyShareRate + coordinatorShareRate = 1.0000
> ```
>
> - 校验时机：**仅当本次请求携带 `platformShareRate` / `propertyShareRate` / `coordinatorShareRate`
>   任一字段时**才校验；保存配送费、状态等无关字段不受影响（兼容历史数据）。
> - 失败返回 **`400 / 97006`**，message 形如
>   「商品平台盘一级分成比例合计必须为100%（我们公司 10% + 物业 60% + 管理盘 40% = 110%）」。
> - **已删除**旧校验「物业分成比例与统筹分成比例之和须等于1」。
> - **已删除**旧校验「板块与个体负责人比例之和不能超过1」；`sectorLeaderRate` /
>   `individualLeaderRate` 为管理盘内部的级联系数，仅各自校验落在 `[0,1]`。
> - `platformShareRate`（我们公司）可由本接口写入，**仅 `platform_admin` 可修改**；
>   `property_admin` 传入会返回 `403 20004`。校验时未传该字段则取已落库值参与三档合计。

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | `String` | ✅ | 物业公司ID（`pc_`） |

**请求体**：与 §51.4 共用 `UpdatePropertyCompanyRequest`，本接口常用配置字段如下（完整字段见 §51.4）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `platformShareRate` | `BigDecimal` | 平台分成比例（仅 `platform_admin` 可写） |
| `propertyShareRate` | `BigDecimal` | 物业公司分成比例；**仅 `platform_admin` 可写** |
| `coordinatorShareRate` | `BigDecimal` | 管理盘（统筹）分成比例；**仅 `platform_admin` 可写** |
| `sectorLeaderRate` | `BigDecimal` | 板块负责人比例；**仅 `platform_admin` 可写** |
| `individualLeaderRate` | `BigDecimal` | 个体负责人比例；**仅 `platform_admin` 可写** |
| `pointEnabled` | `Boolean` | 积分功能开关；平台或物业领导（本物业）可写 |
| `coinEnabled` | `Boolean` | 物业币功能开关；平台或物业领导（本物业）可写 |
| `coinDisplayEnabled` | `Boolean` | 物业币卡片全局显示 |
| `coinIssueMode` | `String` | `auto`/`manual` |
| `coinExpiryDays` | `Integer` | 物业币有效期天数 |
| `coinFreezeDefault` | `Boolean` | 新用户物业币默认冻结 |
| `deliveryBaseFee` | `BigDecimal` | 配送起步价 |
| `deliveryCourierShareRate` | `BigDecimal` | 快递员配送费分成比例 |
| `deliveryCourierPerOrder` | `BigDecimal` | 快递员每单固定配送费 |
| `withdrawalFeeRate` | `BigDecimal` | 提现手续费率；**仅 `platform_admin` 可写** |
| `pointToFeeRate` | `BigDecimal` | 积分抵扣物业费比率 |
| `twoYearClearEnabled` | `Boolean` | 2 年欠费积分清零开关 |
| `pointTransferEnabled` | `Boolean` | 是否允许积分转物业抵扣物业费 |
| `pointTransferMinYears` | `Integer` | 积分转物业所需最低入住年限 |
| `neighborDailyContactLimit` | `Integer` | 邻居私聊每日限制 |

> **权限（2026-10-30 前端确认）**：本接口要求 `config:set`；`platformShareRate` / `propertyShareRate` / `coordinatorShareRate` / `sectorLeaderRate` / `individualLeaderRate` / `withdrawalFeeRate` 仅 `platform_admin` 可写，物业领导只读。物业操作员（含 `propertySubRole=property_operator`）返回 `403 20004`。

**成功响应**：`PropertyCompanyDetailResponse`（完整详情，字段见 §51.3）。

---

### 51.7 GET `/admin/settlement-config` — 查询结算配置

**Auth**: Required  
**Roles**: `platform_admin`、`property_admin`（只读）

**Query 参数**：无（平台级全局配置）

**成功响应**（`SettlementConfigResponse`）：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "wechatSplitEnabled": false,
    "ledgerFallbackEnabled": true,
    "autoWithdrawalEnabled": false,
    "platformWithdrawalFeeEnabled": true,
    "platformWithdrawalFeeRate": 0.0060,
    "platformWithdrawalFeeShareRate": 1.0000,
    "feeForMerchant": true,
    "feeForRole": true,
    "feeForProperty": true,
    "feeForCoin": true
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**响应字段**：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `wechatSplitEnabled` | `Boolean` | 真分账（微信）模式开关 |
| `ledgerFallbackEnabled` | `Boolean` | 记账兜底模式开关（平台持币代付） |
| `autoWithdrawalEnabled` | `Boolean` | 自动提现开关 |
| `platformWithdrawalFeeEnabled` | `Boolean` | 平台提现手续费总开关 |
| `platformWithdrawalFeeRate` | `BigDecimal` | 平台提现手续费率（0~1，如 `0.0060` 表示千分之六） |
| `platformWithdrawalFeeShareRate` | `BigDecimal` | 平台提现手续费分成比例（0~1，`1.0000` 表示平台独占） |
| `feeForMerchant` | `Boolean` | 商家提现是否收取平台手续费 |
| `feeForRole` | `Boolean` | 角色（自然人）提现是否收取平台手续费 |
| `feeForProperty` | `Boolean` | 物业公司提现是否收取平台手续费 |
| `feeForCoin` | `Boolean` | 居民（积分）提现是否收取平台手续费 |

---

### 51.8 PUT `/admin/settlement-config` — 更新结算配置

**Auth**: Required  
**Roles**: **仅 `platform_admin`**（`property_admin` 只读，调用返回 `20004`）

**请求体**（`UpdateSettlementConfigRequest`，全部可选，仅更新非 `null` 字段）：

| 字段 | 类型 | 必填 | 校验 |
| --- | --- | --- | --- |
| `wechatSplitEnabled` | `Boolean` | ❌ | 与 `ledgerFallbackEnabled` **不可同时为 false**（合并后校验，至少启用一种） |
| `ledgerFallbackEnabled` | `Boolean` | ❌ | 同上 |
| `autoWithdrawalEnabled` | `Boolean` | ❌ | |
| `platformWithdrawalFeeEnabled` | `Boolean` | ❌ | |
| `platformWithdrawalFeeRate` | `BigDecimal` | ❌ | 0~1，越界返回 `10002` |
| `platformWithdrawalFeeShareRate` | `BigDecimal` | ❌ | 0~1，越界返回 `10002` |
| `feeForMerchant` | `Boolean` | ❌ | |
| `feeForRole` | `Boolean` | ❌ | |
| `feeForProperty` | `Boolean` | ❌ | |
| `feeForCoin` | `Boolean` | ❌ | |

**请求示例**：

```bash
curl -X PUT https://test.yuanxusoftware.top/api/v1/admin/settlement-config  \
  -H "Content-Type: application/json"  \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."  \
  -d '{
    "wechatSplitEnabled": true,
    "ledgerFallbackEnabled": false,
    "platformWithdrawalFeeRate": 0.0060
  }'
```

**成功响应**：`SettlementConfigResponse`，字段同 §51.7（返回更新后的完整配置）。

---

### 51.8a GET `/admin/order-config` — 获取订单配置

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

获取商家核实/冻结期/退款窗口等订单配置参数。

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "merchantVerificationEnabled": true,
    "merchantVerificationHours": 24,
    "freezeDays": 7,
    "refundWindowDays": 3
  },
  "timestamp": "2026-08-21 17:21:02"
}
```

**字段说明**：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `merchantVerificationEnabled` | `Boolean` | （v6.5 起失效，保留兼容）原商家核实总开关；现核实入口由订单商品分类=团购决定，本字段不再控制核实流程 |
| `merchantVerificationHours` | `Integer` | 商家核实时限（小时），默认 24，范围 1-168（仍生效，控制团购单核实时限） |
| `freezeDays` | `Integer` | 冻结期（天），默认 7，范围 1-30 |
| `refundWindowDays` | `Integer` | 退款窗口期（天），默认 3，范围 1-30，不能超过冻结期 |

---

### 51.8b PUT `/admin/order-config` — 更新订单配置

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

更新商家核实/冻结期/退款窗口等订单配置参数。仅更新非 null 字段。

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `merchantVerificationEnabled` | `Boolean` | ❌ | （v6.5 起失效，保留兼容）原商家核实总开关，不再控制核实入口 |
| `merchantVerificationHours` | `Integer` | ❌ | 商家核实时限（小时），范围 1-168（仍生效） |
| `freezeDays` | `Integer` | ❌ | 冻结期（天），范围 1-30 |
| `refundWindowDays` | `Integer` | ❌ | 退款窗口期（天），范围 1-30，不能超过冻结期 |

**请求示例**：

```bash
curl -X PUT https://test.yuanxusoftware.top/api/v1/admin/order-config \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "merchantVerificationEnabled": true,
    "merchantVerificationHours": 24,
    "freezeDays": 7,
    "refundWindowDays": 3
  }'
```

**成功响应**：同 GET `/admin/order-config`。

**业务规则**：

- `refundWindowDays` 不能超过 `freezeDays`。
- 冻结期满后不可再申请退货退款。

---

### 51.9 GET `/admin/platform-earnings/balance` — 平台收益余额

**Auth**: Required  
**Roles**: `platform_admin`

**Query 参数**：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `propertyCompanyId` | `String` | ❌ | 物业公司ID；不传则统计全部物业 |

**成功响应**（`PlatformEarningsBalanceResponse`，只读对账快照）：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "totalEarned": 5350.00,
    "settledBalance": 5350.00,
    "pendingAmount": 0.00,
    "settleMode": "wechat_profit_sharing",
    "settlementStatus": "已实时入账",
    "withdrawAvailable": false,
    "distributionShare": 5000.00,
    "deliveryShare": 200.00,
    "withdrawalFeeShare": 150.00
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**字段说明**：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `totalEarned` | `BigDecimal` | 累计收益（订单分成 + 配送费分成 + 提现手续费分成） |
| `settledBalance` | `BigDecimal` | 平台已入账余额 |
| `pendingAmount` | `BigDecimal` | 处理中金额（真分账直入账模式下通常为 0） |
| `settleMode` | `String` | 结算方式，如 `wechat_profit_sharing` |
| `settlementStatus` | `String` | 结算状态文案 |
| `withdrawAvailable` | `Boolean` | 是否支持发起提现；真分账模式为 `false`，前端据此隐藏提现按钮 |
| `distributionShare` | `BigDecimal` | 构成：订单分成 |
| `deliveryShare` | `BigDecimal` | 构成：配送费分成 |
| `withdrawalFeeShare` | `BigDecimal` | 构成：提现手续费分成 |

---

### 51.10 GET `/admin/property-companies/{id}/settlement-balance` — 物业公司结算余额

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**说明**：查询指定物业公司的结算余额（已结算-已提现）。

> **（2026-10-06）** 该接口此前**后端未实现**（调用返回 404），现已补齐（`PropertyCompanySettlementService.getBalance`）。

---

### 51.11 GET/POST `/admin/property-companies/{id}/settlement-withdrawals` — 物业公司结算提现

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | `String` | ✅ | 物业公司ID（`pc_`） |

**GET Query 参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `auditStatus` | `String` | ❌ | | `pending`/`approved`/`rejected`/`completed` |
| `startDate` | `String` | ❌ | | 开始日期 |
| `endDate` | `String` | ❌ | | 结束日期 |

**GET 成功响应**：`PageResponse<RoleWithdrawalItemResponse>`（字段见 §51.12 响应说明）。

**POST 请求体**（`ApplySettlementWithdrawalRequest`）：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `amount` | `BigDecimal` | ✅ | 提现金额（元），须 > 0 且 ≤ 分成账户余额 |

**POST 说明**：落库 `withdrawal_records`（`withdrawal_type=property_company`），手续费按物业公司 `withdrawal_fee_rate`（未配置取平台默认物业类费率）计算，初始状态 `pending` 待平台审核。**响应**：单条 `RoleWithdrawalItemResponse`。

---

### 51.12 POST `/admin/property-companies/{id}/settlement-withdrawals/{withdrawalId}/approve` — 审批提现

**Auth**: Required  
**Roles**: `platform_admin`

**说明**：审核通过，将提现金额从物业公司分成账户余额（`settlement_balance`）中扣减，状态置 `approved` 并写 `completedAt`。仅 `pending` 状态可审批；余额不足返回业务错误。**响应**：单条 `RoleWithdrawalItemResponse`。

---

### 51.13 POST `/admin/property-companies/{id}/settlement-withdrawals/{withdrawalId}/reject` — 拒绝提现

**Auth**: Required  
**Roles**: `platform_admin`

**说明**：审核拒绝，状态置 `rejected`（不扣减余额）。仅 `pending` 状态可审批。**响应**：单条 `RoleWithdrawalItemResponse`。

---

### 51.14 POST `/admin/merchants/{id}/coin/earn` — 给商家发放物业币

> ⚠️ **【当前未实现 · 2026-10-27 核对代码】** 后端不存在 `POST /admin/merchants/{id}/coin/earn`。管理端向商家发放物业币暂无入口，请勿按本节联调。以下为原设计文档，保留备查。

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**说明**：管理端手动给商家发放物业币（佣金返现或奖励场景）。2026-10-25 起发放统一进入商家账号的**个人物业币余额**，不再进入家庭池。

---

# 物业联系方式接口

### GET `/property-contact` — 物业联系方式（前端公开）

**Auth**: Optional（匿名可访问）

**说明**：获取物业对外联系方式。`enabled=false` 或未配置时 `data=null`。前端在用户未绑定楼栋时调用并展示。

**成功响应**（`PropertyContactPublicResponse`）：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "propertyName": "阳光花园物业服务中心",
    "contactPhone": "0571-88886666",
    "contactMobile": "13800000000",
    "address": "浙江省杭州市西湖区阳光路1号",
    "serviceHours": "周一至周日 8:00-20:00",
    "email": "service@sunshine-property.com",
    "enabled": true
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

> **旧字段已废弃**：`phone`、`businessHours`、`wechat` 不在响应中；请改用 `contactPhone`、`serviceHours`。`remark`/`id`/时间戳仅管理端可见。

### GET/POST `/admin/property-contact` — 物业联系方式（管理端）

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

| 方法 | 说明 |
| --- | --- |
| GET | 查询当前物业公司联系方式配置 |
| POST | 创建/更新物业联系方式配置（全局单条，已存在则覆盖更新） |

**POST 请求体**（`UpsertPropertyContactRequest`）：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `propertyName` | `String` | ✅ | 物业名称，不能为空 |
| `contactPhone` | `String` | ✅ | 联系电话，不能为空 |
| `contactMobile` | `String` | ❌ | 备用手机/微信电话 |
| `address` | `String` | ❌ | 物业地址 |
| `serviceHours` | `String` | ❌ | 服务时间 |
| `email` | `String` | ❌ | 联系邮箱 |
| `remark` | `String` | ❌ | 备注（仅管理端可见，不下发前端） |
| `enabled` | `Boolean` | ❌ | 是否启用；启用后前端展示 |

**GET/POST 成功响应**（`PropertyContactDetailResponse`）：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "pcc_1a2b3c4d5e6f",
    "propertyName": "阳光花园物业服务中心",
    "contactPhone": "0571-88886666",
    "contactMobile": "13800000000",
    "address": "浙江省杭州市西湖区阳光路1号",
    "serviceHours": "周一至周日 8:00-20:00",
    "email": "service@sunshine-property.com",
    "remark": "前台分机 8001",
    "enabled": true,
    "createdAt": "2026-07-01T07:23:33",
    "updatedAt": "2026-07-01T07:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---

### GET `/residents/me/booking-info` — 预约信息预填

**Auth**: Required  
**Roles**: `resident`

**说明**：获取预约服务时自动带出的联系电话（完整手机号）与默认地址（楼栋+单元+房号），前端在预约表单预显示。

**成功响应**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "residentId": "res_xxx",
    "contactPhone": "13812345678",
    "address": "3号楼5单元502",
    "building": "3",
    "unit": "5单元",
    "room": "502",
    "communityId": "com_xxx",
    "communityName": "演示小区",
    "propertyCompanyId": "pc_xxx",
    "hasPhone": true,
    "hasAddress": true
  }
}
```

---

### GET `/wallet/cards` — 钱包卡片

**Auth**: Required  
**Roles**: `resident`

**说明**：获取住户钱包卡片信息（积分、物业币余额等）。

---

### GET `/technicians/my/wallet/cards` — 技工钱包卡片

**Auth**: Required  
**Roles**: `technician`

**说明**：获取技工钱包卡片信息。

---

# 二期接口总览（新增 §52–§60）

> 以下模块为**二次开发**需求的最优 API 设计，与既有接口兼容扩展。权限码新增见各模块说明。

| 模块 | 路径前缀 | 优先级 | 核心能力 |
| --- | --- | --- | --- |
| §52 定向消息推送 | `/directed-messages`、`/admin/directed-messages` | P0 | 多维筛选、官方会话、已读统计 |
| §53 社区多物业 | `/admin/communities` | P0 | 社区↔物业多对多绑定 |
| §54 物业角色层级 | `/admin/property-operators` | P0 | 领导/管理员、楼栋管辖 |
| §55 商家服务范围 | `/merchants/my/service-scope` | P0 | 小区多选、列表过滤 |
| §56 聊天配额与官方会话 | `/messages/contact-quota` | P0 | 每日3人、公告进聊天 |
| §57 服务需求匹配 | `/service-requests` | P0 | 智能匹配、轮询、费用通知 |
| §58 咨询板块 | `/consultations` | P1 | 医疗/教育/养老 |
| §59 商家广告 | `/merchant/ads` | P1 | 周额度、聊天卡片 |
| §60 业主商户 | `/resident-merchants`、`/distributors` | P0 | 二级分销、保证金、分账 |

---

# 52. 二期 — 定向消息推送 (directed-messages)

## 52.0 业务说明

社区/物业/拥有定向推送权限的后台用户，可按 **楼号、性别、年龄段** 筛选住户，以 **官方身份** 将消息写入用户聊天列表（`messageType=official_message`，`chatType=official`）。用户可回复形成双向会话。统计报表支持相同维度筛选。

**频率限制**：
- 同一发送方对同一接收用户：每天最多 **3** 条定向消息
- 同一物业公司：每天最多 **10** 条全局定向推送任务（按任务计数，非收件人数）

**权限码**：`directed_message.send`、`directed_message.read`、`directed_message.stats`

## 52.0.1 数据表

| 表名 | 说明 |
| --- | --- |
| `directed_message_tasks` | 定向推送任务（`dmt_` 前缀） |
| `directed_message_recipients` | 任务收件人及已读状态（`dmr_`） |
| `age_bracket_configs` | 年龄段配置（物业/平台级，默认6档） |

`directed_message_tasks` 主要字段：`id`, `sender_id`, `property_company_id`, `official_sender_type`, `title`, `content`, `image_urls`(JSON), `filter_buildings`(JSON), `filter_gender`, `filter_age_bracket_ids`(JSON), `recipient_count`, `read_count`, `status`, `created_at`

## 52.1 POST `/admin/directed-messages` — 创建并发送定向消息

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`, `coordinator`（须 `directed_message.send`）

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `title` | `String` | ✅ | 消息标题，`@Size(max=128)` |
| `content` | `String` | ✅ | 正文，`@Size(max=2000)` |
| `imageUrls` | `String[]` | ❌ | 图片 URL，最多 9 张 |
| `officialSenderType` | `String` | ✅ | `community`/`property`/`platform`/`coordinator` |
| `filterBuildings` | `String[]` | ❌ | 楼栋号多选，空=全部楼栋 |
| `filterGender` | `String` | ❌ | `male`/`female`/`all`，默认 `all` |
| `filterAgeBracketIds` | `String[]` | ❌ | 年龄段 ID 多选，空=全部 |
| `merchantIds` | `String[]` | ❌ | 按商家消费筛选：仅向消费过这些商家的住户推送，空=不启用 |
| `productIds` | `String[]` | ❌ | 按产品购买筛选：仅向购买过这些产品的住户推送，空=不启用 |
| `propertyCompanyId` | `String` | 条件 | 平台管理员跨物业发送时必填 |

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "taskId": "dmt_demo001",
    "recipientCount": 156,
    "status": "已发送",
    "statusCode": "sent",
    "createdAt": "2026-07-13 10:00:00"
  },
  "timestamp": "2026-07-13 10:00:00"
}
```

**业务规则**：
1. 后端按筛选条件解析目标 `residents`（含租住人员），逐人写入 `messages` 并关联 `directed_message_recipients`
2. 会话聚合键：`officialPeerId` = 发送方官方账号 ID（如 `official_pc_demo001`）
3. 超限返回 `90101`（同人每日上限）或 `90102`（物业每日全局任务上限）

## 52.2 GET `/admin/directed-messages` — 定向推送任务列表

**Auth**: Required  
**Roles**: 同上 + `directed_message.read`

**Query**：`page`, `pageSize`, `startDate`, `endDate`, `officialSenderType`, `propertyCompanyId`

**列表项字段**：`id`, `title`, `officialSenderType`, `recipientCount`, `readCount`, `readRate`, `status`, `statusCode`, `createdAt`

## 52.3 GET `/admin/directed-messages/{id}` — 任务详情与统计

**成功响应 data**：

```json
{
  "id": "dmt_demo001",
  "title": "停水通知",
  "content": "明日上午9点至12点停水…",
  "filterSummary": "1号楼、2号楼 · 全部性别 · 26-35岁",
  "recipientCount": 156,
  "readCount": 98,
  "unreadCount": 58,
  "readRate": 0.628,
  "status": "已发送",
  "statusCode": "sent",
  "createdAt": "2026-07-13 10:00:00"
}
```

## 52.3.1 GET `/admin/directed-messages/stats` — 定向消息统计（2026-10-29 新增）

**Auth**: Required  
**权限**: `directed_message:stats`

**Query**：`propertyCompanyId`（平台端可选；物业端/标签身份强制取本物业）

**成功响应 data**：

```json
{
  "propertyCompanyId": "pc_demo001",
  "taskCount": 12,
  "recipientCount": 380,
  "readCount": 265,
  "unreadCount": 115,
  "readRate": 0.6974,
  "todayTaskCount": 2,
  "last7DaysTaskCount": 9
}
```

**说明**：`readRate = readCount / recipientCount`，保留 4 位小数；`propertyCompanyId` 为空表示平台端全平台汇总。

## 52.4 GET `/admin/directed-messages/{id}/recipients` — 收件人明细分页

**Query**：`page`, `pageSize`, `readStatus`(`read`/`unread`), `buildingNo`, `gender`, `ageBracketId`

## 52.5 GET `/admin/age-brackets` — 年龄段配置列表

**Roles**: `property_admin`, `platform_admin`

**响应示例**：

```json
{
  "code": 0,
  "data": {
    "list": [
      { "id": "ab_01", "label": "18岁以下", "minAge": 0, "maxAge": 17, "sortOrder": 1 },
      { "id": "ab_02", "label": "18-25岁", "minAge": 18, "maxAge": 25, "sortOrder": 2 }
    ]
  }
}
```

## 52.6 PUT `/admin/age-brackets` — 批量更新年龄段（平台/物业）

**Roles**: `property_admin`, `platform_admin`

**请求体**（`UpdateAgeBracketsRequest`）：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `items` | `Array` | ✅ | 年龄段列表，不能为空；兼容别名 `brackets` |
| `items[].id` | `String` | ❌ | 已有记录ID（新增时为空） |
| `items[].label` | `String` | ❌ | 显示名称 |
| `items[].minAge` | `Integer` | ❌ | 最小年龄 |
| `items[].maxAge` | `Integer` | ❌ | 最大年龄 |
| `items[].sortOrder` | `Integer` | ❌ | 排序号（升序展示） |

**请求示例**：

```json
{ "items": [ { "id": "ab_02", "label": "18-25岁", "minAge": 18, "maxAge": 25, "sortOrder": 2 } ] }
```

**成功响应**：同 §52.5（更新后的完整列表）。

---

## 52.7 GET `/messages/conversations` 增强（官方会话）

在 §30.2 基础上已落地：

| 字段 | 说明 |
| --- | --- |
| `conversationType` | `neighbor`/`official`/`merchant_ad`/`service_request` |
| `officialSenderType` | 官方会话时：`community`/`property`/`platform`/`coordinator` |
| `officialDisplayName` | 如物业公司名、「社区管理处」、「平台官方」、「统筹」 |
| `relatedTaskId` | 关联定向任务/公告/服务需求等 ID（若有） |
| Query `conversationType` | 可选，按 Tab 筛选 |

**官方会话键** `officialPeerId` = `official_{senderType}_{propertyCompanyId}`（例：`official_property_pc_demo001`）。定向推送与公告直达写入消息时 `fromId` 使用该键。

用户回复官方消息：`POST /messages`，`messageType=official_message`，`chatType=official`，`toId=officialPeerId`。

打开官方会话：`GET /messages/conversations/{officialPeerId}`。

---

# 53. 二期 — 社区多物业绑定 (community-bindings)

## 53.0 业务说明

- 层级：**社区 → 物业公司 → 小区(community) → 楼栋 → 住户**
- 社区与物业公司 **多对多**；一个物业可归属多个社区
- 社区管理员默认对旗下物业 **只读**；操作权限由 `community_property_permissions` 配置

## 53.0.1 数据表

| 表名 | 说明 |
| --- | --- |
| `community_entities` | 顶层社区实体（`cent_`），区别于小区 `communities` |
| `community_property_bindings` | 社区↔物业绑定（`cpb_`） |
| `community_property_permissions` | 社区管理员对某物业的权限粒度（`cpp_`） |

## 53.1 GET `/admin/community-entities` — 社区列表

**Roles**: `platform_admin`、`property_admin`（仅可见已绑定本物业的社区）

**Query 参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `keyword` | `String` | ❌ | | 社区名称关键词 |

**成功响应**（`PageResponse<CommunityEntityListItemResponse>`）：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "cent_demo001",
        "name": "阳光社区",
        "description": "下辖 3 个小区",
        "adminResidentId": "res_admin001",
        "adminResidentName": "社区管理员",
        "status": "active",
        "boundPropertyCount": 2,
        "createdAt": "2026-07-01 15:23:33"
      }
    ],
    "pagination": { "page": 1, "pageSize": 20, "total": 1, "totalPages": 1 }
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

## 53.2 POST `/admin/community-entities` — 创建社区

**Roles**: **仅** `platform_admin`  
> 物业领导 / 物业管理员 **不能**创建顶层社区（会返回 `20004` 权限不足）。请用平台超管 `13800000000` 创建后再绑定物业。

**请求体**（`CreateCommunityEntityRequest`）：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `name` | `String` | ✅ | 社区名称，不能为空 |
| `description` | `String` | ❌ | 社区描述 |
| `adminResidentId` | `String` | ❌ | 社区管理员住户ID（`res_`），须存在 |

**成功响应**：`CommunityEntityDetailResponse`（字段同 §53.1 列表项，增加 `updatedAt`，不含 `boundPropertyCount`）。

## 53.3 GET `/admin/community-entities/{id}/property-companies` — 已绑定物业列表

**Roles**: `platform_admin`, `property_admin`

**路径参数**：`id`（社区ID，`cent_`）

**成功响应**：`List<CommunityPropertyBindingItemResponse>`（非分页）：

```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "bindingId": "cpb_demo001",
      "propertyCompanyId": "pc_demo001",
      "propertyCompanyName": "演示物业公司",
      "permissionLevel": "read_only",
      "allowedModules": ["announcement.read", "resident.read"],
      "boundAt": "2026-07-01 15:23:33"
    }
  ],
  "timestamp": "2026-07-01 15:23:33"
}
```

**绑定项字段**：`bindingId`、`propertyCompanyId`、`propertyCompanyName`、`permissionLevel`（`read_only`/`operate`）、`allowedModules`、`boundAt`。

## 53.4 POST `/admin/community-entities/{id}/property-companies` — 绑定物业公司

**Roles**: **仅** `platform_admin`

**请求体**（`BindPropertyCompanyRequest`）：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `propertyCompanyId` | `String` | ✅ | 物业公司ID（`pc_`），不能为空 |
| `permissionLevel` | `String` | ❌ | `read_only`/`operate`，默认 `read_only` |

**成功响应**：`CommunityPropertyBindingItemResponse`（字段同 §53.3）。

> 重复绑定返回 `10002`「该物业公司已绑定」；物业公司不存在返回 `20001`。

## 53.5 DELETE `/admin/community-entities/{id}/property-companies/{propertyCompanyId}` — 解绑

**Roles**: **仅** `platform_admin`

**路径参数**：`id`（社区ID）、`propertyCompanyId`（物业公司ID）

**成功响应**：`data: null`。绑定关系不存在返回 `10002`。

## 53.6 PUT `/admin/community-entities/{id}/property-companies/{propertyCompanyId}/permissions` — 调整权限

**Roles**: **仅** `platform_admin`

**路径参数**：`id`（社区ID）、`propertyCompanyId`（物业公司ID）

**请求体**（`UpdateCommunityPropertyPermissionRequest`，全部可选）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `permissionLevel` | `String` | `read_only`/`operate` |
| `allowedModules` | `List<String>` | 允许的模块编码列表（如 `["announcement.read","resident.read"]`）；传入即全量替换 |

**成功响应**：`CommunityPropertyBindingItemResponse`（字段同 §53.3）。

---

## 53.7 GET `/property-companies` 增强

列表项新增 `communityEntityIds`：所属社区 ID 数组（便于前端展示归属）

---

# 54. 二期 — 物业角色层级 (property-operators)

## 54.0 业务说明

物业公司内部在既有主角色 `property_admin` 之上，用 **`propertySubRole`** 区分领导与管理员：

| 子角色 | `propertySubRole` 值 | 主 `role` | 说明 |
| --- | --- | --- | --- |
| 物业领导 | `property_leader` | **固定** `property_admin` | 本公司全部权限：参数配置、权限分配、人员管理等 |
| 物业管理员 | `property_operator` | **固定** `property_admin` | 日常操作；无参数配置与权限分配；可限定小区/楼栋 |

> JWT / 接口鉴权看主字段 **`role=property_admin`**；前端展示「物业领导 / 物业管理员」读 **`propertySubRole`**。

物业管理员可被分配 **管辖范围**：指定小区 ID 列表、楼栋号列表。

## 54.0.1 数据表

`property_operator_scopes`（`pos_`）：`resident_id`, `property_company_id`, `community_ids`(JSON), `building_nos`(JSON)  
`residents.property_sub_role`：存 `property_leader` / `property_operator`

## 54.1 POST `/admin/property-operators` — 创建物业管理员账号

**Roles**: `property_admin`（含物业领导）、`platform_admin`  
（创建时请求体可带 `propertySubRole`：`property_leader` / `property_operator`，默认 `property_operator`；落库 `role` 一律为 `property_admin`）

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `phone` | `String` | ✅ | 登录手机号 |
| `name` | `String` | ✅ | 姓名 |
| `password` | `String` | ✅ | 初始密码 |
| `propertySubRole` | `String` | ❌ | `property_leader` / `property_operator`，默认后者 |
| `communityIds` | `String[]` | ❌ | 管辖小区，空=全公司 |
| `buildingNos` | `String[]` | ❌ | 管辖楼栋，空=不限楼栋 |
| `permissionPresetCode` | `String` | ❌ | 权限预设编码 |
| `propertyCompanyId` | `String` | 条件 | 平台管理员创建时必填 |

## 54.2 GET `/admin/property-operators` — 管理员列表

**Roles**: `property_admin`（仅物业领导）、`platform_admin`（`property_operator` 返回 `20004`）

**Query 参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `keyword` | `String` | ❌ | | 姓名/手机号关键词 |
| `status` | `String` | ❌ | | `active`/`disabled` |

**成功响应**（`PageResponse<PropertyOperatorListItemResponse>`）：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "pos_demo001",
        "residentId": "res_demo001",
        "name": "物业管理员",
        "phone": "13800000000",
        "propertySubRole": "property_operator",
        "communityIds": ["com_demo001"],
        "buildingNos": ["1栋", "2栋"],
        "status": "active",
        "createdAt": "2026-07-01 15:23:33"
      }
    ],
    "pagination": { "page": 1, "pageSize": 20, "total": 1, "totalPages": 1 }
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**列表项字段**：`id`（`pos_` 管辖范围记录ID）、`residentId`、`name`、`phone`、`propertySubRole`、`communityIds`、`buildingNos`、`status`、`createdAt`。

## 54.3 PUT `/admin/property-operators/{id}/scope` — 更新管辖范围

**Roles**: `property_admin`（仅物业领导）、`platform_admin`

**路径参数**：`id`（管辖范围记录ID `pos_`，**不是住户ID**）

**请求体**（`UpdatePropertyOperatorScopeRequest`，全部可选）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `communityIds` | `List<String>` | 管辖小区ID列表；传 `[]` 清空（=全公司），不传不变 |
| `buildingNos` | `List<String>` | 管辖楼栋号列表；传 `[]` 清空（=不限楼栋），不传不变 |

**成功响应**：`PropertyOperatorListItemResponse`（字段同 §54.2）。

## 54.4 PUT `/admin/property-operators/{id}/status` — 启用/停用

**Roles**: `property_admin`（仅物业领导）、`platform_admin`

**路径参数**：`id`（管辖范围记录ID `pos_`）

**请求体**（`UpdatePropertyOperatorStatusRequest`）：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `status` | `String` | ✅ | `active`/`disabled`，其它值返回 `10002` |

**成功响应**：`PropertyOperatorListItemResponse`（字段同 §54.2）。

---

## 54.5 鉴权增强

`PermissionInterceptor` 对 `property_operator` 校验 `property_operator_scopes`：访问住户/订单/公告等资源时，资源所属 `communityId`/`buildingNo` 须在管辖范围内，否则 `403` + `20008`。

---

# 55. 二期 — 商家服务范围 (merchant-service-scope)

## 55.0 业务说明

商家开通后选择服务的 **小区**（多选或全选）。「全选」= 该商家已挂接物业公司下的全部小区。用户端 `GET /merchants` 自动按当前用户 `communityId` 过滤。

## 55.0.1 数据表

`merchant_service_communities`（`msc_`）：`merchant_id`, `community_id`；`serve_all_communities` 布尔字段在 `merchants` 表

## 55.1 GET `/merchants/my/service-scope` — 查询本店服务范围

**Roles**: `merchant`

```json
{
  "code": 0,
  "data": {
    "serveAllCommunities": false,
    "communities": [
      { "communityId": "com_demo001", "communityName": "幸福花园", "selected": true },
      { "communityId": "com_demo002", "communityName": "阳光苑", "selected": false }
    ]
  }
}
```

## 55.2 PUT `/merchants/my/service-scope` — 更新服务范围

**请求体**：

```json
{
  "serveAllCommunities": false,
  "communityIds": ["com_demo001", "com_demo003"]
}
```

## 55.3 GET `/merchants` 增强（§7.1）

- 登录用户（`resident`）自动追加过滤：仅返回 `serveAllCommunities=true` 或服务范围包含其 `communityId` 的商家
- 未登录用户：仍返回全部已审核商家（或按 Query `communityId` 过滤）

## 55.4 POST `/merchants` 入驻增强（§7.2）

入驻请求体新增：`serveAllCommunities`, `communityIds`（`serveAllCommunities=false` 时必填至少一项）

---

# 56. 二期 — 聊天配额与官方会话 (chat-quota & official)

## 56.0 业务说明

### 56.0.1 邻居私聊配额
- 同一小区内业主与租住人员可互聊
- 每天最多 **主动发起 3 个不同人** 的新对话（已有会话回复不计入）
- 每天 `00:00`（Asia/Shanghai）重置
- 复用表 `neighbor_contact_limits`，默认上限由物业公司参数 `neighborDailyContactLimit` 控制（二期默认 **3**）

### 56.0.2 公告直达聊天
- 发布公告时可设 `deliveryChannel`：`announcement_board`（仅公告栏）/ `chat`（仅聊天）/ `both`（两者）
- 选 `chat` 或 `both` 时，为每位目标用户写入 `official_message` 会话

## 56.1 GET `/messages/contact-quota` — 今日新聊配额

> ⚠️ **【当前未实现 · 2026-10-27 核对代码】** 后端不存在 `GET /messages/contact-quota`。每日新聊配额若需展示，请由后端补查询接口；前端请勿直接调用。以下为原设计文档，保留备查。

**Roles**: `resident`

```json
{
  "code": 0,
  "data": {
    "dailyLimit": 3,
    "usedCount": 1,
    "remainingCount": 2,
    "resetAt": "2026-07-14 00:00:00"
  }
}
```

## 56.2 POST `/messages` 增强（§12.2）

发起 **新邻居私聊** 时若当日不同对象已达上限，返回 `90103`（今日新聊人数已达上限）。

**判定**：`GET /messages/conversations/{peerId}` 无历史记录且 `neighbor_contact_limits` 当日 `distinct_peer_count >= dailyLimit` → 拒绝。

## 56.3 POST `/announcements` 增强

请求体新增：

| 字段 | 类型 | 默认 | 说明 |
| --- | --- | --- | --- |
| `deliveryChannel` | `String` | `announcement_board` | `announcement_board`/`chat`/`both` |
| `pushToChat` | `Boolean` | — | 兼容字段，=true 等价 `deliveryChannel=chat` |

## 56.4 GET `/admin/announcements/{id}/read-stats` — 公告已读统计

> ⚠️ **【当前未实现 · 2026-10-27 核对代码】** 后端不存在 `GET /admin/announcements/{id}/read-stats`。公告已读数据请使用公告详情/定向推送统计中已返回的字段；本接口请勿调用。以下为原设计文档，保留备查。

**响应**：

```json
{
  "code": 0,
  "data": {
    "announcementId": "ann_demo001",
    "deliveryChannel": "both",
    "targetCount": 500,
    "readCount": 320,
    "unreadCount": 180,
    "readRate": 0.64,
    "byBuilding": [
      { "buildingNo": "1号楼", "readCount": 80, "unreadCount": 20 }
    ]
  }
}
```

---

# 57. 二期 — 服务需求智能匹配 (service-requests)

## 57.0 业务流程

```
用户 POST /service-requests（文字描述）
  → 关键词匹配服务分类 + 按商家 messageService 范围筛选
  → 推送第 1 位商家（站内消息 + 待办）
  → 5 分钟未响应 → 自动转下一位
  → 商家接受 → 私聊协商 → POST .../quote 费用通知
  → 用户 POST .../pay-quote（积分/物业币/混合）
  → 全部商家未响应 → status=已关闭，通知用户
```

## 57.0.1 数据表

| 表名 | 说明 |
| --- | --- |
| `service_request_orders` | 服务需求单（`sro_`） |
| `service_request_merchant_queue` | 商家轮询队列（`srmq_`） |
| `service_category_dict` | 服务分类字典（`scd_`） |
| `merchant_message_services` | 商家消息服务配置（`mms_`） |

## 57.1 GET / PUT / POST `/merchants/my/message-service` — 查询与开通/配置消息服务

**Roles**: `merchant`

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/merchants/my/message-service` | 查询当前商家消息服务配置 |
| PUT | `/merchants/my/message-service` | 开通/配置消息服务 |
| POST | `/merchants/my/message-service` | 与 PUT **同逻辑**（兼容前端 POST） |

**请求体**（PUT/POST，`UpdateMessageServiceRequest`）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `enabled` | `Boolean` | 是否开通（必填） |
| `serviceRadius` | `String` | `1km`/`3km`/`5km`/`district`/`city` |
| `categoryIds` | `String[]` | 服务分类 ID（修窗帘、修下水等） |

**响应**（`MessageServiceConfigResponse`）：`enabled`、`serviceRadius`、`categoryIds`

**校验规则**：

- `enabled` 必填；为 `true` 时：
  - `serviceRadius` 必填且 ∈ `1km`/`3km`/`5km`/`district`/`city`
  - `categoryIds` 至少包含一个元素，且每个 ID 必须存在于 `service_category_dict`（`status=active`），否则返回 `10002`
  - 非法/重复 ID 自动去重；`enabled=false` 时仅更新开关，保留历史 `serviceRadius`/`categoryIds`

**错误响应**：

| 错误码 | HTTP | 说明 |
| --- | --- | --- |
| `10002` | 400 | `enabled` 缺失 / `serviceRadius` 非法 / 分类为空 / 含非法分类 ID |
| `10004` | 404 | 路径不存在（如少写 `/my`） |
| `10005` | 405 | 请求方法不支持 |
| `10007` | 400 | 请求体 JSON 解析失败 |

**请求示例**：

```json
{ "enabled": true, "serviceRadius": "3km", "categoryIds": ["scd_01","scd_02"] }
```

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": { "enabled": true, "serviceRadius": "3km", "categoryIds": ["scd_01","scd_02"] }
}
```

## 57.2 GET `/service-categories` — 服务分类字典（公开）

## 57.2.1 GET `/merchant-categories` — 商家分类字典（公开）

**Auth**: Optional（匿名可访问）

**说明**：获取全部已启用的商家分类，前端入驻下拉和商家列表筛选均从此接口取值。

**成功响应**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      { "name": "外卖", "sortOrder": 1 },
      { "name": "周围商家", "sortOrder": 2 },
      { "name": "自营", "sortOrder": 3 },
      { "name": "农家", "sortOrder": 4 },
      { "name": "外卖小吃", "sortOrder": 5 },
      { "name": "团购", "sortOrder": 6 },
      { "name": "酒店民宿", "sortOrder": 7 },
      { "name": "洗浴汗蒸", "sortOrder": 8 },
      { "name": "按摩足疗", "sortOrder": 9 },
      { "name": "美食", "sortOrder": 10 },
      { "name": "外卖小区", "sortOrder": 11 },
      { "name": "看病买药", "sortOrder": 12 },
      { "name": "休闲玩乐", "sortOrder": 13 }
    ]
  }
}
```

## 57.2.2 GET `/product-categories` — 业主商城商品类目字典（公开）

**Auth**: Optional（匿名可访问）

**说明**：获取全部已启用的业主商城商品类目，前端「业主商城」预置筛选项统一从此接口取值。

**成功响应**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      { "name": "食品饮料", "sortOrder": 1 },
      { "name": "日用百货", "sortOrder": 2 },
      { "name": "服饰鞋包", "sortOrder": 3 },
      { "name": "家居用品", "sortOrder": 4 }
    ]
  }
}
```

## 57.3 POST `/service-requests` — 发布服务需求

**Roles**: `resident`

**请求体**：`description`（必填）, `communityId`（默认当前用户）, `contactPhone`, `preferredTime`

**响应**：`id`, `status`（`匹配中`）, `statusCode`（`matching`）, `matchedCategoryName`, `currentMerchantName`（可为 null）

## 57.4 GET `/service-requests/my` — 我的需求列表

## 57.5 GET `/service-requests/{id}` — 需求详情

含 `queue`：已轮询商家列表、`quote`：费用通知（如有）

## 57.6 POST `/service-requests/{id}/cancel` — 用户取消

**Auth**: Required  
**Roles**: `resident`

仅主单状态为 `matching` / `negotiating` 时可取消。

**行为**：

1. 主单 `service_request_orders.status` → `cancelled`，`closedReason` = `用户取消`
2. **同步**将该 `request_id` 下队列行中仍为 `pending` / `waiting` / `accepted` 的记录 → `cancelled`，并写 `respondedAt`
3. 已是 `timeout` / `skipped` / `cancelled` 的队列行不改动

**关联字段**：

| 表 | 字段 | 说明 |
| --- | --- | --- |
| `service_request_orders` | `id`（如 `sro_`） | 居民侧响应 `id` |
| `service_request_merchant_queue` | `request_id` | 指向主单 `id`；商家侧响应 `requestId` |
| `service_request_merchant_queue` | `id`（如 `srmq_`） | 商家侧 `queueId` |

**请求示例**：

```bash
curl -X POST "https://test.yuanxusoftware.top/api/v1/service-requests/sro_7aa21000/cancel" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**成功响应 data（节选）**：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `String` | 需求单 ID |
| `status` | `String` | `已取消` |
| `statusCode` | `String` | `cancelled` |
| `description` | `String` | 需求描述 |
| `matchedCategoryName` | `String` | 匹配分类名 |
| `currentMerchantName` | `String` | 当前商家名（可空） |
| `contactPhone` | `String` | 联系电话 |
| `createdAt` | `String` | 创建时间 |

取消成功后，商家再调 `GET /merchant/service-requests/pending` **不应**再出现该 `requestId`。

**历史脏数据清理（可选，部署后执行一次）**：

```sql
UPDATE service_request_merchant_queue q
INNER JOIN service_request_orders o ON o.id = q.request_id
SET q.status = 'cancelled',
    q.responded_at = NOW(),
    q.updated_at = NOW()
WHERE o.status = 'cancelled'
  AND q.status IN ('pending', 'waiting', 'accepted');
```

## 57.7 GET `/merchant/service-requests/pending` — 商家待响应列表

**Auth**: Required  
**Roles**: `merchant`

仅返回当前商家队列中 **`status=pending`** 的条目（5 分钟响应窗口内）。  
`timeout` / `accepted` / `skipped` / **`cancelled`** **不会**出现在本列表。无待响应时返回空列表：`code=0`，`data.list=[]`（**不是错误**）。

> **说明**：居民取消后对应队列已非 `pending`，故已取消需求不会出现在本列表。`requestId` 与居民侧需求 `id` 一一对应。

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/merchant/service-requests/pending?page=1&pageSize=20" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query**：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| `page` | `int` | ❌ | `1` | 页码，≥1 |
| `pageSize` | `int` | ❌ | `20` | 每页条数，1–100 |

**成功响应 data 列表项**：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `queueId` | `String` | 队列项 ID（`srmq_`） |
| `requestId` | `String` | 需求单 ID（`sro_`） |
| `description` | `String` | 需求描述 |
| `contactPhone` | `String` | 用户联系电话 |
| `matchedCategoryName` | `String` | 匹配分类名 |
| `status` | `String` | 中文，通常「待响应」 |
| `statusCode` | `String` | `pending` |
| `timeoutAt` | `String` | 超时时间 `yyyy-MM-dd HH:mm:ss` |
| `notifiedAt` | `String` | 通知时间 |

**联调前置**：

1. 登录账号 `role=merchant`，且 `residents.phone` = `merchants.contact_phone`（同物业优先）
2. 库表已存在：`service_request_orders`、`service_request_merchant_queue`、`service_category_dict`
3. 定时任务每分钟会把超时 `pending` 置为 `timeout` 并转下一家；联调需在 5 分钟内接单，或临时将队列改回 `pending`

**常见错误**：

| code | 说明 |
| --- | --- |
| `60001` / 商家未找到 | 手机号与 `merchants.contact_phone` 对不上 |
| `60002` | 商家入驻审核未通过，暂不能接单（`auditStatus != approved`） |
| `60005` | 商家已被踢出，无法接单（`status = kicked`） |
| `99001` | 内部数据访问异常（兜底）。**不一定是缺表/缺字段**——已核对生产库 `wuye_points_latest.sql` 表结构完整。出现时请后端在 `app.log` 检索本次请求的 `Caused by` 真实堆栈定位，勿直接补表重部署；缺表/缺字段会有独立专用文案（"数据表缺失…"/"数据库字段缺失…"）。 |

## 57.8 POST `/merchant/service-requests/{id}/accept` — 商家接单

5 分钟内调用；超时返回 `90104`。**接单前服务端会强制校验当前商家 `auditStatus=approved` 且 `status=active`**；未审核或被踢出商家将返回 `60002`/`60005`。

## 57.9 POST `/merchant/service-requests/{id}/skip` — 商家跳过/拒绝

**前置校验**：当前商家须 `auditStatus=approved` 且 `status=active`；未审核/被踢出返回 `60002`/`60005`。

## 57.10 POST `/merchant/service-requests/{id}/quote` — 发送费用通知

**前置校验**：当前商家须 `auditStatus=approved` 且 `status=active`；未审核/被踢出返回 `60002`/`60005`。

**请求体**：

```json
{
  "amount": 150.00,
  "currencyType": "property_coin",
  "description": "更换窗帘轨道及人工",
  "validHours": 24
}
```

为用户写入 `messageType=service_request` 的费用卡片消息。

## 57.11 POST `/service-requests/{id}/pay-quote` — 用户确认支付

**请求体**：同 §8.2 混合支付：`paymentMethod`=`point`/`property_coin`/`mixed`，`pointUsed`, `coinUsed`

支付成功后 `status`→`已支付`/`paid`，生成 `service_orders` 关联记录。

## 57.12 定时任务

`ServiceRequestTimeoutScheduler`：每 1 分钟扫描超时 `pending` 队列条目（5 分钟），转下一家；队列耗尽则 `closed` + 推送通知。

---

# 58. 二期 — 咨询板块 (consultations)

## 58.0 业务说明

领域 Tab：**医疗**、**教育**、**养老**。模式：咨询师卡片 + **图文咨询**（按次收费，站内支付）+ **预约面诊**（表单 + 咨询师确认）。

## 58.0.1 数据表

`consultants`（`cst_`）：含 `appointment_price`（面诊价格）、`introduction`（简介）字段  
`consultation_orders`（`co_`）、`consultation_appointments`（`ca_`）

## 58.1 GET `/consultations/categories` — 咨询领域列表

```json
{ "list": [
  { "code": "medical", "name": "医疗" },
  { "code": "education", "name": "教育" },
  { "code": "elderly", "name": "养老" }
]}
```

## 58.2 GET `/consultations/consultants` — 咨询师列表

**Query**：`category`, `page`, `pageSize`, `keyword`

**列表项**：`id`, `name`, `title`, `organization`, `specialty`, `avatarUrl`, `chatPrice`, `appointmentPrice`, `appointmentEnabled`, `status`

## 58.3 GET `/consultations/consultants/{id}` — 咨询师详情

## 58.4 POST `/consultations/consultants/{id}/chat-orders` — 发起图文咨询

**请求体**：`paymentMethod`, `pointUsed`, `coinUsed`（一步扣款）

**响应**：`orderId`, `conversationPeerId`（用于进入聊天）

## 58.5 POST `/consultations/appointments` — 提交预约面诊

**请求体**：`consultantId`, `appointmentTime`, `patientName`, `phone`, `remark`

## 58.6 GET `/consultations/appointments/my` — 我的预约

## 58.7 管理端

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/admin/consultants` | 创建咨询师（物业/社区审核后上架） |
| PUT | `/admin/consultants/{id}` | 更新 |
| PUT | `/admin/consultants/{id}/audit` | 审核 `approved`/`rejected` |
| GET | `/admin/consultants` | 列表 |
| GET | `/admin/consultation-settings` | 查询平台抽成比例等配置 |
| PUT | `/admin/consultation-settings` | 更新平台抽成比例 `commissionRate` |

**咨询配置响应**（`ConsultationSettingsResponse`）：`commissionRate`（`BigDecimal`）

### POST `/admin/consultants` — 创建咨询师

**Query**：`propertyCompanyId`（平台超管必填）

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `name` | String | ✅ | 咨询师姓名 |
| `category` | String | ✅ | 领域：`medical`/`education`/`elderly` |
| `title` | String | | 职称 |
| `organization` | String | | 所属机构 |
| `specialty` | String | | 专长 |
| `avatarUrl` | String | | 头像 URL |
| `chatPrice` | BigDecimal | | 图文咨询价格（默认 0） |
| `appointmentPrice` | BigDecimal | | 面诊价格 |
| `appointmentEnabled` | Boolean | | 是否开放预约面诊（默认 true） |
| `introduction` | String | | 简介 |

**响应**：`id`, `name`, `title`, `organization`, `specialty`, `avatarUrl`, `category`, `chatPrice`, `appointmentPrice`, `appointmentEnabled`, `introduction`, `auditStatus`, `status`, `createdAt`

### PUT `/admin/consultants/{id}` — 更新咨询师

**请求体**（所有字段可选）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `name` | String | 姓名 |
| `title` | String | 职称 |
| `organization` | String | 机构 |
| `specialty` | String | 专长 |
| `avatarUrl` | String | 头像 URL |
| `chatPrice` | BigDecimal | 图文咨询价格 |
| `appointmentPrice` | BigDecimal | 面诊价格 |
| `appointmentEnabled` | Boolean | 是否开放预约面诊 |
| `introduction` | String | 简介 |
| `status` | String | 状态 |

---

# 59. 二期 — 商家广告推送 (merchant-ads)

## 59.0 业务规则

| 项 | 规则 |
| --- | --- |
| 免费额度 | 每周 **1** 条 |
| 付费扩展 | 联系平台购买周额度包 |
| 推送范围 | 商家 `merchant_service_communities` 范围内用户 |
| 审核 | **不需要**内容审核 |
| 展示 | 图文卡片，`messageType=merchant_ad`，进用户聊天列表 |

## 59.0.1 数据表

`merchant_ads`（`mad_`）、`merchant_ad_quota`（`maq_`，按周计数）

`merchant_ads.status` 记录投放状态：`pending_payment` 待支付 / `active` 投放中 / `cancelled` 已取消（支付超时/失败）。存量数据迁移见 `V20__merchant_ad_status.sql`（幂等，手工执行）。

## 59.1 GET `/merchant/ads/quota` — 本周额度

```json
{
  "code": 0,
  "data": {
    "weekStart": "2026-07-07",
    "weekEnd": "2026-07-13",
    "freeQuota": 1,
    "purchasedQuota": 0,
    "usedCount": 0,
    "remainingCount": 1
  }
}
```

> **说明**：此前 `getQuota()` 复用 `getQuotaByMerchantId()` 导致 merchant 角色被 admin 校验拦截（20004）。现抽出无角色校验的 `buildQuotaResponse()`，两条路径解耦。`GET /merchant/ads`、`POST /merchant/ads`、`POST /merchant/ads/targeted` 走 `requireCurrentMerchant()`，不受影响。

## 59.2 POST `/merchant/ads` — 发布广告

**Auth**: Required
**Roles**: `merchant`（当前登录商家）

**请求体**（`CreateMerchantAdRequest`）：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `title` | String | 是 | 广告标题（≤128） |
| `content` | String | 是 | 广告内容 |
| `imageUrls` | String[] | 否 | 图片 URL 列表 |
| `productId` | String | 否 | 关联商品 ID |
| `durationDays` | int | 是 | 展示天数，1~30（按天计价） |
| `paymentMethod` | String | 是 | `free`(免费额度) / `coin`(物业币) / `point`(商家积分) / `wechat`(微信) / `mock`(模拟，仅开发环境) |
| `openid` | String | 否 | `paymentMethod=wechat` 时使用；不传则取当前用户绑定 openid |

**响应** `data`（`MerchantAdListItemResponse`）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` / `title` / `content` / `imageUrls` / `productId` | — | 基础字段 |
| `durationDays` / `startDate` / `endDate` | — | 投放天数与起止日期 |
| `payAmount` | `BigDecimal` | 应付金额（`0`=免费额度投放） |
| `paymentMethod` | String | 支付方式回显 |
| `status` | String | `pending_payment` 待支付 / `active` 投放中 / `cancelled` 已取消 |
| `recipientCount` | Integer | 实际 fan-out 到的住户数 |
| `paymentRecordId` | String | 微信预下单成功后返回，用于查询支付状态 |
| `payParams` | Object | 微信小程序调起支付参数（`timeStamp`/`nonceStr`/`package`/`signType`/`paySign`），前端原样透传 `wx.requestPayment` |

**行为说明**：

- `free` / `coin` / `point`：同步扣减并立即投放（`status=active`），响应不含 `paymentRecordId`/`payParams`。
- `point`（商家积分）扣减口径：应付金额按商家挂接的积分兑换比折算为积分（`1元 = X积分`，默认 `100`，消费方向向上取整），从该商家所有**已审核通过**的购买记录 `remainingPoints` 中按时间顺序（FIFO）扣减；余额不足返回 `94002`，广告不创建（事务回滚）。
- `wechat`：先落库 `status=pending_payment`（**不投放、不占额度**），返回 `payParams` 由前端调起微信支付；**微信回调确认后才激活投放**。未收到款项绝不投放。
- `mock`：仅当 `app.payment.mock-enabled=true`（开发环境）可用；生产环境返回 `10002`。

**微信支付示例（`paymentMethod=wechat`）**：

```json
{
  "code": 0,
  "data": {
    "id": "mad_xxx",
    "title": "新品上市",
    "status": "pending_payment",
    "payAmount": 35.00,
    "paymentMethod": "wechat",
    "paymentRecordId": "pay_xxx",
    "payParams": {
      "timeStamp": "1720000000",
      "nonceStr": "abc123",
      "package": "prepay_id=wx_xxx",
      "signType": "RSA",
      "paySign": "xxx"
    }
  }
}
```

**错误**：

| 错误码 | 说明 |
| --- | --- |
| `10001` | `paymentMethod=wechat` 且未绑定 openid，无法调起支付 |
| `10002` | `paymentMethod` 不合法 / 生产环境使用 `mock` / 在线支付未开通 |
| `10003` | 展示天数超出 1~30 |
| `60011` | 免费额度已用完，请选择付费投放 |
| `70003` | 该广告已有待支付流水，请勿重复提交（防重复扣款） |
| `90105` | 本周额度不足 |
| `94002` | 商家积分余额不足（`paymentMethod=point`） |

> **说明**：扣减本周额度并写入 `merchant_ads`；投放成功后向服务范围内住户 fan-out `messageType=merchant_ad` 聊天卡片。付费方式（`coin`/`point`/`wechat`）不占用免费名额。

## 59.3 GET `/merchant/ads` — 本店广告历史

**Auth**: Required
**Roles**: `merchant`

标准分页 `data.list` + `data.pagination`，列表项为 `MerchantAdListItemResponse`（字段同上，含 `status`）。`status=pending_payment` 表示微信支付尚未完成，`cancelled` 表示支付超时/失败已取消。

## 59.4 GET `/admin/merchant-ad-packages` — 额度包配置（平台）

**Auth**: Required  
**Roles**: `platform_admin`, `property_admin`

已实现。返回可购买的广告周额度包列表。

**响应** `data[]`（`MerchantAdPackageResponse`）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `String` | 额度包 ID |
| `name` | `String` | 名称 |
| `weeklyQuota` | `Integer` | 每周额度条数 |
| `price` | `BigDecimal` | 价格 |

## 59.5 POST `/admin/merchants/{merchantId}/ad-quota` — 为商家增加付费周额度

**Roles**: `platform_admin`, `property_admin`

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `purchasedQuota` | int | 是 | 为本周增加的付费条数 |

**响应**：与 §59.1 相同额度结构。

## 59.6 GET `/admin/merchants/{merchantId}/ad-quota` — 查询商家本周额度（管理端）

**Auth**: Required
**Roles**: `platform_admin`, `property_admin`

只读查询指定商家本周额度。**该商家本周无额度记录时返回全 0，不报错**。

**路径参数**：`merchantId`（商家 ID，`mch_` 开头）

**响应**：与 §59.1 相同额度结构（`MerchantAdQuotaResponse`）。

> 与 §59.1 的区别：§59.1 走 merchant 自身身份（无角色校验），本接口按 `merchantId` 查任意商家并做 admin 角色校验。

## 59.7 GET `/merchant/ads/quote` — 广告位按天计价试算

**Auth**: Required
**Roles**: `merchant`

发布广告前试算应付金额，用于前端展示"X 天 = Y 元"。

**Query 参数**：

| 字段 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| `days` | int | 否 | `7` | 广告展示天数 |

**响应** `data`（`MerchantAdQuoteResponse`）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `durationDays` | `Integer` | 展示天数（回显入参） |
| `dayPrice` | `BigDecimal` | 单日单价，取自 §59.8 广告位配置 |
| `payAmount` | `BigDecimal` | 应付金额 = `dayPrice × durationDays`；命中免费额度时为 `0` |
| `freeQuotaRemaining` | `Integer` | 本周剩余免费条数 |
| `freeQuotaEnabled` | `Boolean` | 是否启用免费额度 |

**示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/merchant/ads/quote?days=7" \
  -H "Authorization: Bearer <token>"
```

```json
{
  "code": 0,
  "data": {
    "durationDays": 7,
    "dayPrice": 5.00,
    "payAmount": 35.00,
    "freeQuotaRemaining": 1,
    "freeQuotaEnabled": true
  }
}
```

## 59.8 GET / PUT `/admin/merchant-ad-settings` — 广告位配置

**Auth**: Required
**Roles**: `platform_admin`, `property_admin`

全局广告位参数：单日价、免费额度开关、每周免费条数。§59.7 试算与 §59.2 发布均以此为准。

**响应 / 请求体**（`MerchantAdSettingsResponse` / `UpdateMerchantAdSettingsRequest`）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `dayPrice` | `BigDecimal` | 广告位单日价格 |
| `freeQuotaEnabled` | `Boolean` | 是否启用每周免费额度 |
| `freeQuotaPerWeek` | `Integer` | 每周免费条数 |

**示例**：

```bash
curl -X PUT https://test.yuanxusoftware.top/api/v1/admin/merchant-ad-settings \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"dayPrice":5.00,"freeQuotaEnabled":true,"freeQuotaPerWeek":1}'
```

---

# 60. 二期 — 业主商户体系 (resident-merchants)

## 60.0 角色与资金流

> 🆕 **v8.11（件数制）**：业主商户（下称**业主小店**）从一级代理**按件拿货**（拿货时一次性扣减一级代理库存件数），上架后按件零售；卖满（已卖=拿货件数）自动下架。

```
一级代理 (merchants.is_distributor=1，后台开通)  — 有货源，设进货价/建议零售价/库存件数
    ↓ 拿货（POST /resident-merchants/listings，quantity 件，扣一级代理库存）
业主小店 (营业中 resident_merchant_applications)  — 选品上架，自定零售价（≥进货价），件数制售卖
    ↓ 公开橱窗/分享链接（买家可见店主 residentId，可发起私聊）
买家购买 → 订单归属业主小店店铺主体（merchant_source=resident_shop）
    → 确认收货触发结算：
       商品额 = 零售价 × 件数
       货款   = 进货价 × 件数 → 一级代理商家钱包
       平台佣金 = 商品额 × 佣金率（默认 10%，后台可配）
       差价   = 商品额 − 货款 − 佣金 → 店主卖货钱包（仅可提现）
```

**件数守恒**：`took_quantity（拿货件数）= sold_quantity（已卖件数）+ 支付锁定中件数 + quantity_remaining（可售件数）`。

**listing 订单不参与物业四级分成/楼长分成**（`listingId != null` 的订单在完成结算时跳过常规分账，只走上述件数制结算链路）。

## 60.0.1 数据表

| 表名 | 说明 |
| --- | --- |
| `resident_merchant_applications` | 申请与审核（`rma_`）；v8.11 新增 `shop_name`（店铺名，默认"住户姓名的小店"）/ `merchant_id`（店铺主体商家 ID `mch_`，审核通过自动创建）；**v8.14 新增 `description`/`business_hours`/`contact_phone`/`cover_url`（店铺资料，对齐普通商家）** |
| `resident_merchant_deposits` | 保证金（`rmd_`），默认 500 元；可由平台配置 `residentMerchant.defaultDepositAmount` 覆盖 |
| `distributor_products` | 一级货源商品（`dpr_`），`stock` 语义=**库存件数** |
| `resident_merchant_listings` | 二级选品上架（`rml_`）；v8.11 新增件数制列：`took_quantity`（拿货件数）/ `sold_quantity`（已卖件数）/ `sold_out_unlisted`（是否因卖完自动下架）/ `quantity_remaining`（可售件数，支付锁定时扣减、取消/退款恢复） |
| `resident_merchant_settlements` | 结算（`rms_`）；v8.11 新增 `quantity`（本单结算件数） |
| `merchants` | v8.11 新增 `is_distributor`（是否一级代理，后台开通/取消） |
| `activity_leader_identities` | **v8.14 活动组组长身份表（`ali_`）**：住户子身份/标签，`source=appoint/application`，`status=active/revoked`；与楼长/负责人同模式 |
| `activity_leader_applications` | **v8.14 组长申请表（`ala_`）**：住户自助申请，管理端审核（`audit_status=pending/approved/rejected`） |
| `resident_forum_bans` | **v8.14 论坛禁言记录表（`rfb_`）**：影子封禁，`status=active/lifted/expired`，`expires_at` 为空=永久 |
| `community_posts` / `community_comments` | **v8.14 新增 `shadow_banned` 打标列**：禁言作者的内容仅本人与管理端可见 |

## 60.1 POST `/resident-merchants/apply` — 申请成为业主商户

**Roles**: `resident`（业主身份）

**请求体**：

| 字段 | 类型 | 必填 | 校验 | 说明 |
| --- | --- | --- | --- | --- |
| `agreedTerms` | Boolean | ✅ | 须为 true | 是否同意协议 |
| `shopName` | String | ❌ | `@Size(max=100)` | 小店名称（不传默认「{业主姓名}的小店」） |
| `description` | String | ❌ | `@Size(max=500)` | 🆕 v8.14 店铺简介（对齐普通商家） |
| `businessHours` | String | ❌ | `@Size(max=50)` | 🆕 v8.14 营业时间（如 `08:00-22:00`） |
| `contactPhone` | String | ❌ | `@Size(max=20)` | 🆕 v8.14 联系电话（不传则买家看到店主手机号） |
| `coverUrl` | String | ❌ | `@Size(max=500)` | 🆕 v8.14 店铺封面图URL |

**响应**：`applicationId`, `depositAmount`, `status`（`待缴保证金`/`pending_deposit`）, `visibility`（默认 `private`）

> 🔧 **说明（2026-09-04）**：`depositAmount` 现取**平台配置** `residentMerchant.defaultDepositAmount`（管理端 §60.12 `PUT /admin/resident-merchant-settings` 的 `defaultDepositAmount`），未配置时回落 `500.00`。此前该值硬编码为 500，后台改配置不生效。

> 🆕 **v8.14 店铺资料**：`description`/`businessHours`/`contactPhone`/`coverUrl` 均为可选，随申请落库；审核通过后作为店铺主体（`merchant_source=resident_shop`）资料，公开橱窗与订单展示一致。已开小店的主播可随时用 `PUT /resident-merchants/my/shop`（§60.9.1）修改。

## 60.2 POST `/resident-merchants/deposit/pay` — 缴纳保证金（微信支付）

**请求体**：`applicationId`, `paymentMethod`（`wechat` 或 `mock`）、`openid`（可选，不传取用户绑定 openid）

> **说明**：真实微信支付已接入（普通直连 JSAPI 预下单，`PaymentBizType.RESIDENT_SHOP_DEPOSIT`）。`wechat`：成功返回 `paymentRecordId` + `payParams`（`wx.requestPayment`），保证金流水 `pending`（待支付）；支付成功后由回调/查询兜底确认，保证金状态 `pending → frozen`、申请状态 `pending_audit`。**前置校验**：openid 缺失时不落任何数据。

**约束与容错（2026-09-04 修订）**：

| 场景 | 行为 |
| --- | --- |
| `paymentMethod=mock` | 受开关 `app.payment.mock-enabled` 控制。**生产环境已置 `false`**，关闭时返回 `PARAM_INVALID`（"当前环境未开启模拟支付…"）；仅 dev/测试开启 |
| `paymentMethod=wechat` 且微信支付未启用（`wechat.pay.enabled=false`） | 返回 `PARAM_INVALID`（"在线支付未开通…"），**不再静默按模拟支付直接到账** |
| 重新发起支付 | 上一次预下单流水超过 **5 分钟** 仍 `pending` 时自动作废（置 `closed`），用户可立刻重新下单，不会长期卡在「保证金支付处理中，请勿重复提交」 |
| 5 分钟内的重复提交 | 仍拦截，返回 `PARAM_INVALID`（"保证金支付处理中，请勿重复提交"），防止重复扣款 |
| 超时流水兜底 | `PendingPaymentScheduler`（30 分钟）对所有业务类型的超时 `pending` 流水统一收尾；**不再因 `wechat.pay.enabled=false` 跳过**，避免流水永久悬挂 |

**错误响应**：

| 错误码 | HTTP | 说明 |
| --- | --- | --- |
| `60010` | 400 | 申请不存在 / 非本人申请 / 当前状态不是 `pending_deposit` |
| `PARAM_INVALID` | 400 | `paymentMethod` 非法、模拟支付未开启、在线支付未开通、重复提交 |

## 60.2.1 保证金缴不上时的兜底路径（推荐）

线上微信支付失败或用户无法完成付款时，申请会长期停在 `pending_deposit`，**客户端无法自救**（再次支付被拦截、审核接口又只接受 `pending_audit`）。此时应引导用户**线下向物业缴纳保证金**，由管理端调用 §60.12 `POST /admin/resident-merchant-applications/{id}/confirm-deposit` 确认收款后继续流程。

## 60.3 GET `/resident-merchants/my` — 我的业主商户状态

**Auth**: Required  
**Roles**: `resident`

> 🔧 **说明（2026-08-19）**：`GET /resident-merchants/my` 返回的是**业主商户**状态（住户在首页"商家"页申请的店铺），与 §7 的**商家入驻申请**（账号登录后商家页面的"我的商家"）是**两套独立体系**，数据分别落在 `resident_merchant_applications` 与 `merchants` 表。
> 注意：§7.2 `GET /merchants/my` 对 `resident` 返回 `403`，住户查询"商家入驻申请"进度本应使用 §7.2.2 `GET /merchants/my-application`，但该接口**当前后端未实现**；请求勿调用，待后端补齐后再联调。

**响应**：见 `MyResidentMerchantResponse`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `residentId` | `String` | 住户ID |
| `propertyCompanyId` | `String` | 所属物业 |
| `statusCode` | `String` | `none`（未申请）/ `pending_deposit`（待缴保证金）/ `pending_audit`（待审核）/ `active`（营业中）/ `rejected`（已驳回）/ `closed`（已退出） |
| `status` | `String` | 状态中文描述 |
| `visibility` | `String` | `public`/`private` |
| `depositStatus` | `String` | 保证金状态（`none`/`pending`/`frozen`/`refunded`） |
| `depositAmount` | `BigDecimal` | 保证金金额（取平台配置 `defaultDepositAmount`，未配置时 500.00） |
| `balance` | `BigDecimal` | 物业币余额 |
| `listingCount` | `Integer` | 已上架商品数 |
| `auditRemark` | `String` | 驳回原因（若被驳回） |
| `createdAt` | `String` | 申请创建时间 |
| `shopName` | `String` | 🆕 v8.14 店铺名称 |
| `description` | `String` | 🆕 v8.14 店铺简介 |
| `businessHours` | `String` | 🆕 v8.14 营业时间 |
| `contactPhone` | `String` | 🆕 v8.14 联系电话（未设置为 null，前端可回退展示店主手机号） |
| `coverUrl` | `String` | 🆕 v8.14 店铺封面图URL |

**示例**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "residentId": "res_xxx",
    "propertyCompanyId": "pc_xxx",
    "statusCode": "pending_audit",
    "status": "待审核",
    "visibility": "private",
    "depositStatus": "frozen",
    "depositAmount": 500.00,
    "balance": 0.00,
    "listingCount": 0,
    "auditRemark": null,
    "createdAt": "2026-08-13 01:06:28"
  }
}
```

## 60.4 POST `/resident-merchants/quit` — 申请退出

条件：无进行中订单、无纠纷、满 **7** 天冷却期后退还保证金

## 60.5 GET `/distributors/products` — 一级货源选品池

**Query**：`categoryId`（兼容参数名 `category`）, `keyword`, `page`, `pageSize`

**Roles**: `resident`——🆕 **v8.11：仅营业中（`active`）的业主小店可访问**；非营业中（未申请/待缴保证金/待审核/已退出等）调用会被拦截（`PARAM_INVALID`，提示需营业中），前端应在入口层隐藏选品池。

**列表项**：`distributorProductId`, `name`, `coverUrl`, `wholesalePrice`, `suggestedRetailPrice`, `stock`（`stock`=该货源**剩余库存件数**，决定可拿货件数上限）

## 60.6 POST `/resident-merchants/listings` — 从一级选品拿货上架（件数制）

**Roles**: `resident`（营业中业主小店）

**请求体**（`CreateResidentMerchantListingRequest`）：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `distributorProductId` | `String` | ✅ | 一级货源商品 ID（`dpr_`） |
| `retailPrice` | `BigDecimal` | ✅ | 零售价；须 **≥ 该货源进货价**，否则 `10002`（"零售价不能低于进货价"） |
| `quantity` | `Integer` | ❌ | 🆕 v8.11 拿货件数，默认 1，须 ≥1（`60032`）；一次性从一级代理库存扣减，库存不足返回 `60033` |
| `enabled` | `Boolean` | ❌ | 是否立即上架，默认 `true` |

**行为**：拿货成功即创建 listing（`rml_`），`tookQuantity=quantity`、`soldQuantity=0`、`quantityRemaining=quantity`；一级代理货源 `stock` 同步扣减 `quantity`。

**错误码**：`60032`（拿货件数必须为不小于 1 的整数）、`60033`（一级代理货源库存不足）、`60004`（货源商品不存在/已停售）、`10002`（零售价低于进货价）。

## 60.6.1 DELETE `/resident-merchants/listings/{id}` — 取消拿货（退回未卖出件数）

> 🆕 **v8.11（件数制）**：删除自己从一级代理拿货的上架记录，**未卖出件数（`quantityRemaining`）退回一级代理库存**。

**Roles**: `resident`（营业中业主小店，仅本人 listing）

**前置校验**：该 listing 存在**未完成订单**（pending/paid/delivering/refunding 等）时禁止取消，返回 `60034`（该上架存在未完成订单，不可取消）。

**行为**：`quantityRemaining` 加回 `distributor_products.stock`，删除 listing 记录。

## 60.7 GET `/resident-merchants/listings` — 我的上架商品

**Auth**: Required  
**Roles**: `resident`（营业中业主商户）

**成功响应**：`{ "list": [ResidentMerchantListingItemResponse] }`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `String` | 上架记录ID（`rml_` 前缀），即橱窗 listingId |
| `distributorProductId` | `String` | 分销商品ID（`dp_` 前缀，一级选品库 `distributor_products`） |
| `productName` | `String` | 商品名称 |
| `coverUrl` | `String` | 封面图 |
| `wholesalePrice` | `BigDecimal` | 批发价 |
| `retailPrice` | `BigDecimal` | 零售价（业主定价） |
| `enabled` | `Boolean` | 是否上架 |
| `tookQuantity` | `Integer` | 🆕 v8.11 拿货件数（从一级代理库存一次性扣减的件数） |
| `soldQuantity` | `Integer` | 🆕 v8.11 已卖出件数（订单完成结算时累加） |
| `quantityRemaining` | `Integer` | 🆕 v8.11 可售件数（未锁定未售出；买家下单锁定时扣减，取消/超时/退款恢复） |
| `soldOutUnlisted` | `Boolean` | 🆕 v8.11 是否因卖完自动下架（退款恢复余量后自动重新上架） |
| `shareCount` | `Integer` | 分享次数 |
| `createdAt` | `String` | 创建时间 |

> ⚠️ **无商城 `productId`**：列表项**不包含** `products` 表商品ID，不能用于 `GET /products/{id}`（非 `prd_` 前缀会返回 `10002` 商品ID格式不正确）。分销商品卡片/分享应跳橱窗详情（`listingId + resellerId`），或使用公开橱窗 `GET /resident-merchants/public/{residentId}`（私密橱窗返回 `90111`）。

## 60.8 PUT `/resident-merchants/listings/{id}` — 改价/上下架

**请求体**（`UpdateResidentMerchantListingRequest`）：`retailPrice`（改价，仍须 ≥ 进货价）、`enabled`（上下架）。角色：`resident`（营业中业主小店，仅本人 listing）。

## 60.9 POST `/resident-merchants/listings/{id}/share` — 生成分享链接/海报

**响应**：

```json
{
  "shareUrl": "https://test.yuanxusoftware.top/h5/shop?listingId=rml_001&resellerId=res_xxx",
  "posterImageUrl": "https://test.yuanxusoftware.top/api/v1/static/posters/rml_001.png",
  "qrcodeUrl": "...",
  "visibility": "private"
}
```

## 60.9.1 PUT `/resident-merchants/visibility` — 设置对外/不对外展示

**Roles**: `resident`（营业中业主商户，`status=active`）

**请求体**：`visibility`（必填，`public`/`private`）

**响应**：同 §60.3（含 `visibility` 回显）

> 产品规则（会议纪要 20260727 §7.4）：`public`=对外，任何人可查看公开橱窗；`private`=不对外，仅店主/管理端可见。

## 60.9.1.1 PUT `/resident-merchants/my/shop` — 修改我的小店店铺资料（🆕 v8.14）

**Auth**: Required  
**Roles**: `resident`（营业中业主商户，`status=active`）

**Description**: 店主修改店铺资料，对齐普通商家的店铺信息（店名/简介/营业时间/联系电话/封面）。**未传字段不修改**；修改后同步店铺主体（`merchants` + `platform_merchants`），管理端与订单展示保持一致。

**请求体**（`UpdateResidentMerchantShopRequest`）：

| 字段 | 类型 | 必填 | 校验 | 说明 |
| --- | --- | --- | --- | --- |
| `shopName` | String | ❌ | `@Size(max=100)` | 店铺名称 |
| `description` | String | ❌ | `@Size(max=500)` | 店铺简介 |
| `businessHours` | String | ❌ | `@Size(max=50)` | 营业时间 |
| `contactPhone` | String | ❌ | `@Size(max=20)` | 联系电话 |
| `coverUrl` | String | ❌ | `@Size(max=500)` | 店铺封面图URL |

**响应**：同 §60.3（含更新后的店铺资料回显）

**错误响应**：

| 错误码 | HTTP | 说明 |
| --- | --- | --- |
| `60016` | 400 | 业主商户未营业/不存在（非 `active` 状态调用） |

## 60.9.2 GET `/resident-merchants/public` — 公开橱窗列表（任何人可查看）

**Auth**: Optional（无需登录）

**Query**：`page`（默认 1）, `pageSize`（默认 20）, `keyword`（店主姓名/手机号，可选）, `propertyCompanyId`（可选）

**仅返回**：`visibility=public` 且 `status=active` 的业主商户；姓名缺省时手机号脱敏展示。

**列表项**：`residentId`, `residentName`, `propertyCompanyId`, `shopName`（🆕 v8.14 店铺名，列表页可优先展示）, `visibility`, `status`, `depositAmount`, `listingCount`

## 60.9.3 GET `/resident-merchants/public/{residentId}` — 公开橱窗详情（任何人可查看）

**Auth**: Optional（无需登录）

**响应**：`residentId`, `residentName`, `propertyCompanyId`, `visibility`, `status`, `depositAmount`, `listingCount`, `listings`（仅 `enabled=true` 的商品）

**🆕 v8.14 店铺资料字段（对齐普通商家橱窗 §7.3）**：`shopName`（店铺名）、`description`（简介）、`businessHours`（营业时间）、`contactPhone`（联系电话；店铺未设置时后端回退为店主手机号）、`coverUrl`（封面图）

**listings[].字段**：`id`, `productName`, `coverUrl`, `retailPrice`, `shareCount`（**不含批发价/进货价**）

**错误**：`90111`（业主商户未对外展示）——店铺 `private` 或非营业中

## 60.10 POST `/orders` 增强（§8.1）

请求体新增可选字段（orders 表已有列；下单归因链路以字段透传为主）：

| 字段 | 说明 |
| --- | --- |
| `residentMerchantListingId` | 通过业主商户分享购买 |
| `resellerId` | 业主商户住户 ID（与 listing 校验一致） |

> **说明**：结算表 `resident_merchant_settlements` 与管理端查询已就绪；**自动 T+1 入账定时任务暂缓**。

## 60.10.1 POST `/orders/from-listing` — 业主小店下单（件数制）

> 🆕 **v8.11**：购买业主小店上架商品的正式下单入口；**订单归属业主小店店铺主体**（经 `listing → 店主 residentId → 业主商户申请 → merchantId` 解析，`merchant_source=resident_shop`，店铺缺失返回 `60036` 拒绝下单）。响应 `merchantId`/`merchantName` 为**小店店铺**（店名），前端订单卡片直接展示 `merchantName`。

**Auth**: Required  
**Roles**: `resident`

**请求体**（`CreateListingOrderRequest`）：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `listingId` | `String` | ✅ | 上架记录 ID（`rml_` 前缀） |
| `quantity` | `Integer` | ❌ | 🆕 v8.11 购买件数，默认 1，须 ≥1 且 ≤ `quantityRemaining`（超出返回 `60035`） |
| `address` | `String` | ❌ | 配送地址（≤200 字，不传取住户默认地址） |
| `contactPhone` | `String` | ❌ | 联系电话（≤20 字，不传取住户手机号） |
| `remark` | `String` | ❌ | 备注（≤500 字） |
| `familyId` | `String` | ❌ | 家庭 ID（不传取住户所属家庭） |

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/orders/from-listing \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "listingId": "rml_abc123",
    "quantity": 2,
    "address": "阳光花园1号楼1单元101",
    "contactPhone": "13800000000"
  }'
```

**成功响应**（`CreateOrderResponse`）：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "orderId": "ord_xxx",
    "orderNo": "ORD202610130001",
    "merchantId": "mch_xxx",
    "merchantName": "张阿姨的小店",
    "totalAmount": 70.00,
    "deliveryFee": 0.00,
    "paymentMethod": null,
    "pointUsed": 0,
    "coinUsed": 0.00,
    "cashAmount": 0.00,
    "orderStatus": "pending",
    "createdAt": "2026-10-13T08:00:00Z"
  }
}
```

**行为说明**：

1. 商品金额 = `listing.retailPrice × quantity`；配送费按**小店店铺主体**规则计算（默认 0 配送费、无满额减免，后台可调）；
2. 下单后 `orderStatus=pending`，支付成功才锁定件数（`quantityRemaining` 扣减）；走常规配送履约链路（支付后 `pending_choice`，支持配送/自提选择）；
3. 结算口径见 §60.15；支付走 `POST /orders/{id}/pay`（§8.2，`pointUsed` 前端固定传 0）。

**错误响应**：

| 错误码 | HTTP | 说明 |
| --- | --- | --- |
| `60004` | 404 | 上架商品/货源商品不存在或已停售 |
| `10002` | 400 | 该商品已下架 |
| `60035` | 400 | 可售件数不足 |
| `60036` | 404 | 业主小店店铺主体不存在 |
| `70002` | 409 | 订单状态不允许操作 |

## 60.11 POST `/orders/{id}/refund-request` — 退货申请

**Auth**: Required  
**Roles**: `resident`（仅订单本人）

**说明**：住户提交退货/退款申请，申请后订单状态变为 `refunding`，由管理端审核后执行实际退款。仅支持 `paid`（已支付）或 `completed`（已收货）状态的订单。

**请求示例**：
```json
{
  "reason": "商品与描述不符",
  "remark": "请提供退货单号"
}
```

**请求体**：
| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `reason` | `String` | 是 | 退货原因（200字以内） |
| `remark` | `String` | 否 | 补充说明（500字以内） |

**成功响应**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "orderId": "ord_xxx",
    "orderStatus": "refunding",
    "requestedAt": "2026-08-21T10:30:00Z",
    "message": "退货申请已提交，待物业审核"
  }
}
```

**错误响应**：
| 错误码 | HTTP | 说明 |
| --- | --- | --- |
| `70002` | 400 | 当前订单状态不可申请退货 |
| `70003` | 409 | 并发冲突 |
| `20004` | 403 | 非订单本人无权操作 |

---

## 60.11.1 管理端 — 订单退货审核

住户提交 `refund-request` 后订单进入 `refunding`，由物业在管理端审核：**通过**（执行实际退款：退积分/物业币 + **现金走微信原路退回**（先分账退回回笼资金，再调微信退款 API，流水置 `refunding` 由调度轮询终态）+ 恢复库存 + 撤销奖励）或**不通过**（驳回，订单置 `refund_rejected` 终态）。

### GET `/admin/orders/refunds` — 退货审核列表

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**说明**：查询住户退货申请及审核结果（待审核/已通过/已驳回）。支持按状态、关键词（订单号/收货人/手机号）、物业公司过滤。

**Query 参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `status` | `String` | ❌ | | `refunding`(待审核)/`refunded`(已通过)/`refund_rejected`(已驳回) |
| `keyword` | `String` | ❌ | | 搜索关键词（订单号/收货人/手机号） |
| `propertyCompanyId` | `String` | ❌ | | 物业公司ID（平台管理员可指定；物业管理员限定本物业） |

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "orderId": "ord_xxx",
        "orderStatus": "refunding",
        "cancelReason": "商品与描述不符"
      }
    ],
    "pagination": { "page": 1, "pageSize": 20, "total": 1, "totalPages": 1 }
  }
}
```

### POST `/admin/orders/refunds/{orderId}/audit` — 审核退货申请

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**说明**：审核通过（`approve`）则执行退款，订单置 `refunded`；审核不通过（`reject`）则驳回，订单置 `refund_rejected`，需填写 `rejectReason`。

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `action` | `String` | 是 | `approve`=通过(执行退款) / `reject`=不通过(驳回) |
| `rejectReason` | `String` | 否 | 驳回原因（`action=reject` 时必填，200字以内） |

**请求示例**：

```json
{ "action": "approve" }
```

```json
{ "action": "reject", "rejectReason": "商品无质量问题，驳回退货" }
```

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "orderId": "ord_xxx",
    "orderStatus": "refunded",
    "requestedAt": "2026-08-22T09:00:00Z",
    "message": "审核通过，已发起退款"
  }
}
```

**错误响应**：

| 错误码 | HTTP | 说明 |
| --- | --- | --- |
| `70002` | 400 | 该订单不在待审核退款状态 |
| `70003` | 409 | 并发冲突 |
| `PARAM_INVALID` | 400 | action 只能是 approve 或 reject；reject 时 rejectReason 必填 |

---

## 60.12 管理端

| 方法 | 路径 | 说明 | 状态 |
| --- | --- | --- | --- |
| GET | `/admin/resident-merchant-applications` | 申请列表 | ✅ |
| POST | `/admin/resident-merchant-applications/{id}/audit` | 物业审核 | ✅ |
| POST | `/admin/resident-merchant-applications/{id}/confirm-deposit` | 确认保证金已缴纳（线下收款） | ✅（2026-09-04 新增） |
| GET | `/admin/resident-merchant-deposits` | 保证金管理 | ✅ |
| POST | `/admin/resident-merchant-deposits/{id}/deduct` | 违规扣除 | ✅ |
| GET | `/admin/distributor-products` | 分销商品列表（🆕 v8.11 仅展示已开通一级代理权限 `is_distributor=1` 商家的商品） | ✅ |
| POST | `/admin/distributor-products` | 一级经销商上架（🆕 v8.11 **必传 `distributorMerchantId`**） | ✅ |
| PUT | `/admin/distributor-products/{id}` | 更新分销商品（🆕 2026-09-07，见下方） | ✅ |
| DELETE | `/admin/distributor-products/{id}` | 删除分销商品（软删 status=inactive，🆕 2026-09-07，见下方） | ✅ |
| PUT | `/admin/distributor-products/{id}/billing-cycle` | 设置收费周期 | ✅（详见 §63.2） |
| POST | `/admin/distributors/{merchantId}/enable` | 🆕 v8.11 开通一级代理权限（`is_distributor=1`） | ✅ |
| POST | `/admin/distributors/{merchantId}/disable` | 🆕 v8.11 取消一级代理权限（`is_distributor=0`） | ✅ |
| GET | `/admin/resident-merchant-settings` | 配置读取 | ✅ |
| PUT | `/admin/resident-merchant-settings` | 默认保证金、平台抽成、退货天数 | ✅ |

### POST `/admin/resident-merchant-applications/{id}/audit` — 物业审核业主商户申请

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**路径参数**：`id`（申请ID，`rma_`）

**请求体**（`AuditResidentMerchantApplicationRequest`，字段二选一，兼容两套命名）：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `auditResult` | `String` | 条件 | `approved`/`rejected`（优先读取） |
| `auditStatus` | `String` | 条件 | `approved`/`rejected`（兼容字段，`auditResult` 为空时使用） |
| `reason` | `String` | ❌ | 审核备注（兼容字段，`auditRemark` 为空时使用） |
| `auditRemark` | `String` | ❌ | 审核备注（优先读取） |

> 仅 `pending_audit` 状态可审核，否则返回 `60003`。`approved` → 申请置 `active` 并自动创建业主小店店铺主体；`rejected` → 申请置 `closed` 并停用店铺主体。

**成功响应**：`ResidentMerchantApplicationListItemResponse`：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "rma_demo001",
    "residentId": "res_demo001",
    "residentName": "张阿姨",
    "propertyCompanyId": "pc_demo001",
    "depositAmount": 500.00,
    "status": "active",
    "statusCode": "active",
    "visibility": "private",
    "createdAt": "2026-08-13 01:06:28"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

### POST `/admin/resident-merchant-deposits/{id}/deduct` — 违规扣除保证金

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**路径参数**：`id`（保证金记录ID，`rmd_`）

**请求体**（`DeductResidentMerchantDepositRequest`）：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `amount` | `BigDecimal` | ✅ | 扣除金额，须 > 0 |
| `reason` | `String` | ❌ | 扣除原因，写入 `deduct_reason` |

**说明**：仅 `frozen` 状态的保证金可扣除；累计扣除金额 ≥ 保证金金额时状态置 `deducted`。响应为 `ResidentMerchantDepositListItemResponse`（`id`/`applicationId`/`residentId`/`residentName`/`amount`/`status`/`paymentMethod`/`paidAt`/`deductedAmount`）。

### GET/PUT `/admin/resident-merchant-settings` — 业主商户配置

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**PUT 请求体**（`UpdateResidentMerchantSettingsRequest`，全部可选）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `defaultDepositAmount` | `BigDecimal` | 默认保证金金额（未配置时 500.00） |
| `platformCommissionRate` | `BigDecimal` | 平台佣金比例（0~1） |
| `distributorProductCommissionRate` | `BigDecimal` | 业主小店销售一级代理货品的平台佣金比例（默认 0.10） |
| `refundWindowDays` | `Integer` | 退款窗口天数 |
| `markupMinRate` | `BigDecimal` | 业主小店加价下限（0~1） |
| `markupMaxRate` | `BigDecimal` | 业主小店加价上限（0~1） |

**GET/PUT 成功响应**（`ResidentMerchantSettingsResponse`）：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "defaultDepositAmount": 500.00,
    "platformCommissionRate": 0.10,
    "distributorProductCommissionRate": 0.10,
    "refundWindowDays": 7,
    "markupMinRate": 0.05,
    "markupMaxRate": 0.10
  },
  "timestamp": "2026-07-01 15:23:33"
}
```


### POST `/admin/distributors/{merchantId}/enable` / `disable` — 开通/取消一级代理权限（v8.11 新增）

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`（物业隔离：`property_admin` 仅限本物业商家）

**Path**：`merchantId` 商家 ID（`mch_` 前缀）

**行为**：

| 接口 | 行为 |
| --- | --- |
| `POST /admin/distributors/{merchantId}/enable` | 将商家 `merchants.is_distributor` 置 `1`；仅审核通过（`approved`）的商家可开通；开通后可在货源池上架商品并接收业主小店货款 |
| `POST /admin/distributors/{merchantId}/disable` | 将 `is_distributor` 置 `0`；已有货源商品保留（历史可追溯），仅失去新建货源资格 |

**成功响应**：`data: null`

**错误响应**：`60001`（商家不存在）、`60037`（仅审核通过的商家可管理一级代理权限——商家非 `approved`）、`20004`（跨物业/权限不足）

### GET `/admin/distributor-products` — 分销商品列表

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

> **对接注意**：管理端列表请用本接口。业主选品池 `GET /distributors/products` 仅允许 `resident`，管理员 Token 调用会失败。

**Query**：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `page` | int | | 页码（默认 1） |
| `pageSize` | int | | 每页条数（默认 20，最大 100） |
| `keyword` | String | | 按商品名称模糊搜索 |
| `propertyCompanyId` | String | 条件 | 平台管理员可指定；物业管理员可不传，后端取登录物业。过滤逻辑：商品关联的一级经销商商家（`distributor_merchant_id`）所属 `propertyCompanyId` |

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "dpr_xxx",
        "distributorMerchantId": "mch_xxx",
        "propertyCompanyId": "pc_demo001",
        "name": "洗衣液",
        "coverUrl": "/uploads/product.png",
        "wholesalePrice": 20.00,
        "suggestedRetailPrice": 35.00,
        "stock": 100,
        "billingCycle": "monthly",
        "status": "active",
        "createdAt": "2026-07-25T10:00:00Z"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 1,
      "totalPages": 1
    }
  }
}
```

`billingCycle` 枚举：`one_time` / `monthly` / `quarterly` / `yearly`（可能为空，旧数据未设置时）。商品 ID 前缀为 `dpr_`。

### POST `/admin/distributor-products` — 一级经销商上架商品

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

> 🆕 **v8.11 变更**：请求体**必传 `distributorMerchantId`**（不再自动取第一个认证商家）；未传返回 `60031`；指定商家须已开通一级代理权限（`is_distributor=1`，否则 `60030`）且属于操作物业。`stock` 语义为**库存件数**（业主小店按件拿货扣减）。

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `distributorMerchantId` | String | ✅（🆕 v8.11） | 一级代理商家 ID（`mch_`，须已开通 `is_distributor` 且属本物业） |
| `name` | String | ✅ | 商品名称 |
| `coverUrl` | String | | 封面图 |
| `categoryId` | String | | 分类 ID（当前可传，落库以实际实现为准） |
| `category` | String | | 货源类目（如 `食品饮料`、`日用百货`，公开列表按此筛选） |
| `description` | String | | 描述 |
| `wholesalePrice` | BigDecimal | ✅ | 进货价 |
| `suggestedRetailPrice` | BigDecimal | ✅ | 建议零售价 |
| `stock` | Integer | | 库存件数，默认 0 |
| `serviceScope` | String | | 服务区间 `city`(本市)/`community`(小区) |
| `supplyScope` | String | | 供给范围：`company`(本物业公司，默认)/`platform`(全平台)；**仅管理端可设置** |

**错误响应**：`60031`（必须指定一级代理商家 distributorMerchantId）、`60030`（该商家未开通一级代理权限）、`60037`（该商家未通过审核，不可上架货源商品）、`60001`（商家不存在）、`20004`（跨物业/权限不足）。

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "dpr_xxx"
  }
}
```

### PUT `/admin/distributor-products/{id}` — 更新分销商品

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

> 🆕 **2026-09-07 新增（前端问题 H）**：此前管理端无法修改分销商品内容，仅能设置收费周期。全部字段可选，仅更新传入字段。

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | String | ✅ | 分销商品ID（`dpr_`） |

**请求体**（全部可选）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `name` | String | 商品名称 |
| `coverUrl` | String | 封面图URL |
| `category` | String | 类目/类目ID |
| `description` | String | 商品描述 |
| `wholesalePrice` | BigDecimal | 批发价（元） |
| `suggestedRetailPrice` | BigDecimal | 建议零售价（元） |
| `stock` | Integer | 库存（正数补货 / 负数扣减到该值；传原值即不变） |
| `serviceScope` | String | 服务区间 `city`(本市)/`community`(小区) |
| `supplyScope` | String | 供给范围：`company`(本物业公司)/`platform`(全平台)；**仅管理端可设置** |
| `inactive` | Boolean | `true` 下架（status=inactive），`false` 恢复上架 |

**请求示例**：

```json
{ "name": "有机大米 5kg", "wholesalePrice": 35.00, "stock": 100 }
```

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "dpr_xxx",
    "name": "有机大米 5kg",
    "wholesalePrice": 35.00,
    "suggestedRetailPrice": 49.90,
    "stock": 100,
    "status": "active"
  }
}
```

> **错误**：商品不存在 → `60004`；跨物业商品 → `20004`。

### DELETE `/admin/distributor-products/{id}` — 删除分销商品

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

> 🆕 **2026-09-07 新增（前端问题 H）**：**软删除**（`status=inactive`），保留历史拿货记录，列表默认不再展示；如需恢复，调 PUT 传 `"inactive": false`。

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | String | ✅ | 分销商品ID（`dpr_`） |

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "dpr_xxx",
    "name": "商品名",
    "wholesalePrice": 20.00,
    "suggestedRetailPrice": 35.00,
    "stock": 100,
    "status": "inactive"
  }
}
```

### POST `/admin/resident-merchant-applications/{id}/confirm-deposit` — 确认保证金已缴纳（线下收款）

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`（物业管理员仅限本物业的申请）

> 🆕 **2026-09-04 新增**：用于解除「线上保证金支付阻塞」。业主商户申请因微信支付失败/超时而长期停在 `pending_deposit` 时，客户端既无法继续支付、审核接口又只接受 `pending_audit`，形成死锁。此时由物业**线下收取保证金**后调用本接口确认：保证金置 `frozen`，申请推进到 `pending_audit`（或直接 `active`）。

**请求体**（全部可选，Body 可整体省略）：

| 字段 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| `paymentMethod` | String | | `offline` | 收款方式：`offline`(线下)/`transfer`(转账)/`cash`(现金)/`wechat`(微信，人工核实) |
| `directActivate` | Boolean | | `false` | `true` = 直接开通（申请置 `active`，跳过待审核） |
| `remark` | String | | | 备注（收款凭证号等，200 字以内） |

**请求示例**：

```json
{ "paymentMethod": "offline", "directActivate": true, "remark": "现金收款，凭证号 XJ20260904001" }
```

**成功响应**：同申请列表项（`ResidentMerchantApplicationListItemResponse`）

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "rma_351c0e77581000",
    "residentId": "res_dev001",
    "residentName": "张阿姨",
    "propertyCompanyId": "pc_demo001",
    "depositAmount": 500.00,
    "status": "active",
    "statusCode": "active",
    "visibility": "private",
    "createdAt": "2026-08-13 01:06:28"
  }
}
```

**行为说明**：

1. **幂等**：申请已处于 `pending_audit` 或 `active` 时直接返回当前状态，不重复写入。
2. 确认时会**作废该申请下所有未完成的线上预下单流水**（`payment_records` 中 `biz_type=resident_shop_deposit` 且 `status=pending` 置 `closed`，`errorMsg="管理端已线下确认收款，原线上流水作废"`），避免线下收款后又被微信回调重复入账。
3. `directActivate=false` 时申请转 `pending_audit`，仍需走 `POST /admin/resident-merchant-applications/{id}/audit` 审核；`directActivate=true` 时直接置 `active` 并写入 `auditedBy`/`auditedAt`。

**错误响应**：

| 错误码 | HTTP | 说明 |
| --- | --- | --- |
| `70001` | 404 | 申请不存在 |
| `60003` | 409 | 申请状态不可确认收款（非 `pending_deposit`，如已关闭/退出中） |
| `20004` | 403 | 跨物业操作或权限不足 |

### GET `/admin/resident-merchant-applications` — 申请列表

**Roles**：`property_admin`, `platform_admin`

**Query**：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `page` | int | | 页码（默认 1） |
| `pageSize` | int | | 每页条数（默认 20） |
| `status` | String | | 状态过滤（不传默认 `pending_audit`） |
| `keyword` | String | | 按住户姓名/手机号 **或店铺名称** 搜索（v8.28 起为 OR 关系） |
| `propertyCompanyId` | String | | 物业公司 ID（平台超管用） |

**列表项**：`id`, `residentId`, `residentName`, `propertyCompanyId`, `depositAmount`, `status`, `statusCode`, `visibility`, `shopName`, `description`, `businessHours`, `contactPhone`, `coverUrl`, `createdAt`

> v8.28 新增 5 个店铺资料字段：`shopName`（店铺名）、`description`（简介）、`businessHours`（营业时间）、`contactPhone`（联系电话）、`coverUrl`（封面图）；并将 `keyword` 扩展为「住户姓名/手机号 OR 店铺名称」。管理端审核页可直接加列，无需再单独查店铺接口。

### GET `/admin/resident-merchant-deposits` — 保证金列表

**Roles**：`property_admin`, `platform_admin`

**Query**：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `page` | int | | 页码（默认 1） |
| `pageSize` | int | | 每页条数（默认 20） |
| `status` | String | | 状态过滤：`frozen`/`deducted` |
| `propertyCompanyId` | String | | 物业公司 ID（平台超管用） |

**列表项**：`id`, `applicationId`, `residentId`, `residentName`, `amount`, `status`, `paymentMethod`, `paidAt`, `deductedAmount`

## 60.13 GET `/admin/resident-merchant-settlements` — 结算明细

**Roles**: `property_admin`, `platform_admin`

**Query**：`page`, `pageSize`, `startDate`, `endDate`

**列表项**：`id`, `orderId`, `listingId`, `residentId`, `orderAmount`, `resellerProfit`, `status`（`settled`/`reversed`）, `settleAt`, `createdAt`（🆕 v8.11 结算单含 `quantity` 本单结算件数，落库字段）

---

## 60.14 业主小店店铺主体与订单归属（v8.11 新增）

> 🆕 **v8.11**：业主商户审核通过（`active`）时自动创建**店铺主体**——`platform_merchants` + `merchants`（`merchant_source=resident_shop`、`audit_status=approved`、`status=active`）+ `merchant_property_configs`（配送费默认 `0.00`、无满额减免；佣金率=平台配置 `residentMerchant.distributorProductCommissionRate`，默认 `0.1000`；后台可调）。存量已 active 的业主商户由 V22 脚本自动补建店铺主体并回填 `merchant_id`/`shop_name`。

| 项 | 说明 |
| --- | --- |
| 店铺名 | `resident_merchant_applications.shop_name`，默认"`住户姓名`的小店"；下单响应 `merchantName` 即店名 |
| 订单归属 | `POST /orders/from-listing` 订单 `merchantId` = 小店店铺主体（非一级代理）；配送费/履约/佣金按店铺主体配置 |
| 店铺主体缺失 | 下单/试算返回 `60036`（业主小店店铺主体不存在），拒绝下单避免错挂一级代理 |
| 商家列表隔离 | 移动端常规商家列表（`GET /merchants`）与附近商家**排除** `merchant_source=resident_shop` 店铺主体（小店仅在业主橱窗体系展示）；管理端不受限 |
| 前端展示 | 订单卡片直接渲染接口返回的 `merchantName`（小店店名） |

## 60.15 件数制结算口径（v8.11 新增，确认收货触发）

```
设：零售价 P，进货价 W，件数 Q，佣金率 R（默认 0.10，后台可配）

商品额     = P × Q（不含配送费，即 order.totalAmount）
一级代理货款 = W × Q            → 一级代理商家钱包（wallet_flows，entryType=distributor_wholesale_income）
平台佣金    = 商品额 × R        → 平台
店主差价    = 商品额 − 货款 − 佣金 → 店主卖货钱包（wallet_flows，ownerType=resident_merchant，entryType=reseller_profit）
（差价为负时置 0）
```

**行为规则**：

| 场景 | 行为 |
| --- | --- |
| 卖满自动下架 | `soldQuantity = tookQuantity` 时 listing 自动 `enabled=false` 并标记 `soldOutUnlisted=true` |
| 退款冲正 | 已结算订单退款：2 条负向钱包流水（店主差价 + 一级代理货款，`refRecordId` 指向原流水）、结算单置 `reversed`、`soldQuantity` 回退件数、可售件数恢复；卖完下架的 listing 恢复余量后**自动重新上架** |
| 支付锁定 | 买家支付成功（含微信预下单预占）扣 `quantityRemaining`；取消/超时/退款加回 |
| 未支付取消守卫 | `pending` 订单仅当**发起过支付**（存在支付流水）才回补件数；从未发起支付的取消不回补（防 `quantityRemaining` 虚增超卖，前端无感知） |
| 分成隔离 | listing 订单**不走**物业四级分成/楼长分成/物业经理抽成（跳过 `DistributionService` 常规分账） |
| 店主提现 | 业主卖货钱包不变：`GET /resident-merchants/seller-wallet`、`POST/GET /resident-merchants/seller-wallet/withdrawals`（`withdrawalType=resident_seller`，见 §99.3） |

---

## 60.16 一级代理申请与身份（v8.12.1 新增，2026-10-20）

> 一级货源经销商（`merchants.is_distributor=1`）此前只能由管理端开通。本节新增**用户自助申请**全链路：
> 任意已登录角色提交申请 → 管理端审核 → 通过后由后端同事务创建/关联商家载体并开通一级代理。
> 与 §37.7「个体负责人申请」**完全无关**（那是物业板块负责人，历史也叫"一级代理申请"，切勿混淆）。

### 60.16.1 POST `/distributors/apply` — 提交一级代理申请（C端）

**Auth**: Required  
**Roles**: 任意已登录角色（resident / merchant / courier / technician / activity_leader / coordinator / sector_leader / individual_leader / property_admin / platform_admin / community_admin）

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `contactName` | String | 是 | 联系人姓名，≤50字 |
| `contactPhone` | String | 是 | 联系电话（11位手机号） |
| `businessScope` | String | 是 | 经营品类：`fresh`=生鲜 / `daily`=日用 / `grain`=粮油 / `other`=其它 |
| `serviceScope` | String | 是 | 服务范围：`community`=本小区 / `city`=本市 |
| `remark` | String | 否 | 备注，≤200字 |

**成功响应**：
```json
{ "code": 0, "data": { "applicationId": "dap_xxx", "status": "pending",
  "contactName": "张三", "contactPhone": "13800000000",
  "businessScope": "daily", "serviceScope": "community",
  "submittedAt": "2026-10-20T02:00:00.000Z", "message": "申请已提交，等待物业审核" } }
```

**约束与错误码**：同一用户同物业仅一条 `pending` → `97042`；已是一级代理 → `97043`；枚举越界/参数缺失 → `10002`。

### 60.16.2 GET `/distributors/my/application` — 查询我的申请状态（C端）

**Roles**: 同上。返回本人最新一条申请；`status`: `none`/`pending`/`approved`/`rejected`。`rejected` 带 `rejectReason`，`approved` 带 `distributorMerchantId`，无申请返回 `status=none`。

**成功响应（有申请）**：
```json
{ "code": 0, "data": {
  "applicationId": "dap_37dcd56a341000",
  "status": "approved",
  "contactName": "张阿姨",
  "contactPhone": "13624964818",
  "businessScope": "fresh",
  "serviceScope": "community",
  "remark": null,
  "rejectReason": null,
  "distributorMerchantId": "mch_xxx",
  "submittedAt": "2026-10-21T06:12:30.000Z",
  "message": "申请已通过"
} }
```

**成功响应（无申请）**：
```json
{ "code": 0, "data": { "applicationId": null, "status": "none", "message": "暂无申请记录" } }
```

> **v8.15 修复**：此前该接口以 `auditStatus=null` 调用派生查询，Spring Data 将其翻译为 `audit_status IS NULL`，导致有申请也恒返回 `none`。现按 `createdAt` 倒序取最新一条，前端无需改动调用方式。

### 60.16.3 GET `/distributors/my/identity` — 查询我的一级代理身份（C端）

**Roles**: 同上。返回当前用户一级代理身份与钱包；**未开通时 HTTP 200 + `code 0` + `isDistributor=false`**（不返回 `60030`）。

```json
{ "code": 0, "data": { "isDistributor": true, "distributorMerchantId": "mch_xxx",
  "merchantName": "张三的一级代理", "auditStatus": "approved",
  "withdrawableAmount": 12345.00, "withdrawalBlocked": false,
  "totalRevenue": 20000.00, "totalOrders": 30 } }
```

> `withdrawableAmount`=可提现余额（含一级代理货款 `distributor_wholesale_income`），`withdrawalBlocked`=是否被禁止提现。前端据此做入口显隐与钱包展示，无需再"试调列表接口"探测身份（解决前端 D2/D3/D5）。

### 60.16.4 管理端 — 一级代理申请审核

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/admin/distributor-applications` | 申请列表（按物业隔离，platform_admin 可跨物业；支持 `auditStatus` 过滤，`createdAt` 倒序） |
| POST | `/admin/distributor-applications/{applicationId}/audit` | 审核：`approved` 同事务自动建载体并开通一级代理；`rejected` 记录 `rejectReason` |

**Roles**: `property_admin`, `platform_admin`。

#### 列表 `GET /admin/distributor-applications`

| 查询参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `page` | int | ❌ | 页码，默认 1 |
| `pageSize` | int | ❌ | 每页条数，默认 20 |
| `auditStatus` | String | ❌ | `pending` / `approved` / `rejected` |
| `propertyCompanyId` | String | ❌ | 仅 `platform_admin` 跨物业查询时使用；**v8.15 起不传即返回全部物业**。`property_admin` 始终强制本物业 |

列表项字段：`id`、`residentId`、`contactName`、`contactPhone`、`businessScope`、`serviceScope`、`remark`、`auditStatus`、`rejectReason`、`distributorMerchantId`、`auditedAt`、`createdAt`、`propertyCompanyId`。

#### 审核 `POST /admin/distributor-applications/{applicationId}/audit`

**路径参数**：`applicationId`（`dap_` 前缀）。

**请求体**（`AuditDistributorApplicationRequest`）：

| 字段 | 类型 | 必填 | 校验 | 说明 |
| --- | --- | --- | --- | --- |
| `auditStatus` | String | ✅ | `approved` / `rejected` | 审核结果 |
| `rejectReason` | String | 条件必填 | ≤200 字 | `rejected` 时必填 |

**请求示例（通过）**：
```json
{ "auditStatus": "approved" }
```

**成功响应（通过）**：
```json
{ "code": 0, "data": {
  "applicationId": "dap_37dcd56a341000",
  "auditStatus": "approved",
  "residentId": "res_xxx",
  "contactName": "张阿姨",
  "businessScope": "fresh",
  "serviceScope": "community",
  "rejectReason": null,
  "auditedAt": "2026-10-22T01:05:11.000Z",
  "distributorMerchantId": "mch_xxx"
} }
```

**请求示例（驳回）**：
```json
{ "auditStatus": "rejected", "rejectReason": "暂不满足经营资质要求" }
```

**审核通过（approved）副作用**：同一事务内完成载体闭环——创建 `platform_merchants` + `merchants`（`is_distributor=1`、`audit_status=approved`、`merchant_source=distributor`、`merchant_type=goods`、`is_canteen_main_merchant=0`、`serve_all_communities=1`、关联申请人 `resident_id`）+ `merchant_property_configs`；申请人名下已有合格载体则复用（幂等）。**v8.15 修复**：此前 `merchant_type` / `is_canteen_main_merchant` 未赋值导致 NOT NULL 约束报错（HTTP 400），现已在实体层补齐默认值并覆盖所有建店路径，前端无需改动。

**错误码**：`97040`（申请不存在）、`97041`（已审核不可重复）、`20004`（跨物业/权限不足）、`PARAM_MISSING`（rejected 未填原因）、`ACCOUNT_DISABLED`（申请人账号停用）。

> **重试说明**：修复前审核失败的事务已整体回滚，申请仍为 `pending`；部署 v8.15 后直接重新点击「通过」即可，无需补数据。

### 60.16.5 数据表

| 表 | 说明 |
| --- | --- |
| `distributor_applications` | 一级代理申请表（`dap_`）：申请人、物业、联系人、经营品类、服务范围、审核状态、拒绝原因、审核通过后载体ID |
| `merchants.is_distributor` | V22 已存在；审核通过置 1 |

迁移脚本：**统一使用 `sql/manual_upgrade_20261022_distributor_leader_audit_fix.sql`**（幂等可重复执行）——包含 `merchants` 四个 NOT NULL 列兜底与 NULL 修复、`distributor_applications` 表、活动组长身份/申请表、存量 `role=activity_leader` 迁移。Flyway `V24__distributor_applications.sql` 仅作留档（生产 `flyway.enabled=false`，不执行）。

---

# 三期接口总览（§61–§72）

| 模块 | 路径前缀 | 接口数 |
| --- | --- | --- |
| §61 模块负责人体系 | `/sector-leaders/*`, `/individual-leaders/*` | 8 |
| §62 快递负责人体系 | `/courier-managers/*`, `/admin/delivery-price-ranges` | 13 |
| §63 商家端口改造 | `/merchant/ads/targeted`, `/admin/distributor-products` | 3 |
| §64 统筹管理 | `/coordinators/my`, `/coordinators/{id}/*` | 6 |
| §65 价格审批 | `/admin/price-approvals` | 3 |
| §66 通知/发布增强 | `/announcements/product`, `/announcements/activity` | 2 |
| §67 询价 | `/inquiries` | 4 |
| §68 活动组增强 | `/activity-groups/{id}/members/{memberId}/profile` | 1 |
| §69 物业币商城规则 | `/admin/property-coin/mall-rules` | 2 |
| §70 多角色提现 | `/individual-leaders/{id}/withdrawals`, `/courier-managers/my/withdrawals`, `/admin/role-withdrawals*` | 7 |
| §71 平台管理员分成 | `/admin/platform-share-rates`, `/admin/platform-earnings/*` | 4 |
| §72 技工工作台 | `/technicians/my`, `/technicians/my/tasks` | 3 |

# 61. 三期 — 模块负责人体系 (sector-leaders / coordinators)

- **统筹（coordinator）**：新增板块负责人、新增个体负责人、上传商家、审核商家加入、提现。
- **板块负责人（sector_leader）**：管理本板块商家、审批商家加入/分成、查看收入与提现。
- **个体负责人（individual_leader）**：设置商家分成、满额配送费、查看收入与提现。

> **个体负责人路径约定**：个体负责人工作台接口路径参数 `{id}` 支持 `il_`（个体负责人记录 ID）、`res_`（住户 ID）或字面量 `me`（当前登录个体负责人）。建议先调 `GET /individual-leaders/my` 取得标准 `il_` ID。

## 61.1 POST `/coordinators/{id}/individual-leaders` — 增加个体负责人

**Roles**: `coordinator`（须持有 active 统筹记录）、`property_admin`

> **路径变更（2026-10-27 核对）**：旧文档写作 `/sector-leaders/{id}/individual-leaders`，实际代码为 `/coordinators/{id}/individual-leaders`。

**Path**: `id` — 统筹ID（`coo_`）

**请求体**（`AddIndividualLeaderRequest`）：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `residentId` | `String` | ✅ | 业主ID（`res_`） |
| `sectorLeaderId` | `String` | ✅ | 所属板块负责人ID（`sl_`），须属于当前统筹 |
| `sector` | `String` | ✅ | 板块编码，须与板块负责人一致 |
| `name` | `String` | ❌ | 个体负责人显示名；空则用业主姓名 |
| `commissionRate` | `BigDecimal` | ❌ | 抽佣比例；空则取物业公司默认 |

**成功响应**（`IndividualLeaderCreatedResponse`）：

```json
{
  "code": 0,
  "data": {
    "id": "il_xxx",
    "residentId": "res_xxx",
    "sectorLeaderId": "sl_xxx",
    "propertyCompanyId": "pc_xxx",
    "sector": "cleaning",
    "name": "王强家政",
    "commissionRate": 0.05,
    "status": "active"
  }
}
```

**逻辑**：校验板块负责人属于当前统筹、`sector` 与板块负责人一致、业主同物业；同一业主同一板块负责人下不可重复。

---

## 61.2 DELETE `/sector-leaders/{id}/individual-leaders/{subId}` — 删除个体负责人

> ⚠️ **【当前未实现 · 2026-10-27 核对代码】** 后端不存在该删除接口。撤销个体负责人请使用管理端 `DELETE /admin/individual-leaders/{id}`（§37.5），或由统筹/物业在管理端操作。

---

## 61.3 POST `/coordinators/{id}/merchants` — 上传商家

**Roles**: `coordinator`（须持有 active 统筹记录）、`property_admin`

> **路径变更（2026-10-27 核对）**：旧文档写作 `/sector-leaders/{id}/merchants`，实际代码为 `/coordinators/{id}/merchants`。

**Path**: `id` — 统筹ID（`coo_`）

**请求体**（`AddCoordinatorMerchantRequest`）：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `platformMerchantId` | `String` | ✅ | 平台商家ID（`pm_`） |
| `name` | `String` | ✅ | 商家名称 |
| `category` | `String` | ❌ | 商家分类 |
| `contactPhone` | `String` | ❌ | 联系电话 |
| `address` | `String` | ❌ | 地址 |

**成功响应**（`CoordinatorMerchantCreatedResponse`）：

```json
{
  "code": 0,
  "data": {
    "id": "mch_xxx",
    "platformMerchantId": "pm_xxx",
    "propertyCompanyId": "pc_xxx",
    "coordinatorId": "coo_xxx",
    "name": "测试商家",
    "category": "外卖",
    "contactPhone": "13800000000",
    "address": "小区东门",
    "auditStatus": "approved",
    "status": "active"
  }
}
```

**逻辑**：关联 `platformMerchant` 创建商家，防重复挂接（`30004`）。

---

## 61.4 POST `/sector-leaders/{id}/merchant-audits` — 同意/拒绝商家加入

**Roles**: `resident`（须持有启用中的板块负责人记录；v8.13 负责人标签化后不再要求 `role=sector_leader`）

> 🔧 **路径修正（2026-09-03）**：旧文档写作 `PUT /sector-leaders/{id}/merchant-audit`，与实际代码不一致；
> 实际为 **`POST`** 且路径为 **`/merchant-audits`**，请求体字段为 `remark`（非 `reason`）。
> 注意与统筹侧区分：统筹为 `PUT /coordinators/{id}/merchant-audit`（§64.5，单数、PUT）。

```json
// Request Body
{
  "merchantId": "mch_xxx",
  "approved": true,
  "remark": "审核通过"
}
```

## 61.5 PUT `/sector-leaders/{id}/individual-leaders/{subId}/distribution` — 设置个体负责人分成

> ⚠️ **【当前未实现 · 2026-10-27 核对代码】** 后端不存在该接口。个体负责人抽佣比例请在管理端更新 `PUT /admin/individual-leaders/{id}`（§37.4，字段 `commissionRate`）；个体负责人设置**商家**分成请用 §61.6。

---

## 61.6 PUT `/individual-leaders/{id}/merchant-distribution` — 个体负责人设置商家分成

**Roles**: `resident`（须持有启用中的个体负责人记录；v8.13 负责人标签化后不再要求 `role=individual_leader`）  
**Path**: `id` — `il_` / `res_` / `me`

```json
// Request Body
{
  "merchantId": "mch_xxx",
  "commissionRate": 0.10
}
```

**逻辑**：创建 `PriceApproval` 记录（`itemType=merchant_distribution`，`status=pending`）待模块负责人审批。

## 61.7 PUT `/individual-leaders/{id}/delivery-fee` — 设置满额配送费

**Roles**: `resident`（须持有启用中的个体负责人记录；v8.13 负责人标签化后不再要求 `role=individual_leader`）  
**Path**: `id` — `il_` / `res_` / `me`

```json
// Request Body
{
  "merchantId": "mch_xxx",
  "freeDeliveryThreshold": 50.00
}
```

## 61.8 GET `/individual-leaders/my` — 当前个体负责人信息

**Auth**: Required  
**Roles**: `resident`（须持有启用中的个体负责人记录；v8.13 负责人标签化后不再要求 `role=individual_leader`）

返回当前登录用户对应的个体负责人档案；后续工作台接口路径建议使用响应中的 `id`（`il_` 前缀）。

**响应**（`IndividualLeaderMyResponse`）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `String` | 个体负责人记录 ID（`il_`） |
| `residentId` | `String` | 业主 ID |
| `residentName` | `String` | 业主姓名 |
| `phone` | `String` | 手机号 |
| `name` | `String` | 个体负责人姓名 |
| `propertyCompanyId` | `String` | 物业公司 ID |
| `propertyCompanyName` | `String` | 物业公司名称 |
| `sectorLeaderId` | `String` | 板块负责人 ID |
| `sector` | `String` | 所属板块 |
| `commissionRate` | `BigDecimal` | 分成比例 |
| `totalEarnings` | `BigDecimal` | 累计收益 |
| `status` | `String` | 状态 |

---

# 62. 三期 — 快递负责人体系 (courier-managers)

快递负责人CRUD、快递员管理、工作台；物业公司配送价格区间。

## 62.1 POST `/courier-managers` — 创建快递负责人

**Roles**: `property_admin`, `platform_admin`

```json
// Request Body
{
  "residentId": "res_xxx",
  "communityId": "com_xxx",
  "name": "张三",
  "phone": "13800000000",
  "responsibleArea": "1栋-5栋",
  "deliveryTimeConfig": "{\"start\":\"08:00\",\"end\":\"20:00\"}"
}
```

## 62.2 GET `/courier-managers` — 快递负责人列表

**Roles**: `property_admin`, `platform_admin`

**Query**: `page`, `pageSize`, `communityId`, `status`, `keyword`

## 62.3 GET `/courier-managers/{id}` — 快递负责人详情

## 62.4 PUT `/courier-managers/{id}` — 更新快递负责人

**请求体**（全部可选，仅更新传入字段）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `name` | `String` | 负责人姓名（≤50） |
| `phone` | `String` | 联系电话（≤20） |
| `communityId` | `String` | 小区 ID |
| `responsibleArea` | `String` | 负责区域 |
| `deliveryTimeConfig` | `String` | 配送时间配置 JSON |
| `status` | `String` | `active`/`inactive` |
| `shareRate` | `BigDecimal` | 快递负责人分成比例（0~1，对应 `courier_managers.share_rate`） |

## 62.5 DELETE `/courier-managers/{id}` — 删除快递负责人

设为 `inactive`，并解绑名下所有快递员。

## 62.6 POST `/courier-managers/{id}/couriers` — 增加快递员

```json
// Request Body
{ "residentId": "res_xxx" }
```

更新 `residents.courier_manager_id`，同时设 `role=courier`。

## 62.7 DELETE `/courier-managers/{id}/couriers/{courierId}` — 移除快递员

清空 `residents.courier_manager_id`。

## 62.8 GET `/courier-managers/{id}/couriers` — 获取快递员列表

## 62.9 GET `/courier-managers/my` — 当前快递负责人信息

**Roles**: `courier`

## 62.10 GET `/admin/delivery-price-ranges` — 配送价格区间列表

**Roles**: `property_admin`, `platform_admin`

**Query 参数**：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `propertyCompanyId` | `String` | ❌ | 物业公司ID（`platform_admin` 可指定；物业管理员自动取本物业） |
| `status` | `String` | ❌ | `active`/`inactive` |
| `distanceType` | `String` | ❌ | `1km`/`3km`/`5km`/`citywide`/`any` |
| `deliveryScope` | `String` | ❌ | `in_community`/`out_community`/`both` |

> 返回**数组**（非分页），按 `minPrice` 升序。无物业公司上下文时返回空数组。

**成功响应**：`List<DeliveryPriceRangeResponse>`：

```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "id": "dpr_demo001",
      "propertyCompanyId": "pc_demo001",
      "minPrice": 0.00,
      "maxPrice": 100.00,
      "distanceType": "any",
      "distanceTypeLabel": "不限距离",
      "deliveryScope": "both",
      "deliveryScopeLabel": "小区内外均配送",
      "statusCode": "active",
      "statusLabel": "启用",
      "status": "启用",
      "createdAt": "2026-07-01 15:23:33",
      "updatedAt": "2026-07-01 15:23:33"
    }
  ],
  "timestamp": "2026-07-01 15:23:33"
}
```

> 注意：`statusCode` 为英文码（`active`/`inactive`），`status` 为中文展示值。

---

## 62.11 POST `/admin/delivery-price-ranges` — 创建价格区间

**Roles**: `property_admin`, `platform_admin`

**请求体**（`CreateDeliveryPriceRangeRequest`）：

| 字段 | 类型 | 必填 | 校验 / 默认 | 说明 |
| --- | --- | --- | --- | --- |
| `minPrice` | `BigDecimal` | ✅ | `@NotNull`，≥0 | 最低价格 |
| `maxPrice` | `BigDecimal` | ✅ | `@NotNull`，≥0 | 最高价格 |
| `distanceType` | `String` | ❌ | 默认 `any` | `1km`/`3km`/`5km`/`citywide`/`any` |
| `deliveryScope` | `String` | ❌ | 默认 `both` | `in_community`/`out_community`/`both` |
| `propertyCompanyId` | `String` | 条件 | | `platform_admin` 可指定；物业管理员自动取本物业；无法确定时返回 `10001` |

**请求示例**：

```json
{ "minPrice": 0.10, "maxPrice": 0.50, "distanceType": "any", "deliveryScope": "both" }
```

**业务规则**：`minPrice ≤ maxPrice`；与同物业、同 `distanceType`、同 `deliveryScope` 的现有 **active** 区间做闭区间重叠检测，重叠返回 `10002`。

**成功响应**：`DeliveryPriceRangeResponse`（单条，字段同 §62.10）。

---

## 62.12 PUT `/admin/delivery-price-ranges/{id}` — 更新价格区间

**Roles**: `property_admin`, `platform_admin`

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | `String` | ✅ | 区间ID（`dpr_`） |

**请求体**（`UpdateDeliveryPriceRangeRequest`，全部可选）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `minPrice` | `BigDecimal` | 最低价格，≥0 |
| `maxPrice` | `BigDecimal` | 最高价格，≥0 |
| `distanceType` | `String` | 距离类型 |
| `deliveryScope` | `String` | 配送范围 |
| `status` | `String` | `active`/`inactive` |

> 修改价格/维度时会重新做 `min ≤ max` 与重叠检测（排除自身）；仅改 `status` 不触发重叠检测。

**成功响应**：`DeliveryPriceRangeResponse`（单条，字段同 §62.10）。

---

## 62.13 DELETE `/admin/delivery-price-ranges/{id}` — 删除价格区间

**Roles**: `property_admin`, `platform_admin`

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | `String` | ✅ | 区间ID（`dpr_`） |

**说明**：逻辑删除，将 `status` 置为 `inactive`（不物理删除）。

**成功响应**：`DeliveryPriceRangeResponse`（单条，`statusCode=inactive`）。

---

# 63. 三期 — 商家端口改造 (merchant-port)

定向广告投放；管理端分销商品列表 / 上架 / 收费周期设置。

> **对接注意**：管理端列表用 `GET /admin/distributor-products`；业主选品池 `GET /distributors/products` 仅 `resident`，勿混用。

## 63.1 POST `/merchant/ads/targeted` — 创建定向广告

**Auth**: Required
**Roles**: `merchant`

按楼栋 / 性别 / 年龄段定向投放，计费与支付口径同 §59.2（按天计价，`free`/`coin`/`point` 同步扣减立即投放，`wechat` 预下单待回调投放）。

```json
// Request Body
{
  "merchantId": "mch_xxx",
  "title": "定向推广",
  "content": "...",
  "imageUrls": ["url1"],
  "productId": "prd_xxx",
  "targetBuildings": ["1栋","2栋"],
  "targetGender": "male",
  "targetAgeBrackets": ["ab_001"],
  "durationDays": 7,
  "paymentMethod": "wechat",
  "openid": "oGX3Z5Xx..."
}
```

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `merchantId` | String | 是 | 须等于当前登录商家 ID |
| `title` / `content` | String | 是 | 标题 / 内容 |
| `imageUrls` | String[] | 否 | 图片 URL 列表 |
| `productId` | String | 否 | 关联商品 ID |
| `targetBuildings` | String[] | 否 | 目标楼栋列表（与 `residents.building` 对齐） |
| `targetGender` | String | 否 | `male` / `female` / `all` |
| `targetAgeBrackets` | String[] | 否 | 目标年龄段配置 ID 列表（`ab_`） |
| `durationDays` | int | 是 | 展示天数，1~30 |
| `paymentMethod` | String | 是 | `free` / `coin` / `point` / `wechat` / `mock` |
| `openid` | String | 否 | `paymentMethod=wechat` 时使用 |

**响应**：同 §59.2 `MerchantAdListItemResponse`（含 `status` / `paymentRecordId` / `payParams`）。

**错误**：同 §59.2，另有 `20004`（为其他商家创建）。

## 63.2 GET `/admin/distributor-products` — 分销商品列表

**Roles**: `property_admin`, `platform_admin`

完整说明见 [§60.12 GET `/admin/distributor-products`](#get-admindistributor-products--分销商品列表)。

**Query**：`page`、`pageSize`、`keyword`、`propertyCompanyId`（平台管理员建议必传；物业管理员可不传）

**响应**：标准分页 `data.list` + `data.pagination`；列表项含 `id`（`dpr_`）、`name`、`wholesalePrice`、`suggestedRetailPrice`、`billingCycle`、`status`、`createdAt` 等。

## 63.3 PUT `/admin/distributor-products/{id}/billing-cycle` — 设置收费周期

**Roles**: `property_admin`, `platform_admin`, `merchant`

```json
// Request Body
{ "billingCycle": "monthly" }
```

`billingCycle` 可选值：`one_time` / `monthly` / `quarterly` / `yearly`。

路径 `{id}` 须为 `dpr_` 开头的分销商品 ID。

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "dpr_xxx",
    "billingCycle": "monthly"
  }
}
```

---

# 64. 三期 — 统筹管理 (coordinator-workbench)

统筹增加模块负责人/个体负责人/商户；商户冻结/解冻。

## 64.0 GET `/coordinators/my` — 当前统筹信息

**Auth**: Required  
**Roles**: `resident`（须持有启用中的统筹记录；v8.13 负责人标签化后不再要求 `role=coordinator`）

**响应**（`CoordinatorMyResponse`）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `String` | 统筹 ID |
| `residentName` | `String` | 业主姓名 |
| `phone` | `String` | 手机号 |
| `propertyCompanyId` | `String` | 物业公司 ID |
| `propertyCompanyName` | `String` | 物业公司名称 |
| `commissionRate` | `BigDecimal` | 分成比例 |
| `sectorLeaderCount` | `Long` | 下属板块负责人数量 |
| `individualLeaderCount` | `Long` | 下属个体负责人数量 |
| `merchantCount` | `Long` | 管辖商家数量 |
| `activeSpecialOfferCount` | `Long` | 进行中特惠数量 |
| `totalEarnings` | `BigDecimal` | 累计收益 |
| `withdrawableAmount` | `BigDecimal` |  可提现金额（累计分成-已占用提现，占用含 pending/approved/completed） |
| `status` | `String` | 状态 |

## 64.1 POST `/coordinators/{id}/sector-leaders` — 增加模块负责人

**Roles**: `property_admin`，或持有启用中统筹记录的住户（v8.13 标签化）

```json
// Request Body
{
  "residentId": "res_xxx",
  "sector": "cleaning",
  "commissionRate": 0.10
}
```

## 64.2 POST `/coordinators/{id}/individual-leaders` — 增加个体负责人

```json
// Request Body
{
  "residentId": "res_xxx",
  "sector": "cleaning",
  "name": "王强",
  "sectorLeaderId": "sl_xxx",
  "commissionRate": 0.05
}
```

## 64.3 POST `/coordinators/{id}/merchants` — 增加商户

```json
// Request Body
{
  "platformMerchantId": "pm_xxx",
  "name": "测试商家",
  "category": "外卖",
  "contactPhone": "13800000000"
}
```

## 64.4 PUT `/coordinators/{id}/merchants/{merchantId}/freeze` — 商户冻结/解冻

```json
// Request Body
{ "frozen": true }
```

更新 merchant 的 status 字段（`active` / `frozen`）。

## 64.5 PUT `/coordinators/{id}/merchant-audit` — 统筹审核商家加入

**Roles**: `resident`（**仅统筹**：须持有启用中统筹记录，物业管理员不可代审；v8.13 标签化）

审核板块负责人经 §61.3 上报的商家加入申请。与 §61.4 共用同一套请求/响应结构与后端服务，区别仅在于身份维度：

| | 路径 | 角色 | 语义 |
| --- | --- | --- | --- |
| §61.4 | `POST /sector-leaders/{id}/merchant-audits` | `sector_leader` | 板块负责人审核（路径/方法已于 2026-09-03 修正） |
| §64.5 | `/coordinators/{id}/merchant-audit` | `coordinator` | 统筹审核板块上报的申请 |

> ⚠️ 两个路径均真实存在，请勿互相替代：`{id}` 分别为板块负责人 ID 和统筹 ID。

```json
// Request Body
{
  "merchantId": "mch_xxx",
  "approved": true,
  "reason": "审核通过"
}
```

**响应**：`MerchantUploadResponse`，与 §61.4 一致。

## 64.6 POST `/coordinators/{id}/withdrawals` — 统筹申请提现

**Roles**: `resident`（仅本人，且须持有启用中的统筹记录；v8.13 标签化）

**请求体**（`ApplyRoleWithdrawalRequest`）：

| 字段 | 类型 | 必填 | 校验 | 说明 |
| --- | --- | --- | --- | --- |
| `amount` | `BigDecimal` | ✅ | `@NotNull @DecimalMin("0.01")` | 提现金额，不超过可提现余额（累计分成 - 已占用提现） |

**成功响应**（`RoleWithdrawalItemResponse`）：`id`/`withdrawalType`(`coordinator`)/`applicantId`/`residentId`/`propertyCompanyId`/`amount`/`feeRate`/`feeAmount`/`actualAmount`/`status`(`pending`)/`auditedAt`/`completedAt`/`createdAt`

## 64.7 GET `/coordinators/{id}/withdrawals` — 统筹提现记录

**Roles**: `resident`（仅本人，且须持有启用中的统筹记录；v8.13 标签化）

**Query 参数**：`page`/`pageSize`/`auditStatus`(`pending`/`approved`/`rejected`/`completed`)/`startDate`/`endDate`

> 手续费率与平台手续费分成复用物业公司 `withdrawalFeeRate`/`platformWithdrawalFeeShareRate`，与个体负责人/快递员口径一致。

---

# 65. 三期 — 价格审批 (price-approvals)

提交价格修改审批、审批通过/拒绝、审批列表。

## 65.1 POST `/admin/price-approvals` — 提交价格修改审批

**Roles**: `platform_admin`（提交时必须带 `propertyCompanyId`）、`property_admin`（含 `propertySubRole=property_operator`）

> 当前代码要求角色为 `platform_admin` 或物业领导（`property_admin` + `propertySubRole=property_leader`）；`property_operator` 返回 `20004`。请以实际账号角色为准。

**请求体**（`CreatePriceApprovalRequest`）：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `propertyCompanyId` | `String` | 条件 | 物业公司ID（`pc_`）；**仅 `platform_admin` 提交时必填**，物业侧取登录上下文 |
| `itemType` | `String` | ✅ | `delivery_fee` / `product_price` / `merchant_ad` / `merchant_distribution` / `property_fee_price` / `resident_share_rate` |
| `itemId` | `String` | ❌ | 目标对象ID |
| `oldValue` | `String` | ❌ | 原值 |
| `newValue` | `String` | ✅ | 新值，不能为空 |
| `reason` | `String` | ❌ | 申请原因 |

```json
// Request Body
{
  "itemType": "delivery_fee",
  "itemId": "drl_xxx",
  "oldValue": "{\"courier_per_order\":0.35}",
  "newValue": "{\"courier_per_order\":0.40}",
  "reason": "配送费调整"
}
```

> **说明**：`resident_share_rate`（业主/商家/物业币/共享积分分成比例）审批通过后自动落库 `property_companies`，见 §65.2。

---

## 65.2 PUT `/admin/price-approvals/{id}` — 审批价格修改

**Roles**: 仅 `propertySubRole=property_leader`（物业领导）或 `platform_admin`；操作员（`propertySubRole=property_operator`）返回 `20004` 权限不足（403）

> **兼容口径**：老数据 `propertySubRole` 为空视为物业领导（`isCurrentPropertyLeader` 兼容逻辑）；严格区分「操作员 403」请确保账号 `property_sub_role` 已正确落库。

```json
// Request Body
{ "approved": true, "remark": "同意调整" }
```

**审批通过后落库（`itemType=resident_share_rate` 时）**：

- `itemId` 为 `property_companies.id`（`pc_` 开头）。
- 解析 `newValue` JSON 写入四项分成：`residentPointShareRate` / `merchantPointShareRate` / `coinPointShareRate` / `sharedPointShareRate`。
- 兼容手工简写：`{"rate":0.1}` 或 `{"residentPointShareRate":0.1}` → 仅更新业主分成；`rate` 与 `residentPointShareRate` 不可同时出现。
- 校验：各字段须为数值且 ≥ 0；合并后（未修改字段沿用现值）四项合计必须恰好等于 `1.0`（即 100%，容差 0.0001）。任一字段 < 0 或 > 1、或合计 ≠ 100% 返回 `97002 INVALID_SHARE_RATE_TOTAL`（400），**整单不生效**——审批保持 `pending`，物业公司分成不变。
- `itemId` 对应物业公司不存在返回 `97001`（404）。

**错误响应**：

| 错误码 | HTTP | 说明 |
| --- | --- | --- |
| `20004` | 403 | 仅物业领导或平台管理员可审批 |
| `97002` | 400 | newValue 非法 / 字段冲突 / 负数 / 合计必须为100% |
| `60003` | 409 | 审核已处理（非 pending） |
| `97001` | 404 | 物业公司不存在 |

## 65.3 GET `/admin/price-approvals` — 审批列表

**Roles**: 仅 `propertySubRole=property_leader`（物业领导）或 `platform_admin`；操作员返回 `20004`（403）

> **兼容口径**：老数据 `propertySubRole` 为空视为物业领导（`isCurrentPropertyLeader` 兼容逻辑）。

**Query**: `page`, `pageSize`, `status`（`pending` / `approved` / `rejected`）

**数据范围**：`platform_admin` 可传 `propertyCompanyId` 跨物业查看；物业领导自动限定本物业。

---

# 66. 三期 — 通知/发布增强 (announcement-enhance)

产品推荐发布、活动内容发布；定向消息增强。

## 66.1 POST `/announcements/product` — 产品推荐发布

> ⚠️ **【当前未实现 · 2026-10-27 核对代码】** 后端不存在 `POST /announcements/product`。产品/活动推荐请使用 `POST /announcements/activity`（活动公告）或特惠推送 §25；本接口请勿调用。以下为原设计文档，保留备查。

**Roles**: `property_admin`, `platform_admin`, `merchant`

```json
// Request Body
{
  "title": "农夫山泉特惠",
  "content": "整箱购买优惠",
  "communityId": "com_xxx",
  "merchantId": "mch_xxx",
  "productId": "prd_xxx",
  "coverUrl": "/static/xxx.png",
  "publishedAt": "2026-07-22T10:00:00"
}
```

创建 Announcement，`announcement_type=merchant`，`content_type=product`。

## 66.2 POST `/announcements/activity` — 活动内容发布

**Auth**: Required  
**Roles**: `activity_leader` / `property_admin` / `platform_admin`

> **v7.9 修复（400 根因）**：该端点此前复用 `POST /announcements` 的 `CreateAnnouncementRequest`（`announcementType` 为 `@NotBlank` 必填）。但本端点要求前端**不传** `announcementType`（后端强制为 `activity`），导致 `@Valid` 在进入方法前就因缺字段而返回 400「参数校验失败」。
> **修复**：本端点改用独立轻量 DTO `ActivityAnnouncementRequest`（`title`/`content` 必填，其余选填），**不再要求、也不再校验 `announcementType`**，后端强制写入 `activity`。请求体中的 `status`/`propertyCompanyId` 等未知字段会被忽略（Jackson 关闭 `FAIL_ON_UNKNOWN_PROPERTIES`）。

**请求体**（JSON）：

| 字段               | 类型     | 必填 | 说明                                                  |
| ---------------- | ------ | -- | --------------------------------------------------- |
| `title`          | String | ✅  | 公告标题（`@NotBlank`，≤200）                                |
| `content`        | String | ✅  | 公告内容（`@NotBlank`）                                    |
| `communityId`    | String | ❌  | 小区ID（`com_` 前缀）                                      |
| `activityGroupId` | String | ❌  | 关联活动组ID（`act_`/`ag_` 前缀），写入 `announcements.activity_group_id` |
| `publishedAt`    | String | ❌  | 发布时间；`yyyy-MM-dd HH:mm:ss` 或 ISO 本地时间，不传则立即发布           |

```json
// Request Body
{
  "title": "邻里跳蚤市场",
  "content": "本周六社区中心广场",
  "communityId": "com_xxx",
  "activityGroupId": "act_xxx",
  "publishedAt": "2026-07-22 10:00:00"
}
```

创建 Announcement，`announcement_type=activity`，`content_type=activity`；`activityGroupId` 持久化到 `announcements.activity_group_id`（需先执行 `sql/20260906_announcements_activity_group_id.sql` 增加该列）。

## 66.3 定向消息增强

`POST /admin/directed-messages`（§52.1）新增 `merchantIds` 字段，按商家消费筛选接收人：

```json
{
  "title": "...",
  "content": "...",
  "merchantIds": ["mch_demo001", "mch_mch01"]
}
```

`DirectedMessageTask` 实体新增 `merchant_ids` (json) 字段，`DirectedMessageService` 注入 `OrderRepository`，在 `filterResidents` 中按消费过的商家筛选住户。

---

# 68. 商家履约方式选择与自配分账

> **需求**：《商家履约方式选择与自配分账》
> **核心**：订单支付成功后进入「履约方式选择」阶段，商家可选择 **商家自配（merchant_self）** 或 **发布到抢单大厅（courier_hall / 平台配送）**。
> - **平台配送（原逻辑保留）**：平台抽成（默认 10%），余下配送费归配送员；**商家不分配送费分成**。
> - **商家自配（新增）**：平台抽成（默认 10%），余下配送费归**商家**作为 `merchant_delivery_fee_share`；配送员不分配送费。
> - 分账比例默认 90/10，管理端可调（不阻塞本功能）。
> - 所有履约方式选择/超时兜底/管理员改派 **全程留痕**（写入 `fulfillment_choice_logs`）。

## 68.1 状态与流程

> **支付后分流**：按商家类型 `merchants.merchant_type` 与订单商品分类综合判定，与平台全局开关解耦。
> - **社区食堂商家单**（`merchant_type = canteen`，主店与窗口均算）：`requires_delivery=false`、`fulfillment_mode=none`、`delivery_fee=0`，支付成功后 → `completed`（购买即结束，不配送、不进履约选择、不进商家核实、不生成配送单）；
> - **团购单**（所有商品 `category=团购`）：`requires_delivery=false`、`fulfillment_mode=none`，支付成功后 → `pending_verification`（商家核实，见 §8.6.1a），**不进**本节履约方式选择流程；
> - **非团购非食堂单**：`requires_delivery=true`，支付成功后 → `pending_choice`，进入下述履约方式选择流程。

1. 非团购单支付成功 → `order_status = paid`，`fulfillment_mode = pending_choice`，`fulfillment_deadline = 支付时间 + 30 分钟`。
   **此时不生成配送单，订单/配送单均不会出现在待抢大厅**（2026-10-06 修复：此前支付后即生成 `pending` 配送单，导致骑手在商家选择前即可抢单）。
2. 商家在时限内选择（**选择时才创建配送单**）：
   - `merchant_self`：创建商家自配配送单（`carrier_type = merchant`，`status = merchant_self`），订单进入 `delivering`，骑手不可见/不可抢。
   - `courier_hall`：创建平台配送单（`carrier_type = courier`，`status = pending`），订单进入 `delivering`，等待骑手抢单。此时配送单才出现在 `GET /deliveries/pending` 待抢单列表中。
   - （`POST /orders/{id}/send-delivery` 等价于选择 `courier_hall`，兼容历史调用方。）
3. 超时未选 → 定时任务 `FulfillmentChoiceTimeoutJob` 默认改派 `courier_hall`（平台配送），并记录 `action = timeout`。
4. 配送完成记账：
   - 平台配送：骑手 `completeDelivery` → 配送员收入入账，平台抽成入账（原逻辑）。
   - 商家自配：商家 `merchant-confirm` → 平台抽成入账 + 商家 `merchant_delivery_fee_share` 入账。
5. 管理员可在任意未结束订单上 `override-fulfillment` 强制改派（作废原配送单并重新生成，记录 `action = override`）。

> **商家自配单守卫**：`carrier_type = merchant` 的配送单不可被骑手抢单 / 改状态 / 改派 / `completeDelivery`；仅本单商家可通过 `merchant-confirm` 操作。

## 68.2 POST `/orders/{id}/choose-fulfillment` — 商家选择履约方式

**Roles**: `merchant`（仅本商家名下、已支付且 `fulfillment_mode = pending_choice` 的订单）

> **等价入口**：`POST /merchants/my/orders/{orderId}/fulfillment`（商家端现有路径，同一套实现）。
> 两者请求体与响应完全一致，前端任选其一即可。
>
> **2026-09-09 修复（v8.4，前端问题：商家选择履约方式/发布平台配送 409）**
> 此前 `orders.fulfillment_mode` **从未在下单/支付时落库**（恒为 `NULL`），
> 而本接口强校验 `fulfillment_mode = pending_choice`，导致所有订单一律 `409 70002`。
> 现已补齐落库：非团购非食堂订单支付成功后写入 `pending_choice` + `fulfillment_deadline = paid_at + 30min`。
> `fulfillment_mode` 为空的**历史订单**按「需配送 → `pending_choice`」兼容处理，不会因历史数据再次 409。

```json
// Request Body
{
  "mode": "merchant_self"   // merchant_self（商家自配） | courier_hall（平台配送/发布抢单大厅）
}
```

**响应补充**：`alreadyExists=true` 表示复用了已有配送单（幂等）；`false` 表示本次新建。
重复提交**相同**履约方式视为幂等成功（返回当前配送单）；已选**其它**方式返回 `400 70025`。

**Response**（`SendDeliveryResponse`）：

```json
{
  "code": 0,
  "errorCode": "SUCCESS",
  "message": "success",
  "data": {
    "orderId": "ord_xxx",
    "deliveryId": "dlv_xxx",
    "statusCode": "merchant_self",
    "statusLabel": "商家自配中",
    "status": "商家自配中",
    "alreadyExists": false,
    "sentAt": "2026-08-18T10:05:00.000Z"
  }
}
```

## 68.3 POST `/deliveries/{id}/merchant-confirm` — 商家确认自配送达

**Roles**: `merchant`（仅本单商家，且仅 `carrier_type = merchant` 的配送单）

> **2026-09-09 修复（v8.4）**：该接口此前**后端未实现**，商家自配单没有任何完成路径
> （骑手侧 `complete` 又拒绝非 courier 承运单）。现已实现：配送单置 `completed`、
> 订单同步置 `completed`（含 `deliveredAt` / `completedAt`）。
> 平台配送单仍走 `POST /deliveries/{id}/complete`（骑手）。
>
> **2026-10-06 修复（v8.10）**：订单置 `completed` 后现同步执行结算
> （`OrderCompletionService.settleOnCompleted`：生成 `distribution_records`（含楼长分成）与业主卖货明细）。
> 修复此前商家自配单完成却无任何分成记录、楼长钱包不增加的问题。
>
> **2026-10-16 口径说明（v8.12）**：商家自配单**不经居民确认收货**——商家确认送达即订单完成（资金终态），
> 这是与平台配送单（配送员完成配送 → 居民确认收货才完成）的唯一差异，属既有设计。
> 前端无需在调用本接口后再同步订单状态（重复推进会因状态机校验返回 `70002`，无副作用但无必要）。

```json
// Request Body（可空）
{
  "proofImageUrls": ["/static/proof1.png"],
  "remark": "已送达，放门口"
}
```

**Response**：`CompleteDeliveryResponse`，含新增字段 `merchantDeliveryFeeShare`（商家自配配送费分成）、
`carrierType`。

## 68.4 POST `/admin/orders/{id}/override-fulfillment` — 管理员强制改履约方式

**Roles**: `property_admin`, `platform_admin`

> **2026-09-09 修复（v8.4）**：新增该路径（此前仅有 `/admin/orders/refunds/{orderId}/fulfillment-override`，
> 两者并存，实现相同）。改派时会**同步重建/改写配送单**并留痕；
> 已结束（`completed`/`cancelled`/`refunded`）订单返回 `409 70002`；
> 团购/食堂单返回 `400 70022`。

```json
// Request Body
{
  "mode": "courier_hall",     // merchant_self | courier_hall
  "remark": "商家无法自配，改派平台配送"
}
```

管理员强制改派，全程留痕（`action = override`）。已结束（completed/cancelled/refunded）订单不可改派。

## 68.5 新增响应字段

- **订单列表/详情**（`OrderListItemResponse` / `OrderDetailResponse`）：
  `requiresDelivery`(bool)、`fulfillmentMode`(string)、`fulfillmentModeLabel`(string)、`fulfillmentDeadline`(string)、`fulfillmentChosenAt`(string)。
  - `fulfillmentMode` 取值：`pending_choice`（待选择）/ `merchant_self`（商家自配）/ `courier_hall`（平台配送）/ `none`（团购单，无需配送）/ `null`（历史订单未迁移）；
  - 团购单：`requiresDelivery=false`、`fulfillmentMode=none`、`fulfillmentDeadline=null`、`fulfillmentChosenAt=null`，前端据此显示"无需配送"且不出现"发布到抢单大厅"按钮。
- **🆕 v8.12 订单列表/详情配送状态字段**（§8.3/§8.4/§8.5，业务状态与物流状态分离）：
  `deliveryStatusCode`(string：配送单状态 code，历史值 `delivered` 归一为 `completed`)、`deliveryStatusLabel`(string：配送单状态中文，后端下发)；
  订单详情另含 `deliveryDeliveredAt`(string：配送单送达时间)。无配送单时均为 `null`。
- **配送列表/详情**（`PendingDeliveryItemResponse` / `MyDeliveryItemResponse` / `CompleteDeliveryResponse`）：
  `carrierType`(string：courier/merchant)、`merchantDeliveryFeeShare`(decimal：仅商家自配单)。
  - 🆕 v8.12：`MyDeliveryItemResponse` 新增 `statusLabel`/`orderStatus`/`orderStatusLabel`；`PendingDeliveryItemResponse` 新增 `statusLabel`；
    `CompleteDeliveryResponse` 新增 `orderStatus`/`orderStatusLabel`（配送完成 ≠ 订单完成，见 §9.5）。

## 68.6 新增错误码

| 错误码 | HTTP | 说明 |
| --- | --- | --- |
| `70021 FULFILLMENT_DEADLINE_PASSED` | 400 | 已超过履约选择时限，无法再选择 |
| `70022 FULFILLMENT_NOT_REQUIRED` | 400 | 本单无需配送，不可选择履约方式（团购单 `fulfillment_mode=none` 触发） |
| `70023 FULFILLMENT_NOT_CHOSEN` | 400 | 请先选择履约方式 |
| `70024 FULFILLMENT_DELIVERY_INCOMPLETE` | 400 | 配送未完成，不可完成订单 |
| `70025 FULFILLMENT_LOCKED` | 400 | 履约方式已锁定，不可变更 |
| `80020 DELIVERY_NOT_COURIER_ORDER` | 400 | 非配送员承运单，配送员不可抢单/改状态/完成/被指派（用于商家自配单） |
| `80021 DELIVERY_NOT_FOUND` | 404 | 配送单不存在 |

> **2026-09-09 说明（v8.4）**：原文档写的 `80010 DELIVERY_NOT_COURIER_ORDER` 与既有的
> `80010 DELIVERY_PRICE_RANGE_NOT_FOUND`（配送价格区间不存在）**冲突**，实现改用 `80020`。
> `70023` / `70024` 为预留，当前流程未触发。

## 68.7 数据库变更

独立迁移脚本见 `sql/fulfillment_mode_migration.sql`（结构），
2026-09-09 的历史数据回填脚本见 `sql/2026-09-09_履约链路与板块特惠权限_数据库脚本.sql`（幂等版）
与 Flyway `V14__fulfillment_and_sector_offer_20260909.sql`：

- `orders` 新增 `requires_delivery`、`fulfillment_mode`、`fulfillment_deadline`、`fulfillment_chosen_at`；
- `deliveries` 新增 `carrier_type`、`merchant_delivery_fee_share`、`proof_image_urls`、`complete_remark`；
- `merchants` 新增 `default_fulfillment_mode`；
- 新建 `fulfillment_choice_logs`（履约选择审计表）；
- 历史数据回填（2026-09-09）：
  - `deliveries.carrier_type` 为空 → `courier`；
  - 团购单 / 社区食堂单 → `requires_delivery=0`、`fulfillment_mode=none`；
  - 已有配送单的订单 → `courier_hall`；
  - 近 7 天内已支付 / 配送中且无配送单 → `pending_choice` + `paid_at + 30min` 时限；
  - 其余 `requires_delivery` 为空 → `1`。
- **🆕 v8.12 数据规范化（2026-10-16）**：`sql/manual_upgrade_delivery_order_status_20261016.sql`（幂等）——
  历史遗留 `deliveries.status='delivered'` 归一为 `completed`（正式完成态），并同步 `status` 列注释；
  无表结构变更，`orders` 表状态均为合法值无需处理。

## 68.8 2026-09-09 修复清单（v8.4）

| # | 问题 | 根因 | 修复 |
|---|------|------|------|
| 1 | `POST /merchants/my/orders/{id}/fulfillment` 恒 409 | `orders.fulfillment_mode` 下单/支付时从未落库，恒 `NULL` | 下单落库 `requires_delivery`；支付后非团购非食堂单置 `pending_choice` + 30min 时限；历史 `NULL` 兼容为 `pending_choice` |
| 2 | `POST /orders/{id}/send-delivery` 404 | 接口未实现 | 新增，等价 `courier_hall`，幂等 |
| 3 | `POST /orders/{id}/choose-fulfillment` 404 | 接口未实现 | 新增，与 `/merchants/my/orders/{id}/fulfillment` 同一实现 |
| 4 | `POST /deliveries/{id}/merchant-confirm` 404 | 接口未实现，商家自配单无完成路径 | 新增 |
| 5 | 商家选 `courier_hall` 后待抢大厅不显示 | `GET /deliveries/pending` 仅匹配 `order_status=paid`（v6.6 修复未落地） | 查询扩展为 `paid` 或 (`delivering` 且 `fulfillment_mode=courier_hall`)，并排除 `carrier_type=merchant` |
| 6 | 骑手可抢/操作商家自配单 | 无 `carrier_type` 守卫 | 抢单/改状态/完成/指派均校验 `carrier_type != merchant`，否则 `400 80020` |
| 7 | 订单详情 `deliveryId` 恒 `null` | `OrderDetailResponse.deliveryId` 从未赋值 | 按 `deliveries.order_id` 回填，并新增 `carrierType` |
| 8 | 订单列表/详情缺少履约字段 | 未输出 | 新增 `requiresDelivery` / `fulfillmentMode` / `fulfillmentModeLabel` / `fulfillmentDeadline` / `fulfillmentChosenAt` |
| 9 | 板块负责人发/改/删特惠 403 | `sector_leader` 走 `requireAdmin()`，只允许 `property_admin`/`platform_admin` | 按角色放行（不要求 `promotion.push`），数据范围收敛本物业；详情/发布/下架 Roles 同步补齐 |

## 68.9 2026-10-06 修复（履约方式选择落空）

| # | 问题 | 根因 | 修复 |
|---|------|------|------|
| 1 | 「商家选择自配 / 发布抢单大厅」形同虚设：支付后配送单立即进入待抢大厅，骑手可先于商家选择抢单 | 支付成功即调用 `createDeliveryForPaidOrder` 生成 `carrier_type=courier,status=pending` 配送单；且待抢查询匹配 `order_status=paid` | 支付后**不生成配送单**，仅在商家选择 `merchant_self`/`courier_hall` 或超时兜底时创建；待抢查询**移除** `order_status=paid` 分支，仅保留 `delivering AND fulfillment_mode=courier_hall` |

> **影响面**：`POST /merchants/my/orders/{id}/fulfillment`、`POST /orders/{id}/choose-fulfillment`、`POST /orders/{id}/send-delivery`、`FulfillmentChoiceTimeoutJob`、`POST /admin/orders/{id}/override-fulfillment` 行为一致；
> 无数据库结构变更。存量「已支付且未选择」订单若已有 `pending` 配送单，修复后不再出现在待抢大厅，待商家选择后按新规则处理。

## 68.10 2026-10-10 修复（住户地址字段 `floor` 丢失）

| # | 问题 | 根因 | 修复 |
|---|------|------|------|
| 1 | 住户楼层 `floor` 无法保存/读取：楼栋变更审批通过后楼层丢失；管理端新增/修改住户、`GET /auth/profile`、`GET /residents/me`、住户详情/列表均无 `floor` | `residents.floor` 列早已存在且有数据，但 `Resident` 实体未映射该字段；各地址写入路径只写 `building/unit/room`，楼层被静默丢弃 | `Resident` 新增 `floor` 映射；`CreateResidentRequest`/`UpdateResidentRequest` 新增 `floor`；楼栋变更审批（§2.6.2.2）、管理端新增/修改住户写入 `floor`；`/auth/profile`、`/residents/me`、住户详情、住户列表响应新增 `floor` |

> **影响面**：`POST /admin/residents`、`PUT /admin/residents/{id}`、`PUT /residents/me`、`POST /residents/me/building-change` 审批、`GET /auth/profile`、`GET /residents/me`、住户列表/详情。
> **无数据库结构变更**（列早已存在），存量数据无需迁移。

## 68.11 2026-10-16 修复（配送完成与订单完成 · 状态口径与结算展示分离，v8.12）

| # | 问题 | 根因 | 修复 |
|---|------|------|------|
| 1 | 「配送员处显示已完成，商家/居民处仍显示配送中」：同一张商家订单卡片同时出现「配送中」pill 与「已完成」配送提示 | 配送单状态与订单状态是两套独立状态机；订单响应从未下发配送状态字段，前端只能读 `orderStatus`（配送完成后仍为 `delivering`） | 订单列表/详情（§8.3/§8.4/§8.5）新增 `deliveryStatusCode` + `deliveryStatusLabel`（物流状态）；配送列表新增 `statusLabel` + `orderStatus`/`orderStatusLabel`；三端按「业务状态 / 物流状态 / 资金状态」分层展示 |
| 2 | 前端拟用 `courier` 角色调 `PATCH /orders/{id}/status {completed}` 修补显示，会使资金在配送完成时即冻结、居民丧失确认收货与退货窗口 | `completed` 是资金终态（分账冻结 + 不支持线上退货），完成权应在居民/商家/超期兜底，而非配送员 | `applyStatusTransition` 收紧：courier 仅允许 `delivering`，置 `completed` 返回 `70002` 并提示正确路径；`POST /deliveries/{id}/complete` 不级联完成订单（文档 §9.5 明确） |
| 3 | 文档 v8.11 起就写明订单返回 `deliveryStatusCode`/`deliveryStatusLabel`，后端从未实现 | 文档先行、实现缺失 | v8.12 真正落地（见 #1），并订正示例中不存在的 `deliveryStatus`/`orderStatusCode` 等字段 |
| 4 | 配送完成态存在 `completed`（现行）/ `delivered`（旧部署）双写，前端需兼容两套 | 旧部署用 `delivered` + 配送完成即分账；现行版本改为 `completed` + 订单完成时分账 | 统一以 `completed` 为正式完成态；`/deliveries/my` 的 `status=delivered` 过滤自动归一；SQL 脚本归一存量数据（§68.7） |

> **资金时点（不变）**：分成记录生成 + 微信分账冻结仍发生在**订单完成**时点；售后窗口到期完结划账（解冻）。
> 配送完成不解冻——窗口内用户申请退货时配送费尚未划走，避免平台垫付。
> **前端配合项**：移除配送员完成配送后向 `orders/{id}/status` 补同步 `completed` 的逻辑（会收到 `70002`，不阻断流程但无意义）；
> 商家自配送达也无需再补同步（后端已级联）。详细交付说明见随附 HTML《配送完成与订单完成-状态口径与结算展示分离-后端答复》。

---

# 67. 三期 — 询价 (inquiries)

业主发起询价、商家报价、业主接受/拒绝。

## 67.1 POST `/inquiries` — 发起询价

**Roles**: `resident`

```json
// Request Body
{
  "merchantId": "mch_demo001",
  "productId": "prd_001",
  "productName": "农夫山泉550ml",
  "content": "整箱购买有优惠吗？"
}
```

创建 Inquiry 记录，`status=pending`。

## 67.2 GET `/inquiries` — 我的询价列表（业主视角）

**Roles**: `resident`

返回当前用户的询价列表，按创建时间倒序。

## 67.3 GET `/inquiries/merchant` — 商家询价列表

**Roles**: `merchant`

返回当前商家的询价列表，按创建时间倒序。

## 67.4 PUT `/inquiries/{id}` — 询价回复/确认

**Roles**: `merchant`（报价）或 `resident`（接受/拒绝）

```json
// 报价
{ "action": "quote", "quotedPrice": 25.00, "quotedRemark": "整箱优惠" }

// 接受
{ "action": "accept" }

// 拒绝
{ "action": "reject" }
```

**状态机**：仅 `pending` / `quoted` 状态可操作；`quote` 将 `pending→quoted`；`accept` 将 `quoted→accepted`；`reject` 将 `quoted→rejected`。

---

# 68. 三期 — 活动组增强 (activity-group-enhance)

组员个人简介更新。

## 68.1 PUT `/activity-groups/{id}/members/{memberId}/profile` — 更新组员简介

**Roles**: `resident`（本人或组长）、`activity_leader`（须为本组组长）、`coordinator`、`sector_leader`、`individual_leader`、`property_admin`、`platform_admin`

> **实现状态（v7.9.1）**：已接线。校验 `isSelf || isLeader`（组长以 `group.leaderId === 当前用户ID` 判定），物业/平台管理员同物业可改。更新 `ActivityGroupMember.introduction`，成员列表 `GET /activity-groups/{id}/members` 同步回显 `introduction`。

```json
// Request Body
{
  "introduction": "{\"name\":\"张三\",\"age\":60,\"expertise\":\"太极拳\",\"bio\":\"习练20年\"}"
}
```

校验 `isSelf || isLeader`，更新 `ActivityGroupMember.introduction` 字段。

---

# 69. 三期 — 物业币商城规则 (coin-mall-rules)

查询/更新物业币商城开关和限制。

## 69.1 GET `/admin/property-coin/mall-rules` — 查询商城规则

**Roles**: `property_admin`, `platform_admin`

```json
// Response
{
  "code": 0,
  "data": {
    "propertyCompanyId": "pc_demo001",
    "coinMallEnabled": true,
    "coinMallMaxRatio": 0.5000,
    "coinMallMinAmount": 10.00
  }
}
```

## 69.2 PUT `/admin/property-coin/mall-rules` — 更新商城规则

**Roles**: `property_admin`

```json
// Request Body
{
  "coinMallEnabled": true,
  "coinMallMaxRatio": 0.50,
  "coinMallMinAmount": 10.00
}
```

校验：比例 0~1，金额 ≥ 0。

---

# 70. 三期 — 多角色提现 (role-withdrawals)

个体负责人和快递员的提现申请与记录查询，以及管理端审批闭环（§70.5–70.7）。

## 70.1 POST `/individual-leaders/{id}/withdrawals` — 个体负责人提现申请

**Roles**: `resident`（须持有启用中的个体负责人记录；v8.13 负责人标签化后不再要求 `role=individual_leader`）  
**Path**: `id` — `il_` / `res_` / `me`（同 §61）

```json
// Request Body
{ "amount": 100.00 }
```

创建 `WithdrawalRecord`（`withdrawal_type=individual_leader`），手续费率取自物业公司 `withdrawalFeeRate`。

## 70.2 GET `/individual-leaders/{id}/withdrawals` — 个体负责人提现记录

**Roles**: `resident`（须持有启用中的个体负责人记录；v8.13 负责人标签化后不再要求 `role=individual_leader`）  
**Path**: `id` — `il_` / `res_` / `me`（同 §61）

## 70.3 POST `/courier-managers/my/withdrawals` — 快递员提现申请

**Roles**: `courier`

```json
// Request Body
{ "amount": 50.00 }
```

创建 `WithdrawalRecord`（`withdrawal_type=courier`）。

## 70.4 GET `/courier-managers/my/withdrawals` — 快递员提现记录

**Roles**: `courier`

---

## 70.5 GET `/admin/role-withdrawals` — 配送员/多角色提现审核列表

> **背景**：快递员/个体负责人/统筹/板块负责人/单元楼长/业主卖货/物业经理/住户钱包的提现申请统一落库 `withdrawal_records`（`withdrawal_type=courier / individual_leader / coordinator / sector_leader / building_leader / resident_seller / property_manager / resident_wallet`），管理端需经本接口查询审核（原 `/admin/merchant-withdrawals` 仅查商家提现表 `merchant_withdrawals`，不包含这些记录）。
>
> **`resident_wallet` 说明（v8.26）**：住户把个人奖励金转入钱包后申请提现，走本接口审核。审核 `pending → approved` 只占用余额；`approved → completed` 时后端在同一事务内写入一条负向 `wallet_flows`（`entry_type=resident_wallet_withdrawal`）冲减钱包余额，前端无需额外处理。

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`  
**权限项**: `merchant:audit`（与商家提现审核同一审批权限）

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/admin/role-withdrawals?page=1&pageSize=20&withdrawalType=courier&auditStatus=pending&keyword=13900000016" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `withdrawalType` | `String` | ❌ | | 提现类型：`individual_leader`/`courier`/`coordinator`/`sector_leader`/`building_leader`（单元楼长分成）/`resident_seller`（业主卖货收益）/`property_manager`（物业经理抽成）/`resident_wallet`（🆕 v8.26 住户钱包提现） |
| `auditStatus` | `String` | ❌ | | 审核状态：`pending`/`approved`/`rejected`/`completed`（兼容传 `pending_audit`，等价 `pending`） |
| `keyword` | `String` | ❌ | | 关键词（提现ID/申请人ID/住户姓名/手机号，子查询匹配） |
| `startDate` | `String` | ❌ | | 开始日期 `yyyy-MM-dd` |
| `endDate` | `String` | ❌ | | 结束日期 `yyyy-MM-dd`（含当日） |
| `propertyCompanyId` | `String` | ❌ | | 物业公司ID（`platform_admin` 可指定，`property_admin` 只能查自己的） |

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "wr_3520d9a0ac1000",
        "withdrawalType": "courier",
        "applicantId": "cm_demo001",
        "applicantName": "吴十",
        "residentId": "res_courier01",
        "residentName": "吴十",
        "residentPhone": "13900000016",
        "propertyCompanyId": "pc_demo001",
        "amount": 50.00,
        "feeRate": 0.0200,
        "feeAmount": 1.00,
        "actualAmount": 49.00,
        "auditStatus": "pending",
        "auditorId": null,
        "auditedAt": null,
        "completedAt": null,
        "disburseChannel": null,
        "disburseStatus": null,
        "createdAt": "2026-07-09 10:00:00"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 1,
      "totalPages": 1
    }
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**字段说明**：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `list[].id` | `String` | 提现记录ID（`wr_` 前缀） |
| `list[].withdrawalType` | `String` | 提现类型：`individual_leader`/`courier`/`coordinator`/`sector_leader`/`building_leader`/`resident_seller` |
| `list[].applicantId` | `String` | 申请人主体ID（`cm_`/`il_`/`coo_`/`sec_`/`res_`） |
| `list[].applicantName` | `String` | 申请人姓名（关联住户为空时为 null） |
| `list[].residentId` | `String` | 关联住户ID |
| `list[].residentName` | `String` | 关联住户姓名 |
| `list[].residentPhone` | `String` | 关联住户手机号 |
| `list[].propertyCompanyId` | `String` | 物业公司ID |
| `list[].amount` | `BigDecimal` | 提现金额（元） |
| `list[].feeRate` | `BigDecimal` | 手续费比例 |
| `list[].feeAmount` | `BigDecimal` | 手续费（元） |
| `list[].actualAmount` | `BigDecimal` | 实际到账金额（元） |
| `list[].auditStatus` | `String` | 审核状态：`pending`/`approved`/`rejected`/`completed` |
| `list[].auditorId` | `String` | 审核人ID |
| `list[].auditedAt` | `String` | 审核时间 |
| `list[].completedAt` | `String` | 完成时间 |
| `list[].disburseChannel` | `String` | 打款渠道：`manual`/`auto_cbk` |
| `list[].disburseStatus` | `String` | 打款状态：`pending`/`success`/`failed` |
| `list[].createdAt` | `String` | 创建时间 |

**错误响应**：

| 错误码 | HTTP | 说明 |
| --- | --- | --- |
| `10001` | 400 | 参数缺失 / `withdrawalType`、`auditStatus` 值无效 |
| `20004` | 403 | 权限不足（无 `merchant:audit` 权限项） |

---

## 70.6 GET `/admin/role-withdrawals/summary` — 配送员/多角色提现审批汇总（看板）

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`  
**权限项**: `merchant:audit`

**Query 参数**：`propertyCompanyId`（`platform_admin` 可指定，`property_admin` 取当前物业）

**成功响应**：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `pendingCount` | `Long` | 待审笔数（pending + pending_audit） |
| `pendingAmount` | `BigDecimal` | 待审金额合计（申请金额） |
| `todayProcessedCount` | `Long` | 今日已审笔数（按 auditedAt） |
| `todayProcessedAmount` | `BigDecimal` | 今日已审金额合计（实到金额） |
| `approvedCount` | `Long` | 已通过笔数（approved + completed） |
| `approvedAmount` | `BigDecimal` | 已通过金额合计（实到金额） |

---

## 70.7 POST `/admin/role-withdrawals/{id}/audit` — 审核配送员/多角色提现

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`  
**权限项**: `merchant:audit`

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | `String` | ✅ | 提现记录ID（`wr_` 前缀，取 §70.5 列表的 `id`） |

**请求示例**：

```bash
# 审核通过（pending → approved）
curl -X POST https://test.yuanxusoftware.top/api/v1/admin/role-withdrawals/wr_3520d9a0ac1000/audit \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "auditResult": "approved",
    "remark": "审核通过"
  }'

# 打款完成（approved → completed，需记录转账凭证）
curl -X POST https://test.yuanxusoftware.top/api/v1/admin/role-withdrawals/wr_3520d9a0ac1000/audit \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "auditResult": "completed",
    "transferProof": "https://proof.example.com/img.jpg"
  }'

# 拒绝（必填 rejectReason）
curl -X POST https://test.yuanxusoftware.top/api/v1/admin/role-withdrawals/wr_3520d9a0ac1000/audit \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "auditResult": "rejected",
    "rejectReason": "金额超限"
  }'
```

**请求体**：

| 字段 | 类型 | 必填 | 校验 | 说明 |
| --- | --- | --- | --- | --- |
| `auditResult` | `String` | ✅ | `@NotBlank` | `approved`（通过）/ `rejected`（拒绝）/ `completed`（打款完成） |
| `transferProof` | `String` | ❌ | `@Size(max=500)` | 转账凭证URL（`completed` 时建议填写） |
| `rejectReason` | `String` | ❌ | `@Size(max=200)` | 拒绝原因（`rejected` 时必填） |
| `remark` | `String` | ❌ | `@Size(max=200)` | 备注 |

**状态机**：

| 当前状态 | 允许操作 | 说明 |
| --- | --- | --- |
| `pending` / `pending_audit` | `approved` / `rejected` | 首次审核；不能直接 `completed` |
| `approved` | `completed` / `rejected` | 打款完成或复核拒绝 |
| `rejected` / `completed` | — | 终态，不可再审核 |

**业务规则**：

- `approved`/`rejected` 写入 `auditedAt`、`auditorId`；`completed` 写入 `completedAt`，`disburseChannel=manual`、`disburseStatus=success`，并持久化 `transferProof`（人工打款闭环）
- 物业管理员只能审核本物业公司下的提现记录
- 每次审核写入操作日志（action=`audit`，targetType=`role_withdrawal`）

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "wr_3520d9a0ac1000",
    "auditStatus": "approved",
    "transferProof": null,
    "disburseChannel": null,
    "auditedAt": "2026-07-01 14:30:00",
    "auditorId": "res_admin01",
    "completedAt": null
  },
  "timestamp": "2026-07-01 14:30:00"
}
```

**错误响应**：

| 错误码 | HTTP | 说明 |
| --- | --- | --- |
| `10001` | 400 | 参数缺失（`rejected` 未填 `rejectReason` / `auditResult` 为空） |
| `10002` | 400 | `auditResult` 值无效 / `pending` 状态直接 `completed` |
| `94003` | 400 | 提现记录不存在 |
| `60003` | 409 | 审核已处理（终态或状态不允许该操作） |
| `20004` | 403 | 权限不足 / 跨物业操作 |

---

# 71. 三期 — 平台管理员分成体系 (platform-share)

平台管理员从订单分成、配送费、提现手续费三大链路获取分成。

## 71.0 业务说明

### 分成计算公式（distribution_b_v2，规范§4/§5；2026-11-03 V36 起）

**商品分账链路（配送费不进入商品抽佣基数）**：
```
抽佣基础额 = 商品金额 × 抽佣率
商家商品实得 = 商品金额 - 抽佣基础额

用户侧先扣档（V36 新增，从抽佣基础额里先切，不进一级三档）：
用户积分预算 = 抽佣基础额 × userPointShareRate   ← 先向下取整到整数积分再折回元，与实发积分一分不差
用户奖励金预算 = 抽佣基础额 × userRewardShareRate

平台盘 = max(抽佣基础额 - 积分成本 - 用户积分预算 - 用户奖励金预算, 0)
成本击穿差额 = max(积分成本 + 用户两档预算 - 抽佣基础额, 0)   ← 默认物业承担，不扣商家；物业币不再击穿

一级分配（比例之和 = 1.0000）：
平台分享 = 平台盘 × platformShareRate
物业分享 = 平台盘 × propertyShareRate
管理盘 = 平台盘 - 平台分享 - 物业分享        ← 减法吸收分币误差

管理盘级联（sectorLeaderRate=0.6，individualLeaderRate=0.5）：
板块毛额 = 管理盘 × sectorLeaderRate
个体负责人 = 板块毛额 × individualLeaderRate
板块负责人 = 板块毛额 - 个体负责人
统筹 = 管理盘 - 板块毛额
```

> **物业 / 统筹到手会下降**：先扣档不是从某一方嘴里出，而是平台盘整体变小，物业与统筹按扣完的一起等比少（抽佣 5% 时可分配从 5.00 变 4.00）。这是 2026-11-02 老板分账逻辑图的口径，不是 bug；要维持原到手需把抽佣率提到 6.25%（5% ÷ 0.8）。
>
> **历史单不回溯**：V36 之前落库的 `distribution_records` 四个新字段为 `null`、`calculation_version` 仍是 `distribution_b_v1`；前端按 `calculationVersion` 区分口径，别把历史单的 `null` 当 `0.00` 参与合计。

**配送分账链路（规范§5，不再使用 courierPerOrder 固定金额）**：
```
配送结算基数 = 实付配送费 + 配送补贴（满额减免时 = 原配送费）
公司配送收入 = 配送结算基数 × platformDeliveryShareRate
配送员收入 courierEarning = 配送结算基数 - 公司配送收入      ← 减法吸收分币误差

商家最终实得 merchantShare = 商家商品实得 - 商家配送补贴 + 商家调整额
```

**提现手续费链路**：
```
手续费 = 提现金额 × withdrawalFeeRate
平台手续费分成 = 手续费 × platformWithdrawalFeeShareRate（默认100%）
物业手续费分成 = 手续费 - 平台手续费分成
```

### 默认比例

| 比例字段 | 默认值 | 说明 |
| --- | --- | --- |
| `platformShareRate` | 0.0000 | 订单分成（默认0%） |
| `platformDeliveryShareRate` | 0.0000 | 配送费分成（默认0%） |
| `platformWithdrawalFeeShareRate` | 1.0000 | 提现手续费分成（默认100%归平台） |
| `userPointShareRate` | 0.1000 | 🆕 V36 用户积分预算比例（占抽佣基础额），数据库未配置时的兜底值 |
| `userRewardShareRate` | 0.1000 | 🆕 V36 用户奖励金预算比例（占抽佣基础额），数据库未配置时的兜底值 |

## 71.1 GET `/admin/platform-share-rates` — 查询平台分成比例

**Roles**: `platform_admin`, `property_admin`

**Query**: `propertyCompanyId`（可选，`platform_admin` 可查指定物业，`property_admin` 只能查自己的）

```json
// Response
{
  "code": 0,
  "data": {
    "propertyCompanyId": "pc_demo001",
    "platformShareRate": 0.0500,
    "platformDeliveryShareRate": 0.0000,
    "platformWithdrawalFeeShareRate": 1.0000,
    "propertyShareRate": 0.3000,
    "courierShareRate": 0.1500,
    "coordinatorShareRate": 0.7000,
    "sectorLeaderRate": 0.1000,
    "individualLeaderRate": 0.0000,
    "userPointShareRate": 0.1000,
    "userRewardShareRate": 0.1000
  }
}
```

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `propertyCompanyId` | `String` | 物业公司ID |
| `platformShareRate` | `BigDecimal` | 平台从订单可分配金额中的分成比例 |
| `platformDeliveryShareRate` | `BigDecimal` | 平台从配送费中的分成比例 |
| `platformWithdrawalFeeShareRate` | `BigDecimal` | 平台从提现手续费中的分成比例 |
| `propertyShareRate` | `BigDecimal` | 物业公司分成比例 |
| `courierShareRate` | `BigDecimal` | 快递员分成比例 |
| `coordinatorShareRate` | `BigDecimal` | 统筹（管理盘）分成比例 |
| `sectorLeaderRate` | `BigDecimal` | 板块负责人比例 |
| `individualLeaderRate` | `BigDecimal` | 个体负责人比例 |
| `userPointShareRate` | `BigDecimal` | 🆕 V36 用户积分预算比例（占**抽佣基础额**，分账链先扣档，默认 `0.1000`） |
| `userRewardShareRate` | `BigDecimal` | 🆕 V36 用户奖励金预算比例（占**抽佣基础额**，默认 `0.1000`；取代原商家级 `coinRebateRate`，后者已不再影响金额） |

---

## 71.2 PUT `/admin/platform-share-rates` — 更新平台分成比例

**Roles**: `platform_admin`（仅平台管理员可修改）

**请求体**（`UpdatePlatformShareRatesRequest`）：

| 字段 | 类型 | 必填 | 校验 |
| --- | --- | --- | --- |
| `propertyCompanyId` | `String` | ✅ | `@NotBlank` |
| `platformShareRate` | `BigDecimal` | ❌ | 0~1 |
| `platformDeliveryShareRate` | `BigDecimal` | ❌ | 0~1 |
| `platformWithdrawalFeeShareRate` | `BigDecimal` | ❌ | 0~1 |
| `propertyShareRate` | `BigDecimal` | ❌ | 0~1 |
| `courierShareRate` | `BigDecimal` | ❌ | 0~1 |
| `coordinatorShareRate` | `BigDecimal` | ❌ | 0~1 |
| `sectorLeaderRate` | `BigDecimal` | ❌ | 0~1 |
| `individualLeaderRate` | `BigDecimal` | ❌ | 0~1 |
| `userPointShareRate` | `BigDecimal` | ❌ | 🆕 0~1，`@DecimalMin/@DecimalMax` |
| `userRewardShareRate` | `BigDecimal` | ❌ | 🆕 0~1，`@DecimalMin/@DecimalMax` |

```json
// Request Body
{
  "propertyCompanyId": "pc_demo001",
  "platformShareRate": 0.05,
  "platformDeliveryShareRate": 0.10,
  "platformWithdrawalFeeShareRate": 1.0,
  "propertyShareRate": 0.30,
  "courierShareRate": 0.15,
  "coordinatorShareRate": 0.70,
  "sectorLeaderRate": 0.10,
  "individualLeaderRate": 0.00,
  "userPointShareRate": 0.10,
  "userRewardShareRate": 0.10
}
```

**成功响应**：`PlatformShareRatesResponse`，字段同 §71.1。

**校验与错误**：

| 条件 | HTTP | code | 文案 |
| --- | --- | --- | --- |
| 未传 `propertyCompanyId` | 400 | `10001` | `propertyCompanyId不能为空` |
| 物业公司不存在 / ID 格式非法 | 404 / 400 | `97001` / `10002` | — |
| 单档 < 0 或 > 1 | 400 | `10002` | `用户积分预算比例必须在0~100%之间`（其余档同构） |
| 🆕 用户两档合计 > 100% | 400 | `10002` | `用户积分与用户奖励金预算比例合计不得超过100%（当前为 120%）` |
| 一级三档（平台+物业+统筹）合计 ≠ 100% | 400 | `97006` | 原有校验，未变 |

- **只传要改的字段即可**，未传字段保持原值不动。
- **`0` 是合法值**，表示这一档不发；不会被当成「未配置」而回落成 10%。
- 建议前端做即时校验：两个输入框合计超过 100% 就拦下，别等后端报错。
- 等价入口：`PUT /admin/property-companies/{id}`（§51）同样接受这两个字段，同样仅平台管理员可改，物业管理员传了返回 `20004`。

---

## 71.3 GET `/admin/platform-earnings/stats` — 平台收益统计

**Roles**: `platform_admin`

**Query**: `propertyCompanyId`（可选）, `startDate`, `endDate`

```json
// Response
{
  "code": 0,
  "data": {
    "totalPlatformShare": 5000.00,
    "totalDeliveryEarning": 200.00,
    "totalWithdrawalFeeShare": 150.00,
    "totalEarning": 5350.00,
    "byProperty": [
      {
        "propertyCompanyId": "pc_demo001",
        "propertyName": "XX物业",
        "platformShare": 3000.00,
        "deliveryEarning": 100.00,
        "withdrawalFeeShare": 80.00
      }
    ]
  }
}
```

## 71.4 GET `/admin/platform-earnings/records` — 平台收益明细列表

**Roles**: `platform_admin`

**Query**: `propertyCompanyId`（可选）, `type`（`distribution` / `delivery` / `withdrawal`，可选）, `page`, `pageSize`, `startDate`, `endDate`

返回分页列表，每条记录包含 `id`、`type`、`propertyCompanyId`、`propertyName`、`amount`、`orderId`/`withdrawalId`、`createdAt`。

---

## 71.5 分成记录字段变更

### distribution_records 新增字段

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `platformShare` | decimal(10,2) | 平台管理员分成金额 |

### deliveries 新增字段

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `platformEarning` | decimal(10,2) | 平台管理员从配送费中的分成 |

### merchant_withdrawals 新增字段

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `platformFeeShare` | decimal(10,2) | 平台管理员从提现手续费中的分成 |

### withdrawal_records 新增字段

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `platformFeeShare` | decimal(10,2) | 平台管理员从提现手续费中的分成 |

### property_companies 新增字段

| 字段 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `platformShareRate` | decimal(5,4) | 0.0000 | 平台订单分成比例 |
| `platformDeliveryShareRate` | decimal(5,4) | 0.0000 | 平台配送费分成比例 |
| `platformWithdrawalFeeShareRate` | decimal(5,4) | 1.0000 | 平台提现手续费分成比例 |

### §21 分成模块响应增强

`DistributionStatsResponse.Summary` 新增 `platformAmount` 字段。

`DistributionStatsResponse.Summary` 新增 `courierAmount`、`merchantAmount`（商家实得合计 = Σ 订单额 × (1 − 抽佣率)）、`totalOrderAmount`（总订单额合计 = Σ 订单额）字段。

`DistributionCalculateResponse` 新增 `platformShare` 字段。

`DistributionCalculateResponse` 新增 `commissionBaseAmount` 字段（= 订单额 × 抽佣率，即抽佣基础额；实际可分配平台盘 = 此值 − pointCost − coinCost = distributableAmount）。

`DistributionCalculateResponse` 新增 `merchantShare` 字段（= 订单额 × (1 − 抽佣率)，即商家实得；与 `DistributionRecordItemResponse.merchantShare` 口径一致，C 端试算直接读取，勿本地估算）。

`DistributionRecordItemResponse` 新增 `platformShare` 字段。

### v8.9 口径澄清（distribution_b_v1，2026-10-06）

**A1 配送员报酬取数修正**：`courierEarning` 不再读取 `delivery_rules.courier_per_order` 固定每单金额，改为
`deliverySettlementBase × (1 − platform_delivery_share_rate)`，其中 `deliverySettlementBase = 实付配送费 + 配送补贴`。
`delivery_rules.courier_per_order`、`property_companies.delivery_courier_per_order`、`courier_share_rate` 列保留但**不再参与分账**（仅历史兼容）。
`courierShare` 恒等于 `courierEarning`。

**B1 电商模式平台分成口径统一**：商品链路的 `platformShare = 平台盘 × platform_share_rate`，`summary.platformAmount` 即该值合计（demo 场景为 1.00）。
电商收付通模式下，平台分账金额 = `distributableAmount`（商品佣金平台盘）+ `deliveries.platform_earning`（平台配送分成），与普通分账路径平台总收入一致。

**C1 交付前置**：生产禁用 Flyway，需手动执行 `V18__building_leader_and_seller_wallet.sql` 与 `V19__distribution_b_v1_alignment.sql`（均幂等）。

**订单侧新增快照列**（`orders`）：`original_delivery_fee`（满额减免前基础配送费）、`delivery_subsidy_sponsor`（承担方 `none`/`merchant`/`property`/`platform`）、`delivery_subsidy_amount`（配送补贴金额）。结算时读取快照，不得按最新配置重算。

---

# 72. 技工工作台 (technicians)
<a id="72-技工工作台-technicians"></a>

技工查看个人信息与名下服务工单（底层为 §16 服务订单 `ServiceOrder`，`providerId` = 当前技工住户 ID）。可与 §12 技工消息、§16 服务订单交叉使用。

### 工单状态映射

| 技工端 `status` | 服务订单 `orderStatus` | 说明 |
| --- | --- | --- |
| `pending` | `pending` | 待接单 |
| `assigned` | `confirmed` | 已接单（前端用 `assigned`，库内为 `confirmed`；传 `confirmed` 亦兼容） |
| `in_progress` | `in_progress` | 进行中 |
| `completed` | `completed` | 已完成 |
| `cancelled` | `cancelled` | 已取消 |

**允许的状态流转**：

| 当前 | 可转到 |
| --- | --- |
| `pending` | `assigned`、`cancelled` |
| `assigned` | `in_progress`、`completed`、`cancelled` |
| `in_progress` | `completed`、`cancelled` |
| `completed` / `cancelled` | （终态，不可再改） |

## 72.1 GET `/technicians/my` — 当前技工信息

**Auth**: Required  
**Roles**: `technician`

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/technicians/my" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**响应**（`TechnicianMyResponse`）：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "res_tech001",
    "name": "李技工",
    "phone": "138****5678",
    "specialty": "plumbing",
    "status": "active",
    "propertyCompanyId": "pc_demo001",
    "propertyName": "示范物业",
    "taskCount": 3,
    "completedCount": 28
  }
}
```

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `String` | 技工住户 ID（`res_`） |
| `name` | `String` | 姓名 |
| `phone` | `String` | 手机号（脱敏） |
| `specialty` | `String` | 专长/服务类目（取该技工名下一条 `active` 社区服务的 `category`，可空） |
| `status` | `String` | 账号状态 |
| `propertyCompanyId` | `String` | 物业公司 ID |
| `propertyName` | `String` | 物业公司名称 |
| `taskCount` | `Long` | 未完成工单数（`pending`/`confirmed`/`in_progress`） |
| `completedCount` | `Long` | 已完成工单数 |

## 72.2 GET `/technicians/my/tasks` — 我的工单列表

**Auth**: Required  
**Roles**: `technician`

**Query**：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数（最大 100） |
| `status` | `String` | ❌ | — | 技工工单态：`pending`/`assigned`/`in_progress`/`completed`/`cancelled` |
| `sort` | `String` | ❌ | `-createdAt` | 排序字段：`createdAt`/`serviceTime`/`updatedAt`；前缀 `-` 降序、`+` 升序 |

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/technicians/my/tasks?page=1&pageSize=20&status=pending&sort=-createdAt" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**列表项**（`TechnicianTaskItemResponse`）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `String` | 工单 ID（服务订单 ID，前缀 `so_`；兼容历史 `svc_ord`） |
| `title` | `String` | 标题（社区服务名，缺省为「服务工单」） |
| `description` | `String` | 订单备注优先，否则服务描述 |
| `address` | `String` | 服务地址 |
| `contactPhone` | `String` | 联系电话 |
| `contactName` | `String` | 联系人姓名（下单住户） |
| `status` | `String` | 技工端工单状态（见上表） |
| `statusCode` | `String` | 同 `status`（兼容字段） |
| `preferredTime` | `String` | 预约服务时间（ISO UTC） |
| `createdAt` | `String` | 创建时间（ISO UTC） |
| `updatedAt` | `String` | 更新时间（ISO UTC） |
| `completedAt` | `String` | 完成时间（仅 `completed` 时有值，取 `updatedAt`） |

## 72.3 PATCH `/technicians/my/tasks/{id}/status` — 更新工单状态

**Auth**: Required  
**Roles**: `technician`

**路径参数**：`id` — 工单 ID（`so_` 或历史 `svc_ord` 前缀）

**请求体**（`UpdateTechnicianTaskStatusRequest`）：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `status` | `String` | ✅ | 目标技工工单态；接单传 `assigned`，开始传 `in_progress`，完成传 `completed` |
| `remark` | `String` | ❌ | 备注（有值则写入服务订单 `remark`） |

**请求示例**：

```bash
curl -X PATCH "https://test.yuanxusoftware.top/api/v1/technicians/my/tasks/so_demo001/status" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "status": "assigned",
    "remark": "已接单，预计今日上门"
  }'
```

**成功响应**：`data` 为更新后的 `TechnicianTaskItemResponse`（字段同 §72.2）。

**业务规则**：

- 仅可操作 `providerId` = 当前技工 的工单
- 状态须符合上表流转；非法流转返回订单状态错误（`70002`）
- 账号 `frozen`/`disabled` 不可调用

**错误响应**：

| 错误码 | HTTP | 说明 |
| --- | --- | --- |
| `10002` | 400 | `status` 无效或工单 ID 格式不正确 |
| `10003` | 400 | `pageSize` 超过 100（列表接口） |
| `20004` | 403 | 非技工或无权操作该工单 |
| `20005` | 403 | 账号被禁用/冻结 |
| `70002` | 409 | 工单状态不允许该操作 |
| `96002` | 404 | 服务订单不存在 |

---


## 73 社区食堂模块 (Canteen)

> **模块说明**：社区食堂「定向充值 + 绑定商家」。
> - **主商家**（餐饮公司）：后台标记后拥有充值额度，可给用户充**定向余额**；定向余额挂在主商家体系下，只能在该主商家及其绑定的子商家消费。
> - **子商家**：被主商家绑定（如麻辣烫/米线/烧烤），用户在其下单可用主商家体系的定向余额抵扣。
> - **资金闭环**：主商家购买额度（扫呗支付给平台，**手续费内扣**，如付 100 元、费率 2%、到账 98 元额度）→ 主商家给用户充定向余额（扣自己额度）→ 用户消费抵扣 + 微信补差 → 退款原路退回定向余额。
> - **手续费落账**：平台收取的购买手续费写入 `wallet_flows`（ownerType=`platform`，entryType=`canteen_quota_fee`）。
> - 数据库脚本：`backend/sql/canteen_module_v1.sql`（5 张新表 + 3 处改表）。
> - **商家角色类型（v2 新增）**：`merchants.merchant_type` 区分 `goods`/`technician`/`group_leader`/`canteen` 四类；社区食堂商家（主/子）均为 `canteen` 类型，与普通商品商家（口福餐厅等）彻底隔离。增量脚本 `backend/sql/canteen_merchant_type_v2.sql`。

---

### 73.1 主商家创建/标记/取消

<a id="731-标记取消主商家"></a>

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

> **商家角色类型（v2 新增）**：`merchants.merchant_type` 区分四类：`goods` 商品商家 / `technician` 技工商家 / `group_leader` 组长商家 / `canteen` 社区食堂商家，历史数据已按 `merchant_source` 回填。社区食堂主/子商家均为 `canteen` 类型，与普通商品商家隔离：绑定/购买额度/充值/抵扣只认 `canteen` 类型，避免口福餐厅等被误当主商家。

#### 73.1.1 创建食堂商家（主/子）

**请求**：`POST /admin/canteen/merchants`

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `name` | `String` | ✅ | 商家名称 |
| `contactPhone` | `String` | ✅ | 店主手机号（须在该物业下已注册；建议用独立账号，避免与口福餐厅等同账号串户） |
| `propertyCompanyId` | `String` | ✅ | 物业公司 ID |
| `isMain` | `Boolean` | ❌ | 是否主商家（餐饮公司，可购买额度/充值）；默认 `false`（子商家） |
| `category` | `String` | ❌ | 经营类目（不传默认"社区食堂"） |
| `description` / `address` / `businessHours` | `String` | ❌ | 简介 / 地址 / 营业时间 |
| `videoUrl` | `String` | ❌ | 视频 URL |
| `coverUrls` | `Array<String>` | ❌ | 封面图列表（取第一张作主图） |
| `deliveryFee` | `BigDecimal` | ❌ | 配送费（默认 0） |
| `freeDeliveryThreshold` | `BigDecimal` | ❌ | 免配送阈值 |
| `deliveryScope` | `String` | ❌ | `both`/`self`/`community`（默认 `both`） |

**响应**：`MerchantDetailResponse`（`data` 含 `merchantType=canteen`、`isCanteenMainMerchant=isMain`、`auditStatus=approved`）。

> **店主账号开通**：创建成功后，店主（居民）的 `residents.role` 会自动升级为 `merchant`（原为 `resident`/`tenant` 时），店主即可用「手机号 + 密码」登录管理端（商户后台）经营食堂商家；若店主已是 `property_admin`/`platform_admin` 等管理角色则保持不变，避免剥夺其原有权限。管理端登录会拒绝 `role=resident` 的账号（返回 `PERMISSION_DENIED`）。

**错误响应**：

| 错误码 | HTTP | 说明 |
| --- | --- | --- |
| `10002` | 400 | 物业公司不存在（PARAM_INVALID） |
| `30001` | 404 | 店主手机号在该物业下未注册（RESIDENT_NOT_FOUND） |
| `10001` | 400 | 商家名称 / 店主手机号 / 物业 ID 缺失（PARAM_MISSING） |
| `10002` | 400 | 该店主在该物业下已存在社区食堂商家（PARAM_INVALID） |

#### 73.1.2 标记/取消主商家

将已有的 `canteen` 商家标记为主商家（设 `isCanteenMainMerchant=true` 并确认为 `canteen` 类型）；`false` 取消主商家标记（保留 `canteen` 类型，仅降级为非主）。

**请求**：`PUT /admin/canteen/merchants/{merchantId}/main?main=true`

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `merchantId` | `String` | ✅ | 商家 ID（路径） |
| `main` | `Boolean` | ✅ | 是否主商家 |

**响应**：`data` 为 `null`。

**错误响应**：

| 错误码 | HTTP | 说明 |
| --- | --- | --- |
| `60001` | 404 | 商家不存在 |

#### 73.1.3 手动调整可用充值额度

**请求**：`POST /admin/canteen/merchants/{merchantId}/quota-adjust`

> 免手续费、无需支付，直接给主商家入账可用充值额度；落一条 `status=paid` 的额度购买流水（与正常购买到账口径一致，`remaining_quota` 参与汇总与扣减）。**仅社区食堂主商家可调**。用于调试/运营兜底。

**Roles**: `property_admin`, `platform_admin`

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `amount` | `BigDecimal` | ✅ | 入账额度（必须 > 0） |
| `remark` | `String` | ❌ | 备注（仅记录用途，不落库） |

**响应**：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `merchantId` | `String` | 主商家 ID |
| `amount` | `BigDecimal` | 本次入账额度 |
| `remainingQuota` | `BigDecimal` | 调整后可用充值额度总额 |

**错误响应**：

| 错误码 | HTTP | 说明 |
| --- | --- | --- |
| `60001` | 404 | 商家不存在 |
| `CANTEEN_NOT_MAIN_MERCHANT` | 400 | 仅社区食堂主商家可调整可用充值额度 |

---

### 73.2 绑定子商家到主商家

<a id="732-绑定子商家到主商家"></a>

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**请求**：`POST /admin/canteen/bindings`

**请求体**（`BindingCreateRequest`）：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `mainMerchantId` | `String` | ✅ | 主商家 ID（须已标记为主商家，且与子商家同一物业） |
| `subMerchantId` | `String` | ✅ | 子商家 ID |
| `supplyDiscountRate` | `BigDecimal` | ❌ | 供货优惠比例（0.20 = 子商家给主商家供货 8 折），不传默认 `0.20` |

**响应**（`BindingResponse`）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `String` | 绑定 ID（`cmb_`） |
| `mainMerchantId` | `String` | 主商家 ID |
| `mainMerchantName` | `String` | 主商家名称 |
| `subMerchantId` | `String` | 子商家 ID |
| `subMerchantName` | `String` | 子商家名称 |
| `supplyDiscountRate` | `BigDecimal` | 供货优惠比例 |
| `status` | `String` | `active`/`inactive` |
| `createdAt` | `String` | 创建时间（ISO UTC） |

**错误响应**：

| 错误码 | HTTP | 说明 |
| --- | --- | --- |
| `60001` | 404 | 主/子商家不存在 |
| `80021` | 400 | 该商家不是社区食堂主商家（需先 73.1 标记） |
| `80022` | 409 | 该子商家已被该主商家绑定 |
| `10002` | 400 | 主商家与子商家相同 / 不属于同一物业 |

---

### 73.3 解绑

<a id="733-解绑"></a>

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**请求**：`DELETE /admin/canteen/bindings/{id}`

软解绑（`status=inactive`）。历史订单退款不受影响（退款按订单上的主商家体系快照原路退回）。

**响应**：`data` 为 `null`。

**错误响应**：`80023`（404）绑定关系不存在。

---

### 73.4 调整供货优惠比例

<a id="734-调整供货优惠比例"></a>

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**请求**：`PUT /admin/canteen/bindings/{id}/rate`

**请求体**（`BindingUpdateRequest`）：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `supplyDiscountRate` | `BigDecimal` | ✅ | 供货优惠比例（0~1） |

**响应**：更新后的 `BindingResponse`（字段同 73.2）。

---

### 73.5 绑定列表

<a id="735-绑定列表"></a>

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**请求**：`GET /admin/canteen/bindings?page=1&pageSize=20&mainMerchantId=&subMerchantId=`

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `page` | `int` | ❌ | 页码，默认 1 |
| `pageSize` | `int` | ❌ | 每页条数，默认 20（≤100） |
| `mainMerchantId` | `String` | ❌ | 按主商家过滤 |
| `subMerchantId` | `String` | ❌ | 按子商家过滤 |

**响应**：`PageResponse<BindingResponse>`（`list` 元素字段同 73.2）。

---

### 73.6 手续费配置

<a id="736-手续费配置"></a>

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**查询**：`GET /admin/canteen/settings`

**更新**：`PUT /admin/canteen/settings`

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `quotaFeeRate` | `BigDecimal` | ✅ | 购买额度手续费比例（内扣，0~1），默认 `0.02` |

**响应**（`CanteenSettingsResponse`）：`{ "quotaFeeRate": 0.02 }`

---

### 73.7 三类流水查询

<a id="737-三类流水查询"></a>

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**① 额度购买流水**：`GET /admin/canteen/quota-purchases`

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `page` / `pageSize` | `int` | ❌ | 分页（pageSize ≤ 100） |
| `merchantId` | `String` | ❌ | 按主商家过滤 |
| `status` | `String` | ❌ | `pending`/`paid`/`refunded`/`closed` |
| `startDate` / `endDate` | `String` | ❌ | `yyyy-MM-dd` |

**② 定向充值流水**：`GET /admin/canteen/recharges`

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `page` / `pageSize` | `int` | ❌ | 分页 |
| `mainMerchantId` | `String` | ❌ | 按主商家过滤 |
| `residentId` | `String` | ❌ | 按被充值用户过滤 |
| `startDate` / `endDate` | `String` | ❌ | `yyyy-MM-dd` |

**③ 消费/退款流水**：`GET /admin/canteen/flows`

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `page` / `pageSize` | `int` | ❌ | 分页 |
| `mainMerchantId` | `String` | ❌ | 按主商家体系过滤 |
| `merchantId` | `String` | ❌ | 按实际消费商家过滤 |
| `residentId` | `String` | ❌ | 按用户过滤 |
| `flowType` | `String` | ❌ | `consume_deduct`/`refund_back` |
| `startDate` / `endDate` | `String` | ❌ | `yyyy-MM-dd` |

**响应**：
- ① 返回 `PageResponse<QuotaRecordResponse>`：`id`、`type=purchase`、`amount`（支付金额）、`feeAmount`、`actualQuota`（到账额度）、`status`、`remark`、`createdAt`
- ② 返回 `PageResponse<AdminRechargeResponse>`：`id`、`mainMerchantId`、`mainMerchantName`、`residentId`、`residentName`、`residentPhone`、`amount`、`balanceAfter`、`operatorId`、`remark`、`createdAt`
- ③ 返回 `PageResponse<DirectedFlowResponse>`（同 73.13 用户端流水，`orderNo` 当前为 `null`）

---

### 73.8 流水导出 CSV

<a id="738-流水导出csv"></a>

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**请求**：`GET /admin/canteen/export?type=quota_purchase|recharge|directed_flow&merchantId=&startDate=&endDate=`

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `type` | `String` | ✅ | `quota_purchase` / `recharge` / `directed_flow` |
| `merchantId` | `String` | ❌ | 按主商家过滤 |
| `startDate` / `endDate` | `String` | ❌ | `yyyy-MM-dd` |

**响应**：`text/csv` 文件下载（UTF-8 BOM，Excel 可直接打开中文）。一次性导出最多 1000 条。

---

### 73.9 购买充值额度（商家端）

<a id="739-购买充值额度"></a>

**Auth**: Required  
**Roles**: `merchant`（须主商家）

**① 试算**：`POST /merchant/canteen/quota/quote`

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `amount` | `BigDecimal` | ✅ | 申请购买额度金额（> 0） |

**响应**（`QuotaQuoteResponse`）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `quotaAmount` | `BigDecimal` | 申请购买额度金额（= 支付金额） |
| `feeRate` | `BigDecimal` | 手续费比例 |
| `feeAmount` | `BigDecimal` | 手续费金额 |
| `actualQuota` | `BigDecimal` | 实际到账额度 = `quotaAmount − feeAmount` |

**② 发起购买**：`POST /merchant/canteen/quota/purchase`

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `amount` | `BigDecimal` | ✅ | 申请购买额度金额 |
| `openid` | `String` | ❌ | 微信小程序 openid（不传取店主绑定 openid） |

**响应**（`QuotaPurchaseResponse`）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `purchaseId` | `String` | 购买流水 ID（`cqp_`） |
| `quotaAmount` / `feeAmount` / `actualQuota` | `BigDecimal` | 见试算 |
| `payAmount` | `BigDecimal` | 扫呗支付金额（= `quotaAmount`） |
| `paymentRecordId` | `String` | 支付流水 ID（`pr_`） |
| `status` | `String` | `pending`（待支付，返回 `payParams`）或 `paid`（开发模式直接到账） |
| `payParams` | `Object` | `wx.requestPayment` 调起参数（`status=pending` 时返回；支付成功后由回调自动到账，前端可用 `GET /merchant/canteen/quota/records` 轮询 `status=paid`） |

**错误响应**：

| 错误码 | HTTP | 说明 |
| --- | --- | --- |
| `80021` | 400 | 该商家不是社区食堂主商家 |
| `80028` | 400 | 存在待支付的额度购买，请先完成支付或等待超时 |
| `10002` | 400 | 金额不合法 |

---

### 73.10 额度余额与流水（商家端）

<a id="7310-额度余额与流水"></a>

**Auth**: Required  
**Roles**: `merchant`（须主商家）

**① 额度余额**：`GET /merchant/canteen/quota/balance`

**响应**（`MerchantQuotaBalanceResponse`）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `merchantId` | `String` | 主商家 ID |
| `availableQuota` | `BigDecimal` | 可用额度（= Σ已到账购买记录剩余额度） |
| `totalPurchasedQuota` | `BigDecimal` | 累计购买到账额度 |
| `totalRecharged` | `BigDecimal` | 累计已充值额度 |

**② 额度流水**：`GET /merchant/canteen/quota/records?page=&pageSize=`

**响应**：`PageResponse<QuotaRecordResponse>`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `String` | 记录 ID |
| `type` | `String` | `purchase`（购买，金额为正）/ `recharge`（定向充值扣减，金额为负） |
| `amount` | `BigDecimal` | 金额（正负见上） |
| `feeAmount` / `actualQuota` | `BigDecimal` | 仅购买记录有值 |
| `status` | `String` | 购买记录：`pending`/`paid`/`refunded`/`closed` |
| `targetResidentId` | `String` | 充值记录：被充值用户 ID |
| `remark` | `String` | 备注 |
| `createdAt` | `String` | 创建时间（ISO UTC） |

---

### 73.11 给用户充定向余额（商家端）

<a id="7311-给用户充定向余额"></a>

**Auth**: Required  
**Roles**: `merchant`（须主商家）

**请求**：`POST /merchant/canteen/recharge`

**请求体**（`DirectedRechargeRequest`）：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `phone` | `String` | 二选一 | 用户手机号（须在该物业下存在） |
| `residentId` | `String` | 二选一 | 用户 ID |
| `amount` | `BigDecimal` | ✅ | 充值金额（> 0，从主商家额度中扣减） |
| `remark` | `String` | ❌ | 备注 |

**响应**（`DirectedRechargeResponse`）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `rechargeId` | `String` | 充值流水 ID（`cdr_`） |
| `mainMerchantId` | `String` | 主商家体系 ID |
| `residentId` | `String` | 被充值用户 ID |
| `residentName` | `String` | 被充值用户姓名 |
| `amount` | `BigDecimal` | 充值金额 |
| `balanceAfter` | `BigDecimal` | 充值后该体系定向余额 |

**错误响应**：

| 错误码 | HTTP | 说明 |
| --- | --- | --- |
| `80021` | 400 | 该商家不是社区食堂主商家 |
| `30001` | 404 | 该物业下未找到对应手机号的住户 |
| `80024` | 400 | 充值额度余额不足 |
| `10002` | 400 | 住户与商家不属于同一物业 / 参数不合法 |

**商家端充值记录**：`GET /merchant/canteen/recharge-records?page=&pageSize=` → `PageResponse<DirectedRechargeResponse>`。

---

### 73.12 我的定向余额（用户端）

<a id="7312-我的定向余额"></a>

**Auth**: Required  
**Roles**: `resident`

**请求**：`GET /canteen/my-balances`

**响应**：`data` 为数组，按主商家体系展示：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `mainMerchantId` | `String` | 主商家体系 ID |
| `mainMerchantName` | `String` | 主商家名称 |
| `balance` | `BigDecimal` | 可用定向余额 |

> 用户可在**多个**主商家体系持有定向余额；消费时按体系展示/选择。

---

### 73.13 我的定向余额流水（用户端）

<a id="7313-我的定向余额流水"></a>

**Auth**: Required  
**Roles**: `resident`

**请求**：`GET /canteen/flows?page=1&pageSize=20`

**响应**：`PageResponse<DirectedFlowResponse>`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `String` | 流水 ID（`cdf_`） |
| `flowType` | `String` | `recharge`（充值）/ `consume_deduct`（消费扣减）/ `refund_back`（退款退回） |
| `mainMerchantName` | `String` | 主商家体系名称 |
| `amount` | `BigDecimal` | 发生金额（正数） |
| `balanceAfter` | `BigDecimal` | 变动后余额 |
| `orderNo` | `String` | 关联订单号（消费/退款时有值） |
| `merchantName` | `String` | 实际消费商家名称 |
| `createdAt` | `String` | 创建时间（ISO UTC） |

---

### 73.14 订单支付 — 定向余额抵扣（对接前端）

<a id="7314-订单支付定向余额抵扣"></a>

> 定向余额抵扣**复用现有订单支付链路**（`POST /orders/preview` → `POST /orders` → `POST /orders/{id}/pay`），仅扩展字段，不新增下单流程。

**前端流程**：
1. `POST /orders/preview` → 响应 `directedBalances` 数组（订单商家所属主商家体系 + 用户可用余额，**余额>0 才返回**）
2. 若 `directedBalances` 非空：展示"食堂定向余额抵扣"入口，用户选择**一个**主商家体系并输入抵扣金额
3. `POST /orders/{id}/pay`：`paymentMethod=mixed`，同时传 `mainMerchantId`（所选体系）+ `directedBalanceUsed`（抵扣金额）；其余金额走 `coinUsed`/`pointUsed`/现金微信补差
4. 支付成功后：订单详情/支付响应返回 `directedBalanceUsed`（本单使用定向余额额）与 `directedMainMerchantId`；用户在 `GET /canteen/flows` 可见 `consume_deduct` 消费流水
5. **退款**：订单取消/退款审核通过时，`directedBalanceUsed` 原路退回定向余额（`refund_back` 流水）；**即使子商家被解绑，退款仍按订单快照退回**

**规则**：
- 仅 `mixed` 支付方式支持定向余额抵扣；`mainMerchantId` 与 `directedBalanceUsed` 必须成对传入（单独传任一个 → `10002`）
- `mainMerchantId` 必须是订单商家所属体系（订单商家为主商家 → 体系=自己；为子商家 → 体系=绑定它的 active 主商家，多个时由用户选择）
- 抵扣金额 ≤ 该体系定向余额，且 ≤ 剩余应付（不足 → `80025` 定向余额不足）
- 体系不匹配 → `80026`；未选体系但余额>0 → 前端应弹选择（后端不做强制）

---

# 枚举值速查表



## 板块 (Sector) — 2026-09-08 v8.3 统一

> **全域唯一字典**：板块负责人、个体负责人、个体负责人申请、统筹工作台、分账统计共用。
> 写接口接受中文（保洁/维修/安保/绿化/其它），后端统一归一为英文码入库。

| 值 | 中文 | 说明 |
|----|------|------|
| `cleaning` | 保洁 | 保洁板块 |
| `repair` | 维修 | 维修板块 |
| `security` | 安保 | 安保板块（中文别名：保安） |
| `greening` | 绿化 | 绿化板块 |
| `other` | 其它 | 其它板块（中文别名：其他） |

> 历史库注释中的 `elderly`/`medical`/`education`/`canteen`/`pet`/`life_service` **已废弃**；
> 存量数据由配套 SQL 统一归到 `other`，展示时仍会回显旧中文名。


## 特惠推送状态 / 目标类型 (SpecialOffer) — 2026-09-08 v8.3

| 字段 | 值 | 中文 | 住户端可见 |
|------|----|------|-----------|
| `status` | `draft` | 草稿 | ❌ 仅管理端 |
| `status` | `published` | 已发布（含历史别名 `待发布` / `active`） | ✅ |
| `status` | `ended` | 已结束 | ❌ |
| `status` | `archived` | 已归档 / 已删除 | ❌ |
| `targetType` | `all` | 全体业主 | 本物业全体 |
| `targetType` | `building` | 指定小区 | 小区匹配 |
| `targetType` | `role` | 指定商户 | 需消费过该商户 |
| `targetType` | `tag` | 指定标签 | 暂按本物业全体 |


## 公告类型 (AnnouncementType)


| 值                        | 说明        |
| ------------------------ | --------- |
| `property_announcement`  | 物业公告      |
| `community_announcement` | 社区公告      |
| `platform_announcement`  | 统筹/官方推荐公告 |
| `system_announcement`    | 系统公告      |
| `merchant_announcement`  | 商家公告      |
| `activity_announcement`  | 活动组公告     |




## 审核状态 (AuditStatus)


| 值               | 说明  |
| --------------- | --- |
| `pending_audit` | 待审核 |
| `approved`      | 已通过 |
| `rejected`      | 已拒绝 |




## 物业币类型 (CoinType)


| 值          | 说明  |
| ---------- | --- |
| `earn`     | 获得  |
| `spend`    | 消费  |
| `recharge` | 充值  |
| `freeze`   | 冻结  |
| `unfreeze` | 解冻  |
| `expire`   | 过期  |
| `withdraw` | 提现  |




## 配送状态 (DeliveryStatus)


| 值                   | 说明                                          |
| ------------------- | ------------------------------------------- |
| `pending`           | 待抢单                                         |
| `accepted`          | 已接单                                         |
| `delivering`        | 配送中                                         |
| `completed`         | **已完成（正式完成态）**：配送员完成配送 / 商家确认自配送达           |
| `failed`            | 配送失败（订单取消时联动置失败）                            |
| `property_fallback` | 物业兜底（超时无人抢单，转物业处理）                          |
| `merchant_self`     | 商家自配待配送（内部态，骑手不可见）                           |
| `merchant_delivering` | 商家自配配送中（内部态，骑手不可见）                          |
| `delivered`         | ⚠️ 历史遗留完成态（旧部署写入），**读写均归一为 `completed`**，禁止新写入 |
| `refunded`          | ⚠️ 历史遗留退款态（旧部署退款冲正写入），当前模型退款只改订单状态，配送单保持 `completed` |

> **v8.12 口径**：配送单状态回答「货送到哪了」，与订单业务状态（§订单状态）相互独立。
> 配送员完成配送 → 配送单 `completed`，订单仍 `delivering`（已送达待确认）；
> 居民确认收货 / 超期自动确认 / 商家自配确认送达 → 订单 `completed`（资金终态，触发结算）。




## 物业费状态 (FeeStatus)


| 值         | 说明   |
| --------- | ---- |
| `unpaid`  | 未支付  |
| `partial` | 部分支付 |
| `paid`    | 已支付  |
| `overdue` | 逾期   |




## 商家等级 (MerchantLevel)


| 值                    | 说明   |
| -------------------- | ---- |
| `official_certified` | 官方认证 |
| `property_certified` | 物业认证 |
| `normal`             | 普通   |




## 消息类型 (MessageType)


| 值                    | 说明       |
| -------------------- | -------- |
| `property_message`   | 物业消息     |
| `platform_message`   | 统筹消息     |
| `system_message`     | 系统消息     |
| `community_message`  | 社区消息     |
| `resident_message`   | 业主消息(私聊) |
| `technician_message` | 技工消息(群发) |
| `official_message`   | 官方定向/公告会话 |
| `merchant_ad`          | 商家广告卡片 |
| `service_request`    | 服务需求/费用通知 |




## 订单状态 (OrderStatus)


| 值            | 说明  | 流转                          |
| ------------ | --- | --------------------------- |
| `pending`    | 待支付 | → `paid`, `cancelled`       |
| `paid`       | 已支付 | → `delivering`, `refunding` |
| `delivering` | 配送中 | → `completed`               |
| `completed`  | 已完成 | 终态                          |
| `cancelled`  | 已取消 | 终态                          |
| `refunding`  | 退款中 | → `refunded`, `refund_rejected` |
| `refunded`   | 已退款 | 终态                          |
| `refund_rejected` | 已驳回 | 终态（审核不通过）       |




## 支付方式 (PaymentMethod)


| 值               | 说明  |
| --------------- | --- |
| `point`         | 积分  |
| `property_coin` | 物业币 |
| `wechat`        | 微信  |
| `mixed`         | 混合  |


> **注意**：全系统统一使用 `property_coin`，**不接受** `coin` 作为支付方式枚举值。传 `coin` 将返回 `10002` 参数无效。



## 社区食堂-额度购买状态 (CanteenQuotaPurchaseStatus)

| 值 | 说明 |
| --- | --- |
| `pending` | 待支付（扫呗已预下单，等待回调） |
| `paid` | 已到账（额度可用） |
| `refunded` | 已退款 |
| `closed` | 已关闭 |

## 社区食堂-定向余额流水类型 (CanteenDirectedFlowType)

| 值 | 说明 |
| --- | --- |
| `recharge` | 充值（主商家给用户充入，用户端流水展示用） |
| `consume_deduct` | 消费扣减（订单支付抵扣） |
| `refund_back` | 退款退回（订单退款原路退回） |

## 社区食堂-绑定状态 (CanteenBindingStatus)

| 值 | 说明 |
| --- | --- |
| `active` | 启用（子商家可用主商家体系定向余额） |
| `inactive` | 停用（已解绑） |

## 积分来源 (PointSource)


| 值                | 说明    |
| ---------------- | ----- |
| `order`          | 订单    |
| `activity`       | 活动    |
| `sign_in`        | 签到    |
| `manual`         | 手动    |
| `merchant_grant` | 商家录入  |
| `fee_deduct`     | 物业费抵扣 |
| `pool_diff`      | 积分池差额 |
| `transfer`       | 家庭转移  |




## 积分类型 (PointType)


| 值          | 说明    |
| ---------- | ----- |
| `earn`     | 获得    |
| `spend`    | 消费    |
| `transfer` | 转移    |
| `expire`   | 过期    |
| `pool_in`  | 进入积分池 |




## 积分池记录类型 (PoolRecordType)


| 值               | 说明    |
| --------------- | ----- |
| `diff`          | 兑换差额  |
| `expiry_clear`  | 过期清零  |
| `fee_clear`     | 物业费清零 |
| `manual_adjust` | 手动调整  |
| `manual_out`    | 手动流出  |




## 用户角色 (UserRole)


| 值                   | 说明    |
| ------------------- | ----- |
| `resident`          | 业主/住户 |
| `property_admin`    | 物业侧主角色（领导/日常管理员细分见 PropertySubRole） |
| `platform_admin`    | 平台管理员 |
| `merchant`          | 商家    |
| `courier`           | 快递员   |
| `coordinator`       | 统筹负责人 |
| `sector_leader`     | 板块负责人 |
| `individual_leader` | 个体负责人 |
| `activity_leader`   | 活动组组长 |
| `technician`        | 技工    |

> **不要**把 `property_leader`、`property_operator` 写入 `UserRole`/`role`；二者为物业子角色。  
> 前端「物业领导」：`role === 'property_admin' && propertySubRole === 'property_leader'`。


## 用户类型 (UserType)


| 值        | 说明  |
| -------- | --- |
| `owner`  | 业主  |
| `tenant` | 租户  |




## 投票选项 (VoteOption)


| 值         | 说明  |
| --------- | --- |
| `support` | 支持  |
| `oppose`  | 反对  |
| `abstain` | 弃权  |




## 提现状态 (WithdrawalStatus)


| 值               | 说明  |
| --------------- | --- |
| `pending_audit` | 待审核 |
| `approved`      | 已通过 |
| `rejected`      | 已拒绝 |
| `completed`     | 已完成 |
| `failed`        | 失败  |

---

## 官方发送方类型 (OfficialSenderType)

| 值 | 说明 |
| --- | --- |
| `community` | 社区官方 |
| `property` | 物业官方 |
| `platform` | 平台官方 |
| `coordinator` | 统筹/定向推送权限用户 |

## 公告投递渠道 (AnnouncementDeliveryChannel)

| 值 | 说明 |
| --- | --- |
| `announcement_board` | 仅公告栏 |
| `chat` | 仅聊天列表 |
| `both` | 公告栏 + 聊天 |

## 服务需求状态 (ServiceRequestStatus)

主单 `service_request_orders.status`：

| 值 | 中文展示 |
| --- | --- |
| `matching` | 匹配中 |
| `negotiating` | 协商中 |
| `quoted` | 待支付 |
| `paid` | 已支付 |
| `completed` | 已完成 |
| `closed` | 已关闭 |
| `cancelled` | 已取消 |

### 商家队列状态 (ServiceRequestQueueStatus)

表 `service_request_merchant_queue.status`（关联：`request_id` → 主单 `id`）：

| 值 | 中文展示 | 说明 |
| --- | --- | --- |
| `pending` | 待响应 | 当前轮到该商家；出现在 pending 列表 |
| `waiting` | 排队中 | 尚未轮到 |
| `accepted` | 已接单 | 商家已接受 |
| `skipped` | 已跳过 | 商家拒绝/跳过 |
| `timeout` | 已超时 | 5 分钟未响应 |
| `cancelled` | 已取消 | ：居民取消主单时同步写入；不出现在 pending 列表 |

## 商家消息服务半径 (MerchantServiceRadius)

| 值 | 说明 |
| --- | --- |
| `1km` | 1公里 |
| `3km` | 3公里 |
| `5km` | 5公里 |
| `district` | 本区 |
| `city` | 本市 |

## 物业子角色 (PropertySubRole)

> 二期 §54 新增。存于 `residents.property_sub_role`；**不是**独立登录主角色。  
> 主角色字段 `role` **必须**为 `property_admin`。

| 值 | 说明 | 主 `role` |
| --- | --- | --- |
| `property_leader` | 物业领导（全公司权限） | `property_admin` |
| `property_operator` | 物业管理员（日常操作，可限管辖范围） | `property_admin` |

联调账号见 §1.2：`13900000020` / `13900000021`，密码使用创建时返回的 `initialPassword`。

## 业主商户申请状态 (ResidentMerchantStatus)

| 值 | 中文展示 |
| --- | --- |
| `pending_deposit` | 待缴保证金 |
| `pending_audit` | 待审核 |
| `active` | 营业中 |
| `suspended` | 已暂停 |
| `quitting` | 退出冷却中 |
| `closed` | 已关闭 |

## 社区论坛模块 (Community)

> 类 QQ 动态的社区论坛，范围**仅本小区可见**（按 `community_id` 过滤）。采用**先发后审**：发出即可见，依赖「举报 + 管理员删帖」治理。
> 互动能力：发帖、点赞、评论（支持二级回复）、举报；管理端：帖子列表、评论列表、删帖、删评论、处理举报（通过举报自动隐藏内容）。
> 发帖/评论内容经过 `sensitive_words` 本地词库敏感词过滤，命中返回 `98005`。

**Auth**: Required（居民端需 `resident` 角色；管理端需具备管理端工作台身份——`property_admin`/`platform_admin`/`community_admin`，或持活动组长/负责人标签的住户——并持有 `community:read`/`community:manage`）
**Base Path**: `/community`（居民）、`/admin/community`（管理）

### 社区论坛数据结构

| 表 | 前缀 | 说明 |
| --- | --- | --- |
| `community_posts` | `cp_` | 帖子：`community_id`/`property_company_id`/`author_id`/`content`/`image_urls(json)`/`status(published\|hidden\|deleted)`/`like_count`(DB 原子更新)/`comment_count` |
| `community_comments` | `cc_` | 评论：`post_id`/`author_id`/`content`/`parent_id`(二级回复)/`reply_to_user_id`/`reply_to_user_name`/`status(published\|deleted)` |
| `content_reports` | `crp_` | 举报：`reporter_id`/`target_type(post\|comment)`/`target_id`/`reason_type`/`status(pending\|accepted\|rejected)`/`handler_id`/`handle_remark`/`handled_at`，唯一键 `(reporter_id,target_type,target_id)` |
| `community_post_likes` | `like_` | 点赞：`post_id`/`resident_id`，唯一键 `(post_id,resident_id)`（V4 新增，替代 `liked_by` JSON） |
| `sensitive_words` | `sw_` | 敏感词：`word`/`category`/`level`；V3 内置 20 条种子数据 |

### 文件上传

发帖图片先调用 `POST /files/upload?category=community`（multipart `file`，居民可传，≤10MB/张，最多 9 张），返回 `url` 列表填入 `imageUrls`。

### §C.1 POST `/community/posts` — 发帖

**Roles**: `resident`

**请求体**：

| 字段 | 类型 | 必填 | 校验 | 说明 |
| --- | --- | --- | --- | --- |
| `content` | `String` | ✅ | `@NotBlank @Size(max=2000)` | 帖子内容 |
| `imageUrls` | `List<String>` | ❌ | `≤9` | 图片 URL 列表（先调上传接口，最多 9 张） |

**前置条件**：

- 仅 `resident` 角色可发帖，其他角色（`merchant`/`technician`/`activity_leader` 等）返回 `20004` 权限不足。
- 住户必须已绑定 `community_id` 与 `property_company_id`，否则返回 `10002`：「请先完善小区/物业信息后再发帖」。
- 内容超 2000 字返回 `98007`；图片超 9 张返回 `10002`；命中敏感词返回 `98005`。

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "cp_abc",
    "authorName": "张小明",
    "content": "周末跳蚤市场~",
    "imageUrls": [],
    "status": "published",
    "likeCount": 0,
    "commentCount": 0,
    "likedByMe": false,
    "createdAt": "2026-08-03T09:00:00"
  }
}
```

### §C.2 GET `/community/posts` — 本小区帖子流

**Roles**: `resident`
**Query**：`page`(默认1)、`pageSize`(默认10,≤50)、`sort`(`newest`|`hot`|`oldest`，默认`newest`)、`keyword`(按内容模糊)
**说明**：仅返回当前用户所在 `community_id` 且 `status=published` 的帖子；返回 `PageResponse<CommunityPostResponse>`。
**注意**：仅 `resident` 角色可访问（其他角色 `20004`）；用户未绑定 `community_id` 时列表恒空，发帖亦报 `10002`——属账号数据问题，非接口故障。

### §C.3 GET `/community/posts/{id}` — 帖子详情

**Roles**: `resident`（非 published 返回 `98001`）

### §C.4 DELETE `/community/posts/{id}` — 删除自己的帖子

**Roles**: `resident`（仅作者可删，否则 `98006`；软删 `status=deleted`）

### §C.5 POST `/community/posts/{id}/like` — 点赞 / 取消点赞

**Roles**: `resident`
**成功响应**：`{ "likeCount": 13, "liked": true }`
**说明**：点赞记录存独立表 `community_post_likes`（唯一键防重复），`like_count` 由数据库原子增减；重复调用在点赞/取消之间切换。

### §C.6 POST `/community/posts/{id}/comments` — 评论（支持二级回复）

**Roles**: `resident`

**请求体**：

| 字段 | 类型 | 必填 | 校验 | 说明 |
| --- | --- | --- | --- | --- |
| `content` | `String` | ✅ | `@NotBlank @Size(max=1000)` | 评论内容 |
| `parentId` | `String` | ❌ | — | 被回复评论ID（二级回复时填） |
| `replyToUserId` | `String` | ❌ | — | 被回复用户ID |
| `replyToUserName` | `String` | ❌ | — | 被回复用户名称 |

### §C.7 GET `/community/posts/{id}/comments` — 评论列表

**Roles**: `resident`；按创建时间升序，前端按 `parentId` 组装二级回复；返回 `PageResponse<CommunityCommentResponse>`。

### §C.8 DELETE `/community/comments/{id}` — 删除自己的评论

**Roles**: `resident`（仅作者可删，否则 `98006`）

### §C.9 POST `/community/reports` — 举报帖子或评论

**Roles**: `resident`

**请求体**：

| 字段 | 类型 | 必填 | 校验 | 说明 |
| --- | --- | --- | --- | --- |
| `targetType` | `String` | ✅ | `post`\|`comment` | 举报对象类型 |
| `targetId` | `String` | ✅ | — | 举报对象ID |
| `reasonType` | `String` | ❌ | `spam`\|`abuse`\|`politics`\|`porn`\|`other` | 原因类型 |
| `reasonDetail` | `String` | ❌ | `@Size(max=500)` | 说明 |

**规则**：同一用户对同一 `(targetType,targetId)` 只能举报一次，重复返回 `98004`。

### §C.10 GET `/admin/community/posts` — 管理端帖子列表

**Roles**: 管理端工作台身份（`property_admin`/`platform_admin`/`community_admin`，或持活动组长/负责人标签的住户）+ `community:read`
**Query**：`page`、`pageSize`、`status`(可选 `published`|`hidden`|`deleted`)、`keyword`
**说明**：按当前管理员 `property_company_id` 管辖范围，含隐藏/已删除帖子。活动组长/负责人持有 `community:read` 时同样按本物业范围查看。

### §C.11 DELETE `/admin/community/posts/{id}` — 管理端删帖

**Roles**: 管理端 + `community:manage`；跨物业公司返回 `98001`（软删）。

### §C.11a GET `/admin/community/posts/{postId}/comments` — 管理端帖子评论列表（v6.6 新增）

**Auth**: Required  
**Roles**: `property_admin` / `platform_admin` + `community:read`

**说明**：管理端查看指定帖子下的评论列表，含已删除评论，供管理员选择性删除。跨物业公司校验，不可查看其他物业公司帖子的评论。

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/admin/community/posts/cp_gen001/comments?page=1&pageSize=50&status=published" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `postId` | `String` | ✅ | 帖子ID（`cp_` 前缀） |

**Query 参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `50` | 每页条数（上限 100） |
| `status` | `String` | ❌ | 不传返回全部 | 筛选：`published`（已发布）/ `deleted`（已删除） |

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "cc_gen001",
        "postId": "cp_gen001",
        "authorId": "res_001",
        "authorName": "业主-赵六",
        "authorRole": "resident",
        "content": "说得好，支持！",
        "parentId": null,
        "replyToUserId": null,
        "replyToUserName": null,
        "status": "published",
        "createdAt": "2026-07-10T12:00:00.000Z"
      },
      {
        "id": "cc_gen002",
        "postId": "cp_gen001",
        "authorId": "res_003",
        "authorName": "业主-孙八",
        "authorRole": "resident",
        "content": "这条评论已被删除",
        "parentId": null,
        "replyToUserId": null,
        "replyToUserName": null,
        "status": "deleted",
        "createdAt": "2026-07-11T08:30:00.000Z"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 50,
      "total": 2,
      "totalPages": 1
    }
  },
  "timestamp": "2026-08-23T10:00:00.000Z"
}
```

**字段说明**（`CommunityCommentResponse`）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `String` | 评论ID |
| `postId` | `String` | 所属帖子ID |
| `authorId` | `String` | 评论者ID |
| `authorName` | `String` | 评论者姓名 |
| `authorRole` | `String` | 评论者角色 |
| `content` | `String` | 评论内容 |
| `parentId` | `String` | 父评论ID（二级回复时非空） |
| `replyToUserId` | `String` | 回复目标用户ID |
| `replyToUserName` | `String` | 回复目标用户姓名 |
| `status` | `String` | 状态：`published` / `deleted` |
| `createdAt` | `String` | 创建时间（ISO-8601） |

**错误响应**：

| 错误码 | HTTP | 说明 |
| --- | --- | --- |
| `98001` | 404 | 帖子不存在或非本物业公司帖子 |
| `10003` | 400 | pageSize 超出范围 |

---

### §C.12 DELETE `/admin/community/comments/{id}` — 管理端删评论

**Roles**: 管理端 + `community:manage`

### §C.13 GET `/admin/community/reports` — 举报列表

**Roles**: 管理端 + `community:read`
**Query**：`page`、`pageSize`、`status`(可选 `pending`|`accepted`|`rejected`)；返回含 `targetContent`/`targetAuthorName` 摘要。

### §C.14 POST `/admin/community/reports/{id}/handle` — 处理举报

**Roles**: 管理端 + `community:manage`

**请求体**：

| 字段 | 类型 | 必填 | 校验 | 说明 |
| --- | --- | --- | --- | --- |
| `action` | `String` | ✅ | `accept`\|`reject` | accept=通过举报自动隐藏内容；reject=驳回 |
| `handleRemark` | `String` | ❌ | — | 处理备注 |

**规则**：`accept` → 举报单 `status=accepted`，被举报帖子置 `hidden`、评论置 `deleted`；`reject` → `status=rejected`，内容不变；已处理举报再次处理返回 `98009`。

### §C.15 POST `/admin/community/users/{residentId}/forum-ban` — 禁言用户（影子封禁）🆕 v8.14

**Roles**: `property_admin`、`platform_admin`（限本物业公司；`platform_admin` 无归属物业可跨物业）

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `residentId` | `String` | ✅ | 被禁住户ID（`res_` 前缀） |

**请求体**：

| 字段 | 类型 | 必填 | 校验 | 说明 |
| --- | --- | --- | --- | --- |
| `reason` | `String` | ❌ | ≤200 | 禁言原因（管理端留痕） |
| `expiresAt` | `String` | ❌ | ISO8601（`yyyy-MM-ddTHH:mm:ss`，须晚于当前 | 到期时间；**不传=永久禁言** |

**业务说明**：

- **影子封禁（被禁用户无感知）**：被禁用户仍可正常发帖/评论，且本人端能看到自己刚发的内容；其他住户与管理端以外的任何人均看不到其内容（查询层按 `community_posts.shadow_banned` / `community_comments.shadow_banned` 与「作者可见性」过滤：`shadow_banned=0 OR author_id=viewer`）。
- 已处于生效禁言（`active`）的用户重复禁言返回 `97050`。
- 禁言生效时计数口径：先将该作者「已计数」的评论从各帖 `comment_count` 扣回，再给其历史帖子/评论打标 `shadow_banned=true`；帖子 `comment_count` 仅统计对外可见评论，保证「评论数」与实际可见列表始终一致。
- `expiresAt` 为空=永久禁言；到期由调度器（`ForumBanExpiryScheduler`，带分布式锁）自动解除并回补计数（`status=expired`）。

**成功响应**：`ResidentForumBanItemResponse`（字段见 §C.17）

### §C.16 DELETE `/admin/community/users/{residentId}/forum-ban` — 解除禁言 🆕 v8.14

**Roles**: `property_admin`、`platform_admin`

**路径参数**：`residentId`（`res_` 前缀，必填）

**业务说明**：

- 解除禁言：先回补评论计数、取消内容打标（`shadow_banned=false`），再把禁言记录置 `lifted`；历史被禁内容恢复对外可见。
- 无生效中禁言记录时返回 `97051`。

**成功响应**：`ResidentForumBanItemResponse`

### §C.17 GET `/admin/community/forum-bans` — 禁言列表 🆕 v8.14

**Roles**: `property_admin`、`platform_admin`

**查询参数**：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| `page` | `int` | ❌ | 1 | 页码 |
| `pageSize` | `int` | ❌ | 20 | 每页条数（≤100） |
| `status` | `String` | ❌ | — | 状态过滤：`active`/`lifted`/`expired` |

**响应**：分页 `ResidentForumBanItemResponse`

`ResidentForumBanItemResponse` 字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `String` | 禁言记录ID（`rfb_`） |
| `residentId` | `String` | 被禁住户ID |
| `residentName` | `String` | 被禁住户姓名 |
| `residentPhone` | `String` | 手机号（脱敏） |
| `propertyCompanyId` | `String` | 物业公司ID |
| `reason` | `String` | 禁言原因 |
| `bannedBy` | `String` | 操作管理员住户ID |
| `bannedByName` | `String` | 操作管理员姓名 |
| `bannedAt` | `String` | 禁言开始时间(ISO8601) |
| `expiresAt` | `String` | 到期时间(ISO8601)；`null`=永久 |
| `status` | `String` | `active`/`lifted`/`expired` |

> **C 端联动（影子封禁对前端的影响）**：住户在「社区」发帖/评论时，若处于生效禁言，**接口照常返回成功、本人端能看到自己刚发的内容**，但其他住户在帖子列表/详情/评论列表均看不到（后端按可见性过滤）；帖子 `comment_count` 只统计对外可见评论。前端**无需**改造发帖/评论交互，也**不要**提示「您已被禁言」——被禁用户应无感知。管理端在帖子/评论列表（`adminView=true`）可看到 `shadowBanned` 标记以区分正常内容与禁言内容。

### 错误码 98xxx

| 错误码 | HTTP | 含义 | 触发场景 |
| --- | --- | --- | --- |
| `98001` | 404 | 帖子不存在 | 帖子ID无效或非 published |
| `98002` | 404 | 评论不存在 | 评论ID无效 |
| `98003` | 404 | 举报记录不存在 | 举报ID无效 |
| `98004` | 409 | 已举报过该内容 | 同一用户对同一 target 重复举报 |
| `98005` | 400 | 内容包含敏感词 | 发帖/评论命中 `sensitive_words` |
| `98006` | 403 | 无权删除该内容 | 非作者删除帖子/评论 |
| `98007` | 400 | 帖子内容过长 | content > 2000 |
| `98008` | 400 | 评论内容过长 | content > 1000 |
| `98009` | 409 | 该举报已处理 | 重复处理举报 |

# 错误码速查表

> ⚠️ **本速查表为历史整理稿，其中约 60 个码在后端从未实现或已迁移。冲突时一律以文末[「附录二 · 错误码权威总表」](#附录二--错误码权威总表以-errorcodejava-为准2026-11-03-全量核对)为准**，迁移关系见[「附录三 · 失效错误码对照」](#附录三--失效错误码对照历史文档标注--现行实现)。
> 前端如需硬编码错误码，请只从附录二取值（该表由 `ErrorCode.java` 逐条生成）。



## 通用/系统错误 (10xxx)


| 错误码     | HTTP | 含义      | 触发场景           |
| ------- | ---- | ------- | -------------- |
| `10001` | 400  | 参数缺失    | 必填字段为空         |
| `10002` | 400  | 参数格式错误  | 日期格式不合法等       |
| `10003` | 400  | 参数值超出范围 | pageSize超过100等 |




## 鉴权/权限错误 (20xxx)


| 错误码     | HTTP | 含义      | 触发场景              |
| ------- | ---- | ------- | ----------------- |
| `20001` | 401  | Token缺失 | 未携带 Authorization |
| `20002` | 401  | Token过期 | JWT 过期            |
| `20003` | 401  | Token无效 | JWT 解析失败          |
| `20004` | 403  | 权限不足    | 当前角色无操作权限         |
| `20005` | 403  | 账号被禁用   | 用户被冻结             |




## 业主/住户错误 (30xxx)


| 错误码     | HTTP | 含义            | 触发场景         |
| ------- | ---- | ------------- | ------------ |
| `30001` | 404  | 业主不存在         | 用户ID查不到      |
| `30002` | 409  | 家庭人数超限        | 家庭成员超过15人    |
| `30003` | 400  | 微信授权失败        | 微信绑定授权被拒绝    |
| `30004` | 409  | 重复挂接          | 该商家已在当前物业下挂接 |
| `30005` | 409  | 手机号重复         | 该手机号在当前物业公司下已存在（§2.2） |
| `30006` | 409  | 存在未完成的订单（注销）  | 注销前有未完成订单    |
| `30007` | 409  | 存在未缴纳的物业费（注销） | 注销前有未缴纳物业费   |
| `30008` | 409  | 物业币余额不为0（注销）  | 注销前需清空物业币    |
| `30009` | 409  | 户主家庭有其他成员（注销） | 户主需先移除其他成员   |




## 积分错误 (40xxx)


| 错误码     | HTTP | 含义      | 触发场景      |
| ------- | ---- | ------- | --------- |
| `40001` | 400  | 积分余额不足  | 积分不够兑换/使用 |
| `40002` | 404  | 积分规则不存在 | 积分配置ID无效  |
| `97070` | 400  | 本小区未启用积分功能 | `pointEnabled=false` 时写接口（支付抵扣/转物业/购分/赠分/建设积分等）被服务端拒绝 |




## 物业币错误 (50xxx)


| 错误码     | HTTP | 含义         | 触发场景    |
| ------- | ---- | ---------- | ------- |
| `50001` | 400  | 物业币余额不足    | 物业币不够支付 |
| `50002` | 403  | 积分与物业币不可互兑 | 尝试互兑操作  |
| `50003` | 409  | 未开启物业币抵扣   | 物业币商城规则关闭时尝试抵扣（§8.0 / §69） |
| `50004` | 409  | 未达最低使用金额   | 订单金额未达物业币最低消费门槛 |
| `50005` | 409  | 超过最大抵扣比例   | 抵扣金额超过规则允许的最大比例 |
| `97071` | 400  | 本小区未启用物业币功能 | `coinEnabled=false` 时写接口（充值/兑换/支付抵扣/转物业等）被服务端拒绝 |




## 商家错误 (60xxx)


| 错误码     | HTTP | 含义             | 触发场景                              |
| ------- | ---- | -------------- | --------------------------------- |
| `60001` | 404  | 商家不存在          | 商家ID无效                            |
| `60002` | 409  | 商家入驻审核中        | 商家状态不允许操作                         |
| `60003` | 409  | 审核已处理          | 重复审核                              |
| `60004` | 404  | 商品不存在          | 商品ID无效                            |
| `60005` | 409  | 商家已被踢出         | 商家被物业踢出                           |
| `60006` | 404  | 商家在该物业下未完成挂接配置 | `merchant_property_configs` 无对应记录 |
| `60011` | 400  | 广告额度不足 | 免费额度用尽仍选 `free` 投放（§59.2） |
| `60012` | 404  | 广告不存在 | 微信回调/超时收尾时 `merchant_ads` 无对应记录 |
| `60030` | 403  | 该商家未开通一级代理权限 | 建货源商品 / 选品池调用时商家 `is_distributor=0`（§60.12、§60.5） |
| `60037` | 400  | 仅审核通过的商家可管理一级代理权限 | 开通/取消一级代理、上架货源商品时商家 `audit_status != approved`（§60.12，2026-10-16 新增，从 60030 拆出） |
| `60031` | 400  | 必须指定一级代理商家（distributorMerchantId） | `POST /admin/distributor-products` 未传 `distributorMerchantId`（§60.12） |
| `60032` | 400  | 拿货件数必须为不小于1的整数 | `POST /resident-merchants/listings` 的 `quantity` < 1（§60.6） |
| `60033` | 400  | 一级代理货源库存不足 | 拿货件数 > 货源剩余库存件数（§60.6） |
| `60034` | 400  | 该上架存在未完成订单，不可取消 | `DELETE /resident-merchants/listings/{id}` 时有未完成订单（§60.6.1） |
| `60035` | 400  | 可售件数不足 | 下单件数 > listing 可售件数 `quantityRemaining`（§60.10.1、§8.0.0） |
| `60036` | 404  | 业主小店店铺主体不存在 | 下单/试算时未解析到小店店铺主体（§60.10.1、§60.14） |




## 订单错误 (70xxx)


| 错误码     | HTTP | 含义          | 触发场景         |
| ------- | ---- | ----------- | ------------ |
| `70001` | 404  | 订单不存在       | 订单ID无效       |
| `70002` | 409  | 订单状态不允许操作   | 已完成订单不可取消等   |
| `70003` | 409  | 并发冲突        | 同一订单多人同时支付   |
| `70004` | 409  | 当前订单不支持用户主动退款 | 如有问题请联系客服处理 |
| `70021` | 400  | 已超过履约选择时限 | 无法再选择履约方式（>30 分钟未选） |
| `70022` | 400  | 本单无需配送 | 团购单 / 社区食堂单（`fulfillment_mode=none`）不可选择履约方式 |
| `70023` | 400  | 请先选择履约方式 | 预留：发布到抢单大厅或商家自配前未选择 |
| `70024` | 400  | 配送未完成 | 预留：不可完成订单 |
| `70025` | 400  | 履约方式已锁定 | 已选定 `merchant_self` / `courier_hall` 后再次提交**其它**方式 |
| `71001` | 404  | 物业费账单不存在    | 账单ID无效       |
| `71002` | 409  | 该账期已存在物业费账单 | 同住户同账期不可重复创建 |
| `99010` | 409  | 商家核实时限已过 | 订单因超时未核实，系统已自动通过 |
| `99011` | 409  | 订单尚未通过商家核实 | 冻结期结算时订单状态异常 |
| `99012` | 409  | 冻结期已过不可申请退货 | 冻结期满后不可退款 |
| `99013` | 409  | 订单因商家超时未核实已自动通过 | 超时自动通过 |




## 配送错误 (80xxx)


| 错误码     | HTTP | 含义      | 触发场景        |
| ------- | ---- | ------- | ----------- |
| `80001` | 409  | 快递员抢单失败 | 订单已被其他快递员抢走 |
| `80002` | 404  | 配送规则不存在 | 规则ID无效      |
| `80003` | 409  | 配送规则重复  | 该物业已存在配送规则  |
| `80004` | 404  | 快递负责人不存在 | 快递负责人 ID 无效（§62） |
| `80005` | 409  | 快递负责人重复 | 该住户已是快递负责人 |
| `80006` | 409  | 快递员已绑定 | 该快递员已绑定其它快递负责人 |
| `80007` | 404  | 配送价格区间不存在 | 区间 ID 无效 |
| `80008` | 409  | 配送价格区间重叠 | 与现有 active 区间闭区间重叠 |
| `80009` | 400  | 配送价格区间参数非法 | `min`/`max` 不合法等 |
| `80010` | 404  | 配送价格区间不存在 | 区间 ID 无效（与 §62 同名码，以本表为准） |
| `80020` | 400  | 非配送员承运单 | 商家自配单（`carrier_type=merchant`），配送员不可抢单/改状态/完成/被指派 |
| `80021` | 404  | 配送单不存在 | 配送单 ID 无效 |

> 2026-09-09：原文档把「非配送员承运单」写作 `80010`，与既有的 `DELIVERY_PRICE_RANGE_NOT_FOUND`
> 冲突，实现统一使用 **`80020`**。




## 社区食堂错误 (80xxx)

| 错误码 | HTTP | 含义 | 触发场景 |
| --- | --- | --- | --- |
| `80021` | 400 | 非社区食堂主商家 | 非主商家调用购买额度/定向充值（§73.9/§73.11）；或绑定子商家时主商家未标记（§73.2） |
| `80022` | 409 | 绑定关系已存在 | 该子商家已被该主商家绑定（§73.2） |
| `80023` | 404 | 绑定关系不存在 | 解绑/调比例时绑定 ID 无效（§73.3/§73.4） |
| `80024` | 400 | 充值额度余额不足 | 主商家额度不足以充值（§73.11） |
| `80025` | 400 | 定向余额不足 | 消费抵扣超过用户定向余额（§8.2/§73.14） |
| `80026` | 400 | 商家不在所选主商家体系内 | 定向余额抵扣越权（§8.2/§73.14） |
| `80027` | 400 | 未选择主商家体系 | 使用了定向余额但未传 `mainMerchantId`（保留位） |
| `80028` | 400 | 存在待支付的额度购买 | 主商家已有 `pending` 购买记录（§73.9） |
| `80029` | 404 | 额度购买记录不存在 | 支付回调找不到对应购买记录 |

## 公告/消息/活动错误 (90xxx)


| 错误码     | HTTP | 含义            | 触发场景               |
| ------- | ---- | ------------- | ------------------ |
| `90001` | 404  | 公告不存在         | 公告ID无效             |
| `90002` | 409  | 投票已结束         | 意见箱投票截止            |
| `90003` | 409  | 已提交过收集信息      | 重复提交               |
| `90004` | 404  | 意见箱不存在        | 意见箱ID无效            |
| `90005` | 409  | 该家庭已投过票       | 一户一票限制             |
| `90006` | 409  | 每家庭每月仅可发表1条意见 | 月度限制               |
| `90007` | 404  | 活动组不存在        | 活动组ID无效            |
| `90008` | 409  | 已订阅该活动组       | 重复订阅               |
| `90009` | 409  | 已是活动组成员       | 当前为 active 成员时重复加入 |
| `90010` | 409  | 不是活动组成员       | 尝试退出非成员活动组         |
| `90011` | 409  | 组长不能退出活动组     | 组长限制               |
| `90012` | 404  | 消息不存在         | 消息ID无效             |
| `90013` | 409  | 今日邻居私聊次数已达上限  | 每日限制               |
| `90014` | 409  | 未订阅该活动组       | 取消订阅时无有效期内的订阅记录     |




## 二期业务错误 (901xx)


| 错误码     | HTTP | 含义                    | 触发场景                 |
| ------- | ---- | --------------------- | -------------------- |
| `429001` | 429  | 登录请求过于频繁              | 同 IP 5 分钟内登录超 10 次（见 §0.10） |
| `90101` | 429  | 定向消息接收用户当日已达上限        | 同人每日 3 条             |
| `90102` | 429  | 物业公司当日定向任务已达上限        | 同物业每日 10 条任务         |
| `90103` | 429  | 今日新聊邻居人数已达上限          | 每日 3 个不同人             |
| `90104` | 409  | 服务需求商家响应已超时            | 超过 5 分钟未接单           |
| `90105` | 409  | 商家广告本周额度不足             | 免费+付费额度用尽            |
| `90106` | 409  | 业主商户保证金或状态不允许          | 未缴保证金等               |
| `90107` | 409  | 业主商户存在进行中订单无法退出        | 退出申请                 |
| `90108` | 409  | 服务需求无可用商家已关闭           | 队列耗尽                 |
| `90109` | 403  | 物业管理员超出管辖范围            | 楼栋/小区不在 scope       |
| `90110` | 409  | 咨询预约不可用                 | 咨询师关闭预约              |
| `90111` | 404  | 业主商户未对外展示              | 店铺 private 或非营业中      |
| `90120` | 409  | 该账号存在关联业务数据              | 无法彻底删除，请改用 §31.2.3 禁用 / §31.2.4 软删除接口 |




## 统筹/板块/个体负责人错误 (91xxx)


| 错误码     | HTTP | 含义       | 触发场景   |
| ------- | ---- | -------- | ------ |
| `91001` | 404  | 统筹不存在    | 统筹ID无效 |
| `91002` | 404  | 板块负责人不存在 | ID无效   |
| `91003` | 404  | 个体负责人不存在 | ID无效   |
| `91004` | 404  | 楼长不存在 | 楼长ID无效 |
| `91005` | 404  | 物业经理不存在 | 经理ID无效 / 当前住户不是在职经理（§100） |
| `91006` | 400  | 该物业公司已存在在职物业经理 | 同一物业公司重复指派 active 经理（§100） |
| `91007` | 403  | 物业经理身份已停用 | 停用经理访问工作台（§100） |
| `91008` | 403  | 物业经理不可修改自己的分成比例 | 经理本人调用更新接口改自己 `commissionRate`（§100） |




## 权限错误 (92xxx)


| 错误码     | HTTP | 含义         | 触发场景   |
| ------- | ---- | ---------- | ------ |
| `92001` | 404  | 权限项不存在     | 权限ID无效 |
| `92002` | 404  | 角色预设不存在    | 预设ID无效 |
| `92003` | 500  | 权限变更日志写入失败 | 系统错误   |




## 积分池/物业币控制错误 (93xxx)


| 错误码     | HTTP | 含义      | 触发场景      |
| ------- | ---- | ------- | --------- |
| `93001` | 404  | 积分池不存在  | 积分池未初始化   |
| `93002` | 400  | 积分池余额不足 | 积分池资金不够   |
| `93003` | 403  | 物业币已冻结  | 冻结状态下无法使用 |
| `93004` | 400  | 物业币已过期  | 过期物业币无法使用 |




## 商家积分/提现错误 (94xxx)


| 错误码     | HTTP | 含义        | 触发场景    |
| ------- | ---- | --------- | ------- |
| `94001` | 409  | 积分购买审核中   | 审核中不可操作 |
| `94002` | 400  | 商家积分余额不足  | 可用积分不够  |
| `94003` | 409  | 提现申请不允许操作 | 状态不允许   |
| `94004` | 400  | 兑换金额不合法   | 金额不满足条件 |
| `94005` | 400  | 提现已被冻结 | 住户/商家 `withdrawalBlocked=true` |
| `94006` | 400  | 可提现余额不足 | 提现申请/审核金额超过可提现余额；message 带上限值 |




## 文件/服务/物业公司错误 (95xxx-97xxx)


| 错误码     | HTTP | 含义      | 触发场景     |
| ------- | ---- | ------- | -------- |
| `95001` | 404  | 文件不存在   | 文件ID无效   |
| `95002` | 413  | 文件超过服务端大小限制 | 上传体积超过 `spring.servlet.multipart` 上限（注意：nginx `client_max_body_size` 更早拦截，那种 413 是 nginx 的 HTML，不含本码） |
| `96001` | 404  | 服务不存在   | 服务ID无效   |
| `96002` | 404  | 服务订单不存在 | 服务订单ID无效 |
| `96003` | 409  | 已订阅该服务  | 重复订阅     |
| `97001` | 404  | 物业公司不存在 | 物业公司ID无效 |
| `97002` | 400  | 分成比例合计必须为100% | 业主/商家/物业币/共享积分分成比例合计≠1.0 |
| `97003` | 400  | 未配置微信催缴模板 | 无法发送微信提醒 |
| `97004` | 400  | 预览已过期 | 需重新预览后再发送 |
| `97005` | 409  | 重复发送催缴 | `requestId` 已处理 |
| `97017` | 500  | 催缴消息落库/审计写入失败 | 消息/审计表写入（DataAccessException）兜底，返回明确错误码而非「数据访问失败」（v7.0 从 97011 拆出） |
| `97070` | 400  | 积分功能未开启 | `property_companies.point_enabled=false` 时调用积分写接口（如 §2.6.4.1 转给物业）（2026-09-10 登记） |
| `97071` | 400  | 物业币功能未开启 | `property_companies.coin_enabled=false` 时调用物业币写接口（如 §2.6.4.2 转给物业）（2026-09-10 登记） |
| `97050` | 400  | 该用户已被禁言，无需重复操作 | `POST /admin/community/users/{residentId}/forum-ban` 命中已 `active` 禁言记录（§C.15，v8.14 新增） |
| `97051` | 404  | 禁言记录不存在 | `DELETE /admin/community/users/{residentId}/forum-ban` 无生效中禁言记录（§C.16，v8.14 新增） |
| `97060` | 404  | 组长申请不存在 | `POST /admin/activity-leader-applications/{id}/audit` 的 `applicationId` 无效（§101.2，v8.14 新增） |
| `97061` | 400  | 该申请已审核，不可重复审核 | 对非 `pending` 组长申请再次调用审核接口（§101.2，v8.14 新增） |
| `97062` | 400  | 您已有待审核的组长申请或已是组长，请勿重复提交 | `POST /activity-leader-applications/apply` 同物业已有 `pending` 或已是组长（§18.4.1，v8.14 新增） |
| `97063` | 404  | 该用户没有生效中的组长身份 | `POST /admin/activity-leaders/{residentId}/revoke` 无 `active` 组长身份（§101.3，v8.14 新增） |



## 分账/配送结算错误 (970xx / 920xx) — distribution_b_v2

> ⚠️ 本表下半段的 `97007`~`97017` 大多**从未实现**：真实实现落在 `920xx`（`92030`~`92035`）与 `96033`。迁移关系见[附录三](#附录三--失效错误码对照历史文档标注--现行实现)，前端硬编码请只取附录二的码。
> 🆕 V36 起计算版本为 `distribution_b_v2`（抽佣基础额先扣用户积分/奖励金两档），公式与守恒式见 §71.0。


| 错误码     | HTTP | 含义             | 触发场景                 |
| ------- | ---- | -------------- | -------------------- |
| `97006` | 400  | 商品平台盘一级比例合计必须为100% | `platformShareRate + propertyShareRate + coordinatorShareRate ≠ 1.0000`。**2026-09-08 起已实现**：`PATCH/PUT /admin/property-companies/{id}/config`、`PUT /property-companies/{id}`、`PUT /admin/platform-share-rates` 统一命中（容差 0.0001） |
| `97007` | 400  | 配送费抽成比例不合法        | `platformDeliveryShareRate ∉ [0,1]` |
| `97008` | 400  | 满额门槛必须大于0         | 启用满额减免时 `freeDeliveryThreshold ≤ 0` |
| `97009` | 409  | 订单金额快照已失效        | 下单金额与预览不一致，请重新预览 |
| `97010` | 409  | 订单已经完成分账         | 重复结算同一订单分账 |
| `97011` | 409  | 配送已经完成结算         | 重复结算同一配送单 |
| `97012` | 400  | 配送补贴承担方不合法        | `deliverySubsidySponsor` 非 `none/merchant/property/platform` |
| `97013` | 409  | 商家预计收入不足以覆盖配送补贴 | 满额门槛配置导致商家实得 < 0 |
| `97014` | 500  | 分账金额不守恒          | 结算前守恒校验失败（规范§11.3） |
| `97015` | 409  | 退款冲正已处理          | 重复冲正（幂等） |
| `97016` | 400  | 分账比例不合法         | 分账比例超出 [0,1] |
| `97030` | 404  | 个体负责人申请不存在     | `applicationId` 查不到记录（§37.6，2026-09-08 新增） |
| `97031` | 400  | 该申请已审核，不可重复审核 | 对非 `pending` 申请再次调用审核接口（§37.6，2026-09-08 新增） |
| `97040` | 404  | 一级代理申请不存在     | `applicationId` 查不到记录（§60.16.4，2026-10-20 新增） |
| `97041` | 400  | 一级代理申请已审核，不可重复审核 | 对非 `pending` 申请再次调用审核接口（§60.16.4，2026-10-20 新增） |
| `97042` | 400  | 你已有待审核的一级代理申请，请勿重复提交 | `POST /distributors/apply` 同一用户同物业已有 `pending`（§60.16.1） |
| `97043` | 400  | 你已是一级代理，无需重复申请 | `POST /distributors/apply` 名下已有 `is_distributor=1` 且 `approved` 载体（§60.16.1） |
| `97033` | 400  | 物业公司存在关联数据，不可删除 | `DELETE /admin/property-companies/{id}` 命中小区/住户/商家/订单关联（§94.14，2026-09-08 新增） |




## 服务/社区错误 (17xxx)


| 错误码     | HTTP | 含义    | 触发场景        |
| ------- | ---- | ----- | ----------- |
| `17001` | 400  | 服务已下架 | 社区服务下架后不可操作 |




## 社区论坛错误 (98xxx)


| 错误码     | HTTP | 含义         | 触发场景   |
| ------- | ---- | ---------- | ------ |
| `98001` | 404  | 帖子不存在      | 帖子ID无效 |
| `98002` | 404  | 评论不存在      | 评论ID无效 |
| `98003` | 404  | 举报记录不存在    | 举报ID无效 |
| `98004` | 409  | 您已举报过该内容   | 重复举报   |
| `98005` | 400  | 内容包含敏感词    | 请修改后重试 |
| `98006` | 403  | 无权删除该内容    | 非本人/管理员 |
| `98007` | 400  | 帖子内容过长     | 超出字数限制 |
| `98008` | 400  | 评论内容过长     | 超出字数限制 |
| `98009` | 409  | 该举报已处理     | 重复处理   |




## 系统错误 (99xxx)


| 错误码     | HTTP | 含义     | 触发场景 |
| ------- | ---- | ------ | ---- |
| `99001` | 500  | 内部服务异常 | 未知错误 |
| `99002` | 502  | 微信支付接口异常 | 预下单/查询/退款失败，或未返回调起参数 paySign/package |
| `99003` | 502  | 微信支付签名校验失败 | 支付通知验签失败 |
| `99004` | 503  | 微信支付未配置 | `WECHAT_PAY_ENABLED` 未开启或 mch_id/api-v3-key/商户证书序列号/私钥路径/notify_url 配置缺失 |


---



# 业务流程说明



## 订单完整流程

```
┌─────────────┐
│  创建订单    │  POST /orders
│  status:     │  → pending
│  pending     │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  支付订单    │  POST /orders/{id}/pay
│  status:     │  → paid
│  paid        │  获得积分 + 物业币返现
└──────┬──────┘
       │
       ├──────────────────────────────┬─────────────────────────────┐
       ▼                              ▼                             ▼
┌─────────────┐              ┌──────────────┐              ┌──────────────────┐
│ 需配送订单    │              │ 团购/食堂单    │              │ （配送中不回答钱的问题）  │
│ 商家选履约方式  │              │ 无需配送       │              │ 资金态只在结算Tab展示   │
│ merchant_self│              │ pending_      │              └──────────────────┘
│ /courier_hall│              │ verification  │
└──────┬──────┘              └──────┬───────┘
       │                            │
       ▼                            ▼
┌─────────────┐              ┌──────────────┐
│ 配送中        │              │ 商家核实通过    │  POST /orders/{id}/verify
│ delivering   │              │ verified     │  → 冻结期（freezeEndDate）
└──────┬──────┘              └──────┬───────┘
       │                            │
       ▼                            │
┌─────────────┐                     │
│ 配送员完成配送  │ POST /deliveries/{id}/complete        │
│ 配送单 completed│ （订单仍 delivering + deliveredAt）      │
│ 或商家确认自配送达│ POST /deliveries/{id}/merchant-confirm │
│ （商家自配：订单直接 completed）│                            │
└──────┬──────┘                     │
       │                            │
       ▼                            ▼
┌─────────────────────────────────────────┐
│  订单完成 completed（资金终态）                │
│  ① 居民确认收货 POST /orders/{id}/confirm    │
│  ② 超期自动确认（售后窗口期，定时任务）             │
│  ③ 商家手动完成 PATCH /orders/{id}/status    │
│  ④ 团购冻结期到期自动完成                      │
│  → 生成分成记录 + 触发微信分账冻结               │
│  → 售后窗口到期完结划账（解冻）→ 可提现           │
└─────────────────────────────────────────┘
```

> **v8.12 核心口径**：**配送完成 ≠ 订单完成**。配送单状态（`deliveries.status`）与订单业务状态
> （`orders.order_status`）是两套独立状态机；订单列表/详情只回答「货送到哪了」，
> 资金状态（冻结中/已解冻）只在商家「结算」Tab 与配送员「到账结算」界面回答。
> `completed` 一旦落定即触发分账冻结且不再支持线上退货退款，因此平台配送单必须保留
> 「送达 → 居民确认收货」缓冲窗口（超期由定时任务自动确认兜底）。



## 四级分成流程

```
商品金额 A × 商家抽佣比例 R = 抽佣基础额
                     │
    ┌────────────────┼────────────────┐
    │                │                │
    ▼                ▼                ▼
公司平台盘      物业平台盘       积分成本（物业币不作为成本）
platformShare   propertyShare   （从抽佣基础额扣除，最低为0）
    │                │
    │         ┌──────┴──────┐
    │         │             │
    │     管理盘 managementPoolAmount
    │         │  板块毛额 = 管理盘 × 60%
    │         │  ├─ 板块负责人 30%（板块毛额 − 个体负责人）
    │         │  └─ 个体负责人 30%（板块毛额 × 50%）
    │         │  统筹 40%（管理盘 − 板块毛额）
    │
    └──→ 配送分账独立：基数 = 实付配送费 + 补贴
         公司配送收入 + 配送员收入 = 基数
```

> 当前有效比例：统筹 40% ：板块负责人 30% ：个体负责人 30%。
> `sectorLeaderRate = 0.6000`、`individualLeaderRate = 0.5000` 是**级联中间值**，不是最终有效比例，运营配置时须按最终有效比例（40/30/30）预览。



## 社区服务预约/订阅流程

```
弹窗填写：地址、电话、服务时间、支付方式（+ mixed 时的 pointUsed/coinUsed）
        │
        ├─ isSubscription = false  →  POST /services/{id}/book
        │                              （一步完成预约 + 扣款，返回 so_ 订单）
        │
        └─ isSubscription = true   →  POST /services/{id}/subscribe
                                       （一步完成订阅 + 扣款，返回 ss_ 订阅记录）

⚠️ 不存在 POST /service-orders/{id}/pay，勿套用商品订单「下单 → pay」两段式流程。
预约/订阅成功后可用 GET /service-orders 或 GET /services/my-subscriptions 查询记录。
```



## 活动组加入/订阅流程

```
                    ┌─────────────────────────┐
                    │  GET /activity-groups/{id} │
                    │  查看 monthlyFee/yearlyFee   │
                    └─────────────┬───────────────┘
                                  │
              ┌───────────────────┴───────────────────┐
              │                                       │
        免费组（费用=0）                          付费组（费用>0）
              │                                       │
              ▼                                       ▼
    POST /activity-groups/{id}/join          POST /activity-groups/{id}/subscribe
    （直接加入）                              （传 subType + paymentMethod）
              │                                       │
              │                              订阅成功自动加入成员
              │                                       │
              └───────────────────┬───────────────────┘
                                  │
                    可选：POST /activity-groups/{id}/leave
                    （软删除，status=inactive）
                                  │
                    再次 join / subscribe 可重新激活成员记录
```



## 商家挂接与订单支付前置条件

商家在物业公司下开展业务前，须在 `merchant_property_configs` 表中完成挂接配置（抽佣比例、积分兑换比、物业币返现等）。


| 阶段                           | 校验     | 缺失时错误码  |
| ---------------------------- | ------ | ------- |
| 创建订单 `POST /orders`          | 校验挂接配置 | `60006` |
| 支付订单 `POST /orders/{id}/pay` | 校验挂接配置 | `60006` |


测试环境需确保 demo 商家（如 `mch_demo002`）在 `pc_demo001` 下有对应 `merchant_property_configs` 记录。

> **2026-09-06 修复**：自动建店链路（任命活动组长/技工 `POST /admin/role-accounts`、统筹工作台添加商家）现已同步创建 `merchant_property_configs`（默认 `commission_rate=0.1`、`point_exchange_rate=100`）；历史缺配置商家由 `sql/2026-09-06_frontend_issues_fix.sql` 幂等补齐，业主购买组长店商品不再返回 `60006`。

---

## 微信支付流程与支付流水（新增）

> 全系统五类业务（商品订单、服务预约、服务订阅、活动组订阅、物业币充值、业主商户保证金）统一接入**微信支付（普通直连商户号，JSAPI 小程序支付）**。本节点明统一下单与确认链路。
> **支付通道切换说明（2026-08-24）**：原扫呗（利楚 / LCSW）通道已停用；业务 REST 路径与 `payParams` 前端契约保持不变，前端无需重发版。分账走微信普通分账（逐接收方 orders 分账 + 售后窗口后整笔 unfreeze 解冻一次，见 §90）。

### 一、统一下单流程（业务接口一步触发，两段式）

```
业务接口：POST /orders/{id}/pay、/services/{id}/book、/services/{id}/subscribe、
          /activity-groups/{id}/subscribe、/property-coins/recharge、/resident-merchants/deposit/pay
        │  paymentMethod = wechat / mixed（含现金部分）
        ▼
① 落支付流水 payment_records（status=pending，REQUIRES_NEW 独立提交，不占业务事务连接）
        ▼
② 微信支付 APIv3 预下单 —— 普通直连 JSAPI（POST /v3/pay/transactions/jsapi，HTTP 在事务外执行）
        │   成功 → 回写 out_trade_no，返回 payParams（wx.requestPayment 参数）
        │   失败 → 抛 99002「微信支付接口异常」，流水保持 pending，由调度 2h 关单
        ▼
③ 响应 data：{ ..., paymentRecordId, payParams }
        ▼ 前端
wx.requestPayment(payParams) → 用户支付 → 微信支付成功通知（回调）
```

### 二、支付确认（异步，回调 3 秒内应答）

| 环节 | 说明 |
| --- | --- |
| **支付成功通知（回调）** | 验签（微信支付 APIv3 平台证书 / 签名）+ AES-256-GCM 解密 + 金额防篡改（回调金额与本地 `cash_amount` 一致，不一致拒绝入账），仅将流水置 `paid` 并写 Outbox（`payment_settle_outbox`，同事务），随即按微信支付格式应答 `{code:"SUCCESS"}`，不阻塞业务 |
| **异步 finalize** | Outbox 事件（事务提交后）+ 调度兜底双重触发；各业务 `complete*ChannelPayment` **幂等**：订单置 `paid`（进入配送流程）、服务预约置 `confirmed`、服务订阅置 `active`、活动组订阅置 `active` 并自动入组、物业币充值到账、冻结分账（微信普通分账） |
| **调度兜底** | ① 每 10s 扫描 PENDING/FAILED Outbox 重试（指数退避，超 10 次转 `FAILED_PERMANENT` 人工对账）；② 每 30s 对下单超 30s 的 pending 流水主动查单（微信支付查单接口），已支付补标 paid + finalize，超 2h 未付关单；③ **Outbox 卡死回收**：PROCESSING 超 5 分钟视为 JVM 崩溃遗留，重置 PENDING 重新入队 |

### 三、payment_records 字段（支付流水）

| 字段 | 说明 |
| --- | --- |
| `id` | `pr_` + 16 位 hex（雪花完整 64 位，不再截断；历史短 ID 无冲突风险） |
| `biz_type` | `order`/`property_fee`/`coin_recharge`/`service_order`/`service_subscription`/`activity_subscription`/`resident_shop_deposit`/`quota_purchase`/`ad_purchase` |
| `biz_id` | 业务单ID（订单/预约单/订阅ID/充值单） |
| `resident_id` / `property_company_id` | 支付人 |
| `payment_method` | `wechat`/`point`/`property_coin`/`mixed` |
| `amount` / `point_used` / `coin_used` / `cash_amount` | 应付总额 / 积分 / 物业币 / 现金（分） |
| `status` | `pending`（待支付）→ `paid`（已支付）→ `refunding`/`refunded`/`closed`（关单） |
| `out_trade_no` | 商户订单号（微信支付预下单成功后有值；NULL = 预下单未成功/旧版残留） |
| `trace_no` | 终端流水号（= `id`） |
| `channel_trade_no` / `channel_order_no` | 微信支付流水号 `transaction_id`（回调回填） |
| `error_msg` / `notify_body` | 关单原因 / 回调原文或 `[reconciled-by-scheduler]` |
| `paid_at` | 支付时间 |

### 四、前端对接要点

| 项 | 要求 |
| --- | --- |
| `openid` | `wechat` 支付请求体可传 `openid`；不传后端取用户绑定 openid。须与当前小程序 AppID `wxf7230afd38275659` 匹配，否则微信判无效。用户未绑微信时须显式传，否则 `10002`（openid不能为空） |
| `payParams` | 响应含 `payParams`（`appId/timeStamp/nonceStr/package/signType/paySign/outTradeNo`）→ 原样 `wx.requestPayment`；缺 `paySign`/`package` 后端报 `99002`。`signType` 固定 `RSA`，`package=prepay_id=...` |
| 状态轮询 | 商品订单 `GET /orders/{id}` → `paid`；服务预约 `GET /service-orders/{id}` → `confirmed`；服务订阅/活动组订阅 `GET /services/my-subscriptions`、`GET /activity-groups/{id}/subscription-status` → `active` |
| 兜底按钮 | 60s 未确认可提供「我已支付」→ 轮询一次状态即可；后台调度 30s 内自动补齐 |
| 排错 | 前端「数据访问失败」为**全局兜底文案**，真实原因看后端日志。**催缴发送接口已不再返回该文案**：`POST /admin/property-fees/arrears-reminder` 落库/审计失败统一返回 `97017 ARREARS_REMINDER_PERSIST_FAILED`（见 §15.6 / §15.7）。若仍看到「数据访问失败」500，多为支付链路（`partner/jsapi` 相关 `status=... resp=...`）；催缴侧请确认后端 `consumePreview`、`savePreview`、催缴发送、`recordAudit`、物业聊天 insert 日志，必要时按 §15.6 的 97017 排查。 |

### 五、支付相关错误码

| 错误码 | HTTP | 说明 |
| --- | --- | --- |
| `10002` | 400 | 参数无效（`paymentMethod` 错误、`openid` 缺失等） |
| `99002` | 502 | 微信支付接口异常（预下单/查询/退款失败，或未返回调起参数 paySign/package） |
| `99003` | 502 | 微信支付签名校验失败（支付通知验签失败） |
| `99004` | 503 | 微信支付未配置（`WECHAT_PAY_ENABLED` 未开或 mch_id/api-v3-key/商户证书序列号/私钥路径/notify_url 缺失） |

---



# WebSocket 接口



## 连接地址

```
wss://test.yuanxusoftware.top/api/v1/ws?token=<accessToken>
```

备用端点（等价）：

```
wss://test.yuanxusoftware.top/api/v1/ws/v1?token=<accessToken>
```

> **注意**：WebSocket 端点受 Spring Boot `context-path=/api/v1` 影响，**勿使用**无 context-path 的根路径（将返回 404）。SockJS 探测：`GET https://test.yuanxusoftware.top/api/v1/ws/info`

### 鉴权

| 项 | 说明 |
| --- | --- |
| Token 传递 | Query 参数 `token=<accessToken>`，须为登录获得的 **accessToken**（非 refreshToken） |
| Header | **不要**传 `Authorization: Bearer ...`；WebSocket 升级请求无标准 Bearer 鉴权 |
| 编码 | 建议对 token 做 `encodeURIComponent`，避免 JWT 中 `+`/`/` 等字符破坏 URL |
| 服务端排除路径 | `/ws`、`/ws/**`、`/ws/v1`、`/ws/v1/**` 已排除 HTTP `AuthInterceptor` |
| 握手后鉴权 | STOMP `CONNECT` 帧由 `WebSocketAuthInterceptor` 校验 Query `token` |

**常见错误**：

| 现象 | 可能原因 |
| --- | --- |
| HTTP 400 / `20001` TOKEN_MISSING | 未传 `token` 或仍被 HTTP 拦截器误拦（已修复后者） |
| HTTP 200 + `Welcome to SockJS!` | 网关未做 WebSocket Upgrade，见下方「部署与排错」 |
| 1006 abnormal closure | 握手未升级为 WebSocket（多为 Nginx/网关配置） |

### 部署与排错

**Nginx 反向代理示例**（须转发 Upgrade 头，握手成功返回 HTTP **101**）：

```nginx
location /api/v1/ws {
    proxy_pass http://backend_upstream;
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "upgrade";
    proxy_set_header Host $host;
    proxy_read_timeout 3600s;
}
```

**微信小程序真机**：公众平台 → 开发管理 → 服务器域名 → **socket 合法域名** 添加 `wss://test.yuanxusoftware.top`。

**复测通过标准**：客户端日志出现 STOMP `CONNECTED`；30s 内心跳正常；触发 `notification.new` 等推送时可收到 payload。



## 协议

使用 **STOMP** 协议（Simple Text Oriented Messaging Protocol）。

## 消息格式

```json
{
  "type": "message_type",
  "payload": { ... },
  "timestamp": "2026-07-01T15:23:33.000Z",
  "messageId": "ws_msg_a1b2c3"
}
```



## 消息类型


| type                   | 方向      | payload                                       | 说明     |
| ---------------------- | ------- | --------------------------------------------- | ------ |
| `chat.message`         | 双向      | `{ fromId, toId, content, chatType }`         | 聊天消息   |
| `chat.broadcast`       | 服务端→客户端 | `{ fromId, targetRole, content }`             | 群发消息   |
| `notification.new`     | 服务端→客户端 | `{ announcementId, announcementType, title }` | 新公告通知  |
| `order.status_update`  | 服务端→客户端 | `{ orderId, orderStatus, updatedAt }`         | 订单状态变更 |
| `delivery.grab_result` | 服务端→客户端 | `{ orderId, courierId, result }`              | 抢单结果   |
| `point.balance_update` | 服务端→客户端 | `{ residentId, pointType, balance }`          | 余额变更   |
| `heartbeat.ping`       | 客户端→服务端 | `{}`                                          | 心跳     |
| `heartbeat.pong`       | 服务端→客户端 | `{}`                                          | 心跳回复   |




## 心跳与重连

- 客户端每 **30秒** 发送 `heartbeat.ping`
- 服务端回复 `heartbeat.pong`
- **60秒** 未收到 pong → 判定断开 → 触发重连
- 重连策略：1s → 3s → 5s → 10s → 30s（最多5次）



## 订阅端点


| 端点                                     | 说明   | 示例         |
| -------------------------------------- | ---- | ---------- |
| `/user/queue/notifications`            | 个人通知 | 新公告、订单状态变更 |
| `/topic/chat/{userId}`                 | 私聊消息 | 邻居聊天       |
| `/topic/broadcast/{propertyCompanyId}` | 物业广播 | 物业公告广播     |
| `/topic/delivery/{courierId}`          | 配送通知 | 抢单结果、配送任务  |
| `/topic/order/{orderId}`               | 订单状态 | 订单状态实时更新   |


---



## 社区论坛错误 (98xxx)

| 错误码 | HTTP | 含义 | 触发场景 |
| --- | --- | --- | --- |
| `98001` | 404 | 帖子不存在 | 帖子ID无效或非 published |
| `98002` | 404 | 评论不存在 | 评论ID无效 |
| `98003` | 404 | 举报记录不存在 | 举报ID无效 |
| `98004` | 409 | 已举报过该内容 | 同一用户对同一 target 重复举报 |
| `98005` | 400 | 内容包含敏感词 | 发帖/评论命中 sensitive_words |
| `98006` | 403 | 无权删除该内容 | 非作者删除帖子/评论 |
| `98007` | 400 | 帖子内容过长 | content > 2000 |
| `98008` | 400 | 评论内容过长 | content > 1000 |
| `98009` | 409 | 该举报已处理 | 重复处理举报 |

# 90. 微信普通分账管理接口 (cbk-accounts & reconcile)

> **最后更新**：2026-08-24
> **Base**：所有接口前缀 `/api/v1`
> **Auth**：管理端接口需 `Authorization: Bearer <accessToken>`（见 §1.2 `POST /auth/admin-login`）
> **说明**：用于将每个业务实体（物业/商家/统筹/负责人/快递员）的微信普通分账收款账户录入后台，替代手写配置；以及分账失败/待补的人工对账台。平台级参数（普通商户号 mch_id / AppID appId / APIv3 密钥 / 商户证书 / 证书序列号 / 域名 / notifyUrl）走后端环境变量，不在本模块管理。
> **通道切换说明（2026-08-24）**：原扫呗 CBK 分账已切换为微信支付「普通分账」（分账 orders / 整笔解冻 unfreeze / 回退 return-orders / 收款方在微信侧自行提现）。接口路径与本管理台结构不变，仅底层通道与 `accountNo` 语义由「扫呗收款账户号」变为「微信商户号（MERCHANT_ID）/ openid（PERSONAL_OPENID）」。

---

> ⚠️ **【废弃章节 · 2026-09-03 复核】** 本章（§90.1~§90.9）为历史「扫呗 CBK 聚合支付」架构遗留文档。
> 代码中**已无任何 `Cbk*` 控制器/服务**（`/admin/cbk/accounts*`、`/admin/cbk/reconcile*` 全部不存在），通道已切换为**微信支付官方「普通分账」**。
> 请勿按本章联调；现行权威说明见 [§96 支付与分账 · 现行架构](#96-支付与分账--现行架构v72-权威章节)。

## 90.1 概念：平台级参数 vs 实体级账户

- **平台级参数（一份，环境变量）**：微信支付普通商户号 `mch_id`、AppID `appId`、APIv3 密钥、商户 API 证书（私钥）、商户证书序列号、回调 `notifyUrl`、分账比例。前端不接触。
- **实体级账户（每行一个，本模块管理）**：每个 `ownerType + ownerId` 对应的微信商户号（MERCHANT_ID）/ openid（PERSONAL_OPENID）`accountNo`，落 `cbk_accounts` 表，分账时由 `CbkAccountService.resolveAccountNo` 按 `(ownerType, ownerId)` 解析。

**ownerType 取值**：

| ownerType | 含义 | ownerId |
| --- | --- | --- |
| `PLATFORM` | 平台（抽成留存） | 固定 `"PLATFORM"` |
| `PROPERTY` | 物业公司 | 物业 ID（`pc_xxx`） |
| `MERCHANT` | 商家 | 商家 ID（`mer_xxx`） |
| `COORDINATOR` | 统筹 | 统筹账号 ID |
| `SECTOR_LEADER` | 板块负责人 | 负责人 ID |
| `INDIVIDUAL_LEADER` | 个体负责人 | 负责人 ID |
| `COURIER` | 快递员 | 快递员 ID（`cour_xxx`） |

**统一响应**：`{code, message, data, timestamp, errorCode?}`，`code=0` 成功。

---

## 90.2 POST `/admin/cbk/accounts` — 新增/更新单个收款账户

**Auth**: Required（管理员 JWT）
**幂等**：相同 `(ownerType, ownerId)` 重复提交 = 更新，不新建重复行。

**请求体** `CbkAccountUpsertReq`：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `ownerType` | String | ✅ | 见 90.1 七种取值 |
| `ownerId` | String | ✅ | 对应实体 ID |
| `accountNo` | String | ✅ | 微信商户号（MERCHANT_ID）/ openid（PERSONAL_OPENID）；分账时作为接收方账号 |
| `merchantNo` | String | 选填 | 该主体微信商户号（若有；MERCHANT_ID 接收方必填，PERSONAL_OPENID 留空） |
| `accountName` | String | 选填 | 户名/备注 |
| `verified` | Boolean | 选填 | 是否已通过微信进件 / 绑卡校验，默认 `true` |

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/admin/cbk/accounts \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"ownerType":"MERCHANT","ownerId":"mer_a8f3k2","accountNo":"6212...","accountName":"XX便利店","verified":true}'
```

**成功响应**：

```json
{ "code": 0, "message": "success", "data": { "id": "cba_xxx", "ownerType": "MERCHANT", "ownerId": "mer_a8f3k2", "accountNo": "6212...", "accountName": "XX便利店", "merchantNo": null, "verified": true }, "timestamp": "2026-08-14T10:00:00" }
```

---

## 90.3 POST `/admin/cbk/accounts/batch` — 批量导入/更新

**Auth**: Required
**说明**：接收 `CbkAccountUpsertReq` 数组，逐条按 `(ownerType, ownerId)` 幂等 upsert。运营从微信支付商户平台导出收款方信息后粘贴即可。

**请求体**：`CbkAccountUpsertReq[]`

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/admin/cbk/accounts/batch \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '[
    {"ownerType":"PROPERTY","ownerId":"pc_a8f3k2","accountNo":"6222...","accountName":"阳光物业"},
    {"ownerType":"MERCHANT","ownerId":"mer_a8f3k2","accountNo":"6222...","accountName":"XX便利店"},
    {"ownerType":"COURIER","ownerId":"cour_b7m9x1","accountNo":"6222...","accountName":"王师傅"}
  ]'
```

**成功响应**：`data` 为更新/插入后的账户对象数组。

---

## 90.4 GET `/admin/cbk/accounts` — 查询收款账户

**Auth**: Required
**说明**：三个过滤参数任意组合；都不传 = 返回全部。

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `ownerType` | String | 选填 | 按类型过滤 |
| `ownerId` | String | 选填 | 按实体过滤 |
| `verified` | Boolean | 选填 | 是否已校验 |

**请求示例**：

```bash
GET /admin/cbk/accounts?ownerType=PROPERTY&verified=false
```

**成功响应**：`data` 为账户对象数组。

---

## 90.5 GET/PUT/DELETE `/admin/cbk/accounts/{id}` — 查/改/删单条

**Auth**: Required

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/admin/cbk/accounts/{id}` | 返回单条账户对象 |
| `PUT` | `/admin/cbk/accounts/{id}` | body 同 90.2，整体更新该账户 |
| `DELETE` | `/admin/cbk/accounts/{id}` | 删除，返回 `204 No Content` |

---

## 90.6 POST `/admin/cbk/accounts/{id}/verify` — 标记校验状态

**Auth**: Required
**说明**：补录账户后，财务在微信支付商户平台确认已进件/绑卡，将 `verified` 置 `true`。

**请求示例**：

```bash
POST /admin/cbk/accounts/{id}/verify?verified=true
```

**成功响应**：`data` 为更新后的账户对象。

---

## 90.7 GET `/admin/cbk/reconcile/failed` — 查询待人工介入的分账

**Auth**: Required
**Tag**: 微信分账 人工对账台

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `statuses` | String | 选填 | 逗号分隔状态集合；缺省为全部失败/待补状态 |
| `limit` | int | 选填 | 返回条数上限，默认 200 |

**默认状态集合**：`freeze_failed`, `finish_failed`, `withdraw_failed`, `reverse_failed`, `refund_failed`, `skipped`。

> 注：微信普通分账收款方在微信侧自行提现、平台不代提，故 `withdraw_failed` 基本不会出现；`finish_failed` 指本笔「整笔解冻」失败，可手动重试（§90.8）。

**请求示例**：

```bash
GET /admin/cbk/reconcile/failed?statuses=skipped,withdraw_failed&limit=50
```

**成功响应**：`data` 为 `CbkSplitRecord` 列表。

---

## 90.8 POST `/admin/cbk/reconcile/retry/{splitNo}` — 手动重试分账

**Auth**: Required
**说明**：按当前状态自动路由重试：`freeze_failed`→重冻（重新请求分账）；`finish_failed`→重解冻（整笔 unfreeze）；`reverse_failed`/`refund_failed`→重冲正（return-orders）；`withdraw_failed`→重提（普通分账收款方自行提现，通常无需）；`skipped`→缺户补冻（账户补齐后自动补冻）。

**请求示例**：

```bash
POST /admin/cbk/reconcile/retry/{splitNo}
```

**成功响应**：`data` 为 `Boolean`——`true` 表示命中可重试状态并已触发，`false` 表示记录不存在或状态不可重试。

---

## 90.9 错误码

| errorCode | code | 说明 |
| --- | --- | --- |
| `TOKEN_MISSING` | 401 | 未带 `Authorization: Bearer` |
| `TOKEN_INVALID` | 401 | token 无效/过期 |
| 参数校验失败 | 400 | `ownerType`/`ownerId`/`accountNo` 缺失，见 `errors` 字段 |

> 注：本模块不涉及业务金额错误码；分账流水状态见 90.7 状态集合。

# 附录



## ID 前缀规则


| 资源    | 前缀      | 示例            |
| ----- | ------- | ------------- |
| 物业公司  | `pc_`   | `pc_a8f3k2`   |
| 业主/住户 | `res_`  | `res_b7m9x1`  |
| 商家    | `mch_`  | `mch_c4d2p8`  |
| 订单    | `ord_`  | `ord_e6n5q3`  |
| 积分记录  | `pt_`   | `pt_f1h8w7`   |
| 物业币记录 | `coin_` | `coin_g3j2k9` |
| 公告    | `ann_`  | `ann_l5r6t2`  |
| 服务    | `svc_`  | `svc_v8y4z1`  |
| 家庭    | `fam_`  | `fam_h4j7k2`  |
| 小区    | `com_`  | `com_xxx`     |
| 文件    | `file_` | `file_xxx`    |
| 快递负责人 | `cm_`  | `cm_demo001`  |
| 配送价格区间 | `dpr_` | `dpr_demo001` |
| 价格审批  | `pa_`  | `pa_demo001`  |
| 询价    | `inq_`  | `inq_demo001` |
| 提现记录  | `wr_`  | `wr_demo001`  |




## 字段命名规则

- API 请求/响应字段使用 **camelCase**
- 数据库字段使用 **snake_case**
- 后端自动转换，前端直接使用 camelCase



## 日期格式

- 所有日期时间字段使用 `yyyy-MM-dd HH:mm:ss` 格式（本地时间）
- 日期字段使用 `yyyy-MM-dd` 格式
- `ApiResponse.timestamp` 字段同样使用 `yyyy-MM-dd HH:mm:ss` 本地时间格式

## 94. 需求对齐新增接口（v7.1）

> **2026-09-03** 依据前端《大体逻辑.docx》与缺口清单新增/调整的接口汇总。
> 数据库变更见 [§94.11](#9411-数据库变更与升级回滚脚本)。

### 94.1 奖励金/积分发放审批 `/reward-grants`

| 方法 | 路径 | 说明 | 角色 |
| --- | --- | --- | --- |
| `POST` | `/reward-grants` | 提交发放申请（`grantType`=`points`/`reward`、`targetResidentId`、`pointAmount`、`reason`） | resident / merchant |
| `POST` | `/reward-grants/{id}/audit` | 审批（`action`=`approve`/`reject`，拒绝必填 `remark`），通过后发放 | property_admin |
| `GET` | `/reward-grants/my` | 我的申请（分页） | resident / merchant |
| `GET` | `/reward-grants` | 审批列表（可按 `status`、`targetResidentId` 过滤） | property_admin, **platform_admin（🆕 2026-09-07 前端问题 D：平台管理员仅可查看，审批操作仍限物业）** |

归属模式取 `property_companies.points_attribution_mode` / `reward_attribution_mode`。

### 94.2 板块负责人工作台 `/sector-leaders`

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/sector-leaders/{id}/merchants` | 本板块商家列表 |
| `GET` | `/sector-leaders/{id}/earnings` | 收入概览（累计分成 + 最近 200 条明细） |
| `GET` | `/sector-leaders/{id}/merchant-revenues` | 所负责商家数量与每家营收（`revenue`/`orderCount`/`myShare`） |
| `GET` | `/sector-leaders/{id}/approvals` | 待关注的商家分成审批 |
| `POST` | `/sector-leaders/{id}/merchant-audits` | 审批商家加入（`merchantId`、`approved`、`remark`），同意后商家可登录 |
| `POST` | `/sector-leaders/{id}/approvals/{approvalId}/audit` | 审批商家分成比例（`approved`、`remark`） |
| `POST` | `/sector-leaders/{id}/withdrawals` | 提现申请 |
| `GET` | `/sector-leaders/{id}/withdrawals` | 提现记录 |

`{id}` 为板块负责人ID（`sl_`），仅可操作本人。

### 94.3 个体负责人工作台 `/individual-leaders`

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/merchants` | 个体负责人新增商家（复用商家入驻，自动绑定归属个体负责人，需审批） |
| `PUT` | `/individual-leaders/{id}/merchant-distribution` | 设置商家分成比例 → 提交**板块负责人**审批 |
| `GET` | `/individual-leaders/{id}/merchant-revenues` | 所负责商家及营收、审核状态、我的分成 |
| `GET` | `/individual-leaders/{id}/approvals` | 商家审批情况（待审/通过/驳回） |
| `POST` | `/individual-leaders/{id}/withdrawals` | 提现申请（新增可提现余额校验） |
| `GET` | `/individual-leaders/{id}/withdrawals` | 提现记录 |

### 94.4 一级经销商工作台 `/distributors`

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/distributors/my/products` | 上架商品（`name`/`wholesalePrice`/`suggestedRetailPrice`/`stock`/`serviceScope`） |
| `GET` | `/distributors/my/products` | 我的商品（含 `pickupCount` 下游拿货数） |
| `PUT` | `/distributors/my/products/{id}` | 修改商品（未传字段不更新） |
| `PATCH` | `/distributors/my/products/{id}/stock` | 调整库存（`delta` 正数补货 / 负数扣减，不可为负） |
| `DELETE` | `/distributors/my/products/{id}` | 删除商品（软删除：置为 `deleted`，从经销商列表与选品池移除，下游拿货记录保留；已删除商品不可再编辑/补货/删除） |
| `GET` | `/distributors/my/pickups` | 拿货记录（谁拿走、拿走多久、零售价） |
| `POST` | `/distributors/my/pickups/{listingId}/cancel` | 取消某业主商户拿货并释放名额 |
| `GET` | `/merchant/withdrawals` | 经销商提现（复用商家提现，含手续费） |

经销商身份 = `merchantLevel=official_certified` 且审核通过的商家。

### 94.5 业主商户 `/resident-merchants`

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `DELETE` | `/resident-merchants/listings/{id}` | 取消拿货（释放名额） |
| `GET` | `/resident-merchants/settlements` | 卖货明细（订单完成自动生成） |

**定价规则变更**：拿货零售价**仅校验不低于进货价**，取消原「加价 5%~10% 区间」限制（业主商户可自定义加价）。

### 94.6 导航栏配置

| 方法 | 路径 | 说明 | 角色 |
| --- | --- | --- | --- |
| `GET` | `/admin/navigation-items` | 导航项列表 | property_admin / platform_admin |
| `POST`/`PUT`/`DELETE` | `/admin/navigation-items[/{id}]` | 新增/修改/删除 | property_admin / platform_admin |
| `GET` | `/navigation-items?module=home` | 手机端拉取导航（`module`=`home`/`service`/`activity`/`resident_merchant`） | 任意登录用户 |

### 94.7 物业银行卡 `/admin/property-bank-cards`

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/admin/property-bank-cards` | 列表（账号脱敏，含 `purpose`） |
| `POST` | `/admin/property-bank-cards` | 新增（`purpose`=`points`/`reward`，`isDefault` 唯一默认） |
| `PUT` | `/admin/property-bank-cards/{id}` | 修改（未传字段不更新） |
| `DELETE` | `/admin/property-bank-cards/{id}` | 删除 |

### 94.8 区域配额 `/admin/region-quotas`

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/admin/region-quotas?sector=` | 配额列表 |
| `POST` | `/admin/region-quotas` | 新增（`sector`、`quotaType`=`point`/`amount`、`quotaAmount`、周期） |
| `PUT` | `/admin/region-quotas/{id}` | 修改（总量不可低于已使用量） |
| `DELETE` | `/admin/region-quotas/{id}` | 删除 |

### 94.9 平台配置 `/admin/platform-config`

| 方法 | 路径 | 说明 | 角色 |
| --- | --- | --- | --- |
| `GET` | `/admin/platform-config` | 查看归属开关/提现粒度/分成维度 | property_admin / platform_admin |
| `PUT` | `/admin/platform-config` | 更新（仅更新非 null 字段） | platform_admin |

| 配置项 | 取值 | 默认 |
| --- | --- | --- |
| `rewardAttributionMode` | `property` / `resident` | `property` |
| `withdrawalGranularity` | `per_role` / `merchant` / `resident` | `per_role` |
| `shareDimension` | `default`（四级分成）/ `simple`（平台+物业两级） | `default` |

### 94.10 推荐产品与订单

| 方法 | 路径 | 说明 | 角色 |
| --- | --- | --- | --- |
| `PUT` | `/products/{id}/recommend` | 推荐/取消推荐商品（`recommended`、`recommendedSort`） | property_admin / platform_admin |
| `GET` | `/products?recommended=true` | 商品列表按推荐过滤 | 任意 |
| `POST` | `/orders/from-listing` | 向业主商户下单（`listingId`、`quantity` 等） | resident |
| `POST` | `/orders/from-listing/preview` | 业主上架商品支付试算（`listingId`、`quantity`、`coinUsed`、`pointUsed`、`familyId`） | resident |

**两档佣金**（后台 `PUT /admin/resident-merchant/settings`）：

| 配置项 | 默认 | 说明 |
| --- | --- | --- |
| `platformCommissionRate` | `0.20` | 业主商户销售自有商品的平台佣金 |
| `distributorProductCommissionRate` | `0.10` | 业主商户销售一级经销商货品的平台佣金 |

### 94.11 数据库变更与升级/回滚脚本

| 类型 | 文件 | 说明 |
| --- | --- | --- |
| Flyway 迁移 | `backend/src/main/resources/db/migration/V9__requirement_alignment.sql` | **合并迁移**（原 V9~V12 合并为一个文件）。应用启动**不会**自动执行（Flyway 默认关闭，`FLYWAY_ENABLED` 仅非 prod profile 可开；prod 恒禁用），需手动执行 |
| 手工升级 | `sql/upgrade_all_gaps.sql` | 合并升级脚本，覆盖 V4~V9 全部变更，**幂等可重复执行** |
| 手工回滚 | `sql/rollback_all_gaps.sql` | 合并回滚脚本，与升级脚本一一对应，幂等 |

**执行方式**：

```bash
# 升级
mysql -h<host> -u<user> -p wuye_points < sql/upgrade_all_gaps.sql
# 回滚（注意：会删除新增表数据，请先备份）
mysql -h<host> -u<user> -p wuye_points < sql/rollback_all_gaps.sql
```

**新增表**：`reward_grant_applications`、`navigation_items`、`property_bank_cards`、`region_quotas`
**新增列**：`merchants.individual_leader_id` / `individual_leader_rate`、`property_companies.points_attribution_mode` / `reward_attribution_mode`、`property_bank_cards.purpose`、`distributor_products.service_scope`、`products.is_recommended` / `recommended_sort`、`orders.listing_id`、`reward_grant_applications.grant_type`、`navigation_items.module`

### 94.12 活动组注册审核 `/admin/activity-groups`

| 方法 | 路径 | 说明 | 角色 |
| --- | --- | --- | --- |
| `GET` | `/admin/activity-groups/reviews` | 活动组注册审核列表（`status`=`pending`/`approved`/`rejected` 过滤） | property_admin / platform_admin |
| `POST` | `/admin/activity-groups/{id}/review` | 审核活动组注册（`action`=`approve` 通过 / `reject` 驳回，驳回填 `remark`） | property_admin / platform_admin |

> **2026-09-07 前端问题 F（产品确认）**：住户端 `POST /activity-groups` 创建活动组**即刻生效**（`status=active`、`reviewStatus=approved`），无需走 pending 等待审核；本审核接口为**事后兜底**。
> - `approve`：无影响（组创建即生效），仅当传入 `remark` 时补记审核备注供审计。
> - `reject`：**永久硬删除**该活动组（先清订阅/成员/价格档位再删组本体），不可恢复。

### 94.13 注销账号平台终审 `/admin/residents`

| 方法 | 路径 | 说明 | 角色 |
| --- | --- | --- | --- |
| `GET` | `/admin/residents/deletion-requests` | 注销申请列表（处于 `pending_deletion` 冷静期的住户，分页） | platform_admin |
| `POST` | `/admin/residents/{id}/deletion/review` | 平台审核注销：`action`=`approve` 通过并注销 / `reject` 驳回恢复（驳回填 `rejectReason`） | platform_admin |

### 94.14 删除物业公司（真实删除）

| 方法 | 路径 | 说明 | 角色 |
| --- | --- | --- | --- |
| `DELETE` | `/admin/property-companies/{id}` | 真实删除物业公司；存在关联商家/住户/订单/社区时拒绝 | platform_admin |

> **2026-09-08 变更（前端问题：新建物业后立刻删除 400）**
>
> **两档删除语义（推荐前端按此实现）**
>
> | 场景 | 接口 | 说明 |
> |------|------|------|
> | 日常运维 | `PATCH /admin/property-companies/{id}/status`，`{"status":"inactive"}` | 停用后不可再经营；可再次置 `active` 启用。**建议作为列表主按钮「停用/启用」** |
> | 硬删除 | `DELETE /admin/property-companies/{id}` | 仅空壳（无小区/住户/商家/订单）可成功；否则拒绝 |
>
> **拒绝响应（v8.2 起返回具体关联类型与数量）**
>
> - HTTP `400`，错误码 **`97033`**（`PROPERTY_COMPANY_HAS_RELATED_DATA`）
> - `message` 形如：
>   `该物业公司存在关联数据，不可删除（小区 1 个、住户 2 个、商家 3 个、订单 5 个）。如仅需在列表隐藏，请改用 PATCH /admin/property-companies/{id}/status 置为 inactive`
> - 仅列出数量大于 0 的关联类型；`errors` 字段不填充（业务异常统一走 `message`）。
>
> 新建物业公司时会自动写入小区与初始管理员账号，因此**新建后必然命中关联拦截**，属预期行为。

### 94.15 商家卖货明细（merchant 订单视角）

| 方法 | 路径 | 说明 | 角色 |
| --- | --- | --- | --- |
| `GET` | `/merchants/my/sales` | 当前商家自身的订单/卖货明细（`orderStatus` 可过滤，分页返回 `OrderListItemResponse`） | merchant |

### 94.16 补齐“半套”断链条端点（v7.2）

> 以下端点此前**仅有管理端/服务层实现、缺少住户或商家侧触发入口**，导致审批流与状态机空转。本次补齐后闭环。

| 方法 | 路径 | 说明 | 角色 |
| --- | --- | --- | --- |
| `POST` | `/residents/me/building-changes` | **住户提交楼栋变更申请**（`communityId`/`building`/`unit`/`floor`/`room`）；同一住户同时仅允许一条待审批申请 | resident |
| `GET` | `/residents/me/building-changes` | 我的楼栋变更申请（按申请时间倒序） | resident |
| `POST` | `/orders/{id}/refund-request` | **住户申请退货**：订单置 `refunding`，等待 §管理员退货审核；仅 `paid`/`delivering`/`completed` 可申请 | resident |
| `POST` | `/merchants/my/orders/{orderId}/fulfillment` | **商家选择履约方式**：`merchant_self` 自配 / `courier_hall` 平台配送；仅 `pending_choice` 订单可选，写入履约审计日志，返回 `SendDeliveryResponse` | merchant |
| `POST` | `/orders/{id}/choose-fulfillment` | 同上（§68.2 路径，等价入口） | merchant |
| `POST` | `/orders/{id}/send-delivery` | **商家发布配送任务**（§8.6.2，等价选择 `courier_hall`，幂等，`alreadyExists`） | merchant |
| `POST` | `/deliveries/{id}/merchant-confirm` | **商家确认自配送达**（§68.3，仅 `carrier_type=merchant`） | merchant |
| `POST` | `/admin/orders/refunds/{orderId}/fulfillment-override` | 管理员强制改派履约方式（留痕） | property_admin / platform_admin |
| `POST` | `/admin/orders/{id}/override-fulfillment` | 同上（§68.4 路径，等价入口） | property_admin / platform_admin |
| `POST` | `/merchants/my/orders/{orderId}/verify` | **商家核实订单**：`pending_verification` → 通过（进冻结期）/ 拒绝（取消并退款） | merchant |
| `POST` | `/residents/me/points/transfer-to-property` | **住户积分转给物业**（§2.6.4.1，即时扣个人积分，生成 `source=transfer_to_property` 流水） | resident |
| `POST` | `/points/transfer-to-property` | 同上（兼容别名） | resident |
| `POST` | `/residents/me/coins/transfer-to-property` | **住户物业币转给物业**（§2.6.4.2，即时扣**家庭物业币**，个人币不可转，生成 `source=transfer_to_property` 流水） | resident |
| `POST` | `/property-coins/transfer-to-property` | 同上（兼容别名） | resident |
| `PUT` | `/residents/me/community` | **住户完善/绑定小区**（§2.6.1） | resident |

**状态机闭环说明**：

- 楼栋变更：住户提交（`pending`）→ 管理端 `POST /admin/building-changes/{id}/approve|reject`（§既有接口）→ 通过后写入 `residents` 地址。
- 退货：住户申请（`refunding`）→ 管理端 `POST /admin/orders/refunds/{orderId}/audit`（`approve` 执行退款并恢复库存 / `reject` 置 `refund_rejected`）。
- 履约：支付后订单处于 `pending_choice` → 商家选择或 `FulfillmentChoiceTimeoutJob`（30 分钟）默认改派 `courier_hall` / 管理员强制改派 → 写入 `fulfillment_choice_logs` 留痕。
- 团购核实：支付后订单进入 `pending_verification` → 商家核实（通过进冻结期，`FreezeDays` 后结算 / 拒绝取消并退款）。

---

## 96. 支付与分账 · 现行架构（v7.2 权威章节）

> ⚠️ §90 / §92 为历史「扫呗 CBK 聚合支付」遗留文档，相关端点在代码中**已全部移除**；本章为当前唯一权威说明。

### 96.1 通道与配置

- **支付**：微信支付 APIv3（JSAPI 下单），配置项前缀 `wechat.pay.*`：`mch-id`、`app-id`、`api-v3-key`、`private-key-path` / `private-key-base64`、`merchant-serial-number`、`notify-url`、`refund-notify-url`、`profit-sharing`、`connect-timeout-ms`、`read-timeout-ms`。
- **分账**：微信**服务商分账**（普通服务商文档族 `/v3/profitsharing/*`，非电商收付通、非品牌服务商；`sub_mchid` 为商家特约商户号且**必填**，收款方为商户号或个人 openid），配置项前缀 `wechat-split.*`：`enabled`、`user-refund-enabled`、`auto-withdraw-enabled`、`receiver-relation-type`（CUSTOM 时另需 `receiver-custom-relation`）、`after-sale-days`、`max-split-ratio`。直连商户通道（`wechat.direct.*`）只收款、不分账。
- 平台级参数一律走环境变量，管理端只维护结算开关（§96.4）。
- **资金链路开关一律 fail-closed（默认关）**，见 §96.6。

### 96.2 支付结果回调（唯一已实现的回调入口）

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/payment/notify/wechatpay` | 微信支付结果通知（APIv3）。验签 + 解密 + **金额校验** + 标流水 `paid` + 写 `payment_settle_outbox` + 发布事件后**立即应答**（须 5 秒内）。成功返回 `{"code":"SUCCESS"}`；失败返回非 200 + `{"code":"FAIL"}`（微信按策略重推）。业务 finalize 由 `PaymentSettleService` 异步完成（指数退避重试 + 卡死 PROCESSING 原子回收）。 |

**金额校验（fail-closed）**：回调 `amount.total`（分）必须等于本地 `payment_records.cash_amount`（元×100）。

- 一致 → 正常入账。
- **不一致或金额缺失** → **不入账、不自动关单**，流水置 `amount_mismatch` 并写 `error_msg`，同时打 `ERROR` 日志告警，回调仍应答 `SUCCESS`（重推不会改变结果）。
- `amount_mismatch` 不在 `pending` 扫描范围内，故不会被超时调度误关单；需人工核对微信账单后处理（退款或人工补录）。

> 线上需在微信商户平台把「支付回调地址」配置为 `{域名}/api/v1/payment/notify/wechatpay`，并与 `wechat.pay.notify-url` 保持一致。

### 96.3 回调地址清单与实现状态

| 用途 | 配置项 | 约定地址 | 代码状态 |
| --- | --- | --- | --- |
| 支付结果 | `wechat.pay.notify-url` | `https://<域名>/api/v1/payment/notify/wechatpay` | ✅ 已实现 |
| 退款结果 | `wechat.pay.refund-notify-url` | 需配置 | ⚠️ **无回调入口**：退款状态由 `RefundQueryScheduler` 主动查询兜底 |
| 退款结果（缺省） | 未配 `refund-notify-url` 时回退 `wechat.pay.notify-url` | 同上 | 可接收但按「无对应流水」应答 SUCCESS，状态仍由轮询兜底 |
| 分账结果 | ~~`wechat-split.notify-url`~~ | — | ❌ **已删除**：微信普通分账请求体不含 `notify_url`，微信不推送分账结果通知；原配置无代码读取、无接收控制器，属「死回调路径」。结果由 `SplitWindowScheduler` / `ReverseFailedScheduler` / `SplitFailureScheduler` 主动查询 |
| 提现结果 | ~~`wechat-split.notify-url-withdraw`~~ | — | ❌ **已删除**：同上，普通分账无平台代提、无提现回调 |
| 电商分账结果 | `wechat-ecommerce.notify-url` | `https://<域名>/api/v1/payment/ecommerce-split/notify` | ✅ 已实现 |
| 商家转账结果 | `wechat-ecommerce.transfer-notify-url` | `https://<域名>/api/v1/payment/ecommerce-transfer/notify` | ✅ 已实现 |

> 退款与（普通）分账/提现依赖定时任务主动查询保证最终一致；如需实时性，后续以新迁移配套新增回调端点（属 P1，不影响现有资金正确性）。

### 96.4 结算与钱包（仍然有效，原 §92.6 / §92.7）

| 方法 | 路径 | 说明 | 角色 |
| --- | --- | --- | --- |
| `GET` | `/admin/settlement-config` | 结算配置（含 `wechatSplitEnabled` 分账开关） | platform_admin |
| `PUT` | `/admin/settlement-config` | 更新结算配置 | platform_admin |
| `GET` | `/wallet/cards` | 查询某主体钱包卡（`ownerType` / `ownerId`） | 管理端 |
| `GET` | `/technicians/my/wallet/cards` | 技工本人钱包卡 | technician |

> **`wechatSplitEnabled` 为运行时开关，已真正接入分账链路**（非「死开关」）：
> `WechatSplitService.isEnabled()` = 配置总开关 `wechat-split.enabled` **且** `settlement.wechatSplitEnabled`；
> 该结果被 `PaymentSettleService`（支付后冻结）、`SplitWindowScheduler` / `SplitFailureScheduler` / `ReverseFailedScheduler` 共同使用。
> 语义为「管理端只能收紧」：置 `false` 立即停止分账；配置总开关为 `false` 时置 `true` 也不会生效（保持 fail-closed，见 §96.6）。

### 96.5 定时任务（均已加 ShedLock 分布式锁）

`SplitWindowScheduler`（分账窗口出账）、`ReverseFailedScheduler`（分账回退）、`RefundQueryScheduler`（退款状态查询兜底）、`FulfillmentChoiceTimeoutJob`（履约超时兜底，默认改派平台配送）、`PaymentSettleScheduler`（支付 finalize 兜底与主动对账）。

### 96.6 上线前资金链路开关（fail-closed，默认全部关闭）

以下开关均为**默认关闭**，必须显式置 `true` 才生效；任一环境未显式开启时，行为是「拒绝/不执行」而非「静默放行」。

| 配置项 | 默认 | 作用 | 开启方式 |
| --- | --- | --- | --- |
| `wechat.pay.profit-sharing` | `false` | 下单时标记 `settle_info.profit_sharing`，缺失则后续分账报「非分账订单」 | `WECHAT_PAY_PROFIT_SHARING=true` |
| `wechat-split.enabled` | `false` | 分账链路总开关（冻结/出账/回退调度） | `WECHAT_SPLIT_ENABLED=true` |
| `wechat-split.auto-withdraw-enabled` | `false` | Path B 自动提现开关。**生效范围：仅「电商收付通」模式有真实资金动作**；「普通分账」模式下收款方自行在微信商户平台提现，开启仅推进状态位 `withdraw_status`，不代表平台自动打款 | `WECHAT_SPLIT_AUTO_WITHDRAW_ENABLED=true` |
| `app.payment.mock-enabled` | `false` | 模拟支付（保证金/食堂额度直接贷记） | 仅 dev：`application-dev.yml` 已置 `true`；生产**必须保持 false** |
| `settlement.wechatSplitEnabled` | `true`（DB 无记录时） | 管理端「运行时」分账开关，与 `wechat-split.enabled` **取与** | `PUT /admin/settlement-config`（只能收紧：置 `false` 即停分账，置 `true` 无法突破配置总开关） |

- `application-prod.yml` 已对本表首两项与 `mock-enabled` 再显式声明一次（defense in depth），生产环境即使漏配环境变量也保持关闭。
- **微信 HTTP 超时**：`wechat.pay.connect-timeout-ms`（默认 `5000`）、`wechat.pay.read-timeout-ms`（默认 `20000`），防止微信侧无响应时连接/线程被无限占用；超时统一包装为 `WechatPayException`（`WECHAT_HTTP_TIMEOUT`），连接被拒等其他传输异常为 `WECHAT_HTTP_ERROR`，由 `GlobalExceptionHandler` 回传，不再冒泡成 `99001 内部服务异常`。

---

---

> **说明**：§94.12~94.16 与 §96 为 2026-09-03 复核时补齐/重写的端点与章节，代码均已实现（v7.2）。

> **文档维护**：连云港元序软件有限公司
> **负责人**：王乙臣
> **最后更新**：2026-08-13

---

## 73. 活动组多课时价格（A1）

> **说明**：订阅时传 `pricingTierId`，金额取该课时 `price`；§18.6 请求/响应同步更新。有 active 课时的活动组，订阅必须以课时价格为准；无课时仍可按旧 `monthlyFee`/`yearlyFee` 兜底。

### 73.1 GET `/activity-groups/{id}/pricing-tiers` — 活动组课时价格列表

**角色**：公开

**响应**：
```json
{
  "code": 0,
  "data": {
    "activityGroupId": "act_xxx",
    "list": [
      {"id":"agt_xxx","tierName":"一级","tierCode":"level_1","price":300.00,"period":"monthly","sortOrder":1,"status":"active"},
      {"id":"agt_yyy","tierName":"二级","tierCode":"level_2","price":600.00,"period":"monthly","sortOrder":2,"status":"active"}
    ]
  }
}
```

### 73.2 POST `/activity-groups/{id}/pricing-tiers` — 创建课时价格

**角色**：resident（且为该组组长）/ activity_leader

**请求**：
```json
{"tierName":"一级","tierCode":"level_1","price":300.00,"period":"monthly","sortOrder":1}
```

### 73.3 PUT `/activity-groups/pricing-tiers/{tierId}` — 更新课时价格

**请求**：同创建（所有字段可选）

### 73.4 DELETE `/activity-groups/pricing-tiers/{tierId}` — 删除课时价格

### 73.5 PUT `/activity-groups/{id}/pricing-tiers` — 批量替换课时价格

**说明**：先清空原有，再按列表重建。

**请求**：直接传数组，不要包 `{"tiers":[{},{}]}`。

```json
[
  {"tierName":"一级","tierCode":"level_1","price":300.00,"period":"monthly","sortOrder":1},
  {"tierName":"二级","tierCode":"level_2","price":600.00,"period":"monthly","sortOrder":2}
]
```

---



## 74. 活动组类型搜索 + 多图（A2）

### 74.1 GET `/activity-groups` 增强

新增查询参数：
- `activityType`（可选）：活动类型，如 `piano`/`dance`/`english`

响应项新增字段：
- `activityType`：活动类型
- `coverUrls`：JSON 字符串数组，多张封面图

### 74.2 POST `/activity-groups` / PUT `/activity-groups/{id}` 增强

请求体新增字段：
- `activityType`：活动类型
- `coverUrls`：JSON 字符串，如 `["https://...","https://..."]`

---

## 75. 官方推荐商家（A3）

### 75.1 GET `/merchants/official-recommended` — 官方推荐商家列表

**角色**：公开

**查询参数**：`propertyCompanyId`（可选，未登录时使用）

**响应**：`PageResponse<MerchantListItemResponse>`，新增字段 `isOfficialRecommended`、`recommendedSort`。

> **物业推荐首页专用，修复 400**：通用列表 `GET /merchants` 的 `pageSize` 上限为 **100**（超过抛 `10003` 返回 400）。前端「物业推荐」若传 `pageSize=200` 会触发 400。本端点**一次全量返回**所有已审核通过且 `isRecommended=true` 的商家（内部固定 `page=1,pageSize=500`，不接收分页参数），按 `recommendedSort` 降序、`rankOrder`/`levelWeight` 次之排序，避免 400。前端「物业推荐」页应改调本端点，不再用 `/merchants?recommended=true&pageSize=200`。

### 75.2 POST/PUT `/admin/merchants/{id}/recommend` — 设置/取消官方推荐

**角色**：property_admin / platform_admin

> 与 `POST /admin/platform-merchants/{id}/recommend` 等价，均设置 `merchants.is_recommended` / `recommended_sort`。商家运营页调用本接口；同时兼容 PUT/POST 两种方法。
> 商城商家的"物业推荐"列表即 `GET /merchants?recommended=true`（§7.1）。

**请求**：
```json
{"isRecommended":true,"recommendedSort":100}
```

---

## 76. 配送方式与距离维度（A4）

### 76.1 GET `/delivery-rules/scope-options` — 配送范围与距离类型可选项

> ⚠️ **【当前未实现 · 2026-10-27 核对代码】** 后端不存在 `GET /delivery-rules/scope-options`。距离类型与配送范围为固定枚举（`1km/3km/5km/citywide/any`、`in_community/out_community/both`），请前端本地维护选项。以下为原设计文档，保留备查。

**角色**：公开

**响应**：
```json
{
  "deliveryScopes":[{"code":"in_community","description":"小区内"},{"code":"out_community","description":"小区外"},{"code":"both","description":"小区内外"}],
  "distanceTypes":[{"code":"1km","description":"1公里"},{"code":"3km","description":"3公里"},{"code":"5km","description":"5公里"},{"code":"citywide","description":"同城"},{"code":"any","description":"不限"}]
}
```

### 76.2 POST/PUT `/delivery-rules` 增强

请求体新增：`deliveryScope`（默认 `both`）

### 76.3 GET/POST/PUT/DELETE `/admin/delivery-price-ranges/{id}` 增强

请求体新增：`distanceType`（默认 `any`）、`deliveryScope`（默认 `both`）
列表查询支持 `distanceType`、`deliveryScope` 筛选参数。

**业务规则**：重叠校验按 `distanceType + deliveryScope` 维度独立判断。

---

## 77. 商家关键词（A5）

### 77.1 GET `/merchants/my/keywords` — 我的商家关键词

**角色**：merchant

**响应**：`{"merchantId":"mch_xxx","merchantName":"...","keywords":[{"id":"mk_xxx","keyword":"空调清洗","weight":10}]}`

### 77.2 POST `/merchants/my/keywords` — 增加单个关键词

**请求**：`{"keyword":"空调清洗","weight":10}`

### 77.3 POST `/merchants/my/keywords/batch` — 批量增加关键词

**请求**：`{"keywords":["空调清洗","空调维修","空调加氟"]}`

### 77.4 PUT `/merchants/keywords/{id}` — 更新关键词权重

**请求**：`{"weight":20}`

### 77.5 DELETE `/merchants/keywords/{id}` — 删除关键词

### 77.6 POST `/merchants/match-by-keywords` — 按关键词匹配商家

**角色**：公开 / resident

**请求**：`{"keywords":["空调清洗","空调维修"],"propertyCompanyId":"pc_xxx"}`

**响应**：`[{merchantId,name,matchedKeywords,totalWeight}]`，按总权重降序。

---

## 78. 建设积分（A6）

### 78.1 GET `/community-points/my-pool` — 我的小区建设积分

**角色**：resident

**响应**：`{"communityId":"cm_xxx","balance":1250.50,"totalIn":3000.00,"totalOut":1749.50}`

### 78.2 GET `/community-points/{communityId}` — 指定小区建设积分

### 78.3 GET `/community-points/{communityId}/records` — 建设积分流水

**查询参数**：`page`、`pageSize`

### 78.4 POST `/admin/community-points/{communityId}/adjust` — 手动调整

**角色**：property_admin / platform_admin

**请求体**（`ContributeToCommunityRequest`）：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `communityId` | `String` | ✅ | 小区ID；**路径参数之外请求体也必须传**（DTO 校验要求），且应与路径值一致 |
| `amount` | `BigDecimal` | ✅ | 调整金额，正数注入、负数扣减 |
| `remark` | `String` | ❌ | 备注 |

```json
{ "communityId": "com_demo001", "amount": 100.00, "remark": "活动奖励注入" }
```

---

## 79. 物业币使用条件（A7）

### 79.1 GET `/property-coins/use-condition` — 当前物业币使用条件

**角色**：resident

**响应**：`{"condition":"point_threshold","pointThreshold":1000}`

`condition` 取值：`none`/`point_threshold`/`property_fee_paid`/`manual_issue`

### 79.2 PUT `/admin/property-companies/{id}/coin-use-condition` — 设置使用条件

**角色**：property_admin / platform_admin

**请求**：`{"condition":"point_threshold","pointThreshold":1000}`

**业务规则**：消费物业币时校验
- `none`：无限制
- `point_threshold`：业主积分余额 ≥ 阈值
- `property_fee_paid`：业主有已缴清物业费
- `manual_issue`：仅物业配发的物业币可用

### 79.3 PUT `/admin/property-companies/{id}/coin-display` — 物业币显示开关（2026-10-29 新增）

**Auth**: Required  
**权限**: `coin:display_control`（2026-10-30 起为物业领导专属；标签身份不可授予，物业操作员不可用）

**请求**：

```json
{ "enabled": false }
```

**成功响应 data**：

```json
{
  "propertyCompanyId": "pc_demo001",
  "coinDisplayEnabled": false,
  "updatedAt": "2026-10-29T12:00:00Z"
}
```

**数据范围**：`platform_admin` 可操作任意物业；物业领导（`property_admin`）只能操作本人所属物业，跨物业返回 `10001 PERMISSION_DENIED`。标签身份与物业操作员调用返回 `20004 PERMISSION_DENIED`。

**联动**：关闭后住户端 `GET /coins` 的 `coinDisplayEnabled=false`，住户不能主动隐藏/展示物业币（`PUT /coins/hide` 返回 `COIN_EXCHANGE_NOT_ALLOWED`，具体错误码见错误码附录）。

---

## 80. 提现机制增强（A8）

### 80.1 GET `/admin/coin-withdrawals/settings` — 查询提现设置

**角色**：property_admin / platform_admin

**查询参数**：`propertyCompanyId`（`platform_admin` 可指定，否则取当前物业）

**响应**：`{"autoEnabled":true,"periodDays":7,"feeRate":0.006}`

> **（2026-10-06）** 该接口此前**后端未实现**（调用返回 404），现已补齐。取值来源：`property_companies.auto_withdrawal_enabled` / `auto_withdrawal_period_days` / `withdrawal_fee_rate`；未指定物业时返回 `autoEnabled=false`、`periodDays=7`，`feeRate` 取平台默认物业类提现费率。

### 80.2 PUT `/admin/coin-withdrawals/settings` — 更新提现设置

**角色**：property_admin / platform_admin

**查询参数**：`propertyCompanyId`（`platform_admin` 可指定；`property_admin` 取当前物业，不可为空）

**请求**：`{"autoEnabled":true,"periodDays":7}`

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `autoEnabled` | `Boolean` | ✅ | 是否开启自动提现 |
| `periodDays` | `Integer` | ✅ | 自动提现周期天数，1~365 |

**响应**：同 §80.1。写入 `property_companies.auto_withdrawal_enabled` / `auto_withdrawal_period_days`。

> **（2026-10-06）** 该接口此前**后端未实现**（调用返回 404），现已补齐。

### 80.3 PUT `/admin/coin-withdrawals/residents/{id}/block` — 阻止/解阻止居民提现

**角色**：property_admin / platform_admin

**路径参数**：`id` 住户ID（`res_`）

**请求**：`{"blocked":true,"reason":"违规操作"}`

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `blocked` | `Boolean` | ✅ | 是否阻止提现 |
| `reason` | `String` | ❌ | 原因（记入操作日志） |

**响应**：`data` 为 `null`。写 `residents.withdrawal_blocked`，仅影响提现资格，不影响其他功能。

> **（2026-10-06）** 该接口此前**后端未实现**（调用返回 404），现已补齐。

### 80.4 PUT `/admin/coin-withdrawals/merchants/{id}/block` — 阻止/解阻止商家提现

**角色**：property_admin / platform_admin

**路径参数**：`id` 商家ID（`mch_`）

**请求体**（`BlockWithdrawalRequest`）：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `blocked` | `Boolean` | ✅ | 是否阻止提现 |
| `reason` | `String` | ❌ | 原因（记入操作日志） |

```json
{ "blocked": true, "reason": "违规操作" }
```

**响应**：`data` 为 `null`。写 `merchants.withdrawal_blocked`，仅影响提现资格。

> **（2026-10-06）** 该接口此前**后端未实现**（调用返回 404），现已补齐。

### 80.5 定时任务

每日凌晨 4 点自动为开启自动提现的物业公司中 `withdrawalBlocked=false` 且余额>0 的用户创建提现申请。

---

## 81. 信息采集积分激励（A9）

### 81.1 GET `/admin/profile-rewards` — 奖励配置列表

**角色**：property_admin / platform_admin

**查询参数**：`scope`、`scopeId`

### 81.2 POST `/admin/profile-rewards` — 创建奖励配置

**请求**：
```json
{"scope":"property_company","scopeId":"pc_xxx","fieldName":"birthday","rewardPoints":50,"oncePerUser":true}
```

`scope` 取值：`property_company`/`community`/`platform`/`coordinator`

### 81.3 PUT `/admin/profile-rewards/{id}` — 更新配置

**角色**：`property_admin` / `platform_admin`

**路径参数**：`id`（奖励配置ID，`pcr_` 前缀）

**请求体**（`UpdateRewardConfigRequest`，全部可选）：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `rewardPoints` | `Integer` | ❌ | 奖励积分数，`@PositiveOrZero` |
| `oncePerUser` | `Boolean` | ❌ | 是否每用户仅奖励一次 |
| `status` | `String` | ❌ | `active` / `inactive` |

**成功响应**：`RewardConfigItemResponse`（字段同 §81.1 列表项）。

---

### 81.4 DELETE `/admin/profile-rewards/{id}` — 删除配置

### 81.5 POST `/residents/me/profile-reward` — 触发奖励

**角色**：resident

**请求**：`{"fieldName":"birthday"}`

**响应**：`{"fieldName":"birthday","rewardedPoints":50,"message":"奖励已发放"}`

**业务规则**：配置查找按 scope 优先级 community > property_company > platform；`oncePerUser=true` 时已发放过则不再发。

**家庭完善聚合奖励（伪字段 `family_completed`）**：
- 该字段**不是住户真实字段**，而是“家庭信息完善”聚合事件，前端在家庭信息页保存成功后调用即可（也可批量传 `triggerBatch` 语义，逐字段独立事务）。
- 完善判定：有家庭（`familyId`）+ 手机号 + 生日 + 是否有子女 + 楼栋/单元/房号均已填写，任一缺失返回 `rewardedPoints=0`、`message="家庭信息未完善，无法领取"`。
- 领奖记录与普通字段一致写入 `profile_reward_records`，`oncePerUser=true` 时每人仅发一次。

### 81.6 GET `/residents/me/profile-rewards` — 我的奖励记录

### 81.7 GET `/admin/stats/share` — 生成信息采集统计分享链接

**角色**：property_admin / platform_admin

**查询参数**：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `propertyCompanyId` | `String` | ❌ | 物业公司ID；平台管理员可指定，物业管理员自动限定本物业 |

**响应**：
```json
{
  "shareUrl": "https://test.yuanxusoftware.top/h5/stats?propertyCompanyId=pc_xxx",
  "posterImageUrl": "https://test.yuanxusoftware.top/api/v1/static/posters/stats.png",
  "qrcodeUrl": "https://test.yuanxusoftware.top/h5/stats?propertyCompanyId=pc_xxx&format=qrcode"
}
```

**说明**：分享 H5 地址由配置 `app.share.stats-url` 控制（默认 `https://test.yuanxusoftware.top/h5/stats`）。分享动作（微信转发/朋友圈）由前端 `onShareAppMessage`/`onShareTimeline` 承接，后端仅提供可分享链接/海报/二维码。

---

## 82. 领导账户资金流（A10）

### 82.1 GET `/admin/company-account/balance` — 公司账户余额

**角色**：property_admin / platform_admin

**响应**：`{"propertyCompanyId":"pc_xxx","balance":15000.00,"lastUpdatedAt":"..."}`

### 82.2 GET `/admin/company-account/records` — 公司账户流水

**角色**：`property_admin` / `platform_admin`

**Query 参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `propertyCompanyId` | `String` | ❌ | | 物业公司ID（`platform_admin` 可指定） |

**成功响应**（`PageResponse<CompanyAccountRecordItemResponse>`）：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "car_demo001",
        "propertyCompanyId": "pc_demo001",
        "orderId": "ord_demo001",
        "amount": 100.00,
        "source": "leader_share",
        "remark": "领导账户分成",
        "createdAt": "2026-07-01 15:23:33"
      }
    ],
    "pagination": { "page": 1, "pageSize": 20, "total": 1, "totalPages": 1 }
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---

### 82.3 POST `/admin/company-account/adjust` — 手动调整

**角色**：platform_admin

**请求**：`{"propertyCompanyId":"pc_xxx","amount":500.00,"remark":"调整"}`

**业务规则**：当 resident.propertySubRole == `property_leader` 时，相关收益资金直接计入公司账户，不进入个人 coin_balance，领导端无提现接口。

---

## 83. 欠费报表导出（A11）

### 83.1 GET `/admin/property-fees/arrears-report` — 欠费报表

**角色**：property_admin / platform_admin

**查询参数**：`propertyCompanyId`、`communityId`、`building`、`feeType`

**响应**：
```json
{
  "totalArrearsAmount": 125000.00,
  "totalCount": 35,
  "items":[{residentId,residentName,phone,building,unit,room,feeType,period,periodStart,periodEnd,amount,paidAmount,dueDate,daysOverdue}],
  "groupByBuilding":[{building:"1栋",totalAmount:45000.00,count:12}]
}
```

### 83.2 GET `/admin/property-fees/arrears-report/export` — 导出 CSV

**角色**：`property_admin` / `platform_admin`

**Query 参数**（与 §83.1 欠费报表一致）：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `propertyCompanyId` | `String` | ❌ | 物业公司ID（`platform_admin` 可指定） |
| `communityId` | `String` | ❌ | 小区ID |
| `building` | `String` | ❌ | 楼栋 |
| `feeType` | `String` | ❌ | 费用类型 |

**响应**：`Content-Type: text/csv`，UTF-8 BOM，文件名 `arrears_report_yyyyMMdd.csv`。

---

## 84. 可选房号接口（A12）

> **v2 说明（2026-08-17）**：新增独立房号主表 `community_rooms`，房号成为一等数据。
> - 楼宇结构 / 可选房号 直接读取主表；空房（`status=vacant`）可由物业在楼栋管理中新增，锁定房号（`locked`）不可选。
> - 住户入住 / 变更 / 注销时由后端自动同步房号占用状态（`occupied`）。
> - 历史数据未初始化主表时接口自动回退到住户表聚合（legacy 路径）。
> - 房号节点新增 `status` 字段：`vacant` 空置 / `occupied` 已入住 / `locked` 锁定。

### 84.1 GET `/communities/{id}/available-rooms` — 可选房号列表

**Auth**: Required

**查询参数**：`building`、`unit`（可选筛选）

**响应**：
```json
{
  "communityId":"cm_xxx",
  "totalAvailable":45,
  "items":[{"building":"1栋","unit":"1单元","floor":"3层","room":"301","isOccupied":false}]
}
```

### 84.2 GET `/communities/{id}/room-structure` — 楼宇结构

**响应**：
```json
{
  "building":"1栋",
  "units":[{"unit":"1单元","floors":[{"floor":"1层","rooms":[{"room":"101","isOccupied":true,"residentName":"张**","status":"occupied"}]}]}]
}
```

### 84.3 GET `/communities/{communityId}/rooms` — 房号列表（管理）

**Auth**: Required　**角色**：`property_admin`、`platform_admin`

返回社区全部房号（含空置 / 已入住 / 锁定）。

**响应**：
```json
{
  "communityId":"cm_xxx",
  "total":120,
  "rooms":[{"id":"room_xxx","building":"1栋","unit":"1单元","floor":"3层","room":"301","status":"vacant","residentId":null,"residentName":null,"createdAt":"2026-08-17T12:00:00Z"}]
}
```

### 84.4 POST `/communities/{communityId}/rooms` — 新增房号（管理）

**Auth**: Required　**角色**：`property_admin`、`platform_admin`

**请求**：
```json
{"building":"1栋","unit":"1单元","floor":"3","room":"301"}
```
新增空置房号（`status=vacant`）；房号重复返回 `PARAM_INVALID`。

### 84.5 POST `/communities/{communityId}/rooms/batch` — 批量新增房号（管理）

**Auth**: Required　**角色**：`property_admin`、`platform_admin`

同一楼栋下批量新增空置房号：
```json
{"building":"1栋","unit":"1单元","rooms":[{"floor":"3","room":"301"},{"floor":"3","room":"302"}]}
```

### 84.6 PATCH `/communities/{communityId}/rooms/{roomId}` — 更新房号（管理）

**Auth**: Required　**角色**：`property_admin`、`platform_admin`

锁定 / 解锁房号或调整楼层（`status` 仅支持 `vacant` / `locked`；已入住房号不能手动置为空置）：
```json
{"status":"locked","floor":"3"}
```

### 84.7 DELETE `/communities/{communityId}/rooms/{roomId}` — 删除房号（管理）

**Auth**: Required　**角色**：`property_admin`、`platform_admin`

删除空置房号；已入住房号（含真实入住住户）不可删除，返回 `PARAM_INVALID`。

---

## 85. 区域/项目负责人层级（B1）

层级链：物业公司 → 统筹 → 板块负责人 → **区域负责人** → **项目负责人**

### 85.1 GET `/regional-leaders` — 区域负责人列表

**角色**：coordinator / property_admin / platform_admin / sector_leader

**查询参数**：`propertyCompanyId`、`sectorLeaderId`

### 85.2 GET `/regional-leaders/my` — 我的区域负责人信息

**角色**：regional_leader

### 85.3 POST `/regional-leaders` — 创建区域负责人

**请求**：
```json
{"residentId":"res_xxx","sectorLeaderId":"sl_xxx","name":"王五","phone":"138...","regionName":"东区","shareRate":0.05}
```

### 85.4 GET/PUT/DELETE `/regional-leaders/{id}` — 详情/更新/删除

### 85.5 GET `/project-leaders` — 项目负责人列表

**Auth**: Required
**角色**：`property_admin`、`platform_admin`、`coordinator`、`sector_leader`、`resident`

**Query 参数**（均可选，用于过滤）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `propertyCompanyId` | string | 按物业公司过滤 |
| `regionalLeaderId` | string | 按所属区域负责人过滤 |

> ⚠️ 该接口返回**数组**（`ProjectLeaderListItemResponse[]`），**不分页**，无 `page` / `pageSize` 参数。

**响应** `data[]`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | string | 项目负责人 ID |
| `residentId` | string | 关联居民 ID |
| `regionalLeaderId` | string | 上级区域负责人 ID |
| `propertyCompanyId` | string | 所属物业公司 ID |
| `name` | string | 姓名 |
| `phone` | string | 手机号 |
| `projectName` | string | 负责项目名称 |
| `shareRate` | `BigDecimal` | 分成比例 |
| `status` | string | `active` / `inactive` |
| `createdAt` | string | 创建时间 |

### 85.6 GET `/project-leaders/my` — 我的项目负责人信息

**Auth**: Required（按当前登录用户解析，无需传 ID）

**响应**：`ProjectLeaderDetailResponse`，在 85.5 字段基础上增加 `updatedAt`。

### 85.7 POST `/project-leaders` — 创建项目负责人

**角色**：`coordinator`、`property_admin`、`sector_leader`

**请求**：

```json
{
  "residentId": "res_xxx",
  "regionalLeaderId": "rl_xxx",
  "name": "赵六",
  "phone": "13800000000",
  "projectName": "华帝一期",
  "shareRate": 0.03,
  "status": "active"
}
```

| 字段 | 必填 | 说明 |
| --- | --- | --- |
| `residentId` | 是 | 关联居民 ID |
| `regionalLeaderId` | 否 | 上级区域负责人 |
| `name` | 是 | 姓名 |
| `phone` | 否 | 手机号 |
| `projectName` | 否 | 负责项目名称 |
| `shareRate` | 否 | 分成比例 |
| `status` | 否 | 默认 `active` |

**响应**：`ProjectLeaderDetailResponse`。

### 85.8 GET `/project-leaders/{id}` — 项目负责人详情

**角色**：`property_admin`、`platform_admin`、`coordinator`、`sector_leader`

**响应**：`ProjectLeaderDetailResponse`。

### 85.9 PUT `/project-leaders/{id}` — 更新项目负责人

**角色**：`coordinator`、`property_admin`、`sector_leader`

**请求**（全部可选，仅传需要修改的字段）：`regionalLeaderId`、`name`、`phone`、`projectName`、`shareRate`、`status`。

**响应**：`ProjectLeaderDetailResponse`。

### 85.10 DELETE `/project-leaders/{id}` — 删除项目负责人

**角色**：`coordinator`、`property_admin`、`sector_leader`

**软删除**：仅将 `status` 置为 `inactive`，不物理删除记录。

**响应**：`data` 为 `null`。

---

`shareRate` 为从上级负责人中抽取的分成比例。物业公司在 `property_companies` 表中新增 `regional_leader_rate`、`project_leader_rate` 全局比例配置。

---

## 86. 询价对接聊天（B2）

### 86.1 PUT `/inquiries/{id}` 增强

**行为变更**：
- 商家报价（action=quote）后，系统自动通过消息系统向询价人发送报价通知
- 业主接受（action=accept）后，系统自动向商家发送接受通知
- 业主拒绝（action=reject）后，系统自动向商家发送拒绝通知

**消息类型**：`community_message`，`chatType=private`

**消息内容示例**：
- 报价：`【报价通知】询价单号：inq_xxx，商品：空调清洗，报价：150.00元，备注：含上门费`
- 接受：`用户已接受您的报价（询价单号：inq_xxx）`
- 拒绝：`用户已拒绝您的报价（询价单号：inq_xxx）`

**容错**：消息发送失败不影响询价主流程。

---

## 87. 商家动态（B3）

### 87.1 POST `/merchant-posts` — 创建动态

**角色**：merchant / property_admin / platform_admin

**请求**：
```json
{"title":"夏日促销","content":"空调清洗特惠...","imageUrls":"[\"https://...\"]","videoUrl":"https://...","status":"published"}
```

### 87.2 PUT `/merchant-posts/{id}` — 更新动态

### 87.3 DELETE `/merchant-posts/{id}` — 删除动态

### 87.4 GET `/merchant-posts/my` — 我的动态列表

**查询参数**：`page`、`pageSize`

### 87.5 GET `/merchant-posts/merchant/{merchantId}` — 指定商家的已发布动态

### 87.6 GET `/merchant-posts/{id}` — 动态详情（自动 +1 浏览量）

**响应**：
```json
{
  "id":"mp_xxx","merchantId":"mch_xxx","merchantName":"...",
  "title":"夏日促销","content":"...","imageUrls":"[\"https://...\"]",
  "videoUrl":null,"status":"published","viewCount":42,
  "createdAt":"2026-07-29T10:00:00","updatedAt":"2026-07-29T10:00:00"
}
```

---

## 88. 管理端 — 物业住户聊天（新增）

> 复用现有 `messages` 表，不新建表。物业管理员以"物业官方身份"（fromId = `official_property_{pcId}`）与住户一对一对话。住户侧通过 §12.7 `GET /messages/conversations` 查看并回复。

### 88.1 GET `/admin/messages/conversations` — 物业侧会话列表

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**Query 参数**：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| `page` | int | ❌ | 1 | 页码 |
| `pageSize` | int | ❌ | 20 | 每页条数（最大100） |
| `keyword` | String | ❌ | | 搜索关键词（住户姓名/ID） |

**成功响应**：

```json
{
  "code": 0,
  "data": {
    "list": [
      {
        "peerId": "res_b7m9x1",
        "peerName": "张先生",
        "peerAvatarUrl": "https://cdn.example.com/avatar/res_b7m9x1.jpg",
        "conversationType": "official",
        "officialSenderType": "property",
        "lastMessage": "好的，我知道了",
        "lastMessageTime": "2026-07-30T15:30:00",
        "lastMessageFromMe": false,
        "unreadCount": 1
      }
    ],
    "pagination": { "page": 1, "pageSize": 20, "total": 1, "totalPages": 1 }
  }
}
```

### 88.2 GET `/admin/messages/conversations/{residentId}` — 会话详情

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

查看与指定住户的聊天记录，按时间倒序分页。打开会话自动标记忆住户发来的未读消息为已读。

**Query 参数**：

| 参数 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| `page` | int | ❌ | 1 | 页码 |
| `pageSize` | int | ❌ | 20 | 每页条数（最大100） |

**成功响应**：

```json
{
  "code": 0,
  "data": {
    "list": [
      {
        "id": "msg_xxx",
        "fromId": "official_property_pc_demo001",
        "toId": "res_b7m9x1",
        "content": "您的物业费已到期，请尽快缴纳。",
        "messageType": "official_message",
        "chatType": "official",
        "readStatus": "read",
        "createdAt": "2026-07-30T15:30:00",
        "fromMe": true
      }
    ],
    "pagination": { "page": 1, "pageSize": 20, "total": 15, "totalPages": 1 }
  }
}
```

### 88.3 POST `/admin/messages/conversations/{residentId}` — 发送消息给住户

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**请求体**：

| 字段 | 类型 | 必填 | 校验 | 说明 |
| --- | --- | --- | --- | --- |
| `content` | String | ✅ | @NotBlank @Size(max=2000) | 消息内容 |

**请求示例**：

```json
{ "content": "您好，关于您本月物业费的问题..." }
```

**成功响应**：

```json
{
  "code": 0,
  "data": {
    "id": "msg_xxx",
    "fromId": "official_property_pc_demo001",
    "toId": "res_b7m9x1",
    "content": "您好，关于您本月物业费的问题...",
    "messageType": "official_message",
    "chatType": "official",
    "readStatus": "unread",
    "createdAt": "2026-07-30T16:00:00"
  }
}
```

> `platform_admin` 可跨物业发送；`property_admin` 仅限本物业住户。发送后通过 WebSocket 推送 `message.new` 通知住户。

---

# 89. 管理端 — 积分统计（D7）

`AdminPointStatsController`，base path `/admin/points`。**全部为真实聚合数据，不返回任何模拟值**。

**共用 Query 参数**：

| 字段 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| `month` | string | 否 | 当前月 | 月份，格式 `yyyy-MM`，如 `2026-07` |
| `propertyCompanyId` | string | 否 | — | 物业公司 ID。`platform_admin` 可指定任意物业；`property_admin` 传与否都会被**后端强制限定为本物业** |

## 89.1 GET `/admin/points/trend` — 积分月度趋势

**Auth**: Required
**Roles**: `property_admin`, `platform_admin`

**Query 参数**：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `month` | `String` | ❌ | 统计月份 `yyyy-MM`，缺省本月 |
| `propertyCompanyId` | `String` | ❌ | 物业公司ID（`platform_admin` 可指定；物业管理员自动限定本物业） |

按月聚合，返回该月每日的积分发放量与消耗量，用于折线图。

**响应** `data`（`PointTrendResponse`）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `month` | string | 统计月份 `yyyy-MM` |
| `totalEarned` | int | 当月累计发放积分 |
| `totalSpent` | int | 当月累计消耗积分 |
| `daily` | array | 每日明细 |
| `daily[].date` | string | 日期 `yyyy-MM-dd` |
| `daily[].earned` | int | 当日发放 |
| `daily[].spent` | int | 当日消耗 |

**示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/admin/points/trend?month=2026-07" \
  -H "Authorization: Bearer <token>"
```

```json
{
  "code": 0,
  "data": {
    "month": "2026-07",
    "totalEarned": 128500,
    "totalSpent": 96300,
    "daily": [
      { "date": "2026-07-01", "earned": 4200, "spent": 3100 },
      { "date": "2026-07-02", "earned": 3980, "spent": 2750 }
    ]
  }
}
```

---

## 89.2 GET `/admin/points/consumption-structure` — 积分消耗结构

**Auth**: Required
**Roles**: `property_admin`, `platform_admin`

**Query 参数**：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `month` | `String` | ❌ | 统计月份 `yyyy-MM`，缺省本月 |
| `propertyCompanyId` | `String` | ❌ | 物业公司ID（`platform_admin` 可指定；物业管理员自动限定本物业） |

按业务来源 `source` 分组聚合当月积分消耗，用于饼图 / 占比条。

**响应** `data`（`PointConsumptionStructureResponse`）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `month` | string | 统计月份 `yyyy-MM` |
| `totalSpent` | int | 当月消耗总量 |
| `items` | array | 各来源明细 |
| `items[].source` | string | 消耗来源（如 `order`、`redeem`、`delivery` 等业务标识） |
| `items[].amount` | int | 该来源消耗积分 |
| `items[].percentage` | double | 占比（百分比数值，如 `42.5` 表示 42.5%） |

**示例**：

```json
{
  "code": 0,
  "data": {
    "month": "2026-07",
    "totalSpent": 96300,
    "items": [
      { "source": "order", "amount": 40928, "percentage": 42.5 },
      { "source": "redeem", "amount": 33705, "percentage": 35.0 }
    ]
  }
}
```

---

# 91. 统一响应与观测（v7.0 新增）

> 本章补充此前文档目录中「统一响应格式」「分页规范」两个锚点缺失的正文，
> 并说明 v7.0 引入的链路追踪能力。

## 91.1 统一响应格式

所有业务接口（除少数第三方回调外）均返回统一结构 `ApiResponse<T>`：

```json
{
  "code": 0,
  "errorCode": null,
  "message": "success",
  "data": { },
  "timestamp": "2026-09-01 10:20:30",
  "traceId": "l5x2k1-9ab3cd",
  "errors": null
}
```

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `code` | int | 业务状态码，`0` 表示成功，非 0 见[错误码速查表](#错误码速查表) |
| `errorCode` | string | 错误码**枚举名**（如 `ORDER_NOT_FOUND`），仅错误时返回。**前端判等请优先用此字段**，不要依赖数字码 |
| `message` | string | 状态描述，错误时为可直接展示给用户的中文文案 |
| `data` | T | 业务数据 |
| `timestamp` | string | 服务端时间，`yyyy-MM-dd HH:mm:ss`（本地时间，序列化时生成） |
| `traceId` | string | **v7.0 新增**。链路追踪 ID，与响应头 `X-Trace-Id` 一致 |
| `errors` | array | 仅参数校验失败（400）时返回，含 `field` / `message` / `value` |

**前端适配建议**

1. 判断成功用 `code === 0`，不要用 HTTP 状态码；
2. 区分错误类型用 `errorCode` 字符串枚举，数字 `code` 仅作辅助；
3. 用户反馈问题时，请把响应体中的 `traceId`（或响应头 `X-Trace-Id`）一并提供给后端。

## 91.2 分页规范

分页接口统一返回 `PageResponse<T>`：

```json
{
  "code": 0,
  "data": {
    "list": [ ],
    "pagination": { "page": 1, "pageSize": 20, "total": 137, "totalPages": 7 }
  }
}
```

**请求参数**

| 参数 | 类型 | 默认 | 说明 |
| --- | --- | --- | --- |
| `page` | int | 1 | 页码，**1 起始** |
| `pageSize` | int | 20 | 每页条数，超过上限时自动收敛到上限（默认 100，部分接口为 50） |
| `sort` | string | — | 排序表达式，见下 |

**排序表达式 `sort`**

| 写法 | 含义 |
| --- | --- |
| `-createdAt` | 按创建时间**降序**（默认） |
| `+createdAt` | 按创建时间**升序** |
| `totalAmount,desc` / `totalAmount,asc` | 逗号形式指定方向 |
| 不传 / 空 | 使用该接口的默认排序（多数为创建时间倒序；公告为发布时间倒序，社区服务为排序值升序） |

**v7.0 统一后的行为（重要）**

| 场景 | 旧行为（各接口不一致） | v7.0 统一行为 |
| --- | --- | --- |
| `page < 1` | 部分接口抛 400（`PARAM_OUT_OF_RANGE`），部分静默取第 1 页 | 统一修正为第 1 页 |
| `pageSize > 上限` | 部分不限制（存在 OOM 风险） | 统一收敛到上限 |
| `sort` 字段不在白名单内 | 部分抛 400，部分静默降级 | 统一**静默回退为默认排序** |

> 排序字段会进入 JPQL，服务端**始终做白名单校验**，客户端无法注入任意属性名。

## 91.3 错误码与 HTTP 状态码

v7.0 起，HTTP 状态码由错误码枚举自身决定（新增错误码时编译器强制声明），不再依赖易遗漏的手工映射表。大致规则：

| 类别 | HTTP 状态码 | 示例 |
| --- | --- | --- |
| 参数缺失 / 格式错误 / 超出范围 | 400 | `PARAM_MISSING`(10001)、`PARAM_INVALID`(10002) |
| Token 缺失 / 过期 / 无效 | 401 | `TOKEN_EXPIRED`(20002) |
| 权限不足 / 账号禁用 / 功能停用 | 403 | `PERMISSION_DENIED`(20004) |
| 资源不存在 | 404 | `ORDER_NOT_FOUND`(70001) |
| 业务状态冲突 / 重复操作 | 409 | `ORDER_STATUS_INVALID`(70002)、`DISTRIBUTION_ALREADY_SETTLED`(97010) |
| 频次超限 | 429 | `DIRECTED_MSG_USER_DAILY_LIMIT`(90101) |
| 系统内部错误 | 500 | `SYSTEM_ERROR`(99001)、`ARREARS_REMINDER_PERSIST_FAILED`(97017) |

**v7.0 修正的两处状态码**

| 场景 | 旧 | v7.0 |
| --- | --- | --- |
| 请求体 JSON 语法错误 / 类型不匹配 | 500 | **400**（`PARAM_INVALID`） |
| `@Validated` 方法级参数校验失败 | 500 | **400**（`PARAM_INVALID`） |

**异常响应不再回显内部类名**：v7.0 起系统异常统一返回「内部服务异常」，详细堆栈只写入日志，通过 `traceId` 关联。

## 91.4 观测与排障

| 能力 | 说明 |
| --- | --- |
| 链路追踪 | 每个请求生成 `traceId`，写入 MDC 并出现在所有日志行与响应头 `X-Trace-Id`。上游网关可传入自定义 `X-Trace-Id` 实现跨系统串联 |
| 请求日志 | 所有 Controller 入参（经脱敏）、耗时、结果状态统一记录；超过 1 秒的请求以 `WARN` 输出 |
| 慢请求定位 | 日志中搜索 `[慢请求]` |
| 健康检查 | `GET /actuator/health`（容器已配置 `HEALTHCHECK`） |
| 日志位置 | 容器内 `/app/logs/wuye-points.log`，按天滚动、总容量 3GB |

**敏感信息不会进入日志**：手机号、身份证、银行卡号、密码、令牌等均自动脱敏（银行卡仅保留后 4 位，令牌仅保留前 8 位）。

---

# 92. 支付与分账（CBK）

> 本章补齐此前文档**完全缺失**的支付 / 分账 / 对账链路接口。

## 92.1 模块组成

| 模块 | 说明 |
| --- | --- |
| 支付渠道 | 微信支付（收付通）为主；`PayChannel` 为统一抽象 |
| 分账 | 微信电商收付通分账（冻结 → 售后窗口 → 完结 → 自动提现），实现见 `CbkSplitService` |
| 对账 | 本地 `wallet_flows` 账本 + 每日内部核对任务 |
| 兜底 | Outbox（`payment_settle_outbox`）+ 定时重试，保证支付 finalize 不丢失 |

> ⚠️ **【部分废弃 · 2026-09-03 复核】** §92.2~§92.5 为 CBK 遗留内容，代码中**无对应控制器**（`/payment/cbk/split/notify`、`/payment/cbk/withdraw/notify`、`/admin/cbk/accounts*`、`/admin/cbk/reconcile*` 均不存在）。
> §92.6（结算配置）与 §92.7（钱包）仍然有效；现行支付/分账说明见 [§96 支付与分账 · 现行架构](#96-支付与分账--现行架构v72-权威章节)。

## 92.2 分账结果回调 `POST /payment/cbk/split/notify`

**角色**：微信支付平台回调（验签 + AES-256-GCM 解密）

**请求**：微信原始报文（Body）+ 微信签名相关 Header

**响应**：

```json
{ "code": "SUCCESS", "message": "成功" }
```

**业务规则**

- 按 `out_order_no`（对应 `splitNo`）或 `order_id`（对应 `cbkSubTradeNo`）定位分账流水；
- 状态映射：`PROCESSING → frozen`、`FINISHED → frozen`、`CLOSED → freeze_failed`；
- **幂等**：流水处于终态（`finished` / `freeze_failed` / `reverted` / `refunded`）时直接应答 SUCCESS，微信重推不会重复处理；
- v7.0 起回调处理在**事务 + 行锁**下执行，避免并发重推导致的状态竞态。

> **注意**：`FINISHED` 映射为 `frozen` 是**有意设计**——仍需等待售后窗口结束后由调度器调 `finishOrder` 完结并触发自动提现，直接置为终态会导致提现链路被跳过。

## 92.3 提现结果回调 `POST /payment/cbk/withdraw/notify`

**角色**：微信支付平台回调（验签 + 解密）

**响应**：同 92.2 的 `{code, message}` 结构。

## 92.4 CBK 收款账户管理 `/admin/cbk/accounts`

> ⚠️ **安全提示**：以下为资金敏感接口，v7.0 评估中发现其缺少角色鉴权。
> 本次按「鉴权逻辑暂不改动」的原则保留了现状，但**必须在网关层或后续版本中补齐 `platform_admin` 角色校验**，
> 在补齐前请确保该路径不对外网直接暴露。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/admin/cbk/accounts` | 新增/更新收款账户（按 `ownerType`+`ownerId` 幂等 upsert） |
| POST | `/admin/cbk/accounts/batch` | 批量导入/更新 |
| GET | `/admin/cbk/accounts` | 查询，可按 `ownerType` / `ownerId` / `verified` 过滤 |
| GET | `/admin/cbk/accounts/{id}` | 详情 |
| PUT | `/admin/cbk/accounts/{id}` | 更新 |
| DELETE | `/admin/cbk/accounts/{id}` | 删除 |
| POST | `/admin/cbk/accounts/{id}/verify?verified=` | 标记核验状态 |

**请求体**（`CbkAccountUpsertReq`）：

```json
{
  "ownerType": "merchant",
  "ownerId": "mch_xxx",
  "merchantNo": "823204816000005",
  "accountNo": "6222****5678",
  "accountName": "张三",
  "verified": true
}
```

`ownerType` 取值：`property` / `merchant` / `coordinator` / `sector_leader` / `individual_leader` / `courier`。

> 出于合规要求，`accountNo` 在服务端日志中仅保留后 4 位。

## 92.5 分账对账 `/admin/cbk/reconcile`

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/admin/cbk/reconcile/failed` | 查询失败态分账记录（`freeze_failed` / `finish_failed` / `cancel_failed` / `reverse_failed`） |
| POST | `/admin/cbk/reconcile/retry/{splitNo}` | 按分账单号重试 |

> ⚠️ 同上，该路径同样缺少角色鉴权，建议在网关层限制来源或尽快补齐权限码。

## 92.6 结算配置 `/admin/settlement-config`

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/admin/settlement-config` | 查询结算配置 |
| PUT | `/admin/settlement-config` | 更新结算配置 |

## 92.7 钱包 `GET /wallet/cards`

**角色**：登录用户

返回当前用户的钱包卡面信息（余额、可提现金额等）。

## 92.8 定时任务一览（v7.0 起全部加分布式锁）

多实例部署时，以下任务由 ShedLock 保证同一时刻只有一个实例执行：

| 任务 | 触发 | 说明 |
| --- | --- | --- |
| `payment_settle_retry_pending` | 每 10 秒 | 支付 finalize 兜底 |
| `payment_settle_reconcile_pending` | 每 30 秒 | 主动查询补单 |
| `point_clear_for_arrears` | 每日 02:00 | 2 年欠费积分清零（分批短事务 + 行锁 + 幂等） |
| `coin_expiry_auto_clear` | 每日 03:00 | 物业币过期清零（库端聚合 + 行锁 + 幂等） |
| `auto_withdrawal_daily` | 每日 04:00 | 自动提现 |
| `split_finish_expired_window` | 每日 02:30 | 售后窗口完结 |
| `split_retry_failed_withdrawals` | 每日 02:50 | 提现失败重试 |
| `split_reconcile_daily` | 每日 03:10 | 每日内部对账 |
| `split_retry_skipped` | 每日 03:20 | 缺户补分 |
| `split_failure_retry` | 每 2 分钟 | 分账失败态补偿 |
| `split_reverse_failed_retry` | 每 30 分钟 | 分账退回补偿 |
| `pending_payment_close` | 每 10 分钟 | 超时未支付关单（含库存归还） |
| `delivery_property_fallback` | 每 60 秒 | 配送超时物业兜底 |
| `fulfillment_choice_timeout` | 每 60 秒 | 履约方式选择超时兜底 |
| `service_request_timeout` | 每 60 秒 | 服务需求商家响应超时 |
| `merchant_verification_auto_approve` | 每 10 分钟 | 商家核实超时自动通过 |
| `merchant_verification_settle_frozen` | 每 1 小时 | 冻结到期结算 |
| `subscription_expiry_notify` | 每日 09:00 | 订阅到期提醒 |
| `split_monitor_collection_balance` | 每日 09:00 | 归集户余额监控 |

> **幂等保证**：积分清零与物业币过期产生的扣减流水携带 `idempotency_key`
> （格式 `point_clear:{residentId}:{日期}` / `coin_expire:{residentId}:{日期}`），
> 并有数据库唯一索引兜底，任务重跑不会重复出账。

---

## 93. 前端《大体逻辑》对照（v7.1 已实现）

> 来源：`docs/开发计划_前端大体逻辑对照.md`（2026-09-02）
> 本章登记前端《大体逻辑.docx》相对现有契约的**缺口与待确认项**。
> **2026-09-03 更新**：除 93.3 / 93.5 需外部（微信）参数确认外，其余缺口已实现，端点见 [§94](#94-需求对齐新增接口v71)。

### 93.1 拿货名额与服务区间（已实现 · 对应 §3.4）

一级经销商「产品数量」= 该商品可拿货的业主商户名额，服务区间可设为**本市 / 小区**。

| 方法 | 路径 | 说明 | 状态 |
| --- | --- | --- | --- |
| `POST` | `/resident-merchants/listings` | 业主商户拿货：校验名额 > 0 后占用（`stock - 1`） | 已实现 |
| `DELETE` | `/resident-merchants/listings/{id}` | 业主商户取消拿货：释放名额（`stock + 1`） | 已实现 |
| `POST` | `/distributors/my/pickups/{listingId}/cancel` | 一级经销商取消某业主商户的拿货，释放名额 | 已实现 |
| `POST` | `/distributors/my/products` | 上架商品时可指定 `serviceScope`（`city`=本市 / `community`=小区） | 已实现 |

**校验规则**：创建拿货记录时 `stock <= 0` 驳回（`10002 参数格式错误`，提示「该产品拿货名额已满」）；取消拿货 / 经销商取消时原子 `+1`，其他业主商户可继续拿货。
**新增字段**：`distributor_products.service_scope`（`city`/`community`，NULL=沿用原逻辑）。
> 说明：当前为**产品级名额**；若后续需「按区域分别限制人数」再扩展为区域配额表。

### 93.2 奖励金流向开关（已实现 · 对应 §3.3）

按**物业公司**分别配置积分与奖励金的流向，并允许物业绑定**两张**收款银行卡（积分钱 / 奖励金）。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `property_companies.points_attribution_mode` | `varchar(20)` | `property`=到物业公司账号 / `resident`=业主自用（钱到平台） |
| `property_companies.reward_attribution_mode` | `varchar(20)` | 同上，作用于奖励金 |
| `property_bank_cards.purpose` | `varchar(20)` | `points`=积分钱卡 / `reward`=奖励金卡 |

**流程**：申请人（业主/商家）提交 `POST /reward-grants`（`grantType`=`points`/`reward`）→ 物业子账号审批 `POST /reward-grants/{id}/audit` →
审批通过后按该物业的归属模式发放：`property` 时从物业积分池扣减、`resident` 时不扣池（业主自用）。
**归属模式由后台按物业公司设置，不接受申请人指定**；业主自用场景即「物业公司同意哪一个业主使用，该业主才可使用」。

### 93.3 商家微信进件（待产品确认 · 对应 §3.5）

「帮助商家申请微信二级子账号」：当前资金架构为**普通分账（非电商收付通，无 `sub_mchid`）**，
商家收款账户通过微信分账接收方账户 `wechat_split_receiver_accounts` 绑定（`WechatSplitReceiverAccountService`）。
是否切换电商收付通二级商户需产品确认，若切换则单独排期。

### 93.4 售出即下架与卖货结算（已实现 · 对应 §3.6）

| 方法 | 路径 | 说明 | 状态 |
| --- | --- | --- | --- |
| `POST` | `/orders/from-listing` | 向业主商户下单：订单写入 `listing_id` | 已实现 |
| `GET` | `/resident-merchants/settlements` | 业主商户卖货明细（订单完成自动生成） | 已实现 |

**状态机**：订单置为 `completed`（用户确认收货 / 商家或快递员改状态）时触发 `ResidentMerchantSettlementService.settleOnOrderCompleted`：
1. 幂等校验（`resident_merchant_settlements` 按 `order_id` 唯一，重复触发不重复结算）；
2. 生成结算记录：`orderAmount`、`wholesaleAmount`（进货价）、`platformCommission`（按经销商货品佣金率，默认 10%）、`resellerProfit = 售价 - 进货价 - 平台佣金`（下限 0）；
3. 该上架记录 `enabled=false`，即**产品卖出去后自动下架**。

**新增字段**：`orders.listing_id`（业主商户上架记录ID）。

### 93.5 支付/分账收口（对应 §3.2，P0 · 仍待微信参数确认）

`/admin/cbk/*` 与 §92 框架已就绪；但**每个商家是否分配独立微信商户号**未确认，自然人（各负责人）不能误用平台商户号提现，故真实打款参数待微信侧确认后启用。
（原 `AutoWithdrawalService` / `WechatSplitApiService.applyCash` 占位打款代码无任何调用方、未产生真实资金链路，已移除以免被误认为「提现已接入」；Path B 自动提现实际走 `WechatSplitService#maybeAutoWithdraw` 的电商收付通商家转账。）

---

# 95 移动端功能性缺口修复（v7.8）

> 2026-09-05 后端修复：对照《移动端功能性缺口》《业主商城公开橱窗货对接说明》与活动组长反馈。
> 配套脚本：`manual_fix_mobile_gaps_20260905.sql`（幂等）。

## 95.1 活动组长发布活动内容 400 / 405 修复

- **现象 1（405，历史）**：前端调用 `POST /announcements/activity` 返回 405。根因：后端无此路由。已新增该端点（创建活动类型公告，公告类型强制为 `activity`）。
- **现象 2（400，本次）**：前端不传 `announcementType` 时仍返回 400「参数校验失败」。根因：该端点复用了 `POST /announcements` 的 `CreateAnnouncementRequest`，其中 `announcementType` 为 `@NotBlank` 必填；`@Valid` 在进入方法（强制写入 `activity`）之前就因缺字段失败。
- **修复**：`POST /announcements/activity` 改用独立轻量 DTO `ActivityAnnouncementRequest`（`title`/`content` 必填，`communityId`/`activityGroupId`/`publishedAt` 选填），**不再要求、也不再校验 `announcementType`**，后端强制写入 `activity`。前端即使带 `announcementType`（或中文「活动组公告」）也会被忽略，不影响发布。

| 方法 | 路径 | 角色 | 说明 |
| --- | --- | --- | --- |
| POST | `/announcements/activity` | `activity_leader` / `property_admin` / `platform_admin` | 发布活动公告，使用独立 DTO，无需传 `announcementType`（强制 `activity`）；可选 `activityGroupId` 关联到具体活动组 |

- 同时放宽：活动组长（`activity_leader`）可 `GET /announcements`（仅看 published）、`GET /announcements/{id}`、并管理**自己发布**的 `PUT /announcements/{id}` / `DELETE /announcements/{id}`（草稿等非 published 状态仅自己可见）。
- 前端原 `POST /announcements/activity` 调用无需改动即可命中（仍建议传 `announcementType: "activity"` 以兼容旧契约）。
- **数据库变更**：若需持久化 `activityGroupId`，请先执行 `sql/20260906_announcements_activity_group_id.sql` 为 `announcements` 表增加 `activity_group_id` 列（脚本幂等）。

## 95.9 活动组长更新活动组 403 修复

- **现象**：组长账号保存活动组 `PUT /activity-groups/{id}` 返回 403 / `code=20004`。
- **根因**：`ActivityGroupService.updateActivityGroup` 的权限判断仅 `resident` 角色走「组长归属校验」分支（`group.leaderId === 当前用户ID`），其余角色落入 else 分支并被要求 `property_admin`/`platform_admin`，导致 `activity_leader`（即组长本人）被误判为权限不足。
- **修复**：将 `activity_leader` / `coordinator` / `sector_leader` / `individual_leader` 一并纳入「组长归属校验」分支（通过 `RoleGuard.isLeaderRole()`）。现组长更新本人组返回 `code=0`；非本组组长仍返回 `20004`。数据库 `act_8db01000` 的 `leader_id=res_act01` 与登录组长一致，属纯代码权限问题，无需数据迁移。

## 95.10 活动组长解散活动组 405 修复

- **现象**：`DELETE /activity-groups/{id}` 返回 405「请求方法不支持」。
- **根因**：活动组 Controller 此前仅实现了 `GET`/`PUT /{id}`，`DELETE /{id}` 未接线（同类先例见 §95.1 的 405）。
- **修复**：新增 `DELETE /activity-groups/{id}` 并实现硬删除（`ActivityGroupService.disbandActivityGroup`）。权限：`activity_leader`（须为本组组长）/ `property_admin` / `platform_admin`。硬删除顺序：先删 `activity_group_subscriptions`（该表外键无 `ON DELETE CASCADE`，须显式先删），再删成员与价格档位（外键级联），最后删组本体。删除后再次 `GET` 详情返回 `90007`。

## 95.2 活动组长改自己商品价格 403 修复

- **现象**：`PUT /products/{id}` 改价返回 403。根因：原 `isOwnMerchant` 要求登录角色必须为 `merchant`，组长（`activity_leader`）等同以本人手机号注册的店铺无法管理自有商品。
- **修复**：`isOwnMerchant` 改为按「登录住户手机号 + 物业公司」与商家登记 `contactPhone`/`propertyCompanyId` 匹配，**不再限制角色**。故活动组长、个体负责人等拥有店铺者均可管理自有商品（改价/上下架/删除）。

## 95.3 活动组长查看组内成员 403 修复

- **现象**：`GET /activity-groups/{id}/members` 返回 403。根因：该接口原仅允许 `resident` 角色。
- **修复**：`GET /activity-groups/{id}/members` 与 `GET /activity-groups/{id}` 权限放宽为 `resident` / `activity_leader` / `property_admin` / `platform_admin` / `coordinator` / `sector_leader` / `individual_leader`。

## 95.4 历史活动组「业主只看到 1 个」卡死修复

- **现象**：组长能看到 3 个、业主只看到 1 个；管理端审核这些旧组时报「不在待审核状态」。
- **根因**：V8 迁移仅新增 `review_status` 列未回填；旧 active 组 `review_status=NULL`，公开列表只认 `approved`，而审核接口又要求 `pending`，形成死锁。
- **修复（代码）**：
  - 公开列表过滤 `review_status` 现兼容 `approved` 或 `NULL`（历史旧组对业主可见，无需重审）；
  - 审核接口接受 `review_status` 为 `NULL`（视作待审）。
- **修复（数据）**：执行 `manual_fix_mobile_gaps_20260905.sql` 第 1 段，将历史 active 组 `review_status` 补为 `approved`，使数据规范。业主刷新后可见全部 3 个，组长无需重建/重审。

## 95.5 商家分类白名单扩充

首页宫格所需分类现已加入商家入驻/列表/筛选白名单（`RoleGuard.MERCHANT_CATEGORIES`），前端可按 `category` 独立筛选：

| 分类（中文） | 值 | 走通道 |
| --- | --- | --- |
| 美食 | `美食` | 商家商品 `/merchants` + `/products` |
| 酒店民宿 | `酒店民宿` | 商家商品 |
| 洗浴汗蒸 | `洗浴汗蒸` | 商家商品 |
| 按摩足疗 | `按摩足疗` | 商家商品 |
| 看病买药 | `看病买药` | 商家商品（药店/购药商品） |
| 外卖小区 | `外卖小区` | 商家商品（与「外卖/外卖小吃/周围商家」并列，可作为 `GET /merchants?category=` 入参） |

> 说明：酒店民宿/洗浴汗蒸/按摩足疗/看病买药均走**商家商品**体系（入驻可选、列表可筛、可下单），不与 `life_service`（前端「综合技工」）混用，保证首页三个入口独立筛选。

## 95.6 业主商城：扁平公开货源列表 + 货源分类

- **新增接口**：

| 方法 | 路径 | 鉴权 | 说明 |
| --- | --- | --- | --- |
| GET | `/resident-merchants/public-listings` | 公开 | 单接口商品级分页，返回对外(public)+营业中(active)店铺的启用中上架商品；`propertyCompanyId` **必填**（按物业隔离，避免跨物业串货）；`keyword` 模糊匹配商品名；`category` 精确匹配货源类目 |

- 响应项：`listingId` / `residentId` / `residentName` / `category` / `productName` / `coverUrl` / `retailPrice` / `shareCount`。
- **分类来源（方案 A）**：listing 的 `category` 回带一级货源 `distributor_products.category` 字段。`distributor_products` 已加 `category` 列（见 SQL 第 2 段）。管理端/经销商在创建或修改货源商品时传入 `category`（如 `食品饮料`、`日用百货`），业主商城类目 chip 据此渲染。
- **既有 `GET /resident-merchants/public/{residentId}` 的 listings 项已补充**：`category`（货源类目）、`enabled`（上架状态）。
- 前端可改用 `public-listings` 单接口替换「先拉店列表再逐店拉详情」的 N+1 模式。

## 95.7 预约预填信息

- **新增接口**：`GET /residents/me/booking-info`（登录即可，已在 §2.x 文档化）。返回完整手机号 `contactPhone`、默认服务地址 `address`（楼栋+单元+房号）、以及 `hasPhone` / `hasAddress` 标志，供预约服务时自动带出。响应结构见 `BookingContactInfoResponse`。

## 95.8 个体负责人申请 / 退货申请

- **个体负责人住户自主申请**（历史叫法"一级代理申请"，与 §60 货源一级代理无关）：已实现 `POST /individual-leaders/apply` + `GET /individual-leaders/my-application`，文档见 §37.7（个体负责人）。无需重复开发。
- **订单退货**：已实现 `POST /orders/{id}/refund-request`（住户申请）+ `POST /admin/orders/refunds/{orderId}/audit`（管理端审核），文档见 §60.11 / §60.12。无需重复开发。
- 另：`Order` 状态白名单补充 `refund_rejected`（退货被驳回）。

## 95.11 支付试算 `POST /orders/preview` 接线（业主支付 405 修复）

- **现象**：`POST /orders/preview` 返回 **405**（未接线）。
- **修复**：新增 `POST /orders/preview`（§8.0），按物业币商城规则试算：商品小计、配送费（含团购单免配送口径）、满额减免、最大可抵扣物业币 `maxCoinAllowed`、实际抵扣 `coinUsed`、积分 `pointUsed`、现金应付 `cashAmount`、封顶原因 `reasonIfCapped`、社区食堂定向余额 `directedBalances`。仅试算不落库，支付仍以 `POST /orders/{id}/pay` 后端校验为准。
- 错误码与 §8.0 一致：`50003`（未开启抵扣，试算时不抛错、以 `reasonIfCapped` 说明）、`50004`、`50005`。

## 95.19 业主上架商品支付试算 `POST /orders/from-listing/preview` 接线（购买业主上架商品报「商品不存在」修复）

- **现象**：小程序购买业主商户上架的商品（如「张阿姨上架的业主商品」）时，支付页统一调用 `POST /orders/preview`，后端把上架/货源商品当商城商品查 `products` 表，报「商品不存在」400；即便正确走 `POST /orders/from-listing` 下单成功，支付时 `deductStock` 仍用商城 `Product` 表查 `dpr_` 货源商品 → 404。
- **修复**：
  - 新增 `POST /orders/from-listing/preview`（§8.0.0）：按业主上架商品口径试算，返回结构同 `PreviewOrderResponse`。前端支付页对上架商品改调本接口。
  - `OrderService.deductStock` / `restoreStock`：跳过 `dpr_` 前缀货源商品（拿货名额在上架/取消上架时已处理，下单仅卖一次并自动下架，不触碰商城库存）。
  - `prepareItems`：对 `rml_` / `dpr_` 前缀给出明确引导错误（提示走 `/orders/from-listing`），不再误报商品不存在。
  - 上架商品结算（差价+自动下架）沿用 `ResidentMerchantSettlementService.settleOnOrderCompleted`（§94.10），无改动。
- 无数据库变更，纯代码修复。

## 95.12 物业币使用条件 `GET /property-coins/use-condition` 接线（业主支付 404 修复）

- **现象**：`GET /property-coins/use-condition` 返回 **404**（未接线）。
  - 新增 `GET /property-coins/use-condition`（§79.1），返回 `condition` / `pointThreshold` / `satisfied` / `eligible`。无配置时 `condition=none`、`satisfied=true`，保证前端可展示。
  - `satisfied` 判定：`none`→恒满足；`point_threshold`→业主积分余额 ≥ 阈值；`property_fee_paid`→无未缴/逾期物业费；`manual_issue`→存在物业配发（source=manual）物业币流水。
  - 新增管理端 `PUT /admin/property-companies/{id}/coin-use-condition`（§79.2）供配置；`property_companies` 表 `coin_use_condition` / `coin_point_threshold` 字段已存在，无需迁移。

## 95.13 商家/组长消息接口放行（GET /messages 403 修复）

- **现象**：`GET /messages` 对 `merchant` / `activity_leader` 整接口 403。
- **修复**：
  - `GET /messages`（§12.5）角色放行 `coordinator` / `sector_leader` / `individual_leader` / `technician` / `courier` / `merchant` / `activity_leader`（与文档 §12.5 一致）；按 `fromId/toId` 参与方过滤本人相关消息，不再整接口拒绝。
  - 消息类型/聊天类型白名单补充：`official_message` / `service_request` / `merchant_ad`、`chatType=official`（与 §12.5 Query 参数一致）。

## 95.14 会话列表/历史接口接线（活动组长聊天无历史修复）

- **现象**：组长打开与组员的私聊只能发消息、无历史（`GET /messages/conversations` 与 `{peerId}` 未接线）。
- **修复**：
  - 新增 `GET /messages/conversations`（§30.2）与 `GET /messages/conversations/{peerId}`（§30.3）。
  - 角色放宽为 `resident` / `activity_leader` / `merchant` / `technician` / `courier` / `coordinator` / `sector_leader` / `individual_leader` / `property_admin` / `platform_admin`。
  - 数据范围：当前用户作为 `fromId` 或 `toId` 的私聊（含 `community_message`，活动组员私聊与此一致）；打开会话自动将对方发来的未读标记为已读。
  - `peerId` 支持 `res_`（住户）与 `official_`（官方会话键）；会话列表 `peerName` 姓名脱敏（`李*华`）。

## 95.15 活动组长商品创建/编辑放行（组长小店 403 修复）

- **现象**：`POST /products`、`PUT /products/{id}` 对 `activity_leader` 返回 403。
- **修复**：
  - `POST /products`（§6.3）Roles 补充 `activity_leader`，首次调用自动绑定 `merchant_source=group_leader` 店铺（按登录住户手机号+物业匹配，组长店 `mch_act01` 数据已核对正确）。
  - `PUT/DELETE /products/{id}`（§6.4/§6.5）不再要求角色必须为 `merchant`：`isOwnMerchant` 按「登录住户手机号 + 物业公司」匹配店铺（§95.2 口径）。
  - `GET /products` 列表：拥有店铺的账号（含组长）可查看自有商品（含未上架）。

## 95.16 组长配送费接口接线（404 + 403 修复）

- **现象**：`PUT /merchants/my/delivery-fees` 404（未接线）；`PUT /merchants/{id}` 对组长 403。
- **修复**：
  - 新增 `PUT /merchants/my/delivery-fees`（§7.2.1）：Roles `merchant` / `activity_leader` / `technician`（本人店）/ `property_admin` / `platform_admin`；按「当前用户手机号 + 物业」解析店铺，更新 `deliveryFeeTiers` / `freeDeliveryEnabled` / `freeDeliveryThreshold`。
  - `PUT /merchants/{id}`（§7.5）对组长/技工放行：`isOwnMerchant` 改为手机号 + 物业匹配、不限角色；请求体补充 `freeDeliverySponsor` / `freeDeliveryEnabled` / `deliveryScope` / `deliveryFeeTiers`（与 §7.2.1 同结构，兼容回退）。
  - `GET /merchants/my`（§7.2）Roles 补充 `activity_leader` / `technician`；响应（列表与详情）补充 `freeDeliveryEnabled` / `freeDeliverySponsor` / `deliveryScope` / `deliveryFeeTiers`。
  - 多档配送费校验：空数组=清空（回退单档 `deliveryFee`）；非空至少一个 `enabled=true`；`minKm ≥ 0`、`maxKm > minKm`（空=不限）、区间不重叠且按 `minKm` 升序，否则 `10002`。

## 95.17 组长编辑组员简介接线（404 修复）

- **现象**：`PUT /activity-groups/{id}/members/{memberId}/profile` 返回 404（未接线）。
- **修复**：
  - 新增 `PUT /activity-groups/{id}/members/{memberId}/profile`（§68.1）：更新 `ActivityGroupMember.introduction`；校验 `isSelf || isLeader`（组长以 `group.leaderId` 判定），物业/平台管理员同物业可改。
  - Roles：`resident`（本人或组长）、`activity_leader`（须为本组组长）、`coordinator` / `sector_leader` / `individual_leader`、`property_admin` / `platform_admin`。
  - 成员列表 `GET /activity-groups/{id}/members` 响应补充 `introduction` 回显。
  - 同步修正 `DELETE /activity-groups/{id}/members/{memberId}`（§18.9）角色：组长类角色（`activity_leader` 等）与文档一致可移除成员（本组组长校验）。

## 95.18 经销商拿货记录补「拿货人」

- **现象**：`GET /distributors/my/pickups`（§94.4）拿货人字段为空（响应未返回姓名）。
- **修复**：列表项固定返回 `residentId` + **`residentName`**（脱敏：`张阿姨` → `张*姨`）+ **`residentPhone`**（脱敏 `138****1234`），不再只回 ID；私密橱窗同样按商家侧权限返回，不依赖公开橱窗。

---

## 95.20 商家列表支持「官方/物业推荐」过滤（§7.1，前端三列表拆分）
- **现象**：前端需拆分三套商家列表：物业推荐 / 官方全部商家 / 物业公司模块，但 `/merchants` 无区分推荐与否的参数，无法实现「物业删除/取消推荐的商家仅从物业推荐隐藏，仍在官方全部与物业公司模块可见」。
- **修复**：`GET /merchants` 新增可选参数 **`recommended`**（`Boolean`）：`recommended=true`=仅返回官方/物业推荐商家（物业推荐列表）；不传=返回全部审核通过商家（官方全部、物业公司模块）。列表项响应新增 **`isRecommended`**、**`recommendedSort`** 字段。
- **约定**：物业侧「取消推荐/删除」（§75.2 `PUT /admin/merchants/{id}/recommend`）仅将 `isRecommended` 置 `false`，不隐藏商家本身；故官方全部与物业公司模块（不传 `recommended`）仍展示，仅物业推荐列表（`recommended=true`）不再展示。无 DB 变更。

## 95.21 物业币转物业改为仅「家庭物业币」可转（§2.6.4.2）
- **现象**：原来 `POST /residents/me/coins/transfer-to-property` 扣**个人**余额；按账户体系边界，应仅**家庭物业币**可转给物业、**个人物业币不可转**。
- **修复**：改为仅扣**家庭池**。用户未加入家庭返回 `10002`；家庭余额不足返回 `50001`；流水 `coin_type=spend`、`source=transfer_to_property`。两个入口（`/property-coins/transfer-to-property` 别名）行为一致。无 DB 变更。

---

## 95.22 微信支付主链路三处缺陷修复（库存重复扣减 / 未支付订单库存泄漏 / 库存并发超卖）

- **① 支付成功回调二次扣库存（阻断）**：`POST /orders/{id}/pay`（§8.2）预下单时已**预占**扣减商城库存，而回调落账 `completeChannelPayment` 又扣一次 → 库存充足时静默超卖；**末件商品**时回调抛「商品库存不足」，finalize 重试 10 次后置 `FAILED_PERMANENT`，订单永久停留 `pending`（钱已收、单未完成）。
  **修复**：回调落账**不再扣减**库存；库存仅由预下单预占，由取消 / 超时关单归还（`PendingPaymentScheduler`，§21652）。
- **② 未支付订单取消泄漏预占库存**：`POST /orders/{id}/cancel`（§8.3）的归还库存分支漏了 `pending` 状态，而超时调度器仅在订单仍为 `pending` 时才补归还 → 用户主动取消未支付订单后，预占库存永久泄漏。
  **修复**：`pending` 纳入库存归还分支（未支付订单的退款 / 返奖 / 现金退款分支均为空操作，不产生多余流水）。
- **③ 库存操作统一收口，消除并发超卖**：`OrderService.deductStock` / `restoreStock` 原为无锁 `findById → setStock → save`（`ProductStockService` 的注释虽声称已收敛，实际未生效），现统一委托 `ProductStockService`（`findByIdForUpdate` 行锁 + `@Version` 乐观锁）。`dpr_` 业主上架货源商品仍跳过商城库存，口径与 §95.19 一致。
- **无接口签名 / 字段变更，无数据库变更。**

---

> **文档维护**：连云港元序软件有限公司
> **最后更新**：2026-09-06（v7.9.1 前端问题汇总 7 项修复：orders/preview 405、use-condition 404、商家消息 403、组长聊天历史、组长商品 403、组长配送费 404/403、组员简介 404、拿货人缺失）
> **2026-09-13 增补**：新增 §97「电商收付通改造」（商家微信进件 / 四层树收款账号 / 分账回退台账），见文末。
> **2026-10-08 增补**：§95.22 微信支付主链路三处缺陷修复（回调二次扣库存 / 未支付订单库存泄漏 / 库存无锁并发超卖）；另修复 `payment_records.directed_balance_used` NOT NULL 未赋值导致的「数据保存失败（数据库约束冲突）」。

---

# 97. 电商收付通改造（阶段1 + 阶段2 · 2026-09-13 新增）

> **方案文档**：`.trae/documents/电商收付通改造方案.md`
> **数据库脚本**：`docs/sql/电商收付通改造_合并脚本.sql`（**需手动执行，幂等可重复**）
> **前端交付**：`docs/电商收付通改造_前端交付_2026-09-13.html`
> **总开关**：`wechat-ecommerce.enabled`（默认 `false`）。服务商资质与商家进件就绪后置 `true`，届时分账走电商收付通接口（`/v3/ecommerce/profitsharing/*`），普通分账（`/v3/profitsharing/*`）停用。

## 97.0 业务背景与资金流

原「微信普通分账」模式：货款先进平台直连商户号，平台再向各接收方分账，存在平台归集资金的二清合规风险。改造为**微信电商收付通（平台收付通）**后：

```
用户付款 ──直连──▶ 商家二级商户号(sub_mchid)   ← 资金不经过平台账户
                        │
                        ├─ 平台发起分账(冻结)：
                        │    ├─ 平台佣金 ──▶ 平台商户号(收付通进件)
                        │    └─ 配送费   ──▶ 快递员 openid(PERSONAL_OPENID，需预授权)
                        │
                        └─ 售后窗口到期 → 完结(finish) → 剩余货款解冻给商家

四层分成树（物业/统筹/板块/个体）不走微信分账：
平台佣金到账后，由「微信商家转账（新版）」从平台账户出款至各角色
绑定的微信零钱 / 银行卡（零钱走免确认授权模式）。平台内部结算，合规无二清。
```

| 参与方 | 体感 / 动作 |
| --- | --- |
| 商家（merchant / 组长 / 技工） | 「我的店铺 → 微信收款」提交进件资料；资料驳回可改重提；**未完成进件前不可用微信在线支付**（回退积分/物业币支付，错误码 `99004`） |
| 运营后台（property_admin / platform_admin） | 查看商家进件状态、手动提交/刷新进件；审核商家入驻时可跟进进件链路 |
| 法人/经营者 | 微信内完成人脸核验 + 签约（链接由进件接口返回，有实效） |
| 快递员 | 配送费分账直发 openid（收款前需在微信侧授权）；退款时不可回退，由平台垫付并登记待追回台账 |
| 四层树角色（物业/统筹/板块/个体负责人） | 「我的收款方式」绑定本人微信零钱 openid 或银行卡，作为内部结算出款收款账户 |
| 平台财务 | 管理端查询/核销「分账回退待追回台账」，线下追回后标记 |

## 97.1 进件状态机（applymentState）

```
init（资料草稿）
  │  POST /merchants/my/applyment/submit（或运营后台 submit）
  ▼
submitted → auditing（微信审核）
  │          ├─ TO_BE_CONFIRMED → legal_validating（超管扫 sign_url 完成账户验证）
  │          └─ TO_BE_SIGNED → signing（超管扫 sign_url 签约）
  ▼
finished（进件成功，回填 sub_mchid）  /  rejected（驳回，可改资料重提）
```

- 本地状态 `init/submitted/auditing/legal_validating/signing/finished/rejected`；`submitted/auditing/legal_validating/signing` 为非终态，商家 GET 查询会按 15 秒节流实时向微信查询，`ApplymentStatusScheduler` 每日 01:10 兜底。
- **终态（finished / rejected）不可再改资料**；驳回后可重新保存资料并重提（重新进件）。

## 97.2 商家端 — 微信进件（`/merchants/my/applyment`）

**角色**：`merchant` / `activity_leader` / `technician`（仅本人店铺）。敏感字段（证件号、银行卡号、开户名、身份证居住地址、超管姓名/手机/邮箱、UBO 姓名/号码/地址）后端以微信支付公钥/平台证书 RSA-OAEP 加密落库并上送，**不对外回显**。

### 97.2.1 GET `/merchants/my/applyment` — 查询我的进件状态

审核中（`submitted/auditing/legal_validating/signing`）会顺带查询微信状态（15 秒节流），前端 30 秒轮询即可及时拿到 `signUrl`；每日 01:10 另有兜底任务。

**响应** `data`（`ApplymentDetailResponse`）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `merchantId` / `applymentId` | string | 商家ID / 微信申请单号 |
| `applymentState` | string | `init/submitted/auditing/legal_validating/signing/finished/rejected` |
| `wxState` | string | 微信侧 `APPLYMENT_STATE_*` 原值 |
| `organizationType` | string | `ENTERPRISE` / `INDIVIDUAL` |
| `merchantShortname` / `servicePhone` | string | 商户简称 / 客服电话 |
| `qualificationType` | string | 所属行业 |
| `salesScenesType` / `salesAppId` / `salesSubAppId` / `salesPics` | — | 经营场景与截图 |
| `bizStoreName` / `bizAddressCode` / `bizStoreAddress` / `storeEntrancePics` / `storeIndoorPics` | — | 门店场景资料 |
| `businessLicenseNumber` / `merchantName` / `legalPerson` / `licenseAddress` / `licensePeriodBegin` / `licensePeriodEnd` | — | 营业执照非敏感字段 |
| `idCardPeriodBegin` / `idCardPeriodEnd` | string | 法人证件有效期 |
| `contactMobile` | string | 超管手机号（脱敏；重填需重新输入完整号码） |
| `contactType` / `contactName` | string | 超管类型/姓名 |
| `bankAccountType` / `accountBank` / `bankAliasCode` / `bankBranchId` / `bankName` | — | 结算账户非敏感字段 |
| `submitted` | boolean | 是否已提交微信（有 applyment_id） |
| `signUrl` | string | 超级管理员签约链接（转二维码给法人扫码） |
| `rejectReason` / `subMchid` / `submitMode` / `lastError` | — | 驳回原因/二级商户号/提交方式/最近错误 |

> 敏感项（`idCardName`、`idCardNumber`、`idCardAddress`、`bankAccountName`、`bankAccountNumber`、`contactEmail`）不回显，驳回后重填需重新输入；图片 media_id 3 天过期，重提需重新上传。

### 97.2.2 POST `/merchants/my/applyment` — 保存/更新进件资料

**说明**：审核中（非终态）不可修改；驳回后可改资料重提。提交后如配置 `wechat.applyment.auto-submit=true` 会自动进件，否则停留在 `init` 等待调用 `/submit`。

**请求体** `ApplymentSubmitRequest`（字段详见附录 D.2）：主体/执照/法人证件/经营场景/行业/结算账户/超管；`settlementId` 已移除，结算规则由后端按主体类型自动匹配（企业 716 / 个体户 719），商家不可指定费率。

**响应**：`ApplymentDetailResponse`（同 97.2.1）。

### 97.2.3 POST `/merchants/my/applyment/submit` — 提交进件到微信

**说明**：资料就绪后提交，进入微信审核流程（`submitted`）。重复提交返回 `APPLYMENT_STATE_INVALID`。

**响应**：`ApplymentDetailResponse`。

### 97.2.4 POST `/merchants/my/applyment/media` — 上传进件资料图片

**Content-Type**：`multipart/form-data`，参数名 `file`；仅支持 JPG/JPEG/PNG/BMP，≤5MB。
后端按官方《文件上传》自动计算 sha256 并同时上送 `meta`（签名主体为 meta JSON），前端**不需要**自行计算摘要。
**响应** `data`：`{ "mediaId": "xxxx" }`（有效期约 3 天）。
> 上面是统一信封里的 `data` 段，完整响应体为 `{ "code": 0, "message": "success", "data": { "mediaId": "xxxx" }, "timestamp": "...", "traceId": "..." }`；前端取 `res.data.data.mediaId`，**不存在裸 `{ "mediaId" }` 返回**。
> 部署前置：反代 `client_max_body_size` 需 ≥12m。nginx 默认 1m 时，超过 1MB 的图片会在 nginx 层直接返回 HTML 413，请求进不到应用、后端日志查不到该请求。

### 97.2.5 GET `/merchants/my/applyment/industries` — 可选行业与结算规则

**响应** `data`：

```json
{
  "defaultIndustry": "居民生活服务",
  "industries": [{"name": "居民生活服务", "needsQualification": false}],
  "specialRuleIndustries": ["加油/加气", "快递", "物流", "民办学校（非全国高等学校）"],
  "settlementRules": {"ENTERPRISE": "716", "INDIVIDUAL": "719"}
}
```

前端行业下拉直接用该接口，不要硬编码，避免与后端规则漂移。

## 97.3 管理端 — 微信进件（`/admin/merchants/{id}/applyment`）

**角色**：`property_admin` / `platform_admin`。运营后台负责商家资料的人工提交与状态推进。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/admin/merchants/{id}/applyment` | 查看商家进件详情（额外可见 `retryCount` / `lastError`） |
| `POST` | `/admin/merchants/{id}/applyment/submit` | 手动提交商家进件到微信 |
| `POST` | `/admin/merchants/{id}/applyment/refresh` | 手动刷新进件状态（查询微信侧并推进本地状态机） |

响应均为 `ApplymentDetailResponse`（同 97.2.1）。

## 97.4 四层树角色 — 收款账号绑定（`/transfer-accounts`）

**角色**：`property_admin`(PROPERTY) / `coordinator`(统筹) / `sector_leader`(板块) / `individual_leader`(个体)。绑定本人收款渠道，作为**平台内部结算（微信商家转账）出款**的收款账户。银行卡/持卡人明文提交，后端以平台证书加密落库，响应不回显明文。

### 97.4.1 POST `/transfer-accounts` — 绑定我的收款账号

**说明**：`channel=zero` 填 `openid`；`channel=bank` 填卡号/开户行/持卡人。同渠道重复绑定 = 覆盖更新。

**请求体** `BindTransferAccountRequest`：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `channel` | string | ✅ | `zero`(微信零钱) / `bank`(银行卡) |
| `openid` | string | zero 时 | 微信 openid |
| `bankAccountNo` | string | bank 时 | 银行卡号（明文提交，加密落库） |
| `bankName` | string | bank 时 | 开户银行全称 |
| `accountName` | string | bank 时 | 持卡人姓名 |

**响应** `data`（`TransferAccountResponse`）：`id` / `ownerType` / `channel` / `openid`（zero 渠道）/ `bankName`（bank 渠道）/ `verified`（是否已核验可用）。

### 97.4.2 GET `/transfer-accounts/my` — 查询我的收款账号列表

**响应**：`TransferAccountResponse[]`（银行卡号/持卡人不回显）。

### 97.4.3 DELETE `/transfer-accounts/my?channel=zero|bank` — 解绑我的收款账号

**响应**：`data=null`。

## 97.5 管理端 — 分账回退待追回台账（`/admin/split-recovery`）

**角色**：`property_admin` / `platform_admin`。

**背景**：电商收付通模式下，退款时个人类型接收方（快递员 `PERSONAL_OPENID`）**不支持微信回退**，平台先行垫付并登记台账；运营线下追回（对账催收 / 后续配送费抵扣）后核销。

**状态**：`pending`（待追回）→ `recovered`（已追回）/ `written_off`（已核销）。

### 97.5.1 GET `/admin/split-recovery` — 台账分页查询

**Query 参数**：`page` / `pageSize` / `sort`（默认 `-createdAt`）/ `status`（pending/recovered/written_off）/ `ownerType`（如 `COURIER`）。

**响应** `data`（`PageResponse<RecoveryRecordResponse>`），列表项字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | string | 台账ID |
| `splitRecordId` | string | 分账记录ID（wechat_split_record.id） |
| `orderId` | string | 订单ID |
| `ownerType` | string | 角色：`COURIER` 等 |
| `ownerId` | string | 角色ID |
| `amount` | decimal | 待追回金额（元） |
| `status` | string | pending/recovered/written_off |
| `remark` | string | 备注 |
| `createdAt` | string | 登记时间 |

### 97.5.2 POST `/admin/split-recovery/{id}/recovered` — 标记已追回

**请求体**（可选）`UpdateRecoveryRequest`：`remark`（≤500，线下追回记录）。
仅 `pending` 可操作，否则 `10002`。

### 97.5.3 POST `/admin/split-recovery/{id}/write-off` — 核销（坏账）

**请求体**（可选，`UpdateRecoveryRequest`）：

| 字段 | 类型 | 必填 | 校验 | 说明 |
| --- | --- | --- | --- | --- |
| `remark` | `String` | ❌ | `@Size(max=500)` | 核销原因/线下处理记录 |

仅 `pending` 可操作，否则 `10002`。核销后不再追回。

## 97.6 微信回调地址（服务端配置，前端无需对接）

| 用途 | 路径（`wechat-ecommerce.*` 配置） | 说明 |
| --- | --- | --- |
| 分账结果 | `POST /payment/ecommerce-split/notify`（`notify-url`） | 验签 + 幂等修正分账流水状态；成功应答 `{code:"SUCCESS"}` |
| 商家转账结果 | `POST /payment/ecommerce-transfer/notify`（`transfer-notify-url`） | 验签 + 落地转账状态；成功应答 `{code:"SUCCESS"}` |
| 进件结果 | `applyment-notify-url`（预留占位） | 进件无真实回调，状态由 `ApplymentStatusScheduler` 定时查询兜底 |

## 97.7 配置项（`application.yml` → `wechat-ecommerce`）

| 配置项 | 默认 | 说明 |
| --- | --- | --- |
| `enabled` | `false` | 电商收付通分账总开关（资质与进件就绪后置 true） |
| `platform-receiver-mch-id` | 空 | 平台收款商户号（分账给平台/物业时使用；留空取服务商号） |
| `platform-sub-mch-id` | 空 | 平台自身二级商户号（若平台进件为二级商户） |
| `receiver-relation-type` | `SERVICE_PROVIDER` | 分账接收方关系类型（PERSONAL_OPENID 需微信侧授权） |
| `notify-url` | — | 分账结果回调地址 |
| `applyment-auto-submit` | `false` | 商家平台审核通过后是否自动提交进件（默认运营后台手动） |
| `applyment-notify-url` | — | 进件结果回调（预留） |
| `transfer-channel` | `zero` | 内部结算转账默认渠道：`zero`(零钱) / `bank`(银行卡) |
| `transfer-notify-url` | — | 商家转账结果回调地址 |

## 97.8 新增错误码（99xxx）

| code | errorCode | HTTP | 含义 |
| --- | --- | --- | --- |
| `99002` | `WECHAT_API_ERROR` | 502 | 微信接口异常 |
| `99003` | `WECHAT_ECOMMERCE_NOT_CONFIGURED` | 400 | 电商收付通未配置或未启用 |
| `99004` | `ORDER_PAY_UNAVAILABLE` | 400 | 该商家暂未完成微信进件，请使用积分/物业币支付 |
| `99005` | `WECHAT_CERT_NOT_READY` | 502 | 微信平台证书未就绪，无法加密敏感信息 |
| `99006` | `APPLYMENT_NOT_FOUND` | 404 | 微信进件申请不存在 |
| `99007` | `APPLYMENT_STATE_INVALID` | 400 | 进件申请状态不允许该操作 |
| `99008` | `APPLYMENT_ALREADY_EXISTS` | 400 | 该商家已存在进件申请 |
| `99009` | `APPLYMENT_UPLOAD_FAILED` | 502 | 进件资料上传微信失败 |
| `99010` | `TRANSFER_ACCOUNT_NOT_VERIFIED` | 400 | 收款账号未核验，无法转账 |
| `99011` | `TRANSFER_NOT_FOUND` | 404 | 转账单不存在 |
| `99012` | `TRANSFER_AMOUNT_INVALID` | 400 | 转账金额不合法 |
| `99013` | `RECOVERY_NOT_FOUND` | 404 | 待追回台账记录不存在 |

## 97.9 数据库变更（`docs/sql/电商收付通改造_合并脚本.sql`，需手动执行）

| 表 | 说明 |
| --- | --- |
| `merchant_wechat_accounts` | 商家 ↔ 微信二级商户号（sub_mchid）映射 |
| `merchant_applyments` | 商家进件申请（敏感字段加密落库：`id_card_*_enc` / `bank_account_*_enc`） |
| `merchant_transfer_accounts` | 四层树角色收款账号（含 `cert_serial` 记录加密所用平台证书序列号） |
| `merchant_transfers` | 平台内部结算转账单（状态机：pending → processing → success/failed） |
| `split_recovery_records` | 分账回退待追回台账 |
| `payment_records.sub_mchid` | 新增列：电商收付通支付的二级商户号 |

## 97.10 定时任务（均加 ShedLock）

| 任务 | 频率 | 说明 |
| --- | --- | --- |
| `ApplymentStatusScheduler` | 定时 | 查询微信侧进件状态并推进本地状态机 |
| `TransferScheduler` | 5 分钟 | 处理 pending 转账、查询 processing 转账结果 |
| `RecoveryScheduler` | 每日 04:00 | 待追回台账告警提醒 |

## 97.11 上线前确认清单（代码已就绪，等待资质/数据）

1. 服务商资质：电商收付通 / 商家转账（免确认授权模式，`transfer_scene_id=1005`）开通；服务商号需为**未进件过的全新商户号**（普通服务商若进件过特约商户则无法开通平台收付通）。
2. 进件流程：商家提交资料 → 微信审核(1-2 工作日) → 法人扫码人脸核验 → 签约 → 回填 `sub_mchid`。
3. 小微商户（无营业执照）支付限额：单日收款额度有限，信用卡日限额 1000 元。
4. 快递员 openid 需预授权为分账接收方（PERSONAL_OPENID）。
5. 分账比例：单笔分账累计金额 ≤ 实付金额 30%（需确认佣金 + 配送费 ≤ 30% 实付）。
6. 未进件商家：前端回退「积分 / 物业币」支付；不展示微信支付入口或提示 `99004`。
7. 交付件：本 API 章节 + `docs/电商收付通改造_前端交付_2026-09-13.html` + 合并 SQL 脚本。

---

## 98. 优惠券模块 (Coupons)

<a id="98-优惠券模块"></a>

> 优惠券**不是商品**，而是独立的功能模块。下单时作为抵扣项作用于**商品金额**，不参与配送费抵扣。
> 数据库变更见 `backend/src/main/resources/db/migration/V17__coupon.sql`（生产禁用 Flyway，需手动执行）。

### 98.0 业务口径

| 维度 | 取值 | 说明 |
| --- | --- | --- |
| 券类型 `type` | `full_reduction` 满减 / `discount` 折扣 / `instant_reduction` 立减 | 满减、立减用 `valueAmount`；折扣用 `discountRate`（8.5 = 8.5 折），可选 `maxDiscountAmount` 封顶 |
| 适用范围 `scope` | `platform` 平台券 / `merchant` 商家券 | 平台券全平台通用；商家券限指定商家 |
| 到账方式 `source` | `claim` 领券中心自助领取 / `grant` 商家或平台定向发放 | 领取受"发放总量 + 每人限领"约束；发放为管理员/商家主动派发到账户 |
| 有效期 `validType` | `fixed` 固定起止 / `relative` 领取后 N 天 | `fixed` 用 `validStartAt`/`validEndAt`；`relative` 用 `validDays`，实际过期时间在到账时计算 |
| 使用门槛 `minSpendAmount` | 订单**商品金额**（不含配送费）需 ≥ 该值 | 不满足则不可用（`usable=false`） |

**抵扣计算规则**（`productAmount` = 订单商品金额，不含配送费）：

| 券类型 | 抵扣额 |
| --- | --- |
| `full_reduction` / `instant_reduction` | `min(valueAmount, productAmount)` |
| `discount` | `min(productAmount × (10 − discountRate) / 10, maxDiscountAmount, productAmount)`，先判断 `productAmount ≥ minSpendAmount` |
| 未达门槛 | `0`（不可用） |

**分账口径**：优惠金额**由商家承担**；平台佣金、快递配送费均按**实付商品金额**（`totalAmount − couponAmount`）计算。订单 `totalAmount` 仍保存原始商品金额，另存 `couponId`/`couponAmount` 作为审计与分账基数依据；**微信电商分账链路逻辑不变**。

**券状态机**：`unused`（可用）→ `locked`（下单支付中锁定）→ `used`（支付成功核销）；订单取消或超时未支付自动 `release` 回 `unused`；过期由查询时惰性置 `expired`。

### 98.0.1 数据表

| 表 | 说明 |
| --- | --- |
| `coupon_template` | 券模板（名称/类型/范围/面额/门槛/有效期/总量/已领量/每人限领/状态） |
| `user_coupon` | 用户券（模板快照 + `status` + `source` + `expireAt`/`claimedAt`/`usedAt`/`orderId`） |
| `orders.coupon_id` | 新增列：本单使用的用户券 ID |
| `orders.coupon_amount` | 新增列：优惠券抵扣金额（`NOT NULL DEFAULT 0.00`） |

---

### 98.1 GET `/coupons/center` — 领券中心

**Auth**: Required  
**Roles**: `resident`

返回当前用户**可领取**的券模板列表（平台券全局 + 本物业商家券，`status=active` 且未领完）。

**Query 参数**：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `merchantId` | `String` | ❌ | 仅查看某商家的商家券 |

**响应**：`Array<CouponTemplateResponse>`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `String` | 模板 ID（`cpt_` 前缀） |
| `name` | `String` | 券名称 |
| `description` | `String` | 使用说明 |
| `type` | `String` | `full_reduction`/`discount`/`instant_reduction` |
| `scope` | `String` | `platform`/`merchant` |
| `merchantId` / `merchantName` | `String` | 商家券的商家信息（平台券为 null） |
| `valueAmount` | `BigDecimal` | 面额（满减/立减） |
| `discountRate` | `BigDecimal` | 折扣率（折扣券，8.50 表示 8.5 折） |
| `maxDiscountAmount` | `BigDecimal` | 折扣封顶金额（折扣券，可空） |
| `minSpendAmount` | `BigDecimal` | 使用门槛 |
| `validType` | `String` | `fixed`/`relative` |
| `validStartAt` / `validEndAt` | `String` | 固定有效期起止（`fixed`） |
| `validDays` | `Integer` | 领取后有效天数（`relative`） |
| `totalQuantity` | `Integer` | 发放总量（null = 不限量） |
| `claimedQuantity` | `Integer` | 已领取数量 |
| `remainingQuantity` | `Integer` | 剩余数量（null = 不限量） |
| `perUserLimit` | `Integer` | 每人限领张数 |
| `status` | `String` | `active`/`inactive` |
| `claimedByMe` | `Boolean` | 当前用户是否已达领取上限 |
| `createdAt` | `String` | 创建时间 |

**错误响应**：无特殊错误码（未登录返回 `10001`/`TOKEN_MISSING` 等通用鉴权错误）。

---

### 98.2 POST `/coupons/{templateId}/claim` — 领取优惠券

**Auth**: Required  
**Roles**: `resident`

自助领取。校验：模板 `active`、范围可见（平台券或本物业商家券）、未领完、未达每人限领。领取后按 `validType` 计算 `expireAt` 落库，模板 `claimedQuantity` +1。

**Path 参数**：`templateId` 券模板 ID（`cpt_` 前缀）

**响应**：`UserCouponResponse`（结构见 §98.3）

**错误响应**：

| 错误码 | HTTP | 说明 |
| --- | --- | --- |
| `60040` | 404 | 券模板不存在 |
| `60047` | 400 | 优惠券已被领完 |
| `60048` | 400 | 已达到每人限领上限 |
| `60049` | 400 | 该优惠券不可领取（非 active 或范围不可见） |

---

### 98.3 GET `/coupons/my` — 我的优惠券

**Auth**: Required  
**Roles**: `resident`

**Query 参数**：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `status` | `String` | ❌ | `unused`/`locked`/`used`/`expired`，不传 = 全部 |

**响应**：`Array<UserCouponResponse>`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `String` | 用户券 ID（`ucp_` 前缀，下单选券时回传该值） |
| `templateId` | `String` | 券模板 ID |
| `name` | `String` | 券名称（模板快照） |
| `type` | `String` | 券类型 |
| `scope` | `String` | 券范围（按是否有 `merchantId` 推导） |
| `merchantId` / `merchantName` | `String` | 限定商家（平台券为 null） |
| `valueAmount` / `discountRate` / `maxDiscountAmount` / `minSpendAmount` | `BigDecimal` | 面额快照（同模板） |
| `status` | `String` | `unused`/`locked`/`used`/`expired` |
| `source` | `String` | `claim`/`grant` |
| `expireAt` | `String` | 过期时间 |
| `claimedAt` | `String` | 到账时间 |
| `usedAt` | `String` | 核销时间 |
| `orderId` | `String` | 核销订单 ID |
| `expired` | `Boolean` | 按当前时间计算的过期标记 |

> 查询 `unused` 券时会顺带把已过期的券惰性更新为 `expired`。

---

### 98.4 GET `/coupons/available` — 下单可用券

**Auth**: Required  
**Roles**: `resident`

按商家与订单**商品金额**试算当前用户可用券及抵扣额，供下单页选择。

**Query 参数**：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `merchantId` | `String` | ✅ | 订单商家 ID |
| `productAmount` | `BigDecimal` | ❌ | 订单商品金额（不含配送费），不传按 0 计算 |

**响应**：`Array<AvailableCouponResponse>`（按 `discountAmount` 倒序）

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `String` | 用户券 ID（下单回传） |
| `templateId` | `String` | 券模板 ID |
| `name` | `String` | 券名称 |
| `type` / `scope` | `String` | 券类型 / 范围 |
| `merchantId` | `String` | 限定商家 |
| `valueAmount` / `discountRate` / `maxDiscountAmount` / `minSpendAmount` | `BigDecimal` | 面额快照 |
| `expireAt` | `String` | 过期时间 |
| `discountAmount` | `BigDecimal` | 本单可抵扣金额 |
| `usable` | `Boolean` | 是否满足使用门槛（未达门槛时 `discountAmount=0`、`usable=false`） |

---

### 98.5 管理端 — 券模板管理

**Roles**: `platform_admin`、`property_admin`

> 权限：平台券仅 `platform_admin` 可创建/管理；`property_admin` 仅能管理**本物业**的商家券。

#### 98.5.1 GET `/admin/coupons` — 券模板列表

**Query 参数**：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `page` | `Integer` | ❌ | 默认 1 |
| `pageSize` | `Integer` | ❌ | 默认 20，最大 100 |
| `scope` | `String` | ❌ | `platform`/`merchant` |
| `status` | `String` | ❌ | `active`/`inactive` |
| `merchantId` | `String` | ❌ | 按商家过滤 |
| `propertyCompanyId` | `String` | ❌ | 仅 `platform_admin` 可用 |

**响应**：`PageResponse<CouponTemplateResponse>`（`list` + `pagination`，元素结构同 §98.1）

#### 98.5.2 GET `/admin/coupons/{id}` — 券模板详情

**响应**：`CouponTemplateResponse`

#### 98.5.3 POST `/admin/coupons` — 创建券模板

**请求体**（`CreateCouponTemplateRequest`）：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `name` | `String` | ✅ | 券名称，≤100 字 |
| `description` | `String` | ❌ | 使用说明，≤500 字 |
| `type` | `String` | ✅ | `full_reduction`/`discount`/`instant_reduction` |
| `scope` | `String` | ✅ | `platform`/`merchant` |
| `merchantId` | `String` | 条件 | `scope=merchant` 时必填 |
| `valueAmount` | `BigDecimal` | 条件 | 满减/立减必填，>0 |
| `discountRate` | `BigDecimal` | 条件 | 折扣券必填，0~10（8.5 = 8.5 折） |
| `maxDiscountAmount` | `BigDecimal` | ❌ | 折扣券封顶金额 |
| `minSpendAmount` | `BigDecimal` | ❌ | 使用门槛，默认 0 |
| `validType` | `String` | ✅ | `fixed`/`relative` |
| `validStartAt` | `String` | 条件 | `fixed` 必填，格式 `yyyy-MM-dd HH:mm:ss` |
| `validEndAt` | `String` | 条件 | `fixed` 必填，需晚于开始时间 |
| `validDays` | `Integer` | 条件 | `relative` 必填，≥1 |
| `totalQuantity` | `Integer` | ❌ | 发放总量，不传 = 不限量 |
| `perUserLimit` | `Integer` | ✅ | 每人限领，≥1 |
| `status` | `String` | ❌ | `active`（默认）/`inactive` |

**响应**：`CouponTemplateResponse`

#### 98.5.4 PUT `/admin/coupons/{id}` — 更新券模板

**请求体**（`UpdateCouponTemplateRequest`，字段全可选）：`name`/`description`/`valueAmount`/`discountRate`/`maxDiscountAmount`/`minSpendAmount`/`validStartAt`/`validEndAt`/`validDays`/`totalQuantity`/`perUserLimit`/`status`。

> **约束**：券模板**已发放**（`claimedQuantity > 0`）后，**禁止**再修改 `valueAmount`/`discountRate`/`maxDiscountAmount`/`minSpendAmount`（返回 `60050`）。

**响应**：`CouponTemplateResponse`

#### 98.5.5 PUT `/admin/coupons/{id}/status` — 启用/停用

**请求体**：`{ "status": "active" | "inactive" }`

**响应**：`CouponTemplateResponse`

#### 98.5.6 POST `/admin/coupons/grant` — 定向发放

**请求体**（`GrantCouponRequest`）：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `templateId` | `String` | ✅ | 券模板 ID |
| `residentIds` | `Array<String>` | ✅ | 发放对象（业主/居民 ID 列表） |
| `quantity` | `Integer` | ❌ | 每人发放张数，默认 1（受每人限领与剩余总量约束，自动截断） |

**响应**：`{ "grantedCount": 5 }`（实际成功发放张数）

---

### 98.6 商家端 — 本商家优惠券

**Roles**: `merchant`（商家身份由登录态解析）

#### 98.6.1 GET `/merchant/coupons` — 本商家券模板列表

**响应**：`Array<CouponTemplateResponse>`（仅返回 `scope=merchant` 且属于本商家的券，最多 100 条）

#### 98.6.2 POST `/merchant/coupons/grant` — 定向发放本商家券

**请求体**：同 §98.5.6 `GrantCouponRequest`

> 商家只能发放**自己的商家券**；发放平台券或他人商家券返回 `PERMISSION_DENIED`。

**响应**：`{ "grantedCount": n }`

---

### 98.7 订单集成说明

| 接口 | 新增字段 | 说明 |
| --- | --- | --- |
| `POST /orders/preview`（§8.0） | 请求 `userCouponId`；响应 `couponId`、`couponAmount` | 试算不改变券状态，券无效/不适用时 `couponAmount=0` |
| `POST /orders/from-listing/preview`（§8.0.0） | 同上 | 同上 |
| `POST /orders/{id}/pay`（§8.2） | 请求 `userCouponId`；响应 `couponAmount` | 支付时锁定券；即时支付成功后核销，微信支付在回调确认后核销 |
| `GET /orders/my`、`GET /orders/{id}` | 响应新增 `couponId`、`couponAmount` | 订单详情展示优惠明细 |

**典型下单流程**：

1. 进入下单页 → `GET /coupons/available?merchantId=xxx&productAmount=100` 获取可用券及抵扣额
2. 用户选券 → `POST /orders/preview` 带 `userCouponId` 确认 `cashAmount`
3. 支付 → `POST /orders/{id}/pay` 带 `userCouponId`（后端再次校验并锁定）
4. 支付成功 → 券置 `used`；若订单取消/超时未支付 → 券自动释放回 `unused`

---

### 98.8 错误码（优惠券）

| 错误码 | HTTP | 说明 |
| --- | --- | --- |
| `60040` | 404 | 优惠券模板不存在 |
| `60041` | 404 | 优惠券不存在 |
| `60042` | 400 | 优惠券不可用（已使用/已锁定/已过期） |
| `60043` | 400 | 优惠券已过期 |
| `60044` | 400 | 优惠券已使用 |
| `60045` | 400 | 订单金额未达到优惠券使用门槛 |
| `60046` | 400 | 该优惠券不适用于当前商家 |
| `60047` | 400 | 优惠券已被领完 |
| `60048` | 400 | 已达到每人限领上限 |
| `60049` | 400 | 该优惠券不可领取 |
| `60050` | 400 | 优惠券模板已发放，不可修改关键字段 |

---

### 98.9 存量数据处理

商品 `prd_357991e8e01000`「优惠券10元」（`category=团购`，商家 `mch_demo002`）已被订单/询价引用，**不物理删除**，由 `V17__coupon.sql` 将其 `status` 置为 `inactive`（下架）。同表兜底：`category='团购'` 且名称包含「优惠券」的未删除商品一并下架。

---

<a id="99-单元楼长与业主卖货钱包"></a>

# 99. 单元楼长（building_leader）与业主卖货钱包

> 数据库变更见 `backend/src/main/resources/db/migration/V18__building_leader_and_seller_wallet.sql`、`V21__building_leader_unit_binding.sql`（生产禁用 Flyway，需**手动执行**，脚本幂等可重复执行）。

## 99.0 业务口径

### 99.0.1 单元楼长

| 维度 | 说明 |
| --- | --- |
| 身份标识 | 楼长为**「住户 + 楼长」复合身份**：`residents.role` **始终保持 `resident`**，楼长身份由 `building_leaders` 表存在 `status='active'` 记录判定（`GET /auth/profile` 返回 `isBuildingLeader`/`buildingLeaderId`）。**不替换住户角色**，因此社区论坛等住户功能不受影响 |
| 与活动组关系 | **无关**，楼长是与「小区 + 单元」绑定的独立推广角色 |
| 绑定粒度 | 小区 `communityId` + 单元 `unit`；同一小区同一单元**同一时刻仅一名 active 楼长**（历史记录保留，撤销后可重指派）。**与楼栋无关**：`1号楼2单元` 与 `2号楼2单元` 视为同一岗位 |
| 分成比例 | 任命时**按人单独设置** `commissionRate` ∈ (0,1]，默认 `0.1000`；表示占「物业公司分成」的比例 |
| 分成来源 | 从**物业公司分成 `property_share` 中定向扣除**；`property_share` 相应减少，**总额守恒**，不影响统筹/板块/个体链路 |
| 绑定时机 | **订单创建那一刻**快照 `orders.building_leader_id` / `orders.building_leader_rate`。分成定向从订单产生那一刻绑定，后续修改/撤销楼长**不会**把分成改判给新楼长 |
| 结算口径 | 结算严格以**订单快照**为准；无快照或快照比例不合法（≤0 或 >1）则不产生楼长分成 |
| 身份授予/回收 | 任命**不修改** `residents.role`；撤销仅置 `building_leaders.status='inactive'`，住户角色始终不变。`building_leader` 作为历史遗留 role 由 `RoleGuard` 归一化为 `resident` 兜底（存量令牌仍可用） |
| 管理权限 | 物业公司可**选定 / 添加 / 删除**该角色；权限码 `buildingLeader.read` / `buildingLeader.manage`（未入库时回退管理员校验：`platform_admin` 全通、`property_admin` 限本物业） |

**分成公式**（沿用现有 `buildShareBreakdown` 口径，仅新增楼长一段）：

```
building_leader_share = property_share × building_leader_rate
property_share        = property_share − building_leader_share
```

- 有统筹：`property_share = 可分配金额 × propertyRate(默认 0.30)`，再扣楼长分成。
- 无统筹：`property_share = 可分配金额`（全额），再扣楼长分成。

### 99.0.2 业主卖货钱包

| 维度 | 说明 |
| --- | --- |
| 用途 | 业主商城（业主商户上架商品）成交差价收益的**独立钱包**，**仅可提现、不可消费** |
| 是否进入物业币 | **否**。业主卖货收益**不进个人物业币**，只进该独立钱包，只能提现（对应前端反馈问题） |
| 入账时机 | 订单完成（确认收货）→ 生成 `resident_merchant_settlements`（`status='settled'`），其 `reseller_profit` 计入钱包 |
| 可提现余额 | `累计 reseller_profit − 已完成提现 − 在途提现(pending+approved)` |
| 提现类型 | `withdrawal_records.withdrawal_type='resident_seller'`，复用管理端多角色提现审核（§70） |
| 实现方式 | 实时聚合（不新增余额冗余列），口径与提现校验一致 |

### 99.0.3 数据表变更（V18）

| 表 | 变更 |
| --- | --- |
| `building_leaders` | **新建表（V18）**：`id`(`bl_`)/`resident_id`/`property_company_id`/`community_id`/`commission_rate`/`description`/`status`/`created_at`/`updated_at`；**V21 调整**：新增 `unit`（与 `residents.unit` 对齐，绑定粒度），删除 `building`，索引 `idx_bl_building` → `idx_bl_unit(community_id, unit, status)`；保留 `idx_bl_resident`、`idx_bl_property` |
| `orders` | 新增列 `building_leader_id`（`VARCHAR(32)`）、`building_leader_rate`（`DECIMAL(5,4)`），下单时写入的快照 |
| `distribution_records` | 新增列 `building_leader_share`（`DECIMAL(10,2) NOT NULL DEFAULT 0`）、`building_leader_id`、`building_leader_rate`；索引 `idx_dr_building_leader(building_leader_id)` |
| `withdrawal_records` | `withdrawal_type` 扩展支持 `building_leader` / `resident_seller`（列宽不变，仅刷新注释） |
| `residents` | **数据修正**：`UPDATE residents SET role='resident' WHERE role='building_leader'`（历史误改写回滚；楼长身份由 `building_leaders` 表判定） |

### 99.0.4 本次变更（v8.10）：楼长绑定由「楼栋」改为「单元」

| 项 | 变更前 | 变更后 |
| --- | --- | --- |
| 绑定键 | `communityId + building` | `communityId + unit`（与楼栋无关） |
| 管理端指定请求字段 | `building` | `unit` |
| 列表筛选参数 | `building` | `unit` |
| 列表 / 详情 / 工作台响应字段 | `building` | `unit` |
| 订单绑定匹配 | 住户 `communityId + building` | 住户 `communityId + unit` |
| 分成预览匹配 | 住户 `communityId + building` | 住户 `communityId + unit` |
| 数据库 | `building_leaders.building` | `building_leaders.unit`（V21：新增 `unit` 并删除 `building`） |

> **前端注意**：`POST /admin/building-leaders`（指定楼长）请求体字段由 `building` 改为 `unit`；楼长相关列表/详情/工作台响应中的 `building` 字段更名为 `unit`；`GET /admin/building-leaders` 的筛选参数由 `building` 改为 `unit`。存量数据已按住户 `residents.unit` 自动回填。

---

## 99.1 管理端 — 单元楼长管理

**Roles**：`property_admin`、`platform_admin`（`property_admin` 仅限本物业，跨物业隔离）

### 99.1.1 GET `/admin/building-leaders` — 单元楼长列表

**Query 参数**：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `page` | `Integer` | ❌ | 默认 1 |
| `pageSize` | `Integer` | ❌ | 默认 20，最大 100 |
| `keyword` | `String` | ❌ | 住户姓名/手机号模糊搜索 |
| `status` | `String` | ❌ | `active`/`inactive` |
| `communityId` | `String` | ❌ | 小区过滤 |
| `unit` | `String` | ❌ | 单元过滤 |
| `propertyCompanyId` | `String` | ❌ | 仅 `platform_admin` 可用，`property_admin` 强制本物业 |

**响应**：`PageResponse<BuildingLeaderListItemResponse>`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `String` | 楼长 ID（`bl_` 前缀） |
| `residentId` | `String` | 住户 ID（`res_`） |
| `residentName` / `residentPhone` | `String` | 住户姓名 / 手机号 |
| `phone` | `String` | 手机号（旧字段，值同 `residentPhone`） |
| `propertyCompanyId` / `propertyCompanyName` | `String` | 物业公司 |
| `communityId` / `communityName` | `String` | 小区 |
| `unit` | `String` | 单元 |
| `commissionRate` | `BigDecimal` | 楼长分成比例（占物业分成，0~1） |
| `description` | `String` | 备注 |
| `totalEarning` | `BigDecimal` | 累计楼长分成 |
| `status` | `String` | `active`/`inactive` |
| `createdAt` / `appointedAt` | `String` | 任命时间（ISO8601 UTC） |

### 99.1.2 POST `/admin/building-leaders` — 指定单元楼长

**请求体**（`AppointBuildingLeaderRequest`）：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `residentId` | `String` | ✅ | 住户 ID（`res_` 前缀） |
| `communityId` | `String` | ❌ | 不传取住户所属小区；住户未关联小区时必填 |
| `unit` | `String` | ❌ | 不传取住户所属单元；住户未关联单元时必填（`residents.unit`） |
| `commissionRate` | `BigDecimal` | ❌ | 占物业分成的比例，(0,1]，默认 `0.1000` |
| `description` | `String` | ❌ | 备注 |
| `status` | `String` | ❌ | `active`（默认）/`inactive` |

**校验**：住户存在且属本物业；该小区+单元**已存在 active 楼长** → 报错；该住户**已是 active 楼长** → 报错。

**副作用**：新增一条 `building_leaders`（`status='active'`）记录；**不修改** `residents.role`（住户身份保留，楼长身份叠加）。

**响应**：`BuildingLeaderDetailResponse`（结构同 §99.1.1 列表项）

### 99.1.3 GET `/admin/building-leaders/{id}` — 单元楼长详情

**Path**：`id` 楼长 ID（`bl_` 前缀）

**响应**：`BuildingLeaderDetailResponse`

### 99.1.4 PUT `/admin/building-leaders/{id}` — 更新单元楼长

**请求体**（`UpdateBuildingLeaderRequest`，字段全可选）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `commissionRate` | `BigDecimal` | 楼长分成比例，(0,1] |
| `description` | `String` | 备注 |
| `status` | `String` | `active`/`inactive` |

> 更新仅影响**后续订单**；已产生订单按快照结算，不受影响。

**响应**：`BuildingLeaderDetailResponse`

### 99.1.5 DELETE `/admin/building-leaders/{id}` — 撤销单元楼长

**Path**：`id` 楼长 ID（`bl_` 前缀）
**Query**：`reason`（可选，撤销原因，落库到 `description`）

**行为**：置 `status='inactive'`；**不修改** `residents.role`（住户身份自始至终保持 `resident`）。

**响应**：`BuildingLeaderDetailResponse`

---

## 99.2 楼长工作台

**Roles**：`resident`（登录用户须为该单元**启用中的楼长**，否则返回 `91004`）

> 说明：楼长登录后 `role=resident`，前端通过 `GET /auth/profile`.`isBuildingLeader` 判断是否展示楼长工作台入口；工作台接口内部按 `building_leaders` 表校验楼长身份。

### 99.2.1 GET `/building-leaders/my` — 当前楼长信息

**响应**：`BuildingLeaderMyResponse`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `buildingLeaderId` | `String` | 楼长 ID |
| `residentId` / `residentName` | `String` | 住户信息 |
| `propertyCompanyId` | `String` | 物业公司 |
| `communityId` / `communityName` | `String` | 小区 |
| `unit` | `String` | 单元 |
| `commissionRate` | `BigDecimal` | 分成比例 |
| `status` | `String` | `active`/`inactive` |
| `totalEarning` | `BigDecimal` | 累计分成 |
| `withdrawnAmount` | `BigDecimal` | 已提现（completed） |
| `pendingAmount` | `BigDecimal` | 在途提现（pending+approved） |
| `withdrawableAmount` | `BigDecimal` | 可提现余额 = `totalEarning − withdrawnAmount − pendingAmount` |

### 99.2.2 GET `/building-leaders/my/distribution-records` — 我的分成明细

**Query**：`page`（默认 1）、`pageSize`（默认 20，最大 100）

**响应**：`PageResponse<DistributionRecordItemResponse>`（见 §99.4，含 `buildingLeaderShare` 等字段）

### 99.2.3 POST `/building-leaders/{id}/withdrawals` — 楼长申请提现

**Path**：`id` 楼长 ID（`bl_`，需与当前登录楼长一致）
**请求体**：`{ "amount": 100.00 }`（>0）

**校验**：`amount ≤ 可提现余额`；住户未被阻止提现。
**响应**：`RoleWithdrawalItemResponse`（见 §70）

### 99.2.4 GET `/building-leaders/{id}/withdrawals` — 楼长提现记录

**Query**：`page`/`pageSize`/`auditStatus`(pending/approved/rejected/completed)/`startDate`/`endDate`

**响应**：`PageResponse<RoleWithdrawalItemResponse>`

### 99.2.5 楼长「本单元运营」（🆕 v8.36）

> 两条都是**复用既有表**，不新建业务表：单元通知 = `announcements` 行 + 新列 `target_units`；
> 分享商品 = 一条 `content_type='product'` 的产品推荐公告 + 按 `messages` 逐户落地的商品卡片消息。
> **定向范围（小区 + 单元）一律取自本人 active 楼长记录，请求体里没有 `communityId`/`unit` 字段，前端传不了、也不用传。**
> 发布即生效（`status='published'`），与活动公告同口径，**不走物业审核**。
> 身份不满足（非楼长 / 楼长记录已撤销）统一 `91004`（HTTP 404）。

#### 99.2.5.1 POST `/building-leaders/my/notices` — 发布单元通知

**Roles**：`resident`（须为启用中的单元楼长）

**请求体**：`PublishUnitNoticeRequest`

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `title` | `String` | ✅ | ≤200 字 |
| `content` | `String` | ✅ | 正文，不截断 |
| `coverUrls` | `String[]` | ❌ | 最多 9 张 |

**落库**（`announcements`）：`announcement_type='community'`、`content_type='notification'`、
`delivery_channel='announcement_board'`、`target_roles='["resident"]'`、`target_units='["<本单元>"]'`、
`publisher_role='building_leader'`、`status='published'`。

**限频**：楼长每人每日 **3 条**（与 `created_at` 同一时钟源的自然日）。超限 `91009`（HTTP 400）。

**响应**：`UnitNoticeItemResponse`（见 §99.2.5.3）

```json
{
  "code": 0,
  "data": {
    "id": "ann_386db3b9721000",
    "title": "周六楼道清洁",
    "content": "请把杂物收进屋内",
    "contentType": "notification",
    "targetUnits": ["5单元"],
    "deliveryChannel": "announcement_board",
    "productId": null,
    "status": "published",
    "publishedAt": "2026-11-03T12:55:31Z",
    "createdAt": "2026-11-03T12:55:31Z"
  }
}
```

#### 99.2.5.2 GET `/building-leaders/my/notices` — 我发布的单元通知

**Query**：`page`（默认 1）、`pageSize`（默认 20，最大 100）

**响应**：`PageResponse<UnitNoticeItemResponse>`，只含**本人**发布、`content_type='notification'` 的记录，
按 `createdAt` 倒序。其他单元住户在公告列表里看不到这些通知，但楼长自己在这里能看全。

#### 99.2.5.3 `UnitNoticeItemResponse` 字段

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `String` | 公告 ID（`ann_`） |
| `title` / `content` | `String` | 标题 / 全文 |
| `contentType` | `String` | `notification`=单元通知，`product`=商品分享 |
| `targetUnits` | `String[]` | 定向单元，楼长发布固定为 `[本单元]` |
| `deliveryChannel` | `String` | `announcement_board` / `chat` / `both` |
| `productId` | `String?` | 仅 `contentType=product` 时有值 |
| `status` | `String` | `published` |
| `publishedAt` / `createdAt` | `String` | UTC ISO8601 |

#### 99.2.5.4 GET `/building-leaders/my/shareable-products` — 本小区可分享商品

**Query**：`page`/`pageSize`/`keyword`（匹配商品名称）

**口径**：本物业下 + 商家 `audit_status='approved'` 且 `status='active'` + **服务本楼长所在小区** + 商品 `status='active'` 且未软删。
「服务本小区」两种登记任一命中即算：`merchants.serve_all_communities=1`（历史 NULL 按默认值 1 处理），
或 `merchant_service_communities` 里有该小区的绑定记录。

**响应**：`PageResponse<ShareableProductItemResponse>`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` / `name` / `coverUrl` / `description` | `String` | 商品信息 |
| `price` / `memberPrice` | `BigDecimal` | 现价 / 会员价（元） |
| `pointPrice` | `Integer` | 积分价 |
| `stock` | `Integer` | 库存 |
| `category` | `String` | 商品分类 |
| `merchantId` / `merchantName` | `String` | 所属商家 |

#### 99.2.5.5 POST `/building-leaders/my/product-shares` — 分享商品给本单元

**请求体**：`{ "productId": "prd_xxx", "remark": "师傅很靠谱" }`（`remark` 可选，≤200 字）

**行为**：
1. 校验商品在本小区范围内，否则 `60051`（HTTP 400）；商品不存在/已下架/已软删 → `60004`。
2. 生成一条 `content_type='product'`、`delivery_channel='chat'` 的单元定向公告（`title` 取商品名，`content` 为
   `【楼长推荐】<商品名> ￥<价格>[｜<remark>]`）。
3. 给**本单元其他 active 住户**各落一条 `messages`：`from_id`=楼长住户 ID、`to_id`=住户、
   `message_type='resident_message'`、`chat_type='private'`、`card_type='product'`、`card_id`=商品 ID、
   `related_task_id`=公告 ID。**剔除楼长本人与已屏蔽该楼长的住户**（`neighbor_blocks`）。
4. 剔除后收件人为 0 → `10002`（`PARAM_INVALID`，HTTP 400）「本单元暂无其他住户」，此时**不会**留下公告。

**限频**：楼长每人每日 **3 次**；超限 `91010`（HTTP 400）。

**响应**：`ShareUnitProductResponse`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `announcementId` | `String` | 本次生成的公告 ID |
| `productId` / `productName` / `coverUrl` / `price` | — | 商品快照（前端可直接渲染回执卡片） |
| `unit` | `String` | 送达单元 |
| `recipientCount` | `Integer` | 实际送达户数 |
| `sentAt` | `String` | UTC ISO8601 |

#### 99.2.5.6 GET `/building-leaders/my/product-shares` — 我的分享记录

**Query**：`page`/`pageSize`；**响应**：`PageResponse<UnitNoticeItemResponse>`（`contentType='product'`）

#### 99.2.5.7 住户侧可见性（🆕 影响既有接口，务必读）

- `GET /announcements`（§6）：住户视角新增一条过滤——`target_units` 为空视为不限单元（历史公告行为不变），
  非空时**按 JSON 数组元素整段匹配**当前住户的 `unit`。因此 `5单元` 不会命中 `15单元` 的住户。
- `GET /announcements/{id}`（§6）：同样补了 `target_units` 校验，跨单元按 ID 直取返回 `90001`（HTTP 404）。
- 列表项与详情新增字段：`contentType`、`productId`（列表+详情）、`targetUnits`、`deliveryChannel`（详情）。
  纯增量，旧前端忽略即可；渲染产品推荐卡片时用 `productId` 调 `GET /products/{id}`。
- `GET /messages`（§30）列表项与详情新增 `cardType`、`cardId`；`cardType='product'` 时用 `cardId` 渲染商品卡片。

> ⚠️ **`target_buildings` 的既有匹配仍是裸 `LIKE '%3栋%'`**，存在 `3栋` 命中 `13栋` 的同款越权。
> 本轮只把新加的 `target_units` 做成整段匹配，**没有动楼栋口径**（改了会让部分住户看不到现在能看到的公告）。
> 是否一并收紧请产品拍板。

---

## 99.3 业主卖货钱包

**Roles**：`resident`

### 99.3.1 GET `/resident-merchants/seller-wallet` — 钱包概览

**响应**：`SellerWalletResponse`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `residentId` | `String` | 住户 ID |
| `totalProfit` | `BigDecimal` | 累计卖货收益（已结算订单差额汇总，`reseller_profit`） |
| `withdrawnAmount` | `BigDecimal` | 已提现金额（completed） |
| `pendingAmount` | `BigDecimal` | 在途金额（pending+approved） |
| `withdrawableAmount` | `BigDecimal` | 可提现余额 = `totalProfit − withdrawnAmount − pendingAmount` |

> 钱包金额**仅可用于提现**，不可消费、不可转物业币。

### 99.3.2 POST `/resident-merchants/seller-wallet/withdrawals` — 钱包提现申请

**请求体**：`{ "amount": 100.00 }`（>0）

**校验**：`amount ≤ 可提现余额`（累计卖货收益 − 已提现 − 在途）；住户未被阻止提现。
**响应**：`RoleWithdrawalItemResponse`，进入管理端提现审核（`withdrawal_type=resident_seller`，见 §70.5/§70.7）

### 99.3.3 GET `/resident-merchants/seller-wallet/withdrawals` — 钱包提现记录

**Query**：`page`/`pageSize`/`auditStatus`/`startDate`/`endDate`

**响应**：`PageResponse<RoleWithdrawalItemResponse>`

---

## 99.4 分成记录字段变更

### 99.4.1 响应新增字段

| 接口 | 新增字段 | 说明 |
| --- | --- | --- |
| `GET /distribution/records`（§21.1）、`GET /admin/distribution-records`（§21.4） | `buildingLeaderShare`、`buildingLeaderId`、`buildingLeaderName` | 楼长分成金额 / 楼长 ID / 楼长姓名 |
| `GET /distribution/stats`（§21.2）、`GET /admin/distribution/statistics`（§21.5） | `summary.buildingLeaderAmount` | 楼长分成合计 |
| `GET /distribution/calculate`（§21.3） | `buildingLeaderShare`、`buildingLeaderId` | 试算时的楼长分成分项（按住户当前「小区 + 单元」实时解析） |
| `GET /deliveries/pending`（§9.1）、`/deliveries/my`（§9.3）、`POST /deliveries/{id}/grab`（§9.4）、`POST /deliveries/{id}/complete`（§9.5） | `fee`、`originalDeliveryFee`、`deliverySubsidySponsor`、`deliverySubsidyAmount`、`deliverySettlementBase`、`platformDeliveryShare`、`courierEarning`、`courierShare`、`settlementStatus`、`carrierType`、`merchantDeliveryFeeShare` | v8.9 配送侧结算链字段（A1） |

### 99.4.2 订单快照

`orders` 新增 `building_leader_id` / `building_leader_rate`，在下单（创建订单 / 业主上架订单）时写入，不对外暴露为响应字段。

`orders` 新增 `original_delivery_fee` / `delivery_subsidy_sponsor` / `delivery_subsidy_amount`（v8.9，满额配送费减免快照，结算时读取快照不重算）。

### 99.4.3 配送口径（v8.9，A1）

`courierEarning = deliverySettlementBase × (1 − platform_delivery_share_rate)`，`deliverySettlementBase = 实付配送费 + 配送补贴`；
`delivery_rules.courier_per_order` 等固定每单金额列保留但不再参与分账；`courierShare` 恒等于 `courierEarning`。

---

## 99.5 错误码

| 错误码 | HTTP | 说明 |
| --- | --- | --- |
| `91004` | 404 | 楼长不存在 / 当前用户不是楼长（`BUILDING_LEADER_NOT_FOUND`） |
| `10001` | 400 | 参数缺失（如住户未关联小区/单元且未指定 `communityId`/`unit`） |
| `10002` | 400 | 参数格式错误（如 `commissionRate` 不在 (0,1]、`status` 非法、该小区该单元已存在 active 楼长、该住户已是 active 楼长） |
| `30001` | 404 | 业主不存在（`RESIDENT_NOT_FOUND`） |
| `97001` | 404 | 物业公司不存在（`PROPERTY_COMPANY_NOT_FOUND`） |
| `94003` | 400 | 提现申请不允许操作（可提现余额不足，`MERCHANT_WITHDRAWAL_INVALID`） |
| `94005` | 400 | 提现已被冻结（`WITHDRAWAL_BLOCKED`） |
| `20004` | 403 | 权限不足（`PERMISSION_DENIED`，楼长操作他人 / 角色不符） |

---

## 99.6 认证响应新增字段（前端识别「住户 + 楼长」身份）

楼长登录后 `role` 仍为 `resident`，前端需通过下述标志识别楼长身份：

| 接口 | 新增字段 | 说明 |
| --- | --- | --- |
| `GET /auth/profile`（§3.x） | `isBuildingLeader` | 是否为**启用中**的单元楼长 |
| `GET /auth/profile`（§3.x） | `buildingLeaderId` | 楼长 ID（`bl_`）；非楼长为 `null` |
| 登录响应 `resident`（`/auth/wechat-login`、`/auth/admin-login`） | `isBuildingLeader` | 同上，供登录后立即渲染入口 |

> **存量数据迁移提示**：历史曾把楼长 `residents.role` 误写为 `building_leader`。执行 V18 脚本中的 `UPDATE residents SET role='resident' WHERE role='building_leader'` 后即为 `resident`。此前签发的旧令牌由 `RoleGuard` 将 `building_leader` 归一化为 `resident` 兜底，住户功能可正常使用；**建议楼长用户重新登录**以获取带 `isBuildingLeader` 的最新 profile。

---

<a id="100-物业公司经理property-managers"></a>

# 100. 物业公司经理（property-managers）

> 🆕 **v8.11 新增**。物业公司经理是**「住户 + 经理」复合身份**（附录于 §100.0 角色说明）。由 `property_admin` / `platform_admin` 指派，登录角色仍为 `resident`（**不占用 `residents.role`，也不写入座位字段**）。

## 100.0 角色与业务口径

| 项 | 说明 |
| --- | --- |
| 身份属性 | **业主属性**：经理自身的登录角色为 `resident`，经理身份由 `property_managers` 表的 `active` 记录判定（`resident_id` + `property_company_id`） |
| 绑定关系 | 绑定一户 `resident`（`r_`/`res_` 前缀）+ 一个 `property_company`（`pc_`）；由管理端指派 |
| 唯一性 | **同一物业公司同一时刻仅允许一名 `active` 经理**（应用层校验，参照楼长模式） |
| 可做的两件事 | ① 添加**本物业公司范围内**的单元楼长（限定 scope）；② 从**楼长分成中按比例抽成**。**经理本人不能改自己的抽成比例**（防自肥） |
| 抽成比例设置方 | 平台/物业（管理端 `commissionRate`，取值 `[0,1)`，默认 `0`） |

**抽成口径（订单创建时快照，后续修改不影响已产生订单）**：

```
设：楼长分成 L，经理抽成比例 M（commission_rate，0≤M<1）

经理收益   = L × M          → 经理独立钱包（wallet_flows，ownerType=property_manager，entryType=property_manager_share）
楼长实得   = L × (1 − M)    → 楼长分成净额
```

- 快照落点：`orders.manager_id` / `orders.manager_rate`、`distribution_records.manager_id` / `distribution_records.manager_rate`（与楼长快照 `building_leader_id` / `building_leader_rate` 同表同模式，V22 脚本新增列）；
- 无 `active` 经理时，楼长分成不抽成，楼长实得=`L`；
- 经理收益写入 `wallet_flows`（`owner_type=property_manager`、`owner_id=经理 residentId`），`balanceType=withdrawable`，退款时写负向冲正流水（`refRecordId` 指向原流水），可提现；
- **业主小店（listing）订单不参与**楼长分成/物业四级分成，故也**不产生经理抽成**（见 §60.15）。

**数据表**（V22 脚本）：

| 表 | 关键列 |
| --- | --- |
| `property_managers` | `id`（`pm_`）、`resident_id`（`res_`）、`property_company_id`（`pc_`）、`commission_rate`（`DECIMAL(5,4)`，默认 `0.0000`）、`description`、`status`（`active`/`inactive`） |
| `orders` | 新增 `manager_id`、`manager_rate`（快照） |
| `distribution_records` | 新增 `manager_id`、`manager_rate`（快照） |
| `withdrawal_records` | `withdrawal_type` 新增枚举值 `property_manager` |

## 100.1 管理端 — 物业经理管理

**Auth**: Required　**Roles**: `property_admin`, `platform_admin`（物业隔离：`property_admin` 仅限本物业公司，跨物业操作返回 `20004`）

| 方法 | 路径 | 说明 | 状态 |
| --- | --- | --- | --- |
| GET | `/admin/property-managers` | 经理列表 | ✅ |
| POST | `/admin/property-managers` | 指派物业经理 | ✅ |
| GET | `/admin/property-managers/{id}` | 经理详情 | ✅ |
| PUT | `/admin/property-managers/{id}` | 更新（抽成比例/状态） | ✅ |
| DELETE | `/admin/property-managers/{id}` | 停用（置 `inactive`） | ✅ |

### GET `/admin/property-managers` — 经理列表

**Query**：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `page` | Integer | ❌ | 页码，默认 1 |
| `pageSize` | Integer | ❌ | 每页条数，默认 20 |
| `keyword` | String | ❌ | 搜索关键词（住户姓名/手机号） |
| `status` | String | ❌ | 状态 `active`/`inactive` |
| `propertyCompanyId` | String | ❌ | 物业公司 ID（`platform_admin` 可跨物业筛选；`property_admin` 自动限本物业） |

**响应（`PageResponse<PropertyManagerListItemResponse>`，`data.list[]`）**：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | String | 经理 ID（`pm_`） |
| `residentId` | String | 住户 ID |
| `residentName` | String | 住户姓名 |
| `residentPhone` | String | 住户手机号 |
| `propertyCompanyId` | String | 物业公司 ID |
| `propertyCompanyName` | String | 物业公司名称 |
| `commissionRate` | BigDecimal | 经理抽成比例（占单元楼长分成） |
| `status` | String | `active`/`inactive` |
| `createdAt` | String | 创建时间 |

### POST `/admin/property-managers` — 指派物业经理

**请求体**（`CreatePropertyManagerRequest`）：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `residentId` | String | ✅ | 经理对应住户 ID（`res_` 前缀，`@NotBlank`） |
| `commissionRate` | BigDecimal | ❌ | 抽成比例（占楼长分成），`[0,1)`，不传默认 `0` |
| `status` | String | ❌ | `active`/`inactive`，不传默认 `active` |

**行为**：校验证件前缀、住户存在、**同物业（`assertSameProperty`）**；**同一住户已有经理记录（含停用）时禁止重复指派**（返回 `91006`，提示直接更新原记录）；指派 `active` 时校验该物业公司唯一在职经理（重复返回 `91006`）。

**错误响应**：`91006`（该物业公司已存在在职物业经理 / 该住户已有经理记录）、`10003`（参数值超出范围）、`30001`（业主不存在）、`20004`（跨物业/权限不足）。

**curl 示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/admin/property-managers \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <property_admin_token>" \
  -d '{"residentId":"res_1001","commissionRate":0.20,"status":"active"}'
```

### GET `/admin/property-managers/{id}` — 经理详情

**Path**：`id` 经理 ID（`pm_` 前缀，非法前缀返回 `10002`）
**响应（`PropertyManagerDetailResponse`）**：字段同列表项（`id`/`residentId`/`residentName`/`residentPhone`/`propertyCompanyId`/`propertyCompanyName`/`commissionRate`/`status`/`createdAt`）
**错误响应**：`91005`（物业经理不存在）、`20004`（跨物业/权限不足）

### PUT `/admin/property-managers/{id}` — 更新（抽成比例/状态）

**请求体**（`UpdatePropertyManagerRequest`）：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `commissionRate` | BigDecimal | ❌ | 新抽成比例，`[0,1)`（传 null 表示不改） |
| `status` | String | ❌ | `active`/`inactive`（传 null 表示不改） |

**行为与规则**：

- **经理本人调用改自己的 `commissionRate` 时禁止**，返回 `91008`（`manager.residentId == 当前登录人`）；
- 将状态由 `inactive` 改为 `active` 时校验同物业唯一在职经理（重复返回 `91006`）；
- 响应同详情 `PropertyManagerDetailResponse`。

**错误响应**：`91008`（经理不可改自己的分成比例）、`91006`、`91005`、`10003`（比例超出 0~1 范围）、`20004`。

### DELETE `/admin/property-managers/{id}` — 停用

**行为**：将经理 `status` 置为 `inactive`（楼长分成不再被抽成）。响应当前详情 `PropertyManagerDetailResponse`。历史已快照的订单不受影响。
**错误响应**：`91005`、`20004`。

## 100.2 经理工作台（业主端）

**Auth**: Required　**Roles**: `resident`（须为**在职**物业经理；即以 `resident` 登录且在本物业公司存在 `active` 经理记录）

> 身份解析：按 `当前 userId + 当前物业公司ID + status=active` 查找；有停用记录时返回 `91007`（身份已停用），无任何记录返回 `91005`（当前用户不是物业经理）。

| 方法 | 路径 | 说明 | 状态 |
| --- | --- | --- | --- |
| GET | `/property-managers/my` | 我的身份与抽成比例 | ✅ |
| GET | `/property-managers/my/building-leaders` | 名下楼长列表（本物业范围） | ✅ |
| POST | `/property-managers/my/building-leaders` | 指定单元楼长（限本物业） | ✅ |
| GET | `/property-managers/my/earnings` | 我的抽成收益流水 | ✅ |
| GET | `/property-managers/my/wallet` | 我的钱包概览 | ✅ |
| POST | `/property-managers/my/withdrawals` | 申请提现 | ✅ |
| GET | `/property-managers/my/withdrawals` | 提现记录 | ✅ |

### GET `/property-managers/my` — 我的身份与抽成比例

**响应（`PropertyManagerMyResponse`）**：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `propertyManagerId` | String | 经理 ID（`pm_`） |
| `residentId` | String | 住户 ID |
| `residentName` | String | 姓名 |
| `propertyCompanyId` | String | 物业公司 ID |
| `propertyCompanyName` | String | 物业公司名称 |
| `commissionRate` | BigDecimal | 经理抽成比例（占单元楼长分成） |
| `status` | String | `active`/`inactive` |

> 前端可用本接口判定「当前登录人是否为在职经理」以决定是否展示经理工作台入口；未开通则 `91005`。

### GET `/property-managers/my/building-leaders` — 名下楼长列表

**Query**：`page`（默认 1）、`pageSize`（默认 20）、`keyword`（住户姓名/手机号）、`status`（`active`/`inactive`）

**响应**：`PageResponse<BuildingLeaderListItemResponse>`（本物业公司范围，字段同管理端楼长列表 §99.1）

### POST `/property-managers/my/building-leaders` — 指定单元楼长

**请求体**（`AppointBuildingLeaderRequest`，复用管理端楼长指定 DTO）：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `residentId` | String | ✅ | 住户（楼长）ID（`res_` 前缀） |
| `communityId` | String | ❌ | 小区 ID；不传取住户所属小区 |
| `unit` | String | ❌ | 单元；不传取住户所属单元 |
| `commissionRate` | BigDecimal | ❌ | 楼长分成比例（占物业公司分成），不传默认 `0.1000` |
| `description` | String | ❌ | 备注/说明 |
| `status` | String | ❌ | `active`/`inactive`，不传默认 `active` |

**行为**：复用管理端楼长指定核心逻辑，**限定本物业公司**作用域。**响应**：`BuildingLeaderDetailResponse`。

**curl 示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/property-managers/my/building-leaders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <resident_token>" \
  -d '{"residentId":"res_2001","unit":"1单元","commissionRate":0.10,"description":"1单元楼长"}'
```

### GET `/property-managers/my/earnings` — 我的抽成收益流水

**Query**：`page`（默认 1）、`pageSize`（默认 20，**上限 100**，超过返回 `10003`）

**响应（`PageResponse<PropertyManagerEarningItemResponse>`，`data.list[]`）**：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | String | 流水 ID（`wf_`） |
| `orderId` | String | 订单 ID |
| `distributionRecordId` | String | 分账记录 ID（`dr_`） |
| `entryType` | String | 流水类型 `property_manager_share` |
| `amount` | BigDecimal | 金额（正数入账，**负数冲正**） |
| `balanceType` | String | 余额类型 `withdrawable` |
| `refRecordId` | String | 冲正关联的原流水 ID |
| `createdAt` | String | 创建时间 |

### GET `/property-managers/my/wallet` — 我的钱包概览

**响应（`PropertyManagerWalletResponse`）**：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `totalEarning` | BigDecimal | 累计抽成收益（含退款冲正负向） |
| `withdrawnAmount` | BigDecimal | 已提现金额（`completed`） |
| `pendingAmount` | BigDecimal | 在途提现金额（待审 `pending` + 已审待打款 `approved`） |
| `withdrawableAmount` | BigDecimal | 可提现余额 = `totalEarning − withdrawnAmount − pendingAmount`（负则置 0） |

### POST `/property-managers/my/withdrawals` — 申请提现

**请求体**（`ApplyRoleWithdrawalRequest`）：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `amount` | BigDecimal | ✅ | 提现金额，`≥0.01`；不得超过可提现余额 |

**行为**：落库 `withdrawal_records`（`withdrawal_type=property_manager`、`applicant_id=经理 pm_ id`、`resident_id`、`property_company_id`）。

**响应（`RoleWithdrawalItemResponse`）**：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | String | 提现记录 ID |
| `withdrawalType` | String | 提现类型 `property_manager` |
| `applicantId` | String | 申请主体ID（经理 ID `pm_`） |
| `residentId` | String | 关联住户 ID |
| `propertyCompanyId` | String | 物业公司 ID |
| `amount` | BigDecimal | 提现金额 |
| `feeRate` | BigDecimal | 手续费比例 |
| `feeAmount` | BigDecimal | 手续费金额 |
| `actualAmount` | BigDecimal | 实际到账金额 |
| `status` | String | `pending`/`approved`/`rejected`/`completed` |
| `auditedAt` / `completedAt` / `createdAt` | String | 审核/完成/创建时间 |

### GET `/property-managers/my/withdrawals` — 提现记录

**Query**：`page`（默认 1）、`pageSize`（默认 20）、`auditStatus`（审核状态）、`startDate`、`endDate`

**响应**：`PageResponse<RoleWithdrawalItemResponse>`（同上字段）

## 100.3 提现审核（管理端）

经理提现与其他多角色提现统一落 `withdrawal_records`（`withdrawal_type=property_manager`），**管理端提现审核列表**（§70.5 `GET /admin/role-withdrawals`）新增该类型筛选值。审核流程（`pending → approved → completed` / `rejected`）与其余角色一致。

## 100.4 错误码

| 错误码 | HTTP | 含义 | 触发场景 |
| --- | --- | --- | --- |
| `91005` | 404 | 物业经理不存在 | 详情/更新/停用 ID 无效；或当前住户无在职经理记录（工作台） |
| `91006` | 400 | 该物业公司已存在在职物业经理 | 指派/激活时同物业已有 active 经理；或该住户已有经理记录重复指派 |
| `91007` | 403 | 物业经理身份已停用 | 停用经理访问经理工作台 |
| `91008` | 403 | 物业经理不可修改自己的分成比例 | 经理本人调用更新接口调整自身 `commissionRate` |
| `10003` | 400 | 参数值超出范围 | `commissionRate` 不在 `[0,1)`；工作台收益 `pageSize>100` |
| `30001` | 404 | 业主不存在 | 指派时 `residentId` 无效 |
| `20004` | 403 | 权限不足 | 非 `property_admin`/`platform_admin`；或跨物业公司操作 |

> 件数制相关码段（`60030`–`60036`）见 §60.5/§60.6/§60.6.1/§60.10.1/§60.12 与「错误码速查表」。

---

# 102. 上线修复第一批（v8.16）

> 本章为本批**唯一需要前端改动的契约变更**。未在本章列出的接口保持原样。
> 数据库脚本：`sql/上线修复第一批_数据库变更_20261024.sql`（对应 Flyway `V26__batch1_refund_family_snapshot.sql`）。

## 102.0 数据库变更（必须先执行）

脚本幂等，可重复执行。新增列：

| 表 | 新增列 | 用途 |
| --- | --- | --- |
| `orders` | `cancelled_by` | 取消来源：`user` / `system` / `merchant` |
| `orders` | `refund_status` | `null` / `refunding` / `refunded` / `failed` |
| `orders` | `refund_fail_reason` | 退款失败原因 |
| `point_records` | `family_amount` | 本次变动中家庭池部分（正数进入、负数扣除、0 仅个人） |
| `point_records` | `family_balance` | 变动后家庭积分池余额快照 |
| `property_coin_records` | `family_amount` | 本次变动中家庭池部分 |
| `property_coin_records` | `family_balance` | 变动后家庭物业币池余额快照 |

执行顺序：先导入/确认基线库 → 执行本脚本 → 再启动新版本应用。脚本末尾附有**历史双记余额只读核对 SQL**（默认注释，不自动改数），生产如有历史数据请先核对再决定是否冲正。

## 102.1 物业币充值（变更）

### POST `/property-coins/recharge` — 充值预下单

**Auth**：Required，Roles：`resident`

**请求体**：

```json
{ "amount": 100.00 }
```

**响应**（`data`）：

```json
{
  "paymentRecordId": "pr_abc123",
  "outTradeNo": "wxo_abc123",
  "status": "pending",
  "amount": 100.00,
  "payParams": {
    "appId": "wxf7230afd38275659",
    "timeStamp": "1780000000",
    "nonceStr": "a1b2c3d4e5f6",
    "package": "prepay_id=wx1234567890",
    "signType": "RSA",
    "paySign": "xxxx",
    "outTradeNo": "wxo_abc123"
  }
}
```

**规则**：

- 后端不再直接加余额。预下单只创建 `payment_records` 并冻结/预占（金额、房号、积分/物业币拆分以服务端为准），到账以微信回调/查单为准。
- `paymentRecordId` 是内部支付流水 ID（`pr_` 前缀）；`outTradeNo` 是微信商户订单号（`wxo_` 前缀），两者**不是同一个值**。客服对账、微信侧查询使用 `outTradeNo`。
- `payParams` 必须原样传给 `wx.requestPayment`；`signType` 固定 `RSA`。
- 同一住户已有 `pending` 充值流水时返回 `70003 订单支付处理中，请勿重复提交`。
- 微信支付未开通（缺少商户参数/私钥）时返回 `10002`，不会回退为直接到账。
- 住户未绑定 `openid` 时返回参数缺失错误；前端需先完成微信登录/绑定。

### GET `/property-coins/recharge/{paymentRecordId}` — 查询充值支付状态（新增）

**Auth**：Required，Roles：`resident`；只能查自己的流水。

**响应**（`data`）：

```json
{
  "paymentRecordId": "pr_abc123",
  "outTradeNo": "wxo_abc123",
  "status": "paid",
  "amount": 100.00,
  "balanceAfter": 220.50,
  "familyBalanceAfter": 600.00,
  "rechargedAt": "2026-10-24T08:00:00.000Z",
  "failReason": null
}
```

字段说明：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `status` | String | `pending` / `paid` / `closed` / `amount_mismatch` / `refunded` / `failed` |
| `balanceAfter` | BigDecimal | 到账后个人物业币余额；未到账为 `null`。v8.21 起物业币充值统一入个人余额 |
| `familyBalanceAfter` | BigDecimal | 到账后家庭物业币余额快照（仅展示，充值不再入家庭池）；无家庭或未到账为 `null` |
| `rechargedAt` | String | 到账时间；未到账为 `null` |
| `failReason` | String | 关单/对账失败原因 |

**前端判定**：`status === "paid"` 且 `rechargedAt != null` 才算到账。`wx.requestPayment:success` 只代表微信收银台完成，不能直接当做到账。

## 102.2 微信支付/分账/转账回调（后端内部，前端无需调用）

以下路径已加入认证白名单，由微信服务器直接调用，**不带业务 Bearer**：

- `POST /api/v1/payment/notify/wechatpay`
- `POST /api/v1/payment/ecommerce-split/notify`
- `POST /api/v1/payment/ecommerce-transfer/notify`

后端在控制器内完成验签、解密、金额校验和幂等落账。部署时必须把 `.env` 中对应回调地址配置为公网 HTTPS：

```env
WECHAT_PAY_NOTIFY_URL=https://<正式域名>/api/v1/payment/notify/wechatpay
WECHAT_PAY_REFUND_NOTIFY_URL=https://<正式域名>/api/v1/payment/notify/wechatpay
WECHAT_ECOMMERCE_NOTIFY_URL=https://<正式域名>/api/v1/payment/ecommerce-split/notify
WECHAT_ECOMMERCE_TRANSFER_NOTIFY_URL=https://<正式域名>/api/v1/payment/ecommerce-transfer/notify
```

> 旧开发配置里的 `/api/v1/payment/wechat/notify` 是错误路径，生产不要使用。

## 102.3 家庭池记账口径（变更）

### 账户模型

- `residents.point_balance` / `residents.coin_balance`：个人余额。
- `families.point_balance` / `families.coin_balance`：家庭共享池。
- 两个账户**相互独立**。住户创建/加入家庭时，个人积分和物业币并入家庭池并将个人余额清零；离开家庭时按成员数拆分回个人余额。
- 有家庭时，新获得的**积分**只进入家庭池，不再同时加个人余额（修复原双记 bug）。
- **物业币发放口径（2026-10-25 调整）**：管理端发放、订单返币、微信充值等统一发放/到账入口一律加**个人余额** `residents.coin_balance`，不再进入家庭池；发放流水 `familyId=null`、`familyAmount=0`、`familyBalance=null`。家庭池只保留调整前的历史余额。**订单取消/退货退款例外**，仍按原支付流水的家庭/个人扣减比例原路退回。

### 消费与退款

- 订单/物业费/服务/活动等消费：`useFamilyPool=true` 时先扣家庭池，不足部分再扣个人余额；个人余额不足且家庭池不足时返回余额不足。
- 取消/拒单退款：按原支付流水 `family_amount` 记录的比例原路退回家庭池/个人余额；旧数据没有 `family_amount` 时，订单有 `familyId` 退回家庭池，否则退回个人余额。
- 返现/返积分冲正：按原入账流水的 `family_amount` 比例扣回，避免把家庭池的钱扣到个人余额。

### 流水新增字段

`GET /points/records`、`GET /property-coins/records` 返回项新增：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `familyAmount` | Integer / BigDecimal | 本次变动中家庭池部分：正数进入家庭池，负数从家庭池扣除，0 仅个人池 |
| `familyBalance` | Integer / BigDecimal | 变动后的家庭池余额快照；无家庭为 `null` |

**前端展示必须调整**：

- `pointBalance` / `coinBalance` 与 `familyPointBalance` / `familyCoinBalance` 继续分开展示，不能相加。
- **物业币发放到账后余额变化看 `coinBalance`**；`familyCoinBalance` 只反映家庭池历史余额。发放成功但个人余额不变，才说明异常。
- 家庭成员的“可用余额”应包含家庭池；收银台“全部”按钮的上限不能只用个人余额，应使用后端试算结果或 `个人 + 家庭` 的可用上限。
- 旧前端若仍按个人余额封顶，家庭用户会出现“有家庭余额但用不了”的体验问题。

## 102.4 角色修改权限（变更）

### PUT `/residents/{id}`

- `role` 字段只有 `platform_admin` 可以修改；`property_admin`、住户本人修改角色返回 `20004 / 403 仅平台管理员可修改角色`。
- 客户端原样回传当前角色不算修改，不会报错（兼容整对象提交的旧前端）。
- 高权限角色（`platform_admin` / `property_admin`）必须由平台管理员在管理端设置。

### PUT `/residents/me`

- 不接受 `role` 变更；提交与当前角色不同的 `role` 返回 `20004 / 403 我的资料不支持修改角色`。
- 前端“我的资料”payload 不应包含 `role`。

## 102.5 商家提现（变更）

### GET `/merchants/my` 新增字段

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `withdrawableAmount` | BigDecimal | 可提现余额（已扣除 pending/approved/completed 提现占用；被冻结时为 0） |
| `withdrawableBlocked` | Boolean | 提现是否被冻结 |

### POST `/merchant/withdrawals`

- 请求：`{ "amount": 100.00 }`
- 后端校验：`amount > 0`、`amount <= 999999.99`、`amount <= withdrawableAmount`、商户未被冻结。
- 错误码：
  - `94005`：提现已被冻结（HTTP 400）
  - `94006`：可提现余额不足（HTTP 400，**新增**；不再依赖 `94003` + 中文正则）
  - `10003`：金额超出范围（HTTP 400）
- 审核端 `POST /admin/merchant-withdrawals/{id}/audit` 在 `pending → approved` 时会再次校验可提现余额；并发审核使用行锁，避免重复打款。

**前端调整**：提现页使用 `withdrawableAmount` 展示上限；超限时直接禁用提交并提示“超出可提现余额（最多 ¥x）”；识别 `94006` 后刷新余额与列表。

## 102.6 商家拒单退款（变更）

`POST /merchants/my/orders/{orderId}/verify`，`approved=false`：

- 订单置为 `cancelled`，`cancelledBy=merchant`。
- 退回买家积分/物业币（按原家庭/个人比例）、现金原路退款、恢复库存、撤销分账/配送、释放优惠券。
- 纯积分/物业币退款即时完成，`refundStatus=refunded`；含现金时先 `refundStatus=refunding`，由退款轮询落 `refunded` 或 `failed`。
- 退款失败不会静默吞掉：`refundStatus=failed` + `refundFailReason`，需人工处理。

**前端调整**：拒单成功不能只按“已取消”展示；订单详情并列展示 `refundStatus`（退款中/已退款/退款失败），并在 `refunding` 时轮询订单详情。

## 102.7 取消待支付订单（变更）

`POST /orders/{id}/cancel`：

- `pending` 且只有 `pending` 微信流水：后端主动调用微信关单；未支付，无退款；`cancelledBy=user`，`refundStatus=null`。
- `pending` 但微信已支付（回调/查单先到、finalize 未到）：只退现金，`refundStatus=refunding`；积分/物业币尚未扣减，不重复退。
- 已支付/配送中/待核实/已核实：退积分/物业币、扣回返现、恢复库存、撤销分账/配送、释放优惠券；含现金时 `refundStatus=refunding`，纯积分/物业币时 `refunded`。
- 取消后支付成功：回调检测到订单已取消，自动原路退款，并回写 `refundStatus`。

**前端调整**：

- 取消成功后停止支付轮询，提示“订单已取消”。
- 取消确认弹窗增加“取消后若已完成支付，款项将原路退回”。
- 订单详情展示 `cancelledBy`（`user` 用户取消 / `system` 超时取消 / `merchant` 商家拒单）。

## 102.8 订单响应新增字段

`GET /orders/{id}`、`GET /orders/my`、商家订单列表等订单响应新增：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `refundStatus` | String | `null` / `refunding` / `refunded` / `failed` |
| `refundFailReason` | String | 退款失败原因；成功时为 `null` |
| `cancelledBy` | String | `user` / `system` / `merchant`；未取消为 `null` |
| `cancelReason` | String | 取消原因（详情接口） |

示例：

```json
{
  "orderStatus": "cancelled",
  "refundStatus": "refunding",
  "refundFailReason": null,
  "cancelledBy": "merchant",
  "cancelReason": "商品与描述不符",
  "cashAmount": 84.90
}
```

## 102.9 错误码（本批新增/变更）

| 错误码 | HTTP | 含义 | 场景 |
| --- | --- | --- | --- |
| `10002` | 400 | 参数值无效 | 微信支付未开通、金额/参数不合法 |
| `70002` | 409 | 订单状态不允许操作 | 已取消/已完成订单再次取消 |
| `70003` | 409 | 并发冲突/支付处理中 | 重复发起充值、重复支付 |
| `94005` | 400 | 提现已被冻结 | 商户 `withdrawalBlocked=true` |
| `94006` | 400 | 可提现余额不足 | **新增**；提现申请/审核超出可提现余额 |
| `20004` | 403 | 权限不足 | 非平台管理员修改角色、跨物业操作 |

> 前端不要再依赖 `94003` + 中文正则判断提现超额；统一识别 `94006`。

## 102.10 前端联调检查清单

- [ ] 充值页改为 `POST /property-coins/recharge` → `wx.requestPayment(payParams)` → 轮询 `GET /property-coins/recharge/{paymentRecordId}`；`status=paid && rechargedAt != null` 才提示到账。
- [ ] 充值页识别 `payParams.outTradeNo`，不要把 `paymentRecordId` 当作微信订单号。
- [ ] 收银台“全部”上限使用家庭池 + 个人余额（或后端试算结果），不能只用个人余额。
- [ ] 积分/物业币流水展示新增 `familyAmount` / `familyBalance`（如页面有流水详情）。
- [ ] 管理端隐藏非平台管理员的角色编辑入口；“我的资料”不再提交 `role`。
- [ ] 提现页展示 `withdrawableAmount`，超限禁用提交，识别 `94006` 后刷新。
- [ ] 商家拒单/用户取消后展示 `refundStatus` + `cancelledBy`，`refunding` 时轮询订单详情。
- [ ] 取消确认弹窗增加“取消后若已完成支付，款项将原路退回”。
- [ ] 所有支付结果页保持“支付中/对账中”兜底，不因 `wx.requestPayment:success` 直接跳成功。

## 102.11 兼容性与上线注意

1. **充值响应结构变化**：旧字段 `recordId` / `balanceAfter` / `familyBalanceAfter` / `rechargedAt` 保留但预下单时为 `null`，新前端只读 `paymentRecordId` / `outTradeNo` / `status` / `payParams`。
2. **家庭池历史数据**：本次修复阻止新的双记，但不会自动冲正历史重复余额。上线前请用 SQL 脚本末尾的只读核对 SQL 检查家庭成员个人余额与家庭池流水差异，确认后再人工处理。
3. **数据库必须执行 V26**：否则 `orders.refund_status`、`point_records.family_amount` 等列不存在，订单/流水查询会报 Unknown column。
4. **微信支付必须配置完整**：`WECHAT_PAY_MCH_ID`、`WECHAT_PAY_API_V3_KEY`、`WECHAT_PAY_PRIVATE_KEY_BASE64`、`WECHAT_PAY_MERCHANT_SERIAL_NUMBER`、`WECHAT_PAY_NOTIFY_URL` 缺一不可；否则充值/支付会 fail-closed 返回 `10002`。
5. **回调验签**：三个 notify 路径免 Bearer，但控制器内部验签/解密不可关闭；不要为了联调把它们加入其他白名单或跳过校验。

---

# 103. 上线修复第二批（v8.17）

## 103.0 数据库与上线脚本

| 脚本 | 用途 | 是否必须 |
| --- | --- | --- |
| `sql/上线修复第二批_数据库变更_20261024.sql`（Flyway V27） | `property_coin_records.expired_amount`、过期索引、`merchant_ads.status` 兜底、`wechat_split_record` 遗留 NOT NULL 列可空化 | 必须，在 V26 之后执行 |
| `sql/上线前结构核对_20261024.sql` | 只读核对 V26/V27 关键列/表是否齐全 | 上线前执行一次 |
| `sql/上线前账户安全清理_20261024.sql` | 重置/禁用基线种子账号；默认 `@confirm_cleanup=0`，确认后改 1 才执行 | 生产建议执行 |

执行顺序：**V26 → V27 → 结构核对（全部 present=1）→ 账户清理（确认后）→ 启动后端 → 前端联调。**

## 103.1 管理端资金配置权限（变更）

| 接口 | platform_admin | property_admin | 说明 |
| --- | --- | --- | --- |
| `GET /admin/settlement-config` | ✅ | ✅ 只读 | 物业管理员可查看当前配置 |
| `PUT /admin/settlement-config` | ✅ | ❌ 403 | 仅平台管理员可改分账/提现费率 |
| `GET /admin/order-config` | ✅ | ✅ 只读 | 物业管理员可查看 |
| `PUT /admin/order-config` | ✅ | ❌ 403 | 仅平台管理员可改核实/冻结/退款窗口 |

**前端调整（PC 管理端）**：非 platform_admin 将保存/编辑按钮置灰或隐藏，保留只读展示；403 时提示“仅平台管理员可修改”，不要静默失败。

## 103.2 钱包两张卡权限（变更）

`GET /wallet/cards?ownerType=&ownerId=` 现在仅允许 `platform_admin` 调用。

- 移动端全仓无调用，不需要适配。
- 如有其他端调用，必须改为平台管理员，或改为从 token 推导当前主体（后续版本再开放）。
- 住户/商家/骑手不会再拿到 403 以外的数据。

## 103.3 物业管理员初始密码（变更）

- 新建物业公司/新增物业管理员时，后端随机生成 **14 位**初始密码（含大小写字母与数字，去除易混淆字符）。
- 初始密码只在创建响应 `adminAccount.initialPassword` 中返回一次，后续详情不返回。
- 登录使用 `admin123456` 会直接返回 **403 “初始密码已禁用，请重置密码后再登录”**，即使数据库尚未清理。
- 存量 `res_admin01` 等种子账号请按 §103.0 的账户清理脚本重置密码或禁用。

**前端调整（PC 管理端）**：
- 创建成功弹窗必须完整展示 `initialPassword`，并提供复制；提示管理员首次登录后立即改密。
- 登录页对 403 的“初始密码已禁用”给出明确文案，不要显示“手机号或密码错误”。

## 103.4 微信 `dev_` 登录后门（后端）

- `dev_` / `dev_phone_` 伪造登录只在 `app.wechat.skip-api=true` 时生效。
- `application.yml` 默认改为 `false`；`application-prod.yml` 固定 `false`；仅 `application-dev.yml` 显式 `true`。
- `.env` 增加 `APP_WECHAT_SKIP_API=false`，生产禁止改为 true。
- 前端正式包不得传 `dev_` / `dev_phone_` 开头的 code；否则生产会走真实微信校验并失败。

## 103.5 物业币过期清零（后端，前端仅感知余额）

### 规则

- 每笔 `earn` / `recharge` 流水新增 `expired_amount`，记录该笔已过期金额；后续任务只扣“未过期部分”，**不会重复清零**。
- 个人池：只处理无家庭住户的个人余额与个人流水。
- 家庭池：处理家庭余额与家庭流水，并包含成员入家庭前的个人流水；过期金额从家庭池扣除。
- 过期金额真实计入 `point_pools.balance/total_in`，`CoinExpiryRecord.poolInAmount` 与实际入池一致。
- 自动任务加 ShedLock，多实例不会重复执行。
- 管理端手动执行 `POST /admin/coin-expiry` 复用同一逻辑；`dryRun=true` 时 `clearedToPool=0`（只试算不写库）。

### 前端/运营感知

- 不需要改接口。住户物业币余额会在过期任务执行后按规则减少；流水新增 `expire` 类型记录。
- 管理端“已过期金额/入池金额”报表本次开始与积分池真实余额一致。

## 103.6 错误码补充

| 错误码 | HTTP | 含义 | 场景 |
| --- | --- | --- | --- |
| `20004` | 403 | 权限不足 | 非平台管理员写资金配置、钱包卡越权、`admin123456` 登录 |
| `10002` | 400 | 参数值无效 | 微信支付未开通/参数错误（沿用） |

## 103.7 兼容性与上线注意

1. **V27 必须先执行**：否则 `property_coin_records.expired_amount` 不存在，物业币查询/过期任务会报 Unknown column。
2. **V27 同时兜底 V20 与分账表漂移**：`merchant_ads.status` 缺失、`wechat_split_record.receiver_account/amount` NOT NULL 都会在脚本中修复。
3. **种子账号**：代码已拒绝 `admin123456` 登录，但生产仍应执行账户清理脚本，避免其他弱口令/演示账号残留。
4. **PC 管理端**：需要配合 §103.1 的按钮权限与 §103.3 的初始密码展示。
5. **移动端**：本批无接口字段变更；仅需确认正式包不依赖 `dev_` 登录，并知晓家庭/个人物业币过期后的余额变化。

---

# 104. 上线修复第三批（v8.18）

## 104.0 数据库变更

| 脚本 | 内容 | 是否必须 |
| --- | --- | --- |
| `sql/上线修复第三批_数据库变更_20261024.sql`（Flyway V28） | `residents.token_version`（令牌版本号，默认 0） | 必须，在 V27 之后执行 |
| `sql/上线前结构核对_20261024.sql` | 已追加 `residents.token_version` 核对项 | 上线前执行 |

## 104.1 A8 服务端支付规则校验（变更）

`POST /orders/{id}/pay`（含业主小店订单）现在在服务端重新校验金额，不再信任前端传入的 `pointUsed/coinUsed`：

| 支付方式 | 服务端校验 |
| --- | --- |
| `property_coin` | 物业币商城开关、最低使用金额、最大抵扣比例、家庭+个人真实余额 |
| `point` | 按商家积分汇率计算所需积分，校验家庭+个人真实积分余额 |
| `mixed` | 积分不能为负且不能超过可用积分；物业币走商城规则；积分+物业币不能超过应付；现金=应付-抵扣 |
| `wechat` | 不涉及积分/物业币 |

常见错误码：

| 错误码 | 含义 | 前端处理 |
| --- | --- | --- |
| `40001` | 积分余额不足 | 提示积分不足，刷新余额 |
| `50001` | 物业币余额不足 | 提示物业币不足，刷新余额 |
| `50002` | 积分与物业币不可互兑 | 提示不可混用规则 |
| `10002` | 参数不合法/物业币规则不满足 | 按后端 message 提示 |
| `96030` / `96031` / `96032` | 物业币商城未开启 / 低于起购金额 / 超过最大抵扣占比 | 使用错误码表映射文案 |

**前端要求**：
- 混合支付金额以 `POST /orders/preview` 返回为准；提交被服务端拒绝时**阻断提交并提示**，不要回退本地金额继续支付。
- 若试算与提交结果不一致，以提交接口的最终校验结果为准，提示用户刷新后重试。

## 104.2 B3 登出与令牌版本（变更）

### POST `/auth/logout`（新增）

- Auth：Required。
- 行为：当前账号 `residents.token_version + 1`，该账号所有旧 access/refresh token 立即失效。
- 响应：`{ "code": 0, "message": "success", "data": null }`。

### 令牌行为

- 新登录/刷新签发的 access/refresh token 带 `tv`（token version）声明。
- 每次带 `tv` 的请求，后端会校验：账号存在、未删除/禁用/冻结、`tv` 与库内 `token_version` 一致。
- 登出、后台禁用/删除账号后，旧 token 下一次请求即 401/403。
- **上线兼容**：上线前签发的旧 refresh token 不带 `tv`，本版本起刷新会返回 401，用户需重新登录一次；旧 access token 最长 1 小时内自然过期。

**前端要求**：
- 退出登录调用 `POST /auth/logout`，无论接口成功失败都要清本地 token。
- 401 后清理本地登录态并回登录页；不要无限重试 refresh。
- 上线当天提示用户可能需要重新登录一次。

## 104.3 B6 订单支付状态（变更）

`GET /orders/{id}` 新增：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `paymentStatus` | String | `pending` / `paid` / `closed` / `refunding` / `refunded` / `refund_failed` / `amount_mismatch`；无流水为 `null` |

**`amount_mismatch` 口径**：
- 微信实收金额与本地预下单金额不一致时，支付流水置 `amount_mismatch`，**订单保持 `pending`，不自动履约、不自动退款**。
- 前端展示“支付待对账”，提示“款项已收到，系统正在核对”，不要显示支付成功。
- 运营需人工对账：确认后由管理端走退款或补单流程；当前版本不提供自动冲正。

## 104.4 B5 广告微信支付开关（变更）

- 配置 `MERCHANT_AD_WECHAT_PAYMENT_ENABLED`，**默认 `true`（v8.26 起开放）**；如需临时关闭，显式置 `false`。
- 关闭时，广告创建传 `paymentMethod=wechat` 返回 `10002`：“广告微信支付暂未开放，当前仅可使用免费额度”。
- 开放后，`paymentMethod=wechat` 且金额 > 0 时：广告先落 `pending_payment`，响应返回 `paymentRecordId` 与 `payParams`，由前端调 `wx.requestPayment`；微信回调确认后才激活投放。未收款绝不投放。
- 支付方式仅保留 `free`（免费额度）与 `wechat`（微信现金）；`coin/point/mock` 已下线，继续传会返回 `10002`。
- 前端必须支持：`pending_payment / active / cancelled` 状态、`payParams` 调起支付、`POST /merchant/ads/{id}/repay` 继续支付、以及轮询/刷新确认最终状态。
- `GET /merchant/ads/quote` 与 `GET /merchant/ads/quota` 新增 `wechatPaymentEnabled` 字段，前端据此动态显示“微信支付”入口，不要写死提示。
- 微信支付通道未配置时，接口返回 `10002`：“在线支付未开通，请稍后再试或使用免费额度”；不会回退为直接投放。

## 104.5 B4 错误码表（新增交付）

- JSON：[`docs/错误码表_v8.18.json`](file:///D:/物业积分服务系统/docs/错误码表_v8.18.json)，共 184 个码，由 `ErrorCode.java` 自动生成。
- 字段：`code`、`name`、`httpStatus`、`message`、`scene`。
- 前端可按 `code` 映射提示；`scene` 当前为枚举名，便于日志/客服定位。
- 异常返回收紧：
  - 数据库唯一约束冲突统一 `409 / 70003 数据冲突，请刷新后重试`，不再回显表名/列名/重复值。
  - 微信支付异常只返回“支付服务暂时不可用，请稍后重试或联系客服”，微信原文只写服务端日志。
  - `IllegalArgumentException` 只返回“参数不合法”。

## 104.6 运维加固（部署）

- `application-prod.yml` 关闭 Swagger/OpenAPI：`springdoc.api-docs.enabled=false`、`swagger-ui.enabled=false`。
- 应用开启优雅停机：`server.shutdown=graceful`、`spring.lifecycle.timeout-per-shutdown-phase=30s`。
- `docker-compose.yml`：
  - 8888/3306 只绑定 `127.0.0.1`，由 Nginx/内网访问；
  - 应用 `stop_grace_period: 40s`；
  - `json-file` 日志限制 `100m × 5`。
- `SensitiveMasker` 新增无引号 `password=...` / `refreshToken=...` 脱敏，覆盖 DTO `toString()` 日志。
- **仍需运维处理**：密钥轮换、`/static/**` 上传文件公开访问、CORS 白名单、MySQL 最小权限账号、`wuye_points_latest.sql` 的 PII/演示数据清理。

## 104.7 升级注意

1. **V28 必须先执行**：否则新 token 的 `tv` 校验会因 `residents.token_version` 不存在而报错。
2. **上线后旧 refresh token 失效**：所有用户需重新登录一次；这是为了彻底关闭“登出后旧 refresh 仍可续期”的窗口。
3. **广告微信支付默认开放（v8.26）**：前端需支持 `pending_payment` 待支付态、`payParams` 调起 `wx.requestPayment`、`/merchant/ads/{id}/repay` 继续支付；如前端暂未完成，部署时置 `MERCHANT_AD_WECHAT_PAYMENT_ENABLED=false` 临时关闭。
4. **支付规则收紧**：前端必须移除试算失败时的本地金额兜底；否则用户会在支付提交阶段收到 400。
5. **C1 requestId 幂等**：已在 v8.19 实现，见 §105；上线前需执行 V29 建 `idempotency_records` 表。

---

# 105. C1 写接口幂等（v8.19）

## 105.0 数据库变更

| 脚本 | 内容 | 是否必须 |
| --- | --- | --- |
| `sql/上线修复第四批_数据库变更_20261024.sql`（Flyway V29） | `idempotency_records` 表（用户+请求哈希唯一、响应体、TTL 索引） | 必须，在 V28 之后执行 |
| `sql/上线前结构核对_20261024.sql` | 已追加幂等表与唯一键核对项 | 上线前执行 |

## 105.1 前端怎么用

对需要防重复的写接口，在请求头携带：

```http
X-Request-Id: 550e8400-e29b-41d4-a716-446655440000
```

规则：

- 每次**新操作**生成一个新 ID（UUID 即可）；用户重试/弱网重发时**复用同一个 ID**。
- 长度 8~128，只允许字母、数字、`.`、`_`、`:`、`-`。
- 不需要改请求体；不传 `X-Request-Id` 时接口行为与以前一致（不做幂等）。
- 同一个 ID 不要跨接口/跨业务复用；后端 key 已包含用户+方法+路径，跨路径不会误回放，但语义上应一操作一 ID。

## 105.2 覆盖接口

| 接口 | 说明 |
| --- | --- |
| `POST /orders` | 创建订单 |
| `POST /orders/from-listing` | 业主小店下单 |
| `POST /orders/{id}/pay` | 订单支付（含微信预下单 payParams 回放） |
| `POST /orders/{id}/cancel` | 取消订单 |
| `POST /merchant/withdrawals` | 商家提现申请 |
| `POST /activity-groups/{id}/subscribe` | 活动组订阅 |
| `POST /activity-groups/{id}/join` | 加入活动组 |

## 105.3 重复请求行为

| 场景 | 后端行为 | 前端看到 |
| --- | --- | --- |
| 首次请求 | 正常执行，成功后缓存响应 | 正常响应 |
| 重复请求（首次已完成） | 不执行业务，回放首次响应体 | 与首次相同的 200 响应 |
| 重复请求（首次处理中） | 最多等待 2 秒；完成则回放，否则返回 409 | `70003 请求处理中，请稍后重试` |
| 首次请求业务失败 | 删除幂等记录，允许同一 ID 重试 | 原始业务错误 |
| 无 `X-Request-Id` | 直接执行业务，不做幂等 | 与旧版本一致 |
| `X-Request-Id` 格式错误 | 不执行业务 | `10002 X-Request-Id格式不正确` |

## 105.4 TTL 与清理

- `processing` 记录保留 2 分钟；超过视为异常中断，可被新请求接管。
- `completed` 记录保留 2 小时；覆盖用户重试窗口，同时避免长期缓存微信 `payParams`。
- 每日 04:30 定时清理过期记录（ShedLock，多实例只跑一次）。

## 105.5 边界与残余风险

1. **崩溃窗口**：业务事务已提交、幂等记录尚未写 completed 时进程崩溃，记录会停在 processing；2 分钟后同一 ID 可重试，理论上存在重复执行可能。订单/提现等业务仍有各自的唯一约束、余额校验和状态机兜底；上线后建议监控 `idempotency_records` 中长时间 processing 的记录。
2. **响应体存储**：成功响应会以 JSON 存入 `idempotency_records.response_body`（最长 2 小时），可能包含微信 `payParams`、订单金额等；数据库账号需最小权限，备份需加密。
3. **大响应**：超过 1MB 的响应不缓存，但仍正常返回；当前覆盖接口响应均远小于该值。
4. **并发重复**：后到请求等待 2 秒仍未完成会收到 409；前端可提示“处理中，请稍后查看”，不要自动换新 requestId 重试。

## 105.6 前端检查清单

- [ ] 下单、支付、取消、提现、活动报名都生成 `X-Request-Id`。
- [ ] 弱网重试/用户再次点击时复用同一 ID，不生成新 ID。
- [ ] 409 `请求处理中` 不自动重试新 ID，提示用户稍后查看结果。
- [ ] 成功后清理该操作的 requestId；下一次新操作重新生成。
- [ ] 确认 V29 已执行，结构核对中幂等表与唯一键 present=1。

---

# 106. 平台交易明细（v8.20）

## 106.1 权限与边界

| 项目 | 规则 |
| --- | --- |
| 角色 | **仅 `platform_admin`**；`property_admin`、统筹等一律 `403 / 20004 权限不足` |
| 权限码 | 不新增权限码，不能通过授权开放给非平台管理员 |
| 数据范围 | 不传 `propertyCompanyId` 查全部物业；传则按物业过滤 |
| 默认时间 | 不传时间默认最近 30 天（含今天） |
| 分页 | `page >= 1`，`pageSize` 1~100，超过 100 返回 `10003` |
| 导出 | 单次最多 50000 行，超过时截断并返回 `X-Export-Truncated: true` |
| 只读 | 全部为查询接口，不写库、不触发任何资金动作 |

## 106.2 账本口径

| category | 中文 | 来源表 | 时间字段 | amount 口径 | unit |
| --- | --- | --- | --- | --- | --- |
| `payment` | 支付 | `payment_records` | `created_at` | 应付总额（含积分/物业币/定向余额抵扣） | CNY |
| `refund` | 退款 | `payment_records`（有退款单号或退款状态） | `updated_at` | 退回的现金部分 | CNY |
| `wallet` | 钱包流水 | `wallet_flows` | `created_at` | 流水绝对值，正负看 `direction` | CNY |
| `distribution` | 订单分账 | `distribution_records` | `created_at` | 可分配金额 | CNY |
| `split` | 微信分账 | `wechat_split_record` | `created_at` | 冻结/分账金额 | CNY |
| `withdrawal` | 提现 | `merchant_withdrawals` + `coin_withdrawals` + `withdrawal_records` | `created_at` | 申请提现金额 | CNY |
| `transfer` | 内部转账 | `merchant_transfers` | `created_at` | 转账金额 | CNY |
| `settlement` | 结算 | `resident_merchant_settlements` | `created_at` | 转卖利润 | CNY |
| `point` | 积分 | `point_records` | `created_at` | 积分绝对值 | POINT |
| `coin` | 物业币 | `property_coin_records` | `created_at` | 物业币绝对值 | COIN |
| `canteen` | 食堂定向余额 | `canteen_directed_flows` | `created_at` | 消费/退回金额 | CNY_DIRECTED |

**分组与分类对照：**

| group | 中文 | 包含 category |
| --- | --- | --- |
| `PAYMENT` | 支付与退款 | `payment`、`refund` |
| `WALLET` | 钱包与分账 | `wallet`、`distribution`、`split` |
| `SETTLEMENT` | 提现与结算 | `withdrawal`、`transfer`、`settlement` |
| `VALUE` | 积分与物业币 | `point`、`coin`、`canteen` |

## 106.3 接口

### 106.3.1 GET `/admin/transactions` — 交易明细列表

`GET /admin/transactions`

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `propertyCompanyId` | String | 否 | 物业公司ID，`pc_` 开头；不传=全部 |
| `startDate` | String | 否 | 支持 `yyyy-MM-dd`、`yyyy-MM-dd HH:mm:ss`、ISO8601；默认 30 天前 |
| `endDate` | String | 否 | 同上；纯日期按当天 23:59:59 |
| `group` | String | 否 | `PAYMENT` / `WALLET` / `SETTLEMENT` / `VALUE` |
| `category` | String | 否 | 见 §106.2；与 `group` 同时传时必须匹配 |
| `direction` | String | 否 | `income` / `expense` / `neutral` |
| `status` | String | 否 | 业务原始状态，如 `paid`、`pending_audit`、`finished` |
| `keyword` | String | 否 | 最长 100 字；匹配交易ID、业务单号、订单号、渠道流水号、主体名称、说明、物业名称 |
| `page` | int | 否 | 默认 1 |
| `pageSize` | int | 否 | 默认 20，最大 100 |

响应行字段：

| 字段 | 说明 |
| --- | --- |
| `id` | 来源记录ID（`payment`/`refund` 可能共用同一个支付流水ID） |
| `category` / `categoryLabel` | 分类码 / 中文名 |
| `group` / `groupLabel` | 分组码 / 中文名 |
| `occurredAt` | 发生时间（`yyyy-MM-dd HH:mm:ss`） |
| `direction` / `directionLabel` | `income` 收入 / `expense` 支出 / `neutral` 不计收支 |
| `unit` / `unitLabel` | `CNY` 元 / `POINT` 积分 / `COIN` 物业币 / `CNY_DIRECTED` 定向余额(元) |
| `amount` | 金额绝对值（方向见 `direction`） |
| `feeAmount` | 手续费（提现） |
| `netAmount` | 实收/实付/净额（支付=现金部分；提现=实际到账；分账=订单总额） |
| `platformAmount` | 平台分成/佣金 |
| `status` / `statusLabel` | 原始状态 / 中文状态 |
| `bizType` / `typeLabel` | 业务类型 / 中文类型（如 `merchant_goods_income` / 商家商品收入） |
| `description` | 说明或失败原因 |
| `orderId` / `bizId` / `tradeNo` | 订单号 / 业务单号 / 渠道流水号 |
| `partyType` / `partyId` / `partyName` | 资金主体类型/ID/名称（商家、物业、统筹、负责人、配送员、住户、平台） |
| `propertyCompanyId` / `propertyCompanyName` | 物业公司 |
| `cashAmount` | 支付现金部分 |
| `pointUsed` / `coinUsed` / `directedUsed` | 支付时的积分/物业币/定向余额抵扣 |

示例：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "pr_xxx",
        "category": "payment",
        "categoryLabel": "支付",
        "group": "PAYMENT",
        "groupLabel": "支付与退款",
        "occurredAt": "2026-10-20 10:00:00",
        "direction": "income",
        "directionLabel": "收入",
        "unit": "CNY",
        "unitLabel": "元",
        "amount": 100.00,
        "feeAmount": 0.00,
        "netAmount": 90.00,
        "platformAmount": 0.00,
        "status": "paid",
        "statusLabel": "已支付",
        "bizType": "order",
        "typeLabel": "商品订单",
        "description": "",
        "orderId": "ord_xxx",
        "bizId": "ord_xxx",
        "tradeNo": "wx_xxx",
        "partyType": "resident",
        "partyId": "r_xxx",
        "partyName": "张三",
        "propertyCompanyId": "pc_xxx",
        "propertyCompanyName": "示例物业",
        "cashAmount": 90.00,
        "pointUsed": 500,
        "coinUsed": 5.00,
        "directedUsed": 0.00
      }
    ],
    "pagination": { "page": 1, "pageSize": 20, "total": 1, "totalPages": 1 }
  }
}
```

### 106.3.2 GET `/admin/transactions/overview` — 交易总览

`GET /admin/transactions/overview`

**Query 参数**（同 §106.3.1，不含 `page`/`pageSize`）：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `propertyCompanyId` | String | 否 | 物业公司ID，`pc_` 开头；不传=全部 |
| `startDate` | String | 否 | 开始时间；默认 30 天前 |
| `endDate` | String | 否 | 结束时间 |
| `group` | String | 否 | `PAYMENT`/`WALLET`/`SETTLEMENT`/`VALUE` |
| `category` | String | 否 | 见 §106.2 |
| `direction` | String | 否 | `income`/`expense`/`neutral` |
| `status` | String | 否 | 业务原始状态 |
| `keyword` | String | 否 | 关键词 |

响应：

| 字段 | 说明 |
| --- | --- |
| `range` | 实际生效的 `start` / `end` / `propertyCompanyId` |
| `note` | 口径说明（分组合计剔除重叠分类） |
| `groups[]` | 按 `group + unit` 汇总：`count`、`volumeAmount`、`incomeAmount`、`expenseAmount`、`feeAmount`、`actualAmount`、`platformAmount` |
| `categories[]` | 按 `category` 汇总，字段同上 |
| `statusBreakdown[]` | 按 `category + status` 汇总笔数与金额 |
| `trend[]` | 按日 + 单位：`date`、`incomeAmount`、`expenseAmount`、`count` |

**重要口径：**

1. 不同 `unit`（元/积分/物业币/定向余额）**不可直接相加**，前端必须分开展示。
2. 一笔已退款支付会同时产生：`payment` 收入行（原支付）+ `refund` 支出行（退款），这是总额口径，不是重复。
3. `wallet` 是官方钱包账本；`distribution`、`split` 是分账过程快照，`settlement` 与钱包入账可能重叠。`groups[]` 默认只汇总非重叠分类（payment/refund/wallet/withdrawal/transfer/point/coin/canteen）；传入具体 `category` 时该分类会进入分组汇总。`categories[]` 始终完整。
4. `amount` 永远是非负数，收/支看 `direction`。
5. 只有**真实发生**的资金移动才计入收入/支出：待支付/已关闭支付、待审核/已驳回提现、失败/关闭转账方向为 `neutral`；其金额仍计入 `volumeAmount` 和分类笔数。

### 106.3.3 GET `/admin/transactions/export` — 导出 CSV

`GET /admin/transactions/export`

**Query 参数**（同 §106.3.1，不含 `page`/`pageSize`）：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `propertyCompanyId` | String | 否 | 物业公司ID；不传=全部 |
| `startDate` | String | 否 | 开始时间；默认 30 天前 |
| `endDate` | String | 否 | 结束时间 |
| `group` | String | 否 | `PAYMENT`/`WALLET`/`SETTLEMENT`/`VALUE` |
| `category` | String | 否 | 见 §106.2 |
| `direction` | String | 否 | `income`/`expense`/`neutral` |
| `status` | String | 否 | 业务原始状态 |
| `keyword` | String | 否 | 关键词 |

返回 `text/csv;charset=UTF-8`：

- 文件带 UTF-8 BOM，Excel 直接双击不乱码；
- 单次最多 50000 行；
- 响应头：
  - `X-Export-Rows`：实际导出行数；
  - `X-Export-Truncated`：`true` 表示超过 50000 行已截断，需缩小时间范围。
- 以 `= + - @` 开头的文本会自动加单引号，防止 Excel 公式注入。

CSV 列：交易时间、交易分类、分类码、所属分组、收支方向、金额、单位、状态、状态码、业务类型、业务单号、订单号、渠道流水号、交易ID、主体类型、主体ID、主体名称、物业公司ID、物业公司、手续费、实收/实付、平台分成、现金部分、积分抵扣、物业币抵扣、定向余额抵扣、说明。

### 106.3.4 GET `/admin/transactions/detail` — 流水详情（打印/对账用）🆕 v8.34

**Auth**: Required | **Roles**: `platform_admin`（硬校验，物业管理员即使被授权也返回 `403 / 20004`）

按「分类 + 记录ID」精确取**一条**流水，并把打印要用的上下文一次给齐：来源表、关联单号、可继续调用的接口。前端/外包不需要猜路径，也不需要把整页列表带进打印页。

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `category` | `String` | ✅ | 11 类之一：`payment`/`refund`/`wallet`/`distribution`/`split`/`withdrawal`/`transfer`/`settlement`/`point`/`coin`/`canteen`。**必传**：`payment` 与 `refund` 共用同一条 `payment_records.id`，只给 ID 分不清收款还是退款，打出来的单子方向会反 |
| `id` | `String` | ✅ | 来源记录ID（≤64 字符），即列表行的 `id` |

**成功响应**（`TransactionDetailResponse`）：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "transaction": {
      "id": "pr_1853201745993",
      "category": "payment",
      "categoryLabel": "支付",
      "group": "PAYMENT",
      "groupLabel": "支付与退款",
      "occurredAt": "2026-11-03 10:21:44",
      "direction": "income",
      "directionLabel": "收入",
      "unit": "CNY",
      "unitLabel": "元",
      "amount": 100.00,
      "feeAmount": 0.00,
      "netAmount": 90.00,
      "platformAmount": 0.00,
      "status": "paid",
      "statusLabel": "已支付",
      "bizType": "order",
      "typeLabel": "商品订单",
      "description": "",
      "orderId": "ord_1853201702331",
      "bizId": "ord_1853201702331",
      "tradeNo": "4200002381202611035566778899",
      "partyType": "resident",
      "partyId": "res_1799203344112",
      "partyName": "张三",
      "propertyCompanyId": "pc_demo001",
      "propertyCompanyName": "连云港阳光物业",
      "cashAmount": 90.00,
      "pointUsed": 500,
      "coinUsed": 5.00,
      "directedUsed": 0.00
    },
    "sourceTable": "payment_records",
    "relatedKeyword": "ord_1853201702331",
    "links": [
      { "label": "同一单据的全部流水", "method": "GET", "path": "/admin/transactions?keyword=ord_1853201702331&pageSize=100" },
      { "label": "订单详情", "method": "GET", "path": "/orders/ord_1853201702331" },
      { "label": "退货审核列表", "method": "GET", "path": "/admin/orders/refunds?keyword=ord_1853201702331" }
    ]
  }
}
```

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `transaction` | `TransactionItemResponse` | 流水行本体，25 个字段与 §106.3.1 列表行**完全一致**（同一套映射，不存在字段名分叉） |
| `sourceTable` | `String` | 来源表名，供单据标注数据出处：`payment_records` / `wallet_flows` / `distribution_records` / `wechat_split_record` / `merchant_withdrawals` / `coin_withdrawals` / `withdrawal_records` / `merchant_transfers` / `resident_merchant_settlements` / `point_records` / `property_coin_records` / `canteen_directed_flows` |
| `relatedKeyword` | `String` | 关联单号：有 `orderId` 用订单号，否则用 `bizId`。直接塞进 §106.3.1 的 `keyword` 就能把同一笔单据的收款→分账→入账→提现整条链捞全 |
| `links[]` | `label`/`method`/`path` | 后端算好的关联接口（相对 Base URL，仍需自带 `Authorization`）。**只列已存在且 platform_admin 可访问的路径**，前端顺着打就行 |

`links` 按分类给出（有对应 ID 才会出现）：

| 分类 | 会带上的 links |
| --- | --- |
| `payment` / `refund` | 同一单据全部流水、`GET /orders/{orderId}`、`GET /admin/orders/refunds?keyword={orderId}` |
| `wallet` / `distribution` / `split` | 同一单据全部流水、`GET /orders/{orderId}`、`GET /distribution/records?orderId={orderId}` |
| `withdrawal` | `merchant_withdrawal` → `GET /admin/merchant-withdrawals?merchantId={partyId}`；其余 → `GET /admin/role-withdrawals?keyword={bizId}` |
| `transfer` | 仅同一单据全部流水（无独立管理端列表接口） |
| `settlement` | `GET /admin/resident-merchant-settlements` |
| `canteen` | `GET /admin/canteen/flows?residentId={partyId}` |
| `point` / `coin` | `GET /residents/{partyId}`（住户详情） |

**错误响应**：

| 错误码 | HTTP | 说明 |
| --- | --- | --- |
| `10001` | 400 | `category` 传空，或 `id` 为空 |
| `10002` | 400 | `category` 不在 11 类内；`id` 超过 64 字符 |
| `97075` | 404 | 🆕 `TRANSACTION_NOT_FOUND` 交易流水不存在。**分类传错也会落这里**（如把退款当 `payment` 查），不代表单号写错 |
| `20004` | 403 | 非 `platform_admin` |

**性能口径**：`id` 谓词下推到对应来源表（只展开一个 UNION 分支、走主键），不会因为查一条而把整段账本物化；时间窗在本接口里不参与过滤（兜底 1970→2100），所以**历史单也能查到**。

## 106.4 前端页面建议

1. 顶部 4 张卡片：支付与退款、钱包与分账、提现与结算、积分与物业币；每张卡片内按 `unit` 分行，不要跨单位求和。
2. 中部趋势图：直接使用 `trend[]`，按 `unit` 分多条线；金额类默认看 `CNY`。
3. 明细表：列建议「时间 / 分类 / 方向 / 金额 / 状态 / 业务类型 / 主体 / 物业 / 订单号 / 说明」，点击行可展开 `cashAmount/pointUsed/coinUsed/directedUsed`。
4. 导出按钮直接下载 `/admin/transactions/export`，参数与当前筛选一致。
5. 入口仅对 `platform_admin` 展示；后端已做硬校验，前端隐藏只是体验优化。

## 106.5 前端检查清单

- [ ] PC 管理端新增「交易明细」菜单，仅 platform_admin 可见。
- [ ] 筛选默认最近 30 天；切换物业、分组、分类、方向、状态、关键词时重置到第 1 页。
- [ ] 金额按 `unit` 分组展示，不把积分/物业币与元相加。
- [ ] 已退款订单同时展示原支付收入行与退款支出行，不要误判为重复数据。
- [ ] 总览卡片使用 `groups[]` 或 `categories[]`，并在旁边展示 `note` 口径说明。
- [ ] 导出后若 `X-Export-Truncated=true`，提示用户缩小时间范围。
- [ ] 明细行加「查看详情」：调 §106.3.4 `GET /admin/transactions/detail?category=&id=`，用返回的 `links` 顺出同一单据的关联流水。

## 106.6 数据库变更

本功能**不新增表、不新增列**，不执行任何 SQL 也能上线。

可选索引脚本：`sql/交易明细查询索引_v8.20_20261024.sql`，为各账本的 `created_at` / `property_company_id` 补复合索引，可重复执行，不删除现有索引。建议数据量增长后执行。

## 106.7 常见问题

| 现象 | 原因 / 处理 |
| --- | --- |
| 返回 403 / 20004 | 当前账号不是 platform_admin；本功能不开放授权 |
| 明细为空 | 默认只查最近 30 天；检查 `startDate/endDate` 与 `propertyCompanyId` |
| 支付与退款出现同一个 `id` | 同一支付流水的原支付行与退款行，用 `category` 区分；查详情必须 `category` + `id` 一起传（§106.3.4） |
| 详情接口返回 `97075` | 单号或分类不对：`refund` 的行用 `payment` 查一定未命中。先用 `GET /admin/transactions?keyword=<流水ID>` 找到该行的 `category` |
| 分组合计小于分类之和 | 分组合计默认剔除了 distribution/split/settlement 等重叠分类，见 §106.3.2 |
| 导出只有 50000 行 | 单次导出上限，缩小时间范围或按物业/分类分批导出 |

---

# 107. v8.22 前后端联调（2026-10-26）

> 本章为本次改造的完整契约。**字段名以本章为准**；未列出的字段保持不变。
> 金额单位：元（保留 2 位）；积分单位：分（整数）；奖励金单位：元。

## 107.1 变更总览

| 模块 | 接口 | 类型 | 兼容性 |
| --- | --- | --- | --- |
| 商家 | `GET /merchants/my` | 新增返回字段 | 向后兼容 |
| 广告 | `GET /merchant/ads/quote`、`POST /merchant/ads`、`POST /merchant/ads/targeted`、`GET /merchant/ads` | 规则变更 + 字段 | **不兼容 coin/point/mock** |
| 代理 | `GET /distributors/my/orders` | **新增接口** | 新增 |
| 代理 | `GET /resident-merchants/settlements` | 新增返回字段 | 向后兼容 |
| 楼长 | `GET /building-leaders/my`、`POST /building-leaders/{id}/withdrawals` | 口径变更 | 字段名不变，数值口径变 |
| 消息 | `GET /messages/conversations`、`GET /messages/neighbors` | 新增返回字段 | 向后兼容 |
| 消息 | `POST /messages`、`POST /messages/neighbor-chat` | 新增可选入参 | 向后兼容 |
| 商家 | `GET /merchants/official-recommended`、`GET /merchants?recommended=true` | 结果范围扩大 | 向后兼容 |
| 奖励金 | `POST /property-coins/transfer-to-wallet` | **新增接口** | 新增 |
| 积分 | `POST /residents/me/points/transfer-to-property` | 规则变更 + 字段 | **默认不可用，需物业公司开启** |
| 积分 | `POST /points/transfer` | **已下线** | 调用返回业务错误 |
| 订单 | `GET /orders/*`、`GET /merchants/my/sales` | 新增返回字段 | 向后兼容 |
| 活动组 | 创建/更新/详情/列表 | 新增费用字段 | 向后兼容 |
| 商品 | `GET /products` | 新增返回字段 | 向后兼容 |
| 货源 | `GET /distributors/products` | 结果按供给范围过滤 | 行为变更 |

## 107.2 商家页汇总字段（问题 8 / 14）

`GET /merchants/my` 新增：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `totalOrders` | number | 该商家累计订单总数（不含 `cancelled/refunded`） |
| `pointBalance` | number | 商家购分账户剩余积分（与 `/merchant/points/*` 同一账户，已审核通过批次余额之和） |
| `profitBalance` | number | 盈利余额，**本店铺口径**（不含楼长分成/业主卖货收益）；账号统一口径见 §108.1 `accountWithdrawableAmount` |

示例：

```json
{
  "code": 0,
  "data": {
    "id": "mch_xxx",
    "name": "幸福超市",
    "withdrawableAmount": 1280.50,
    "totalOrders": 128,
    "pointBalance": 5000,
    "profitBalance": 1280.50
  }
}
```

> `coinBalance` 若返回，语义仍是「商家绑定居民的物业币余额」，**不要**当作积分余额。

## 107.3 广告：免费额度永久一次 + 微信现金

### 107.3.1 支付方式

| paymentMethod | 状态 | 说明 |
| --- | --- | --- |
| `free` | 可用 | 免费额度，**每个商家永久仅一次**，不再按周/按月重置 |
| `wechat` | 可用（受开关控制） | 微信小程序现金支付，返回 `payParams` |
| `coin` / `point` / `mock` | **已下线** | 返回 400：`广告已不支持物业币、商家积分或模拟支付，请使用免费额度或微信支付` |

### 107.3.2 报价 `GET /merchant/ads/quote?days=7`

- `freeQuotaRemaining`：`freeQuotaEnabled=true` 且该商家历史从未用过免费额度时为 1（或配置值），否则 0。
- `freeQuotaPerWeek`：保留字段，含义为配置值，前端不要再用它判断“本周额度”。
- `payAmount = 单日价 × 天数`。

### 107.3.3 下单 `POST /merchant/ads` / `POST /merchant/ads/targeted`

`paymentMethod=wechat` 且金额 > 0 时，广告先落 `pending_payment`，响应：

```json
{
  "code": 0,
  "data": {
    "id": "mad_xxx",
    "status": "pending_payment",
    "payStatus": "unpaid",
    "payAmount": 70.00,
    "paymentRecordId": "pay_xxx",
    "payParams": {
      "timeStamp": "1730000000",
      "nonceStr": "abc123",
      "package": "prepay_id=wx123",
      "signType": "RSA",
      "paySign": "xxxx"
    }
  }
}
```

- 拿到 `payParams` 立即 `wx.requestPayment`；**没有 `payParams` 不要当免费成功**。
- 支付回调成功后广告置 `active`，`startDate/endDate` 从支付成功起算。
- 支付超时/失败保持 `pending_payment`，不投放；超时由后端置 `cancelled`。

### 107.3.4 列表 `GET /merchant/ads`

`MerchantAdListItemResponse` 新增：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `payStatus` | string | `unpaid` 待支付 / `paid` 已支付 / `refunded` 已退款 |
| `paymentRecordId` | string | 支付流水ID（待支付时可用于查状态） |

> **v8.23 补充**：列表还会返回 `status`（pending_payment/active/ended/cancelled）、`canRepay`、`remainingDays`；
> 待支付广告可调 `POST /merchant/ads/{id}/repay` 重新获取 `payParams`。详见 §108.2 / §108.4。

## 107.4 代理货源订单归属一级代理商

### 107.4.1 业务口径

- 业主在业主小店购买的是「一级代理商的货」；
- 下单后：**订单、发货、结算、售后均面向一级代理商**；
- 业主小店只做代销，按原差价逻辑拿佣金，不再作为结算主体；
- 订单的 `merchantId` 写一级代理商商家ID，`residentMerchantId` 写业主小店店铺主体ID；
  因此一级代理商可直接使用既有商家履约接口 `POST /merchants/my/orders/{orderId}/fulfillment` 发货；
- 一级代理商在 `GET /distributors/my/orders` 能看到完整购买记录（含地址）。

### 107.4.2 订单字段

订单列表/详情（`GET /orders/*`、`GET /merchants/my/sales`）新增：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `sourceType` | string | `mall` 商城/店铺直购；`distributor` 业主小店代销代理货源 |
| `distributorId` | string | 一级代理商商家ID（代销订单） |
| `residentMerchantId` | string | 业主小店店铺主体ID（代销场景） |
| `settlementOwner` | string | `merchant` 或 `distributor` |

### 107.4.3 新增：一级代理商用户购买记录

`GET /distributors/my/orders?page=1&pageSize=20&orderStatus=completed`

- 权限：当前登录人名下 `is_distributor=1` 且审核通过的商家；
- 返回：商品、件数、成交金额、代销小店、脱敏买家、收货地址、订单状态。

```json
{
  "code": 0,
  "data": {
    "list": [
      {
        "orderId": "ord_xxx",
        "orderNo": "202610260001",
        "orderStatus": "completed",
        "totalAmount": 39.90,
        "quantity": 2,
        "productName": "五常大米 5kg",
        "productCover": "https://...",
        "shopName": "张阿姨的小店",
        "buyerName": "张*",
        "buyerPhone": "138****8000",
        "address": "幸福小区3栋2单元501",
        "createdAt": "2026-10-26T10:00:00.000Z",
        "settlementOwner": "distributor"
      }
    ],
    "page": 1,
    "pageSize": 20,
    "total": 1
  }
}
```

### 107.4.4 业主小店卖货明细

`GET /resident-merchants/settlements` 每项新增：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `commissionAmount` | number | 店主佣金，与 `resellerProfit` 同值 |
| `distributorMerchantId` | string | 结算主体（一级代理商） |
| `sourceType` | string | 固定 `distributor` |
| `settlementOwner` | string | 固定 `distributor` |

> 前端按 `sourceType === 'distributor'` 展示「一级代理直供 / 佣金」即可；不要再用 `orderAmount` 当店主到账金额。

## 107.5 楼长收益并入账号统一收益账户

`GET /building-leaders/my` 字段名不变，数值口径改为**账号统一收益**：

| 字段 | 新口径 |
| --- | --- |
| `totalEarning` | 楼长分成 + 业主卖货收益 + 名下商家剩余可提现 |
| `withdrawnAmount` | 账号下全部角色已完成提现 |
| `pendingAmount` | 账号下全部角色在途提现（pending + approved） |
| `withdrawableAmount` | 统一可提现 = 总收益 − 已提现/在途 − 已由微信分账到账部分 |

`POST /building-leaders/{id}/withdrawals` 从统一账户校验并扣减；`GET /building-leaders/{id}/withdrawals` 仍只列楼长类型记录（历史流水口径不变）。

> 商家提现、业主卖货提现也加了统一账户兜底，避免同一住户用不同角色重复提同一笔钱。

## 107.6 会话列表楼长标识

`GET /messages/conversations` 每项新增：

```json
{ "peerId": "res_xxx", "peerName": "李四", "isBuildingLeader": true }
```

- 楼长 `role` 仍为 `resident`，只靠该字段区分；
- 前端兼容顺序：`isBuildingLeader` → `peerIsBuildingLeader` → `isUnitLeader` → `peerRole` → `peerRoleName`。

`GET /messages/neighbors` 每项也新增 `isBuildingLeader` / `isUnitLeader`，并**楼长/单元负责人排在最前**。

> **v8.23 补充**：`GET /messages/conversations` 也已返回 `isUnitLeader` 与 `leaderRole`，会话列表可直接置顶/打标签。详见 §108.5。

## 107.7 沿街店铺推荐排序

- `GET /merchants/official-recommended` 与 `GET /merchants?recommended=true` 返回项继续带 `isRecommended`、`recommendedSort`，顺序按 `recommendedSort DESC, rankOrder DESC, levelWeight DESC`；
- v8.22 起，被推荐的业主小店/代销小店**也会出现在推荐结果中**（此前被 `merchant_source=resident_shop` 过滤掉）；
- 待确认项：推荐位是否排除食堂商家（当前不排除，推荐什么返回什么）。

## 107.8 奖励金转钱包（新增）

`POST /property-coins/transfer-to-wallet`

请求：

```json
{ "coinAmount": 100.00 }
```

响应：

```json
{
  "code": 0,
  "data": {
    "recordId": "coin_xxx",
    "coinBalance": 40.00,
    "walletBalance": 100.00
  }
}
```

规则：

- 只能从**个人奖励金**转出，不动家庭池；
- 转出后奖励金减少、钱包（`wallet_flows.owner_type=resident`，`entry_type=coin_transfer_to_wallet`）增加；
- 生成 `source=transfer_to_wallet` 的 `spend` 流水；
- 不可撤销；余额不足返回 `COIN_INSUFFICIENT`；
- **v8.26 起**：奖励金扣减与钱包流水写入同一事务，避免扣减回滚后钱包已入账。

### 107.8.1 GET `/wallet/my` — 我的钱包余额（v8.26）

**Auth**: Required  
**Roles**: `resident`

响应：

```json
{
  "code": 0,
  "data": {
    "coinBalance": 40.00,
    "walletBalance": 100.00,
    "occupiedWithdrawalAmount": 40.00,
    "withdrawableAmount": 60.00,
    "withdrawalBlocked": false
  }
}
```

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `coinBalance` | BigDecimal | 个人奖励金（物业币）余额，用于继续转钱包 |
| `walletBalance` | BigDecimal | 钱包累计余额（`wallet_flows` 可提现类型汇总，未扣在途提现） |
| `occupiedWithdrawalAmount` | BigDecimal | 在途提现占用（`pending/approved`） |
| `withdrawableAmount` | BigDecimal | 可提现余额 = `walletBalance − occupiedWithdrawalAmount` |
| `withdrawalBlocked` | Boolean | 提现是否被冻结 |

### 107.8.2 POST `/wallet/withdrawals` — 住户钱包提现申请（v8.26）

**Auth**: Required  
**Roles**: `resident`

请求：

```json
{ "amount": 40.00 }
```

响应：`RoleWithdrawalItemResponse`（`withdrawalType=resident_wallet`，`status=pending`）。

规则：

- 只能提现钱包余额（由个人奖励金转入），**家庭池不参与**；
- 可提现余额 = 钱包累计余额 − 在途提现（`pending/approved`）；超过返回 `94006`，提现被冻结返回 `94005`；
- 手续费率与多角色提现一致（物业公司 `withdrawal_fee_rate` + 平台配置）；
- 审核流程：`pending → approved → completed`，或 `approved → rejected`；`pending` 不能直接 `completed`。

### 107.8.3 GET `/wallet/withdrawals` — 我的钱包提现记录（v8.26）

**Auth**: Required  
**Roles**: `resident`

**Query**: `page`、`pageSize`、`auditStatus`（`pending/approved/rejected/completed`）、`startDate`、`endDate`。

响应：`PageResponse<RoleWithdrawalItemResponse>`。

### 107.8.4 管理端审核（复用既有接口）

- 列表：`GET /admin/role-withdrawals?withdrawalType=resident_wallet`
- 审核：`POST /admin/role-withdrawals/{id}/audit`，`auditResult=approved/rejected/completed`
- `approved → completed` 时后端在同一事务内写入负向 `wallet_flows`（`entry_type=resident_wallet_withdrawal`）冲减钱包余额；`rejected` 释放占用，不写流水。

## 107.9 积分：用途收敛为「转物业抵物业费」

### 107.9.1 业务规则

- 积分**唯一用途**：转给物业公司，抵扣物业费；
- 积分在消费/交易中产生（订单返积分等），不再支持商城下单抵扣、家庭内转赠；
- 物业公司可配置「满 N 年才能转」（默认 5 年），并由总开关控制是否开放（**默认关闭**）。

### 107.9.2 转物业 `POST /residents/me/points/transfer-to-property`

请求不变：`{ "pointAmount": 100, "remark": "可选" }`

新增/变更：

| 项 | 说明 |
| --- | --- |
| 开关 | `property_companies.point_transfer_enabled=false` 时返回 400：`积分转物业费暂未开放` |
| 年限 | 入住未满 `point_transfer_min_years`（默认 5）年返回 400：`入住未满5年，暂不可将积分转物业费` |
| 冲抵顺序 | 按 `due_date` 最早账期依次冲抵 `unpaid/partial/overdue` 账单 |
| 回写 | 更新 `paid_amount/point_deducted/payment_status/paid_at`，前端物业费页可直接看到欠款减少 |
| 响应新增 | `appliedFeeAmount` 实际冲抵金额 |

响应示例：

```json
{
  "code": 0,
  "data": {
    "recordId": "pt_xxx",
    "residentId": "res_xxx",
    "pointAmount": 100,
    "balanceAfter": 400,
    "appliedFeeAmount": 1.00,
    "transferredAt": "2026-10-26T10:00:00Z"
  }
}
```

### 107.9.3 已关闭的入口

| 入口 | 行为 |
| --- | --- |
| `POST /points/transfer`（家庭内转赠） | 400：`积分仅可用于抵扣物业费，暂不支持家庭内转赠` |
| 商城下单 `pointUsed > 0`（`/orders/preview`、`/orders`、`/orders/from-listing/preview`） | 400：`积分仅可用于抵扣物业费，商城下单不支持积分抵扣` |

> 前端请移除商城结算页的积分抵扣勾选项；`pointUsed` 固定传 0 或不传。

## 107.10 视频增量后端项

### 107.10.1 一级代理货源供给范围

`distributor_products.supply_scope`：

| 值 | 含义 |
| --- | --- |
| `company`（默认） | 仅本物业公司的业主小店可选品/拿货 |
| `platform` | 全平台所有物业公司的业主小店均可代销（超级代理商） |

`GET /distributors/products` 已按当前住户物业公司过滤；拿货时越权返回 403。

> **v8.23 补充**：`supply_scope` 只能由平台/物业管理员通过管理端创建/更新接口设置；
> 代理端只读，`POST/PUT /distributors/my/products` 传该字段返回 403。详见 §108.7。

### 107.10.2 活动组按季度/按次计费

`ActivityGroup` 新增 `quarterlyFee`、`perTimeFee`；订阅 `subType` 新增：

| subType | 金额字段 | 订阅有效期 |
| --- | --- | --- |
| `monthly` | `monthlyFee` | 1 个月 |
| `quarterly` | `quarterlyFee` | 3 个月 |
| `yearly` | `yearlyFee` | 1 年 |
| `per_time` | `perTimeFee` | 当天（单次） |

创建/更新活动组、活动组详情/列表、我加入的活动组均返回新字段。

### 107.10.3 商品详情「联系店主」带商品上下文

`POST /messages`、`POST /messages/neighbor-chat` 新增可选入参：

| 字段 | 说明 |
| --- | --- |
| `relatedProductId` | 商城商品ID（`prd_`） |
| `relatedListingId` | 业主小店上架ID（`rml_`） |

消息落库后会话记录可带商品上下文，供聊天页展示“咨询商品”。

### 107.10.4 商品聚合流带店铺字段

`GET /products` 每项新增 `shopId`、`shopName`、`shopStatus`（与 `merchantId/merchantName` 同值），点商品可直接跳店铺并定位商品详情。

> **v8.23 补充**：`shopId` 明确为 `merchants.id`；新增 `shopType`（merchant/resident）、`shopOpen`、`shopOwnerResidentId`，
> `shopStatus` 全量枚举为 active/inactive/stopped/kicked/rejected/frozen。商家列表也补 `shopType`。详见 §108.6。

> 按公里计费暂未开放：现有配送费仍按商家 `deliveryFee/freeDeliveryThreshold` 与配送规则计算，需产品补齐“配送方按公里报价”的数据来源后再改造。

## 107.11 数据库变更（V30）

执行脚本：`sql/v8.22_前后端联调_20261026.sql`（可重复执行）。

| 表 | 新增列 | 说明 |
| --- | --- | --- |
| `orders` | `source_type` / `distributor_id` / `resident_merchant_id` / `settlement_owner` | 代理订单归属；新增 `idx_orders_distributor_created` |
| `property_companies` | `point_transfer_enabled` / `point_transfer_min_years` | 积分转物业开关/年限，默认 0 / 5 |
| `distributor_products` | `supply_scope` | 默认 `company` |
| `activity_groups` | `quarterly_fee` / `per_time_fee` | 按季/按次价格 |
| `messages` | `related_product_id` / `related_listing_id` | 商品会话上下文 |

## 107.12 联调检查清单

- [ ] 商家页订单数不再被最近 5 条限制，积分余额显示购分账户余额。
- [ ] 广告只提交 `free` 或 `wechat`；免费额度用过一次后 `freeQuotaRemaining=0`。
- [ ] 广告微信支付拿到 `payParams` 才调起支付；未支付列表显示“待支付”。
- [ ] 代理货源订单在商家端显示「一级代理直供」，金额按佣金展示。
- [ ] 一级代理商「用户购买记录」能看到商品、地址、脱敏买家。
- [ ] 楼长工作台显示“我的收益（全部角色合并）”，提现金额与统一账户一致。
- [ ] 邻居会话楼长置顶；邻里列表楼长/单元负责人排在最前。
- [ ] 奖励金转钱包成功返回 `coinBalance/walletBalance/recordId`。
- [ ] 积分页只保留“转物业费”；商城结算页不再出现积分抵扣。
- [ ] 活动组可设置按季/按次价格并下单成功。

---

# 108. v8.23 前端联调反馈修复（2026-10-26）

> 本章逐条回应《v8.22-前端联调反馈-给后端》2.1–2.9。**字段名/枚举以本章为准**；未列出的字段保持不变。
> 数据库脚本：`sql/v8.23_前端反馈修复_20261026.sql`（Flyway V31）；后端测试：287 / 287 通过。

## 108.1 商家余额口径（反馈 2.1）

### 结论

`GET /merchants/my` 的 `profitBalance` 是**本商家店铺口径**，**不含**楼长分成、业主卖货收益。
`withdrawableAmount` 是**本店提现入口实际可用**：商家提现同时校验「本店铺可提现」和「账号统一可提现」，
因此它等于 `min(shopWithdrawableAmount, accountWithdrawableAmount)`。
新增 `shopWithdrawableAmount` / `accountWithdrawableAmount`，前端不再需要猜口径。

### 字段

| 字段 | 类型 | 口径 | 说明 |
| --- | --- | --- | --- |
| `withdrawableAmount` | number | 本店提现入口 | 实际可用 = `min(shopWithdrawableAmount, accountWithdrawableAmount)`；商家提现页用这个 |
| `shopWithdrawableAmount` | number | 本店铺 | 本店可提现（店铺收益 − 店铺提现占用），未受其他角色占用影响 |
| `profitBalance` | number | 本店铺 | 盈利余额（店铺口径，与 `shopWithdrawableAmount` 同值） |
| `profitScope` | string | — | 固定 `shop`，供前端判断文案 |
| `accountWithdrawableAmount` | number | 账号统一 | 楼长分成 + 业主卖货收益 + 名下商家剩余可提现，扣全部角色提现在途 |

> 正常情况 `accountWithdrawableAmount >= shopWithdrawableAmount`，此时 `withdrawableAmount = shopWithdrawableAmount`；
> 若同一住户已通过楼长/卖货入口提走了部分款项，账号统一余额可能低于店铺余额，`withdrawableAmount` 会取更小的账号值，避免超额提现。
> 商家提现接口仍只允许本店提现入口；楼长/卖货部分请走各自角色的提现入口。

### 响应示例

```json
{
  "code": 0,
  "data": {
    "id": "mch_xxx",
    "name": "幸福超市",
    "withdrawableAmount": 1280.50,
    "shopWithdrawableAmount": 1280.50,
    "profitBalance": 1280.50,
    "profitScope": "shop",
    "accountWithdrawableAmount": 1580.50,
    "totalOrders": 128,
    "pointBalance": 5000
  }
}
```

前端文案建议：

- `profitBalance` / `shopWithdrawableAmount` → 「本店可提现 / 本店盈利余额」；
- 提现页可用金额 → `withdrawableAmount`；
- 需要展示「账号可提现」时，读 `accountWithdrawableAmount`，不要用 `profitBalance`。

## 108.2 广告补付与重复提交（反馈 2.2）

### 新增：待支付广告补付

`POST /merchant/ads/{id}/repay`

- 权限：商家本人（广告所属商家）；
- 适用：`paymentMethod=wechat` 且 `payAmount>0`，状态为 `pending_payment` 或支付超时 `cancelled`；
- 返回：与发布广告相同的 `MerchantAdListItemResponse`，其中包含新的 `payParams`；
- 已支付/已投放：返回 `70003`（409）`广告已投放，无需重复支付`；
- 非微信现金广告：返回 `10002`（400）`该广告无需支付`；
- 微信支付未开启：返回 `10002`（400）`广告微信支付暂未开放`；
- 非本人：返回 `20004`（403）。

```http
POST /api/v1/merchant/ads/mad_xxx/repay
Authorization: Bearer <token>
```

```json
{
  "code": 0,
  "data": {
    "id": "mad_xxx",
    "status": "pending_payment",
    "payStatus": "unpaid",
    "payAmount": 70.00,
    "canRepay": true,
    "paymentRecordId": "pr_xxx",
    "payParams": {
      "timeStamp": "1730000000",
      "nonceStr": "abc123",
      "package": "prepay_id=wx123",
      "signType": "RSA",
      "paySign": "xxxx"
    }
  }
}
```

前端流程：列表项 `canRepay=true` → 调 `repay` → 拿 `payParams` 调 `wx.requestPayment` → 支付回调后后端置 `active`，再刷新列表。

### 列表新增字段

`GET /merchant/ads` 每项新增：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `paymentRecordId` | string | 最近一笔支付流水 ID（含待支付/已关闭流水），补付或查单用 |
| `canRepay` | boolean | 是否可继续支付 |
| `remainingDays` | number | 剩余投放天数；未支付或已结束为 0 |

> 补付接口内置行锁与流水复用：5 分钟内重复点击复用同一笔待支付流水，不会重复扣款；
> 超过 5 分钟的旧流水关闭后重新预下单。

### 重复提交

- `POST /merchant/ads` / `POST /merchant/ads/targeted` **每次成功调用都会创建一条新广告**；
- 前端可用请求头 `X-Request-Id`（8–128 位字母/数字/`._:-`）做幂等：同一用户 + 同一路径 + 同一 ID 会回放首次成功响应，不会重复建广告；
- 已存在的待支付广告请走 `repay`，不要重新发布。

## 108.3 积分转物业开关与资格（反馈 2.3）

### 字段下发位置

| 接口 | 新增字段 | 用途 |
| --- | --- | --- |
| `GET /property-companies/current` | `pointTransferEnabled`、`pointTransferMinYears` | 居民端读取开关/年限 |
| `GET /admin/property-companies/{id}` | 同上 | 管理端查看 |
| `PUT /admin/property-companies/{id}` | 请求体可传 `pointTransferEnabled`、`pointTransferMinYears` | 管理端开启/调整 |
| `GET /points/balance` | `pointTransferEnabled`、`pointTransferMinYears`、`pointTransferEligible`、`pointTransferBlockedReason` | 积分余额页直接判断 |
| `GET /residents/me/points` | 同上 | 我的积分页直接判断 |

### 字段说明

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `pointTransferEnabled` | boolean | 物业公司是否开放积分转物业（默认 `false`） |
| `pointTransferMinYears` | number | 最低入住年限（默认 `5`） |
| `pointTransferEligible` | boolean | 开关开启且入住年限达标时为 `true` |
| `pointTransferBlockedReason` | string | 不可转原因；可转时为 `null`（响应中省略） |

判定口径：`入住年限 = 当前时间 - residents.created_at`，需满 `pointTransferMinYears` 年。
入住年限以住户账户创建时间为基准，与转物业接口的既有校验一致。

### 示例

```json
{
  "code": 0,
  "data": {
    "pointBalance": 5000,
    "pointTransferEnabled": true,
    "pointTransferMinYears": 2,
    "pointTransferEligible": false,
    "pointTransferBlockedReason": "入住未满2年，暂不可将积分转物业费"
  }
}
```

> 管理端开启示例：`PUT /admin/property-companies/pc_xxx`，body `{"pointTransferEnabled": true, "pointTransferMinYears": 5}`。
> 开关关闭时，转物业接口返回 400：`积分转物业费暂未开放`。

## 108.4 广告状态全量枚举（反馈 2.4）

### `status`（列表展示状态）

| 值 | 含义 | 说明 |
| --- | --- | --- |
| `pending_payment` | 待支付 | 微信现金广告已下单、未收款 |
| `active` | 投放中 | 已支付/免费投放，且 `endDate >= 今天` |
| `ended` | 已结束 | **派生值**：数据库仍为 `active`，`endDate < 今天` 时返回 `ended` |
| `cancelled` | 已取消 | 支付超时/失败，由后端定时任务置为取消 |

当前系统**没有** `expired` / `rejected` / `paused` 状态。
「投放中 / 已结束」以 `status` 为准，不要用 `payStatus` 判断。

### `payStatus`（支付状态）

| 值 | 含义 | 说明 |
| --- | --- | --- |
| `unpaid` | 未支付 | 无已支付流水 |
| `paid` | 已支付 | 含免费额度投放 |
| `refunded` | 已退款 | 预留值；当前广告无退款流程，暂不会产生 |

> `canRepay=true` 仅出现在 `status=pending_payment` 或 `cancelled`、支付方式为 `wechat` 且尚未支付的广告。

## 108.5 会话列表楼长/单元负责人标识（反馈 2.5）

`GET /messages/conversations` 每项新增：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `isUnitLeader` | boolean | 对方是否单元负责人/楼长，与 `isBuildingLeader` 同口径 |
| `leaderRole` | string | `building_leader`=单元楼长；空串=普通住户 |

```json
{
  "peerId": "res_xxx",
  "peerName": "李*",
  "isBuildingLeader": true,
  "isUnitLeader": true,
  "leaderRole": "building_leader"
}
```

> 当前系统「单元楼长」与「单元负责人」是同一身份（`building_leaders` 表），因此两个布尔值一致；
> 没有单独的 `unit_leader` 表，`leaderRole` 不会返回 `unit_leader`。
> `GET /messages/neighbors` 原本已返回两个字段，本次不变。

## 108.6 商品/店铺字段与跳转（反馈 2.6）

### `GET /products` / `GET /products/{id}` 新增

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `shopId` | string | **`merchants.id`**（不是业主小店上架 ID） |
| `shopName` | string | 店铺名称（与 `merchantName` 同值） |
| `shopType` | string | `merchant` 普通商家；`resident` 业主小店（`merchant_source=resident_shop`） |
| `shopStatus` | string | 原始状态：`active` / `inactive` / `stopped` / `kicked` / `rejected` / `frozen`（仅 `active` 时 `shopOpen=true`） |
| `shopOpen` | boolean | `shopStatus=active` 时为 `true` |
| `shopOwnerResidentId` | string | `shopType=resident` 时返回店主住户 ID |

### `shopStatus` 枚举

| 值 | 含义 |
| --- | --- |
| `active` | 营业中 |
| `inactive` | 停业/未启用 |
| `stopped` | 账号停用连带停业 |
| `kicked` | 已被清退 |
| `rejected` | 审核未通过 |
| `frozen` | 已被物业/协调员冻结（可解冻恢复 `active`） |

> 前端展示建议：`shopOpen=false` 时统一显示「暂停营业」；具体原因可按 `shopStatus` 细分。

### 跳转建议

- `shopType=merchant` → `/pages/service/merchantProducts?id=<shopId>`
- `shopType=resident` → `/pages/shop/detail?residentId=<shopOwnerResidentId>`；
  商品流没有 `listingId`（上架 ID 只在业主小店上架/下单链路出现），不要用商品 ID 冒充 `listingId`。

`GET /merchants`、`GET /merchants/official-recommended` 的列表项也新增 `merchantSource`、`shopType`，
推荐位里的业主小店可按 `shopType=resident` 分流；其 `id` 仍是 `merchants.id`，可直接用于商家详情。

### 示例

```json
{
  "id": "prd_xxx",
  "merchantId": "mch_xxx",
  "shopId": "mch_xxx",
  "shopName": "张阿姨的小店",
  "shopType": "resident",
  "shopStatus": "active",
  "shopOpen": true,
  "shopOwnerResidentId": "res_xxx"
}
```

## 108.7 货源供给范围权限（反馈 2.7）

`distributor_products.supply_scope`：

| 值 | 含义 |
| --- | --- |
| `company`（默认） | 仅本物业公司的业主小店可选品/拿货 |
| `platform` | 全平台所有物业公司的业主小店均可代销（超级代理商品） |

### 谁能设置

- **仅平台管理员 / 物业管理员**：
  - `POST /admin/distributor-products` 请求体可传 `supplyScope`；
  - `PUT /admin/distributor-products/{id}` 请求体可传 `supplyScope`；
  - 物业管理员只能操作本物业公司的货源（跨物业返回 403）。
- **一级代理不可设置**：
  - `GET /distributors/my/products` 与 `GET /distributors/products` 返回 `supplyScope` 只读；
  - `POST /distributors/my/products`、`PUT /distributors/my/products/{id}` 不要传 `supplyScope`；
    传任意非空值（包括 `company`）都会返回 `20004`（403）：
    `供给范围由平台/物业管理员设置，一级代理不可自行修改`；
  - 代理新建货源时后端固定写 `company`，不会因为前端漏传而写成 `platform`。

### 示例

```json
{
  "id": "dpr_xxx",
  "name": "五常大米 5kg",
  "supplyScope": "company"
}
```

管理端授予全平台代销：

```http
PUT /api/v1/admin/distributor-products/dpr_xxx
Authorization: Bearer <property_admin_token>
Content-Type: application/json

{ "supplyScope": "platform" }
```

## 108.8 一级代理商购买记录（反馈 2.8）

`GET /distributors/my/orders`

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `page` | number | 否 | 默认 1 |
| `pageSize` | number | 否 | 默认 20，最大 100 |
| `orderStatus` | string | 否 | 见下方枚举 |
| `keyword` | string | 否 | 订单号或商品名模糊搜索 |
| `startDate` | string | 否 | 下单开始日期 `yyyy-MM-dd`（含当天） |
| `endDate` | string | 否 | 下单结束日期 `yyyy-MM-dd`（含当天） |

### `orderStatus` 枚举

`pending` / `paid` / `delivering` / `completed` / `cancelled` / `refunding` / `refunded` / `refund_rejected`

> 没有 `shipped`；配送中请传 `delivering`。传其他值返回 400 `orderStatus值无效`。

### 分页结构

统一为 `data.list` + `data.pagination`（没有平铺的 `data.total`）：

```json
{
  "code": 0,
  "data": {
    "list": [
      {
        "orderId": "ord_xxx",
        "orderNo": "202610260001",
        "orderStatus": "completed",
        "totalAmount": 39.90,
        "quantity": 2,
        "productName": "五常大米 5kg",
        "productCover": "https://...",
        "shopName": "张阿姨的小店",
        "buyerName": "张*",
        "buyerPhone": "138****8000",
        "address": "幸福小区3栋2单元501",
        "createdAt": "2026-10-26 10:00:00",
        "settlementOwner": "distributor"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 1,
      "totalPages": 1
    }
  }
}
```

### 脱敏口径

- `buyerName`：张三 → 张*；李华明 → 李*明；单字姓名不脱敏；
- `buyerPhone`：138****8000；
- **后端已完成脱敏，前端原样展示即可，不要二次脱敏**（否则会出现 `张**`）。

## 108.9 家庭转账历史流水（反馈 2.9）

- 历史家庭内转赠流水保留原值：`pointType=transfer`、`source=transfer`，**不做数据改写**；
- `transfer_to_property`（积分转物业）是另一用途，保持独立；用 `source=transfer` 筛选不会混入转物业流水；
- `GET /points/records?source=transfer` 会同时命中历史 `transfer` 与早期 `family_transfer` 两种取值；
- 前端传 `source=family_transfer` 也会被后端归一为 `transfer` 查询，同样命中；
- 新增流水不再产生家庭转赠（入口已下线），因此「转账」筛选只会命中历史数据。

## 108.10 数据库变更（V31，合并版）

执行脚本：`sql/v8.23_前端反馈修复_20261026.sql`（同时作为 Flyway `V31__v8.23_frontend_feedback.sql`）。

- **合并版**：第 0 节包含 v8.22（V30）前置列/索引；若服务器尚未执行 v8.22，可直接执行本脚本一次补齐；
- **幂等**：列/索引先查 `information_schema`，可重复执行；
- v8.23 本身不新增业务列，只增加以下索引：
  - `orders.idx_orders_distributor_status_created (distributor_id, order_status, created_at)`
  - `merchant_ads.idx_merchant_ads_merchant_created (merchant_id, created_at)`
  - `distributor_products.idx_distributor_products_supply (status, supply_scope, distributor_merchant_id)`
- 历史 NULL/空值兜底：`supply_scope` → `company`；`point_transfer_enabled` → `0`；`point_transfer_min_years` → `5`。

> 部署顺序：先执行 SQL，再重启后端。若先重启而后端实体已包含新列，查询会因缺列失败。

## 108.11 联调检查清单

- [ ] 商家页「本店盈利余额」= `profitBalance`；提现页可用额 = `withdrawableAmount`；「账号可提现」= `accountWithdrawableAmount`，三者不要混用。
- [ ] 广告列表能展示「待支付 / 投放中 / 已结束 / 已取消」；待支付项有「继续支付」按钮并调 `repay` 拿到 `payParams`。
- [ ] 同一广告连续点「继续支付」不会生成多笔支付流水；重复发布用 `X-Request-Id` 防重。
- [ ] 居民端能从 `GET /property-companies/current` 或积分余额接口读到开关/年限，未达标时按钮置灰并展示原因。
- [ ] 会话列表能拿到 `isUnitLeader` / `leaderRole`，楼长会话置顶。
- [ ] 商品流按 `shopType` 分流：普通商家进商家商品页，业主小店用 `shopOwnerResidentId` 进小店页。
- [ ] 推荐位里的业主小店按 `shopType=resident` 分流，不再直接进商家商品页。
- [ ] 代理货源编辑弹窗不提交 `supplyScope`；管理端可设置 `platform`。
- [ ] 代理购买记录分页读 `data.pagination`，买家姓名/手机直接展示后端脱敏值。
- [ ] 积分记录「转账」筛选能命中历史 `transfer` / `family_transfer` 流水。

## 附录 A：管理端遗漏接口补录（v8.24）

> 本附录补充《API 接口文档》中此前**未文档化**的管理端接口，源于后端 60 个 Admin 控制器全量盘点（路径归一化 diff）。共补录 **20 个路径模式 / 34 个接口**。
>
> 约定：接口前缀 `/api/v1`；响应统一为 `ApiResponse<T>`；分页为 `ApiResponse<PageResponse<T>>`，`PageResponse` 结构为 `{ list, pagination:{ page, pageSize, total, totalPages } }`。「字段同 X 节」表示该接口响应复用既有文档已定义的响应对象，未重复展开。

### A.1 商家微信进件管理（`/admin/merchants/{id}/applyment*`）

> 商家微信支付进件（入驻审核通过后的微信侧开户/签约流程）由运营后台管理。角色：`property_admin`、`platform_admin`。

#### A.1.1 GET `/admin/merchants/{id}/applyment` — 查看商家微信进件详情

**请求**：`GET /admin/merchants/{id}/applyment`

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | String | 是 | 商家ID（m_） |

**响应**：`ApiResponse<ApplymentDetailResponse>`
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| merchantId | String | 商家ID |
| applymentId | String | 微信支付申请单号 |
| applymentState | String | 进件状态机：init/submitted/auditing/legal_validating/signing/finished/rejected |
| wxState | String | 微信侧状态原值 |
| organizationType | String | 主体类型：ENTERPRISE/INDIVIDUAL |
| merchantShortname / servicePhone | String | 商户简称 / 客服电话 |
| qualificationType | String | 所属行业 |
| contactMobile | String | 超管手机号（脱敏） |
| contactType / contactName | String | 超管类型 / 姓名 |
| bankAccountType / accountBank / bankAliasCode / bankBranchId / bankName | String | 结算账户非敏感字段 |
| submitted | Boolean | 是否已提交微信（存在 applyment_id） |
| signUrl | String | 超级管理员签约链接（转二维码给法人扫码） |
| rejectReason | String | 微信驳回原因 |
| subMchid | String | 进件成功后微信分配的二级商户号 |
| submitMode | String | 提交方式：manual(运营后台)/auto(审核通过自动) |
| lastError | String | 提交失败错误信息（仅运营后台可见） |

> 场景/门店/执照/证件有效期等其余非敏感回显字段见 §97.2.1；敏感证件号/卡号/邮箱不回显。
> 审核中调用该接口会按 15 秒节流刷新微信状态。

#### A.1.2 POST `/admin/merchants/{id}/applyment/submit` — 手动提交商家进件到微信

**请求**：`POST /admin/merchants/{id}/applyment/submit`

**路径参数**：同 A.1.1（id）。**无请求体**。

**响应**：`ApiResponse<ApplymentDetailResponse>`（字段同 A.1.1）

#### A.1.3 POST `/admin/merchants/{id}/applyment/refresh` — 手动刷新商家进件状态

**请求**：`POST /admin/merchants/{id}/applyment/refresh`

**路径参数**：同 A.1.1（id）。**无请求体**。

**响应**：`ApiResponse<ApplymentDetailResponse>`（字段同 A.1.1）

---

### A.2 社区论坛 — 帖子可见范围（`/admin/community/posts/{id}/visibility`）

> 角色：`property_admin`。

#### A.2.1 POST `/admin/community/posts/{id}/visibility` — 设置帖子可见范围

**请求**：`POST /admin/community/posts/{id}/visibility`

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | String | 是 | 帖子ID |

**请求体**（`SetPostVisibilityRequest`）：
| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| visibilityType | String | 是 | 可见范围：`community`=本小区可见；`family`=仅指定住户可见 |
| visibleResidentId | String | 否（visibilityType=family 时必填） | 可见的指定住户ID（res_） |

**响应**：`ApiResponse<Void>`

---

### A.3 活动组注册审核（`/admin/activity-groups/reviews`、`/admin/activity-groups/{id}/review`）

> 活动组注册申请的审核列表与审核动作。角色：`property_admin`、`platform_admin`。

#### A.3.1 GET `/admin/activity-groups/reviews` — 活动组审核列表

**请求**：`GET /admin/activity-groups/reviews`

**查询参数**
| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| page | int | 否 | 页码，默认 1 |
| pageSize | int | 否 | 每页条数，默认 20 |
| status | String | 否 | 审核状态：pending/approved/rejected |

**响应**：`ApiResponse<PageResponse<ActivityGroupListItemResponse>>`（列表项字段见 §18 活动组模块既有定义：id, name, description, coverUrl, leaderId, leaderName, communityId, communityName, memberCount, monthlyFee, yearlyFee, quarterlyFee, perTimeFee, status, createdAt 等）

#### A.3.2 POST `/admin/activity-groups/{id}/review` — 审核活动组注册

**请求**：`POST /admin/activity-groups/{id}/review`

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | String | 是 | 活动组ID（act_/ag_） |

**请求体**（`ReviewActivityGroupRequest`）：
| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| action | String | 是 | 审核动作：approve=通过；reject=驳回 |
| remark | String | 否（驳回时填写） | 审核备注 |

**响应**：`ApiResponse<Void>`

---

### A.4 咨询师管理（`/admin/consultants`、`/admin/consultant*`、`/admin/consultation-settings`）

> 咨询师（问诊）的列表、创建、更新、审核与平台配置。角色：`property_admin`、`platform_admin`（platform_admin 可跨物业公司；PC 端经 Query 自动附加 propertyCompanyId）。

#### A.4.1 GET `/admin/consultants` — 咨询师列表

**请求**：`GET /admin/consultants`

**查询参数**
| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| page | int | 否 | 页码，默认 1 |
| pageSize | int | 否 | 每页条数，默认 20 |
| category | String | 否 | 咨询分类 |
| keyword | String | 否 | 关键词（姓名/机构等） |
| status | String | 否 | 状态 |
| propertyCompanyId | String | 否 | 物业公司ID（platform_admin 可传以跨公司查询） |

**响应**：`ApiResponse<PageResponse<ConsultantListItemResponse>>`
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | String | 咨询师ID |
| name | String | 姓名 |
| title | String | 职称 |
| organization | String | 机构 |
| specialty | String | 擅长领域 |
| avatarUrl | String | 头像URL |
| category | String | 咨询分类 |
| chatPrice | BigDecimal | 图文咨询价格 |
| appointmentPrice | BigDecimal | 预约咨询价格 |
| appointmentEnabled | Boolean | 是否开启预约 |
| auditStatus | String | 审核状态 |
| status | String | 状态 |
| statusCode | String | 状态码 |
| createdAt | String | 创建时间 |

#### A.4.2 POST `/admin/consultants` — 创建咨询师

**请求**：`POST /admin/consultants`

**查询参数**
| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| propertyCompanyId | String | 否（platform_admin 必填） | 物业公司ID |

**请求体**（`CreateConsultantRequest`）：
| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| propertyCompanyId | String | 否 | 物业公司ID（未传时从登录态解析） |
| category | String | 是 | 咨询分类 |
| name | String | 是 | 姓名 |
| title | String | 否 | 职称 |
| organization | String | 否 | 机构 |
| specialty | String | 否 | 擅长领域 |
| avatarUrl | String | 否 | 头像URL |
| chatPrice | BigDecimal | 否 | 图文咨询价格 |
| appointmentPrice | BigDecimal | 否 | 预约咨询价格 |
| appointmentEnabled | Boolean | 否 | 是否开启预约 |
| phone | String | 否 | 电话 |
| introduction | String | 否 | 简介 |

**响应**：`ApiResponse<ConsultantDetailResponse>`（同 A.4.1 列表项字段 + `introduction` 详情字段）

#### A.4.3 PUT `/admin/consultants/{id}` — 更新咨询师

**请求**：`PUT /admin/consultants/{id}`

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | String | 是 | 咨询师ID |

**请求体**（`UpdateConsultantRequest`，全部可选，仅更新传入字段）：
| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| name | String | 否 | 姓名 |
| title | String | 否 | 职称 |
| organization | String | 否 | 机构 |
| specialty | String | 否 | 擅长领域 |
| avatarUrl | String | 否 | 头像URL |
| chatPrice | BigDecimal | 否 | 图文咨询价格 |
| appointmentPrice | BigDecimal | 否 | 预约咨询价格 |
| appointmentEnabled | Boolean | 否 | 是否开启预约 |
| phone | String | 否 | 电话 |
| introduction | String | 否 | 简介 |
| status | String | 否 | 状态 |

**响应**：`ApiResponse<ConsultantDetailResponse>`

#### A.4.4 PUT `/admin/consultants/{id}/audit` — 审核咨询师

**请求**：`PUT /admin/consultants/{id}/audit`

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | String | 是 | 咨询师ID |

**请求体**（`AuditConsultantRequest`）：
| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| auditResult | String | 否 | 审核结果：approved/rejected（优先取此字段） |
| auditStatus | String | 否 | 兼容字段，同 auditResult |
| reason | String | 否 | 拒绝原因 |

**响应**：`ApiResponse<ConsultantDetailResponse>`

#### A.4.5 GET `/admin/consultation-settings` — 咨询平台配置

**请求**：`GET /admin/consultation-settings`

**响应**：`ApiResponse<ConsultationSettingsResponse>`
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| commissionRate | BigDecimal | 平台抽成比例（0–1） |

#### A.4.6 PUT `/admin/consultation-settings` — 更新咨询平台配置

**请求**：`PUT /admin/consultation-settings`

**请求体**（`UpdateConsultationSettingsRequest`）：
| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| commissionRate | BigDecimal | 否 | 平台抽成比例（0–1），仅更新非 null 字段 |

**响应**：`ApiResponse<ConsultationSettingsResponse>`

---

### A.5 配送规则管理端（`/admin/delivery-rules`）

> 公开端 `/delivery-rules`（§10）已文档化；此处为**管理端 CRUD**（带物业公司隔离）。

#### A.5.1 GET `/admin/delivery-rules` — 配送规则列表

**请求**：`GET /admin/delivery-rules`

**查询参数**
| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| page | int | 否 | 页码，默认 1 |
| pageSize | int | 否 | 每页条数，默认 20 |
| propertyCompanyId | String | 否 | 物业公司ID |

**响应**：`ApiResponse<PageResponse<DeliveryRuleResponse>>`
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | String | 规则ID |
| name | String | 规则名称 |
| baseFee | BigDecimal | 基础配送费 |
| perKgFee | BigDecimal | 每公斤配送费 |
| freeThreshold | BigDecimal | 免配送费门槛金额 |
| maxWeight | BigDecimal | 最大重量限制(kg) |
| maxDistance | BigDecimal | 最大配送距离(km) |
| courierShareRate | BigDecimal | 配送员分成比例 |
| courierPerOrder | BigDecimal | 配送员每单固定收入 |
| timeoutMinutes | Integer | 超时时间(分钟) |
| propertyCompanyId | String | 物业公司ID |
| status | String | 规则状态：active/inactive |
| createdAt | String | 创建时间 |
| updatedAt | String | 更新时间 |

#### A.5.2 GET `/admin/delivery-rules/{id}` — 配送规则详情

**请求**：`GET /admin/delivery-rules/{id}`

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | String | 是 | 配送规则ID |

**响应**：`ApiResponse<DeliveryRuleResponse>`（字段同 A.5.1）

#### A.5.3 POST `/admin/delivery-rules` — 创建配送规则

**请求**：`POST /admin/delivery-rules`

**请求体**（`CreateDeliveryRuleRequest`）：
| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| name | String | 是 | 规则名称（≤100） |
| baseFee | BigDecimal | 是 | 基础费用（≥0） |
| perKgFee | BigDecimal | 否 | 每公斤费用 |
| freeThreshold | BigDecimal | 否 | 免运费门槛(金额) |
| maxWeight | BigDecimal | 否 | 最大重量(kg) |
| maxDistance | BigDecimal | 否 | 最大配送距离(km) |
| courierShareRate | BigDecimal | 否 | 快递员分成比例 |
| courierPerOrder | BigDecimal | 否 | 快递员每单固定收入（≥0） |
| timeoutMinutes | Integer | 否 | 超时时间(分钟，≥1) |
| propertyCompanyId | String | 否 | 物业公司ID |

**响应**：`ApiResponse<DeliveryRuleResponse>`（字段同 A.5.1）

#### A.5.4 PUT `/admin/delivery-rules/{id}` — 更新配送规则

**请求**：`PUT /admin/delivery-rules/{id}`

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | String | 是 | 配送规则ID |

**请求体**（`UpdateDeliveryRuleRequest`，全部可选，仅更新传入字段）：
| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| name | String | 否 | 规则名称（≤100） |
| baseFee | BigDecimal | 否 | 基础配送费（≥0） |
| perKgFee | BigDecimal | 否 | 每公斤配送费 |
| freeThreshold | BigDecimal | 否 | 免配送费门槛金额 |
| maxWeight | BigDecimal | 否 | 最大重量限制(kg) |
| maxDistance | BigDecimal | 否 | 最大配送距离(km) |
| courierShareRate | BigDecimal | 否 | 配送员分成比例 |
| courierPerOrder | BigDecimal | 否 | 配送员每单固定收入（≥0） |
| timeoutMinutes | Integer | 否 | 超时时间(分钟，≥1) |
| status | String | 否 | 规则状态 |

**响应**：`ApiResponse<DeliveryRuleResponse>`（字段同 A.5.1）

---

### A.6 导航栏配置（`/admin/navigation-items`）

> 手机端导航栏配置维护（后台）。角色：`property_admin`、`platform_admin`。`propertyCompanyId` 为空表示平台级默认导航。

#### A.6.1 GET `/admin/navigation-items` — 导航项列表

**请求**：`GET /admin/navigation-items`

**查询参数**
| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| propertyCompanyId | String | 否 | 物业公司ID（platform_admin 可传） |

**响应**：`ApiResponse<List<NavigationItem>>`
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | String | 导航项ID |
| position | String | 位置：bottom/top |
| module | String | 所属模块：home(首页)/service(服务)/activity(活动组)/resident_merchant(业主商家) |
| title | String | 标题 |
| icon | String | 图标（URL 或图标名） |
| linkType | String | 跳转类型：page/tab/h5/none |
| linkUrl | String | 跳转目标（页面路径或链接） |
| sortOrder | Integer | 排序值，越小越靠前 |
| enabled | Boolean | 是否启用 |
| visibleRoles | String | 可见角色（逗号分隔；空=全部可见） |
| propertyCompanyId | String | 物业公司ID（空=平台级默认） |

#### A.6.2 POST `/admin/navigation-items` — 新增导航项

**请求**：`POST /admin/navigation-items`

**请求体**（直接传 `NavigationItem` 实体字段）：
| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| position | String | 是 | 位置：bottom/top |
| module | String | 是 | 所属模块：home/service/activity/resident_merchant |
| title | String | 是 | 标题 |
| icon | String | 否 | 图标 |
| linkType | String | 是 | 跳转类型：page/tab/h5/none |
| linkUrl | String | 否 | 跳转目标 |
| sortOrder | Integer | 否 | 排序值 |
| enabled | Boolean | 否 | 是否启用 |
| visibleRoles | String | 否 | 可见角色（逗号分隔；空=全部） |
| propertyCompanyId | String | 否 | 物业公司ID（空=平台级默认） |

**响应**：`ApiResponse<NavigationItem>`（字段同 A.6.1）

#### A.6.3 PUT `/admin/navigation-items/{id}` — 修改导航项

**请求**：`PUT /admin/navigation-items/{id}`

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | String | 是 | 导航项ID |

**请求体**（同 A.6.2，仅更新传入字段）

**响应**：`ApiResponse<NavigationItem>`（字段同 A.6.1）

#### A.6.4 DELETE `/admin/navigation-items/{id}` — 删除导航项

**请求**：`DELETE /admin/navigation-items/{id}`

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | String | 是 | 导航项ID |

**响应**：`ApiResponse<Void>`

---

### A.7 订单退款 — 强制改派履约方式（`/admin/orders/refunds/{orderId}/fulfillment-override`）

> 管理员强制修改订单履约方式，写入履约选择审计日志留痕。角色：`property_admin`、`platform_admin`。

#### A.7.1 POST `/admin/orders/refunds/{orderId}/fulfillment-override` — 强制改派履约方式

**请求**：`POST /admin/orders/refunds/{orderId}/fulfillment-override`

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| orderId | String | 是 | 订单ID（ord_xxx） |

**请求体**（`OverrideFulfillmentRequest`）：
| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| mode | String | 是 | 履约方式：`merchant_self` / `courier_hall` |
| remark | String | 否 | 改派说明（≤200 字，留痕） |

**响应**：`ApiResponse<Void>`

---

### A.8 平台配置（`/admin/platform-config`）

> 归属开关、提现粒度、分成维度等平台级配置。查看：`platform_admin`、`property_admin`；更新：仅 `platform_admin`。

#### A.8.1 GET `/admin/platform-config` — 查看平台配置

**请求**：`GET /admin/platform-config`

**响应**：`ApiResponse<PlatformConfigResponse>`
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| rewardAttributionMode | String | 归属模式：property/platform |
| withdrawalGranularity | String | 提现粒度：per_role/merchant/resident |
| shareDimension | String | 分成维度：default/simple |

#### A.8.2 PUT `/admin/platform-config` — 更新平台配置

**请求**：`PUT /admin/platform-config`

**请求体**（`UpdatePlatformConfigRequest`，仅更新非 null 字段）：
| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| rewardAttributionMode | String | 否 | 归属模式：property/platform |
| withdrawalGranularity | String | 否 | 提现粒度：per_role/merchant/resident |
| shareDimension | String | 否 | 分成维度：default/simple |

**响应**：`ApiResponse<PlatformConfigResponse>`（字段同 A.8.1）

---

### A.9 物业收款银行卡（`/admin/property-bank-cards`）

> 物业公司收款银行卡维护（账号脱敏展示）。角色：`property_admin`、`platform_admin`。

#### A.9.1 GET `/admin/property-bank-cards` — 银行卡列表

**请求**：`GET /admin/property-bank-cards`

**响应**：`ApiResponse<List<PropertyBankCardItemResponse>>`
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | String | 银行卡ID |
| propertyCompanyId | String | 物业公司ID |
| bankName | String | 开户行 |
| branchName | String | 支行 |
| accountName | String | 户名 |
| accountNoMasked | String | 账号（脱敏） |
| purpose | String | 用途：points(积分钱)/reward(奖励金) |
| isDefault | Boolean | 是否默认 |
| status | String | 状态：active/disabled |
| remark | String | 备注 |
| createdAt | String | 创建时间 |

#### A.9.2 POST `/admin/property-bank-cards` — 新增银行卡

**请求**：`POST /admin/property-bank-cards`

**请求体**（直接传 `PropertyBankCard` 实体字段）：
| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| propertyCompanyId | String | 是 | 物业公司ID |
| bankName | String | 是 | 开户行名称 |
| branchName | String | 否 | 支行名称 |
| accountName | String | 是 | 户名 |
| accountNo | String | 是 | 对公账号（敏感，展示时脱敏） |
| purpose | String | 是 | 用途：points/reward |
| isDefault | Boolean | 否 | 是否默认收款卡 |
| status | String | 否 | 状态：active/disabled |
| remark | String | 否 | 备注 |

**响应**：`ApiResponse<PropertyBankCardItemResponse>`（字段同 A.9.1）

#### A.9.3 PUT `/admin/property-bank-cards/{id}` — 修改银行卡

**请求**：`PUT /admin/property-bank-cards/{id}`

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | String | 是 | 银行卡ID |

**请求体**（同 A.9.2，仅更新非 null 字段）

**响应**：`ApiResponse<PropertyBankCardItemResponse>`（字段同 A.9.1）

#### A.9.4 DELETE `/admin/property-bank-cards/{id}` — 删除银行卡

**请求**：`DELETE /admin/property-bank-cards/{id}`

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | String | 是 | 银行卡ID |

**响应**：`ApiResponse<Void>`

---

### A.10 区域配额（`/admin/region-quotas`）

> 区域/板块额度配额维护（某板块周期内可发放的积分/资金额度）。角色：`property_admin`、`platform_admin`。修改时配额总量不可低于已使用量。

#### A.10.1 GET `/admin/region-quotas` — 配额列表

**请求**：`GET /admin/region-quotas`

**查询参数**
| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| sector | String | 否 | 板块/区域标识（按板块过滤） |

**响应**：`ApiResponse<List<RegionQuota>>`
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | String | 配额ID |
| propertyCompanyId | String | 物业公司ID |
| sector | String | 板块/区域标识 |
| quotaType | String | 配额类型：point/amount |
| quotaAmount | BigDecimal | 配额总量 |
| usedAmount | BigDecimal | 已使用量（返回字段，修改接口不可低于此值） |
| periodStart | String | 周期开始日期（yyyy-MM-dd） |
| periodEnd | String | 周期结束日期（yyyy-MM-dd） |
| status | String | 状态：active/disabled |
| remark | String | 备注 |

#### A.10.2 POST `/admin/region-quotas` — 新增配额

**请求**：`POST /admin/region-quotas`

**请求体**（直接传 `RegionQuota` 实体字段）：
| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| propertyCompanyId | String | 是 | 物业公司ID |
| sector | String | 是 | 板块/区域标识 |
| quotaType | String | 是 | 配额类型：point/amount |
| quotaAmount | BigDecimal | 是 | 配额总量 |
| usedAmount | BigDecimal | 是 | 已使用量（新建通常传 0） |
| periodStart | String | 是 | 周期开始日期 |
| periodEnd | String | 是 | 周期结束日期 |
| status | String | 否 | 状态：active/disabled |
| remark | String | 否 | 备注 |

**响应**：`ApiResponse<RegionQuota>`（字段同 A.10.1）

#### A.10.3 PUT `/admin/region-quotas/{id}` — 修改配额

**请求**：`PUT /admin/region-quotas/{id}`

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | String | 是 | 配额ID |

**请求体**（同 A.10.2，仅更新非 null 字段；quotaAmount 不可低于 usedAmount）

**响应**：`ApiResponse<RegionQuota>`（字段同 A.10.1）

#### A.10.4 DELETE `/admin/region-quotas/{id}` — 删除配额

**请求**：`DELETE /admin/region-quotas/{id}`

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | String | 是 | 配额ID |

**响应**：`ApiResponse<Void>`

---

### A.11 业主商户 — 违规扣除保证金（`/admin/resident-merchant-deposits/{id}/deduct`）

> 保证金列表 `GET /admin/resident-merchant-deposits` 已文档化；此处补「违规扣除」动作。角色：`property_admin`、`platform_admin`。

#### A.11.1 POST `/admin/resident-merchant-deposits/{id}/deduct` — 违规扣除保证金

**请求**：`POST /admin/resident-merchant-deposits/{id}/deduct`

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | String | 是 | 保证金记录ID |

**请求体**（`DeductResidentMerchantDepositRequest`）：
| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| amount | BigDecimal | 是 | 扣除金额 |
| reason | String | 否 | 扣除原因 |

**响应**：`ApiResponse<ResidentMerchantDepositListItemResponse>`（字段同既有保证金列表项定义）

---

### A.12 住户注销审核（`/admin/residents/deletion*`）

> 平台管理员审核处于注销冷静期的住户。角色：`platform_admin`。

#### A.12.1 GET `/admin/residents/deletion-requests` — 注销申请列表

**请求**：`GET /admin/residents/deletion-requests`

**查询参数**
| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| page | int | 否 | 页码，默认 1 |
| pageSize | int | 否 | 每页条数，默认 20 |

**响应**：`ApiResponse<PageResponse<ResidentListItemResponse>>`（列表项字段见居民模块既有定义：id, name, phone(脱敏), avatarUrl, gender, age, maritalStatus, hasChildren, role, userType, propertyCompanyId, communityId, communityName, building, floor, unit, room, familyId, pointBalance 等）

#### A.12.2 POST `/admin/residents/{id}/deletion/review` — 审核注销账号

**请求**：`POST /admin/residents/{id}/deletion/review`

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | String | 是 | 住户ID（res_） |

**请求体**（`ReviewAccountDeletionRequest`）：
| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| action | String | 是 | 审核动作：approve=通过并注销；reject=驳回恢复 |
| rejectReason | String | 否（action=reject 时填写） | 驳回原因 |

**响应**：`ApiResponse<Void>`

---

# 附录 B：文档保留但后端未实现的接口（2026-10-27 核对）

> 下表接口在本文档中仍有章节/示例，但当前后端代码中**不存在对应控制器**。联调前请勿调用；如需上线，请先由后端补实现。CBK 遗留章节（§90、§92.2~§92.5）已单独标注废弃，此处不再重复。

| 模块 | 方法 / 路径 | 现状与替代方案 |
| --- | --- | --- |
| 商家入驻 | `GET /merchants/my-application` | 未实现；住户端暂无商家入驻申请进度查询接口 |
| 订单 | `POST /orders/{id}/merchant-cancel` | 未实现；商家侧暂无取消订单入口 |
| 订单 | `POST /orders/{id}/verify` | 路径不存在；实际为 `POST /merchants/my/orders/{orderId}/verify`（§94.16） |
| 商家积分 | `GET /merchant/points/quote` | 未实现；可由前端按 `point_exchange_rate` 本地计算 |
| 商家物业币 | `POST /admin/merchants/{id}/coin/earn` | 未实现；管理端暂无给商家发放物业币入口 |
| 公告 | `GET /admin/announcements/{id}/read-stats` | 未实现；请使用公告详情/定向推送统计字段 |
| 公告 | `POST /announcements/product` | 未实现；请使用 `POST /announcements/activity` 或特惠推送 |
| 消息 | `GET /messages/contact-quota` | 未实现；暂无每日新聊配额查询接口 |
| 配送规则 | `GET /delivery-rules/scope-options` | 未实现；距离类型/配送范围为固定枚举，前端本地维护 |
| 活动组 | `POST /activity-groups/{id}/unsubscribe` | 未实现；退出活动组请用 `POST /activity-groups/{id}/leave` |

## B.1 路径已变更（文档旧路径 → 实际路径）

| 模块 | 文档旧路径 | 实际路径 |
| --- | --- | --- |
| 统筹新增个体负责人 | `POST /sector-leaders/{id}/individual-leaders` | `POST /coordinators/{id}/individual-leaders` |
| 统筹上传商家 | `POST /sector-leaders/{id}/merchants` | `POST /coordinators/{id}/merchants` |
| 商家核实订单 | `POST /orders/{id}/verify` | `POST /merchants/my/orders/{orderId}/verify` |

---

# 附录 C：v8.27 微信支付双通道 + 服务商分账（2026-10-27）

> 本附录是 v8.27 的增量契约。历史章节中「微信支付只有一个商户号」的描述同时作废：
> 平台现在接入**两个独立的微信商户主体**，凭据不可混用。

## C.1 双通道模型

| 通道 | 配置前缀 | 商户主体 | 用途 | 结算去向 |
| --- | --- | --- | --- | --- |
| `partner` 服务商 | `wechat.pay` | 服务商 1752012787（sp_mchid） | 商城订单（商家商品） | 用户付款直进**商家特约商户号**，平台按订单分成走官方分账 |
| `direct` 直连商户 | `wechat.direct` | 平台直连商户 1665249236 | 物业费、物业币充值、食堂充值、商家广告、服务预约/订阅、活动组订阅、业主商户保证金 | 资金进**平台直连商户号**，不参与微信分账 |
| `ecommerce` 电商收付通 | `wechat-ecommerce` | 二期备选（需 ICP/EDI） | 商城订单（收付通模式） | 资金进商家二级商户号，走 `/v3/ecommerce/*` |

**通道固化**：预下单时把 `pay_mode`（`direct`/`partner`/`ecommerce`）与 `mch_id` 写入 `payment_records`。
之后的查单、退款、退款查询、关单、分账一律按流水 `pay_mode` 路由，运营中途调整配置不会影响历史流水。

**下单选择规则（商城订单）**：

1. `wechat-ecommerce.enabled=true` → `ecommerce` 通（需商家 `sub_mchid`）；
2. 否则 `wechat-split.enabled=true` 且该订单物业公司在灰度白名单内 → `partner` 通道（需商家 `sub_mchid`，缺失则拒绝在线支付）；
3. 否则 → `direct` 通道（平台代收，不做微信分账）。

平台自营业务恒走 `direct` 通道；`wechat.direct` 未就绪时在线支付返回明确业务错误，**不会回退到服务商号**。

## C.2 回调地址（必须分别配置到对应商户平台）

| 通道 | 支付/退款回调地址 |
| --- | --- |
| 服务商 `wechat.pay` | `https://<域名>/api/v1/payment/notify/wechatpay` |
| 直连商户 `wechat.direct` | `https://<域名>/api/v1/payment/notify/wechatpay-direct` |

两个地址使用各自的验签公钥（微信支付公钥 `PUB_KEY_ID_*` 或平台证书）与 APIv3 密钥。
回调解密后还会校验回调中的 `sp_mchid`（服务商）/ `mchid`（直连）与通道配置一致，不一致直接拒绝（`FAIL`）。

> 旧开发配置中的 `/api/v1/payment/wechat/notify` 是错误的，微信侧必须使用上表地址。

## C.3 新增管理端接口

### C.3.1 GET `/admin/wechat-pay/status` — 双通道配置自检

角色：`platform_admin`。只读，不返回任何密钥内容。

**响应**：`ApiResponse<Object>`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| partner | object | 服务商通道状态：enabled/configured/mchId/appId/notifyUrl/notifyUrlConfigured/verifyMode(PUBLIC_KEY\|PLATFORM_CERT)/publicKeyId/publicKeyPresent/privateKeyPresent/apiV3KeyLength/platformReceiverName |
| direct | object | 直连通道状态，字段同上（platformReceiverName 为 null） |
| split.configEnabled | boolean | `wechat-split.enabled` 配置总开关（还需运营开关 `settlement.wechatSplitEnabled` 才真正启用） |
| split.afterSaleDays | int | 售后窗口天数（代码硬性封顶 30） |
| split.maxSplitRatio | number | 单笔分账总额上限比例（默认 0.30，须与微信商户平台「最大分账比例」一致） |
| split.receiverRelationType | string | 合作方接收方关系类型（平台自身固定 SERVICE_PROVIDER） |

> `configured=true` 需同时具备：enabled、mch-id、APIv3 密钥（32 位）、私钥、API 证书序列号、app-id、notify-url。

### C.3.2 微信分账收款账户

角色：`platform_admin`。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/admin/wechat-split/receivers?ownerType=` | 列表；ownerType 可选，取值为 `WechatSplitOwnerTypes`（PROPERTY/COORDINATOR/SECTOR_LEADER/INDIVIDUAL_LEADER/BUILDING_LEADER/PROPERTY_MANAGER/COURIER/RESIDENT/MERCHANT） |
| POST | `/admin/wechat-split/receivers` | **仅登记微信商户号**（新增/覆盖同一主体，ownerType + ownerId 唯一）。个人 openid 手填已于 🆕 v8.36 下线 |
| DELETE | `/admin/wechat-split/receivers/{id}` | 删除 |

**POST 请求体**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| ownerType | String | 是 | 主体类型；`PLATFORM` 不允许登记（平台佣金固定落服务商商户号） |
| ownerId | String | 是 | 主体 ID（物业公司ID/角色ID/住户ID 等，≤32） |
| accountNo | String | 是 | **8~20 位纯数字微信商户号**。传个人 openid 直接 `10002` 拒绝，提示改走 `/receivers/auto` |
| accountName | String | 是 | 商户全称（微信要求，缺失返回 `10001 PARAM_MISSING`） |
| verified | Boolean | 否 | 默认 true；置 false 时分账跳过该主体，账户补齐后自动补分（见下方调度说明） |

> **为什么下线个人 openid 手填**：openid 是打款地址，抄错一位会静默打给另一个人；且 `(ownerType, ownerId)` 唯一索引一旦被错误值占用，正确账号再也绑不上。后端没有任何出口向管理端下发他人 openid 明文，因此手填通道本身就没有合法数据来源。个人接收方一律用 §110.1.2 `POST /admin/wechat-split/receivers/auto`（只传 `ownerType` + `ownerIds`，openid 由后端按角色表主键→`residentId` 反查）。

**响应字段**：id、ownerType、ownerId、receiverType（`MERCHANT_ID`/`PERSONAL_OPENID`）、accountNo、accountNoMasked、accountName、verified、reversable（仅商户号可微信回退；个人 openid 退款时平台垫付并登记追回台账）、createdAt、updatedAt。

| 字段 | 下发规则（🆕 v8.36） |
| --- | --- |
| `accountNo` | **仅 `MERCHANT_ID` 下发明文商户号**；`PERSONAL_OPENID` 时该键缺失（全局 `non_null` 序列化），前端拿不到也不该拿 openid 明文 |
| `accountNoMasked` | **恒下发，列表展示一律用它**。商户号=明文（可直接回填编辑）；openid=脱敏值 `oUpF****eS6o` |

> **补分调度（实测）**：`SplitWindowScheduler#retrySkippedSplitRecords`，cron `0 20 3 * * ?`（每日 03:20），扫描 `skipped` 记录重新解析账户并补冻。前提是账户已登记且 `verified=true`（`WechatSplitReceiverAccountService.resolveAccount` 只认 verified），**不需要重新登记**；超出售后窗口/已退款的记录会被置 `expired` 并不再补分。

> 实际的 `/v3/profitsharing/receivers/add` 在首次分账时按商家特约商户号惰性调用（进程内幂等缓存），
> 运营无需为每个商家重复登记接收方。

### C.3.3 商家特约商户号（sub_mchid）绑定

角色：`platform_admin`。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/admin/merchants/{merchantId}/wechat-account` | 查询绑定（未绑定返回 data=null） |
| PUT | `/admin/merchants/{merchantId}/wechat-account` | 绑定/修正 |

**PUT 请求体**

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| subMchid | String | 是 | 8~20 位数字；不能等于服务商 sp_mchid 或平台直连商户号 |
| merchantName | String | 否 | 特约商户主体名称 |
| status | String | 否 | `active`（默认）/`auditing`/`frozen`；仅 `active` 可用于下单 |

**响应字段**：id、merchantId、subMchid、merchantName、status、createdAt、updatedAt。

> 只有 `merchant_wechat_accounts.status=active` 且 `sub_mchid` 非空的商家，商城订单才可走服务商通道；
> 否则下单返回「该商家暂未完成微信进件，请使用积分/物业币支付」。

## C.4 行为与错误变化

| 场景 | 行为 |
| --- | --- |
| 商城订单、分账已启用、商家无 active sub_mchid | 拒绝在线支付（`ORDER_PAY_UNAVAILABLE`），提示使用积分/物业币 |
| 服务商通道未配置（wechat.pay） | 商城订单在线支付拒绝，错误信息含「服务商通道未配置」 |
| 直连通道未配置（wechat.direct） | 平台自营业务在线支付拒绝，错误信息含「直连商户通道未配置」；不会回退服务商号 |
| 分账总额 > **微信交易金额 `cashAmount`** × `wechat-split.max-split-ratio` | 整单拦截不发起任何分账，`ERROR` 日志提示调整比例或申请更高上限。基数不是订单总额：物业币/积分抵扣部分没有微信交易，不计入上限 |
| 售后窗口 > 30 天 | 按 30 天处理（微信自动解冻上限），完结返回 `NOT_ENOUGH` 按幂等成功处理 |
| 回调商户号与通道不一致 | 返回 `FAIL` 并记录 `ERROR` 日志（配置串号防护） |
| 历史流水无 `pay_mode` | 无 sub_mchid 视为 `direct`；有 sub_mchid 按全局收付通开关视为 `ecommerce`/`partner` |

## C.5 新增/变更配置项

| 配置 | 环境变量 | 说明 |
| --- | --- | --- |
| `wechat.pay.public-key-id` | `WECHAT_PAY_PUBLIC_KEY_ID` | 微信支付公钥 ID（`PUB_KEY_ID_*`），配置后进入公钥验签模式 |
| `wechat.pay.public-key-path` | `WECHAT_PAY_PUBLIC_KEY_PATH` | 公钥 PEM 文件路径（与 base64 二选一） |
| `wechat.pay.public-key-base64` | `WECHAT_PAY_PUBLIC_KEY_BASE64` | 公钥内容 Base64（与 path 二选一） |
| `wechat.pay.platform-receiver-name` | `WECHAT_PAY_PLATFORM_RECEIVER_NAME` | 平台分账接收方商户全称（服务商主体全称） |
| `wechat.direct.*` | `WECHAT_DIRECT_*` | 直连商户通道全套凭据与回调地址（见 `.env.example`） |
| `wechat-split.after-sale-days` | `WECHAT_SPLIT_AFTER_SALE_DAYS` | 售后窗口天数（默认 7，最大 30） |
| `wechat-split.max-split-ratio` | `WECHAT_SPLIT_MAX_RATIO` | 单笔分账总额上限比例（默认 0.30） |

> 服务商通道下单会强制 `settle_info.profit_sharing=true`（否则后续无法分账），
> `WECHAT_PAY_PROFIT_SHARING` 仅保留兼容，不再控制服务商订单。

## C.6 数据库变更（V32，手工执行）

`backend/src/main/resources/db/migration/V32__wechat_dual_channel_payment.sql`：

- `payment_records.pay_mode` varchar(20)：支付通道（direct/partner/ecommerce）；
- `payment_records.mch_id` varchar(32)：下单时实际收款商户号快照；
- 索引 `idx_payment_records_mode_status(pay_mode, status)`。

幂等脚本，生产 Flyway 关闭时手工执行；历史流水 `pay_mode` 保持 NULL，由代码推断。

---

# 附录 D：商家自助进件（服务商特约商户，v8.28）

> 背景：进件产品从「电商收付通」切换为「普通服务商特约商户进件」
> （`POST /v3/applyment4sub/applyment/`）。电商收付通需 ICP/EDI，平台未开通；
> 原 `/v3/ecommerce/applyments/` 封装已删除。商家自助填资料，平台不再逐家上微信控制台。

## D.1 接口清单（商家端）

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/merchants/my/applyment/media` | 上传资料图片（multipart `file`），返回 `mediaId`（有效期约 3 天） |
| POST | `/merchants/my/applyment` | 保存/更新资料（`init`/`rejected` 可改） |
| POST | `/merchants/my/applyment/submit` | 提交微信审核，**每次提交生成新的业务申请编号** |
| GET | `/merchants/my/applyment` | 查询状态、驳回原因、`signUrl`（签约链接） |
| GET | `/merchants/my/applyment/banks?bankAccountType=BANK_ACCOUNT_TYPE_CORPORATE` | 开户银行列表（对公/对私） |
| GET | `/merchants/my/applyment/banks/{bankAliasCode}/branches?cityCode=211100&offset=0&limit=100` | 支行列表（联行号；`cityCode` 必填，支持 `offset`/`limit` 分页） |
| GET | `/merchants/my/applyment/industries` | 可选行业与结算规则（前端下拉数据源） |

> 运营后台沿用 `GET/POST /admin/merchants/{id}/applyment*`（查看/提交/刷新），行为同上。

## D.2 保存资料请求字段

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| organizationType | String | 是 | `ENTERPRISE` / `INDIVIDUAL`（小微需单独开通权限，不在本版） |
| merchantShortname | String(64) | 是 | 商户简称 |
| servicePhone | String(32) | 是 | 客服电话 |
| businessLicenseMedia | String | 是 | 营业执照 media_id |
| businessLicenseNumber | String(64) | 是 | 统一社会信用代码/注册号 |
| merchantName | String(128) | 是 | 执照商户名称 |
| legalPerson | String(100) | 是 | 执照经营者/法定代表人 |
| licenseAddress / licensePeriodBegin / licensePeriodEnd | String | 否 | 注册地址与有效期 |
| idCardName / idCardNumber | String | 是 | 法人证件（后端加密） |
| idCardCopyMedia / idCardNationalMedia | String | 是 | 身份证人像面/国徽面 media_id |
| idCardPeriodBegin / idCardPeriodEnd | String | 是 | 证件有效期 |
| idCardAddress | String(512) | 企业必填 | 身份证居住地址（后端 RSA 加密后上送） |
| uboList[] | Object[] | 条件 | 企业仅法人一名受益人时可留空（微信自动回填）；有多名受益人时必填，最多4人：idDocCopy/idDocCopyBack/name/number/address/periodBegin/periodEnd |
| salesScenesType | String | 是 | 仅支持 `SALES_SCENES_MINI_PROGRAM`/`SALES_SCENES_STORE`/`SALES_SCENES_MP` |
| salesAppId / salesSubAppId | String | 二选一 | 小程序/公众号 AppID；小程序场景两者都留空时后端默认使用平台小程序 AppID |
| salesPics[] | String[] | 公众号必填 | 页面截图 media_id |
| bizStoreName / bizAddressCode / bizStoreAddress | String | 门店必填 | 门店信息 |
| storeEntrancePics[] / storeIndoorPics[] | String[] | 门店必填 | 门头照/店内照 media_id |
| qualificationType | String | 否 | 所属行业，留空默认「居民生活服务」；可选值以 `/industries` 为准 |
| qualifications[] | String[] | 条件 | 「其他缴费」等需特殊资质行业必填 |
| bankAccountType | String | 是 | `BANK_ACCOUNT_TYPE_CORPORATE`/`BANK_ACCOUNT_TYPE_PERSONAL`（企业仅对公） |
| bankAccountName / bankAccountNumber | String | 是 | 开户名/账号（后端加密）；对公开户名须等于执照商户名称，个人卡须等于法人姓名 |
| accountBank | String | 是 | 开户银行，取银行列表返回的 `account_bank` 值 |
| bankAliasCode | String | 条件 | 银行列表 `bank_alias_code`，仅用于查询支行 |
| bankBranchId / bankName | String | 条件 | 银行要求支行时二选一 |
| contactType | String | 是 | 本版仅 `LEGAL`（法定代表人本人作超管） |
| contactName / contactMobile / contactEmail | String | 是 | 超管姓名/手机号/邮箱（后端加密上送） |

## D.3 状态机与响应

`init` → `submitted` → `auditing` → `legal_validating`（待账户验证）→ `signing`（待签约）→ `finished`；
驳回为 `rejected`（可改资料重提）。

响应 `ApplymentDetailResponse`：状态字段（`applymentId`/`applymentState`/`wxState`/`submitted`/`signUrl`/
`rejectReason`/`subMchid`/`submitMode`/`lastError`）+ 非敏感资料回显（主体、场景、门店、执照、行业、
结算账户、超管姓名等，便于驳回后重填；敏感证件号/卡号/邮箱不回显）。

审核中的 `GET` 会按 15 秒节流触发微信状态查询，前端 30 秒轮询即可及时拿到 `signUrl`。

> `signUrl` 为超级管理员签约链接（微信仅在 `TO_BE_SIGNED`/`FINISHED` 返回 `sub_mchid`）。
> 前端把 `signUrl` 转二维码，由**法定代表人本人**扫码完成账户验证与签约。

## D.4 行为与错误

| 场景 | 行为 |
| --- | --- |
| 审核中（submitted/auditing/legal_validating/signing）保存资料 | 400 `APPLYMENT_STATE_INVALID` |
| 驳回后重新提交 | 自动生成新的 `business_code`，旧申请单作废 |
| 平台未指定行业/结算规则 | 自动取官方公示值（企业 716 / 个体户 719），行业默认「居民生活服务」 |
| 行业为快递/物流/加油/民办学校 | 400，提示适用 0.3% 专项规则、平台不代受理 |
| 行业为「其他缴费」但未传特殊资质 | 400，提示上传收费授权证明 |
| 企业主体缺身份证居住地址 | 400，提示补全 |
| 企业主体选个人银行卡 | 400「企业主体只能使用对公银行账户」 |
| 对公开户名 ≠ 执照商户名称 / 个人卡开户名 ≠ 法人姓名 | 400，提前拦截（微信必驳回项） |
| 经营场景为 APP/网站/企业微信 | 400（本版无对应资料结构） |
| 超管非 LEGAL / 超管姓名 ≠ 法人姓名 | 400（本版仅支持法定代表人本人） |
| 小程序场景未填 AppID | 自动使用平台小程序 AppID；平台也未配置才 400 |
| 进件完成 | 自动写 `merchant_wechat_accounts`（active + sub_mchid），商城订单开始走服务商通道 |
| 微信凭据未配置 | 502 `WECHAT_APPLYMENT_NOT_CONFIGURED`（要求服务商号/私钥/证书序列号；公钥模式还需公钥，平台证书模式还需 apiV3Key；不要求 notifyUrl） |

## D.5 配置与数据库

| 配置 | 环境变量 | 说明 |
| --- | --- | --- |
| `wechat.applyment.enabled` | `WECHAT_APPLYMENT_ENABLED` | 进件能力开关（默认 true） |
| `wechat.applyment.auto-submit` | `WECHAT_APPLYMENT_AUTO_SUBMIT` | 保存后自动提交（默认 false） |
| `wechat.applyment.settlement-id` | `WECHAT_APPLYMENT_SETTLEMENT_ID` | 入驻结算规则ID 覆盖值（留空自动 716/719） |
| `wechat.applyment.qualification-type` | `WECHAT_APPLYMENT_QUALIFICATION_TYPE` | 所属行业覆盖值（默认「居民生活服务」） |
| `wechat.applyment.finance-institution` | `WECHAT_APPLYMENT_FINANCE_INSTITUTION` | 是否金融机构（默认 false） |

数据库：依次执行 `V33__partner_applyment_fields.sql`（新增 30 列 + `business_code` 索引）与
`V34__partner_applyment_id_card_address_enc.sql`（新增 `id_card_address_enc`，身份证居住地址密文上送）。
原电商收付通配置项 `WECHAT_ECOMMERCE_APPLYMENT_AUTO_SUBMIT` / `WECHAT_ECOMMERCE_APPLYMENT_NOTIFY_URL` 已移除。

## D.6 微信官方接口对照

| 能力 | 微信接口 |
| --- | --- |
| 文件上传 | `POST /v3/merchant/media/upload`（multipart 必带 `meta`，签名主体为 meta JSON） |
| 提交申请单 | `POST /v3/applyment4sub/applyment/` |
| 查询申请状态 | `GET /v3/applyment4sub/applyment/business_code/{business_code}` |
| 银行列表 | `GET /v3/capital/capitallhh/banks/corporate-banking` / `personal-banking` |
| 支行列表 | `GET /v3/capital/capitallhh/banks/{bank_alias_code}/branches` |

---

# 109. v8.29 物业币资金归属与商家应收拆账（2026-10-29）

> 本章回应 2026-10-29 评审的 C1–C5 与「充值现金进平台直连户、成本击穿记物业账」资金错配。
> **字段名/口径以本章为准**；本章未提到的字段保持不变。
> 数据库脚本：`V35__property_coin_fund_and_merchant_coin_settlement.sql`。后端测试：356 / 356 通过。

## 109.1 五条口径裁定（C1–C5）

| 编号 | 结论 | 影响 |
| --- | --- | --- |
| C1 分账上限基数 | 以**微信交易金额 `cashAmount`** 为基数，不是订单总额；物业币/积分部分不进入微信交易 | 代码原实现正确，v8.25 C.4 的「订单金额」表述已修正 |
| C2 商家提现 | **现金与物业币拆成两本账**：微信侧现金由商家特约商户号自行提现；平台内部现金走 `/merchant/withdrawals`；物业币应收走 `/merchant/coin-receivables` | 新增商家应收字段与独立入口；标准商家首页不再恢复全局提现按钮 |
| C3 分成记录可见性 | 管理端继续用 `/distribution/records`；商家/负责人新增 `/distribution/records/mine`，只返回本人份额 | 商家不再需要管理端权限 |
| C4 分成记录生成时点 | **订单完成（确认收货/超时自动确认）** 时生成，不是支付成功 | §21.6 已修正；骑手仍是两段式 |
| C5 分账表名 | `wechat_split_record`（单数） | 文档/前端统一 |

## 109.2 物业币资金模型（本次核心修正）

### 钱怎么走

```text
住户充值 100 元
  → 微信直连商户号 1665249236（direct 通道，不参与分账）
  → 平台记：property_coin_fund_ledger 对物业公司「代管资金 +100」

住户用 100 物业币买商品（商家应得 80）
  → 商家特约商户号没有现金
  → 平台从代管资金内部结算 80 给商家（source=coin_receivable）
  → 平台保留 20 作为佣金/分账份额
```

### 成本击穿修正

- **物业币不是成本**：充值时就已收到等额现金，属于支付工具，不再从「可分配佣金」中扣减，也不再把差额记到物业 `PointPool`。
- **积分才是成本**：积分由物业免费发放，仍按原逻辑从可分配佣金扣除，不足部分由物业承担（`deficitSponsor=property`）。
- 计算式变为：

```text
可分配佣金 = 商品实付金额 × 抽佣率 − 积分成本
（不再减物业币成本）
```

> 历史 `distribution_records` 不做追溯重算；新订单按新口径。历史 `pool_diff` 若需冲回，由管理端按台账人工调整。

### 代管资金台账

| 记账类型 `entryType` | 方向 | 触发 | 金额 |
| --- | --- | --- | --- |
| `recharge_in` | credit | 物业币充值回调到账 | 充值金额 |
| `merchant_settlement` | debit | 商家物业币应收提现审核 `completed` | 提现单全额（手续费归平台，不再属于物业代管资金） |
| `property_transfer` | debit | 平台向物业公司划转代管资金 | 划转金额 |
| `adjustment` | credit/debit | 人工对账调整（预留） | 调整金额 |

- 账户余额 = `SUM(credit) − SUM(debit)`，按物业公司隔离；余额为负表示免费发放物业币超过充值现金，管理端展示为「资金缺口」。
- 唯一键 `(biz_type, biz_id, entry_type)` 保证回调/审核重复执行只记一次。

## 109.3 商家物业币应收（前端结算 Tab）

### 口径

```text
商家应得 = 商品金额（订单总额 − 优惠券）− 抽佣 − 商家承担的配送补贴
现金容量 = 微信实收现金 − 配送费 − 已执行到商家账户的货物分账
物业币应收 = max(0, 商家应得 − 现金容量)
```

- 纯物业币订单：现金容量为 0，商家应得全部进入物业币应收。
- 混合支付：现金先抵抽佣，剩余部分进入物业币应收。
- 服务商/电商现金已在商家微信商户号，**不计入**平台内部可提现。
- 直连代收现金在平台手里，计入 `cashWithdrawableAmount`。

### `GET /merchants/my` 新增字段

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `settlementMode` | string | `internal`（技师店/组长店/业主小店）或 `split`（现金+物业币拆账） |
| `cashWithdrawableAmount` | number | 平台内部现金可提现（直连代收；微信侧现金不在此列） |
| `coinReceivableAmount` | number | 物业币应收累计 |
| `coinSettledAmount` | number | 已结算金额 |
| `coinPendingAmount` | number | 在途金额（pending/approved） |
| `coinAvailableAmount` | number | 可结算余额 |
| `wechatCashSettledAmount` | number | 已由微信分账结算到商家商户号的现金（仅供对账） |
| `totalIncomeAmount` | number | 商家累计应得（商品金额 − 抽佣 − 商家配送补贴） |

> 原 `withdrawableAmount` / `shopWithdrawableAmount` 含义收窄为「平台内部现金可提现」；
> 内部收益店铺（`settlementMode=internal`）仍保留原口径，全部收益走 `/merchant/withdrawals`。

### `GET /merchant/coin-receivables` — 应收明细

**Auth**: Required | **Roles**: `merchant`

Query：`page`、`pageSize`（≤100）。

```json
{
  "code": 0,
  "data": {
    "grossAmount": 320.00,
    "settledAmount": 120.00,
    "pendingAmount": 50.00,
    "availableAmount": 150.00,
    "orderCount": 6,
    "settledOrderCount": 2,
    "items": [
      {
        "orderId": "ord_xxx",
        "orderNo": "NO20261029001",
        "completedAt": "2026-10-29T08:00:00Z",
        "totalAmount": 100.00,
        "couponAmount": 0.00,
        "coinUsed": 60.00,
        "cashAmount": 40.00,
        "payMode": "partner",
        "commissionRate": 0.2000,
        "commissionAmount": 20.00,
        "merchantIncome": 80.00,
        "coinReceivableAmount": 60.00,
        "settledAmount": 0.00,
        "pendingAmount": 0.00,
        "availableAmount": 60.00,
        "settlementStatus": "available"
      }
    ],
    "page": 1,
    "pageSize": 20,
    "totalElements": 6
  }
}
```

- `settledAmount` / `pendingAmount` 按提现单创建时间 **FIFO** 归集到订单，供商家逐单对账。
- `settlementStatus`：`available` / `partial` / `pending` / `settled`。

### `POST /merchant/coin-receivables/withdrawals` — 申请结算

**Auth**: Required | **Roles**: `merchant`；支持 `X-Request-Id` 幂等

```json
{ "amount": 150.00 }
```

- 服务端强制 `source=coin_receivable`，客户端不需要也不能指定来源。
- 申请即校验 `coinAvailableAmount`（含 pending/approved 占用），管理端审核通过时复核。
- 审核完成由管理端登记转账凭证；后端自动写 `property_coin_fund_ledger` 的 `merchant_settlement` 出账。
- 内部收益店铺调用返回 94003（该店铺走内部收益提现）。

## 109.4 `GET /distribution/records/mine` — 我的分成记录

**Auth**: Required | **Roles**: 商家 / 组长 / 技工 / 统筹 / 板块负责人 / 个体负责人 / 楼长

按当前身份自动收敛，只返回本人份额：

| 身份 | 数据范围 | `shareType` |
| --- | --- | --- |
| 商家 / 组长 / 技工 | 本人店铺 | `merchant` |
| 统筹 | `coordinator_id=本人` | `coordinator` |
| 板块负责人 | `sector_leader_id=本人` | `sector_leader` |
| 个体负责人 | `individual_leader_id=本人` | `individual_leader` |
| 单元楼长 | `building_leader_id=本人` | `building_leader` |

响应字段：`id`、`orderId`、`orderNo`、`merchantName`、`totalAmount`、`commissionRate`、`shareType`、`myShareAmount`、`createdAt`。
不返回其他主体的份额；管理端全量记录仍用 `GET /distribution/records`。

## 109.5 权限下发

### `GET /auth/my-permissions` — 当前用户有效权限

**Auth**: Required（任意登录角色）

```json
{
  "code": 0,
  "data": {
    "role": "resident",
    "normalizedRole": "resident",
    "propertySubRole": null,
    "propertyCompanyId": "pc_demo001",
    "adminWorkspace": true,
    "roleCodes": ["coordinator", "resident"],
    "permissions": ["merchant:audit", "promotion:push", "community:read"]
  }
}
```

- 权限码统一 **冒号格式**（`resource:action`）；数据库存点号，接口返回冒号。
- 计算规则：`角色预设 ∪ 用户授权 − 用户撤销`，并剔除当前身份在管理端不可用的码。
- `platform_admin` 返回全部；`property_admin` 返回全部非平台专属权限。
- 标签身份（活动组长/统筹/板块/个体负责人/社区管理员）返回预设与账号授权的并集，但剔除平台专属（`merchant:read/create/update`、`property_company:manage`）和物业领导专属（`config:set`、`permission:read/manage`、`property_operator:manage`）；这些码即使历史数据里有授权也不会下发。
- 历史 `property_leader` / `property_operator` 角色已归一为 `property_admin`：
  - `property_leader`：等同物业主账号；
  - `property_operator`：除 `config.set` / `permission.manage` / `permission.read` / `property_operator.manage` 外等同主账号（用户级授权仍可单独授予）。
- `adminWorkspace=true` 表示可进入管理端工作台：物业/平台/社区管理员，或持活动组长/统筹/板块/个体负责人标签的住户。普通住户、商家、技工、配送员为 `false`。
- 社区管理员（`community_admin`）默认拥有 `community:read` / `community:manage`；其他管理权限仍需单独授权。
- `/auth/profile` 同步返回：`adminWorkspace`、`normalizedRole`、`propertySubRole`、`rolePresetId`、`rolePresetName`、`effectiveRoleCodes`、`effectivePermissions`。登录后优先用 `/auth/profile` 一次取齐；保存权限后可用 `/auth/my-permissions` 轻量刷新。

## 109.6 管理端物业币资金归属

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| GET | `/admin/property-coin-funds` | `pool:read` 或管理员 | 按物业公司汇总余额/累计入账/累计出账/商家结算/资金缺口 |
| GET | `/admin/property-coin-funds/records` | `pool:read` 或管理员 | 台账明细，支持 `propertyCompanyId`、`entryType` |
| POST | `/admin/property-coin-funds/transfers` | `pool:adjust`（平台管理员） | 平台向物业划转代管资金，登记 `transferProof` |

`GET /admin/property-coin-funds` 响应示例：

```json
{
  "code": 0,
  "data": [
    {
      "propertyCompanyId": "pc_demo001",
      "propertyName": "阳光物业",
      "balance": 1860.00,
      "totalIn": 5000.00,
      "totalOut": 3140.00,
      "merchantSettlementAmount": 2640.00,
      "fundingGapAmount": 0.00
    }
  ]
}
```

- 物业管理员固定只看本物业；平台管理员可传 `propertyCompanyId` 筛选。
- 台账 `entryType`：`recharge_in` / `merchant_settlement` / `property_transfer` / `adjustment`。

### `GET /admin/property-coin-funds/records` — 资金台账明细

**权限**：`pool:read` 或管理员。Query：`page`（默认 1）、`pageSize`（默认 20）、`propertyCompanyId`、`entryType`。
响应 `PageResponse<PropertyCoinFundLedgerItemResponse>`，`list[]` 字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `String` | 台账 ID |
| `propertyCompanyId` | `String` | 物业公司 ID |
| `entryType` | `String` | `recharge_in` / `merchant_settlement` / `property_transfer` / `adjustment` |
| `direction` | `String` | `credit`（入账）/ `debit`（出账） |
| `amount` | `BigDecimal` | 发生金额 |
| `balanceAfter` | `BigDecimal` | 记账后该物业公司余额 |
| `bizType` / `bizId` | `String` | 业务类型 / 业务单号（唯一键 `(bizType, bizId, entryType)` 保证回调/审核重复执行只记一次） |
| `residentId` | `String` | 充值住户 ID（仅 `recharge_in`） |
| `merchantId` | `String` | 结算商家 ID（仅 `merchant_settlement`） |
| `description` | `String` | 说明 |
| `operatorId` | `String` | 操作人 ID（人工划转/调整时） |
| `transferProof` | `String` | 平台转物业的转账凭证 / 银行流水号 |
| `createdAt` | `String` | 创建时间 |

### `POST /admin/property-coin-funds/transfers` — 平台向物业划转代管资金

**权限**：`pool:adjust`（仅平台管理员；物业管理员调用返回 `20004`）。

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `propertyCompanyId` | `String` | ✅ | 收款物业公司 ID |
| `amount` | `BigDecimal` | ✅ | 划转金额，必须 > 0 |
| `transferProof` | `String` | ❌ | 转账凭证 / 银行流水号（强烈建议填，台账可追溯） |
| `remark` | `String` | ❌ | 备注 |

**成功响应**：`PropertyCoinFundLedgerItemResponse`（同上一表，`entryType=property_transfer`、`direction=debit`）。

| 错误码 | HTTP | 说明 |
| --- | --- | --- |
| `97001` | 404 | 物业公司不存在 |
| `10002` | 400 | `划转金额必须大于0` |
| `94007` | 400 | 🆕 **物业币代管资金余额不足**（`PROPERTY_COIN_FUND_INSUFFICIENT`，文案含当前余额，划转额超过该物业代管资金余额） |
| `20004` | 403 | 权限不足（非平台管理员 / 无 `pool:adjust`） |

## 109.7 上线执行

```bash
mysql -h<host> -u<user> -p --default-character-set=utf8mb4 <db> \
  < backend/src/main/resources/db/migration/V35__property_coin_fund_and_merchant_coin_settlement.sql
```

执行后核对：

```sql
SELECT COUNT(*) FROM property_coin_fund_account;
SELECT COUNT(*) FROM property_coin_fund_ledger;
SHOW COLUMNS FROM merchant_withdrawals LIKE 'source';
```

V35 会把历史已支付 `payment_records(biz_type='coin_recharge', status='paid')` 回填为各物业公司的 `recharge_in`，并重算账户余额；重复执行不会重复入账。

> **存量提现单处理**：V35 之前创建的 `merchant_withdrawals` 统一标记为 `source=legacy`。
> 上线后管理端审核会按新口径复核「平台内部现金」；若该单实际是物业币应收，
> 请驳回后由商家在 `/merchant/coin-receivables` 重新申请，避免现金/物业币混提。

## 109.8 前端页面调整清单

1. 商家结算 Tab：现金区展示 `cashWithdrawableAmount`（微信侧现金只展示 `wechatCashSettledAmount` 对账，不提供提现按钮）；物业币区展示 `coinReceivableAmount` / `coinSettledAmount` / `coinPendingAmount` / `coinAvailableAmount`，按钮进入 `/merchant/coin-receivables`。
2. 商家首页：标准商家不再显示全局提现按钮；技师店/组长店/业主小店保留内部收益提现按钮。
3. 新增「物业币应收」页：订单级明细 + 申请结算弹窗（金额 ≤ `availableAmount`）。
4. 新增「我的分成」页：调用 `/distribution/records/mine`，按 `shareType` 显示文案。
5. 权限配置页：登录后先调 `/auth/my-permissions`，按钮级用 `permissions` 判断；不要再硬编码角色。
6. 管理端新增「物业币资金」页：汇总 + 台账 + 平台转物业。
7. 分账试算页：明确「分账上限按微信交易金额 `cashAmount` 计算」；物业币不进入微信交易。


---

# 110. v8.33 新增（2026-11-01 ～ 11-03）

> 本章补齐「代码已实现、此前文档未写」的 4 个管理端接口，以及微信支付回调地址的正式契约。
> 与 V36 分账口径相关的改动见 §21 / §51.4 / §71，小程序登录改动见 §1.1。

## 110.1 微信分账 · 个人接收方主体预检与批量登记

角色：`platform_admin`（`RoleGuard.requireAnyRole("platform_admin")`，物业管理员调用返回 `403 / 20004`）。

背景：分账要把钱打到统筹/板块/个体负责人、楼长、物业经理、快递员的**微信零钱**，收款标识是 `residents.openid`。
运营不该手抄 openid——`residents.openid` 既是登录凭证也是打款地址，抄错一位就静默打给另一个人。
所以这两个接口的设计是：**前端只传主体类型 + 主体 ID，openid 由后端反查**；返回的 openid 一律脱敏（`oUpF****eS6o`），仅用于人工核对是否本人，不能回填提交。

### 110.1.1 GET `/admin/wechat-split/receivers/principals` — 登记前预检

**Query**：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `ownerType` | `String` | ✅ | `RESIDENT` / `COORDINATOR` / `SECTOR_LEADER` / `INDIVIDUAL_LEADER` / `BUILDING_LEADER` / `COURIER` / `PROPERTY_MANAGER` / `MERCHANT`。传 `PROPERTY` / `PLATFORM` 返回 `10002`（物业公司走商户号，平台固定用服务商商户号） |
| `ownerIds` | `List<String>` | ✅ | 主体 ID 列表，**支持重复参数或逗号分隔**（`?ownerIds=cm_1,cm_2` 与 `?ownerIds=cm_1&ownerIds=cm_2` 等价）。注意这里传的是**角色表主键**（如 `coordinators.id`、`courier_managers.id`），不是 `residents.id`；仅 `RESIDENT` / `PROPERTY_MANAGER` 两类本身就是住户 ID |

**成功响应**（`List<ReceiverPrincipalResponse>`，与入参顺序一致，空白 ID 会被跳过）：

```json
{
  "code": 0,
  "data": [
    {
      "ownerType": "COURIER",
      "ownerId": "cm_001",
      "name": "李四",
      "phoneMasked": "138****0008",
      "propertyCompanyId": "pc_demo001",
      "openidAvailable": true,
      "openidMasked": "oUpF****eS6o",
      "unavailableReason": null,
      "registered": false,
      "receiverAccountId": null
    }
  ]
}
```

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `ownerType` / `ownerId` | `String` | 回显入参 |
| `name` / `phoneMasked` / `propertyCompanyId` | `String` | 主体对应住户的姓名 / 脱敏手机号 / 所属物业公司；主体记录不存在或未关联住户时**均为 `null`** |
| `openidAvailable` | `Boolean` | 是否可登记为个人收款方（即该住户已认领微信身份） |
| `openidMasked` | `String` | 脱敏 openid，仅供核对是否本人；**不可用于提交** |
| `unavailableReason` | `String` | 不可登记原因，可登记时为 `null`。实测文案：`该账号未认领微信身份（无 openid），请本人在小程序登录一次` / `openid 格式异常（应为 20~64 位字母/数字/_/-），请先修复该账号的微信身份`（此时 `openidMasked` 仍会返回） / `快递负责人主体记录不存在或未关联住户账号`（`describe(type)` 会按主体类型换成「住户 / 统筹负责人 / 板块负责人 / 个体负责人 / 单元楼长 / 快递负责人 / 物业经理 / 商家店主」） |
| `registered` | `Boolean` | 是否已登记为分账接收方 |
| `receiverAccountId` | `String` | 已登记时的接收方账户 ID（可用于 POST 覆盖与 DELETE /admin/wechat-split/receivers/{id}），未登记为 `null` |

**错误响应**：

| 错误码 | HTTP | 说明 |
| --- | --- | --- |
| `10001` | 400 | `ownerType` 为空（`主体类型不能为空`）；`ownerIds` 为空走 Bean Validation `@NotEmpty` → `10001` |
| `10002` | 400 | `ownerType` 不在允许集合：`不支持的主体类型: xxx（平台主体固定使用服务商商户号，无需登记）`；为 `PROPERTY`/`PLATFORM`：`主体 XXX 不支持按 openid 自动登记：物业公司请登记商户号，平台固定用服务商商户号` |
| `20004` | 403 | 非 `platform_admin` |

### 110.1.2 POST `/admin/wechat-split/receivers/auto` — 按主体批量登记

**请求体**（`AutoRegisterReceiversRequest`）：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `ownerType` | `String` | ✅ | 取值同上（`PROPERTY` 不支持自动登记） |
| `ownerIds` | `List<String>` | ✅ | `@NotEmpty`，主体 ID 列表（角色表主键） |
| `verified` | `Boolean` | ❌ | 默认 `true`；置 `false` 时该主体分账会被跳过，账户补齐后由补偿调度自动补分 |

```json
{ "ownerType": "COURIER", "ownerIds": ["cm_001", "cm_002"], "verified": true }
```

**成功响应**（`List<AutoRegisterReceiverResult>`）：

```json
{
  "code": 0,
  "data": [
    { "ownerId": "cm_001", "name": "李四", "status": "registered",
      "openidMasked": "oUpF****eS6o", "receiverAccountId": "wsra_001", "reason": null },
    { "ownerId": "cm_002", "name": "王五", "status": "skipped",
      "openidMasked": null, "receiverAccountId": null,
      "reason": "该账号未认领微信身份（无 openid），请本人在小程序登录一次" }
  ]
}
```

> **逐条返回、不整批失败**：`status` 取 `registered`（已写入接收方）/ `skipped`（个别人缺微信身份等原因）。
> 前端务必检查每条的 `status`，把 `skipped` 的 `reason` 显示给运营——不要只看 HTTP 200 就提示「登记成功」。
> 同一主体（`ownerType` + `ownerId`）唯一，重复提交即覆盖，可安全重试。

**错误响应**：`10001`（`ownerType` 或 `ownerIds` 为空）、`10002`（`ownerType` 不支持）、`20004`（非平台管理员）。

> 登记完成后，缺户的历史分账由补偿调度自动补分，无需人工逐单重放。
>
> **实现细节**：自动登记与 §C.3.2 共用同一落库路径，但**不提交 `accountName`**——微信会校验实名一致性，住户表里的姓名未必等于微信实名，填了反而被微信拒。自 v8.36 起个人 openid **没有任何手填入口**（§C.3.2 只收商户号），因此也不存在「带姓名手工登记」这条路；如需修正某人的收款账户，请删除该记录后用本接口重新按主体登记。
> 单条 `skipped` 的 `reason` 即该主体在落库时抛出的业务错误 `message`（例如账户号校验失败），可直接展示。

---

## 110.2 管理端 — 住户微信身份认领

**Auth**: Required | **Roles**: `property_admin`（仅本物业公司账号）、`platform_admin`
（`RoleGuard.requireAnyRole("property_admin","platform_admin")`；物业管理员访问其他物业公司的账号时，服务端**按「账号不存在」返回 `30001`**，不暴露该账号是否存在）

解决的问题：管理端预建的商家/组长/技工/配送员账号只有手机号、没有 openid；本人第一次用小程序登录时，V36 之前会新建一条「有 openid 没手机号」的记录，于是**有手机号的账号没 openid、有 openid 的账号没手机号**，分账个人接收方永远登记不到人。本接口把 openid 从来源记录迁到目标账号。

> **V36 之后新增的幽灵记录已被 §1.1 的强制手机号绑定堵住**；本接口继续用于清理 V36 之前产生的存量记录。

### 110.2.1 GET `/admin/residents/{id}/wechat-claim-sources` — 列出可认领的微信身份

**路径参数**：`id` = 目标账号 ID（`residents.id`，即要把微信身份**绑进来**的那个账号）。

**成功响应**（`List<WechatClaimSourceResponse>`，同手机号且带 openid 的**其他**住户记录；openid 只回脱敏值）：

```json
{
  "code": 0,
  "data": [
    {
      "residentId": "res_ghost001",
      "name": "王测试",
      "phoneMasked": "138****0008",
      "propertyCompanyId": "pc_demo001",
      "openidMasked": "oUpF****eS6o",
      "pointBalance": 0,
      "coinBalance": 0.00,
      "claimable": true,
      "reason": null
    }
  ]
}
```

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `residentId` | `String` | 来源记录 ID（小程序登录时自动新建、带 openid 的那条） |
| `name` / `phoneMasked` / `propertyCompanyId` | `String` | 来源记录姓名 / 脱敏手机号 / 所属物业公司 |
| `openidMasked` | `String` | 脱敏 openid，仅供核对是否本人；无 openid 时为 `null` |
| `pointBalance` / `coinBalance` | `Integer` / `BigDecimal` | 来源记录余额，**必须都为 0** 才能认领 |
| `claimable` | `Boolean` | 是否可直接认领（前端据此置灰按钮） |
| `reason` | `String` | 不可认领原因 / 风险提示。取值：`该记录没有微信身份，不能作为来源` / `该记录仍挂有积分/物业币，需先清零` / `注意：该记录属于另一家物业公司（认领只迁微信身份，不迁数据）`；可认领时为 `null` |

> 空数组表示该手机号下没有可配对的微信身份记录——让本人先在小程序登录一次（V36 起登录会强制授权手机号），再回来刷新。

**错误响应**：

| 错误码 | HTTP | 说明 |
| --- | --- | --- |
| `30001` | 404 | 目标账号不存在，**或属于其他物业公司**（物业管理员跨公司查询按此码返回，不区分两种情况） |
| `10002` | 400 | 目标账号没有手机号，无法按手机号匹配微信身份 |
| `20004` | 403 | 角色不是 `property_admin` / `platform_admin` |

### 110.2.2 POST `/admin/residents/{id}/wechat-claim` — 认领微信身份

**路径参数**：`id` = 目标账号 ID。

**请求体**（`ClaimWechatIdentityRequest`）：

| 字段 | 类型 | 必填 | 校验 | 说明 |
| --- | --- | --- | --- | --- |
| `sourceResidentId` | `String` | ✅ | `@NotBlank @Size(max=32)` | 来源记录 ID（取自 §110.2.1 列表） |

**成功响应**（`ClaimWechatIdentityResponse`）：

```json
{
  "code": 0,
  "data": {
    "residentId": "res_001",
    "sourceResidentId": "res_ghost001",
    "openidMasked": "oUpF****eS6o",
    "wechatBound": true,
    "message": "已认领微信身份。请通知本人在小程序重新登录一次，使登录态切到该账号；来源记录已降级为无微信身份的普通账号，可另行注销。"
  }
}
```

**行为与约束**（服务端逐条校验，任一不满足即整笔回滚）：

| 约束 | 说明 |
| --- | --- |
| 只迁微信身份，**不迁数据** | 订单 / 钱包 / 积分 / 物业币都留在原记录，只有 `openid`、`unionid`、`wechatBound` 三个字段搬家 |
| 目标账号必须**尚无 openid** | 已绑定返回 `10002`「该账号已绑定微信身份，无需认领」 |
| 来源记录必须有 openid 且格式合法 | `[A-Za-z0-9_-]{20,64}`，否则 `10002` |
| 两条记录手机号必须一致 | 否则 `10002`「两条记录的手机号不一致，不能认领」 |
| 来源记录余额必须清零 | 积分与物业币都为 0，否则 `10002`，文案含具体余额（迁移后来源记录无法再用微信登录，钱会锁死） |
| 写入顺序 | 先清空来源再写目标（`residents.openid` 是唯一索引，反序会撞 `Duplicate entry`） |

**认领后务必通知本人在小程序重新登录一次**，否则其本地 token 仍指向来源记录，看起来「钱没动」。随后即可用 §110.1 把该主体登记为分账个人接收方。

**错误响应**：`30001`（目标或来源账号不存在，含物业管理员跨公司）、`10002`（上表任一约束不满足，`message` 即原因，请原样展示）、`20004`（角色不是 `property_admin` / `platform_admin`）。

---

## 110.3 微信支付 / 退款回调（前端不调用，供配置核对）

| 通道 | 回调路径 | 说明 |
| --- | --- | --- |
| 服务商 `wechat.pay` | `POST /payment/notify/wechatpay` | 微信侧配置为 `https://<域名>/api/v1/payment/notify/wechatpay` |
| 直连商户 `wechat.direct` | `POST /payment/notify/wechatpay-direct` | 微信侧配置为 `https://<域名>/api/v1/payment/notify/wechatpay-direct` |
| 电商收付通分账 | `POST /payment/ecommerce-split/notify` | 二期备选通道，`wechat-ecommerce.enabled=true` 时才生效 |
| 电商收付通转账 | `POST /payment/ecommerce-transfer/notify` | 同上 |

- 这些地址**由微信服务器调用**，无 `Authorization`、不走 `AuthInterceptor`；前端不要调用、也不要加签。
- 两个通道使用各自的验签公钥与 APIv3 密钥；解密后还会校验回调里的 `sp_mchid`（服务商）/ `mchid`（直连）与通道配置一致，不一致直接返回 `FAIL`。
- 旧开发配置里的 `/api/v1/payment/wechat/notify` 是**错误地址**，微信侧必须用上表。
- 回调成功一律返回 HTTP 200 + `{"code":"SUCCESS"}`；失败返回 `{"code":"FAIL","message":...}`，由微信按策略重试。

---

## 110.4 前端页面调整清单（v8.33）

1. **登录页（必改）**：接 `getPhoneNumber`，首次登录带 `phoneCode`；`30010` 按流程信号处理并**换新 `code` 重试**；`30011` 原样展示文案且**不重试**。详见 §1.1。
2. **分账配置页**：新增「用户积分预算比例」「用户奖励金预算比例」两个输入（`GET/PUT /admin/platform-share-rates`，§71.1/§71.2），前端即时校验合计 ≤ 100%；仅平台管理员可编辑。
3. **分账预览/明细页**：渲染 `userPointShareRate`/`userRewardShareRate`/`userPointBudget`/`userRewardBudget` 四个新字段，并优先用 `calculationVersion` 判断口径；历史单（`distribution_b_v1`）这四项为 `null`，按「-」显示，**不要当 0.00 参与合计**。合计校验式见 §71.0。
4. **商家返现配置**：`coinRebateRate` / `coinRebateEnabled` 已不再影响任何金额（§45 / §7.1），置灰或删除。
5. **管理端「分账收款账户」页**：先调 §110.1.1 预检列出可登记主体，再调 §110.1.2 批量登记，逐条展示 `status`/`reason`。
6. **管理端「住户微信身份认领」入口**：在账号详情或列表增加操作，走 §110.2.1 列候选 → §110.2.2 认领；`claimable=false` 置灰并展示 `reason`。
7. **现金支付订单**：`pointEarned`/`coinEarned` 现在会非零（V36 修复回调发放），积分明细会出现 `source=order` 记录，属正常。


---

# 附录二 · 错误码权威总表（以 `ErrorCode.java` 为准，2026-11-03 全量核对）

> 本表由 `backend/src/main/java/com/wuye/points/exception/ErrorCode.java` 逐条生成，共 **190** 个业务码（2026-11-03 v8.34 新增 `97075`）。
> **各接口小节里标注的历史错误码如与本表冲突，一律以本表为准**（失效码迁移关系见附录三）。
> `HTTP` 列即该码对应的响应状态码；响应体固定为 `{ "code": <码>, "message": ..., "data": null, "timestamp": ... }`。

另有 **非枚举码**：`429001`（HTTP 429）由 `RateLimitFilter` 直接写出，只含 `code`/`message`，见 §0.10。

### 10xxx（7 个）

| code | HTTP | 枚举名 | message |
| --- | --- | --- | --- |
| `10001` | 400 | `PARAM_MISSING` | 参数缺失 |
| `10002` | 400 | `PARAM_INVALID` | 参数格式错误 |
| `10003` | 400 | `PARAM_OUT_OF_RANGE` | 参数值超出范围 |
| `10004` | 404 | `ENDPOINT_NOT_FOUND` | 接口不存在（路径或方法不匹配） |
| `10005` | 405 | `METHOD_NOT_SUPPORTED` | 请求方法不支持 |
| `10006` | 415 | `MEDIA_TYPE_NOT_SUPPORTED` | 不支持的媒体类型 |
| `10007` | 400 | `BODY_PARSE_FAILED` | 请求体解析失败 |

### 17xxx（1 个）

| code | HTTP | 枚举名 | message |
| --- | --- | --- | --- |
| `17001` | 400 | `SERVICE_UNAVAILABLE` | 服务已下架 |

### 20xxx（5 个）

| code | HTTP | 枚举名 | message |
| --- | --- | --- | --- |
| `20001` | 401 | `TOKEN_MISSING` | Token缺失 |
| `20002` | 401 | `TOKEN_EXPIRED` | Token过期 |
| `20003` | 401 | `TOKEN_INVALID` | Token无效 |
| `20004` | 403 | `PERMISSION_DENIED` | 权限不足 |
| `20005` | 403 | `ACCOUNT_DISABLED` | 账号被禁用 |

### 30xxx（11 个）

| code | HTTP | 枚举名 | message |
| --- | --- | --- | --- |
| `30001` | 404 | `RESIDENT_NOT_FOUND` | 业主不存在 |
| `30002` | 400 | `FAMILY_MEMBER_LIMIT` | 家庭人数超限 |
| `30003` | 400 | `WECHAT_AUTH_FAILED` | 微信授权失败 |
| `30004` | 400 | `MERCHANT_DUPLICATE_BINDING` | 该商家已在当前物业下挂接 |
| `30005` | 400 | `RESIDENT_DUPLICATE_PHONE` | 该手机号在当前物业公司下已存在 |
| `30006` | 400 | `ACCOUNT_DELETION_PENDING_ORDER` | 存在未完成的订单，请先处理 |
| `30007` | 400 | `ACCOUNT_DELETION_PENDING_FEE` | 存在未缴纳的物业费，请先处理 |
| `30008` | 400 | `ACCOUNT_DELETION_COIN_BALANCE` | 物业币余额不为0，请先处理 |
| `30009` | 400 | `ACCOUNT_DELETION_FAMILY_MEMBERS` | 您是户主，请先移除家庭成员 |
| `30010` | 400 | `WECHAT_PHONE_AUTH_REQUIRED` | 首次登录需授权手机号 |
| `30011` | 400 | `WECHAT_PHONE_ACCOUNT_AMBIGUOUS` | 该手机号匹配到多个账号，请联系物业在管理端确认微信身份 |

### 40xxx（2 个）

| code | HTTP | 枚举名 | message |
| --- | --- | --- | --- |
| `40001` | 400 | `POINT_INSUFFICIENT` | 积分余额不足 |
| `40002` | 404 | `POINT_RULE_NOT_FOUND` | 积分规则不存在 |

### 50xxx（2 个）

| code | HTTP | 枚举名 | message |
| --- | --- | --- | --- |
| `50001` | 400 | `COIN_INSUFFICIENT` | 物业币余额不足 |
| `50002` | 400 | `COIN_EXCHANGE_NOT_ALLOWED` | 积分与物业币不可互兑 |

### 60xxx（38 个）

| code | HTTP | 枚举名 | message |
| --- | --- | --- | --- |
| `60001` | 404 | `MERCHANT_NOT_FOUND` | 商家不存在 |
| `60002` | 409 | `MERCHANT_AUDITING` | 商家入驻审核中 |
| `60003` | 409 | `AUDIT_ALREADY_PROCESSED` | 审核已处理 |
| `60004` | 404 | `PRODUCT_NOT_FOUND` | 商品不存在 |
| `60005` | 400 | `MERCHANT_ALREADY_KICKED` | 商家已被踢出 |
| `60006` | 404 | `MERCHANT_CONFIG_NOT_FOUND` | 商家在该物业下未完成挂接配置 |
| `60010` | 400 | `RESIDENT_MERCHANT_DEPOSIT_INVALID` | 商家保证金不合法 |
| `60011` | 400 | `MERCHANT_AD_QUOTA_INSUFFICIENT` | 广告额度不足 |
| `60012` | 404 | `MERCHANT_AD_NOT_FOUND` | 广告不存在 |
| `60020` | 400 | `CANTEEN_PURCHASE_PENDING` | 额度购买待支付 |
| `60021` | 404 | `CANTEEN_QUOTA_PURCHASE_NOT_FOUND` | 额度购买记录不存在 |
| `60022` | 400 | `CANTEEN_QUOTA_INSUFFICIENT` | 可用额度不足 |
| `60023` | 404 | `CANTEEN_MERCHANT_NOT_IN_SYSTEM` | 食堂商家未在系统内 |
| `60024` | 400 | `CANTEEN_BALANCE_INSUFFICIENT` | 食堂余额不足 |
| `60025` | 400 | `CANTEEN_NOT_MAIN_MERCHANT` | 非社区食堂主商家 |
| `60026` | 400 | `CANTEEN_BINDING_EXISTS` | 绑定关系已存在 |
| `60027` | 404 | `CANTEEN_BINDING_NOT_FOUND` | 绑定关系不存在 |
| `60028` | 400 | `RESIDENT_MERCHANT_HAS_ORDERS` | 该住户商家已有订单 |
| `60029` | 400 | `RESIDENT_MERCHANT_NOT_PUBLIC` | 住户商家未公开 |
| `60030` | 403 | `DISTRIBUTOR_NOT_ENABLED` | 该商家未开通一级代理权限 |
| `60031` | 400 | `DISTRIBUTOR_PRODUCT_MERCHANT_REQUIRED` | 必须指定一级代理商家（distributorMerchantId） |
| `60032` | 400 | `LISTING_QUANTITY_INVALID` | 拿货件数必须为不小于1的整数 |
| `60033` | 400 | `DISTRIBUTOR_STOCK_INSUFFICIENT` | 一级代理货源库存不足 |
| `60034` | 400 | `LISTING_NOT_CANCELLABLE` | 该上架存在未完成订单，不可取消 |
| `60035` | 400 | `LISTING_STOCK_INSUFFICIENT` | 可售件数不足 |
| `60036` | 404 | `SHOP_MERCHANT_NOT_FOUND` | 业主小店店铺主体不存在 |
| `60037` | 400 | `DISTRIBUTOR_MERCHANT_NOT_APPROVED` | 仅审核通过的商家可管理一级代理权限 |
| `60040` | 404 | `COUPON_TEMPLATE_NOT_FOUND` | 优惠券模板不存在 |
| `60041` | 404 | `COUPON_NOT_FOUND` | 优惠券不存在 |
| `60042` | 400 | `COUPON_UNAVAILABLE` | 优惠券不可用 |
| `60043` | 400 | `COUPON_EXPIRED` | 优惠券已过期 |
| `60044` | 400 | `COUPON_ALREADY_USED` | 优惠券已使用 |
| `60045` | 400 | `COUPON_MIN_SPEND_NOT_MET` | 订单金额未达到优惠券使用门槛 |
| `60046` | 400 | `COUPON_SCOPE_MISMATCH` | 该优惠券不适用于当前商家 |
| `60047` | 400 | `COUPON_STOCK_EXHAUSTED` | 优惠券已被领完 |
| `60048` | 400 | `COUPON_CLAIM_LIMIT_EXCEEDED` | 已达到每人限领上限 |
| `60049` | 400 | `COUPON_NOT_CLAIMABLE` | 该优惠券不可领取 |
| `60050` | 400 | `COUPON_TEMPLATE_IN_USE` | 优惠券模板已发放，不可修改关键字段 |

### 70xxx（8 个）

| code | HTTP | 枚举名 | message |
| --- | --- | --- | --- |
| `70001` | 404 | `ORDER_NOT_FOUND` | 订单不存在 |
| `70002` | 409 | `ORDER_STATUS_INVALID` | 订单状态不允许操作 |
| `70003` | 409 | `ORDER_CONFLICT` | 并发冲突 |
| `70021` | 400 | `FULFILLMENT_DEADLINE_PASSED` | 已超过履约选择时限，无法再选择 |
| `70022` | 400 | `FULFILLMENT_NOT_REQUIRED` | 本单无需配送，不可选择履约方式 |
| `70023` | 400 | `FULFILLMENT_NOT_CHOSEN` | 请先选择履约方式 |
| `70024` | 400 | `FULFILLMENT_DELIVERY_INCOMPLETE` | 配送未完成，不可完成订单 |
| `70025` | 400 | `FULFILLMENT_LOCKED` | 履约方式已锁定，不可变更 |

### 71xxx（2 个）

| code | HTTP | 枚举名 | message |
| --- | --- | --- | --- |
| `71001` | 404 | `PROPERTY_FEE_NOT_FOUND` | 物业费账单不存在 |
| `71002` | 409 | `PROPERTY_FEE_DUPLICATE_PERIOD` | 该账期已存在物业费账单 |

### 75xxx（1 个）

| code | HTTP | 枚举名 | message |
| --- | --- | --- | --- |
| `75001` | 404 | `BUILDING_CHANGE_NOT_FOUND` | 楼栋变更申请不存在 |

### 80xxx（11 个）

| code | HTTP | 枚举名 | message |
| --- | --- | --- | --- |
| `80001` | 400 | `DELIVERY_GRAB_FAILED` | 快递员抢单失败 |
| `80002` | 404 | `DELIVERY_RULE_NOT_FOUND` | 配送规则不存在 |
| `80003` | 409 | `DELIVERY_RULE_DUPLICATE` | 该物业已存在配送规则 |
| `80010` | 404 | `DELIVERY_PRICE_RANGE_NOT_FOUND` | 配送价格区间不存在 |
| `80011` | 400 | `DELIVERY_PRICE_RANGE_INVALID` | 配送价格区间不合法 |
| `80012` | 400 | `DELIVERY_PRICE_RANGE_OVERLAP` | 配送价格区间重叠 |
| `80013` | 400 | `COURIER_MANAGER_DUPLICATE` | 快递负责人已存在 |
| `80014` | 400 | `COURIER_ALREADY_BOUND` | 快递员已绑定其他负责人 |
| `80015` | 404 | `COURIER_MANAGER_NOT_FOUND` | 快递负责人不存在 |
| `80020` | 400 | `DELIVERY_NOT_COURIER_ORDER` | 非配送员承运单，配送员不可抢单/操作 |
| `80021` | 404 | `DELIVERY_NOT_FOUND` | 配送单不存在 |

### 90xxx（15 个）

| code | HTTP | 枚举名 | message |
| --- | --- | --- | --- |
| `90001` | 404 | `ANNOUNCEMENT_NOT_FOUND` | 公告不存在 |
| `90002` | 400 | `VOTE_ENDED` | 投票已结束 |
| `90003` | 400 | `COLLECT_ALREADY_SUBMITTED` | 已提交过收集信息 |
| `90004` | 404 | `OPINION_BOX_NOT_FOUND` | 意见箱不存在 |
| `90005` | 400 | `OPINION_ALREADY_VOTED` | 该家庭已投过票 |
| `90006` | 400 | `OPINION_MONTHLY_LIMIT` | 每家庭每月仅可发表1条意见 |
| `90007` | 404 | `ACTIVITY_GROUP_NOT_FOUND` | 活动组不存在 |
| `90008` | 400 | `ACTIVITY_ALREADY_SUBSCRIBED` | 已订阅该活动组 |
| `90009` | 400 | `ACTIVITY_ALREADY_MEMBER` | 已是活动组成员 |
| `90010` | 400 | `ACTIVITY_NOT_MEMBER` | 不是活动组成员 |
| `90011` | 400 | `ACTIVITY_LEADER_CANNOT_LEAVE` | 组长不能退出活动组 |
| `90012` | 404 | `MESSAGE_NOT_FOUND` | 消息不存在 |
| `90013` | 400 | `NEIGHBOR_CONTACT_LIMIT_EXCEEDED` | 今日邻居私聊次数已达上限 |
| `90014` | 400 | `DIRECTED_MSG_COMPANY_DAILY_LIMIT` | 定向消息物业每日发送上限 |
| `90015` | 400 | `DIRECTED_MSG_USER_DAILY_LIMIT` | 定向消息用户每日接收上限 |

### 91xxx（8 个）

| code | HTTP | 枚举名 | message |
| --- | --- | --- | --- |
| `91001` | 404 | `COORDINATOR_NOT_FOUND` | 统筹不存在 |
| `91002` | 404 | `SECTOR_LEADER_NOT_FOUND` | 板块负责人不存在 |
| `91003` | 404 | `INDIVIDUAL_LEADER_NOT_FOUND` | 个体负责人不存在 |
| `91004` | 404 | `BUILDING_LEADER_NOT_FOUND` | 楼长不存在 |
| `91005` | 404 | `PROPERTY_MANAGER_NOT_FOUND` | 物业经理不存在 |
| `91006` | 400 | `PROPERTY_MANAGER_ALREADY_EXISTS` | 该物业公司已存在在职物业经理 |
| `91007` | 403 | `PROPERTY_MANAGER_NOT_ACTIVE` | 物业经理身份已停用 |
| `91008` | 403 | `PROPERTY_MANAGER_SELF_RATE_FORBIDDEN` | 物业经理不可修改自己的分成比例 |

### 92xxx（9 个）

| code | HTTP | 枚举名 | message |
| --- | --- | --- | --- |
| `92001` | 404 | `PERMISSION_NOT_FOUND` | 权限项不存在 |
| `92002` | 404 | `PERMISSION_PRESET_NOT_FOUND` | 角色预设不存在 |
| `92003` | 500 | `PERMISSION_CHANGE_LOG_FAILED` | 权限变更日志写入失败 |
| `92030` | 400 | `DISTRIBUTION_NOT_CONSERVED` | 分红金额不守恒 |
| `92031` | 400 | `DELIVERY_SUBSIDY_SPONSOR_INVALID` | 配送补贴出资方不合法 |
| `92032` | 400 | `DISTRIBUTION_RATE_INVALID` | 分红比例不合法 |
| `92033` | 400 | `DELIVERY_SHARE_RATE_INVALID` | 配送分成比例不合法 |
| `92034` | 400 | `DISTRIBUTION_RATE_TOTAL_INVALID` | 分红比例合计不合法 |
| `92035` | 400 | `INVALID_SHARE_RATE_TOTAL` | 分成比例合计不合法 |

### 93xxx（5 个）

| code | HTTP | 枚举名 | message |
| --- | --- | --- | --- |
| `93001` | 404 | `POINT_POOL_NOT_FOUND` | 积分池不存在 |
| `93002` | 400 | `POINT_POOL_INSUFFICIENT` | 积分池余额不足 |
| `93003` | 400 | `COIN_FROZEN` | 物业币已冻结，无法使用 |
| `93004` | 400 | `COIN_EXPIRED` | 物业币已过期 |
| `93030` | 400 | `ORDER_VERIFICATION_EXPIRED` | 订单验证已过期 |

### 94xxx（7 个）

| code | HTTP | 枚举名 | message |
| --- | --- | --- | --- |
| `94001` | 400 | `MERCHANT_POINT_PURCHASE_AUDITING` | 积分购买审核中 |
| `94002` | 400 | `MERCHANT_POINT_INSUFFICIENT` | 商家积分余额不足 |
| `94003` | 400 | `MERCHANT_WITHDRAWAL_INVALID` | 提现申请不允许操作 |
| `94004` | 400 | `COIN_WITHDRAWAL_AMOUNT_INVALID` | 兑换金额不合法 |
| `94005` | 400 | `WITHDRAWAL_BLOCKED` | 提现已被冻结 |
| `94006` | 400 | `MERCHANT_WITHDRAWAL_INSUFFICIENT` | 可提现余额不足 |
| `94007` | 400 | `PROPERTY_COIN_FUND_INSUFFICIENT` | 物业币代管资金余额不足 |

### 95xxx（2 个）

| code | HTTP | 枚举名 | message |
| --- | --- | --- | --- |
| `95001` | 404 | `FILE_NOT_FOUND` | 文件不存在 |
| `95002` | 413 | `FILE_TOO_LARGE` | 文件超过服务端大小限制 |

### 96xxx（18 个）

| code | HTTP | 枚举名 | message |
| --- | --- | --- | --- |
| `96001` | 404 | `SERVICE_NOT_FOUND` | 服务不存在 |
| `96002` | 404 | `SERVICE_ORDER_NOT_FOUND` | 服务订单不存在 |
| `96003` | 400 | `SERVICE_ALREADY_SUBSCRIBED` | 已订阅该服务 |
| `96010` | 400 | `COMMUNITY_POST_TOO_LONG` | 帖子内容过长 |
| `96011` | 404 | `COMMUNITY_POST_NOT_FOUND` | 帖子不存在 |
| `96012` | 400 | `COMMUNITY_NOT_ALLOWED_DELETE` | 该帖子不允许删除 |
| `96013` | 400 | `COMMUNITY_COMMENT_TOO_LONG` | 评论内容过长 |
| `96014` | 404 | `COMMUNITY_COMMENT_NOT_FOUND` | 评论不存在 |
| `96015` | 400 | `COMMUNITY_REPORT_DUPLICATE` | 已举报过该内容 |
| `96016` | 404 | `COMMUNITY_REPORT_NOT_FOUND` | 举报记录不存在 |
| `96017` | 400 | `COMMUNITY_REPORT_ALREADY_HANDLED` | 举报已处理 |
| `96018` | 400 | `COMMUNITY_FORBIDDEN_WORD` | 包含敏感词 |
| `96020` | 400 | `SERVICE_REQUEST_TIMEOUT` | 服务工单已超时 |
| `96030` | 400 | `COIN_MALL_DISABLED` | 物业币商城未开启 |
| `96031` | 400 | `COIN_BELOW_MIN_AMOUNT` | 低于物业币商城起购金额 |
| `96032` | 400 | `COIN_EXCEED_MAX_RATIO` | 超过物业币商城最大占比 |
| `96033` | 400 | `PREVIEW_EXPIRED` | 预览快照已过期 |
| `96034` | 400 | `CONSULTATION_APPOINTMENT_UNAVAILABLE` | 咨询预约不可用 |

### 97xxx（22 个）

| code | HTTP | 枚举名 | message |
| --- | --- | --- | --- |
| `97001` | 404 | `PROPERTY_COMPANY_NOT_FOUND` | 物业公司不存在 |
| `97002` | 403 | `PROPERTY_OPERATOR_OUT_OF_SCOPE` | 物业操作员超出权限范围 |
| `97006` | 400 | `SHARE_RATE_PRIMARY_TOTAL_INVALID` | 商品平台盘一级分成比例合计必须为100% |
| `97030` | 404 | `INDIVIDUAL_LEADER_APPLICATION_NOT_FOUND` | 个体负责人申请不存在 |
| `97031` | 400 | `INDIVIDUAL_LEADER_APPLICATION_ALREADY_AUDITED` | 该申请已审核，不可重复审核 |
| `97033` | 400 | `PROPERTY_COMPANY_HAS_RELATED_DATA` | 该物业公司存在关联数据，不可删除 |
| `97040` | 404 | `DISTRIBUTOR_APPLICATION_NOT_FOUND` | 一级代理申请不存在 |
| `97041` | 400 | `DISTRIBUTOR_APPLICATION_ALREADY_AUDITED` | 该申请已审核，不可重复审核 |
| `97042` | 400 | `DISTRIBUTOR_APPLICATION_PENDING_EXISTS` | 你已有待审核的一级代理申请，请勿重复提交 |
| `97043` | 400 | `DISTRIBUTOR_ALREADY_ENABLED` | 你已是一级代理，无需重复申请 |
| `97050` | 400 | `FORUM_BAN_ALREADY_ACTIVE` | 该用户已被禁言，无需重复操作 |
| `97051` | 404 | `FORUM_BAN_NOT_FOUND` | 禁言记录不存在 |
| `97060` | 404 | `ACTIVITY_LEADER_APPLICATION_NOT_FOUND` | 组长申请不存在 |
| `97061` | 400 | `ACTIVITY_LEADER_APPLICATION_ALREADY_AUDITED` | 该申请已审核，不可重复审核 |
| `97062` | 400 | `ACTIVITY_LEADER_APPLICATION_PENDING_EXISTS` | 您已有待审核的组长申请或已是组长，请勿重复提交 |
| `97063` | 404 | `ACTIVITY_LEADER_IDENTITY_NOT_FOUND` | 该用户没有生效中的组长身份 |
| `97070` | 400 | `POINT_FEATURE_DISABLED` | 本小区未启用积分功能 |
| `97071` | 400 | `COIN_FEATURE_DISABLED` | 本小区未启用物业币功能 |
| `97072` | 400 | `ROLE_ACCOUNT_HAS_BUSINESS_DATA` | 角色账号存在业务数据，不可删除 |
| `97073` | 400 | `ROLE_ACCOUNT_ALREADY_DELETED` | 角色账号已删除 |
| `97074` | 400 | `ROLE_ACCOUNT_PERSIST_FAILED` | 业务角色建号写库失败 |
| `97075` | 404 | `TRANSACTION_NOT_FOUND` | 交易流水不存在（`GET /admin/transactions/detail` 未命中；分类传错也走这里） |

### 98xxx（1 个）

| code | HTTP | 枚举名 | message |
| --- | --- | --- | --- |
| `98001` | 404 | `REWARD_GRANT_NOT_FOUND` | 发放申请不存在 |

### 99xxx（16 个）

| code | HTTP | 枚举名 | message |
| --- | --- | --- | --- |
| `99001` | 500 | `SYSTEM_ERROR` | 内部服务异常 |
| `99002` | 502 | `WECHAT_API_ERROR` | 微信接口异常 |
| `99003` | 400 | `WECHAT_ECOMMERCE_NOT_CONFIGURED` | 电商收付通未配置或未启用 |
| `99004` | 400 | `ORDER_PAY_UNAVAILABLE` | 该商家暂未完成微信进件，请使用积分/物业币支付 |
| `99005` | 502 | `WECHAT_CERT_NOT_READY` | 微信平台证书未就绪，无法加密敏感信息 |
| `99006` | 404 | `APPLYMENT_NOT_FOUND` | 微信进件申请不存在 |
| `99007` | 400 | `APPLYMENT_STATE_INVALID` | 进件申请状态不允许该操作 |
| `99008` | 400 | `APPLYMENT_ALREADY_EXISTS` | 该商家已存在进件申请 |
| `99009` | 502 | `APPLYMENT_UPLOAD_FAILED` | 进件资料上传微信失败 |
| `99010` | 400 | `TRANSFER_ACCOUNT_NOT_VERIFIED` | 收款账号未核验，无法转账 |
| `99011` | 404 | `TRANSFER_NOT_FOUND` | 转账单不存在 |
| `99012` | 400 | `TRANSFER_AMOUNT_INVALID` | 转账金额不合法 |
| `99013` | 404 | `RECOVERY_NOT_FOUND` | 待追回台账记录不存在 |
| `99014` | 409 | `SPLIT_REVERSE_INCOMPLETE` | 订单分账尚未全部回退完成，暂不能发起退款 |
| `99015` | 400 | `REFUND_NOT_ENABLED` | 当前未开放用户主动申请退款，如有问题请联系物业 |
| `99016` | 502 | `WECHAT_APPLYMENT_NOT_CONFIGURED` | 微信进件未配置（服务商凭据或结算规则缺失） |

---

# 附录三 · 失效错误码对照（历史文档标注 → 现行实现）

> 下列码**在代码中不存在**，接口不会返回。前端本地码表若按旧文档映射，会把这些场景全部落到「未知错误」。
> 迁移原因：号段与真实功能错配（例如旧文档把 `97010`/`97011` 标给「订单已完成分账」「配送已完成结算」，而这两个码从未实现，2026-11-02 已把角色账号相关码迁到 `97072`+）。

| 文档旧码 | 旧文档标注含义 | 现行返回 | 备注 |
| --- | --- | --- | --- |
| `20008` | 物业操作员越权（§54.5） | `97002` / `20004` | PROPERTY_OPERATOR_OUT_OF_SCOPE / PERMISSION_DENIED |
| `40101` | 鉴权失败（§0.9 CORS 段） | `20001` / `20002` / `20003` | Token 缺失 / 过期 / 无效 |
| `41001` | 统筹不存在或非 active（§39） | `91001` | COORDINATOR_NOT_FOUND |
| `40003` | 积分功能未开启 | `97070` | v8.32 已改，本表复核 |
| `50006` | 物业币功能未开启 | `97071` | v8.32 已改，本表复核 |
| `50003` | 该物业未开启物业币抵扣 | `96030` | COIN_MALL_DISABLED |
| `50004` | 未达物业币最低使用金额 | `96031` | COIN_BELOW_MIN_AMOUNT |
| `50005` | 超过物业币最大抵扣比例 | `96032` | COIN_EXCEED_MAX_RATIO |
| `60015` | 审核时可提现余额不足（§34） | `94006` | MERCHANT_WITHDRAWAL_INSUFFICIENT |
| `60016` | 业主商户未营业 / 非 active（§60） | `60010` | message 为「业主商户未营业」 |
| `70004` | 当前订单不支持用户主动退款 | `99015` | REFUND_NOT_ENABLED |
| `80004` | 快递负责人不存在（§62） | `80015` | COURIER_MANAGER_NOT_FOUND |
| `80005` | 快递负责人重复（§62） | `80013` | COURIER_MANAGER_DUPLICATE |
| `80006` | 快递员已绑定其它负责人（§62） | `80014` | COURIER_ALREADY_BOUND |
| `80007` | 配送价格区间不存在（§62.10） | `80010` | DELIVERY_PRICE_RANGE_NOT_FOUND |
| `80008` | 配送价格区间重叠（§62.11/62.12） | `80012` | DELIVERY_PRICE_RANGE_OVERLAP |
| `80009` | 配送价格区间参数非法（§62.11/62.12） | `80011` | DELIVERY_PRICE_RANGE_INVALID（格式类问题走 `10002`） |
| `80022` | 该子商家已被该主商家绑定（§73） | `60026` | CANTEEN_BINDING_EXISTS |
| `80023` | 绑定关系不存在（§73） | `60027` | CANTEEN_BINDING_NOT_FOUND |
| `80024` | 充值额度余额不足（§73） | `60022` | CANTEEN_QUOTA_INSUFFICIENT |
| `80025` | 定向余额不足（§73） | `60024` | CANTEEN_BALANCE_INSUFFICIENT |
| `80026` | 所选主商家体系与订单商家不匹配（§73） | `10002` / `60023` | 无独立码，以 `message` 为准 |
| `80027` | 使用了定向余额但未选主商家体系（§73） | `10001` / `10002` | 无独立码，以 `message` 为准 |
| `80028` | 存在待支付的额度购买（§73） | `60020` | CANTEEN_PURCHASE_PENDING |
| `80029` | 额度购买记录不存在（§73） | `60021` | CANTEEN_QUOTA_PURCHASE_NOT_FOUND |
| `90101` | 同人每日定向接收上限（§52） | `90015` | DIRECTED_MSG_USER_DAILY_LIMIT |
| `90102` | 物业每日全局任务上限（§52） | `90014` | DIRECTED_MSG_COMPANY_DAILY_LIMIT |
| `90103` | 今日新聊人数已达上限（§56.1） | `90013` | NEIGHBOR_CONTACT_LIMIT_EXCEEDED |
| `90104` | 服务需求接单超时（§57.8） | `96020` | SERVICE_REQUEST_TIMEOUT |
| `90105` | 商家广告本周额度不足（§59.2） | `60011` | MERCHANT_AD_QUOTA_INSUFFICIENT |
| `90106` | 业主商户保证金或状态不允许（§60） | `60010` | RESIDENT_MERCHANT_DEPOSIT_INVALID |
| `90107` | 业主商户存在进行中订单无法退出（§60.4） | `60028` | RESIDENT_MERCHANT_HAS_ORDERS |
| `90108` | 服务需求无可用商家已关闭（§57） | 无错误码 | 需求置 `closed` + `closedReason=暂无可用商家，请稍后重试`；接单侧按 `96020`/`10002` |
| `90109` | 物业管理员超出管辖范围（§31/§54） | `97002` | PROPERTY_OPERATOR_OUT_OF_SCOPE |
| `90110` | 咨询预约不可用（§58） | `96034` | CONSULTATION_APPOINTMENT_UNAVAILABLE |
| `90111` | 业主商户未对外展示（§60.9.1） | `60029` | RESIDENT_MERCHANT_NOT_PUBLIC |
| `90120` | 角色账号存在业务数据不可删除（§31.2.3） | `97072` | 2026-11-02 从 97010 号段迁入 |
| `90121` | 账号已删除，无法再次操作（§31.2.3） | `97073` | ROLE_ACCOUNT_ALREADY_DELETED |
| `97003` | 微信订阅消息模板未配置（§15 催缴） | 无此码 | 代码中不存在；模板缺失按应用内消息回退，以 `message` 为准 |
| `97004` | 催缴预览快照过期（§15） | `96033` | PREVIEW_EXPIRED |
| `97005` | 重复发送催缴（§15） | `10002` | message「该 requestId 已发送过，请勿重复提交」 |
| `97007` | 配送费抽成比例不合法（§7.6） | `92033` | DELIVERY_SHARE_RATE_INVALID |
| `97008` | 满额门槛必须大于 0（§7.6） | `10002` | 无独立码 |
| `97009` | 订单金额快照已失效（§8.0） | `96033` | PREVIEW_EXPIRED（预览/快照类统一） |
| `97010` | 订单已经完成分账（§71.5/970xx 表） | 无此码 | **从未实现**；重复结算幂等处理。该号段现由角色账号 `97072`~`97074` 使用 |
| `97011` | 配送已经完成结算（§71.5/970xx 表） | 无此码 | **从未实现**；重复结算幂等处理 |
| `97012` | 配送补贴承担方不合法（§7.6） | `92031` | DELIVERY_SUBSIDY_SPONSOR_INVALID |
| `97013` | 商家预计收入不足以覆盖配送补贴（§7.6） | 无此码 | 后端不做该前置校验；`merchantShare` 可为负并在分账明细里原样返回 |
| `97014` | 分账金额不守恒（§7.6/§21） | `92030` | DISTRIBUTION_NOT_CONSERVED |
| `97015` | 退款冲正已处理（§7.6） | 无此码 | 冲正幂等直接返回成功，不产生第二条负向流水 |
| `97016` | 分账比例不合法（§7.6） | `92032` / `92034` / `97006` | DISTRIBUTION_RATE_INVALID / 合计不合法 / 一级三档合计 ≠ 100% |
| `97017` | 催缴消息落库/审计写入失败（§15） | `99001` | SYSTEM_ERROR（HTTP 500）。`97074` 现义为「业务角色建号写库失败」 |
| `98002` | 评论不存在（§社区论坛） | `96014` | COMMUNITY_COMMENT_NOT_FOUND |
| `98003` | 举报记录不存在（§社区论坛） | `96016` | COMMUNITY_REPORT_NOT_FOUND |
| `98004` | 已举报过该内容（§社区论坛） | `96015` | COMMUNITY_REPORT_DUPLICATE |
| `98005` | 命中敏感词（§社区论坛） | `96018` | COMMUNITY_FORBIDDEN_WORD |
| `98006` | 非作者/不允许删除帖子（§社区论坛） | `96012` | COMMUNITY_NOT_ALLOWED_DELETE |
| `98007` | 帖子内容过长（§社区论坛） | `96010` | COMMUNITY_POST_TOO_LONG |
| `98008` | 评论内容过长（§社区论坛） | `96013` | COMMUNITY_COMMENT_TOO_LONG |
| `98009` | 该举报已处理（§社区论坛） | `96017` | COMMUNITY_REPORT_ALREADY_HANDLED |

## 同码不同义（高危，务必核对本地码表）

| 码 | 旧文档含义 | 现行 `ErrorCode.java` 含义 | 前端应改为 |
| --- | --- | --- | --- |
| `98001` | 帖子不存在（§社区论坛） | `REWARD_GRANT_NOT_FOUND` — 奖励金发放申请不存在（§`/reward-grants`） | 帖子/评论/举报一律用 `96010`~`96018`（帖子不存在 = `96011`） |
| `97010` | 订单已经完成分账 | `ROLE_ACCOUNT_HAS_BUSINESS_DATA`（角色账号存在业务数据，不可删除）——注意：真正的角色账号码已在 `97072`，`97010` **当前未被使用** | 重复结算场景按幂等成功处理，不要监听 `97010` |
| `97011` | 配送已经完成结算 | 未被使用 | 同上 |
| `97070`/`97071` | —（v8.32 新增） | `POINT_FEATURE_DISABLED` / `COIN_FEATURE_DISABLED`，文案「本小区未启用积分功能 / 物业币功能」 | 按小区开关提示，不要再匹配 `40003`/`50006` |

**不是错误码、无需映射的两项**：`429001` 真实有效（`RateLimitFilter` 写出，不在枚举内，见 §0.10）；`43101` 是**微信侧 errcode**（订阅消息需重新授权），出现在联调日志里但不是本系统响应 `code`。
