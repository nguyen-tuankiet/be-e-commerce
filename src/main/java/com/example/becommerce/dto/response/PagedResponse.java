package com.example.becommerce.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Paginated list wrapper returned for collection endpoints.
 *
 * <pre>
 * {
 *   "items": [...],
 *   "pagination": {
 *     "page": 1,
 *     "limit": 10,
 *     "total": 85,
 *     "totalPages": 9
 *   }
 * }
 * </pre>
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PagedResponse<T> {

    private final List<T> items;
    private final PaginationMeta pagination;

    @Getter
    @Builder
    public static class PaginationMeta {
        private final int page;
        private final int limit;
        private final long total;
        private final int totalPages;
    }

    /** Convenience builder that calculates totalPages automatically. */
    public static <T> PagedResponse<T> of(List<T> items, int page, int limit, long total) {
        int totalPages = (int) Math.ceil((double) total / limit);
        return PagedResponse.<T>builder()
                .items(items)
                .pagination(PaginationMeta.builder()
                        .page(page)
                        .limit(limit)
                        .total(total)
                        .totalPages(totalPages)
                        .build())
                .build();
    }
}
