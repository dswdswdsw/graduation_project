package com.campus.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.annotation.RequireRole;
import com.campus.common.Result;
import com.campus.dto.RepairDTO;
import com.campus.service.RepairService;
import com.campus.util.JwtUtil;
import com.campus.vo.RepairVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/repair")
@RequiredArgsConstructor
public class RepairController {

    private final RepairService repairService;
    private final JwtUtil jwtUtil;

    @GetMapping("/my")
    public Result<Page<RepairVO>> listMyRepairs(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "") String category,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        return Result.success(repairService.listRepairs(userId, category, status, page, size));
    }

    @GetMapping("/{id}")
    public Result<RepairVO> getRepairDetail(@PathVariable Long id) {
        return Result.success(repairService.getRepairDetail(id));
    }

    @PostMapping
    public Result<Void> addRepair(@RequestHeader("Authorization") String token, @Valid @RequestBody RepairDTO dto) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        repairService.addRepair(dto, userId);
        return Result.success("报修提交成功", null);
    }

    @PostMapping("/{id}/handle")
    @RequireRole({"admin"})
    public Result<Void> handleRepair(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        Long handlerId = jwtUtil.getUserIdFromToken(token);
        repairService.handleRepair(id, handlerId, body.get("result"));
        return Result.success("处理成功", null);
    }

    @PostMapping("/{id}/rate")
    public Result<Void> rateRepair(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        Integer rating = (Integer) body.get("rating");
        String content = (String) body.get("content");
        repairService.rateRepair(id, userId, rating, content);
        return Result.success("评价成功", null);
    }

    @GetMapping("/all")
    @RequireRole({"admin"})
    public Result<Page<RepairVO>> listAllRepairs(
            @RequestParam(defaultValue = "") String category,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(repairService.listAllRepairs(category, status, page, size));
    }
}
