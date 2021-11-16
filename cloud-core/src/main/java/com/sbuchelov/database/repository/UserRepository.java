package com.sbuchelov.database.repository;

import com.sbuchelov.database.service.ConnectionService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {
    public User findUser(String login, String password) {
        Connection connection = ConnectionService.connect();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM USER WHERE LOGIN = ? AND PASS = ?");
            statement.setString(1, login);
            statement.setString(2, password);

            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getString("login"),
                        rs.getString("pass"),
                        rs.getString("nickName")
                );
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("SWW", e);
        } finally {
            ConnectionService.close(connection);
        }
    }

    public List<User> findAll() {
        Connection connection = ConnectionService.connect();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM USER");

            ResultSet rs = statement.executeQuery();
            List<User> users = new ArrayList<>();

            while (rs.next()) {
                users.add(
                        new User(
                                rs.getString("login"),
                                rs.getString("pass"),
                                rs.getString("nickName"))
                );
            }
            return users;
        } catch (SQLException e) {
            throw new RuntimeException("SWW", e);
        } finally {
            ConnectionService.close(connection);
        }
    }

    public User findByName(String name) {
        Connection connection = ConnectionService.connect();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM USER WHERE LOGIN = ?");
            statement.setString(1, name);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getString("login"),
                        rs.getString("pass"),
                        rs.getString("nickName")
                );
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("SWW", e);
        } finally {
            ConnectionService.close(connection);
        }
    }

    public void updateUser(String newName, String oldName) {
        Connection connection = ConnectionService.connect();
        try {
            PreparedStatement statement = connection.prepareStatement("UPDATE USER SET nickName=? " +
                    "WHERE nickName=?");
            statement.setString(1, newName);
            statement.setString(2, oldName);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("SWW", e);
        } finally {
            ConnectionService.close(connection);
        }
    }
}