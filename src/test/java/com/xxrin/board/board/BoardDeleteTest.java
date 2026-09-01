package com.xxrin.board.board;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.xxrin.board.domain.Board;
import com.xxrin.board.exception.InvalidPasswordException;
import com.xxrin.board.repository.BoardRepository;
import com.xxrin.board.service.BoardService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class BoardDeleteTest {

    @Test
    void serviceDeletesBoardWhenPasswordMatches() {
        BoardRepository repository = mock(BoardRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        Board board = Board.builder().title("제목").writer("작성자")
                .passwordHash("encoded-password").build();
        when(repository.findById(1L)).thenReturn(Optional.of(board));
        when(passwordEncoder.matches("1234", "encoded-password")).thenReturn(true);
        BoardService service = new BoardService(repository, passwordEncoder);

        service.delete(1L, "1234");

        verify(repository).delete(board);
    }

    @Test
    void serviceDoesNotDeleteBoardWhenPasswordDoesNotMatch() {
        BoardRepository repository = mock(BoardRepository.class);
        Board board = Board.builder().title("제목").writer("작성자")
                .passwordHash("encoded-password").build();
        when(repository.findById(1L)).thenReturn(Optional.of(board));
        BoardService service = new BoardService(repository, mock(PasswordEncoder.class));

        assertThatThrownBy(() -> service.delete(1L, "wrong"))
                .isInstanceOf(InvalidPasswordException.class);
    }
}
