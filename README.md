# 物业积分服务系统 · 后端

Spring Boot 3.2 / JDK 17 / MySQL 8 / Flyway。管理端（Web）、小程序端、商家端共用这一套 REST 接口，Base URL `/api/v1`。

## 快速开始

```bash
cd backend
cp ../.env.example .env        # 填 DB 与微信相关变量
mvn spring-boot:run            # 默认 profile=dev，端口 8888，接口前缀 /api/v1
mvn test                       # 全量单测
mvn clean verify               # 额外跑 jacoco 覆盖率门禁与 enforcer，CI 用的就是这条
```

`mvn test` 不含覆盖率门禁；本地绿了不代表 CI 绿，交付前请跑 `verify`。

## 接口契约

- **`API接口文档_v8.26.md`（仓库根目录）**：文件名固定在 v8.26，**实际版本以文件首行的「版本」为准**（当前 v8.36），历史小节里的旧版本号不要当依据。
- 错误码以文档「附录二 · 错误码权威总表」为准；`backend/.../exception/ErrorCode.java` 是代码侧唯一来源，两者冲突时以代码为准并回写文档。
- `docs/角色端能力_20261103.xlsx`：角色 × 端的能力矩阵。

## 目录结构

`backend/src/main/java/com/wuye/points/`

| 包 | 职责 |
| --- | --- |
| `controller` | 137 个 REST 入口，按端与域拆分（`Admin*` 管理端、`Merchant*` 商家端、`BuildingLeader*`/`ActivityGroup*` 运营侧） |
| `service` | 152 个业务服务，事务边界在这里 |
| `payment` | 微信支付：服务商通道与平台自营直连通道双份配置、进件、分账、回调分流 |
| `support` / `util` | 幂等切面、金额与分页工具、脱敏、交易台账 SQL；`support.*` 有 60% 行 / 50% 分支的覆盖率硬门禁 |
| `dto` `entity` `repository` | 契约、JPA 实体与仓储 |
| `config` | 安全、CORS、Jackson、微信与分账配置绑定 |
| `scheduler` | 进件状态轮询、对账修复、物业币过期清零等定时任务 |
| `filter` `interceptor` | 限流、鉴权上下文、权限校验 |

数据库迁移在 `backend/src/main/resources/db/migration/`，Flyway V1–V37，**只增不改**；上线前的手工核对 SQL 单独走 `sql/` 目录，不混进迁移。

## 配置与密钥

- `application.yml` 是开发默认，`application-prod.yml` 只写环境变量占位与 fail-closed 默认值，两者都不含密钥。
- 真实值走环境变量（见 `.env.example`）。微信商户凭据目录（`*合作伙伴平台信息*/`、`1665249236/`、`APIv3*.txt`、`*.pem`、`*.p12`）已在 `.gitignore` 里按模式拦住，改名也拦得住。
- 微信支付双通道：任一侧未配齐时启动只 WARN 不打挂，真正发起支付时 fail-closed 报 `*_NOT_CONFIGURED`。启动日志的
  `[微信支付] 服务商通道 configured=…` / `[微信支付] 直连商户通道 configured=…` 两行是判断就绪与否的唯一依据，别看仓库默认值。

## 前端

管理端 Web（Vue3 + Vite + Pinia + Capacitor）在**另一个独立仓库**，不在本仓库内，本仓库只放后端与文档。
