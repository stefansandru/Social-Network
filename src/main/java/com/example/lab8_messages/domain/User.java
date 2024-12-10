package com.example.lab8_messages.domain;

public class User extends Entity<Long> {
    private final Long id;
    private final String name;

    public User(Long id, String name) {
        super();
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
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
