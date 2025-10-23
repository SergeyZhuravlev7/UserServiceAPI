package ru.aston.UserServiceAPI.controllers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.validation.BindingResult;
import ru.aston.UserServiceAPI.Utils.NotValidUserException;
import ru.aston.UserServiceAPI.Utils.UserDTOValidator;
import ru.aston.UserServiceAPI.Utils.UserNotFoundException;
import ru.aston.UserServiceAPI.dtos.UserDTORequest;
import ru.aston.UserServiceAPI.dtos.UserDTOResponse;
import ru.aston.UserServiceAPI.entitys.User;
import ru.aston.UserServiceAPI.hateoas.UserAssembler;
import ru.aston.UserServiceAPI.kafka.ProducerService;
import ru.aston.UserServiceAPI.kafka.Sendable;
import ru.aston.UserServiceAPI.services.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith (MockitoExtension.class)
@ActiveProfiles ("test")
class UserControllerUnitTest {

    static List<UserDTOResponse> userDTOResponseList;
    static List<EntityModel<UserDTOResponse>> entityModels;
    UserDTORequest validUserDTORequest;
    User validUser;
    UserDTOResponse validUserDTOResponse;
    UserDTORequest invalidUserDTORequest;
    @Mock
    private UserService userService;
    @Mock
    private UserDTOValidator userDTOValidator;
    @Mock
    private ProducerService producerService;
    @InjectMocks
    private UserController userController;
    @Mock
    private UserAssembler userAssembler;

    @BeforeAll
    static void beforeAll() {
        userDTOResponseList = new ArrayList<>();
        entityModels = new ArrayList<>();

        for (int i = 0;i < 5;i++) {
            String name = "name" + i;
            String email = "testemail" + i + "gmail.com";
            int age = 30 + i;
            userDTOResponseList.add(new UserDTOResponse(name,email,age));
            entityModels.add(EntityModel.of(new UserDTOResponse(name,email,age)));
        }
    }

    @BeforeEach
    void setUp() {
        validUserDTORequest = new UserDTORequest("SomeName","someemail@gmail.com",30);
        validUser = new User(validUserDTORequest.getName(),validUserDTORequest.getEmail(),validUserDTORequest.getAge());
        validUser.setId(1L);
        validUserDTOResponse = new UserDTOResponse(validUser.getName(),validUser.getEmail(),validUser.getAge());
        invalidUserDTORequest = new UserDTORequest("","ActuallyNotEmail",999);
    }

    @Test
    void getUserWithValidIdShouldReturnUser() throws Exception {
        when(userService.getUserById(validUser.getId())).thenReturn(Optional.of(validUser));
        when(userService.getDTOFromUser(validUser)).thenReturn(validUserDTOResponse);
        when(userAssembler.toModel(validUserDTOResponse)).thenReturn(EntityModel.of(validUserDTOResponse));

        ResponseEntity<EntityModel<UserDTOResponse>> response = userController.getUser(validUser.getId(),null,null);

        assertEquals(HttpStatus.OK,response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        assertEquals(validUserDTOResponse,response.getBody().getContent());
    }

    @Test
    void getUserWithValidNameShouldReturnUser() throws Exception {
        when(userService.getUserByName(validUser.getName())).thenReturn(Optional.of(validUser));
        when(userService.getDTOFromUser(validUser)).thenReturn(validUserDTOResponse);
        when(userAssembler.toModel(validUserDTOResponse)).thenReturn(EntityModel.of(validUserDTOResponse));

        ResponseEntity<EntityModel<UserDTOResponse>> response = userController.getUser(null,validUser.getName(),null);

        assertEquals(HttpStatus.OK,response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        assertEquals(validUserDTOResponse,response.getBody().getContent());
    }

    @Test
    void getUserWithValidEmailShouldReturnUser() throws Exception {
        when(userService.getUserByEmail(validUser.getEmail())).thenReturn(Optional.of(validUser));
        when(userService.getDTOFromUser(validUser)).thenReturn(validUserDTOResponse);
        when(userAssembler.toModel(validUserDTOResponse)).thenReturn(EntityModel.of(validUserDTOResponse));

        ResponseEntity<EntityModel<UserDTOResponse>> response = userController.getUser(null,null,validUser.getEmail());

        assertEquals(HttpStatus.OK,response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        assertEquals(validUserDTOResponse,response.getBody().getContent());
    }

    @Test
    void getUserWithAllArgsShouldReturnUserByIdAndIgnoreOtherArgs() throws Exception {
        when(userService.getUserById(validUser.getId())).thenReturn(Optional.of(validUser));
        when(userService.getDTOFromUser(validUser)).thenReturn(validUserDTOResponse);
        when(userAssembler.toModel(validUserDTOResponse)).thenReturn(EntityModel.of(validUserDTOResponse));

        ResponseEntity<EntityModel<UserDTOResponse>> response = userController.getUser(validUser.getId(),
                validUser.getName(),
                validUser.getEmail());

        assertEquals(HttpStatus.OK,response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        assertEquals(validUserDTOResponse,response.getBody().getContent());
        verify(userService,times(1)).getUserById(validUser.getId());
        verify(userService,never()).getUserByEmail(anyString());
        verify(userService,never()).getUserByName(anyString());
    }

    @ParameterizedTest
    @MethodSource ("invalidUserArgsMethodSource")
    void getUserWithNotValidArgsShouldThrowUserNotFoundException(Long id,String name,String email) {
        assertThrows(UserNotFoundException.class,() -> userController.getUser(id,name,email));
    }

    @ParameterizedTest
    @CsvSource (value = {"1,10","0,19","2,33","20,48"})
    void getAllUsersWithPageAndSizeShouldReturnListOfUsers(int page,int size) throws Exception {
        when(userService.getAllUsersWithPagination(anyInt(),anyInt())).thenReturn(userDTOResponseList);
        when(userAssembler.toCollectionModel(userDTOResponseList)).thenReturn(CollectionModel.of(entityModels));

        ResponseEntity<CollectionModel<EntityModel<UserDTOResponse>>> response = userController.getAllUsers(page,size,null);

        assertEquals(HttpStatus.OK,response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        assertTrue(response.getBody().getContent().containsAll(entityModels));
        assertEquals(response
                .getBody()
                .getContent()
                .size(),entityModels.size());
        verify(userService,times(1)).getAllUsersWithPagination(page,size);
    }

    @ParameterizedTest
    @ValueSource (strings = {"asc","desc"})
    void getAllUsersWithSortShouldReturnListOfUsers(String sort) throws Exception {
        when(userService.getAllUsersDefaultWithSort(sort)).thenReturn(userDTOResponseList);
        when(userAssembler.toCollectionModel(userDTOResponseList)).thenReturn(CollectionModel.of(entityModels));

        ResponseEntity<CollectionModel<EntityModel<UserDTOResponse>>> response = userController.getAllUsers(null,null,sort);

        assertEquals(HttpStatus.OK,response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        assertTrue(response.getBody().getContent().containsAll(entityModels));
        assertEquals(response
                .getBody()
                .getContent()
                .size(),entityModels.size());
        verify(userService,times(1)).getAllUsersDefaultWithSort(sort);
    }

    @ParameterizedTest
    @CsvSource (value = {"1,2,notSort","1,2,blabla","1,2,"})
    void getAllUsersWithAllArgsShouldNotInvokeGetUserWithPagination(Integer page,Integer size,String sort) throws Exception {
        userController.getAllUsers(page,size,sort);

        verify(userService,never()).getAllUsersWithPaginationAndSort(anyInt(),anyInt(),anyString());
    }

    @ParameterizedTest
    @CsvSource (value = {"0,,null",",0,null",",,null",",0,notSort"})
    void getAllUserWithInvalidArgsShouldInvokeGetUserDefault(Integer page,Integer size,String sort) {
        userController.getAllUsers(page,size,sort);

        verify(userService,never()).getAllUsersWithPaginationAndSort(anyInt(),anyInt(),anyString());
        verify(userService,never()).getAllUsersWithPagination(anyInt(),anyInt());
        verify(userService,never()).getAllUsersDefaultWithSort(anyString());
        verify(userService,times(1)).getAllUsersDefault();
    }

    @Test
    void createUserShouldReturnCreatedUser() throws Exception {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.createUser(validUserDTORequest)).thenReturn(validUserDTOResponse);
        doNothing()
                .when(producerService)
                .send(any(Sendable.class))
        ;

        ResponseEntity<UserDTOResponse> response = userController.createUser(validUserDTORequest,bindingResult);

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(validUserDTOResponse,response.getBody());
    }

    @Test
    void createUserShouldThrowNotValidUserException() throws Exception {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);

        assertThrows(NotValidUserException.class,() -> userController.createUser(validUserDTORequest,bindingResult));
    }

    @ParameterizedTest
    @CsvSource (value = {"-1","-2","-3",","})
    void deleteUserShouldThrowUserNotFoundException(Long id) throws Exception {

        assertThrows(UserNotFoundException.class,() -> userController.deleteUser(id));
    }

    @ParameterizedTest
    @CsvSource (value = {"1","2","3","4"})
    void deleteUserShouldTryToFindUserThenThrow(Long id) throws Exception {
        when(userService.deleteUserById(id)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,() -> userController.deleteUser(id));
        verify(userService,times(1)).deleteUserById(id);
    }

    @ParameterizedTest
    @CsvSource (value = {"1","2","3","4"})
    void deleteUserShouldReturnEntityWithDeletedUser(Long id) throws Exception {
        when(userService.deleteUserById(id)).thenReturn(Optional.of(validUserDTOResponse));
        doNothing()
                .when(producerService)
                .send(any(Sendable.class))
        ;

        ResponseEntity<UserDTOResponse> response = userController.deleteUser(id);

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(validUserDTOResponse,response.getBody());
    }

    @ParameterizedTest
    @CsvSource (value = {"1","2","3","4"})
    void updateUserShouldReturnUpdatedUser(Long id) throws Exception {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.updateUser(id,validUserDTORequest)).thenReturn(Optional.of(validUserDTOResponse));

        ResponseEntity<UserDTOResponse> response = userController.updateUser(id,validUserDTORequest,bindingResult);

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(validUserDTOResponse,response.getBody());
    }

    @ParameterizedTest
    @CsvSource (value = {"1","2","3","4"})
    void updateUserWithInvalidUserDTOInShouldThrowNotValidException(Long id) throws Exception {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);

        assertThrows(NotValidUserException.class,() -> userController.updateUser(id,validUserDTORequest,bindingResult));
    }

    @ParameterizedTest
    @CsvSource (value = {"0",",","-300","-3"})
    void updateUserWithInvalidIdShouldThrowNotValidException(Long id) throws Exception {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        assertThrows(NotValidUserException.class,() -> userController.updateUser(id,validUserDTORequest,bindingResult));
    }

    @ParameterizedTest
    @CsvSource (value = {"1","2","3","4"})
    void updateUserWithInvalidIdAndUserDTOInShouldThrowNotValidUserException(Long id) throws Exception {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);

        assertThrows(NotValidUserException.class,() -> userController.updateUser(id,validUserDTORequest,bindingResult));
    }

    private static Stream<Arguments> invalidUserArgsMethodSource() {
        return Stream.of(Arguments.of(null,null,null),
                Arguments.of(0L,"",null),
                Arguments.of(Long.MIN_VALUE,null,null),
                Arguments.of(0L,"",""),
                Arguments.of(0L,null,""));
    }
}