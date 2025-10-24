package ru.aston.UserServiceAPI.Utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler (UserNotFoundException.class)
    @ResponseStatus (HttpStatus.NOT_FOUND)
    public ErrorResponse handleUserNotFoundException(UserNotFoundException ex) {
        //TODO("Implement specific handler logic")
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler (NotValidUserException.class)
    @ResponseStatus (HttpStatus.BAD_REQUEST)
    public ErrorResponseMap handleNotValidUserException(NotValidUserException ex) {
        //TODO("Implement specific handler logic")
        return new ErrorResponseMap(ex.getErrorMap());
    }

    @ExceptionHandler (HttpMessageNotReadableException.class)
    @ResponseStatus (HttpStatus.BAD_REQUEST)
    public ErrorResponse handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        return new ErrorResponse(ErrorMessages.NOT_READABLE.getMessage());
    }

    @ExceptionHandler (ServletRequestBindingException.class)
    @ResponseStatus (HttpStatus.BAD_REQUEST)
    public ErrorResponse handleServletRequestBindingException(ServletRequestBindingException ex) {
        return new ErrorResponse(ErrorMessages.BAD_REQUEST.getMessage());
    }

    @ExceptionHandler (HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()),ex.getStatusCode());
    }

    @ExceptionHandler (Exception.class)
    @ResponseStatus (HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleUnknownException(Exception ex) {
        return new ErrorResponse(ErrorMessages.UNKNOWN_EXCEPTION.getMessage());
    }

    public static class ErrorResponse {

        private String error;
        private LocalDateTime timestamp;

        public ErrorResponse(String error) {
            this.error = error;
            this.timestamp = LocalDateTime.now();
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }
    }

    public static class ErrorResponseMap {
        private Map<String, String> errors;
        private LocalDateTime timestamp;

        public ErrorResponseMap(Map<String, String> errors) {
            this.errors = errors;
            this.timestamp = LocalDateTime.now();
        }

        public Map<String, String> getErrors() {
            return errors;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }
    }
}
