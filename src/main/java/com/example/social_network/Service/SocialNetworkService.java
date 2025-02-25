package com.example.social_network.Service;

import com.example.social_network.Repo.MessageRepo;
import com.example.social_network.Repo.dbFriendshipRepo;
import com.example.social_network.Repo.dbUserRepo;
import com.example.social_network.domain.Friendship;
import com.example.social_network.domain.Message;
import com.example.social_network.domain.Tuple;
import com.example.social_network.domain.User;
import com.example.social_network.paging.Page;
import com.example.social_network.paging.Pageable;

import java.util.*;
import java.time.LocalDateTime;

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
        List<User > notfriends = friendshipRepo.getNotFriendsRepository(userId);
        System.out.println("Not friends service:");
        for (User user : notfriends) {
            System.out.println(user);
        }
        return notfriends;
    }

    public Map<User, LocalDateTime> getPendingFriendships(Long userId) {
        return friendshipRepo.getPendingFriendships(userId);
    }

    public User findUser(Long userId) {
        return userRepo.findOne(userId).orElse(null);
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

    public Page<User> findAllOnPage(Pageable pageable, Long id)  {
        return friendshipRepo.findFriendsOnPage(pageable, id);
    }

    public int getNumberOfFriends(Long id) {
        return friendshipRepo.getNumberOfFriends(id);
    }

    public int getTotalMessagesSent(Long id) {
        return messageRepo.getTotalMessagesSent(id);
    }
}
