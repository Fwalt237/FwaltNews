package com.mjc.school.exception.handler;

public record ErrorResponse(String code, String message, String errorDetails) {
}
