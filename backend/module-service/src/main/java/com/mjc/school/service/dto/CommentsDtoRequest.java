package com.mjc.school.service.dto;

import com.mjc.school.service.validator.constraint.NotNull;
import com.mjc.school.service.validator.constraint.Size;

public record CommentsDtoRequest(
        @NotNull
        @Size(min = 5, max = 255)
        String content,

        @NotNull
        Long newsId) {
}
