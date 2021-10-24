package com.example.security1.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.example.security1.entity.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


/**использующиеся для доступа в базу данных,
 например Query, Insert, Update, Delete.
 Классы DAO обычно аннотированыы с помощью @Repository чтобы сказать Spring управлять ими как Spring BEAN.*/

/**Класс AppUserDAO используется для манипуляции с таблицей APP_USER.
 Он имеет метод поиска пользователя в базе данных соответствующего имени пользователя.*/

@Repository
@Transactional
public class AppRoleDAO {

    @Autowired
    private EntityManager entityManager;

    public List<String> getRoleNames(Long userId) {
        String sql = "Select ur.appRole.roleName from " + UserRole.class.getName() + " ur " //
                + " where ur.appUser.userId = :userId ";

        Query query = this.entityManager.createQuery(sql, String.class);
        query.setParameter("userId", userId);
        return query.getResultList();
    }

}