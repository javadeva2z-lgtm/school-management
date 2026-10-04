package com.school.paymentservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import com.school.common.response.ApiResponse;

@FeignClient(name = "user-service", path = "/api/v1/students")
public interface StudentServiceClient {
    @GetMapping("/admission/{admissionNumber}")
    ApiResponse<StudentIdentity> getStudentByAdmissionNumber(
            @PathVariable("admissionNumber") Long admissionNumber,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization);

}
