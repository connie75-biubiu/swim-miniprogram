package com.swim.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("split")
public class Split {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long workoutId;
    private Integer seq;
    private String stroke;
    private Integer distance;
    private Integer duration;
    private BigDecimal pace;
}
