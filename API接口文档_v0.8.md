# 物业积分服务系统 — API 接口文档

> **版本**：v0.8  
> **Base URL**：`https://test.yuanxusoftware.top/api/v1`  
> **最后更新**：2026-07-04
> **维护方**：连云港元序软件有限公司

---

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
  - [家庭模块](#3-家庭模块-families)
  - [积分模块](#4-积分模块-points)
  - [物业币模块](#5-物业币模块-property-coins)
  - [商品模块](#6-商品模块-products)
  - [商家模块](#7-商家模块-merchants)
  - [订单模块](#8-订单模块-orders)
  - [配送模块](#9-配送模块-deliveries)
  - [配送规则模块](#10-配送规则模块-delivery-rules)
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
  - [商家提现模块](#23-商家提现模块-merchant-withdrawals)
  - [物业币兑换模块](#24-物业币兑换模块-coin-withdrawals)
  - [特惠推送模块](#25-特惠推送模块-special-offers)
  - [消费过的商家](#26-消费过的商家模块-resident-merchant)
  - [投票记录](#27-投票记录模块-my-votes)
  - [家庭贡献](#28-家庭贡献模块-family-contributions)
  - [活动组订阅状态](#29-活动组订阅状态模块-subscription-status)
  - [邻居搜索](#30-邻居搜索模块-neighbor-search)
  - [账号注销](#31-账号注销模块-account-deletion)
- [管理端 — 公告管理](#32-管理端--公告管理)
  - [管理端 — 物业币提现审核](#33-管理端--物业币提现审核)
  - [管理端 — 商家审核](#34-管理端--商家审核)
  - [管理端 — 操作日志](#35-管理端--操作日志)
  - [管理端 — 积分清零](#36-管理端--积分清零)
  - [管理端 — 个体负责人管理](#37-管理端--个体负责人管理)
  - [管理端 — 线下积分发放](#38-管理端--线下积分发放)
  - [管理端 — 板块负责人管理](#39-管理端--板块负责人管理)
  - [管理端 — 积分池管理](#40-管理端--积分池管理)
  - [管理端 — 统筹管理](#41-管理端--统筹管理)
  - [管理端 — 平台商家管理](#42-管理端--平台商家管理)
  - [管理端 — 商家踢出](#43-管理端--商家踢出)
  - [管理端 — 仪表盘](#44-管理端--仪表盘)
  - [管理端 — 批量商家配置](#45-管理端--批量商家配置)
  - [管理端 — 商家挂接](#46-管理端--商家挂接)
  - [管理端 — 盈利空间](#47-管理端--盈利空间)
  - [管理端 — 权限管理](#48-管理端--权限管理)
  - [管理端 — 业主物业币管控](#49-管理端--业主物业币管控)
  - [管理端 — 物业币过期管理](#50-管理端--物业币过期管理)
  - [管理端 — 物业公司管理](#51-管理端--物业公司管理)
- [枚举值速查表](#枚举值速查表)
- [错误码速查表](#错误码速查表)
- [业务流程说明](#业务流程说明)
- [WebSocket 接口](#websocket-接口)
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

**字段说明**：

**data 字段**：

| 字段 | 类型 | 说明 |
|------|------|------|
| `accessToken` | `String` | 访问令牌 |
| `refreshToken` | `String` | 刷新令牌 |
| `expiresIn` | `Integer` | 过期时间（秒） |
| `tokenType` | `String` | 令牌类型，固定 `Bearer` |

**data.resident 字段**：

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | `String` | 住户ID |
| `name` | `String` | 姓名 |
| `avatarUrl` | `String` | 头像URL |
| `phone` | `String` | 手机号（脱敏） |
| `role` | `String` | 角色 |
| `propertyCompanyId` | `String` | 物业公司ID |
| `communityId` | `String` | 小区ID |
| `familyId` | `String` | 家庭ID |
| `wechatBound` | `Boolean` | 是否绑定微信 |
| `newUser` | `Boolean` | 是否新用户 |
| `status` | `String` | 状态：`active`/`frozen`/`disabled` |

**错误响应**：

| 错误码 | HTTP | 说明 |
|--------|------|------|
| `30003` | 400 | 微信授权失败 |
| `20005` | 403 | 账号被禁用 |

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

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `phone` | `String` | ✅ | `@NotBlank @Pattern("^1\\d{10}$")` | 手机号 |
| `password` | `String` | ✅ | `@NotBlank @Size(min=6, max=32)` | 密码 |

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
      "id": "res_admin01",
      "name": "管理员",
      "phone": "138****5678",
      "role": "property_admin",
      "propertyCompanyId": "pc_demo001",
      "status": "active"
    }
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**错误响应**：

| 错误码 | HTTP | 说明 |
|--------|------|------|
| `30001` | 404 | 手机号或密码错误 |
| `20005` | 403 | 账号被禁用 |

**初始管理员账号**：

| 手机号 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| `13800138000` | `admin123456` | `platform_admin` | 平台超管 |

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

| code格式 | 说明 | 示例 |
|----------|------|------|
| `dev_手机号` | 用指定手机号登录（自动注册） | `dev_13800000001` |
| `dev_任意字符` | 随便一个openid登录 | `dev_abc123` |

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

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `refreshToken` | `String` | ✅ | `@NotBlank` | 刷新令牌 |

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

| 错误码 | HTTP | 说明 |
|--------|------|------|
| `20001` | 401 | Token缺失 |
| `20002` | 401 | Token过期 |
| `20003` | 401 | Token无效 |

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

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `code` | `String` | ✅ | `@NotBlank` | 手机号授权code |
| `encryptedData` | `String` | ❌ | | 加密数据 |
| `iv` | `String` | ❌ | `@Size(max=24)` | 加密向量 |

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

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `keyword` | `String` | ❌ | | 搜索关键词（姓名/手机号） |
| `communityId` | `String` | ❌ | | 小区ID筛选 |
| `building` | `String` | ❌ | | 楼栋筛选 |
| `status` | `String` | ❌ | | 状态筛选：`active`/`frozen`/`disabled` |
| `role` | `String` | ❌ | | 角色筛选 |
| `propertyCompanyId` | `String` | ❌ | | 物业公司ID（超管筛选） |
| `sort` | `String` | ❌ | `-createdAt` | 排序字段 |

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
        "unit": "5单元",
        "room": "502",
        "familyId": "fam_demo001",
        "pointBalance": 500,
        "coinBalance": 99.50,
        "coinFrozen": false,
        "status": "active",
        "createdAt": "2026-03-15 10:00:00"
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
|------|------|------|
| `list[].id` | `String` | 住户ID |
| `list[].name` | `String` | 姓名 |
| `list[].phone` | `String` | 手机号（脱敏） |
| `list[].avatarUrl` | `String` | 头像URL |
| `list[].gender` | `Integer` | 性别：0未知 1男 2女 |
| `list[].age` | `Integer` | 年龄 |
| `list[].maritalStatus` | `String` | 婚姻状态：single/married/divorced/widowed |
| `list[].hasChildren` | `Boolean` | 是否有子女 |
| `list[].role` | `String` | 角色：`resident`/`property_admin`/`platform_admin`/`merchant`/`courier`/`coordinator`/`sector_leader`/`individual_leader`/`activity_leader`/`technician` |
| `list[].userType` | `String` | 用户类型：`owner`（业主）/`tenant`（租户） |
| `list[].propertyCompanyId` | `String` | 物业公司ID |
| `list[].communityId` | `String` | 小区ID |
| `list[].communityName` | `String` | 小区名称 |
| `list[].building` | `String` | 楼栋 |
| `list[].unit` | `String` | 单元 |
| `list[].room` | `String` | 房号 |
| `list[].familyId` | `String` | 家庭ID |
| `list[].pointBalance` | `Integer` | 积分余额 |
| `list[].coinBalance` | `BigDecimal` | 物业币余额 |
| `list[].coinFrozen` | `Boolean` | 物业币是否冻结 |
| `list[].status` | `String` | 状态：`active`/`frozen`/`disabled` |
| `list[].createdAt` | `String` | 注册时间 |

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

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `name` | `String` | ✅ | `@NotBlank @Size(max=50)` | 姓名 |
| `phone` | `String` | ✅ | `@NotBlank @Pattern("^1\\d{10}$")` | 手机号 |
| `avatarUrl` | `String` | ❌ | `@Size(max=500)` | 头像URL |
| `gender` | `Integer` | ❌ | `@Min(0) @Max(2)` | 0未知 1男 2女 |
| `age` | `Integer` | ❌ | `@Min(0) @Max(150)` | 年龄 |
| `maritalStatus` | `String` | ❌ | `@Size(max=20)` | 婚姻状态：`single`/`married`/`divorced`/`widowed` |
| `hasChildren` | `Boolean` | ❌ | | 是否有子女 |
| `userType` | `String` | ✅ | `@Pattern("owner\|tenant")` | owner业主/tenant租户 |
| `role` | `String` | ✅ | | 角色（仅支持resident） |
| `propertyCompanyId` | `String` | ✅ | | 物业公司ID |
| `communityId` | `String` | ✅ | | 小区ID |
| `building` | `String` | ❌ | `@Size(max=20)` | 楼栋 |
| `unit` | `String` | ❌ | `@Size(max=20)` | 单元 |
| `room` | `String` | ❌ | `@Size(max=20)` | 房号 |

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

| 错误码 | HTTP | 说明 |
|--------|------|------|
| `30005` | 409 | 该手机号在当前物业公司下已存在 |

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

**成功响应**：同 `/auth/profile`，额外包含 `totalConsumption`、`totalOrders`

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

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `name` | `String` | ❌ | `@Size(max=50)` | 姓名 |
| `avatarUrl` | `String` | ❌ | `@Size(max=500)` | 头像URL |
| `gender` | `Integer` | ❌ | `@Min(0) @Max(2)` | 0未知 1男 2女 |
| `age` | `Integer` | ❌ | `@Min(0) @Max(150)` | 年龄 |
| `maritalStatus` | `String` | ❌ | `@Size(max=20)` | 婚姻状态：`single`/`married`/`divorced`/`widowed` |
| `hasChildren` | `Boolean` | ❌ | | 是否有子女 |
| `building` | `String` | ❌ | `@Size(max=20)` | 楼栋 |
| `unit` | `String` | ❌ | `@Size(max=20)` | 单元 |
| `room` | `String` | ❌ | `@Size(max=20)` | 房号 |

**成功响应**：更新后的用户详情

---

### 2.7 GET `/residents/{id}` — 业主详情

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`, `resident`（本人）

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `String` | ✅ | 业主ID |

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
    "updatedAt": "2026-06-28 14:30:00"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---

### 2.8 PUT `/residents/{id}` — 更新业主信息

**Auth**: Required  
**Roles**: `resident`（本人）, `property_admin`, `platform_admin`

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

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `String` | ✅ | 业主ID |

**请求体**：

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `name` | `String` | ❌ | `@Size(max=50)` | 姓名 |
| `avatarUrl` | `String` | ❌ | `@Size(max=500)` | 头像URL |
| `gender` | `Integer` | ❌ | `@Min(0) @Max(2)` | 0未知 1男 2女 |
| `age` | `Integer` | ❌ | `@Min(0) @Max(150)` | 年龄 |
| `maritalStatus` | `String` | ❌ | `@Size(max=20)` | 婚姻状态：`single`/`married`/`divorced`/`widowed` |
| `hasChildren` | `Boolean` | ❌ | | 是否有子女 |
| `building` | `String` | ❌ | `@Size(max=20)` | 楼栋 |
| `unit` | `String` | ❌ | `@Size(max=20)` | 单元 |
| `room` | `String` | ❌ | `@Size(max=20)` | 房号 |

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

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `status` | `String` | ✅ | `@NotBlank @Pattern("active|frozen|disabled")` | 目标状态 |
| `reason` | `String` | ❌ | `@Size(max=200)` | 操作原因 |

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

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**请求示例**：

```bash
curl -X DELETE https://test.yuanxusoftware.top/api/v1/residents/res_b7m9x1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `String` | ✅ | 住户ID |

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
- 物业管理员只能删除本公司下的住户
- 平台管理员可删除任意住户

**错误响应**：

| 错误码 | HTTP | 说明 |
|--------|------|------|
| `30001` | 404 | 住户不存在 |
| `40003` | 403 | 无权操作该住户 |

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
    "name": "张家",
    "building": "3栋",
    "unit": "5单元",
    "room": "502",
    "communityId": "com_demo001"
  }'
```

**请求体**：

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `name` | `String` | ✅ | `@NotBlank @Size(min=1, max=50)` | 家庭名称 |
| `building` | `String` | ✅ | `@NotBlank @Size(max=20)` | 楼栋 |
| `unit` | `String` | ❌ | `@Size(max=20)` | 单元 |
| `room` | `String` | ✅ | `@NotBlank @Size(max=20)` | 房号 |
| `communityId` | `String` | ✅ | `@NotBlank` | 小区ID |

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

| 错误码 | HTTP | 说明 |
|--------|------|------|
| `30002` | 409 | 家庭人数超限（最多15人） |

---

### 3.2 GET `/families/me` — 获取我的家庭信息

**Auth**: Required  
**Roles**: `resident`

**成功响应**：

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | `String` | 家庭ID |
| `name` | `String` | 家庭名称 |
| `ownerId` | `String` | 户主ID |
| `ownerName` | `String` | 户主姓名 |
| `propertyCompanyId` | `String` | 物业公司ID |
| `communityId` | `String` | 小区ID |
| `communityName` | `String` | 小区名称 |
| `building` | `String` | 楼栋 |
| `unit` | `String` | 单元 |
| `room` | `String` | 房号 |
| `memberCount` | `Integer` | 成员人数 |
| `pointBalance` | `Integer` | 家庭积分余额 |
| `coinBalance` | `BigDecimal` | 家庭物业币余额 |
| `voteWeight` | `Integer` | 投票权重 |
| `auditStatus` | `String` | 审核状态 |
| `status` | `String` | 状态 |
| `members[]` | `Array` | 家庭成员列表 |
| `members[].id` | `String` | 成员ID |
| `members[].name` | `String` | 成员姓名 |
| `members[].phone` | `String` | 手机号（脱敏） |
| `members[].avatarUrl` | `String` | 头像URL |
| `members[].relation` | `String` | 关系：`owner`/`spouse`/`child`/`parent`/`sibling`/`other` |
| `members[].isOwner` | `Boolean` | 是否户主 |
| `members[].pointBalance` | `Integer` | 成员积分余额 |
| `members[].coinBalance` | `BigDecimal` | 成员物业币余额 |
| `members[].status` | `String` | 状态 |
| `members[].joinedAt` | `String` | 加入时间 |
| `createdAt` | `String` | 创建时间 |
| `updatedAt` | `String` | 更新时间 |

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

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `name` | `String` | ❌ | `@Size(max=50)` | 家庭名称 |
| `building` | `String` | ❌ | `@Size(max=20)` | 楼栋 |
| `unit` | `String` | ❌ | `@Size(max=20)` | 单元 |
| `room` | `String` | ❌ | `@Size(max=20)` | 房号 |

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

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `phone` | `String` | ✅ | `@NotBlank @Pattern("^1\\d{10}$")` | 手机号 |
| `name` | `String` | ❌ | `@Size(max=50)` | 姓名 |
| `relation` | `String` | ✅ | `@NotBlank` | 关系：`owner`/`spouse`/`child`/`parent`/`sibling`/`other` |

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

| 错误码 | HTTP | 说明 |
|--------|------|------|
| `30001` | 404 | 业主不存在 |
| `30002` | 409 | 家庭人数超限 |

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

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `reason` | `String` | ❌ | 移除原因 |

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

| 错误码 | HTTP | 说明 |
|--------|------|------|
| `30001` | 404 | 家庭或成员不存在 |
| `40003` | 403 | 非户主无权操作 |

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

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `pointType` | `String` | ❌ | | `earn`/`spend`/`transfer`/`expire`/`pool_in` |
| `source` | `String` | ❌ | | 来源筛选 |
| `startDate` | `String` | ❌ | | 开始日期 |
| `endDate` | `String` | ❌ | | 结束日期 |
| `familyId` | `String` | ❌ | | 家庭ID |
| `sort` | `String` | ❌ | `-createdAt` | 排序 |

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

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `familyId` | `String` | ✅ | `@NotBlank` | 家庭ID |
| `toMemberId` | `String` | ✅ | `@NotBlank` | 目标成员ID |
| `amount` | `Integer` | ✅ | `@NotNull @Min(1)` | 转移数量 |
| `remark` | `String` | ❌ | `@Size(max=200)` | 备注 |

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

| 错误码 | HTTP | 说明 |
|--------|------|------|
| `40001` | 400 | 积分余额不足 |

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

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `propertyFeeId` | `String` | ✅ | `@NotBlank` | 物业费账单ID |
| `points` | `Integer` | ✅ | `@NotNull @Min(1)` | 抵扣积分数量 |
| `familyId` | `String` | ❌ | | 使用家庭积分池 |

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

| 错误码 | HTTP | 说明 |
|--------|------|------|
| `40001` | 400 | 积分余额不足 |
| `71001` | 404 | 物业费账单不存在 |

---

### 4.7 POST `/points/redeem` — 积分兑换（100积分=1元）

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

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `points` | `Integer` | ✅ | `@NotNull @Min(100)` | 兑换积分（最少100） |

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

| 错误码 | HTTP | 说明 |
|--------|------|------|
| `40001` | 400 | 积分余额不足 |

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

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `residentId` | `String` | ✅ | `@NotBlank` | 用户ID |
| `pointAmount` | `Integer` | ✅ | `@NotNull @Min(1)` | 积分数量 |
| `source` | `String` | ✅ | `@NotBlank` | 来源 |
| `description` | `String` | ❌ | `@Size(max=200)` | 描述 |
| `sourceId` | `String` | ❌ | | 来源ID |
| `familyId` | `String` | ❌ | | 家庭ID |

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

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `residentId` | `String` | ✅ | `@NotBlank` | 用户ID |
| `pointAmount` | `Integer` | ✅ | `@NotNull @Min(1)` | 积分数量 |
| `source` | `String` | ✅ | `@NotBlank` | 来源 |
| `description` | `String` | ❌ | `@Size(max=200)` | 描述 |
| `sourceId` | `String` | ❌ | | 来源ID |
| `familyId` | `String` | ❌ | | 家庭ID |
| `useFamilyPool` | `Boolean` | ❌ | | 是否优先使用家庭积分池 |

**错误响应**：

| 错误码 | HTTP | 说明 |
|--------|------|------|
| `40001` | 400 | 积分余额不足 |

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

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `coinType` | `String` | ❌ | | `earn`/`spend`/`recharge`/`freeze`/`unfreeze`/`expire`/`withdraw` |
| `source` | `String` | ❌ | | 来源筛选 |
| `startDate` | `String` | ❌ | | 开始日期 |
| `endDate` | `String` | ❌ | | 结束日期 |
| `familyId` | `String` | ❌ | | 家庭ID |
| `sort` | `String` | ❌ | `-createdAt` | 排序 |

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

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `amount` | `BigDecimal` | ✅ | `@NotNull @DecimalMin("0.01")` | 充值金额 |

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "recordId": "coin_demo001",
    "residentId": "res_b7m9x1",
    "amount": 50.00,
    "balanceAfter": 149.50,
    "familyBalanceAfter": 300.00,
    "rechargedAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

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

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `hidden` | `Boolean` | ✅ | `@NotNull` | 是否隐藏：`true`隐藏，`false`显示 |

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

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `residentId` | `String` | ✅ | `@NotBlank` | 用户ID |
| `coinAmount` | `BigDecimal` | ✅ | `@NotNull @DecimalMin("0.01")` | 物业币数量 |
| `source` | `String` | ✅ | `@NotBlank` | 来源 |
| `description` | `String` | ❌ | `@Size(max=200)` | 描述 |
| `sourceId` | `String` | ❌ | | 来源ID |

---

### 5.8 POST `/property-coins/spend` — 消费物业币（管理员）

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**请求体**：

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `residentId` | `String` | ✅ | `@NotBlank` | 用户ID |
| `coinAmount` | `BigDecimal` | ✅ | `@NotNull @DecimalMin("0.01")` | 物业币数量 |
| `source` | `String` | ✅ | `@NotBlank` | 来源 |
| `description` | `String` | ❌ | `@Size(max=200)` | 描述 |
| `sourceId` | `String` | ❌ | | 来源ID |
| `familyId` | `String` | ❌ | | 家庭ID |
| `useFamilyPool` | `Boolean` | ❌ | | 是否优先使用家庭物业币 |

**错误响应**：

| 错误码 | HTTP | 说明 |
|--------|------|------|
| `50001` | 400 | 物业币余额不足 |
| `93003` | 403 | 物业币已冻结 |

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

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `residentId` | `String` | ✅ | `@NotBlank` | 用户ID |
| `amount` | `BigDecimal` | ❌ | `@DecimalMin("0")` | 冻结金额（0=全部冻结） |
| `reason` | `String` | ✅ | `@NotBlank @Size(min=1, max=200)` | 冻结原因 |

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

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `residentId` | `String` | ✅ | `@NotBlank` | 用户ID |
| `frozenRecordId` | `String` | ❌ | | 冻结记录ID（不传解冻全部） |
| `reason` | `String` | ✅ | `@NotBlank @Size(min=1, max=200)` | 解冻原因 |

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

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `keyword` | `String` | ❌ | | 搜索关键词 |
| `merchantId` | `String` | ❌ | | 商家ID |
| `category` | `String` | ❌ | | 分类 |
| `status` | `String` | ❌ | | `active`/`inactive`/`sold_out` |
| `propertyCompanyId` | `String` | ❌ | | 物业公司ID |
| `sort` | `String` | ❌ | `-createdAt` | 排序 |

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

---

### 6.2 GET `/products/{id}` — 商品详情

**Auth**: Optional  
**Roles**: `any`

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `String` | ✅ | 商品ID |

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

---

### 6.3 POST `/products` — 创建商品

**Auth**: Required  
**Roles**: `merchant`

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

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `name` | `String` | ✅ | `@NotBlank @Size(max=100)` | 商品名称 |
| `description` | `String` | ❌ | | 描述 |
| `coverUrl` | `String` | ❌ | `@Size(max=500)` | 封面图 |
| `price` | `BigDecimal` | ✅ | `@NotNull @DecimalMin("0.01")` | 价格 |
| `memberPrice` | `BigDecimal` | ❌ | `@DecimalMin("0.01")` | 业主折扣价 |
| `pointPrice` | `Integer` | ❌ | `@Min(1)` | 积分兑换价格 |
| `stock` | `Integer` | ❌ | `@Min(0)` | 库存 |
| `category` | `String` | ❌ | `@Size(max=50)` | 分类 |
| `status` | `String` | ❌ | | 状态 |

---

### 6.4 PUT `/products/{id}` — 更新商品

**Auth**: Required  
**Roles**: `merchant`（仅自己的商家）

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

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `String` | ✅ | 商品ID |

**请求体**：

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `name` | `String` | ❌ | `@Size(max=100)` | 商品名称 |
| `description` | `String` | ❌ | | 描述 |
| `coverUrl` | `String` | ❌ | `@Size(max=500)` | 封面图 |
| `price` | `BigDecimal` | ❌ | `@DecimalMin("0.01")` | 价格 |
| `memberPrice` | `BigDecimal` | ❌ | `@DecimalMin("0.01")` | 业主折扣价 |
| `pointPrice` | `Integer` | ❌ | `@Min(1)` | 积分兑换价格 |
| `stock` | `Integer` | ❌ | `@Min(0)` | 库存 |
| `category` | `String` | ❌ | `@Size(max=50)` | 分类 |
| `status` | `String` | ❌ | | 状态 |

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
**Roles**: `merchant`（仅自己的商家）

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

| 错误码 | HTTP | 说明 |
|--------|------|------|
| `30001` | 404 | 商品不存在 |
| `40003` | 403 | 无权操作该商品 |

---

## 7. 商家模块 (Merchants)

### 7.1 GET `/merchants` — 商家列表

**Auth**: Optional  
**Roles**: `any`

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/merchants?page=1&pageSize=10&merchantLevel=official_certified"
```

**Query 参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `keyword` | `String` | ❌ | | 搜索关键词 |
| `category` | `String` | ❌ | | 分类 |
| `merchantLevel` | `String` | ❌ | | `official_certified`/`property_certified`/`normal` |
| `auditStatus` | `String` | ❌ | | `pending_audit`/`approved`/`rejected`（管理端用，筛选审核状态） |
| `propertyCompanyId` | `String` | ❌ | | 物业公司ID |
| `sort` | `String` | ❌ | `-rankOrder` | 排序 |

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
        "address": "小区东门",
        "deliveryFee": 0.30,
        "freeDeliveryThreshold": 50.00,
        "memberDiscountPrice": null,
        "coinRebateRate": 0.05,
        "coinRebateEnabled": true,
        "rankOrder": 1,
        "auditStatus": "approved",
        "status": "active",
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

| 字段 | 类型 | 说明 |
|------|------|------|
| `list[].id` | `String` | 商家ID |
| `list[].platformMerchantId` | `String` | 平台商家ID |
| `list[].propertyCompanyId` | `String` | 物业公司ID |
| `list[].name` | `String` | 商家名称 |
| `list[].description` | `String` | 商家描述 |
| `list[].coverUrls` | `List<String>` | 封面图URL列表 |
| `list[].videoUrl` | `String` | 视频URL |
| `list[].qrCodeUrl` | `String` | 二维码URL |
| `list[].merchantLevel` | `String` | 商家等级：`official_certified`/`property_certified`/`normal` |
| `list[].levelWeight` | `Integer` | 等级权重 |
| `list[].category` | `String` | 分类 |
| `list[].businessHours` | `String` | 营业时间 |
| `list[].contactPhone` | `String` | 联系电话 |
| `list[].address` | `String` | 地址 |
| `list[].deliveryFee` | `BigDecimal` | 配送费 |
| `list[].freeDeliveryThreshold` | `BigDecimal` | 免配送费门槛 |
| `list[].memberDiscountPrice` | `BigDecimal` | 会员折扣价 |
| `list[].coinRebateRate` | `BigDecimal` | 物业币返利率 |
| `list[].coinRebateEnabled` | `Boolean` | 是否启用物业币返利 |
| `list[].rankOrder` | `Integer` | 排序权重 |
| `list[].auditStatus` | `String` | 审核状态：`pending_audit`/`approved`/`rejected` |
| `list[].status` | `String` | 状态：`active`/`frozen`/`disabled` |
| `list[].createdAt` | `String` | 创建时间 |

---

### 7.2 GET `/merchants/my` — 我的商家

**Auth**: Required  
**Roles**: `merchant`

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
    "address": "小区东门",
    "deliveryFee": 0.30,
    "freeDeliveryThreshold": 50.00,
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

---

### 7.3 GET `/merchants/{id}` — 商家详情

**Auth**: Optional  
**Roles**: `any`

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `String` | ✅ | 商家ID |

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
    "address": "小区东门",
    "deliveryFee": 0.30,
    "freeDeliveryThreshold": 50.00,
    "memberDiscountPrice": null,
    "coinRebateRate": 0.05,
    "coinRebateEnabled": true,
    "rankOrder": 1,
    "auditStatus": "approved",
    "status": "active",
    "createdAt": "2026-03-15 10:00:00"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

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

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `name` | `String` | ✅ | `@NotBlank @Size(max=100)` | 商家名称 |
| `category` | `String` | ✅ | `@NotBlank` | 分类 |
| `contactPhone` | `String` | ✅ | `@NotBlank @Size(max=20)` | 联系电话 |
| `address` | `String` | ❌ | `@Size(max=200)` | 地址 |
| `coverUrls` | `List<String>` | ❌ | | 封面图片URL |
| `videoUrl` | `String` | ❌ | `@Size(max=500)` | 视频URL |
| `description` | `String` | ❌ | | 描述 |
| `businessHours` | `String` | ❌ | `@Size(max=50)` | 营业时间 |
| `deliveryFee` | `BigDecimal` | ❌ | | 配送费 |
| `freeDeliveryThreshold` | `BigDecimal` | ❌ | | 满额免配送费 |
| `commissionRate` | `BigDecimal` | ❌ | | 建议抽佣比例 |
| `pointExchangeRate` | `BigDecimal` | ❌ | | 建议积分兑换比 |

**错误响应**：

| 错误码 | HTTP | 说明 |
|--------|------|------|
| `30004` | 409 | 该商家已在当前物业下挂接 |

---

### 7.5 PUT `/merchants/{id}` — 更新商家信息

**Auth**: Required  
**Roles**: `merchant`（仅自己的商家）, `property_admin`, `platform_admin`

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

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `String` | ✅ | 商家ID |

**请求体**：

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `name` | `String` | ❌ | `@Size(max=100)` | 商家名称 |
| `description` | `String` | ❌ | | 描述 |
| `coverUrls` | `List<String>` | ❌ | | 封面图片URL |
| `videoUrl` | `String` | ❌ | `@Size(max=500)` | 视频URL |
| `businessHours` | `String` | ❌ | `@Size(max=50)` | 营业时间 |
| `contactPhone` | `String` | ❌ | `@Size(max=20)` | 联系电话 |
| `address` | `String` | ❌ | `@Size(max=200)` | 地址 |
| `deliveryFee` | `BigDecimal` | ❌ | | 配送费 |
| `freeDeliveryThreshold` | `BigDecimal` | ❌ | | 满额免配送费 |
| `rankOrder` | `Integer` | ❌ | | 排序权重 |

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

## 8. 订单模块 (Orders)

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

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `merchantId` | `String` | ✅ | `@NotBlank` | 商家ID |
| `items` | `List` | ✅ | `@NotEmpty @Valid` | 商品列表 |
| `items[].productId` | `String` | ✅ | `@NotBlank` | 商品ID |
| `items[].quantity` | `int` | ✅ | `@Min(1)` | 数量 |
| `address` | `String` | ❌ | `@Size(max=200)` | 配送地址 |
| `contactPhone` | `String` | ❌ | `@Size(max=20)` | 联系电话 |
| `remark` | `String` | ❌ | `@Size(max=500)` | 备注 |
| `familyId` | `String` | ❌ | | 家庭ID |

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "orderId": "ord_demo001",
    "orderNo": "ORD202607010001",
    "merchantId": "mch_demo001",
    "merchantName": "万达超市",
    "totalAmount": 10.00,
    "deliveryFee": 0.30,
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

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `paymentMethod` | `String` | ✅ | `@NotBlank` | `point`/`coin`/`wechat`/`mixed` |
| `pointUsed` | `Integer` | ❌ | `@Min(0)` | 使用积分 |
| `coinUsed` | `BigDecimal` | ❌ | `@Min(0)` | 使用物业币 |
| `wechatPayCode` | `String` | ❌ | | 微信支付code |
| `familyId` | `String` | ❌ | | 使用家庭余额 |

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
    "cashAmount": 0.00,
    "paidAt": "2026-07-01 15:24:00",
    "pointEarned": 10,
    "coinEarned": 0.30
  },
  "timestamp": "2026-07-01 15:24:00"
}
```

**错误响应**：

| 错误码 | HTTP | 说明 |
|--------|------|------|
| `70001` | 404 | 订单不存在 |
| `70002` | 409 | 订单状态不允许操作 |
| `70003` | 409 | 并发冲突 |
| `40001` | 400 | 积分余额不足 |
| `50001` | 400 | 物业币余额不足 |

---

### 8.3 GET `/orders/my` — 我的订单

**Auth**: Required  
**Roles**: `resident`

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/orders/my?page=1&pageSize=10&orderStatus=paid" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

---

### 8.4 GET `/orders` — 订单列表（商家/管理员）

**Auth**: Required  
**Roles**: `merchant`, `property_admin`, `platform_admin`

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/orders?page=1&pageSize=10&orderStatus=paid&startDate=2026-07-01" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `orderStatus` | `String` | ❌ | | `pending`/`paid`/`delivering`/`completed`/`cancelled`/`refunding`/`refunded` |
| `merchantId` | `String` | ❌ | | 商家ID |
| `residentId` | `String` | ❌ | | 业主ID |
| `propertyCompanyId` | `String` | ❌ | | 物业公司ID |
| `startDate` | `String` | ❌ | | 开始日期 `yyyy-MM-dd` |
| `endDate` | `String` | ❌ | | 结束日期 `yyyy-MM-dd` |
| `sort` | `String` | ❌ | `-createdAt` | 排序 |

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
        "deliveryFee": 0.30,
        "paymentMethod": "mixed",
        "pointUsed": 100,
        "coinUsed": 5.00,
        "cashAmount": 0.00,
        "orderStatus": "paid",
        "deliveryAddress": "3栋5单元502",
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

---

### 8.5 GET `/orders/{id}` — 订单详情

**Auth**: Required  
**Roles**: `resident`（本人）, `property_admin`, `merchant`, `platform_admin`

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `String` | ✅ | 订单ID |

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
    "deliveryFee": 0.30,
    "paymentMethod": "mixed",
    "pointUsed": 100,
    "coinUsed": 5.00,
    "cashAmount": 0.00,
    "orderStatus": "paid",
    "deliveryAddress": "3栋5单元502",
    "contactPhone": "138****1234",
    "remark": "请尽快配送",
    "courierId": null,
    "courierName": null,
    "deliveryId": null,
    "paidAt": "2026-07-01 15:24:00",
    "completedAt": null,
    "cancelledAt": null,
    "createdAt": "2026-07-01 15:23:33",
    "updatedAt": "2026-07-01 15:24:00"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

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
        "fee": 0.30,
        "courierEarning": 0.35,
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

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `status` | `String` | ❌ | | `accepted`/`delivering`/`completed`/`cancelled` |
| `sort` | `String` | ❌ | `-createdAt` | 排序 |

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
        "fee": 0.30,
        "courierEarning": 0.35,
        "status": "accepted",
        "acceptedAt": "2026-07-01 15:25:00",
        "timeoutAt": "2026-07-01 15:55:00",
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
    "courierEarning": 0.35,
    "acceptedAt": "2026-07-01 15:25:00",
    "timeoutAt": "2026-07-01 15:55:00"
  },
  "timestamp": "2026-07-01 15:25:00"
}
```

**错误响应**：

| 错误码 | HTTP | 说明 |
|--------|------|------|
| `80001` | 409 | 快递员抢单失败（已被抢走） |

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
    "proofImageUrls": ["https://cdn.example.com/proof/xxx.jpg"],
    "remark": "已送达"
  }'
```

**请求体**：

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `proofImageUrls` | `List<String>` | ❌ | `@Size(max=5)` | 证明图片（最多5张） |
| `remark` | `String` | ❌ | `@Size(max=200)` | 备注 |

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "dlv_demo001",
    "orderId": "ord_demo001",
    "status": "completed",
    "courierEarning": 0.35,
    "deliveredAt": "2026-07-01 15:40:00"
  },
  "timestamp": "2026-07-01 15:40:00"
}
```

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

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `String` | ✅ | 配送ID |

**请求体**：

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `status` | `String` | ✅ | `@NotBlank @Pattern("delivering|completed|cancelled")` | 目标状态 |

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

## 10. 配送规则模块 (Delivery Rules)

### 10.1 GET `/delivery-rules` — 配送规则列表

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**Query 参数**：`page`, `pageSize`, `propertyCompanyId`

---

### 10.2 GET `/delivery-rules/{id}` — 配送规则详情

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `String` | ✅ | 配送规则ID |

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
    "courierPerOrder": 0.35,
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
    "courierPerOrder": 0.35,
    "timeoutMinutes": 30
  }'
```

**请求体**：

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `name` | `String` | ✅ | `@NotBlank @Size(max=100)` | 规则名称 |
| `baseFee` | `BigDecimal` | ✅ | `@NotNull @DecimalMin("0.00")` | 起步价 |
| `perKgFee` | `BigDecimal` | ❌ | | 每公斤费用 |
| `freeThreshold` | `BigDecimal` | ❌ | | 免费门槛 |
| `maxWeight` | `BigDecimal` | ❌ | | 最大重量 |
| `maxDistance` | `BigDecimal` | ❌ | | 最大距离 |
| `courierShareRate` | `BigDecimal` | ❌ | | 快递员分成比例 |
| `courierPerOrder` | `BigDecimal` | ❌ | `@DecimalMin("0.00")` | 每单报酬 |
| `timeoutMinutes` | `Integer` | ❌ | `@Min(1)` | 超时时间 |
| `propertyCompanyId` | `String` | ❌ | | 物业公司ID |

**错误响应**：

| 错误码 | HTTP | 说明 |
|--------|------|------|
| `80003` | 409 | 该物业已存在配送规则 |

---

### 10.4 PUT `/delivery-rules/{id}` — 更新配送规则

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `String` | ✅ | 配送规则ID |

**请求示例**：

```bash
curl -X PUT https://test.yuanxusoftware.top/api/v1/delivery-rules/dr_demo001 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "name": "默认配送规则（更新）",
    "baseFee": 0.50,
    "courierPerOrder": 0.40,
    "timeoutMinutes": 25
  }'
```

**请求体**：

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `name` | `String` | ❌ | `@Size(max=100)` | 规则名称 |
| `baseFee` | `BigDecimal` | ❌ | `@DecimalMin("0.00")` | 起步价 |
| `perKgFee` | `BigDecimal` | ❌ | | 每公斤费用 |
| `freeThreshold` | `BigDecimal` | ❌ | | 免费门槛 |
| `maxWeight` | `BigDecimal` | ❌ | | 最大重量 |
| `maxDistance` | `BigDecimal` | ❌ | | 最大距离 |
| `courierShareRate` | `BigDecimal` | ❌ | | 快递员分成比例 |
| `courierPerOrder` | `BigDecimal` | ❌ | `@DecimalMin("0.00")` | 每单报酬 |
| `timeoutMinutes` | `Integer` | ❌ | `@Min(1)` | 超时时间 |

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
    "courierPerOrder": 0.40,
    "timeoutMinutes": 25,
    "propertyCompanyId": "pc_demo001",
    "createdAt": "2026-06-01 10:00:00",
    "updatedAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

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

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `title` | `String` | ✅ | `@NotBlank @Size(max=200)` | 标题 |
| `content` | `String` | ✅ | `@NotBlank` | 内容 |
| `announcementType` | `String` | ✅ | `@NotBlank` | 公告类型 |
| `propertyCompanyId` | `String` | ❌ | | 物业公司ID |
| `communityId` | `String` | ❌ | | 小区ID |
| `merchantId` | `String` | ❌ | | 商家ID |
| `coverUrls` | `List<String>` | ❌ | | 封面图片 |
| `targetRoles` | `List<String>` | ❌ | | 目标角色 |
| `targetBuildings` | `List<String>` | ❌ | | 目标楼栋 |
| `collectEnabled` | `Boolean` | ❌ | | 是否启用信息收集 |
| `collectFields` | `List<Object>` | ❌ | | 收集字段定义 |
| `status` | `String` | ❌ | | `draft`/`published`/`archived` |

---

### 11.2 GET `/announcements` — 公告列表

**Auth**: Optional  
**Roles**: `any`

**Query 参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `announcementType` | `String` | ❌ | | 公告类型 |
| `communityId` | `String` | ❌ | | 小区ID |
| `merchantId` | `String` | ❌ | | 商家ID |
| `readStatus` | `String` | ❌ | | 阅读状态 |
| `sort` | `String` | ❌ | `-publishedAt` | 排序 |

---

### 11.3 GET `/announcements/{id}` — 公告详情

**Auth**: Optional  
**Roles**: `any`

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `String` | ✅ | 公告ID |

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

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `String` | ✅ | 公告ID |

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

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `title` | `String` | ❌ | `@Size(max=200)` | 标题 |
| `content` | `String` | ❌ | | 内容 |
| `announcementType` | `String` | ❌ | | 公告类型 |
| `coverUrls` | `List<String>` | ❌ | | 封面图片 |
| `targetRoles` | `List<String>` | ❌ | | 目标角色 |
| `targetBuildings` | `List<String>` | ❌ | | 目标楼栋 |
| `collectEnabled` | `Boolean` | ❌ | | 是否启用信息收集 |
| `collectFields` | `List<Object>` | ❌ | | 收集字段定义 |
| `status` | `String` | ❌ | | `draft`/`published`/`archived` |

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

| 错误码 | HTTP | 说明 |
|--------|------|------|
| `90001` | 404 | 公告不存在 |
| `90003` | 409 | 已提交过收集信息 |

---

## 12. 消息模块 (Messages)

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

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `toId` | `String` | ✅ | `@NotBlank` | 接收方ID |
| `content` | `String` | ✅ | `@NotBlank @Size(min=1, max=500)` | 消息内容 |

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

**错误响应**：

| 错误码 | HTTP | 说明 |
|--------|------|------|
| `90013` | 409 | 今日邻居私聊次数已达上限 |

---

### 12.3 POST `/messages/broadcast` — 广播消息

**Auth**: Required  
**Roles**: `resident`

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/messages/broadcast \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "content": "请问有谁家需要维修服务？",
    "toRole": "technician"
  }'
```

---

### 12.4 POST `/messages` — 发送消息

**Auth**: Required  
**Roles**: `resident`, `property_admin`, `platform_admin`

**请求示例**：

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

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `toId` | `String` | ✅ | `@NotBlank` | 接收方ID |
| `content` | `String` | ✅ | `@NotBlank @Size(min=1, max=500)` | 消息内容 |
| `messageType` | `String` | ✅ | `@NotBlank` | 消息类型：`property_message`/`resident_message`/`system_message` |
| `chatType` | `String` | ✅ | `@NotBlank @Pattern("private|group")` | 聊天类型：`private`/`group` |

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
**Roles**: `resident`, `property_admin`, `platform_admin`

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/messages?page=1&pageSize=20&messageType=resident_message&chatType=private" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `messageType` | `String` | ❌ | | 消息类型筛选 |
| `chatType` | `String` | ❌ | | `private`/`group` |
| `readStatus` | `String` | ❌ | | `read`/`unread` |
| `fromId` | `String` | ❌ | | 发送方ID筛选 |
| `sort` | `String` | ❌ | `-createdAt` | 排序 |

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
**Roles**: `resident`, `property_admin`, `platform_admin`

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `String` | ✅ | 消息ID |

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

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `file` | `MultipartFile` | ✅ | 文件二进制 |
| `category` | `String` | ✅ | `avatar`/`announcement`/`merchant`/`service`/`activity` |

**文件限制**：

| 类别 | 最大大小 | 允许格式 |
|------|---------|---------|
| 头像 | 2 MB | jpg, png, webp |
| 公告图片 | 5 MB | jpg, png, webp |
| 商家图片 | 5 MB | jpg, png, webp |
| 商家视频 | 50 MB | mp4, mov |
| 服务图片 | 5 MB | jpg, png, webp |
| 活动组图片 | 10 MB | jpg, png, webp, mp4 |

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

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `String` | ✅ | 文件ID |

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

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `String` | ✅ | 文件ID |

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
    "coinIssueMode": "auto",
    "coinExpiryDays": 365,
    "deliveryConfig": {
      "baseFee": 0.00,
      "courierShareRate": 1.00,
      "courierPerOrder": 0.35
    },
    "withdrawalFeeRate": 0.006,
    "pointToFeeRate": 0.01,
    "twoYearClearEnabled": true,
    "neighborDailyContactLimit": 2
  },
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

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `keyword` | `String` | ❌ | | 搜索关键词（名称/电话） |
| `status` | `String` | ❌ | | `active`/`inactive` |
| `sort` | `String` | ❌ | `-createdAt` | 排序 |

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

---

### 14.4 GET `/property-companies/{id}/communities` — 小区列表

**Auth**: Optional  
**Roles**: `any`

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `String` | ✅ | 物业公司ID |

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

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `String` | ✅ | 物业公司ID |

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
      "courierShareRate": 1.00,
      "courierPerOrder": 0.35
    },
    "withdrawalFeeRate": 0.006,
    "pointToFeeRate": 0.01,
    "twoYearClearEnabled": true,
    "neighborDailyContactLimit": 2,
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

---

### 14.6 PUT `/property-companies/{id}` — 更新物业公司

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `String` | ✅ | 物业公司ID |

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

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `name` | `String` | ❌ | `@Size(max=100)` | 公司名称 |
| `logoUrl` | `String` | ❌ | `@Size(max=500)` | Logo URL |
| `contactPhone` | `String` | ❌ | `@Size(max=20)` | 联系电话 |
| `address` | `String` | ❌ | `@Size(max=200)` | 地址 |

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

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `feeType` | `String` | ❌ | | `property`/`parking`/`water`/`electricity` |
| `paymentStatus` | `String` | ❌ | | `unpaid`/`partial`/`paid`/`overdue` |
| `period` | `String` | ❌ | | 账期 |
| `sort` | `String` | ❌ | `-dueDate` | 排序 |

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

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `paymentMethod` | `String` | ✅ | `@NotBlank` | 支付方式 |
| `amount` | `BigDecimal` | ✅ | `@NotNull @DecimalMin("0.01")` | 支付金额 |
| `pointUsed` | `Integer` | ❌ | `@Min(0)` | 使用积分 |
| `coinUsed` | `BigDecimal` | ❌ | `@DecimalMin("0.00")` | 使用物业币 |
| `wechatPayCode` | `String` | ❌ | | 微信支付code |
| `familyId` | `String` | ❌ | | 使用家庭余额 |

**成功响应**：

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

---

### 15.3 GET `/property-fees/{id}` — 物业费详情

**Auth**: Required  
**Roles**: `resident`（本人/家庭）, `property_admin`, `platform_admin`

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `String` | ✅ | 物业费账单ID |

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

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `feeType` | `String` | ❌ | | `property`/`parking`/`water`/`electricity` |
| `paymentStatus` | `String` | ❌ | | `unpaid`/`partial`/`paid`/`overdue` |
| `period` | `String` | ❌ | | 账期 |
| `communityId` | `String` | ❌ | | 小区ID |
| `sort` | `String` | ❌ | `-dueDate` | 排序 |

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
    "amount": 100.00,
    "dueDate": "2026-07-31"
  }'
```

**请求体**：

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `residentId` | `String` | ✅ | `@NotBlank` | 住户ID |
| `feeType` | `String` | ✅ | `@NotBlank` | 费用类型：`property`/`parking`/`water`/`electricity` |
| `period` | `String` | ✅ | `@NotBlank` | 账期（如 `2026-Q3`、`2026-07`） |
| `amount` | `BigDecimal` | ✅ | `@NotNull @DecimalMin("0.01")` | 应缴金额 |
| `communityId` | `String` | ❌ | | 小区ID |
| `dueDate` | `String` | ❌ | | 截止日期 `yyyy-MM-dd` |
| `autoDeduct` | `Boolean` | ❌ | | 是否自动抵扣积分/物业币 |

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

| 错误码 | HTTP | 说明 |
|--------|------|------|
| `30001` | 404 | 住户不存在 |
| `71002` | 409 | 该账期已存在物业费账单 |

---

## 16. 服务订单模块 (Service Orders)

### 16.1 GET `/service-orders` — 服务订单列表

**Auth**: Required  
**Roles**: `resident`（本人）, `property_admin`, `platform_admin`

**Query 参数**：`page`, `pageSize`, `orderStatus`, `asProvider`, `sort`

---

### 16.2 GET `/service-orders/{id}` — 服务订单详情

**Auth**: Required  
**Roles**: `resident`（本人）, `property_admin`, `platform_admin`

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `String` | ✅ | 服务订单ID |

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

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `String` | ✅ | 服务订单ID |

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

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `orderStatus` | `String` | ✅ | `@NotBlank` | 目标状态：`pending`/`in_progress`/`completed`/`cancelled` |
| `providerId` | `String` | ❌ | | 技工ID（指派时必填） |
| `remark` | `String` | ❌ | `@Size(max=200)` | 备注 |

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

### 17.1 GET `/services/my-subscriptions` — 我的订阅列表

**Auth**: Required  
**Roles**: `resident`

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/services/my-subscriptions?page=1&pageSize=20&status=active" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `status` | `String` | ❌ | | `active`/`expired`/`cancelled` |

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

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `category` | `String` | ❌ | | 分类 |
| `mine` | `Boolean` | ❌ | | 是否只看我的 |
| `sort` | `String` | ❌ | `+rankOrder` | 排序 |

---

### 17.3 POST `/services` — 创建服务

**Auth**: Required  
**Roles**: `merchant`, `individual_leader`, `resident`

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

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `name` | `String` | ✅ | `@NotBlank` | 服务名称 |
| `category` | `String` | ✅ | `@NotBlank` | 服务分类：`cleaning_repair`/`canteen`/`tutoring`/`elderly_care`/`medical`/`pet_medical`/`life_service`/`dating` |
| `description` | `String` | ❌ | | 服务描述 |
| `coverUrls` | `List<String>` | ❌ | | 封面图URL列表 |
| `price` | `BigDecimal` | ❌ | `@DecimalMin("0.00")` | 价格 |
| `memberPrice` | `BigDecimal` | ❌ | `@DecimalMin("0.00")` | 会员价 |
| `priceUnit` | `String` | ❌ | | 价格单位（如 `次`、`月`、`年`） |
| `isSubscription` | `Boolean` | ❌ | | 是否为订阅制服务 |

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

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/services/svc_demo001/subscribe \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "subType": "monthly",
    "paymentMethod": "wechat",
    "autoRenew": true
  }'
```

**错误响应**：

| 错误码 | HTTP | 说明 |
|--------|------|------|
| `96003` | 409 | 已订阅该服务 |

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

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `address` | `String` | ✅ | `@NotBlank` | 服务地址 |
| `contactPhone` | `String` | ✅ | `@NotBlank` | 联系电话 |
| `paymentMethod` | `String` | ✅ | `@NotBlank` | 支付方式：`wechat`/`point`/`property_coin`/`mixed` |
| `serviceTime` | `String` | ❌ | | 期望服务时间 `yyyy-MM-dd HH:mm:ss` |
| `remark` | `String` | ❌ | | 备注 |

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
    "createdAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**错误响应**：

| 错误码 | HTTP | 说明 |
|--------|------|------|
| `30001` | 404 | 服务不存在 |
| `17001` | 400 | 服务已下架 |

---

### 17.6 PUT `/services/{id}` — 更新服务

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `String` | ✅ | 服务ID |

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

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `name` | `String` | ❌ | `@Size(max=100)` | 服务名称 |
| `description` | `String` | ❌ | | 描述 |
| `coverUrl` | `String` | ❌ | `@Size(max=500)` | 封面图 |
| `price` | `BigDecimal` | ❌ | `@DecimalMin("0.01")` | 价格 |
| `memberPrice` | `BigDecimal` | ❌ | `@DecimalMin("0.01")` | 业主折扣价 |
| `priceUnit` | `String` | ❌ | `@Size(max=20)` | 价格单位 |
| `category` | `String` | ❌ | | 分类 |
| `status` | `String` | ❌ | | `active`/`inactive` |

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

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `String` | ✅ | 服务ID |

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

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `String` | ✅ | 服务ID |

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

### 18.1 GET `/activity-groups/my-subscriptions` — 我的订阅列表

**Auth**: Required  
**Roles**: `resident`

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/activity-groups/my-subscriptions?page=1&pageSize=20&status=active" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `status` | `String` | ❌ | | `active`/`expired`/`cancelled` |

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "ag_sub001",
        "activityGroupId": "ag_demo001",
        "activityGroupName": "广场舞队",
        "subscriptionType": "free",
        "status": "active",
        "isMember": true,
        "subscribedAt": "2026-06-15 10:00:00",
        "expiresAt": null
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

### 18.2 GET `/activity-groups` — 活动组列表

**Auth**: Optional  
**Roles**: `any`

**Query 参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `communityId` | `String` | ❌ | | 小区ID |
| `mine` | `Boolean` | ❌ | | 是否只看我的 |
| `sort` | `String` | ❌ | `-createdAt` | 排序 |

---

### 18.3 POST `/activity-groups` — 创建活动组

**Auth**: Required  
**Roles**: `resident`

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

### 18.4 GET `/activity-groups/{id}/members` — 成员列表

**Auth**: Required  
**Roles**: `resident`（活动组成员）, `property_admin`, `platform_admin`

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `String` | ✅ | 活动组ID |

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

### 18.5 POST `/activity-groups/{id}/subscribe` — 订阅活动组

**Auth**: Required  
**Roles**: `resident`

**错误响应**：

| 错误码 | HTTP | 说明 |
|--------|------|------|
| `90008` | 409 | 已订阅该活动组 |

---

### 18.6 POST `/activity-groups/{id}/join` — 加入活动组

**Auth**: Required  
**Roles**: `resident`

**错误响应**：

| 错误码 | HTTP | 说明 |
|--------|------|------|
| `90009` | 409 | 已是活动组成员 |

---

### 18.7 POST `/activity-groups/{id}/leave` — 离开活动组

**Auth**: Required  
**Roles**: `resident`

**错误响应**：

| 错误码 | HTTP | 说明 |
|--------|------|------|
| `90010` | 409 | 不是活动组成员 |
| `90011` | 409 | 组长不能退出活动组 |

---

### 18.8 DELETE `/activity-groups/{id}/members/{memberId}` — 移除成员

**Auth**: Required  
**Roles**: `activity_leader`

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `String` | ✅ | 活动组ID |
| `memberId` | `String` | ✅ | 成员ID |

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

### 18.9 PUT `/activity-groups/{id}` — 更新活动组

**Auth**: Required  
**Roles**: `activity_leader`

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `String` | ✅ | 活动组ID |

**请求体**：

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `name` | `String` | ❌ | `@Size(max=100)` | 活动组名称 |
| `description` | `String` | ❌ | | 描述 |
| `coverUrl` | `String` | ❌ | `@Size(max=500)` | 封面图URL |
| `monthlyFee` | `BigDecimal` | ❌ | `@DecimalMin("0")` | 月费 |
| `yearlyFee` | `BigDecimal` | ❌ | `@DecimalMin("0")` | 年费 |

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

### 18.10 GET `/activity-groups/{id}` — 活动组详情

**Auth**: Optional  
**Roles**: `any`

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `String` | ✅ | 活动组ID |

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

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `status` | `String` | ❌ | | `active`/`ended`/`closed` |
| `mine` | `Boolean` | ❌ | | 是否只看我创建的 |
| `voted` | `Boolean` | ❌ | | 是否已投票 |
| `sort` | `String` | ❌ | `-createdAt` | 排序 |

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

**错误响应**：

| 错误码 | HTTP | 说明 |
|--------|------|------|
| `90006` | 409 | 每家庭每月仅可发表1条意见 |

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

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `voteOption` | `String` | ✅ | `@NotBlank` | `support`/`oppose`/`abstain` |

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

| 错误码 | HTTP | 说明 |
|--------|------|------|
| `90002` | 409 | 投票已结束 |
| `90005` | 409 | 该家庭已投过票 |

---

### 19.5 GET `/opinion-boxes/{id}` — 意见箱详情

**Auth**: Required  
**Roles**: `resident`

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `String` | ✅ | 意见箱ID |

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

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `startDate` | `String` | ❌ | | 开始日期 `yyyy-MM-dd` |
| `endDate` | `String` | ❌ | | 结束日期 `yyyy-MM-dd` |
| `propertyCompanyId` | `String` | ❌ | | 物业公司ID（超管筛选） |
| `groupBy` | `String` | ❌ | | 分组维度 |

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

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `startDate` | `String` | ❌ | | 开始日期 `yyyy-MM-dd` |
| `endDate` | `String` | ❌ | | 结束日期 `yyyy-MM-dd` |
| `propertyCompanyId` | `String` | ❌ | | 物业公司ID（超管筛选） |
| `groupBy` | `String` | ❌ | | 分组维度 |

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

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `startDate` | `String` | ❌ | | 开始日期 `yyyy-MM-dd` |
| `endDate` | `String` | ❌ | | 结束日期 `yyyy-MM-dd` |
| `propertyCompanyId` | `String` | ❌ | | 物业公司ID（超管筛选） |
| `dimension` | `String` | ❌ | `summary` | 统计维度 |

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
**Roles**: `coordinator`, `property_admin`, `platform_admin`

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/distribution/records?page=1&pageSize=20&startDate=2026-06-01&endDate=2026-06-30&sort=-createdAt" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `orderId` | `String` | ❌ | | 订单ID筛选 |
| `merchantId` | `String` | ❌ | | 商家ID筛选 |
| `coordinatorId` | `String` | ❌ | | 统筹负责人ID筛选 |
| `sectorLeaderId` | `String` | ❌ | | 板块负责人ID筛选 |
| `individualLeaderId` | `String` | ❌ | | 个体负责人ID筛选 |
| `propertyCompanyId` | `String` | ❌ | | 物业公司ID筛选 |
| `startDate` | `String` | ❌ | | 开始日期 |
| `endDate` | `String` | ❌ | | 结束日期 |
| `sort` | `String` | ❌ | `-createdAt` | 排序 |

**成功响应**：

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
        "totalAmount": 100.00,
        "distributableAmount": 15.00,
        "propertyAmount": 4.50,
        "coordinatorAmount": 10.50,
        "sectorLeaderAmount": 1.05,
        "individualLeaderAmount": 0.53,
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

### 21.2 GET `/distribution/stats` — 分成统计

**Auth**: Required  
**Roles**: `coordinator`, `property_admin`, `platform_admin`

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/distribution/stats?startDate=2026-06-01&endDate=2026-06-30&propertyCompanyId=pc_demo001" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `startDate` | `String` | ❌ | | 开始日期 |
| `endDate` | `String` | ❌ | | 结束日期 |
| `dimension` | `String` | ❌ | `summary` | 统计维度 |
| `propertyCompanyId` | `String` | ❌ | | 物业公司ID筛选 |

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "summary": {
      "totalDistributableAmount": 15000.00,
      "propertyAmount": 4500.00,
      "coordinatorAmount": 10500.00,
      "sectorLeaderAmount": 1050.00,
      "individualLeaderAmount": 525.00
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

---

### 21.3 GET `/distribution/calculate` — 试算分成（预览，不落库）

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/distribution/calculate?merchantId=mch_demo001&totalAmount=1000.00&pointUsed=500&coinUsed=50.00" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `merchantId` | `String` | ✅ | | 商家ID |
| `totalAmount` | `BigDecimal` | ✅ | | 订单总金额 |
| `pointUsed` | `Integer` | ❌ | `0` | 使用积分数量 |
| `coinUsed` | `BigDecimal` | ❌ | `0` | 使用物业币数量 |
| `propertyCompanyId` | `String` | ❌ | | 物业公司ID |

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "merchantId": "mch_demo001",
    "totalAmount": 1000.00,
    "commissionRate": 0.15,
    "distributableAmount": 150.00,
    "pointCost": 5.00,
    "coinCost": 50.00,
    "netDistributable": 95.00,
    "breakdown": {
      "propertyAmount": 28.50,
      "coordinatorAmount": 66.50,
      "sectorLeaderAmount": 6.65,
      "individualLeaderAmount": 3.33
    }
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

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

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `orderId` | `String` | ❌ | | 订单ID筛选 |
| `merchantId` | `String` | ❌ | | 商家ID筛选 |
| `coordinatorId` | `String` | ❌ | | 统筹负责人ID筛选 |
| `sectorLeaderId` | `String` | ❌ | | 板块负责人ID筛选 |
| `individualLeaderId` | `String` | ❌ | | 个体负责人ID筛选 |
| `propertyCompanyId` | `String` | ❌ | | 物业公司ID（超管筛选） |
| `startDate` | `String` | ❌ | | 开始日期 |
| `endDate` | `String` | ❌ | | 结束日期 |
| `sort` | `String` | ❌ | `-createdAt` | 排序 |

**成功响应**：

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
        "distributableAmount": 15.00,
        "propertyAmount": 4.50,
        "coordinatorAmount": 10.50,
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

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `startDate` | `String` | ✅ | | 开始日期 `yyyy-MM-dd` |
| `endDate` | `String` | ✅ | | 结束日期 `yyyy-MM-dd` |
| `dimension` | `String` | ❌ | `summary` | 统计维度 |
| `propertyCompanyId` | `String` | 超管必填 | | 物业公司ID |

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
      "pendingAmount": 3000.00,
      "propertyAmount": 4500.00,
      "coordinatorAmount": 10500.00,
      "sectorLeaderAmount": 1050.00,
      "individualLeaderAmount": 525.00
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
    "pointAmount": 1000,
    "payAmount": 100.00
  }'
```

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

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `auditStatus` | `String` | ❌ | | `pending_audit`/`approved`/`rejected` |
| `startDate` | `String` | ❌ | | 开始日期 `yyyy-MM-dd` |
| `endDate` | `String` | ❌ | | 结束日期 `yyyy-MM-dd` |
| `sort` | `String` | ❌ | `-createdAt` | 排序 |

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
        "payAmount": 100.00,
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
    "purchaseId": "mpp_demo001",
    "residentId": "res_b7m9x1",
    "pointAmount": 50,
    "description": "消费赠送"
  }'
```

**错误响应**：

| 错误码 | HTTP | 说明 |
|--------|------|------|
| `94001` | 409 | 积分购买审核中 |
| `94002` | 400 | 商家积分余额不足 |

---

## 23. 商家提现模块 (Merchant Withdrawals)

### 23.1 POST `/merchant/withdrawals` — 商家提现申请

**Auth**: Required  
**Roles**: `merchant`

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

| 错误码 | HTTP | 说明 |
|--------|------|------|
| `94003` | 409 | 提现申请不允许操作 |

---

### 23.2 GET `/merchant/withdrawals` — 提现记录

**Auth**: Required  
**Roles**: `merchant`

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/merchant/withdrawals?page=1&pageSize=20&auditStatus=completed&sort=-createdAt" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `auditStatus` | `String` | ❌ | | `pending_audit`/`approved`/`rejected`/`completed` |
| `startDate` | `String` | ❌ | | 开始日期 `yyyy-MM-dd` |
| `endDate` | `String` | ❌ | | 结束日期 `yyyy-MM-dd` |
| `sort` | `String` | ❌ | `-createdAt` | 排序 |

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

| 错误码 | HTTP | 说明 |
|--------|------|------|
| `50001` | 400 | 物业币余额不足 |
| `93003` | 403 | 物业币已冻结 |
| `94004` | 400 | 兑换金额不合法 |

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

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `merchantId` | `String` | ❌ | | 商家ID筛选 |
| `auditStatus` | `String` | ❌ | | `pending_audit`/`approved`/`rejected`/`completed` |
| `startDate` | `String` | ❌ | | 开始日期 `yyyy-MM-dd` |
| `endDate` | `String` | ❌ | | 结束日期 `yyyy-MM-dd` |
| `sort` | `String` | ❌ | `-createdAt` | 排序 |

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
**Roles**: `property_admin`, `platform_admin`, `merchant`

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/special-offers \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "title": "周末特惠：超市满50减5",
    "description": "周末购物满50元立减5元",
    "merchantId": "mch_demo001",
    "coverUrl": "https://cdn.example.com/special-offers/so_demo001.jpg",
    "discountType": "fixed",
    "discountValue": 5.00,
    "minConsumption": 50.00,
    "startTime": "2026-07-04 00:00:00",
    "endTime": "2026-07-05 23:59:59",
    "totalQuota": 100,
    "perUserQuota": 1,
    "targetBuildings": ["1栋", "2栋", "3栋"]
  }'
```

**请求体**：

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `title` | `String` | ✅ | `@NotBlank @Size(max=100)` | 推送标题 |
| `description` | `String` | ❌ | | 推送描述 |
| `merchantId` | `String` | ❌ | | 商家ID（不传则为物业推送） |
| `coverUrl` | `String` | ❌ | `@Size(max=500)` | 封面图URL |
| `discountType` | `String` | ✅ | `@NotBlank` | `fixed`（固定金额）/ `percent`（百分比） |
| `discountValue` | `BigDecimal` | ✅ | `@NotNull @DecimalMin("0.01")` | 优惠值 |
| `minConsumption` | `BigDecimal` | ❌ | `@DecimalMin("0.00")` | 最低消费门槛 |
| `startTime` | `String` | ✅ | `@NotBlank` | 开始时间 `yyyy-MM-dd HH:mm:ss` |
| `endTime` | `String` | ✅ | `@NotBlank` | 结束时间 `yyyy-MM-dd HH:mm:ss` |
| `totalQuota` | `Integer` | ❌ | `@Min(1)` | 总数量（不限则不传） |
| `perUserQuota` | `Integer` | ❌ | `@Min(1)` | 每人限领数量 |
| `targetBuildings` | `List<String>` | ❌ | | 目标楼栋（不传则全部） |
| `propertyCompanyId` | `String` | ❌ | | 物业公司ID |

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "so_demo001",
    "title": "周末特惠：超市满50减5",
    "merchantId": "mch_demo001",
    "merchantName": "万达超市",
    "discountType": "fixed",
    "discountValue": 5.00,
    "minConsumption": 50.00,
    "startTime": "2026-07-04 00:00:00",
    "endTime": "2026-07-05 23:59:59",
    "totalQuota": 100,
    "remainingQuota": 100,
    "perUserQuota": 1,
    "status": "active",
    "createdAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---

### 25.2 GET `/special-offers` — 特惠推送列表

**Auth**: Optional  
**Roles**: `any`

**Query 参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `merchantId` | `String` | ❌ | | 商家ID |
| `status` | `String` | ❌ | | `active`/`ended`/`archived` |
| `keyword` | `String` | ❌ | | 搜索关键词 |
| `sort` | `String` | ❌ | `-createdAt` | 排序 |

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
        "merchantId": "mch_demo001",
        "merchantName": "万达超市",
        "coverUrl": "https://cdn.example.com/special-offers/so_demo001.jpg",
        "discountType": "fixed",
        "discountValue": 5.00,
        "minConsumption": 50.00,
        "startTime": "2026-07-04 00:00:00",
        "endTime": "2026-07-05 23:59:59",
        "totalQuota": 100,
        "remainingQuota": 85,
        "perUserQuota": 1,
        "status": "active",
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

### 25.3 GET `/special-offers/{id}` — 特惠推送详情

**Auth**: Optional  
**Roles**: `any`

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "so_demo001",
    "title": "周末特惠：超市满50减5",
    "description": "周末购物满50元立减5元",
    "merchantId": "mch_demo001",
    "merchantName": "万达超市",
    "coverUrl": "https://cdn.example.com/special-offers/so_demo001.jpg",
    "discountType": "fixed",
    "discountValue": 5.00,
    "minConsumption": 50.00,
    "startTime": "2026-07-04 00:00:00",
    "endTime": "2026-07-05 23:59:59",
    "totalQuota": 100,
    "remainingQuota": 85,
    "perUserQuota": 1,
    "targetBuildings": ["1栋", "2栋", "3栋"],
    "status": "active",
    "createdAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---

### 25.4 PUT `/special-offers/{id}` — 更新特惠推送

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`, `merchant`

**请求示例**：

```bash
curl -X PUT https://test.yuanxusoftware.top/api/v1/special-offers/so_demo001 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "title": "周末特惠：超市满100减10",
    "discountValue": 10.00,
    "minConsumption": 100.00
  }'
```

**成功响应**：更新后的特惠推送详情

---

### 25.5 DELETE `/special-offers/{id}` — 删除特惠推送

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`, `merchant`

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "so_demo001",
    "status": "archived",
    "deletedAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

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

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `keyword` | `String` | ❌ | | 商家名称搜索 |
| `sort` | `String` | ❌ | `-lastConsumedAt` | 排序 |

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

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `sort` | `String` | ❌ | `-votedAt` | 排序 |

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
**Roles**: `resident`

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
    "activityGroupId": "ag_demo001",
    "residentId": "res_b7m9x1",
    "familyId": "fam_demo001",
    "isSubscribed": true,
    "isMember": false,
    "subscribedAt": "2026-06-15 10:00:00",
    "subscriptionType": "free",
    "subscriptionExpiry": null,
    "autoRenew": false
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

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

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `keyword` | `String` | ❌ | | 搜索关键词（姓名/房号） |
| `communityId` | `String` | ❌ | | 小区ID（默认当前小区） |
| `building` | `String` | ❌ | | 楼栋筛选 |
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |

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
- 姓名中间字用 `*` 隐藏，保护隐私
- `chatStatus`: `available`（可聊天）/ `limited`（今日次数已用完）/ `blocked`（被屏蔽）

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

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `reason` | `String` | ❌ | `@Size(max=200)` | 注销原因 |
| `confirmText` | `String` | ✅ | 必须为 `"确认注销"` | 确认文本 |

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "requestId": "del_req_demo001",
    "residentId": "res_b7m9x1",
    "status": "pending",
    "reason": "不再居住于此小区",
    "processedAt": null,
    "expiresAt": "2026-07-15 15:23:33",
    "message": "注销申请已提交，将在15天后生效。期间可随时撤销。"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**业务规则**：

- 注销申请提交后有 **15天冷静期**，期间可撤销
- 冷静期结束后，账号状态变为 `pending_deletion`
- 系统在7天内完成数据清理
- 以下情况不允许注销：
  - 存在未完成的订单
  - 存在未支付的物业费
  - 物业币余额不为0（需先提现或转让）
  - 是家庭户主且家庭有其他成员

**错误响应**：

| 错误码 | HTTP | 说明 |
|--------|------|------|
| `30005` | 409 | 存在未完成的订单 |
| `30006` | 409 | 存在未支付的物业费 |
| `30007` | 409 | 物业币余额不为0 |
| `30008` | 409 | 户主家庭有其他成员 |

---

## 32. 管理端 — 公告管理

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

### 32.1 GET `/admin/announcements/{id}/collects` — 公告收集列表

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `String` | ✅ | 公告ID |

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/admin/announcements/ann_demo001/collects?page=1&pageSize=20" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `sort` | `String` | ❌ | `-createdAt` | 排序 |

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

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `merchantId` | `String` | ❌ | | 商家ID筛选 |
| `auditStatus` | `String` | ❌ | | `pending_audit`/`approved`/`rejected`/`completed` |
| `status` | `String` | ❌ | | 同 `auditStatus`，兼容字段 |
| `startDate` | `String` | ❌ | | 开始日期 `yyyy-MM-dd` |
| `endDate` | `String` | ❌ | | 结束日期 `yyyy-MM-dd` |
| `propertyCompanyId` | `String` | ❌ | | 物业公司ID（超管筛选） |
| `sort` | `String` | ❌ | `-createdAt` | 排序 |

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

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `merchantId` | `String` | ❌ | | 商家ID |
| `auditStatus` | `String` | ❌ | | `pending_audit`/`approved`/`rejected` |
| `status` | `String` | ❌ | | 同 `auditStatus`，兼容字段 |
| `startDate` | `String` | ❌ | | 开始日期 `yyyy-MM-dd` |
| `endDate` | `String` | ❌ | | 结束日期 `yyyy-MM-dd` |
| `propertyCompanyId` | `String` | ❌ | | 物业公司ID（超管筛选） |
| `sort` | `String` | ❌ | `-createdAt` | 排序 |

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
        "payAmount": 100.00,
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

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `String` | ✅ | 积分购买记录ID |

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

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `auditResult` | `String` | ✅ | `@NotBlank` | `approved`（通过）/ `rejected`（拒绝） |
| `rejectReason` | `String` | ❌ | `@Size(max=200)` | 拒绝原因（拒绝时必填） |
| `remark` | `String` | ❌ | `@Size(max=200)` | 备注 |

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
    "payAmount": 100.00,
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

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `merchantId` | `String` | ❌ | | 商家ID |
| `auditStatus` | `String` | ❌ | | `pending_audit`/`approved`/`rejected`/`completed` |
| `status` | `String` | ❌ | | 同 `auditStatus`，兼容字段 |
| `startDate` | `String` | ❌ | | 开始日期 `yyyy-MM-dd` |
| `endDate` | `String` | ❌ | | 结束日期 `yyyy-MM-dd` |
| `propertyCompanyId` | `String` | ❌ | | 物业公司ID（超管筛选） |
| `sort` | `String` | ❌ | `-createdAt` | 排序 |

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

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `String` | ✅ | 提现记录ID |

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

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `auditResult` | `String` | ✅ | `@NotBlank` | `approved`（通过）/ `rejected`（拒绝） |
| `rejectReason` | `String` | ❌ | `@Size(max=200)` | 拒绝原因（拒绝时必填） |
| `remark` | `String` | ❌ | `@Size(max=200)` | 备注 |

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

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `auditResult` | `String` | ✅ | `@NotBlank` | `approved`（通过）/ `rejected`（拒绝） |
| `rejectReason` | `String` | ❌ | `@Size(max=200)` | 拒绝原因（拒绝时必填） |
| `remark` | `String` | ❌ | `@Size(max=200)` | 备注 |
| `merchantLevel` | `String` | ❌ | | 审核时调整商家等级：`official_certified`/`property_certified`/`normal` |
| `category` | `String` | ❌ | `@Size(max=50)` | 审核时调整分类 |
| `businessHours` | `String` | ❌ | `@Size(max=50)` | 审核时调整营业时间 |
| `deliveryFee` | `String` | ❌ | | 审核时调整配送费 |
| `freeDeliveryThreshold` | `String` | ❌ | | 审核时调整满额免配送费 |

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
- 审核通过：`auditStatus` → `approved`，`status` → `active`
- 审核拒绝：`auditStatus` → `rejected`，`status` → `rejected`
- 审核时可顺带调整商家参数（等级、分类、配送费等）
- 已审核的商家不可重复审核

**错误响应**：

| 错误码 | HTTP | 说明 |
|--------|------|------|
| `60001` | 404 | 商家不存在 |
| `60003` | 409 | 审核已处理（非pending_audit状态） |
| `20004` | 403 | 权限不足 |

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

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `operatorId` | `String` | ❌ | | 操作人ID |
| `action` | `String` | ❌ | | 操作类型 |
| `startDate` | `String` | ❌ | | 开始日期 |
| `endDate` | `String` | ❌ | | 结束日期 |
| `sort` | `String` | ❌ | `-createdAt` | 排序 |

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

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `startDate` | `String` | ❌ | | 开始日期 |
| `endDate` | `String` | ❌ | | 结束日期 |
| `sort` | `String` | ❌ | `-createdAt` | 排序 |

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

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `keyword` | `String` | ❌ | | 搜索关键词（姓名/手机号） |
| `status` | `String` | ❌ | | `active`/`inactive` |
| `sort` | `String` | ❌ | `-createdAt` | 排序 |

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
**Roles**: `property_admin`, `platform_admin`

**请求示例**：

```bash
curl -X POST https://test.yuanxusoftware.top/api/v1/admin/individual-leaders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "residentId": "res_b7m9x1",
    "sectorId": "sl_demo001",
    "description": "负责3栋保洁工作"
  }'
```

**请求体**：

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `residentId` | `String` | ✅ | `@NotBlank` | 业主ID |
| `sectorId` | `String` | ✅ | `@NotBlank` | 板块ID |
| `description` | `String` | ❌ | `@Size(max=200)` | 描述 |

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "il_demo002",
    "residentId": "res_b7m9x1",
    "residentName": "张先生",
    "sectorId": "sl_demo001",
    "sectorName": "保洁板块",
    "description": "负责3栋保洁工作",
    "status": "active",
    "createdAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---

### 37.3 GET `/admin/individual-leaders/{id}` — 详情

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `String` | ✅ | 个体负责人记录ID |

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

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `String` | ✅ | 个体负责人记录ID |

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

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `sectorId` | `String` | ❌ | `@NotBlank` | 板块ID |
| `description` | `String` | ❌ | `@Size(max=200)` | 描述 |
| `status` | `String` | ❌ | | `active`/`inactive` |

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

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `String` | ✅ | 个体负责人记录ID |

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "il_demo001",
    "status": "inactive",
    "deletedAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

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

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `residentId` | `String` | ❌ | | 业主ID |
| `startDate` | `String` | ❌ | | 开始日期 |
| `endDate` | `String` | ❌ | | 结束日期 |
| `sort` | `String` | ❌ | `-createdAt` | 排序 |

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
**Roles**: `property_admin`, `platform_admin`

### 39.1 GET `/admin/sector-leaders` — 板块负责人列表

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/admin/sector-leaders?page=1&pageSize=20&keyword=张&status=active" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `keyword` | `String` | ❌ | | 搜索关键词（姓名/手机号） |
| `status` | `String` | ❌ | | `active`/`inactive` |
| `sort` | `String` | ❌ | `-createdAt` | 排序 |

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
        "coordinatorId": "coo_demo001",
        "coordinatorName": "统筹负责人A",
        "sector": "cleaning",
        "sectorName": "保洁",
        "propertyCompanyId": "pc_demo001",
        "description": "负责保洁板块管理",
        "status": "active",
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

---

### 39.2 POST `/admin/sector-leaders` — 指派板块负责人

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

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

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `residentId` | `String` | ✅ | `@NotBlank` | 业主ID |
| `coordinatorId` | `String` | ✅ | `@NotBlank` | 统筹负责人ID |
| `sector` | `String` | ✅ | `@NotBlank` | 板块类型：`cleaning`/`repair`/`security`/`greening`/`other` |
| `description` | `String` | ❌ | `@Size(max=200)` | 描述 |

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
**Roles**: `property_admin`, `platform_admin`

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `String` | ✅ | 板块负责人记录ID |

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
**Roles**: `property_admin`, `platform_admin`

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `String` | ✅ | 板块负责人记录ID |

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

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `coordinatorId` | `String` | ❌ | `@NotBlank` | 统筹负责人ID |
| `sector` | `String` | ❌ | `@NotBlank` | 板块类型 |
| `description` | `String` | ❌ | `@Size(max=200)` | 描述 |
| `status` | `String` | ❌ | | `active`/`inactive` |

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
    "updatedAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---

### 39.5 DELETE `/admin/sector-leaders/{id}` — 撤销

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `String` | ✅ | 板块负责人记录ID |

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

## 40. 管理端 — 积分池管理

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

### 40.1 GET `/admin/point-pools` — 获取积分池余额

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

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

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `poolType` | `String` | ❌ | | `diff`/`expiry_clear`/`fee_clear`/`manual_adjust`/`manual_out` |
| `source` | `String` | ❌ | | 来源筛选 |
| `startDate` | `String` | ❌ | | 开始日期 |
| `endDate` | `String` | ❌ | | 结束日期 |
| `propertyCompanyId` | `String` | ❌ | | 物业公司ID（超管筛选） |
| `sort` | `String` | ❌ | `-createdAt` | 排序 |

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

| 错误码 | HTTP | 说明 |
|--------|------|------|
| `93001` | 404 | 积分池不存在 |
| `93002` | 400 | 积分池余额不足 |

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

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `keyword` | `String` | ❌ | | 搜索关键词（姓名/手机号） |
| `status` | `String` | ❌ | | `active`/`inactive` |
| `sort` | `String` | ❌ | `-createdAt` | 排序 |

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "coo_demo001",
        "residentId": "res_b7m9x1",
        "residentName": "张先生",
        "residentPhone": "138****1234",
        "propertyCompanyId": "pc_demo001",
        "propertyCompanyName": "演示物业公司",
        "description": "负责统筹管理",
        "sectorCount": 3,
        "individualLeaderCount": 10,
        "status": "active",
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

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `residentId` | `String` | ✅ | `@NotBlank` | 业主ID |
| `propertyCompanyId` | `String` | ✅ | `@NotBlank` | 物业公司ID |
| `description` | `String` | ❌ | `@Size(max=200)` | 描述 |

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "coo_demo002",
    "residentId": "res_b7m9x1",
    "residentName": "张先生",
    "propertyCompanyId": "pc_demo001",
    "description": "负责统筹管理",
    "status": "active",
    "createdAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---

### 41.3 GET `/admin/coordinators/{id}` — 详情

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `String` | ✅ | 统筹负责人记录ID |

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "coo_demo001",
    "residentId": "res_b7m9x1",
    "residentName": "张先生",
    "residentPhone": "138****1234",
    "propertyCompanyId": "pc_demo001",
    "propertyCompanyName": "演示物业公司",
    "description": "负责统筹管理",
    "sectorCount": 3,
    "individualLeaderCount": 10,
    "status": "active",
    "createdAt": "2026-06-01 10:00:00",
    "updatedAt": "2026-06-28 14:30:00"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---

### 41.4 PUT `/admin/coordinators/{id}` — 更新

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `String` | ✅ | 统筹负责人记录ID |

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

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `description` | `String` | ❌ | `@Size(max=200)` | 描述 |
| `status` | `String` | ❌ | | `active`/`inactive` |

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

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `String` | ✅ | 统筹负责人记录ID |

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

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `keyword` | `String` | ❌ | | 搜索关键词（名称/电话） |
| `category` | `String` | ❌ | | 分类 |
| `status` | `String` | ❌ | | `active`/`inactive` |
| `sort` | `String` | ❌ | `-createdAt` | 排序 |

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

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `name` | `String` | ✅ | `@NotBlank @Size(max=100)` | 商家名称 |
| `category` | `String` | ✅ | `@NotBlank` | 分类 |
| `contactPhone` | `String` | ✅ | `@NotBlank @Size(max=20)` | 联系电话 |
| `description` | `String` | ❌ | | 描述 |
| `coverUrl` | `String` | ❌ | `@Size(max=500)` | 封面图URL |
| `businessHours` | `String` | ❌ | `@Size(max=50)` | 营业时间 |
| `address` | `String` | ❌ | `@Size(max=200)` | 地址 |

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

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `String` | ✅ | 平台商家ID |

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

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `String` | ✅ | 平台商家ID |

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

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `name` | `String` | ❌ | `@Size(max=100)` | 商家名称 |
| `category` | `String` | ❌ | | 分类 |
| `contactPhone` | `String` | ❌ | `@Size(max=20)` | 联系电话 |
| `description` | `String` | ❌ | | 描述 |
| `coverUrl` | `String` | ❌ | `@Size(max=500)` | 封面图URL |
| `businessHours` | `String` | ❌ | `@Size(max=50)` | 营业时间 |
| `address` | `String` | ❌ | `@Size(max=200)` | 地址 |
| `status` | `String` | ❌ | | `active`/`inactive` |

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

## 43. 管理端 — 商家踢出

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

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

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `reason` | `String` | ✅ | `@NotBlank @Size(min=1, max=200)` | 踢出原因 |
| `notifyMerchant` | `Boolean` | ❌ | | 是否通知商家（默认true） |

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

| 错误码 | HTTP | 说明 |
|--------|------|------|
| `60001` | 404 | 商家不存在 |
| `60005` | 409 | 商家已被踢出 |

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

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `propertyCompanyId` | `String` | 超管必填 | 从token获取 | 物业公司ID（超管必传，物业管理员可不传） |

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "period": "month",
    "startDate": "2026-06-01",
    "endDate": "2026-06-30",
    "summary": {
      "totalOrders": 1250,
      "totalRevenue": 62500.00,
      "totalResidents": 520,
      "activeResidents": 380,
      "newResidents": 15,
      "totalMerchants": 18,
      "activeMerchants": 15,
      "totalPointsIssued": 25000,
      "totalPointsRedeemed": 8000,
      "totalCoinIssued": 3125.00,
      "totalCoinRedeemed": 1250.00,
      "propertyFeeCollectionRate": 0.88,
      "propertyFeeAmount": 88000.00,
      "propertyFeeCollected": 77440.00
    },
    "trends": {
      "orderGrowthRate": 0.12,
      "revenueGrowthRate": 0.15,
      "residentGrowthRate": 0.03,
      "merchantGrowthRate": 0.05
    },
    "topMerchants": [
      {
        "merchantId": "mch_demo001",
        "merchantName": "万达超市",
        "orderCount": 320,
        "revenue": 16000.00
      }
    ],
    "recentActivity": [
      {
        "type": "new_resident",
        "description": "新业主入驻：王五",
        "timestamp": "2026-06-30 14:30:00"
      }
    ]
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

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

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `merchantIds` | `List<String>` | ✅ | `@NotEmpty @Size(max=50)` | 商家ID列表（最多50个） |
| `config` | `Object` | ✅ | `@Valid` | 配置参数 |
| `config.commissionRate` | `BigDecimal` | ❌ | `@DecimalMin("0") @DecimalMax("1")` | 抽佣比例 |
| `config.pointExchangeRate` | `Integer` | ❌ | `@Min(1)` | 积分兑换比（1元=X积分） |
| `config.coinRebateRate` | `BigDecimal` | ❌ | `@DecimalMin("0") @DecimalMax("1")` | 物业币返现比例 |
| `config.coinRebateEnabled` | `Boolean` | ❌ | | 是否启用物业币返现 |

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

| 错误码 | HTTP | 说明 |
|--------|------|------|
| `60001` | 404 | 部分商家不存在 |

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

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `propertyCompanyId` | `String` | ✅ | `@NotBlank` | 物业公司ID |
| `customConfig` | `Object` | ❌ | | 自定义配置（覆盖平台默认） |
| `customConfig.commissionRate` | `BigDecimal` | ❌ | | 抽佣比例 |
| `customConfig.deliveryFee` | `BigDecimal` | ❌ | | 配送费 |
| `customConfig.freeDeliveryThreshold` | `BigDecimal` | ❌ | | 满额免配送费 |

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

| 错误码 | HTTP | 说明 |
|--------|------|------|
| `30004` | 409 | 该商家已在当前物业下挂接 |
| `60001` | 404 | 平台商家不存在 |

---

## 47. 管理端 — 盈利空间

**Auth**: Required  
**Roles**: `property_admin`, `platform_admin`

### 47.1 GET `/admin/platform-merchants/{id}/profit-space` — 盈利空间计算

**请求示例**：

```bash
curl -X GET "https://test.yuanxusoftware.top/api/v1/admin/platform-merchants/pm_demo001/profit-space?propertyCompanyId=pc_demo001" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Query 参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `propertyCompanyId` | `String` | ✅ | | 物业公司ID |
| `period` | `String` | ❌ | `month` | `week`/`month`/`quarter`/`year` |

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
    "startDate": "2026-06-01",
    "endDate": "2026-06-30",
    "metrics": {
      "totalOrders": 320,
      "totalRevenue": 16000.00,
      "totalCommission": 2400.00,
      "totalDeliveryFee": 96.00,
      "totalPointCost": 800.00,
      "totalCoinCost": 160.00,
      "netProfit": 1536.00,
      "profitMargin": 0.096
    },
    "breakdown": {
      "propertyShare": 720.00,
      "coordinatorShare": 1680.00,
      "sectorLeaderShare": 168.00,
      "individualLeaderShare": 84.00,
      "platformShare": 0.00
    },
    "comparison": {
      "lastPeriodRevenue": 14000.00,
      "revenueGrowthRate": 0.14,
      "lastPeriodProfit": 1200.00,
      "profitGrowthRate": 0.28
    }
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

**业务规则**：

- 盈利空间 = 总收入 - 配送费 - 积分成本 - 物业币成本
- 利润分配按照物业公司配置的分成比例计算
- 支持按周/月/季度/年查看历史数据

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

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `keyword` | `String` | ❌ | | 搜索关键词（姓名/手机号） |

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

| 字段 | 类型 | 说明 |
|------|------|------|
| `accounts[].id` | `String` | 用户ID |
| `accounts[].name` | `String` | 姓名 |
| `accounts[].phone` | `String` | 手机号（脱敏） |
| `accounts[].role` | `String` | 角色代码 |
| `accounts[].propertyCompanyId` | `String` | 所属物业公司ID |
| `accounts[].status` | `String` | 状态：`active`/`frozen`/`disabled` |
| `accounts[].effectivePermissionCount` | `int` | 最终权限数量 |

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

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `name` | `String` | ✅ | `@NotBlank @Size(max=50)` | 预设名称 |
| `role` | `String` | ✅ | `@NotBlank` | 角色代码 |
| `permissionCodes` | `List<String>` | ✅ | `@NotEmpty` | 权限码列表 |

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

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `String` | ✅ | 角色预设ID |

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

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `name` | `String` | ❌ | `@Size(max=50)` | 预设名称 |
| `permissionCodes` | `List<String>` | ❌ | | 权限码列表 |

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

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `String` | ✅ | 用户ID |

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

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `permissionCodes` | `List<String>` | ✅ | 权限码列表（`模块:动作` 格式） |
| `reason` | `String` | ❌ | 授权原因 |

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

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `userId` | `String` | ❌ | | 用户ID |
| `startDate` | `String` | ❌ | | 开始日期 |
| `endDate` | `String` | ❌ | | 结束日期 |
| `sort` | `String` | ❌ | `-createdAt` | 排序 |

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "pcl_demo001",
        "userId": "res_admin01",
        "userName": "管理员",
        "operatorId": "res_admin02",
        "operatorName": "超级管理员",
        "action": "grant",
        "permissionCode": "coordinator:manage",
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

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `residentId` | `String` | ❌ | | 业主ID筛选 |
| `action` | `String` | ❌ | `freeze` | `freeze`（冻结记录）/ `unfreeze`（解冻记录） |
| `propertyCompanyId` | `String` | ❌ | | 物业公司ID（超管必传） |
| `sort` | `String` | ❌ | `-createdAt` | 排序 |

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

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `amount` | `BigDecimal` | ✅ | `@NotNull @DecimalMin("0")` | 冻结金额（0=全部冻结） |
| `reason` | `String` | ✅ | `@NotBlank @Size(min=1, max=200)` | 冻结原因 |

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

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `frozenRecordId` | `String` | ❌ | | 冻结记录ID（不传解冻全部） |
| `reason` | `String` | ✅ | `@NotBlank @Size(min=1, max=200)` | 解冻原因 |

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

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `expiryDate` | `String` | ✅ | `@NotBlank` | 过期日期（yyyy-MM-dd） |
| `dryRun` | `Boolean` | ❌ | | 仅预览不执行（默认false） |
| `residentIds` | `List<String>` | ❌ | | 指定业主ID列表（不传则全部） |
| `propertyCompanyId` | `String` | ❌ | | 物业公司ID（不传则全部） |

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

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `residentId` | `String` | ❌ | | 业主ID筛选 |
| `startDate` | `String` | ❌ | | 开始日期 |
| `endDate` | `String` | ❌ | | 结束日期 |
| `propertyCompanyId` | `String` | ❌ | | 物业公司ID |

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

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | `int` | ❌ | `1` | 页码 |
| `pageSize` | `int` | ❌ | `20` | 每页条数 |
| `keyword` | `String` | ❌ | | 搜索关键词（名称/电话） |
| `status` | `String` | ❌ | | `active`/`inactive` |
| `sort` | `String` | ❌ | `-createdAt` | 排序 |

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

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| `name` | `String` | ✅ | `@NotBlank` | 物业公司名称 |
| `logoUrl` | `String` | ❌ | | Logo URL |
| `contactPhone` | `String` | ✅ | `@NotBlank` | 联系电话 |
| `address` | `String` | ❌ | | 地址 |
| `communities` | `List` | ✅ | `@NotEmpty @Valid` | 小区列表（至少1个） |
| `communities[].name` | `String` | ✅ | `@NotBlank` | 小区名称 |
| `communities[].address` | `String` | ❌ | | 小区地址 |
| `communities[].totalBuildings` | `Integer` | ❌ | | 楼栋总数 |
| `communities[].totalUnits` | `Integer` | ❌ | | 单元总数 |
| `adminAccount` | `Object` | ✅ | `@Valid` | 管理员账号 |
| `adminAccount.name` | `String` | ✅ | `@NotBlank` | 管理员姓名 |
| `adminAccount.phone` | `String` | ✅ | `@NotBlank` | 管理员手机号 |
| `adminAccount.email` | `String` | ❌ | | 管理员邮箱 |

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
      "phone": "139****1111"
    },
    "defaultConfigs": "已应用默认配置",
    "createdAt": "2026-07-01 15:23:33"
  },
  "timestamp": "2026-07-01 15:23:33"
}
```

---

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

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `name` | `String` | ❌ | 名称 |
| `logoUrl` | `String` | ❌ | Logo |
| `contactPhone` | `String` | ❌ | 联系电话 |
| `address` | `String` | ❌ | 地址 |

---

### 51.5 PATCH `/admin/property-companies/{id}/status` — 更新物业公司状态

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `status` | `String` | ✅ | `active`/`inactive` |

---

### 51.6 PATCH `/admin/property-companies/{id}/config` — 更新物业公司配置

**请求示例**：

```bash
curl -X PATCH https://test.yuanxusoftware.top/api/v1/admin/property-companies/pc_demo001/config \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "propertyShareRate": 0.30,
    "coordinatorShareRate": 0.70,
    "sectorLeaderRate": 0.10,
    "individualLeaderRate": 0.00,
    "coinDisplayEnabled": true,
    "coinExpiryDays": 365,
    "coinFreezeDefault": false,
    "deliveryBaseFee": 0.50,
    "deliveryCourierShareRate": 1.00,
    "deliveryCourierPerOrder": 0.35,
    "withdrawalFeeRate": 0.006,
    "pointToFeeRate": 0.01,
    "twoYearClearEnabled": true,
    "neighborDailyContactLimit": 2
  }'
```

**请求体**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `propertyShareRate` | `BigDecimal` | ❌ | 物业分成比例（默认0.30） |
| `coordinatorShareRate` | `BigDecimal` | ❌ | 统筹分成比例（默认0.70） |
| `sectorLeaderRate` | `BigDecimal` | ❌ | 板块负责人比例（默认0.10） |
| `individualLeaderRate` | `BigDecimal` | ❌ | 个体负责人比例（默认0.00） |
| `coinDisplayEnabled` | `Boolean` | ❌ | 物业币卡片全局显示 |
| `coinIssueMode` | `String` | ❌ | 发放模式：`auto`/`manual` |
| `coinExpiryDays` | `Integer` | ❌ | 物业币有效期天数（默认365） |
| `coinFreezeDefault` | `Boolean` | ❌ | 新用户物业币默认冻结 |
| `deliveryBaseFee` | `BigDecimal` | ❌ | 配送起步价 |
| `deliveryCourierShareRate` | `BigDecimal` | ❌ | 快递员配送费分成比例 |
| `deliveryCourierPerOrder` | `BigDecimal` | ❌ | 快递员每单报酬 |
| `withdrawalFeeRate` | `BigDecimal` | ❌ | 提现手续费率 |
| `pointToFeeRate` | `BigDecimal` | ❌ | 积分抵扣物业费比率 |
| `twoYearClearEnabled` | `Boolean` | ❌ | 2年欠费积分清零开关 |
| `neighborDailyContactLimit` | `Integer` | ❌ | 邻居私聊每日限制 |

---

# 枚举值速查表

## 公告类型 (AnnouncementType)

| 值 | 说明 |
|----|------|
| `property_announcement` | 物业公告 |
| `community_announcement` | 社区公告 |
| `platform_announcement` | 统筹/官方推荐公告 |
| `system_announcement` | 系统公告 |
| `merchant_announcement` | 商家公告 |
| `activity_announcement` | 活动组公告 |

## 审核状态 (AuditStatus)

| 值 | 说明 |
|----|------|
| `pending_audit` | 待审核 |
| `approved` | 已通过 |
| `rejected` | 已拒绝 |

## 物业币类型 (CoinType)

| 值 | 说明 |
|----|------|
| `earn` | 获得 |
| `spend` | 消费 |
| `recharge` | 充值 |
| `freeze` | 冻结 |
| `unfreeze` | 解冻 |
| `expire` | 过期 |
| `withdraw` | 提现 |

## 配送状态 (DeliveryStatus)

| 值 | 说明 |
|----|------|
| `pending` | 待抢单 |
| `accepted` | 已接单 |
| `delivering` | 配送中 |
| `completed` | 已完成 |
| `failed` | 配送失败 |
| `property_fallback` | 物业兜底 |

## 物业费状态 (FeeStatus)

| 值 | 说明 |
|----|------|
| `unpaid` | 未支付 |
| `partial` | 部分支付 |
| `paid` | 已支付 |
| `overdue` | 逾期 |

## 商家等级 (MerchantLevel)

| 值 | 说明 |
|----|------|
| `official_certified` | 官方认证 |
| `property_certified` | 物业认证 |
| `normal` | 普通 |

## 消息类型 (MessageType)

| 值 | 说明 |
|----|------|
| `property_message` | 物业消息 |
| `platform_message` | 统筹消息 |
| `system_message` | 系统消息 |
| `community_message` | 社区消息 |
| `resident_message` | 业主消息(私聊) |
| `technician_message` | 技工消息(群发) |

## 订单状态 (OrderStatus)

| 值 | 说明 | 流转 |
|----|------|------|
| `pending` | 待支付 | → `paid`, `cancelled` |
| `paid` | 已支付 | → `delivering`, `refunding` |
| `delivering` | 配送中 | → `completed` |
| `completed` | 已完成 | 终态 |
| `cancelled` | 已取消 | 终态 |
| `refunding` | 退款中 | → `refunded` |
| `refunded` | 已退款 | 终态 |

## 支付方式 (PaymentMethod)

| 值 | 说明 |
|----|------|
| `point` | 积分 |
| `coin` | 物业币 |
| `wechat` | 微信 |
| `mixed` | 混合 |

## 积分来源 (PointSource)

| 值 | 说明 |
|----|------|
| `order` | 订单 |
| `activity` | 活动 |
| `sign_in` | 签到 |
| `manual` | 手动 |
| `merchant_grant` | 商家录入 |
| `fee_deduct` | 物业费抵扣 |
| `pool_diff` | 积分池差额 |
| `transfer` | 家庭转移 |

## 积分类型 (PointType)

| 值 | 说明 |
|----|------|
| `earn` | 获得 |
| `spend` | 消费 |
| `transfer` | 转移 |
| `expire` | 过期 |
| `pool_in` | 进入积分池 |

## 积分池记录类型 (PoolRecordType)

| 值 | 说明 |
|----|------|
| `diff` | 兑换差额 |
| `expiry_clear` | 过期清零 |
| `fee_clear` | 物业费清零 |
| `manual_adjust` | 手动调整 |
| `manual_out` | 手动流出 |

## 用户角色 (UserRole)

| 值 | 说明 |
|----|------|
| `resident` | 业主/住户 |
| `property_admin` | 物业管理员 |
| `platform_admin` | 平台管理员 |
| `merchant` | 商家 |
| `courier` | 快递员 |
| `coordinator` | 统筹负责人 |
| `sector_leader` | 板块负责人 |
| `individual_leader` | 个体负责人 |
| `activity_leader` | 活动组组长 |
| `technician` | 技工 |

## 用户类型 (UserType)

| 值 | 说明 |
|----|------|
| `owner` | 业主 |
| `tenant` | 租户 |

## 投票选项 (VoteOption)

| 值 | 说明 |
|----|------|
| `support` | 支持 |
| `oppose` | 反对 |
| `abstain` | 弃权 |

## 提现状态 (WithdrawalStatus)

| 值 | 说明 |
|----|------|
| `pending_audit` | 待审核 |
| `approved` | 已通过 |
| `rejected` | 已拒绝 |
| `completed` | 已完成 |
| `failed` | 失败 |

---

# 错误码速查表

## 通用/系统错误 (10xxx)

| 错误码 | HTTP | 含义 | 触发场景 |
|--------|------|------|---------|
| `10001` | 400 | 参数缺失 | 必填字段为空 |
| `10002` | 400 | 参数格式错误 | 日期格式不合法等 |
| `10003` | 400 | 参数值超出范围 | pageSize超过100等 |

## 鉴权/权限错误 (20xxx)

| 错误码 | HTTP | 含义 | 触发场景 |
|--------|------|------|---------|
| `20001` | 401 | Token缺失 | 未携带 Authorization |
| `20002` | 401 | Token过期 | JWT 过期 |
| `20003` | 401 | Token无效 | JWT 解析失败 |
| `20004` | 403 | 权限不足 | 当前角色无操作权限 |
| `20005` | 403 | 账号被禁用 | 用户被冻结 |

## 业主/住户错误 (30xxx)

| 错误码 | HTTP | 含义 | 触发场景 |
|--------|------|------|---------|
| `30001` | 404 | 业主不存在 | 用户ID查不到 |
| `30002` | 409 | 家庭人数超限 | 家庭成员超过15人 |
| `30003` | 400 | 微信授权失败 | 微信绑定授权被拒绝 |
| `30004` | 409 | 重复挂接 | 该商家已在当前物业下挂接 |

## 积分错误 (40xxx)

| 错误码 | HTTP | 含义 | 触发场景 |
|--------|------|------|---------|
| `40001` | 400 | 积分余额不足 | 积分不够兑换/使用 |
| `40002` | 404 | 积分规则不存在 | 积分配置ID无效 |

## 物业币错误 (50xxx)

| 错误码 | HTTP | 含义 | 触发场景 |
|--------|------|------|---------|
| `50001` | 400 | 物业币余额不足 | 物业币不够支付 |
| `50002` | 403 | 积分与物业币不可互兑 | 尝试互兑操作 |

## 商家错误 (60xxx)

| 错误码 | HTTP | 含义 | 触发场景 |
|--------|------|------|---------|
| `60001` | 404 | 商家不存在 | 商家ID无效 |
| `60002` | 409 | 商家入驻审核中 | 商家状态不允许操作 |
| `60003` | 409 | 审核已处理 | 重复审核 |
| `60004` | 404 | 商品不存在 | 商品ID无效 |

## 订单错误 (70xxx)

| 错误码 | HTTP | 含义 | 触发场景 |
|--------|------|------|---------|
| `70001` | 404 | 订单不存在 | 订单ID无效 |
| `70002` | 409 | 订单状态不允许操作 | 已完成订单不可取消等 |
| `70003` | 409 | 并发冲突 | 同一订单多人同时支付 |
| `71001` | 404 | 物业费账单不存在 | 账单ID无效 |

## 配送错误 (80xxx)

| 错误码 | HTTP | 含义 | 触发场景 |
|--------|------|------|---------|
| `80001` | 409 | 快递员抢单失败 | 订单已被其他快递员抢走 |
| `80002` | 404 | 配送规则不存在 | 规则ID无效 |
| `80003` | 409 | 配送规则重复 | 该物业已存在配送规则 |

## 公告/消息/活动错误 (90xxx)

| 错误码 | HTTP | 含义 | 触发场景 |
|--------|------|------|---------|
| `90001` | 404 | 公告不存在 | 公告ID无效 |
| `90002` | 409 | 投票已结束 | 意见箱投票截止 |
| `90003` | 409 | 已提交过收集信息 | 重复提交 |
| `90004` | 404 | 意见箱不存在 | 意见箱ID无效 |
| `90005` | 409 | 该家庭已投过票 | 一户一票限制 |
| `90006` | 409 | 每家庭每月仅可发表1条意见 | 月度限制 |
| `90007` | 404 | 活动组不存在 | 活动组ID无效 |
| `90008` | 409 | 已订阅该活动组 | 重复订阅 |
| `90009` | 409 | 已是活动组成员 | 重复加入 |
| `90010` | 409 | 不是活动组成员 | 尝试退出非成员活动组 |
| `90011` | 409 | 组长不能退出活动组 | 组长限制 |
| `90012` | 404 | 消息不存在 | 消息ID无效 |
| `90013` | 409 | 今日邻居私聊次数已达上限 | 每日限制 |

## 统筹/板块/个体负责人错误 (91xxx)

| 错误码 | HTTP | 含义 | 触发场景 |
|--------|------|------|---------|
| `91001` | 404 | 统筹不存在 | 统筹ID无效 |
| `91002` | 404 | 板块负责人不存在 | ID无效 |
| `91003` | 404 | 个体负责人不存在 | ID无效 |

## 权限错误 (92xxx)

| 错误码 | HTTP | 含义 | 触发场景 |
|--------|------|------|---------|
| `92001` | 404 | 权限项不存在 | 权限ID无效 |
| `92002` | 404 | 角色预设不存在 | 预设ID无效 |
| `92003` | 500 | 权限变更日志写入失败 | 系统错误 |

## 积分池/物业币控制错误 (93xxx)

| 错误码 | HTTP | 含义 | 触发场景 |
|--------|------|------|---------|
| `93001` | 404 | 积分池不存在 | 积分池未初始化 |
| `93002` | 400 | 积分池余额不足 | 积分池资金不够 |
| `93003` | 403 | 物业币已冻结 | 冻结状态下无法使用 |
| `93004` | 400 | 物业币已过期 | 过期物业币无法使用 |

## 商家积分/提现错误 (94xxx)

| 错误码 | HTTP | 含义 | 触发场景 |
|--------|------|------|---------|
| `94001` | 409 | 积分购买审核中 | 审核中不可操作 |
| `94002` | 400 | 商家积分余额不足 | 可用积分不够 |
| `94003` | 409 | 提现申请不允许操作 | 状态不允许 |
| `94004` | 400 | 兑换金额不合法 | 金额不满足条件 |

## 文件/服务/物业公司错误 (95xxx-97xxx)

| 错误码 | HTTP | 含义 | 触发场景 |
|--------|------|------|---------|
| `95001` | 404 | 文件不存在 | 文件ID无效 |
| `96001` | 404 | 服务不存在 | 服务ID无效 |
| `96002` | 404 | 服务订单不存在 | 服务订单ID无效 |
| `96003` | 409 | 已订阅该服务 | 重复订阅 |
| `97001` | 404 | 物业公司不存在 | 物业公司ID无效 |

## 系统错误 (99xxx)

| 错误码 | HTTP | 含义 | 触发场景 |
|--------|------|------|---------|
| `99001` | 500 | 内部服务异常 | 未知错误 |

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
消费金额 P × 商家抽佣比例 R_T = 可分利润
                     │
    ┌────────────────┼────────────────┐
    │                │                │
    ▼                ▼                ▼
物业公司 30%      统筹 70%        积分成本
    │                │            物业币成本
    │                │
    │         ┌──────┴──────┐
    │         │             │
    │    板块负责人      个体负责人
    │    (统筹×10%)     (板块×比例)
    │
    └──→ 积分池资金
```

---

# WebSocket 接口

## 连接地址

```
wss://test.yuanxusoftware.top/ws?token=<accessToken>
```

> **注意**：生产环境使用 `wss://` 协议，开发环境使用 `wss://test.yuanxusoftware.top/ws/v1?token=<accessToken>`

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

| type | 方向 | payload | 说明 |
|------|------|---------|------|
| `chat.message` | 双向 | `{ fromId, toId, content, chatType }` | 聊天消息 |
| `chat.broadcast` | 服务端→客户端 | `{ fromId, targetRole, content }` | 群发消息 |
| `notification.new` | 服务端→客户端 | `{ announcementId, announcementType, title }` | 新公告通知 |
| `order.status_update` | 服务端→客户端 | `{ orderId, orderStatus, updatedAt }` | 订单状态变更 |
| `delivery.grab_result` | 服务端→客户端 | `{ orderId, courierId, result }` | 抢单结果 |
| `point.balance_update` | 服务端→客户端 | `{ residentId, pointType, balance }` | 余额变更 |
| `heartbeat.ping` | 客户端→服务端 | `{}` | 心跳 |
| `heartbeat.pong` | 服务端→客户端 | `{}` | 心跳回复 |

## 心跳与重连

- 客户端每 **30秒** 发送 `heartbeat.ping`
- 服务端回复 `heartbeat.pong`
- **60秒** 未收到 pong → 判定断开 → 触发重连
- 重连策略：1s → 3s → 5s → 10s → 30s（最多5次）

## 订阅端点

| 端点 | 说明 | 示例 |
|------|------|------|
| `/user/queue/notifications` | 个人通知 | 新公告、订单状态变更 |
| `/topic/chat/{userId}` | 私聊消息 | 邻居聊天 |
| `/topic/broadcast/{propertyCompanyId}` | 物业广播 | 物业公告广播 |
| `/topic/delivery/{courierId}` | 配送通知 | 抢单结果、配送任务 |
| `/topic/order/{orderId}` | 订单状态 | 订单状态实时更新 |

---

# 附录

## ID 前缀规则

| 资源 | 前缀 | 示例 |
|------|------|------|
| 物业公司 | `pc_` | `pc_a8f3k2` |
| 业主/住户 | `res_` | `res_b7m9x1` |
| 商家 | `mch_` | `mch_c4d2p8` |
| 订单 | `ord_` | `ord_e6n5q3` |
| 积分记录 | `pt_` | `pt_f1h8w7` |
| 物业币记录 | `coin_` | `coin_g3j2k9` |
| 公告 | `ann_` | `ann_l5r6t2` |
| 服务 | `svc_` | `svc_v8y4z1` |
| 家庭 | `fam_` | `fam_h4j7k2` |
| 小区 | `com_` | `com_xxx` |
| 文件 | `file_` | `file_xxx` |

## 字段命名规则

- API 请求/响应字段使用 **camelCase**
- 数据库字段使用 **snake_case**
- 后端自动转换，前端直接使用 camelCase

## 日期格式

- 所有日期时间字段使用 `yyyy-MM-dd HH:mm:ss` 格式
- 日期字段使用 `yyyy-MM-dd` 格式

---

> **文档维护**：连云港元序软件有限公司  
> **负责人**：王乙臣  
> **最后更新**：2026-07-04
