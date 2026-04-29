package com.campus.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ClassroomVO {

    private Long id;
    private String name;
    private String building;
    private Integer capacity;
    private String type;
    private String facilities;
    private Integer status;
    private BigDecimal occupiedRate;
}
