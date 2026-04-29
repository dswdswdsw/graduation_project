package com.campus.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_file")
public class SysFile {

    private Long id;
    private String originalName;
    private String storedName;
    private String filePath;
    private Long fileSize;
    private String fileType;
    private String bizType;
    private Long bizId;
    private Long uploadUserId;
    private LocalDateTime createTime;
    private Integer deleted;
}
