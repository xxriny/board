package com.xxrin.board.controller;

import com.xxrin.board.dto.request.CommentCreateRequest;
import com.xxrin.board.dto.request.CommentUpdateRequest;
import com.xxrin.board.dto.response.ApiResponse;
import com.xxrin.board.dto.response.CommentResponse;
import com.xxrin.board.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;

/** 댓글 REST API의 HTTP 요청과 응답을 처리한다. */
@RestController
@RequestMapping("/api/boards/{boardId}/comments")
@Tag(name = "Comment", description = "1-depth 댓글 API")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "댓글 생성")
    public ApiResponse<CommentResponse> create(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long boardId,
            @Valid @RequestBody CommentCreateRequest request) {
        return ApiResponse.success(
                commentService.create(Long.valueOf(jwt.getSubject()), boardId, request),
                "댓글이 생성되었습니다.");
    }

    @GetMapping
    @Operation(summary = "댓글 목록 조회")
    public ApiResponse<List<CommentResponse>> findAll(@PathVariable Long boardId) {
        return ApiResponse.success(
                commentService.findAll(boardId),
                "댓글 목록을 조회했습니다.");
    }

    @PutMapping("/{commentId}")
    @Operation(summary = "댓글 수정", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "작성자 아님"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "댓글 없음 또는 소속 불일치")
    })
    public ApiResponse<CommentResponse> update(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long boardId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentUpdateRequest request) {
        return ApiResponse.success(
                commentService.update(
                        Long.valueOf(jwt.getSubject()),
                        boardId,
                        commentId,
                        request),
                "댓글이 수정되었습니다.");
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "댓글 삭제", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "작성자 아님"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "댓글 없음 또는 소속 불일치")
    })
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long boardId,
            @PathVariable Long commentId) {
        commentService.delete(Long.valueOf(jwt.getSubject()), boardId, commentId);
        return ApiResponse.success(null, "댓글이 삭제되었습니다.");
    }
}
