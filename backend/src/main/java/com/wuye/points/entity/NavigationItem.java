package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 手机端导航栏配置项（后台可配置底部/顶部导航）。
 * property_company_id 为空表示平台级默认导航。
 */
@Data
@Entity
@Table(name = "navigation_items")
@EqualsAndHashCode(callSuper = true)
public class NavigationItem extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    /** 位置 bottom / top */
    @Column(name = "position", length = 20, nullable = false)
    private String position;

    /** 所属模块 home(首页)/service(服务)/activity(活动组)/resident_merchant(业主商家) */
    @Column(name = "module", length = 30, nullable = false)
    private String module;

    @Column(name = "title", length = 50, nullable = false)
    private String title;

    /** 图标（URL 或图标名） */
    @Column(name = "icon", length = 200)
    private String icon;

    /** 跳转类型 page / tab / h5 / none */
    @Column(name = "link_type", length = 20, nullable = false)
    private String linkType;

    /** 跳转目标（页面路径或链接） */
    @Column(name = "link_url", length = 200)
    private String linkUrl;

    /** 排序值，越小越靠前 */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    /** 是否启用 */
    @Column(name = "enabled", nullable = false)
    private Boolean enabled;

    /** 可见角色，逗号分隔；空=全部可见 */
    @Column(name = "visible_roles", length = 200)
    private String visibleRoles;

    /** 物业公司ID（空=平台级默认） */
    @Column(name = "property_company_id", length = 32)
    private String propertyCompanyId;
}
