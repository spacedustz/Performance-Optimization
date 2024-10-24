package com.thread.lockfree;

import java.util.concurrent.atomic.AtomicInteger;

public class ECommerceInventoryCounter {

    // 감소 스레드
    public static class DecrementingThread extends Thread {
        private
    }

    // 인벤토리 카운터
    public static class InventoryCounter {
        private AtomicInteger items = new AtomicInteger(0);

        public void increment() {
            items.incrementAndGet();
        }

        public void decrement() {

        }
    }
}
