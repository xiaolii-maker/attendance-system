package com.example.attendancesystem.service.impl;

import com.example.attendancesystem.dao.StudentDao;
import com.example.attendancesystem.entity.Student;
import com.example.attendancesystem.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class StudentServiceImpl implements StudentService {

    @Autowired
    private StudentDao studentDao;

    @Override
    public String createStudent(Student student) {
        // 业务逻辑：校验姓名不能为空
        if (student.getName() == null || student.getName().isEmpty()) {
            throw new RuntimeException("姓名不能为空");
        }

        // 业务逻辑：校验学号不能为空
        if (student.getStudentId() == null || student.getStudentId().isEmpty()) {
            throw new RuntimeException("学号不能为空");
        }

        // 业务逻辑：校验班级不能为空
        if (student.getClassName() == null || student.getClassName().isEmpty()) {
            throw new RuntimeException("班级不能为空");
        }

        // 业务逻辑：检查学号是否已存在
        Student existing = studentDao.findById(student.getStudentId());
        if (existing != null) {
            throw new RuntimeException("学号 " + student.getStudentId() + " 已存在");
        }

        // 调用Dao层
        studentDao.insert(student);
        return "创建成功";
    }

    @Override
    public Student getStudentById(String studentId) {
        // 业务逻辑：校验学号不能为空
        if (studentId == null || studentId.isEmpty()) {
            throw new RuntimeException("学号不能为空");
        }

        // 调用Dao层
        Student student = studentDao.findById(studentId);
        if (student == null) {
            throw new RuntimeException("未找到学号为 " + studentId + " 的学生");
        }
        return student;
    }

    @Override
    public List<Student> getStudentsByClass(String className, Integer page) {
        // 业务逻辑：校验班级不能为空
        if (className == null || className.isEmpty()) {
            throw new RuntimeException("班级不能为空");
        }

        // 调用Dao层
        List<Student> allStudents = studentDao.findByClassName(className);

        // 简单分页（每页2条）
        int pageSize = 2;
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, allStudents.size());

        if (start < allStudents.size()) {
            return allStudents.subList(start, end);
        }
        return List.of();
    }

    @Override
    public List<Student> getAllStudents() {
        return studentDao.findAll();
    }
}