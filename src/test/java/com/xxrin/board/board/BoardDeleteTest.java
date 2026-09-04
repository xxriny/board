package com.xxrin.board.board;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.xxrin.board.domain.Board;
import com.xxrin.board.domain.Member;
import com.xxrin.board.exception.BusinessException;
import com.xxrin.board.exception.ErrorCode;
import com.xxrin.board.repository.BoardRepository;
import com.xxrin.board.repository.MemberRepository;
import com.xxrin.board.service.BoardService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class BoardDeleteTest {

    @Test
    void ownerCanDeleteBoard() {
        BoardRepository repository = mock(BoardRepository.class);
        Member owner = Member.create("owner@example.com", "hash", "작성자", "01012345678");
        ReflectionTestUtils.setField(owner, "id", 10L);
        Board board = Board.builder()
                .title("제목")
                .author(owner)
                .build();
        when(repository.findById(1L)).thenReturn(Optional.of(board));
        BoardService service = new BoardService(repository, mock(MemberRepository.class));

        service.delete(10L, 1L);

        verify(repository).delete(board);
    }

    @Test
    void anotherMemberCannotDeleteBoard() {
        BoardRepository repository = mock(BoardRepository.class);
        Member owner = Member.create("owner@example.com", "hash", "작성자", "01012345678");
        ReflectionTestUtils.setField(owner, "id", 10L);
        Board board = Board.builder()
                .title("제목")
                .author(owner)
                .build();
        when(repository.findById(1L)).thenReturn(Optional.of(board));
        BoardService service = new BoardService(repository, mock(MemberRepository.class));

        assertThatThrownBy(() -> service.delete(999L, 1L))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        org.assertj.core.api.Assertions.assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.FORBIDDEN_RESOURCE));
    }
}
