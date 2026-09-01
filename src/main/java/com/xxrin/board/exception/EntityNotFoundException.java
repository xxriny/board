package com.xxrin.board.exception;

/** 요청한 게시글 또는 댓글이 존재하지 않을 때 발생한다. */
public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String message) {
        super(message);
    }
}
