package ru.aston.UserServiceAPI.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.aston.UserServiceAPI.Utils.ErrorMessages;
import ru.aston.UserServiceAPI.Utils.UserDTOValidator;
import ru.aston.UserServiceAPI.dtos.UserDTOIn;
import ru.aston.UserServiceAPI.dtos.UserDTOOut;
import ru.aston.UserServiceAPI.entitys.User;
import ru.aston.UserServiceAPI.repos.UserRepository;
import ru.aston.UserServiceAPI.services.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:18")
            .withDatabaseName("UserServiceAPI")
            .withUsername("postgres")
            .withPassword("postgres");

    static List<UserDTOIn> users = new ArrayList<>();
    static boolean isInitialized = false;

    @Autowired
    private UserRepository userRepository;
    @MockitoSpyBean
    private UserService userService;
    @Autowired
    private UserDTOValidator validator;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    UserDTOIn validUserDTOIn;
    User validUser;
    UserDTOOut validUserDTOOut;
    UserDTOIn invalidUserDTOIn;
    String userNotFoundMessage = "Sorry, but user with current parameters was not found";

    @BeforeAll
    static void beforeAll() {
        postgreSQLContainer.start();
        postgreSQLContainer.withDatabaseName("UserServiceAPI");
        for (int i = 0;i < 30;i++) {
            users.add(new UserDTOIn("testname" + i,"testemail" + i + "@gmail.com",30));
        }
    }

    @AfterAll
    static void afterAll() {
        postgreSQLContainer.stop();
    }

    public void init() {
        userService.createUser(validUserDTOIn);
        for (UserDTOIn userDTOIn : users) {
            userService.createUser(userDTOIn);
        }
        isInitialized = true;
    }

    @BeforeEach
    void setUp() {
        validUserDTOIn = new UserDTOIn("SomeName","someemail@gmail.com",30);
        validUser = new User(validUserDTOIn.getName(),validUserDTOIn.getEmail(),validUserDTOIn.getAge());
        validUser.setId(1L);
        validUserDTOOut = new UserDTOOut(validUser.getName(),validUser.getEmail(),validUser.getAge());
        validUserDTOOut.setId(1L);
        invalidUserDTOIn = new UserDTOIn("","ActuallyNotEmail",999);
        if (! isInitialized) init();
    }

    @ParameterizedTest
    @MethodSource ("getExistingIds")
    void getUserByIdShouldReturnValidUser(long existingId) throws Exception {
        var response = mockMvc.perform(get("/user")
                        .param("id",String.valueOf(existingId))
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        UserDTOOut actualUser = objectMapper.readValue(response.getResponse().getContentAsString(),UserDTOOut.class);

        assertEquals(200,response.getResponse().getStatus());
        assertEquals(actualUser.getId(),existingId);
    }

    @ParameterizedTest
    @ValueSource (longs = {0,- 1,- 10,- 11,- 20,Long.MIN_VALUE,Long.MAX_VALUE})
    void getUserShouldReturnErrorMessage(long notExistingId) throws Exception {
        var response = mockMvc.perform(get("/user")
                        .param("id",String.valueOf(notExistingId))
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();

        assertEquals(400,response.getResponse().getStatus());
        assertTrue(response.getResponse().getContentAsString().contains(userNotFoundMessage));
    }

    @ParameterizedTest
    @CsvSource (value = {"0,12","1,9","2,8","3,5","4,1"})
    void getAllUsersWithPageAndSizeShouldReturnListOfUsers(String page,String size) throws Exception {
        var response = mockMvc.perform(get("/user/all")
                        .param("page",page)
                        .param("size",size)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();

        List<UserDTOOut> expectedUsers = (List<UserDTOOut>) objectMapper.readValue(response.getResponse().getContentAsString(),List.class);

        assertEquals(200,response.getResponse().getStatus());
        assertEquals(expectedUsers.size(),Integer.parseInt(size));
        verify(userService,times(1)).getAllUsersWithPagination(Integer.parseInt(page),Integer.parseInt(size));
    }

    @ParameterizedTest
    @CsvSource (value = {"0,12,asc","1,9,asc","2,8,desc","3,5,desc","4,1,asc"})
    void getAllUsersWithPageAndSizeAndSortShouldReturnListOfUsers(String page,String size,String sort) throws Exception {
        var response = mockMvc.perform(get("/user/all")
                        .param("page",page)
                        .param("size",size)
                        .param("sort",sort)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();

        List<UserDTOOut> expectedUsers = (List<UserDTOOut>) objectMapper.readValue(response.getResponse().getContentAsString(),List.class);

        assertEquals(200,response.getResponse().getStatus());
        assertEquals(expectedUsers.size(),Integer.parseInt(size));
        verify(userService,times(1)).getAllUsersWithPaginationAndSort(Integer.parseInt(page),Integer.parseInt(size),sort);
    }

    @ParameterizedTest
    @CsvSource (value = {",,","-1,-9,bla","1,-999,desc1","0,0,","4,,1asc","0,0,"})
    void getAllUsersWithInvalidArgsShouldReturnListOfUsers(String page,String size,String sort) throws Exception {
        var response = mockMvc.perform(get("/user/all")
                        .param("page",page)
                        .param("size",size)
                        .param("sort",sort)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();

        assertEquals(200,response.getResponse().getStatus());
        verify(userService,times(1)).getAllUsersDefault();
    }

    @ParameterizedTest
    @MethodSource ("getValidUsers")
    void createUserShouldReturnCreatedUser(UserDTOIn validUserDTOIn) throws Exception {
        var response = mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserDTOIn))
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        UserDTOOut actualUser = objectMapper.readValue(response.getResponse().getContentAsString(),UserDTOOut.class);

        assertEquals(200,response.getResponse().getStatus());
        assertEquals(validUserDTOIn.getName(),actualUser.getName());
        assertEquals(validUserDTOIn.getEmail(),actualUser.getEmail());
        assertEquals(validUserDTOIn.getAge(),actualUser.getAge());
    }

    @ParameterizedTest
    @ValueSource (longs = {1,2,3,4,5,6,7,8,9,10})
    void deleteUserShouldReturnDeletedUser(Long existingId) throws Exception {
        var response = mockMvc.perform(delete("/user")
                        .param("id",String.valueOf(existingId))
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        UserDTOOut deletedUser = objectMapper.readValue(response.getResponse().getContentAsString(),UserDTOOut.class);

        assertEquals(200,response.getResponse().getStatus());
        assertEquals(deletedUser.getId(),existingId);
        verify(userService,times(1)).deleteUserById(existingId);
    }

    @ParameterizedTest
    @ValueSource (longs = {- 1,- 2,- 3,- 4,- 5,- 6,- 7,- 8,- 9,- 10,- 50,Long.MIN_VALUE,Long.MAX_VALUE})
    void deleteUserShouldReturnReturnErrorMessage(Long existingId) throws Exception {
        var response = mockMvc.perform(delete("/user")
                        .param("id",String.valueOf(existingId))
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        String error = response.getResponse().getContentAsString();

        assertEquals(400,response.getResponse().getStatus());
        assertTrue(error.contains(ErrorMessages.UserNotFound.getMessage()));
    }

    @ParameterizedTest
    @NullSource
    void deleteUserShouldReturnReturnBadRequestMessage(String id) throws Exception {
        var response = mockMvc.perform(delete("/user")
                        .param("id",id)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        String error = response.getResponse().getContentAsString();

        assertEquals(400,response.getResponse().getStatus());
        assertTrue(error.contains(ErrorMessages.BadRequest.getMessage()));
    }

    @ParameterizedTest
    @MethodSource ("getUpdatedUsers")
    void updateUser(String id, UserDTOIn userDTOIn) throws Exception {
        var response = mockMvc.perform(put("/user")
                        .param("id",id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTOIn))
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        UserDTOOut updatedUser = objectMapper.readValue(response.getResponse().getContentAsString(),UserDTOOut.class);

        assertEquals(200,response.getResponse().getStatus());
        assertEquals(userDTOIn.getName(),updatedUser.getName());
        assertEquals(userDTOIn.getEmail(),updatedUser.getEmail());
        assertEquals(userDTOIn.getAge(),updatedUser.getAge());
    }

    private static Stream<Arguments> getExistingIds() {
        return IntStream.rangeClosed(1,30)
                .mapToObj(i -> Arguments.of((long) i));
    }

    private static Stream<Arguments> getValidUsers() {
        return IntStream.rangeClosed(1,10)
                .mapToObj(i -> Arguments.of(new UserDTOIn("ValidUser" + i,"ValidUser" + i + "@gmail.com",70)));
    }

    private static Stream<Arguments> getUpdatedUsers() {
        return IntStream.rangeClosed(1,30)
                .mapToObj(i -> Arguments.of(String.valueOf(i),new UserDTOIn("UpdatedUser" + i,"UpdatedUser" + i + "@gmail.com",70)));
    }
}