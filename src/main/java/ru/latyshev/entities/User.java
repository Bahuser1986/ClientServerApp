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

    //login command parsing
    public static boolean checkLoginCommand(String login) {
        String[] loginArr = login.trim().split(" ", 2);
        if (loginArr.length != 2) {
            return false;
        }

        String loginCommand = loginArr[0];
        String[] loginParams = loginArr[1].trim().split("=");
        String loginKey = loginParams[0].trim();

        if (!loginCommand.equals("login")) {
            return false;
        }
        if (!loginKey.equals("-u")) {
            return false;
        }
        return true;
    }
    public static String getLoginNameFromCommand(String loginName) {
        String[] loginArr = loginName.split("=");
        return loginArr[1].trim();
    }
}
