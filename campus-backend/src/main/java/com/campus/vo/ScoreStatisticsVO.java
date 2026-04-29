package com.campus.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ScoreStatisticsVO {

    private BigDecimal avgScore;
    private BigDecimal maxScore;
    private BigDecimal minScore;
    private BigDecimal passRate;
    private BigDecimal excellentRate;
    private Integer totalStudents;
    private BigDecimal totalCredits;
    private BigDecimal gpa;
}
