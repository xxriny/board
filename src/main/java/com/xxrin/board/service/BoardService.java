package com.xxrin.board.service;

import com.xxrin.board.domain.Board;
import com.xxrin.board.dto.request.BoardCreateRequest;
import com.xxrin.board.dto.response.BoardResponse;
import com.xxrin.board.repository.BoardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 게시글 유스케이스와 트랜잭션 경계를 관리한다. */
@Service
public class BoardService {

    private final BoardRepository boardRepository;

    public BoardService(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    @Transactional
    public BoardResponse create(BoardCreateRequest request) {
        Board board = Board.builder()
                .title(request.title())
                .content(request.content())
                .writer(request.writer())
                .build();
        return BoardResponse.from(boardRepository.save(board));
    }
}
