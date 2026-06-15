package com.example.attendancesystem.dto;

import java.time.LocalDateTime;

public class AttendanceStatisticsDTO {
    private String studentId;
    private String studentName;
    private String major;
    private String className;
    private LocalDateTime checkInTime;
    private String status;      // 正常、迟到、缺勤、请假
    private String remark;
    private String courseName;
    private String leaveStatus;  // 请假审批状态：PENDING, APPROVED, REJECTED
    private String leaveReason;  // 请假原因

    // 构造函数
    public AttendanceStatisticsDTO() {}

    // Getter 和 Setter
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public LocalDateTime getCheckInTime() { return checkInTime; }
    public void setCheckInTime(LocalDateTime checkInTime) { this.checkInTime = checkInTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getLeaveStatus() { return leaveStatus; }
    public void setLeaveStatus(String leaveStatus) { this.leaveStatus = leaveStatus; }
    public String getLeaveReason() { return leaveReason; }
    public void setLeaveReason(String leaveReason) { this.leaveReason = leaveReason; }
}