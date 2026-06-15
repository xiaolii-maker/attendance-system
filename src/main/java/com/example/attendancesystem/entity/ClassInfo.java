package com.example.attendancesystem.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "class_info")
public class ClassInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "class_name", unique = true, length = 50)
    private String className;  // 班级名称，如：人工智能1班

    @Column(name = "grade", length = 10)
    private String grade;  // 年级，如：2024

    @Column(name = "major", length = 50)
    private String major;  // 专业，如：人工智能

    @Column(name = "create_time")
    private LocalDateTime createTime;

    // 构造函数
    public ClassInfo() {}

    public ClassInfo(String className, String grade, String major) {
        this.className = className;
        this.grade = grade;
        this.major = major;
        this.createTime = LocalDateTime.now();
    }

    // Getter 和 Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}