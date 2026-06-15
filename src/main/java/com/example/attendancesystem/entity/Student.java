package com.example.attendancesystem.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "student")
public class Student {

    @Id
    @Column(name = "student_id", length = 20)
    private String studentId;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "age")
    private Integer age;

    @Column(name = "major", length = 50)
    private String major;  // 专业

    // 关联班级（使用班级名称，便于导入）
    @Column(name = "class_name", length = 50)
    private String className;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    public Student() {}

    public Student(String studentId, String name, String className, String major, String gender, Integer age) {
        this.studentId = studentId;
        this.name = name;
        this.className = className;
        this.major = major;
        this.gender = gender;
        this.age = age;
        this.createTime = LocalDateTime.now();
    }

    // Getter 和 Setter
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}