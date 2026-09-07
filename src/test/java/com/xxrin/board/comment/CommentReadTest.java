package com.xxrin.board.comment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.xxrin.board.domain.Board;
import com.xxrin.board.domain.Comment;
import com.xxrin.board.domain.Member;
import com.xxrin.board.repository.BoardRepository;
import com.xxrin.board.repository.CommentRepository;
import com.xxrin.board.repository.MemberRepository;
import com.xxrin.board.service.CommentService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CommentReadTest {

    @Test
    void listsCommentsForExistingBoard() {
        BoardRepository boards = mock(BoardRepository.class);
        CommentRepository comments = mock(CommentRepository.class);
        Member author = Member.create("user@example.com", "hash", "작성자", "01012345678");
        Board board = Board.builder().title("제목").author(author).build();
        Comment comment = Comment.builder().content("댓글").author(author).board(board).build();
        when(boards.findById(1L)).thenReturn(Optional.of(board));
        when(comments.findAllByBoard_IdOrderByCreatedAtAscIdAsc(1L))
                .thenReturn(List.of(comment));

        assertThat(new CommentService(boards, comments, mock(MemberRepository.class)).findAll(1L))
                .extracting("writer")
                .containsExactly("작성자");
    }
}
