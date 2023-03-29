package ru.latyshev.entities;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class User implements Serializable {
    private String loginName;
    private boolean isLogged = false;

    public User(String loginName) {this.loginName = loginName;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLogged(boolean logged) {
        isLogged = logged;
    }
}
