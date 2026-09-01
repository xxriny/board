package com.xxrin.board.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class BoardTest {

    @Test
    void newBoardStartsWithZeroViewsAndCanIncreaseViewCount() {
        Board board = board();

        assertThat(board.getViewCount()).isZero();

        board.increaseViewCount();

        assertThat(board.getViewCount()).isEqualTo(1);
    }

    @Test
    void updateChangesOnlyTitleAndContent() {
        Board board = board();

        board.update("변경 제목", "변경 내용");

        assertThat(board.getTitle()).isEqualTo("변경 제목");
        assertThat(board.getContent()).isEqualTo("변경 내용");
        assertThat(board.getWriter()).isEqualTo("작성자");
    }

    @Test
    void addingAndRemovingCommentKeepsBothSidesInSync() {
        Board board = board();
        Comment comment = Comment.builder()
                .content("댓글")
                .writer("댓글 작성자")
                .board(board)
                .build();

        assertThat(board.getComments()).containsExactly(comment);
        assertThat(comment.getBoard()).isSameAs(board);

        board.removeComment(comment);

        assertThat(board.getComments()).isEmpty();
        assertThat(comment.getBoard()).isNull();
    }

    private Board board() {
        return Board.builder()
                .title("제목")
                .content("내용")
                .writer("작성자")
                .build();
    }
}
