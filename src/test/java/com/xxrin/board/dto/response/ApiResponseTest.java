package com.xxrin.board.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ApiResponseTest {

    @Test
    void successFactoryCreatesSuccessfulEnvelope() {
        ApiResponse<String> response = ApiResponse.success("payload", "성공");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEqualTo("payload");
        assertThat(response.getMessage()).isEqualTo("성공");
    }

    @Test
    void errorFactoryCreatesFailedEnvelope() {
        ApiResponse<Void> response = ApiResponse.error("실패");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getData()).isNull();
        assertThat(response.getMessage()).isEqualTo("실패");
    }
}
