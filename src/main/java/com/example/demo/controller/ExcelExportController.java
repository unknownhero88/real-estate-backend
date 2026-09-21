package com.example.demo.controller;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/admin/export")
@PreAuthorize("hasRole('ADMIN')")
public class ExcelExportController {

    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final InquiryRepository inquiryRepository;

    public ExcelExportController(
            UserRepository userRepository,
            PropertyRepository propertyRepository,
            InquiryRepository inquiryRepository) {
        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
        this.inquiryRepository = inquiryRepository;
    }

    @GetMapping("/users")
    public ResponseEntity<byte[]> exportUsers() throws IOException {
        List<User> users = userRepository.findAll();
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Users");

        // Header
        Row header = sheet.createRow(0);
        String[] headers = {"ID", "Full Name", "Email", "Phone", "Role", "Verified", "Banned", "Created At"};
        CellStyle headerStyle = createHeaderStyle(workbook);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, 5000);
        }

        int rowNum = 1;
        for (User u : users) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(u.getId());
            row.createCell(1).setCellValue(u.getFullName());
            row.createCell(2).setCellValue(u.getEmail());
            row.createCell(3).setCellValue(u.getPhone() != null ? u.getPhone() : "");
            row.createCell(4).setCellValue(u.getRole().name());
            row.createCell(5).setCellValue(u.getIsVerified() ? "Yes" : "No");
            row.createCell(6).setCellValue(u.getIsBanned() ? "Yes" : "No");
            row.createCell(7).setCellValue(u.getCreatedAt() != null ? u.getCreatedAt().toString() : "");
        }

        return buildExcelResponse(workbook, "users_report.xlsx");
    }

    @GetMapping("/properties")
    public ResponseEntity<byte[]> exportProperties() throws IOException {
        List<Property> properties = propertyRepository.findAll();
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Properties");

        Row header = sheet.createRow(0);
        String[] headers = {"ID", "Title", "Type", "Listing Type", "Price", "City", "State", "Bedrooms", "Bathrooms", "Area (sqft)", "Status", "Approved", "Views", "Seller", "Created At"};
        CellStyle headerStyle = createHeaderStyle(workbook);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, 4500);
        }

        int rowNum = 1;
        for (Property p : properties) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(p.getId());
            row.createCell(1).setCellValue(p.getTitle());
            row.createCell(2).setCellValue(p.getType() != null ? p.getType().name() : "");
            row.createCell(3).setCellValue(p.getListingType() != null ? p.getListingType().name() : "");
            row.createCell(4).setCellValue(p.getPrice() != null ? p.getPrice().doubleValue() : 0);
            row.createCell(5).setCellValue(p.getCity());
            row.createCell(6).setCellValue(p.getState());
            row.createCell(7).setCellValue(p.getBedrooms() != null ? p.getBedrooms() : 0);
            row.createCell(8).setCellValue(p.getBathrooms() != null ? p.getBathrooms() : 0);
            row.createCell(9).setCellValue(p.getAreaSqft() != null ? p.getAreaSqft().doubleValue() : 0);
            row.createCell(10).setCellValue(p.getStatus() != null ? p.getStatus().name() : "");
            row.createCell(11).setCellValue(p.getIsApproved() ? "Yes" : "No");
            row.createCell(12).setCellValue(p.getViewCount() != null ? p.getViewCount() : 0);
            row.createCell(13).setCellValue(p.getSeller().getFullName());
            row.createCell(14).setCellValue(p.getCreatedAt() != null ? p.getCreatedAt().toString() : "");
        }

        return buildExcelResponse(workbook, "properties_report.xlsx");
    }

    private CellStyle createHeaderStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private ResponseEntity<byte[]> buildExcelResponse(XSSFWorkbook workbook, String filename) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(out.toByteArray());
    }
}