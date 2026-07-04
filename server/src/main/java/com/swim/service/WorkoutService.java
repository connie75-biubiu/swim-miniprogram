package com.swim.service;



import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.swim.common.BusinessException;

import com.swim.dto.SplitRequest;

import com.swim.dto.WorkoutDetailVO;

import com.swim.dto.WorkoutRequest;

import com.swim.entity.Split;

import com.swim.entity.Workout;

import com.swim.mapper.SplitMapper;

import com.swim.mapper.WorkoutMapper;

import com.swim.util.PaceCalculator;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;



import java.math.BigDecimal;

import java.time.LocalDateTime;

import java.util.*;



@Service

@RequiredArgsConstructor

public class WorkoutService {

    public static final int SOURCE_TRAIN = 1;

    public static final int SOURCE_COMPETE = 2;

    private static final List<String> STROKES = List.of("自由泳", "蛙泳", "仰泳", "蝶泳");



    private final WorkoutMapper workoutMapper;

    private final SplitMapper splitMapper;



    public Map<String, Object> list(Long userId, int page, int size, Integer sourceType) {

        LambdaQueryWrapper<Workout> qw = new LambdaQueryWrapper<Workout>()

                .eq(Workout::getUserId, userId)

                .orderByDesc(Workout::getDate)

                .orderByDesc(Workout::getId);

        if (sourceType != null) {

            qw.eq(Workout::getSourceType, sourceType);

        }

        Page<Workout> p = workoutMapper.selectPage(new Page<>(page, size), qw);

        Map<String, Object> result = new HashMap<>();

        result.put("records", p.getRecords());

        result.put("total", p.getTotal());

        return result;

    }



    public List<Map<String, Object>> competitionSummary(Long userId) {

        List<Map<String, Object>> summary = new ArrayList<>();

        for (String stroke : STROKES) {

            List<Workout> list = workoutMapper.selectList(new LambdaQueryWrapper<Workout>()

                    .eq(Workout::getUserId, userId)

                    .eq(Workout::getSourceType, SOURCE_COMPETE)

                    .eq(Workout::getStroke, stroke)

                    .orderByDesc(Workout::getDate));

            Map<String, Object> item = new LinkedHashMap<>();

            item.put("stroke", stroke);

            item.put("count", list.size());

            BigDecimal best = list.stream()

                    .map(Workout::getAvgPace)

                    .filter(Objects::nonNull)

                    .min(BigDecimal::compareTo)

                    .orElse(null);

            item.put("bestPace", best);

            summary.add(item);

        }

        return summary;

    }



    public Map<String, Object> listCompetitions(Long userId, String stroke, int page, int size) {

        Page<Workout> p = workoutMapper.selectPage(

                new Page<>(page, size),

                new LambdaQueryWrapper<Workout>()

                        .eq(Workout::getUserId, userId)

                        .eq(Workout::getSourceType, SOURCE_COMPETE)

                        .eq(Workout::getStroke, stroke)

                        .orderByDesc(Workout::getDate)

                        .orderByDesc(Workout::getId));

        Map<String, Object> result = new HashMap<>();

        result.put("records", p.getRecords());

        result.put("total", p.getTotal());

        if (!p.getRecords().isEmpty()) {

            BigDecimal best = p.getRecords().stream()

                    .map(Workout::getAvgPace)

                    .filter(Objects::nonNull)

                    .min(BigDecimal::compareTo)

                    .orElse(null);

            long totalCount = workoutMapper.selectCount(new LambdaQueryWrapper<Workout>()

                    .eq(Workout::getUserId, userId)

                    .eq(Workout::getSourceType, SOURCE_COMPETE)

                    .eq(Workout::getStroke, stroke));

            result.put("totalCount", totalCount);

            result.put("bestPace", best);

        } else {

            result.put("totalCount", 0L);

            result.put("bestPace", null);

        }

        return result;

    }



    public WorkoutDetailVO get(Long userId, Long id) {

        Workout w = requireOwned(userId, id);

        List<Split> splits = splitMapper.selectList(new LambdaQueryWrapper<Split>()

                .eq(Split::getWorkoutId, id)

                .orderByAsc(Split::getSeq));

        return new WorkoutDetailVO(w, splits);

    }



    @Transactional

    public Workout create(Long userId, WorkoutRequest req) {

        validate(req);

        PaceCalculator.Aggregate agg = calcAggregate(req.getSplits());

        Workout w = buildWorkout(userId, req, agg);

        workoutMapper.insert(w);

        saveSplits(w.getId(), req.getSplits());

        return w;

    }



    @Transactional

    public Workout update(Long userId, Long id, WorkoutRequest req) {

        requireOwned(userId, id);

        validate(req);

        PaceCalculator.Aggregate agg = calcAggregate(req.getSplits());

        Workout w = buildWorkout(userId, req, agg);

        w.setId(id);

        workoutMapper.updateById(w);

        splitMapper.delete(new LambdaQueryWrapper<Split>().eq(Split::getWorkoutId, id));

        saveSplits(id, req.getSplits());

        return w;

    }



    @Transactional

    public void delete(Long userId, Long id) {

        requireOwned(userId, id);

        splitMapper.delete(new LambdaQueryWrapper<Split>().eq(Split::getWorkoutId, id));

        workoutMapper.deleteById(id);

    }



    private Workout requireOwned(Long userId, Long id) {

        Workout w = workoutMapper.selectById(id);

        if (w == null || !w.getUserId().equals(userId)) {

            throw new BusinessException(404, "记录不存在");

        }

        return w;

    }



    private void validate(WorkoutRequest req) {

        if (req.getSplits() == null || req.getSplits().isEmpty()) {

            throw new BusinessException(400, "至少1段");

        }

        if (req.getSourceType() != null && req.getSourceType() != SOURCE_TRAIN && req.getSourceType() != SOURCE_COMPETE) {

            throw new BusinessException(400, "数据来源无效");

        }

        if (Integer.valueOf(SOURCE_COMPETE).equals(req.getSourceType())

                && (req.getNote() == null || req.getNote().isBlank())) {

            throw new BusinessException(400, "请填写比赛名称");

        }

        for (SplitRequest s : req.getSplits()) {

            if (s.getDistance() == null || s.getDistance() <= 0

                    || s.getDuration() == null || s.getDuration() <= 0) {

                throw new BusinessException(400, "距离和时长必须大于0");

            }

        }

    }



    private PaceCalculator.Aggregate calcAggregate(List<SplitRequest> splits) {

        int totalDist = splits.stream().mapToInt(SplitRequest::getDistance).sum();

        int totalDur = splits.stream().mapToInt(SplitRequest::getDuration).sum();

        return PaceCalculator.aggregate(totalDist, totalDur);

    }



    private Workout buildWorkout(Long userId, WorkoutRequest req, PaceCalculator.Aggregate agg) {

        Workout w = new Workout();

        w.setUserId(userId);

        w.setDate(req.getDate());

        w.setStroke(req.getStroke());

        w.setPoolType(req.getPoolType());

        w.setSourceType(req.getSourceType() != null ? req.getSourceType() : SOURCE_TRAIN);

        w.setNote(req.getNote());

        w.setTotalDistance(agg.totalDistance());

        w.setTotalDuration(agg.totalDuration());

        w.setAvgPace(agg.avgPace());

        w.setCreatedAt(LocalDateTime.now());

        return w;

    }



    private void saveSplits(Long workoutId, List<SplitRequest> splits) {

        for (SplitRequest sr : splits) {

            Split split = new Split();

            split.setWorkoutId(workoutId);

            split.setSeq(sr.getSeq());

            split.setStroke(sr.getStroke());

            split.setDistance(sr.getDistance());

            split.setDuration(sr.getDuration());

            split.setPace(PaceCalculator.calcPace(sr.getDistance(), sr.getDuration()));

            splitMapper.insert(split);

        }

    }

}

