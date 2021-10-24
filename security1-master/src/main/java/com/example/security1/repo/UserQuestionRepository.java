package com.example.security1.repo;

import com.example.security1.entity.AppQuestion;
import com.example.security1.entity.AppUser;
import com.example.security1.entity.UserQuestion;
import org.springframework.data.repository.CrudRepository;

public interface UserQuestionRepository extends CrudRepository<UserQuestion, Long> {
    //получить пользователя по вопросу
    UserQuestion getUserQuestionByAppQuestion(String question);

    //вернет все объекты
    Iterable<UserQuestion>findUserQuestionByIsAnswered(Boolean answered);

    //готовы принять пользователя, вопрос которого не отвечен
    //если работник не пустой, то этого пользователя вызвали конуретного и вопрос не отвечен
    UserQuestion findUserQuestionByWorkerIdNotNullAndAppUserAndIsAnswered(AppUser appUser, Boolean isAnswered);

    //У конкретного worker должно быть поле с 0
    UserQuestion findUserQuestionByIsAnsweredAndWorkerId(Boolean isAnswered, AppUser appUser);

    UserQuestion findUserQuestionByIsAnsweredAndAppUserAndWorkerIdNotNull(Boolean isAnswered, AppQuestion appQuestion);

    //вернет все вопросы пользователя
    Iterable<UserQuestion> findUserQuestionsByAppUserAndIsAnsweredTrue(AppUser appUser);

    //поиск по id
    UserQuestion getById(Long id);

    //получить вопросы пользователей назначенные рабоникам, отвеченные и с оценками выставленными
    Iterable<UserQuestion>findUserQuestionsByWorkerIdAndIsAnsweredNotNullAndMarkNotNull(AppUser id);






}
