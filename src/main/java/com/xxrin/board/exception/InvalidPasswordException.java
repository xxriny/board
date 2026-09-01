package com.xxrin.board.exception;

/** 작성 비밀번호가 일치하지 않을 때 발생한다. */
public class InvalidPasswordException extends RuntimeException {

    public InvalidPasswordException(String message) {
        super(message);
    }
}
