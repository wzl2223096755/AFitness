package com.wzl.fitness.service.impl;

import com.wzl.fitness.entity.NutritionRecord;
import com.wzl.fitness.entity.TrainingRecord;
import com.wzl.fitness.entity.User;
import com.wzl.fitness.repository.NutritionRecordRepository;
import com.wzl.fitness.repository.TrainingRecordRepository;
import com.wzl.fitness.repository.UserRepository;
import com.wzl.fitness.service.DataExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 数据导出服务实现
 * 使用Apache POI生成Excel文件
 * 
 * @deprecated 请使用 {@link com.wzl.fitness.modules.admin.service.impl.DataExportServiceImpl}
 */
@Deprecated
@Service("legacyDataExportService")
@RequiredArgsConstructor
@Slf4j
public class DataExportServiceImpl implements DataExportService {
    
    private final UserRepository userRepository;
    private final TrainingRecordRepository trainingRecordRepository;
    private final NutritionRecordRepository nutritionRecordRepository;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    public byte[] exportUsersToExcel() {
        log.info("开始导出用户数据");
        List<User> users = userRepository.findAll();
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("用户数据");
            
            // 创建表头样式
            CellStyle headerStyle = createHeaderStyle(workbook);
            
            // 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "用户名", "邮箱", "角色", "年龄", "性别", "身高(cm)", "体重(kg)", 
                               "经验等级", "积分", "创建时间", "最后登录时间"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // 填充数据
            int rowNum = 1;
            for (User user : users) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(user.getId());
                row.createCell(1).setCellValue(user.getUsername());
                row.createCell(2).setCellValue(user.getEmail() != null ? user.getEmail() : "");
                row.createCell(3).setCellValue(user.getRole() != null ? user.getRole().name() : "");
                row.createCell(4).setCellValue(user.getAge() != null ? user.getAge() : 0);
                row.createCell(5).setCellValue(user.getGender() != null ? user.getGender() : "");
                row.createCell(6).setCellValue(user.getHeight() != null ? user.getHeight() : 0);
                row.createCell(7).setCellValue(user.getWeight() != null ? user.getWeight() : 0);
                row.createCell(8).setCellValue(user.getExperienceLevel() != null ? user.getExperienceLevel() : "");
                row.createCell(9).setCellValue(user.getPoints() != null ? user.getPoints() : 0);
                row.createCell(10).setCellValue(user.getCreatedAt() != null ? 
                    user.getCreatedAt().format(DATETIME_FORMATTER) : "");
                row.createCell(11).setCellValue(user.getLastLoginAt() != null ? 
                    user.getLastLoginAt().format(DATETIME_FORMATTER) : "");
            }
            
            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            return writeWorkbookToBytes(workbook);
        } catch (IOException e) {
            log.error("导出用户数据失败", e);
            throw new RuntimeException("导出用户数据失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public byte[] exportTrainingRecordsToExcel(Long userId, LocalDate startDate, LocalDate endDate) {
        log.info("开始导出训练记录, userId={}, startDate={}, endDate={}", userId, startDate, endDate);
        
        List<TrainingRecord> records = trainingRecordRepository
            .findByUserIdAndTrainingDateBetweenOrderByTrainingDateDesc(userId, startDate, endDate);
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("训练记录");
            
            // 创建表头样式
            CellStyle headerStyle = createHeaderStyle(workbook);
            
            // 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "训练日期", "运动名称", "组数", "次数", "重量(kg)", 
                               "时长(分钟)", "总训练量", "训练压力", "备注"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // 填充数据
            int rowNum = 1;
            for (TrainingRecord record : records) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(record.getId());
                row.createCell(1).setCellValue(record.getTrainingDate() != null ? 
                    record.getTrainingDate().format(DATE_FORMATTER) : "");
                row.createCell(2).setCellValue(record.getExerciseName() != null ? record.getExerciseName() : "");
                row.createCell(3).setCellValue(record.getSets() != null ? record.getSets() : 0);
                row.createCell(4).setCellValue(record.getReps() != null ? record.getReps() : 0);
                row.createCell(5).setCellValue(record.getWeight() != null ? record.getWeight() : 0);
                row.createCell(6).setCellValue(record.getDuration() != null ? record.getDuration() : 0);
                row.createCell(7).setCellValue(record.getTotalVolume() != null ? record.getTotalVolume() : 
                    record.getCalculatedTotalVolume());
                row.createCell(8).setCellValue(record.getTrainingStress() != null ? record.getTrainingStress() : 0);
                row.createCell(9).setCellValue(record.getNotes() != null ? record.getNotes() : "");
            }
            
            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            return writeWorkbookToBytes(workbook);
        } catch (IOException e) {
            log.error("导出训练记录失败", e);
            throw new RuntimeException("导出训练记录失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public byte[] exportNutritionRecordsToExcel(Long userId, LocalDate startDate, LocalDate endDate) {
        log.info("开始导出营养记录, userId={}, startDate={}, endDate={}", userId, startDate, endDate);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在: " + userId));
        
        List<NutritionRecord> records = nutritionRecordRepository
            .findByUserAndRecordDateBetweenOrderByRecordDateAscMealTypeAsc(user, startDate, endDate);
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("营养记录");
            
            // 创建表头样式
            CellStyle headerStyle = createHeaderStyle(workbook);
            
            // 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "记录日期", "餐次", "食物名称", "份量(g)", "热量(kcal)", 
                               "蛋白质(g)", "碳水(g)", "脂肪(g)", "纤维(g)", "糖分(g)", "钠(mg)", "备注"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // 填充数据
            int rowNum = 1;
            for (NutritionRecord record : records) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(record.getId());
                row.createCell(1).setCellValue(record.getRecordDate() != null ? 
                    record.getRecordDate().format(DATE_FORMATTER) : "");
                row.createCell(2).setCellValue(record.getMealType() != null ? record.getMealType() : "");
                row.createCell(3).setCellValue(record.getFoodName() != null ? record.getFoodName() : "");
                row.createCell(4).setCellValue(record.getAmount() != null ? record.getAmount() : 0);
                row.createCell(5).setCellValue(record.getCalories() != null ? record.getCalories() : 0);
                row.createCell(6).setCellValue(record.getProtein() != null ? record.getProtein() : 0);
                row.createCell(7).setCellValue(record.getCarbs() != null ? record.getCarbs() : 0);
                row.createCell(8).setCellValue(record.getFat() != null ? record.getFat() : 0);
                row.createCell(9).setCellValue(record.getFiber() != null ? record.getFiber() : 0);
                row.createCell(10).setCellValue(record.getSugar() != null ? record.getSugar() : 0);
                row.createCell(11).setCellValue(record.getSodium() != null ? record.getSodium() : 0);
                row.createCell(12).setCellValue(record.getNotes() != null ? record.getNotes() : "");
            }
            
            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            return writeWorkbookToBytes(workbook);
        } catch (IOException e) {
            log.error("导出营养记录失败", e);
            throw new RuntimeException("导出营养记录失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public byte[] exportSystemStatsToExcel() {
        log.info("开始导出系统统计数据");
        
        try (Workbook workbook = new XSSFWorkbook()) {
            // 用户统计Sheet
            createUserStatsSheet(workbook);
            
            // 训练统计Sheet
            createTrainingStatsSheet(workbook);
            
            return writeWorkbookToBytes(workbook);
        } catch (IOException e) {
            log.error("导出系统统计数据失败", e);
            throw new RuntimeException("导出系统统计数据失败: " + e.getMessage(), e);
        }
    }
    
    private void createUserStatsSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("用户统计");
        CellStyle headerStyle = createHeaderStyle(workbook);
        
        // 表头
        Row headerRow = sheet.createRow(0);
        String[] headers = {"统计项", "数值"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // 统计数据
        long totalUsers = userRepository.count();
        long adminCount = userRepository.countByRole(com.wzl.fitness.entity.Role.ADMIN);
        long userCount = userRepository.countByRole(com.wzl.fitness.entity.Role.USER);
        
        int rowNum = 1;
        createStatRow(sheet, rowNum++, "总用户数", totalUsers);
        createStatRow(sheet, rowNum++, "管理员数量", adminCount);
        createStatRow(sheet, rowNum++, "普通用户数量", userCount);
        
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }
    
    private void createTrainingStatsSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("训练统计");
        CellStyle headerStyle = createHeaderStyle(workbook);
        
        // 表头
        Row headerRow = sheet.createRow(0);
        String[] headers = {"统计项", "数值"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // 统计数据
        long totalRecords = trainingRecordRepository.count();
        
        int rowNum = 1;
        createStatRow(sheet, rowNum++, "总训练记录数", totalRecords);
        createStatRow(sheet, rowNum++, "导出时间", java.time.LocalDateTime.now().format(DATETIME_FORMATTER));
        
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }
    
    private void createStatRow(Sheet sheet, int rowNum, String label, Object value) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        if (value instanceof Number) {
            row.createCell(1).setCellValue(((Number) value).doubleValue());
        } else {
            row.createCell(1).setCellValue(value.toString());
        }
    }
    
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    
    private byte[] writeWorkbookToBytes(Workbook workbook) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}
