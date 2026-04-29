package com.campus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CourseDTO {

    private Long id;

    @NotBlank(message = "课程编号不能为空")
    private String code;

    @NotBlank(message = "课程名称不能为空")
    private String name;

    @NotNull(message = "授课教师不能为空")
    private Long teacherId;

    private BigDecimal credit;
    private Integer hours;

    @NotBlank(message = "学期不能为空")
    private String semester;

    @NotNull(message = "星期几不能为空")
    private Integer weekday;

    @NotNull(message = "开始节次不能为空")
    private Integer startSection;

    @NotNull(message = "结束节次不能为空")
    private Integer endSection;

    private String location;
    private Integer maxStudents;
    private String description;
    private String courseType;
}
