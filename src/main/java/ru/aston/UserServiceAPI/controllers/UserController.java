package ru.aston.UserServiceAPI.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.aston.UserServiceAPI.Utils.Loggable;
import ru.aston.UserServiceAPI.Utils.NotValidUserException;
import ru.aston.UserServiceAPI.Utils.UserDTOValidator;
import ru.aston.UserServiceAPI.Utils.UserNotFoundException;
import ru.aston.UserServiceAPI.dtos.UserDTOIn;
import ru.aston.UserServiceAPI.dtos.UserDTOOut;
import ru.aston.UserServiceAPI.entitys.User;
import ru.aston.UserServiceAPI.services.UserService;

import java.util.List;
import java.util.Optional;

import static ru.aston.UserServiceAPI.Utils.ErrorMessageConverter.convertToMessage;

@RestController
@RequestMapping ("/user")
@Loggable
public class UserController {

    private final UserService userService;
    private final UserDTOValidator validator;

    @Autowired
    public UserController(UserService userService,UserDTOValidator validator) {
        this.userService = userService;
        this.validator = validator;
    }

    @GetMapping
    public ResponseEntity<UserDTOOut> getUser(@RequestParam (required = false) Long id,
            @RequestParam (required = false) String name,
            @RequestParam (required = false) String email) {
        Optional<User> findedUser = Optional.empty();
        if (id != null && id > 0) {
            findedUser = userService.getUserById(id);
        } else if (name != null && ! name.isBlank()) {
            findedUser = userService.getUserByName(name);
        } else if (email != null && ! email.isBlank()) {
            findedUser = userService.getUserByEmail(email);
        }
        if (findedUser.isPresent()) {
            return new ResponseEntity<>(userService.getDTOFromUser(findedUser.get()),HttpStatus.OK);
        }
        throw new UserNotFoundException();
    }

    @GetMapping ("/all")
    public ResponseEntity<List<UserDTOOut>> getAllUsers(@RequestParam (required = false) Integer page,
            @RequestParam (required = false) Integer size,
            @RequestParam (required = false) String sort) {
        List<UserDTOOut> userDTOOutList;
        boolean sortNotNullAndEqAscOrDesc = sort != null && (sort.equals("asc") || sort.equals("desc"));
        if (page != null && page >= 0 && size != null && size > 0) {
            if (sortNotNullAndEqAscOrDesc) {
                userDTOOutList = userService.getAllUsersWithPaginationAndSort(page,size,sort);
            } else {
                userDTOOutList = userService.getAllUsersWithPagination(page,size);
            }
        } else {
            if (sortNotNullAndEqAscOrDesc) {
                userDTOOutList = userService.getAllUsersDefaultWithSort(sort);
            } else {
                userDTOOutList = userService.getAllUsersDefault();
            }
        }
        return new ResponseEntity<>(userDTOOutList,HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<UserDTOOut> createUser(@RequestBody @Valid UserDTOIn userDTOIn,BindingResult bindingResult) {
        validator.validate(userDTOIn,bindingResult);
        if (bindingResult.hasErrors()) throw new NotValidUserException(convertToMessage(bindingResult));
        UserDTOOut userDTOOut = userService.createUser(userDTOIn);
        return new ResponseEntity<>(userDTOOut,HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity<UserDTOOut> deleteUser(@RequestParam Long id) {
        if (id == null || id <= 0) throw new UserNotFoundException();
        Optional<UserDTOOut> userDTOOutOptional = userService.deleteUserById(id);
        if (userDTOOutOptional.isEmpty()) throw new UserNotFoundException();
        return new ResponseEntity<>(userDTOOutOptional.get(),HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<UserDTOOut> updateUser(@RequestParam Long id,
            @RequestBody @Valid UserDTOIn userDTOIn,
            BindingResult bindingResult) {
        validator.validate(userDTOIn,bindingResult);
        if (bindingResult.hasErrors() || id == null || id <= 0)
            throw new NotValidUserException(convertToMessage(bindingResult));
        Optional<UserDTOOut> userDTOOutOptional = userService.updateUser(id,userDTOIn);
        if (userDTOOutOptional.isEmpty()) throw new UserNotFoundException();
        return new ResponseEntity<>(userDTOOutOptional.get(),HttpStatus.OK);
    }
}
