package com.xxrin.board.controller;

import com.xxrin.board.dto.request.CommentCreateRequest;
import com.xxrin.board.dto.request.CommentUpdateRequest;
import com.xxrin.board.dto.request.PasswordRequest;
import com.xxrin.board.dto.response.ApiResponse;
import com.xxrin.board.dto.response.CommentResponse;
import com.xxrin.board.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 댓글 REST API의 HTTP 요청과 응답을 처리한다. */
@RestController
@RequestMapping("/api/boards/{boardId}/comments")
@Tag(name = "Comment", description = "1-depth 댓글 API")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    @Operation(summary = "댓글 생성")
    public ResponseEntity<ApiResponse<CommentResponse>> create(
            @PathVariable Long boardId, @Valid @RequestBody CommentCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                commentService.create(boardId, request), "댓글이 생성되었습니다."));
    }

    @GetMapping
    @Operation(summary = "댓글 목록 조회")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> findAll(@PathVariable Long boardId) {
        return ResponseEntity.ok(ApiResponse.success(
                commentService.findAll(boardId), "댓글 목록을 조회했습니다."));
    }

    @PutMapping("/{commentId}")
    @Operation(summary = "댓글 수정", responses =
            {@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "비밀번호 불일치"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "댓글 없음 또는 소속 불일치")})
    public ResponseEntity<ApiResponse<CommentResponse>> update(
            @PathVariable Long boardId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                commentService.update(boardId, commentId, request), "댓글이 수정되었습니다."));
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "댓글 삭제", responses =
            {@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "비밀번호 불일치"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "댓글 없음 또는 소속 불일치")})
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long boardId,
            @PathVariable Long commentId,
            @Valid @RequestBody PasswordRequest request) {
        commentService.delete(boardId, commentId, request.password());
        return ResponseEntity.ok(ApiResponse.success(null, "댓글이 삭제되었습니다."));
    }
}
