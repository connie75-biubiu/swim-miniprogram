package com.swim.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class WorkoutRequest {
    private LocalDate date;
    private String stroke;
    private Integer poolType;
    private Integer sourceType;
    private String note;
    private List<SplitRequest> splits;
}
