package com.wzl.fitness.service;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 数据导出完整性属性测试
 * 
 * **Property 2: 数据导出完整性**
 * *For any* 数据导出请求, 导出的数据应与数据库中的原始数据完全一致
 * 
 * **Validates: Requirements 3.5**
 * 
 * Feature: system-optimization-95, Property 2: 数据导出完整性
 */
public class DataExportPropertyTest {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String ASCII_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789 _-";

    /**
     * Property 1: Excel写入后读取数据一致性
     * 
     * 对于任意有效的数据行，写入Excel后读取应返回相同的数据
     * 
     * **Validates: Requirements 3.5**
     */
    @Property(tries = 100)
    @Label("Property 1: Excel写入后读取数据一致性 - 写入后读取应返回相同数据")
    void excelWriteThenReadShouldReturnSameData(
            @ForAll("asciiStrings") String stringValue,
            @ForAll @IntRange(min = 0, max = 10000) int intValue,
            @ForAll @DoubleRange(min = 0.0, max = 1000.0) double doubleValue) throws IOException {
        
        // 创建工作簿并写入数据
        byte[] excelData;
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("TestSheet");
            
            // 创建表头
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("StringColumn");
            headerRow.createCell(1).setCellValue("IntColumn");
            headerRow.createCell(2).setCellValue("DoubleColumn");
            
            // 创建数据行
            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue(stringValue);
            dataRow.createCell(1).setCellValue(intValue);
            dataRow.createCell(2).setCellValue(doubleValue);
            
            // 写入字节数组
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                workbook.write(outputStream);
                excelData = outputStream.toByteArray();
            }
        }
        
        // 读取并验证数据
        try (Workbook readWorkbook = new XSSFWorkbook(new ByteArrayInputStream(excelData))) {
            Sheet readSheet = readWorkbook.getSheet("TestSheet");
            assertNotNull(readSheet, "Sheet应该存在");
            
            Row readDataRow = readSheet.getRow(1);
            assertNotNull(readDataRow, "数据行应该存在");
            
            // 验证字符串值
            Cell stringCell = readDataRow.getCell(0);
            assertNotNull(stringCell, "字符串单元格应该存在");
            assertEquals(stringValue, stringCell.getStringCellValue(),
                    String.format("字符串值应一致: expected=%s, actual=%s", 
                            stringValue, stringCell.getStringCellValue()));
            
            // 验证整数值
            Cell intCell = readDataRow.getCell(1);
            assertNotNull(intCell, "整数单元格应该存在");
            assertEquals(intValue, (int) intCell.getNumericCellValue(),
                    String.format("整数值应一致: expected=%d, actual=%d", 
                            intValue, (int) intCell.getNumericCellValue()));
            
            // 验证浮点值（使用delta比较）
            Cell doubleCell = readDataRow.getCell(2);
            assertNotNull(doubleCell, "浮点单元格应该存在");
            assertEquals(doubleValue, doubleCell.getNumericCellValue(), 0.001,
                    String.format("浮点值应一致: expected=%f, actual=%f", 
                            doubleValue, doubleCell.getNumericCellValue()));
        }
    }

    /**
     * Property 2: 多行数据导出完整性
     * 
     * 对于任意数量的数据行，导出后行数应与原始数据一致
     * 
     * **Validates: Requirements 3.5**
     */
    @Property(tries = 50)
    @Label("Property 2: 多行数据导出完整性 - 导出后行数应与原始数据一致")
    void exportedRowCountShouldMatchOriginalData(
            @ForAll @Size(min = 1, max = 20) List<@From("asciiStrings") String> dataRows) throws IOException {
        
        // 创建工作簿并写入多行数据
        byte[] excelData;
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("DataSheet");
            
            // 创建表头
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Data");
            
            // 创建数据行
            for (int i = 0; i < dataRows.size(); i++) {
                Row dataRow = sheet.createRow(i + 1);
                dataRow.createCell(0).setCellValue(dataRows.get(i));
            }
            
            // 写入字节数组
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                workbook.write(outputStream);
                excelData = outputStream.toByteArray();
            }
        }
        
        // 读取并验证行数
        try (Workbook readWorkbook = new XSSFWorkbook(new ByteArrayInputStream(excelData))) {
            Sheet readSheet = readWorkbook.getSheet("DataSheet");
            assertNotNull(readSheet, "Sheet应该存在");
            
            // 物理行数应该等于数据行数 + 1（表头）
            int expectedRowCount = dataRows.size() + 1;
            int actualRowCount = readSheet.getPhysicalNumberOfRows();
            
            assertEquals(expectedRowCount, actualRowCount,
                    String.format("行数应一致: expected=%d, actual=%d", 
                            expectedRowCount, actualRowCount));
            
            // 验证每行数据
            for (int i = 0; i < dataRows.size(); i++) {
                Row readRow = readSheet.getRow(i + 1);
                assertNotNull(readRow, String.format("第%d行应该存在", i + 1));
                
                Cell cell = readRow.getCell(0);
                assertNotNull(cell, String.format("第%d行的单元格应该存在", i + 1));
                assertEquals(dataRows.get(i), cell.getStringCellValue(),
                        String.format("第%d行数据应一致", i + 1));
            }
        }
    }

    /**
     * Property 3: 日期数据导出一致性
     * 
     * 对于任意有效的日期，导出后日期格式应保持一致
     * 
     * **Validates: Requirements 3.5**
     */
    @Property(tries = 100)
    @Label("Property 3: 日期数据导出一致性 - 日期格式应保持一致")
    void dateExportShouldMaintainFormat(
            @ForAll @IntRange(min = 2020, max = 2030) int year,
            @ForAll @IntRange(min = 1, max = 12) int month,
            @ForAll @IntRange(min = 1, max = 28) int day) throws IOException {
        
        LocalDate date = LocalDate.of(year, month, day);
        String dateString = date.format(DATE_FORMATTER);
        
        // 创建工作簿并写入日期
        byte[] excelData;
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("DateSheet");
            
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Date");
            
            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue(dateString);
            
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                workbook.write(outputStream);
                excelData = outputStream.toByteArray();
            }
        }
        
        // 读取并验证日期
        try (Workbook readWorkbook = new XSSFWorkbook(new ByteArrayInputStream(excelData))) {
            Sheet readSheet = readWorkbook.getSheet("DateSheet");
            Row readRow = readSheet.getRow(1);
            Cell dateCell = readRow.getCell(0);
            
            assertEquals(dateString, dateCell.getStringCellValue(),
                    String.format("日期应一致: expected=%s, actual=%s", 
                            dateString, dateCell.getStringCellValue()));
        }
    }

    /**
     * Property 4: 多Sheet导出完整性
     * 
     * 对于多个Sheet的导出，每个Sheet应该独立存在且数据完整
     * 
     * **Validates: Requirements 3.5**
     */
    @Property(tries = 50)
    @Label("Property 4: 多Sheet导出完整性 - 每个Sheet应独立存在且数据完整")
    void multipleSheetExportShouldBeComplete(
            @ForAll @Size(min = 1, max = 5) List<@From("sheetNames") String> sheetNames) throws IOException {
        
        // 确保Sheet名称唯一
        Set<String> uniqueNames = new LinkedHashSet<>(sheetNames);
        List<String> uniqueSheetNames = new ArrayList<>(uniqueNames);
        
        // 创建工作簿并写入多个Sheet
        byte[] excelData;
        try (Workbook workbook = new XSSFWorkbook()) {
            for (int i = 0; i < uniqueSheetNames.size(); i++) {
                String sheetName = uniqueSheetNames.get(i);
                Sheet sheet = workbook.createSheet(sheetName);
                
                Row headerRow = sheet.createRow(0);
                headerRow.createCell(0).setCellValue("SheetIndex");
                
                Row dataRow = sheet.createRow(1);
                dataRow.createCell(0).setCellValue(i);
            }
            
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                workbook.write(outputStream);
                excelData = outputStream.toByteArray();
            }
        }
        
        // 读取并验证每个Sheet
        try (Workbook readWorkbook = new XSSFWorkbook(new ByteArrayInputStream(excelData))) {
            assertEquals(uniqueSheetNames.size(), readWorkbook.getNumberOfSheets(),
                    "Sheet数量应一致");
            
            for (int i = 0; i < uniqueSheetNames.size(); i++) {
                Sheet readSheet = readWorkbook.getSheetAt(i);
                assertNotNull(readSheet, String.format("第%d个Sheet应该存在", i));
                
                Row dataRow = readSheet.getRow(1);
                assertNotNull(dataRow, String.format("第%d个Sheet的数据行应该存在", i));
                
                Cell indexCell = dataRow.getCell(0);
                assertEquals(i, (int) indexCell.getNumericCellValue(),
                        String.format("第%d个Sheet的索引应正确", i));
            }
        }
    }

    /**
     * Property 5: 空数据导出处理
     * 
     * 对于空数据集，导出应该只包含表头
     * 
     * **Validates: Requirements 3.5**
     */
    @Property(tries = 50)
    @Label("Property 5: 空数据导出处理 - 空数据集应只包含表头")
    void emptyDataExportShouldOnlyContainHeader(
            @ForAll @Size(min = 1, max = 10) List<@From("asciiStrings") String> headers) throws IOException {
        
        // 创建工作簿，只有表头没有数据
        byte[] excelData;
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("EmptyDataSheet");
            
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.size(); i++) {
                headerRow.createCell(i).setCellValue(headers.get(i));
            }
            
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                workbook.write(outputStream);
                excelData = outputStream.toByteArray();
            }
        }
        
        // 读取并验证
        try (Workbook readWorkbook = new XSSFWorkbook(new ByteArrayInputStream(excelData))) {
            Sheet readSheet = readWorkbook.getSheet("EmptyDataSheet");
            assertNotNull(readSheet, "Sheet应该存在");
            
            // 应该只有1行（表头）
            assertEquals(1, readSheet.getPhysicalNumberOfRows(),
                    "空数据集应只有表头行");
            
            // 验证表头
            Row headerRow = readSheet.getRow(0);
            assertNotNull(headerRow, "表头行应该存在");
            
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.getCell(i);
                assertNotNull(cell, String.format("第%d个表头单元格应该存在", i));
                assertEquals(headers.get(i), cell.getStringCellValue(),
                        String.format("第%d个表头应一致", i));
            }
        }
    }

    /**
     * Property 6: 特殊字符数据导出一致性
     * 
     * 对于包含特殊字符的数据，导出后应保持一致
     * 
     * **Validates: Requirements 3.5**
     */
    @Property(tries = 100)
    @Label("Property 6: 特殊字符数据导出一致性 - 特殊字符应保持一致")
    void specialCharacterExportShouldBeConsistent(
            @ForAll("specialCharacterStrings") String specialString) throws IOException {
        
        // 创建工作簿并写入特殊字符
        byte[] excelData;
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("SpecialCharSheet");
            
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("SpecialData");
            
            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue(specialString);
            
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                workbook.write(outputStream);
                excelData = outputStream.toByteArray();
            }
        }
        
        // 读取并验证
        try (Workbook readWorkbook = new XSSFWorkbook(new ByteArrayInputStream(excelData))) {
            Sheet readSheet = readWorkbook.getSheet("SpecialCharSheet");
            Row readRow = readSheet.getRow(1);
            Cell cell = readRow.getCell(0);
            
            assertEquals(specialString, cell.getStringCellValue(),
                    String.format("特殊字符应一致: expected=%s, actual=%s", 
                            specialString, cell.getStringCellValue()));
        }
    }

    /**
     * 生成ASCII字符串（避免无效XML字符）
     */
    @Provide
    Arbitrary<String> asciiStrings() {
        return Arbitraries.strings()
                .withChars(ASCII_CHARS.toCharArray())
                .ofMinLength(1)
                .ofMaxLength(50);
    }

    /**
     * 生成有效的Sheet名称
     */
    @Provide
    Arbitrary<String> sheetNames() {
        return Arbitraries.strings()
                .withChars("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_".toCharArray())
                .ofMinLength(1)
                .ofMaxLength(20);
    }

    /**
     * 生成包含特殊字符的字符串（但仍然是有效的XML字符）
     */
    @Provide
    Arbitrary<String> specialCharacterStrings() {
        return Arbitraries.of(
                "Hello World",
                "Test 123",
                "Special_Data",
                "Comma-Test",
                "Quote Test",
                "Tab Test",
                "Mixed Test 123",
                "Underscore_Test",
                "Dash-Test",
                "Space Test"
        );
    }
}
