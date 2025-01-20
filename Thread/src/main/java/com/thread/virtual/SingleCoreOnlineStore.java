package com.thread.virtual;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class SingleCoreOnlineStore {
    private static final int NUMBER_OF_TASKS = 10_000;

    public static void main(String[] args) {
        log.info("실행 작업 수 : {}", NUMBER_OF_TASKS);

        long start = System.currentTimeMillis();
        performTasks();
        log.info("작업 수행 완료 시간 : {}ms", System.currentTimeMillis() - start);
    }

    // newVirtualThreadPerTaskExecutor()를 사용해 작업이 생길때마다 가상 스레드를 동적으로 생성
    private static void performTasks() {
        try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {

            for (int i = 0; i < NUMBER_OF_TASKS; i++) {
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        for (int j = 0; j < 100; j++) {
                            blockingIoOperation();
                        }
                    }
                });
            }
        }
    }

    // Blocking I/O
    private static void blockingIoOperation() {
        log.info("Blocking I/O 수행 Thread : {}", Thread.currentThread());
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
