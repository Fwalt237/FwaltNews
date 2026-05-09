package com.mjc.school.exception.handler;

public class ApiVersionNotSupportedException extends RuntimeException{

    public ApiVersionNotSupportedException(final String errorMessage) {
        super(errorMessage);
    }
}
