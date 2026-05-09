package com.mjc.school.service.aiservice.dto;

import java.time.LocalDateTime;

public record ChatTurn(String role, String content, LocalDateTime timestamp) {}
