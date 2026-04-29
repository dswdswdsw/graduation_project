package com.campus.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.annotation.RequireRole;
import com.campus.common.Result;
import com.campus.dto.CourseDTO;
import com.campus.service.CourseService;
import com.campus.util.JwtUtil;
import com.campus.vo.AdminScheduleVO;
import com.campus.vo.ClassroomVO;
import com.campus.vo.CourseVO;
import com.campus.vo.ScheduleVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/course")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final JwtUtil jwtUtil;

    @GetMapping("/list")
    public Result<Page<CourseVO>> listCourses(
            @RequestParam(defaultValue = "") String semester,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(courseService.listCourses(semester, keyword, page, size));
    }

    @GetMapping("/{id}")
    public Result<CourseVO> getCourseDetail(@PathVariable Long id) {
        return Result.success(courseService.getCourseDetail(id));
    }

    @PostMapping
    @RequireRole({"admin", "teacher"})
    public Result<Void> addCourse(@Valid @RequestBody CourseDTO dto) {
        courseService.addCourse(dto);
        return Result.success("添加成功", null);
    }

    @PutMapping
    @RequireRole({"admin", "teacher"})
    public Result<Void> updateCourse(@Valid @RequestBody CourseDTO dto) {
        courseService.updateCourse(dto);
        return Result.success("更新成功", null);
    }

    @DeleteMapping("/{id}")
    @RequireRole({"admin"})
    public Result<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return Result.success("删除成功", null);
    }

    @GetMapping("/schedule/student")
    public Result<List<ScheduleVO>> getStudentSchedule(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "") String semester) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        return Result.success(courseService.getStudentSchedule(userId, semester));
    }

    @GetMapping("/schedule/teacher")
    public Result<List<ScheduleVO>> getTeacherSchedule(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "") String semester) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        return Result.success(courseService.getTeacherSchedule(userId, semester));
    }

    @GetMapping("/admin/schedule")
    @RequireRole({"admin"})
    public Result<Page<AdminScheduleVO>> listAdminSchedule(
            @RequestParam(defaultValue = "") String semester,
            @RequestParam(defaultValue = "") String className,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(defaultValue = "") String courseType,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size) {
        return Result.success(courseService.listAdminSchedule(semester, className, teacherId, courseType, keyword, page, size));
    }

    @GetMapping("/admin/classes")
    @RequireRole({"admin"})
    public Result<List<String>> listAllClassNames() {
        return Result.success(courseService.listAllClassNames());
    }

    @PutMapping("/batch")
    @RequireRole({"admin"})
    public Result<Integer> batchUpdateCourses(@RequestBody List<CourseDTO> dtoList) {
        int count = courseService.batchUpdateCourses(dtoList);
        return Result.success("批量更新成功，共更新" + count + "门课程", count);
    }

    @GetMapping("/available-classrooms")
    public Result<List<ClassroomVO>> listAvailableClassrooms(
            @RequestParam Integer weekday,
            @RequestParam Integer startSection,
            @RequestParam Integer endSection,
            @RequestParam(defaultValue = "") String semester) {
        return Result.success(courseService.listAvailableClassrooms(weekday, startSection, endSection, semester));
    }

    @PostMapping("/check-conflict")
    public Result<String> checkCourseConflict(@RequestBody CourseDTO dto) {
        String conflict = courseService.checkCourseConflict(dto);
        return Result.success(conflict);
    }
}
