package com.campus.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("score_change_log")
public class ScoreChangeLog {

    private Long id;
    private Long scoreId;
    private Long operatorId;
    private BigDecimal oldScore;
    private BigDecimal newScore;
    private String reason;
    private LocalDateTime createTime;
}
