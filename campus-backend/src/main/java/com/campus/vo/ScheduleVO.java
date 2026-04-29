package com.campus.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ScheduleVO {

    private Long courseId;
    private String courseName;
    private String code;
    private Integer weekday;
    private Integer startSection;
    private Integer endSection;
    private String location;
    private String locationDisplay;
    private String teacherName;
    private String classNames;
    private BigDecimal credit;
    private Integer hours;
    private String courseType;
    private Integer currentCount;
    private Integer maxStudents;
}
