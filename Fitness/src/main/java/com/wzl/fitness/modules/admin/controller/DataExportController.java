package com.wzl.fitness.modules.admin.controller;

import com.wzl.fitness.annotation.RequireAdmin;
import com.wzl.fitness.modules.admin.service.DataExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 数据导出控制器
 * 提供用户数据、训练记录、营养记录的Excel导出功能
 * 
 * @see Requirements 6.2 - 支持通过配置文件启用/禁用模块
 */
@RestController
@RequestMapping("/api/v1/admin/export")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "数据导出", description = "数据导出接口")
@ConditionalOnProperty(
    prefix = "fitness.modules.admin",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class DataExportController {

    private final DataExportService dataExportService;
    
    private static final String EXCEL_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final DateTimeFormatter FILE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 导出所有用户数据
     */
    @GetMapping("/users")
    @RequireAdmin
    @Operation(
            summary = "导出用户数据", 
            description = "导出所有用户数据到Excel文件，包含用户基本信息"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "导出成功",
                    content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            )
    })
    public ResponseEntity<byte[]> exportUsers() {
        log.info("请求导出用户数据");
        
        byte[] excelData = dataExportService.exportUsersToExcel();
        String filename = "users_" + LocalDate.now().format(FILE_DATE_FORMATTER) + ".xlsx";
        
        return createExcelResponse(excelData, filename);
    }

    /**
     * 导出训练记录
     */
    @GetMapping("/training-records/{userId}")
    @RequireAdmin
    @Operation(
            summary = "导出训练记录", 
            description = "导出指定用户在指定日期范围内的训练记录到Excel文件"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "导出成功",
                    content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            )
    })
    public ResponseEntity<byte[]> exportTrainingRecords(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "开始日期 (yyyy-MM-dd)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期 (yyyy-MM-dd)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("请求导出训练记录, userId={}, startDate={}, endDate={}", userId, startDate, endDate);
        
        byte[] excelData = dataExportService.exportTrainingRecordsToExcel(userId, startDate, endDate);
        String filename = "training_records_" + userId + "_" + 
                         startDate.format(FILE_DATE_FORMATTER) + "_" + 
                         endDate.format(FILE_DATE_FORMATTER) + ".xlsx";
        
        return createExcelResponse(excelData, filename);
    }

    /**
     * 导出营养记录
     */
    @GetMapping("/nutrition-records/{userId}")
    @RequireAdmin
    @Operation(
            summary = "导出营养记录", 
            description = "导出指定用户在指定日期范围内的营养记录到Excel文件"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "导出成功",
                    content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            )
    })
    public ResponseEntity<byte[]> exportNutritionRecords(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "开始日期 (yyyy-MM-dd)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期 (yyyy-MM-dd)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("请求导出营养记录, userId={}, startDate={}, endDate={}", userId, startDate, endDate);
        
        byte[] excelData = dataExportService.exportNutritionRecordsToExcel(userId, startDate, endDate);
        String filename = "nutrition_records_" + userId + "_" + 
                         startDate.format(FILE_DATE_FORMATTER) + "_" + 
                         endDate.format(FILE_DATE_FORMATTER) + ".xlsx";
        
        return createExcelResponse(excelData, filename);
    }

    /**
     * 导出系统统计数据
     */
    @GetMapping("/system-stats")
    @RequireAdmin
    @Operation(
            summary = "导出系统统计", 
            description = "导出系统统计数据到Excel文件，包含用户统计、训练统计等"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "导出成功",
                    content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            )
    })
    public ResponseEntity<byte[]> exportSystemStats() {
        log.info("请求导出系统统计数据");
        
        byte[] excelData = dataExportService.exportSystemStatsToExcel();
        String filename = "system_stats_" + LocalDate.now().format(FILE_DATE_FORMATTER) + ".xlsx";
        
        return createExcelResponse(excelData, filename);
    }

    /**
     * 创建Excel文件响应
     */
    private ResponseEntity<byte[]> createExcelResponse(byte[] data, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(EXCEL_CONTENT_TYPE));
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(data.length);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(data);
    }
}
