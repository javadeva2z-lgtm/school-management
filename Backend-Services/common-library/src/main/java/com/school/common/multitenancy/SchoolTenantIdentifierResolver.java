package com.school.common.multitenancy;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SchoolTenantIdentifierResolver implements CurrentTenantIdentifierResolver<String> {

    private final String defaultSchema;

    public SchoolTenantIdentifierResolver(
            @Value("${app.tenant.default-schema:school_management}") String defaultSchema) {
        this.defaultSchema = defaultSchema;
    }

    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenant = TenantContext.getTenant();
        return tenant == null ? defaultSchema : tenant;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}
