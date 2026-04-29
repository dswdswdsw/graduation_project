package com.campus.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("notice_read")
public class NoticeRead {

    private Long id;
    private Long noticeId;
    private Long userId;
    private LocalDateTime readTime;
}
