package com.school.academicservice.service;

import org.junit.jupiter.api.AfterEach;

import com.school.common.multitenancy.TenantContext;

class HomeworkServiceStorageTest {

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

}
