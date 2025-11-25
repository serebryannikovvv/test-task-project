package com.liquidsort.core.model;

import java.util.Objects;

public class Move {
    private final int from;
    private final int to;

    public Move(int from, int to) {
        this.from = from;
        this.to = to;
    }

    public int getFrom() { return from; }
    public int getTo() { return to; }

    @Override
    public String toString() {
        return String.format("(%2d, %2d)", from, to);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Move move = (Move) o;
        return from == move.from && to == move.to;
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }
}
