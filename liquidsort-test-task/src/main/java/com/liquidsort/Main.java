package com.liquidsort;

import com.liquidsort.core.model.State;
import com.liquidsort.core.model.Move;
import com.liquidsort.core.solver.BFSSolver;
import com.liquidsort.core.solver.DFSSolver;
import com.liquidsort.core.solver.Solver;
import com.liquidsort.ui.ConsoleAnimator;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("🚀 Liquid Sort Solver");
        System.out.println("=====================\n");

//        int[][] initialState = {
//                {1, 2, 3, 1},  // Tube 0
//                {2, 3, 1, 2},  // Tube 1
//                {3, 1, 2, 3},  // Tube 2
//                {0, 0, 0, 0},  // Tube 3 (empty)
//                {0, 0, 0, 0}   // Tube 4 (empty)
//        };

        int[][] initialState = {
                {1,2,3,4}, {4,1,2,3}, {3,4,1,2}, {2,3,4,1},
                {0,0,0,0}, {0,0,0,0}
        };

        System.out.println("Solving puzzle...");

        State state = new State(initialState, 4);
        Solver solver = createSolver();
        List<Move> solution = solver.solve(state);

        System.out.println("\nSolution moves:");
        for (int i = 0; i < solution.size(); i++) {
            System.out.print(solution.get(i));
            if ((i + 1) % 8 == 0) System.out.println();
        }
        System.out.println("\n");

        // Анимация если не указан флаг --no-animation
        if (args.length == 0 || !args[0].equals("--no-animation")) {
            System.out.println("Starting animation in 3 seconds...");
            TimeUnit.SECONDS.sleep(3);

            ConsoleAnimator animator = new ConsoleAnimator();
            animator.animateSolution(state, solution, 800);
        }
    }

    private static Solver createSolver() {
        String solverName = System.getProperty("solver", "bfs").toLowerCase().trim();

        switch (solverName) {
            case "bfs":
                System.out.println("BFSSSSSSSSSSS");
                return new BFSSolver();
            case "dfs":
                System.out.println("DFSSSSSSSSSS");
                return new DFSSolver();
            default:
                System.out.println("Неизвестный солвер: '" + solverName + "'");
                System.out.println("Доступные: bfs, dfs");
                System.out.println("Используется BFS по умолчанию");
                return new BFSSolver();
        }
    }
}