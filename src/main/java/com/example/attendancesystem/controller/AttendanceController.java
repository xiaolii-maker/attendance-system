package com.example.attendancesystem.controller;

import com.example.attendancesystem.entity.Attendance;
import com.example.attendancesystem.util.Result;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {

    // 任务三：考勤记录更新接口（JSON体参数）
    @PostMapping("/update")
    public Result<String> updateAttendance(@RequestBody Attendance attendance) {
        // 模拟更新考勤记录
        String message = String.format("考勤记录更新成功：学生 %s(%s) %s 打卡，课程：%s",
                attendance.getStudentName(),
                attendance.getStudentId(),
                attendance.getStatus(),
                attendance.getCourseName());

        return Result.success(message);
    }
}

