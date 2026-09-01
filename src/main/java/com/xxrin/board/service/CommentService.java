package com.xxrin.board.service;

import com.xxrin.board.domain.Board;
import com.xxrin.board.domain.Comment;
import com.xxrin.board.dto.request.CommentCreateRequest;
import com.xxrin.board.dto.request.CommentUpdateRequest;
import com.xxrin.board.dto.response.CommentResponse;
import com.xxrin.board.exception.EntityNotFoundException;
import com.xxrin.board.exception.InvalidPasswordException;
import com.xxrin.board.repository.BoardRepository;
import com.xxrin.board.repository.CommentRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 댓글 유스케이스와 트랜잭션 경계를 관리한다. */
@Service
public class CommentService {

    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final PasswordEncoder passwordEncoder;

    public CommentService(BoardRepository boardRepository, CommentRepository commentRepository) {
        this(boardRepository, commentRepository, new BCryptPasswordEncoder());
    }

    @Autowired
    public CommentService(
            BoardRepository boardRepository, CommentRepository commentRepository, PasswordEncoder passwordEncoder) {
        this.boardRepository = boardRepository;
        this.commentRepository = commentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public CommentResponse create(Long boardId, CommentCreateRequest request) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));
        Comment comment = Comment.builder()
                .content(request.content())
                .writer(request.writer())
                .passwordHash(passwordEncoder.encode(request.password()))
                .board(board)
                .build();
        return CommentResponse.from(commentRepository.save(comment));
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> findAll(Long boardId) {
        boardRepository.findById(boardId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));
        return commentRepository.findAllByBoardId(boardId).stream()
                .map(CommentResponse::from)
                .toList();
    }

    @Transactional
    public CommentResponse update(Long boardId, Long commentId, CommentUpdateRequest request) {
        Comment comment = findComment(boardId, commentId);
        verifyPassword(request.password(), comment.getPasswordHash());
        comment.update(request.content());
        return CommentResponse.from(comment);
    }

    @Transactional
    public void delete(Long boardId, Long commentId, String password) {
        Comment comment = findComment(boardId, commentId);
        verifyPassword(password, comment.getPasswordHash());
        commentRepository.delete(comment);
    }

    private Comment findComment(Long boardId, Long commentId) {
        return commentRepository.findByBoardIdAndId(boardId, commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다."));
    }

    private void verifyPassword(String rawPassword, String passwordHash) {
        if (passwordHash == null || !passwordEncoder.matches(rawPassword, passwordHash)) {
            throw new InvalidPasswordException("비밀번호가 일치하지 않습니다.");
        }
    }
}
