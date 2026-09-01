package com.xxrin.board.dto.response;

import java.util.List;

/** 목록 데이터와 전체 건수를 함께 제공하는 페이지 응답이다. */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {

    public static <T> PageResponse<T> of(
            List<T> content, int page, int size, long totalElements) {
        int totalPages = totalElements == 0
                ? 0
                : (int) Math.ceil((double) totalElements / size);
        return new PageResponse<>(List.copyOf(content), page, size, totalElements, totalPages);
    }
}
