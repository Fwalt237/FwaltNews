package com.mjc.school.service.dto;

import com.mjc.school.service.validator.constraint.NotNull;
import com.mjc.school.service.validator.constraint.Size;

public record AuthorDtoRequest(
    @NotNull
    @Size(min = 3)
    String name) {
}
