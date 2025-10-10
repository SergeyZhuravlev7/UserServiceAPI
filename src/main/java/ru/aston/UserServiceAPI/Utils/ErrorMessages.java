package ru.aston.UserServiceAPI.Utils;

public enum ErrorMessages {
    NOT_READABLE("Request body should not be empty, and should contains name, email, age."),USER_NOT_FOUND(
            "Sorry, but user with current parameters was not found"),BAD_REQUEST(
            "Request should contains required parameters"),UNKNOWN_EXCEPTION("Something went wrong"),
    ;

    private final String message;

    ErrorMessages(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

}
