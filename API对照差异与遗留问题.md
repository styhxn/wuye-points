# 物业积分服务系统 — API 文档对照差异与遗留问题

> **对照文档**：`API接口文档_v1.5.md`（2026-07-07）  
> **v1.5 已修复项**：活动组 ID 双前缀、`18.6` 订阅参数示例、退出后重新加入、订单 `60006` 挂接配置错误码  
> **对照代码**：`backend/`（Spring Boot，`context-path: /api/v1`）  
> **扫描日期**：2026-07-04  
> **结论摘要**：文档列出的 **212** 个 HTTP 接口在 Controller 层均有对应路径，**路径覆盖率 100%**；差异主要集中在鉴权白名单、业务规则、错误码、支付/实时通道与权限拦截实现。

---

## 一、总体结论

| 维度 | 结论 |
|------|------|
| HTTP 接口路径 | 文档 212 个 ↔ 代码 212 个，无缺失、无多余路径 |
| 鉴权（Optional） | 文档允许匿名访问的接口多于代码实现 |
| 业务规则 | 账号注销、分成时机、支付方式枚举等与文档不一致 |
| 错误码 | 文档接口级错误码与 `ErrorCode` 枚举存在冲突与缺失 |
| 限流 / 幂等 | 文档目录有入口，正文缺失；代码未实现 |
| WebSocket | 有基础骨架，路径/订阅端点/推送接线与文档不符 |
| 微信支付 | 配置了回调地址，无回调接口；支付流程直接落库为已支付 |

---

## 二、严重问题（建议优先处理）

### 2.1 账号注销与文档严重不符

**文档**（§31.1）：

- 请求体需 `confirmText = "确认注销"`，可选 `reason`
- 响应含 `requestId`、`status: pending`、`expiresAt`（15 天冷静期）
- 冷静期后才进入 `pending_deletion`，再清理数据
- 前置校验失败错误码：`30005`～`30008`（未完成订单 / 未缴物业费 / 物业币余额非 0 / 户主仍有其他成员）

**代码**（`ResidentService.requestAccountDeletion`）：

- **无请求体**，不校验 `confirmText` / `reason`
- **立即**将账号设为 `disabled` 并写入 `deletedAt`
- 响应仅有 `residentId` / `status` / `message`，无冷静期字段
- **未做**订单、物业费、物业币、户主成员等前置校验

影响：按文档联调会失败；生产上可绕过业务约束直接销户。

---

### 2.2 支付方式枚举不一致（`coin` vs `property_coin`）

| 来源 | 取值 |
|------|------|
| 文档 `PaymentMethod` | `point` / **`coin`** / `wechat` / `mixed` |
| 代码 `RoleGuard.PAYMENT_METHODS` | `point` / **`property_coin`** / `wechat` / `mixed` |
| 部分 DTO `@Schema(allowableValues)` | 仍写 `coin`（与实现矛盾） |

按文档传 `"paymentMethod": "coin"` 会得到 `10002`（参数无效）。订单、物业费、社区服务、活动组订阅均受影响。

---

### 2.3 微信支付未真正对接

- `application.yml` 配置了 `notify-url: .../payment/wechat/notify`
- **不存在** `/payment/wechat/notify`（或同类）Controller
- `OrderService.executePayment` 仅扣积分/物业币；`wechat` / `mixed` 中的 `cashAmount` **不调用微信 SDK**，订单直接变为 `paid`
- 开发环境 `app.payment.skip-sdk: true`，生产配置虽关闭 skip，但代码侧无支付 SDK 调用点

影响：文档中的微信支付、回调确认流程在实现中缺失；现金部分等于“空付即成功”。

---

### 2.4 分成生成时机与文档流程不一致

**文档业务流程**：确认收货（`completed`）后生成分成记录。

**代码**：`OrderService.payOrder` 在支付成功时调用 `distributionService.calculateAndSave`；`confirmOrder` **不再**生成分成。

取消订单时会 `reverseForCancelledOrder`，说明实现按“支付即分成”设计。需与产品确认以哪边为准，否则报表/分成对账会与文档预期不符。

---

### 2.5 管理端权限拦截器路径匹配失效

`PermissionInterceptor.extractAdminPath` 返回形如 `/platform-merchants`，而权限表 key 为 `GET:/admin/platform-merchants`，**永远匹配不上**。

结果：

- `platform_admin`：直接放行（唯一有效分支）
- `property_admin` 等：权限码校验**实际未执行**（仅依赖各 Service 内 `RoleGuard`）

另：`role == null` 时直接 `return true`，存在绕过风险窗口。

---

### 2.6 HTTP 状态码映射错误（鉴权类）

`GlobalExceptionHandler.getHttpStatus`：

```text
20001～20005 → 一律 UNAUTHORIZED(401)
```

其后对 `20004` 的 `FORBIDDEN` 分支**不可达**。

| 错误码 | 文档 HTTP | 代码实际 HTTP |
|--------|-----------|---------------|
| `20004` 权限不足 | 403 | **401** |
| `20005` 账号被禁用 | 403 | **401** |

前端若按文档区分 401/403，行为会错乱。

---

## 三、鉴权与安全差异

### 3.1 Optional 接口白名单不足

文档标注 **Auth: Optional**，但代码 `AuthInterceptor` **仅**放行：

- `GET /products`、`GET /products/{id}`
- `GET /merchants`、`GET /merchants/{id}`（排除 `/merchants/my`）

以下文档允许匿名、代码**强制登录**（无 Token 返回 `20001`）：

| 方法 | 路径 |
|------|------|
| GET | `/announcements`、`/announcements/{id}` |
| GET | `/property-companies`、`/property-companies/{id}`、`/property-companies/{id}/communities` |
| GET | `/services`、`/services/{id}` |
| GET | `/activity-groups`、`/activity-groups/{id}` |
| GET | `/special-offers`、`/special-offers/{id}` |

登录相关路径（wechat-login / admin-login / refresh-token）已正确排除。

### 3.2 Profile 角色范围

文档 `/auth/profile` Roles：`resident` / `property_admin` / `platform_admin`。

代码 `PROFILE_ROLES` 同样限制为上述三者；商家、快递员等角色调 profile 会失败（若产品需要这些角色登录后看资料，则遗漏）。

### 3.3 敏感配置

`application.yml` / `application-dev.yml` 中含数据库口令、JWT 默认密钥、微信 app-secret、支付密钥等默认值，存在泄露风险（与接口契约无关，但属上线遗留风险）。

---

## 四、错误码差异

### 4.1 文档有、代码 `ErrorCode` 无

| 错误码 | 文档含义 | 说明 |
|--------|----------|------|
| `17001` | 服务已下架 | 代码用其它错误码或文案代替 |
| `40003` | 无权操作（住户/家庭/商品等） | 代码统一用 `20004` |
| `71002` | 该账期已存在物业费账单 | **创建物业费未做账期唯一校验** |
| `30006` | 存在未支付物业费（注销） | 注销逻辑未实现 |
| `30007` | 物业币余额不为 0（注销） | 注销逻辑未实现 |
| `30008` | 户主家庭有其他成员（注销） | 注销逻辑未实现 |

### 4.2 同一错误码语义冲突

| 错误码 | 文档用法 | 代码用法 |
|--------|----------|----------|
| `30005` | 注销：存在未完成订单 | `RESIDENT_DUPLICATE_PHONE`（手机号在物业下已存在） |
| `30001` | 多处复用（住户/家庭/商品/服务不存在、登录失败） | 固定为「业主不存在」；登录失败也复用该码（与文档 admin-login 一致，但与其它接口文档表冲突） |

### 4.3 代码有、文档速查表未收录

代码额外定义且文档速查表未列全的包括：`30005`（手机号重复）、`60005`（商家已被踢出）、`71001` 以外的物业费相关、`90003`～`90013`、`91xxx`～`97xxx` 等——部分在接口小节有写，但**错误码速查表不完整**。

### 4.4 HTTP 映射不完整

`GlobalExceptionHandler` 未覆盖全部业务码（如 `90004`～`90011`、`93002`、`94002` 等），未命中分支会落到 **500**，与文档各接口标注的 400/403/409 不符。

---

## 五、WebSocket 差异

| 项 | 文档 | 代码 |
|----|------|------|
| 连接地址 | `wss://.../ws` 或 `/ws/v1` | 受 `context-path` 影响，实际为 **`/api/v1/ws`**（SockJS） |
| 个人队列 | `/user/queue/notifications` | `/user/queue/messages` |
| 广播 | `/topic/broadcast/{propertyCompanyId}` | `/topic/broadcast`（无物业维度） |
| 私聊 / 配送 / 订单 topic | `/topic/chat/{userId}` 等 | **未实现对应订阅端点** |
| 心跳 | 客户端 `heartbeat.ping` → 服务端 `heartbeat.pong` | `@MessageMapping("/heartbeat")`（即 `/app/heartbeat`），pong 发到 `/topic/heartbeat.pong` |
| 业务推送 | 订单状态、抢单结果、余额变更、新公告 | `WebSocketService` 有方法，但业务 Service **几乎未调用**（仅账号注销广播、订阅到期通知等少量场景） |

结论：实时通道仅为骨架，与文档消息类型表不对齐，前端按文档订阅会收不到事件。

---

## 六、时间与响应格式差异

| 项 | 文档示例 | 代码实现 |
|----|----------|----------|
| `timestamp` / 业务时间字段 | `2026-07-01 15:23:33`（空格分隔本地时间） | `Instant.toString()` / `DateTimeUtil.toIsoUtc` → **ISO-8601 UTC**（如 `2026-07-01T07:23:33Z`） |
| 统一响应 | `code` / `message` / `data` / `timestamp` | 一致；校验失败额外带 `errors` 数组（文档部分章节未强调） |
| 分页 | `page` / `pageSize` / `total` / `totalPages` | `PageResponse` 结构一致 |

前端若按文档字符串解析时间，需兼容 ISO 格式。

---

## 七、业务规则遗漏与未充分考虑

### 7.1 物业费创建

文档错误码 `71002`：同住户同账期不可重复。代码 `createPropertyFee` **无唯一性检查**，可重复建单。

### 7.2 社区服务下架

文档 `17001` 服务已下架。代码缺少独立错误码与明确下架态拦截（部分场景用通用错误）。

### 7.3 积分清零 / 物业币过期

- 管理端提供手动 `POST /admin/point-clear/run`、`POST /admin/coin-expiry/run`
- **无定时任务**自动执行清零/过期（仅有配送兜底、订阅到期提醒等 Scheduler）
- 若 PRD/文档期望“到期自动执行”，则未覆盖

### 7.4 订单取消与退款

- 已支付订单取消会退积分/物业币并冲回分成
- **微信现金部分**无退款通道（与支付未对接一致）
- 文档订单状态含 `refunding` / `refunded`，需确认代码状态机是否完整覆盖退款中路径

### 7.5 软删除范围

文档多处写“软删除”。代码明确软删字段主要见于 `Resident`、`Product` 等；其它资源是否统一软删需按模块再验。

### 7.6 限流与幂等

- 文档目录含「限流与幂等性」，**正文无对应章节**
- 代码无 RateLimiter / Idempotency-Key / Redis 限流
- 支付、抢单等并发仅部分依赖乐观锁（`ORDER_CONFLICT`）

### 7.7 实时事件未接线

订单支付/状态变更、配送抢单、积分余额变动等文档要求的 WS 推送，业务层未系统调用 `WebSocketService.sendOrderUpdate` 等。

---

## 八、文档自身问题（实现时需注意）

1. **版本号**：标题为 v0.8，文件名为 `API接口文档_v1.md`。
2. **限流与幂等**：目录有链接，正文缺失。
3. **错误码复用混乱**：同一 `30001` 在不同接口表示不同资源不存在；注销用的 `30005` 与手机号重复码冲突。
4. **WebSocket 路径自相矛盾**：正文写 `/ws`，注释又写开发环境 `/ws/v1`。
5. **分成时机**写在确认收货，与部分支付即返积分/物业币的描述需统一。

---

## 九、已对齐项（无需重复投入）

- Base path：`/api/v1`（`server.servlet.context-path`）
- 212 个 REST 路径与 HTTP Method 一一对应
- 统一响应包装 `ApiResponse`、分页 `PageResponse`
- JWT 登录 / 刷新 / 绑定手机 / profile 主流程存在
- 开发模式 `dev_` 前缀登录（`app.wechat.skip-api=true`）已实现
- 登录响应手机号脱敏（`PhoneUtil.mask`）
- 管理端模块 Controller 齐全（公告收集、提现审核、积分池、权限、仪表盘等）
- 报表导出接口存在（CSV 流式输出）

---

## 十、建议修复优先级

| 优先级 | 项 | 建议 |
|--------|----|------|
| P0 | 支付方式 `coin` / `property_coin` | 统一枚举；DTO、文档、校验三处一致 |
| P0 | 账号注销 | 按文档补请求体、冷静期、前置校验与错误码（并解决 `30005` 冲突） |
| P0 | 权限拦截器路径 | 修正 `extractAdminPath` 与 key 格式，补全权限映射 |
| P0 | `20004`/`20005` HTTP 状态 | 修正为 403 |
| P1 | Optional 鉴权白名单 | 按文档补全匿名可读接口 |
| P1 | 微信支付与回调 | 实现预下单 + `/payment/wechat/notify`，或文档标明“暂不支持微信” |
| P1 | 分成时机 | 产品确认后改代码或改文档 |
| P1 | 错误码补齐与 HTTP 映射 | 补 `71002`、`17001`、注销码；完善 `getHttpStatus` |
| P2 | WebSocket | 对齐路径/订阅端点，业务事件接线 |
| P2 | 时间格式 | 统一为文档本地时间或修订文档为 ISO-8601 |
| P2 | 定时清零/过期 | 按产品要求增加 Scheduler |
| P2 | 限流幂等 | 补文档正文并实现关键写接口幂等键 |

---

## 十一、附录：快速核对清单

- [ ] 客户端支付传 `coin` 还是 `property_coin`
- [ ] 未登录能否浏览公告 / 服务 / 活动组 / 特惠 / 物业公司
- [ ] 注销是否需冷静期与四项前置校验
- [ ] 分成记在支付还是确认收货
- [ ] 微信订单是否应停留在待支付直至回调
- [ ] 物业管理员细粒度权限是否生效
- [ ] 前端时间解析格式
- [ ] WS 连接 URL 是否带 `/api/v1` 前缀

---

*本报告基于静态代码与文档对照，未做全量接口联调与压测。修复后建议按模块做契约测试（路径 + 鉴权 + 错误码 + 关键业务规则）。*
