package com.test.todo_list_backend.services.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.test.todo_list_backend.mappers.TodoMapper;
import com.test.todo_list_backend.models.dtos.TodoResponseDTO;
import com.test.todo_list_backend.models.dtos.TodoUpdateRequestDTO;
import com.test.todo_list_backend.models.dtos.TodosSummaryResponseDTO;
import com.test.todo_list_backend.models.dtos.TodoRequestDTO;
import com.test.todo_list_backend.models.entities.Todo;
import com.test.todo_list_backend.models.entities.User;
import com.test.todo_list_backend.models.enums.TodoStatus;
import com.test.todo_list_backend.repositories.TodoRepository;
import com.test.todo_list_backend.services.TodoService;
import com.test.todo_list_backend.services.UserService;
import com.test.todo_list_backend.services.exceptions.DefaultValidationError;
import com.test.todo_list_backend.services.exceptions.ResourceNotFoundException;
import com.test.todo_list_backend.utils.ConvertMultipartFileIntoBase64;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TodoServiceImpl implements TodoService {

    private final TodoRepository todoRepository;
    private final UserService userService;
    
    @Override
    @Transactional(readOnly = true)
    public Page<TodoResponseDTO> findAllNotCompletedTodosFromConnectedUser(Pageable pageable) {
        User user = userService.getConnectedUser();
        return this.todoRepository.findAllNotCompletedTodosByUserId(user.getId(), pageable)
            .map(todo -> TodoMapper.convertEntityToResponseDTO(todo));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TodoResponseDTO> findAllCompletedTodosFromConnectedUser(Pageable pageable) {
        User user = userService.getConnectedUser();
        return this.todoRepository.findAllCompletedTodosByUserId(user.getId(), pageable)
            .map(todo -> TodoMapper.convertEntityToResponseDTO(todo));
    }

    @Override
    @Transactional(readOnly = true)
    public TodoResponseDTO findTodoById(String id) {
        return todoRepository.findById(id)
            .map(TodoMapper::convertEntityToResponseDTO)
            .orElseThrow(() -> new ResourceNotFoundException("Todo", id));
    }

    @Override
    @Transactional
    public void createTodo(TodoRequestDTO request, MultipartFile image) {
        if (image == null) {
            throw new DefaultValidationError("Image is required");
        }
        User user = userService.getConnectedUser();
        Todo todo = TodoMapper.convertRequestDTOToEntity(request, user);
        String todoImage = ConvertMultipartFileIntoBase64.convertMultipartFileIntoBase64(image);
        todo.setImage(todoImage);
        todoRepository.save(todo);
    }

    @Override
    @Transactional
    public void updateTodo(String id, TodoUpdateRequestDTO request, MultipartFile image) {
        Todo todo = todoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Todo", id));
        TodoMapper.updateTodoEntity(todo, request);
        if (image != null) {
            String todoImage = ConvertMultipartFileIntoBase64.convertMultipartFileIntoBase64(image);
            todo.setImage(todoImage);
        }
        todoRepository.save(todo);
    }

    @Override
    @Transactional
    public void deleteTodo(String id) {
        if (!todoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Todo", id);
        }
        todoRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public TodosSummaryResponseDTO getTodosSummaryFromConnectedUser() {
        List<Todo> todos = todoRepository.findByUserId(userService.getConnectedUser().getId());
        int totalTodos = todos.size();

        if (totalTodos == 0) {
            return new TodosSummaryResponseDTO(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        int completedTodos = todos.stream().filter(todo -> todo.getStatus().equals(TodoStatus.COMPLETED)).toList().size();
        int inProgressTodos = todos.stream().filter(todo -> todo.getStatus().equals(TodoStatus.IN_PROGRESS)).toList().size();
        int notStartedTodos = todos.stream().filter(todo -> todo.getStatus().equals(TodoStatus.NOT_STARTED)).toList().size();

        BigDecimal completedTodosPercentage = BigDecimal.valueOf(completedTodos).divide(BigDecimal.valueOf(totalTodos), 2, RoundingMode.HALF_UP);
        BigDecimal inProgressTodosPercentage = BigDecimal.valueOf(inProgressTodos).divide(BigDecimal.valueOf(totalTodos), 2, RoundingMode.HALF_UP);
        BigDecimal notStartedTodosPercentage = BigDecimal.valueOf(notStartedTodos).divide(BigDecimal.valueOf(totalTodos), 2, RoundingMode.HALF_UP);

        
        return new TodosSummaryResponseDTO(completedTodosPercentage, inProgressTodosPercentage, notStartedTodosPercentage);
    }

}
