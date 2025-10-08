package ru.aston.UserServiceAPI.Utils;

import java.util.Map;

public class NotValidUserException extends RuntimeException {
    private Map<String, String> errors;

    public NotValidUserException(Map<String, String> errors) {
        this.errors = errors;
    }

    public NotValidUserException(String message) {
        super(message);
    }

    public Map<String, String> getErrorMap() {
        return errors;
    }
}
