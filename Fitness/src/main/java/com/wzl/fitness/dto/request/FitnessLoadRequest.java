package com.wzl.fitness.dto.request;

import lombok.Data;

@Data
public class FitnessLoadRequest {
    private Integer duration;
    private Integer intensity;
    private String type;
}
