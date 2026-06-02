package com.example.attendancesystem.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance")
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "student_id", length = 20)
    private String studentId;

    @Column(name = "student_name", length = 50)
    private String studentName;

    @Column(name = "course_id", length = 20)
    private String courseId;

    @Column(name = "course_name", length = 100)
    private String courseName;

    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;

    @Column(name = "status", length = 20)
    private String status;  // NORMAL正常/LATE迟到/EARLY早退/ABSENT缺勤

    @Column(name = "ip", length = 15)
    private String ip;

    @Column(name = "remark", length = 255)
    private String remark;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    // 构造函数
    public Attendance() {
    }

    // Getter 和 Setter
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public LocalDateTime getCheckInTime() { return checkInTime; }
    public void setCheckInTime(LocalDateTime checkInTime) { this.checkInTime = checkInTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}