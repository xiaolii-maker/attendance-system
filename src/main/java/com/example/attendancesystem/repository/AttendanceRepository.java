package com.example.attendancesystem.repository;

import com.example.attendancesystem.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Integer>, JpaSpecificationExecutor<Attendance> {
    Page<Attendance> findByStudentId(String studentId, Pageable pageable);
}