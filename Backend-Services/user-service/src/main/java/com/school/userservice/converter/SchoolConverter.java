package com.school.userservice.converter;

import com.school.userservice.dto.SchoolDTO;
import com.school.userservice.entity.School;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SchoolConverter {

    public SchoolDTO entityToDTO(School school) {
        if (school == null) {
            return null;
        }
        return SchoolDTO.builder()
                .id(school.getId())
                .schoolCode(school.getSchoolCode())
                .schoolName(school.getSchoolName())
                .address(school.getAddress())
                .phone(school.getPhone())
                .email(school.getEmail())
                .website(school.getWebsite())
                .principalName(school.getPrincipalName())
                .announcement(school.getAnnouncement())
                .logo(school.getLogo())
                .favicon(school.getFavicon())
                .banner(school.getBanner())
                .isActive(school.isActive())
                .build();
    }

    public School dtoToEntity(SchoolDTO schoolDTO) {
        if (schoolDTO == null) {
            return null;
        }
        return School.builder()
                .id(schoolDTO.getId())
                .schoolCode(schoolDTO.getSchoolCode())
                .schoolName(schoolDTO.getSchoolName())
                .address(schoolDTO.getAddress())
                .phone(schoolDTO.getPhone())
                .email(schoolDTO.getEmail())
                .website(schoolDTO.getWebsite())
                .principalName(schoolDTO.getPrincipalName())
                .announcement(schoolDTO.getAnnouncement())
                .logo(schoolDTO.getLogo())
                .favicon(schoolDTO.getFavicon())
                .banner(schoolDTO.getBanner())
                .isActive(schoolDTO.isActive())
                .build();
    }
}
