package com.school.academicservice.converter;

import com.school.academicservice.dto.AttendanceDTO;
import com.school.academicservice.entity.Attendance;
import org.springframework.stereotype.Component;

@Component
public class AttendanceConverter {

    public AttendanceDTO entityToDTO(Attendance attendance) {
        if (attendance == null) {
            return null;
        }
        return AttendanceDTO.builder()
                .id(attendance.getId())
                .admissionNumber(attendance.getAdmissionNumber())
                .teacherId(attendance.getTeacherId())
                .classId(attendance.getClassId())
                .sectionName(attendance.getSectionName())
                .attendanceDate(attendance.getAttendanceDate())
                .status(attendance.getStatus())
                .remarks(attendance.getRemarks())
                .onLeave(attendance.isOnLeave())
                .build();
    }

    public Attendance dtoToEntity(AttendanceDTO attendanceDTO) {
        if (attendanceDTO == null) {
            return null;
        }
        return Attendance.builder()
                .id(attendanceDTO.getId())
                .admissionNumber(attendanceDTO.getAdmissionNumber())
                .teacherId(attendanceDTO.getTeacherId())
                .classId(attendanceDTO.getClassId())
                .sectionName(attendanceDTO.getSectionName())
                .attendanceDate(attendanceDTO.getAttendanceDate())
                .status(attendanceDTO.getStatus())
                .remarks(attendanceDTO.getRemarks())
                .onLeave(attendanceDTO.isOnLeave())
                .build();
    }
}
