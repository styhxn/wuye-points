# API接口设计 - 物业积分服务系统

## 1. API设计原则

### 1.1 URL设计规范
- RESTful风格，资源路径使用名词复数
- 小写字母+连字符（kebab-case）
- 版本号：`/api/v1/...`
- 子资源嵌套不超过两层

### 1.2 HTTP方法
| 方法 | 语义 | 使用场景 |
|------|------|----------|
| GET | 查询 | 获取列表/详情 |
| POST | 创建 | 新增记录 |
| PUT | 全量更新 | 替换整个资源 |
| PATCH | 部分更新 | 更新部分字段 |
| DELETE | 删除 | 删除记录 |

### 1.3 请求参数位置
| 位置 | 场景 |
|------|------|
| Query | 过滤、排序、分页 |
| Body | 创建/更新的数据 |
| Path | 资源标识符 |
| Header | 鉴权Token |

## 2. 核心API接口清单

### 2.1 认证模块 (Auth)

```
POST   /api/v1/auth/login                  # 微信小程序登录
POST   /api/v1/auth/refresh                 # 刷新Token
POST   /api/v1/auth/logout                  # 退出登录
GET    /api/v1/auth/me                      # 获取当前用户信息
```

### 2.2 业主模块 (Residents)

```
POST   /api/v1/residents                    # 注册业主
GET    /api/v1/residents/me                 # 获取个人信息
PUT    /api/v1/residents/me                 # 更新个人信息
GET    /api/v1/residents/:id                # 获取业主详情（管理员）
GET    /api/v1/residents                    # 业主列表（管理员）
PATCH  /api/v1/residents/:id/status         # 更新业主状态
POST   /api/v1/residents/bind-wechat        # 绑定微信
```

### 2.3 家庭模块 (Families)

```
POST   /api/v1/families                     # 创建家庭
GET    /api/v1/families/me                  # 获取家庭信息
PUT    /api/v1/families/me                  # 更新家庭信息
POST   /api/v1/families/me/members          # 添加家庭成员
DELETE /api/v1/families/me/members/:id      # 移除家庭成员
GET    /api/v1/families/me/members          # 家庭成员列表
```

### 2.4 商家模块 (Merchants)

```
POST   /api/v1/merchants                    # 商家入驻申请
GET    /api/v1/merchants                    # 商家列表
GET    /api/v1/merchants/:id                # 商家详情
PUT    /api/v1/merchants/:id                # 更新商家信息
PATCH  /api/v1/merchants/:id/status         # 审核商家
GET    /api/v1/merchants/:id/products       # 商家商品列表
POST   /api/v1/merchants/:id/products       # 添加商品
```

### 2.5 商品模块 (Products)

```
GET    /api/v1/products                     # 商品列表
GET    /api/v1/products/:id                 # 商品详情
POST   /api/v1/products                     # 创建商品
PUT    /api/v1/products/:id                 # 更新商品
DELETE /api/v1/products/:id                 # 删除商品
```

### 2.6 订单模块 (Orders)

```
POST   /api/v1/orders                       # 创建订单
GET    /api/v1/orders                       # 订单列表
GET    /api/v1/orders/:id                   # 订单详情
PATCH  /api/v1/orders/:id/status            # 更新订单状态
POST   /api/v1/orders/:id/pay               # 支付订单
POST   /api/v1/orders/:id/cancel            # 取消订单
GET    /api/v1/orders/my                    # 我的订单列表
```

### 2.7 积分模块 (Points)

```
GET    /api/v1/points/balance               # 获取积分余额
GET    /api/v1/points/records               # 积分记录列表
POST   /api/v1/points/earn                  # 获取积分（内部调用）
POST   /api/v1/points/spend                 # 消费积分（内部调用）
POST   /api/v1/points/transfer              # 积分转让
GET    /api/v1/points/family-balance        # 家庭积分余额
GET    /api/v1/point-rules                  # 积分规则列表
POST   /api/v1/point-rules                  # 创建积分规则
PUT    /api/v1/point-rules/:id              # 更新积分规则
DELETE /api/v1/point-rules/:id              # 删除积分规则
POST   /api/v1/points/redeem                # 积分兑换（100积分=1元）
GET    /api/v1/points/expiring              # 即将过期积分查询
POST   /api/v1/points/expire-execute        # 执行积分过期清理
GET    /api/v1/points/expire-records        # 积分过期记录
```

### 2.8 物业币模块 (PropertyCoins)

```
GET    /api/v1/property-coins/balance       # 获取物业币余额
GET    /api/v1/property-coins/records       # 物业币记录列表
POST   /api/v1/property-coins/earn          # 获取物业币（内部调用）
POST   /api/v1/property-coins/spend         # 消费物业币（内部调用）
POST   /api/v1/property-coins/recharge      # 充值物业币
GET    /api/v1/property-coins/family-balance # 家庭物业币余额
GET    /api/v1/property-coin-rules          # 物业币规则列表
POST   /api/v1/property-coin-rules          # 创建物业币规则
PUT    /api/v1/property-coin-rules/:id      # 更新物业币规则
```

### 2.9 物业费模块 (PropertyFees)

```
GET    /api/v1/property-fees                # 物业费列表
GET    /api/v1/property-fees/:id            # 物业费详情
POST   /api/v1/property-fees                # 创建物业费账单
POST   /api/v1/property-fees/:id/pay        # 缴纳物业费
POST   /api/v1/property-fees/auto-deduct    # 设置自动扣款
GET    /api/v1/property-fees/my             # 我的物业费
GET    /api/v1/property-fee-rules           # 物业费规则列表
POST   /api/v1/property-fee-rules           # 创建物业费规则
PUT    /api/v1/property-fee-rules/:id       # 更新物业费规则
POST   /api/v1/property-fees/batch-create   # 批量创建账单
POST   /api/v1/property-fees/auto-deduct-execute  # 执行自动扣款
```

### 2.10 公告模块 (Announcements)

```
POST   /api/v1/announcements                # 发布公告
GET    /api/v1/announcements                # 公告列表
GET    /api/v1/announcements/:id            # 公告详情
PUT    /api/v1/announcements/:id            # 更新公告
DELETE /api/v1/announcements/:id            # 删除公告
```

### 2.11 配送模块 (Deliveries)

```
GET    /api/v1/deliveries                   # 配送列表
GET    /api/v1/deliveries/:id               # 配送详情
POST   /api/v1/deliveries                   # 创建配送任务
POST   /api/v1/deliveries/:id/grab          # 快递员抢单
PATCH  /api/v1/deliveries/:id/status        # 更新配送状态
GET    /api/v1/deliveries/my                # 我的配送任务
GET    /api/v1/delivery-rules               # 配送规则列表
POST   /api/v1/delivery-rules               # 创建配送规则
PUT    /api/v1/delivery-rules/:id           # 更新配送规则
GET    /api/v1/deliveries/available         # 可抢单列表（快递员）
POST   /api/v1/deliveries/:id/assign        # 物业指派配送
```

### 2.12 消息模块 (Messages)

```
POST   /api/v1/messages                     # 发送消息
GET    /api/v1/messages                     # 消息列表
GET    /api/v1/messages/:id                 # 消息详情
POST   /api/v1/messages/broadcast           # 群发消息
POST   /api/v1/messages/:id/read            # 标记已读
```

### 2.13 活动组模块 (ActivityGroups)

```
POST   /api/v1/activity-groups              # 创建活动组
GET    /api/v1/activity-groups              # 活动组列表
GET    /api/v1/activity-groups/:id          # 活动组详情
PUT    /api/v1/activity-groups/:id          # 更新活动组
POST   /api/v1/activity-groups/:id/join     # 加入活动组
POST   /api/v1/activity-groups/:id/leave    # 退出活动组
GET    /api/v1/activity-groups/:id/members  # 活动组成员列表
DELETE /api/v1/activity-groups/:id/members/:memberId  # 移除成员
```

### 2.14 意见箱模块 (OpinionBoxes)

```
POST   /api/v1/opinion-boxes                # 创建意见箱
GET    /api/v1/opinion-boxes                # 意见箱列表
GET    /api/v1/opinion-boxes/:id            # 意见箱详情
POST   /api/v1/opinion-boxes/:id/vote       # 投票
GET    /api/v1/opinion-boxes/:id/result     # 投票结果
```

### 2.15 社区服务模块 (Services)

```
POST   /api/v1/services                     # 发布服务
GET    /api/v1/services                     # 服务列表
GET    /api/v1/services/:id                 # 服务详情
PUT    /api/v1/services/:id                 # 更新服务
DELETE /api/v1/services/:id                 # 删除服务
POST   /api/v1/services/:id/book            # 预约服务
GET    /api/v1/service-orders               # 服务订单列表
GET    /api/v1/service-orders/:id           # 服务订单详情
PATCH  /api/v1/service-orders/:id/status    # 更新服务订单状态
```

### 2.16 统筹模块 (Coordinators)

```
POST   /api/v1/coordinators                 # 统筹入驻
GET    /api/v1/coordinators/me              # 我的统筹信息
GET    /api/v1/coordinators/me/stats        # 统筹收益统计
GET    /api/v1/coordinators/me/orders       # 统筹订单列表
```

### 2.17 分成模块 (Distribution)

```
GET    /api/v1/distribution/records         # 分成记录列表
GET    /api/v1/distribution/stats           # 分成统计
GET    /api/v1/distribution/calculate       # 试算分成（预览）
```

### 2.18 报表模块 (Reports)

```
GET    /api/v1/reports/overview             # 总览报表
GET    /api/v1/reports/point-summary        # 积分汇总
GET    /api/v1/reports/order-summary        # 订单汇总
GET    /api/v1/reports/distribution-summary # 分成汇总
GET    /api/v1/reports/export               # 导出报表
```

### 2.19 文件模块 (Files)

```
POST   /api/v1/files/upload                 # 上传文件
POST   /api/v1/files/upload-batch           # 批量上传
GET    /api/v1/files/:id                    # 获取文件信息
DELETE /api/v1/files/:id                    # 删除文件
```

### 2.20 物业公司模块 (PropertyCompanies)

```
POST   /api/v1/property-companies           # 创建物业公司
GET    /api/v1/property-companies           # 物业公司列表
GET    /api/v1/property-companies/:id       # 物业公司详情
PUT    /api/v1/property-companies/:id       # 更新物业公司
GET    /api/v1/property-companies/:id/communities  # 物业公司下的小区
```

## 3. 请求/响应示例

### 3.1 创建订单

**请求**：
```json
POST /api/v1/orders
Authorization: Bearer <token>

{
  "merchantId": "mch_c4d2p8",
  "items": [
    {
      "productId": "prd_xxx001",
      "quantity": 2
    },
    {
      "productId": "prd_xxx002",
      "quantity": 1
    }
  ],
  "paymentMethod": "property_coin",
  "address": "3栋-502",
  "contactPhone": "138****1234",
  "remark": "请尽快配送"
}
```

**响应**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "ord_e6n5q3",
    "orderNo": "ORD20260630120000001",
    "orderStatus": "pending",
    "totalAmount": 35.50,
    "deliveryFee": 0.30,
    "paymentMethod": "property_coin",
    "items": [
      {
        "productId": "prd_xxx001",
        "productName": "大米5kg",
        "quantity": 2,
        "unitPrice": 12.00,
        "totalPrice": 24.00
      },
      {
        "productId": "prd_xxx002",
        "productName": "食用油1L",
        "quantity": 1,
        "unitPrice": 11.50,
        "totalPrice": 11.50
      }
    ],
    "createdAt": "2026-06-30T12:00:00.000Z"
  },
  "timestamp": "2026-06-30T12:00:00.000Z"
}
```

### 3.2 获取积分余额

**请求**：
```
GET /api/v1/points/balance
Authorization: Bearer <token>
```

**响应**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "pointBalance": 8420,
    "familyPointBalance": 12500,
    "todayEarned": 150,
    "todaySpent": 0,
    "expiringPoints": 500,
    "expiringDate": "2026-07-31"
  },
  "timestamp": "2026-06-30T12:00:00.000Z"
}
```

### 3.3 订单列表（分页）

**请求**：
```
GET /api/v1/orders/my?page=1&pageSize=20&sort=-createdAt
Authorization: Bearer <token>
```

**响应**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "ord_e6n5q3",
        "orderNo": "ORD20260630120000001",
        "merchantName": "万达超市",
        "merchantCover": "https://cdn.example.com/merchant/xxx.jpg",
        "orderStatus": "completed",
        "totalAmount": 35.50,
        "itemCount": 3,
        "createdAt": "2026-06-30T12:00:00.000Z"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 156,
      "totalPages": 8
    }
  },
  "timestamp": "2026-06-30T12:00:00.000Z"
}
```

### 3.4 错误响应

**参数校验错误**：
```json
{
  "code": 10001,
  "message": "参数校验失败",
  "data": null,
  "errors": [
    {
      "field": "name",
      "message": "商家名称不能为空",
      "value": null
    },
    {
      "field": "commissionRate",
      "message": "佣金比例必须在0-1之间",
      "value": 1.5
    }
  ],
  "timestamp": "2026-06-30T12:00:00.000Z"
}
```

**业务错误**：
```json
{
  "code": 40001,
  "message": "积分余额不足",
  "data": null,
  "timestamp": "2026-06-30T12:00:00.000Z"
}
```

## 4. 权限矩阵

### 4.1 接口权限声明

| 接口 | 鉴权 | 允许角色 |
|------|------|----------|
| POST /auth/login | Optional | any |
| GET /residents/me | Required | all |
| GET /residents | Required | property_admin, platform_admin |
| POST /merchants | Required | platform_admin |
| PATCH /merchants/:id/status | Required | property_admin, platform_admin |
| GET /orders/my | Required | resident |
| POST /orders | Required | resident |
| POST /deliveries/:id/grab | Required | courier |
| POST /announcements | Required | property_admin, platform_admin |
| GET /reports/overview | Required | property_admin, platform_admin |

### 4.2 数据范围限制

| 角色 | 数据范围 |
|------|----------|
| resident | 仅自己和家庭的数据 |
| property_admin | 所属物业公司的所有数据 |
| platform_admin | 全平台数据 |
| merchant | 自己商家的数据 |
| courier | 所属物业范围内的配送订单 |

## 5. WebSocket接口

### 5.1 连接地址
```
wss://zlysapi.51jijiehao.com/ws?token=<accessToken>
```

### 5.2 消息类型

| type | 方向 | payload |
|------|------|---------|
| chat.message | 双向 | { fromId, toId, content, chatType } |
| chat.broadcast | 服务端→客户端 | { fromId, targetRole, content } |
| notification.new | 服务端→客户端 | { announcementId, announcementType, title } |
| order.status_update | 服务端→客户端 | { orderId, orderStatus, updatedAt } |
| delivery.grab_result | 服务端→客户端 | { orderId, courierId, result } |
| point.balance_update | 服务端→客户端 | { residentId, pointType, balance } |
| heartbeat.ping | 客户端→服务端 | {} |
| heartbeat.pong | 服务端→客户端 | {} |

### 5.3 心跳机制
- 客户端每30秒发送 `heartbeat.ping`
- 服务端收到后立即回复 `heartbeat.pong`
- 客户端若60秒未收到pong，触发重连

## 6. 核心业务规则

### 6.1 积分规则
- **获取**：消费返积分、活动奖励、签到
- **兑换**：100积分 = 1元（可配置）
- **过期**：2年未使用自动清零
- **共享**：家庭成员共享积分池
- **限制**：不可兑换物业币

### 6.2 物业币规则
- **获取**：消费获得（按商家设定比例）
- **使用**：支付物业费、购买服务
- **价值**：1物业币 = 1元
- **共享**：家庭成员共享物业币池
- **限制**：不可兑换积分

### 6.3 物业费规则
- **类型**：物业费、停车费、水电费
- **周期**：月度、季度、年度
- **计算**：固定金额、按面积、按用量
- **扣款**：支持自动扣款（物业币/积分）
- **滞纳**：逾期按日加收滞纳金

### 6.4 配送规则
- **费用**：基础配送费 + 按重量加收
- **免配送**：满额免配送费
- **抢单**：快递员抢单 + 物业兜底
- **分成**：快递员获得配送费（可配置比例）

### 6.5 分成规则
- **公式**：消费金额 × 佣金比例 = 可分成金额
- **物业**：可分成金额 × 30%
- **统筹**：可分成金额 × 70%
- **技工**：统筹收入 × 10%

### 6.6 家庭规则
- **创建**：业主注册时创建家庭
- **成员**：最多15人
- **共享**：积分、物业币、物业费
- **退出**：成员退出不影响家庭积分

### 6.7 公告规则
- **类型**：物业、社区、统筹、系统、商家、活动组
- **推送**：按角色、楼栋定向推送
- **商家**：每周限发1次

### 6.8 意见箱规则
- **投票**：支持、反对、弃权
- **权限**：一户一票
- **范围**：全物业范围投票
- **时效**：设定截止时间

## 7. 分页参数规范

### 6.1 请求参数
| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| page | number | 1 | 当前页码 |
| pageSize | number | 20 | 每页条数（最大100） |
| sort | string | -createdAt | 排序（-降序，+升序） |

### 6.2 过滤参数
| 参数 | 示例 | 说明 |
|------|------|------|
| status | ?status=active | 等于过滤 |
| createdAt.gte | ?createdAt.gte=2026-01-01 | 大于等于 |
| createdAt.lte | ?createdAt.lte=2026-06-30 | 小于等于 |
| keyword | ?keyword=超市 | 模糊搜索 |

## 7. 枚举值定义

### 7.1 用户角色 (UserRole)
```typescript
RESIDENT: 'resident'           // 业主
PROPERTY_ADMIN: 'property_admin'  // 物业管理员
PLATFORM_ADMIN: 'platform_admin'  // 平台管理员
MERCHANT: 'merchant'          // 商家
COURIER: 'courier'            // 快递员
ACTIVITY_LEADER: 'activity_leader' // 活动组组长
TECHNICIAN: 'technician'      // 技工
```

### 7.2 商家分级 (MerchantLevel)
```typescript
OFFICIAL_CERTIFIED: 'official_certified'  // 官方认证
PROPERTY_CERTIFIED: 'property_certified'  // 物业认证
NORMAL: 'normal'                          // 普通
```

### 7.3 订单状态 (OrderStatus)
```typescript
PENDING: 'pending'        // 待支付
PAID: 'paid'              // 已支付
DELIVERING: 'delivering'  // 配送中
COMPLETED: 'completed'    // 已完成
CANCELLED: 'cancelled'    // 已取消
REFUNDING: 'refunding'    // 退款中
REFUNDED: 'refunded'      // 已退款
```

### 7.4 公告类型 (AnnouncementType)
```typescript
PROPERTY: 'property_announcement'     // 物业公告
COMMUNITY: 'community_announcement'   // 社区公告
PLATFORM: 'platform_announcement'     // 统筹/官方推荐
SYSTEM: 'system_announcement'         // 系统公告
MERCHANT: 'merchant_announcement'     // 商家公告
ACTIVITY: 'activity_announcement'     // 活动组公告
```

### 7.5 投票选项 (VoteOption)
```typescript
SUPPORT: 'support'    // 支持
OPPOSE: 'oppose'      // 反对
ABSTAIN: 'abstain'    // 弃权
```

### 7.6 消息类型 (MessageType)
```typescript
PROPERTY: 'property_message'       // 物业消息
PLATFORM: 'platform_message'       // 统筹消息
SYSTEM: 'system_message'           // 系统消息
COMMUNITY: 'community_message'     // 社区消息
RESIDENT: 'resident_message'       // 业主消息（私聊）
TECHNICIAN: 'technician_message'   // 技工消息（群发）
```

---

## 8. 物业公司配置接口（住户端）

> 住户端获取当前所属物业公司的公开配置信息，用于前端展示分成比例、配送费、提现费率、积分兑换比等。

### 8.1 接口清单

| 方法 | 路径 | 名称 | 鉴权 | 允许角色 |
|------|------|------|------|----------|
| GET | `/api/v1/property-companies/current` | 获取当前物业公司配置 | Required | resident, merchant, courier, activity_leader, technician |

### 8.2 GET `/api/v1/property-companies/current` - 获取当前物业公司配置

**鉴权**：Required
**允许角色**：`resident`、`merchant`、`courier`、`activity_leader`、`technician`
**所需权限码**：无（登录用户默认可查本公司公开配置）

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
      "baseFee": 3.00,
      "courierShareRate": 1.0000,
      "courierPerOrder": 0.35
    },
    "withdrawalFeeRate": 0.0060,
    "pointToFeeRate": 0.0100,
    "twoYearClearEnabled": true,
    "neighborDailyContactLimit": 2
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

**业务说明**：仅返回公开字段，敏感配置（如内部分成成本）不返回。前端用于展示物业币卡片、积分兑换比、配送费、邻居私聊次数限制等。

**关联数据表**：`property_companies`

---

## 9. 平台级商家库接口（管理端）

> 平台管理员统一录入商家基础信息（一次录入，多物业复用），后续挂接到各物业公司时独立设定参数。

### 9.1 接口清单

| 方法 | 路径 | 名称 | 鉴权 | 允许角色 | 权限码 |
|------|------|------|------|----------|--------|
| GET | `/api/v1/admin/platform-merchants` | 平台商家库列表 | Required | platform_admin | `merchant:read` |
| POST | `/api/v1/admin/platform-merchants` | 创建平台商家 | Required | platform_admin | `merchant:create` |
| GET | `/api/v1/admin/platform-merchants/:id` | 平台商家详情 | Required | platform_admin | `merchant:read` |
| PUT | `/api/v1/admin/platform-merchants/:id` | 更新平台商家 | Required | platform_admin | `merchant:update` |
| POST | `/api/v1/merchants` | 商家挂接到物业 | Required | platform_admin, property_admin | `merchant:bind` |

### 9.2 GET `/api/v1/admin/platform-merchants` - 平台商家库列表

**鉴权**：Required
**允许角色**：`platform_admin`
**所需权限码**：`merchant:read`

**请求参数（Query）**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `page` | number | 否 | 当前页（默认1） |
| `pageSize` | number | 否 | 每页条数（默认20，最大100） |
| `keyword` | string | 否 | 名称/电话模糊搜索 |
| `category` | string | 否 | 品类筛选（外卖/周围商家/自营/农家/外卖小吃/团购） |
| `linkedPropertyCompanyId` | string | 否 | 已挂接到某物业公司的商家 |
| `status` | string | 否 | `active`/`inactive` |
| `sort` | string | 否 | 默认 `-createdAt` |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "pm_x1y2z3",
        "name": "万达超市",
        "category": "超市便利",
        "coverUrl": "https://cdn.example.com/merchant/001.jpg",
        "contactPhone": "021-12345678",
        "address": "上海市浦东新区张江路50号",
        "businessHours": "08:00-22:00",
        "linkedPropertyCount": 5,
        "status": "active",
        "createdAt": "2026-03-10T10:00:00.000Z"
      }
    ],
    "pagination": { "page": 1, "pageSize": 20, "total": 256, "totalPages": 13 }
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

**关联数据表**：`platform_merchants`、`merchants`（统计挂接数）

### 9.3 POST `/api/v1/admin/platform-merchants` - 创建平台商家

**鉴权**：Required
**允许角色**：`platform_admin`
**所需权限码**：`merchant:create`

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `name` | string | 是 | 商家名称 |
| `category` | string | 是 | 品类（外卖/周围商家/自营/农家/外卖小吃/团购） |
| `contactPhone` | string | 是 | 联系电话 |
| `address` | string | 否 | 商家地址 |
| `coverUrl` | string | 否 | 封面图URL |
| `description` | string | 否 | 商家描述 |
| `businessHours` | string | 否 | 营业时间（如 `08:00-22:00`） |
| `businessLicense` | string | 否 | 营业执照URL |
| `contactPerson` | string | 否 | 联系人 |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "pm_x1y2z3",
    "name": "万达超市",
    "category": "超市便利",
    "createdAt": "2026-07-01T10:00:00.000Z"
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

**业务说明**：平台商家库基础信息录入后，可通过 `POST /api/v1/merchants` 挂接到具体物业公司。同一平台商家在不同物业公司下独立配置参数。

**关联数据表**：`platform_merchants`

### 9.4 PUT `/api/v1/admin/platform-merchants/:id` - 更新平台商家

**鉴权**：Required
**允许角色**：`platform_admin`
**所需权限码**：`merchant:update`

**路径参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | string | 是 | 平台商家ID |

**请求参数（Body）**：同 9.3，所有字段可选。

**响应**：返回更新后的完整商家对象。

**关联数据表**：`platform_merchants`

### 9.5 POST `/api/v1/merchants` - 商家挂接到物业

**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`
**所需权限码**：`merchant:bind`

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `platformMerchantId` | string | 是 | 平台商家库ID |
| `propertyCompanyId` | string | 是 | 挂接的物业公司ID（property_admin 自动取当前账号所属公司） |
| `name` | string | 否 | 商家名称（不传则使用平台商家名称） |
| `merchantLevel` | string | 否 | 分级：`official_certified`/`property_certified`/`normal`（默认 `normal`，`official_certified` 仅 platform_admin 可设） |
| `rankOrder` | number | 否 | 手动排序值（默认0） |
| `deliveryFee` | number | 否 | 商家设定的配送费（默认0） |
| `freeDeliveryThreshold` | number | 否 | 满额免配送费 |
| `commissionRate` | number | 是 | 抽佣比例（0-1，如0.10=10%） |
| `pointExchangeRate` | number | 是 | 积分兑换比（如100积分=1元则填100） |
| `memberDiscountPrice` | number | 否 | 业主折扣价 |
| `coinRebateRate` | number | 否 | 物业币返现比（仅高抽佣≥20%商家生效，默认0） |
| `coinRebateEnabled` | boolean | 否 | 是否启用物业币返现（默认false） |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "mch_c4d2p8",
    "platformMerchantId": "pm_x1y2z3",
    "propertyCompanyId": "pc_a1b2c3d4",
    "name": "万达超市",
    "merchantLevel": "property_certified",
    "auditStatus": "approved",
    "config": {
      "commissionRate": 0.10,
      "pointExchangeRate": 100,
      "coinRebateEnabled": false
    },
    "createdAt": "2026-07-01T10:00:00.000Z"
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

**业务说明**：挂接时同时创建 `merchants` 记录和 `merchant_property_configs` 参数记录。同一平台商家+物业公司组合唯一，重复挂接返回 `30004 duplicate_merchant_binding`。`property_admin` 仅可挂接到本公司。

**关联数据表**：`platform_merchants`、`merchants`、`merchant_property_configs`

---

## 10. 线下积分接口（管理员手动发放）

> 物业管理员手动为线下消费场景（如现金消费、外部商家）的住户发放积分，发放后写入积分池扣除。

### 10.1 接口清单

| 方法 | 路径 | 名称 | 鉴权 | 允许角色 | 权限码 |
|------|------|------|------|----------|--------|
| POST | `/api/v1/admin/offline-points/grant` | 线下积分发放 | Required | platform_admin, property_admin | `points:input` |
| GET | `/api/v1/admin/offline-points/records` | 发放记录列表 | Required | platform_admin, property_admin | `points:input` |

### 10.2 POST `/api/v1/admin/offline-points/grant` - 线下积分发放

**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`
**所需权限码**：`points:input`

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `residentId` | string | 是 | 接收住户ID |
| `familyId` | string | 否 | 家庭ID（默认取住户的家庭） |
| `pointAmount` | number | 是 | 发放积分数量（正整数） |
| `source` | string | 是 | 来源：`manual`/`offline_consume`/`activity_reward` |
| `description` | string | 是 | 发放说明（如"线下现金消费返积分"） |
| `relatedOrderId` | string | 否 | 关联订单ID（如有） |
| `operatorRemark` | string | 否 | 操作备注 |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "recordId": "pt_f1h8w7",
    "residentId": "res_001",
    "pointAmount": 500,
    "balanceAfter": 8420,
    "familyBalanceAfter": 12500,
    "source": "manual",
    "operatorId": "res_admin001",
    "createdAt": "2026-07-01T10:00:00.000Z"
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

**业务说明**：发放时写入 `point_records`（`source=manual`），同时更新住户与家庭积分余额。所有发放需记录到操作日志。`property_admin` 仅可发放给本公司住户。

**关联数据表**：`point_records`、`residents`、`families`、`operation_logs`

### 10.3 GET `/api/v1/admin/offline-points/records` - 线下积分发放记录

**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`
**所需权限码**：`points:input`

**请求参数（Query）**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `page` | number | 否 | 当前页 |
| `pageSize` | number | 否 | 每页条数 |
| `residentId` | string | 否 | 按住户筛选 |
| `operatorId` | string | 否 | 按操作人筛选 |
| `source` | string | 否 | 按来源筛选 |
| `startDate` | string | 否 | 起始日期 `YYYY-MM-DD` |
| `endDate` | string | 否 | 结束日期 `YYYY-MM-DD` |
| `propertyCompanyId` | string | 否 | 仅 platform_admin 可指定 |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "pt_f1h8w7",
        "residentId": "res_001",
        "residentName": "王五",
        "pointAmount": 500,
        "balanceAfter": 8420,
        "source": "manual",
        "description": "线下现金消费返积分",
        "operatorId": "res_admin001",
        "operatorName": "张三",
        "createdAt": "2026-07-01T10:00:00.000Z"
      }
    ],
    "pagination": { "page": 1, "pageSize": 20, "total": 86, "totalPages": 5 }
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

**关联数据表**：`point_records`

---

## 11. 商家积分购买/赠送/提现接口

> 商家在线上购买积分（用于线下消费返积分场景），将购买积分录入给住户；商家提现佣金收入到银行账户。

### 11.1 接口清单

| 方法 | 路径 | 名称 | 鉴权 | 允许角色 | 权限码 |
|------|------|------|------|----------|--------|
| POST | `/api/v1/merchant/points/purchase` | 商家购买积分 | Required | merchant | `merchant:points_purchase` |
| GET | `/api/v1/merchant/points/purchases` | 购买记录 | Required | merchant | `merchant:points_purchase` |
| POST | `/api/v1/merchant/points/grant` | 商家赠送积分给住户 | Required | merchant | `merchant:points_grant` |
| POST | `/api/v1/merchant/withdrawals` | 商家提现申请 | Required | merchant | `merchant:withdraw` |
| GET | `/api/v1/merchant/withdrawals` | 提现记录 | Required | merchant | `merchant:withdraw` |
| POST | `/api/v1/admin/merchant-point-purchases/:id/audit` | 审核购买 | Required | platform_admin, property_admin | `merchant:audit` |
| POST | `/api/v1/admin/merchant-withdrawals/:id/audit` | 审核提现 | Required | platform_admin, property_admin | `merchant:audit` |

### 11.2 POST `/api/v1/merchant/points/purchase` - 商家购买积分

**鉴权**：Required
**允许角色**：`merchant`
**所需权限码**：`merchant:points_purchase`

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `pointAmount` | number | 是 | 购买积分数量（正整数） |
| `payAmount` | number | 是 | 支付金额（元） |
| `paymentMethod` | string | 是 | 支付方式：`wechat`/`alipay`/`bank_transfer` |
| `paymentProof` | string | 否 | 付款凭证URL（银行转账必填） |
| `remark` | string | 否 | 申请备注（如"用于618促销"） |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "mpp_x1y2z3",
    "merchantId": "mch_c4d2p8",
    "pointAmount": 100000,
    "payAmount": 1000.00,
    "paymentMethod": "wechat",
    "auditStatus": "pending",
    "createdAt": "2026-06-28T15:00:00.000Z"
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

**业务说明**：购买申请提交后进入 `pending` 审核状态，由物业/平台管理员审核通过后积分到商家积分账户。审核前不可使用。

**关联数据表**：`merchant_point_purchases`

### 11.3 GET `/api/v1/merchant/points/purchases` - 购买记录

**鉴权**：Required
**允许角色**：`merchant`
**所需权限码**：`merchant:points_purchase`

**请求参数（Query）**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `page` | number | 否 | 当前页 |
| `pageSize` | number | 否 | 每页条数 |
| `auditStatus` | string | 否 | `pending`/`approved`/`rejected` |
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
        "id": "mpp_x1y2z3",
        "pointAmount": 100000,
        "payAmount": 1000.00,
        "paymentMethod": "wechat",
        "paymentProof": "https://cdn.example.com/payment/001.jpg",
        "auditStatus": "approved",
        "remainingPoints": 85000,
        "auditedAt": "2026-06-29T10:00:00.000Z",
        "auditorName": "张三",
        "createdAt": "2026-06-28T15:00:00.000Z"
      }
    ],
    "pagination": { "page": 1, "pageSize": 20, "total": 8, "totalPages": 1 }
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

**关联数据表**：`merchant_point_purchases`

### 11.4 POST `/api/v1/merchant/points/grant` - 商家赠送积分给住户

**鉴权**：Required
**允许角色**：`merchant`
**所需权限码**：`merchant:points_grant`

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `purchaseId` | string | 是 | 关联购买记录ID（扣减剩余积分） |
| `residentId` | string | 是 | 接收住户ID |
| `pointAmount` | number | 是 | 录入积分数量（正整数，不超过 purchaseId 剩余额度） |
| `description` | string | 否 | 录入说明（如"线下消费满100返10积分"） |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "mpg_a1b2c3",
    "merchantId": "mch_c4d2p8",
    "purchaseId": "mpp_x1y2z3",
    "residentId": "res_001",
    "pointAmount": 500,
    "exchangeRate": 100,
    "purchaseRemainingPoints": 84500,
    "residentBalanceAfter": 8420,
    "createdAt": "2026-07-01T10:00:00.000Z"
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

**业务说明**：扣减 `merchant_point_purchases.remaining_points`，同时为住户写入积分记录（`source=merchant_grant`）。住户所属物业公司必须与商家挂接的物业公司一致。商家仅可录入到自己挂接的物业公司下的住户。

**关联数据表**：`merchant_point_grants`、`merchant_point_purchases`、`point_records`、`residents`

### 11.5 POST `/api/v1/merchant/withdrawals` - 商家提现申请

**鉴权**：Required
**允许角色**：`merchant`
**所需权限码**：`merchant:withdraw`

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `amount` | number | 是 | 提现金额（元，不超过可提现余额） |
| `bankCard` | string | 是 | 银行卡号（脱敏存储） |
| `accountName` | string | 是 | 账户名 |
| `bankName` | string | 是 | 开户行 |
| `remark` | string | 否 | 申请备注 |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "mw_d4e5f6",
    "merchantId": "mch_c4d2p8",
    "amount": 5000.00,
    "feeRate": 0.0060,
    "feeAmount": 30.00,
    "actualAmount": 4970.00,
    "auditStatus": "pending",
    "createdAt": "2026-06-29T10:00:00.000Z"
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

**业务说明**：手续费率取当前物业公司 `withdrawal_fee_rate` 配置。提现金额冻结到商家待结算余额，审核拒绝时返还。审核通过后由财务线下打款并上传转账凭证。

**关联数据表**：`merchant_withdrawals`、`property_companies`（取费率）

### 11.6 GET `/api/v1/merchant/withdrawals` - 提现记录

**鉴权**：Required
**允许角色**：`merchant`
**所需权限码**：`merchant:withdraw`

**请求参数（Query）**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `page` | number | 否 | 当前页 |
| `pageSize` | number | 否 | 每页条数 |
| `auditStatus` | string | 否 | `pending`/`approved`/`rejected`/`completed` |
| `startDate` | string | 否 | 起始日期 |
| `endDate` | string | 否 | 结束日期 |

**响应**：结构同 11.5 响应列表形式，含 `transferProof`（审核通过后由管理员上传）字段。

**关联数据表**：`merchant_withdrawals`

### 11.7 POST `/api/v1/admin/merchant-point-purchases/:id/audit` - 审核购买

**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`
**所需权限码**：`merchant:audit`

**路径参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | string | 是 | 购买记录ID |

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `auditResult` | string | 是 | `approved`/`rejected` |
| `actualPointAmount` | number | 否 | 实际发放积分（仅通过时，不传则按申请数量） |
| `rejectReason` | string | 否 | 拒绝时必填 |
| `remark` | string | 否 | 审核备注 |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "mpp_x1y2z3",
    "auditStatus": "approved",
    "pointAmount": 100000,
    "actualPointAmount": 100000,
    "merchantPointBalance": 250000,
    "auditedAt": "2026-07-01T10:00:00.000Z",
    "auditorId": "res_admin001"
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

**业务说明**：通过后积分入商家积分账户（`merchant_point_purchases.remaining_points`）；拒绝则关闭申请，不扣款。重复审核返回 `60003 audit_already_processed`。

**关联数据表**：`merchant_point_purchases`

### 11.8 POST `/api/v1/admin/merchant-withdrawals/:id/audit` - 审核提现

**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`
**所需权限码**：`merchant:audit`

**路径参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | string | 是 | 提现记录ID |

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `auditResult` | string | 是 | `approved`/`rejected`/`completed` |
| `transferProof` | string | 否 | 转账凭证URL（通过时建议提供） |
| `rejectReason` | string | 否 | 拒绝时必填 |
| `remark` | string | 否 | 审核备注 |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "mw_d4e5f6",
    "auditStatus": "approved",
    "transferProof": "https://cdn.example.com/transfer/001.jpg",
    "auditedAt": "2026-07-01T10:00:00.000Z",
    "auditorId": "res_admin001"
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

**业务说明**：`approved` 表示审核通过待打款，`completed` 表示已打款完成。拒绝时冻结金额返还商家可提现余额。

**关联数据表**：`merchant_withdrawals`

---

## 12. 物业币兑换接口

> 商家将累计收取的物业币兑换为现金，提现到银行账户。

### 12.1 接口清单

| 方法 | 路径 | 名称 | 鉴权 | 允许角色 | 权限码 |
|------|------|------|------|----------|--------|
| POST | `/api/v1/coin-withdrawals` | 物业币兑换申请 | Required | merchant | `coin:withdraw` |
| GET | `/api/v1/coin-withdrawals` | 兑换记录 | Required | merchant | `coin:withdraw` |
| POST | `/api/v1/admin/coin-withdrawals/:id/audit` | 审核兑换 | Required | platform_admin, property_admin | `coin:audit` |

### 12.2 POST `/api/v1/coin-withdrawals` - 物业币兑换申请

**鉴权**：Required
**允许角色**：`merchant`
**所需权限码**：`coin:withdraw`

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `coinAmount` | number | 是 | 兑换物业币数量（不超过商家当前可兑换余额） |
| `bankCard` | string | 是 | 银行卡号 |
| `accountName` | string | 是 | 账户名 |
| `bankName` | string | 是 | 开户行 |
| `remark` | string | 否 | 申请备注 |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "cw_g7h8i9",
    "merchantId": "mch_c4d2p8",
    "coinAmount": 5000.00,
    "cashAmount": 5000.00,
    "feeRate": 0.0060,
    "feeAmount": 30.00,
    "actualAmount": 4970.00,
    "auditStatus": "pending",
    "createdAt": "2026-07-01T10:00:00.000Z"
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

**业务说明**：1 物业币 = 1 元。兑换金额 = 物业币数量，手续费 = 兑换金额 × 物业公司 `withdrawal_fee_rate`。审核期间对应物业币从商家账户冻结。

**关联数据表**：`coin_withdrawals`、`property_companies`

### 12.3 GET `/api/v1/coin-withdrawals` - 兑换记录

**鉴权**：Required
**允许角色**：`merchant`（仅本人）、`platform_admin`、`property_admin`（管理员查询全部）
**所需权限码**：`coin:withdraw`（商家）/ `coin:audit`（管理员）

**请求参数（Query）**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `page` | number | 否 | 当前页 |
| `pageSize` | number | 否 | 每页条数 |
| `merchantId` | string | 否 | 商家筛选（管理员可用） |
| `auditStatus` | string | 否 | `pending`/`approved`/`rejected`/`completed` |
| `startDate` | string | 否 | 起始日期 |
| `endDate` | string | 否 | 结束日期 |

**响应**：结构同 12.2 响应列表形式。

**关联数据表**：`coin_withdrawals`

### 12.4 POST `/api/v1/admin/coin-withdrawals/:id/audit` - 审核兑换

**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`
**所需权限码**：`coin:audit`

**路径参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | string | 是 | 兑换记录ID |

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `auditResult` | string | 是 | `approved`/`rejected`/`completed` |
| `transferProof` | string | 否 | 转账凭证URL |
| `rejectReason` | string | 否 | 拒绝时必填 |
| `remark` | string | 否 | 审核备注 |

**响应**：返回更新后的兑换记录（含 `auditedAt`、`auditorId`）。

**业务说明**：通过后冻结的物业币正式扣除；拒绝则返还商家账户。

**关联数据表**：`coin_withdrawals`

---

## 13. 物业币管控接口

> 物业管理员冻结/解冻住户物业币、住户自行隐藏物业币卡片、手动触发过期清零。

### 13.1 接口清单

| 方法 | 路径 | 名称 | 鉴权 | 允许角色 | 权限码 |
|------|------|------|------|----------|--------|
| POST | `/api/v1/admin/residents/:id/coin/freeze` | 冻结物业币 | Required | platform_admin, property_admin | `coin:freeze` |
| POST | `/api/v1/admin/residents/:id/coin/unfreeze` | 解冻物业币 | Required | platform_admin, property_admin | `coin:freeze` |
| POST | `/api/v1/residents/me/coin/hide` | 隐藏物业币卡片 | Required | resident | 无 |
| POST | `/api/v1/admin/coin-expiry/run` | 手动触发过期清零 | Required | platform_admin, property_admin | `coin:expiry_run` |
| GET | `/api/v1/admin/coin-expiry/records` | 过期清零记录 | Required | platform_admin, property_admin | `coin:expiry_run` |

### 13.2 POST `/api/v1/admin/residents/:id/coin/freeze` - 冻结物业币

**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`
**所需权限码**：`coin:freeze`

**路径参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | string | 是 | 住户ID |

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `amount` | number | 否 | 冻结金额（不传则全部冻结） |
| `reason` | string | 是 | 冻结原因 |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "cfr_j1k2l3",
    "residentId": "res_001",
    "action": "freeze",
    "amount": 500.00,
    "reason": "违规操作",
    "operatorId": "res_admin001",
    "createdAt": "2026-06-25T10:00:00.000Z"
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

**业务说明**：将 `residents.coin_frozen` 置为 `true`，冻结期间禁止消费、提现、转账。冻结金额不影响余额显示，但会校验可用余额。

**关联数据表**：`coin_freeze_records`、`residents`

### 13.3 POST `/api/v1/admin/residents/:id/coin/unfreeze` - 解冻物业币

**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`
**所需权限码**：`coin:freeze`

**路径参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | string | 是 | 住户ID |

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `frozenRecordId` | string | 否 | 指定解冻记录ID（不传则全部解冻） |
| `reason` | string | 是 | 解冻原因 |

**响应**：返回解冻记录（结构同 13.2，`action=unfreeze`）。

**业务说明**：解冻后 `residents.coin_frozen` 置为 `false`（若仍有未解冻记录则保持 `true`）。

**关联数据表**：`coin_freeze_records`、`residents`

### 13.4 POST `/api/v1/residents/me/coin/hide` - 隐藏物业币卡片

**鉴权**：Required
**允许角色**：`resident`
**所需权限码**：无

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `hidden` | boolean | 是 | `true`=隐藏 / `false`=显示 |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "residentId": "res_b7m9x1",
    "coinHidden": true,
    "updatedAt": "2026-07-01T10:00:00.000Z"
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

**业务说明**：住户可自行隐藏 APP 端物业币卡片，仅影响展示不影响余额。物业公司的 `coin_display_enabled=false` 时全局隐藏，本接口不生效。

**关联数据表**：`residents`

### 13.5 POST `/api/v1/admin/coin-expiry/run` - 手动触发过期清零

**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`
**所需权限码**：`coin:expiry_run`

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `expiryDate` | string | 是 | 过期日期 `YYYY-MM-DD` |
| `dryRun` | boolean | 否 | 是否试运行（默认 false，试运行不实际清零） |
| `residentIds` | array | 否 | 指定住户ID列表（不传则全部过期住户） |
| `propertyCompanyId` | string | 否 | 仅 platform_admin 可指定 |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "taskId": "task_cer_001",
    "expiryDate": "2026-06-30",
    "dryRun": false,
    "processedCount": 56,
    "totalExpiredAmount": 4280.50,
    "clearedToPool": 4280.50,
    "status": "completed",
    "executedAt": "2026-07-01T03:00:00.000Z"
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

**业务说明**：异步执行。过期物业币清零后写入 `coin_expiry_records`，金额转入积分池（`point_pool_records.source=coin_expired`）。试运行模式仅返回预计处理结果，不实际变更数据。

**关联数据表**：`coin_expiry_records`、`property_coin_records`、`point_pool_records`、`point_pools`

### 13.6 GET `/api/v1/admin/coin-expiry/records` - 过期清零记录

**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`
**所需权限码**：`coin:expiry_run`

**请求参数（Query）**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `page` | number | 否 | 当前页 |
| `pageSize` | number | 否 | 每页条数 |
| `residentId` | string | 否 | 按住户筛选 |
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
        "id": "cer_m4n5o6",
        "residentId": "res_001",
        "residentName": "王五",
        "expiredAmount": 156.80,
        "poolInAmount": 156.80,
        "expiredAt": "2026-06-30T23:59:59.000Z",
        "createdAt": "2026-07-01T03:00:00.000Z"
      }
    ],
    "pagination": { "page": 1, "pageSize": 20, "total": 56, "totalPages": 3 }
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

**关联数据表**：`coin_expiry_records`

---

## 14. 积分池接口（管理员）

> 物业公司维度的积分池资金管理，资金来源于兑换比例差额、物业币过期清零、2年欠费清零。

### 14.1 接口清单

| 方法 | 路径 | 名称 | 鉴权 | 允许角色 | 权限码 |
|------|------|------|------|----------|--------|
| GET | `/api/v1/admin/point-pools` | 积分池余额 | Required | platform_admin, property_admin | `pool:read` |
| GET | `/api/v1/admin/point-pools/records` | 积分池流水 | Required | platform_admin, property_admin | `pool:read` |
| POST | `/api/v1/admin/point-pools/adjust` | 手动调整 | Required | platform_admin, property_admin | `pool:adjust` |

### 14.2 GET `/api/v1/admin/point-pools` - 积分池余额

**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`
**所需权限码**：`pool:read`

**请求参数（Query）**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `propertyCompanyId` | string | 否 | 仅 platform_admin 可指定，不传则取当前账号所属公司 |

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
    "totalIn": 3986000,
    "totalOut": 128000,
    "lastMonthInflow": 125000,
    "lastMonthOutflow": 28000,
    "lastMonthNetInflow": 97000,
    "updatedAt": "2026-06-30T23:59:59.000Z"
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

**关联数据表**：`point_pools`

### 14.3 GET `/api/v1/admin/point-pools/records` - 积分池流水

**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`
**所需权限码**：`pool:read`

**请求参数（Query）**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `page` | number | 否 | 当前页 |
| `pageSize` | number | 否 | 每页条数 |
| `poolType` | string | 否 | `pool_in`/`pool_out` |
| `source` | string | 否 | `exchange_diff`/`coin_expired`/`fee_clear`/`spend`/`manual_adjust` |
| `startDate` | string | 否 | 起始日期 |
| `endDate` | string | 否 | 结束日期 |
| `propertyCompanyId` | string | 否 | 仅 platform_admin 可指定 |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "ppr_p7q8r9",
        "poolType": "pool_in",
        "source": "exchange_diff",
        "amount": 1200,
        "balance": 3858000,
        "description": "消费差额自动进池（订单 ord_e6n5q3）",
        "sourceId": "ord_e6n5q3",
        "operatorId": null,
        "createdAt": "2026-06-30T15:30:00.000Z"
      },
      {
        "id": "ppr_s1t2u3",
        "poolType": "pool_out",
        "source": "spend",
        "amount": 5000,
        "balance": 3853000,
        "description": "小区门禁系统修缮",
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

**关联数据表**：`point_pool_records`

### 14.4 POST `/api/v1/admin/point-pools/adjust` - 手动调整

**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`
**所需权限码**：`pool:adjust`

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `amount` | number | 是 | 调整金额（正数=入池，负数=出池） |
| `poolType` | string | 是 | `pool_in`/`pool_out` |
| `source` | string | 是 | `manual_adjust` |
| `purpose` | string | 是 | 用途说明 |
| `category` | string | 否 | 分类：`repair`/`facility`/`activity`/`other` |
| `voteId` | string | 否 | 关联投票ID（动用积分池需投票时必填） |
| `attachments` | array | 否 | 附件URL数组（合同/报价单等） |
| `propertyCompanyId` | string | 否 | 仅 platform_admin 可指定 |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "ppr_v4w5x6",
    "poolType": "pool_out",
    "amount": 5000,
    "balance": 3853000,
    "purpose": "小区门禁系统修缮",
    "category": "repair",
    "voteId": "vote_001",
    "voteStatus": "passed",
    "operatorId": "res_admin001",
    "createdAt": "2026-07-01T10:00:00.000Z"
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

**业务说明**：若物业公司配置 `pointPoolSpendRequireVote=true`，出池操作必须关联已通过的投票，否则返回 `50001 point_pool_vote_required`。所有调整记录到 `point_pool_records`，并写入操作日志。

**关联数据表**：`point_pool_records`、`point_pools`、`operation_logs`

---

## 15. 权限管理接口

> 权限池、角色快捷预设、用户权限覆盖、权限变更日志的统一管理。

### 15.1 接口清单

| 方法 | 路径 | 名称 | 鉴权 | 允许角色 | 权限码 |
|------|------|------|------|----------|--------|
| GET | `/api/v1/admin/permissions` | 权限池列表 | Required | platform_admin, property_admin | `permission:read` |
| GET | `/api/v1/admin/role-presets` | 角色预设列表 | Required | platform_admin, property_admin | `permission:read` |
| POST | `/api/v1/admin/role-presets` | 创建预设 | Required | platform_admin, property_admin | `permission:manage` |
| PUT | `/api/v1/admin/role-presets/:id` | 更新预设 | Required | platform_admin, property_admin | `permission:manage` |
| GET | `/api/v1/admin/users/:id/permissions` | 用户权限覆盖 | Required | platform_admin, property_admin | `permission:read` |
| POST | `/api/v1/admin/users/:id/permissions/grant` | 授权 | Required | platform_admin, property_admin | `permission:manage` |
| POST | `/api/v1/admin/users/:id/permissions/revoke` | 收回 | Required | platform_admin, property_admin | `permission:manage` |
| GET | `/api/v1/admin/permission-change-logs` | 权限变更日志 | Required | platform_admin, property_admin | `permission:read` |

### 15.2 GET `/api/v1/admin/permissions` - 权限池列表

**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`
**所需权限码**：`permission:read`

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
        "isPlatformOnly": false,
        "status": "active"
      },
      {
        "id": "perm_002",
        "code": "property_company:create",
        "name": "创建物业公司",
        "module": "property_company",
        "description": "仅平台管理员可用",
        "isPlatformOnly": true,
        "status": "active"
      }
    ],
    "modules": ["merchant", "property_company", "resident", "family", "report", "announcement", "fee", "permission", "coin", "pool"]
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

**业务说明**：`isPlatformOnly=true` 的权限仅 `platform_admin` 可分配。`property_admin` 调用时自动过滤平台级权限。

**关联数据表**：`permissions`

### 15.3 GET `/api/v1/admin/role-presets` - 角色预设列表

**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`
**所需权限码**：`permission:read`

**请求参数（Query）**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `scope` | string | 否 | `platform`/`property`（不传则返回平台级+本公司级） |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "presets": [
      {
        "id": "rp_001",
        "name": "统筹",
        "code": "coordinator",
        "description": "统筹标准权限包",
        "propertyCompanyId": null,
        "isBuiltin": true,
        "permissionCodes": ["announcement:publish", "rank:drag", "merchant:audit"],
        "permissionCount": 6,
        "createdAt": "2026-01-15T09:30:00.000Z"
      },
      {
        "id": "rp_007",
        "name": "财务专员",
        "code": "finance",
        "description": "仅查看报表与物业费管理",
        "propertyCompanyId": "pc_a1b2c3d4",
        "isBuiltin": false,
        "permissionCodes": ["report:view", "property_fee:manage"],
        "permissionCount": 2,
        "createdAt": "2026-03-10T10:00:00.000Z"
      }
    ]
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

**关联数据表**：`role_presets`、`role_preset_permissions`、`permissions`

### 15.4 POST `/api/v1/admin/role-presets` - 创建预设

**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`
**所需权限码**：`permission:manage`

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `name` | string | 是 | 预设名称 |
| `code` | string | 否 | 预设编码（不传则自动生成） |
| `description` | string | 否 | 描述 |
| `permissionCodes` | array | 是 | 权限代码数组 |

**响应**：返回新创建的预设（结构同 15.3 单项）。

**业务说明**：`property_admin` 创建的预设 `property_company_id` 自动取当前账号所属公司；`platform_admin` 不传 `propertyCompanyId` 时创建平台级预设。

**关联数据表**：`role_presets`、`role_preset_permissions`

### 15.5 PUT `/api/v1/admin/role-presets/:id` - 更新预设

**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`
**所需权限码**：`permission:manage`

**路径参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | string | 是 | 预设ID |

**请求参数（Body）**：同 15.4，字段可选（`permissionCodes` 传数组时全量替换）。

**响应**：返回更新后的预设。

**业务说明**：内置预设（`isBuiltin=true`）仅可更新 `description`，不可修改 `permissionCodes`。

**关联数据表**：`role_presets`、`role_preset_permissions`

### 15.6 GET `/api/v1/admin/users/:id/permissions` - 用户权限覆盖

**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`
**所需权限码**：`permission:read`

**路径参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | string | 是 | 用户ID |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "accountId": "res_admin001",
    "accountName": "张三",
    "appliedPreset": {
      "id": "rp_001",
      "name": "统筹",
      "code": "coordinator"
    },
    "overrides": [
      { "permissionCode": "merchant:audit", "granted": true, "source": "custom", "operatorId": "res_admin002", "updatedAt": "2026-06-30T12:00:00.000Z" },
      { "permissionCode": "coin:freeze", "granted": false, "source": "custom", "operatorId": "res_admin002", "updatedAt": "2026-06-30T12:00:00.000Z" }
    ],
    "effectivePermissions": ["announcement:publish", "rank:drag", "merchant:audit", "report:view"],
    "effectivePermissionCount": 36,
    "updatedAt": "2026-06-30T12:00:00.000Z"
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

**业务说明**：`effectivePermissions` = 预设权限 ∪ `granted=true` 自定义权限 − `granted=false` 自定义权限。

**关联数据表**：`user_permissions`、`role_presets`、`role_preset_permissions`、`permissions`

### 15.7 POST `/api/v1/admin/users/:id/permissions/grant` - 授权

**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`
**所需权限码**：`permission:manage`

**路径参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | string | 是 | 被授权用户ID |

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `permissionCodes` | array | 是 | 权限代码数组 |
| `reason` | string | 否 | 授权原因 |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "accountId": "res_admin001",
    "grantedCount": 2,
    "grantedCodes": ["merchant:audit", "report:view"],
    "operatorId": "res_admin002",
    "createdAt": "2026-07-01T10:00:00.000Z"
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

**业务说明**：写入 `user_permissions`（`granted=true`），同时记录 `permission_change_logs`（`action=grant`）。`property_admin` 不能授权平台级权限。

**关联数据表**：`user_permissions`、`permission_change_logs`

### 15.8 POST `/api/v1/admin/users/:id/permissions/revoke` - 收回

**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`
**所需权限码**：`permission:manage`

**路径参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | string | 是 | 被收回权限的用户ID |

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `permissionCodes` | array | 是 | 权限代码数组 |
| `reason` | string | 否 | 收回原因 |

**响应**：返回收回结果（结构同 15.7，`grantedCount` 改为 `revokedCount`）。

**业务说明**：写入 `user_permissions`（`granted=false`，覆盖预设）。同时记录 `permission_change_logs`（`action=revoke`）。

**关联数据表**：`user_permissions`、`permission_change_logs`

### 15.9 GET `/api/v1/admin/permission-change-logs` - 权限变更日志

**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`
**所需权限码**：`permission:read`

**请求参数（Query）**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `page` | number | 否 | 当前页 |
| `pageSize` | number | 否 | 每页条数 |
| `residentId` | string | 否 | 被操作用户筛选 |
| `operatorId` | string | 否 | 操作人筛选 |
| `action` | string | 否 | `grant`/`revoke`/`apply_preset` |
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
        "id": "pcl_y7z8a9",
        "residentId": "res_admin001",
        "residentName": "张三",
        "operatorId": "res_admin002",
        "operatorName": "李四",
        "action": "grant",
        "permissionCode": "merchant:audit",
        "presetCode": null,
        "beforeValue": "false",
        "afterValue": "true",
        "reason": "岗位调整",
        "createdAt": "2026-07-01T10:00:00.000Z"
      }
    ],
    "pagination": { "page": 1, "pageSize": 20, "total": 56, "totalPages": 3 }
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

**关联数据表**：`permission_change_logs`

---

## 16. 统筹/板块/个体负责人接口

> 物业管理员管理统筹、板块负责人、个体负责人的任命/撤销/收益统计，以及查询分成记录与统计。

### 16.1 接口清单

| 方法 | 路径 | 名称 | 鉴权 | 允许角色 | 权限码 |
|------|------|------|------|----------|--------|
| GET | `/api/v1/admin/coordinators` | 统筹列表 | Required | platform_admin, property_admin | `coordinator:read` |
| POST | `/api/v1/admin/coordinators` | 任命统筹 | Required | platform_admin, property_admin | `coordinator:manage` |
| GET | `/api/v1/admin/coordinators/:id` | 统筹详情 | Required | platform_admin, property_admin | `coordinator:read` |
| PUT | `/api/v1/admin/coordinators/:id` | 更新统筹 | Required | platform_admin, property_admin | `coordinator:manage` |
| DELETE | `/api/v1/admin/coordinators/:id` | 撤销统筹 | Required | platform_admin, property_admin | `coordinator:manage` |
| GET | `/api/v1/admin/sector-leaders` | 板块负责人列表 | Required | platform_admin, property_admin | `sector:read` |
| POST | `/api/v1/admin/sector-leaders` | 任命板块负责人 | Required | platform_admin, property_admin | `sector:manage` |
| PUT | `/api/v1/admin/sector-leaders/:id` | 更新板块负责人 | Required | platform_admin, property_admin | `sector:manage` |
| DELETE | `/api/v1/admin/sector-leaders/:id` | 撤销板块负责人 | Required | platform_admin, property_admin | `sector:manage` |
| GET | `/api/v1/admin/individual-leaders` | 个体负责人列表 | Required | platform_admin, property_admin, sector_leader | `individual:read` |
| POST | `/api/v1/admin/individual-leaders` | 任命个体负责人 | Required | platform_admin, property_admin, sector_leader | `individual:manage` |
| PUT | `/api/v1/admin/individual-leaders/:id` | 更新个体负责人 | Required | platform_admin, property_admin, sector_leader | `individual:manage` |
| DELETE | `/api/v1/admin/individual-leaders/:id` | 撤销个体负责人 | Required | platform_admin, property_admin, sector_leader | `individual:manage` |
| GET | `/api/v1/admin/distribution-records` | 分成记录查询 | Required | platform_admin, property_admin | `distribution:read` |
| GET | `/api/v1/admin/distribution/statistics` | 分成统计 | Required | platform_admin, property_admin | `distribution:read` |

### 16.2 POST `/api/v1/admin/coordinators` - 任命统筹

**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`
**所需权限码**：`coordinator:manage`

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `residentId` | string | 是 | 业主ID |
| `commissionRate` | number | 否 | 分成比例（默认取物业公司 `coordinator_share_rate=0.70`） |
| `startDate` | string | 是 | 任职开始日期 `YYYY-MM-DD` |
| `endDate` | string | 否 | 任职结束日期 |
| `remark` | string | 否 | 备注 |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "coo_b1c2d3",
    "residentId": "res_003",
    "residentName": "钱七",
    "commissionRate": 0.70,
    "status": "active",
    "createdAt": "2026-02-20T10:00:00.000Z"
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

**业务说明**：同一住户在同一物业公司仅可成为一位统筹（`uk_resident_property` 唯一约束）。任命后自动应用"统筹"角色预设。

**关联数据表**：`coordinators`、`residents`

### 16.3 GET `/api/v1/admin/coordinators` - 统筹列表

**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`
**所需权限码**：`coordinator:read`

**请求参数（Query）**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `page` | number | 否 | 当前页 |
| `pageSize` | number | 否 | 每页条数 |
| `keyword` | string | 否 | 姓名/手机号搜索 |
| `status` | string | 否 | `active`/`inactive` |
| `propertyCompanyId` | string | 否 | 仅 platform_admin 可指定 |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "coo_b1c2d3",
        "residentId": "res_003",
        "name": "钱七",
        "phone": "13800007777",
        "commissionRate": 0.70,
        "sectorLeaderCount": 3,
        "individualLeaderCount": 28,
        "totalEarnings": 89000.00,
        "status": "active",
        "appointedAt": "2026-02-20T10:00:00.000Z"
      }
    ],
    "pagination": { "page": 1, "pageSize": 20, "total": 5, "totalPages": 1 }
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

**关联数据表**：`coordinators`、`residents`、`distribution_records`（统计收益）

### 16.4 PUT `/api/v1/admin/coordinators/:id` - 更新统筹

**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`
**所需权限码**：`coordinator:manage`

**路径参数**：`id`（统筹ID）

**请求参数（Body）**：同 16.2，字段可选（`commissionRate`、`endDate`、`status`、`remark`）。

**响应**：返回更新后的统筹对象。

**关联数据表**：`coordinators`

### 16.5 DELETE `/api/v1/admin/coordinators/:id` - 撤销统筹

**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`
**所需权限码**：`coordinator:manage`

**请求参数（Query）**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `reason` | string | 是 | 撤销原因 |

**响应**：返回撤销结果（`status=inactive`）。

**业务说明**：撤销后该统筹不再参与后续分成，但历史分成记录保留。其下属板块负责人需重新指派统筹。

**关联数据表**：`coordinators`

### 16.6 POST `/api/v1/admin/sector-leaders` - 任命板块负责人

**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`
**所需权限码**：`sector:manage`

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `residentId` | string | 是 | 业主ID |
| `sector` | string | 是 | 板块：`elderly`/`medical`/`education`/`cleaning`/`canteen`/`pet`/`life_service` |
| `commissionRate` | number | 否 | 分成比例（默认取物业公司 `sector_leader_rate=0.10`） |
| `coordinatorId` | string | 否 | 所属统筹ID |
| `startDate` | string | 是 | 任职开始日期 |
| `endDate` | string | 否 | 任职结束日期 |
| `remark` | string | 否 | 备注 |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "sl_e4f5g6",
    "residentId": "res_001",
    "residentName": "赵六",
    "sector": "elderly",
    "sectorName": "养老板块",
    "commissionRate": 0.10,
    "coordinatorId": "coo_b1c2d3",
    "status": "active",
    "createdAt": "2026-03-10T10:00:00.000Z"
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

**业务说明**：同一住户在同一物业公司+同一板块唯一（`uk_resident_property_sector`）。任命后应用"板块负责人"角色预设。

**关联数据表**：`sector_leaders`、`coordinators`、`residents`

### 16.7 GET `/api/v1/admin/sector-leaders` - 板块负责人列表

**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`
**所需权限码**：`sector:read`

**请求参数（Query）**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `page` | number | 否 | 当前页 |
| `pageSize` | number | 否 | 每页条数 |
| `sector` | string | 否 | 板块筛选 |
| `coordinatorId` | string | 否 | 所属统筹筛选 |
| `status` | string | 否 | `active`/`inactive` |
| `propertyCompanyId` | string | 否 | 仅 platform_admin 可指定 |

**响应**：结构同 16.6 响应列表形式，含 `individualLeaderCount`、`totalEarnings` 字段。

**关联数据表**：`sector_leaders`、`residents`、`distribution_records`

### 16.8 PUT `/api/v1/admin/sector-leaders/:id` / DELETE `/api/v1/admin/sector-leaders/:id`

**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`
**所需权限码**：`sector:manage`

**请求参数**：PUT 同 16.6 字段可选；DELETE 仅需 Query 参数 `reason`（撤销原因，必填）。

**响应**：返回更新/撤销后的板块负责人对象。

**关联数据表**：`sector_leaders`

### 16.9 POST `/api/v1/admin/individual-leaders` - 任命个体负责人

**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`、`sector_leader`（仅本人板块下）
**所需权限码**：`individual:manage`

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `residentId` | string | 是 | 业主ID |
| `sectorLeaderId` | string | 是 | 所属板块负责人ID |
| `sector` | string | 是 | 板块（同板块负责人） |
| `name` | string | 否 | 个体名称（如"钢琴老师张三"） |
| `commissionRate` | number | 否 | 分成比例（默认取物业公司 `individual_leader_rate=0.00`） |
| `startDate` | string | 是 | 任职开始日期 |
| `endDate` | string | 否 | 任职结束日期 |
| `remark` | string | 否 | 备注 |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "il_h7i8j9",
    "residentId": "res_002",
    "name": "钢琴老师A",
    "sectorLeaderId": "sl_e4f5g6",
    "sector": "education",
    "commissionRate": 0.05,
    "status": "active",
    "createdAt": "2026-03-15T10:00:00.000Z"
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

**业务说明**：`sector_leader` 角色仅可任命自己板块下的个体负责人。任命后应用"个体负责人"对应权限。

**关联数据表**：`individual_leaders`、`sector_leaders`、`residents`

### 16.10 GET `/api/v1/admin/individual-leaders` / PUT / DELETE

**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`、`sector_leader`（仅本人板块下）
**所需权限码**：`individual:read` / `individual:manage`

**请求参数**：
- GET Query：`page`、`pageSize`、`sectorLeaderId`、`keyword`、`status`、`sector`
- PUT Body：同 16.9 字段可选
- DELETE Query：`reason`（撤销原因，必填）

**响应**：列表/单个个体负责人对象。`sector_leader` 仅可查询/操作本人负责板块下的记录。

**关联数据表**：`individual_leaders`

### 16.11 GET `/api/v1/admin/distribution-records` - 分成记录查询

**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`
**所需权限码**：`distribution:read`

**请求参数（Query）**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `page` | number | 否 | 当前页 |
| `pageSize` | number | 否 | 每页条数 |
| `orderId` | string | 否 | 按订单筛选 |
| `merchantId` | string | 否 | 按商家筛选 |
| `coordinatorId` | string | 否 | 按统筹筛选 |
| `sectorLeaderId` | string | 否 | 按板块负责人筛选 |
| `individualLeaderId` | string | 否 | 按个体负责人筛选 |
| `startDate` | string | 否 | 起始日期 |
| `endDate` | string | 否 | 结束日期 |
| `propertyCompanyId` | string | 否 | 仅 platform_admin 可指定 |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "dr_k1l2m3",
        "orderId": "ord_e6n5q3",
        "orderNo": "ORD20260630120000001",
        "merchantId": "mch_c4d2p8",
        "merchantName": "万达超市",
        "residentId": "res_001",
        "residentName": "王五",
        "totalAmount": 35.50,
        "commissionRate": 0.10,
        "distributableAmount": 3.55,
        "propertyShare": 1.065,
        "coordinatorShare": 2.485,
        "sectorLeaderShare": 0.2485,
        "individualLeaderShare": 0.00,
        "coordinatorId": "coo_b1c2d3",
        "coordinatorName": "钱七",
        "sectorLeaderId": "sl_e4f5g6",
        "sectorLeaderName": "赵六",
        "createdAt": "2026-06-30T12:00:00.000Z"
      }
    ],
    "pagination": { "page": 1, "pageSize": 20, "total": 28560, "totalPages": 1428 }
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

**关联数据表**：`distribution_records`、`orders`、`merchants`、`residents`

### 16.12 GET `/api/v1/admin/distribution/statistics` - 分成统计

**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`
**所需权限码**：`distribution:read`

**请求参数（Query）**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `startDate` | string | 否 | 起始日期 |
| `endDate` | string | 否 | 结束日期 |
| `dimension` | string | 否 | `summary`/`detail`（默认 summary） |
| `propertyCompanyId` | string | 否 | 仅 platform_admin 可指定 |

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
      { "coordinatorId": "coo_b1c2d3", "name": "钱七", "amount": 90265.00 }
    ],
    "bySector": [
      { "sector": "elderly", "sectorName": "养老板块", "amount": 3500.00 }
    ]
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

**关联数据表**：`distribution_records`

---

## 17. 物业公司管理接口（平台管理员）

> 平台管理员创建/查询/更新/停用物业公司，更新分成比例、配送、提现等核心配置。

### 17.1 接口清单

| 方法 | 路径 | 名称 | 鉴权 | 允许角色 | 权限码 |
|------|------|------|------|----------|--------|
| GET | `/api/v1/admin/property-companies` | 物业公司列表 | Required | platform_admin | `property_company:read` |
| POST | `/api/v1/admin/property-companies` | 创建物业公司 | Required | platform_admin | `property_company:create` |
| GET | `/api/v1/admin/property-companies/:id` | 物业公司详情 | Required | platform_admin, property_admin | `property_company:read` |
| PUT | `/api/v1/admin/property-companies/:id` | 更新物业公司 | Required | platform_admin | `property_company:update` |
| PATCH | `/api/v1/admin/property-companies/:id/status` | 启用/停用 | Required | platform_admin | `property_company:update` |
| PATCH | `/api/v1/admin/property-companies/:id/config` | 更新配置 | Required | platform_admin, property_admin | `config:set` |

### 17.2 GET `/api/v1/admin/property-companies` - 物业公司列表

**鉴权**：Required
**允许角色**：`platform_admin`
**所需权限码**：`property_company:read`

**请求参数（Query）**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `page` | number | 否 | 当前页 |
| `pageSize` | number | 否 | 每页条数 |
| `keyword` | string | 否 | 公司名称模糊搜索 |
| `status` | string | 否 | `active`/`inactive` |
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

**关联数据表**：`property_companies`、`communities`、`merchants`、`residents`、`point_pools`

### 17.3 POST `/api/v1/admin/property-companies` - 创建物业公司

**鉴权**：Required
**允许角色**：`platform_admin`
**所需权限码**：`property_company:create`

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

**业务说明**：创建时自动初始化 `point_pools` 记录、默认积分规则、默认配送规则、默认角色预设。事务执行，任一步失败全部回滚。

**关联数据表**：`property_companies`、`communities`、`residents`、`point_pools`、`point_rules`、`delivery_rules`、`role_presets`

### 17.4 GET `/api/v1/admin/property-companies/:id` - 物业公司详情

**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`（仅本人所属公司）
**所需权限码**：`property_company:read`

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

**业务说明**：`property_admin` 调用时 `id` 必须等于本人所属物业公司，否则返回 `20003 forbidden_company_access`。

**关联数据表**：`property_companies`、`communities`、`residents`

### 17.5 PUT `/api/v1/admin/property-companies/:id` - 更新物业公司

**鉴权**：Required
**允许角色**：`platform_admin`
**所需权限码**：`property_company:update`

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `name` | string | 否 | 物业公司名称 |
| `logoUrl` | string | 否 | 公司Logo |
| `contactPhone` | string | 否 | 联系电话 |
| `address` | string | 否 | 公司地址 |

**响应**：返回更新后的物业公司完整对象（结构同 17.4）。

**关联数据表**：`property_companies`

### 17.6 PATCH `/api/v1/admin/property-companies/:id/status` - 启用/停用

**鉴权**：Required
**允许角色**：`platform_admin`
**所需权限码**：`property_company:update`

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `status` | string | 是 | `active`/`inactive` |
| `reason` | string | 否 | 停用原因（停用时必填） |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": { "id": "pc_a1b2c3d4", "status": "inactive", "updatedAt": "2026-07-01T10:00:00.000Z" },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

**业务说明**：停用后该公司所有管理员账号自动失效，APP 端无法登录、消费；积分池冻结。

**关联数据表**：`property_companies`、`residents`、`point_pools`

### 17.7 PATCH `/api/v1/admin/property-companies/:id/config` - 更新配置

**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`（仅本人所属公司）
**所需权限码**：`config:set`

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `propertyShareRate` | number | 否 | 物业分成比例（0-1） |
| `coordinatorShareRate` | number | 否 | 统筹分成比例（0-1） |
| `sectorLeaderRate` | number | 否 | 板块负责人比例（从统筹中抽取） |
| `individualLeaderRate` | number | 否 | 个体负责人比例（从板块中抽取） |
| `coinDisplayEnabled` | boolean | 否 | 物业币卡片全局显示 |
| `coinIssueMode` | string | 否 | `auto`/`manual_approval` |
| `coinExpiryDays` | number | 否 | 物业币有效期天数 |
| `coinFreezeDefault` | boolean | 否 | 新用户物业币默认冻结 |
| `deliveryBaseFee` | number | 否 | 配送起步价 |
| `deliveryCourierShareRate` | number | 否 | 快递员配送费分成比例 |
| `deliveryCourierPerOrder` | number | 否 | 快递员每单报酬 |
| `withdrawalFeeRate` | number | 否 | 提现手续费率 |
| `pointToFeeRate` | number | 否 | 积分抵扣物业费比率 |
| `twoYearClearEnabled` | boolean | 否 | 2年欠费积分清零开关 |
| `neighborDailyContactLimit` | number | 否 | 邻居私聊每日限制 |
| `remark` | string | 否 | 变更备注 |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "pc_a1b2c3d4",
    "propertyShareRate": 0.30,
    "coordinatorShareRate": 0.70,
    "coinDisplayEnabled": true,
    "coinExpiryDays": 365,
    "deliveryBaseFee": 3.00,
    "withdrawalFeeRate": 0.0060,
    "updatedAt": "2026-07-01T10:00:00.000Z"
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

**业务说明**：所有配置变更需记录到操作日志和配置变更日志，便于追溯。分成比例四项之和需满足约束：`propertyShareRate + coordinatorShareRate = 1.0`，`sectorLeaderRate + individualLeaderRate ≤ 1.0`。

**关联数据表**：`property_companies`、`operation_logs`

---

## 18. 公告信息收集接口

> 公告支持信息收集功能，住户提交收集信息，管理员查看收集结果。

### 18.1 接口清单

| 方法 | 路径 | 名称 | 鉴权 | 允许角色 | 权限码 |
|------|------|------|------|----------|--------|
| POST | `/api/v1/announcements/:id/collect` | 住户提交收集信息 | Required | resident | 无 |
| GET | `/api/v1/admin/announcements/:id/collects` | 收集结果列表 | Required | platform_admin, property_admin | `notice:collect` |

### 18.2 POST `/api/v1/announcements/:id/collect` - 住户提交收集信息

**鉴权**：Required
**允许角色**：`resident`
**所需权限码**：无

**路径参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | string | 是 | 公告ID |

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `collectedData` | object | 是 | 收集字段键值对（如 `{ "building": "3栋", "consent": true, "suggestion": "同意" }`） |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "ac_n4o5p6",
    "announcementId": "ann_001",
    "residentId": "res_b7m9x1",
    "submittedAt": "2026-07-01T11:00:00.000Z"
  },
  "timestamp": "2026-07-01T11:00:00.000Z"
}
```

**业务说明**：仅 `collect_enabled=true` 的公告可提交。同一住户对同一公告仅可提交一次（`uk_announcement_resident` 唯一约束），重复提交返回 `90003 collect_already_submitted`。提交字段需符合公告 `collect_fields` 定义。

**关联数据表**：`announcement_collects`、`announcements`

### 18.3 GET `/api/v1/admin/announcements/:id/collects` - 收集结果列表

**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`
**所需权限码**：`notice:collect`

**路径参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | string | 是 | 公告ID |

**请求参数（Query）**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `page` | number | 否 | 当前页 |
| `pageSize` | number | 否 | 每页条数 |
| `export` | boolean | 否 | 是否导出（true 则返回 CSV 流） |
| `keyword` | string | 否 | 按住户姓名/手机号搜索 |

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
        "id": "ac_n4o5p6",
        "residentId": "res_001",
        "residentName": "王五",
        "phone": "13800001234",
        "building": "3栋",
        "collectedData": {
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

**业务说明**：`export=true` 时返回 CSV 流（`Content-Type: text/csv`），单次导出上限 100 万行。

**关联数据表**：`announcement_collects`、`announcements`、`residents`

---

## 19. 邻居私聊限制接口

> 住户间邻居私聊每日次数限制（默认2次/天，由物业公司配置），防止骚扰。

### 19.1 接口清单

| 方法 | 路径 | 名称 | 鉴权 | 允许角色 | 权限码 |
|------|------|------|------|----------|--------|
| GET | `/api/v1/messages/neighbor-limit` | 查询今日剩余次数 | Required | resident | 无 |
| POST | `/api/v1/messages/neighbor-chat` | 邻居私聊（受限） | Required | resident | 无 |

### 19.2 GET `/api/v1/messages/neighbor-limit` - 查询今日剩余次数

**鉴权**：Required
**允许角色**：`resident`
**所需权限码**：无

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "residentId": "res_b7m9x1",
    "dailyLimit": 2,
    "usedToday": 1,
    "remaining": 1,
    "resetAt": "2026-07-02T00:00:00.000Z"
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

**业务说明**：每日限制次数取当前住户所属物业公司的 `neighbor_daily_contact_limit` 配置。计数按自然日重置。

**关联数据表**：`neighbor_contact_limits`、`property_companies`

### 19.3 POST `/api/v1/messages/neighbor-chat` - 邻居私聊

**鉴权**：Required
**允许角色**：`resident`
**所需权限码**：无

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `toId` | string | 是 | 接收住户ID（必须为同物业公司住户） |
| `content` | string | 是 | 消息内容（文本，最长500字符） |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "msg_q7r8s9",
    "fromId": "res_b7m9x1",
    "toId": "res_t1u2v3",
    "content": "你好，请问家里有人吗？",
    "messageType": "resident_message",
    "chatType": "private",
    "usedToday": 2,
    "remaining": 0,
    "createdAt": "2026-07-01T10:00:00.000Z"
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

**业务说明**：发送前校验：①双方属同一物业公司；②发送方今日剩余次数 > 0；③接收方未屏蔽。发送成功后 `neighbor_contact_limits.contact_count` +1，并写入 `messages` 表。超出限制返回 `90004 neighbor_contact_limit_exceeded`。

**关联数据表**：`messages`、`neighbor_contact_limits`、`residents`

---

## 20. 服务/活动组订阅接口

> 支持包月/包年订阅社区服务和活动组，订阅期内享受对应权益。

### 20.1 接口清单

| 方法 | 路径 | 名称 | 鉴权 | 允许角色 | 权限码 |
|------|------|------|------|----------|--------|
| POST | `/api/v1/services/:id/subscribe` | 订阅服务 | Required | resident | 无 |
| GET | `/api/v1/services/my-subscriptions` | 我的服务订阅 | Required | resident | 无 |
| POST | `/api/v1/activity-groups/:id/subscribe` | 订阅活动组 | Required | resident | 无 |
| GET | `/api/v1/activity-groups/my-subscriptions` | 我的活动组订阅 | Required | resident | 无 |

### 20.2 POST `/api/v1/services/:id/subscribe` - 订阅服务

**鉴权**：Required
**允许角色**：`resident`
**所需权限码**：无

**路径参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | string | 是 | 服务ID |

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `subType` | string | 是 | `monthly`/`yearly` |
| `autoRenew` | boolean | 否 | 是否自动续费（默认 false） |
| `paymentMethod` | string | 是 | 支付方式：`property_coin`/`wechat`/`point` |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "ss_w8x9y0",
    "serviceId": "svc_v8y4z1",
    "serviceName": "每月保洁服务",
    "residentId": "res_b7m9x1",
    "subType": "monthly",
    "amount": 200.00,
    "startDate": "2026-07-01",
    "endDate": "2026-07-31",
    "autoRenew": false,
    "status": "active",
    "createdAt": "2026-07-01T10:00:00.000Z"
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

**业务说明**：仅 `is_subscription=true` 的服务可订阅。订阅期间自动享受服务，到期后 `status=expired`。同一服务+住户同时仅可有一个 `active` 订阅。支付成功后写入 `service_subscriptions`。

**关联数据表**：`service_subscriptions`、`community_services`、`property_coin_records`（物业币支付时）

### 20.3 GET `/api/v1/services/my-subscriptions` - 我的服务订阅

**鉴权**：Required
**允许角色**：`resident`
**所需权限码**：无

**请求参数（Query）**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `page` | number | 否 | 当前页 |
| `pageSize` | number | 否 | 每页条数 |
| `status` | string | 否 | `active`/`expired`/`cancelled` |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "ss_w8x9y0",
        "serviceId": "svc_v8y4z1",
        "serviceName": "每月保洁服务",
        "serviceCover": "https://cdn.example.com/service/001.jpg",
        "subType": "monthly",
        "amount": 200.00,
        "startDate": "2026-07-01",
        "endDate": "2026-07-31",
        "autoRenew": false,
        "status": "active",
        "daysRemaining": 30,
        "createdAt": "2026-07-01T10:00:00.000Z"
      }
    ],
    "pagination": { "page": 1, "pageSize": 20, "total": 3, "totalPages": 1 }
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

**关联数据表**：`service_subscriptions`、`community_services`

### 20.4 POST `/api/v1/activity-groups/:id/subscribe` - 订阅活动组

**鉴权**：Required
**允许角色**：`resident`
**所需权限码**：无

**路径参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | string | 是 | 活动组ID |

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `subType` | string | 是 | `monthly`/`yearly` |
| `autoRenew` | boolean | 否 | 是否自动续费（默认 false） |
| `paymentMethod` | string | 是 | 支付方式：`property_coin`/`wechat`/`point` |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "ags_z1a2b3",
    "groupId": "act_x1y2z3",
    "groupName": "万达健身操队",
    "residentId": "res_b7m9x1",
    "subType": "monthly",
    "amount": 30.00,
    "startDate": "2026-07-01",
    "endDate": "2026-07-31",
    "autoRenew": false,
    "status": "active",
    "createdAt": "2026-07-01T10:00:00.000Z"
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

**业务说明**：订阅后自动加入活动组成员（`activity_group_members`），享受活动组权益。月费/年费取活动组配置。

**关联数据表**：`activity_group_subscriptions`、`activity_groups`、`activity_group_members`

### 20.5 GET `/api/v1/activity-groups/my-subscriptions` - 我的活动组订阅

**鉴权**：Required
**允许角色**：`resident`
**所需权限码**：无

**请求参数（Query）**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `page` | number | 否 | 当前页 |
| `pageSize` | number | 否 | 每页条数 |
| `status` | string | 否 | `active`/`expired`/`cancelled` |

**响应**：结构同 20.4 响应列表形式，含 `groupCover`、`daysRemaining` 字段。

**关联数据表**：`activity_group_subscriptions`、`activity_groups`

---

## 21. 操作日志接口

> 查询系统操作日志，便于审计和问题排查。

### 21.1 接口清单

| 方法 | 路径 | 名称 | 鉴权 | 允许角色 | 权限码 |
|------|------|------|------|----------|--------|
| GET | `/api/v1/admin/operation-logs` | 操作日志查询 | Required | platform_admin, property_admin | `log:read` |

### 21.2 GET `/api/v1/admin/operation-logs` - 操作日志查询

**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`
**所需权限码**：`log:read`

**请求参数（Query）**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `page` | number | 否 | 当前页 |
| `pageSize` | number | 否 | 每页条数 |
| `operatorId` | string | 否 | 按操作人筛选 |
| `module` | string | 否 | 按模块筛选（`merchant`/`resident`/`point_pool`/`permission` 等） |
| `action` | string | 否 | 按操作类型筛选（如 `create`/`update`/`delete`/`audit`） |
| `targetType` | string | 否 | 按目标资源类型筛选 |
| `targetId` | string | 否 | 按目标资源ID筛选 |
| `startDate` | string | 否 | 起始日期 |
| `endDate` | string | 否 | 结束日期 |
| `propertyCompanyId` | string | 否 | 仅 platform_admin 可指定 |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "ol_c4d5e6",
        "operatorId": "res_admin001",
        "operatorName": "张三",
        "operatorRole": "property_admin",
        "propertyCompanyId": "pc_a1b2c3d4",
        "propertyCompanyName": "万达物业",
        "module": "permission",
        "action": "grant",
        "targetType": "user_permission",
        "targetId": "res_admin002",
        "targetDescription": "为李四授予 merchant:audit 权限",
        "beforeData": { "permissions": ["report:view"] },
        "afterData": { "permissions": ["report:view", "merchant:audit"] },
        "ipAddress": "192.168.1.100",
        "userAgent": "Mozilla/5.0 ...",
        "createdAt": "2026-07-01T10:00:00.000Z"
      }
    ],
    "pagination": { "page": 1, "pageSize": 20, "total": 5680, "totalPages": 284 }
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

**业务说明**：`property_admin` 仅可查询本物业公司操作日志；`platform_admin` 可查询全部。日志保留 180 天，超期自动归档。所有写操作（POST/PUT/PATCH/DELETE）自动记录日志。

**关联数据表**：`operation_logs`、`residents`、`property_companies`

---

## 22. 2年欠费清零接口

> 物业费欠费超过2年的住户，其积分清零并入积分池（物业公司配置开关控制）。

### 22.1 接口清单

| 方法 | 路径 | 名称 | 鉴权 | 允许角色 | 权限码 |
|------|------|------|------|----------|--------|
| POST | `/api/v1/admin/point-clear/run` | 手动触发2年清零 | Required | platform_admin, property_admin | `point:clear_run` |
| GET | `/api/v1/admin/point-clear/records` | 清零记录 | Required | platform_admin, property_admin | `point:clear_run` |

### 22.2 POST `/api/v1/admin/point-clear/run` - 手动触发2年清零

**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`
**所需权限码**：`point:clear_run`

**请求参数（Body）**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `clearDate` | string | 是 | 清零基准日期 `YYYY-MM-DD`（清零此日期前2年未缴费的住户） |
| `dryRun` | boolean | 否 | 是否试运行（默认 false，试运行不实际清零） |
| `residentIds` | array | 否 | 指定住户ID列表（不传则全部符合条件住户） |
| `propertyCompanyId` | string | 否 | 仅 platform_admin 可指定 |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "taskId": "task_pclear_001",
    "clearDate": "2026-07-01",
    "lookbackDate": "2024-07-01",
    "dryRun": false,
    "processedCount": 28,
    "totalClearedPoints": 156800,
    "clearedToPool": 1568.00,
    "status": "completed",
    "executedAt": "2026-07-01T03:00:00.000Z"
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

**业务说明**：异步执行。仅当物业公司 `two_year_clear_enabled=true` 时可执行。清零对象为截至 `clearDate` 前 2 年（`lookbackDate = clearDate - 2年`）仍有未缴物业费的住户。清零的积分按 `pointToFeeRate`（100积分=1元则0.01）折算为元并入积分池（`point_pool_records.source=fee_clear`）。试运行模式仅返回预计结果，不实际变更数据。

**关联数据表**：`point_records`、`point_pool_records`、`point_pools`、`property_fees`、`residents`、`property_companies`

### 22.3 GET `/api/v1/admin/point-clear/records` - 清零记录

**鉴权**：Required
**允许角色**：`platform_admin`、`property_admin`
**所需权限码**：`point:clear_run`

**请求参数（Query）**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `page` | number | 否 | 当前页 |
| `pageSize` | number | 否 | 每页条数 |
| `residentId` | string | 否 | 按住户筛选 |
| `startDate` | string | 否 | 起始日期 |
| `endDate` | string | 否 | 结束日期 |
| `propertyCompanyId` | string | 否 | 仅 platform_admin 可指定 |

**响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "pt_f1h8w7",
        "residentId": "res_001",
        "residentName": "王五",
        "clearedPoints": 5600,
        "equivalentAmount": 56.00,
        "poolInAmount": 56.00,
        "lookbackStartDate": "2024-07-01",
        "lookbackEndDate": "2026-07-01",
        "unpaidFeeCount": 3,
        "unpaidFeeTotalAmount": 840.00,
        "operatorId": "res_admin001",
        "createdAt": "2026-07-01T03:00:00.000Z"
      }
    ],
    "pagination": { "page": 1, "pageSize": 20, "total": 28, "totalPages": 2 }
  },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

**业务说明**：清零记录通过 `point_records`（`point_type=expire`、`source=fee_clear`）查询。响应字段为聚合视图（含住户信息、欠费情况、清零金额、入池金额）。

**关联数据表**：`point_records`、`point_pool_records`、`property_fees`、`residents`

---

## 23. 新增接口权限矩阵补充

> 以下为本文件第 8-22 章新增接口的权限矩阵补充，与第 4 章既有矩阵合并使用。

| 模块 | 接口前缀 | 允许角色 | 权限码 |
|------|----------|----------|--------|
| 物业公司配置（住户端） | `/property-companies/current` | resident, merchant, courier, activity_leader, technician | 无 |
| 平台级商家库 | `/admin/platform-merchants` | platform_admin | `merchant:read`、`merchant:create`、`merchant:update` |
| 商家挂接 | `POST /merchants` | platform_admin, property_admin | `merchant:bind` |
| 线下积分 | `/admin/offline-points` | platform_admin, property_admin | `points:input` |
| 商家积分购买/赠送 | `/merchant/points/*` | merchant | `merchant:points_purchase`、`merchant:points_grant` |
| 商家提现 | `/merchant/withdrawals` | merchant | `merchant:withdraw` |
| 商家审核（购买/提现） | `/admin/merchant-point-purchases/:id/audit`、`/admin/merchant-withdrawals/:id/audit` | platform_admin, property_admin | `merchant:audit` |
| 物业币兑换 | `/coin-withdrawals` | merchant（申请）、platform_admin, property_admin（审核） | `coin:withdraw`、`coin:audit` |
| 物业币管控 | `/admin/residents/:id/coin/*`、`/admin/coin-expiry/*` | platform_admin, property_admin | `coin:freeze`、`coin:expiry_run` |
| 物业币卡片隐藏 | `/residents/me/coin/hide` | resident | 无 |
| 积分池 | `/admin/point-pools` | platform_admin, property_admin | `pool:read`、`pool:adjust` |
| 权限管理 | `/admin/permissions`、`/admin/role-presets`、`/admin/users/:id/permissions/*`、`/admin/permission-change-logs` | platform_admin, property_admin | `permission:read`、`permission:manage` |
| 统筹管理 | `/admin/coordinators` | platform_admin, property_admin | `coordinator:read`、`coordinator:manage` |
| 板块负责人 | `/admin/sector-leaders` | platform_admin, property_admin | `sector:read`、`sector:manage` |
| 个体负责人 | `/admin/individual-leaders` | platform_admin, property_admin, sector_leader | `individual:read`、`individual:manage` |
| 分成查询 | `/admin/distribution-records`、`/admin/distribution/statistics` | platform_admin, property_admin | `distribution:read` |
| 物业公司管理 | `/admin/property-companies` | platform_admin（部分接口允许 property_admin） | `property_company:read`/`create`/`update`、`config:set` |
| 公告信息收集 | `/announcements/:id/collect`、`/admin/announcements/:id/collects` | resident（提交）、platform_admin, property_admin（查看） | `notice:collect`（管理员） |
| 邻居私聊 | `/messages/neighbor-limit`、`/messages/neighbor-chat` | resident | 无 |
| 服务订阅 | `/services/:id/subscribe`、`/services/my-subscriptions` | resident | 无 |
| 活动组订阅 | `/activity-groups/:id/subscribe`、`/activity-groups/my-subscriptions` | resident | 无 |
| 操作日志 | `/admin/operation-logs` | platform_admin, property_admin | `log:read` |
| 2年欠费清零 | `/admin/point-clear/*` | platform_admin, property_admin | `point:clear_run` |

---

## 24. 新增接口关联数据表索引

> 便于后端开发定位接口与数据表的对应关系。

| 数据表 | 关联接口章节 |
|--------|--------------|
| `property_companies` | 8、9.5、11.5、12.2、13.5、17、19.2、22.2 |
| `communities` | 17.2、17.3、17.4 |
| `residents` | 8、10.2、11.4、13.2、13.4、15.6、15.7、15.8、16.2、19.3、20、22.2 |
| `families` | 10.2 |
| `platform_merchants` | 9.2、9.3、9.4、9.5 |
| `merchants` | 9.2、9.5、11.2、11.3、11.4、11.5、11.6、11.7、11.8、12.2、12.3、12.4、16.11 |
| `merchant_property_configs` | 9.5 |
| `point_records` | 10.2、10.3、11.4、22.2、22.3 |
| `point_pools` | 13.5、14.2、14.4、17.3、22.2 |
| `point_pool_records` | 13.5、14.3、14.4、22.2、22.3 |
| `property_coin_records` | 13.5、20.2 |
| `coin_freeze_records` | 13.2、13.3 |
| `coin_expiry_records` | 13.5、13.6 |
| `coin_withdrawals` | 12.2、12.3、12.4 |
| `permissions` | 15.2、15.6 |
| `role_presets` | 15.3、15.4、15.5、15.6 |
| `role_preset_permissions` | 15.3、15.4、15.5 |
| `user_permissions` | 15.6、15.7、15.8 |
| `permission_change_logs` | 15.7、15.8、15.9 |
| `coordinators` | 16.2、16.3、16.4、16.5 |
| `sector_leaders` | 16.6、16.7、16.8、16.9 |
| `individual_leaders` | 16.9、16.10 |
| `distribution_records` | 16.3、16.7、16.11、16.12 |
| `announcements` | 18.2、18.3 |
| `announcement_collects` | 18.2、18.3 |
| `messages` | 19.3 |
| `neighbor_contact_limits` | 19.2、19.3 |
| `community_services` | 20.2、20.3 |
| `service_subscriptions` | 20.2、20.3 |
| `activity_groups` | 20.4、20.5 |
| `activity_group_members` | 20.4 |
| `activity_group_subscriptions` | 20.4、20.5 |
| `merchant_point_purchases` | 11.2、11.3、11.4、11.7 |
| `merchant_point_grants` | 11.4 |
| `merchant_withdrawals` | 11.5、11.6、11.8 |
| `operation_logs` | 10.2、14.4、15.7、17.7、21.2 |
| `property_fees` | 22.2、22.3 |

---

## 25. 新增枚举值补充

### 25.1 线下积分来源 (OfflinePointSource)
```typescript
MANUAL: 'manual'                  // 手动发放
OFFLINE_CONSUME: 'offline_consume' // 线下消费返积分
ACTIVITY_REWARD: 'activity_reward' // 活动奖励
```

### 25.2 商家积分购买审核状态 (PurchaseAuditStatus)
```typescript
PENDING: 'pending'    // 待审核
APPROVED: 'approved'  // 已通过
REJECTED: 'rejected'  // 已拒绝
```

### 25.3 提现/兑换审核状态 (WithdrawalAuditStatus)
```typescript
PENDING: 'pending'      // 待审核
APPROVED: 'approved'    // 审核通过（待打款）
REJECTED: 'rejected'    // 审核拒绝
COMPLETED: 'completed'  // 已完成（已打款）
```

### 25.4 物业币冻结动作 (CoinFreezeAction)
```typescript
FREEZE: 'freeze'       // 冻结
UNFREEZE: 'unfreeze'   // 解冻
```

### 25.5 积分池记录类型 (PointPoolRecordType)
```typescript
POOL_IN: 'pool_in'    // 进池
POOL_OUT: 'pool_out'  // 出池
```

### 25.6 积分池记录来源 (PointPoolSource)
```typescript
EXCHANGE_DIFF: 'exchange_diff'  // 兑换比例差额
COIN_EXPIRED: 'coin_expired'    // 物业币过期清零
FEE_CLEAR: 'fee_clear'          // 2年欠费清零
SPEND: 'spend'                  // 动用积分池
MANUAL_ADJUST: 'manual_adjust'  // 手动调整
```

### 25.7 权限变更动作 (PermissionChangeAction)
```typescript
GRANT: 'grant'                // 授权
REVOKE: 'revoke'              // 收回
APPLY_PRESET: 'apply_preset'  // 应用预设
```

### 25.8 板块类型 (SectorType)
```typescript
ELDERLY: 'elderly'              // 养老
MEDICAL: 'medical'              // 医疗
EDUCATION: 'education'          // 教育
CLEANING: 'cleaning'            // 保洁
CANTEEN: 'canteen'              // 食堂
PET: 'pet'                      // 宠物
LIFE_SERVICE: 'life_service'    // 生活服务
```

### 25.9 订阅类型 (SubscriptionType)
```typescript
MONTHLY: 'monthly'  // 包月
YEARLY: 'yearly'    // 包年
```

### 25.10 订阅状态 (SubscriptionStatus)
```typescript
ACTIVE: 'active'        // 生效中
EXPIRED: 'expired'      // 已过期
CANCELLED: 'cancelled'  // 已取消
```

### 25.11 物业币发放模式 (CoinIssueMode)
```typescript
AUTO: 'auto'                      // 自动发放
MANUAL_APPROVAL: 'manual_approval' // 人工审核发放
```

### 25.12 支付方式 (PaymentMethod)
```typescript
PROPERTY_COIN: 'property_coin'  // 物业币
POINT: 'point'                  // 积分
WECHAT: 'wechat'                // 微信支付
ALIPAY: 'alipay'                // 支付宝
BANK_TRANSFER: 'bank_transfer'  // 银行转账
MIXED: 'mixed'                  // 混合支付
```

---

**文档版本**：v1.1
**最后更新**：2026-07-01
**适用项目**：物业积分服务系统（小程序端 + 管理端）
**新增章节**：第 8-25 章（共 18 个新章节，覆盖 15 个接口模块）
**新增接口数**：60+ 个（含管理端审核、CRUD、查询、动作类接口）
