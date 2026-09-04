package com.xxrin.board.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class CommentTest {

    @Test
    void commentBelongsDirectlyToBoardWithoutParentCommentAssociation() {
        Board board = Board.builder()
                .title("제목")
                .content("내용")
                .author(member("작성자"))
                .build();

        Comment comment = Comment.builder()
                .content("댓글")
                .author(member("댓글 작성자"))
                .board(board)
                .build();

        assertThat(comment.getBoard()).isSameAs(board);
        assertThat(comment.getContent()).isEqualTo("댓글");
        assertThat(comment.getAuthor().getNickname()).isEqualTo("댓글 작성자");
        assertThat(Arrays.stream(Comment.class.getDeclaredFields())
                .map(Field::getName))
                .doesNotContain("parent", "parentComment");
    }

    @Test
    void updateChangesCommentContentAndUpdatedAt() {
        Board board = Board.builder()
                .title("제목")
                .content("내용")
                .author(member("작성자"))
                .build();
        Comment comment = Comment.builder()
                .content("기존 댓글")
                .author(member("댓글 작성자"))
                .board(board)
                .build();

        comment.update("수정 댓글");

        assertThat(comment.getContent()).isEqualTo("수정 댓글");
        assertThat(comment.getUpdatedAt()).isNotNull();
    }

    private Member member(String nickname) {
        return Member.create(nickname + "@example.com", "hash", nickname, "01012345678");
    }
}
