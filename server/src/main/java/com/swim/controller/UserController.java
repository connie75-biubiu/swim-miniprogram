package com.swim.controller;



import com.swim.common.Result;

import com.swim.dto.*;

import com.swim.service.AuthService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;



import java.util.List;



@RestController

@RequestMapping("/api/user")

@RequiredArgsConstructor

public class UserController {

    private final AuthService authService;



    @GetMapping("/profile")

    public Result<ProfileVO> profile(@RequestAttribute("userId") Long userId) {

        return Result.ok(authService.getProfile(userId));

    }



    @PutMapping("/profile")

    public Result<ProfileVO> updateProfile(@RequestAttribute("userId") Long userId,

                                           @RequestBody ProfileUpdateRequest req) {

        return Result.ok(authService.updateProfile(userId, req));

    }



    @PostMapping("/role")

    public Result<ProfileVO> selectRole(@RequestAttribute("userId") Long userId,

                                        @RequestBody RoleSelectRequest req) {

        return Result.ok(authService.selectRole(userId, req));

    }



    @GetMapping("/coach-auth")

    public Result<List<CoachAuthVO>> listCoachAuth(@RequestAttribute("userId") Long userId) {

        return Result.ok(authService.listCoachAuth(userId));

    }



    @PostMapping("/coach-auth")

    public Result<CoachAuthVO> addCoachAuth(@RequestAttribute("userId") Long userId,

                                            @RequestBody CoachAuthRequest req) {

        return Result.ok(authService.addCoachAuth(userId, req));

    }



    @DeleteMapping("/coach-auth/{id}")

    public Result<Void> revokeCoachAuth(@RequestAttribute("userId") Long userId,

                                        @PathVariable Long id) {

        authService.revokeCoachAuth(userId, id);

        return Result.ok(null);

    }



    @GetMapping("/stroke-order")

    public Result<List<String>> getStrokeOrder(@RequestAttribute("userId") Long userId) {

        return Result.ok(authService.getStrokeOrder(userId));

    }



    @PutMapping("/stroke-order")

    public Result<List<String>> updateStrokeOrder(@RequestAttribute("userId") Long userId,

                                                  @RequestBody StrokeOrderRequest req) {

        return Result.ok(authService.updateStrokeOrder(userId, req));

    }

}

