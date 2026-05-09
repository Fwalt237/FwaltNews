package com.mjc.school.repository.exception;

public class EntityConflictRepositoryException extends RuntimeException {
    public EntityConflictRepositoryException(final String message) {
        super(message);
    }
}
