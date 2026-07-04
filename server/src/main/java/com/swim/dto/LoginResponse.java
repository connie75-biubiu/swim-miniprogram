package com.swim.dto;



import lombok.Data;



@Data

public class LoginResponse {

    private String token;

    private boolean needBindPhone;

    private boolean needSelectRole;

    private String role;



    public LoginResponse(String token, boolean needBindPhone, boolean needSelectRole, String role) {

        this.token = token;

        this.needBindPhone = needBindPhone;

        this.needSelectRole = needSelectRole;

        this.role = role;

    }

}

