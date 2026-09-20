package com.school.userservice.converter;

import com.school.userservice.dto.SectionDTO;
import com.school.userservice.entity.Section;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SectionConverter {

    public SectionDTO entityToDTO(Section section) {
        if (section == null) {
            return null;
        }
        return SectionDTO.builder()
                .id(section.getId())
                .classId(section.getClassId())
                .sectionName(section.getSectionName())
                .capacity(section.getCapacity())
                .isActive(section.getIsActive())
                .build();
    }

    public Section dtoToEntity(SectionDTO sectionDTO) {
        if (sectionDTO == null) {
            return null;
        }
        return Section.builder()
                .id(sectionDTO.getId())
                .classId(sectionDTO.getClassId())
                .sectionName(sectionDTO.getSectionName())
                .capacity(sectionDTO.getCapacity())
                .isActive(sectionDTO.getIsActive())
                .build();
    }
}
