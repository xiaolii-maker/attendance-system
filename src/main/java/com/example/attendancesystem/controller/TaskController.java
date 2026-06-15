package com.example.attendancesystem.controller;

import com.example.attendancesystem.entity.Course;
import com.example.attendancesystem.entity.Task;
import com.example.attendancesystem.repository.CourseRepository;
import com.example.attendancesystem.repository.TaskRepository;
import com.example.attendancesystem.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;


import com.example.attendancesystem.entity.Student;
import com.example.attendancesystem.entity.Attendance;
import com.example.attendancesystem.repository.StudentRepository;
import com.example.attendancesystem.repository.AttendanceRepository;

@Controller
@RequestMapping("/task")
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    private String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        return principal.toString();
    }

    private String generateTaskCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    private String getCurrentHost() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return request.getServerName() + ":" + request.getServerPort();
        }
        return "localhost:8080";
    }

    @GetMapping("/publish")
    public String publishPage(Model model) {
        List<Course> courses = courseRepository.findAll();
        model.addAttribute("courses", courses);
        return "task-publish";
    }

    @PostMapping("/publish")
    public String publishTask(@RequestParam String courseId,
                              @RequestParam String taskName,
                              @RequestParam String startTime,
                              @RequestParam String endTime,
                              RedirectAttributes redirectAttributes) {
        String username = getCurrentUsername();
        Course course = courseRepository.findById(courseId).orElse(null);

        if (course == null) {
            redirectAttributes.addFlashAttribute("errorMsg", "课程不存在");
            return "redirect:/task/publish";
        }

        String taskCode;
        do {
            taskCode = generateTaskCode();
        } while (taskRepository.findByTaskCode(taskCode).isPresent());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

        Task task = new Task();
        task.setTaskCode(taskCode);
        task.setCourseId(courseId);
        task.setCourseName(course.getCourseName());
        task.setTaskName(taskName);
        task.setStartTime(LocalDateTime.parse(startTime, formatter));
        task.setEndTime(LocalDateTime.parse(endTime, formatter));
        task.setStatus("ACTIVE");
        task.setCreateTime(LocalDateTime.now());
        task.setCreateBy(username);

        taskRepository.save(task);

        redirectAttributes.addFlashAttribute("successMsg", "打卡任务发布成功！");
        redirectAttributes.addFlashAttribute("taskCode", taskCode);
        return "redirect:/task/publish";
    }

    @GetMapping("/list")
    public String taskList(Model model) {
        String username = getCurrentUsername();
        List<Task> tasks = taskRepository.findByCreateByOrderByCreateTimeDesc(username);
        model.addAttribute("tasks", tasks);
        return "task-list";
    }

    @GetMapping("/scan")
    public String scanPage(@RequestParam(required = false) String taskCode, Model model) {
        List<Course> courses = courseRepository.findAll();
        model.addAttribute("courses", courses);

        if (taskCode != null && !taskCode.isEmpty()) {
            Task task = taskRepository.findByTaskCode(taskCode).orElse(null);
            if (task == null) {
                model.addAttribute("errorMsg", "任务码无效");
            } else if (LocalDateTime.now().isAfter(task.getEndTime())) {
                model.addAttribute("errorMsg", "打卡时间已过");
            } else {
                model.addAttribute("task", task);
            }
        }
        return "task-scan";
    }



    /**
     * 学生输入学号和任务码打卡（简化版：时间段内都算成功）
     */
    @PostMapping("/doCheckin")
    public String doCheckin(@RequestParam String studentId,
                            @RequestParam String taskCode,
                            RedirectAttributes redirectAttributes) {
        try {
            // 1. 验证学生
            Student student = studentRepository.findById(studentId).orElse(null);
            if (student == null) {
                redirectAttributes.addFlashAttribute("errorMsg", "学号不存在");
                return "redirect:/task/scan";
            }

            // 2. 验证任务
            Task task = taskRepository.findByTaskCode(taskCode).orElse(null);
            if (task == null) {
                redirectAttributes.addFlashAttribute("errorMsg", "任务码无效");
                return "redirect:/task/scan";
            }

            // 3. 检查是否在任务有效时间内
            LocalDateTime now = LocalDateTime.now();

            if (now.isBefore(task.getStartTime())) {
                redirectAttributes.addFlashAttribute("errorMsg",
                        "打卡尚未开始，开始时间：" + task.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                return "redirect:/task/scan";
            }

            if (now.isAfter(task.getEndTime())) {
                redirectAttributes.addFlashAttribute("errorMsg",
                        "打卡时间已过，结束时间：" + task.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                return "redirect:/task/scan";
            }

            // 4. 检查是否已经打卡
            List<Attendance> existing = attendanceRepository.findByStudentIdAndCourseIdAndCheckInTimeBetween(
                    studentId, task.getCourseId(), task.getStartTime(), task.getEndTime());

            if (!existing.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMsg", "您已经完成本次打卡，请勿重复打卡");
                return "redirect:/task/scan";
            }

            // 5. 创建考勤记录（全部标记为 NORMAL，不区分迟到）
            Attendance attendance = new Attendance();
            attendance.setStudentId(studentId);
            attendance.setStudentName(student.getName());
            attendance.setCourseId(task.getCourseId());
            attendance.setCourseName(task.getCourseName());
            attendance.setCheckInTime(now);
            attendance.setCreateTime(now);
            attendance.setStatus("NORMAL");  // 全部正常
            attendance.setRemark("任务打卡");

            attendanceRepository.save(attendance);

            redirectAttributes.addFlashAttribute("successMsg", "打卡成功！");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "打卡失败：" + e.getMessage());
        }
        return "redirect:/task/scan";
    }
}