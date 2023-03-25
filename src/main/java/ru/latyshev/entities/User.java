package ru.latyshev.entities;

import java.util.HashSet;
import java.util.Set;

public class User {
    private String loginName;
    private boolean isLogged = false;
    private static Set<User> users = new HashSet<>();


    public User(String loginName) {
        this.loginName = loginName;
    }

    public String getLoginName() {
        return loginName;
    }

    public static Set<User> getUsers() {
        return users;
    }

    public static boolean userLogin(String login) {
        String[] loginArr = login.split(" "); // продебажить удаление пробелов
        String loginCommand = loginArr[0].trim();

        String[] loginParams = loginArr[1].split("="); //проверить выход за границы массива
        String loginKey = loginParams[0].trim();
        String loginName = loginParams[1].trim();
        if (!loginCommand.equals("login")) {
            return false;
        }
        if (!loginKey.equals("-u")) {
            return false;
        }
        User user = new User(loginName);
        user.isLogged = true;
        users.add(user); // нужно сделать проверки на уникальных пользователей, на занятые имена
        return true;
    }
}
