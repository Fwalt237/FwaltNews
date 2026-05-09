package com.mjc.school.service.aiservice.dto;

import java.util.List;

public record EmbeddingResponse(Embedding embedding) {
    public record Embedding(List<Double> values){}
}
