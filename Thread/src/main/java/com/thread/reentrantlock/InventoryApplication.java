package com.thread.reentrantlock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
@Component
public class InventoryApplication {
    private static final int HIGHEST_PRICE = 1000;

    public static class InventoryDatabase {
        private TreeMap<Integer, Integer> inventory = new TreeMap<>();
        private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        private Lock readLock = lock.readLock();
        private Lock writeLock = lock.writeLock();
//        private ReentrantLock lock = new ReentrantLock();

        public int getNumberOfItemInPriceRange(int lowerBound, int upperBound) {
            readLock.lock();

            try {
                Integer fromKey = inventory.ceilingKey(lowerBound); // 하한가와 같거나 큰 값중 가장 큰 키의 수 = 트리내의 최저가
                Integer toKey = inventory.floorKey(upperBound); // 상한가와 같거나 작은 값중 가장 큰 키의 수 = 트리내의 최고가

                if (fromKey == null && toKey == null) return 0;

                // 위에서 뽑은 상한가와 하한가를 NavigableMap에 새로 기록
                NavigableMap<Integer, Integer> rangeOfPrices = inventory.subMap(fromKey, true, toKey, true);

                // 위에서 새로 기록한 트리의 Value를 모두 더한 값
                int sum = 0;
                for (int numberOfItemForPrice : rangeOfPrices.values()) {
                    sum += numberOfItemForPrice;
                }

                return sum;
            } finally {
                readLock.unlock();
            }
        }

        public void addItem(int price) {
            writeLock.lock();

            try {
                inventory.merge(price, 1, Integer::sum);
            } finally {
                writeLock.unlock();
            }
        }

        public void removeItem(int price) {
            writeLock.lock();

            try {
                Integer numberOfItemsForPrice = inventory.get(price);

                if (numberOfItemsForPrice == null || numberOfItemsForPrice == 1) inventory.remove(price);
                else inventory.put(price, numberOfItemsForPrice - 1);

            } finally {
                writeLock.unlock();
            }
        }
    }

    public static void main(String[] args) {
        InventoryDatabase db = new InventoryDatabase();
        Random random = new Random();

        // 실제 환경 벤치마크 테스트를 위해 내부 트리에 초기 키 넣기
        for (int i = 0; i < 100000; i++) {
            db.addItem(random.nextInt(HIGHEST_PRICE));
        }

        // 랜덤한 Price(키값)를 기준으로 아이템 추가/삭제 작업을 무한 반복하는 데몬 스레드
        Thread writer = new Thread(() -> {
            while (true) {
                db.addItem(random.nextInt(HIGHEST_PRICE));
                db.removeItem(random.nextInt(HIGHEST_PRICE));

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    log.warn("Writer Thread Sleep Interrupted : {}", e.getMessage());
                }
            }
        });

        writer.setDaemon(true);
        writer.start();

        // Reader Thread 7개 생성, 각 Reader는 10만번의 조회 실행
        int numberOfReaderThread = 7;
        List<Thread> readers = new ArrayList<>();

        for (int i = 0; i < numberOfReaderThread; i++) {
            Thread reader = new Thread(() -> {
                for (int j = 0; j < 100000; j++) {
                    int upperBoundPrice = random.nextInt(HIGHEST_PRICE);
                    int lowerBoundPrice = upperBoundPrice > 0 ? random.nextInt(upperBoundPrice) : 0;

                    db.getNumberOfItemInPriceRange(lowerBoundPrice, upperBoundPrice);
                }
            });

            reader.setDaemon(true);
            readers.add(reader);
        }

        long startReadingTime = System.currentTimeMillis();

        for (Thread reader : readers) {
            reader.start();
        }

        for (Thread reader : readers) {
            try {
                reader.join();
            } catch (InterruptedException e) {
                log.error("Reader Thread Join Interrupted : {}", e.getMessage());
            }
        }

        long endReadingTime = System.currentTimeMillis();
        log.info("Reading Time : {} ms", endReadingTime - startReadingTime);
    }
}
