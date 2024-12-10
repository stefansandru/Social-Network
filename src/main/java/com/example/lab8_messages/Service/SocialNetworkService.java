package com.example.lab8_messages.Service;

import com.example.lab8_messages.Repo.MessageRepo;
import com.example.lab8_messages.Repo.dbFriendshipRepo;
import com.example.lab8_messages.Repo.dbUserRepo;
import com.example.lab8_messages.domain.Friendship;
import com.example.lab8_messages.domain.Message;
import com.example.lab8_messages.domain.Tuple;
import com.example.lab8_messages.domain.User;

import java.sql.SQLException;
import java.util.*;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class SocialNetworkService {
    private final dbUserRepo userRepo;
    private final dbFriendshipRepo friendshipRepo;
    private final MessageRepo messageRepo;


    public SocialNetworkService(dbUserRepo userRepo, dbFriendshipRepo friendshipRepo, MessageRepo messageRepo) {
        this.userRepo  = userRepo;
        this.friendshipRepo = friendshipRepo;
        this.messageRepo = messageRepo;
    }

    public void addFriendship(Long userId1, Long userId2, String Status) {
        try {
            Friendship friendship = new Friendship(userId1, userId2, LocalDateTime.now(), Status);
            friendshipRepo.save(friendship);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void removeFriendship(Long userId1, Long userId2) {
        try {
            friendshipRepo.delete(new Tuple<>(userId1, userId2));
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public List<User> findNotFriendsByPrefix(String prefix, Long userID) {
        return friendshipRepo.findNotFriendsByPrefix(prefix, userID);
    }

    public List<User> findUsersByPrefix(String prefix, Long userID) {
        List<User> users = new ArrayList<>();
        userRepo.findUsersByPrefix(prefix).forEach(users::add);
        users.removeIf(user -> user.getId().equals(userID));
        return users;
    }

   public List<User> getUsers(Long userId) {
        List<User> users = new ArrayList<>();
        userRepo.findAll().forEach(users::add);
        users.removeIf(user -> user.getId().equals(userId));
        return users;
}

    public List<User> getNotFriends(Long userId) {
        return friendshipRepo.getNotFriendsRepository(userId);
    }

    public Map<User, LocalDateTime> getFriends(Long userId) {
        return friendshipRepo.getFriendsOfUser(userId);
    }

    public Map<User, LocalDateTime> getPendingFriendships(Long userId) {
        return friendshipRepo.getPendingFriendships(userId);
    }

    public boolean findUser(Long userId, String username) {
        User foundUser = userRepo.findOne(userId).orElse(null);
        assert foundUser != null;
        if (Objects.equals(foundUser.getName(), username)) {
            return true;
        }
        return false;
    }

    public void updateFriendshipStatus(Long idFriend1, Long idFriend2, String active) {
        friendshipRepo.update(new Friendship(idFriend1, idFriend2, LocalDateTime.now(), active));
    }

    public void sendMessage(Long from, List<Long> to, String message, Long reply) {
        try {
            User userFrom = userRepo.findOne(from).orElse(null);
            List<User> usersTo = new ArrayList<>();
            to.forEach(id -> usersTo.add(userRepo.findOne(id).orElse(null)));
            Message replyMessage = messageRepo.findOne(reply).orElse(null);
            messageRepo.save(new Message(null, userFrom, usersTo, message, LocalDateTime.now().withNano(0), replyMessage));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Message> getChat(Long from, Long to) {
        // Get chat sorted by date - oldest first
        List<Message> messages = (List<Message>) messageRepo.findChat(from, to);
        messages.sort(Comparator.comparing(Message::getDate));
        return messages;
    }
}
