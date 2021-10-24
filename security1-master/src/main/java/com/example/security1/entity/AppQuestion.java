package com.example.security1.entity;

import javax.persistence.*;

@Entity
public class AppQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "AppQuestion_Id", nullable = false)
    private long id;

    private String question; //вопрос, кот задаст пользователь

    public AppQuestion() { }

    public AppQuestion(String question) {
        this.question = question;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }



    @Override
    public String toString() {
        return question;
    }
}
