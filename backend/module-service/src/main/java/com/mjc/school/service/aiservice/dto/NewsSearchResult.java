package com.mjc.school.service.aiservice.dto;

import java.util.List;

public record NewsSearchResult(List<Item> articles, int totalFound) {
    public record Item(Long id, String title, String excerpt, String publishedAt,
                       String author, String tags){}
}
