package com.mjc.school.exception.handler;

import com.mjc.school.service.exceptions.NotFoundException;
import com.mjc.school.service.exceptions.ResourceConflictServiceException;
import com.mjc.school.service.exceptions.ServiceException;
import com.mjc.school.service.exceptions.ValidatorException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;


import static com.mjc.school.service.exceptions.ServiceErrorCode.ACCESS_DENIED;
import static com.mjc.school.service.exceptions.ServiceErrorCode.API_VERSION_NOT_SUPPORTED;
import static com.mjc.school.service.exceptions.ServiceErrorCode.AUTHENTICATION_FAILED;
import static com.mjc.school.service.exceptions.ServiceErrorCode.BAD_CREDENTIALS;
import static com.mjc.school.service.exceptions.ServiceErrorCode.RATE_LIMIT_EXCEEDED;
import static com.mjc.school.service.exceptions.ServiceErrorCode.RESOURCE_NOT_FOUND;
import static com.mjc.school.service.exceptions.ServiceErrorCode.UNEXPECTED_ERROR;
import static com.mjc.school.service.exceptions.ServiceErrorCode.USERNAME_DOES_NOT_EXIST;
import static com.mjc.school.service.exceptions.ServiceErrorCode.VALIDATION;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {


    @ExceptionHandler(value = ApiVersionNotSupportedException.class)
    protected ResponseEntity<ErrorResponse> handleApiVersionNotSupportedException(ApiVersionNotSupportedException exc) {
        return buildErrorResponse(API_VERSION_NOT_SUPPORTED.getErrorCode(), API_VERSION_NOT_SUPPORTED.getMessage(), exc.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(value = ValidatorException.class)
    protected ResponseEntity<ErrorResponse> handleValidationException(Exception exc) {
        return buildErrorResponse(VALIDATION.getErrorCode(), String.format(VALIDATION.getMessage(), exc.getMessage()), exc.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = NotFoundException.class)
    protected ResponseEntity<ErrorResponse> handleResourceNotFoundException(Exception exc) {
        return buildErrorResponse(RESOURCE_NOT_FOUND.getErrorCode(), RESOURCE_NOT_FOUND.getMessage(),
                    exc.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = ResourceConflictServiceException.class)
    protected ResponseEntity<ErrorResponse> handleResourceConflictException(ServiceException exc) {
        return buildErrorResponse(exc.getCode(), exc.getMessage(),
                exc.getDetails(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(value = BadCredentialsException.class)
    protected ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException bce){
        return buildErrorResponse(BAD_CREDENTIALS.getErrorCode(),BAD_CREDENTIALS.getMessage(),
                bce.getMessage(),HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    protected ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ade){
        return buildErrorResponse(ACCESS_DENIED.getErrorCode(),ACCESS_DENIED.getMessage(),
                ade.getMessage(),HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(value = UsernameNotFoundException.class)
    protected ResponseEntity<ErrorResponse> handleUsernameNotFoundException(UsernameNotFoundException unfe){
        return buildErrorResponse(USERNAME_DOES_NOT_EXIST.getErrorCode(),USERNAME_DOES_NOT_EXIST.getMessage(),
                unfe.getMessage(),HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = AuthenticationException.class)
    protected ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ae) {
        return buildErrorResponse(AUTHENTICATION_FAILED.getErrorCode(), AUTHENTICATION_FAILED.getMessage(),
                ae.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = Exception.class)
    protected ResponseEntity<ErrorResponse> handleUnexpectedException(Exception exc) {
        return buildErrorResponse(UNEXPECTED_ERROR.getErrorCode(), UNEXPECTED_ERROR.getMessage(),
                exc.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(value = RateLimitExceededException.class)
    protected ResponseEntity<ErrorResponse> handleRateLimit(RateLimitExceededException rle){
        return buildErrorResponse(RATE_LIMIT_EXCEEDED.getErrorCode(), RATE_LIMIT_EXCEEDED.getMessage(),
                rle.getMessage(), HttpStatus.TOO_MANY_REQUESTS);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(String code, String errorMessage,
                                                      String errorDetails, HttpStatus status) {
        return new ResponseEntity<>(new ErrorResponse(code, errorMessage, errorDetails), status);
    }
}
