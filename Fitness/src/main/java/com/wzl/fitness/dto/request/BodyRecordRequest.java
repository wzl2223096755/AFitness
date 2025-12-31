package com.wzl.fitness.dto.request;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BodyRecordRequest {
    private Double weight;
    private Double bodyFat;
    private Double muscleMass;
    private Double waistCircumference;
    private Double hipCircumference;
    private Double chestCircumference;
    private LocalDateTime recordTime;
}
