package com.xxrin.board.board;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.xxrin.board.domain.Board;
import com.xxrin.board.domain.Member;
import com.xxrin.board.dto.request.BoardUpdateRequest;
import com.xxrin.board.repository.BoardRepository;
import com.xxrin.board.repository.MemberRepository;
import com.xxrin.board.service.BoardService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class BoardUpdateTest {

    @Test
    void ownerCanUpdateBoard() {
        BoardRepository boards = mock(BoardRepository.class);
        Member owner = Member.create("owner@example.com", "hash", "작성자", "01012345678");
        ReflectionTestUtils.setField(owner, "id", 10L);
        Board board = Board.builder()
                .title("기존 제목")
                .content("기존 내용")
                .author(owner)
                .build();
        when(boards.findById(1L)).thenReturn(Optional.of(board));

        var response = new BoardService(boards, mock(MemberRepository.class))
                .update(10L, 1L, new BoardUpdateRequest("새 제목", "새 내용"));

        assertThat(response.title()).isEqualTo("새 제목");
        assertThat(response.content()).isEqualTo("새 내용");
    }
}
