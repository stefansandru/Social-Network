package com.example.lab8_messages;

import com.example.lab8_messages.Repo.MessageRepo;
import com.example.lab8_messages.Repo.dbFriendshipRepo;
import com.example.lab8_messages.Repo.dbUserRepo;
import com.example.lab8_messages.Service.SocialNetworkService;
import com.example.lab8_messages.Validator.FriendshipValidator;
import com.example.lab8_messages.Validator.UserValidator;
import com.example.lab8_messages.domain.Message;
import com.example.lab8_messages.domain.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class ChatController {

    @FXML
    private Label chatWithLabel;

    @FXML
    private ObservableList<Message> messagesList = FXCollections.observableArrayList();

    @FXML
    private TextField messageField;

    @FXML
    private ListView<Message> chatListView;

    private Long mainUserId;
    private String mainUsername;
    private Long selectedUserId;
    private String selectedUsername;
    private SocialNetworkService service;
    private Message selectedMessage;

    public ChatController() {
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

    public void setUsers(Long mainUserId, String mainUsername, Long selectedUserId, String selectedUsername) {
        this.mainUserId = mainUserId;
        this.mainUsername = mainUsername;
        this.selectedUserId = selectedUserId;
        this.selectedUsername = selectedUsername;
        initializeChat();
    }

    private void loadMessages() {
        messagesList.clear();
        List<Message> messages = service.getChat(mainUserId, selectedUserId);
        messages.sort(Comparator.comparing(Message::getDate)); // Sort messages by date

        messagesList.addAll(messages);
        chatListView.setItems(messagesList);
        chatListView.setCellFactory(param -> new ListCell<Message>() {
            @Override
            protected void updateItem(Message message, boolean empty) {
                super.updateItem(message, empty);
                if (empty || message == null) {
                    setText(null);
                } else {
                    String displayText = "- " + message.getFrom().getName() + " - \n" + message.getMessage();
                    if (message.getReply() != null) {
                        displayText = "- " + message.getFrom().getName() + " - \n" +
                                "(Reply to " + message.getReply().getFrom().getName() + ": " + message.getReply().getMessage() + ")\n" +
                                message.getMessage();
                    }
                    setText(displayText);
                }
            }
        });
    }

    public void handleBackButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("main-view.fxml"));
            Stage stage = (Stage) chatListView.getScene().getWindow();
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

    private void initializeChat() {
        chatWithLabel.setText("Chat with " + selectedUsername);
        loadMessages();
    }

    @FXML
    private void handleSendMessage() {
        String message = messageField.getText();
        if (!message.isEmpty()) {
            Long reply = selectedMessage != null ? selectedMessage.getId() : null;
            service.sendMessage(mainUserId, List.of(this.selectedUserId), message, reply);
            messageField.clear();
            loadMessages();
            selectedMessage = null;
        }
    }

    @FXML
    private void handleChatListClick() {
        Message clickedMessage = chatListView.getSelectionModel().getSelectedItem();
        if (clickedMessage != null) {
            if (clickedMessage.equals(selectedMessage)) {
                selectedMessage = null;
                chatListView.getSelectionModel().clearSelection();
            } else {
                selectedMessage = clickedMessage;
            }
        }
    }
}