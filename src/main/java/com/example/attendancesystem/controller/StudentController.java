package com.example.attendancesystem.controller;
import com.example.attendancesystem.service.StudentService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.attendancesystem.util.Result;
import com.example.attendancesystem.entity.Student;

import java.util.List;

import java.util.Arrays;

@RestController
@RequestMapping("/student")
public class StudentController {
    @GetMapping("/info")
    public String getStudentInfo(){
        return "姓名：李佳蔚 学号：42411020 班级：人工智能";
    }
    @PostMapping("/attendance")
    public String attendane(@RequestBody String studentId){
        return "学号为 "+studentId+"的学生打卡成功！";
    }
    @GetMapping("/course")
    public List<String> getCourse(){
        return Arrays.asList("JavaEE开发实践","计算机组成原理",
                "机器学习与数据挖掘","数据库原理与应用","大学物理实验");
    }

    @GetMapping("/search")
    public String searchStudent(@RequestParam String name,
                                @RequestParam(defaultValue = "1")int page){
        return "搜索学生姓名："+name+",页码："+page;
    }
    @Autowired
    private StudentService studentService;

    // 任务一：学生信息查询接口（路径参数）
    @GetMapping("/{id}")
    public Result<Student> getById(@PathVariable String id) {
        return Result.success(studentService.getStudentById(id));
    }

    // 任务二：学生列表查询接口（查询参数）
    @GetMapping("/list")
    public Result<List<Student>> getList(@RequestParam String className,
                                         @RequestParam(defaultValue = "1") Integer page) {
        return Result.success(studentService.getStudentsByClass(className, page));
    }

    // 任务三：学生新增接口（JSON体参数）
    @PostMapping("/create")
    public Result<String> create(@RequestBody Student student) {
        return Result.success(studentService.createStudent(student));
    }

}

