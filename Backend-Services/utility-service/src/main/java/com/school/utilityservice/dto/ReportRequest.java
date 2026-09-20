package com.school.utilityservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ReportRequest {
    @NotBlank private String title;
    @NotEmpty private List<String> headers;
    private List<List<String>> rows;
}
