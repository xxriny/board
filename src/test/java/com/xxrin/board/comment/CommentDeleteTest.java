package com.xxrin.board.comment;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.xxrin.board.domain.Board;
import com.xxrin.board.domain.Comment;
import com.xxrin.board.repository.BoardRepository;
import com.xxrin.board.repository.CommentRepository;
import com.xxrin.board.service.CommentService;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CommentDeleteTest {

    @Test
    void deletesCommentSelectedByBoardAndCommentIds() {
        CommentRepository comments = mock(CommentRepository.class);
        Board board = Board.builder().title("제목").writer("작성자").passwordHash("hash").build();
        Comment comment = Comment.builder().content("댓글").writer("댓글 작성자").board(board).build();
        when(comments.findByBoardIdAndId(1L, 10L)).thenReturn(Optional.of(comment));
        CommentService service = new CommentService(mock(BoardRepository.class), comments);

        service.delete(1L, 10L);

        verify(comments).delete(comment);
    }
}
