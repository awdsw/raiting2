package com.example.security1.config;

import javax.sql.DataSource;

import com.example.security1.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;



@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private DataSource dataSource;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        return bCryptPasswordEncoder;
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {

        // Настройка сервиса для поиска пользователя в базе данных.
        // И Установка PasswordEncoder
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());

    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.csrf().disable();

        // Страницы не требуют входа в систему
        http.authorizeRequests().antMatchers("/", "/login", "/logout").permitAll();

        // /userInfo требует входа в систему как ROLE_USER или ROLE_ADMIN.
        //  Если входа нет, он перенаправится на страницу входа /login page.
        //http.authorizeRequests().antMatchers("/userInfo").access("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')");
        http.authorizeRequests().antMatchers("/userInfo").access("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')");

        // For ADMIN only.
        //http.authorizeRequests().antMatchers("/admin").access("hasRole('ROLE_ADMIN')");

        // For Super_ADMIN only.
        http.authorizeRequests().antMatchers("/admin").access("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_ADMIN')");

        //Когда пользователь вошел в систему под именем XX.
        // Но доступ к странице, для которой требуется роль YY,
        // будет вызвано исключение AccessDeniedException.
        http.authorizeRequests().and().exceptionHandling().accessDeniedPage("/403");

        // Конфигурация для формы входа в систему
        http.authorizeRequests().and().formLogin()//
                // Отправьте URL страницы входа в систему.
                .loginProcessingUrl("/j_spring_security_check") // Submit URL
                .loginPage("/login")//
                .defaultSuccessUrl("/userAccountInfo")//
                .failureUrl("/login?error=true")//
                .usernameParameter("username")//
                .passwordParameter("password")
                // Конфигурация для страницы выхода Logout Page
                .and().logout().logoutUrl("/logout").logoutSuccessUrl("/logoutSuccessful");

        // Config Remember Me. //запомнить меня
        http.authorizeRequests().and() //
                .rememberMe().tokenRepository(this.persistentTokenRepository()) //
                .tokenValiditySeconds(1 * 24 * 60 * 60); // 24h

    }

    //работа с БД
    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        JdbcTokenRepositoryImpl db = new JdbcTokenRepositoryImpl();
        db.setDataSource(dataSource);
        return db;
    }

}

