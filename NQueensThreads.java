import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class NQueensThreads {
    private static int numberOfQueens;
    private static final AtomicInteger totalSolutions = new AtomicInteger(0);
    private static final Set<String> uniqueSolutions = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static final List<ThreadStatistics> threadStats = new ArrayList<>();

    public static void main(String[] args) throws InterruptedException {
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
                System.out.println(e.getMessage());
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }

        threadsSolution();
        scanner.close();
    }

    private static void threadsSolution() throws InterruptedException {
        long startTime = System.nanoTime();

        int availableProcessors = Runtime.getRuntime().availableProcessors();
        ExecutorService threadPool = Executors.newFixedThreadPool(availableProcessors);
        List<Future<ThreadStatistics>> futures = new ArrayList<>();

        for (int i = 0; i < availableProcessors; i++) {
            final int threadId = i;
            ThreadStatistics stats = new ThreadStatistics();
            threadStats.add(stats);

            Callable<ThreadStatistics> task = () -> {
                long taskStartTime = System.nanoTime();
                int startColumn = threadId;
                for (int col = startColumn; col < numberOfQueens; col += availableProcessors) {
                    solveFromRow(0, col, stats);
                }
                stats.setExecutionTime(System.nanoTime() - taskStartTime);
                return stats;
            };

            futures.add(threadPool.submit(task));
        }

        threadPool.shutdown();
        threadPool.awaitTermination(1, TimeUnit.HOURS);

        long endTime = System.nanoTime();
        double executionTime = (endTime - startTime) / 1e6;

        System.out.println("Total de soluções: " + totalSolutions.get());
        System.out.printf("Tempo de execução total: %.2f ms%n", executionTime);

        printThreadStatistics(futures);

        Scanner scanner = new Scanner(System.in);
        System.out.println("\nDeseja salvar as soluções sequenciais em um arquivo? (s/n): ");
        String response = scanner.nextLine().trim().toLowerCase();
        if (response.equals("s")) {
            saveSolutionsToFile(uniqueSolutions);
            System.out.println("Soluções salvas no arquivo 'solucoes_threads.txt'.");
        }
    }

    private static void solveFromRow(int row, int startColumn, ThreadStatistics stats) {
        int[] positions = new int[numberOfQueens];
        positions[row] = startColumn;
        solve(row + 1, positions, stats);
    }

    private static void solve(int row, int[] positions, ThreadStatistics stats) {
        if (row == numberOfQueens) {
            String solution = formatBoard(positions);
            if (uniqueSolutions.add(solution)) {
                totalSolutions.incrementAndGet();
                stats.incrementSolutionCount();
            }
            return;
        }

        for (int column = 0; column < numberOfQueens; column++) {
            if (isSafe(row, column, positions)) {
                positions[row] = column;
                solve(row + 1, positions, stats);
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
        for (int row : positions) {
            for (int i = 0; i < numberOfQueens; i++) {
                if (i == row) {
                    sb.append(" Q ");
                } else {
                    sb.append(" . ");
                }
            }
            sb.append("\n");
        }
        sb.append("\n");
        return sb.toString();
    }

    private static void saveSolutionsToFile(Set<String> solutions) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("solucoes_threads.txt"))) {
            for (String solution : solutions) {
                writer.write(solution);
            }
        } catch (IOException e) {
            System.err.println("Erro ao salvar as soluções sequenciais em um arquivo: " + e.getMessage());
        }
    }

    private static void printThreadStatistics(List<Future<ThreadStatistics>> futures) {
        System.out.println("+-------+------------------+----------------+");
        System.out.println("| Nº    | Tempo (ms)       | Soluções       |");
        System.out.println("+-------+------------------+----------------+");

        int threadNumber = 1;
        for (Future<ThreadStatistics> future : futures) {
            try {
                ThreadStatistics stats = future.get();
                System.out.printf("| %-5d | %-16.2f | %-14d |%n",
                        threadNumber++,
                        stats.getExecutionTime() / 1e6,
                        stats.getSolutionCount());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        System.out.println("+-------+------------------+----------------+");
    }

    private static class ThreadStatistics {
        private final AtomicInteger solutionCount = new AtomicInteger(0);
        private long executionTime;

        public int getSolutionCount() {
            return solutionCount.get();
        }

        public void incrementSolutionCount() {
            solutionCount.incrementAndGet();
        }

        public long getExecutionTime() {
            return executionTime;
        }

        public void setExecutionTime(long executionTime) {
            this.executionTime = executionTime;
        }
    }
}
