package com.xxrin.board.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xxrin.board.exception.ErrorCode;
import org.junit.jupiter.api.Test;

class ApiResponseTest {

    @Test
    void successFactoryCreatesSuccessfulEnvelope() {
        ApiResponse<String> response = ApiResponse.success("payload", "성공");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEqualTo("payload");
        assertThat(response.getCode()).isNull();
        assertThat(response.getMessage()).isEqualTo("성공");
    }

    @Test
    void errorFactoryCreatesFailedEnvelope() {
        ApiResponse<Void> response = ApiResponse.error(ErrorCode.BOARD_NOT_FOUND);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getData()).isNull();
        assertThat(response.getCode()).isEqualTo("BOARD_NOT_FOUND");
        assertThat(response.getMessage()).isEqualTo("게시글을 찾을 수 없습니다.");
    }

    @Test
    void successfulJsonOmitsErrorCode() throws Exception {
        ApiResponse<String> response = ApiResponse.success("payload", "성공");

        JsonNode json = new ObjectMapper().readTree(new ObjectMapper().writeValueAsString(response));

        assertThat(json.has("code")).isFalse();
    }
}
