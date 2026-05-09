package com.mjc.school.repository.exception;

public class SearchOperationNotFoundException extends RuntimeException{
    public SearchOperationNotFoundException() {}

    public SearchOperationNotFoundException(final String message) {
        super(message);
    }
}
