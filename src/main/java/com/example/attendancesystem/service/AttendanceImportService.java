package com.example.attendancesystem.service;

import com.example.attendancesystem.dto.ImportResult;
import com.example.attendancesystem.entity.Attendance;
import com.example.attendancesystem.entity.Course;
import com.example.attendancesystem.entity.User;
import com.example.attendancesystem.repository.AttendanceRepository;
import com.example.attendancesystem.repository.CourseRepository;
import com.example.attendancesystem.repository.UserRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.apache.poi.ss.usermodel.DateUtil;
import java.time.format.DateTimeFormatter;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

@Service
public class AttendanceImportService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    /**
     * 验证文件格式
     */
    public boolean isValidExcelFile(MultipartFile file) {
        String filename = file.getOriginalFilename();
        return filename != null && (filename.endsWith(".xlsx") || filename.endsWith(".xls"));
    }

    /**
     * 解析 Excel 并导入数据（带去重检查）
     */
    public ImportResult importFromExcel(MultipartFile file) {
        ImportResult result = new ImportResult();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                result.addErrorMessage("Excel文件第一个工作表不存在");
                return result;
            }

            // 跳过标题行，从第二行开始读取
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    // 读取单元格数据
                    String studentId = getCellValue(row.getCell(0)).trim();
                    String courseId = getCellValue(row.getCell(1)).trim();
                    String checkInTimeStr = getCellValue(row.getCell(2)).trim();
                    String status = getCellValue(row.getCell(3)).trim();
                    String remark = getCellValue(row.getCell(4)).trim();

                    // 数据验证
                    if (studentId.isEmpty() || courseId.isEmpty() || checkInTimeStr.isEmpty()) {
                        result.incrementFail();
                        result.addErrorMessage("第" + (i + 1) + "行：学号、课程ID、打卡时间不能为空");
                        continue;
                    }

                    // 验证学生是否存在
                    User user = userRepository.findByUsername(studentId).orElse(null);
                    if (user == null) {
                        result.incrementFail();
                        result.addErrorMessage("第" + (i + 1) + "行：学号 " + studentId + " 不存在");
                        continue;
                    }

                    // 验证课程是否存在
                    Course course = courseRepository.findById(courseId).orElse(null);
                    if (course == null) {
                        result.incrementFail();
                        result.addErrorMessage("第" + (i + 1) + "行：课程ID " + courseId + " 不存在");
                        continue;
                    }

                    // 解析打卡时间
                    LocalDateTime checkInTime = parseDateTime(checkInTimeStr);
                    if (checkInTime == null) {
                        result.incrementFail();
                        result.addErrorMessage("第" + (i + 1) + "行：打卡时间格式错误");
                        continue;
                    }

                    // ========== 去重检查：判断是否已存在相同的考勤记录 ==========
                    boolean exists = attendanceRepository.existsByStudentIdAndCourseIdAndCheckInTime(
                            studentId, courseId, checkInTime);
                    if (exists) {
                        result.incrementFail();
                        result.addErrorMessage("第" + (i + 1) + "行：考勤记录已存在（学号：" + studentId + "，课程：" + courseId + "，时间：" + checkInTimeStr + "），跳过");
                        continue;
                    }

                    // 验证状态
                    if (!status.isEmpty() && !status.equals("NORMAL") && !status.equals("LATE")) {
                        status = "NORMAL";
                    }

                    // 创建考勤记录
                    Attendance attendance = new Attendance();
                    attendance.setStudentId(studentId);
                    attendance.setStudentName(user.getRealName());
                    attendance.setCourseId(courseId);
                    attendance.setCourseName(course.getCourseName());
                    attendance.setCheckInTime(checkInTime);
                    attendance.setCreateTime(LocalDateTime.now());
                    attendance.setStatus(status.isEmpty() ? "NORMAL" : status);
                    attendance.setRemark(remark);

                    attendanceRepository.save(attendance);
                    result.incrementSuccess();

                } catch (Exception e) {
                    result.incrementFail();
                    result.addErrorMessage("第" + (i + 1) + "行：" + e.getMessage());
                }
            }

        } catch (Exception e) {
            result.addErrorMessage("读取Excel文件失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 获取单元格值
     */
    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                // 处理数字类型（Excel中的数字）
                double numValue = cell.getNumericCellValue();
                // 判断是否为整数
                if (numValue == (long) numValue) {
                    return String.valueOf((long) numValue);
                } else {
                    return String.valueOf(numValue);
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }

    /**
     * 解析日期时间
     */
    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) return null;

        // 支持多种格式
        String[] patterns = {
                "yyyy-MM-dd HH:mm:ss",
                "yyyy/MM/dd HH:mm:ss",
                "yyyy-MM-dd HH:mm",
                "yyyy/MM/dd HH:mm",
                "yyyy-M-d H:mm:ss",
                "yyyy/M/d H:mm:ss",
                "yyyy-M-d H:mm",
                "yyyy/M/d H:mm"
        };

        for (String pattern : patterns) {
            try {
                return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern(pattern));
            } catch (Exception e) {
                // 继续尝试下一个格式
            }
        }

        // 如果是 Excel 数字日期格式
        try {
            double excelDate = Double.parseDouble(dateTimeStr);
            LocalDate date = LocalDate.of(1900, 1, 1).plusDays((long) excelDate - 2);
            return date.atStartOfDay();
        } catch (Exception e) {
            // 忽略
        }

        return null;
    }
}