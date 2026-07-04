package com.swim.dto;

import lombok.Data;

import java.util.List;

@Data
public class StrokeOrderRequest {
    private List<String> order;
}
