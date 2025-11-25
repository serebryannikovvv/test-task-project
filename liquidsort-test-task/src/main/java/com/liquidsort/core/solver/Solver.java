package com.liquidsort.core.solver;

import com.liquidsort.core.model.Move;
import com.liquidsort.core.model.State;

import java.util.List;

public interface Solver {
    List<Move> solve(State initialState);

    default String getName() {
        return this.getClass().getSimpleName();
    }
}
