package com.test.todo_list_backend.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.test.todo_list_backend.models.entities.Role;

@Repository
public interface RoleRepository extends MongoRepository<Role,String> {

    Role findByAuthority(String authority);

}
