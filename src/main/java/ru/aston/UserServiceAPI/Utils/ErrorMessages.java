package ru.aston.UserServiceAPI.Utils;

public enum ErrorMessages {
    NotReadable("Request body should not be empty, and should contains name, email, age."),
    UserNotFound("Sorry, but user with current parameters was not found"),
    BadRequest("Request should contains required parameters"),
    UnknownException("Something went wrong"),
    ;

    ErrorMessages(String message) {
        this.message = message;
    }

    private final String message;

    public String getMessage() {
        return this.message;
    }

}
