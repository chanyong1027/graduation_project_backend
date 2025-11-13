package com.example.BookProject.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * API 응답 표준화를 위한 공통 Response DTO
 * - 성공/실패 여부, 메시지, 데이터, 타임스탬프 포함
 */
@Getter
@NoArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    private ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * 성공 응답 (데이터 있음)
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "성공", data);
    }

    /**
     * 성공 응답 (커스텀 메시지)
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    /**
     * 성공 응답 (데이터 없음)
     */
    public static <Void> ApiResponse<Void> success(String message) {
        return new ApiResponse<>(true, message, null);
    }

    /**
     * 실패 응답
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }

    /**
     * 실패 응답 (데이터 포함)
     */
    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(false, message, data);
    }
}
