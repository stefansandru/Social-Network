package com.example.social_network.Validator;

public interface Validator<E> {
    void validate(E entity) throws ValidationException;
}