package com.example.lab8_messages;

import com.example.lab8_messages.Repo.MessageRepo;
import com.example.lab8_messages.Repo.dbFriendshipRepo;
import com.example.lab8_messages.Repo.dbUserRepo;
import com.example.lab8_messages.Validator.FriendshipValidator;
import com.example.lab8_messages.Validator.UserValidator;
import com.example.lab8_messages.Service.SocialNetworkService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class LoginController {

    @FXML
    private Label errorText;

    @FXML
    private TextField userIdField;

    @FXML
    private TextField usernameField;

    private SocialNetworkService service;

    @FXML
    private void initialize() {
        UserValidator userValidator = new UserValidator();
        FriendshipValidator friendshipValidator = new FriendshipValidator();

        String url = "jdbc:postgresql://localhost:5432/postgres";
        String user = "stefansandru";
        String password = "1234";
        dbUserRepo userRepo = new dbUserRepo(userValidator, url, user, password);
        dbFriendshipRepo friendshipRepo = new dbFriendshipRepo(friendshipValidator, url, user, password);
        MessageRepo messageRepo = new MessageRepo(url, user, password, userRepo);

        this.service = new SocialNetworkService(userRepo, friendshipRepo, messageRepo);
    }

    @FXML
    private void handleLogin() {
        Long userId = Long.valueOf(userIdField.getText());
        String username = usernameField.getText();

        try {
            if (isValidUser(userId, username)) {
                openMainWindow(userId, username);
            } else {
                showAlert("Invalid user or password!");
            }
        } catch (Exception e) {
            showAlert("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isValidUser(Long userId, String username) {
        if (service.findUser(userId, username)) {
            System.out.println("User found");
            return true;
        } else {
            System.out.println("User not found");
            return false;
        }
    }

    private void showAlert(String message) {
    if (errorText != null) {
        if (message == null || message.isEmpty()) {
            errorText.setVisible(false);
        } else {
            errorText.setText("User not found");
            errorText.setVisible(true);
        }
    } else {
        System.err.println("Error TextArea is not initialized.");
    }
}

    private void openMainWindow(Long userId, String username) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("main-view.fxml"));
            Stage stage = (Stage) userIdField.getScene().getWindow();
            Scene scene = new Scene(loader.load(), 1000, 600);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("style.css")).toExternalForm());
            MainController controller = loader.getController();
            controller.setUser(userId, username);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            showAlert("An error occurred: " + e.getMessage());
        }
    }
}