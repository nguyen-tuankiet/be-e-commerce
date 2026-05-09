package com.example.becommerce.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Paginated notification inbox response with unread counter.
 */
@Getter
@Builder
public class NotificationListResponse {

    private long unreadCount;
    private List<NotificationResponse> items;
    private PagedResponse.PaginationMeta pagination;
}

