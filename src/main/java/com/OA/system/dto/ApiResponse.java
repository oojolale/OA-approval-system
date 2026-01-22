package com.OA.system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
@Data
@AllArgsConstructor
public class ApiResponse<T> {
    private int code;      // 0=OK
    private String message;
    private T data;
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(0, "OK", data);
    }
    public static <T> ApiResponse<T> fail(String msg) {
        return new ApiResponse<>(-1, msg, null);
    }
}