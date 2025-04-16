package com.test.todo_list_backend.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.test.todo_list_backend.models.entities.ActivationCode;
import java.util.Optional;

@Repository
public interface ActivationCodeRepository extends MongoRepository<ActivationCode,String> {

    Optional<ActivationCode> findByCode(String code);

}
