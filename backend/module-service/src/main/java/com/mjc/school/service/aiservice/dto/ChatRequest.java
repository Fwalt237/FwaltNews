package com.mjc.school.service.aiservice.dto;

import com.mjc.school.service.validator.constraint.NotNull;

public record ChatRequest(
        @NotNull
        String sessionId,

        @NotNull
        String message) {}
