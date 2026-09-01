package com.xxrin.board.board;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.xxrin.board.domain.Board;
import com.xxrin.board.domain.Comment;
import com.xxrin.board.dto.response.BoardDetailResponse;
import com.xxrin.board.exception.EntityNotFoundException;
import com.xxrin.board.repository.BoardRepository;
import com.xxrin.board.service.BoardService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class BoardDetailTest {

    @Test
    void detailIncreasesViewCountAndIncludesComments() {
        BoardRepository repository = mock(BoardRepository.class);
        Board board = board(1L);
        Comment comment = Comment.builder()
                .content("댓글")
                .writer("댓글 작성자")
                .board(board)
                .build();
        ReflectionTestUtils.setField(comment, "id", 10L);
        when(repository.findById(1L)).thenReturn(Optional.of(board));
        BoardService service = new BoardService(repository);

        BoardDetailResponse response = service.findDetail(1L);

        assertThat(response.viewCount()).isEqualTo(1);
        assertThat(response.comments()).extracting("id").containsExactly(10L);
    }

    @Test
    void detailThrowsNotFoundForMissingBoard() {
        BoardRepository repository = mock(BoardRepository.class);
        when(repository.findById(99L)).thenReturn(Optional.empty());
        BoardService service = new BoardService(repository);

        assertThatThrownBy(() -> service.findDetail(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("게시글을 찾을 수 없습니다.");
    }

    private Board board(Long id) {
        Board board = Board.builder()
                .title("제목")
                .content("내용")
                .writer("작성자")
                .build();
        ReflectionTestUtils.setField(board, "id", id);
        return board;
    }
}
