package com.swim.controller;

import com.swim.common.Result;
import com.swim.dto.BindPhoneRequest;
import com.swim.dto.LoginResponse;
import com.swim.dto.WxLoginRequest;
import com.swim.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/wx-login")
    public Result<LoginResponse> wxLogin(@RequestBody WxLoginRequest req) {
        return Result.ok(authService.wxLogin(req.getCode()));
    }

    @PostMapping("/bind-phone")
    public Result<?> bindPhone(@RequestAttribute("userId") Long userId,
                               @RequestBody BindPhoneRequest req) {
        return Result.ok(authService.bindPhone(userId, req));
    }
}
