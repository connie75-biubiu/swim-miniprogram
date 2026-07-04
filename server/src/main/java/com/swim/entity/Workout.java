package com.swim.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("workout")
public class Workout {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private LocalDate date;
    private String stroke;
    private Integer poolType;
    private Integer sourceType;
    private Integer totalDistance;
    private Integer totalDuration;
    private BigDecimal avgPace;
    private String note;
    private LocalDateTime createdAt;
}
