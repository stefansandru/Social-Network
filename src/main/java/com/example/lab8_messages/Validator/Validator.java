package com.example.lab8_messages.Validator;

public interface Validator<E> {
    void validate(E entity) throws ValidationException;
}