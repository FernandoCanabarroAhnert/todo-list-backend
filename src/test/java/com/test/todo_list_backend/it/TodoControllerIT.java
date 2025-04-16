package com.test.todo_list_backend.it;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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
import com.mongodb.client.result.InsertOneResult;
import com.test.todo_list_backend.models.dtos.TodoRequestDTO;
import com.test.todo_list_backend.models.dtos.TodoUpdateRequestDTO;
import com.test.todo_list_backend.utils.TokenUtils;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestMethodOrder(OrderAnnotation.class)
@Testcontainers
public class TodoControllerIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Container
    public static MongoDBContainer mongoDBContainer;
    private static MongoClient mongoClient;
    private static MongoDatabase database;

    private String token;
    private static String todoId;
    private TodoRequestDTO todoRequest;
    private TodoUpdateRequestDTO updateTodoRequest;
    private MockMultipartFile requestJsonPart;
    private MockMultipartFile updateRequestJsonPart;
    private MockMultipartFile imagePart;

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
        users.insertOne(user);

        MongoCollection<Document> todos = database.getCollection("todos");
        Document notCompletedTodo = new Document(Map.of(
            "title", "title",
            "description", "description",
            "priority", "EXTREME",
            "status", "NOT_STARTED",
            "image", "image",
            "createdAt", Instant.parse("2025-04-07T00:48:43.566Z"),
            "expiresAt", Instant.parse("2025-04-07T00:48:00.000Z"),
            "user", user
        ));
        Document completedTodo = new Document(Map.of(
            "title", "title",
            "description", "description",
            "priority", "EXTREME",
            "status", "COMPLETED",
            "image", "image",
            "createdAt", Instant.parse("2025-04-07T00:48:43.566Z"),
            "expiresAt", Instant.parse("2025-04-07T00:48:00.000Z"),
            "user", user
        ));
        InsertOneResult result = todos.insertOne(notCompletedTodo);
        todoId = result.getInsertedId().asObjectId().getValue().toHexString();
        todos.insertOne(completedTodo);
    }

    @BeforeEach
    public void setup() throws Exception {
        token = TokenUtils.obtainAccessToken(mockMvc, "test@example.com" , "12345Az@123", objectMapper);

        todoRequest = TodoRequestDTO.builder()
            .title("title")
            .description("description")
            .priority(3)
            .expiresAt(LocalDateTime.now().plusDays(1))
            .build();

        updateTodoRequest = new TodoUpdateRequestDTO();
        updateTodoRequest.setTitle("title");
        updateTodoRequest.setDescription("description");
        updateTodoRequest.setPriority(3);
        updateTodoRequest.setStatus(1);
        updateTodoRequest.setExpiresAt(LocalDateTime.now().plusDays(1));

        String jsonRequest = objectMapper.writeValueAsString(todoRequest);
        requestJsonPart = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                jsonRequest.getBytes());

        String updateJsonRequest = objectMapper.writeValueAsString(updateTodoRequest);
        updateRequestJsonPart = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                updateJsonRequest.getBytes());

        imagePart = new MockMultipartFile(
                "image",
                "imagem.png",
                MediaType.IMAGE_PNG_VALUE,
                "image".getBytes());
    }

    @AfterAll
    public static void afterAll() {
        if (mongoClient != null) {
            mongoClient.close();
        }
        mongoDBContainer.stop();
    }
    
    @Test
    @Order(1)
    public void findAllNotCompletedTodosFromConnectedUserShouldReturnPageOfTodosAndStatus200() throws Exception {
        mockMvc.perform(get("/todos")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].title").value("title"));
    }

    @Test
    public void findAllNotCompletedTodosFromConnectedUserShouldReturnStatus401WhenAuthTokenIsNotProvided() throws Exception {
        mockMvc.perform(get("/todos")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(2)
    public void findAllCompletedTodosFromConnectedUserShouldReturnPageOfTodosAndStatus200() throws Exception {
        mockMvc.perform(get("/todos/completed")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].title").value("title"));
    }

    @Test
    public void findAllCompletedTodosFromConnectedUserShouldReturnStatus401WhenAuthTokenIsNotProvided() throws Exception {
        mockMvc.perform(get("/todos/completed")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(3)
    public void findByIdShouldReturnTodoAndStatus200WhenIdExists() throws Exception {
        mockMvc.perform(get("/todos/" + todoId)
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("title"));  
    }

    @Test
    public void findByIdShouldReturnStatus401WhenAuthTokenIsNotProvided() throws Exception {
        mockMvc.perform(get("/todos/" + todoId)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(4)
    public void findByIdShouldReturnStatus404WhenIdDoesNotExist() throws Exception {
        mockMvc.perform(get("/todos/" + "huhquwhehqthuwhtu")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @Order(5)
    public void getTodosSummaryFromConnectedUserShouldReturnSummaryAndStatus200() throws Exception {
        mockMvc.perform(get("/todos/summary")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.completedTodosPercentage").value(0.50))
            .andExpect(jsonPath("$.inProgressTodosPercentage").value(0.00))  
            .andExpect(jsonPath("$.notStartedTodosPercentage").value(0.50));  
    }

    @Test
    public void getTodosSummaryFromConnectedUserShouldReturnStatus401WhenAuthTokenIsNotProvided() throws Exception {
        mockMvc.perform(get("/todos/summary")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(6)
    public void createTodoShouldReturnStatus201WhenDataIsValid() throws Exception {
        mockMvc.perform(multipart("/todos")
            .file(requestJsonPart)
            .file(imagePart)
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());
    }

    @Test
    public void createTodoShouldReturnStatus401WhenAuthTokenIsNotProvided() throws Exception {
        mockMvc.perform(multipart("/todos")
            .file(requestJsonPart)
            .file(imagePart)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void createTodoShouldReturnStatus422WhenTitleIsBlank() throws Exception {
        todoRequest.setTitle("");
        String jsonRequest = objectMapper.writeValueAsString(todoRequest);
        requestJsonPart = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                jsonRequest.getBytes());
        mockMvc.perform(multipart("/todos")
            .file(requestJsonPart)
            .file(imagePart)
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.fieldMessages[0].fieldName").value("title"))
            .andExpect(jsonPath("$.fieldMessages[0].message").value("Required field"));
    }

    @Test
    public void createTodoShouldReturnStatus422WhenDescriptionIsBlank() throws Exception {
        todoRequest.setDescription("");
        String jsonRequest = objectMapper.writeValueAsString(todoRequest);
        requestJsonPart = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                jsonRequest.getBytes());
        mockMvc.perform(multipart("/todos")
            .file(requestJsonPart)
            .file(imagePart)
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.fieldMessages[0].fieldName").value("description"))
            .andExpect(jsonPath("$.fieldMessages[0].message").value("Required field"));
    }

    @Test
    public void createTodoShouldReturnStatus422WhenPriorityIsNull() throws Exception {
        todoRequest.setPriority(null);
        String jsonRequest = objectMapper.writeValueAsString(todoRequest);
        requestJsonPart = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                jsonRequest.getBytes());
        mockMvc.perform(multipart("/todos")
            .file(requestJsonPart)
            .file(imagePart)
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.fieldMessages[0].fieldName").value("priority"))
            .andExpect(jsonPath("$.fieldMessages[0].message").value("Required field"));
    }

    @Test
    public void createTodoShouldReturnStatus422WhenExpiresAtIsNull() throws Exception {
        todoRequest.setExpiresAt(null);
        String jsonRequest = objectMapper.writeValueAsString(todoRequest);
        requestJsonPart = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                jsonRequest.getBytes());
        mockMvc.perform(multipart("/todos")
            .file(requestJsonPart)
            .file(imagePart)
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.fieldMessages[0].fieldName").value("expiresAt"))
            .andExpect(jsonPath("$.fieldMessages[0].message").value("Required field"));
    }

    @Test
    @Order(7)
    public void updateTodoShouldReturnStatus200WhenDataIsValidAndIdExists() throws Exception {
        mockMvc.perform(multipart("/todos/" + todoId)
            .file(updateRequestJsonPart)
            .file(imagePart)
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .with(request -> {
                request.setMethod("PUT");
                return request;
            }))
            .andExpect(status().isOk());
    }

    @Test
    public void updateTodoShouldReturnStatus401WhenAuthTokenIsNotProvided() throws Exception {
        mockMvc.perform(multipart("/todos/" + todoId)
            .file(updateRequestJsonPart)
            .file(imagePart)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .with(request -> {
                request.setMethod("PUT");
                return request;
            }))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void updateTodoShouldReturnStatus404WhenIdDoesNotExist() throws Exception {
        mockMvc.perform(multipart("/todos/" + "heuheuheuhuehrh")
            .file(updateRequestJsonPart)
            .file(imagePart)
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .with(request -> {
                request.setMethod("PUT");
                return request;
            }))
            .andExpect(status().isNotFound());
    }

    @Test
    public void updateTodoShouldReturnStatus422WhenTitleIsBlank() throws Exception {
        updateTodoRequest.setTitle("");
        String updateJsonRequest = objectMapper.writeValueAsString(updateTodoRequest);
        updateRequestJsonPart = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                updateJsonRequest.getBytes());
        mockMvc.perform(multipart("/todos/" + todoId)
            .file(updateRequestJsonPart)
            .file(imagePart)
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .with(request -> {
                request.setMethod("PUT");
                return request;
            }))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.fieldMessages[0].fieldName").value("title"))
            .andExpect(jsonPath("$.fieldMessages[0].message").value("Required field"));
    }

    @Test
    public void updateTodoShouldReturnStatus422WhenDescriptionIsBlank() throws Exception {
        updateTodoRequest.setDescription("");
        String updateJsonRequest = objectMapper.writeValueAsString(updateTodoRequest);
        updateRequestJsonPart = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                updateJsonRequest.getBytes());
        mockMvc.perform(multipart("/todos/" + todoId)
            .file(updateRequestJsonPart)
            .file(imagePart)
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .with(request -> {
                request.setMethod("PUT");
                return request;
            }))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.fieldMessages[0].fieldName").value("description"))
            .andExpect(jsonPath("$.fieldMessages[0].message").value("Required field"));
    }

    @Test
    public void updateTodoShouldReturnStatus422WhenPriorityIsNull() throws Exception {
        updateTodoRequest.setPriority(null);
        String updateJsonRequest = objectMapper.writeValueAsString(updateTodoRequest);
        updateRequestJsonPart = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                updateJsonRequest.getBytes());
        mockMvc.perform(multipart("/todos/" + todoId)
            .file(updateRequestJsonPart)
            .file(imagePart)
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .with(request -> {
                request.setMethod("PUT");
                return request;
            }))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.fieldMessages[0].fieldName").value("priority"))
            .andExpect(jsonPath("$.fieldMessages[0].message").value("Required field"));
    }

    @Test
    public void updateTodoShouldReturnStatus422WhenExpiresAtIsNull() throws Exception {
        updateTodoRequest.setExpiresAt(null);
        String updateJsonRequest = objectMapper.writeValueAsString(updateTodoRequest);
        updateRequestJsonPart = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                updateJsonRequest.getBytes());
        mockMvc.perform(multipart("/todos/" + todoId)
            .file(updateRequestJsonPart)
            .file(imagePart)
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .with(request -> {
                request.setMethod("PUT");
                return request;
            }))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.fieldMessages[0].fieldName").value("expiresAt"))
            .andExpect(jsonPath("$.fieldMessages[0].message").value("Required field"));
    }

    @Test
    public void updateTodoShouldReturnStatus422WhenStatusIsNull() throws Exception {
        updateTodoRequest.setStatus(null);
        String updateJsonRequest = objectMapper.writeValueAsString(updateTodoRequest);
        updateRequestJsonPart = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                updateJsonRequest.getBytes());
        mockMvc.perform(multipart("/todos/" + todoId)
            .file(updateRequestJsonPart)
            .file(imagePart)
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .with(request -> {
                request.setMethod("PUT");
                return request;
            }))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.fieldMessages[0].fieldName").value("status"))
            .andExpect(jsonPath("$.fieldMessages[0].message").value("Required field"));
    }

    @Test
    @Order(8)
    public void deleteShouldReturnStatus204WhenIdExists() throws Exception {
        mockMvc.perform(delete("/todos/" + todoId)
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());
    }

    @Test
    @Order(9)
    public void deleteShouldReturnStatus401WhenAuthTokenIsNotProvided() throws Exception {
        mockMvc.perform(delete("/todos/" + "rehuehuthuwhhiqw")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(9)
    public void deleteShouldReturnStatus404WhenIdDoesNotExist() throws Exception {
        mockMvc.perform(delete("/todos/" + "rehuehuthuwhhiqw")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

}
