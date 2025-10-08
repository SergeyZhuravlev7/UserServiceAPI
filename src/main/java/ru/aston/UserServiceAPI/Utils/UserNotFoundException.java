package ru.aston.UserServiceAPI.Utils;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException() {
        super(ErrorMessages.UserNotFound.getMessage());
    }
}
