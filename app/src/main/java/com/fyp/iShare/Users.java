package com.fyp.iShare;

import java.util.HashMap;

public class Users {
    String name;
    String email;
    String password;

    public Users(){}

    public Users(String name, String email, String password  ) {
        this.name = name;
        this.email = email;
        this.password = password;

    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

}
