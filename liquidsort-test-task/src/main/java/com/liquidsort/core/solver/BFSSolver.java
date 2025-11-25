package com.liquidsort.core.solver;

import com.liquidsort.core.model.State;
import com.liquidsort.core.model.Move;

import java.util.*;

public class BFSSolver implements Solver{

    public List<Move> solve(State initialState) {
        Queue<State> queue = new LinkedList<>();
        Map<State, List<Move>> path = new HashMap<>();
        Set<State> visited = new HashSet<>();

        queue.offer(initialState);
        visited.add(initialState);
        path.put(initialState, new ArrayList<>());

        int statesExplored = 0;
        int maxQueueSize = 0;

        while (!queue.isEmpty()) {
            maxQueueSize = Math.max(maxQueueSize, queue.size());
            State current = queue.poll();
            statesExplored++;

            if (current.isSolved()) {
                System.out.println("Solution found!");
                System.out.println("States explored: " + statesExplored);
                System.out.println("Max queue size: " + maxQueueSize);
                System.out.println("Solution length: " + path.get(current).size());
                return path.get(current);
            }

            for (Move move : current.getPossibleMoves()) {
                State nextState = current.copy();
                nextState.applyMove(move.getFrom(), move.getTo());

                if (!visited.contains(nextState)) {
                    visited.add(nextState);
                    List<Move> newPath = new ArrayList<>(path.get(current));
                    newPath.add(move);
                    path.put(nextState, newPath);
                    queue.offer(nextState);
                }
            }
        }

        throw new RuntimeException("No solution found after exploring " + statesExplored + " states");
    }
}
