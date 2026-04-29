package com.campus.common;

import lombok.Data;

/**
 * 统一API响应结果封装类
 * 
 * <p>所有Controller接口的返回值都使用此类进行统一封装，
 * 确保前端接收到的响应格式一致，便于处理。</p>
 * 
 * <h2>标准响应格式：</h2>
 * <pre>
 * {
 *   "code": 200,           // 状态码
 *   "message": "操作成功",   // 提示消息
 *   "data": { ... }        // 业务数据（可为null）
 * }
 * </pre>
 * 
 * <h2>使用示例：</h2>
 * <pre>
 * // 成功响应（无数据）
 * return Result.success();
 * 
 * // 成功响应（带数据）
 * return Result.success(userVO);
 * 
 * // 失败响应
 * return Result.fail("用户名或密码错误");
 * 
 * // 使用枚举定义的错误码
 * return Result.fail(ResultCode.UNAUTHORIZED);
 * </pre>
 * 
 * @param <T> 泛型参数，表示data字段的类型
 * @see ResultCode 响应状态码枚举
 */
@Data
public class Result<T> {

    /**
     * 响应状态码
     * 200表示成功，其他表示各种错误情况
     * @see ResultCode
     */
    private int code;

    /**
     * 响应消息
     * 对当前操作的简要描述或错误提示信息
     * 前端可据此显示给用户
     */
    private String message;

    /**
     * 响应数据
     * 包含具体的业务数据，类型由泛型T决定
     * 可以是单个对象、列表、Map等任意类型
     */
    private T data;

    /**
     * 私有构造方法
     * 防止外部直接实例化，强制使用静态工厂方法创建对象
     */
    private Result() {}

    /**
     * 创建成功响应（无数据）
     * 
     * @param <T> 数据类型
     * @return Result对象，code=200, data=null
     */
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(ResultCode.SUCCESS.getMessage());
        return result;
    }

    /**
     * 创建成功响应（带数据）
     * 
     * @param data 要返回的业务数据
     * @param <T> 数据类型
     * @return Result对象，code=200, data=传入的数据
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(ResultCode.SUCCESS.getMessage());
        result.setData(data);
        return result;
    }

    /**
     * 创建成功响应（自定义消息和数据）
     * 
     * @param message 自定义的成功消息
     * @param data 要返回的业务数据
     * @param <T> 数据类型
     * @return Result对象，code=200, message和data为传入的值
     */
    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    /**
     * 创建失败响应（默认失败消息）
     * 
     * @param <T> 数据类型
     * @return Result对象，code=500, message="操作失败"
     */
    public static <T> Result<T> fail() {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.FAIL.getCode());
        result.setMessage(ResultCode.FAIL.getMessage());
        return result;
    }

    /**
     * 创建失败响应（自定义失败消息）
     * 
     * @param message 错误提示信息
     * @param <T> 数据类型
     * @return Result对象，code=500, message为传入的消息
     */
    public static <T> Result<T> fail(String message) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.FAIL.getCode());
        result.setMessage(message);
        return result;
    }

    /**
     * 创建失败响应（使用预定义的状态码）
     * 
     * @param resultCode ResultCode枚举值，包含状态码和消息
     * @param <T> 数据类型
     * @return Result对象，code和message来自resultCode
     */
    public static <T> Result<T> fail(ResultCode resultCode) {
        Result<T> result = new Result<>();
        result.setCode(resultCode.getCode());
        result.setMessage(resultCode.getMessage());
        return result;
    }

    /**
     * 创建失败响应（自定义状态码和消息）
     * 
     * @param code HTTP状态码或业务错误码
     * @param message 错误提示信息
     * @param <T> 数据类型
     * @return Result对象，code和message为传入的值
     */
    public static <T> Result<T> fail(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
