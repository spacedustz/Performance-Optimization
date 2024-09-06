package com.thread.communicate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SemaphoreBarrier {
    public static void main(String [] args) throws InterruptedException {
        int numberOfThreads = 20;

        List<Thread> threads = new ArrayList<>();

        // 공유 자원- > 모든 스레드가 이 Barrier 객체를 가지며 내부 counter 값으로 Semaphore를 통해 Thread Blocking이 일어남
        Barrier barrier = new Barrier(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            threads.add(new Thread(new CoordinatedWorkRunner(barrier)));
        }

        for(Thread thread: threads) {
            thread.start();
        }
    }

    static class Barrier {
        private final int numberOfWorkers;
        private final Semaphore semaphore = new Semaphore(0); // 처음에는 0으로 시작, 모든 쓰레드 대기
        private int counter = 0; // 작업을 완료한 쓰레드 수 추적
        private final Lock lock = new ReentrantLock();

        public Barrier(int numberOfWorkers) {
            this.numberOfWorkers = numberOfWorkers;
        }

        // 첫번째 Thread가 작업 완료 후 나머지 모든 스레드들을 기다리고, 나머지 모든 스레드가 작업 1을 완료 했으면 그떄 전부 꺠워서 작업 2 수행
        public void waitForOthers() throws InterruptedException {
            lock.lock();
            boolean isLastWorker = false;
            try {
                counter++;
                if (counter == numberOfWorkers) {
                    isLastWorker = true; // 마지막 쓰레드인지 확인
                }
            } finally {
                lock.unlock();
            }

            if (isLastWorker) {
                semaphore.release(numberOfWorkers - 1); // 마지막 쓰레드는 모든 다른 쓰레드를 깨움
                System.out.println("==================== 작업 끝 ====================");
            } else {
                semaphore.acquire(); // 다른 쓰레드는 대기
            }
        }
    }

    static class CoordinatedWorkRunner implements Runnable {
        private final Barrier barrier;

        public CoordinatedWorkRunner(Barrier barrier) {
            this.barrier = barrier;
        }

        @Override
        public void run() {
            try {
                task();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        private void task() throws InterruptedException {
            // 첫 번째 작업 수행
            System.out.println(Thread.currentThread().getName()
                    + " 작업 1 수행");

            barrier.waitForOthers(); // 다른 쓰레드들이 도착할 때까지 대기

            // 두 번째 작업 수행
            System.out.println(Thread.currentThread().getName()
                    + " 작업 2 수행");
        }
    }
}
