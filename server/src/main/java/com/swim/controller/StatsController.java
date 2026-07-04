package com.swim.controller;

import com.swim.common.Result;
import com.swim.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {
    private final StatsService statsService;

    @GetMapping("/summary")
    public Result<Map<String, Object>> summary(@RequestAttribute("userId") Long userId,
                                               @RequestParam(defaultValue = "week") String period) {
        return Result.ok(statsService.summary(userId, period));
    }

    @GetMapping("/trend")
    public Result<List<Map<String, Object>>> trend(@RequestAttribute("userId") Long userId,
                                                   @RequestParam(defaultValue = "30") int days) {
        return Result.ok(statsService.trend(userId, days));
    }

    @GetMapping("/pr")
    public Result<List<Map<String, Object>>> pr(@RequestAttribute("userId") Long userId) {
        return Result.ok(statsService.pr(userId));
    }

    @GetMapping("/heatmap")
    public Result<List<Map<String, Object>>> heatmap(@RequestAttribute("userId") Long userId,
                                                     @RequestParam(defaultValue = "2026") int year) {
        return Result.ok(statsService.heatmap(userId, year));
    }

    @GetMapping("/stroke-trend")
    public Result<List<Map<String, Object>>> strokeTrend(@RequestAttribute("userId") Long userId,
                                                         @RequestParam String stroke,
                                                         @RequestParam int distance,
                                                         @RequestParam(defaultValue = "4") int weeks) {
        return Result.ok(statsService.strokeTrend(userId, stroke, distance, weeks));
    }

    @GetMapping("/stroke-overview")
    public Result<List<Map<String, Object>>> strokeOverview(@RequestAttribute("userId") Long userId,
                                                             @RequestParam(defaultValue = "4") int weeks) {
        return Result.ok(statsService.strokeOverview(userId, weeks));
    }

    @GetMapping("/stroke-improve")
    public Result<List<Map<String, Object>>> strokeImprove(@RequestAttribute("userId") Long userId,
                                                           @RequestParam(defaultValue = "100") int distance,
                                                           @RequestParam(defaultValue = "4") int weeks) {
        return Result.ok(statsService.strokeImproveRank(userId, distance, weeks));
    }
}
