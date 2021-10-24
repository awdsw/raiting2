package com.example.security1.repo;

import com.example.security1.entity.AppUser;
import com.example.security1.entity.UserRole;
import org.springframework.data.repository.CrudRepository;

public interface UserRoleRepository extends CrudRepository<UserRole, Long> {
    //получить пользователя
    UserRole getUserRoleByAppUser(AppUser id);




//    UserRole findUserRoleById(long id);
//
//    //получить пользователя
//    Iterable<UserRole> findUserRolesByAppUser(long id);
//
//    //получить роль
//    Iterable<UserRole>findUserRolesByAppRole(long id);
}
