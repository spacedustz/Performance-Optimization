package com.thread.blockingio;

import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class IOBoundApplicationV1 {
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
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // 동적 스레드 풀 생성
    // 이 동적 스레드풀은 작업을 완료하는데 필요한 스레드를 계속 생성하고 캐시로 저장해 필요할 떄 재사용 함
    // 실제 환경에서 동시에 얼마나 많은 작업을 처리해야 하는지 정확한 숫자를 가늠하기는 현실적으로 어렵고 그 작업들을 수행하기 위해 몇개의 스레드를 미리 할당해야 하는지도 알기 어려움
    // 각 스레드에 1000개의 작업을 할당함
    private static void performTasks() {
        ExecutorService executorService = Executors.newCachedThreadPool();

        try (Closeable close = executorService::shutdown) {

            // 새 작업이 실행될 때마다 스레드 풀에 있는 스레드로 실행
            for (int i = 0; i < NUMBER_OF_TASKS; i++) {
                executorService.submit(IOBoundApplicationV1::blockingIoOperation);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
