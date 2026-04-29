package com.campus.exception;

import com.campus.common.Result;
import com.campus.common.ResultCode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器 - 统一处理所有Controller层抛出的异常
 * 
 * <p>使用@RestControllerAdvice注解实现全局异常拦截，
 * 将各种异常转换为统一的Result格式返回给前端。
 * 避免了在每个Controller方法中重复编写try-catch代码。</p>
 * 
 * <h2>处理的异常类型：</h2>
 * <ul>
 *   <li><b>BusinessException</b> - 业务异常（可预期的）</li>
 *   <li><b>MethodArgumentNotValidException</b> - @Validated校验失败</li>
 *   <li><b>BindException</b> - 参数绑定失败</li>
 *   <li><b>ConstraintViolationException</b> - 约束校验失败</li>
 *   <li><b>Exception</b> - 其他未预期异常（兜底处理）</li>
 * </ul>
 * 
 * <h2>设计原则：</h2>
 * <ul>
 *   <li>所有异常都转换为统一的Result格式</li>
 *   <li>业务异常返回具体错误信息，系统异常隐藏细节</li>
 *   <li>记录详细的日志用于问题排查</li>
 *   <li>参数校验错误提取字段级别的错误信息</li>
 * </ul>
 * 
 * <h2>响应格式：</h2>
 * <pre>
 * {
 *   "code": 400,              // 错误码
 *   "message": "用户名不能为空", // 错误提示
 *   "data": null              // 无数据
 * }
 * </pre>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     * 
     * <p>捕获Service层抛出的BusinessException，
     * 直接使用异常中的code和message构建响应。</p>
     * 
     * @param e 业务异常对象
     * @return Result包含错误码和错误消息
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 处理请求体参数校验异常 (@Valid + @RequestBody)
     * 
     * <p>当Controller方法的@RequestBody参数使用了@Valid/@Validated注解，
     * 且校验失败时触发此异常。提取所有字段错误信息并拼接返回。</p>
     * 
     * @param e 参数校验异常对象
     * @return Result包含PARAM_ERROR状态码和详细的字段错误信息
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("参数校验异常: {}", message);
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), message);
    }

    /**
     * 处理参数绑定异常
     * 
     * <p>当请求参数无法正确绑定到Controller方法参数时触发，
     * 例如类型转换失败、缺少必需参数等。</p>
     * 
     * @param e 参数绑定异常对象
     * @return Result包含PARAM_ERROR状态码和错误详情
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("参数绑定异常: {}", message);
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), message);
    }

    /**
     * 处理约束校验异常 (方法级别校验)
     * 
     * <p>当在Controller方法或参数上使用约束注解（如@NotNull, @Size等）
     * 且校验失败时触发此异常。</p>
     * 
     * @param e 约束校验异常对象
     * @return Result包含PARAM_ERROR状态码和校验错误消息
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        log.warn("约束校验异常: {}", message);
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), message);
    }

    /**
     * 处理其他未预期的异常（兜底处理）
     * 
     * <p>捕获所有未被上述处理器处理的异常。
     * 为了安全，不将具体的异常信息返回给前端，
     * 只返回通用的"系统内部错误"提示。</p>
     * 
     * <p>详细的异常堆栈信息会记录到日志文件中，
     * 用于开发人员排查问题。</p>
     * 
     * @param e 异常对象
     * @return Result包含FAIL状态码和通用错误消息
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常: ", e);
        return Result.fail(ResultCode.FAIL.getCode(), "系统内部错误");
    }
}
