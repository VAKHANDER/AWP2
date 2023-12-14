package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TaskBalancer {
    private final List<Worker> workers;
    private final ExecutorService executor;
    private final Lock lock;
    public TaskBalancer(int numWorkers) {
        workers = new ArrayList<>();
        executor = Executors.newFixedThreadPool(numWorkers);
        lock = new ReentrantLock();
        for (int i = 0; i < numWorkers; i++) {
            Worker worker = new Worker();
            workers.add(worker);
            executor.execute(worker);
        }
    }
    public void submitTask(Runnable task) {
        lock.lock();
        try {
            Worker minWorker = workers.get(0);
            for (int i = 1; i < workers.size(); i++) {
                if (workers.get(i).getNumPendingTasks() < minWorker.getNumPendingTasks()) {
                    minWorker = workers.get(i);
                }
            }
            minWorker.submitTask(task);
        } finally {
            lock.unlock();
        }
    }
    public void stop() {
        lock.lock();
        try {
            for (Worker worker : workers) {
                worker.stop();
            }
            executor.shutdown();
        } finally {
            lock.unlock();
        }
    }
    private class Worker implements Runnable {
        private final BlockingQueue<Runnable> taskQueue;
        private volatile boolean running;

        public Worker() {
            taskQueue = new LinkedBlockingQueue<>();
            running = true;
        }
        public int getNumPendingTasks() {
            return taskQueue.size();
        }

        public void submitTask(Runnable task) {
            taskQueue.offer(task);
        }
        public void stop() {
            running = false;
        }
        @Override
        public void run() {
            while (running) {
                try {
                    Runnable task = taskQueue.take();
                    task.run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
