package com.xxrin.board.board;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xxrin.board.controller.BoardController;
import com.xxrin.board.domain.Board;
import com.xxrin.board.dto.request.BoardCreateRequest;
import com.xxrin.board.dto.response.BoardResponse;
import com.xxrin.board.exception.GlobalExceptionHandler;
import com.xxrin.board.repository.BoardRepository;
import com.xxrin.board.service.BoardService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class BoardCreateTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void serviceCreatesBoardAndReturnsPersistedValues() {
        BoardRepository repository = mock(BoardRepository.class);
        when(repository.save(any(Board.class))).thenAnswer(invocation -> {
            Board board = invocation.getArgument(0);
            ReflectionTestUtils.setField(board, "id", 1L);
            return board;
        });
        BoardService service = new BoardService(repository);

        BoardResponse response = service.create(
                new BoardCreateRequest("제목", "내용", "작성자"));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("제목");
        assertThat(response.content()).isEqualTo("내용");
        assertThat(response.writer()).isEqualTo("작성자");
        assertThat(response.viewCount()).isZero();
    }

    @Test
    void controllerReturnsCreatedEnvelope() throws Exception {
        BoardService service = mock(BoardService.class);
        when(service.create(any(BoardCreateRequest.class)))
                .thenReturn(new BoardResponse(1L, "제목", "내용", "작성자", 0, null, null));
        MockMvc mockMvc = mockMvc(service);

        String body = mockMvc.perform(post("/api/boards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"제목","content":"내용","writer":"작성자"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(body);
        assertThat(json.path("success").asBoolean()).isTrue();
        assertThat(json.path("data").path("id").asLong()).isEqualTo(1L);
        assertThat(json.path("message").asText()).isEqualTo("게시글이 생성되었습니다.");
    }

    @Test
    void controllerRejectsBlankTitleAndWriter() throws Exception {
        MockMvc mockMvc = mockMvc(mock(BoardService.class));

        String body = mockMvc.perform(post("/api/boards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":" ","content":"내용","writer":""}
                                """))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(body);
        assertThat(json.path("success").asBoolean()).isFalse();
        assertThat(json.path("data").path("title").asText()).isEqualTo("제목은 필수입니다.");
        assertThat(json.path("data").path("writer").asText()).isEqualTo("작성자는 필수입니다.");
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
