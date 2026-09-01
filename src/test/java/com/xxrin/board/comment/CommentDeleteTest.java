package com.xxrin.board.comment;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.xxrin.board.domain.Board;
import com.xxrin.board.domain.Comment;
import com.xxrin.board.exception.InvalidPasswordException;
import com.xxrin.board.repository.BoardRepository;
import com.xxrin.board.repository.CommentRepository;
import com.xxrin.board.service.CommentService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class CommentDeleteTest {

    @Test
    void deletesCommentSelectedByBoardAndCommentIds() {
        CommentRepository comments = mock(CommentRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        Board board = Board.builder().title("제목").writer("작성자").passwordHash("hash").build();
        Comment comment = Comment.builder().content("댓글").writer("댓글 작성자")
                .passwordHash("encoded-password").board(board).build();
        when(comments.findByBoardIdAndId(1L, 10L)).thenReturn(Optional.of(comment));
        when(passwordEncoder.matches("1234", "encoded-password")).thenReturn(true);
        CommentService service = new CommentService(mock(BoardRepository.class), comments, passwordEncoder);

        service.delete(1L, 10L, "1234");

        verify(comments).delete(comment);
    }

    @Test
    void rejectsWrongCommentPassword() {
        CommentRepository comments = mock(CommentRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        Board board = Board.builder().title("제목").writer("작성자").passwordHash("hash").build();
        Comment comment = Comment.builder().content("댓글").writer("댓글 작성자")
                .passwordHash("encoded-password").board(board).build();
        when(comments.findByBoardIdAndId(1L, 10L)).thenReturn(Optional.of(comment));
        CommentService service = new CommentService(mock(BoardRepository.class), comments, passwordEncoder);

        assertThatThrownBy(() -> service.delete(1L, 10L, "wrong"))
                .isInstanceOf(InvalidPasswordException.class);
    }
}
