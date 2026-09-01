package com.xxrin.board.controller;

import com.xxrin.board.dto.request.BoardCreateRequest;
import com.xxrin.board.dto.request.BoardUpdateRequest;
import com.xxrin.board.dto.response.ApiResponse;
import com.xxrin.board.dto.response.BoardDetailResponse;
import com.xxrin.board.dto.response.BoardResponse;
import com.xxrin.board.dto.response.PageResponse;
import com.xxrin.board.service.BoardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

/** 게시글 REST API의 HTTP 요청과 응답을 처리한다. */
@RestController
@RequestMapping("/api/boards")
@Validated
@Tag(name = "Board", description = "게시글 API")
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @PostMapping
    @Operation(summary = "게시글 생성", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "검증 실패")})
    public ResponseEntity<ApiResponse<BoardResponse>> create(
            @Valid @RequestBody BoardCreateRequest request) {
        BoardResponse response = boardService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "게시글이 생성되었습니다."));
    }

    @GetMapping
    @Operation(summary = "게시글 목록 조회")
    public ResponseEntity<ApiResponse<PageResponse<BoardResponse>>> findAll(
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "page는 0 이상이어야 합니다.")
            int page,
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "size는 1 이상이어야 합니다.")
            @Max(value = 100, message = "size는 100 이하여야 합니다.")
            int size) {
        return ResponseEntity.ok(
                ApiResponse.success(boardService.findAll(page, size), "게시글 목록을 조회했습니다."));
    }

    @GetMapping("/{id}")
    @Operation(summary = "게시글 상세 조회", responses =
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "게시글 없음"))
    public ResponseEntity<ApiResponse<BoardDetailResponse>> findDetail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                boardService.findDetail(id), "게시글 상세를 조회했습니다."));
    }

    @PutMapping("/{id}")
    @Operation(summary = "게시글 수정", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "비밀번호 불일치"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "게시글 없음")})
    public ResponseEntity<ApiResponse<BoardResponse>> update(
            @PathVariable Long id, @Valid @RequestBody BoardUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                boardService.update(id, request), "게시글이 수정되었습니다."));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "게시글 삭제", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "비밀번호 불일치"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "게시글 없음")})
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @RequestParam @jakarta.validation.constraints.NotBlank(
                    message = "비밀번호는 필수입니다.") String password) {
        boardService.delete(id, password);
        return ResponseEntity.ok(ApiResponse.success(null, "게시글이 삭제되었습니다."));
    }
}
