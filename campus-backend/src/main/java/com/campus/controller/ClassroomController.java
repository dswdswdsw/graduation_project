package com.campus.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.annotation.RequireRole;
import com.campus.common.Result;
import com.campus.entity.Classroom;
import com.campus.service.ClassroomService;
import com.campus.vo.ClassroomVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classroom")
@RequiredArgsConstructor
public class ClassroomController {

    private final ClassroomService classroomService;

    @GetMapping("/list")
    public Result<Page<ClassroomVO>> listClassrooms(
            @RequestParam(defaultValue = "") String building,
            @RequestParam(defaultValue = "") String type,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(classroomService.listClassrooms(building, type, keyword, page, size));
    }

    @GetMapping("/available")
    public Result<List<ClassroomVO>> listAvailableClassrooms(
            @RequestParam Integer weekday,
            @RequestParam Integer startSection,
            @RequestParam Integer endSection,
            @RequestParam(defaultValue = "") String semester) {
        return Result.success(classroomService.listAvailableClassrooms(weekday, startSection, endSection, semester));
    }

    @PostMapping
    @RequireRole({"admin"})
    public Result<Void> addClassroom(@RequestBody Classroom classroom) {
        classroomService.addClassroom(classroom);
        return Result.success("添加成功", null);
    }

    @PutMapping
    @RequireRole({"admin"})
    public Result<Void> updateClassroom(@RequestBody Classroom classroom) {
        classroomService.updateClassroom(classroom);
        return Result.success("更新成功", null);
    }

    @DeleteMapping("/{id}")
    @RequireRole({"admin"})
    public Result<Void> deleteClassroom(@PathVariable Long id) {
        classroomService.deleteClassroom(id);
        return Result.success("删除成功", null);
    }

    @GetMapping("/buildings")
    public Result<List<String>> listAllBuildings() {
        return Result.success(classroomService.listAllBuildings());
    }
}
