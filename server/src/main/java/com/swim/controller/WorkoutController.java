package com.swim.controller;

import com.swim.common.Result;
import com.swim.dto.WorkoutDetailVO;
import com.swim.dto.WorkoutRequest;
import com.swim.entity.Workout;
import com.swim.service.WorkoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class WorkoutController {
    private final WorkoutService workoutService;

    @GetMapping("/workouts")
    public Result<Map<String, Object>> list(@RequestAttribute("userId") Long userId,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "20") int size,
                                            @RequestParam(required = false) Integer sourceType) {
        return Result.ok(workoutService.list(userId, page, size, sourceType));
    }

    @GetMapping("/competitions/summary")
    public Result<List<Map<String, Object>>> competitionSummary(@RequestAttribute("userId") Long userId) {
        return Result.ok(workoutService.competitionSummary(userId));
    }

    @GetMapping("/competitions")
    public Result<Map<String, Object>> listCompetitions(@RequestAttribute("userId") Long userId,
                                                        @RequestParam String stroke,
                                                        @RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "20") int size) {
        return Result.ok(workoutService.listCompetitions(userId, stroke, page, size));
    }

    @PostMapping("/workouts")
    public Result<Workout> create(@RequestAttribute("userId") Long userId,
                                  @RequestBody WorkoutRequest req) {
        return Result.ok(workoutService.create(userId, req));
    }

    @GetMapping("/workouts/{id}")
    public Result<WorkoutDetailVO> get(@RequestAttribute("userId") Long userId,
                                     @PathVariable Long id) {
        return Result.ok(workoutService.get(userId, id));
    }

    @PutMapping("/workouts/{id}")
    public Result<Workout> update(@RequestAttribute("userId") Long userId,
                                  @PathVariable Long id,
                                  @RequestBody WorkoutRequest req) {
        return Result.ok(workoutService.update(userId, id, req));
    }

    @DeleteMapping("/workouts/{id}")
    public Result<Void> delete(@RequestAttribute("userId") Long userId,
                               @PathVariable Long id) {
        workoutService.delete(userId, id);
        return Result.ok(null);
    }
}
