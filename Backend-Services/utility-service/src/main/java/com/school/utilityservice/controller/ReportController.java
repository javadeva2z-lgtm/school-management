package com.school.utilityservice.controller;

import com.itextpdf.text.DocumentException;
import com.school.utilityservice.dto.ReportRequest;
import com.school.utilityservice.service.ReportService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;

@RestController @RequestMapping("/api/v1/reports") @RequiredArgsConstructor
@Tag(name = "Reports", description = "Report generation endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {
    private final ReportService service;
    @PostMapping(value = "/csv", produces = "text/csv") public ResponseEntity<byte[]> csv(@Valid @RequestBody ReportRequest request) throws IOException { return download(service.csv(request), request.getTitle() + ".csv", "text/csv"); }
    @PostMapping(value = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE) public ResponseEntity<byte[]> pdf(@Valid @RequestBody ReportRequest request) throws DocumentException { return download(service.pdf(request), request.getTitle() + ".pdf", MediaType.APPLICATION_PDF_VALUE); }
    private ResponseEntity<byte[]> download(byte[] content, String fileName, String mediaType) { return ResponseEntity.ok().contentType(MediaType.parseMediaType(mediaType)).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName.replaceAll("[^a-zA-Z0-9._-]", "_") + "\"").body(content); }
}
