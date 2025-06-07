package com.Main.dto.information;

public class ApiResponseDTO<T> {
    private Integer code;     // 状态码，200表示成功
    private String message;   // 描述信息
    private T data;           // 返回数据

    // 构造函数
    public ApiResponseDTO() {
    }

    public ApiResponseDTO(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // 静态工厂方法
    public static <T> ApiResponseDTO<T> success(T data) {
        return new ApiResponseDTO<>(200, "操作成功", data);
    }

    public static <T> ApiResponseDTO<T> success(String message, T data) {
        return new ApiResponseDTO<>(200, message, data);
    }

    public static <T> ApiResponseDTO<T> error(Integer code, String message) {
        return new ApiResponseDTO<>(code, message, null);
    }

    // Getter和Setter方法
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
} 