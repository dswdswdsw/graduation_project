package com.campus.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("notice")
public class Notice extends BaseEntity {

    private String title;
    private String content;
    private Long publisherId;
    private String category;
    private String targetRole;
    private Integer isTop;
    private LocalDateTime publishTime;
    private Integer status;
}
