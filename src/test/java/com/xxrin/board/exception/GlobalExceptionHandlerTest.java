package com.xxrin.board.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.xxrin.board.dto.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.resource.NoResourceFoundException;

class GlobalExceptionHandlerTest {

    @Test
    void returnsConfiguredStatusAndCodeForBusinessException() {
        ResponseEntity<ApiResponse<Void>> response = new GlobalExceptionHandler()
                .handleBusiness(new BusinessException(ErrorCode.BOARD_NOT_FOUND));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getCode()).isEqualTo("BOARD_NOT_FOUND");
        assertThat(response.getBody().getMessage())
                .isEqualTo("게시글을 찾을 수 없습니다.");
    }

    @Test
    void returnsApiNotFoundCodeForUnknownResourcePath() {
        ResponseEntity<ApiResponse<Void>> response = new GlobalExceptionHandler()
                .handleNoResource(new NoResourceFoundException(HttpMethod.GET, "not-found"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getCode()).isEqualTo("API_NOT_FOUND");
        assertThat(response.getBody().getMessage())
                .isEqualTo("요청한 API를 찾을 수 없습니다.");
    }
}
