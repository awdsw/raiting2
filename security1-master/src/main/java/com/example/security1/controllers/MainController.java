package com.example.security1.controllers;

import java.security.Principal;
import java.util.*;

import com.example.security1.entity.*;
import com.example.security1.repo.*;
import com.example.security1.utils.EncrytedPasswordUtils;
import com.example.security1.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Controller
public class MainController {
    //Создаем очередь двунаправленная очередь/ добавление и удаление с 2-х сторон
    ArrayDeque<UserQuestion> appQuestions = new ArrayDeque<>();

    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserRoleRepository userRoleRepository;
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private UserQuestionRepository userQuestionRepository;

    @RequestMapping(value = { "/", "/welcome" }, method = RequestMethod.GET)
    public String welcomePage(Model model, HttpServletRequest request) {
        model.addAttribute("title", "Welcome");
        model.addAttribute("message", "Это страница приветствия!");

        //отсдедить кто залогинился в зависимости от роли
        System.out.println(request.isUserInRole("ROLE_SUPER_ADMIN"));

        //Создание ролей и сохранение их в БД
        /*AppRole superAdmin = new AppRole("ROLE_SUPER_ADMIN");
        roleRepository.save(superAdmin);
        AppRole admin = new AppRole("ROLE_ADMIN");
        roleRepository.save(admin);
        AppRole user = new AppRole("ROLE_USER");
        roleRepository.save(user);*/

        //Вывод в консоли всех ролей
        for (AppRole role:roleRepository.findAll()){
            System.out.println(role.toString());
        }

        return "welcomePage";
    }

    /** Principal - Это активный пользователь*/
    @RequestMapping(value = "/admin", method = RequestMethod.GET)
    public String adminPage(Model model, Principal principal,
                            HttpServletRequest request) {

        //запускаем метод
        updateQueue();

        User loginedUser = (User) ((Authentication) principal).getPrincipal();

        String userInfo = WebUtils.toString(loginedUser);
        model.addAttribute("userInfo", userInfo);

        //Вывод пользователей для админа
        List<AppUser> users;
        if (request.isUserInRole("ROLE_SUPER_ADMIN")){
            users = (List<AppUser>) userRepository.findAll();
        }
        else {
            users = sortingRole("ROLE_USER");
        }

        //Вывод всех вопросов на странице админа
        List<UserQuestion> questions = new ArrayList<>();
        //проходимся по коллекции
        for (UserQuestion question: appQuestions) {
            //добавление в очередь
            questions.add(question);
        }

        model.addAttribute("users", users);
        model.addAttribute("questions", questions); //далее adminPage

        //оценка специалиста по каждому из отвеченных вопросов
        List<UserQuestion>list =
                (List<UserQuestion>) userQuestionRepository
                        .findUserQuestionsByWorkerIdAndIsAnsweredNotNullAndMarkNotNull
                                (userRepository.getAppUserByUserName(getCurrentUser()));

        if (list!=null){
            System.out.println("---");
        }
        double avg=getAvgMark(list);
        model.addAttribute("avgMark", avg);
        return "adminPage";
    }

    //средняя оценка вспом метод
    public Double getAvgMark(List<UserQuestion> list){
        double sum = 0;
        int count = 0;
        for (UserQuestion user : list) {
            if (user.getMark() != null){
                sum+=user.getMark();
                count++;
            }
        }
        return sum/count;
    }

    //вспомогательный метод / вернет неотвеченные ?
    public void updateQueue(){
        //подтягиваие всех пользователей, на которые вопросы не отвечены
        List<UserQuestion> appendToMainQueue = (List<UserQuestion>) userQuestionRepository.findUserQuestionByIsAnswered(false);
        for (UserQuestion qstSearch: appendToMainQueue) {
            //если объект, который смотрим - нет его
            if (!searchInQueue(qstSearch)){
                //добавляем
                appQuestions.addLast(qstSearch);
            }
        }
    }

    public boolean searchInQueue(UserQuestion uq){
        for (UserQuestion question:appQuestions) {
            if (question.getAppQuestion().getId() == uq.getAppQuestion().getId()){
                return true;
            }
        }
        return false;
    }

    //дополнительный метод, принимает роль, и
    // который выводит пользователей согласно введенной роли
    public List<AppUser> sortingRole(String role){
        List<AppUser> users = new ArrayList<>();
        for (UserRole elementUserRole: userRoleRepository.findAll()) {
            if (elementUserRole.getAppRole().getRoleName().equals(role)){
                users.add(elementUserRole.getAppUser());
            }
        }
        return users;
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String loginPage(Model model) {

        return "loginPage";
    }

    @RequestMapping(value = "/logoutSuccessful", method = RequestMethod.GET)
    public String logoutSuccessfulPage(Model model) {
        model.addAttribute("title", "Logout");
        return "logoutSuccessfulPage";
    }

    @RequestMapping(value = "/userInfo", method = RequestMethod.GET)
    public String userInfo(Model model, Principal principal) {

        //Считываем кто авторизовался
        String currentUser = getCurrentUser();
        //Есть ли у него ожидающие мероприятия
        AppUser user = userRepository.getAppUserByUserName(currentUser);
        UserQuestion usQues = userQuestionRepository.findUserQuestionByWorkerIdNotNullAndAppUserAndIsAnswered(user, false);

        //получить вопросы пользователя(отвеченные)
        List<UserQuestion> questions = (List<UserQuestion>) userQuestionRepository.findUserQuestionsByAppUserAndIsAnsweredTrue(userRepository.getAppUserByUserName(getCurrentUser()));
        if (questions != null){
            model.addAttribute("questions", questions);
        }

        if (usQues != null){
            model.addAttribute("message", usQues.getWorkerId().getUserName());
        }
        else {
            model.addAttribute("message", "Нет вопросов от пользователей");
        }

//        //Выввод всех вопросов конкретного пользователя
//        List<UserQuestion> questions =
//                (List<UserQuestion>) userQuestionRepository.findUserQuestionsByAppUserAndIsAnsweredTrue(userRepository.getAppUserByUserName(getCurrentUser()));
//
//        //Вывод в браузере вопросов списком
//        if (questions != null){
//            model.addAttribute("questions", questions);
//        }

        // After user login successfully.
        String userName = principal.getName();

        System.out.println("Имя пользователя: " + userName);

        User loginedUser = (User) ((Authentication) principal).getPrincipal();

        String userInfo = WebUtils.toString(loginedUser);
        model.addAttribute("userInfo", userInfo);

        AppUser appUser = userRepository.getAppUserByUserName(getCurrentUser());
        UserRole userRole = userRoleRepository.getUserRoleByAppUser(appUser);
        if (userRole.getAppRole().getRoleName().equals("ROLE_USER")){
            model.addAttribute("isUser", userRole.getAppRole().getRoleName());
        }else {
            model.addAttribute("isUser", null);
        }

        return "userInfoPage";
    }

    @RequestMapping(value = "/403", method = RequestMethod.GET)
    public String accessDenied(Model model, Principal principal) {

        if (principal != null) {
            User loginedUser = (User) ((Authentication) principal).getPrincipal();

            String userInfo = WebUtils.toString(loginedUser);

            model.addAttribute("userInfo", userInfo);

            String message = "Здравствуйте " + principal.getName() //
                    + "<br> У вас нет разрешения на доступ к этой странице!";
            model.addAttribute("message", message);

        }
        return "403Page";
    }

    //регистрация пользователя
    @RequestMapping(value = "/registration", method = RequestMethod.POST)
    public String registration(@RequestParam String name,
                               @RequestParam String password,
                               Model model){
        //Занесение в БД. Создаем объект, но предварительно шифруем пароль
        //Зашифрованный пароль, его вносим в User
        //EncrytedPasswordUtils.encrytePassword(password);
        //enabled - активные или неактивный пользователь

        try {
            password = EncrytedPasswordUtils.encrytePassword(password);

            //Создаем пользователя
            AppUser appUser = new AppUser(name, password, true);

            //Первоначально регистрация возможна как обычному поользователю 3 - простой пользователь
            //AppRole appRole = roleRepository.findById((long)3).get();
            AppRole appRole = roleRepository.getAppRoleByRoleName("ROLE_USER");

            //Сопоставление данных в связной таблице /назначение ролей пользователю
            UserRole userRole = new UserRole(appUser,appRole);

            //сохраение пользователя
            userRepository.save(appUser);

            //сохранение роли пользоватея
            userRoleRepository.save(userRole);
        } catch (Exception ex){}

        //Задаем роль пользователю (1 - супер-админ, 2 - админ 3 - простой пользователь)
        //получить из БД роли по id / сохранение супер-админа
        /*AppRole superAdmin = roleRepository.findById((long)1).get();
        roleRepository.save(superAdmin);*/

        /*AppRole admin = roleRepository.findById((long)2).get();
        roleRepository.save(admin);*/

        //Как только разегался, сразу можно авторизовывааться
        return "loginPage";
    }

    //Для редактирования пароля
    //Получение id из БД
    @GetMapping("/admin/{id}/edit") //id - получение цифры из БД
    public String adminEdit(@PathVariable(value = "id")
                                    long id, Model model){
        //Проверка на существование
        if (!userRepository.existsById(id)){
            return "redirect:/admin";
        }
        //Поиск по id и найденное помещаем в объект
        Optional<AppUser> userOptional =
                userRepository.findById(id);
        //Перевод из Optional в ArrayList
        ArrayList<AppUser> result = new ArrayList<>();
        userOptional.ifPresent(result::add);
        model.addAttribute("userOptional", result);

        return "edit";
    }

    //Редактирование паролей
    @PostMapping(value = "/admin/{id}/edit")
    public String edit(@PathVariable(value = "id") long id,
                       @RequestParam String password,
                       Model model) {
        //закодировали пароль
        password = EncrytedPasswordUtils.encrytePassword(password);

        AppUser appUser = userRepository.findById(id).orElseThrow();
        appUser.setEncrytedPassword(password);
        userRepository.save(appUser);

        return "redirect:/admin";
    }

    //Назначение админа / Домашка
    @PostMapping(value = "/admin/{id}/setAdmin")
    public String setAdmin(@PathVariable(value = "id") long id,
                       Model model) {

        AppUser appUser = userRepository.findById(id).orElseThrow();

        UserRole userRole = userRoleRepository.getUserRoleByAppUser(appUser);

        AppRole appRole = roleRepository.getAppRoleByRoleName("ROLE_ADMIN");

        userRole.setAppRole(appRole);

        //сохр изм
        userRoleRepository.save(userRole);

        return "redirect:/admin";
    }

    //Обработка формы с отправкой вопросов каким-либо пользователем
    @RequestMapping(value = "/askQuestion", method = RequestMethod.POST)
    public String addToQue(@RequestParam String question, Model model,
                           HttpServletRequest request){
        //получили пользователя
        String currentUser = getCurrentUser();

        //Получаем id пользователя по имени
        userRepository.getAppUserByUserName(currentUser);

        //создаем объект вопрос и передаем вопрос с формы
        AppQuestion appQuestion = new AppQuestion(question);

        //достать пользователя / сохраняем в объект
        AppUser appUser = userRepository.getAppUserByUserName(getCurrentUser());
        //если пользователь есть, то добавляем вопрос
        if (appUser != null){
            //создаем объект для объединенной таблицы
            //false - т.к. на вопрос еще не ответили, его пока только создали
            UserQuestion userQuestion = new UserQuestion(appUser, appQuestion, new Date(System.currentTimeMillis()), false);
            //сохрание в БД вопроса
            questionRepository.save(appQuestion);
            //сохрание в БД связи
            userQuestionRepository.save(userQuestion);
            //очередь связать с БД вопроы добавляем в конец
            appQuestions.addLast(userQuestion); //переход на Admin, чтобы вывести все вопросы

        }

        return "userInfoPage";
    }

    //Метод, который возвращает активного пользователя
    public String getCurrentUser(){
        //достать из контекста
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }


    //есть вопрос и на него необходимо ответить
    @RequestMapping(value = "/updateIsAnswered", method = RequestMethod.POST)
    public String updateQuestion(@RequestParam Long id, Model model){
        //создать ? из сводной таблицы
        UserQuestion uq = userQuestionRepository.findById(id).get();
        uq.setAnswered(true); //отвечен вопрос
        appQuestions.remove(uq); //удалить вопрос, т.к. он отвечен удаление из очереди
        userQuestionRepository.save(uq); //сохранить изменение
        return "welcomePage";
    }

    //Принять человека
    //админ нажал на кнопку, нужно выкинуть пользователя ajax
    @PostMapping("/acceptPerson")
    @ResponseBody //вернет просто текст
    public String acceptPerson(Model model){
        //
        UserQuestion userQues = userQuestionRepository.findUserQuestionByIsAnsweredAndWorkerId(false, userRepository.getAppUserByUserName(getCurrentUser()));

        if (userQues == null){
            UserQuestion userQuestion = appQuestions.pollFirst();

            if (userQuestion != null){
                //пишется имя, если админ отправляет запрос
                String user = getCurrentUser();

                //Установить репозиторий по получению работника
                userQuestion.setWorkerId(userRepository.getAppUserByUserName(user));
                userQuestionRepository.save(userQuestion);
                return user;
            }
            return "Нет вопросов";
        }else {
            return "Вы уже принимаете человека";
        }
    }

    //Завершить обслуживание человека / программа на 2 кнопку
    //админ нажал на кнопку, нужно выкинуть пользователя ajax
    @PostMapping("/endSession")
    @ResponseBody //вернет просто текст
    public String endSession(Model model){
        //найти что есть связь и кого-то принимаем
        UserQuestion userQues = userQuestionRepository.findUserQuestionByIsAnsweredAndWorkerId(false, userRepository.getAppUserByUserName(getCurrentUser()));

        if (userQues != null){
            userQues.setAnswered(true); //вопрос отвечен
            userQuestionRepository.save(userQues);

            //удалить из очереди
            appQuestions.pollFirst();

            return "Обслужен " + userQues.getAppUser().getUserName() + " успешно";
        }else {
            return "Вы не можете завершить сессию, она не начата";
        }
    }

    //метод принимает пользователяя и проверяет вызвали ли его
    //пользователь есть в очереди, либо его нет в очереди
    public Boolean CheckIsAccept(AppQuestion appQuestion){
        //отправляем принятого пользователя в таблицу запросов
        UserQuestion userQuestions = userQuestionRepository.findUserQuestionByIsAnsweredAndAppUserAndWorkerIdNotNull(false, appQuestion);

        return userQuestions != null;
    }

    //Сохранение оценки принимает id- вопроса-ответчика и на id-вопроса выставляется оценка
    @PostMapping("/saveMark")
    public String saveMark(@RequestParam Long questionId,
                           @RequestParam Long mark, Model model){
        //получили вопрос по id
        UserQuestion userQuestion = userQuestionRepository.getById(questionId);
        //установили оценку
        userQuestion.setMark(mark);

        //сохранили в репо
        userQuestionRepository.save(userQuestion);
        return "userInfoPage";
    }



}
