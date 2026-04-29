package com.campus.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class HomeworkGradeVO {
    private Long id;
    private Long homeworkId;
    private String title;
    private String courseName;
    private String deadline;
    private BigDecimal totalScore;
    private Long studentId;
    private String studentName;
    private String userNo;
    private String className;
    private Boolean submitted;
    private String content;
    private Long attachmentId;
    private String attachmentName;
    private BigDecimal score;
    private Integer status;
    private LocalDateTime submitTime;
}
