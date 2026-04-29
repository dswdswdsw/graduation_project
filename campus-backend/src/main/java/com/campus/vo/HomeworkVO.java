package com.campus.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class HomeworkVO {

    private Long id;
    private Long courseId;
    private String courseName;
    private Long teacherId;
    private String teacherName;
    private String title;
    private String description;
    private LocalDateTime deadline;
    private BigDecimal totalScore;
    private Integer status;
    private Integer submitCount;
    private Integer studentCount;
    private Boolean submitted;
    private BigDecimal score;
    private String classNames;
    private LocalDateTime createTime;
    private Long attachmentId;
    private String attachmentName;
    private HomeworkSubmissionVO mySubmission;
}
