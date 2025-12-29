package com.dineo_backend.dineo.common;

import java.util.List;

/**
 * Generic paginated response wrapper
 * @param <T> the type of items in the page
 */
public class PaginatedResponse<T> {
    private List<T> items;
    private PaginationMetadata pagination;

    public PaginatedResponse() {}

    public PaginatedResponse(List<T> items, PaginationMetadata pagination) {
        this.items = items;
        this.pagination = pagination;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    public PaginationMetadata getPagination() {
        return pagination;
    }

    public void setPagination(PaginationMetadata pagination) {
        this.pagination = pagination;
    }

    public static class PaginationMetadata {
        private int currentPage;
        private int pageSize;
        private int totalItems;
        private int totalPages;
        private boolean hasNextPage;
        private boolean hasPreviousPage;

        public PaginationMetadata() {}

        public PaginationMetadata(int currentPage, int pageSize, int totalItems, int totalPages, 
                                  boolean hasNextPage, boolean hasPreviousPage) {
            this.currentPage = currentPage;
            this.pageSize = pageSize;
            this.totalItems = totalItems;
            this.totalPages = totalPages;
            this.hasNextPage = hasNextPage;
            this.hasPreviousPage = hasPreviousPage;
        }

        public int getCurrentPage() {
            return currentPage;
        }

        public void setCurrentPage(int currentPage) {
            this.currentPage = currentPage;
        }

        public int getPageSize() {
            return pageSize;
        }

        public void setPageSize(int pageSize) {
            this.pageSize = pageSize;
        }

        public int getTotalItems() {
            return totalItems;
        }

        public void setTotalItems(int totalItems) {
            this.totalItems = totalItems;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }

        public boolean isHasNextPage() {
            return hasNextPage;
        }

        public void setHasNextPage(boolean hasNextPage) {
            this.hasNextPage = hasNextPage;
        }

        public boolean isHasPreviousPage() {
            return hasPreviousPage;
        }

        public void setHasPreviousPage(boolean hasPreviousPage) {
            this.hasPreviousPage = hasPreviousPage;
        }
    }

    /**
     * Create a paginated response from a list of items
     * @param allItems the complete list of items
     * @param page the page number (1-based)
     * @param pageSize the number of items per page
     * @param <T> the type of items
     * @return paginated response
     */
    public static <T> PaginatedResponse<T> of(List<T> allItems, int page, int pageSize) {
        int totalItems = allItems.size();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        
        // Ensure page is within valid range
        int validPage = Math.max(1, Math.min(page, Math.max(1, totalPages)));
        
        // Calculate start and end indices
        int startIndex = (validPage - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalItems);
        
        // Get page items
        List<T> pageItems = startIndex < totalItems ? allItems.subList(startIndex, endIndex) : List.of();
        
        // Create pagination metadata
        PaginationMetadata metadata = new PaginationMetadata(
            validPage,
            pageSize,
            totalItems,
            totalPages,
            validPage < totalPages,
            validPage > 1
        );
        
        return new PaginatedResponse<>(pageItems, metadata);
    }
}
