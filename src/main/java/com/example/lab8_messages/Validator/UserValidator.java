package com.example.lab8_messages.Validator;

import com.example.lab8_messages.domain.User;

public class UserValidator implements Validator<User> {
    @Override
    public void validate(User user) throws ValidationException {
        if (user.getId() == null || user.getId() < 0) {
            throw new ValidationException("User ID must not be null or empty");
        }
        if (user.getName() == null || user.getName().isEmpty()) {
            throw new ValidationException("User name must not be null or empty");
        }
    }
}