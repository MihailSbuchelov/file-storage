package com.sbuchelov.database.service;

import com.sbuchelov.database.repository.User;
import com.sbuchelov.database.repository.UserRepository;

public class Authentication {
    private UserRepository users;

    public Authentication() {
        users = new UserRepository();
    }

    public String findNicknameByLoginAndPassword(String login, String password) {
        User user = users.findUser(login, password);
        if (user != null) {
            return user.getNickname();
        }
        return null;
    }

    public String findNicknameByLogin(String login) {
        User user = users.findByName(login);
        if (user != null) {
            return user.getNickname();
        } else return null;
    }

    public void updateNickname(String newName, String oldName) {
        users.updateUser(newName, oldName);
    }
}
