package ru.aston.UserServiceAPI.Utils;

public enum ErrorMessages {
    BAD_REQUEST("Request should contains required parameters"),
    NOT_READABLE("Request body should not be empty, and should contains name, email, age."),
    UNKNOWN_EXCEPTION("Something went wrong"),
    USER_NOT_FOUND("Sorry, but user with current parameters was not found"),
    ;

    private final String message;

    ErrorMessages(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

}
