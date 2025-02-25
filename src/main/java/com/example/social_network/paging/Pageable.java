package com.example.social_network.paging;

public class Pageable {
    private final int pageSize;
    private final int pageNumber;

    public Pageable(int pageSize, int pageNumber) {
        this.pageSize = pageSize;
        this.pageNumber = pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getPageNumber() {
        return pageNumber;
    }
}