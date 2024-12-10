package com.example.lab8_messages.domain;

public class Entity<ID> {
    private ID id;

    public Entity() {
    }

    public ID getId() {
        return id;
    }

    public void setId(ID id) {
        this.id = id;
    }
}