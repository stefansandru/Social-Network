package com.example.social_network;

import com.example.social_network.Repo.MessageRepo;
import com.example.social_network.Repo.dbFriendshipRepo;
import com.example.social_network.Repo.dbUserRepo;
import com.example.social_network.Service.SocialNetworkService;
import com.example.social_network.Validator.FriendshipValidator;
import com.example.social_network.Validator.UserValidator;
import com.example.social_network.domain.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
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
    private String mainProfilePhoto;
    private SocialNetworkService service;

    public MultipleMessage() {
        UserValidator userValidator = new UserValidator();
        FriendshipValidator friendshipValidator = new FriendshipValidator();

        String url = "jdbc:postgresql://localhost:5432/postgres";
        String user = "stefansandru";
        String password = "1234";
        String photosFolder = "/Users/stefansandru/Desktop";
        dbUserRepo userRepo = new dbUserRepo(userValidator, url, user, password, photosFolder);
        dbFriendshipRepo friendshipRepo = new dbFriendshipRepo(friendshipValidator, url, user, password, photosFolder);
        MessageRepo messageRepo = new MessageRepo(url, user, password, userRepo);

        this.service = new SocialNetworkService(userRepo, friendshipRepo, messageRepo);
    }

    @FXML
    public void initialize() {
        usersListView.setItems(usersList);
        selectedUsersListView.setItems(selectedUsers);
    }

    public void setUser(Long userId, String username, String profilePhoto) {
        this.mainUserId = userId;
        this.mainUsername = username;
        this.mainProfilePhoto = profilePhoto;
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
            Scene scene = new Scene(loader.load(), 950, 800);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("style.css")).toExternalForm());
            MainController controller = loader.getController();
            controller.setUser(mainUserId, mainUsername, mainProfilePhoto);
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
