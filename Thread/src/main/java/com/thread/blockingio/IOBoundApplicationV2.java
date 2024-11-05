package com.thread.blockingio;

import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class IOBoundApplicationV2 {
    private static final int NUMBER_OF_TASKS = 1000; // 작업의 수

    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);

        System.out.println("Press Enter to Start");
        s.nextLine();
        log.info("실행 중인 작업 수 : {}", NUMBER_OF_TASKS);

        long start = System.currentTimeMillis();
        performTasks();
        log.info("작업 완료까지 소요 시간 : {}ms", System.currentTimeMillis() - start);
    }

    // Long Blocking IO 테스트
    private static void blockingIoOperation() {
        log.info("Blocking Task 실행 스레드 : {}", Thread.currentThread());

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void performTasks() {
        ExecutorService executorService = Executors.newCachedThreadPool();

        try {
            for (int i = 0; i < NUMBER_OF_TASKS; i++) {
                executorService.submit(() -> {
                    for (int j = 0; j < 100; j++) {
                        blockingIoOperation();
                    }
                });
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            executorService.shutdown(); // 스레드 풀 종료
            try {
                // 모든 작업이 완료될 때까지 대기
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow(); // 강제 종료
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow(); // 예외 발생 시 강제 종료
            }
        }
    }
}
