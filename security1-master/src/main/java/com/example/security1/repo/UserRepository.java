package com.example.security1.repo;

import com.example.security1.entity.AppUser;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<AppUser, Long> {

    AppUser findByUserId(long id);

    //getИмяОбъектаByКритерий(Тип данных перменная)

    //получить пользователя по имени
    AppUser getAppUserByUserName(String name);

    AppUser getAppUserByUserNameAndUserId(String name, long id);



}
