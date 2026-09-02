package com.xxrin.board.board;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.xxrin.board.domain.Board;
import com.xxrin.board.dto.response.PageResponse;
import com.xxrin.board.repository.BoardRepository;
import com.xxrin.board.service.BoardService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

class BoardListTest {

    @Test
    void listReturnsPageMetadataAndRepositoryOrder() {
        BoardRepository repository = mock(BoardRepository.class);
        Board first = board(2L, "두 번째");
        Board second = board(1L, "첫 번째");
        when(repository.findAll(org.mockito.ArgumentMatchers.any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(first, second), PageRequest.of(1, 2), 12));
        BoardService service = new BoardService(repository);

        PageResponse<?> response = service.findAll(1, 2);

        assertThat(response.content()).extracting("id").containsExactly(2L, 1L);
        assertThat(response.page()).isEqualTo(1);
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.totalElements()).isEqualTo(12L);
        assertThat(response.totalPages()).isEqualTo(6);
    }

    private Board board(Long id, String title) {
        Board board = Board.builder()
                .title(title)
                .content("내용")
                .writer("작성자")
                .build();
        ReflectionTestUtils.setField(board, "id", id);
        return board;
    }
}
