package com.thread.create;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ImplementRunnable {

    /* Implement Runnable */
    public static void create() throws InterruptedException {
        // Thread()안의 파라미터는 내부적으로 new Runnable()로 실행됩니다.
        Thread thread = new Thread(() -> {
            // 어떤 코드를 넣던 운영 체제가 스케줄링 하자마자 새로운 스레드로 실행됩니다.
            log.info("Thread 이름 : {}", Thread.currentThread().getName());
            log.info("{} Thread's Priority : {}", Thread.currentThread().getName(), Thread.currentThread().getPriority());
        });

        // Thread Naming & Set Priority
        thread.setName("테스트-1");
        thread.setPriority(Thread.MAX_PRIORITY);

        // 실행
        log.info("실행 전 Thread 이름 : {}", Thread.currentThread().getName());
        thread.start();
        log.info("실행 후 Thread 이름 : {}", Thread.currentThread().getName());

        // InterruptedException - Sleep는 반복되는 명령이 아닙니다.
        Thread.sleep(10000);
    }

    /* 캐치되지 않은 Exception Handler */
    public static void handler() {
        Thread thread = new Thread(() -> {
            // 고의적으로 예외 발생
            throw new RuntimeException("예외 처리 테스트");
        });

        thread.setName("예외-테스트");
        thread.setUncaughtExceptionHandler((t, e) ->  {
            log.info("{} Thread 내부에 치명적인 에러 발생, 에러 메시지 : {}", Thread.currentThread().getName(), e.getMessage());
        });
        thread.start();
    }

    public static void main(String[] args) {
//        try {
//            create();
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }

        handler();
    }
}
