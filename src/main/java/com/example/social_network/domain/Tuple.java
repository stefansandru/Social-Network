package com.example.social_network.domain;

import java.util.Objects;

public class Tuple<L, R> {
    private final L left;
    private final R right;

    public Tuple(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public L getLeft() {
        return left;
    }

    public R getRight() {
        return right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tuple<?, ?> tuple = (Tuple<?, ?>) o;
        return Objects.equals(left, tuple.left) && Objects.equals(right, tuple.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }

    @Override
    public String toString() {
        return "Tuple{" + "left=" + left + ", right=" + right + '}';
    }
}