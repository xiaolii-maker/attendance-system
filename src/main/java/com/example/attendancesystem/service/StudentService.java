package com.example.attendancesystem.service;

import com.example.attendancesystem.entity.Student;
import java.util.List;

public interface StudentService {

    String createStudent(Student student);

    Student getStudentById(String studentId);

    List<Student> getStudentsByClass(String className, Integer page);

    List<Student> getAllStudents();
}