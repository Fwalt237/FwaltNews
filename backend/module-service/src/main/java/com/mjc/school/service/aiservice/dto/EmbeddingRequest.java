package com.mjc.school.service.aiservice.dto;

import java.util.List;

public record EmbeddingRequest(String model, Content content, int output_dimensionality) {
    public static EmbeddingRequest of(String model, String text, int dimensions) {
        return new EmbeddingRequest(model, new Content(List.of(new Part(text))), dimensions);
    }
    public record Content(List<Part> parts){}
    public record Part(String text){}
}


