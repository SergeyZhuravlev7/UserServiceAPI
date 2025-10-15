package ru.aston.UserServiceAPI.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.Commit;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import ru.aston.UserServiceAPI.Utils.ErrorMessages;
import ru.aston.UserServiceAPI.Utils.UserDTOValidator;
import ru.aston.UserServiceAPI.dtos.UserDTOIn;
import ru.aston.UserServiceAPI.dtos.UserDTOOut;
import ru.aston.UserServiceAPI.kafka.Sendable;
import ru.aston.UserServiceAPI.repos.UserRepository;
import ru.aston.UserServiceAPI.kafka.ProducerService;
import ru.aston.UserServiceAPI.services.UserService;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@DisplayNameGeneration (DisplayNameGenerator.Simple.class)
@Transactional
@TestInstance (TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext (classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles ("test")
public class UserControllerIntegrationTest {

    @Container
    public static KafkaContainer kafkaContainer = new KafkaContainer("apache/kafka:4.1.0");
    @Container
    @ServiceConnection
    static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:18")
            .withDatabaseName("UserServiceAPI")
            .withUsername("postgres")
            .withPassword("postgres")
            ;

    static {
        kafkaContainer.start();
    }

    String USER_NOT_FOUND_ERROR = ErrorMessages.USER_NOT_FOUND.getMessage();
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
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @MockitoSpyBean
    private ProducerService producerService;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private Consumer<String, String> consumer;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("notifications.boot_strap_server",kafkaContainer::getBootstrapServers);
    }

    @BeforeAll
    @Commit
    @Transactional
    void beforeAll() {
        for (int i = 0;i < 30;i++) {
            userService.createUser(new UserDTOIn("testname" + i,"testemail" + i + "@gmail.com",30));
        }
    }

    @ParameterizedTest
    @MethodSource ("getExistingIds")
    @DisplayName ("getUserByIdShouldReturnValidUser")
    void getUserByIdShouldReturnValidUser(long existingId) throws Exception {
        var response = mockMvc
                .perform(get("/user")
                        .param("id",String.valueOf(existingId))
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        UserDTOOut actualUser = objectMapper.readValue(response
                .getResponse()
                .getContentAsString(),UserDTOOut.class);

        assertEquals(200,response
                .getResponse()
                .getStatus());
        assertEquals(actualUser.getId(),existingId);
    }

    @ParameterizedTest
    @ValueSource (longs = {0,- 1,- 10,- 11,- 20,Long.MIN_VALUE,Long.MAX_VALUE})
    void getUserShouldReturnErrorMessage(long notExistingId) throws Exception {
        var response = mockMvc
                .perform(get("/user")
                        .param("id",String.valueOf(notExistingId))
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();

        assertEquals(400,response
                .getResponse()
                .getStatus());
        assertTrue(response
                .getResponse()
                .getContentAsString()
                .contains(USER_NOT_FOUND_ERROR));
    }

    @ParameterizedTest
    @CsvSource (value = {"0,12","1,9","2,8","3,5","4,1"})
    void getAllUsersWithPageAndSizeShouldReturnListOfUsers(String page,String size) throws Exception {
        var response = mockMvc
                .perform(get("/user/all")
                        .param("page",page)
                        .param("size",size)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();

        List<UserDTOOut> expectedUsers = objectMapper.readValue(response
                .getResponse()
                .getContentAsString(),new TypeReference<>() {});

        assertEquals(200,response
                .getResponse()
                .getStatus());
        assertEquals(Integer.parseInt(size),expectedUsers.size());
        verify(userService,times(1)).getAllUsersWithPagination(Integer.parseInt(page),Integer.parseInt(size));
    }

    @ParameterizedTest
    @CsvSource (value = {"0,12,asc","1,9,asc","2,8,desc","3,5,desc","4,1,asc"})
    void getAllUsersWithPageAndSizeAndSortShouldReturnListOfUsers(String page,String size,String sort) throws Exception {
        var response = mockMvc
                .perform(get("/user/all")
                        .param("page",page)
                        .param("size",size)
                        .param("sort",sort)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();

        List<UserDTOOut> expectedUsers = objectMapper.readValue(response
                .getResponse()
                .getContentAsString(),new TypeReference<>() {});
        assertEquals(200,response
                .getResponse()
                .getStatus());
        assertEquals(Integer.parseInt(size),expectedUsers.size());
        verify(userService,times(1)).getAllUsersWithPaginationAndSort(Integer.parseInt(page),Integer.parseInt(size),sort);
    }

    @ParameterizedTest
    @CsvSource (value = {",,","-1,-9,bla","1,-999,desc1","0,0,","4,,1asc","0,0,"})
    void getAllUsersWithInvalidArgsShouldReturnListOfUsers(String page,String size,String sort) throws Exception {
        var response = mockMvc
                .perform(get("/user/all")
                        .param("page",page)
                        .param("size",size)
                        .param("sort",sort)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();

        assertEquals(200,response
                .getResponse()
                .getStatus());
        verify(userService,times(1)).getAllUsersDefault();
    }

    @ParameterizedTest
    @MethodSource ("getValidUsers")
    void createUserShouldReturnCreatedUser(UserDTOIn validUserDTOIn) throws Exception {
        var response = mockMvc
                .perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserDTOIn))
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        UserDTOOut actualUser = objectMapper.readValue(response
                .getResponse()
                .getContentAsString(),UserDTOOut.class);
        ConsumerRecords<String, String> records = consumer.poll(Duration.of(1,ChronoUnit.SECONDS));

        assertEquals(200,response
                .getResponse()
                .getStatus());
        assertEquals(validUserDTOIn.getName(),actualUser.getName());
        assertEquals(validUserDTOIn.getEmail(),actualUser.getEmail());
        assertEquals(validUserDTOIn.getAge(),actualUser.getAge());
        assertNotNull(records);
        verify(producerService, times(1)).send(any(Sendable.class));
        for (ConsumerRecord<String, String> record : records) {
            assertEquals(validUserDTOIn.getEmail(),record.key());
            assertEquals("created",record.value());
        }
    }

    @ParameterizedTest
    @ValueSource (longs = {1,2,3,4,5,6,7,8,9,10})
    void deleteUserShouldReturnDeletedUser(Long existingId) throws Exception {
        var response = mockMvc
                .perform(delete("/user")
                        .param("id",String.valueOf(existingId))
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        UserDTOOut deletedUser = objectMapper.readValue(response
                .getResponse()
                .getContentAsString(),UserDTOOut.class);
        ConsumerRecords<String, String> records = consumer.poll(Duration.of(1,ChronoUnit.SECONDS));

        assertEquals(200,response
                .getResponse()
                .getStatus());
        assertEquals(deletedUser.getId(),existingId);
        verify(userService,times(1)).deleteUserById(existingId);
        verify(producerService,times(1)).send(any(Sendable.class));
        for (ConsumerRecord<String, String> record : records) {
            assertEquals(deletedUser.getEmail(),record.key());
            assertEquals("deleted",record.value());
        }
    }

    @ParameterizedTest
    @ValueSource (longs = {- 1,- 2,- 3,- 4,- 5,- 6,- 7,- 8,- 9,- 10,- 50,Long.MIN_VALUE,Long.MAX_VALUE})
    void deleteUserShouldReturnReturnErrorMessage(Long existingId) throws Exception {
        var response = mockMvc
                .perform(delete("/user")
                        .param("id",String.valueOf(existingId))
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        String error = response
                .getResponse()
                .getContentAsString();

        assertEquals(400,response
                .getResponse()
                .getStatus());
        assertTrue(error.contains(ErrorMessages.USER_NOT_FOUND.getMessage()));
    }

    @ParameterizedTest
    @NullSource
    void deleteUserShouldReturnReturnBadRequestMessage(String id) throws Exception {
        var response = mockMvc
                .perform(delete("/user")
                        .param("id",id)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        String error = response
                .getResponse()
                .getContentAsString();

        assertEquals(400,response
                .getResponse()
                .getStatus());
        assertTrue(error.contains(ErrorMessages.BAD_REQUEST.getMessage()));
    }

    @ParameterizedTest
    @MethodSource ("getUpdatedUsers")
    void updateUser(String id,UserDTOIn userDTOIn) throws Exception {
        var response = mockMvc
                .perform(put("/user")
                        .param("id",id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTOIn))
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        UserDTOOut updatedUser = objectMapper.readValue(response
                .getResponse()
                .getContentAsString(),UserDTOOut.class);

        assertEquals(200,response
                .getResponse()
                .getStatus());
        assertEquals(userDTOIn.getName(),updatedUser.getName());
        assertEquals(userDTOIn.getEmail(),updatedUser.getEmail());
        assertEquals(userDTOIn.getAge(),updatedUser.getAge());
    }

    @ParameterizedTest
    @MethodSource ("getUsersWithAgeSmallerThan18")
    void createUserWithAgeSmallerThan18ShouldReturnErrorMessage(UserDTOIn userDTOIn) throws Exception {
        var response = mockMvc
                .perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTOIn)))
                .andReturn();

        String error = response
                .getResponse()
                .getContentAsString();
        assertEquals(400,response
                .getResponse()
                .getStatus());
        assertTrue(error.contains("Age"));
        assertFalse(error.contains("Email"));
        assertFalse(error.contains("Name"));
    }

    @ParameterizedTest
    @MethodSource ("getUsersWithAgeGreaterThan99")
    void createUserWithAgeGreaterThan99ShouldReturnErrorMessage(UserDTOIn userDTOIn) throws Exception {
        var response = mockMvc
                .perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTOIn)))
                .andReturn();

        String error = response
                .getResponse()
                .getContentAsString();
        assertEquals(400,response
                .getResponse()
                .getStatus());
        assertTrue(error.contains("Age"));
        assertFalse(error.contains("Email"));
        assertFalse(error.contains("Name"));
    }

    @ParameterizedTest
    @MethodSource ("getUsersWithInvalidName")
    void createUserWithInvalidNameShouldReturnErrorMessage(UserDTOIn userDTOIn) throws Exception {
        var response = mockMvc
                .perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTOIn)))
                .andReturn();

        String error = response
                .getResponse()
                .getContentAsString();
        assertEquals(400,response
                .getResponse()
                .getStatus());
        assertTrue(error.contains("Name"));
        assertFalse(error.contains("Email"));
        assertFalse(error.contains("Age"));
    }

    @ParameterizedTest
    @MethodSource ("getUsersWithInvalidEmail")
    void createUserWithInvalidEmailShouldReturnErrorMessage(UserDTOIn userDTOIn) throws Exception {
        var response = mockMvc
                .perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTOIn)))
                .andReturn();

        String error = response
                .getResponse()
                .getContentAsString();
        assertEquals(400,response
                .getResponse()
                .getStatus());
        assertTrue(error.contains("Email"));
        assertFalse(error.contains("Name"));
        assertFalse(error.contains("Age"));
    }

    private static Stream<Arguments> getExistingIds() {
        return IntStream
                .rangeClosed(1,30)
                .mapToObj(i -> Arguments.of((long) i));
    }

    private static Stream<Arguments> getValidUsers() {
        return IntStream
                .rangeClosed(1,10)
                .mapToObj(i -> Arguments.of(new UserDTOIn("Validuser","ValidUser" + i + "@gmail.com",70)));
    }

    private static Stream<Arguments> getUpdatedUsers() {
        return IntStream
                .rangeClosed(1,30)
                .mapToObj(i -> Arguments.of(String.valueOf(i),new UserDTOIn("Updateduser","UpdatedUser" + i + "@gmail.com",70)));
    }

    private static Stream<Arguments> getUsersWithAgeSmallerThan18() {
        return IntStream
                .of(1,8,9,12,14,17)
                .mapToObj(i -> Arguments.of(new UserDTOIn("Validname","validemail@gmail.com",i)));
    }

    private static Stream<Arguments> getUsersWithAgeGreaterThan99() {
        return IntStream
                .of(100,115,500,485,999)
                .mapToObj(i -> Arguments.of(new UserDTOIn("Validname","validemail@gmail.com",i)));
    }

    private static Stream<Arguments> getUsersWithInvalidName() {
        return Stream
                .of("",null,"nAME","Na","Namenamenamenamename")
                .map(str -> Arguments.of(new UserDTOIn(str,"validemail@gmail.com",30)));
    }

    private static Stream<Arguments> getUsersWithInvalidEmail() {
        return Stream
                .of("",null,"blablabla","actuallyNotEmail","blabla@gmail.com@gmail.com")
                .map(str -> Arguments.of(new UserDTOIn("Validname",str,30)));
    }
}