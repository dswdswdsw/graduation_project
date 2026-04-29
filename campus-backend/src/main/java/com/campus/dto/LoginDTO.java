package com.campus.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户登录请求DTO (Data Transfer Object)
 * 
 * <p>用于接收前端传递的用户登录参数。
 * 使用Jakarta Validation进行参数校验，确保必填字段不为空。</p>
 * 
 * <h2>使用场景：</h2>
 * <ul>
 *   <li>用户登录接口 POST /api/auth/login</li>
 *   <li>作为Controller层的请求体（@RequestBody）</li>
 * </ul>
 * 
 * <h2>校验规则：</h2>
 * <ul>
 *   <li>username: 必填，不能为空字符串</li>
 *   <li>password: 必填，不能为空字符串</li>
 * </ul>
 * 
 * @see AuthController#login(LoginDTO) 登录接口
 */
@Data
public class LoginDTO {

    /**
     * 用户名
     * 用户的登录账号，必填字段
     * @NotBlank 校验：不允许为null、空字符串或纯空白字符
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 密码
     * 用户的登录密码，必填字段
     * 注意：传输时应使用HTTPS加密，密码本身不在此处加密
     * @NotBlank 校验：不允许为null、空字符串或纯空白字符
     */
    @NotBlank(message = "密码不能为空")
    private String password;
}
