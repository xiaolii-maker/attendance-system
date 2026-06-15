package com.example.attendancesystem.controller;

import com.example.attendancesystem.dto.ImportResult;
import com.example.attendancesystem.service.StudentImportService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
public class StudentImportController {

    @Autowired
    private StudentImportService studentImportService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 学生批量导入页面
     */
    @GetMapping("/student/import")
    public String importStudentPage() {
        return "student-import";
    }

    /**
     * 处理学生批量导入
     */
    @PostMapping("/student/import")
    public String importStudents(@RequestParam("file") MultipartFile file,
                                 RedirectAttributes redirectAttributes) {

        // 1. 验证文件是否为空
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMsg", "请选择文件");
            return "redirect:/student/import";
        }

        // 2. 验证文件大小（不超过10MB）
        if (file.getSize() > 10 * 1024 * 1024) {
            redirectAttributes.addFlashAttribute("errorMsg", "文件大小不能超过10MB");
            return "redirect:/student/import";
        }

        // 3. 验证文件格式
        if (!studentImportService.isValidExcelFile(file)) {
            redirectAttributes.addFlashAttribute("errorMsg", "文件格式不正确，请上传 .xlsx 或 .xls 文件");
            return "redirect:/student/import";
        }

        try {
            // 4. 解析Excel并导入数据
            ImportResult result = studentImportService.importStudents(file);

            String successMsg = String.format("导入完成！成功：%d条，失败：%d条",
                    result.getSuccessCount(), result.getFailCount());
            redirectAttributes.addFlashAttribute("successMsg", successMsg);

            if (result.getFailCount() > 0) {
                redirectAttributes.addFlashAttribute("errorMsg", result.getErrorMessages());
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "导入失败：" + e.getMessage());
        }

        return "redirect:/student/import";
    }

    /**
     * 下载学生导入模板
     */
    @GetMapping("/template/student.xlsx")
    public void downloadStudentTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=student_template.xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("学生名单");

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"学号", "姓名", "班级", "专业", "性别", "年龄"};
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
            exampleRow.createCell(1).setCellValue("张三");
            exampleRow.createCell(2).setCellValue("人工智能1班");
            exampleRow.createCell(3).setCellValue("人工智能");
            exampleRow.createCell(4).setCellValue("男");
            exampleRow.createCell(5).setCellValue("20");

            // 第二行示例数据
            Row exampleRow2 = sheet.createRow(2);
            exampleRow2.createCell(0).setCellValue("2024002");
            exampleRow2.createCell(1).setCellValue("李四");
            exampleRow2.createCell(2).setCellValue("人工智能1班");
            exampleRow2.createCell(3).setCellValue("人工智能");
            exampleRow2.createCell(4).setCellValue("女");
            exampleRow2.createCell(5).setCellValue("19");

            // 调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.setColumnWidth(i, 5000);
            }

            workbook.write(response.getOutputStream());
            response.getOutputStream().flush();
        }
    }
}