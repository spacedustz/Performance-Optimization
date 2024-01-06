package com.thread.share;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InventoryCounter {
    public static void main(String[] args) throws InterruptedException {
        Counter counter = new Counter();
        IncrementingThread increment = new IncrementingThread(counter);
        DecrementingThread decrement = new DecrementingThread(counter);

        increment.start();
        decrement.start();

        increment.join();
        decrement.join();

        log.info("현재 아이템 개수 : {}", counter.getItems());
    }

    /* Item을 관리하는 Counter */
    private static class Counter {
        private int items = 0;
        Object lock = new Object(); // Lock 객체

        public void increment() {
            // Synchronized 블럭을 만들어서 Lock 객체에 동기화 시킴
            synchronized (this.lock) {
                items++;
            }
        }
        public synchronized void decrement() {
            synchronized (this.lock) {
                items--;
            }
        }
        public synchronized int getItems() {
            synchronized (this.lock) {
                return items;
            }
        }
    }

    /* Item을 10000개 증가 시키는 스레드 */
    @RequiredArgsConstructor
    public static class IncrementingThread extends Thread {
        private final Counter counter;

        @Override
        public void run() {
            for (int i=0; i<10000; i++) {
                counter.increment();
            }
        }
    }

    /* Item을 10000개 감소 시키는 스레드 */
    @RequiredArgsConstructor
    public static class DecrementingThread extends Thread {
        private final Counter counter;

        @Override
        public void run() {
            for (int i=0; i<10000; i++) {
                counter.decrement();
            }
        }
    }
}
