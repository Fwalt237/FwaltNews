package com.mjc.school.service.exceptions;

public enum ServiceErrorCode {
    NEWS_ID_DOES_NOT_EXIST("000001", "News with id %d does not exist."),
    AUTHOR_ID_DOES_NOT_EXIST("000002", "Author with id %d does not exist."),
    TAG_ID_DOES_NOT_EXIST("000003", "Tag with id %d does not exist."),
    AUTHOR_DOES_NOT_EXIST_FOR_NEWS_ID("000004", "Author not found for news with id %d."),
    COMMENT_ID_DOES_NOT_EXIST("000005", "Comment with id %d does not exist."),
    VALIDATION("000013", "Validation failed: %s"),
    RESOURCE_NOT_FOUND("000014", "Resource not found"),
    API_VERSION_NOT_SUPPORTED("000016", "This API version is not supported."),
    AUTHOR_CONFLICT("000021", "Author has a persistence conflict due to " +
            "entity id existence."),
    UNEXPECTED_ERROR("000050", "Unexpected error happened on server."),
    NEWS_CONFLICT("000031", "News has a persistence conflict due to " +
            "entity id existence."),
    TAG_CONFLICT("000041", "Tag has a persistence conflict due to " +
            "entity id existence."),
    COMMENT_CONFLICT("000051", "Comment has a persistence conflict due to " +
            "entity id existence."),
    BAD_CREDENTIALS("000032","Invalid credentials"),
    ACCESS_DENIED("000023","Access Denied: you don't have the necessary permissions."),
    USERNAME_DOES_NOT_EXIST("00006","Username not found"),
    AUTHENTICATION_FAILED("000024","Authentication failed"),
    RATE_LIMIT_EXCEEDED("000429", "Rate limit exceeded. Try again in a minute.")
    ;

    private final String errorCode;

    private final String errorMessage;

    ServiceErrorCode(String errorCode, String message) {
        this.errorCode = errorCode;
        this.errorMessage = "ERROR_CODE: " + errorCode + " ERROR_MESSAGE: " + message;
    }

    public String getMessage() {
        return errorMessage;
    }

    public String getErrorCode () {
        return errorCode;
    }
}
