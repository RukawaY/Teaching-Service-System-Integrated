package com.Main.dto.course_selection;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 通用响应数据传输对象
 */
public class ResponseDTO<T> {
    @JsonProperty("code")
    private String code;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private T data;
    
    public ResponseDTO() {
    }
    
    public ResponseDTO(String code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public ResponseDTO(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
    
    public static <T> ResponseDTO<T> success() {
        return new ResponseDTO<>("200", "操作成功");
    }
    
    public static <T> ResponseDTO<T> success(T data) {
        return new ResponseDTO<>("200", "操作成功", data);
    }
    
    public static <T> ResponseDTO<T> fail(String message) {
        return new ResponseDTO<>("500", message);
    }
    
    // Getters and Setters
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
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