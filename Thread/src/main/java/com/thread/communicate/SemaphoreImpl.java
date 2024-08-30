package com.thread.communicate;

import lombok.AllArgsConstructor;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Producer-Consumer 패턴으로 구현한 Semaphore
 * 1. Consumer 가 full을 acquire해 스레드가 블럭됨
 * 2. Producer가 empty를 acquire하고 아이템 생산 후 full을 release해 소비를 하게 함
 * 3. 소비가 완료될떄까지 기다린 후 소비가 완료되면 Consumer 스레드가 empty를 release하여 다시 생산
 */
public class SemaphoreImpl {
    Semaphore full = new Semaphore(0);
    Semaphore empty = new Semaphore(5); // 공유 리소스 Queue의 값만큼 설정
    Queue itemQueue = new LinkedBlockingDeque(5);

    private final AtomicInteger producedCount = new AtomicInteger(0);
    private final AtomicInteger consumedCount = new AtomicInteger(0);

    public static void main(String[] args) {
        SemaphoreImpl semaphoreImpl = new SemaphoreImpl();

        // 2개의 Producer 스레드 생성
        Thread producer1 = new Thread(new Producer(semaphoreImpl, 1));
        Thread producer2 = new Thread(new Producer(semaphoreImpl, 2));

        // 2개의 Consumer 스레드 생성
        Thread consumer1 = new Thread(new Consumer(semaphoreImpl, 1));
        Thread consumer2 = new Thread(new Consumer(semaphoreImpl, 2));

        // 스레드 시작
        producer1.start();
        producer2.start();
        consumer1.start();
        consumer2.start();
    }

    public void incrementProducedCount() {
        int count = producedCount.incrementAndGet();
        System.out.println("Total items produced: " + count);
    }

    public void incrementConsumedCount() {
        int count = consumedCount.incrementAndGet();
        System.out.println("Total items consumed: " + count);
    }
}

/* Item 클래스 */
@AllArgsConstructor
class Item {
    private final int id;

    @Override
    public String toString() {
        return "Item(" + "id=" + id + "}";
    }
}

/* 생산자 스레드 */
class Producer implements Runnable {
    private final SemaphoreImpl semaphoreImpl;
    private final int producerId;

    public Producer(SemaphoreImpl semaphoreImpl, int producerId) {
        this.semaphoreImpl = semaphoreImpl;
        this.producerId = producerId;
    }

    @Override
    public void run() {
        try {
            int itemCounter = 0;
            while (true) {
                semaphoreImpl.empty.acquire();
                Item item = new Item(itemCounter++);
                boolean added = semaphoreImpl.itemQueue.offer(item);
                if (added) {
//                    System.out.println("Producer " + producerId + " produced " + item);
                    semaphoreImpl.incrementProducedCount();
                    semaphoreImpl.full.release();
                } else {
                    System.out.println("Producer " + producerId + " failed to produce " + item);
                }
                Thread.sleep((long) (Math.random() * 1000)); // 임의의 시간동안 대기
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

/* 소비자 스레드 */
class Consumer implements Runnable {
    private final SemaphoreImpl semaphoreImpl;
    private final int consumerId;

    public Consumer(SemaphoreImpl semaphoreImpl, int consumerId) {
        this.semaphoreImpl = semaphoreImpl;
        this.consumerId = consumerId;
    }

    @Override
    public void run() {
        try {
            while (true) {
                semaphoreImpl.full.acquire();
                Item item = (Item) semaphoreImpl.itemQueue.poll();
                if (item != null) {
//                    System.out.println("Consumer " + consumerId + " consumed " + item);
                    semaphoreImpl.incrementConsumedCount();
                    semaphoreImpl.empty.release();
                }
                Thread.sleep((long) (Math.random() * 1000)); // 임의의 시간동안 대기
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
