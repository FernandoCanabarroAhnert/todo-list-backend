package com.test.todo_list_backend.it;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.test.todo_list_backend.models.dtos.AccountActivationRequestDTO;
import com.test.todo_list_backend.models.dtos.LoginRequestDTO;
import com.test.todo_list_backend.models.dtos.RegistrationRequestDTO;
import com.test.todo_list_backend.models.dtos.UpdatePasswordRequestDTO;
import com.test.todo_list_backend.models.dtos.UpdateUserInfosRequestDTO;
import com.test.todo_list_backend.utils.TokenUtils;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestMethodOrder(OrderAnnotation.class)
@Testcontainers
public class UserControllerIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Container
    public static MongoDBContainer mongoDBContainer;
    private static MongoClient mongoClient;
    private static MongoDatabase database;

    private String token;
    private RegistrationRequestDTO registrationRequest;
    private LoginRequestDTO loginRequest;
    private AccountActivationRequestDTO accountActivationRequest;
    private UpdateUserInfosRequestDTO updateUserInfosRequest;
    private UpdatePasswordRequestDTO updatePasswordRequest;

    static {
        mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:7.0.0")); 
        mongoDBContainer.start();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("spring.data.mongodb.database", () -> "todo-list-app");
    }

    @BeforeAll
    public static void beforeAll() {
        String uri = mongoDBContainer.getConnectionString();
        mongoClient = MongoClients.create(uri);
        database = mongoClient.getDatabase("todo-list-app");
        MongoCollection<Document> users = database.getCollection("users");

        Document user = new Document(Map.of(
            "fullName", "fullName",
            "userName", "userName",
            "email", "test@example.com",
            "password", "$2a$10$83sHueI4ObW8D5/dZXjOVOrGThMUIV8ATb1CnOBa6IKMhEAepSKXK",
            "isActive", true,
            "roles", List.of(new Document("authority", "ROLE_USER")) 
        ));
        Document toBeUpdatedUser = new Document(Map.of(
            "fullName", "fullName",
            "userName", "userName",
            "email", "user2@example.com",
            "password", "$2a$10$83sHueI4ObW8D5/dZXjOVOrGThMUIV8ATb1CnOBa6IKMhEAepSKXK",
            "isActive", true,
            "roles", List.of(new Document("authority", "ROLE_USER")) 
        ));
        users.insertMany(Arrays.asList(user, toBeUpdatedUser));

        MongoCollection<Document> activationCodes = database.getCollection("activation_codes");
        Document activationCode = new Document(Map.of(
            "user", toBeUpdatedUser,
            "code", "778371726",
            "createdAt", Instant.now(),
            "expiresAt", Instant.now().plusSeconds(3600L),
            "isValidated", false,
            "validatedAt", Instant.now().plusSeconds(3600L)
        ));
        Document expiredActivationCode = new Document(Map.of(
            "user", toBeUpdatedUser,
            "code", "12345678",
            "createdAt", Instant.now(),
            "expiresAt", Instant.now().minusSeconds(3600L),
            "isValidated", false,
            "validatedAt", Instant.now().plusSeconds(3600L)
        ));
        activationCodes.insertMany(Arrays.asList(activationCode,expiredActivationCode));
    }

    @BeforeEach
    public void setup() throws Exception {
        token = TokenUtils.obtainAccessToken(mockMvc, "test@example.com", "12345Az@123", objectMapper);
        registrationRequest = RegistrationRequestDTO.builder()
            .fullName("fullname")
            .userName("username")
            .email("fernando@gmail.com")
            .password("12345Az@")
            .build();
        loginRequest = LoginRequestDTO.builder()
            .email("email")
            .password("12345")
            .build();
        accountActivationRequest = new AccountActivationRequestDTO("778371726");
        updateUserInfosRequest = UpdateUserInfosRequestDTO.builder()
            .fullName("fullname update")
            .userName("username update")
            .email("update@example.com")
            .build();
        updatePasswordRequest = UpdatePasswordRequestDTO.builder()
            .currentPassword("12345Az@123")
            .newPassword("12345Az@")
            .build();
    }

    @AfterAll
    public static void afterAll() {
        if (mongoClient != null) {
            mongoClient.close();
        }
        mongoDBContainer.stop();
    }

    @Test
    public void registerShouldReturnStatus201WhenDataIsValid() throws Exception {
        mockMvc.perform(post("/users/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registrationRequest))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());
    }

    @Test
    public void registerShouldReturnStatus409WhenEmailAlreadyExists() throws Exception {
        registrationRequest.setEmail("test@example.com");
        mockMvc.perform(post("/users/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registrationRequest))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isConflict());
    }

    @Test
    public void registerShouldReturnStatus422WhenFullNameIsBlank() throws Exception {
        registrationRequest.setFullName("");
        mockMvc.perform(post("/users/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registrationRequest))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.fieldMessages[0].fieldName").value("fullName"))
            .andExpect(jsonPath("$.fieldMessages[0].message").value("Required field"));
    }

    @Test
    public void registerShouldReturnStatus422WhenUserNameIsBlank() throws Exception {
        registrationRequest.setUserName("");
        mockMvc.perform(post("/users/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registrationRequest))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.fieldMessages[0].fieldName").value("userName"))
            .andExpect(jsonPath("$.fieldMessages[0].message").value("Required field"));
    }

    @Test
    public void registerShouldReturnStatus422WhenEmailIsBlank() throws Exception {
        registrationRequest.setEmail("");
        mockMvc.perform(post("/users/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registrationRequest))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.fieldMessages[0].fieldName").value("email"))
            .andExpect(jsonPath("$.fieldMessages[0].message").value("Required field"));
    }

    @Test
    public void registerShouldReturnStatus422WhenEmailIsInInvalidFormat() throws Exception {
        registrationRequest.setEmail("email");
        mockMvc.perform(post("/users/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registrationRequest))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.fieldMessages[0].fieldName").value("email"))
            .andExpect(jsonPath("$.fieldMessages[0].message").value("Invalid e-mail format"));
    }

    @Test
    public void registerShouldReturnStatus422WhenPasswordHasLessThan8Characters() throws Exception {
        registrationRequest.setPassword("1234Az@");
        mockMvc.perform(post("/users/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registrationRequest))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.fieldMessages[0].fieldName").value("password"))
            .andExpect(jsonPath("$.fieldMessages[0].message").value("Password must be at least 8 characters long"));
    }

    @Test
    public void registerShouldReturnStatus422WhenPasswordDoesNotHaveOneUpperCaseLetter() throws Exception {
        registrationRequest.setPassword("12345az@");
        mockMvc.perform(post("/users/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registrationRequest))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.fieldMessages[0].fieldName").value("password"))
            .andExpect(jsonPath("$.fieldMessages[0].message").value("Password must contain at least one uppercase letter"));
    }

    @Test
    public void registerShouldReturnStatus422WhenPasswordDoesNotHaveOneLowerCaseLetter() throws Exception {
        registrationRequest.setPassword("12345AZ@");
        mockMvc.perform(post("/users/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registrationRequest))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.fieldMessages[0].fieldName").value("password"))
            .andExpect(jsonPath("$.fieldMessages[0].message").value("Password must contain at least one lowercase letter"));
    }

    @Test
    public void registerShouldReturnStatus422WhenPasswordDoesNotHaveOneNumber() throws Exception {
        registrationRequest.setPassword("abcdeFG@");
        mockMvc.perform(post("/users/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registrationRequest))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.fieldMessages[0].fieldName").value("password"))
            .andExpect(jsonPath("$.fieldMessages[0].message").value("Password must contain at least one number"));
    }

    @Test
    public void registerShouldReturnStatus422WhenPasswordDoesNotHaveOneSpecialCharacter() throws Exception {
        registrationRequest.setPassword("12345AzA");
        mockMvc.perform(post("/users/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registrationRequest))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.fieldMessages[0].fieldName").value("password"))
            .andExpect(jsonPath("$.fieldMessages[0].message").value("Password must contain at least one special character"));
    }

    @Test
    public void loginShouldReturnStatus401WhenCredentialsAreInvalid() throws Exception {
        mockMvc.perform(post("/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(4)
    public void activateAccountShouldReturnStatus200WhenCodeIsValid() throws Exception {
        mockMvc.perform(post("/users/activate")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(accountActivationRequest))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    public void activateAccountShouldReturnStatus400WhenCodeIsExpired() throws Exception {
        accountActivationRequest.setCode("12345678");
        mockMvc.perform(post("/users/activate")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(accountActivationRequest))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void activateAccountShouldReturnStatus404WhenCodeDoesNotExist() throws Exception {
        accountActivationRequest.setCode("123");
        mockMvc.perform(post("/users/activate")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(accountActivationRequest))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    public void activateAccountShouldReturnStatus422WhenCodeIsNull() throws Exception {
        accountActivationRequest.setCode(null);
        mockMvc.perform(post("/users/activate")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(accountActivationRequest))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.fieldMessages[0].fieldName").value("code"))
            .andExpect(jsonPath("$.fieldMessages[0].message").value("Required field"));
    }

    @Test
    public void activateAccountShouldReturnStatus422WhenCodeIsBlank() throws Exception {
        accountActivationRequest.setCode("");
        mockMvc.perform(post("/users/activate")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(accountActivationRequest))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.fieldMessages[0].fieldName").value("code"))
            .andExpect(jsonPath("$.fieldMessages[0].message").value("Required field"));
    }

    @Test
    public void verifyTokenShouldReturnStatus200WhenTokenIsValid() throws Exception {
        mockMvc.perform(get("/users/verify-token")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    public void verifyTokenShouldReturnStatus401WhenAuthTokenIsNotProvided() throws Exception {
        mockMvc.perform(get("/users/verify-token")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void verifyTokenShouldReturnStatus403WhenAuthTokenIsInvalid() throws Exception {
        mockMvc.perform(get("/users/verify-token")
            .header("Authorization", "Bearer token")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void verifyExistingEmailShouldReturnStatus200WhenEmailExists() throws Exception {
        mockMvc.perform(get("/users/verify-email?email=test@example.com")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.isAlreadyInUse").value(true));
    }

    @Test
    public void verifyExistingEmailShouldReturnStatus200WhenEmailDoesNotExist() throws Exception {
        mockMvc.perform(get("/users/verify-email?email=" + "email@example.com")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.isAlreadyInUse").value(false));
    }

    @Test
    @Order(2)
    public void updateUserInfosShouldReturnStatus200WhenDataIsValid() throws Exception {
        token = TokenUtils.obtainAccessToken(mockMvc, "user2@example.com", "12345Az@", objectMapper);
        mockMvc.perform(put("/users/update-infos")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateUserInfosRequest))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    @Order(3)
    public void updateUserInfosShouldReturnStatus409WhenEmailAlreadyExists() throws Exception {
        token = TokenUtils.obtainAccessToken(mockMvc, "update@example.com", "12345Az@", objectMapper);
        updateUserInfosRequest.setEmail("test@example.com");
        mockMvc.perform(put("/users/update-infos")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateUserInfosRequest))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isConflict());
    }

    @Test
    public void updateUserInfosShouldReturnStatus422WhenFullNameIsBlank() throws Exception {
        updateUserInfosRequest.setFullName("");
        mockMvc.perform(put("/users/update-infos")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateUserInfosRequest))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.fieldMessages[0].fieldName").value("fullName"))
            .andExpect(jsonPath("$.fieldMessages[0].message").value("Required field"));
    }

    @Test
    public void updateUserInfosShouldReturnStatus422WhenUserNameIsBlank() throws Exception {
        updateUserInfosRequest.setUserName("");
        mockMvc.perform(put("/users/update-infos")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateUserInfosRequest))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.fieldMessages[0].fieldName").value("userName"))
            .andExpect(jsonPath("$.fieldMessages[0].message").value("Required field"));
    }

    @Test
    public void updateUserInfosShouldReturnStatus422WhenEmailIsInBlank() throws Exception {
        updateUserInfosRequest.setEmail("");
        mockMvc.perform(put("/users/update-infos")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateUserInfosRequest))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.fieldMessages[0].fieldName").value("email"))
            .andExpect(jsonPath("$.fieldMessages[0].message").value("Required field"));
    }

    @Test
    public void updateUserInfosShouldReturnStatus422WhenEmailIsInInvalidFormat() throws Exception {
        updateUserInfosRequest.setEmail("email");
        mockMvc.perform(put("/users/update-infos")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateUserInfosRequest))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.fieldMessages[0].fieldName").value("email"))
            .andExpect(jsonPath("$.fieldMessages[0].message").value("Invalid e-mail format"));
    }

    @Test
    @Order(1)
    public void updatePasswordShouldReturnStatus200WhenDataIsValid() throws Exception {
        token = TokenUtils.obtainAccessToken(mockMvc, "user2@example.com", "12345Az@123", objectMapper);
        mockMvc.perform(put("/users/update-password")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updatePasswordRequest))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    public void updatePasswordShouldReturnStatus409WhenCurrentPasswordIsIncorrect() throws Exception {
        updatePasswordRequest.setCurrentPassword("12345678");
        mockMvc.perform(put("/users/update-password")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updatePasswordRequest))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isConflict());
    }

    @Test
    public void updatePasswordShouldReturnStatus422WhenCurrentPasswordIsBlank() throws Exception {
        updatePasswordRequest.setCurrentPassword("");
        mockMvc.perform(put("/users/update-password")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updatePasswordRequest))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.fieldMessages[0].fieldName").value("currentPassword"))
            .andExpect(jsonPath("$.fieldMessages[0].message").value("Required field"));
    }

    @Test
    public void updatePasswordShouldReturnStatus422WhenNewPasswordDoesNotHaveOneUpperCaseLetter() throws Exception {
        updatePasswordRequest.setNewPassword("12345az@");
        mockMvc.perform(put("/users/update-password")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updatePasswordRequest))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.fieldMessages[0].fieldName").value("newPassword"))
            .andExpect(jsonPath("$.fieldMessages[0].message").value("Password must contain at least one uppercase letter"));
    }

    @Test
    public void updatePasswordShouldReturnStatus422WhenNewPasswordDoesNotHaveOneLowerCaseLetter() throws Exception {
        updatePasswordRequest.setNewPassword("12345AZ@");
        mockMvc.perform(put("/users/update-password")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updatePasswordRequest))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.fieldMessages[0].fieldName").value("newPassword"))
            .andExpect(jsonPath("$.fieldMessages[0].message").value("Password must contain at least one lowercase letter"));
    }

    @Test
    public void updatePasswordShouldReturnStatus422WhenNewPasswordDoesNotHaveOneNumber() throws Exception {
        updatePasswordRequest.setNewPassword("abcdeAz@");
        mockMvc.perform(put("/users/update-password")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updatePasswordRequest))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.fieldMessages[0].fieldName").value("newPassword"))
            .andExpect(jsonPath("$.fieldMessages[0].message").value("Password must contain at least one number"));
    }

    @Test
    public void updatePasswordShouldReturnStatus422WhenNewPasswordDoesNotHaveOneSpecialCharacter() throws Exception {
        updatePasswordRequest.setNewPassword("12345AzA");
        mockMvc.perform(put("/users/update-password")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updatePasswordRequest))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.fieldMessages[0].fieldName").value("newPassword"))
            .andExpect(jsonPath("$.fieldMessages[0].message").value("Password must contain at least one special character"));
    }

}
