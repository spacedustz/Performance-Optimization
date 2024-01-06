package com.thread.share;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;

@Slf4j
public class TimeAverage {
    public static void main(String[] args) {
        Metrics metrics = new Metrics();

        BusinessLogic business1 = new BusinessLogic(metrics);
        BusinessLogic business2 = new BusinessLogic(metrics);
        MetricsPrinter printer = new MetricsPrinter(metrics);

        business1.start();
        business2.start();
        printer.start();
    }

    /* 샘플의 평균값을 가지고 있는 클래스 */
    @Getter
    public static class Metrics {
        private long count = 0; // 지금까지 캡쳐된 샘플의 개수를 추적하는 Count 변수
        private volatile double average = 0.0; // 모든 샘플의 총합을 개수로 나눈 평균값

        // 새로운 Sample 값을 받아 새로운 평균값을 업데이트 해주는 함수
        public synchronized void addSample(long sample) {
            double currentSum = average * count; // 기존 평균값
            count++;
            average = (currentSum + sample) / count; // 새로운 평균값
        }
    }

    /* 시작 & 종료 시간을 캡쳐해 샘플을 추가하는 클래스 */
    @RequiredArgsConstructor
    public static class BusinessLogic extends Thread {
        private final Metrics metrics;
        private Random random = new Random();

        @Override
        public void run() {

            while (true) {
                long start = System.currentTimeMillis();

                try {
                    Thread.sleep(random.nextInt(10));
                } catch (InterruptedException e) {
                    log.error("Thread Interrupted");
                }

                long end = System.currentTimeMillis();

                metrics.addSample(end - start);
            }
        }
    }

    /* BusinessLogic 클래스와 병렬로 실행되며 BusinessLogic의 평균 시간을 캡쳐 후 출력하는 클래스 */
    @RequiredArgsConstructor
    public static class MetricsPrinter extends Thread {
        private final Metrics metrics;

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    log.error("Thread Interrupted");
                }

                double currentAverage = metrics.getAverage();

                log.info("현재 Average 값 : {}", currentAverage);
            }
        }
    }
}
