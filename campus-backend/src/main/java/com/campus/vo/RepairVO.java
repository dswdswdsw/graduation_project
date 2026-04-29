package com.campus.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RepairVO {

    private Long id;
    private Long userId;
    private String userName;
    private String title;
    private String content;
    private String location;
    private String category;
    private String urgency;
    private Integer status;
    private Long handlerId;
    private String handlerName;
    private String handleResult;
    private LocalDateTime handleTime;
    private Integer rating;
    private String ratingContent;
    private LocalDateTime createTime;
}
