package com.mjc.school.service.exceptions;

public class ValidatorException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ValidatorException(String message) {
        super(message);
    }
}
