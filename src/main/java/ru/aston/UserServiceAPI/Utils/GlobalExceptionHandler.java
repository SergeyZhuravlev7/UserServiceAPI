package ru.aston.UserServiceAPI.Utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler (UserNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUserNotFoundException(UserNotFoundException ex) {
        //TODO("Implement specific handler logic")
        return new ResponseEntity<>(getErrorMap(ex.getMessage()),HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler (NotValidUserException.class)
    public ResponseEntity<Map<String, String>> handleNotValidUserException(NotValidUserException ex) {
        //TODO("Implement specific handler logic")
        return new ResponseEntity<>(ex.getErrorMap(),HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String,String>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        return new ResponseEntity<>(getErrorMap(ErrorMessages.NotReadable.getMessage()),HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ServletRequestBindingException.class)
    public ResponseEntity<Map<String,String>> handleServletRequestBindingException(ServletRequestBindingException ex) {
        return new ResponseEntity<>(getErrorMap(ErrorMessages.BadRequest.getMessage()),HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String,String>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        return new ResponseEntity<>(getErrorMap(ex.getMessage()),ex.getStatusCode());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,String>> handleUnknownException(HttpRequestMethodNotSupportedException ex) {
        return new ResponseEntity<>(getErrorMap(ErrorMessages.UnknownException.getMessage()),ex.getStatusCode());
    }

    private Map<String, String> getErrorMap(String message) {
        Map<String, String> map = new HashMap<>();
        map.put("Error message",message);
        map.put("timestamp",LocalDateTime.now().toString());
        return map;
    }
}
