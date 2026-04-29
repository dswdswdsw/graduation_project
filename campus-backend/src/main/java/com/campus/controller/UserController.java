package com.campus.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.annotation.RequireRole;
import com.campus.common.Result;
import com.campus.dto.PasswordDTO;
import com.campus.entity.User;
import com.campus.service.UserService;
import com.campus.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @GetMapping("/info")
    public Result<User> getUserInfo(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        User user = userService.getUserInfo(userId);
        return Result.success(user);
    }

    @PutMapping("/info")
    public Result<Void> updateUserInfo(@RequestHeader("Authorization") String token, @RequestBody User user) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        user.setId(userId);
        user.setPassword(null);
        user.setRole(null);
        user.setStatus(null);
        userService.updateUser(user);
        return Result.success("更新成功", null);
    }

    @PutMapping("/password")
    public Result<Void> changePassword(@RequestHeader("Authorization") String token, @Valid @RequestBody PasswordDTO dto) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        userService.changePassword(userId, dto);
        return Result.success("密码修改成功", null);
    }

    @GetMapping("/list")
    @RequireRole({"admin"})
    public Result<Page<User>> listUsers(
            @RequestParam(defaultValue = "") String role,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(userService.listUsers(role, keyword, page, size));
    }

    @PutMapping("/{id}")
    @RequireRole({"admin"})
    public Result<Void> updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        userService.updateUser(user);
        return Result.success("更新成功", null);
    }

    @PutMapping("/{id}/reset-password")
    @RequireRole({"admin"})
    public Result<Void> resetPassword(@PathVariable Long id) {
        userService.resetPassword(id);
        return Result.success("密码已重置", null);
    }
}
