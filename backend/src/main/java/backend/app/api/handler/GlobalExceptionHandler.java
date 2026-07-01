package backend.app.api.handler;

import backend.app.api.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Instant;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxSize(MaxUploadSizeExceededException e, HttpServletRequest request) {
        log.warn("Upload rejected: file too large path={}", request.getRequestURI());
        return error(HttpStatus.BAD_REQUEST, "File size must not exceed 20 MB", request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException e, HttpServletRequest request) {
        log.warn("Bad request path={} message={}", request.getRequestURI(), e.getMessage());
        return error(HttpStatus.BAD_REQUEST, e.getMessage(), request);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleStatus(ResponseStatusException e, HttpServletRequest request) {
        String message = e.getReason() == null ? "Request failed" : e.getReason();
        if (e.getStatusCode().is4xxClientError()) {
            log.warn("Request failed path={} status={} message={}", request.getRequestURI(), e.getStatusCode(), message);
        } else {
            log.error("Request failed path={} status={} message={}", request.getRequestURI(), e.getStatusCode(), message);
        }
        return ResponseEntity
                .status(e.getStatusCode())
                .body(new ErrorResponse(
                        Instant.now(),
                        e.getStatusCode().value(),
                        e.getStatusCode().toString(),
                        message,
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ErrorResponse> handleIo(IOException e, HttpServletRequest request) {
        log.error("File processing failed path={}", request.getRequestURI(), e);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "File processing failed", request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException e,
            HttpServletRequest request
    ) {
        String message = e.getBindingResult().getFieldErrors().isEmpty()
                ? "Validation failed"
                : e.getBindingResult().getFieldErrors().getFirst().getDefaultMessage();
        log.warn("Validation failed path={} message={}", request.getRequestURI(), message);
        return error(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler({ConstraintViolationException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ErrorResponse> handleConstraintViolation(Exception e, HttpServletRequest request) {
        log.warn("Validation failed path={} message={}", request.getRequestURI(), e.getMessage());
        return error(HttpStatus.BAD_REQUEST, e.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleInternal(Exception e, HttpServletRequest request) {
        log.error("Internal server error path={}", request.getRequestURI(), e);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", request);
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String message, HttpServletRequest request) {
        return ResponseEntity
                .status(status)
                .body(new ErrorResponse(
                        Instant.now(),
                        status.value(),
                        status.getReasonPhrase(),
                        message,
                        request.getRequestURI()
                ));
    }
}
