package com.example.attendancesystem.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_request")
public class Leave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", length = 20)
    private String studentId;

    @Column(name = "student_name", length = 50)
    private String studentName;

    @Column(name = "course_id", length = 20)
    private String courseId;

    @Column(name = "course_name", length = 100)
    private String courseName;

    @Column(name = "leave_date")
    private LocalDateTime leaveDate;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "leave_type", length = 20)
    private String leaveType;  // ADVANCE提前请假, AFTER补假条

    @Column(name = "status", length = 20)
    private String status;  // PENDING待审批, APPROVED已通过, REJECTED已拒绝

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "approve_time")
    private LocalDateTime approveTime;

    @Column(name = "approve_remark", length = 200)
    private String approveRemark;

    public Leave() {}

    // Getter 和 Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public LocalDateTime getLeaveDate() { return leaveDate; }
    public void setLeaveDate(LocalDateTime leaveDate) { this.leaveDate = leaveDate; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getApproveTime() { return approveTime; }
    public void setApproveTime(LocalDateTime approveTime) { this.approveTime = approveTime; }
    public String getApproveRemark() { return approveRemark; }
    public void setApproveRemark(String approveRemark) { this.approveRemark = approveRemark; }
}