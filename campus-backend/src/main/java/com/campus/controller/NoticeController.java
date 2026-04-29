package com.campus.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.annotation.RequireRole;
import com.campus.common.Result;
import com.campus.dto.NoticeDTO;
import com.campus.service.NoticeService;
import com.campus.util.JwtUtil;
import com.campus.vo.NoticeVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;
    private final JwtUtil jwtUtil;

    @GetMapping("/list")
    public Result<Page<NoticeVO>> listNotices(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "") String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);
        return Result.success(noticeService.listNotices(category, role, userId, page, size));
    }

    @GetMapping("/{id}")
    public Result<NoticeVO> getNoticeDetail(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        return Result.success(noticeService.getNoticeDetail(id, userId));
    }

    @PostMapping
    @RequireRole({"admin", "teacher"})
    public Result<Void> addNotice(@RequestHeader("Authorization") String token, @Valid @RequestBody NoticeDTO dto) {
        Long publisherId = jwtUtil.getUserIdFromToken(token);
        noticeService.addNotice(dto, publisherId);
        return Result.success("发布成功", null);
    }

    @PutMapping
    @RequireRole({"admin", "teacher"})
    public Result<Void> updateNotice(@Valid @RequestBody NoticeDTO dto) {
        noticeService.updateNotice(dto);
        return Result.success("更新成功", null);
    }

    @DeleteMapping("/{id}")
    @RequireRole({"admin"})
    public Result<Void> deleteNotice(@PathVariable Long id) {
        noticeService.deleteNotice(id);
        return Result.success("删除成功", null);
    }

    @PostMapping("/{id}/read")
    public Result<Void> markAsRead(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        noticeService.markAsRead(id, userId);
        return Result.success(null);
    }

    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);
        return Result.success(noticeService.getUnreadCount(userId, role));
    }
}
