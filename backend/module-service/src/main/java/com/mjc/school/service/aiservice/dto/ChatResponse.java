package com.mjc.school.service.aiservice.dto;

import java.util.List;

public record ChatResponse(String message, List<Long> articleIds) {}
