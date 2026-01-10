package com.wzl.fitness.service;

import java.time.LocalDate;

/**
 * 数据导出服务接口
 * 提供用户数据、训练记录、营养记录的Excel导出功能
 * 
 * @deprecated 请使用 {@link com.wzl.fitness.modules.admin.service.DataExportService}
 */
@Deprecated
public interface DataExportService {
    
    /**
     * 导出所有用户数据到Excel
     * @return Excel文件字节数组
     */
    byte[] exportUsersToExcel();
    
    /**
     * 导出指定用户的训练记录到Excel
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return Excel文件字节数组
     */
    byte[] exportTrainingRecordsToExcel(Long userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 导出指定用户的营养记录到Excel
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return Excel文件字节数组
     */
    byte[] exportNutritionRecordsToExcel(Long userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 导出系统统计数据到Excel
     * @return Excel文件字节数组
     */
    byte[] exportSystemStatsToExcel();
}
