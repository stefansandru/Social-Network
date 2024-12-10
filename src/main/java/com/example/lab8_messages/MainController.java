package com.example.lab8_messages;

import com.example.lab8_messages.Repo.MessageRepo;
import com.example.lab8_messages.Repo.dbFriendshipRepo;
import com.example.lab8_messages.Repo.dbUserRepo;
import com.example.lab8_messages.Service.SocialNetworkService;
import com.example.lab8_messages.Validator.FriendshipValidator;
import com.example.lab8_messages.Validator.UserValidator;
import com.example.lab8_messages.domain.Constants;
import com.example.lab8_messages.domain.User;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class MainController {

    private Long userId;
    private String username;
    private final SocialNetworkService service;
    private ObservableMap<User, LocalDateTime> friendsList = FXCollections.observableHashMap();
    private ObservableList<User> notFriendsList = FXCollections.observableArrayList();
    private ObservableMap<User, LocalDateTime> pendingFriendshipsList = FXCollections.observableHashMap();
    private ContextMenu currentContextMenu;

    @FXML
    private Label welcomeUserLabel;

    @FXML
    private ListView<User> friendsListView;

    @FXML
    private ListView<User> searchResultsListView;

    @FXML
    private ListView<User> pendingFriendshipsListView;

    @FXML
    private TextField searchField;

    @FXML
    private Label notificationLabel;


    public MainController() {
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

    public void setUser(Long userId, String username) {
        this.userId = userId;
        this.username = username;
        loadFriends();
        loadNotFriends();
        loadPendingFriendships();

        int pendingRequests = pendingFriendshipsList.size();
        if (pendingRequests > 0) {
            showNotification("You have " + pendingRequests + " pending friend requests!");
        }

        welcomeUserLabel.setText("Welcome, " + username + "! Click on one user to enable the menu.");
    }

    @FXML
    private void handleGlobalClick(MouseEvent event) {
        Object source = event.getTarget();

        if (!(source instanceof ListView)) {
            friendsListView.getSelectionModel().clearSelection();
            searchResultsListView.getSelectionModel().clearSelection();
            pendingFriendshipsListView.getSelectionModel().clearSelection();

            if (currentContextMenu != null) {
                currentContextMenu.hide();
                currentContextMenu = null;
            }
        }
    }

    private void loadFriends() {
        friendsList.clear();
        friendsList.putAll(service.getFriends(userId));
        friendsListView.setItems(FXCollections.observableArrayList(friendsList.keySet()));
        friendsListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                } else {
                    setText(user.getName() + " - " + friendsList.get(user).toString());
                }
            }
        });
    }

    private void loadNotFriends() {
        notFriendsList.setAll(service.getNotFriends(userId));
        searchResultsListView.setItems(notFriendsList);
    }

    private void loadPendingFriendships() {
        pendingFriendshipsList.clear();
        pendingFriendshipsList.putAll(service.getPendingFriendships(userId));
        pendingFriendshipsListView.setItems(FXCollections.observableArrayList(pendingFriendshipsList.keySet()));
        pendingFriendshipsListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                } else {
                    setText(user.getName() + " - " + pendingFriendshipsList.get(user).toString());
                }
            }
        });
    }

    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase();
        List<User> filteredList = service.findNotFriendsByPrefix(searchText, userId);
        notFriendsList.setAll(filteredList);
        searchResultsListView.setItems(notFriendsList);
    }

    private void showContextMenu(ListView<User> listView, MouseEvent event, String... actions) {
        if (currentContextMenu != null) {
            currentContextMenu.hide();
        }
        ContextMenu contextMenu = new ContextMenu();
        for (String action : actions) {
            MenuItem menuItem = new MenuItem(action);
            menuItem.setOnAction(e -> handleAction(action, listView.getSelectionModel().getSelectedItem()));
            contextMenu.getItems().add(menuItem);
        }
        contextMenu.show(listView, event.getScreenX(), event.getScreenY());
        currentContextMenu = contextMenu;
    }

    private void handleAction(String action, User selectedItem) {
        switch (action) {
            case "Add User":
                handleAddUser(selectedItem);
                break;
            case "Accept":
                handleAcceptFriendship(selectedItem);
                break;
            case "Reject":
                handleRejectFriendship(selectedItem);
                break;
            case "Unfriend":
                handleUnfriend(selectedItem);
                break;
            case "Chat":
                handleChat(selectedItem);
                break;
        }
    }

    @FXML
    private void handleSearchResultsClick(MouseEvent event) {
        if (event.getClickCount() == 1) {
            showContextMenu(searchResultsListView, event, "Chat", "Add User");
        }
    }

    @FXML
    private void handlePendingFriendshipsClick(MouseEvent event) {
        if (event.getClickCount() == 1) {
            showContextMenu(pendingFriendshipsListView, event, "Chat", "Accept", "Reject");
        }
    }

    @FXML
    private void handleFriendsListClick(MouseEvent event) {
        if (event.getClickCount() == 1) {
            showContextMenu(friendsListView, event, "Chat", "Unfriend");
        }
    }

    private void handleUnfriend(User selectedFriend) {
        if (selectedFriend != null) {
            service.removeFriendship(userId, selectedFriend.getId());
            loadFriends();
            loadNotFriends();
            loadPendingFriendships();
        }
    }

    private void handleAddUser(User selectedUser) {
        if (selectedUser != null) {
            service.addFriendship(userId, selectedUser.getId(), Constants.PENDING + selectedUser.getId());
            loadFriends();
            loadNotFriends();
            loadPendingFriendships();
        }
    }

    private void handleAcceptFriendship(User user) {
        if (user != null && !user.getId().equals(userId)) {
            service.updateFriendshipStatus(userId, user.getId(), Constants.ACTIVE);
            loadFriends();
            loadNotFriends();
            loadPendingFriendships();
        }
    }

    private void handleRejectFriendship(User user) {
        if (user != null) {
            service.removeFriendship(userId, user.getId());
            loadFriends();
            loadNotFriends();
            loadPendingFriendships();
        }
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login-view.fxml"));
            Stage stage = (Stage) friendsListView.getScene().getWindow();
            Scene scene = new Scene(loader.load(), 500, 500);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("style.css")).toExternalForm());
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleChat(User selectedUser) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/lab8_messages/chat-view.fxml"));
            Stage stage = (Stage) friendsListView.getScene().getWindow();
            Scene scene = new Scene(loader.load(), 500, 600);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/example/lab8_messages/style.css")).toExternalForm());
            ChatController controller = loader.getController();
            controller.setUsers(userId, username, selectedUser.getId(), selectedUser.getName());
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleMultipleMessage() {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/lab8_messages/multiple-message.fxml"));
        Stage stage = (Stage) friendsListView.getScene().getWindow();
        Scene scene = new Scene(loader.load(), 500, 600);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/example/lab8_messages/style.css")).toExternalForm());
        MultipleMessage controller = loader.getController();
        controller.setUser(userId, username);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    } catch (IOException e) {
        e.printStackTrace();
    }
    }

    @FXML
    private void showNotification(String message) {
        notificationLabel.setText(message);
        notificationLabel.setVisible(true);

        Timeline timeline = new Timeline(new KeyFrame(
                Duration.seconds(6),
                event -> notificationLabel.setVisible(false)
        ));
        timeline.setCycleCount(1);
        timeline.play();
    }
}