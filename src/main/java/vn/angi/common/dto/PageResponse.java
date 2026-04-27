package vn.angi.common.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

public record PageResponse<T>(
        List<T> data,
        Pagination pagination
) {
    public record Pagination(int page, int size, long totalElements, int totalPages) {}

    public static <E, T> PageResponse<T> of(Page<E> page, Function<E, T> mapper) {
        return new PageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                new Pagination(page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages())
        );
    }

    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                new Pagination(page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages())
        );
    }
}
