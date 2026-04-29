package com.campus.controller;

import com.campus.common.Result;
import com.campus.service.DashboardService;
import com.campus.util.JwtUtil;
import com.campus.vo.DashboardVO;
import com.campus.vo.TodayCourseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final JwtUtil jwtUtil;

    @GetMapping("/student")
    public Result<DashboardVO> getStudentDashboard(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        return Result.success(dashboardService.getStudentDashboard(userId));
    }

    @GetMapping("/teacher")
    public Result<DashboardVO> getTeacherDashboard(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        return Result.success(dashboardService.getTeacherDashboard(userId));
    }

    @GetMapping("/admin")
    public Result<DashboardVO> getAdminDashboard() {
        return Result.success(dashboardService.getAdminDashboard());
    }

    @GetMapping("/today-courses")
    public Result<List<TodayCourseVO>> getTodayCourses(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);
        return Result.success(dashboardService.getTodayCourses(userId, role));
    }
}
