package com.example.attendancesystem.repository;

import com.example.attendancesystem.entity.Leave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LeaveRepository extends JpaRepository<Leave, Long> {
    List<Leave> findByStudentId(String studentId);
    List<Leave> findByStatus(String status);
    List<Leave> findByStudentIdAndStatus(String studentId, String status);
    List<Leave> findByLeaveDateBetween(LocalDateTime start, LocalDateTime end);
    List<Leave> findByStatusIn(List<String> statuses);
    List<Leave> findByStudentIdAndCourseIdAndLeaveDate(String studentId, String courseId, LocalDateTime leaveDate);
}