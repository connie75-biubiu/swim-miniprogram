package com.swim.dto;



import lombok.Data;



@Data

public class ProfileUpdateRequest {

    private String nickname;

    private String phone;

    private Integer gender;

    private String birthMonth;

}

