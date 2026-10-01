package com.wuye.points.service;

import com.wuye.points.dto.propertycontact.PropertyContactDetailResponse;
import com.wuye.points.dto.propertycontact.PropertyContactPublicResponse;
import com.wuye.points.dto.propertycontact.UpsertPropertyContactRequest;
import com.wuye.points.entity.PropertyContactConfig;
import com.wuye.points.repository.PropertyContactConfigRepository;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 物业联系方式配置：全局单条，管理端可配置，前端在用户未绑定楼栋时展示
 */
@Service
@RequiredArgsConstructor
public class PropertyContactService {

    private final PropertyContactConfigRepository repository;
    private final RoleGuard roleGuard;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional(readOnly = true)
    public PropertyContactDetailResponse getConfig() {
        roleGuard.requireAnyRole("platform_admin", "property_admin");
        return repository.findFirstByOrderByIdAsc()
                .map(this::toDetail)
                .orElse(null);
    }

    @Transactional
    public PropertyContactDetailResponse upsertConfig(UpsertPropertyContactRequest request) {
        roleGuard.requireAnyRole("platform_admin", "property_admin");

        PropertyContactConfig config = repository.findFirstByOrderByIdAsc().orElseGet(() -> {
            PropertyContactConfig c = new PropertyContactConfig();
            c.setId(idGenerator.generateId("pcc"));
            return c;
        });

        config.setPropertyName(request.getPropertyName());
        config.setContactPhone(request.getContactPhone());
        config.setContactMobile(request.getContactMobile());
        config.setAddress(request.getAddress());
        config.setServiceHours(request.getServiceHours());
        config.setEmail(request.getEmail());
        config.setRemark(request.getRemark());
        config.setEnabled(request.getEnabled() != null && request.getEnabled());

        repository.save(config);
        return toDetail(config);
    }

    @Transactional(readOnly = true)
    public PropertyContactPublicResponse getPublicConfig() {
        return repository.findFirstByOrderByIdAsc()
                .filter(c -> Boolean.TRUE.equals(c.getEnabled()))
                .map(this::toPublic)
                .orElse(null);
    }

    private PropertyContactDetailResponse toDetail(PropertyContactConfig c) {
        PropertyContactDetailResponse r = new PropertyContactDetailResponse();
        r.setId(c.getId());
        r.setPropertyName(c.getPropertyName());
        r.setContactPhone(c.getContactPhone());
        r.setContactMobile(c.getContactMobile());
        r.setAddress(c.getAddress());
        r.setServiceHours(c.getServiceHours());
        r.setEmail(c.getEmail());
        r.setRemark(c.getRemark());
        r.setEnabled(c.getEnabled());
        r.setCreatedAt(c.getCreatedAt());
        r.setUpdatedAt(c.getUpdatedAt());
        return r;
    }

    private PropertyContactPublicResponse toPublic(PropertyContactConfig c) {
        PropertyContactPublicResponse r = new PropertyContactPublicResponse();
        r.setPropertyName(c.getPropertyName());
        r.setContactPhone(c.getContactPhone());
        r.setContactMobile(c.getContactMobile());
        r.setAddress(c.getAddress());
        r.setServiceHours(c.getServiceHours());
        r.setEmail(c.getEmail());
        r.setEnabled(c.getEnabled());
        return r;
    }
}
