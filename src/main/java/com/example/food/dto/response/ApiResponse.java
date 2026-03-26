package com.example.food.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private int statusCode;
    private String timestamp;
    private String path;
    private String method;
    private String message;
    private T data;
    private Object meta; // Pagination uchun

    public static <T> ApiResponse<T> success(int statusCode, String method, String path, String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .statusCode(statusCode)
                .timestamp(LocalDateTime.now().toString())
                .path(path)
                .method(method)
                .message(message)
                .data(data)
                .build();
    }
}