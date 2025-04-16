package com.test.todo_list_backend.tests;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;

import com.test.todo_list_backend.factories.TodoFactory;
import com.test.todo_list_backend.factories.UserFactory;
import com.test.todo_list_backend.models.dtos.TodoRequestDTO;
import com.test.todo_list_backend.models.dtos.TodoResponseDTO;
import com.test.todo_list_backend.models.dtos.TodoUpdateRequestDTO;
import com.test.todo_list_backend.models.dtos.TodosSummaryResponseDTO;
import com.test.todo_list_backend.models.entities.Todo;
import com.test.todo_list_backend.models.entities.User;
import com.test.todo_list_backend.repositories.TodoRepository;
import com.test.todo_list_backend.services.UserService;
import com.test.todo_list_backend.services.exceptions.DefaultValidationError;
import com.test.todo_list_backend.services.exceptions.ResourceNotFoundException;
import com.test.todo_list_backend.services.impl.TodoServiceImpl;

@ExtendWith(MockitoExtension.class)
public class TodoServiceTests {

    @InjectMocks
    private TodoServiceImpl todoService;
    @Mock
    private TodoRepository todoRepository;
    @Mock
    private UserService userService;

    private Todo todo;
    private TodoRequestDTO todoRequest;
    private TodoUpdateRequestDTO todoUpdateRequest;
    private User user;
    private String existingId;
    private String nonExistingId;
    private Pageable pageable;
    private Page<Todo> pageResponse;
    private MockMultipartFile mockImage;

    @BeforeEach
    public void setup() {
        this.todo = TodoFactory.create();
        this.todoRequest = TodoFactory.createRequest();
        this.todoUpdateRequest = TodoFactory.createUpdateRequest();
        this.user = UserFactory.create();
        this.existingId = "id";
        this.nonExistingId = "non-existing-id";
        this.pageable = PageRequest.of(0,3);
        this.pageResponse = new PageImpl<>(new ArrayList<>(Arrays.asList(todo)));

        this.mockImage = new MockMultipartFile(
            "image",                   
            "imagem.jpg",           
            "image/jpeg",        
            "image".getBytes()
        );
    }

    @Test
    public void findAllNotCompletedTodosFromConnectedUserShouldReturnPageOfTodoResponseDTO() {
        when(userService.getConnectedUser()).thenReturn(user);
        when(todoRepository.findAllNotCompletedTodosByUserId(user.getId(), pageable)).thenReturn(pageResponse);

        Page<TodoResponseDTO> response = todoService.findAllNotCompletedTodosFromConnectedUser(pageable);

        assertThat(response.getContent()).isNotEmpty();
        assertThat(response.getContent().get(0).getId()).isEqualTo(todo.getId());
        assertThat(response.getContent().get(0).getTitle()).isEqualTo(todo.getTitle());
        assertThat(response.getContent().get(0).getDescription()).isEqualTo(todo.getDescription());
        assertThat(response.getContent().get(0).getStatus()).isEqualTo(todo.getStatus().getStatus());
    }

    @Test
    public void findAllCompletedTodosFromConnectedUserShouldReturnPageOfResponseDTO() {
        when(userService.getConnectedUser()).thenReturn(user);
        when(todoRepository.findAllCompletedTodosByUserId(user.getId(), pageable)).thenReturn(pageResponse);

        Page<TodoResponseDTO> response = todoService.findAllCompletedTodosFromConnectedUser(pageable);

        assertThat(response.getContent()).isNotEmpty();
        assertThat(response.getContent().get(0).getId()).isEqualTo(todo.getId());
        assertThat(response.getContent().get(0).getTitle()).isEqualTo(todo.getTitle());
        assertThat(response.getContent().get(0).getDescription()).isEqualTo(todo.getDescription());
        assertThat(response.getContent().get(0).getStatus()).isEqualTo(todo.getStatus().getStatus());
    }

    @Test
    public void findByIdShouldReturnTodoResponseDTOWhenIdExists() {
        when(todoRepository.findById(existingId)).thenReturn(Optional.of(todo));

        TodoResponseDTO response = todoService.findTodoById(existingId);

        assertThat(response.getId()).isEqualTo(existingId);
    }

    @Test
    public void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        when(todoRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> todoService.findTodoById(nonExistingId)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    public void createTodoShouldThrowNoExceptionWhenImageIsNotNull() {
        when(userService.getConnectedUser()).thenReturn(user);
        when(todoRepository.save(any(Todo.class))).thenReturn(todo);

        assertThatCode(() -> todoService.createTodo(todoRequest, mockImage)).doesNotThrowAnyException();
    }

    @Test
    public void createTodoShouldThrowDefaultValidationErrorWhenImageIsNull() {
        assertThatThrownBy(() -> todoService.createTodo(todoRequest, null)).isInstanceOf(DefaultValidationError.class);
    }

    @Test
    public void updateTodoShouldThrowNoExceptionWhenIdExistsAndImageIsNotNull() {
        when(todoRepository.findById(existingId)).thenReturn(Optional.of(todo));
        when(todoRepository.save(any(Todo.class))).thenReturn(todo);

        assertThatCode(() -> todoService.updateTodo(existingId, todoUpdateRequest, mockImage)).doesNotThrowAnyException();
    }

    @Test
    public void updateTodoShouldThrowNoExceptionWhenIdExistsAndImageIsNull() {
        when(todoRepository.findById(existingId)).thenReturn(Optional.of(todo));
        when(todoRepository.save(any(Todo.class))).thenReturn(todo);

        assertThatCode(() -> todoService.updateTodo(existingId, todoUpdateRequest, null)).doesNotThrowAnyException();
    }

    @Test
    public void updateTodoShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        when(todoRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> todoService.updateTodo(nonExistingId, todoUpdateRequest, mockImage)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    public void deleteShouldThrowNoExceptionWhenIdExists() {
        when(todoRepository.existsById(existingId)).thenReturn(true);
        doNothing().when(todoRepository).deleteById(existingId);

        assertThatCode(() -> todoService.deleteTodo(existingId)).doesNotThrowAnyException();
    }

    @Test
    public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        when(todoRepository.existsById(nonExistingId)).thenReturn(false);
        
        assertThatThrownBy(() -> todoService.deleteTodo(nonExistingId)).isInstanceOfAny(ResourceNotFoundException.class);
    }

    @Test
    public void getTodosSummaryFromConnectedUserShouldReturnCompleteSummaryWhenTodosListIsNotEmpty() {
        when(userService.getConnectedUser()).thenReturn(user);
        when(todoRepository.findByUserId(user.getId())).thenReturn(new ArrayList<>(Arrays.asList(todo)));

        TodosSummaryResponseDTO response = todoService.getTodosSummaryFromConnectedUser();

        assertThat(response).isNotNull();
        assertThat(response.getNotStartedTodosPercentage()).isEqualTo(BigDecimal.valueOf(1).setScale(2, RoundingMode.HALF_UP));
        assertThat(response.getInProgressTodosPercentage()).isEqualTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        assertThat(response.getCompletedTodosPercentage()).isEqualTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    public void getTodosSummaryFromConnectedUserShouldReturnEmptySummaryWhenTodosListIsEmpty() {
        when(userService.getConnectedUser()).thenReturn(user);
        when(todoRepository.findByUserId(user.getId())).thenReturn(Collections.emptyList());

        TodosSummaryResponseDTO response = todoService.getTodosSummaryFromConnectedUser();

        assertThat(response).isNotNull();
        assertThat(response.getNotStartedTodosPercentage()).isEqualTo(BigDecimal.ZERO);
        assertThat(response.getInProgressTodosPercentage()).isEqualTo(BigDecimal.ZERO);
        assertThat(response.getCompletedTodosPercentage()).isEqualTo(BigDecimal.ZERO);
    }

}
