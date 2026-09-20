package com.school.apigateway.controller;

import com.school.common.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GatewayFallbackController {
    @RequestMapping("/fallback")
    public ResponseEntity<ApiResponse<Void>> fallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error(HttpStatus.SERVICE_UNAVAILABLE.value(),
                        "The requested service is temporarily unavailable"));
    }
}
