package com.mjc.school.service.dto;

import java.time.LocalDateTime;

public record CommentsDtoForNewsResponse(
        Long id,
        String content,
        LocalDateTime createdDate,
        LocalDateTime lastUpdatedDate
) {
}