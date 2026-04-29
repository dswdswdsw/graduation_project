package com.campus.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.annotation.RequireRole;
import com.campus.common.Result;
import com.campus.dto.ScoreDTO;
import com.campus.service.ScoreService;
import com.campus.util.JwtUtil;
import com.campus.vo.ScoreStatisticsVO;
import com.campus.vo.ScoreVO;
import java.util.List;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/score")
@RequiredArgsConstructor
public class ScoreController {

    private final ScoreService scoreService;
    private final JwtUtil jwtUtil;

    @GetMapping("/student")
    public Result<Page<ScoreVO>> listStudentScores(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "") String semester,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        return Result.success(scoreService.listStudentScores(userId, semester, page, size));
    }

    @GetMapping("/course/{courseId}")
    @RequireRole({"admin", "teacher"})
    public Result<Page<ScoreVO>> listCourseScores(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "") String semester,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(scoreService.listCourseScores(courseId, semester, page, size));
    }

    @GetMapping("/course/{courseId}/students")
    @RequireRole({"admin", "teacher"})
    public Result<List<ScoreVO>> listCourseStudentsForEntry(@PathVariable Long courseId) {
        return Result.success(scoreService.listCourseStudentsForEntry(courseId));
    }

    @PostMapping
    @RequireRole({"admin", "teacher"})
    public Result<Void> addScore(@RequestHeader("Authorization") String token, @Valid @RequestBody ScoreDTO dto) {
        Long teacherId = jwtUtil.getUserIdFromToken(token);
        scoreService.addScore(dto, teacherId);
        return Result.success("录入成功", null);
    }

    @PutMapping
    @RequireRole({"admin", "teacher"})
    public Result<Void> updateScore(@RequestHeader("Authorization") String token, @Valid @RequestBody ScoreDTO dto) {
        Long teacherId = jwtUtil.getUserIdFromToken(token);
        scoreService.updateScore(dto, teacherId);
        return Result.success("修改成功", null);
    }

    @GetMapping("/student/statistics")
    public Result<ScoreStatisticsVO> getStudentStatistics(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "") String semester) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        return Result.success(scoreService.getStudentStatistics(userId, semester));
    }

    @GetMapping("/course/{courseId}/statistics")
    @RequireRole({"admin", "teacher"})
    public Result<ScoreStatisticsVO> getCourseStatistics(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "") String semester) {
        return Result.success(scoreService.getCourseStatistics(courseId, semester));
    }
}
