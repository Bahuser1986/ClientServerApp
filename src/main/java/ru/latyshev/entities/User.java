package ru.latyshev.entities;

import java.util.HashSet;
import java.util.Set;

public class User {
    private String loginName;
    private boolean isLogged = false;

    public User(String loginName) {this.loginName = loginName;
    }

    public String getLoginName() {
        return loginName;
    }

    public boolean isLogged() {
        return isLogged;
    }

    public void setLogged(boolean logged) {
        isLogged = logged;
    }
}
