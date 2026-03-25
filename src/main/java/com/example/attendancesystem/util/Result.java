package com.example.attendancesystem.util;

public class Result<T> {
    private Integer code;
    private String msg;      // 改成 msg
    private T data;

    // 无参构造函数
    public Result() {
    }

    // 有参构造函数
    public Result(Integer code, String msg, T data) {  // 参数名也改成 msg
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // 成功响应（带数据）
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    // 成功响应（带自定义消息）
    public static <T> Result<T> success(String msg, T data) {  // 参数名 msg
        return new Result<>(200, msg, data);
    }

    // 失败响应
    public static <T> Result<T> error(String msg) {  // 参数名 msg
        return new Result<>(500, msg, null);
    }

    // Getter 和 Setter
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {  // 改成 getMsg
        return msg;
    }

    public void setMsg(String msg) {  // 改成 setMsg
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}