package com.example.attendancesystem.repository;

import com.example.attendancesystem.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, String>, JpaSpecificationExecutor<Student> {

    // 1. 分页查询所有学生
    Page<Student> findAll(Pageable pageable);

    // 2. 根据班级分页查询
    Page<Student> findByClassName(String className, Pageable pageable);

    // 3. 根据姓名模糊分页查询
    Page<Student> findByNameContaining(String name, Pageable pageable);

    // 4. 根据班级和性别分页查询
    Page<Student> findByClassNameAndGender(String className, String gender, Pageable pageable);

    // 5. 自定义 JPQL 分页查询
    @Query("SELECT s FROM Student s WHERE s.className = :className ORDER BY s.studentId")
    Page<Student> findStudentsByClass(@Param("className") String className, Pageable pageable);

    // 6. 根据年龄范围分页查询
    @Query("SELECT s FROM Student s WHERE s.age BETWEEN :minAge AND :maxAge")
    Page<Student> findByAgeBetween(@Param("minAge") Integer minAge, @Param("maxAge") Integer maxAge, Pageable pageable);
}