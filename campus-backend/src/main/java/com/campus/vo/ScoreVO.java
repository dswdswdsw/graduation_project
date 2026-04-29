package com.campus.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ScoreVO {

    private Long id;
    private Long studentId;
    private String studentName;
    private String userNo;
    private String className;
    private Long courseId;
    private String courseName;
    private Long teacherId;
    private String teacherName;
    private BigDecimal usualScore;
    private BigDecimal examScore;
    private BigDecimal finalScore;
    private BigDecimal gradePoint;
    private String semester;
    private String remark;
}
