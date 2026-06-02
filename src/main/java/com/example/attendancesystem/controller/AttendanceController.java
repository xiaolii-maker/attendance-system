package com.example.attendancesystem.controller;

import com.example.attendancesystem.entity.Attendance;
import com.example.attendancesystem.entity.Course;
import com.example.attendancesystem.entity.User;
import com.example.attendancesystem.repository.AttendanceRepository;
import com.example.attendancesystem.repository.CourseRepository;
import com.example.attendancesystem.repository.UserRepository;
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


import com.example.attendancesystem.dto.ImportResult;
import com.example.attendancesystem.service.AttendanceImportService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

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
    private AttendanceImportService attendanceImportService;

    @Autowired
    private PasswordEncoder passwordEncoder;


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

    /**
     * 打卡页面
     */
    @GetMapping("/attendance/checkin")
    public String checkInPage(Model model) {
        List<Course> courses = courseRepository.findAll();
        model.addAttribute("courses", courses);
        return "attendance-checkin";
    }

    /**
     * 提交打卡（带时间限制和早退判断）
     */
    @PostMapping("/attendance/checkin")
    public String checkIn(@RequestParam String courseId,
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
            LocalTime startTime = course.getStartTime();
            LocalTime endTime = course.getEndTime();

            // 默认值处理（如果没有设置时间）
            if (startTime == null) startTime = LocalTime.of(9, 0);
            if (endTime == null) endTime = LocalTime.of(17, 0);

            // 计算可打卡时间范围：开始前15分钟 到 开始后30分钟
            LocalTime earliestTime = startTime.minusMinutes(15);
            LocalTime latestTime = startTime.plusMinutes(30);

            // 判断是否在可打卡时间内
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

            // 判断状态：迟到、早退、正常
            String status;
            if (nowTime.isAfter(startTime)) {
                status = "LATE";  // 迟到
            } else {
                status = "NORMAL"; // 正常
            }

            // 创建考勤记录
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

            // 成功提示
            String successMsg = status.equals("LATE") ? "打卡成功（迟到）" : "打卡成功";
            redirectAttributes.addFlashAttribute("successMsg", successMsg);
            return "redirect:/attendance/list";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "打卡失败：" + e.getMessage());
            return "redirect:/attendance/checkin";
        }
    }

    /**
     * 考勤记录列表
     */
    @GetMapping("/attendance/list")
    public String list(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        String username = getCurrentUsername();

        Specification<Attendance> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("studentId"), username));

            if (startDate != null && !startDate.isEmpty()) {
                LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
                predicates.add(cb.greaterThanOrEqualTo(root.get("checkInTime"), start));
            }
            if (endDate != null && !endDate.isEmpty()) {
                LocalDateTime end = LocalDate.parse(endDate).plusDays(1).atStartOfDay();
                predicates.add(cb.lessThan(root.get("checkInTime"), end));
            }
            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "checkInTime"));
        Page<Attendance> attendancePage = attendanceRepository.findAll(spec, pageable);

        model.addAttribute("records", attendancePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", attendancePage.getTotalPages());
        model.addAttribute("size", size);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("status", status);

        return "attendance-list";
    }

    @Value("${file.upload.path:D:/uploads/attendance/}")
    private String uploadPath;

    /**
     * 批量导入页面
     */
    @GetMapping("/attendance/import")
    public String importPage() {
        return "attendance-import";
    }

    /**
     * 处理文件上传和导入
     */
    @PostMapping("/attendance/import")
    public String importFile(@RequestParam("file") MultipartFile file,
                             RedirectAttributes redirectAttributes) {

        // 1. 验证文件是否为空
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMsg", "请选择文件");
            return "redirect:/attendance/import";
        }

        // 2. 验证文件大小（不超过10MB）
        if (file.getSize() > 10 * 1024 * 1024) {
            redirectAttributes.addFlashAttribute("errorMsg", "文件大小不能超过10MB");
            return "redirect:/attendance/import";
        }

        // 3. 验证文件格式
        if (!attendanceImportService.isValidExcelFile(file)) {
            redirectAttributes.addFlashAttribute("errorMsg", "文件格式不正确，请上传 .xlsx 或 .xls 文件");
            return "redirect:/attendance/import";
        }

        try {
            // 4. 解析Excel并导入数据
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

    /**
     * 下载 Excel 模板
     */
    @GetMapping("/template/attendance.xlsx")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=attendance_template.xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("考勤记录");

            // 创建标题行
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
            }

            // 添加示例数据
            Row exampleRow = sheet.createRow(1);
            exampleRow.createCell(0).setCellValue("2024001");
            exampleRow.createCell(1).setCellValue("CS101");
            exampleRow.createCell(2).setCellValue("2024-05-26 09:30:00");
            exampleRow.createCell(3).setCellValue("NORMAL");
            exampleRow.createCell(4).setCellValue("正常打卡");

            // 调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.setColumnWidth(i, 5000);
            }

            workbook.write(response.getOutputStream());
        }
    }


}