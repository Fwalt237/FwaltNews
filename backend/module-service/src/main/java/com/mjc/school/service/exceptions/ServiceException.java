package com.mjc.school.service.exceptions;

public class ServiceException extends RuntimeException {

    private final String code;

    private final String details;

    public ServiceException(final String message, final String code, final String details) {
        super(message);
        this.code = code;
        this.details = details;
    }

    public String getCode() {
        return code;
    }

    public String getDetails() {
        return details;
    }
}
