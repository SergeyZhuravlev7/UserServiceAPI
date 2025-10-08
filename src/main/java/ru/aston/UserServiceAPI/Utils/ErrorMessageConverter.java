package ru.aston.UserServiceAPI.Utils;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.Map;

public class ErrorMessageConverter {

    public static Map<String, String> convertToMessage(BindingResult bindingResult) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : bindingResult.getFieldErrors()) {
            errors.put(error.getField(),error.getDefaultMessage());
        }
        return errors;
    }
}
