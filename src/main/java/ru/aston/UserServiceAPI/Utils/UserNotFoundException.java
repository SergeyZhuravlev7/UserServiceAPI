package ru.aston.UserServiceAPI.Utils;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException() {
        super(ErrorMessages.USER_NOT_FOUND.getMessage());
    }
}
