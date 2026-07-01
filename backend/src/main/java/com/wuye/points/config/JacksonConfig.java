package com.wuye.points.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson 配置
 * API 输入输出使用 camelCase，数据库字段为 snake_case（由 JPA @Column 映射）
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> {
            builder.propertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);
        };
    }
}
