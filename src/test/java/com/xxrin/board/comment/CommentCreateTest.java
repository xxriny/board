package com.xxrin.board.comment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.xxrin.board.domain.Board;
import com.xxrin.board.domain.Comment;
import com.xxrin.board.domain.Member;
import com.xxrin.board.dto.request.CommentCreateRequest;
import com.xxrin.board.repository.BoardRepository;
import com.xxrin.board.repository.CommentRepository;
import com.xxrin.board.repository.MemberRepository;
import com.xxrin.board.service.CommentService;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CommentCreateTest {

    @Test
    void createsCommentForAuthenticatedMember() {
        BoardRepository boards = mock(BoardRepository.class);
        CommentRepository comments = mock(CommentRepository.class);
        MemberRepository members = mock(MemberRepository.class);
        Member author = Member.create("user@example.com", "hash", "댓글 작성자", "01012345678");
        Board board = Board.builder()
                .title("제목")
                .author(author)
                .build();
        when(boards.findById(1L)).thenReturn(Optional.of(board));
        when(members.getReferenceById(10L)).thenReturn(author);
        when(comments.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = new CommentService(boards, comments, members)
                .create(10L, 1L, new CommentCreateRequest("댓글"));

        assertThat(response.writer()).isEqualTo("댓글 작성자");
        verify(boards).incrementCommentCount(1L);
    }
}
