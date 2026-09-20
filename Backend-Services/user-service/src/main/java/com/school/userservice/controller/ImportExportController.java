package com.school.userservice.controller;

import java.io.IOException;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.school.userservice.service.SectionService;
import com.school.userservice.service.StudentService;
import com.school.userservice.service.TeacherService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/bulk")
@RequiredArgsConstructor
@Tag(name = "Import Export", description = "Import export endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ImportExportController {
	
	private final SectionService sectionService;
	private final ResourceLoader resourceLoader;
	private final StudentService studentService;
	private final TeacherService teacherService;
	

	@PostMapping(value = "/import-csv/{type}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "Import Medicines via CSV", description = "Uploads a CSV file to bulk-import  records.")
	public ResponseEntity<String> uploadCsv(@PathVariable String type,
			@Parameter(description = "CSV file containing records to be imported") @RequestPart("file") MultipartFile file) {

				if (file.isEmpty()) {
					return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please upload a valid CSV file.");
				}
				if(!type.equalsIgnoreCase("student") && !type.equalsIgnoreCase("teacher") && !type.equalsIgnoreCase("class") && !type.equalsIgnoreCase("section")) {
					return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid type. Allowed values: student, teacher, class, section");
				}
				if(type.equalsIgnoreCase("student")) {
					studentService.importCsv(file);
				} else if(type.equalsIgnoreCase("teacher")) {
					teacherService.importCsv(file);
				} else if(type.equalsIgnoreCase("section")) {
					sectionService.importCsv(file);
				}
		
			return ResponseEntity.status(HttpStatus.OK).body("CSV file uploaded and data saved successfully.");
	}

	/**
	 * Export a template for different types of import.
	 * @param type eg. STUDENT, TEACHER, SECTION
	 * @return
	 */
	@GetMapping("/template/{type}")
	@Operation(summary = "Get Import template for student/teacher/class/section", description = "Get the template that can be used to upload students/teachers/classes/sections.")
	public ResponseEntity<Resource> downloadStaticCsv(
			 @Parameter(
			            description = "Type of template to fetch. Allowed values: student, teacher, class, section",
			            example = "student"
			        )
			@PathVariable String type) {
		try {
			Resource resource = resourceLoader.getResource("classpath:templates/"+(type.toUpperCase())+"_import.csv");
			if (!resource.exists()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
			}
			HttpHeaders headers = new HttpHeaders();
			headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+type+"_template.csv");

			return ResponseEntity.ok().headers(headers).contentLength(resource.contentLength())
					.contentType(MediaType.parseMediaType("text/csv")).body(resource);

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GetMapping("/export/teacher")
	@Operation(summary = "Export all teachers in csv file", description = "Export all the available teacher in the school.")
	public void exportTeacher(HttpServletResponse response) throws IOException {
		// 1. Set the content type and attachment header
		response.setContentType("text/csv");
		response.setHeader("Content-Disposition", "attachment; filename=\"teacher_export.csv\"");
		teacherService.exportAllTeachersToCsv(response.getWriter());
	}
	
	@GetMapping("/export/student/class/{classId}/section/{sectionName}")
	@Operation(summary = "Export all students in csv file", description = "Export all the available students in the school.")
	public void exportUsersToCsv(HttpServletResponse response, @PathVariable Long classId, @PathVariable String sectionName) throws IOException {
		// 1. Set the content type and attachment header
		response.setContentType("text/csv");
		response.setHeader("Content-Disposition", "attachment; filename=\"medicines_export.csv\"");
		studentService.exportAllStudentsToCsv(response.getWriter(), classId, sectionName);
	}

}
