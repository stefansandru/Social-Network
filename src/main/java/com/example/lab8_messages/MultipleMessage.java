package com.example.lab8_messages;

import com.example.lab8_messages.Repo.MessageRepo;
import com.example.lab8_messages.Repo.dbFriendshipRepo;
import com.example.lab8_messages.Repo.dbUserRepo;
import com.example.lab8_messages.Service.SocialNetworkService;
import com.example.lab8_messages.Validator.FriendshipValidator;
import com.example.lab8_messages.Validator.UserValidator;
import com.example.lab8_messages.domain.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MultipleMessage {

    @FXML
    private TextField messageField;

    @FXML
    private ObservableList<User> usersList = FXCollections.observableArrayList();

    @FXML
    private ObservableList<User> selectedUsers = FXCollections.observableArrayList();

    @FXML
    private TextField searchField;

    @FXML
    private ListView<User> usersListView;

    @FXML ListView<User> selectedUsersListView;

    private Long mainUserId;
    private String mainUsername;
    private SocialNetworkService service;

    public MultipleMessage() {
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
    public void initialize() {
        usersListView.setItems(usersList);
        selectedUsersListView.setItems(selectedUsers);
    }

    public void setUser(Long userId, String username) {
        this.mainUserId = userId;
        this.mainUsername = username;
        loadUsers();
    }

    private void loadUsers() {
        List<User> users = service.getUsers(mainUserId);
        usersList.setAll(users);
        usersListView.setItems(usersList);
    }

    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase();
        List<User> filteredList = service.findUsersByPrefix(searchText, mainUserId);
        usersList.setAll(filteredList);
        usersListView.setItems(usersList);
    }

    @FXML
    private void handleSendMessage() {
        String message = messageField.getText().trim();
        List<Long> userIds = selectedUsers.stream().map(User::getId).collect(Collectors.toList());
        service.sendMessage(mainUserId, userIds, message, null);
        messageField.clear();
    }

    @FXML
    private void handleBackButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("main-view.fxml"));
            Stage stage = (Stage) usersListView.getScene().getWindow();
            Scene scene = new Scene(loader.load(), 1000, 600);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("style.css")).toExternalForm());
            MainController controller = loader.getController();
            controller.setUser(mainUserId, mainUsername);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClickUsers(MouseEvent event) {
        if (event.getClickCount() == 1) {
            // Obține toți utilizatorii selectați
            List<User> selected = usersListView.getSelectionModel().getSelectedItems();
            if (!selected.isEmpty()) {
                // Adaugă utilizatorii selectați în lista de utilizatori selectați
                selectedUsers.addAll(selected);

                // Actualizează `selectedUsersListView`
                selectedUsersListView.setItems(selectedUsers);
            }
        }
    }

    @FXML
    private void handleClickSelectedUsers(MouseEvent event) {
        if (event.getClickCount() == 1) {
            // Obține utilizatorii selectați din lista de utilizatori selectați
            User selectedUser = selectedUsersListView.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                // Mută utilizatorul înapoi în `usersListView`
                selectedUsers.remove(selectedUser);
                usersList.add(selectedUser);

                // Actualizează listele
                selectedUsersListView.setItems(selectedUsers);
                usersListView.setItems(usersList);
            }
        }
    }
}