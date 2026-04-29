package com.campus.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course")
public class Course extends BaseEntity {

    private String code;
    private String name;
    private Long teacherId;
    private BigDecimal credit;
    private Integer hours;
    private String semester;
    private Integer weekday;
    private Integer startSection;
    private Integer endSection;
    private String location;
    private Integer maxStudents;
    private Integer currentCount;
    private Integer version;
    private String description;
    private String courseType;
    private Integer status;
}
