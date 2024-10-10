package com.thread.communicate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SyncObject {
    private String data;
    private boolean hasData = false;

    public synchronized void produce(String newData) throws InterruptedException {
        while (hasData) {
            wait();  // 이미 데이터가 있으면 소비될 때까지 대기
        }
        data = newData;
        hasData = true;
        log.info("생산 : {}", newData);
        notify();  // 소비자에게 데이터가 준비되었음을 알림
    }

    public synchronized String consume() throws InterruptedException {
        while (!hasData) {
            wait();  // 데이터가 없으면 생산될 때까지 대기
        }
        String consumedData = data;
        hasData = false;
        log.info("소비 : {}", consumedData);
        notify();  // 생산자에게 데이터를 소비했음을 알림
        return consumedData;
    }

    public static class ProducerConsumerExample {
        public static void main(String[] args) {
            SyncObject resource = new SyncObject();

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