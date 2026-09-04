package com.xxrin.board.exception;

import lombok.Getter;

/** 예상 가능한 비즈니스 오류와 API 오류 코드를 함께 전달한다. */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
