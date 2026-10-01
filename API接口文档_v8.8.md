# 物业积分服务系统 — API 接口文档

> **版本**：v8.8
> **Base URL**：`https://test.yuanxusoftware.top/api/v1`
> **维护方**：连云港元序软件有限公司
> **文档说明**：本文件为**当前最新完整接口文档**（已去除历史版本迭代说明，仅保留最新契约）。

# 目录

- [快速开始](#快速开始)
- [认证与鉴权](#认证与鉴权)
- [统一响应格式](#统一响应格式)
- [分页规范](#分页规范)
- [错误码体系](#错误码体系)
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

---



# 快速开始



## 1. 获取 Token

```bash
# 微信小程序登录
curl -X POST https://test.yuanxusoftware.top/api/v1/auth/wechat-login \
  -H "Content-Type: application/json" \
  -d '{
    "code": "wx_login_code_here",
    "propertyCompanyId": "pc_demo001"
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
| `code`              | `String` | ✅   | `@NotBlank @Size(max=256)` | 微信登录授权码            |
| `encryptedData`     | `String` | ❌   |                            | 加密数据（获取手机号时使用）     |
| `iv`                | `String` | ❌   | `@Size(max=24)`            | 解密向量               |
| `propertyCompanyId` | `String` | ❌   | `@Size(max=32)`            | 物业公司ID（新用户首次登录时必传） |
| `communityId`       | `String` | ❌   | `@Size(max=32)`            | 小区ID（注册时选择的小区，可选；需与 `propertyCompanyId` 归属一致，否则返回 10002） |


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
| `30003` | 400  | 微信授权失败 |
| `20005` | 403  | 账号被禁用  |


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
    "phone": "13800138000",
    "password": "admin123456"
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

**错误响应**：


| 错误码     | HTTP | 说明       |
| ------- | ---- | -------- |
| `30001` | 404  | 手机号或密码错误 |
| `20005` | 403  | 账号被禁用    |


**初始管理员账号**：


| 手机号           | 密码            | 角色 `role`        | 子角色 `propertySubRole` | 说明   |
| ------------- | ------------- | ---------------- | ---------------------- | ---- |
| `13800138000` | `admin123456` | `platform_admin` | — | 平台超管 |


**物业角色层级联调账号**（`POST /auth/admin-login`，密码均为 `admin123456`）：


| 手机号           | 用户ID | `role` | `propertySubRole` | 说明 |
| ------------- | ------ | ------ | ----------------- | ---- |
| `13900000020` | `res_pleader01` | `property_admin` | `property_leader` | 物业领导（全公司权限） |
| `13900000021` | `res_poperator01` | `property_admin` | `property_operator` | 物业管理员（可限小区/楼栋） |


**三角色联调测试账号**（`POST /auth/admin-login`，密码均为 `admin123456`）：


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
    "propertyCompanyId": "pc_demo001",
    "propertyName": "演示物业公司",
    "communityId": "com_demo001",
    "communityName": "演示小区",
    "building": "3栋",
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

**错误响应**：


| 错误码     | HTTP | 说明       |
| ------- | ---- | -------- |
| `20004` | 403  | 权限不足（角色不在 Roles 列表内） |

> **三角色说明**：`merchant`、`courier`、`activity_leader` 调用本接口将返回 `20004`，**属预期行为**。三角色客户端登录后**不应**调用本接口；用户信息与路由所需字段以 `POST /auth/admin-login` 响应中的 `resident` 对象为准（含 `id`、`role`、`propertySubRole`、`propertyCompanyId` 等）。

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

---



### 2.5 GET `/residents/me` — 获取当前用户详情

**Auth**: Required  
**Roles**: `resident`, `property_admin`, `platform_admin`

**成功响应**：同 `/auth/profile`，额外包含 `totalConsumption`、`totalOrders`。

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
- 积分功能关闭（`property_companies.point_enabled=false`，§51）时返回 `97020`「积分功能未开启」
- 个人积分余额不足返回 `40001`

**错误响应**：

| 错误码   | HTTP | 说明            |
| ----- | ---- | ------------- |
| `40001` | 400  | 积分余额不足        |
| `97020` | 400  | 积分功能未开启（原文档误写 `40003`，实际返回 `97020`） |
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
- 家庭余额不足返回 `50001`；物业币功能关闭（`property_companies.coin_enabled=false`，§51）时返回 `97021`「物业币功能未开启」；物业币被冻结返回 `93003`

**错误响应**：

| 错误码   | HTTP | 说明            |
| ----- | ---- | ------------- |
| `50001` | 400  | 物业币余额不足       |
| `97021` | 400  | 物业币功能未开启（原文档误写 `50006`，实际返回 `97021`） |
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
    "arrearsAmount": 0.00
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

> **说明**：详情响应新增 `hasArrears`/`arrearsCount`/`arrearsAmount` 三字段，与列表 §2.1 一致。

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
| `unit`          | `String`  | ❌   | `@Size(max=20)`     | 单元（仅管理端角色可改）                               |
| `room`          | `String`  | ❌   | `@Size(max=20)`     | 房号（仅管理端角色可改）                               |
| `role`          | `String`  | ❌   | 仅管理端角色可传        | 角色变更（管理端专用，如 `activity_leader`）。改为 `activity_leader` 时自动开通组长小店（§18 能力总览），幂等 |


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
| `propertyCompanyId` | `String` | ❌   |              | 物业公司ID                         |
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
        "distanceKm": 1.2,
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
| `propertyCompanyId` | `String` | ❌   |              | 物业公司ID                                                |
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
| `list[].coinRebateRate`        | `BigDecimal`   | 物业币返利率                                                  |
| `list[].coinRebateEnabled`     | `Boolean`      | 是否启用物业币返利                                               |
| `list[].rankOrder`             | `Integer`      | 排序权重                                                    |
| `list[].auditStatus`           | `String`       | 审核状态：`pending_audit`/`approved`/`rejected`              |
| `list[].status`                | `String`       | 状态：`active`/`frozen`/`disabled`                         |
| `list[].isRecommended`        | `Boolean`      | 是否官方/物业推荐（用于区分「物业推荐」列表与「官方全部/物业公司模块」） |
| `list[].recommendedSort`      | `Integer`      | 推荐排序（越大越靠前）                                       |
| `list[].merchantSource`        | `String`       | 商家来源：`platform`（平台）/ `group_leader`（组长店）/ `technician`（技师店）；前端按此字段排除商家 / 展示活动组（组长店为 `group_leader`） |
| `list[].createdAt`             | `String`       | 创建时间                                                    |


---



### 7.2 GET `/merchants/my` — 我的商家

**Auth**: Required  
**Roles**: `merchant`、`activity_leader`（组长店，`merchantSource=group_leader`）、`technician`（技工店，`merchantSource=technician`）

> **组长/技工说明**：组长（`activity_leader`）/技工（`technician`）由系统自动绑定自己的店铺（按 `resident_id + 物业 + merchantSource` 解析，优先 `resident_id`，回退手机号）。**组长账号任命（§2.8 改 `role=activity_leader`）时立即自动创建** `merchant_source=group_leader` 小店（免审核：`auditStatus=approved`、`status=active`、店铺名=`{姓名或手机号}的小店`、分类=`活动组`、`deliveryFee=0`、`serveAllCommunities=true`、默认抽佣 0.1）；本接口可直接读取店铺配置，含 `deliveryFee` / `freeDeliveryThreshold` / `deliveryScope`。店铺未绑定/不存在时返回 `60001`（商家不存在）。

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

> ⚠️ **角色限制**：本接口仅对 `merchant` / `activity_leader` / `technician`（即**已通过审核**）角色开放。`resident`（提交入驻申请但**尚未通过**）调用会返回 `403`。住户查询自己"商家入驻申请"进度，请使用 §7.2.2 `GET /merchants/my-application`。

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
    "merchantSource": "group_leader",
    "totalOrders": 120,
    "totalRevenue": 12500.00,
    "withdrawableAmount": 8000.00,
    "pendingAmount": 0.00,
    "frozenAmount": 0.00,
    "receivableAmount": 0.00,
    "totalWithdrawn": 3000.00,
    "withdrawalBlocked": false,
    "createdAt": "2026-03-15 10:00:00"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

> **补充**：当 `MerchantPropertyConfig` 记录不存在时（新建商家），`commissionRate`/`pointExchangeRate` 不再返回 null，统一返回 `0`。`deliveryScope`、`commissionRate`、`pointExchangeRate`、`totalOrders`、`totalRevenue`、`withdrawableAmount`、`totalWithdrawn`、`withdrawalBlocked` 等字段已定义。



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

> ℹ️ **住户查询自己申请进度的正确入口**：`GET /merchants/my`（§7.2，商户端接口，对 `resident` 返回 403）**不能**用于住户查询。住户应使用 §7.2.2 `GET /merchants/my-application` 读取自己真实的商家入驻申请状态（`auditStatus`：`none`/`pending_audit`/`approved`/`rejected`/`kicked`/`closed`/`stopped`）。

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

### 7.6 满额配送费减免规则（distribution_b_v1）

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

**请求体**（`PreviewOrderRequest`）：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `merchantId` | `String` | ✅ | 商家 ID |
| `items` | `Array` | ✅ | 商品列表（同创建订单：`productId`、`quantity`） |
| `coinUsed` | `BigDecimal` | ❌ | 期望抵扣的物业币金额 |
| `pointUsed` | `Integer` | ❌ | 期望使用的积分 |
| `familyId` | `String` | ❌ | 使用家庭物业币池时传入 |

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
| `pointUsed` | `Integer` | 使用积分 |
| `cashAmount` | `BigDecimal` | 现金应付金额 |
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

对业主商户上架商品（`listingId`）试算可抵扣金额与现金应付，口径与下单 `POST /orders/from-listing` 一致（商品金额=零售价×数量，运费按一级经销商配送规则）。上架商品无商城 `productId`，**不可走 `POST /orders/preview`**。仅试算不落库，支付仍以下单后 `POST /orders/{id}/pay` 后端校验为准。

**请求体**（`PreviewListingOrderRequest`）：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `listingId` | `String` | ✅ | 业主上架记录 ID（`rml_` 前缀，见 `GET /resident-merchants/public-listings`/公开橱窗） |
| `quantity` | `Integer` | ❌ | 数量，默认 1 |
| `coinUsed` | `BigDecimal` | ❌ | 期望抵扣的物业币金额 |
| `pointUsed` | `Integer` | ❌ | 期望使用的积分 |
| `familyId` | `String` | ❌ | 使用家庭物业币池时传入 |

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

**响应**：字段与 `POST /orders/preview`（§8.0）完全一致（`PreviewOrderResponse`：`totalAmount`/`subtotal`/`productAmount`/`deliveryFee`/`originalDeliveryFee`/`deliveryWaiverAmount`/`deliverySubsidySponsor`/`deliverySubsidyAmount`/`deliverySettlementBase`/`freeDeliveryEligible`/`waiverReason`/`maxCoinAllowed`/`coinUsed`/`pointUsed`/`cashAmount`/`reasonIfCapped`/`directedBalances`）。

**错误响应**：

| 错误码 | HTTP | 说明 |
| --- | --- | --- |
| `10002` | 400 | `listingId` 非 `rml_` 前缀 |
| `60004` | 404 | 上架商品/货源商品不存在 |
| `70002` | 409 | 该商品已下架 / 货源商品已停售 |
| `60001` | 404 | 货源商家不存在 |

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
| `pointUsed`     | `Integer`    | ❌   | `@Min(0)`   | 使用积分                                     |
| `coinUsed`      | `BigDecimal` | ❌   | `@Min(0)`   | 使用物业币                                    |
| `mainMerchantId` | `String`    | ❌   |             | 社区食堂定向余额所属**主商家体系ID**（`mixed` 且使用定向余额时必传，须在 `POST /orders/preview` 返回的 `directedBalances` 中） |
| `directedBalanceUsed` | `BigDecimal` | ❌ | `@Min(0)` | 社区食堂**定向余额抵扣金额**（`mixed` 且使用定向余额时必传，须 ≤ 该体系余额；与 `mainMerchantId` 成对传入，否则返回 `10002`） |
| `wechatPayCode` | `String`     | ❌   |             | 微信支付code                                 |
| `familyId`      | `String`     | ❌   |             | 使用家庭余额                                   |
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
    "cashAmount": 0.00,
    "paidAt": "2026-07-01 15:24:00",
    "pointEarned": 10,
    "coinEarned": 0.30
  },
  "timestamp": "2026-07-01 15:24:00"
}
```

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
        "deliveryStatus": "待抢单",
        "deliveryStatusCode": "pending",
        "deliveryStatusLabel": "待抢单",
        "pointUsed": 100,
        "coinUsed": 5.00,
        "cashAmount": 0.00,
        "orderStatus": "已支付",
        "orderStatusCode": "paid",
        "orderStatusLabel": "已支付",
        "deliveryAddress": "3栋5单元502",
        "contactPhone": "13800138000",
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
| `orderStatus` | `String` | **展示用中文**（已支付/配送中等） |
| `orderStatusCode` | `String` | 订单状态 code |
| `orderStatusLabel` | `String` | 与 `orderStatus` 相同 |
| `deliveryId` | `String` | 配送单 ID |
| `deliveryStatus` | `String` | **展示用中文**（待抢单/已完成等） |
| `deliveryStatusCode` | `String` | 配送状态 code |
| `deliveryStatusLabel` | `String` | 与 `deliveryStatus` 相同 |
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
    "orderStatus": "已支付",
    "orderStatusCode": "paid",
    "orderStatusLabel": "已支付",
    "deliveryAddress": "3栋5单元502",
    "contactPhone": "13800138000",
    "remark": "请尽快配送",
    "courierId": null,
    "courierName": null,
    "deliveryId": "dlv_demo001",
    "deliveryStatus": "待抢单",
    "deliveryStatusCode": "pending",
    "deliveryStatusLabel": "待抢单",
    "paidAt": "2026-07-01 15:24:00",
    "completedAt": null,
    "cancelledAt": null,
    "createdAt": "2026-07-01 15:23:33",
    "updatedAt": "2026-07-01 15:24:00"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**详情字段**：与 §8.4 列表项相同，另含 `remark`、`courierName`、`updatedAt` 等；`orderStatus`/`paymentMethod`/`deliveryStatus` 均为**中文展示**，逻辑用 `*Code`。金额字段语义与列表一致（§8.4 字段表），住户视角至少返回 `productAmount`/`originalDeliveryFee`/`deliveryFee`/`deliveryWaiverAmount`/`totalAmount`/`freeDeliveryEligible`/`waiverReason`；商家与管理员视角额外返回 `deliverySubsidySponsor`/`deliverySubsidyAmount`/`merchantGoodsShare`/`merchantDeliverySubsidy`/`merchantShare`/`distributionStatus`。

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

> **待抢单/可抢单列表项字段**（`PendingDeliveryItemResponse`）：`id`、`orderId`、`orderNo`、`merchantId`、`merchantName`、`merchantAddress`、`pickupAddress`、`deliveryAddress`、`contactPhone`、`fee`（= 用户实际支付配送费）、`originalDeliveryFee`（满额减免前基础配送费）、`deliverySubsidySponsor`、`deliverySubsidyAmount`、`deliverySettlementBase`（= 配送费 + 补贴）、`platformDeliveryShare`（公司配送收入）、`courierEarning`（配送员最终收入 = 基数 − 公司配送收入）、`settlementStatus`（`pending`/`settled`/`reversed`）、`status`、`timeoutMinutes`、`createdAt`。

> **v6.6 变更说明**：待抢单列表查询条件扩展为匹配两类订单的配送单（`delivery.status = pending`）：
> 1. **`order.orderStatus = paid`**（订单已支付，尚未选择履约方式，等待商家决策）；
> 2. **`order.orderStatus = delivering` AND `order.fulfillmentMode = courier_hall`**（商家已选择平台配送，配送单已创建但尚未被骑手抢单）。
>
> 此前仅匹配第 1 类，导致非团购订单商家选择「发布到抢单大厅」后（订单状态变为 `delivering`），配送单不会出现在待抢单列表中。现在两类均会展示。

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
| `status`   | `String` | ❌   |              | `accepted`/`delivering`/`completed`/`cancelled` |
| `sort`     | `String` | ❌   | `-createdAt` | 排序                                              |


**列表项字段（补充）**：


| 字段                 | 类型             | 说明                              |
| ------------------ | -------------- | ------------------------------- |
| `proofImageUrls`   | `List<String>` | 完成配送时上传的凭证 URL（仅 `completed` 有值） |
| `completeRemark`   | `String`       | 完成配送备注                          |


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
        "settlementStatus": "pending",
        "status": "accepted",
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


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "dlv_demo001",
    "orderId": "ord_demo001",
    "status": "completed",
    "fee": 0.30,
    "originalDeliveryFee": 0.30,
    "deliverySubsidySponsor": "merchant",
    "deliverySubsidyAmount": 0.00,
    "deliverySettlementBase": 0.30,
    "platformDeliveryShare": 0.03,
    "courierEarning": 0.27,
    "settlementStatus": "settled",
    "deliveredAt": "2026-07-01 15:40:00",
    "proofImageUrls": ["https://test.yuanxusoftware.top/api/v1/static/proof/xxx.jpg"],
    "remark": "已送达"
  },
  "timestamp": "2026-07-01 15:40:00"
}
```

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

**Query 参数**：`page`、`pageSize`、`propertyCompanyId`（同 §9.8）

**响应**：同 §9.8 `CourierOptionResponse` 分页。

---

### 10.5.4 GET `/admin/delivery-orders` — 配送相关订单列表（管理端）

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

分页查询配送相关订单（底层与订单列表同源筛选）。

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
| `category` | `String`        | ✅   | `avatar`/`announcement`/`merchant`/`service`/`activity`/`product` |


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
> **服务端拦截（2026-08）**：`pointEnabled=false` / `coinEnabled=false` 时，后端**写接口拒绝执行**（不仅靠前端隐藏），包括：支付抵扣（积分抵扣物业费、订单/预约/订阅/服务需求等积分或物业币支付）、转给物业（§2.6.4）、商家购分/赠分（§22）、建设积分调整（§78）、物业币充值/兑换、物业币冻结解冻之外的发放消费等，分别返回 `40003`（积分功能已停用）/ `50006`（物业币功能已停用）。
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
> 2. 创建成功后初始管理员账号自动写入初始密码 **`admin123456`**（BCrypt 落库），可直接 `POST /auth/admin-login` 登录；密码仅在创建响应的 `adminAccount.initialPassword` 中返回一次，详情接口不返回。
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
      "initialPassword": "admin123456"
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
**Roles**: `platform_admin`

**请求体新增字段**：

| 字段 | 类型 | 校验 | 说明 |
| --- | --- | --- | --- |
| `deliveryPerKgFee` | BigDecimal | @DecimalMin(0) | 按重量加价（元/kg） |
| `pointEnabled` | Boolean |  | 积分功能开关（false 时前端隐藏积分全部能力，缺省 true） |
| `coinEnabled` | Boolean |  | 物业币功能开关（false 时前端隐藏物业币全部能力，缺省 true） |
| `pointDisplayEnabled` | Boolean |  | 积分卡片是否全局展示（缺省 true） |
| `coinDisplayEnabled` | Boolean |  | 物业币卡片是否展示（缺省 true） |
| `pointExchangeRate` | BigDecimal | @DecimalMin(0) | 物业级积分兑换比（1元=X积分），默认 `100.00`（100积分=1元）。**仅作展示/参考**：订单积分支付与返积分实际按商家挂接配置 `merchant_property_configs.point_exchange_rate` 计算；物业费抵扣/积分相关支付按 `point_to_fee_rate` 计算 |
| `residentPointShareRate` | BigDecimal(5,4) | @DecimalMin(0) @DecimalMax(1) | 业主积分分成比例 |
| `merchantPointShareRate` | BigDecimal(5,4) | @DecimalMin(0) @DecimalMax(1) | 商家积分分成比例 |
| `coinPointShareRate` | BigDecimal(5,4) | @DecimalMin(0) @DecimalMax(1) | 物业币积分分成比例 |
| `sharedPointShareRate` | BigDecimal(5,4) | @DecimalMin(0) @DecimalMax(1) | 共享积分分成比例 |

> **后端校验**：`residentPointShareRate + merchantPointShareRate + coinPointShareRate + sharedPointShareRate` 必须恰好等于 `1.0`（即 100%，容差 0.0001）；任一字段 < 0 或 > 1 返回 `97002 INVALID_SHARE_RATE_TOTAL`；四项合计 ≠ 100% 同样返回 `97002 INVALID_SHARE_RATE_TOTAL`。（替代原「合计 ≤ 0.30」约束）

> **禁止直改分成**：四个分成字段（`residentPointShareRate`/`merchantPointShareRate`/`coinPointShareRate`/`sharedPointShareRate`）禁止物业侧直改——`property_admin`（含领导/操作员）请求体携带任一字段返回 `20004`（403）「分成比例调整需通过价格审批流程，禁止直接修改」；仅 `platform_admin` 可直改（运维兜底）。分成调整请走 §65 价格审批。

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

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

> **`wechat`/`mixed` 含现金部分说明**：成功响应返回 `paymentRecordId` + `payParams`（`wx.requestPayment`），此时账单 `paymentStatus` 仍为 `unpaid`（待微信支付），**属预期，勿当失败**；支付成功后由回调/查询兜底确认，`paymentStatus` 变更为 `paid`。

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

- 本接口**一步完成订阅 + 支付**：`property_coin`/`point` 余额充足直接生效（`status=active`）；`wechat` 走微信预下单。
- `wechat`：成功响应返回 `paymentRecordId` 与 `payParams`，订阅记录保持 `pending`，**支付成功后由微信支付回调/查询兜底确认**，`status` 变更为 `active`；前端勿把 `pending` 当失败，轮询 `GET /services/my-subscriptions`。

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
    "status": "pending",
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

> `payParams` 字段原样传给 `wx.requestPayment(...)`；`status=pending` 时订阅尚未生效，生效由回调/兜底确认。

**错误响应**：

| 错误码 | HTTP | 说明 |
| --- | --- | --- |
| `10002` | 400 | 参数无效（`paymentMethod` 错误、`subType` 无效、混合支付金额不足等） |
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
- `wechat`：后端创建订单后 `orderStatus` 为 `pending`（待微信支付），成功响应含 `paymentRecordId` 与 `payParams`（前端 `wx.requestPayment`）；支付成功后由回调/查询兜底确认，`orderStatus` 变更为 `confirmed`。属预期，**勿当作接口失败**。
- `mixed`：须传 `pointUsed`、`coinUsed`，且积分折算 + 物业币 + 现金部分须覆盖应付金额，否则返回 `10002`（混合支付金额不足）。
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

**组长如何产生**：

| 方式 | 说明 |
| --- | --- |
| 管理端任命 | 管理端调用 §2.8 `PUT /residents/{id}`，将 `role` 改为 `activity_leader`。任命时**立即自动开通**组长小店（见下） |
| 创建活动组 | 任意 `resident` 调用 §18.4 `POST /activity-groups` 创建活动组后即成为该组组长（`group.leaderId`=创建者、成员 `role=leader`）；但账号角色仍为 `resident`，`my-led`、小店等组长完整能力需管理端将角色改为 `activity_leader` 后才可用 |

**任命即开通组长小店（免审核）**：

- 触发时机：管理端将住户 `role` 改为 `activity_leader` 时**立即**创建（幂等：已存在则跳过）；历史兜底：组长首次访问商品 / 提现 / 我的商家接口时也会自动创建
- 店铺参数：
  - `merchantSource = group_leader`（组长小店）
  - 店铺名 = `{姓名或手机号}的小店`；分类 `category = 活动组`
  - `auditStatus = approved`（**免审核**）、`status = active`、`deliveryFee = 0`、`serveAllCommunities = true`
  - 默认抽佣 `commissionRate = 0.1000`、积分兑换 `pointExchangeRate = 100`
- 收入结算：组长店商品走 §6 `/products` 主商城，订单完成收入进入组长店钱包，可走 §23 商家提现链路提现

**组长管理活动组接口清单**：

| 能力 | 接口 | 说明 |
| --- | --- | --- |
| 我管理的组列表 | §18.2.1 `GET /activity-groups/my-led` | 仅返回本人担任组长的活动组 |
| 创建活动组 | §18.4 `POST /activity-groups` | 创建者即该组组长 |
| 更新活动组 | §18.10 `PUT /activity-groups/{id}` | 仅本人担任组长的组（否则 `20004`） |
| 成员列表 | §18.5 `GET /activity-groups/{id}/members` | 仅本人担任组长的组（否则 `20004`） |
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
- 组长不可退出自己组长的活动组（`90011` 组长不能退出活动组）；移除成员时也不可移除组长本人
- 组长店身份解析按 `resident_id + 物业 + merchantSource=group_leader` 三重匹配

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
**Roles**: `activity_leader`

**Description**: 活动组组长专用。返回当前登录用户作为 `leaderId` 的活动组列表（状态为 `active`），响应格式与 `GET /activity-groups` 列表项一致。

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

> **组长说明**：创建者自动成为该组组长（`group.leaderId`=创建者、成员 `role=leader`）。创建活动组**本身不会**自动开通组长小店——小店在管理端将账号角色改为 `activity_leader` 时开通（见本模块「活动组组长能力总览」）。组长账号（`activity_leader`）同样可调用本接口创建新组并成为其组长。

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
| `status`         | `String`     | 订阅状态，一般为 `active`    |
| `paidAt`         | `String`     | 支付时间（ISO-8601 UTC）   |
| `pricingTierId`  | `String`     | 课时价格ID，无课时为 `null`   |
| `tierName`       | `String`     | 课时名称，无课时为 `null`     |
| `tierCode`       | `String`     | 课时编码，无课时为 `null`     |
| `paymentRecordId` | `String`    | 支付流水ID（`pr_`，`wechat` 场景返回） |
| `payParams`      | `Object`     | 微信小程序调起支付参数（`wechat` 场景返回，`wx.requestPayment`） |


**业务说明**：

- 订阅成功后会**自动加入**活动组（若当前不是活跃成员）
- `wechat`：成功响应返回 `paymentRecordId` 与 `payParams`（`wx.requestPayment`），订阅 `status=pending`，**支付成功后由回调/查询兜底确认**变更为 `active`；前端勿把 `pending` 当失败，轮询 `GET /activity-groups/{id}/subscription-status`
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


**常见联调错误**：


| 现象                 | 原因                        | 正确写法                                     |
| ------------------ | ------------------------- | ---------------------------------------- |
| `参数校验失败：支付方式不能为空`  | 未传 `paymentMethod` 或字段名错误 | 请求体包含 `"paymentMethod": "property_coin"` |
| `subType值无效`       | 使用了 `MONTHLY`/`YEARLY` 大写 | 使用小写 `"monthly"` / `"yearly"`            |
| `paymentMethod值无效` | 使用了 `coin`                | 使用 `property_coin`                       |


---



### 18.6.1 POST `/activity-groups/{id}/unsubscribe` — 取消订阅活动组

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
**Roles**: `coordinator`, `sector_leader`, `individual_leader`, `property_admin`, `platform_admin`

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
        "calculationVersion": "distribution_b_v1",
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
| `productBase` | `BigDecimal` | 商品实付 = 商品金额 − 积分成本 − 物业币成本 |
| `pointCost`/`coinCost` | `BigDecimal` | 积分 / 物业币成本 |
| `distributableAmount` | `BigDecimal` | 平台盘 = max(抽佣基础额 − 积分成本 − 物业币成本, 0) |
| `deficitAmount`/`deficitSponsor` | - | 成本击穿差额 / 承担方（默认 property） |
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
| `calculationVersion` | `String` | 计算版本（`legacy`/`distribution_b_v1`） |
| `status` | `String` | `pending`/`distributed`/`reversed`/`partially_reversed` |
| `coordinatorId`/`coordinatorName` | `String` | 统筹ID / 姓名 |
| `sectorLeaderId`/`sectorLeaderName` | `String` | 板块负责人ID / 姓名 |
| `individualLeaderId`/`individualLeaderName` | `String` | 个体负责人ID / 姓名 |
| `createdAt` | `String` | 创建时间 |

**守恒校验（结算前强制，规范§11.3）**：

```
distributableAmount = platformShare + propertyShare + coordinatorShare + sectorLeaderShare + individualLeaderShare
deliverySettlementBase = platformDeliveryShare + courierEarning
merchantShare = merchantGoodsShare − merchantDeliverySubsidy + merchantAdjustmentAmount
totalAmount = productAmount + deliveryFee
```

---



### 21.2 GET `/distribution/stats` — 分成统计

**Auth**: Required  
**Roles**: `coordinator`, `sector_leader`, `individual_leader`, `property_admin`, `platform_admin`

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
    "distributableAmount": 20.00,
    "deficitAmount": 0.00,
    "deficitSponsor": "property",
    "platformShare": 2.00,
    "propertyShare": 12.00,
    "managementPoolAmount": 6.00,
    "coordinatorShare": 2.40,
    "sectorLeaderShare": 1.80,
    "individualLeaderShare": 1.80,
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
    "calculationVersion": "distribution_b_v1"
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
| `pointCost`/`coinCost` | 积分 / 物业币成本 |
| `commissionBaseAmount` | 抽佣基础额 = 商品金额 × 抽佣率 |
| `distributableAmount` | 平台盘 = max(抽佣基础额 − 积分成本 − 物业币成本, 0) |
| `deficitAmount`/`deficitSponsor` | 成本击穿差额 / 承担方（默认 property） |
| `platformShare` | 公司商品平台盘收入 = 平台盘 × platformShareRate |
| `propertyShare` | 物业商品平台盘收入 |
| `managementPoolAmount` | 管理盘 = 平台盘 − 平台 − 物业（减法吸收分币误差） |
| `coordinatorShare` | 统筹实得 = 管理盘 − 板块毛额 |
| `sectorLeaderShare` | 板块负责人实得 = 板块毛额 − 个体负责人 |
| `individualLeaderShare` | 个体负责人实得 = 板块毛额 × individualLeaderRate |
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
| `courierEarning` | 配送员最终收入 = 基数 − 公司配送收入（**正式配送员收入字段**，`courierShare` 不再使用） |
| `coordinatorId`/`sectorLeaderId`/`individualLeaderId` | 各级负责人ID |
| `calculationVersion` | 计算版本（`distribution_b_v1`） |

> **兼容字段**：`productCommission` = `commissionBaseAmount`、`productBase` = 商品实付 = `productAmount − pointCost − coinCost`、`effectiveDeliveryFee` = `deliveryFee`、`courierShare` = `courierEarning`、`breakdown` 旧结构保留。标准字段语义以本表为准。

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
        "calculationVersion": "distribution_b_v1",
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

- 支付成功 → 生成分账记录时：商家商品收入、商家配送补贴扣减、公司/物业/管理盘三级份额、公司配送收入、成本击穿（§9.2）；
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
    "phone": "13800138000",
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
- 余额不足时返回错误

**错误响应**：


| 错误码     | HTTP | 说明       |
| ------- | ---- | -------- |
| `94002` | 400  | 商家积分余额不足 |


---



### 22.4 GET `/merchant/points/quote` — 积分报价
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
    "amount": 500.00
  }'
```

**错误响应**：


| 错误码     | HTTP | 说明        |
| ------- | ---- | --------- |
| `20004` | 403  | 权限不足（角色非 `merchant`/`activity_leader`/`technician`） |
| `60001` | 404  | 商家店铺不存在 |
| `60002` | 409  | 商家店铺入驻审核中 |
| `97001` | 404  | 物业公司不存在 |
| `10002` | 400  | 参数格式错误 |

> **说明**：提现申请仅登记申请（`status=pending`）进入审核队列，不在此处校验可提现余额；余额/风控校验由管理端审核环节兜底。


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
| `subscribed`     | `Boolean`    | 当前是否在有效期内已订阅                     |
| `subscriptionType` | `String`   | 订阅类型：`monthly`/`quarterly`/`yearly` |
| `startDate`      | `String`     | 订阅开始日期 `yyyy-MM-dd`                |
| `endDate`        | `String`     | 订阅结束日期 `yyyy-MM-dd`                |
| `status`         | `String`     | 订阅状态：`active`/`expired`/`cancelled` |
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


| 参数   | 类型       | 必填  | 说明   |
| ---- | -------- | --- | ---- |
| `id` | `String` | ✅   | 公告ID |


**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/admin/announcements/ann_demo001/collects?page=1&pageSize=20" \
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
        "id": "ac_demo001",
        "announcementId": "ann_demo001",
        "announcementTitle": "停水通知",
        "residentId": "res_b7m9x1",
        "residentName": "张先生",
        "communityName": "演示小区",
        "building": "3栋",
        "room": "502",
        "collectedData": { "needWater": true, "contactPhone": "138****1234" },
        "collectedAt": "2026-07-01 16:00:00"
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

---



## 33. 管理端 — 物业币提现审核

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

### 33.1 GET `/admin/coin-withdrawals` — 物业币提现审核列表

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/admin/coin-withdrawals?page=1&pageSize=20&auditStatus=pending_audit&sort=-createdAt" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：


| 参数                  | 类型       | 必填  | 默认值          | 说明                                                |
| ------------------- | -------- | --- | ------------ | ------------------------------------------------- |
| `page`              | `int`    | ❌   | `1`          | 页码                                                |
| `pageSize`          | `int`    | ❌   | `20`         | 每页条数                                              |
| `merchantId`        | `String` | ❌   |              | 商家ID筛选                                            |
| `auditStatus`       | `String` | ❌   |              | `pending_audit`/`approved`/`rejected`/`completed` |
| `status`            | `String` | ❌   |              | 同 `auditStatus`，兼容字段                              |
| `startDate`         | `String` | ❌   |              | 开始日期 `yyyy-MM-dd`                                 |
| `endDate`           | `String` | ❌   |              | 结束日期 `yyyy-MM-dd`                                 |
| `propertyCompanyId` | `String` | ❌   |              | 物业公司ID（超管筛选）                                      |
| `sort`              | `String` | ❌   | `-createdAt` | 排序                                                |


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
        "residentPhone": "138****1234",
        "communityName": "演示小区",
        "coinAmount": 100.00,
        "exchangeAmount": 100.00,
        "status": "pending_audit",
        "createdAt": "2026-07-01 10:00:00"
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

---



### 33.2 POST `/admin/coin-withdrawals/{id}/audit` — 审核物业币提现

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/admin/coin-withdrawals/cw_demo001/audit \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "auditStatus": "approved",
    "remark": "审核通过"
  }'
```

---



### 33.3 GET `/admin/coin-withdrawals/summary` — 物业币提现审批汇总（看板）

**Roles**: `property_admin`, `platform_admin`

**查询参数**：`propertyCompanyId`（platform_admin 可指定，否则取当前）

**响应**（`CoinWithdrawalSummaryResponse`）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `pendingCount` | `Long` | 待审笔数 |
| `pendingAmount` | `BigDecimal` | 待审金额合计（实到金额） |
| `todayCompletedCount` | `Long` | 今日完成笔数（按 completedAt） |
| `todayCompletedAmount` | `BigDecimal` | 今日完成金额合计 |
| `approvedCount` | `Long` | 已通过笔数（approved+completed） |
| `approvedAmount` | `BigDecimal` | 已通过金额合计（实到金额） |

---

## 34. 管理端 — 商家审核

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

### 34.1 GET `/admin/merchant-point-purchases` — 商家积分购买审核列表

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/admin/merchant-point-purchases?page=1&pageSize=20&auditStatus=pending_audit&merchantId=mch_demo001" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：


| 参数                  | 类型       | 必填  | 默认值          | 说明                                    |
| ------------------- | -------- | --- | ------------ | ------------------------------------- |
| `page`              | `int`    | ❌   | `1`          | 页码                                    |
| `pageSize`          | `int`    | ❌   | `20`         | 每页条数                                  |
| `merchantId`        | `String` | ❌   |              | 商家ID                                  |
| `auditStatus`       | `String` | ❌   |              | `pending_audit`/`approved`/`rejected` |
| `status`            | `String` | ❌   |              | 同 `auditStatus`，兼容字段                  |
| `startDate`         | `String` | ❌   |              | 开始日期 `yyyy-MM-dd`                     |
| `endDate`           | `String` | ❌   |              | 结束日期 `yyyy-MM-dd`                     |
| `propertyCompanyId` | `String` | ❌   |              | 物业公司ID（超管筛选）                          |
| `sort`              | `String` | ❌   | `-createdAt` | 排序                                    |


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
        "status": "pending_audit",
        "createdAt": "2026-07-01 10:00:00"
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



### 34.2 POST `/admin/merchant-point-purchases/{id}/audit` — 审核商家积分购买

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**路径参数**：


| 参数   | 类型       | 必填  | 说明       |
| ---- | -------- | --- | -------- |
| `id` | `String` | ✅   | 积分购买记录ID |


**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/admin/merchant-point-purchases/mpp_demo001/audit \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "auditResult": "approved",
    "remark": "审核通过"
  }'
```

**请求体**：


| 字段             | 类型       | 必填  | 校验               | 说明                             |
| -------------- | -------- | --- | ---------------- | ------------------------------ |
| `auditResult`  | `String` | ✅   | `@NotBlank`      | `approved`（通过）/ `rejected`（拒绝） |
| `rejectReason` | `String` | ❌   | `@Size(max=200)` | 拒绝原因（拒绝时必填）                    |
| `remark`       | `String` | ❌   | `@Size(max=200)` | 备注                             |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "mpp_demo001",
    "merchantId": "mch_demo001",
    "merchantName": "万达超市",
    "pointAmount": 1000,
    "payAmount": 10.00,
    "status": "approved",
    "auditResult": "approved",
    "auditRemark": "审核通过",
    "operatorId": "res_admin01",
    "auditedAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 34.3 GET `/admin/merchant-withdrawals` — 商家提现审核列表

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/admin/merchant-withdrawals?page=1&pageSize=20&auditStatus=pending_audit&merchantId=mch_demo001" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：


| 参数                  | 类型       | 必填  | 默认值          | 说明                                                |
| ------------------- | -------- | --- | ------------ | ------------------------------------------------- |
| `page`              | `int`    | ❌   | `1`          | 页码                                                |
| `pageSize`          | `int`    | ❌   | `20`         | 每页条数                                              |
| `merchantId`        | `String` | ❌   |              | 商家ID                                              |
| `auditStatus`       | `String` | ❌   |              | `pending_audit`/`approved`/`rejected`/`completed` |
| `status`            | `String` | ❌   |              | 同 `auditStatus`，兼容字段                              |
| `startDate`         | `String` | ❌   |              | 开始日期 `yyyy-MM-dd`                                 |
| `endDate`           | `String` | ❌   |              | 结束日期 `yyyy-MM-dd`                                 |
| `propertyCompanyId` | `String` | ❌   |              | 物业公司ID（超管筛选）                                      |
| `sort`              | `String` | ❌   | `-createdAt` | 排序                                                |


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
        "feeAmount": 3.00,
        "actualAmount": 497.00,
        "status": "pending_audit",
        "createdAt": "2026-07-01 10:00:00"
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



### 34.4 POST `/admin/merchant-withdrawals/{id}/audit` — 审核商家提现

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**路径参数**：


| 参数   | 类型       | 必填  | 说明     |
| ---- | -------- | --- | ------ |
| `id` | `String` | ✅   | 提现记录ID |


**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/admin/merchant-withdrawals/mw_demo001/audit \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "auditResult": "approved",
    "remark": "审核通过"
  }'
```

**请求体**：


| 字段             | 类型       | 必填  | 校验               | 说明                             |
| -------------- | -------- | --- | ---------------- | ------------------------------ |
| `auditResult`  | `String` | ✅   | `@NotBlank`      | `approved`（通过）/ `rejected`（拒绝） |
| `rejectReason` | `String` | ❌   | `@Size(max=200)` | 拒绝原因（拒绝时必填）                    |
| `remark`       | `String` | ❌   | `@Size(max=200)` | 备注                             |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "mw_demo001",
    "merchantId": "mch_demo001",
    "merchantName": "万达超市",
    "amount": 500.00,
    "feeAmount": 3.00,
    "actualAmount": 497.00,
    "status": "approved",
    "auditResult": "approved",
    "auditRemark": "审核通过",
    "operatorId": "res_admin01",
    "auditedAt": "2026-07-01 15:23:33"
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
- 审核通过：`auditStatus` → `approved`，`status` → `active`；同时按 `merchants.contact_phone` + `merchants.property_company_id` 匹配居民，将其 `residents.role` 升级为**入驻申请身份** `applyRole`（`merchant`/`technician`/`activity_leader`，缺省 `merchant`），开通对应端登录/操作权限
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



### 34.5 GET `/admin/merchant-withdrawals/summary` — 商家提现审批汇总（看板）

**Roles**: `property_admin`, `platform_admin`

**查询参数**：`propertyCompanyId`（platform_admin 可指定，否则取当前）

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

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/admin/operation-logs?page=1&pageSize=20&operatorId=res_admin01&action=audit_merchant&startDate=2026-06-01&endDate=2026-06-30" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：


| 参数           | 类型       | 必填  | 默认值          | 说明    |
| ------------ | -------- | --- | ------------ | ----- |
| `page`       | `int`    | ❌   | `1`          | 页码    |
| `pageSize`   | `int`    | ❌   | `20`         | 每页条数  |
| `operatorId` | `String` | ❌   |              | 操作人ID |
| `action`     | `String` | ❌   |              | 操作类型  |
| `startDate`  | `String` | ❌   |              | 开始日期  |
| `endDate`    | `String` | ❌   |              | 结束日期  |
| `sort`       | `String` | ❌   | `-createdAt` | 排序    |


**成功响应**：

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
        "action": "audit_merchant",
        "targetType": "merchant",
        "targetId": "mch_demo001",
        "targetName": "万达超市",
        "detail": "审核通过商家入驻申请",
        "ip": "192.168.1.100",
        "createdAt": "2026-07-01 15:23:33"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 120,
      "totalPages": 6
    }
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



## 36. 管理端 — 积分清零

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

### 36.1 POST `/admin/point-clear/run` — 执行积分清零

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/admin/point-clear/run \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "dryRun": true
  }'
```

---



### 36.2 GET `/admin/point-clear/records` — 积分清零记录

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/admin/point-clear/records?page=1&pageSize=20&startDate=2026-01-01&endDate=2026-06-30" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：


| 参数          | 类型       | 必填  | 默认值          | 说明   |
| ----------- | -------- | --- | ------------ | ---- |
| `page`      | `int`    | ❌   | `1`          | 页码   |
| `pageSize`  | `int`    | ❌   | `20`         | 每页条数 |
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
        "id": "pcr_demo001",
        "taskId": "task_demo001",
        "operatorId": "res_admin01",
        "operatorName": "管理员",
        "dryRun": false,
        "processedCount": 25,
        "totalClearedAmount": 5000,
        "status": "completed",
        "executedAt": "2026-07-01 00:00:00",
        "createdAt": "2026-06-30 23:59:00"
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



## 37. 管理端 — 个体负责人管理

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

### 37.1 GET `/admin/individual-leaders` — 个体负责人列表

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/admin/individual-leaders?page=1&pageSize=20&keyword=张&status=active" \
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
        "id": "il_demo001",
        "residentId": "res_b7m9x1",
        "residentName": "张先生",
        "residentPhone": "138****1234",
        "sectorId": "sl_demo001",
        "sectorName": "保洁板块",
        "propertyCompanyId": "pc_demo001",
        "description": "负责3栋保洁工作",
        "status": "active",
        "createdAt": "2026-06-01 10:00:00"
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

---



### 37.3 GET `/admin/individual-leaders/{id}` — 详情

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**路径参数**：


| 参数   | 类型       | 必填  | 说明        |
| ---- | -------- | --- | --------- |
| `id` | `String` | ✅   | 个体负责人记录ID |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "il_demo001",
    "residentId": "res_b7m9x1",
    "residentName": "张先生",
    "residentPhone": "138****1234",
    "sectorId": "sl_demo001",
    "sectorName": "保洁板块",
    "coordinatorId": "coo_demo001",
    "coordinatorName": "统筹负责人A",
    "propertyCompanyId": "pc_demo001",
    "propertyCompanyName": "演示物业公司",
    "description": "负责3栋保洁工作",
    "status": "active",
    "createdAt": "2026-06-01 10:00:00",
    "updatedAt": "2026-06-28 14:30:00"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 37.4 PUT `/admin/individual-leaders/{id}` — 更新

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**路径参数**：


| 参数   | 类型       | 必填  | 说明        |
| ---- | -------- | --- | --------- |
| `id` | `String` | ✅   | 个体负责人记录ID |


**请求示例**：

```bash
curl -X PUT https://test.yuanxusoftware.top/api/v1/admin/individual-leaders/il_demo001 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "sectorId": "sl_demo002",
    "description": "负责5栋保洁工作"
  }'
```

**请求体**：


| 字段            | 类型       | 必填  | 校验               | 说明                  |
| ------------- | -------- | --- | ---------------- | ------------------- |
| `sectorId`    | `String` | ❌   | `@NotBlank`      | 板块ID                |
| `description` | `String` | ❌   | `@Size(max=200)` | 描述                  |
| `status`      | `String` | ❌   |                  | `active`/`inactive` |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "il_demo001",
    "sectorId": "sl_demo002",
    "description": "负责5栋保洁工作",
    "updatedAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 37.5 DELETE `/admin/individual-leaders/{id}` — 撤销

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

将个体负责人置为 `inactive`（软删除，不物理删除记录）。

**路径参数**：


| 参数   | 类型       | 必填  | 说明        |
| ---- | -------- | --- | --------- |
| `id` | `String` | ✅   | 个体负责人记录ID |

**查询参数（可选）**：

| 参数     | 类型     | 必填 | 说明                                       |
| ------ | ------ | -- | ---------------------------------------- |
| `reason` | `String` | ❌ | 撤销原因（可选，后端不落库，仅前端展示用；不传也能正常撤销） |

> 变更说明：`reason` 由必填改为可选——此前若前端未传该参数，Spring 抛 `MissingServletRequestParameterException`，被全局异常兜底误报为 `99001 内部服务异常`，导致"撤销撤销不了"。

**成功响应**：返回个体负责人详情（同 §37.3，`status` 为 `inactive`），**无 `deletedAt` 字段**。

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "il_demo001",
    "status": "inactive",
    "residentName": "李四",
    "sector": "cleaning"
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

## 37.7 住户端 — 一级代理申请

### POST `/individual-leaders/apply` — 申请成为个体负责人

**Auth**: Required  
**Roles**: `resident`

**说明**：住户自主申请成为个体负责人（一级代理），需登录。每位住户在同一物业公司下仅可有一个 `pending` 状态的申请。

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

---

## 38. 管理端 — 线下积分发放

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

### 38.1 POST `/admin/offline-points/grant` — 线下发放积分

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/admin/offline-points/grant \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "residentId": "res_b7m9x1",
    "pointAmount": 100,
    "description": "线下活动奖励"
  }'
```

---



### 38.2 GET `/admin/offline-points/records` — 线下发放记录

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/admin/offline-points/records?page=1&pageSize=20&residentId=res_b7m9x1&startDate=2026-06-01&endDate=2026-06-30" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：


| 参数           | 类型       | 必填  | 默认值          | 说明   |
| ------------ | -------- | --- | ------------ | ---- |
| `page`       | `int`    | ❌   | `1`          | 页码   |
| `pageSize`   | `int`    | ❌   | `20`         | 每页条数 |
| `residentId` | `String` | ❌   |              | 业主ID |
| `startDate`  | `String` | ❌   |              | 开始日期 |
| `endDate`    | `String` | ❌   |              | 结束日期 |
| `sort`       | `String` | ❌   | `-createdAt` | 排序   |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "opr_demo001",
        "residentId": "res_b7m9x1",
        "residentName": "张先生",
        "pointAmount": 100,
        "description": "线下活动奖励",
        "operatorId": "res_admin01",
        "operatorName": "管理员",
        "createdAt": "2026-07-01 15:23:33"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 15,
      "totalPages": 1
    }
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
**Roles**: `property_admin`, `platform_admin`, `coordinator`

> **2026-09-08 变更**：与 §39 总述对齐，补上 `coordinator`。统筹指派时 `coordinatorId`
> 强制写为本人（忽略请求体中的 `coordinatorId`）。

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/admin/sector-leaders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "residentId": "res_b7m9x1",
    "coordinatorId": "coo_demo001",
    "sector": "cleaning",
    "description": "负责保洁板块管理"
  }'
```

**请求体**：


| 字段              | 类型       | 必填  | 校验               | 说明                                                     |
| --------------- | -------- | --- | ---------------- | ------------------------------------------------------ |
| `residentId`    | `String` | ✅   | `@NotBlank`      | 业主ID                                                   |
| `coordinatorId` | `String` | ✅   | `@NotBlank`      | 统筹负责人ID                                                |
| `sector`        | `String` | ✅   | `@NotBlank`      | 板块类型：`cleaning`/`repair`/`security`/`greening`/`other` |
| `description`   | `String` | ❌   | `@Size(max=200)` | 描述                                                     |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "sl_demo002",
    "residentId": "res_b7m9x1",
    "residentName": "张先生",
    "coordinatorId": "coo_demo001",
    "sector": "cleaning",
    "sectorName": "保洁",
    "description": "负责保洁板块管理",
    "status": "active",
    "createdAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 39.3 GET `/admin/sector-leaders/{id}` — 详情

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`, `coordinator`（只读本物业，2026-09-07 前端问题 C 放行）

**路径参数**：


| 参数   | 类型       | 必填  | 说明        |
| ---- | -------- | --- | --------- |
| `id` | `String` | ✅   | 板块负责人记录ID |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "sl_demo001",
    "residentId": "res_b7m9x1",
    "residentName": "张先生",
    "residentPhone": "138****1234",
    "coordinatorId": "coo_demo001",
    "coordinatorName": "统筹负责人A",
    "sector": "cleaning",
    "sectorName": "保洁",
    "propertyCompanyId": "pc_demo001",
    "propertyCompanyName": "演示物业公司",
    "description": "负责保洁板块管理",
    "individualLeaderCount": 5,
    "status": "active",
    "createdAt": "2026-06-01 10:00:00",
    "updatedAt": "2026-06-28 14:30:00"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 39.4 PUT `/admin/sector-leaders/{id}` — 更新

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`, `coordinator`

> **2026-09-08 变更（前端问题：统筹写操作 403）**：Roles 与 §39 总述对齐，补上 `coordinator`。
> 统筹需持有 `sector.manage` 权限码（统筹标准权限包默认含），且**仅可更新本人名下**的板块负责人
> （`sector_leaders.coordinator_id` = 当前统筹记录ID），否则返回 `403 20004`
> message=「只能操作本人名下的板块负责人」。统筹提交 `coordinatorId` 时会被忽略并强制写为本人。
>
> **2026-09-08 第二轮（v8.3，前端问题：改板块 400）**
> - **根因与修复**：后端 `sector` 白名单此前误用旧枚举
>   （`elderly`/`medical`/`education`/`cleaning`/`canteen`/`pet`/`life_service`），
>   与本文五枚举**只有 `cleaning` 重合**，因此除保洁外全部返回 `400 10002`「sector不合法」。
>   现全域统一为 `cleaning`/`repair`/`security`/`greening`/`other`，并支持中文入参归一化
>   （保洁 / 维修 / 安保 / 绿化 / 其它 → 对应英文码），大小写不敏感。
> - **同物业同板块冲突**：改为某板块前，若该物业下该板块已有**其他**启用中的板块负责人，
>   返回 `400 10002`，message 含冲突对象 ID 与姓名（见下方「错误响应」）。
>   **改为自己当前板块（幂等）不受此限制**。
> - **新增 `description`**：写入 `sector_leaders.description`（需先执行配套 SQL 加列）。
> - `status` 支持大小写不敏感（`ACTIVE` / `active` 均可）。

**路径参数**：


| 参数   | 类型       | 必填  | 说明        |
| ---- | -------- | --- | --------- |
| `id` | `String` | ✅   | 板块负责人记录ID |


**请求示例**：

```bash
curl -X PUT https://test.yuanxusoftware.top/api/v1/admin/sector-leaders/sl_demo001 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "sector": "repair",
    "description": "负责维修板块管理"
  }'
```

**请求体**：


| 字段              | 类型       | 必填  | 校验               | 说明                  |
| --------------- | -------- | --- | ---------------- | ------------------- |
| `coordinatorId` | `String` | ❌   |                  | 统筹负责人ID（统筹提交时被忽略，强制写为本人） |
| `sector`        | `String` | ❌   |                  | 板块：`cleaning`/`repair`/`security`/`greening`/`other`，亦支持中文（保洁/维修/安保/绿化/其它） |
| `description`   | `String` | ❌   | `@Size(max=200)` | 板块描述/备注             |
| `status`        | `String` | ❌   |                  | `active`/`inactive`（大小写不敏感） |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "sl_demo001",
    "sector": "repair",
    "sectorName": "维修",
    "description": "负责维修板块管理",
    "status": "active",
    "statusCode": "active",
    "statusLabel": "启用",
    "updatedAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**错误响应 — 板块非法（值不在五枚举内）**：

```json
{
  "code": 10002,
  "message": "sector不合法：foo；可选值 cleaning/repair/security/greening/other（保洁/维修/安保/绿化/其它）",
  "data": null
}
```

**错误响应 — 同物业同板块冲突**：

```json
{
  "code": 10002,
  "message": "该物业下板块「维修」已有启用中的负责人 sl_002（张三），请先撤销或调整其板块后再改",
  "data": null
}
```

**验收用例**：

| # | 操作 | 期望 |
|---|------|------|
| A | `sl_001` 当前 cleaning，改 `sector=cleaning` 再保存 | 200（幂等） |
| B | 物业下无 repair 负责人时，`sl_001` 改 `repair` | 200，回显 `sector=repair`、`sectorName=维修` |
| C | 已有另一人占用 repair 时再改 `sl_001`→`repair` | 400 + 上面的冲突文案 |
| D | `sector=security` / `greening` / `other`（无冲突） | 200 |
| E | `sector=foo` | 400「sector不合法…」 |
| F | `sector=维修`（中文） | 200，回显 `sector=repair` |

---



### 39.5 DELETE `/admin/sector-leaders/{id}` — 撤销

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`, `coordinator`

> **2026-09-08 变更（前端问题：统筹写操作 403）**：补上 `coordinator`，限制同 §39.4
> （仅可撤销本人名下记录）。

> **2026-09-07 变更（前端问题 A）**：存在启用中的下级个体负责人时不再抛 500，改为返回 `400 code=10002`，message 明确提示数量：「该板块负责人名下仍有 N 名启用中的个体负责人，请先撤销或转移个体负责人后再撤销板块负责人」。`reason` Query 参数可选，仅作撤销备注。

**路径参数**：


| 参数   | 类型       | 必填  | 说明        |
| ---- | -------- | --- | --------- |
| `id` | `String` | ✅   | 板块负责人记录ID |
| `reason` | `String` | ❌ | 撤销原因（Query 参数，仅备注） |

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "sl_demo001",
    "status": "inactive",
    "deletedAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



## 39.6 板块负责人工作台



### GET `/sector-leaders/my` — 获取当前板块负责人信息

**Auth**: Required  
**Roles**: `sector_leader`

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

**Roles**: `sector_leader`（仅本人）

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
**Roles**: `property_admin`, `platform_admin`

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/admin/coordinators \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "residentId": "res_b7m9x1",
    "propertyCompanyId": "pc_demo001",
    "description": "负责统筹管理"
  }'
```

**请求体**：


| 字段                  | 类型       | 必填  | 校验               | 说明     |
| ------------------- | -------- | --- | ---------------- | ------ |
| `residentId`        | `String` | ✅   | `@NotBlank`      | 业主ID   |
| `propertyCompanyId` | `String` | ❌   | | 物业公司ID（可省略：实际挂接以 `residentId` 所属物业为准，传值需一致） |
| `description`       | `String` | ❌   | `@Size(max=200)` | 描述（落库到 `coordinators.description`） |
| `commissionRate`    | `BigDecimal` | ❌ | | 统筹分成比例，缺省取物业公司 `coordinatorShareRate` |
| `status`            | `String` | ❌ | | `active`/`inactive`，缺省 `active` |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "coo_demo002",
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
    "totalEarnings": 0,
    "status": "active",
    "createdAt": "2026-07-01 15:23:33",
    "appointedAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

> **2026-09-06 同步**：请求体实际接受 `residentId` + 可选 `commissionRate`/`status`/`description`（`propertyCompanyId` 以住户所属物业为准）；响应为完整详情结构。

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
**Roles**: `property_admin`, `platform_admin`

**路径参数**：


| 参数   | 类型       | 必填  | 说明        |
| ---- | -------- | --- | --------- |
| `id` | `String` | ✅   | 统筹负责人记录ID |


**请求示例**：

```bash
curl -X PUT https://test.yuanxusoftware.top/api/v1/admin/coordinators/coo_demo001 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "description": "更新后的描述"
  }'
```

**请求体**：


| 字段            | 类型       | 必填  | 校验               | 说明                  |
| ------------- | -------- | --- | ---------------- | ------------------- |
| `description` | `String` | ❌   | `@Size(max=200)` | 描述                  |
| `status`      | `String` | ❌   |                  | `active`/`inactive` |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "coo_demo001",
    "description": "更新后的描述",
    "updatedAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 41.5 DELETE `/admin/coordinators/{id}` — 撤销

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

> **2026-09-07 变更（前端问题 A）**：存在启用中的下级板块负责人时不再抛 500，改为返回 `400 code=10002`，message 明确提示数量：「该统筹名下仍有 N 名启用中的板块负责人，请先撤销或转移板块负责人后再撤销统筹」。

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
    "status": "inactive",
    "deletedAt": "2026-07-01 15:23:33"
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
        "id": "pm_demo001",
        "name": "万达超市",
        "category": "外卖",
        "contactPhone": "138****5678",
    "residentId": "res_mch02",
        "description": "超市提供日常用品配送",
        "coverUrl": "https://cdn.example.com/merchant/pm_demo001.jpg",
        "status": "active",
        "linkedPropertyCount": 3,
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
**Roles**: `property_admin`, `platform_admin`

### 45.1 POST `/admin/platform-merchants/batch-config` — 批量设定商家参数

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/admin/platform-merchants/batch-config \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "merchantIds": ["mch_demo001", "mch_demo002", "mch_demo003"],
    "config": {
      "commissionRate": 0.15,
      "pointExchangeRate": 100,
      "coinRebateRate": 0.05,
      "coinRebateEnabled": true
    }
  }'
```

**请求体**：


| 字段                         | 类型             | 必填  | 校验                                  | 说明            |
| -------------------------- | -------------- | --- | ----------------------------------- | ------------- |
| `merchantIds`              | `List<String>` | ✅   | `@NotEmpty @Size(max=50)`           | 商家ID列表（最多50个） |
| `config`                   | `Object`       | ✅   | `@Valid`                            | 配置参数          |
| `config.commissionRate`    | `BigDecimal`   | ❌   | `@DecimalMin("0") @DecimalMax("1")` | 抽佣比例          |
| `config.pointExchangeRate` | `Integer`      | ❌   | `@Min(1)`                           | 积分兑换比（1元=X积分） |
| `config.coinRebateRate`    | `BigDecimal`   | ❌   | `@DecimalMin("0") @DecimalMax("1")` | 物业币返现比例       |
| `config.coinRebateEnabled` | `Boolean`      | ❌   |                                     | 是否启用物业币返现     |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "updatedCount": 3,
    "failedCount": 0,
    "results": [
      {
        "merchantId": "mch_demo001",
        "status": "success"
      },
      {
        "merchantId": "mch_demo002",
        "status": "success"
      },
      {
        "merchantId": "mch_demo003",
        "status": "success"
      }
    ],
    "updatedAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**错误响应**：


| 错误码     | HTTP | 说明      |
| ------- | ---- | ------- |
| `60001` | 404  | 部分商家不存在 |


---



## 46. 管理端 — 商家挂接

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

### 46.1 POST `/admin/platform-merchants/{id}/link` — 从商家库挂接到物业

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/admin/platform-merchants/pm_demo001/link \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "propertyCompanyId": "pc_demo001",
    "customConfig": {
      "commissionRate": 0.12,
      "deliveryFee": 0.30,
      "freeDeliveryThreshold": 50.00
    }
  }'
```

**请求体**：


| 字段                                   | 类型           | 必填  | 校验          | 说明            |
| ------------------------------------ | ------------ | --- | ----------- | ------------- |
| `propertyCompanyId`                  | `String`     | ✅   | `@NotBlank` | 物业公司ID        |
| `customConfig`                       | `Object`     | ❌   |             | 自定义配置（覆盖平台默认） |
| `customConfig.commissionRate`        | `BigDecimal` | ❌   |             | 抽佣比例          |
| `customConfig.deliveryFee`           | `BigDecimal` | ❌   |             | 配送费           |
| `customConfig.freeDeliveryThreshold` | `BigDecimal` | ❌   |             | 满额免配送费        |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "merchantId": "mch_new001",
    "platformMerchantId": "pm_demo001",
    "platformMerchantName": "万达超市",
    "propertyCompanyId": "pc_demo001",
    "propertyName": "演示物业公司",
    "status": "pending_audit",
    "linkedAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**业务规则**：

- 挂接后商家状态为 `pending_audit`，需要物业管理员审核
- 同一平台商家不能在同一物业下重复挂接
- 挂接成功后自动创建商家账户

**错误响应**：


| 错误码     | HTTP | 说明           |
| ------- | ---- | ------------ |
| `30004` | 409  | 该商家已在当前物业下挂接 |
| `60001` | 404  | 平台商家不存在      |


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
- 商品链路：抽佣基础额 = 商品金额 × 抽佣比例；商家商品实得 = 商品金额 − 抽佣基础额；平台盘 = max(抽佣基础额 − 积分成本 − 物业币成本, 0)；
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

**说明**：`platform_admin` 返回全平台所有账号，`property_admin` 仅返回本公司下的账号。

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/admin/users?keyword=张" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：


| 参数        | 类型       | 必填  | 默认值 | 说明            |
| --------- | -------- | --- | --- | ------------- |
| `keyword` | `String` | ❌   |     | 搜索关键词（姓名/手机号） |


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
        "propertyCompanyId": "pc_demo001",
        "status": "active",
        "effectivePermissionCount": 45
      },
      {
        "id": "res_002",
        "name": "物业管理员",
        "phone": "139****1234",
        "role": "property_admin",
        "propertyCompanyId": "pc_demo001",
        "status": "active",
        "effectivePermissionCount": 41
      }
    ],
    "total": 2
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
| `accounts[].role`                     | `String` | 角色代码                            |
| `accounts[].propertyCompanyId`        | `String` | 所属物业公司ID                        |
| `accounts[].status`                   | `String` | 状态：`active`/`frozen`/`disabled` |
| `accounts[].effectivePermissionCount` | `int`    | 最终权限数量                          |


---



### 48.2 GET `/admin/permissions` — 权限列表

**Auth**: Required  
**Roles**: `platform_admin`, `property_admin`

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
        "code": "resident:read",
        "name": "查看业主",
        "module": "resident",
        "action": "read",
        "description": "查看业主列表和详情"
      },
      {
        "code": "merchant:audit",
        "name": "审核商家",
        "module": "merchant",
        "action": "audit",
        "description": "审核商家入驻申请"
      },
      {
        "code": "reports:view",
        "name": "查看报表",
        "module": "reports",
        "action": "view",
        "description": "查看各类统计报表"
      }
    ]
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 48.3 GET `/admin/role-presets` — 角色预设列表

**Auth**: Required  
**Roles**: `platform_admin`, `property_admin`

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/admin/role-presets" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "presets": [
      {
        "id": "rp_demo001",
        "name": "物业管理员",
        "role": "property_admin",
        "permissionCodes": ["resident:read", "merchant:audit", "reports:view", "announcements:manage"],
        "isDefault": true,
        "createdAt": "2026-03-15 10:00:00"
      },
      {
        "id": "rp_demo002",
        "name": "平台管理员",
        "role": "platform_admin",
        "permissionCodes": ["resident:read", "merchant:audit", "reports:view", "announcements:manage", "property_company:manage"],
        "isDefault": true,
        "createdAt": "2026-03-15 10:00:00"
      }
    ]
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 48.4 POST `/admin/role-presets` — 创建角色预设

**Auth**: Required  
**Roles**: `platform_admin`

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/admin/role-presets \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "name": "自定义管理员",
    "role": "property_admin",
    "permissionCodes": ["resident:read", "merchant:read", "reports:view"]
  }'
```

**请求体**：


| 字段                | 类型             | 必填  | 校验                        | 说明    |
| ----------------- | -------------- | --- | ------------------------- | ----- |
| `name`            | `String`       | ✅   | `@NotBlank @Size(max=50)` | 预设名称  |
| `role`            | `String`       | ✅   | `@NotBlank`               | 角色代码  |
| `permissionCodes` | `List<String>` | ✅   | `@NotEmpty`               | 权限码列表 |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "rp_demo003",
    "name": "自定义管理员",
    "role": "property_admin",
    "permissionCodes": ["resident:read", "merchant:read", "reports:view"],
    "isDefault": false,
    "createdAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 48.5 PUT `/admin/role-presets/{id}` — 更新角色预设

**Auth**: Required  
**Roles**: `platform_admin`

**路径参数**：


| 参数   | 类型       | 必填  | 说明     |
| ---- | -------- | --- | ------ |
| `id` | `String` | ✅   | 角色预设ID |


**请求示例**：

```bash
curl -X PUT https://test.yuanxusoftware.top/api/v1/admin/role-presets/rp_demo003 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "name": "自定义管理员（更新）",
    "permissionCodes": ["resident:read", "merchant:read", "reports:view", "announcements:manage"]
  }'
```

**请求体**：


| 字段                | 类型             | 必填  | 校验              | 说明    |
| ----------------- | -------------- | --- | --------------- | ----- |
| `name`            | `String`       | ❌   | `@Size(max=50)` | 预设名称  |
| `permissionCodes` | `List<String>` | ❌   |                 | 权限码列表 |


**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "rp_demo003",
    "name": "自定义管理员（更新）",
    "permissionCodes": ["resident:read", "merchant:read", "reports:view", "announcements:manage"],
    "updatedAt": "2026-07-01 15:23:33"
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
    "userId": "res_admin01",
    "userName": "管理员",
    "role": "property_admin",
    "rolePresetId": "rp_demo001",
    "rolePresetName": "物业管理员",
    "effectivePermissions": [
      "resident:read",
      "merchant:audit",
      "merchant:read",
      "reports:view",
      "announcements:manage",
      "point_pool:manage",
      "coordinator:manage"
    ],
    "grantedPermissions": [
      { "code": "coordinator:manage", "reason": "需要管理统筹负责人", "grantedAt": "2026-06-01 10:00:00" }
    ],
    "revokedPermissions": []
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---



### 48.7 POST `/admin/users/{id}/permissions/grant` — 授予权限

**Auth**: Required  
**Roles**: `platform_admin`

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
**Roles**: `platform_admin`

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/admin/users/res_b7m9x1/permissions/revoke \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "permissionCodes": ["merchant:audit"]
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
      "initialPassword": "admin123456"
    },
    "defaultConfigs": "已应用默认配置",
    "createdAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

> **2026-09-06 同步**：与 `POST /property-companies`（§14.3）共用同一套校验与创建逻辑：重名/手机号占用返回 400 而非 500；创建成功后初始管理员密码 `admin123456` 仅在 `adminAccount.initialPassword` 返回一次；请求中的 Query `propertyCompanyId` 被忽略。



### 51.3 GET `/admin/property-companies/{id}` — 物业公司详情

**成功响应**：包含完整的物业公司信息、分成比例、物业币配置、小区列表、管理员列表。

---



### 51.4 PUT `/admin/property-companies/{id}` — 更新物业公司信息

**请求示例**：

```bash
curl -X PUT https://test.yuanxusoftware.top/api/v1/admin/property-companies/pc_demo001 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "name": "更新后的物业公司",
    "contactPhone": "13800000000"
  }'
```

**请求体**：


| 字段             | 类型       | 必填  | 说明   |
| -------------- | -------- | --- | ---- |
| `name`         | `String` | ❌   | 名称   |
| `logoUrl`      | `String` | ❌   | Logo |
| `contactPhone` | `String` | ❌   | 联系电话 |
| `address`      | `String` | ❌   | 地址   |

> **禁止直改分成**：本接口请求体若携带 `residentPointShareRate`/`merchantPointShareRate`/`coinPointShareRate`/`sharedPointShareRate`，物业侧（`property_admin` 含领导/操作员）返回 `20004`（403），提示「分成比例调整需通过价格审批流程，禁止直接修改」；仅 `platform_admin` 可直改。分成调整请走 §65 价格审批。


---



### 51.5 PATCH `/admin/property-companies/{id}/status` — 更新物业公司状态

**Auth**: Required  
**Roles**: `platform_admin`（仅平台管理员可变更状态）

> **2026-09-08 变更**：与「删除物业公司」两档语义对齐（见 §94.14）。停用（`inactive`）后该物业不可再经营，
> 住户端 `GET /property-companies/current` 会返回 `20005 账号被禁用`（message=「物业公司已停用」）。
> 本接口**不触发**分账比例三档校验（见 §51.6），停用/启用不会因历史比例数据被拦截。
> 停用后：该物业的<strong>管理端账号</strong>调用 `POST /auth/admin-login` 会被拒绝，返回 `403 20005`
> message=「物业公司已停用」（平台超管不受影响）；住户端 `GET /property-companies/current`
> 同样返回 `20005`。

**请求体**：


| 字段       | 类型       | 必填  | 说明                  |
| -------- | -------- | --- | ------------------- |
| `status` | `String` | ✅   | `active`/`inactive` |


---



### 51.6 PATCH `/admin/property-companies/{id}/config` — 更新物业公司配置

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

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
>   `individualLeaderRate` 为管理盘内部的级联系数（默认 `0.6` / `0.5`），仅各自校验落在 `[0,1]`。
> - `platformShareRate`（我们公司）可由本接口写入，**仅 `platform_admin` 可修改**；
>   `property_admin` 传入会返回 `403 20004`。校验时未传该字段则取已落库值参与三档合计。

**请求示例**：

```bash
curl -X PATCH https://test.yuanxusoftware.top/api/v1/admin/property-companies/pc_demo001/config \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "propertyShareRate": 0.598,
    "coordinatorShareRate": 0.302,
    "sectorLeaderRate": 0.10,
    "individualLeaderRate": 0.00,
    "coinDisplayEnabled": true,
    "coinExpiryDays": 365,
    "coinFreezeDefault": false,
    "deliveryBaseFee": 0.50,
    "deliveryCourierShareRate": 1.00,
    "withdrawalFeeRate": 0.006,
    "pointToFeeRate": 0.01,
    "twoYearClearEnabled": true,
    "neighborDailyContactLimit": 2
  }'
```

**请求体**：


| 字段                          | 类型           | 必填  | 说明                   |
| --------------------------- | ------------ | --- | -------------------- |
| `platformShareRate`         | `BigDecimal` | ❌   | **（v8.2 新增）**我们公司（平台）分成比例，一级三档之一；仅 `platform_admin` 可写 |
| `propertyShareRate`         | `BigDecimal` | ❌   | 物业公司分成比例，一级三档之一（默认0.30） |
| `coordinatorShareRate`      | `BigDecimal` | ❌   | 管理盘（统筹）分成比例，一级三档之一（默认0.70） |
| `sectorLeaderRate`          | `BigDecimal` | ❌   | 板块负责人比例，管理盘内部级联系数（默认0.10），**不与个体相加校验** |
| `individualLeaderRate`      | `BigDecimal` | ❌   | 个体负责人比例，管理盘内部级联系数（默认0.00），**不与板块相加校验** |
| `coinDisplayEnabled`        | `Boolean`    | ❌   | 物业币卡片全局显示            |
| `coinIssueMode`             | `String`     | ❌   | 发放模式：`auto`/`manual` |
| `coinExpiryDays`            | `Integer`    | ❌   | 物业币有效期天数（默认365）      |
| `coinFreezeDefault`         | `Boolean`    | ❌   | 新用户物业币默认冻结           |
| `deliveryBaseFee`           | `BigDecimal` | ❌   | 配送起步价                |
| `deliveryCourierShareRate`  | `BigDecimal` | ❌   | 快递员配送费分成比例（仅历史兼容，B 方案下配送员收入改由配送费核算） |
| `withdrawalFeeRate`         | `BigDecimal` | ❌   | 提现手续费率               |
| `pointToFeeRate`            | `BigDecimal` | ❌   | 积分抵扣物业费比率            |
| `twoYearClearEnabled`       | `Boolean`    | ❌   | 2年欠费积分清零开关           |
| `neighborDailyContactLimit` | `Integer`    | ❌   | 邻居私聊每日限制             |---

### 51.7 GET `/admin/settlement-config` — 查询结算配置

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**成功响应**：返回当前物业公司的结算配置（platformShareRate、propertyShareRate 等）。

---

### 51.8 PUT `/admin/settlement-config` — 更新结算配置

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**请求体**：同 `PATCH /admin/property-companies/{id}/config` 相关字段。

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

**说明**：查询平台累计收益余额（已结算-已提现）。

---

### 51.10 GET `/admin/property-companies/{id}/settlement-balance` — 物业公司结算余额

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**说明**：查询指定物业公司的结算余额（已结算-已提现）。

---

### 51.11 GET/POST `/admin/property-companies/{id}/settlement-withdrawals` — 物业公司结算提现

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

| 方法 | 说明 |
| --- | --- |
| GET | 查询物业公司结算提现记录列表 |
| POST | 提交物业公司结算提现申请 |

---

### 51.12 POST `/admin/property-companies/{id}/settlement-withdrawals/{withdrawalId}/approve` — 审批提现

**Auth**: Required  
**Roles**: `platform_admin`

---

### 51.13 POST `/admin/property-companies/{id}/settlement-withdrawals/{withdrawalId}/reject` — 拒绝提现

**Auth**: Required  
**Roles**: `platform_admin`

---

### 51.14 POST `/admin/merchants/{id}/coin/earn` — 给商家发放物业币

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**说明**：管理端手动给商家发放物业币（佣金返现或奖励场景）。

---

# 物业联系方式接口

### GET `/property-contact` — 物业联系方式（前端公开）

**Auth**: Optional（匿名可访问）

**说明**：获取物业对外联系方式（如客服电话、地址）。`enabled=false` 时 `data=null`。前端在用户未绑定楼栋时调用并展示。

**成功响应**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "phone": "0518-12345678",
    "address": "XX小区物业服务中心",
    "businessHours": "08:30-17:30",
    "wechat": "wuye_kefu"
  }
}
```

### GET/POST `/admin/property-contact` — 物业联系方式（管理端）

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

| 方法 | 说明 |
| --- | --- |
| GET | 查询当前物业公司联系方式配置 |
| POST | 创建/更新物业联系方式配置 |

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

**请求体**：`{ "items": [ { "id": "ab_02", "label": "18-25岁", "minAge": 18, "maxAge": 25 } ] }`

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

**Roles**: `platform_admin`, `property_admin`（含物业领导/物业管理员，主 role 均为 `property_admin`；仅可见已绑定本物业的社区）

## 53.2 POST `/admin/community-entities` — 创建社区

**Roles**: **仅** `platform_admin`  
> 物业领导 / 物业管理员 **不能**创建顶层社区（会返回 `20004` 权限不足）。请用平台超管 `13800138000` 创建后再绑定物业。

**请求体**：`name`, `description`, `adminResidentId`（社区管理员住户 ID）

## 53.3 GET `/admin/community-entities/{id}/property-companies` — 已绑定物业列表

**响应项**：`propertyCompanyId`, `propertyCompanyName`, `permissionLevel`（`read_only`/`operate`）, `boundAt`

## 53.4 POST `/admin/community-entities/{id}/property-companies` — 绑定物业公司

**请求体**：`propertyCompanyId`, `permissionLevel`（默认 `read_only`）

## 53.5 DELETE `/admin/community-entities/{id}/property-companies/{propertyCompanyId}` — 解绑

## 53.6 PUT `/admin/community-entities/{id}/property-companies/{propertyCompanyId}/permissions` — 调整权限

**请求体**：`permissionLevel`, `allowedModules`（如 `["announcement.read","resident.read"]`）

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

## 54.3 PUT `/admin/property-operators/{id}/scope` — 更新管辖范围

## 54.4 PUT `/admin/property-operators/{id}/status` — 启用/停用

**请求体**：`status`：`active`/`disabled`

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

**请求体**：`title`, `content`, `imageUrls`, `productId`（可选，关联商品）

**错误**：`90105` 本周额度不足

> **说明**：扣减本周额度并写入 `merchant_ads`；**暂不向服务范围内居民批量 fan-out 聊天消息**（前端可先走广告历史/运营位；后续版本补推送）。

## 59.3 GET `/merchant/ads` — 本店广告历史

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

```
一级经销商 (distributor_level=1)  — 有货源，设进货价/建议零售价
    ↓ 供货
业主商户 (distributor_level=2)  — 选品上架，自定价，分享赚差价
    ↓ 分享链接（带 resellerId）
普通用户购买 → 平台收款 → 进货价结算一级 → 差价 T+1 至业主商户物业币余额
```

## 60.0.1 数据表

| 表名 | 说明 |
| --- | --- |
| `resident_merchant_applications` | 申请与审核（`rma_`） |
| `resident_merchant_deposits` | 保证金（`rmd_`），默认 500 元；可由平台配置 `residentMerchant.defaultDepositAmount` 覆盖 |
| `distributor_products` | 一级上架商品（`dpr_`） |
| `resident_merchant_listings` | 二级选品上架（`rml_`） |
| `resident_merchant_settlements` | T+1 结算（`rms_`） |

## 60.1 POST `/resident-merchants/apply` — 申请成为业主商户

**Roles**: `resident`（业主身份）

**请求体**：`agreedTerms`（必填 true）

**响应**：`applicationId`, `depositAmount`, `status`（`待缴保证金`/`pending_deposit`）, `visibility`（默认 `private`）

> 🔧 **说明（2026-09-04）**：`depositAmount` 现取**平台配置** `residentMerchant.defaultDepositAmount`（管理端 §60.12 `PUT /admin/resident-merchant-settings` 的 `defaultDepositAmount`），未配置时回落 `500.00`。此前该值硬编码为 500，后台改配置不生效。

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
> 注意：§7.2 `GET /merchants/my` 对 `resident` 返回 `403`，住户查询"商家入驻申请"进度请使用 §7.2.2 `GET /merchants/my-application`，**不是**本接口。

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

**Query**：`categoryId`, `keyword`, `page`, `pageSize`

**列表项**：`distributorProductId`, `name`, `coverUrl`, `wholesalePrice`, `suggestedRetailPrice`, `stock`

## 60.6 POST `/resident-merchants/listings` — 从一级选品上架

**请求体**：`distributorProductId`, `retailPrice`, `enabled`

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
| `shareCount` | `Integer` | 分享次数 |
| `createdAt` | `String` | 创建时间 |

> ⚠️ **无商城 `productId`**：列表项**不包含** `products` 表商品ID，不能用于 `GET /products/{id}`（非 `prd_` 前缀会返回 `10002` 商品ID格式不正确）。分销商品卡片/分享应跳橱窗详情（`listingId + resellerId`），或使用公开橱窗 `GET /resident-merchants/public/{residentId}`（私密橱窗返回 `90111`）。

## 60.8 PUT `/resident-merchants/listings/{id}` — 改价/上下架

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

## 60.9.2 GET `/resident-merchants/public` — 公开橱窗列表（任何人可查看）

**Auth**: Optional（无需登录）

**Query**：`page`（默认 1）, `pageSize`（默认 20）, `keyword`（店主姓名/手机号，可选）, `propertyCompanyId`（可选）

**仅返回**：`visibility=public` 且 `status=active` 的业主商户；姓名缺省时手机号脱敏展示。

**列表项**：`residentId`, `residentName`, `propertyCompanyId`, `visibility`, `status`, `depositAmount`, `listingCount`

## 60.9.3 GET `/resident-merchants/public/{residentId}` — 公开橱窗详情（任何人可查看）

**Auth**: Optional（无需登录）

**响应**：`residentId`, `residentName`, `propertyCompanyId`, `visibility`, `status`, `depositAmount`, `listingCount`, `listings`（仅 `enabled=true` 的商品）

**listings[].字段**：`id`, `productName`, `coverUrl`, `retailPrice`, `shareCount`（**不含批发价/进货价**）

**错误**：`90111`（业主商户未对外展示）——店铺 `private` 或非营业中

## 60.10 POST `/orders` 增强（§8.1）

请求体新增可选字段（orders 表已有列；下单归因链路以字段透传为主）：

| 字段 | 说明 |
| --- | --- |
| `residentMerchantListingId` | 通过业主商户分享购买 |
| `resellerId` | 业主商户住户 ID（与 listing 校验一致） |

> **说明**：结算表 `resident_merchant_settlements` 与管理端查询已就绪；**自动 T+1 入账定时任务暂缓**。

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
| GET | `/admin/distributor-products` | 分销商品列表 | ✅ |
| POST | `/admin/distributor-products` | 一级经销商上架 | ✅ |
| PUT | `/admin/distributor-products/{id}` | 更新分销商品（🆕 2026-09-07，见下方） | ✅ |
| DELETE | `/admin/distributor-products/{id}` | 删除分销商品（软删 status=inactive，🆕 2026-09-07，见下方） | ✅ |
| PUT | `/admin/distributor-products/{id}/billing-cycle` | 设置收费周期 | ✅（详见 §63.2） |
| GET | `/admin/resident-merchant-settings` | 配置读取 | ✅ |
| PUT | `/admin/resident-merchant-settings` | 默认保证金、平台抽成、退货天数 | ✅ |

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

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `name` | String | ✅ | 商品名称 |
| `coverUrl` | String | | 封面图 |
| `categoryId` | String | | 分类 ID（当前可传，落库以实际实现为准） |
| `description` | String | | 描述 |
| `wholesalePrice` | BigDecimal | ✅ | 进货价 |
| `suggestedRetailPrice` | BigDecimal | ✅ | 建议零售价 |
| `stock` | Integer | | 库存，默认 0 |

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
| `keyword` | String | | 按姓名/手机号搜索 |
| `propertyCompanyId` | String | | 物业公司 ID（平台超管用） |

**列表项**：`id`, `residentId`, `residentName`, `propertyCompanyId`, `depositAmount`, `status`, `statusCode`, `createdAt`

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

# 61. 三期 — 模块负责人体系 (sector-leaders)

板块负责人管理个体负责人、上传商家、审核商家加入、设置分成。

> **个体负责人路径约定**：工作台接口路径参数 `{id}` 支持 `il_`（个体负责人记录 ID）、`res_`（住户 ID）或字面量 `me`（当前登录个体负责人）。建议先调 `GET /individual-leaders/my` 取得标准 `il_` ID。

## 61.1 POST `/sector-leaders/{id}/individual-leaders` — 增加个体负责人

**Roles**: `sector_leader`

**Path**: `id` — 板块负责人ID

```json
// Request Body
{
  "residentId": "res_xxx",
  "sector": "cleaning",
  "name": "王强家政",
  "commissionRate": 0.05
}
```

```json
// Response
{
  "code": 0,
  "data": {
    "id": "il_xxx",
    "residentId": "res_xxx",
    "sector": "cleaning",
    "commissionRate": 0.05,
    "status": "active"
  }
}
```

**逻辑**：校验 `canManageIndividualLeaders=true`，sector 须与板块负责人一致，业主须同物业。

## 61.2 DELETE `/sector-leaders/{id}/individual-leaders/{subId}` — 删除个体负责人

**Roles**: `sector_leader`

将个体负责人 status 设为 `inactive`。

## 61.3 POST `/sector-leaders/{id}/merchants` — 上传商家

**Roles**: `sector_leader`

```json
// Request Body
{
  "platformMerchantId": "pm_xxx",
  "name": "测试商家",
  "category": "外卖",
  "contactPhone": "13800000000",
  "address": "小区东门"
}
```

**逻辑**：关联 platformMerchant 创建 Merchant，`audit_status=pending`，防重复挂接。

## 61.4 POST `/sector-leaders/{id}/merchant-audits` — 同意/拒绝商家加入

**Roles**: `sector_leader`

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

**Roles**: `sector_leader`

```json
// Request Body
{ "commissionRate": 0.08 }
```

## 61.6 PUT `/individual-leaders/{id}/merchant-distribution` — 个体负责人设置商家分成

**Roles**: `individual_leader`  
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

**Roles**: `individual_leader`  
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
**Roles**: `individual_leader`

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

## 62.11 POST `/admin/delivery-price-ranges` — 创建价格区间

```json
// Request Body
{ "minPrice": 0.10, "maxPrice": 0.50 }
```

创建/更新时校验 `min ≤ max` 并与现有 active 区间做闭区间重叠检测。

## 62.12 PUT `/admin/delivery-price-ranges/{id}` — 更新价格区间

## 62.13 DELETE `/admin/delivery-price-ranges/{id}` — 删除价格区间

---

# 63. 三期 — 商家端口改造 (merchant-port)

定向广告投放；管理端分销商品列表 / 上架 / 收费周期设置。

> **对接注意**：管理端列表用 `GET /admin/distributor-products`；业主选品池 `GET /distributors/products` 仅 `resident`，勿混用。

## 63.1 POST `/merchant/ads/targeted` — 创建定向广告

**Roles**: `merchant`

```json
// Request Body
{
  "merchantId": "mch_xxx",
  "title": "定向推广",
  "content": "...",
  "imageUrls": "[\"url1\"]",
  "productId": "prd_xxx",
  "targetBuildings": "[\"1栋\",\"2栋\"]",
  "targetGender": "male",
  "targetAgeBrackets": "[\"30-40\",\"40-50\"]"
}
```

`targetGender` 可选值：`male` / `female` / `all`。消耗本周广告额度。

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
**Roles**: `coordinator`

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

**Roles**: `coordinator`, `property_admin`

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

**Roles**: `coordinator`（**仅统筹**，物业管理员不可代审）

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

**Roles**: `coordinator`（仅本人）

**请求体**（`ApplyRoleWithdrawalRequest`）：

| 字段 | 类型 | 必填 | 校验 | 说明 |
| --- | --- | --- | --- | --- |
| `amount` | `BigDecimal` | ✅ | `@NotNull @DecimalMin("0.01")` | 提现金额，不超过可提现余额（累计分成 - 已占用提现） |

**成功响应**（`RoleWithdrawalItemResponse`）：`id`/`withdrawalType`(`coordinator`)/`applicantId`/`residentId`/`propertyCompanyId`/`amount`/`feeRate`/`feeAmount`/`actualAmount`/`status`(`pending`)/`auditedAt`/`completedAt`/`createdAt`

## 64.7 GET `/coordinators/{id}/withdrawals` — 统筹提现记录

**Roles**: `coordinator`（仅本人）

**Query 参数**：`page`/`pageSize`/`auditStatus`(`pending`/`approved`/`rejected`/`completed`)/`startDate`/`endDate`

> 手续费率与平台手续费分成复用物业公司 `withdrawalFeeRate`/`platformWithdrawalFeeShareRate`，与个体负责人/快递员口径一致。

---

# 65. 三期 — 价格审批 (price-approvals)

提交价格修改审批、审批通过/拒绝、审批列表。

## 65.1 POST `/admin/price-approvals` — 提交价格修改审批

**Roles**: `property_admin`（含 `propertySubRole=property_operator` 的物业管理员）、`coordinator`、`sector_leader`、`individual_leader`

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

`itemType` 可选值：`delivery_fee` / `product_price` / `merchant_ad` / `merchant_distribution` / `property_fee_price` / `resident_share_rate`。

> **说明**：`resident_share_rate`（业主/商家/物业币/共享积分分成比例）审批通过后自动落库 `property_companies`，见 §65.2。

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
2. 商家在时限内选择：
   - `merchant_self`：创建商家自配配送单（`carrier_type = merchant`，`status = merchant_self`），订单进入 `delivering`。
   - `courier_hall`：创建平台配送单（`carrier_type = courier`，`status = pending`），订单进入 `delivering`，等待骑手抢单。配送单会出现在 `GET /deliveries/pending` 待抢单列表中（v6.6 修复：此前因查询仅匹配 `orderStatus=paid`，商家选择后订单变为 `delivering` 导致不显示）。
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
- **配送列表/详情**（`PendingDeliveryItemResponse` / `MyDeliveryItemResponse` / `CompleteDeliveryResponse`）：
  `carrierType`(string：courier/merchant)、`merchantDeliveryFeeShare`(decimal：仅商家自配单)。

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

**Roles**: `individual_leader`  
**Path**: `id` — `il_` / `res_` / `me`（同 §61）

```json
// Request Body
{ "amount": 100.00 }
```

创建 `WithdrawalRecord`（`withdrawal_type=individual_leader`），手续费率取自物业公司 `withdrawalFeeRate`。

## 70.2 GET `/individual-leaders/{id}/withdrawals` — 个体负责人提现记录

**Roles**: `individual_leader`  
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

> **背景**：快递员/个体负责人/统筹/板块负责人的提现申请统一落库 `withdrawal_records`（`withdrawal_type=courier / individual_leader / coordinator / sector_leader`），管理端需经本接口查询审核（原 `/admin/merchant-withdrawals` 仅查商家提现表 `merchant_withdrawals`，不包含这些记录）。

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
| `withdrawalType` | `String` | ❌ | | 提现类型：`individual_leader`/`courier`/`coordinator`/`sector_leader` |
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
| `list[].withdrawalType` | `String` | 提现类型：`individual_leader`/`courier`/`coordinator`/`sector_leader` |
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

### 分成计算公式（distribution_b_v1，规范§4/§5）

**商品分账链路（配送费不进入商品抽佣基数）**：
```
抽佣基础额 = 商品金额 × 抽佣率
商家商品实得 = 商品金额 - 抽佣基础额
平台盘 = max(抽佣基础额 - 积分成本 - 物业币成本, 0)
成本击穿差额 = max(积分成本 + 物业币成本 - 抽佣基础额, 0)   ← 默认物业承担，不扣商家

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
    "coordinatorShareRate": 0.7000,
    "sectorLeaderRate": 0.1000,
    "individualLeaderRate": 0.0000
  }
}
```

## 71.2 PUT `/admin/platform-share-rates` — 更新平台分成比例

**Roles**: `platform_admin`（仅平台管理员可修改）

```json
// Request Body
{
  "propertyCompanyId": "pc_demo001",
  "platformShareRate": 0.05,
  "platformDeliveryShareRate": 0.10,
  "platformWithdrawalFeeShareRate": 1.0
}
```

校验：所有比例必须在 0~1 之间。

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


| 值                   | 说明   |
| ------------------- | ---- |
| `pending`           | 待抢单  |
| `accepted`          | 已接单  |
| `delivering`        | 配送中  |
| `completed`         | 已完成  |
| `failed`            | 配送失败 |
| `property_fallback` | 物业兜底 |




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

联调账号见 §1.2：`13900000020` / `13900000021`，密码 `admin123456`。

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

**Auth**: Required（居民端需 `resident` 角色；管理端需 `property_admin`/`community_admin`/`platform_admin`，并经 `community:read`/`community:manage` 权限校验）
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

**Roles**: `property_admin`/`community_admin`/`platform_admin` + `community:read`
**Query**：`page`、`pageSize`、`status`(可选 `published`|`hidden`|`deleted`)、`keyword`
**说明**：按当前管理员 `property_company_id` 管辖范围，含隐藏/已删除帖子。

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
| `40003` | 400  | 积分功能已停用 | `pointEnabled=false` 时写接口（支付抵扣/转物业/购分/赠分/建设积分等）被服务端拒绝 |




## 物业币错误 (50xxx)


| 错误码     | HTTP | 含义         | 触发场景    |
| ------- | ---- | ---------- | ------- |
| `50001` | 400  | 物业币余额不足    | 物业币不够支付 |
| `50002` | 403  | 积分与物业币不可互兑 | 尝试互兑操作  |
| `50003` | 409  | 未开启物业币抵扣   | 物业币商城规则关闭时尝试抵扣（§8.0 / §69） |
| `50004` | 409  | 未达最低使用金额   | 订单金额未达物业币最低消费门槛 |
| `50005` | 409  | 超过最大抵扣比例   | 抵扣金额超过规则允许的最大比例 |
| `50006` | 400  | 物业币功能已停用 | `coinEnabled=false` 时写接口（充值/兑换/支付抵扣/转物业等）被服务端拒绝 |




## 商家错误 (60xxx)


| 错误码     | HTTP | 含义             | 触发场景                              |
| ------- | ---- | -------------- | --------------------------------- |
| `60001` | 404  | 商家不存在          | 商家ID无效                            |
| `60002` | 409  | 商家入驻审核中        | 商家状态不允许操作                         |
| `60003` | 409  | 审核已处理          | 重复审核                              |
| `60004` | 404  | 商品不存在          | 商品ID无效                            |
| `60005` | 409  | 商家已被踢出         | 商家被物业踢出                           |
| `60006` | 404  | 商家在该物业下未完成挂接配置 | `merchant_property_configs` 无对应记录 |




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
| `94005` | 403  | 提现已被管理员阻止 | 管理员阻止提现 |
| `94006` | 400  | 物业币使用条件未满足 | coinEnabled 等条件不满足 |




## 文件/服务/物业公司错误 (95xxx-97xxx)


| 错误码     | HTTP | 含义      | 触发场景     |
| ------- | ---- | ------- | -------- |
| `95001` | 404  | 文件不存在   | 文件ID无效   |
| `96001` | 404  | 服务不存在   | 服务ID无效   |
| `96002` | 404  | 服务订单不存在 | 服务订单ID无效 |
| `96003` | 409  | 已订阅该服务  | 重复订阅     |
| `97001` | 404  | 物业公司不存在 | 物业公司ID无效 |
| `97002` | 400  | 分成比例合计必须为100% | 业主/商家/物业币/共享积分分成比例合计≠1.0 |
| `97003` | 400  | 未配置微信催缴模板 | 无法发送微信提醒 |
| `97004` | 400  | 预览已过期 | 需重新预览后再发送 |
| `97005` | 409  | 重复发送催缴 | `requestId` 已处理 |
| `97017` | 500  | 催缴消息落库/审计写入失败 | 消息/审计表写入（DataAccessException）兜底，返回明确错误码而非「数据访问失败」（v7.0 从 97011 拆出） |
| `97020` | 400  | 积分功能未开启 | `property_companies.point_enabled=false` 时调用积分写接口（如 §2.6.4.1 转给物业）（2026-09-10 登记） |
| `97021` | 400  | 物业币功能未开启 | `property_companies.coin_enabled=false` 时调用物业币写接口（如 §2.6.4.2 转给物业）（2026-09-10 登记） |



## 分账/配送结算错误 (970xx) — distribution_b_v1


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
       ▼
┌─────────────┐
│  快递员抢单  │  POST /deliveries/{id}/grab
│  或          │  或
│  物业指派    │  POST /deliveries/{id}/assign
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  配送中      │  PATCH /orders/{id}/status
│  status:     │  → delivering
│  delivering  │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  确认收货    │  POST /orders/{id}/confirm
│  status:     │  → completed
│  completed   │  分成记录生成
└─────────────┘
```



## 四级分成流程

```
商品金额 A × 商家抽佣比例 R = 抽佣基础额
                     │
    ┌────────────────┼────────────────┐
    │                │                │
    ▼                ▼                ▼
公司平台盘      物业平台盘       积分成本 / 物业币成本
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
| `biz_type` | `order`/`property_fee`/`coin_recharge`/`service_order`/`service_subscription`/`activity_subscription`/`resident_shop_deposit` |
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
| Flyway 迁移 | `backend/src/main/resources/db/migration/V9__requirement_alignment.sql` | **合并迁移**（原 V9~V12 合并为一个文件），应用启动时自动执行 |
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

- **支付**：微信支付 APIv3（JSAPI 下单），配置项前缀 `wechat.pay.*`：`mch-id`、`app-id`、`api-v3-key`、`private-key-path` / `private-key-base64`、`merchant-serial-number`、`notify-url`、`refund-notify-url`、`profit-sharing`。
- **分账**：微信支付**普通分账**（非电商收付通，**无 `sub_mchid` 二级商户**；收款方为已进件的普通商户号或个人 openid），配置项前缀 `wechat-split.*`：`enabled`、`courier-split-enabled`、`user-refund-enabled`、`auto-withdraw-enabled`、`notify-url`、`notify-url-withdraw`、`receiver-relation-type`。
- 平台级参数一律走环境变量，管理端只维护结算开关（§96.4）。

### 96.2 支付结果回调（唯一已实现的回调入口）

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/payment/notify/wechatpay` | 微信支付结果通知（APIv3）。验签 + 解密 + 标流水 `paid` + 写 `payment_settle_outbox` + 发布事件后**立即应答**（须 5 秒内）。成功返回 `{"code":"SUCCESS"}`；失败返回非 200 + `{"code":"FAIL"}`（微信按策略重推）。业务 finalize 由 `PaymentSettleService` 异步完成（指数退避重试 + 卡死 PROCESSING 原子回收）。 |

> 线上需在微信商户平台把「支付回调地址」配置为 `{域名}/api/v1/payment/notify/wechatpay`，并与 `wechat.pay.notify-url` 保持一致。

### 96.3 回调地址清单与实现状态

| 用途 | 配置项 | 约定地址 | 代码状态 |
| --- | --- | --- | --- |
| 支付结果 | `wechat.pay.notify-url` | `https://<域名>/api/v1/payment/notify/wechatpay` | ✅ 已实现 |
| 退款结果 | `wechat.pay.refund-notify-url` | 需配置 | ⚠️ **无回调入口**：退款状态由 `RefundQueryScheduler` 主动查询兜底 |
| 分账结果 | `wechat-split.notify-url` | `https://<域名>/api/v1/payment/wechat-split/split/notify` | ⚠️ **无回调入口**：分账结果由 `SplitWindowScheduler` / `ReverseFailedScheduler` 主动查询兜底 |
| 提现结果 | `wechat-split.notify-url-withdraw` | `https://<域名>/api/v1/payment/wechat-split/withdraw/notify` | ⚠️ **无回调入口** |

> 上述 3 个 ⚠️ 项：配置项已就绪，但代码未提供接收控制器。当前依赖定时任务主动查询保证最终一致；如需实时性，后续以 V10+ 迁移配套新增回调端点（属 P1，不影响现有资金正确性）。

### 96.4 结算与钱包（仍然有效，原 §92.6 / §92.7）

| 方法 | 路径 | 说明 | 角色 |
| --- | --- | --- | --- |
| `GET` | `/admin/settlement-config` | 结算配置（含 `wechatSplitEnabled` 分账开关） | platform_admin |
| `PUT` | `/admin/settlement-config` | 更新结算配置 | platform_admin |
| `GET` | `/wallet/cards` | 查询某主体钱包卡（`ownerType` / `ownerId`） | 管理端 |
| `GET` | `/technicians/my/wallet/cards` | 技工本人钱包卡 | technician |

### 96.5 定时任务（均已加 ShedLock 分布式锁）

`SplitWindowScheduler`（分账窗口出账）、`ReverseFailedScheduler`（分账回退）、`RefundQueryScheduler`（退款状态查询兜底）、`FulfillmentChoiceTimeoutJob`（履约超时兜底，默认改派平台配送）、`PaymentSettleScheduler`（支付 finalize 兜底与主动对账）。

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

**请求**：`{"amount":100.00,"remark":"活动奖励注入"}`

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

---

## 80. 提现机制增强（A8）

### 80.1 GET `/admin/coin-withdrawals/settings` — 查询提现设置

**角色**：property_admin / platform_admin

**响应**：`{"autoEnabled":true,"periodDays":7,"feeRate":0.006}`

### 80.2 PUT `/admin/coin-withdrawals/settings` — 更新提现设置

**请求**：`{"autoEnabled":true,"periodDays":7}`

### 80.3 PUT `/admin/coin-withdrawals/residents/{id}/block` — 阻止/解阻止居民提现

**请求**：`{"blocked":true,"reason":"违规操作"}`

### 80.4 PUT `/admin/coin-withdrawals/merchants/{id}/block` — 阻止/解阻止商家提现

**请求**：同上

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

**查询参数**：`propertyCompanyId`（可选；平台管理员可指定，物业管理员自动限定本物业）

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

**查询参数**：`page`、`pageSize`

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

**响应**：`Content-Type: text/csv`，UTF-8 BOM，文件名 `arrears_report_yyyyMMdd.csv`

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

## 89.2 GET `/admin/points/consumption-structure` — 积分消耗结构

**Auth**: Required
**Roles**: `property_admin`, `platform_admin`

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

`/admin/cbk/*` 与 §92 框架已就绪，`AutoWithdrawalService` 已对接 `applyCash` 并具备失败回退人工能力；
但**每个商家是否分配独立微信商户号**未确认，自然人（各负责人）不能误用平台商户号提现，故真实打款参数待微信侧确认后启用。

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

## 95.8 一级代理申请 / 退货申请

- **一级代理（个体负责人）住户自主申请**：已实现 `POST /individual-leaders/apply` + `GET /individual-leaders/my-application`，文档见 §（个体负责人）。无需重复开发。
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

> **文档维护**：连云港元序软件有限公司
> **最后更新**：2026-09-06（v7.9.1 前端问题汇总 7 项修复：orders/preview 405、use-condition 404、商家消息 403、组长聊天历史、组长商品 403、组长配送费 404/403、组员简介 404、拿货人缺失）
> **2026-09-13 增补**：新增 §97「电商收付通改造」（商家微信进件 / 四层树收款账号 / 分账回退台账），见文末。

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
  │          ├─ LEGAL_VALIDATION → legal_validating（返回法人验证链接）
  │          └─ SIGNING → signing（返回签约链接）
  ▼
finished（进件成功，回填 sub_mchid）  /  rejected（驳回，可改资料重提）
```

- 本地状态 `init/submitted/auditing/legal_validating/signing/finished/rejected`；`submitted/auditing/legal_validating/signing` 为非终态，由 `ApplymentStatusScheduler` 定时向微信查询推进。
- **终态（finished / rejected）不可再改资料**；驳回后可重新保存资料并重提（重新进件）。

## 97.2 商家端 — 微信进件（`/merchants/my/applyment`）

**角色**：`merchant` / `activity_leader` / `technician`（仅本人店铺）。敏感字段（身份证号、银行卡号、开户名）后端以微信平台证书 RSA-OAEP 加密落库，**不对外回显**。

### 97.2.1 GET `/merchants/my/applyment` — 查询我的进件状态

**响应** `data`（`ApplymentDetailResponse`）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `merchantId` | string | 商家ID |
| `applymentState` | string | 本地状态机，见 §97.1 |
| `wxState` | string | 微信侧状态原值 |
| `organizationType` | string | `ENTERPRISE` / `INDIVIDUAL` / `MICRO` |
| `merchantShortname` | string | 商户简称 |
| `merchantCategory` | string | 经营类目 |
| `contactMobile` | string | 联系人手机号（脱敏） |
| `bankAccountType` | string | 结算账户类型 |
| `bankName` | string | 开户银行全称 |
| `submitted` | boolean | 是否已提交微信（有 applyment_id） |
| `legalValidationUrl` | string | 法人验证链接（仅 `legal_validating` 阶段返回，有时效） |
| `signUrl` | string | 签约链接（仅 `signing` 阶段返回，有时效） |
| `rejectReason` | string | 微信驳回原因 |
| `subMchid` | string | 进件成功后微信分配的二级商户号 |
| `submitMode` | string | `manual`（运营后台）/ `auto`（审核通过自动） |

### 97.2.2 POST `/merchants/my/applyment` — 保存/更新进件资料

**说明**：审核中（非终态）不可修改；驳回后可改资料重提。提交后如配置 `applymentAutoSubmit=true` 会自动进件，否则停留在 `init` 等待运营后台提交。

**请求体** `ApplymentSubmitRequest`：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `organizationType` | string | ✅ | `ENTERPRISE`(企业) / `INDIVIDUAL`(个体户) / `MICRO`(小微,无营业执照) |
| `merchantShortname` | string | ✅ | 商户简称（≤64） |
| `merchantCategory` | string | ❌ | 经营类目 |
| `contactName` | string | ✅ | 联系人姓名 |
| `contactMobile` | string | ✅ | 联系人手机号 |
| `contactEmail` | string | ❌ | 联系人邮箱 |
| `idCardName` | string | ✅ | 法人/经营者姓名 |
| `idCardNumber` | string | ✅ | 身份证号 |
| `idCardCopyMedia` | string | ✅ | 身份证人像面 media_id（先调 97.2.4 上传） |
| `idCardNationalMedia` | string | ✅ | 身份证国徽面 media_id |
| `businessLicenseMedia` | string | ❌ | 营业执照 media_id（小微可空） |
| `bankAccountType` | string | ✅ | `BANK_ACCOUNT_TYPE_CORPORATE`(对公) / `BANK_ACCOUNT_TYPE_PERSONAL`(个人) |
| `bankAccountName` | string | ✅ | 开户名 |
| `bankAccountNumber` | string | ✅ | 银行账号 |
| `bankName` | string | ✅ | 开户银行全称（含支行，≤128） |

**响应**：`ApplymentDetailResponse`（同 97.2.1）。

### 97.2.3 POST `/merchants/my/applyment/submit` — 提交进件到微信

**说明**：资料就绪后提交，进入微信审核流程（`submitted`）。重复提交返回 `99007`。

**响应**：`ApplymentDetailResponse`。

### 97.2.4 POST `/merchants/my/applyment/media` — 上传进件资料图片

**Content-Type**：`multipart/form-data`，参数名 `file`（≤10MB）。
**响应**：`{ "mediaId": "xxxx" }`（微信媒体ID，有效期约 3 天，用于提交进件）。

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

**请求体**（可选）同 97.5.2。仅 `pending` 可操作。核销后不再追回。

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
