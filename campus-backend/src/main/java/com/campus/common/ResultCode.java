package com.campus.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应状态码枚举类
 * 
 * <p>定义系统中所有API响应的状态码和对应的提示消息。
 * 使用枚举确保状态码的一致性和可维护性。</p>
 * 
 * <h2>设计原则：</h2>
 * <ul>
 *   <li>遵循HTTP状态码语义</li>
 *   <li>提供清晰的中文错误提示</li>
 *   <li>便于前端统一处理不同类型的错误</li>
 * </ul>
 * 
 * <h2>使用示例：</h2>
 * <pre>
 * // 在Controller中使用
 * return Result.fail(ResultCode.UNAUTHORIZED);
 * return Result.fail(ResultCode.PARAM_ERROR);
 * </pre>
 * 
 * @see Result 统一响应封装类
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    /**
     * 操作成功
     * 状态码：200
     * 用于表示请求处理成功，无论是否有返回数据
     */
    SUCCESS(200, "操作成功"),

    /**
     * 操作失败（通用）
     * 状态码：500
     * 服务器内部错误或未预期的异常情况
     */
    FAIL(500, "操作失败"),

    /**
     * 未授权/未登录
     * 状态码：401
     * 用户未登录或Token已过期/无效
     * 前端收到此状态码后应跳转到登录页面
     */
    UNAUTHORIZED(401, "未授权"),

    /**
     * 无权限访问
     * 状态码：403
     * 用户已登录但无权访问该资源
     * 通常用于角色权限校验不通过的情况
     */
    FORBIDDEN(403, "无权限"),

    /**
     * 资源不存在
     * 状态码：404
     * 请求的资源在系统中不存在
     * 可能是URL路径错误或资源ID无效
     */
    NOT_FOUND(404, "资源不存在"),

    /**
     * 参数错误
     * 状态码：400
     * 请求参数不符合要求（缺失、格式错误、超出范围等）
     * 通常由@Validated校验触发
     */
    PARAM_ERROR(400, "参数错误");

    /**
     * HTTP状态码/业务错误码
     * 对应HTTP标准状态码或自定义业务码
     */
    private final int code;

    /**
     * 状态描述消息
     * 对当前状态的简要说明，用于显示给用户
     */
    private final String message;
}
