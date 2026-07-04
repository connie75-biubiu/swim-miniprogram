package com.swim.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.swim.entity.Split;
import com.swim.entity.Workout;
import com.swim.mapper.SplitMapper;
import com.swim.mapper.WorkoutMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {
    private static final int SOURCE_TRAIN = 1;

    private final WorkoutMapper workoutMapper;
    private final SplitMapper splitMapper;

    public Map<String, Object> summary(Long userId, String period) {
        LocalDate start = "month".equals(period)
                ? LocalDate.now().withDayOfMonth(1)
                : LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        List<Workout> list = workoutMapper.selectList(trainingQuery(userId)
                .ge(Workout::getDate, start));
        int totalDistance = list.stream().mapToInt(Workout::getTotalDistance).sum();
        int totalDuration = list.stream().mapToInt(Workout::getTotalDuration).sum();
        return Map.of(
                "totalDistance", totalDistance,
                "totalDuration", totalDuration,
                "count", list.size());
    }

    public List<Map<String, Object>> trend(Long userId, int days) {
        LocalDate start = LocalDate.now().minusDays(days - 1L);
        List<Workout> list = workoutMapper.selectList(trainingQuery(userId)
                .ge(Workout::getDate, start)
                .orderByAsc(Workout::getDate));
        Map<LocalDate, Integer> grouped = list.stream().collect(Collectors.groupingBy(
                Workout::getDate,
                Collectors.summingInt(Workout::getTotalDistance)));
        List<Map<String, Object>> result = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(LocalDate.now()); d = d.plusDays(1)) {
            result.add(Map.of("date", d.toString(), "distance", grouped.getOrDefault(d, 0)));
        }
        return result;
    }

    public List<Map<String, Object>> pr(Long userId) {
        List<Workout> workouts = workoutMapper.selectList(trainingQuery(userId));
        if (workouts.isEmpty()) {
            return List.of();
        }
        List<Long> ids = workouts.stream().map(Workout::getId).toList();
        List<Split> splits = splitMapper.selectList(new LambdaQueryWrapper<Split>()
                .in(Split::getWorkoutId, ids));
        Map<String, BigDecimal> bestPace = new HashMap<>();
        Map<String, Integer> maxDistance = new HashMap<>();
        for (Split s : splits) {
            if (s.getPace() != null) {
                bestPace.merge(s.getStroke(), s.getPace(), (a, b) -> a.compareTo(b) < 0 ? a : b);
            }
            maxDistance.merge(s.getStroke(), s.getDistance(), Math::max);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (String stroke : bestPace.keySet()) {
            result.add(Map.of(
                    "stroke", stroke,
                    "bestPace", bestPace.get(stroke),
                    "maxDistance", maxDistance.getOrDefault(stroke, 0)));
        }
        return result;
    }

    public List<Map<String, Object>> heatmap(Long userId, int year) {
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);
        List<Workout> list = workoutMapper.selectList(trainingQuery(userId)
                .between(Workout::getDate, start, end));
        Map<LocalDate, Integer> grouped = list.stream().collect(Collectors.groupingBy(
                Workout::getDate,
                Collectors.summingInt(Workout::getTotalDistance)));
        return grouped.entrySet().stream()
                .map(e -> Map.<String, Object>of("date", e.getKey().toString(), "distance", e.getValue()))
                .sorted(Comparator.comparing(m -> (String) m.get("date")))
                .toList();
    }

    public List<Map<String, Object>> strokeTrend(Long userId, String stroke, int distance, int weeks) {
        LocalDate start = LocalDate.now().minusWeeks(weeks - 1L)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        List<Workout> workouts = workoutMapper.selectList(trainingQuery(userId)
                .ge(Workout::getDate, start));
        if (workouts.isEmpty()) {
            return List.of();
        }
        Map<Long, LocalDate> dateMap = workouts.stream()
                .collect(Collectors.toMap(Workout::getId, Workout::getDate));
        List<Long> ids = workouts.stream().map(Workout::getId).toList();
        List<Split> splits = splitMapper.selectList(new LambdaQueryWrapper<Split>()
                .in(Split::getWorkoutId, ids)
                .eq(Split::getStroke, stroke)
                .eq(Split::getDistance, distance));
        Map<LocalDate, List<BigDecimal>> weekPaces = new TreeMap<>();
        for (Split s : splits) {
            if (s.getPace() == null) continue;
            LocalDate date = dateMap.get(s.getWorkoutId());
            LocalDate weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            weekPaces.computeIfAbsent(weekStart, k -> new ArrayList<>()).add(s.getPace());
        }
        List<Map<String, Object>> result = new ArrayList<>();
        weekPaces.forEach((week, paces) -> {
            BigDecimal avg = paces.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(paces.size()), 2, java.math.RoundingMode.HALF_UP);
            result.add(Map.of("week", week.toString(), "avgPace", avg));
        });
        return result;
    }

    private static final List<String> STROKES = List.of("自由泳", "蛙泳", "仰泳", "蝶泳");
    private static final List<Integer> DISTANCES = List.of(50, 100, 200, 400);

    public List<Map<String, Object>> strokeOverview(Long userId, int weeks) {
        List<Map<String, Object>> overview = new ArrayList<>();
        for (String stroke : STROKES) {
            for (int distance : DISTANCES) {
                List<Map<String, Object>> trend = strokeTrend(userId, stroke, distance, weeks);
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("stroke", stroke);
                item.put("distance", distance);
                item.put("trend", trend);
                if (trend.size() >= 2) {
                    BigDecimal latest = (BigDecimal) trend.get(trend.size() - 1).get("avgPace");
                    BigDecimal prev = (BigDecimal) trend.get(trend.size() - 2).get("avgPace");
                    item.put("latestPace", latest);
                    item.put("changeSeconds", latest.subtract(prev));
                } else if (trend.size() == 1) {
                    item.put("latestPace", trend.get(0).get("avgPace"));
                    item.put("changeSeconds", BigDecimal.ZERO);
                } else {
                    item.put("latestPace", null);
                    item.put("changeSeconds", null);
                }
                overview.add(item);
            }
        }
        return overview;
    }

    public List<Map<String, Object>> strokeImproveRank(Long userId, int distance, int weeks) {
        List<Map<String, Object>> rank = new ArrayList<>();
        for (String stroke : STROKES) {
            List<Map<String, Object>> trend = strokeTrend(userId, stroke, distance, weeks);
            if (trend.size() < 2) continue;
            BigDecimal latest = (BigDecimal) trend.get(trend.size() - 1).get("avgPace");
            BigDecimal first = (BigDecimal) trend.get(0).get("avgPace");
            BigDecimal improve = first.subtract(latest);
            rank.add(Map.of("stroke", stroke, "improveSeconds", improve));
        }
        rank.sort((a, b) -> ((BigDecimal) b.get("improveSeconds")).compareTo((BigDecimal) a.get("improveSeconds")));
        return rank;
    }

    private LambdaQueryWrapper<Workout> trainingQuery(Long userId) {
        return new LambdaQueryWrapper<Workout>()
                .eq(Workout::getUserId, userId)
                .eq(Workout::getSourceType, SOURCE_TRAIN);
    }
}
