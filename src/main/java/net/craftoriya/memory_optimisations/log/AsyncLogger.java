package net.craftoriya.memory_optimisations.log;

import java.util.concurrent.*;

public class AsyncLogger {
    private static final BlockingQueue<String> logQueue = new LinkedBlockingQueue<>();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private static final AsyncLogger INSTANCE = new AsyncLogger();



    static {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                while (!logQueue.isEmpty()) {
                    String log = logQueue.poll();
                    if (log != null) {
                        System.out.println(log);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error during log processing: " + e.getMessage());
            }
        }, 0, 50, TimeUnit.MILLISECONDS);
    }

    // Method to log messages
    public static void log(String message) {
        CompletableFuture.runAsync(() -> {
            try {
                logQueue.put(message);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                CompletableFuture.runAsync(() -> {
                    try {
                        logQueue.put(message);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        System.err.println("Failed to log message after retry: " + message);
                    }
                });
            }
        });
    }

    public static AsyncLogger getInstance() {
        return INSTANCE;
    }
}
