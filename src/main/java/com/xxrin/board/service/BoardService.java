package com.xxrin.board.service;

import com.xxrin.board.domain.Board;
import com.xxrin.board.domain.Member;
import com.xxrin.board.dto.request.BoardCreateRequest;
import com.xxrin.board.dto.request.BoardUpdateRequest;
import com.xxrin.board.dto.response.BoardDetailResponse;
import com.xxrin.board.dto.response.BoardResponse;
import com.xxrin.board.dto.response.PageResponse;
import com.xxrin.board.exception.BusinessException;
import com.xxrin.board.exception.ErrorCode;
import com.xxrin.board.repository.BoardRepository;
import com.xxrin.board.repository.MemberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

/** 게시글 유스케이스와 트랜잭션 경계를 관리한다. */
@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;

    private final MemberRepository memberRepository;

    @Transactional
    public BoardResponse create(Long memberId, BoardCreateRequest request) {
        Member author = memberRepository.getReferenceById(memberId);
        Board board = request.toEntity(author);
        return BoardResponse.from(boardRepository.save(board));
    }

    @Transactional(readOnly = true)
    public PageResponse<BoardResponse> findAll(int page, int size) {
        Page<BoardResponse> boards = boardRepository
                .findAll(PageRequest.of(
                        page,
                        size,
                        Sort.by(Sort.Direction.DESC, "createdAt", "id")))
                .map(BoardResponse::from);
        return PageResponse.from(boards);
    }

    @Transactional
    public BoardDetailResponse findDetail(Long id) {
        Board board = findBoard(id);
        board.increaseViewCount();
        return BoardDetailResponse.from(board);
    }

    @Transactional
    public BoardResponse update(Long memberId, Long id, BoardUpdateRequest request) {
        Board board = findBoard(id);
        verifyOwner(board, memberId);
        board.update(request.title(), request.content());
        return BoardResponse.from(board);
    }

    @Transactional
    public void delete(Long memberId, Long id) {
        Board board = findBoard(id);
        verifyOwner(board, memberId);
        boardRepository.delete(board);
    }

    /** 게시글 조회와 404 변환을 한곳에서 처리한다. */
    private Board findBoard(Long id) {
        return boardRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOARD_NOT_FOUND));
    }

    private void verifyOwner(Board board, Long memberId) {
        if (!board.isOwnedBy(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_RESOURCE);
        }
    }

    /*
     * Legacy: 비회원 비밀번호 방식 비교용
     * private void verifyPassword(String rawPassword, String passwordHash) { ... }
     */
}
