package com.preschool.preschool.repository;

import com.preschool.preschool.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByStudent_Id(Long studentId);

    java.util.Optional<Attendance> findByStudent_IdAndDate(Long studentId, java.time.LocalDate date);

    List<Attendance> findByDate(java.time.LocalDate date);
}
