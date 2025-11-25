package com.liquidsort.ui;

import com.liquidsort.core.model.State;
import com.liquidsort.core.model.Move;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class ConsoleAnimator {

    private static final Map<Integer, String> COLOR_MAP = createColorMap();

    private static Map<Integer, String> createColorMap() {
        Map<Integer, String> map = new HashMap<>();
        map.put(0, "\u001B[37m");  // White
        map.put(1, "\u001B[31m");  // Red
        map.put(2, "\u001B[34m");  // Blue
        map.put(3, "\u001B[32m");  // Green
        map.put(4, "\u001B[33m");  // Yellow
        map.put(5, "\u001B[35m");  // Purple
        map.put(6, "\u001B[36m");  // Cyan
        map.put(7, "\u001B[91m");  // Bright Red
        map.put(8, "\u001B[94m");  // Bright Blue
        return map;
    }

    private static final String RESET = "\u001B[0m";

    private String repeatString(String str, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    public void animateSolution(State initialState, List<Move> solution, int delayMillis)
            throws InterruptedException {

        State currentState = initialState;

        for (int moveIndex = 0; moveIndex < solution.size(); moveIndex++) {
            Move move = solution.get(moveIndex);
            clearConsole();

            printHeader(moveIndex, solution.size(), move);
            printState(currentState, move.getFrom(), move.getTo(),
                    String.format("Move %d/%d", moveIndex + 1, solution.size()));

            TimeUnit.MILLISECONDS.sleep(delayMillis);

            currentState = currentState.copy();
            currentState.applyMove(move.getFrom(), move.getTo());
        }

        clearConsole();
        printState(currentState, -1, -1, "SOLVED! 🎉");
        printStats(solution);
    }

    private void printHeader(int moveIndex, int totalMoves, Move move) {
        System.out.println("┌────────────────────────────────────────┐");
        System.out.println("│          LIQUID SORT SOLVER           │");
        System.out.println("└────────────────────────────────────────┘");
        System.out.printf("Move: %d/%d | From: %d → To: %d\n\n",
                moveIndex + 1, totalMoves, move.getFrom(), move.getTo());
    }

    public void printState(State state, int highlightFrom, int highlightTo, String title) {
        String horizontalLine = repeatString("═", 45);
        System.out.println("╔" + horizontalLine + "╗");
        System.out.printf("║ %-41s ║\n", title);
        System.out.println("╚" + horizontalLine + "╝");

        int tubeCapacity = state.getTubeCapacity();
        int numTubes = state.getTubeCount();

        System.out.print("   ");
        for (int i = 0; i < numTubes; i++) {
            String marker = " ";
            if (i == highlightFrom) marker = "↓";
            if (i == highlightTo) marker = "↑";
            System.out.printf(" %2d%s   ", i, marker);
        }
        System.out.println();

        for (int level = tubeCapacity - 1; level >= 0; level--) {
            System.out.print("   ");
            for (int tubeIdx = 0; tubeIdx < numTubes; tubeIdx++) {
                String tubeDisplay;

                if (level < state.getTube(tubeIdx).size()) {
                    int color = state.getTube(tubeIdx).getLiquid(level);
                    tubeDisplay = COLOR_MAP.getOrDefault(color, "") +
                            "│ " + getLiquidSymbol(color) + " │" + RESET;
                } else {
                    tubeDisplay = "│   │";
                }

                if (tubeIdx == highlightFrom || tubeIdx == highlightTo) {
                    tubeDisplay = "[" + tubeDisplay + "]";
                } else {
                    tubeDisplay = " " + tubeDisplay + " ";
                }

                System.out.print(tubeDisplay);
            }
            System.out.println();
        }

        System.out.print("   ");
        for (int i = 0; i < numTubes; i++) {
            System.out.print(" └───┘ ");
        }
        System.out.println("\n");
    }

    private String getLiquidSymbol(int color) {
        if (color == 0) return " ";
        String[] symbols = {"●", "◆", "▲", "■", "★", "♦", "♠", "♥"};
        return symbols[color % symbols.length];
    }

    private void printStats(List<Move> solution) {
        System.out.println("📊 SOLUTION STATISTICS:");
        System.out.println("├── Total moves: " + solution.size());
        System.out.println("├── Efficiency: " + calculateEfficiency(solution));
        System.out.println("└── Complexity: " + analyzeComplexity(solution));
    }

    private String calculateEfficiency(List<Move> solution) {
        if (solution.size() < 20) return "⭐ Excellent";
        if (solution.size() < 50) return "✅ Good";
        if (solution.size() < 100) return "⚠️  Average";
        return "🐌 Could be optimized";
    }

    private String analyzeComplexity(List<Move> solution) {
        long backAndForth = solution.stream()
                .filter(move -> Math.abs(move.getFrom() - move.getTo()) == 1)
                .count();
        double ratio = (double) backAndForth / solution.size();

        if (ratio > 0.3) return "High (many back-and-forth moves)";
        if (ratio > 0.15) return "Medium";
        return "Low (efficient path)";
    }

    private void clearConsole() {
        try {
            final String os = System.getProperty("os.name");
            if (os.contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
    }
}