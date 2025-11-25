package com.liquidsort.core.model;

import java.util.*;

public class Tube {
    private final List<Integer> liquids;
    private final int capacity;

    public Tube(int capacity) {
        this.capacity = capacity;
        this.liquids = new ArrayList<>();
    }

    public Tube(Tube other) {
        this.capacity = other.capacity;
        this.liquids = new ArrayList<>(other.liquids);
    }

    public void addLiquid(int color) {
        if (liquids.size() >= capacity) {
            throw new IllegalStateException("Tube is full");
        }
        liquids.add(color);
    }

    public int removeTop() {
        if (liquids.isEmpty()) {
            throw new IllegalStateException("Tube is empty");
        }
        return liquids.remove(liquids.size() - 1);
    }

    public boolean isEmpty() {
        return liquids.isEmpty();
    }

    public boolean isFull() {
        return liquids.size() >= capacity;
    }

    public boolean isUniform() {
        if (liquids.isEmpty()) return true;
        int firstColor = liquids.get(0);
        for (int color : liquids) {
            if (color != firstColor) return false;
        }
        return true;
    }

    public int getTopColor() {
        return liquids.isEmpty() ? 0 : liquids.get(liquids.size() - 1);
    }

    public int getTopSequenceLength() {
        if (liquids.isEmpty()) return 0;

        int topColor = getTopColor();
        int count = 0;

        for (int i = liquids.size() - 1; i >= 0; i--) {
            if (liquids.get(i) == topColor) {
                count++;
            } else {
                break;
            }
        }

        return count;
    }

    public int size() {
        return liquids.size();
    }

    public int getLiquid(int level) {
        if (level < 0 || level >= liquids.size()) {
            return 0;
        }
        return liquids.get(level);
    }

    public Set<Integer> getColors() {
        return new HashSet<>(liquids);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tube tube = (Tube) o;
        return liquids.equals(tube.liquids);
    }

    @Override
    public int hashCode() {
        return liquids.hashCode();
    }

    @Override
    public String toString() {
        return liquids.toString();
    }
}