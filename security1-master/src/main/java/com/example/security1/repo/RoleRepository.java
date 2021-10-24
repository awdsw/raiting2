package com.example.security1.repo;

import com.example.security1.entity.AppRole;
import org.springframework.data.repository.CrudRepository;

public interface RoleRepository extends CrudRepository<AppRole, Long> {
    //получили роль
    AppRole getAppRoleByRoleName(String name);

}
