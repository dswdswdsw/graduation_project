package com.campus.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.Result;
import com.campus.entity.CourseSelection;
import com.campus.service.CourseSelectionService;
import com.campus.util.JwtUtil;
import com.campus.vo.MyCourseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/course-selection")
@RequiredArgsConstructor
public class CourseSelectionController {

    private final CourseSelectionService courseSelectionService;
    private final JwtUtil jwtUtil;

    @PostMapping("/select/{courseId}")
    public Result<Void> selectCourse(@RequestHeader("Authorization") String token, @PathVariable Long courseId) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        courseSelectionService.selectCourse(userId, courseId);
        return Result.success("选课成功", null);
    }

    @PostMapping("/drop/{courseId}")
    public Result<Void> dropCourse(@RequestHeader("Authorization") String token, @PathVariable Long courseId) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        courseSelectionService.dropCourse(userId, courseId);
        return Result.success("退选成功", null);
    }

    @GetMapping("/my")
    public Result<Page<MyCourseVO>> listMySelections(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "") String semester,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        return Result.success(courseSelectionService.listMySelections(userId, semester, page, size));
    }

    @GetMapping("/admin/classes")
    public Result<List<String>> listAllClasses() {
        return Result.success(courseSelectionService.listAllClasses());
    }

    @PostMapping("/admin/assign")
    public Result<Integer> batchAssignToClass(@RequestParam Long courseId, @RequestParam String className) {
        int count = courseSelectionService.batchAssignToClass(courseId, className);
        return Result.success("排课成功，为" + className + "的" + count + "名学生添加了课程", count);
    }
}
