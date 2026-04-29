package com.campus.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NoticeVO {

    private Long id;
    private String title;
    private String content;
    private Long publisherId;
    private String publisherName;
    private String category;
    private String targetRole;
    private Integer isTop;
    private LocalDateTime publishTime;
    private Integer status;
    private Boolean isRead;
    private LocalDateTime createTime;
}
