package com.campus.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RepairDTO {

    private Long id;

    @NotBlank(message = "报修标题不能为空")
    private String title;

    @NotBlank(message = "报修描述不能为空")
    private String content;

    @NotBlank(message = "故障地点不能为空")
    private String location;

    @NotBlank(message = "分类不能为空")
    private String category;

    private String urgency;
}
