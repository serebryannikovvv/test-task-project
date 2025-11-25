package com.liquidsort.core.solver;

import com.liquidsort.core.model.Move;
import com.liquidsort.core.model.State;

import java.util.*;

public class DFSSolver implements Solver {

    @Override
    public List<Move> solve(State initialState) {
        Stack<StateWithPath> stack = new Stack<>();
        Set<State> visited = new HashSet<>();

        stack.push(new StateWithPath(initialState.copy(), new ArrayList<>()));

        while (!stack.isEmpty()) {
            StateWithPath current = stack.pop();
            State state = current.state;

            if (state.isSolved()) {
                System.out.println("DFS нашёл решение за " + current.path.size() + " ходов");
                return current.path;
            }

            // Проверяем visited ПОСЛЕ извлечения из стека (важно для DFS!)
            if (!visited.add(state)) {
                continue; // уже были здесь
            }

            for (Move move : state.getPossibleMoves()) {
                State next = state.copy();
                next.applyMove(move.getFrom(), move.getTo());

                List<Move> newPath = new ArrayList<>(current.path);
                newPath.add(move);

                stack.push(new StateWithPath(next, newPath));
            }
        }

        throw new RuntimeException("DFS: решение не найдено");
    }

    private static class StateWithPath {
        final State state;
        final List<Move> path;

        StateWithPath(State state, List<Move> path) {
            this.state = state;
            this.path = path;
        }
    }
}
