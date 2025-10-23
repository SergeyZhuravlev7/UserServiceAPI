package ru.aston.UserServiceAPI.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.aston.UserServiceAPI.Utils.Loggable;
import ru.aston.UserServiceAPI.Utils.NotValidUserException;
import ru.aston.UserServiceAPI.Utils.UserDTOValidator;
import ru.aston.UserServiceAPI.Utils.UserNotFoundException;
import ru.aston.UserServiceAPI.dtos.UserDTORequest;
import ru.aston.UserServiceAPI.dtos.UserDTOResponse;
import ru.aston.UserServiceAPI.entitys.User;
import ru.aston.UserServiceAPI.hateoas.UserAssembler;
import ru.aston.UserServiceAPI.kafka.CreatedKafkaMessage;
import ru.aston.UserServiceAPI.kafka.DeletedKafkaMessage;
import ru.aston.UserServiceAPI.kafka.ProducerService;
import ru.aston.UserServiceAPI.services.UserService;

import java.util.List;
import java.util.Optional;

import static ru.aston.UserServiceAPI.Utils.ErrorMessageConverter.convertToMessage;

@RestController
@RequestMapping (value = "/user", produces = MediaTypes.HAL_FORMS_JSON_VALUE)
@Loggable
public class UserController {

    private final UserService userService;
    private final UserDTOValidator validator;
    private final ProducerService producerService;
    private final UserAssembler assembler;

    @Autowired
    public UserController(UserService userService,
            UserDTOValidator validator,
            ProducerService producerService,
            UserAssembler assembler) {
        this.userService = userService;
        this.validator = validator;
        this.producerService = producerService;
        this.assembler = assembler;
    }

    @GetMapping
    public ResponseEntity<EntityModel<UserDTOResponse>> getUser(@RequestParam (required = false) Long id,
            @RequestParam (required = false) String name,
            @RequestParam (required = false) String email) {
        Optional<User> foundUser = Optional.empty();
        if (id != null && id > 0) {
            foundUser = userService.getUserById(id);
        } else if (name != null && ! name.isBlank()) {
            foundUser = userService.getUserByName(name);
        } else if (email != null && ! email.isBlank()) {
            foundUser = userService.getUserByEmail(email);
        }
        if (foundUser.isPresent()) {
            UserDTOResponse userResponse = userService.getDTOFromUser(foundUser.get());
            return new ResponseEntity<>(assembler.toModel(userResponse), HttpStatus.OK);
        }
        throw new UserNotFoundException();
    }

    @GetMapping ("/all")
    public ResponseEntity<CollectionModel<EntityModel<UserDTOResponse>>> getAllUsers(@RequestParam (required = false) Integer page,
            @RequestParam (required = false) Integer size,
            @RequestParam (required = false) String sort) {
        List<UserDTOResponse> userDTOResponseList;
        boolean sortNotNullAndEqAscOrDesc = sort != null && (sort.equals("asc") || sort.equals("desc"));
        if (page != null && page >= 0 && size != null && size > 0) {
            if (sortNotNullAndEqAscOrDesc) {
                userDTOResponseList = userService.getAllUsersWithPaginationAndSort(page,size,sort);
            } else {
                userDTOResponseList = userService.getAllUsersWithPagination(page,size);
            }
        } else {
            if (sortNotNullAndEqAscOrDesc) {
                userDTOResponseList = userService.getAllUsersDefaultWithSort(sort);
            } else {
                userDTOResponseList = userService.getAllUsersDefault();
            }
        }
        return new ResponseEntity<>(assembler.toCollectionModel(userDTOResponseList), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<UserDTOResponse> createUser(@RequestBody @Valid UserDTORequest userDTORequest,
            BindingResult bindingResult) {
        validator.validate(userDTORequest,bindingResult);
        if (bindingResult.hasErrors()) throw new NotValidUserException(convertToMessage(bindingResult));
        UserDTOResponse userDTOResponse = userService.createUser(userDTORequest);
        producerService.send(new CreatedKafkaMessage(userDTOResponse.getEmail()));
        return new ResponseEntity<>(userDTOResponse,HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity<UserDTOResponse> deleteUser(@RequestParam Long id) {
        if (id == null || id <= 0) throw new UserNotFoundException();
        Optional<UserDTOResponse> userDTOOutOptional = userService.deleteUserById(id);
        if (userDTOOutOptional.isEmpty()) throw new UserNotFoundException();
        UserDTOResponse userDTOResponse = userDTOOutOptional.get();
        producerService.send(new DeletedKafkaMessage(userDTOResponse.getEmail()));
        return new ResponseEntity<>(userDTOOutOptional.get(),HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<UserDTOResponse> updateUser(@RequestParam Long id,
            @RequestBody @Valid UserDTORequest userDTORequest,
            BindingResult bindingResult) {
        validator.validate(userDTORequest,bindingResult);
        if (bindingResult.hasErrors() || id == null || id <= 0)
            throw new NotValidUserException(convertToMessage(bindingResult));
        Optional<UserDTOResponse> userDTOOutOptional = userService.updateUser(id,userDTORequest);
        if (userDTOOutOptional.isEmpty()) throw new UserNotFoundException();
        return new ResponseEntity<>(userDTOOutOptional.get(),HttpStatus.OK);
    }
}
