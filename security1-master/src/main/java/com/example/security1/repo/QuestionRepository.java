package com.example.security1.repo;

import com.example.security1.entity.AppQuestion;
import com.example.security1.entity.UserQuestion;
import org.springframework.data.repository.CrudRepository;

public interface QuestionRepository extends CrudRepository<AppQuestion, Long> {

}
