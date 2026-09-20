package com.school.academicservice.controller;

import com.school.academicservice.dto.AttendanceDTO;
import com.school.academicservice.service.AttendanceService;
import com.school.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Attendance", description = "Attendance management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AttendanceController {
    private final AttendanceService attendanceService;

    @PostMapping("/mark")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Mark attendance")
    public ResponseEntity<ApiResponse<String>> markAttendance(@Valid @RequestBody AttendanceDTO attendanceDTO) {
        log.info("Mark attendance request received for student: {}", attendanceDTO.getAdmissionNumber());
        attendanceService.markAttendance(attendanceDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Attendance marked successfully"));
    }
    
    @PostMapping("/mark/all")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Mark attendance")
    public ResponseEntity<ApiResponse<String>> markAttendanceForAll(@Valid @RequestBody List<AttendanceDTO> attendanceDTOs) {
       attendanceService.markAttendanceForAll(attendanceDTOs);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Attendance marked successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get attendance by ID")
    public ResponseEntity<ApiResponse<AttendanceDTO>> getAttendanceById(@PathVariable Long id) {
        log.info("Get attendance request received for id: {}", id);
        AttendanceDTO response = attendanceService.getAttendanceById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/history/params")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get attendance between dates")
    public ResponseEntity<ApiResponse<List<AttendanceDTO>>> getStudentAttendanceDateRange1(
            @RequestParam Long classId,
            @RequestParam String sectionName,
            @RequestParam(required = false) Long admissionNumber,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        log.info("Get student attendance request between {} and {}", fromDate, toDate);
        List<AttendanceDTO> response = attendanceService.getStudentAttendanceBetweenDates(classId, sectionName, admissionNumber, fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    
    @GetMapping("/history/self/params")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get attendance between dates")
    public ResponseEntity<ApiResponse<List<AttendanceDTO>>> getMyAttendanceDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        log.info("Get student attendance request between {} and {}", fromDate, toDate);
        List<AttendanceDTO> response = attendanceService.getStudentAttendanceBetweenDates(fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/student/{admissionNumber}/date-range")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get attendance between dates")
    public ResponseEntity<ApiResponse<List<AttendanceDTO>>> getStudentAttendanceDateRange(
            @PathVariable Long admissionNumber,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        log.info("Get student attendance request between {} and {}", fromDate, toDate);
        List<AttendanceDTO> response = attendanceService.getStudentAttendanceBetweenDates(admissionNumber, fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/class/{classId}/section/{sectionName}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Get class/section attendance")
    public ResponseEntity<ApiResponse<List<AttendanceDTO>>> getClassSectionAttendance(
            @PathVariable Long classId,
            @PathVariable String sectionName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("Get class section attendance request for class: {} section: {}", classId, sectionName);
        List<AttendanceDTO> response = attendanceService.getClassSectionAttendance(classId, sectionName, date);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Update attendance")
    public ResponseEntity<ApiResponse<AttendanceDTO>> updateAttendance(
            @PathVariable Long id,
            @Valid @RequestBody AttendanceDTO attendanceDTO) {
        log.info("Update attendance request received for id: {}", id);
        AttendanceDTO response = attendanceService.updateAttendance(id, attendanceDTO);
        return ResponseEntity.ok(ApiResponse.success(response, "Attendance updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete attendance")
    public ResponseEntity<ApiResponse<Void>> deleteAttendance(@PathVariable Long id) {
        log.info("Delete attendance request received for id: {}", id);
        attendanceService.deleteAttendance(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Attendance deleted successfully"));
    }
}
