package com.xxrin.board.controller;

import com.xxrin.board.dto.request.BoardCreateRequest;
import com.xxrin.board.dto.response.ApiResponse;
import com.xxrin.board.dto.response.BoardResponse;
import com.xxrin.board.dto.response.PageResponse;
import com.xxrin.board.service.BoardService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

/** 게시글 REST API의 HTTP 요청과 응답을 처리한다. */
@RestController
@RequestMapping("/api/boards")
@Validated
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BoardResponse>> create(
            @Valid @RequestBody BoardCreateRequest request) {
        BoardResponse response = boardService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "게시글이 생성되었습니다."));
    }

    @GetMapping
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
}
