package com.xxrin.board.service;

import com.xxrin.board.domain.Board;
import com.xxrin.board.domain.Comment;
import com.xxrin.board.domain.Member;
import com.xxrin.board.dto.request.CommentCreateRequest;
import com.xxrin.board.dto.request.CommentUpdateRequest;
import com.xxrin.board.dto.response.CommentResponse;
import com.xxrin.board.exception.BusinessException;
import com.xxrin.board.exception.ErrorCode;
import com.xxrin.board.repository.BoardRepository;
import com.xxrin.board.repository.CommentRepository;
import com.xxrin.board.repository.MemberRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

/** 댓글 유스케이스와 트랜잭션 경계를 관리한다. */
@Service
@RequiredArgsConstructor
public class CommentService {

    private final BoardRepository boardRepository;

    private final CommentRepository commentRepository;

    private final MemberRepository memberRepository;

    @Transactional
    public CommentResponse create(
            Long memberId,
            Long boardId,
            CommentCreateRequest request) {
        Board board = findBoard(boardId);
        Member author = memberRepository.getReferenceById(memberId);
        Comment comment = commentRepository.save(request.toEntity(board, author));
        boardRepository.incrementCommentCount(boardId);
        return CommentResponse.from(comment);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> findAll(Long boardId) {
        findBoard(boardId);
        return commentRepository.findAllByBoard_IdOrderByCreatedAtAscIdAsc(boardId)
                .stream()
                .map(CommentResponse::from)
                .toList();
    }

    @Transactional
    public CommentResponse update(
            Long memberId,
            Long boardId,
            Long commentId,
            CommentUpdateRequest request) {
        Comment comment = findComment(boardId, commentId);
        verifyOwner(comment, memberId);
        comment.update(request.content());
        return CommentResponse.from(comment);
    }

    @Transactional
    public void delete(Long memberId, Long boardId, Long commentId) {
        Comment comment = findComment(boardId, commentId);
        verifyOwner(comment, memberId);
        commentRepository.delete(comment);
        boardRepository.decrementCommentCount(boardId);
    }

    /** 댓글 생성·조회에 필요한 게시글 존재 여부를 확인한다. */
    private Board findBoard(Long boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOARD_NOT_FOUND));
    }

    /** 댓글 소속 검증과 404 변환을 하나의 조회로 처리한다. */
    private Comment findComment(Long boardId, Long commentId) {
        return commentRepository.findByBoard_IdAndId(boardId, commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
    }

    private void verifyOwner(Comment comment, Long memberId) {
        if (!comment.isOwnedBy(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_RESOURCE);
        }
    }

    /*
     * Legacy: 비회원 비밀번호 방식 비교용
     * private void verifyPassword(String rawPassword, String passwordHash) { ... }
     */
}
