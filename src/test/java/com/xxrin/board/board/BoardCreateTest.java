package com.xxrin.board.board;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.xxrin.board.domain.Board;
import com.xxrin.board.domain.Member;
import com.xxrin.board.dto.request.BoardCreateRequest;
import com.xxrin.board.repository.BoardRepository;
import com.xxrin.board.repository.MemberRepository;
import com.xxrin.board.service.BoardService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class BoardCreateTest {

    @Test
    void createsBoardForAuthenticatedMember() {
        BoardRepository boards = mock(BoardRepository.class);
        MemberRepository members = mock(MemberRepository.class);
        Member author = Member.create("user@example.com", "hash", "작성자", "01012345678");
        when(members.getReferenceById(10L)).thenReturn(author);
        when(boards.save(any(Board.class))).thenAnswer(invocation -> {
            Board board = invocation.getArgument(0);
            ReflectionTestUtils.setField(board, "id", 1L);
            return board;
        });

        var response = new BoardService(boards, members)
                .create(10L, new BoardCreateRequest("제목", "내용"));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.writer()).isEqualTo("작성자");
    }
}
