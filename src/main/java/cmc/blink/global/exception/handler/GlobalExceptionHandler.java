package cmc.blink.global.exception.handler;

import cmc.blink.global.exception.dto.ApiErrorResponse;
import io.sentry.Sentry;
import cmc.blink.global.exception.GeneralException;
import cmc.blink.global.exception.JwtAuthenticationException;
import cmc.blink.global.exception.constant.ErrorCode;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice(annotations = {RestController.class})
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Sentry.captureException(ex);
        return handleExceptionInternalFalse(ex, ErrorCode.INVALID_DATA, headers, status, request);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler
    public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException e, WebRequest request) {
        Sentry.captureException(e);
        return handleExceptionInternal(e, ErrorCode.UNAUTHORIZED, request);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler
    public ResponseEntity<Object> handleGeneralException(GeneralException e, WebRequest request) {
        Sentry.captureException(e);
        return handleExceptionInternal(e, e.getErrorCode(), request);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler
    public ResponseEntity<Object> exception (Exception e, WebRequest request) {
        e.printStackTrace();
        Sentry.captureException(e);
        return handleExceptionInternalFalse(e, ErrorCode.INTERNAL_ERROR, HttpHeaders.EMPTY, ErrorCode.INTERNAL_ERROR.getHttpStatus(), request);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(JwtAuthenticationException.class)
    public ResponseEntity<Object> handleJwtAuthenticationException(JwtAuthenticationException e, WebRequest request) {
        Sentry.captureException(e);
        return handleExceptionInternal(e, ErrorCode.UNAUTHORIZED, request);
    }

    private ResponseEntity<Object> handleExceptionInternal(Exception e, ErrorCode errorCode,
                                                           WebRequest request) {
        return handleExceptionInternal(e, errorCode, HttpHeaders.EMPTY, errorCode.getHttpStatus(),
                request);
    }

    private ResponseEntity<Object> handleExceptionInternal(Exception e, ErrorCode errorCode,
                                                           HttpHeaders headers, HttpStatus status, WebRequest request) {
        ApiErrorResponse body = ApiErrorResponse.of(errorCode, e);
        return super.handleExceptionInternal(
                e,
                body,
                headers,
                status,
                request
        );
    }

    private ResponseEntity<Object> handleExceptionInternalFalse(Exception e, ErrorCode errorCode,
                                                           HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ApiErrorResponse body = ApiErrorResponse.of(errorCode);
        return super.handleExceptionInternal(
                e,
                body,
                headers,
                status,
                request
        );
    }

    private ResponseEntity<Object> handleExceptionInternalFalse(Exception e, ErrorCode errorCode,
                                                                HttpHeaders headers, HttpStatus status, WebRequest request) {
        ApiErrorResponse body = ApiErrorResponse.of(errorCode);
        return super.handleExceptionInternal(
                e,
                body,
                headers,
                status,
                request
        );
    }

}
