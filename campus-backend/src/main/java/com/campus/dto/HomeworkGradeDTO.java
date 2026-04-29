package com.campus.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class HomeworkGradeDTO {

    @NotNull(message = "提交ID不能为空")
    private Long submissionId;

    private BigDecimal score;
    private String comment;
}
