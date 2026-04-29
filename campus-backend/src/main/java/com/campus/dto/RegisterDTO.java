package com.campus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 用户注册请求DTO (Data Transfer Object)
 * 
 * <p>用于接收前端传递的用户注册参数。
 * 包含严格的参数校验规则，确保用户输入的数据符合系统要求。</p>
 * 
 * <h2>使用场景：</h2>
 * <ul>
 *   <li>用户注册接口 POST /api/auth/register</li>
 *   <li>作为Controller层的请求体（@RequestBody）</li>
 * </ul>
 * 
 * <h2>校验规则：</h2>
 * <ul>
 *   <li>username: 必填，用户名不能为空</li>
 *   <li>password: 必填，必须包含字母和数字，长度6-20位</li>
 *   <li>realName: 必填，真实姓名不能为空</li>
 *   <li>phone: 可选，如果填写需符合手机号格式（1开头，11位数字）</li>
 *   <li>email: 可选，电子邮箱地址</li>
 * </ul>
 * 
 * @see AuthController#register(RegisterDTO) 注册接口
 */
@Data
public class RegisterDTO {

    /**
     * 用户名
     * 用于登录系统的唯一标识符
     * @NotBlank 校验：必填字段
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 密码
     * 用户设置的登录密码
     * @NotBlank 校验：必填字段
     * @Pattern 校验规则：
     *   - 必须包含至少一个字母（a-zA-Z）
     *   - 必须包含至少一个数字（0-9）
     *   - 长度在6-20个字符之间
     * 示例有效密码：Test123, abc123xyz, Pass2024
     */
    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).{6,20}$", message = "密码需包含字母和数字，长度6-20位")
    private String password;

    /**
     * 真实姓名
     * 用户的真实姓名，用于身份识别和显示
     * @NotBlank 校验：必填字段
     */
    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    /**
     * 用户角色
     * 注册时指定的角色类型（可选）
     * 可选值：admin, teacher, student
     * 如果不指定，默认为student
     */
    private String role;

    /**
     * 手机号码
     * 用于接收验证码和通知（可选）
     * @Pattern 校验规则：
     *   - 以1开头
     *   - 第二位为3-9的数字
     *   - 总共11位数字
     * 示例：13800138000, 15912345678
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 电子邮箱
     * 用于接收邮件通知和密码重置（可选）
     * 建议符合标准邮箱格式：xxx@xxx.com
     */
    private String email;
}
