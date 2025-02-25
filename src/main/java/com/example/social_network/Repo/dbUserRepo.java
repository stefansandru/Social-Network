package com.example.social_network.Repo;

import com.example.social_network.Validator.UserValidator;
import com.example.social_network.Validator.ValidationException;
import com.example.social_network.domain.User;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class dbUserRepo implements Repository<Long, User> {
    private final String url;
    private final String user;
    private final String password;
    private final String photosFolder;
    UserValidator validator;

    public dbUserRepo(
            UserValidator validator,
            String url,
            String user,
            String password,
            String photosFolder) {
        this.validator = validator;
        this.url = url;
        this.user = user;
        this.password = password;
        this.photosFolder = photosFolder;
    }

    @Override
    public Optional<User> findOne(Long id) {
        String query = "SELECT * FROM users WHERE ID = ?";
        User user = null;

        try (Connection connection = DriverManager.getConnection(url, this.user, password);
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Long userID = resultSet.getLong("ID");
                String name = resultSet.getString("name");
                String password = resultSet.getString("password");
                String profileImagePath = resultSet.getString("profile_image_path");
                user = new User(userID, name, password, photosFolder + profileImagePath);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(user);
    }

    @Override
    public Iterable<User> findAll() {
        Map<Long, User> users = new HashMap<>();
        String query = "SELECT * FROM users";
        try (Connection connection = DriverManager.getConnection(url, this.user, password);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                String name = resultSet.getString("name");
                String password = resultSet.getString("password");
                String profileImagePath = resultSet.getString("profile_image_path");
                User user = new User(id, name, password, profileImagePath);
                users.put(id, user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users.values();
    }

    public Iterable<User> findUsersByPrefix(String prefix) {
        Map<Long, User> users = new HashMap<>();
        String query = "SELECT * FROM users WHERE name LIKE ?";
        try (Connection connection = DriverManager.getConnection(url, this.user, password);
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, prefix + "%");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                String name = resultSet.getString("name");
                String password = resultSet.getString("password");
                String profileImagePath = resultSet.getString("profile_image_path");
                User user = new User(id, name, password, profileImagePath);
                users.put(id, user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users.values();
    }

    @Override
    public Optional<User> save(User entity) {
        if (entity.getName().isEmpty())
            throw new ValidationException("User name must not be null or empty");

        String query = "INSERT INTO users (name) VALUES (?)";
        try (Connection connection = DriverManager.getConnection(url, this.user, password);
             PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, entity.getName());

            int affectedRows = statement.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        entity.setId(generatedKeys.getLong(1));
                    }
                }
                return Optional.of(entity);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> delete(Long id) {
        Optional<User> userToDelete = findOne(id);
        if (userToDelete.isEmpty()) {
            return Optional.empty();
        }

        String query = "DELETE FROM users WHERE ID = ?";
        try (Connection connection = DriverManager.getConnection(url, this.user, password);
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
        return userToDelete;
    }

    @Override
    public Optional<User> update(User entity) {
        if (entity == null || entity.getId() == null) {
            throw new IllegalArgumentException("User or User ID cannot be null!");
        }

        String query = "UPDATE users SET name = ? WHERE ID = ?";
        try (Connection connection = DriverManager.getConnection(url, this.user, password);
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, entity.getName());
            statement.setLong(2, entity.getId());

            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated == 0) {
                return Optional.empty();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.of(entity);
    }
}