package com.campus.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class HomeworkSubmissionVO {

    private Long id;
    private Long homeworkId;
    private Long studentId;
    private String studentName;
    private String userNo;
    private String content;
    private Long attachmentId;
    private String attachmentName;
    private BigDecimal score;
    private String comment;
    private Integer status;
    private LocalDateTime submitTime;
    private LocalDateTime gradeTime;
}
