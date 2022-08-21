package com.HuimangTech.iShare.ui.login;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {

    public String Name;
    public String Email;
    public String Password;


    public User() {
    }

    public User(String name, String email, String password) {
        this.Name = name;
        this.Email = email;
        this.Password = password;
    }
}