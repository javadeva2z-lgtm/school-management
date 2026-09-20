package com.school.userservice.service;

import com.school.userservice.dto.SchoolDTO;
import com.school.userservice.entity.School;
import com.school.userservice.repository.SchoolRepository;
import com.school.userservice.converter.SchoolConverter;
import com.school.common.exception.ResourceNotFoundException;
import com.school.common.service.BaseService;
import com.school.common.exception.DuplicateResourceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class SchoolService extends BaseService {
    private final SchoolRepository schoolRepository;
    private final SchoolConverter schoolConverter;

    public List<SchoolDTO> getAllSchools() {
        log.info("Fetching all schools");
        List<School> schools = schoolRepository.findAll();
        return schools.stream()
                .map(schoolConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    public SchoolDTO getSchoolByCode(String schoolCode) {
        log.info("Fetching school with code: {}", schoolCode);
        School school = schoolRepository.findBySchoolCode(schoolCode)
                .orElseThrow(() -> new ResourceNotFoundException("School", "schoolCode", schoolCode));
        return schoolConverter.entityToDTO(school);
    }

    public SchoolDTO createSchool(SchoolDTO schoolDTO) {
        log.info("Creating school: {}", schoolDTO.getSchoolName());

        if (!schoolRepository.findAll().isEmpty()) {
            throw new DuplicateResourceException("School", "schoolCode", schoolDTO.getSchoolCode());
        }

        School school = schoolConverter.dtoToEntity(schoolDTO);
        school = schoolRepository.save(school);
        log.info("School created successfully with id: {}", school.getId());
        return schoolConverter.entityToDTO(school);
    }

    public SchoolDTO getSchoolById(Long schoolId) {
        log.info("Fetching school with id: {}", schoolId);
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School", "schoolId", schoolId));
        return schoolConverter.entityToDTO(school);
    }

    public SchoolDTO updateSchoolAnnouncement(SchoolDTO schoolDTO) {
        log.info("Updating school with code: {}", schoolDTO.getSchoolCode());
        School school = schoolRepository.findBySchoolCode(schoolDTO.getSchoolCode())
                .orElseThrow(() -> new ResourceNotFoundException("School", "schoolCode", schoolDTO.getSchoolCode()));
        school.setAnnouncement(schoolDTO.getAnnouncement());

        schoolRepository.save(school);
        log.info("Announcement updated successfully");
        return schoolConverter.entityToDTO(school);
    }

    public SchoolDTO enableDisableSchool(String code, boolean isActive) {
        log.info("Updating school with code: {}", code);
        School school = schoolRepository.findBySchoolCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("School", "schoolCode", code));
        school.setActive(isActive);

        schoolRepository.save(school);
        log.info("School enabled/disabled successfully");
        return schoolConverter.entityToDTO(school);
    }

    public boolean validateToken(String token) {
        log.info("Validating token: {}", token);
        School school = schoolRepository.findByKeywords(token)
                .orElseThrow(() -> new ResourceNotFoundException("School", "token", token));
        return school.isActive();
    }

    public String getToken() {
        School school = schoolRepository.findAll().stream().filter(x -> StringUtils.isNotEmpty(x.getKeywords()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("School", "token", getSchoolCodeFromRequestHeader()));
        return school.getKeywords();
    }

    public void clearToken() {
        List<School> schools = schoolRepository.findAll();
        if (schools.isEmpty()) {
            throw new ResourceNotFoundException("School", "token", "No schools found");
        } else {
            for (School school : schools) {
                if (StringUtils.isNotEmpty(school.getKeywords())) {
                    school.setKeywords(null);
                }
            }
        }
        schoolRepository.saveAll(schools);
    }

    public SchoolDTO updateSchool(SchoolDTO schoolDTO) {
        School school = schoolRepository.findBySchoolCode(schoolDTO.getSchoolCode())
                .orElseThrow(() -> new ResourceNotFoundException("School", "schoolCode", schoolDTO.getSchoolCode()));

        school.setSchoolName(schoolDTO.getSchoolName());
        school.setSchoolCode(schoolDTO.getSchoolCode());
        school.setAddress(schoolDTO.getAddress());
        school.setEmail(schoolDTO.getEmail());
        school.setWebsite(schoolDTO.getWebsite());
        school.setPrincipalName(schoolDTO.getPrincipalName());
        school.setAnnouncement(schoolDTO.getAnnouncement());
        school.setLogo(schoolDTO.getLogo());
        school.setFavicon(schoolDTO.getFavicon());
        school.setBanner(schoolDTO.getBanner());
        school.setPhone(schoolDTO.getPhone());

        school = schoolRepository.save(school);
        log.info("School updated successfully with id: {}", school.getId());
        return schoolConverter.entityToDTO(school);
    }

    public void deleteSchool(Long schoolId) {
        log.info("Deleting school with id: {}", schoolId);
        if (!schoolRepository.existsById(schoolId)) {
            throw new ResourceNotFoundException("School", "schoolId", schoolId);
        }
        schoolRepository.deleteById(schoolId);
        log.info("School deleted successfully with id: {}", schoolId);
    }
}