package com.campus.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MyCourseVO {
    private Long selectionId;
    private Long courseId;
    private String code;
    private String name;
    private String teacherName;
    private BigDecimal credit;
    private Integer hours;
    private String semester;
    private Integer weekday;
    private Integer startSection;
    private Integer endSection;
    private String location;
    private String courseType;
    private LocalDateTime createTime;
}
