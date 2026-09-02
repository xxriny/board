package com.xxrin.board.board;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.xxrin.board.controller.BoardController;
import com.xxrin.board.domain.Board;
import com.xxrin.board.dto.request.BoardUpdateRequest;
import com.xxrin.board.dto.response.BoardResponse;
import com.xxrin.board.exception.EntityNotFoundException;
import com.xxrin.board.exception.InvalidPasswordException;
import com.xxrin.board.exception.GlobalExceptionHandler;
import com.xxrin.board.repository.BoardRepository;
import com.xxrin.board.service.BoardService;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class BoardUpdateTest {

    @Test
    void serviceUpdatesBoardThroughDomainMethod() {
        BoardRepository repository = mock(BoardRepository.class);
        Board board = Board.builder().title("기존 제목").content("기존 내용").writer("작성자")
                .passwordHash("encoded-password")
                .build();
        ReflectionTestUtils.invokeMethod(board, "prePersist");
        LocalDateTime before = board.getUpdatedAt();
        when(repository.findById(1L)).thenReturn(Optional.of(board));
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        when(passwordEncoder.matches("password", "encoded-password")).thenReturn(true);
        BoardService service = new BoardService(repository, passwordEncoder);

        BoardResponse response = service.update(
                1L, new BoardUpdateRequest("새 제목", "새 내용", "password"));

        assertThat(response.title()).isEqualTo("새 제목");
        assertThat(response.content()).isEqualTo("새 내용");
        assertThat(response.updatedAt()).isAfter(before);
        verify(repository).findById(1L);
    }

    @Test
    void serviceRejectsMissingBoard() {
        BoardRepository repository = mock(BoardRepository.class);
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new BoardService(repository, mock(PasswordEncoder.class))
                .update(99L, new BoardUpdateRequest("제목", "내용", "1234")))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void serviceRejectsWrongPassword() {
        BoardRepository repository = mock(BoardRepository.class);
        Board board = Board.builder().title("제목").writer("작성자")
                .passwordHash("encoded-password").build();
        when(repository.findById(1L)).thenReturn(Optional.of(board));
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        BoardService service = new BoardService(repository, passwordEncoder);

        assertThatThrownBy(() -> service.update(
                1L, new BoardUpdateRequest("새 제목", "새 내용", "wrong")))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    void controllerRejectsBlankTitle() throws Exception {
        MockMvc mockMvc = mockMvc(mock(BoardService.class));

        mockMvc.perform(put("/api/boards/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":" ","content":"내용","password":"1234"}
                                """))
                .andExpect(status().isBadRequest());
    }

    private MockMvc mockMvc(BoardService service) {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        return MockMvcBuilders.standaloneSetup(new BoardController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }
}
