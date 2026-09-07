package com.xxrin.board.controller;

import com.xxrin.board.dto.request.BoardCreateRequest;
import com.xxrin.board.dto.request.BoardUpdateRequest;
import com.xxrin.board.dto.response.ApiResult;
import com.xxrin.board.dto.response.BoardDetailResponse;
import com.xxrin.board.dto.response.BoardResponse;
import com.xxrin.board.dto.response.PageResponse;
import com.xxrin.board.service.BoardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** 게시글 REST API의 HTTP 요청과 응답을 처리한다. */
@RestController
@RequestMapping("/api/boards")
@Validated
@Tag(name = "Board", description = "게시글 API")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @Operation(
            summary = "게시글 생성",
            description = "로그인 회원을 작성자로 하여 제목과 본문으로 게시글을 생성합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "생성 성공"),
                    @ApiResponse(
                            responseCode = "400",
                            description = "검증 실패")
            })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResult<BoardResponse> create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody BoardCreateRequest request) {
        return ApiResult.success(
                boardService.create(Long.valueOf(jwt.getSubject()), request),
                "게시글이 생성되었습니다.");
    }

    @Operation(
            summary = "게시글 목록 조회",
            description = "게시글을 생성 시각과 식별자 기준 최신순으로 페이징 조회합니다.")
    @GetMapping
    public ApiResult<PageResponse<BoardResponse>> findAll(
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "page는 0 이상이어야 합니다.")
            int page,
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "size는 1 이상이어야 합니다.")
            @Max(value = 100, message = "size는 100 이하여야 합니다.")
            int size) {
        return ApiResult.success(
                boardService.findAll(page, size),
                "게시글 목록을 조회했습니다.");
    }

    @Operation(
            summary = "게시글 상세 조회",
            description = "조회수를 1 증가시키고 댓글을 포함한 게시글 상세 정보를 조회합니다.",
            responses = @ApiResponse(
                    responseCode = "404",
                    description = "게시글 없음"))
    @GetMapping("/{id}")
    public ApiResult<BoardDetailResponse> findDetail(@PathVariable Long id) {
        return ApiResult.success(boardService.findDetail(id),
                "게시글 상세를 조회했습니다.");
    }

    @Operation(
            summary = "게시글 수정",
            description = "작성자 본인만 게시글 제목과 본문을 수정할 수 있습니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "403",
                            description = "작성자 아님"),
                    @ApiResponse(
                            responseCode = "404",
                            description = "게시글 없음")
            })
    @PutMapping("/{id}")
    public ApiResult<BoardResponse> update(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @Valid @RequestBody BoardUpdateRequest request) {
        return ApiResult.success(
                boardService.update(Long.valueOf(jwt.getSubject()), id, request),
                "게시글이 수정되었습니다.");
    }

    @Operation(
            summary = "게시글 삭제",
            description = "작성자 본인만 게시글과 소속 댓글을 함께 삭제할 수 있습니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "403",
                            description = "작성자 아님"),
                    @ApiResponse(
                            responseCode = "404",
                            description = "게시글 없음")
            })
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id) {
        boardService.delete(Long.valueOf(jwt.getSubject()), id);
        return ApiResult.success(null, "게시글이 삭제되었습니다.");
    }
}
