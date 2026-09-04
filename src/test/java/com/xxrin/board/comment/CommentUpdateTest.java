package com.xxrin.board.comment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.xxrin.board.domain.Board;
import com.xxrin.board.domain.Comment;
import com.xxrin.board.domain.Member;
import com.xxrin.board.dto.request.CommentUpdateRequest;
import com.xxrin.board.repository.BoardRepository;
import com.xxrin.board.repository.CommentRepository;
import com.xxrin.board.repository.MemberRepository;
import com.xxrin.board.service.CommentService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class CommentUpdateTest {

    @Test
    void ownerCanUpdateComment() {
        CommentRepository comments = mock(CommentRepository.class);
        Member owner = Member.create("owner@example.com", "hash", "작성자", "01012345678");
        ReflectionTestUtils.setField(owner, "id", 10L);
        Board board = Board.builder().title("제목").author(owner).build();
        Comment comment = Comment.builder()
                .content("기존 댓글")
                .author(owner)
                .board(board)
                .build();
        when(comments.findByBoard_IdAndId(1L, 20L)).thenReturn(Optional.of(comment));

        var response = new CommentService(
                mock(BoardRepository.class),
                comments,
                mock(MemberRepository.class))
                .update(10L, 1L, 20L, new CommentUpdateRequest("수정 댓글"));

        assertThat(response.content()).isEqualTo("수정 댓글");
    }
}
