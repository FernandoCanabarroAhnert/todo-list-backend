package com.test.todo_list_backend.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.test.todo_list_backend.models.dtos.TodoResponseDTO;
import com.test.todo_list_backend.models.dtos.TodoUpdateRequestDTO;
import com.test.todo_list_backend.models.dtos.TodosSummaryResponseDTO;
import com.test.todo_list_backend.models.dtos.TodoRequestDTO;

public interface TodoService {

    Page<TodoResponseDTO> findAllNotCompletedTodosFromConnectedUser(Pageable pageable);
    Page<TodoResponseDTO> findAllCompletedTodosFromConnectedUser(Pageable pageable);
    TodoResponseDTO findTodoById(String id);
    void createTodo(TodoRequestDTO request, MultipartFile image);
    void updateTodo(String id, TodoUpdateRequestDTO request, MultipartFile image);
    void deleteTodo(String id);

    TodosSummaryResponseDTO getTodosSummaryFromConnectedUser();

}
