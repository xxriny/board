package com.xxrin.board.controller;

import com.xxrin.board.dto.request.CommentCreateRequest;
import com.xxrin.board.dto.response.ApiResponse;
import com.xxrin.board.dto.response.CommentResponse;
import com.xxrin.board.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 댓글 REST API의 HTTP 요청과 응답을 처리한다. */
@RestController
@RequestMapping("/api/boards/{boardId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponse>> create(
            @PathVariable Long boardId, @Valid @RequestBody CommentCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                commentService.create(boardId, request), "댓글이 생성되었습니다."));
    }
}
