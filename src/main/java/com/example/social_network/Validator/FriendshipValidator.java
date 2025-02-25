package com.example.social_network.Validator;

import com.example.social_network.domain.Friendship;

public class FriendshipValidator implements Validator<Friendship> {
    @Override
    public void validate(Friendship friendship) throws ValidationException {
        if (friendship.getId() == null) {
            throw new ValidationException("Friendship ID must not be null");
        }
        if (friendship.getId().getLeft() == null || friendship.getId().getRight() == null) {
            throw new ValidationException("Both friend IDs in the friendship must not be null");
        }
        if (friendship.getDate() == null) {
            throw new ValidationException("Friendship date must not be null");
        }
        if (friendship.getId().getLeft().equals(friendship.getId().getRight())) {
            throw new ValidationException("A friendship cannot be established between the same user");
        }
    }
}