package com.Main.dto.information;

import java.util.List;

public class ApiResponseListDTO<T> {
    private Integer code;     // 状态码，200表示成功
    private String message;   // 描述信息
    private List<T> data;     // 返回数据列表

    // 构造函数
    public ApiResponseListDTO() {
    }

    public ApiResponseListDTO(Integer code, String message, List<T> data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // 静态工厂方法
    public static <T> ApiResponseListDTO<T> success(List<T> data) {
        return new ApiResponseListDTO<>(200, "操作成功", data);
    }

    public static <T> ApiResponseListDTO<T> success(String message, List<T> data) {
        return new ApiResponseListDTO<>(200, message, data);
    }

    public static <T> ApiResponseListDTO<T> error(Integer code, String message) {
        return new ApiResponseListDTO<>(code, message, null);
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

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }
}
