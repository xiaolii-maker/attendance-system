package com.example.attendancesystem.controller;

import com.example.attendancesystem.dto.AttendanceStatisticsDTO;
import com.example.attendancesystem.entity.Attendance;
import com.example.attendancesystem.entity.Course;
import com.example.attendancesystem.entity.Student;
import com.example.attendancesystem.entity.User;
import com.example.attendancesystem.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

import com.example.attendancesystem.entity.ClassInfo;


import com.example.attendancesystem.dto.ImportResult;
import com.example.attendancesystem.service.AttendanceImportService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.example.attendancesystem.entity.Task;
import com.example.attendancesystem.repository.TaskRepository;
import com.example.attendancesystem.repository.StudentRepository;

import com.example.attendancesystem.entity.Leave;
import com.example.attendancesystem.repository.LeaveRepository;

import org.springframework.security.crypto.password.PasswordEncoder;

@Controller
public class AttendanceController {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;  // 新增：用于验证学生

    @Autowired
    private AttendanceImportService attendanceImportService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ClassInfoRepository classInfoRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private LeaveRepository leaveRepository;


    @Value("${file.upload.path:D:/uploads/attendance/}")
    private String uploadPath;

    /**
     * 获取当前登录用户的用户名
     */
    private String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        return principal.toString();
    }

    // ==================== 学生免登录打卡（新增）====================

    /**
     * 学生打卡页面（免登录）
     */
    @GetMapping("/attendance/checkin")
    public String checkInPage(Model model) {
        List<Course> courses = courseRepository.findAll();
        model.addAttribute("courses", courses);
        return "attendance-checkin";
    }

    /**
     * 学生提交打卡（免登录，验证学号）
     */
    @PostMapping("/attendance/doCheckin")
    public String doCheckin(@RequestParam String courseId,
                            @RequestParam String studentId,
                            @RequestParam(required = false) String remark,
                            RedirectAttributes redirectAttributes) {
        try {
            // 1. 验证学生是否存在（从 student 表验证）
            Student student = studentRepository.findById(studentId).orElse(null);
            if (student == null) {
                redirectAttributes.addFlashAttribute("errorMsg", "学号不存在，请确认后重新打卡");
                return "redirect:/attendance/checkin";
            }

            // 2. 验证课程是否存在
            Course course = courseRepository.findById(courseId).orElse(null);
            if (course == null) {
                redirectAttributes.addFlashAttribute("errorMsg", "课程不存在");
                return "redirect:/attendance/checkin";
            }

            // 3. 检查今天是否已经打卡（防止重复打卡）
            LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1);
            List<Attendance> todayRecords = attendanceRepository.findByStudentIdAndCourseIdAndCheckInTimeBetween(
                    studentId, courseId, startOfDay, endOfDay);

            if (!todayRecords.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMsg", "您今天已经打过卡了，请勿重复打卡");
                return "redirect:/attendance/checkin";
            }

            LocalDateTime now = LocalDateTime.now();
            LocalTime nowTime = now.toLocalTime();
            LocalTime startTime = course.getStartTime() != null ? course.getStartTime() : LocalTime.of(9, 0);

            // 可打卡时间范围：开始前15分钟 到 开始后30分钟
            LocalTime earliestTime = startTime.minusMinutes(15);
            LocalTime latestTime = startTime.plusMinutes(30);

            if (nowTime.isBefore(earliestTime)) {
                redirectAttributes.addFlashAttribute("errorMsg",
                        "打卡时间未到！课程 " + course.getCourseName() + " 在 " + startTime + " 开始，请在 " + earliestTime + " 之后打卡");
                return "redirect:/attendance/checkin";
            }

            if (nowTime.isAfter(latestTime)) {
                redirectAttributes.addFlashAttribute("errorMsg",
                        "打卡时间已过！课程 " + course.getCourseName() + " 在 " + startTime + " 开始，最晚打卡时间为 " + latestTime);
                return "redirect:/attendance/checkin";
            }

            // 判断状态：迟到或正常
            String status = nowTime.isAfter(startTime) ? "LATE" : "NORMAL";

            // 创建考勤记录
            Attendance attendance = new Attendance();
            attendance.setStudentId(studentId);
            attendance.setStudentName(student.getName());
            attendance.setCourseId(courseId);
            attendance.setCourseName(course.getCourseName());
            attendance.setCheckInTime(now);
            attendance.setCreateTime(now);
            attendance.setRemark(remark);
            attendance.setStatus(status);

            attendanceRepository.save(attendance);

            String successMsg = status.equals("LATE") ? "打卡成功（迟到）" : "打卡成功！";
            redirectAttributes.addFlashAttribute("successMsg", successMsg);

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "打卡失败：" + e.getMessage());
        }

        return "redirect:/attendance/checkin";
    }

    // ==================== 原有打卡接口（保留给老师/管理员）====================

    /**
     * 原打卡页面（需要登录）- 重定向到新页面
     */
    @GetMapping("/attendance/checkin-old")
    public String checkInPageOld(Model model) {
        return "redirect:/attendance/checkin";
    }

    /**
     * 原提交打卡接口（保留，可能被其他地方调用）
     */
    @PostMapping("/attendance/checkin-old-submit")
    public String checkInOld(@RequestParam String courseId,
                             @RequestParam(required = false) String remark,
                             RedirectAttributes redirectAttributes) {
        try {
            String username = getCurrentUsername();
            User user = userRepository.findByUsername(username).orElse(null);
            Course course = courseRepository.findById(courseId).orElse(null);

            if (user == null || course == null) {
                redirectAttributes.addFlashAttribute("errorMsg", "用户或课程不存在");
                return "redirect:/attendance/checkin";
            }

            LocalDateTime now = LocalDateTime.now();
            LocalTime nowTime = now.toLocalTime();
            LocalTime startTime = course.getStartTime() != null ? course.getStartTime() : LocalTime.of(9, 0);

            LocalTime earliestTime = startTime.minusMinutes(15);
            LocalTime latestTime = startTime.plusMinutes(30);

            if (nowTime.isBefore(earliestTime)) {
                redirectAttributes.addFlashAttribute("errorMsg",
                        "打卡时间未到！课程 " + course.getCourseName() + " 在 " + startTime + " 开始，请在 " + earliestTime + " 之后打卡");
                return "redirect:/attendance/checkin";
            }

            if (nowTime.isAfter(latestTime)) {
                redirectAttributes.addFlashAttribute("errorMsg",
                        "打卡时间已过！课程 " + course.getCourseName() + " 在 " + startTime + " 开始，最晚打卡时间为 " + latestTime);
                return "redirect:/attendance/checkin";
            }

            String status = nowTime.isAfter(startTime) ? "LATE" : "NORMAL";

            Attendance attendance = new Attendance();
            attendance.setStudentId(username);
            attendance.setStudentName(user.getRealName());
            attendance.setCourseId(courseId);
            attendance.setCourseName(course.getCourseName());
            attendance.setCheckInTime(now);
            attendance.setCreateTime(LocalDateTime.now());
            attendance.setRemark(remark);
            attendance.setStatus(status);

            attendanceRepository.save(attendance);

            String successMsg = status.equals("LATE") ? "打卡成功（迟到）" : "打卡成功";
            redirectAttributes.addFlashAttribute("successMsg", successMsg);
            return "redirect:/attendance/list";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "打卡失败：" + e.getMessage());
            return "redirect:/attendance/checkin";
        }
    }

    // ==================== 考勤记录列表（老师/管理员可看全班）====================

    /**
     * 考勤记录列表（显示全班学生，缺勤的显示缺勤）
     */
    @GetMapping("/attendance/list")
    public String list(
            @RequestParam(required = false) String courseId,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        String username = getCurrentUsername();
        User currentUser = userRepository.findByUsername(username).orElse(null);

        boolean isAdmin = currentUser != null && ("ADMIN".equals(currentUser.getRole()) || "TEACHER".equals(currentUser.getRole()));

        // 获取课程列表（用于筛选）
        List<Course> courseList = courseRepository.findAll();
        model.addAttribute("courseList", courseList);

        // 获取班级列表（用于筛选）
        List<ClassInfo> classList = classInfoRepository.findAll();
        model.addAttribute("classList", classList);

        // 日期处理
        LocalDate queryStartDate;
        LocalDate queryEndDate;

        if (startDate != null && !startDate.isEmpty()) {
            queryStartDate = LocalDate.parse(startDate);
        } else {
            queryStartDate = LocalDate.now();
        }

        if (endDate != null && !endDate.isEmpty()) {
            queryEndDate = LocalDate.parse(endDate);
        } else {
            queryEndDate = LocalDate.now();
        }

        LocalDateTime startDateTime = queryStartDate.atStartOfDay();
        LocalDateTime endDateTime = queryEndDate.plusDays(1).atStartOfDay();

        List<AttendanceStatisticsDTO> statistics = new ArrayList<>();

        // 获取所有请假记录
        List<Leave> allLeaves = leaveRepository.findAll();

        if (isAdmin) {
            // ========== 管理员/教师：查看全班学生 ==========

            // 按班级筛选学生
            List<Student> allStudents;
            if (className != null && !className.isEmpty()) {
                allStudents = studentRepository.findByClassName(className);
            } else {
                allStudents = studentRepository.findAll();
            }

            // 查询考勤记录
            List<Attendance> attendances = attendanceRepository.findByCheckInTimeBetween(startDateTime, endDateTime);

            // 按课程筛选考勤记录
            if (courseId != null && !courseId.isEmpty()) {
                attendances = attendances.stream()
                        .filter(a -> a.getCourseId().equals(courseId))
                        .toList();
                allLeaves = allLeaves.stream()
                        .filter(l -> l.getCourseId().equals(courseId))
                        .toList();
            }

            // 按班级筛选考勤记录（只保留属于该班级学生的考勤）
            if (className != null && !className.isEmpty()) {
                List<String> studentIdsInClass = allStudents.stream()
                        .map(Student::getStudentId)
                        .toList();
                attendances = attendances.stream()
                        .filter(a -> studentIdsInClass.contains(a.getStudentId()))
                        .toList();
                allLeaves = allLeaves.stream()
                        .filter(l -> studentIdsInClass.contains(l.getStudentId()))
                        .toList();
            }

            for (Student student : allStudents) {
                // 查找该学生的考勤记录
                List<Attendance> studentAttendances = attendances.stream()
                        .filter(a -> a.getStudentId().equals(student.getStudentId()))
                        .toList();

                // 查找该学生在此日期范围内的请假记录
                List<Leave> studentLeaves = allLeaves.stream()
                        .filter(l -> l.getStudentId().equals(student.getStudentId())
                                && !l.getLeaveDate().toLocalDate().isBefore(queryStartDate)
                                && !l.getLeaveDate().toLocalDate().isAfter(queryEndDate))
                        .toList();

                if (studentAttendances.isEmpty()) {
                    // 没有打卡记录
                    if (!studentLeaves.isEmpty()) {
                        // 有请假记录
                        for (Leave leave : studentLeaves) {
                            AttendanceStatisticsDTO dto = new AttendanceStatisticsDTO();
                            dto.setStudentId(student.getStudentId());
                            dto.setStudentName(student.getName());
                            dto.setMajor(student.getMajor());
                            dto.setClassName(student.getClassName());
                            dto.setCheckInTime(null);
                            dto.setCourseName(leave.getCourseName());
                            dto.setLeaveReason(leave.getReason());
                            dto.setLeaveStatus(leave.getStatus());

                            if ("APPROVED".equals(leave.getStatus())) {
                                dto.setStatus("请假（已批）");
                            } else if ("PENDING".equals(leave.getStatus())) {
                                dto.setStatus("请假（待审批）");
                            } else {
                                dto.setStatus("缺勤");
                            }

                            // 状态筛选
                            if (status != null && !status.isEmpty()) {
                                String statusCN = getStatusChinese(status);
                                if (dto.getStatus().equals(statusCN)) {
                                    statistics.add(dto);
                                }
                            } else {
                                statistics.add(dto);
                            }
                        }
                    } else {
                        // 没有请假记录，显示缺勤
                        AttendanceStatisticsDTO dto = new AttendanceStatisticsDTO();
                        dto.setStudentId(student.getStudentId());
                        dto.setStudentName(student.getName());
                        dto.setMajor(student.getMajor());
                        dto.setClassName(student.getClassName());
                        dto.setCheckInTime(null);
                        dto.setStatus("缺勤");
                        dto.setCourseName("—");

                        if (status != null && !status.isEmpty()) {
                            String statusCN = getStatusChinese(status);
                            if (dto.getStatus().equals(statusCN)) {
                                statistics.add(dto);
                            }
                        } else {
                            statistics.add(dto);
                        }
                    }
                } else {
                    // 有打卡记录
                    for (Attendance attendance : studentAttendances) {
                        AttendanceStatisticsDTO dto = new AttendanceStatisticsDTO();
                        dto.setStudentId(student.getStudentId());
                        dto.setStudentName(student.getName());
                        dto.setMajor(student.getMajor());
                        dto.setClassName(student.getClassName());
                        dto.setCheckInTime(attendance.getCheckInTime());
                        dto.setStatus(attendance.getStatus().equals("NORMAL") ? "正常" : "迟到");
                        dto.setRemark(attendance.getRemark());
                        dto.setCourseName(attendance.getCourseName());

                        if (status != null && !status.isEmpty()) {
                            String statusCN = getStatusChinese(status);
                            if (dto.getStatus().equals(statusCN)) {
                                statistics.add(dto);
                            }
                        } else {
                            statistics.add(dto);
                        }
                    }
                }
            }

        } else if (currentUser != null) {
            // ========== 学生：只查看自己的考勤记录 ==========
            // 学生部分保持不变
            List<Attendance> myAttendances = attendanceRepository.findByStudentIdAndCheckInTimeBetween(
                    username, startDateTime, endDateTime);

            // 按课程筛选
            if (courseId != null && !courseId.isEmpty()) {
                myAttendances = myAttendances.stream()
                        .filter(a -> a.getCourseId().equals(courseId))
                        .toList();
            }

            List<Leave> myLeaves = leaveRepository.findByStudentId(username);

            // 按课程筛选请假记录
            if (courseId != null && !courseId.isEmpty()) {
                myLeaves = myLeaves.stream()
                        .filter(l -> l.getCourseId().equals(courseId))
                        .toList();
            }

            Student currentStudent = studentRepository.findById(username).orElse(null);

            // 先添加打卡记录
            for (Attendance attendance : myAttendances) {
                AttendanceStatisticsDTO dto = new AttendanceStatisticsDTO();
                dto.setStudentId(attendance.getStudentId());
                dto.setStudentName(attendance.getStudentName());
                dto.setMajor(currentStudent != null ? currentStudent.getMajor() : "");
                dto.setClassName(currentStudent != null ? currentStudent.getClassName() : "");
                dto.setCheckInTime(attendance.getCheckInTime());
                dto.setStatus(attendance.getStatus().equals("NORMAL") ? "正常" : "迟到");
                dto.setRemark(attendance.getRemark());
                dto.setCourseName(attendance.getCourseName());
                statistics.add(dto);
            }

            // 添加请假记录（如果没有对应的打卡）
            for (Leave leave : myLeaves) {
                LocalDateTime leaveStart = leave.getLeaveDate().withHour(0).withMinute(0);
                LocalDateTime leaveEnd = leave.getLeaveDate().withHour(23).withMinute(59);

                boolean hasCheckin = myAttendances.stream()
                        .anyMatch(a -> a.getCourseId().equals(leave.getCourseId())
                                && !a.getCheckInTime().isBefore(leaveStart)
                                && !a.getCheckInTime().isAfter(leaveEnd));

                if (!hasCheckin) {
                    AttendanceStatisticsDTO dto = new AttendanceStatisticsDTO();
                    dto.setStudentId(username);
                    dto.setStudentName(currentStudent != null ? currentStudent.getName() : "");
                    dto.setMajor(currentStudent != null ? currentStudent.getMajor() : "");
                    dto.setClassName(currentStudent != null ? currentStudent.getClassName() : "");
                    dto.setCheckInTime(leave.getLeaveDate());
                    dto.setCourseName(leave.getCourseName());
                    dto.setLeaveReason(leave.getReason());
                    dto.setLeaveStatus(leave.getStatus());

                    if ("APPROVED".equals(leave.getStatus())) {
                        dto.setStatus("请假（已批）");
                    } else if ("PENDING".equals(leave.getStatus())) {
                        dto.setStatus("请假（待审批）");
                    } else {
                        dto.setStatus("请假（已拒）");
                    }
                    statistics.add(dto);
                }
            }

            // 如果没有记录，显示提示
            if (statistics.isEmpty()) {
                AttendanceStatisticsDTO emptyDto = new AttendanceStatisticsDTO();
                emptyDto.setStudentId(username);
                emptyDto.setStudentName(currentStudent != null ? currentStudent.getName() : "");
                emptyDto.setStatus("暂无考勤记录");
                statistics.add(emptyDto);
            }
        }

        // 分页处理
        int total = statistics.size();
        int start = (page - 1) * size;
        int end = Math.min(start + size, total);

        List<AttendanceStatisticsDTO> pageData = start < total ? statistics.subList(start, end) : new ArrayList<>();

        model.addAttribute("records", pageData);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", (total + size - 1) / size);
        model.addAttribute("size", size);
        model.addAttribute("totalElements", total);
        model.addAttribute("startDate", queryStartDate.toString());
        model.addAttribute("endDate", queryEndDate.toString());
        model.addAttribute("selectedCourse", courseId);
        model.addAttribute("selectedClass", className);
        model.addAttribute("status", status);
        model.addAttribute("isAdmin", isAdmin);

        return "attendance-list";
    }

    // 辅助方法：状态中文转换
    private String getStatusChinese(String status) {
        if ("NORMAL".equals(status)) return "正常";
        if ("LATE".equals(status)) return "迟到";
        if ("ABSENT".equals(status)) return "缺勤";
        if ("LEAVE".equals(status)) return "请假";
        return status;
    }

    // ==================== 批量导入功能 ====================

    @GetMapping("/attendance/import")
    public String importPage() {
        return "attendance-import";
    }

    @PostMapping("/attendance/import")
    public String importFile(@RequestParam("file") MultipartFile file,
                             RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMsg", "请选择文件");
            return "redirect:/attendance/import";
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            redirectAttributes.addFlashAttribute("errorMsg", "文件大小不能超过10MB");
            return "redirect:/attendance/import";
        }

        if (!attendanceImportService.isValidExcelFile(file)) {
            redirectAttributes.addFlashAttribute("errorMsg", "文件格式不正确，请上传 .xlsx 或 .xls 文件");
            return "redirect:/attendance/import";
        }

        try {
            ImportResult result = attendanceImportService.importFromExcel(file);

            String successMsg = String.format("导入完成！成功：%d条，失败：%d条",
                    result.getSuccessCount(), result.getFailCount());
            redirectAttributes.addFlashAttribute("successMsg", successMsg);

            if (result.getFailCount() > 0) {
                redirectAttributes.addFlashAttribute("errorMsg", result.getErrorMessages());
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "导入失败：" + e.getMessage());
        }

        return "redirect:/attendance/import";
    }

    @GetMapping("/gen-pwd")
    @ResponseBody
    public String generatePassword() {
        return passwordEncoder.encode("123456");
    }

    @GetMapping("/template/attendance.xlsx")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=attendance_template.xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("考勤记录");
            Row headerRow = sheet.createRow(0);
            String[] headers = {"学号", "课程ID", "打卡时间", "状态", "备注"};
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 5000);
            }

            workbook.write(response.getOutputStream());
        }
    }

    /**
     * 任务打卡提交（扫码打卡）
     */
    @PostMapping("/attendance/taskCheckin")
    public String taskCheckin(@RequestParam String taskCode,
                              @RequestParam String courseId,
                              @RequestParam String studentId,
                              @RequestParam(required = false) String remark,
                              RedirectAttributes redirectAttributes) {
        try {
            Task task = taskRepository.findByTaskCode(taskCode).orElse(null);
            if (task == null) {
                redirectAttributes.addFlashAttribute("errorMsg", "任务无效");
                return "redirect:/task/scan?error";
            }

            if (LocalDateTime.now().isAfter(task.getEndTime())) {
                redirectAttributes.addFlashAttribute("errorMsg", "打卡时间已过");
                return "redirect:/task/scan?error";
            }

            Student student = studentRepository.findById(studentId).orElse(null);
            if (student == null) {
                redirectAttributes.addFlashAttribute("errorMsg", "学号不存在");
                return "redirect:/task/scan?taskCode=" + taskCode;
            }

            // 检查是否已经打卡
            List<Attendance> todayRecords = attendanceRepository.findByStudentIdAndCourseIdAndCheckInTimeBetween(
                    studentId, courseId, task.getStartTime(), task.getEndTime());

            if (!todayRecords.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMsg", "您已经完成本次打卡");
                return "redirect:/task/scan?taskCode=" + taskCode;
            }

            LocalDateTime now = LocalDateTime.now();
            String status = now.isAfter(task.getStartTime()) ? "LATE" : "NORMAL";

            Course course = courseRepository.findById(courseId).orElse(null);

            Attendance attendance = new Attendance();
            attendance.setStudentId(studentId);
            attendance.setStudentName(student.getName());
            attendance.setCourseId(courseId);
            attendance.setCourseName(course != null ? course.getCourseName() : "");
            attendance.setCheckInTime(now);
            attendance.setCreateTime(now);
            attendance.setRemark(remark);
            attendance.setStatus(status);

            attendanceRepository.save(attendance);

            redirectAttributes.addFlashAttribute("successMsg", status.equals("LATE") ? "打卡成功（迟到）" : "打卡成功！");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "打卡失败：" + e.getMessage());
        }
        return "redirect:/page/dashboard";
    }

    /**
     * 传统打卡（直接输入学号选择课程）
     */
    @PostMapping("/attendance/checkin-submit")
    public String directCheckin(@RequestParam String courseId,
                                @RequestParam String studentId,
                                @RequestParam(required = false) String remark,
                                RedirectAttributes redirectAttributes) {
        try {
            Student student = studentRepository.findById(studentId).orElse(null);
            if (student == null) {
                redirectAttributes.addFlashAttribute("errorMsg", "学号不存在");
                return "redirect:/task/scan";
            }

            Course course = courseRepository.findById(courseId).orElse(null);
            if (course == null) {
                redirectAttributes.addFlashAttribute("errorMsg", "课程不存在");
                return "redirect:/task/scan";
            }

            // 检查今天是否已经打卡
            LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1);
            List<Attendance> todayRecords = attendanceRepository.findByStudentIdAndCourseIdAndCheckInTimeBetween(
                    studentId, courseId, startOfDay, endOfDay);

            if (!todayRecords.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMsg", "您今天已经打过卡了");
                return "redirect:/task/scan";
            }

            LocalDateTime now = LocalDateTime.now();
            LocalTime startTime = course.getStartTime() != null ? course.getStartTime() : LocalTime.of(9, 0);
            String status = now.toLocalTime().isAfter(startTime) ? "LATE" : "NORMAL";

            Attendance attendance = new Attendance();
            attendance.setStudentId(studentId);
            attendance.setStudentName(student.getName());
            attendance.setCourseId(courseId);
            attendance.setCourseName(course.getCourseName());
            attendance.setCheckInTime(now);
            attendance.setCreateTime(now);
            attendance.setRemark(remark);
            attendance.setStatus(status);

            attendanceRepository.save(attendance);

            redirectAttributes.addFlashAttribute("successMsg", status.equals("LATE") ? "打卡成功（迟到）" : "打卡成功！");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "打卡失败：" + e.getMessage());
        }
        return "redirect:/page/dashboard";
    }

}