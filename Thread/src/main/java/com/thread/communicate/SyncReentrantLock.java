package com.thread.communicate;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
class SyncReentrantLock {
    private String data;
    private boolean hasData = false;
    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();

    public void produce(String newData) throws InterruptedException {
        lock.lock();
        try {
            while (hasData) {
                condition.await();  // 데이터가 소비될 때까지 대기
            }
            data = newData;
            hasData = true;
            log.info("생산 : {}", newData);
            condition.signal();  // 소비자에게 알림
        } finally {
            lock.unlock();
        }
    }

    public String consume() throws InterruptedException {
        lock.lock();
        try {
            while (!hasData) {
                condition.await();  // 데이터가 생산될 때까지 대기
            }
            String consumedData = data;
            hasData = false;
            log.info("소비 : {}", consumedData);
            condition.signal();  // 생산자에게 알림
            return consumedData;
        } finally {
            lock.unlock();
        }
    }

    public static class ProducerConsumerExample {
        public static void main(String[] args) {
            SyncReentrantLock resource = new SyncReentrantLock();

            Thread producer = new Thread(() -> {
                try {
                    resource.produce("Hello World");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            Thread consumer = new Thread(() -> {
                try {
                    String data = resource.consume();
                    log.info("Received : {}", data);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            producer.start();
            consumer.start();
        }
    }
}

