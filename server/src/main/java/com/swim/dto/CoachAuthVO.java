package com.swim.dto;



import lombok.Data;



import java.time.LocalDateTime;



@Data

public class CoachAuthVO {

    private Long id;

    private String coachName;

    private String coachPhone;

    private LocalDateTime createdAt;

}

