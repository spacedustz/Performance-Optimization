package com.thread.coordination;

import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;

/**
 * 1. 인터럽트 하려는 스레드가 인터럽트 당했을 때 InterruptedException을 발생시키는 경우
 * 2. 인터럽트 하려는 스레드가 신호를 명시적으로 처리하는 겨우
 */
@Slf4j
public class InterruptThread {

    // Runnable을 구현하며 잘못된 시간을 차단하는 작업을 수행하는 스레드
    private static class BlockingTask implements Runnable {

        @Override
        public void run() {
            try {
                Thread.sleep(500000);
            } catch (InterruptedException e) {
                log.info("Blocking Thread 종료");
            }
        }
    }

    // 거듭제곱을 수행하는 스레드
    private static class LongComputationTask implements Runnable {
        private BigInteger base; // 밑수
        private BigInteger power; // 제곱

        public LongComputationTask(BigInteger base, BigInteger power) {
            this.base = base;
            this.power = power;
        }

        // 밑과 제곱을 올리는 함수
        private BigInteger pow(BigInteger base, BigInteger power) {
            // 결과만 선언하고 1초 초기화
            BigInteger result = BigInteger.ONE;

            // 그리고, 0부터 제곱의 값까지 반복
            for (BigInteger i = BigInteger.ZERO; i.compareTo(power) != 0; i = i.add(BigInteger.ONE)) {
                // 조건문 추가 - Interrupt 시 계산 중지
                if (Thread.currentThread().isInterrupted()) {
                    log.info("계산 중지");
                    return BigInteger.ZERO;
                }

                // 각각의 반복에서는 이전 반복에서 도출된 결과에 밑수를 곱해 새로운 결과를 계산합니다.
                result = result.multiply(base);
            }

            // 결과 반환
            return result;
        }

        // 밑수와 제곱을 계산해 결과를 반환하는 스레드 실행
        @Override
        public void run() {
            log.info("{} * {} = {}", base, power, pow(base, power));
        }
    }

    public static void main(String[] args) {
        /* BlockingTask 실행 코드 */
        /* main 스레드가 종료되었는데도 불구하고, Blocking Thread 때문에 앱이 종료가 되지 않음 */
//        Thread thread = new Thread(new BlockingTask());
//        thread.start();
//
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//
//        Thread orderStopThread = new Thread(thread::interrupt);
//        orderStopThread.start();


        /* LongComputationTask 실행 코드 */
        Thread thread = new Thread(new LongComputationTask(new BigInteger("200000"), new BigInteger("10000000")));
        // 2의 10제곱 계산
        thread.setDaemon(true);
        thread.start();
        thread.interrupt();
    }
}
