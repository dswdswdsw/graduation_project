package com.campus.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.annotation.RequireRole;
import com.campus.common.Result;
import com.campus.dto.HomeworkDTO;
import com.campus.dto.HomeworkGradeDTO;
import com.campus.service.HomeworkService;
import com.campus.util.JwtUtil;
import com.campus.vo.HomeworkGradeVO;
import com.campus.vo.HomeworkSubmissionVO;
import com.campus.vo.HomeworkVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import java.util.List;

@RestController
@RequestMapping("/api/homework")
@RequiredArgsConstructor
public class HomeworkController {

    private final HomeworkService homeworkService;
    private final JwtUtil jwtUtil;

    @GetMapping("/list")
    public Result<Page<HomeworkVO>> listHomework(
            @RequestParam(required = false) Long courseId,
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long teacherId = null;
        if (token != null) {
            Long userId = jwtUtil.getUserIdFromToken(token);
            String role = jwtUtil.getRoleFromToken(token);
            if ("teacher".equals(role)) {
                teacherId = userId;
            }
        }
        return Result.success(homeworkService.listHomework(courseId, null, teacherId, page, size));
    }

    @GetMapping("/student")
    public Result<Page<HomeworkVO>> listStudentHomework(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) Long courseId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        return Result.success(homeworkService.listHomework(courseId, userId, null, page, size));
    }

    @GetMapping("/{id}")
    public Result<HomeworkVO> getHomeworkDetail(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);
        Long studentId = "student".equals(role) ? userId : null;
        Long teacherId = "teacher".equals(role) ? userId : null;
        return Result.success(homeworkService.getHomeworkDetail(id, studentId, teacherId));
    }

    @PostMapping
    @RequireRole({"admin", "teacher"})
    public Result<Void> addHomework(@RequestHeader("Authorization") String token, @Valid @RequestBody HomeworkDTO dto) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        homeworkService.addHomework(dto, userId);
        return Result.success("发布成功", null);
    }

    @PutMapping
    @RequireRole({"admin", "teacher"})
    public Result<Void> updateHomework(@RequestHeader("Authorization") String token, @Valid @RequestBody HomeworkDTO dto) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);
        Long operatorId = "admin".equals(role) ? null : userId;
        homeworkService.updateHomework(dto, operatorId);
        return Result.success("更新成功", null);
    }

    @DeleteMapping("/{id}")
    @RequireRole({"admin", "teacher"})
    public Result<Void> deleteHomework(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);
        Long operatorId = "admin".equals(role) ? null : userId;
        homeworkService.deleteHomework(id, operatorId);
        return Result.success("删除成功", null);
    }

    @PostMapping("/{id}/submit")
    public Result<Void> submitHomework(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @RequestBody java.util.Map<String, Object> body) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        String content = body.get("content") != null ? body.get("content").toString().trim() : "";
        Long attachmentId = body.get("attachmentId") != null ? Long.valueOf(body.get("attachmentId").toString()) : null;
        if (content.isEmpty() && attachmentId == null) {
            return Result.fail("作业内容和附件不能同时为空");
        }
        homeworkService.submitHomework(id, userId, content, attachmentId);
        return Result.success("提交成功", null);
    }

    @GetMapping("/{id}/submissions")
    @RequireRole({"admin", "teacher"})
    public Result<Page<HomeworkSubmissionVO>> listSubmissions(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);
        Long teacherId = "admin".equals(role) ? null : userId;
        return Result.success(homeworkService.listSubmissions(id, teacherId, page, size));
    }

    @GetMapping("/{id}/grade-detail")
    @RequireRole({"admin", "teacher"})
    public Result<HomeworkGradeVO> getGradeDetail(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);
        Long teacherId = "admin".equals(role) ? null : userId;
        return Result.success(homeworkService.getGradeDetail(id, teacherId));
    }

    @GetMapping("/{id}/students")
    @RequireRole({"admin", "teacher"})
    public Result<List<HomeworkGradeVO>> listHomeworkStudents(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);
        Long teacherId = "admin".equals(role) ? null : userId;
        return Result.success(homeworkService.listHomeworkStudents(id, teacherId));
    }

    @PostMapping("/grade")
    @RequireRole({"admin", "teacher"})
    public Result<Void> gradeSubmission(@RequestHeader("Authorization") String token, @Valid @RequestBody HomeworkGradeDTO dto) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);
        Long teacherId = "admin".equals(role) ? null : userId;
        homeworkService.gradeSubmission(dto, teacherId);
        return Result.success("批改成功", null);
    }

    @PostMapping("/return")
    @RequireRole({"admin", "teacher"})
    public Result<Void> returnSubmission(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> body) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);
        Long teacherId = "admin".equals(role) ? null : userId;
        Long submissionId = ((Number) body.get("submissionId")).longValue();
        String comment = (String) body.getOrDefault("comment", "");
        homeworkService.returnSubmission(submissionId, comment, teacherId);
        return Result.success("已打回", null);
    }
}