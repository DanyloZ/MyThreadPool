package com.danylo.mythreadpool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;

public class MyThreadPool implements Executor {
    private List<Worker> threads = new ArrayList<>();
    private BlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();
    private boolean isStopped;

    private class Worker extends Thread {
        private boolean isWaitingForTask;
        @Override
        public void run() {
            while(!isStopped && !tasks.isEmpty()) {
                try {
                    isWaitingForTask = true;
                    Runnable task = tasks.take();
                    isWaitingForTask = false;
                    task.run();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public MyThreadPool(int size) {
        for (int i = 0; i < size; i++) {
            Worker worker = new Worker();
            worker.start();
            threads.add(worker);
        }
    }

    @Override
    public void execute(Runnable command) {
        tasks.add(command);
    }

    public void shutdown() {
        isStopped = true;
        stopWaitingWorkers();
    }

    public List<Runnable> shutdownNow() {
        isStopped = true;
        stopWaitingWorkers();
        List<Runnable> commands = new ArrayList<>();
        tasks.drainTo(commands);
        return commands;
    }

    private void stopWaitingWorkers() {
        for (Worker worker : threads) {
            if (worker.isWaitingForTask) {
                worker.interrupt();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        MyThreadPool pool = new MyThreadPool(3);
        for (int i = 1; i < 11; i++) {
            final int taskNum = i;
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    System.out.println("Task" + taskNum + "started");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Task" + taskNum + "ended");
                }
            });
        }
        Thread.sleep(4000);
        List<Runnable> unexecutedTasks = pool.shutdownNow();
        System.out.println("unexecuted tasks quantity: " + unexecutedTasks.size());
    }
}
