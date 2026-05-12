package com.example.attendancesystem.controller;

import com.example.attendancesystem.entity.Student;
import com.example.attendancesystem.repository.StudentRepository;
import com.example.attendancesystem.service.StudentService;
import com.example.attendancesystem.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@RestController
public class StudentController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private StudentRepository studentRepository;

    @GetMapping("/student/info")
    public String getStudentInfo() {
        return studentService.getStudentInfo();
    }

    @PostMapping("/student/attendance")
    public String attendance(@RequestBody String studentId) {
        return studentService.attendance(studentId);
    }

    @GetMapping("/student/course")
    public List<String> getCourse() {
        return studentService.getCourse();
    }

    @GetMapping("/student/{id}")
    public String getStudentById(@PathVariable String id) {
        return studentService.getStudentById(id);
    }

    @GetMapping("/student/search")
    public String searchStudent(@RequestParam String name,
                                @RequestParam(defaultValue = "1") int page) {
        return studentService.searchStudent(name, page);
    }

    // ========== 任务一：分页查询（课件示例）==========

    @GetMapping("/list")
    public Result<Page<Student>> getStudentList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String className) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Student> result;

        if (className != null && !className.isEmpty()) {
            result = studentRepository.findByClassName(className, pageable);
        } else {
            result = studentRepository.findAll(pageable);
        }

        return Result.success(result);
    }

    // ========== 任务二：排序功能（课件示例）==========

    @GetMapping("/list/sort")
    public Result<Page<Student>> getStudentListWithSort(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createTime") String sortField,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortField);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Student> result = studentRepository.findAll(pageable);
        return Result.success(result);
    }

    // ========== 任务三：多条件动态查询（Specification）==========

    @GetMapping("/search")
    public Result<Page<Student>> searchStudents(
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge,
            @RequestParam(required = false) String gender,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createTime") String sortField,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        // 构建排序
        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortField);
        Pageable pageable = PageRequest.of(page, size, sort);

        // 构建动态查询条件（课件 Specification 示例）
        Specification<Student> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (className != null && !className.isEmpty()) {
                predicates.add(cb.equal(root.get("className"), className));
            }
            if (name != null && !name.isEmpty()) {
                predicates.add(cb.like(root.get("name"), "%" + name + "%"));
            }
            if (gender != null && !gender.isEmpty()) {
                predicates.add(cb.equal(root.get("gender"), gender));
            }
            if (minAge != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("age"), minAge));
            }
            if (maxAge != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("age"), maxAge));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Student> result = studentRepository.findAll(spec, pageable);
        return Result.success(result);
    }
}