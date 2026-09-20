package com.school.common.config;

import com.school.common.multitenancy.SchoolTenantConnectionProvider;
import com.school.common.multitenancy.SchoolTenantIdentifierResolver;

import org.hibernate.cfg.AvailableSettings;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaConfig implements HibernatePropertiesCustomizer {

    private final SchoolTenantConnectionProvider connectionProvider;
    private final SchoolTenantIdentifierResolver tenantIdentifierResolver;

    public JpaConfig(
            SchoolTenantConnectionProvider connectionProvider,
            SchoolTenantIdentifierResolver tenantIdentifierResolver) {
        this.connectionProvider = connectionProvider;
        this.tenantIdentifierResolver = tenantIdentifierResolver;
    }

    @Override
    public void customize(java.util.Map<String, Object> hibernateProperties) {
        hibernateProperties.put(AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER, connectionProvider);
        hibernateProperties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, tenantIdentifierResolver);
    }
}
