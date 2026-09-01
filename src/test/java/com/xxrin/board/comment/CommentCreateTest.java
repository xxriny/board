package com.xxrin.board.comment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.xxrin.board.domain.Board;
import com.xxrin.board.domain.Comment;
import com.xxrin.board.dto.request.CommentCreateRequest;
import com.xxrin.board.repository.BoardRepository;
import com.xxrin.board.repository.CommentRepository;
import com.xxrin.board.service.CommentService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class CommentCreateTest {

    @Test
    void createsCommentForExistingBoard() {
        BoardRepository boards = mock(BoardRepository.class);
        CommentRepository comments = mock(CommentRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        Board board = Board.builder().title("제목").writer("작성자").passwordHash("hash").build();
        when(boards.findById(1L)).thenReturn(Optional.of(board));
        when(comments.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordEncoder.encode("1234")).thenReturn("encoded-password");

        var response = new CommentService(boards, comments, passwordEncoder)
                .create(1L, new CommentCreateRequest("댓글", "댓글 작성자", "1234"));

        assertThat(response.content()).isEqualTo("댓글");
        assertThat(board.getComments()).hasSize(1);
        assertThat(board.getComments().get(0).getPasswordHash()).isEqualTo("encoded-password");
    }
}
