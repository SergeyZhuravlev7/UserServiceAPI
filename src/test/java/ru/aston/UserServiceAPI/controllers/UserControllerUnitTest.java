package ru.aston.UserServiceAPI.controllers;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.validation.BindingResult;
import ru.aston.UserServiceAPI.Utils.NotValidUserException;
import ru.aston.UserServiceAPI.Utils.UserDTOValidator;
import ru.aston.UserServiceAPI.Utils.UserNotFoundException;
import ru.aston.UserServiceAPI.dtos.UserDTOIn;
import ru.aston.UserServiceAPI.dtos.UserDTOOut;
import ru.aston.UserServiceAPI.entitys.User;
import ru.aston.UserServiceAPI.services.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith (MockitoExtension.class)
@ActiveProfiles ("test")
class UserControllerUnitTest {

    static List<UserDTOOut> userDTOOutList;
    UserDTOIn validUserDTOIn;
    User validUser;
    UserDTOOut validUserDTOOut;
    UserDTOIn invalidUserDTOIn;
    @Mock
    private UserService userService;
    @Mock
    private UserDTOValidator userDTOValidator;
    @InjectMocks
    private UserController userController;

    @BeforeAll
    static void beforeAll() {
        userDTOOutList = new ArrayList<>();

        for (int i = 0;i < 5;i++) {
            String name = "name" + i;
            String email = "testemail" + i + "gmail.com";
            int age = 30 + i;
            userDTOOutList.add(new UserDTOOut(name,email,age));
        }
    }

    @BeforeEach
    void setUp() {
        validUserDTOIn = new UserDTOIn("SomeName","someemail@gmail.com",30);
        validUser = new User(validUserDTOIn.getName(),validUserDTOIn.getEmail(),validUserDTOIn.getAge());
        validUser.setId(1L);
        validUserDTOOut = new UserDTOOut(validUser.getName(),validUser.getEmail(),validUser.getAge());
        invalidUserDTOIn = new UserDTOIn("","ActuallyNotEmail",999);
    }

    @Test
    void getUserWithValidIdShouldReturnUser() throws Exception {
        when(userService.getUserById(validUser.getId())).thenReturn(Optional.of(validUser));
        when(userService.getDTOFromUser(validUser)).thenReturn(validUserDTOOut);

        ResponseEntity<UserDTOOut> response = userController.getUser(validUser.getId(),null,null);

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(validUserDTOOut,response.getBody());
    }

    @Test
    void getUserWithValidNameShouldReturnUser() throws Exception {
        when(userService.getUserByName(validUser.getName())).thenReturn(Optional.of(validUser));
        when(userService.getDTOFromUser(validUser)).thenReturn(validUserDTOOut);

        ResponseEntity<UserDTOOut> response = userController.getUser(null,validUser.getName(),null);

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(validUserDTOOut,response.getBody());
    }

    @Test
    void getUserWithValidEmailShouldReturnUser() throws Exception {
        when(userService.getUserByEmail(validUser.getEmail())).thenReturn(Optional.of(validUser));
        when(userService.getDTOFromUser(validUser)).thenReturn(validUserDTOOut);

        ResponseEntity<UserDTOOut> response = userController.getUser(null,null,validUser.getEmail());

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(validUserDTOOut,response.getBody());
    }

    @Test
    void getUserWithAllArgsShouldReturnUserByIdAndIgnoreOtherArgs() throws Exception {
        when(userService.getUserById(validUser.getId())).thenReturn(Optional.of(validUser));
        when(userService.getDTOFromUser(validUser)).thenReturn(validUserDTOOut);

        ResponseEntity<UserDTOOut> response = userController.getUser(validUser.getId(),
                                                                     validUser.getName(),
                                                                     validUser.getEmail());

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(validUserDTOOut,response.getBody());
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
        when(userService.getAllUsersWithPagination(anyInt(),anyInt())).thenReturn(userDTOOutList);

        ResponseEntity<List<UserDTOOut>> response = userController.getAllUsers(page,size,null);

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(userDTOOutList,response.getBody());
        verify(userService,times(1)).getAllUsersWithPagination(page,size);
    }

    @ParameterizedTest
    @ValueSource (strings = {"asc","desc"})
    void getAllUsersWithSortShouldReturnListOfUsers(String sort) throws Exception {
        when(userService.getAllUsersDefaultWithSort(sort)).thenReturn(userDTOOutList);

        ResponseEntity<List<UserDTOOut>> response = userController.getAllUsers(null,null,sort);

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(userDTOOutList,response.getBody());
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
        when(userService.createUser(validUserDTOIn)).thenReturn(validUserDTOOut);

        ResponseEntity<UserDTOOut> response = userController.createUser(validUserDTOIn,bindingResult);

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(validUserDTOOut,response.getBody());
    }

    @Test
    void createUserShouldThrowNotValidUserException() throws Exception {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);

        assertThrows(NotValidUserException.class,() -> userController.createUser(validUserDTOIn,bindingResult));
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
        when(userService.deleteUserById(id)).thenReturn(Optional.of(validUserDTOOut));

        ResponseEntity<UserDTOOut> response = userController.deleteUser(id);

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(validUserDTOOut,response.getBody());
    }

    @ParameterizedTest
    @CsvSource (value = {"1","2","3","4"})
    void updateUserShouldReturnUpdatedUser(Long id) throws Exception {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.updateUser(id,validUserDTOIn)).thenReturn(Optional.of(validUserDTOOut));

        ResponseEntity<UserDTOOut> response = userController.updateUser(id,validUserDTOIn,bindingResult);

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(validUserDTOOut,response.getBody());
    }

    @ParameterizedTest
    @CsvSource (value = {"1","2","3","4"})
    void updateUserWithInvalidUserDTOInShouldThrowNotValidException(Long id) throws Exception {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);

        assertThrows(NotValidUserException.class,() -> userController.updateUser(id,validUserDTOIn,bindingResult));
    }

    @ParameterizedTest
    @CsvSource (value = {"0",",","-300","-3"})
    void updateUserWithInvalidIdShouldThrowNotValidException(Long id) throws Exception {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        assertThrows(NotValidUserException.class,() -> userController.updateUser(id,validUserDTOIn,bindingResult));
    }

    @ParameterizedTest
    @CsvSource (value = {"1","2","3","4"})
    void updateUserWithInvalidIdAndUserDTOInShouldThrowNotValidUserException(Long id) throws Exception {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);

        assertThrows(NotValidUserException.class,() -> userController.updateUser(id,validUserDTOIn,bindingResult));
    }

    private static Stream<Arguments> invalidUserArgsMethodSource() {
        return Stream.of(Arguments.of(null,null,null),
                         Arguments.of(0L,"",null),
                         Arguments.of(Long.MIN_VALUE,null,null),
                         Arguments.of(0L,"",""),
                         Arguments.of(0L,null,""));
    }
}