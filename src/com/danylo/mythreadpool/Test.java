package com.danylo.mythreadpool;

import java.util.List;

public class Test {
    public static void main(String[] args) throws InterruptedException {
        MyThreadPool pool = new MyThreadPool(3);
        Thread.sleep(500);
        for (int i = 1; i < 11; i++) {
            final int taskNum = i;
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        System.out.println("Task " + taskNum + " started");
                        Thread.sleep(2000);
                        System.out.println("Task " + taskNum + " ended");
                    } catch (InterruptedException e) {
                        System.out.println("Task " + taskNum + " is interrupted");
                    }
                }
            });
            System.out.println("tast " + taskNum + " created");
//            Thread.sleep(100);
        }
        Thread.sleep(3000);
//        pool.shutdown();
        List<Runnable> unexecutedTasks = pool.shutdownNow();
        System.out.println("unexecuted tasks quantity: " + unexecutedTasks.size());
    }
}
