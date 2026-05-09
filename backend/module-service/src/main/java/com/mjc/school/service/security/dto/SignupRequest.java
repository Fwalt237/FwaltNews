package com.mjc.school.service.security.dto;

import com.mjc.school.service.validator.constraint.NotNull;
import com.mjc.school.service.validator.constraint.Size;
import org.springframework.lang.Nullable;

public record SignupRequest(
        @NotNull
        @Size(max=50)
        String username,

        @NotNull
        @Size(max=255)
        String password,

        @NotNull
        @Size(max=100)
        String email,

        @Nullable
        @Size(max=50)
        String firstName,

        @Nullable
        @Size(max=50)
        String lastName
) {
}
