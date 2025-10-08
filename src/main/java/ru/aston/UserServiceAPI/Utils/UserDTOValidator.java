package ru.aston.UserServiceAPI.Utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ru.aston.UserServiceAPI.dtos.UserDTOIn;
import ru.aston.UserServiceAPI.entitys.User;
import ru.aston.UserServiceAPI.services.UserServiceImpl;

import java.util.Optional;

@Component
public class UserDTOValidator implements Validator {

    private final UserServiceImpl userService;

    @Autowired
    public UserDTOValidator(UserServiceImpl userService) {
        this.userService = userService;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return UserDTOIn.class.equals(clazz);
    }

    @Override
    public void validate(Object target,Errors errors) {
        UserDTOIn userDTOIn = (UserDTOIn) target;
        if (userDTOIn.getEmail() != null) {
            Optional<User> userOptional = userService.getUserByEmail(userDTOIn.getEmail());
            if (userOptional.isPresent()) {
                errors.rejectValue("email",null,"Email " + userDTOIn.getEmail() + " already exists.");
            }
        }
    }
}
