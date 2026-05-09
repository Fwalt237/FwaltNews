package com.mjc.school.service.exceptions;

public class ResourceConflictServiceException extends ServiceException {
    public ResourceConflictServiceException(final String message, final String code, final String details) {
        super(message, code, details);
    }
}
