package com.xxrin.board.dto.response;

import java.util.List;
import org.springframework.data.domain.Page;

/** 목록 데이터와 전체 건수를 함께 제공하는 페이지 응답이다. */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {

    /** Spring Data가 계산한 페이지 정보를 API 응답으로 변환한다. */
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                List.copyOf(page.getContent()),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}
