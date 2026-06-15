package com.example.attendancesystem.controller;

import com.example.attendancesystem.entity.Course;
import com.example.attendancesystem.entity.Leave;
import com.example.attendancesystem.entity.User;
import com.example.attendancesystem.repository.CourseRepository;
import com.example.attendancesystem.repository.LeaveRepository;
import com.example.attendancesystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Controller
public class LeaveController {

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    private String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        return principal.toString();
    }

    @GetMapping("/leave/add")
    public String addLeavePage(Model model) {
        List<Course> courses = courseRepository.findAll();
        model.addAttribute("courses", courses);
        return "leave-add";
    }

    @PostMapping("/leave/submit")
    public String submitLeave(@RequestParam String leaveDate,
                              @RequestParam String courseId,
                              @RequestParam String reason,
                              @RequestParam String leaveType,
                              RedirectAttributes redirectAttributes) {
        String username = getCurrentUsername();
        User user = userRepository.findByUsername(username).orElse(null);
        Course course = courseRepository.findById(courseId).orElse(null);

        if (user == null || course == null) {
            redirectAttributes.addFlashAttribute("errorMsg", "用户或课程不存在");
            return "redirect:/leave/add";
        }

        // 解析请假日期
        LocalDateTime leaveDateTime = LocalDate.parse(leaveDate).atStartOfDay();

        // 检查请假日期
        LocalDateTime now = LocalDateTime.now();
        String typeName = "ADVANCE".equals(leaveType) ? "提前请假" : "补假条";

        if ("ADVANCE".equals(leaveType) && leaveDateTime.isBefore(now)) {
            redirectAttributes.addFlashAttribute("errorMsg", "提前请假只能选择未来的日期");
            return "redirect:/leave/add";
        }

        // 检查是否已经提交过相同的请假申请
        List<Leave> existingLeaves = leaveRepository.findByStudentIdAndCourseIdAndLeaveDate(
                username, courseId, leaveDateTime);
        if (!existingLeaves.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMsg", "该日期的请假申请已提交，请勿重复提交");
            return "redirect:/leave/add";
        }

        Leave leave = new Leave();
        leave.setStudentId(username);
        leave.setStudentName(user.getRealName());
        leave.setCourseId(courseId);
        leave.setCourseName(course.getCourseName());
        leave.setLeaveDate(leaveDateTime);
        leave.setReason(reason);
        leave.setLeaveType(leaveType);
        leave.setStatus("PENDING");
        leave.setCreateTime(LocalDateTime.now());

        leaveRepository.save(leave);

        String successMsg = "ADVANCE".equals(leaveType) ?
                "请假申请已提交，等待审批" : "补假条已提交，等待审批";
        redirectAttributes.addFlashAttribute("successMsg", successMsg);
        return "redirect:/leave/add";
    }

    @GetMapping("/leave/approve")
    public String approvePage(Model model) {
        String username = getCurrentUsername();

        // 待审批申请
        List<Leave> pendingLeaves = leaveRepository.findByStatus("PENDING");
        model.addAttribute("pendingLeaves", pendingLeaves);

        // 我的审批记录（已处理过的）
        List<Leave> myApprovals = leaveRepository.findByStatusIn(Arrays.asList("APPROVED", "REJECTED"));
        model.addAttribute("myApprovals", myApprovals);

        return "leave-approve";
    }

    @PostMapping("/leave/approve/{id}")
    public String approveLeave(@PathVariable Long id,
                               @RequestParam String action,
                               @RequestParam(required = false) String remark,
                               RedirectAttributes redirectAttributes) {
        Leave leave = leaveRepository.findById(id).orElse(null);
        if (leave == null) {
            redirectAttributes.addFlashAttribute("errorMsg", "申请不存在");
            return "redirect:/leave/approve";
        }

        if ("approve".equals(action)) {
            leave.setStatus("APPROVED");
        } else if ("reject".equals(action)) {
            leave.setStatus("REJECTED");
        }
        leave.setApproveTime(LocalDateTime.now());
        leave.setApproveRemark(remark);
        leaveRepository.save(leave);

        redirectAttributes.addFlashAttribute("successMsg", "审批完成");
        return "redirect:/leave/approve";
    }
}