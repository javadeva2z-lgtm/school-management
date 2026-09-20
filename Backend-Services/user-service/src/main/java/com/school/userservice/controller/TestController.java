package com.school.userservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.school.common.response.ApiResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Test", description = "Test microservices")
public class TestController {


    @GetMapping
    public ResponseEntity<ApiResponse<String>> test() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("Test successfull..."));
    }
}
