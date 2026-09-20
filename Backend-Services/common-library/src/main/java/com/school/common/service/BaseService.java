package com.school.common.service;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.school.common.enums.UserRole;

@Service
public class BaseService {

    public String getSchoolCodeFromRequestHeader() {
        HttpServletRequest request = getCurrentRequest();
        return request.getHeader("X-School-Code");
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = 
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        
        if (attributes == null) {
            throw new IllegalStateException("No current request bound to this thread.");
        }
        
        return attributes.getRequest();
    }

    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null; // Or throw an exception if user must be authenticated
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            return (String) principal;
        }

        return principal.toString();
    }

    public List<String> getCurrentUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Collections.emptyList(); // Or throw an exception if user must be authenticated
        }

        return authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .collect(Collectors.toList());
    }

    public boolean hasRole(UserRole role) {
        List<String> roles = getCurrentUserRoles();
        String roleWithPrefix = "ROLE_" + role.getValue(); // Assuming roles are prefixed with "ROLE_"
         if(roles.contains(roleWithPrefix) || roles.contains(role.getValue())) {
            return true;
         }
         return false;
    }
}