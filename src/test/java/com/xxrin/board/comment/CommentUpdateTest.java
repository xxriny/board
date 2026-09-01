package com.xxrin.board.comment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.xxrin.board.domain.Board;
import com.xxrin.board.domain.Comment;
import com.xxrin.board.dto.request.CommentUpdateRequest;
import com.xxrin.board.exception.EntityNotFoundException;
import com.xxrin.board.exception.InvalidPasswordException;
import com.xxrin.board.repository.BoardRepository;
import com.xxrin.board.repository.CommentRepository;
import com.xxrin.board.service.CommentService;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.security.crypto.password.PasswordEncoder;

class CommentUpdateTest {

    @Test
    void updatesOwnedCommentAndAdvancesUpdatedAt() {
        CommentRepository comments = mock(CommentRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        Board board = Board.builder().title("제목").writer("작성자").passwordHash("hash").build();
        Comment comment = Comment.builder().content("기존 댓글").writer("댓글 작성자")
                .passwordHash("encoded-password").board(board).build();
        ReflectionTestUtils.invokeMethod(comment, "prePersist");
        LocalDateTime before = comment.getUpdatedAt();
        when(comments.findByBoardIdAndId(1L, 10L)).thenReturn(Optional.of(comment));
        when(passwordEncoder.matches("1234", "encoded-password")).thenReturn(true);

        var response = new CommentService(mock(BoardRepository.class), comments, passwordEncoder)
                .update(1L, 10L, new CommentUpdateRequest("수정 댓글", "1234"));

        assertThat(response.content()).isEqualTo("수정 댓글");
        assertThat(response.updatedAt()).isAfter(before);
    }

    @Test
    void rejectsCommentOwnedByAnotherBoard() {
        CommentRepository comments = mock(CommentRepository.class);
        when(comments.findByBoardIdAndId(1L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new CommentService(mock(BoardRepository.class), comments, mock(PasswordEncoder.class))
                .update(1L, 10L, new CommentUpdateRequest("수정 댓글", "1234")))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void rejectsWrongCommentPassword() {
        CommentRepository comments = mock(CommentRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        Board board = Board.builder().title("제목").writer("작성자").passwordHash("hash").build();
        Comment comment = Comment.builder().content("댓글").writer("댓글 작성자")
                .passwordHash("encoded-password").board(board).build();
        when(comments.findByBoardIdAndId(1L, 10L)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> new CommentService(mock(BoardRepository.class), comments, passwordEncoder)
                .update(1L, 10L, new CommentUpdateRequest("수정 댓글", "wrong")))
                .isInstanceOf(InvalidPasswordException.class);
    }
}
