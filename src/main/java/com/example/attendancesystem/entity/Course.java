package com.example.attendancesystem.entity;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "course")
public class Course {

    @Id
    @Column(name = "course_id", length = 20)
    private String courseId;

    @Column(name = "course_name", length = 100)
    private String courseName;

    @Column(name = "class_name", length = 50)
    private String className;

    @Column(name = "teacher_id")
    private Long teacherId;

    @Column(name = "weekday")
    private Integer weekday;

    @Column(name = "start_week")
    private Integer startWeek;

    @Column(name = "end_week")
    private Integer endWeek;

    // 新增：课程开始时间（如 09:00:00）
    @Column(name = "start_time")
    private LocalTime startTime;

    // 新增：课程结束时间（如 11:30:00）
    @Column(name = "end_time")
    private LocalTime endTime;

    // 构造函数
    public Course() {
    }

    // Getter 和 Setter
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }

    public Integer getWeekday() { return weekday; }
    public void setWeekday(Integer weekday) { this.weekday = weekday; }

    public Integer getStartWeek() { return startWeek; }
    public void setStartWeek(Integer startWeek) { this.startWeek = startWeek; }

    public Integer getEndWeek() { return endWeek; }
    public void setEndWeek(Integer endWeek) { this.endWeek = endWeek; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
}