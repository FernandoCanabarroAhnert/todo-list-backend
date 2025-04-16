package com.test.todo_list_backend.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.test.todo_list_backend.models.entities.Todo;

@Repository
public interface TodoRepository extends MongoRepository<Todo,String> {

    @Query("{  \"user._id\": ObjectId(?0) }")
    List<Todo> findByUserId(String userId);

    @Query("{  status: 'COMPLETED', \"user._id\": ObjectId(?0) }")
    Page<Todo> findAllCompletedTodosByUserId(String userId, Pageable pageable);

    @Query("{  $or: [ { status: 'NOT_STARTED' }, { status: 'IN_PROGRESS' } ], \"user._id\": ObjectId(?0) }")
    Page<Todo> findAllNotCompletedTodosByUserId(String userId, Pageable pageable);
}
