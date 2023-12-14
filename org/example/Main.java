package org.example;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        int numWorkers = 4;
        org.example.TaskBalancer taskBalancer = new org.example.TaskBalancer(numWorkers);
        for (int i = 0; i < 10; i++) {
            taskBalancer.submitTask(new Task(i));
        }
        Thread.sleep(1000);
        taskBalancer.stop();
    }
    private static class Task implements Runnable {
        private final int taskId;
        public Task(int taskId) {
            this.taskId = taskId;
        }
        @Override
        public void run() {
            System.out.println("Executing task " + taskId);
        }
    }
}