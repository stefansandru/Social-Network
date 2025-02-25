package com.example.social_network.domain;

public class User extends Entity<Long> {
    private final Long id;
    private final String name;
    private final String password;
    private final String profileImagePath;

    public User(Long id, String name, String password, String profileImagePath) {
        super();
        this.id = id;
        this.name = name;
        this.password = password;
        this.profileImagePath = profileImagePath;

    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getProfileImagePath() {
        return profileImagePath;
    }

    @Override
    public String toString() {
        return "ID='" + id + "', Name='" + name + "'";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        User user = (User) obj;
        return id==user.getId() && name.equals(user.name);
    }
}
