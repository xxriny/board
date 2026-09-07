package com.xxrin.board.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.xxrin.board.exception.ErrorCode;
import lombok.Getter;

/** 모든 성공 및 오류 응답에 사용하는 공통 JSON 래퍼다. */
@Getter
public final class ApiResult<T> {

    private final boolean success;

    private final T data;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String code;

    private final String message;

    private ApiResult(boolean success, T data, String code, String message) {
        this.success = success;
        this.data = data;
        this.code = code;
        this.message = message;
    }

    public static <T> ApiResult<T> success(T data, String message) {
        return new ApiResult<>(true, data, null, message);
    }

    public static <T> ApiResult<T> error(ErrorCode errorCode) {
        return new ApiResult<>(
                false,
                null,
                errorCode.name(),
                errorCode.getMessage());
    }

    public static <T> ApiResult<T> error(T data, ErrorCode errorCode) {
        return new ApiResult<>(
                false,
                data,
                errorCode.name(),
                errorCode.getMessage());
    }
}
