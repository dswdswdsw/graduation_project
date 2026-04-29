package com.campus.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CourseVO {

    private Long id;
    private String code;
    private String name;
    private Long teacherId;
    private String teacherName;
    private BigDecimal credit;
    private Integer hours;
    private String semester;
    private Integer weekday;
    private Integer startSection;
    private Integer endSection;
    private String location;
    private String locationDisplay;
    private Integer maxStudents;
    private Integer currentCount;
    private Integer remainCount;
    private String description;
    private String courseType;
    private Integer status;
}
