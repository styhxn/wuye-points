# Cursor AI 开发流程 - 物业积分服务系统

## 1. 开发环境配置

### 1.1 Cursor设置
```json
// .cursor/settings.json
{
  "editor.formatOnSave": true,
  "editor.defaultFormatter": "esbenp.prettier-vscode",
  "java.configuration.updateBuildConfiguration": "automatic",
  "java.jdt.ls.java.home": "${env:JAVA_HOME}",
  "editor.codeActionsOnSave": {
    "source.organizeImports": "explicit"
  }
}
```

### 1.2 项目配置文件

创建 `.cursorrules` 文件：

```
# 物业积分服务系统 - Cursor规则

## 技术栈
- Java 17 + Spring Boot
- MySQL 8.0 + JPA
- Maven构建
- Docker部署

## 代码规范
1. 使用统一响应格式 ApiResponse
2. 使用BizException处理业务异常
3. 使用SnowflakeIdGenerator生成ID
4. 字段命名：数据库snake_case，API camelCase
5. 所有金额使用BigDecimal，积分使用Integer

## 开发流程
1. 先创建Entity实体类
2. 创建Repository接口
3. 创建Service服务类
4. 创建Controller控制器
5. 创建DTO请求/响应类
6. 编写单元测试

## 禁止事项
- 不要使用System.out.println
- 不要使用@RestController返回String
- 不要在Controller中写业务逻辑
- 不要忽略异常处理
```

## 2. 模块开发流程

### 2.1 开发一个新模块的步骤

#### Step 1: 创建Entity实体类

**Cursor提示词**：
```
请为我创建 [模块名] 的Entity实体类，要求：
1. 使用JPA注解
2. 字段使用snake_case命名
3. 包含id、created_at、updated_at字段
4. 使用Lombok注解
5. 包含必要的索引注释

表结构参考：
[粘贴数据库设计文档中的表结构]
```

**示例输出**：
```java
@Entity
@Table(name = "merchants")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Merchant {
    @Id
    @Column(length = 32)
    private String id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "merchant_level", length = 30)
    private MerchantLevel merchantLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "audit_status", length = 20)
    private AuditStatus auditStatus;

    @Column(name = "property_company_id", length = 32)
    private String propertyCompanyId;

    @Column(name = "commission_rate", precision = 5, scale = 2)
    private BigDecimal commissionRate;

    @Column(name = "delivery_fee", precision = 10, scale = 2)
    private BigDecimal deliveryFee;

    @Column(name = "free_delivery_threshold", precision = 10, scale = 2)
    private BigDecimal freeDeliveryThreshold;

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "active";

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

#### Step 2: 创建Repository接口

**Cursor提示词**：
```
请为我创建 [模块名] 的Repository接口，要求：
1. 继承JpaRepository
2. 包含常用的查询方法
3. 支持分页查询
4. 包含自定义查询方法
```

**示例输出**：
```java
@Repository
public interface MerchantRepository extends JpaRepository<Merchant, String> {

    Page<Merchant> findByPropertyCompanyId(String propertyCompanyId, Pageable pageable);

    Page<Merchant> findByPropertyCompanyIdAndStatus(String propertyCompanyId, String status, Pageable pageable);

    Page<Merchant> findByPropertyCompanyIdAndAuditStatus(String propertyCompanyId, AuditStatus auditStatus, Pageable pageable);

    @Query("SELECT m FROM Merchant m WHERE m.propertyCompanyId = :propertyCompanyId AND m.name LIKE %:keyword%")
    Page<Merchant> searchByKeyword(@Param("propertyCompanyId") String propertyCompanyId, @Param("keyword") String keyword, Pageable pageable);

    long countByPropertyCompanyIdAndStatus(String propertyCompanyId, String status);

    boolean existsByNameAndPropertyCompanyId(String name, String propertyCompanyId);
}
```

#### Step 3: 创建Service服务类

**Cursor提示词**：
```
请为我创建 [模块名] 的Service服务类，要求：
1. 实现完整的业务逻辑
2. 使用@Transactional注解
3. 使用BizException处理业务异常
4. 使用SnowflakeIdGenerator生成ID
5. 包含参数校验
6. 支持分页查询

业务规则：
[描述业务规则]
```

**示例输出**：
```java
@Service
@Slf4j
public class MerchantService {

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private SnowflakeIdGenerator idGenerator;

    @Autowired
    private DistributionService distributionService;

    public Merchant create(MerchantCreateRequest request, String operatorId) {
        // 参数校验
        if (merchantRepository.existsByNameAndPropertyCompanyId(request.getName(), request.getPropertyCompanyId())) {
            throw new BizException(60002, "商家名称已存在");
        }

        // 创建商家
        Merchant merchant = Merchant.builder()
                .id(idGenerator.generateId("mch"))
                .name(request.getName())
                .description(request.getDescription())
                .merchantLevel(MerchantLevel.NORMAL)
                .auditStatus(AuditStatus.PENDING_AUDIT)
                .propertyCompanyId(request.getPropertyCompanyId())
                .commissionRate(request.getCommissionRate())
                .deliveryFee(request.getDeliveryFee())
                .freeDeliveryThreshold(request.getFreeDeliveryThreshold())
                .status("active")
                .build();

        return merchantRepository.save(merchant);
    }

    @Transactional
    public Merchant approve(String merchantId, String operatorId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));

        if (merchant.getAuditStatus() != AuditStatus.PENDING_AUDIT) {
            throw new BizException(60002, "商家状态不允许审核");
        }

        merchant.setAuditStatus(AuditStatus.APPROVED);
        return merchantRepository.save(merchant);
    }

    public Page<Merchant> list(String propertyCompanyId, String status, String keyword, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        if (StringUtils.isNotBlank(keyword)) {
            return merchantRepository.searchByKeyword(propertyCompanyId, keyword, pageable);
        }

        if (StringUtils.isNotBlank(status)) {
            return merchantRepository.findByPropertyCompanyIdAndStatus(propertyCompanyId, status, pageable);
        }

        return merchantRepository.findByPropertyCompanyId(propertyCompanyId, pageable);
    }
}
```

#### Step 4: 创建Controller控制器

**Cursor提示词**：
```
请为我创建 [模块名] 的Controller控制器，要求：
1. 使用@RestController注解
2. 使用统一响应格式ApiResponse
3. 实现RESTful风格
4. 包含参数校验
5. 包含权限校验说明
6. 支持分页查询

接口清单：
[列出接口清单]
```

**示例输出**：
```java
@RestController
@RequestMapping("/api/v1/merchants")
@Slf4j
public class MerchantController {

    @Autowired
    private MerchantService merchantService;

    /**
     * 创建商家
     * Auth: Required
     * Roles: [platform_admin]
     */
    @PostMapping
    public ApiResponse<MerchantResponse> create(
            @Valid @RequestBody MerchantCreateRequest request,
            @AuthenticationPrincipal String operatorId) {
        Merchant merchant = merchantService.create(request, operatorId);
        return ApiResponse.success(convertToResponse(merchant));
    }

    /**
     * 审核商家
     * Auth: Required
     * Roles: [property_admin, platform_admin]
     */
    @PatchMapping("/{id}/status")
    public ApiResponse<MerchantResponse> approve(
            @PathVariable String id,
            @AuthenticationPrincipal String operatorId) {
        Merchant merchant = merchantService.approve(id, operatorId);
        return ApiResponse.success(convertToResponse(merchant));
    }

    /**
     * 商家列表
     * Auth: Required
     * Roles: [property_admin, platform_admin]
     */
    @GetMapping
    public ApiResponse<PageResponse<MerchantResponse>> list(
            @RequestParam String propertyCompanyId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Page<Merchant> merchants = merchantService.list(propertyCompanyId, status, keyword, page, pageSize);
        List<MerchantResponse> responses = merchants.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ApiResponse.success(PageResponse.of(responses, page, pageSize, merchants.getTotalElements()));
    }

    private MerchantResponse convertToResponse(Merchant merchant) {
        return MerchantResponse.builder()
                .id(merchant.getId())
                .name(merchant.getName())
                .description(merchant.getDescription())
                .merchantLevel(merchant.getMerchantLevel().name())
                .auditStatus(merchant.getAuditStatus().name())
                .propertyCompanyId(merchant.getPropertyCompanyId())
                .commissionRate(merchant.getCommissionRate())
                .deliveryFee(merchant.getDeliveryFee())
                .freeDeliveryThreshold(merchant.getFreeDeliveryThreshold())
                .createdAt(merchant.getCreatedAt())
                .updatedAt(merchant.getUpdatedAt())
                .build();
    }
}
```

#### Step 5: 创建DTO类

**Cursor提示词**：
```
请为我创建 [模块名] 的DTO类，要求：
1. 请求DTO使用Validation注解
2. 响应DTO使用Lombok注解
3. 字段命名使用camelCase
4. 包含必要的字段说明

请求DTO：
[列出请求字段]

响应DTO：
[列出响应字段]
```

**示例输出**：
```java
// 请求DTO
@Data
public class MerchantCreateRequest {
    @NotBlank(message = "商家名称不能为空")
    @Size(max = 100, message = "商家名称不能超过100个字符")
    private String name;

    @Size(max = 1000, message = "商家描述不能超过1000个字符")
    private String description;

    @NotNull(message = "佣金比例不能为空")
    @DecimalMin(value = "0", message = "佣金比例不能小于0")
    @DecimalMax(value = "1", message = "佣金比例不能大于1")
    private BigDecimal commissionRate;

    @DecimalMin(value = "0", message = "配送费不能小于0")
    private BigDecimal deliveryFee;

    @DecimalMin(value = "0", message = "免配送费门槛不能小于0")
    private BigDecimal freeDeliveryThreshold;

    @NotBlank(message = "物业公司ID不能为空")
    private String propertyCompanyId;
}

// 响应DTO
@Data
@Builder
public class MerchantResponse {
    private String id;
    private String name;
    private String description;
    private String merchantLevel;
    private String auditStatus;
    private String propertyCompanyId;
    private BigDecimal commissionRate;
    private BigDecimal deliveryFee;
    private BigDecimal freeDeliveryThreshold;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

## 3. 常用Cursor提示词模板

### 3.1 开发CRUD接口

```
请为我创建 [模块名] 的完整CRUD接口，包括：
1. Entity实体类
2. Repository接口
3. Service服务类（包含增删改查业务逻辑）
4. Controller控制器（RESTful风格）
5. DTO请求/响应类
6. 错误码定义

表结构：
[粘贴SQL建表语句]

业务规则：
[描述业务规则]
```

### 3.2 开发分页查询接口

```
请为我创建 [模块名] 的分页查询接口，要求：
1. 支持多条件过滤
2. 支持关键字搜索
3. 支持排序
4. 返回分页数据

查询条件：
[列出查询条件]

排序字段：
[列出排序字段]
```

### 3.3 开发复杂业务逻辑

```
请为我实现 [业务名称] 的业务逻辑，要求：
1. 使用@Transactional保证事务
2. 处理并发情况
3. 记录操作日志
4. 使用BizException处理异常

业务规则：
[详细描述业务规则]

输入参数：
[列出输入参数]

输出结果：
[描述输出结果]
```

### 3.4 代码审查

```
请审查以下代码，检查：
1. 是否符合项目规范
2. 是否有潜在的bug
3. 是否有性能问题
4. 是否有安全隐患
5. 是否需要优化

[粘贴代码]
```

### 3.5 生成单元测试

```
请为以下Service类生成单元测试，要求：
1. 使用JUnit 5
2. 使用Mockito mock依赖
3. 覆盖正常流程和异常流程
4. 测试边界条件

[粘贴Service代码]
```

## 4. 开发检查清单

### 4.1 代码编写检查
- [ ] Entity类使用JPA注解
- [ ] Repository继承JpaRepository
- [ ] Service使用@Transactional
- [ ] Controller使用@RestController
- [ ] DTO使用Validation注解
- [ ] 字段命名符合规范（snake_case/camelCase）
- [ ] 使用统一响应格式ApiResponse
- [ ] 使用BizException处理业务异常
- [ ] 使用SnowflakeIdGenerator生成ID

### 4.2 业务逻辑检查
- [ ] 参数校验完整
- [ ] 权限校验正确
- [ ] 数据范围限制正确
- [ ] 并发处理正确
- [ ] 事务管理正确
- [ ] 异常处理完整
- [ ] 日志记录完整

### 4.3 代码质量检查
- [ ] 无重复代码
- [ ] 无硬编码
- [ ] 无魔法数字
- [ ] 方法长度适中
- [ ] 类职责单一
- [ ] 命名清晰有意义

## 5. 调试技巧

### 5.1 使用Cursor调试

```
请帮我调试以下问题：
1. 问题描述：[描述问题]
2. 错误信息：[粘贴错误信息]
3. 相关代码：[粘贴代码]
4. 已尝试的方案：[列出已尝试的方案]
```

### 5.2 性能优化

```
请帮我优化以下代码的性能：
1. 问题描述：[描述性能问题]
2. 当前代码：[粘贴代码]
3. 数据量：[描述数据量]
4. 期望性能：[描述期望性能]
```

## 6. Git提交规范

### 6.1 提交信息格式
```
<type>(<scope>): <subject>

<body>

<footer>
```

### 6.2 Type类型
- feat: 新功能
- fix: 修复bug
- docs: 文档更新
- style: 代码格式（不影响功能）
- refactor: 重构
- test: 测试
- chore: 构建/工具

### 6.3 示例
```
feat(merchant): 实现商家入驻审核功能

- 添加商家入驻申请接口
- 添加商家审核接口
- 实现商家状态流转逻辑
- 添加相关单元测试

Closes #123
```
