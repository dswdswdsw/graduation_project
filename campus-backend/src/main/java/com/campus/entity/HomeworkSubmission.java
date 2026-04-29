package com.campus.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("homework_submission")
public class HomeworkSubmission extends BaseEntity {

    private Long homeworkId;
    private Long studentId;
    private String content;
    private Long attachmentId;
    private BigDecimal score;
    private String comment;
    private Integer status;
    private LocalDateTime submitTime;
    private LocalDateTime gradeTime;
}
