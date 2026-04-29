package com.campus.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course_selection")
public class CourseSelection extends BaseEntity {

    private Long studentId;
    private Long courseId;
    private Integer status;
}
