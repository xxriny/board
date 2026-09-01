package com.xxrin.board.dto.response;

import lombok.Getter;

/** 모든 성공 및 오류 응답에 사용하는 공통 JSON 래퍼다. */
@Getter
public final class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final String message;

    private ApiResponse(boolean success, T data, String message) {
        this.success = success;
        this.data = data;
        this.message = message;
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, null, message);
    }

    public static <T> ApiResponse<T> error(T data, String message) {
        return new ApiResponse<>(false, data, message);
    }
}
