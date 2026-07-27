package com.ivan.erp.shared.web;

import org.springframework.data.domain.Page;
import org.springframework.ui.Model;

import java.util.List;
import java.util.stream.IntStream;

public final class PaginationSupport {

    public static final List<Integer> PAGE_SIZES = List.of(10, 25, 50, 100);
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_VISIBLE_PAGES = 5;

    private PaginationSupport() {
    }

    public static int sanitizeSize(int requestedSize) {
        return PAGE_SIZES.contains(requestedSize) ? requestedSize : DEFAULT_SIZE;
    }

    public static void addToModel(Model model, Page<?> page, int requestedSize) {
        model.addAttribute("currentPage", page.getNumber());
        model.addAttribute("size", sanitizeSize(requestedSize));
        model.addAttribute("pageSizes", PAGE_SIZES);
        model.addAttribute("pageNumbers", visiblePages(page));
    }

    private static List<Integer> visiblePages(Page<?> page) {
        int totalPages = page.getTotalPages();
        if (totalPages <= 0) {
            return List.of();
        }

        int current = page.getNumber();
        int start = Math.max(0, current - 2);
        int end = Math.min(totalPages - 1, start + MAX_VISIBLE_PAGES - 1);
        start = Math.max(0, end - MAX_VISIBLE_PAGES + 1);
        return IntStream.rangeClosed(start, end).boxed().toList();
    }
}
