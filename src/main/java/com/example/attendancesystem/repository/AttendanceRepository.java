package com.example.attendancesystem.repository;

import com.example.attendancesystem.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Integer>, JpaSpecificationExecutor<Attendance> {
    Page<Attendance> findByStudentId(String studentId, Pageable pageable);
    List<Attendance> findByStudentIdAndCourseIdAndCheckInTimeBetween(
            String studentId, String courseId, LocalDateTime start, LocalDateTime end);

    List<Attendance> findByCheckInTimeBetween(LocalDateTime start, LocalDateTime end);
    // 查询某个学生在指定日期范围内的考勤记录
    List<Attendance> findByStudentIdAndCheckInTimeBetween(String studentId, LocalDateTime start, LocalDateTime end);

    boolean existsByStudentIdAndCourseIdAndCheckInTime(String studentId, String courseId, LocalDateTime checkInTime);
}