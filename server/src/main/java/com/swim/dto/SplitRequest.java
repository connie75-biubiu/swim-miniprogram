package com.swim.dto;

import lombok.Data;

@Data
public class SplitRequest {
    private Integer seq;
    private String stroke;
    private Integer distance;
    private Integer duration;
}
