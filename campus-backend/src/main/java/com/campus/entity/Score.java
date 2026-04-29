package com.campus.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("score")
public class Score extends BaseEntity {

    private Long studentId;
    private Long courseId;
    private Long teacherId;
    private BigDecimal usualScore;
    private BigDecimal examScore;
    private BigDecimal finalScore;
    private BigDecimal gradePoint;
    private String semester;
    private String remark;
}
