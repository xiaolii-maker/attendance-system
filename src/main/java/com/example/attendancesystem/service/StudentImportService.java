package com.example.attendancesystem.service;

import com.example.attendancesystem.dto.ImportResult;
import com.example.attendancesystem.entity.ClassInfo;
import com.example.attendancesystem.entity.Student;
import com.example.attendancesystem.entity.User;
import com.example.attendancesystem.repository.ClassInfoRepository;
import com.example.attendancesystem.repository.StudentRepository;
import com.example.attendancesystem.repository.UserRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;

@Service
public class StudentImportService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ClassInfoRepository classInfoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean isValidExcelFile(MultipartFile file) {
        String filename = file.getOriginalFilename();
        return filename != null && (filename.endsWith(".xlsx") || filename.endsWith(".xls"));
    }

    public ImportResult importStudents(MultipartFile file) {
        ImportResult result = new ImportResult();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                result.addErrorMessage("Excel文件第一个工作表不存在");
                return result;
            }

            for (int i = 3; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    String studentId = getCellValue(row.getCell(0)).trim();   // A列：学号
                    String name = getCellValue(row.getCell(1)).trim();        // B列：姓名
                    String className = getCellValue(row.getCell(2)).trim();   // C列：班级
                    String major = getCellValue(row.getCell(3)).trim();       // D列：专业

                    if (studentId.isEmpty() || name.isEmpty()) {
                        result.incrementFail();
                        result.addErrorMessage("第" + (i + 1) + "行：学号或姓名不能为空");
                        continue;
                    }

                    // 1. 保存到 student 表（修正：班级用 className，专业用 major）
                    Student student = studentRepository.findById(studentId).orElse(new Student());
                    student.setStudentId(studentId);
                    student.setName(name);
                    student.setClassName(className);      // ✅ 班级 = className
                    student.setMajor(major);              // ✅ 专业 = major
                    student.setCreateTime(LocalDateTime.now());
                    studentRepository.save(student);

                    // 2. 同步到 user 表（如果不存在）
                    if (!userRepository.existsByUsername(studentId)) {
                        User user = new User();
                        user.setUsername(studentId);
                        user.setPassword(passwordEncoder.encode("123456"));
                        user.setRealName(name);
                        user.setRole("STUDENT");
                        user.setCreateTime(LocalDateTime.now());
                        userRepository.save(user);
                    }

                    // 3. 自动创建班级（如果不存在）✅ 用 className 创建班级
                    if (!className.isEmpty() && !classInfoRepository.existsByClassName(className)) {
                        ClassInfo newClass = new ClassInfo();
                        newClass.setClassName(className);
                        newClass.setMajor(major);
                        newClass.setCreateTime(LocalDateTime.now());
                        classInfoRepository.save(newClass);
                    }

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

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                double numValue = cell.getNumericCellValue();
                if (numValue == (long) numValue) {
                    return String.valueOf((long) numValue);
                } else {
                    return String.valueOf(numValue);
                }
            default:
                return "";
        }
    }
}