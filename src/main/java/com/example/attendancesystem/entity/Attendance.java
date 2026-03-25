package com.example.attendancesystem.entity;

import java.time.LocalDateTime;

public class Attendance {
    private Long id;                // 记录ID
    private String studentId;        // 学号
    private String studentName;      // 学生姓名
    private LocalDateTime checkInTime;  // 打卡时间
    private String status;           // 状态：正常、迟到、早退、缺勤
    private String courseName;       // 课程名称

    // 无参构造函数
    public Attendance() {
    }

    // 有参构造函数
    public Attendance(Long id, String studentId, String studentName,
                      LocalDateTime checkInTime, String status, String courseName) {
        this.id = id;
        this.studentId = studentId;
        this.studentName = studentName;
        this.checkInTime = checkInTime;
        this.status = status;
        this.courseName = courseName;
    }

    // Getter 和 Setter 方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }
}
