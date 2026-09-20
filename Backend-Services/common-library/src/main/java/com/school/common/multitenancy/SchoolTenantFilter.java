package com.school.common.multitenancy;

import java.io.IOException;
import java.util.regex.Pattern;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.pawan.share.jwt.JwtCache;
import com.pawan.share.jwt.JwtUtil;

import io.jsonwebtoken.Claims;

@Component
public class SchoolTenantFilter extends OncePerRequestFilter {
	
	private final JwtUtil jwtUtil;

	SchoolTenantFilter(JwtUtil jwtUtil) {
		this.jwtUtil = jwtUtil;
	}

    private static final Pattern VALID_SCHEMA_NAME = Pattern.compile("[A-Za-z0-9_]+");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
            //for all these requests we don't need to check for the tenant header and the default schema will be used that is school_management 
        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) || request.getRequestURI().contains("v3/api-docs")
                || request.getRequestURI().contains("swagger") || request.getRequestURI().contains("/schools/public/")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String schoolCode = request.getHeader(TenantContext.HEADER_NAME);
        
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
			schoolCode = schoolCodeFromJwtToken(token);
		}
       
        if (schoolCode == null || schoolCode.isBlank()
                || !VALID_SCHEMA_NAME.matcher(schoolCode).matches()) {
            response.sendError(
                    HttpStatus.BAD_REQUEST.value(),
                    "A valid X-School-Code header is required");
            return;
        }

        TenantContext.setTenant(schoolCode);
        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
    
    private String schoolCodeFromJwtToken(String token) {

		Claims claims = JwtCache.get(token);
		System.out.println("Claims available in cache : " + (claims != null));
		if (claims == null) {
			claims = jwtUtil.validateTokenAndGetClaim(token);
		}
		return (String) claims.get("schoolCode");
    }
}
