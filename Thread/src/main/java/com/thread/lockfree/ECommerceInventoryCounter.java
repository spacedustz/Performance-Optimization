package com.thread.lockfree;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ECommerceInventoryCounter {
    public static void main(String[] args) throws InterruptedException {
        InventoryCounter counter = new InventoryCounter();
        IncrementingThread incrementingThread = new IncrementingThread(counter);
        DecrementingThread decrementingThread = new DecrementingThread(counter);

        incrementingThread.start();
        decrementingThread.start();

        incrementingThread.join();
        decrementingThread.join();

        log.info("현재 보유한 아이템 수 : {}", counter.getItems());
    }

    // 증가 스레드
    @AllArgsConstructor
    public static class IncrementingThread extends Thread {
        private InventoryCounter inventoryCounter;

        @Override
        public void run() {
            for (int i=0; i<10000; i++) {
                inventoryCounter.increment();
            }
        }
    }

    // 감소 스레드
    @AllArgsConstructor
    public static class DecrementingThread extends Thread {
        private InventoryCounter inventoryCounter;;

        @Override
        public void run() {
            for (int i=0; i<10000; i++) {
                inventoryCounter.decrement();
            }
        }
    }

    // 인벤토리 카운터
    public static class InventoryCounter {
        private AtomicInteger items = new AtomicInteger(0);

        public void increment() {
            items.incrementAndGet();
        }

        public void decrement() {
            items.decrementAndGet();
        }

        public int getItems() {
            return items.get();
        }
    }
}
