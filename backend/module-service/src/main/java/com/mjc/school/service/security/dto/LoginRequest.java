package com.mjc.school.service.security.dto;

import com.mjc.school.service.validator.constraint.NotNull;
import com.mjc.school.service.validator.constraint.Size;

public record LoginRequest(
        @NotNull
        @Size(max=50)
        String username,

        @NotNull
        @Size(max=255)
        String password
) {
}
