package com.ankur.loganalyzer.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PaginationUtils {

    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    public static final int MIN_PAGE_SIZE = 1;

    public static Pageable createPageable(int page, int size) {
        return createPageable(page, size, null);
    }

    public static Pageable createPageable(int page, int size, Sort sort) {
        if (page < 0) {
            page = 0;
        }
        if (size < MIN_PAGE_SIZE) {
            size = DEFAULT_PAGE_SIZE;
        }
        if (size > MAX_PAGE_SIZE) {
            size = MAX_PAGE_SIZE;
        }

        if (sort != null) {
            return PageRequest.of(page, size, sort);
        }
        return PageRequest.of(page, size);
    }

    public static Pageable createPageableWithSort(int page, int size, String sortBy, String direction) {
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(sortDirection, sortBy);
        return createPageable(page, size, sort);
    }

    public static int getOffset(int page, int size) {
        return page * size;
    }

    public static int getTotalPages(long total, int size) {
        return (int) Math.ceil((double) total / size);
    }
}
