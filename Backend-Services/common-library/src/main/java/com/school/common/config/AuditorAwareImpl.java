package com.school.common.config;

import java.util.Optional;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.lang.Objects;

@Component("auditorProvider")
public class AuditorAwareImpl implements AuditorAware<String> {
    @Override
    public Optional<String> getCurrentAuditor() {
    	String loginUser =  SecurityContextHolder.getContext().getAuthentication().getName();
    	if(loginUser == null || Objects.isEmpty(loginUser)) {
    		loginUser = "SYSTEM-USER";
    	}
        return  Optional.of(loginUser);
    }
}
