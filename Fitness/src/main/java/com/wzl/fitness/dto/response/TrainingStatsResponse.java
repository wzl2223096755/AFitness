package com.wzl.fitness.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingStatsResponse {
    private Long totalRecords;
    private Double totalVolume;
    private Long totalDuration;
}
