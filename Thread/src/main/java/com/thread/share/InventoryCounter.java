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
        increment.join();

        decrement.start();
        decrement.join();

        log.info("현재 아이템 개수 : {}", counter.getItems());
    }

    /* Item을 관리하는 Counter */
    private static class Counter {
        private int items = 0;

        public void increment() { items++; }
        public void decrement() { items--; }
        public int getItems() { return items; }
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
