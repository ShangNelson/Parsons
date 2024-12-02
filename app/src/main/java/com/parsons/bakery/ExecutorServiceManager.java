package com.parsons.bakery;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorServiceManager {
    // Use a cached thread pool to handle background tasks
    private static final ExecutorService executor = Executors.newCachedThreadPool();

    private ExecutorServiceManager() {
        // Prevent instantiation
    }

    public static ExecutorService getExecutor() {
        return executor;
    }

    // Call this to shutdown the executor when it's no longer needed (e.g., at app shutdown)
    public static void shutdownExecutor() {
        if (!executor.isShutdown()) {
            executor.shutdown();
        }
    }
}

