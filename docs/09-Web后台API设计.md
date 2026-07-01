# Web 后台 API 设计 - 物业积分服务系统

> 适用范围：Web 后台管理端（平台管理员 / 物业管理员使用），与 App 端 API（`/api/v1/`）独立。
> 配套文档：`03-API接口设计.md`（App 端）、`02-数据库设计.md`、`00-项目概览.md`。

## 1. 设计原则

### 1.1 路径与版本
- 全部接口统一前缀：`/api/v1/admin/`
- RESTful 风格，资源路径使用名词复数
- 小写字母 + 连字符（kebab-case）
- 子资源嵌套不超过两层（如 `/property-companies/:id/configs`）

### 1.2 鉴权与角色
- 所有接口鉴权：**Required**
- 允许角色：`platform_admin`（平台管理员）、`property_admin`（物业管理员），部分接口允许 `sector_leader`
- 鉴权方式：`Authorization: Bearer <accessToken>`

| 角色 | 数据范围 |
|------|----------|
| `platform_admin` | 全平台数据，可跨物业公司操作 |
| `property_admin` | 仅本物业公司数据（基于 `property_company_id` 强隔离） |
| `sector_leader` | 仅本人负责板块下的个体负责人/收益数据 |

### 1.3 数据隔离规则
1. `property_admin` 调用任何接口时，服务端自动注入 `propertyCompanyId = 当前账号所属物业公司ID`，无法越权访问其他物业公司数据。
2. 路径中出现的 `:id`（物业公司ID）对 `property_admin` 而言必须等于其所属物业公司，否则返回 `403 forbidden_company_access`。
3. `platform_admin` 可访问任意物业公司，需显式传 `propertyCompanyId`。

### 1.4 统一响应格式
```json
{
  "code": 0,
  "message": "success",
  "data": { },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```
- `code = 0` 表示成功；非 0 表示业务错误
- 参数校验错误附带 `errors` 数组（字段级）
- 列表接口统一返回 `{ list: [], pagination: {...} }`

### 1.5 通用请求头
| Header | 必填 | 说明 |
|--------|------|------|
| `Authorization` | 是 | `Bearer <accessToken>` |
| `Content-Type` | 是（POST/PUT/PATCH） | `application/json` |
| `X-Request-Id` | 否 | 链路追踪ID |

### 1.6 分页与排序
| 参数 | 类型 | 默认 | 说明 |
|------|------|------|------|
| `page` | number | 1 | 当前页码 |
| `pageSize` | number | 20 | 每页条数（最大 100） |
| `sort` | string | `-createdAt` | 排序字段（`-`降序、`+`升序），多字段逗号分隔 |

通用过滤参数：`status`、`keyword`、`createdAt.gte`、`createdAt.lte`、`propertyCompanyId`（仅 platform_admin 可用）。

### 1.7 错误码段
| 段 | 说明 |
|----|------|
| `0` | 成功 |
| `10000-19999` | 通用错误（参数/鉴权/限流） |
| `20000-29999` | 物业公司/配置模块 |
| `30000-39999` | 商家模块 |
| `40000-49999` | 用户/家庭/权限模块 |
| `50000-59999` | 分成/积分池/物业币模块 |
| `60000-69999` | 物业费/公告/审核模块 |
| `70000-79999` | 报表/统计模块 |

---

## 2. 物业公司管理（platform_admin only）

### 2.1 GET `/api/v1/admin/property-companies` - 物业公司总览面板
> 列表 + 运营概况（商家数 / 住户数 / 消费总额 / 积分池余额）

**鉴权**：Required
**允许角色**：`platform_admin`

**请求参数（Query）**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `page` | number | 否 | 当前页 |
| `pageSize` | number | 否 | 每页条数 |
| `keyword` | string | 否 | 公司名称模糊搜索 |
| `status` | string | 否 | `active` / `inactive` |
| `sort` | string | 否 | 默认 `-createdAt` |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "pc_a1b2c3d4",
        "name": "万达物业管理有限公司",
        "logoUrl": "https://cdn.example.com/pc/logo.png",
        "contactPhone": "021-88888888",
        "address": "上海市浦东新区张江路100号",
        "status": "active",
        "communityCount": 5,
        "stats": {
          "merchantCount": 128,
          "residentCount": 4520,
          "totalConsumptionAmount": 1289500.00,
          "pointPoolBalance": 3858000
        },
        "createdAt": "2026-01-15T09:30:00.000Z",
        "updatedAt": "2026-06-30T12:00:00.000Z"
      }
    ],
    "pagination": { "page": 1, "pageSize": 20, "total": 36, "totalPages": 2 }
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

---

### 2.2 POST `/api/v1/admin/property-companies` - 新物业公司入驻
> 填写公司信息 + 小区信息 + 管理员账号 → 生成独立运营空间

**鉴权**：Required
**允许角色**：`platform_admin`

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `name` | string | 是 | 物业公司名称 |
| `logoUrl` | string | 否 | 公司Logo URL |
| `contactPhone` | string | 是 | 联系电话 |
| `address` | string | 否 | 公司地址 |
| `communities` | array | 是 | 小区列表（至少1个） |
| `communities[].name` | string | 是 | 小区名称 |
| `communities[].address` | string | 否 | 小区地址 |
| `communities[].totalBuildings` | number | 否 | 总楼栋数 |
| `communities[].totalUnits` | number | 否 | 总户数 |
| `adminAccount` | object | 是 | 物业管理员账号 |
| `adminAccount.name` | string | 是 | 管理员姓名 |
| `adminAccount.phone` | string | 是 | 管理员手机号 |
| `adminAccount.password` | string | 是 | 初始密码（加密传输） |
| `adminAccount.email` | string | 否 | 邮箱 |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "pc_a1b2c3d4",
    "name": "万达物业管理有限公司",
    "status": "active",
    "communities": [
      { "id": "com_x1y2z3", "name": "万达花园", "totalBuildings": 12, "totalUnits": 720 }
    ],
    "adminAccount": {
      "id": "res_admin001",
      "name": "张三",
      "phone": "13800000001",
      "role": "property_admin"
    },
    "defaultConfigs": "已自动初始化默认配置项",
    "createdAt": "2026-07-01T10:00:00.000Z"
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

---

### 2.3 GET `/api/v1/admin/property-companies/:id` - 物业公司详情
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`（仅本人所属公司）

**路径参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | string | 是 | 物业公司ID |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "pc_a1b2c3d4",
    "name": "万达物业管理有限公司",
    "logoUrl": "https://cdn.example.com/pc/logo.png",
    "contactPhone": "021-88888888",
    "address": "上海市浦东新区张江路100号",
    "status": "active",
    "communities": [
      { "id": "com_x1y2z3", "name": "万达花园", "totalBuildings": 12, "totalUnits": 720, "status": "active" }
    ],
    "adminAccounts": [
      { "id": "res_admin001", "name": "张三", "phone": "13800000001", "lastLoginAt": "2026-06-30T18:00:00.000Z" }
    ],
    "createdAt": "2026-01-15T09:30:00.000Z",
    "updatedAt": "2026-06-30T12:00:00.000Z"
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

---

### 2.4 PUT `/api/v1/admin/property-companies/:id` - 更新物业公司信息
**鉴权**：Required
**允许角色**：`platform_admin`

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `name` | string | 否 | 物业公司名称 |
| `logoUrl` | string | 否 | 公司Logo |
| `contactPhone` | string | 否 | 联系电话 |
| `address` | string | 否 | 公司地址 |

**响应**：返回更新后的物业公司完整对象（结构同 2.3）。

---

### 2.5 PATCH `/api/v1/admin/property-companies/:id/status` - 启用/停用物业公司
**鉴权**：Required
**允许角色**：`platform_admin`

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `status` | string | 是 | `active` / `inactive` |
| `reason` | string | 否 | 停用原因（停用时必填） |

**响应**：
```json
{
  "code": 0,
  "message": "success",
  "data": { "id": "pc_a1b2c3d4", "status": "inactive", "updatedAt": "2026-07-01T10:00:00.000Z" },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

> 停用后该公司所有管理员账号自动失效，App 端无法登录、消费；积分池冻结。

---

### 2.6 GET `/api/v1/admin/property-companies/:id/stats` - 运营统计数据
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`（仅本人所属公司）

**请求参数（Query）**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `startDate` | string | 否 | 起始日期 `YYYY-MM-DD` |
| `endDate` | string | 否 | 结束日期 `YYYY-MM-DD` |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "propertyCompanyId": "pc_a1b2c3d4",
    "propertyName": "万达物业管理有限公司",
    "summary": {
      "merchantCount": 128,
      "residentCount": 4520,
      "familyCount": 1380,
      "totalConsumptionAmount": 1289500.00,
      "totalOrderCount": 28560,
      "pointPoolBalance": 3858000,
      "propertyCoinCirculation": 528600.00,
      "propertyFeeCollectionRate": 0.8765
    },
    "trend": [
      { "date": "2026-06-01", "orderCount": 920, "consumptionAmount": 42500.00 },
      { "date": "2026-06-02", "orderCount": 980, "consumptionAmount": 45120.00 }
    ]
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

---

## 3. 物业公司配置管理（property_admin）

### 3.1 GET `/api/v1/admin/property-companies/:id/configs` - 获取配置项
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`（仅本人所属公司）

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "propertyCompanyId": "pc_a1b2c3d4",
    "configs": {
      "pointExchangeRate": 100,                  // 积分兑换比例：100积分=1元
      "propertyCoinCashbackRate": 0.05,          // 物业币返现比例（消费额的5%）
      "merchantCommissionRate": 0.10,            // 商家抽佣比例（默认10%）
      "distributionRates": {                     // 四级分成比例
        "propertyRate": 0.30,                    // 物业 30%
        "coordinatorRate": 0.70,                 // 统筹 70%
        "sectorLeaderRate": 0.10,                // 板块负责人（统筹收入的10%）
        "individualLeaderRate": 0.05             // 个体负责人（统筹收入的5%）
      },
      "propertyCoinValidityMonths": 24,          // 物业币有效期（月）
      "pointValidityMonths": 24,                 // 积分有效期（月）
      "displayConfig": {
        "showPropertyCoin": true,                // 是否显示物业币
        "showDistributionDetail": false          // 是否显示分成明细
      },
      "issuanceMode": "auto",                    // 发放模式：auto/manual
      "deliveryConfig": {
        "baseFee": 3.00,                         // 配送起步价
        "tieredPricing": [                       // 阶梯计费
          { "maxWeightKg": 5, "fee": 3.00 },
          { "maxWeightKg": 10, "fee": 5.00 },
          { "maxWeightKg": 999, "fee": 8.00 }
        ]
      },
      "grabOrderReward": 2.00,                   // 抢单报酬
      "withdrawalFeeRate": 0.006,                // 提现手续费比例
      "activityGroupFee": {
        "monthlyFee": 30.00,                     // 活动组月费
        "yearlyFee": 300.00                      // 活动组年费
      },
      "announcementPushDimensions": [            // 通告推送维度
        "role", "building", "community"
      ],
      "selfOperatedReplacementMode": "platform_merchant",  // 自营替代模式：platform_merchant/external_merchant
      "pointPoolSpendRequireVote": true,         // 动用积分池是否需要业主投票
      "pointPoolSpendVoteThreshold": 0.66        // 投票通过阈值（2/3）
    },
    "updatedAt": "2026-06-30T12:00:00.000Z"
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

---

### 3.2 PUT `/api/v1/admin/property-companies/:id/configs` - 批量更新配置
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`（仅本人所属公司）

**请求参数（Body）**：与响应结构中的 `configs` 对象相同，仅传需要更新的字段。

```json
{
  "configs": {
    "pointExchangeRate": 120,
    "merchantCommissionRate": 0.12,
    "propertyCoinValidityMonths": 36
  },
  "remark": "调整积分兑换比例"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `configs` | object | 是 | 待更新配置项（部分字段） |
| `remark` | string | 否 | 变更备注 |

**响应**：返回更新后的完整配置（结构同 3.1）。

> 配置变更需记录到 `permission-logs` / 配置变更日志，便于追溯。

---

## 4. 平台级商家库管理（platform_admin）

### 4.1 GET `/api/v1/admin/platform-merchants` - 平台级商家库列表
**鉴权**：Required
**允许角色**：`platform_admin`

**请求参数（Query）**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `page` | number | 否 | 当前页 |
| `pageSize` | number | 否 | 每页条数 |
| `keyword` | string | 否 | 名称/电话模糊搜索 |
| `category` | string | 否 | 品类筛选 |
| `linkedPropertyCompanyId` | string | 否 | 已挂接到某物业公司的商家 |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "pmch_x1y2z3",
        "name": "万达超市",
        "category": "超市便利",
        "coverUrl": "https://cdn.example.com/merchant/001.jpg",
        "phone": "021-12345678",
        "address": "上海市浦东新区张江路50号",
        "linkedPropertyCount": 5,
        "createdAt": "2026-03-10T10:00:00.000Z"
      }
    ],
    "pagination": { "page": 1, "pageSize": 20, "total": 256, "totalPages": 13 }
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

---

### 4.2 POST `/api/v1/admin/platform-merchants` - 录入商家基础信息
> 一次录入基础信息（名称/地址/电话/品类等），后续可挂接到多个物业公司。

**鉴权**：Required
**允许角色**：`platform_admin`

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `name` | string | 是 | 商家名称 |
| `category` | string | 是 | 品类（超市/餐饮/家政/医疗/教育...） |
| `phone` | string | 是 | 联系电话 |
| `address` | string | 否 | 商家地址 |
| `longitude` | number | 否 | 经度 |
| `latitude` | number | 否 | 纬度 |
| `coverUrls` | array | 否 | 封面图URL数组 |
| `videoUrl` | string | 否 | 视频URL |
| `description` | string | 否 | 商家描述 |
| `businessLicense` | string | 否 | 营业执照URL |
| `contactPerson` | string | 否 | 联系人 |

**响应**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "pmch_x1y2z3",
    "name": "万达超市",
    "category": "超市便利",
    "createdAt": "2026-07-01T10:00:00.000Z"
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

---

### 4.3 GET `/api/v1/admin/platform-merchants/:id` - 商家详情
**鉴权**：Required
**允许角色**：`platform_admin`

**响应**：返回完整商家基础信息（结构同 4.2 响应 + 全部字段）。

---

### 4.4 PUT `/api/v1/admin/platform-merchants/:id` - 更新商家基础信息
**鉴权**：Required
**允许角色**：`platform_admin`

**请求参数（Body）**：同 4.2，所有字段可选。

**响应**：返回更新后的完整商家对象。

---

### 4.5 GET `/api/v1/admin/platform-merchants/:id/property-configs` - 商家在各物业下的参数配置列表
**鉴权**：Required
**允许角色**：`platform_admin`

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "merchantId": "pmch_x1y2z3",
    "merchantName": "万达超市",
    "configs": [
      {
        "id": "cfg_001",
        "propertyCompanyId": "pc_a1b2c3d4",
        "propertyCompanyName": "万达物业管理有限公司",
        "commissionRate": 0.10,            // 抽佣比例
        "pointExchangeRate": 100,          // 兑换比（100积分=1元）
        "discountRate": 0.95,              // 折扣价（95折）
        "cashbackRate": 0.05,              // 返现比（5%物业币）
        "status": "active",
        "createdAt": "2026-03-15T10:00:00.000Z"
      }
    ]
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

---

### 4.6 POST `/api/v1/admin/platform-merchants/:id/property-configs` - 为商家添加物业公司挂接
**鉴权**：Required
**允许角色**：`platform_admin`

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `propertyCompanyId` | string | 是 | 物业公司ID |
| `commissionRate` | number | 是 | 抽佣比例（0-1） |
| `pointExchangeRate` | number | 是 | 兑换比 |
| `discountRate` | number | 否 | 折扣价（0-1，默认1） |
| `cashbackRate` | number | 否 | 返现比（0-1，默认0） |

**响应**：返回新创建的配置项（结构同 4.5 configs 数组中的单项）。

---

### 4.7 PUT `/api/v1/admin/platform-merchants/:id/property-configs/:configId` - 更新某物业下的商家参数
**鉴权**：Required
**允许角色**：`platform_admin`

**请求参数（Body）**：同 4.6 字段（除 `propertyCompanyId` 外均可更新）。

**响应**：返回更新后的配置项。

---

### 4.8 DELETE `/api/v1/admin/platform-merchants/:id/property-configs/:configId` - 解除商家与某物业的挂接
**鉴权**：Required
**允许角色**：`platform_admin`

**请求参数（Query）**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `reason` | string | 是 | 解除原因 |

**响应**：
```json
{
  "code": 0,
  "message": "success",
  "data": { "configId": "cfg_001", "deletedAt": "2026-07-01T10:00:00.000Z" },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

> 解除挂接后，该公司下用户无法再看到该商家，已生成订单不受影响。

---

## 5. 商家审核与管理（property_admin）

### 5.1 GET `/api/v1/admin/merchants` - 本公司商家列表
> 仅显示挂接到本公司的商家

**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Query）**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `page` | number | 否 | 当前页 |
| `pageSize` | number | 否 | 每页条数 |
| `keyword` | string | 否 | 名称搜索 |
| `category` | string | 否 | 品类 |
| `merchantLevel` | string | 否 | `official_certified`/`property_certified`/`normal` |
| `status` | string | 否 | `active`/`frozen` |
| `propertyCompanyId` | string | 否 | 仅 platform_admin 可指定 |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "mch_001",
        "name": "万达超市",
        "category": "超市便利",
        "merchantLevel": "property_certified",
        "auditStatus": "approved",
        "status": "active",
        "commissionRate": 0.10,
        "rank": 1,
        "orderCount": 1280,
        "totalAmount": 56800.00,
        "createdAt": "2026-03-15T10:00:00.000Z"
      }
    ],
    "pagination": { "page": 1, "pageSize": 20, "total": 128, "totalPages": 7 }
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

---

### 5.2 GET `/api/v1/admin/merchants/pending` - 待审核商家列表
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**响应**：结构同 5.1，仅返回 `auditStatus = pending_audit` 的记录。

---

### 5.3 PATCH `/api/v1/admin/merchants/:id/audit` - 审核商家
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `auditResult` | string | 是 | `approved` / `rejected` |
| `rejectReason` | string | 否 | 拒绝时必填 |
| `merchantLevel` | string | 否 | 通过时可设定分级 |
| `commissionRate` | number | 否 | 通过时可调整抽佣比例 |

**响应**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "mch_001",
    "auditStatus": "approved",
    "merchantLevel": "property_certified",
    "auditedAt": "2026-07-01T10:00:00.000Z",
    "auditedBy": "res_admin001"
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

---

### 5.4 PATCH `/api/v1/admin/merchants/:id/status` - 冻结/解冻商家
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `status` | string | 是 | `active` / `frozen` |
| `reason` | string | 否 | 冻结原因（冻结时必填） |

**响应**：返回商家状态更新结果。

---

### 5.5 PATCH `/api/v1/admin/merchants/:id/level` - 设置商家分级
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `merchantLevel` | string | 是 | `official_certified` / `property_certified` / `normal` |

**响应**：
```json
{
  "code": 0,
  "message": "success",
  "data": { "id": "mch_001", "merchantLevel": "official_certified", "updatedAt": "2026-07-01T10:00:00.000Z" },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

> `official_certified` 仅 `platform_admin` 可设置。

---

### 5.6 PUT `/api/v1/admin/merchants/:id/rank` - 拖拽排序
> 服务排名手动拖拽

**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `newRank` | number | 是 | 新排序位置（从1开始） |
| `affectedMerchantIds` | array | 否 | 受影响的其他商家ID列表（用于批量更新） |

**响应**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "merchantId": "mch_001",
    "newRank": 3,
    "updatedAt": "2026-07-01T10:00:00.000Z"
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

---

### 5.7 DELETE `/api/v1/admin/merchants/:id` - 踢出商家
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Query）**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `reason` | string | 是 | 踢出原因 |

**响应**：
```json
{
  "code": 0,
  "message": "success",
  "data": { "id": "mch_001", "removedAt": "2026-07-01T10:00:00.000Z" },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

> 踢出后商家与该物业公司解除挂接，已生成订单继续有效。

---

## 6. 权限管理（property_admin）

### 6.1 GET `/api/v1/admin/permissions` - 权限池列表
> 全部可分配权限

**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "permissions": [
      {
        "id": "perm_001",
        "code": "merchant:audit",
        "name": "商家审核",
        "module": "merchant",
        "description": "审核商家入驻申请",
        "isPlatformOnly": false
      },
      {
        "id": "perm_002",
        "code": "property_company:create",
        "name": "创建物业公司",
        "module": "property_company",
        "description": "仅平台管理员可用",
        "isPlatformOnly": true
      }
    ],
    "modules": ["merchant", "property_company", "resident", "family", "report", "announcement", "fee"]
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

---

### 6.2 GET `/api/v1/admin/role-presets` - 角色快捷预设列表
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "presets": [
      {
        "id": "preset_001",
        "name": "超级管理员",
        "description": "拥有本公司所有权限",
        "permissionCodes": ["merchant:audit", "resident:manage", "report:view", "..."],
        "isBuiltin": true,
        "createdAt": "2026-01-15T09:30:00.000Z"
      },
      {
        "id": "preset_002",
        "name": "财务专员",
        "description": "仅查看报表与物业费管理",
        "permissionCodes": ["report:view", "property_fee:manage"],
        "isBuiltin": false,
        "createdAt": "2026-03-10T10:00:00.000Z"
      }
    ]
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

---

### 6.3 POST `/api/v1/admin/role-presets` - 创建角色预设
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `name` | string | 是 | 预设名称 |
| `description` | string | 否 | 描述 |
| `permissionCodes` | array | 是 | 权限代码数组 |

**响应**：返回新创建的预设（结构同 6.2 单项）。

---

### 6.4 PUT `/api/v1/admin/role-presets/:id` - 更新角色预设
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Body）**：同 6.3，字段可选。

**响应**：返回更新后的预设。

---

### 6.5 GET `/api/v1/admin/accounts` - 账号列表
> 含权限信息

**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Query）**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `keyword` | string | 否 | 姓名/手机号搜索 |
| `role` | string | 否 | 角色筛选 |
| `status` | string | 否 | 状态筛选 |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "res_admin001",
        "name": "张三",
        "phone": "13800000001",
        "role": "property_admin",
        "status": "active",
        "lastLoginAt": "2026-06-30T18:00:00.000Z",
        "permissions": {
          "presetId": "preset_001",
          "presetName": "超级管理员",
          "customPermissions": ["merchant:audit"],
          "totalPermissionCount": 35
        },
        "createdAt": "2026-01-15T09:30:00.000Z"
      }
    ],
    "pagination": { "page": 1, "pageSize": 20, "total": 8, "totalPages": 1 }
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

---

### 6.6 GET `/api/v1/admin/accounts/:id/permissions` - 获取用户权限
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "accountId": "res_admin001",
    "accountName": "张三",
    "appliedPreset": {
      "id": "preset_001",
      "name": "超级管理员"
    },
    "permissions": [
      { "code": "merchant:audit", "name": "商家审核", "source": "preset" },
      { "code": "resident:manage", "name": "用户管理", "source": "custom" }
    ],
    "effectivePermissionCount": 36,
    "updatedAt": "2026-06-30T12:00:00.000Z"
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

---

### 6.7 PUT `/api/v1/admin/accounts/:id/permissions` - 设置用户权限
> 勾选/取消单项，覆盖预设

**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `addPermissionCodes` | array | 否 | 新增权限代码 |
| `removePermissionCodes` | array | 否 | 移除权限代码 |
| `resetCustom` | boolean | 否 | 是否重置自定义权限（恢复为预设） |

**响应**：返回更新后的用户权限（结构同 6.6）。

---

### 6.8 POST `/api/v1/admin/accounts/:id/apply-preset` - 应用角色快捷预设
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `presetId` | string | 是 | 角色预设ID |
| `keepCustomPermissions` | boolean | 否 | 是否保留原有自定义权限（默认 false） |

**响应**：返回应用后的用户权限（结构同 6.6）。

---

### 6.9 GET `/api/v1/admin/permission-logs` - 权限变更日志
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Query）**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `accountId` | string | 否 | 按账号筛选 |
| `operatorId` | string | 否 | 按操作人筛选 |
| `startDate` | string | 否 | 起始日期 |
| `endDate` | string | 否 | 结束日期 |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "log_001",
        "accountId": "res_admin001",
        "accountName": "张三",
        "operatorId": "res_admin002",
        "operatorName": "李四",
        "action": "apply_preset",
        "targetType": "preset",
        "targetId": "preset_002",
        "targetName": "财务专员",
        "changes": {
          "added": ["report:view", "property_fee:manage"],
          "removed": ["merchant:audit"]
        },
        "createdAt": "2026-06-30T12:00:00.000Z"
      }
    ],
    "pagination": { "page": 1, "pageSize": 20, "total": 56, "totalPages": 3 }
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

---

## 7. 用户管理（property_admin）

### 7.1 GET `/api/v1/admin/residents` - 业主/住户列表
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Query）**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `keyword` | string | 否 | 姓名/手机号搜索 |
| `communityId` | string | 否 | 小区筛选 |
| `building` | string | 否 | 楼栋筛选 |
| `status` | string | 否 | `active`/`frozen`/`disabled` |
| `role` | string | 否 | 角色筛选 |
| `propertyCompanyId` | string | 否 | 仅 platform_admin 可指定 |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "res_001",
        "name": "王五",
        "phone": "13800001234",
        "avatarUrl": "https://cdn.example.com/avatar/001.jpg",
        "role": "resident",
        "communityId": "com_x1y2z3",
        "communityName": "万达花园",
        "building": "3栋",
        "unit": "1单元",
        "room": "502",
        "familyId": "fam_001",
        "pointBalance": 8420,
        "coinBalance": 156.80,
        "status": "active",
        "createdAt": "2026-02-10T10:00:00.000Z"
      }
    ],
    "pagination": { "page": 1, "pageSize": 20, "total": 4520, "totalPages": 226 }
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

---

### 7.2 GET `/api/v1/admin/residents/:id` - 业主详情
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "res_001",
    "name": "王五",
    "phone": "13800001234",
    "avatarUrl": "https://cdn.example.com/avatar/001.jpg",
    "gender": 1,
    "role": "resident",
    "propertyCompanyId": "pc_a1b2c3d4",
    "propertyName": "万达物业管理有限公司",
    "communityId": "com_x1y2z3",
    "communityName": "万达花园",
    "building": "3栋",
    "unit": "1单元",
    "room": "502",
    "familyId": "fam_001",
    "familyName": "王五家庭",
    "pointBalance": 8420,
    "coinBalance": 156.80,
    "totalConsumption": 12500.00,
    "totalOrders": 86,
    "wechantBound": true,
    "status": "active",
    "createdAt": "2026-02-10T10:00:00.000Z",
    "lastLoginAt": "2026-06-30T18:00:00.000Z"
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

---

### 7.3 PATCH `/api/v1/admin/residents/:id/status` - 冻结/解冻/禁用账号
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `status` | string | 是 | `active` / `frozen` / `disabled` |
| `reason` | string | 否 | 操作原因（冻结/禁用时必填） |

**响应**：返回用户状态更新结果。

---

### 7.4 GET `/api/v1/admin/families` - 家庭列表
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Query）**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `keyword` | string | 否 | 家庭名称/户主姓名搜索 |
| `communityId` | string | 否 | 小区筛选 |
| `auditStatus` | string | 否 | `pending`/`approved`/`rejected` |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "fam_001",
        "name": "王五家庭",
        "ownerId": "res_001",
        "ownerName": "王五",
        "communityName": "万达花园",
        "building": "3栋",
        "unit": "1单元",
        "room": "502",
        "memberCount": 4,
        "pointBalance": 12500,
        "coinBalance": 280.50,
        "auditStatus": "approved",
        "createdAt": "2026-02-10T10:00:00.000Z"
      }
    ],
    "pagination": { "page": 1, "pageSize": 20, "total": 1380, "totalPages": 69 }
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

---

### 7.5 GET `/api/v1/admin/families/:id` - 家庭详情（含成员）
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "fam_001",
    "name": "王五家庭",
    "ownerId": "res_001",
    "ownerName": "王五",
    "communityName": "万达花园",
    "building": "3栋",
    "unit": "1单元",
    "room": "502",
    "memberCount": 4,
    "pointBalance": 12500,
    "coinBalance": 280.50,
    "auditStatus": "approved",
    "members": [
      {
        "id": "res_001",
        "name": "王五",
        "phone": "13800001234",
        "relation": "owner",
        "isOwner": true,
        "pointBalance": 8420,
        "coinBalance": 156.80,
        "status": "active"
      },
      {
        "id": "res_002",
        "name": "李四",
        "phone": "13800005678",
        "relation": "spouse",
        "isOwner": false,
        "pointBalance": 4080,
        "coinBalance": 123.70,
        "status": "active"
      }
    ],
    "createdAt": "2026-02-10T10:00:00.000Z"
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

---

### 7.6 PATCH `/api/v1/admin/families/:id/audit` - 家庭审核
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `auditResult` | string | 是 | `approved` / `rejected` |
| `rejectReason` | string | 否 | 拒绝时必填 |

**响应**：返回更新后的家庭信息。

---

### 7.7 POST `/api/v1/admin/residents/:id/migrate` - 迁移用户到另一物业公司
> 搬迁场景

**鉴权**：Required
**允许角色**：`platform_admin`

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `targetPropertyCompanyId` | string | 是 | 目标物业公司ID |
| `targetCommunityId` | string | 是 | 目标小区ID |
| `targetBuilding` | string | 是 | 楼栋号 |
| `targetUnit` | string | 否 | 单元号 |
| `targetRoom` | string | 是 | 房间号 |
| `migrateFamily` | boolean | 否 | 是否迁移整个家庭（默认 true） |
| `pointsHandling` | string | 否 | 积分处理方式：`keep`/`clear`/`convert` |
| `coinsHandling` | string | 否 | 物业币处理方式：`keep`/`clear`/`convert` |
| `reason` | string | 是 | 迁移原因 |

**响应**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "residentId": "res_001",
    "fromPropertyCompanyId": "pc_a1b2c3d4",
    "toPropertyCompanyId": "pc_e5f6g7h8",
    "migratedFamily": true,
    "familyMembersCount": 4,
    "pointsHandled": "keep",
    "coinsHandled": "convert",
    "migratedAt": "2026-07-01T10:00:00.000Z"
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

> 迁移操作需异步处理，返回后需轮询迁移结果或通过 WebSocket 接收完成通知。

---

## 8. 板块负责人管理（property_admin）

### 8.1 GET `/api/v1/admin/sector-leaders` - 板块负责人列表
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Query）**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `sector` | string | 否 | 板块筛选（`elderly_care`/`medical`/`education`/`housekeeping`等） |
| `status` | string | 否 | `active`/`inactive` |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "sl_001",
        "residentId": "res_001",
        "name": "赵六",
        "phone": "13800009999",
        "sector": "elderly_care",
        "sectorName": "养老板块",
        "shareRate": 0.10,
        "individualLeaderCount": 12,
        "totalEarnings": 12500.00,
        "status": "active",
        "appointedAt": "2026-03-10T10:00:00.000Z"
      }
    ],
    "pagination": { "page": 1, "pageSize": 20, "total": 8, "totalPages": 1 }
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

---

### 8.2 POST `/api/v1/admin/sector-leaders` - 任命板块负责人
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `residentId` | string | 是 | 业主ID |
| `sector` | string | 是 | 板块：`elderly_care`/`medical`/`education`/`housekeeping`/`culture`/`sports` |
| `shareRate` | number | 是 | 分成比例（默认 0.10） |
| `startDate` | string | 是 | 任职开始日期 |
| `endDate` | string | 否 | 任职结束日期 |
| `remark` | string | 否 | 备注 |

**响应**：返回新任命的板块负责人（结构同 8.1 单项）。

---

### 8.3 PUT `/api/v1/admin/sector-leaders/:id` - 更新板块负责人
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Body）**：同 8.2，字段可选。

**响应**：返回更新后的板块负责人。

---

### 8.4 DELETE `/api/v1/admin/sector-leaders/:id` - 撤销板块负责人
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Query）**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `reason` | string | 是 | 撤销原因 |

**响应**：返回撤销结果。

---

### 8.5 GET `/api/v1/admin/sector-leaders/:id/stats` - 板块负责人收益统计
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`、`sector_leader`（仅本人）

**请求参数（Query）**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `startDate` | string | 否 | 起始日期 |
| `endDate` | string | 否 | 结束日期 |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "sectorLeaderId": "sl_001",
    "name": "赵六",
    "sector": "elderly_care",
    "summary": {
      "totalEarnings": 12500.00,
      "monthlyEarnings": 2100.00,
      "individualLeaderCount": 12,
      "activeIndividualLeaders": 10,
      "totalOrderCount": 560,
      "totalOrderAmount": 28000.00
    },
    "monthlyTrend": [
      { "month": "2026-06", "earnings": 2100.00, "orderCount": 95 },
      { "month": "2026-05", "earnings": 1850.00, "orderCount": 82 }
    ],
    "individualLeaders": [
      { "id": "il_001", "name": "钢琴老师A", "earnings": 800.00, "orderCount": 32 }
    ]
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

---

## 9. 个体负责人管理（property_admin / sector_leader）

### 9.1 GET `/api/v1/admin/individual-leaders` - 个体负责人列表
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`、`sector_leader`（仅本人板块下）

**请求参数（Query）**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `sectorLeaderId` | string | 否 | 所属板块负责人筛选 |
| `keyword` | string | 否 | 姓名/特长搜索 |
| `status` | string | 否 | `active`/`inactive` |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "il_001",
        "residentId": "res_002",
        "name": "钢琴老师A",
        "phone": "13800008888",
        "specialty": "钢琴教学",
        "sectorLeaderId": "sl_001",
        "sectorLeaderName": "赵六",
        "sector": "education",
        "shareRate": 0.05,
        "totalEarnings": 8000.00,
        "status": "active",
        "appointedAt": "2026-03-15T10:00:00.000Z"
      }
    ],
    "pagination": { "page": 1, "pageSize": 20, "total": 56, "totalPages": 3 }
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

---

### 9.2 POST `/api/v1/admin/individual-leaders` - 任命个体负责人
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`、`sector_leader`（仅本人板块下）

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `residentId` | string | 是 | 业主ID |
| `specialty` | string | 是 | 特长（如"钢琴老师"、"篮球教练"） |
| `sectorLeaderId` | string | 是 | 所属板块负责人ID |
| `shareRate` | number | 是 | 分成比例（默认 0.05） |
| `startDate` | string | 是 | 任职开始日期 |
| `endDate` | string | 否 | 任职结束日期 |
| `remark` | string | 否 | 备注 |

**响应**：返回新任命的个体负责人（结构同 9.1 单项）。

---

### 9.3 PUT `/api/v1/admin/individual-leaders/:id` - 更新
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`、`sector_leader`（仅本人板块下）

**请求参数（Body）**：同 9.2，字段可选。

**响应**：返回更新后的个体负责人。

---

### 9.4 DELETE `/api/v1/admin/individual-leaders/:id` - 撤销
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`、`sector_leader`（仅本人板块下）

**请求参数（Query）**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `reason` | string | 是 | 撤销原因 |

**响应**：返回撤销结果。

---

## 10. 统筹管理（property_admin）

### 10.1 GET `/api/v1/admin/coordinators` - 统筹列表
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Query）**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `keyword` | string | 否 | 姓名/手机号搜索 |
| `status` | string | 否 | `active`/`inactive` |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "co_001",
        "residentId": "res_003",
        "name": "钱七",
        "phone": "13800007777",
        "shareRate": 0.70,
        "totalEarnings": 89000.00,
        "sectorLeaderCount": 3,
        "individualLeaderCount": 28,
        "status": "active",
        "appointedAt": "2026-02-20T10:00:00.000Z"
      }
    ],
    "pagination": { "page": 1, "pageSize": 20, "total": 5, "totalPages": 1 }
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

---

### 10.2 POST `/api/v1/admin/coordinators` - 任命统筹
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `residentId` | string | 是 | 业主ID |
| `shareRate` | number | 是 | 分成比例（默认 0.70） |
| `startDate` | string | 是 | 任职开始日期 |
| `endDate` | string | 否 | 任职结束日期 |
| `remark` | string | 否 | 备注 |

**响应**：返回新任命的统筹（结构同 10.1 单项）。

---

### 10.3 PUT `/api/v1/admin/coordinators/:id` - 更新统筹信息
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Body）**：同 10.2，字段可选。

**响应**：返回更新后的统筹。

---

### 10.4 DELETE `/api/v1/admin/coordinators/:id` - 撤销统筹
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Query）**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `reason` | string | 是 | 撤销原因 |

**响应**：返回撤销结果。

---

### 10.5 GET `/api/v1/admin/coordinators/:id/stats` - 统筹收益统计
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Query）**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `startDate` | string | 否 | 起始日期 |
| `endDate` | string | 否 | 结束日期 |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "coordinatorId": "co_001",
    "name": "钱七",
    "summary": {
      "totalEarnings": 89000.00,
      "monthlyEarnings": 12500.00,
      "sectorLeaderCount": 3,
      "individualLeaderCount": 28,
      "totalOrderCount": 2850,
      "totalOrderAmount": 142500.00
    },
    "monthlyTrend": [
      { "month": "2026-06", "earnings": 12500.00, "orderCount": 480 },
      { "month": "2026-05", "earnings": 11200.00, "orderCount": 420 }
    ],
    "sectorLeaders": [
      {
        "id": "sl_001",
        "name": "赵六",
        "sector": "elderly_care",
        "earnings": 4200.00,
        "individualLeaderCount": 12
      }
    ]
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

---

## 11. 积分池管理（property_admin）

### 11.1 GET `/api/v1/admin/point-pools` - 积分池余额（按物业公司）
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "propertyCompanyId": "pc_a1b2c3d4",
    "propertyCompanyName": "万达物业管理有限公司",
    "balance": 3858000,
    "equivalentAmount": 38580.00,
    "lastMonthInflow": 125000,
    "lastMonthOutflow": 28000,
    "lastMonthNetInflow": 97000,
    "updatedAt": "2026-06-30T23:59:59.000Z"
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

---

### 11.2 GET `/api/v1/admin/point-pools/records` - 积分池进出记录
> 差额进池、物业币过期清零进池等

**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Query）**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `page` | number | 否 | 当前页 |
| `pageSize` | number | 否 | 每页条数 |
| `type` | string | 否 | `inflow`/`outflow` |
| `source` | string | 否 | 来源：`consumption_diff`/`coin_expired`/`pool_spend`/`manual_adjust` |
| `startDate` | string | 否 | 起始日期 |
| `endDate` | string | 否 | 结束日期 |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "pp_001",
        "type": "inflow",
        "source": "consumption_diff",
        "amount": 1200,
        "description": "消费差额自动进池（订单 ord_xxx）",
        "relatedOrderId": "ord_xxx001",
        "operatorId": null,
        "createdAt": "2026-06-30T15:30:00.000Z"
      },
      {
        "id": "pp_002",
        "type": "outflow",
        "source": "pool_spend",
        "amount": 5000,
        "description": "小区门禁系统修缮",
        "relatedVoteId": "vote_001",
        "operatorId": "res_admin001",
        "operatorName": "张三",
        "createdAt": "2026-06-28T10:00:00.000Z"
      }
    ],
    "pagination": { "page": 1, "pageSize": 20, "total": 568, "totalPages": 29 }
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

---

### 11.3 POST `/api/v1/admin/point-pools/spend` - 动用积分池资金
> 小区修缮等，需业主投票/签字

**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `amount` | number | 是 | 动用金额（积分数量） |
| `purpose` | string | 是 | 用途说明 |
| `category` | string | 是 | 分类：`repair`/`facility`/`activity`/`other` |
| `voteId` | string | 否 | 关联投票ID（如已开启业主投票） |
| `attachments` | array | 否 | 附件URL数组（合同/报价单等） |
| `signatures` | array | 否 | 业主签字记录数组 |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "pp_002",
    "type": "outflow",
    "amount": 5000,
    "purpose": "小区门禁系统修缮",
    "category": "repair",
    "voteId": "vote_001",
    "voteStatus": "passed",
    "status": "completed",
    "operatorId": "res_admin001",
    "createdAt": "2026-07-01T10:00:00.000Z"
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

> 若配置 `pointPoolSpendRequireVote = true`，未提供 `voteId` 或投票未通过将返回 `50001 point_pool_vote_required`。

---

## 12. 物业币管控（property_admin）

### 12.1 GET `/api/v1/admin/property-coins/config` - 物业币显示控制
> 全局显示/隐藏

**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "propertyCompanyId": "pc_a1b2c3d4",
    "showPropertyCoin": true,
    "displayScenes": {
      "appHome": true,
      "wallet": true,
      "payment": true,
      "merchantDetail": false
    },
    "updatedAt": "2026-06-30T12:00:00.000Z"
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

---

### 12.2 PUT `/api/v1/admin/property-coins/config` - 更新物业币显示控制
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `showPropertyCoin` | boolean | 否 | 全局开关 |
| `displayScenes` | object | 否 | 分场景开关 |

**响应**：返回更新后的配置（结构同 12.1）。

---

### 12.3 GET `/api/v1/admin/property-coins/frozen` - 冻结记录列表
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Query）**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `residentId` | string | 否 | 按用户筛选 |
| `status` | string | 否 | `frozen`/`unfrozen` |
| `startDate` | string | 否 | 起始日期 |
| `endDate` | string | 否 | 结束日期 |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "fr_001",
        "residentId": "res_001",
        "residentName": "王五",
        "frozenAmount": 500.00,
        "reason": "违规操作",
        "status": "frozen",
        "operatorId": "res_admin001",
        "operatorName": "张三",
        "frozenAt": "2026-06-25T10:00:00.000Z",
        "unfrozenAt": null
      }
    ],
    "pagination": { "page": 1, "pageSize": 20, "total": 12, "totalPages": 1 }
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

---

### 12.4 POST `/api/v1/admin/property-coins/:residentId/freeze` - 冻结用户物业币
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `amount` | number | 否 | 冻结金额（不传则全部冻结） |
| `reason` | string | 是 | 冻结原因 |

**响应**：返回冻结记录（结构同 12.3 单项）。

---

### 12.5 POST `/api/v1/admin/property-coins/:residentId/unfreeze` - 解冻用户物业币
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `frozenRecordId` | string | 否 | 指定解冻记录ID（不传则全部解冻） |
| `reason` | string | 是 | 解冻原因 |

**响应**：返回解冻结果。

---

### 12.6 GET `/api/v1/admin/property-coins/expiring` - 即将过期物业币列表
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Query）**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `daysBeforeExpiry` | number | 否 | 距过期天数（默认30） |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "residentId": "res_001",
        "residentName": "王五",
        "phone": "13800001234",
        "expiringAmount": 156.80,
        "expiryDate": "2026-07-30",
        "daysRemaining": 29
      }
    ],
    "pagination": { "page": 1, "pageSize": 20, "total": 86, "totalPages": 5 }
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

---

### 12.7 POST `/api/v1/admin/property-coins/expire-execute` - 执行过期清零
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `expiryDate` | string | 是 | 过期日期 `YYYY-MM-DD` |
| `dryRun` | boolean | 否 | 是否试运行（默认 false） |
| `residentIds` | array | 否 | 指定用户ID（不传则全部） |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "expiryDate": "2026-06-30",
    "dryRun": false,
    "processedCount": 56,
    "totalExpiredAmount": 4280.50,
    "clearedToPool": 4280.50,
    "executedAt": "2026-07-01T03:00:00.000Z"
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

> 过期清零的物业币金额自动转入积分池（`source = coin_expired`）。

---

## 13. 物业费管理（property_admin）

### 13.1 GET `/api/v1/admin/property-fees` - 物业费列表
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Query）**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `keyword` | string | 否 | 户主姓名/房号搜索 |
| `feeType` | string | 否 | `property`/`parking`/`utility` |
| `status` | string | 否 | `pending`/`paid`/`overdue` |
| `communityId` | string | 否 | 小区筛选 |
| `period` | string | 否 | 账期 `YYYY-MM` |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "pf_001",
        "residentId": "res_001",
        "residentName": "王五",
        "communityName": "万达花园",
        "building": "3栋",
        "room": "502",
        "feeType": "property",
        "feeTypeName": "物业费",
        "period": "2026-07",
        "amount": 280.00,
        "paidAmount": 0.00,
        "status": "pending",
        "dueDate": "2026-07-15",
        "autoDeductEnabled": true,
        "createdAt": "2026-07-01T00:00:00.000Z"
      }
    ],
    "pagination": { "page": 1, "pageSize": 20, "total": 4520, "totalPages": 226 }
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

---

### 13.2 POST `/api/v1/admin/property-fees/batch-create` - 批量创建账单
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `ruleId` | string | 是 | 物业费规则ID |
| `period` | string | 是 | 账期 `YYYY-MM` |
| `communityId` | string | 否 | 小区ID（不传则全公司） |
| `buildingFilter` | array | 否 | 指定楼栋列表 |
| `residentIds` | array | 否 | 指定用户ID列表 |
| `dueDate` | string | 是 | 截止日期 |
| `dryRun` | boolean | 否 | 是否试运行（默认 false） |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "ruleId": "pfr_001",
    "period": "2026-07",
    "dryRun": false,
    "createdCount": 720,
    "totalAmount": 201600.00,
    "executedAt": "2026-07-01T10:00:00.000Z"
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

---

### 13.3 GET `/api/v1/admin/property-fees/overdue` - 逾期账单列表
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Query）**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `overdueDaysMin` | number | 否 | 最小逾期天数 |
| `overdueDaysMax` | number | 否 | 最大逾期天数 |

**响应**：结构同 13.1，每条记录附加 `overdueDays`、`lateFee` 字段。

---

### 13.4 POST `/api/v1/admin/property-fees/auto-deduct-execute` - 执行自动扣款
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `period` | string | 是 | 账期 `YYYY-MM` |
| `deductMethod` | string | 否 | `property_coin`/`points`（默认 `property_coin`） |
| `residentIds` | array | 否 | 指定用户ID |
| `dryRun` | boolean | 否 | 是否试运行 |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "period": "2026-07",
    "deductMethod": "property_coin",
    "dryRun": false,
    "processedCount": 580,
    "successCount": 528,
    "failedCount": 52,
    "totalDeducted": 147840.00,
    "failedRecords": [
      { "residentId": "res_002", "reason": "insufficient_balance", "amount": 280.00 }
    ],
    "executedAt": "2026-07-01T03:00:00.000Z"
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

---

### 13.5 GET `/api/v1/admin/property-fee-rules` - 物业费规则列表
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "pfr_001",
        "name": "万达花园物业费",
        "feeType": "property",
        "calcMethod": "by_area",
        "unitPrice": 2.50,
        "periodType": "monthly",
        "communityId": "com_x1y2z3",
        "communityName": "万达花园",
        "lateFeeRate": 0.0005,
        "lateFeeGraceDays": 15,
        "autoDeductEnabled": true,
        "status": "active",
        "createdAt": "2026-01-15T09:30:00.000Z"
      }
    ],
    "pagination": { "page": 1, "pageSize": 20, "total": 8, "totalPages": 1 }
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

---

### 13.6 POST `/api/v1/admin/property-fee-rules` - 创建物业费规则
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `name` | string | 是 | 规则名称 |
| `feeType` | string | 是 | `property`/`parking`/`utility` |
| `calcMethod` | string | 是 | `fixed`/`by_area`/`by_usage` |
| `unitPrice` | number | 否 | 单价（按面积/用量时填） |
| `fixedAmount` | number | 否 | 固定金额（fixed 时填） |
| `periodType` | string | 是 | `monthly`/`quarterly`/`yearly` |
| `communityId` | string | 是 | 小区ID |
| `lateFeeRate` | number | 否 | 滞纳金日费率（默认 0.0005） |
| `lateFeeGraceDays` | number | 否 | 宽限期天数（默认 15） |
| `autoDeductEnabled` | boolean | 否 | 是否允许自动扣款 |

**响应**：返回新创建的规则（结构同 13.5 单项）。

---

### 13.7 PUT `/api/v1/admin/property-fee-rules/:id` - 更新物业费规则
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Body）**：同 13.6，字段可选。

**响应**：返回更新后的规则。

---

## 14. 报表与统计（property_admin / platform_admin）

### 14.1 GET `/api/v1/admin/reports/overview` - 总览
> 商家数/住户数/消费总额/积分池/物业币流通量/物业费收缴率

**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Query）**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `propertyCompanyId` | string | 否 | 仅 platform_admin 可指定，不传则汇总全平台 |
| `startDate` | string | 否 | 起始日期 |
| `endDate` | string | 否 | 结束日期 |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "scope": {
      "propertyCompanyId": "pc_a1b2c3d4",
      "propertyCompanyName": "万达物业管理有限公司"
    },
    "summary": {
      "merchantCount": 128,
      "residentCount": 4520,
      "familyCount": 1380,
      "totalConsumptionAmount": 1289500.00,
      "totalOrderCount": 28560,
      "pointPoolBalance": 3858000,
      "propertyCoinCirculation": 528600.00,
      "propertyFeeCollectionRate": 0.8765,
      "propertyFeeTotalAmount": 504000.00,
      "propertyFeePaidAmount": 441636.00,
      "propertyFeeOverdueAmount": 62364.00
    },
    "comparison": {
      "consumptionGrowthRate": 0.125,
      "orderGrowthRate": 0.086,
      "residentGrowthRate": 0.032
    }
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

---

### 14.2 GET `/api/v1/admin/reports/consumption` - 消费统计
> 按商家/按板块/按时间段

**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Query）**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `dimension` | string | 是 | `merchant`/`sector`/`time` |
| `startDate` | string | 否 | 起始日期 |
| `endDate` | string | 否 | 结束日期 |
| `merchantId` | string | 否 | 指定商家 |
| `sector` | string | 否 | 指定板块 |
| `groupBy` | string | 否 | `day`/`week`/`month`（dimension=time 时必填） |

**响应示例（按商家）**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "dimension": "merchant",
    "list": [
      {
        "merchantId": "mch_001",
        "merchantName": "万达超市",
        "category": "超市便利",
        "orderCount": 1280,
        "totalAmount": 56800.00,
        "avgOrderAmount": 44.38,
        "commissionAmount": 5680.00
      }
    ],
    "pagination": { "page": 1, "pageSize": 20, "total": 128, "totalPages": 7 }
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

---

### 14.3 GET `/api/v1/admin/reports/distribution` - 分成统计
> 物业/统筹/板块/个体各方收入

**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Query）**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `startDate` | string | 否 | 起始日期 |
| `endDate` | string | 否 | 结束日期 |
| `dimension` | string | 否 | `summary`/`detail`（默认 summary） |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "summary": {
      "totalDistributableAmount": 128950.00,
      "propertyAmount": 38685.00,
      "coordinatorAmount": 90265.00,
      "sectorLeaderAmount": 9026.50,
      "individualLeaderAmount": 4513.25
    },
    "byProperty": [
      { "propertyCompanyId": "pc_a1b2c3d4", "propertyName": "万达物业", "amount": 38685.00 }
    ],
    "byCoordinator": [
      { "coordinatorId": "co_001", "name": "钱七", "amount": 90265.00 }
    ],
    "bySector": [
      { "sector": "elderly_care", "sectorName": "养老板块", "amount": 3500.00 }
    ]
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

---

### 14.4 GET `/api/v1/admin/reports/points` - 积分统计
> 发放/消费/过期/池余额

**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Query）**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `startDate` | string | 否 | 起始日期 |
| `endDate` | string | 否 | 结束日期 |
| `groupBy` | string | 否 | `day`/`week`/`month` |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "summary": {
      "totalIssued": 12895000,
      "totalConsumed": 9036500,
      "totalExpired": 428000,
      "currentBalance": 3858000,
      "poolBalance": 3858000,
      "equivalentAmount": 38580.00
    },
    "trend": [
      {
        "date": "2026-06",
        "issued": 2150000,
        "consumed": 1580000,
        "expired": 428000
      }
    ]
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

---

### 14.5 GET `/api/v1/admin/reports/property-fee` - 物业费统计
> 应缴/已缴/欠费率

**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Query）**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `period` | string | 否 | 账期 `YYYY-MM` |
| `communityId` | string | 否 | 小区筛选 |
| `feeType` | string | 否 | 费用类型 |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "summary": {
      "totalAmount": 504000.00,
      "paidAmount": 441636.00,
      "overdueAmount": 62364.00,
      "collectionRate": 0.8765,
      "overdueRate": 0.1235,
      "totalBills": 1800,
      "paidBills": 1578,
      "overdueBills": 222
    },
    "byCommunity": [
      {
        "communityId": "com_x1y2z3",
        "communityName": "万达花园",
        "totalAmount": 201600.00,
        "paidAmount": 185000.00,
        "collectionRate": 0.9176
      }
    ],
    "byFeeType": [
      { "feeType": "property", "totalAmount": 168000.00, "paidAmount": 152000.00 },
      { "feeType": "parking", "totalAmount": 24000.00, "paidAmount": 24000.00 }
    ]
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

---

### 14.6 GET `/api/v1/admin/reports/export` - 导出报表
> CSV/Excel，流式返回

**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Query）**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `reportType` | string | 是 | `overview`/`consumption`/`distribution`/`points`/`property_fee`/`merchants`/`residents` |
| `format` | string | 是 | `csv`/`xlsx` |
| `startDate` | string | 否 | 起始日期 |
| `endDate` | string | 否 | 结束日期 |
| `propertyCompanyId` | string | 否 | 仅 platform_admin 可指定 |
| `filter` | object | 否 | 其他过滤条件（JSON字符串） |

**响应**：
```
Content-Type: text/csv; charset=utf-8
Content-Disposition: attachment; filename="consumption_2026-06.csv"
Transfer-Encoding: chunked

<CSV/Excel二进制流>
```

> 大数据量采用流式返回，避免内存溢出。单次导出上限 100 万行，超出请缩小时间范围。

---

## 15. 公告管理（property_admin）

### 15.1 POST `/api/v1/admin/announcements` - 发布公告
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `title` | string | 是 | 公告标题 |
| `content` | string | 是 | 公告内容（Markdown/HTML） |
| `type` | string | 是 | `property_announcement`/`community_announcement`/`platform_announcement`/`system_announcement`/`merchant_announcement`/`activity_announcement` |
| `pushDimensions` | array | 否 | 推送维度：`role`/`building`/`community` |
| `targetRoles` | array | 否 | 目标角色 |
| `targetBuildings` | array | 否 | 目标楼栋 |
| `targetCommunityIds` | array | 否 | 目标小区 |
| `collectInfo` | object | 否 | 信息收集配置 |
| `collectInfo.enabled` | boolean | 否 | 是否启用信息收集 |
| `collectInfo.fields` | array | 否 | 收集字段定义 |
| `collectInfo.deadline` | string | 否 | 收集截止时间 |
| `publishAt` | string | 否 | 定时发布时间（不传则立即发布） |
| `expireAt` | string | 否 | 过期时间 |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "ann_001",
    "title": "关于小区门禁系统升级的通知",
    "type": "property_announcement",
    "status": "published",
    "publishAt": "2026-07-01T10:00:00.000Z",
    "targetCount": 4520,
    "createdAt": "2026-07-01T10:00:00.000Z"
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

---

### 15.2 GET `/api/v1/admin/announcements` - 公告列表
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Query）**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `type` | string | 否 | 公告类型 |
| `status` | string | 否 | `draft`/`published`/`expired` |
| `startDate` | string | 否 | 起始日期 |
| `endDate` | string | 否 | 结束日期 |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "ann_001",
        "title": "关于小区门禁系统升级的通知",
        "type": "property_announcement",
        "status": "published",
        "publishAt": "2026-07-01T10:00:00.000Z",
        "targetCount": 4520,
        "readCount": 3250,
        "collectInfoEnabled": true,
        "collectResponseCount": 1820,
        "createdAt": "2026-07-01T10:00:00.000Z"
      }
    ],
    "pagination": { "page": 1, "pageSize": 20, "total": 86, "totalPages": 5 }
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

---

### 15.3 PUT `/api/v1/admin/announcements/:id` - 更新公告
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Body）**：同 15.1，字段可选（已发布公告仅可更新 `expireAt`）。

**响应**：返回更新后的公告。

---

### 15.4 DELETE `/api/v1/admin/announcements/:id` - 删除公告
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**响应**：返回删除结果。

---

### 15.5 GET `/api/v1/admin/announcements/:id/collects` - 公告信息收集结果
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Query）**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `page` | number | 否 | 当前页 |
| `pageSize` | number | 否 | 每页条数 |
| `export` | boolean | 否 | 是否导出（true 则返回 CSV 流） |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "announcementId": "ann_001",
    "announcementTitle": "关于小区门禁系统升级的通知",
    "totalResponses": 1820,
    "responseRate": 0.4027,
    "fields": [
      { "key": "building", "name": "楼栋号", "type": "text" },
      { "key": "consent", "name": "是否同意", "type": "boolean" },
      { "key": "suggestion", "name": "建议", "type": "textarea" }
    ],
    "list": [
      {
        "id": "col_001",
        "residentId": "res_001",
        "residentName": "王五",
        "phone": "13800001234",
        "building": "3栋",
        "responses": {
          "building": "3栋",
          "consent": true,
          "suggestion": "同意升级，建议加快进度"
        },
        "submittedAt": "2026-07-01T11:00:00.000Z"
      }
    ],
    "pagination": { "page": 1, "pageSize": 20, "total": 1820, "totalPages": 91 }
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

---

## 16. 商家积分购买审核（property_admin）

### 16.1 GET `/api/v1/admin/merchant-point-purchases` - 商家购买积分记录列表
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Query）**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `merchantId` | string | 否 | 商家ID |
| `status` | string | 否 | `pending`/`approved`/`rejected` |
| `startDate` | string | 否 | 起始日期 |
| `endDate` | string | 否 | 结束日期 |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "mpp_001",
        "merchantId": "mch_001",
        "merchantName": "万达超市",
        "pointAmount": 100000,
        "paymentAmount": 1000.00,
        "paymentMethod": "wechat",
        "paymentProof": "https://cdn.example.com/payment/001.jpg",
        "status": "pending",
        "remark": "用于618促销活动",
        "createdAt": "2026-06-28T15:00:00.000Z"
      }
    ],
    "pagination": { "page": 1, "pageSize": 20, "total": 28, "totalPages": 2 }
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

---

### 16.2 POST `/api/v1/admin/merchant-point-purchases/:id/approve` - 审核通过
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `remark` | string | 否 | 审核备注 |
| `actualPointAmount` | number | 否 | 实际发放积分（不传则按申请数量） |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "mpp_001",
    "status": "approved",
    "pointAmount": 100000,
    "actualPointAmount": 100000,
    "merchantPointBalance": 250000,
    "auditedAt": "2026-07-01T10:00:00.000Z",
    "auditedBy": "res_admin001"
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

---

### 16.3 POST `/api/v1/admin/merchant-point-purchases/:id/reject` - 审核拒绝
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `rejectReason` | string | 是 | 拒绝原因 |

**响应**：返回审核拒绝结果（结构同 16.2，`status = rejected`）。

---

### 16.4 GET `/api/v1/admin/merchant-withdrawals` - 商家提现申请列表
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Query）**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `merchantId` | string | 否 | 商家ID |
| `status` | string | 否 | `pending`/`approved`/`rejected`/`completed` |
| `startDate` | string | 否 | 起始日期 |
| `endDate` | string | 否 | 结束日期 |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "wd_001",
        "merchantId": "mch_001",
        "merchantName": "万达超市",
        "withdrawalAmount": 5000.00,
        "feeAmount": 30.00,
        "actualAmount": 4970.00,
        "bankCard": "**** **** **** 1234",
        "accountName": "万达超市有限公司",
        "status": "pending",
        "createdAt": "2026-06-29T10:00:00.000Z"
      }
    ],
    "pagination": { "page": 1, "pageSize": 20, "total": 12, "totalPages": 1 }
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

---

### 16.5 POST `/api/v1/admin/merchant-withdrawals/:id/approve` - 审核通过
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `remark` | string | 否 | 审核备注 |
| `transferProof` | string | 否 | 转账凭证URL |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "wd_001",
    "status": "approved",
    "transferProof": "https://cdn.example.com/transfer/001.jpg",
    "auditedAt": "2026-07-01T10:00:00.000Z",
    "auditedBy": "res_admin001"
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

---

### 16.6 POST `/api/v1/admin/merchant-withdrawals/:id/reject` - 审核拒绝
**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `rejectReason` | string | 是 | 拒绝原因 |

**响应**：返回审核拒绝结果（结构同 16.5，`status = rejected`）。

> 拒绝后冻结的提现金额自动返还到商家余额。

---

## 17. 接口权限矩阵汇总

| 模块 | 接口前缀 | 允许角色 |
|------|----------|----------|
| 物业公司管理 | `/property-companies` | `platform_admin`（部分详情/配置允许 `property_admin`） |
| 物业公司配置 | `/property-companies/:id/configs` | `platform_admin`、`property_admin` |
| 平台级商家库 | `/platform-merchants` | `platform_admin` |
| 商家审核与管理 | `/merchants` | `platform_admin`、`property_admin` |
| 权限管理 | `/permissions`、`/role-presets`、`/accounts`、`/permission-logs` | `platform_admin`、`property_admin` |
| 用户管理 | `/residents`、`/families` | `platform_admin`、`property_admin` |
| 板块负责人 | `/sector-leaders` | `platform_admin`、`property_admin` |
| 个体负责人 | `/individual-leaders` | `platform_admin`、`property_admin`、`sector_leader` |
| 统筹管理 | `/coordinators` | `platform_admin`、`property_admin` |
| 积分池 | `/point-pools` | `platform_admin`、`property_admin` |
| 物业币管控 | `/property-coins` | `platform_admin`、`property_admin` |
| 物业费 | `/property-fees`、`/property-fee-rules` | `platform_admin`、`property_admin` |
| 报表统计 | `/reports` | `platform_admin`、`property_admin` |
| 公告管理 | `/announcements` | `platform_admin`、`property_admin` |
| 商家积分购买审核 | `/merchant-point-purchases`、`/merchant-withdrawals` | `platform_admin`、`property_admin` |

---

## 18. 枚举值定义

### 18.1 物业公司状态 (PropertyCompanyStatus)
```typescript
ACTIVE: 'active'        // 启用
INACTIVE: 'inactive'    // 停用
```

### 18.2 商家分级 (MerchantLevel)
```typescript
OFFICIAL_CERTIFIED: 'official_certified'  // 官方认证（仅 platform_admin）
PROPERTY_CERTIFIED: 'property_certified'  // 物业认证
NORMAL: 'normal'                          // 普通
```

### 18.3 商家审核状态 (AuditStatus)
```typescript
PENDING_AUDIT: 'pending_audit'  // 待审核
APPROVED: 'approved'            // 已通过
REJECTED: 'rejected'            // 已拒绝
```

### 18.4 商家状态 (MerchantStatus)
```typescript
ACTIVE: 'active'    // 正常
FROZEN: 'frozen'    // 冻结
```

### 18.5 板块类型 (SectorType)
```typescript
ELDERLY_CARE: 'elderly_care'    // 养老板块
MEDICAL: 'medical'              // 医疗板块
EDUCATION: 'education'          // 教育板块
HOUSEKEEPING: 'housekeeping'    // 家政板块
CULTURE: 'culture'              // 文化板块
SPORTS: 'sports'                // 体育板块
```

### 18.6 用户状态 (ResidentStatus)
```typescript
ACTIVE: 'active'      // 正常
FROZEN: 'frozen'      // 冻结
DISABLED: 'disabled'  // 禁用
```

### 18.7 物业费类型 (FeeType)
```typescript
PROPERTY: 'property'    // 物业费
PARKING: 'parking'      // 停车费
UTILITY: 'utility'      // 水电费
```

### 18.8 物业费计算方式 (CalcMethod)
```typescript
FIXED: 'fixed'          // 固定金额
BY_AREA: 'by_area'      // 按面积
BY_USAGE: 'by_usage'    // 按用量
```

### 18.9 物业费账单状态 (FeeStatus)
```typescript
PENDING: 'pending'    // 待支付
PAID: 'paid'          // 已支付
OVERDUE: 'overdue'    // 已逾期
```

### 18.10 积分池记录类型 (PointPoolRecordType)
```typescript
INFLOW: 'inflow'      // 进池
OUTFLOW: 'outflow'    // 出池
```

### 18.11 积分池记录来源 (PointPoolSource)
```typescript
CONSUMPTION_DIFF: 'consumption_diff'  // 消费差额进池
COIN_EXPIRED: 'coin_expired'          // 物业币过期清零进池
POOL_SPEND: 'pool_spend'              // 动用积分池
MANUAL_ADJUST: 'manual_adjust'        // 手动调整
```

### 18.12 公告类型 (AnnouncementType)
```typescript
PROPERTY: 'property_announcement'      // 物业公告
COMMUNITY: 'community_announcement'    // 社区公告
PLATFORM: 'platform_announcement'      // 统筹/官方推荐
SYSTEM: 'system_announcement'          // 系统公告
MERCHANT: 'merchant_announcement'      // 商家公告
ACTIVITY: 'activity_announcement'      // 活动组公告
```

### 18.13 公告推送维度 (PushDimension)
```typescript
ROLE: 'role'            // 按角色
BUILDING: 'building'    // 按楼栋
COMMUNITY: 'community'  // 按小区
```

### 18.14 提现状态 (WithdrawalStatus)
```typescript
PENDING: 'pending'        // 待审核
APPROVED: 'approved'      // 审核通过
REJECTED: 'rejected'      // 审核拒绝
COMPLETED: 'completed'    // 已完成
```

---

## 19. 通用错误码

| code | message | 说明 |
|------|---------|------|
| 0 | success | 成功 |
| 10001 | 参数校验失败 | 字段级错误附 errors |
| 10002 | 未授权访问 | 缺少 Authorization |
| 10003 | Token 已过期 | 需刷新 |
| 10004 | 权限不足 | 角色不允许 |
| 10005 | 资源不存在 | 404 |
| 10006 | 操作过于频繁 | 限流 |
| 20001 | 物业公司不存在 | — |
| 20002 | 物业公司已停用 | — |
| 20003 | 跨物业公司访问被拒 | `property_admin` 越权 |
| 30001 | 商家不存在 | — |
| 30002 | 商家未挂接到本公司 | — |
| 30003 | 商家已审核 | 重复审核 |
| 40001 | 用户不存在 | — |
| 40002 | 家庭成员超过上限 | 上限 15 人 |
| 40003 | 用户已禁用 | — |
| 50001 | 积分池动用需投票通过 | 配置开启投票校验 |
| 50002 | 物业币余额不足 | — |
| 50003 | 积分余额不足 | — |
| 60001 | 物业费规则不存在 | — |
| 60002 | 公告不存在 | — |
| 60003 | 提现申请已处理 | 重复审核 |
| 70001 | 报表导出超过上限 | 超 100 万行 |

---

## 20. 开发注意事项

### 20.1 数据隔离实现要点
1. 自定义注解 `@PropertyScope` 标记需要数据隔离的接口
2. 拦截器统一注入当前账号的 `propertyCompanyId` 到查询条件
3. `platform_admin` 通过 Query 参数 `propertyCompanyId` 显式指定目标公司
4. 所有 SQL 查询必须包含 `property_company_id` 条件（除平台级表）

### 20.2 异步任务
- 批量创建账单、自动扣款、过期清零、用户迁移、报表导出 等耗时操作必须异步执行
- 接口立即返回 `taskId`，通过 `GET /api/v1/admin/tasks/:taskId` 查询进度
- 任务完成后通过 WebSocket 推送通知

### 20.3 操作日志
所有写操作（POST/PUT/PATCH/DELETE）必须记录操作日志：
- 操作人、操作时间、操作类型、目标资源、变更前后数据
- 敏感操作（权限变更、积分池动用、用户迁移）需记录完整审计链路

### 20.4 接口幂等性
- 所有 POST 接口支持 `X-Idempotency-Key` 请求头，避免重复提交
- 服务端缓存 24 小时内的幂等键，命中则直接返回首次结果

### 20.5 限流策略
- 单账号每分钟最多 600 次请求
- 导出接口单账号每分钟最多 5 次
- 批量操作接口单账号每分钟最多 3 次

### 20.6 字段命名约定
- 请求/响应字段统一使用 `camelCase`
- 数据库字段使用 `snake_case`，通过 JPA 自动映射
- 时间字段统一返回 ISO 8601 格式（`YYYY-MM-DDTHH:mm:ss.SSSZ`）
- 金额字段统一使用 `DECIMAL` 序列化为字符串或保留 2 位小数
- 积分字段统一使用整数

---

**文档版本**：v1.0
**最后更新**：2026-07-01
**适用项目**：物业积分服务系统 Web 后台
