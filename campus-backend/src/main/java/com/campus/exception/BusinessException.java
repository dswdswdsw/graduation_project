package com.campus.exception;

import com.campus.common.ResultCode;
import lombok.Getter;

/**
 * 业务异常类 - 用于表示业务逻辑中的可预期异常
 * 
 * <p>当业务规则校验失败或遇到预期的错误情况时抛出此异常。
 * 与系统异常（Exception）不同，业务异常是正常的程序流程，
 * 应该被全局异常处理器捕获并转换为友好的错误提示返回给前端。</p>
 * 
 * <h2>使用场景：</h2>
 * <ul>
 *   <li>用户名或密码错误</li>
 *   <li>用户不存在</li>
 *   <li>账号被禁用</li>
 *   <li>数据重复（如用户名已存在）</li>
 *   <li>权限不足</li>
 * </ul>
 * 
 * <h2>与RuntimeException的关系：</h2>
 * <p>继承自RuntimeException，属于非检查型异常，
 * 不需要强制try-catch，可以向上层传播至全局异常处理器。</p>
 * 
 * <h2>使用示例：</h2>
 * <pre>
 * // 使用预定义的状态码
 * throw new BusinessException(ResultCode.UNAUTHORIZED, "用户不存在");
 * 
 * // 使用自定义消息
 * throw new BusinessException("用户名已存在");
 * 
 * // 使用自定义状态码
 * throw new BusinessException(400, "参数错误");
 * </pre>
 * 
 * @see GlobalExceptionHandler 全局异常处理器
 * @see ResultCode 状态码枚举
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 业务错误码
     * 对应HTTP状态码或自定义业务错误码
     * 用于前端判断错误类型并做相应处理
     */
    private final int code;

    /**
     * 构造方法 - 仅指定错误消息，使用默认错误码500
     * 
     * @param message 错误提示信息
     */
    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.FAIL.getCode();
    }

    /**
     * 构造方法 - 使用预定义的ResultCode枚举
     * 
     * @param resultCode ResultCode枚举值，包含状态码和默认消息
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    /**
     * 构造方法 - 自定义状态码和消息
     * 
     * @param code HTTP状态码或业务错误码
     * @param message 错误提示信息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 构造方法 - 使用ResultCode的状态码但覆盖消息内容
     * 
     * @param resultCode ResultCode枚举值（使用其code）
     * @param message 自定义的错误消息（覆盖resultCode的message）
     */
    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }
}
