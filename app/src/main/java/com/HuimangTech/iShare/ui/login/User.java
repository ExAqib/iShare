package com.HuimangTech.iShare.ui.login;

public class User {

    public String name;
    public String email;

    public User() {
    }

    public String password;

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }
}