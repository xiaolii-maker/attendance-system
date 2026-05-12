package com.example.attendancesystem.service;

import java.util.List;

public interface StudentService {
    String getStudentInfo();
    String attendance(String studentId);
    List<String> getCourse();
    String getStudentById(String id);
    String searchStudent(String name, int page);


}