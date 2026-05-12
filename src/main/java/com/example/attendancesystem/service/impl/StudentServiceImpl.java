package com.example.attendancesystem.service.impl;


import com.example.attendancesystem.entity.Student;
import com.example.attendancesystem.repository.StudentRepository;
import com.example.attendancesystem.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.List;

@Service
public class StudentServiceImpl implements StudentService {

    @Override
    public String getStudentInfo() {
        return "姓名：李佳蔚 学号：42411020 班级：人工智能";
    }

    @Override
    public String attendance(String studentId) {
        return "学号为 " + studentId + " 的学生打卡成功！";
    }

    @Override
    public List<String> getCourse() {
        return Arrays.asList("JavaEE开发实践", "计算机组成原理",
                "机器学习与数据挖掘", "数据库原理与应用", "大学物理实验");
    }

    @Override
    public String getStudentById(String id) {
        return "查询学号为" + id + "的学生信息";
    }

    @Override
    public String searchStudent(String name, int page) {
        return "搜索学生姓名：" + name + ", 页码：" + page;
    }




}