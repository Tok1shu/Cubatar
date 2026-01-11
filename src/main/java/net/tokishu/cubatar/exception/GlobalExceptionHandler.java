package net.tokishu.cubatar.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException e) {
        String message = e.getReason() != null ? e.getReason() : e.getStatusCode().toString();

        ErrorResponse response = ErrorResponse.builder()
                .message(message)
                .build();
        return new ResponseEntity<>(response, e.getStatusCode());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(NoResourceFoundException e) { // Before 3.2 was NoHandlerFoundException
        String message = "Resource not found: " + e.getResourcePath() + " use /avatar/{:nickname or :uuid or :base64-url}";

        ErrorResponse response = ErrorResponse.builder()
                .message(message)
                .build();
        return new ResponseEntity<>(response, e.getStatusCode());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        String message = "Method not supported: " + e.getMethod();

        ErrorResponse response = ErrorResponse.builder()
                .message(message)
                .build();
        return new ResponseEntity<>(response, e.getStatusCode());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexceptedException(Exception e) {
        log.error("Unexcepted error", e);
        String message = "Internal server error";

        ErrorResponse response = ErrorResponse.builder()
                .message(message)
                .build();

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
