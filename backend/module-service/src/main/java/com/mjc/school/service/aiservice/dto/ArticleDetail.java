package com.mjc.school.service.aiservice.dto;

public record ArticleDetail(Long id, String title, String fullContent, String publishedAt,
                            String author, String tags) {
}
