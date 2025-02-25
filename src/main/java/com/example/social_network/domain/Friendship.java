package com.example.social_network.domain;

import java.time.LocalDateTime;
import java.util.Objects;

public class Friendship extends Entity<Tuple<Long, Long>> {
    private final Long idFriend1;
    private final Long idFriend2;
    private final LocalDateTime date;
    private String status;

    public Friendship(Long idFriend1, Long idFriend2, LocalDateTime date, String status) {
        super();
        this.idFriend1 = idFriend1;
        this.idFriend2 = idFriend2;
        this.date = date;
        this.status = status;
        this.setId(new Tuple<>(idFriend1, idFriend2));
    }

    public enum FriendshipStatus {
        PENDING, ACCEPTED
    }

    public Long getIdFriend1() {
        return idFriend1;
    }

    public Long getIdFriend2() {
        return idFriend2;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Friendship between ID1='" + idFriend1 + "' and ID2='" + idFriend2 + "', Date='" + date + "', Status='" + status + "'";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Friendship that = (Friendship) obj;
        return Objects.equals(idFriend1, that.idFriend1) &&
                Objects.equals(idFriend2, that.idFriend2) &&
                Objects.equals(date, that.date) &&
                Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idFriend1, idFriend2, date, status);
    }
}