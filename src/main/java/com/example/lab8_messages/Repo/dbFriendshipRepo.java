package com.example.lab8_messages.Repo;

import com.example.lab8_messages.Validator.FriendshipValidator;
import com.example.lab8_messages.domain.Constants;
import com.example.lab8_messages.domain.Friendship;
import com.example.lab8_messages.domain.Tuple;
import com.example.lab8_messages.domain.User;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.StreamSupport;

public class dbFriendshipRepo implements Repository<Tuple<Long, Long>, Friendship> {
    private final String url;
    private final String user;
    private final String password;
    FriendshipValidator friendshipValidator;

    public dbFriendshipRepo(FriendshipValidator validator, String url, String user, String password) {
        this.friendshipValidator = validator;
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public Optional<Friendship> findOne(@NotNull Tuple<Long, Long> ID) {
        String query = "SELECT * FROM Friendships WHERE ID1 = ? AND ID2 = ?";
        Friendship friendship = null;
        try (Connection connection = DriverManager.getConnection(url, this.user, password);
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setLong(1, ID.getLeft());
            statement.setLong(2, ID.getRight());
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Long idFriend1 = resultSet.getLong("ID1");
                Long idFriend2 = resultSet.getLong("ID2");
                Timestamp date = resultSet.getTimestamp("F_DATE");
                LocalDateTime localDateTime = date.toLocalDateTime();
                String status = resultSet.getString("STATUS");
                friendship = new Friendship(idFriend1, idFriend2, localDateTime, status);
                friendship.setId(new Tuple<>(idFriend1, idFriend2));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.ofNullable(friendship);
    }

    @Override
    public Iterable<Friendship> findAll() {
        Map<Tuple<Long, Long>, Friendship> friendships = new HashMap<>();
        try (Connection connection = DriverManager.getConnection(url, this.user, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM Friendships");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Long idFriend1 = resultSet.getLong("ID1");
                Long idFriend2 = resultSet.getLong("ID2");
                Timestamp date = resultSet.getTimestamp("F_DATE");
                LocalDateTime localDateTime = date.toLocalDateTime();
                String status = resultSet.getString("STATUS");
                Friendship friendship = new Friendship(idFriend1, idFriend2, localDateTime, status);
                friendship.setId(new Tuple<>(idFriend1, idFriend2));
                friendships.put(friendship.getId(), friendship);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return friendships.values();
    }

    @Override
    public Optional<Friendship> save(Friendship entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Friendship can't be null!");
        }
        friendshipValidator.validate(entity);
        String query = "INSERT INTO Friendships(ID1, ID2, F_DATE, STATUS) VALUES (?,?,?,?)";

        try (Connection connection = DriverManager.getConnection(url, this.user, password);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, entity.getId().getLeft());
            statement.setLong(2, entity.getId().getRight());
            statement.setTimestamp(3, java.sql.Timestamp.valueOf(entity.getDate()));
            statement.setString(4, entity.getStatus());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return Optional.of(entity);
    }

    @Override
    public Optional<Friendship> delete(Tuple<Long, Long> ID) {
        String query = "DELETE FROM Friendships WHERE ID1 = ? AND ID2 = ? OR ID1 = ? AND ID2 = ?";
        Friendship friendshipToDelete = StreamSupport.stream(findAll().spliterator(), false)
                .filter(user -> Objects.equals(user.getId(), ID))
                .findFirst()
                .orElse(null);
        try (Connection connection = DriverManager.getConnection(url, this.user, password);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, ID.getLeft());
            statement.setLong(2, ID.getRight());
            statement.setLong(3, ID.getRight());
            statement.setLong(4, ID.getLeft());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.ofNullable(friendshipToDelete);
    }

    @Override
    public Optional<Friendship> update(Friendship entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Friendship can't be null!");
        }
        friendshipValidator.validate(entity);
        String query = "UPDATE Friendships SET F_DATE = ?, STATUS = ? WHERE ID1 = ? AND ID2 = ? OR ID1 = ? AND ID2 = ?";

        try (Connection connection = DriverManager.getConnection(url, this.user, password);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setTimestamp(1, java.sql.Timestamp.valueOf(entity.getDate()));
            statement.setString(2, entity.getStatus());
            statement.setLong(3, entity.getId().getLeft());
            statement.setLong(4, entity.getId().getRight());
            statement.setLong(5, entity.getId().getRight());
            statement.setLong(6, entity.getId().getLeft());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return Optional.of(entity);
    }

   public List<User> findNotFriendsByPrefix(String prefix, Long userId) {
    String sql = "SELECT * FROM users WHERE name LIKE ? AND id NOT IN (" +
                 "SELECT id1 FROM friendships WHERE id2 = ? " +
                 "UNION " +
                 "SELECT id2 FROM friendships WHERE id1 = ?)" +
                 "AND id <> ?";
    List<User> users = new ArrayList<>();
    try (Connection conn = DriverManager.getConnection(url, this.user, password);
         PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
        preparedStatement.setString(1, prefix + "%");
        preparedStatement.setLong(2, userId);
        preparedStatement.setLong(3, userId);
        preparedStatement.setLong(4, userId);
        ResultSet rs = preparedStatement.executeQuery();
        while (rs.next()) {
            Long id = rs.getLong("id");
            String name = rs.getString("name");
            users.add(new User(id, name));
        }
    } catch (SQLException e) {
        System.out.println(e.getMessage());
    }
    return users;
}

    public List<User> getNotFriendsRepository(Long userId) {
        String sql = "SELECT * FROM users WHERE id NOT IN " +
                "(SELECT id1 FROM friendships WHERE id2 = ? " +
                "UNION SELECT id2 FROM friendships WHERE id1 = ?)" +
                "AND id <> ?";
        List<User> notFriends = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(url, this.user, password);
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setLong(1, userId);
            preparedStatement.setLong(2, userId);
            preparedStatement.setLong(3, userId);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                Long id = rs.getLong("id");
                String name = rs.getString("name");
                notFriends.add(new User(id, name));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return notFriends;
    }

    public Map<User, LocalDateTime> getPendingFriendships(Long userId) {
    String sql = "SELECT u.id, u.name, f.f_date FROM users u JOIN friendships f ON (u.id = f.id1 OR u.id = f.id2) WHERE (f.id1 = ? OR f.id2 = ?) AND f.status = ? AND u.id <> ?";
    Map<User, LocalDateTime> pendingFriendships = new HashMap<>();
    try (Connection conn = DriverManager.getConnection(url, this.user, password);
         PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
        String pendingStatus = Constants.PENDING + userId;
        preparedStatement.setLong(1, userId);
        preparedStatement.setLong(2, userId);
        preparedStatement.setString(3, pendingStatus);
        preparedStatement.setLong(4, userId);
        ResultSet rs = preparedStatement.executeQuery();
        while (rs.next()) {
            Long id = rs.getLong("id");
            String name = rs.getString("name");
            LocalDateTime date = rs.getTimestamp("f_date").toLocalDateTime();
            pendingFriendships.put(new User(id, name), date);
        }
    } catch (SQLException e) {
        System.out.println(e.getMessage());
    }
    return pendingFriendships;
}

    public Map<User, LocalDateTime> getFriendsOfUser(Long userID) {
        String sql = "SELECT u.id, u.name, f.f_date FROM users u JOIN friendships f ON (u.id = f.id1 OR u.id = f.id2) WHERE (f.id1 = ? OR f.id2 = ?) AND f.status = 'active' AND u.id <> ?";
        Map<User, LocalDateTime> friends = new HashMap<>();
        try (Connection conn = DriverManager.getConnection(url, this.user, password);
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setLong(1, userID);
            preparedStatement.setLong(2, userID);
            preparedStatement.setLong(3, userID);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                Long id = rs.getLong("id");
                String name = rs.getString("name");
                LocalDateTime date = rs.getTimestamp("f_date").toLocalDateTime();
                friends.put(new User(id, name), date);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return friends;
    }

    private int count(Long id1) throws SQLException {
        String sql = "select count(*) as count from friendships where id1 = ? or id2 = ? and status = 'active'";
        try (Connection connection = DriverManager.getConnection(url, this.user, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id1);
            statement.setLong(2, id1);
            try (ResultSet result = statement.executeQuery()) {
                int totalNumberOfFriends = 0;
                if (result.next()) {
                    totalNumberOfFriends = result.getInt("count");
                }
                return totalNumberOfFriends;
            }
        }
    }
}