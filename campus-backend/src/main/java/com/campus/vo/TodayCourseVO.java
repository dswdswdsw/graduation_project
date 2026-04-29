package com.campus.vo;

import lombok.Data;

@Data
public class TodayCourseVO {

    private Long courseId;
    private String courseName;
    private String code;
    private Integer weekday;
    private Integer startSection;
    private Integer endSection;
    private String location;
    private String teacherName;
    private Integer currentCount;
    private Integer maxStudents;
    private String courseType;
}
