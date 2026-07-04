package com.swim.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String openid;
    private String phone;
    private String nickname;
    private String avatar;
    private String role;
    private Integer gender;
    private String birthMonth;
    private String strokeOrder;
    private LocalDateTime createdAt;
}
