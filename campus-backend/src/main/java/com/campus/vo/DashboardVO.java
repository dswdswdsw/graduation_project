package com.campus.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DashboardVO {

    private Integer courseCount;
    private Integer homeworkCount;
    private Integer repairCount;
    private Integer unreadCount;
    private Integer studentCount;
    private Integer pendingScore;
    private Integer userCount;
    private Integer selectionCount;
    private Integer teacherCount;
    private Integer classroomCount;
    private BigDecimal avgPassRate;
    private Integer pendingRepairCount;
    private Integer totalRepairCount;
    private Integer noticeCount;
}
