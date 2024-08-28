import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class NQueensSequential {
    private static int numberOfQueens;
    private static int totalSolutions = 0;
    private static int solutionIndex = 0;
    private static final List<String> solutions = new ArrayList<>();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Digite o número de rainhas: ");
        while (true) {
            try {
                numberOfQueens = Integer.parseInt(scanner.nextLine());
                if (numberOfQueens <= 0) {
                    throw new IllegalArgumentException("O número de rainhas deve ser maior que zero");
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("Por favor, insira um número válido.");
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }

        sequencialSolution();
        scanner.close();
    }

    private static void sequencialSolution() {
        solutionIndex = 0;
        solutions.clear();

        long startTime = System.nanoTime();

        List<String> sequentialSolutions = solveSequential(numberOfQueens);

        long endTime = System.nanoTime();
        double executionTime = (endTime - startTime) / 1e6;

        if (sequentialSolutions.size() > 0) {
            System.out.println("Total de soluções: " + sequentialSolutions.size());
            System.out.printf("Tempo de execução total: %.2f ms%n", executionTime);
        } else {
            System.out.println("Não foram encontradas soluções.");
        }

        Scanner scanner = new Scanner(System.in);
        System.out.println("\nDeseja salvar as soluções sequenciais em um arquivo? (s/n): ");
        String response = scanner.nextLine().trim().toLowerCase();
        if (response.equals("s")) {
            saveSequentialSolutionsToFile(sequentialSolutions);
            System.out.println("Soluções salvas no arquivo 'solucoes_sequenciais.txt'.");
        }
    }

    private static List<String> solveSequential(int numberOfQueens) {
        List<String> solutions = new ArrayList<>();
        int[] positions = new int[numberOfQueens];
        solve(0, positions, solutions);
        return solutions;
    }

    private static void solve(int row, int[] positions, List<String> solutions) {
        if (row == numberOfQueens) {
            totalSolutions++;
            solutions.add(formatBoard(positions));
            return;
        }

        for (int column = 0; column < numberOfQueens; column++) {
            if (isSafe(row, column, positions)) {
                positions[row] = column;
                solve(row + 1, positions, solutions);
            }
        }
    }

    private static boolean isSafe(int row, int column, int[] positions) {
        for (int previousRow = 0; previousRow < row; previousRow++) {
            if (positions[previousRow] == column ||
                    Math.abs(positions[previousRow] - column) == Math.abs(previousRow - row)) {
                return false;
            }
        }
        return true;
    }

    private static String formatBoard(int[] positions) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n------------------\n");
        sb.append("Solução #").append(++solutionIndex).append(":\n");
        for (int row = 0; row < numberOfQueens; row++) {
            for (int column = 0; column < numberOfQueens; column++) {
                if (positions[row] == column) {
                    sb.append(" Q ");
                } else {
                    sb.append(" . ");
                }
            }
            sb.append("\n");
        }
        sb.append("------------------\n");
        return sb.toString();
    }

    private static void saveSequentialSolutionsToFile(List<String> solutions) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("solucoes_sequenciais.txt"))) {
            for (String solution : solutions) {
                writer.write(solution);
            }
        } catch (IOException e) {
            System.err.println("Erro ao salvar as soluções sequenciais em um arquivo: " + e.getMessage());
        }
    }
}
