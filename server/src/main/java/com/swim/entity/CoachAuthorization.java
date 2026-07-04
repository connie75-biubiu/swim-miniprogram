package com.swim.entity;



import com.baomidou.mybatisplus.annotation.IdType;

import com.baomidou.mybatisplus.annotation.TableId;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;



import java.time.LocalDateTime;



@Data

@TableName("coach_authorization")

public class CoachAuthorization {

    @TableId(type = IdType.AUTO)

    private Long id;

    private Long studentId;

    private Long coachId;

    private LocalDateTime createdAt;

}

