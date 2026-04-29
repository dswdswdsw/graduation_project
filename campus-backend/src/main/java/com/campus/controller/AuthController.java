package com.campus.controller;

import com.campus.common.Result;
import com.campus.dto.LoginDTO;
import com.campus.dto.RegisterDTO;
import com.campus.entity.User;
import com.campus.service.UserService;
import com.campus.vo.LoginVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        LoginVO vo = userService.login(dto.getUsername(), dto.getPassword());
        return Result.success(vo);
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setRealName(dto.getRealName());
        user.setRole(dto.getRole() != null ? dto.getRole() : "student");
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        userService.register(user);
        return Result.success("注册成功", null);
    }
}
