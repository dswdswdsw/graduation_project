package com.campus.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("classroom")
public class Classroom extends BaseEntity {

    private String name;
    private String building;
    private Integer capacity;
    private String type;
    private String facilities;
    private Integer status;
}
