package com.liquidsort.core.model;

import java.util.*;

public class State {
    private final List<Tube> tubes;
    private final int tubeCapacity;

    public State(int[][] initialState, int tubeCapacity) {
        this.tubes = new ArrayList<>();
        this.tubeCapacity = tubeCapacity;

        for (int[] tubeData : initialState) {
            Tube tube = new Tube(tubeCapacity);
            for (int color : tubeData) {
                if (color != 0) {
                    tube.addLiquid(color);
                }
            }
            tubes.add(tube);
        }
    }

    private State(List<Tube> tubes, int tubeCapacity) {
        this.tubes = new ArrayList<>();
        for (Tube tube : tubes) {
            this.tubes.add(new Tube(tube));
        }
        this.tubeCapacity = tubeCapacity;
    }

    public State copy() {
        return new State(tubes, tubeCapacity);
    }

    public boolean isValidMove(int from, int to) {
        if (from < 0 || from >= tubes.size() || to < 0 || to >= tubes.size()) {
            return false;
        }

        Tube fromTube = tubes.get(from);
        Tube toTube = tubes.get(to);

        if (fromTube.isEmpty()) return false;
        if (toTube.isFull()) return false;

        int topColor = fromTube.getTopColor();

        if (!toTube.isEmpty() && toTube.getTopColor() != topColor) {
            return false;
        }

        return true;
    }

    public void applyMove(int from, int to) {
        if (!isValidMove(from, to)) {
            throw new IllegalArgumentException("Invalid move: " + from + " -> " + to);
        }

        Tube fromTube = tubes.get(from);
        Tube toTube = tubes.get(to);

        int topColor = fromTube.getTopColor();
        int availableSpace = tubeCapacity - toTube.size();
        int transferCount = Math.min(fromTube.getTopSequenceLength(), availableSpace);

        for (int i = 0; i < transferCount; i++) {
            int liquid = fromTube.removeTop();
            toTube.addLiquid(liquid);
        }
    }

    public boolean isSolved() {
        for (Tube tube : tubes) {
            if (!tube.isEmpty()) {
                if (!tube.isFull() || !tube.isUniform()) {
                    return false;
                }
            }
        }
        return true;
    }

    public List<Move> getPossibleMoves() {
        List<Move> moves = new ArrayList<>();

        for (int from = 0; from < tubes.size(); from++) {
            for (int to = 0; to < tubes.size(); to++) {
                if (from != to && isValidMove(from, to)) {
                    moves.add(new Move(from, to));
                }
            }
        }

        return moves;
    }

    // Getters
    public Tube getTube(int index) {
        return tubes.get(index);
    }

    public int getTubeCount() {
        return tubes.size();
    }

    public int getTubeCapacity() {
        return tubeCapacity;
    }

    public Set<Integer> getAllColors() {
        Set<Integer> colors = new HashSet<>();
        for (Tube tube : tubes) {
            colors.addAll(tube.getColors());
        }
        colors.remove(0);
        return colors;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        State state = (State) o;
        return tubes.equals(state.tubes);
    }

    @Override
    public int hashCode() {
        return tubes.hashCode();
    }

    @Override
    public String toString() {
        return tubes.toString();
    }
}
