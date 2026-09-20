package com.school.academicservice.repository;

import com.school.academicservice.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByAdmissionNumberAndAttendanceDate(Long admissionNumber, LocalDate attendanceDate);
    List<Attendance> findByAdmissionNumber(Long admissionNumber);
    List<Attendance> findByAdmissionNumberAndClassId(Long admissionNumber, Long classId);
    List<Attendance> findByClassIdAndSectionNameAndAttendanceDate(Long classId, String sectionName, LocalDate attendanceDate);
    List<Attendance> findByAdmissionNumberAndAttendanceDateBetween(Long admissionNumber, LocalDate fromDate, LocalDate toDate);
    List<Attendance> findByClassIdAndSectionNameAndAttendanceDateBetween(Long classId, String sectionName, LocalDate fromDate, LocalDate toDate);
}
