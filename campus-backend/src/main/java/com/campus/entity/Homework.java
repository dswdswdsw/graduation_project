package com.campus.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("homework")
public class Homework extends BaseEntity {

    private Long courseId;
    private Long teacherId;
    private String title;
    private String description;
    private LocalDateTime deadline;
    private Long attachmentId;
    private BigDecimal totalScore;
    private Integer status;
    private Integer submitCount;
}
