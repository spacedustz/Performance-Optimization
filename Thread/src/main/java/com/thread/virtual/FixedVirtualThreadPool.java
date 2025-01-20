package com.thread.virtual;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

@Slf4j
public class FixedVirtualThreadPool {
    private static final int MAX_CONCURRENT_THREADS = 1001; // 최대 동시 실행 스레드 수
    private static final Semaphore semaphore = new Semaphore(MAX_CONCURRENT_THREADS);

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis(); // 시작 시간 기록
        try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 1000; i++) {
                semaphore.acquire(); // 실행 전 세마포어 획득
                executorService.submit(() -> {
                    try {
                        log.info("Thread : {}", Thread.currentThread());
                        Thread.sleep(1000); // 작업 수행
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        semaphore.release(); // 작업 완료 후 세마포어 해제
                    }
                });
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        long endTime = System.currentTimeMillis(); // 종료 시간 기록
        log.info("작업 완료 시간: {}ms", (endTime - startTime)); // 실행 시간 출력
    }
}

