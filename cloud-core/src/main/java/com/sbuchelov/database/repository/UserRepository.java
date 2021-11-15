package com.sbuchelov.database.repository;

import com.sbuchelov.database.service.ConnectionService;
import com.sbuchelov.database.service.CredentialsEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {
    public CredentialsEntry findUser(String login, String password) {
        Connection connection = ConnectionService.connect();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM USER WHERE LOGIN = ? AND PASS = ?");
            statement.setString(1, login);
            statement.setString(2, password);

            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return new CredentialsEntry(
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

    public List<CredentialsEntry> findAll() {
        Connection connection = ConnectionService.connect();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM USER");

            ResultSet rs = statement.executeQuery();
            List<CredentialsEntry> users = new ArrayList<>();

            while (rs.next()) {
                users.add(
                        new CredentialsEntry(
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

    public CredentialsEntry findByName(String name) {
        Connection connection = ConnectionService.connect();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM USER WHERE LOGIN = ?");
            statement.setString(1, name);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return new CredentialsEntry(
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