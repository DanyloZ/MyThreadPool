package com.danylo.mythreadpool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;

public class MyThreadPool implements Executor {
    private Map<Worker, Thread> workers = new HashMap<>();
    private BlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();
    private volatile boolean isStopped;
    private volatile boolean isStoppedNow;

    private class Worker implements Runnable {
        private boolean isWaitingForTask;

        @Override
        public void run() {
            try {
                while (true) {
                    isWaitingForTask = true;
                    Runnable task = tasks.take();
                    isWaitingForTask = false;
                    task.run();
                    if (isStoppedNow || (isStopped && tasks.isEmpty())) {
                        break;
                    }
                }
            } catch (InterruptedException e) {
               //NOP
            }
        }
    }

    public MyThreadPool(int size) {
        for (int i = 0; i < size; i++) {
            Worker worker = new Worker();
            Thread thread = new Thread(worker);
            thread.start();
            workers.put(worker, thread);
        }
    }

    @Override
    public void execute(Runnable command) {
        if (isStopped) {
            throw new RuntimeException("ThreadPool is stopped");
        }
        tasks.add(command);
    }

    public void shutdown() {
        isStopped = true;
        for (Map.Entry<Worker, Thread> entry : workers.entrySet()) {
            if (entry.getKey().isWaitingForTask) {
                entry.getValue().interrupt();
            }
        }
    }

    public List<Runnable> shutdownNow() {
        isStoppedNow = true;
        for (Thread thread : workers.values()) {
            thread.interrupt();
        }
        List<Runnable> commands = new ArrayList<>();
        tasks.drainTo(commands);
        return commands;
    }


}
