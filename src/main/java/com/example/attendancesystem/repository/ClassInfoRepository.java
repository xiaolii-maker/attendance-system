package com.example.attendancesystem.repository;

import com.example.attendancesystem.entity.ClassInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ClassInfoRepository extends JpaRepository<ClassInfo, Long> {
    Optional<ClassInfo> findByClassName(String className);
    boolean existsByClassName(String className);
}