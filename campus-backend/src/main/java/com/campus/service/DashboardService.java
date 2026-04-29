package com.campus.service;

import com.campus.vo.DashboardVO;
import com.campus.vo.TodayCourseVO;

import java.util.List;

public interface DashboardService {

    DashboardVO getStudentDashboard(Long userId);

    DashboardVO getTeacherDashboard(Long userId);

    DashboardVO getAdminDashboard();

    List<TodayCourseVO> getTodayCourses(Long userId, String role);
}
