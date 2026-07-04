package com.swim.dto;

import com.swim.entity.Split;
import com.swim.entity.Workout;
import lombok.Data;

import java.util.List;

@Data
public class WorkoutDetailVO {
    private Workout workout;
    private List<Split> splits;

    public WorkoutDetailVO(Workout workout, List<Split> splits) {
        this.workout = workout;
        this.splits = splits;
    }
}
