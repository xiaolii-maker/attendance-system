package com.example.attendancesystem.repository;

import com.example.attendancesystem.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    Optional<Task> findByTaskCode(String taskCode);
    List<Task> findByStatus(String status);
    List<Task> findByCreateByOrderByCreateTimeDesc(String createBy);
    List<Task> findByStartTimeBeforeAndEndTimeAfterAndStatus(LocalDateTime now, LocalDateTime now2, String status);
}