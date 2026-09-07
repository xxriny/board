package com.xxrin.board.controller;

import com.xxrin.board.dto.request.CommentCreateRequest;
import com.xxrin.board.dto.request.CommentUpdateRequest;
import com.xxrin.board.dto.response.ApiResult;
import com.xxrin.board.dto.response.CommentResponse;
import com.xxrin.board.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** 댓글 REST API의 HTTP 요청과 응답을 처리한다. */
@RestController
@RequestMapping("/api/boards/{boardId}/comments")
@Tag(name = "Comment", description = "1-depth 댓글 API")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(
            summary = "댓글 생성",
            description = "로그인 회원을 작성자로 하여 게시글에 댓글을 생성하고 댓글 수를 증가시킵니다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResult<CommentResponse> create(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long boardId,
            @Valid @RequestBody CommentCreateRequest request) {
        return ApiResult.success(
                commentService.create(Long.valueOf(jwt.getSubject()), boardId, request),
                "댓글이 생성되었습니다.");
    }

    @Operation(
            summary = "댓글 목록 조회",
            description = "게시글의 댓글을 생성 시각과 식별자 기준 오래된 순으로 조회합니다.")
    @GetMapping
    public ApiResult<List<CommentResponse>> findAll(@PathVariable Long boardId) {
        return ApiResult.success(
                commentService.findAll(boardId),
                "댓글 목록을 조회했습니다.");
    }

    @Operation(
            summary = "댓글 수정",
            description = "게시글 소속을 확인하고 작성자 본인만 댓글 내용을 수정할 수 있습니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "403",
                            description = "작성자 아님"),
                    @ApiResponse(
                            responseCode = "404",
                            description = "댓글 없음 또는 소속 불일치")
            })
    @PutMapping("/{commentId}")
    public ApiResult<CommentResponse> update(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long boardId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentUpdateRequest request) {
        return ApiResult.success(
                commentService.update(
                        Long.valueOf(jwt.getSubject()),
                        boardId,
                        commentId,
                        request),
                "댓글이 수정되었습니다.");
    }

    @Operation(
            summary = "댓글 삭제",
            description = "게시글 소속과 작성자를 확인해 댓글을 삭제하고 댓글 수를 감소시킵니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "403",
                            description = "작성자 아님"),
                    @ApiResponse(
                            responseCode = "404",
                            description = "댓글 없음 또는 소속 불일치")
            })
    @DeleteMapping("/{commentId}")
    public ApiResult<Void> delete(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long boardId,
            @PathVariable Long commentId) {
        commentService.delete(Long.valueOf(jwt.getSubject()), boardId, commentId);
        return ApiResult.success(null, "댓글이 삭제되었습니다.");
    }
}
