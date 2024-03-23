package com.thread.lock;

import lombok.extern.slf4j.Slf4j;

import java.util.Random;

@Slf4j
public class TrainB implements Runnable {
    private InterSection interSection;
    private Random random = new Random(); // 임의의 기차 스케쥴 생성

    public TrainB(InterSection interSection) {
        this.interSection = interSection;
    }

    // 같은 연산을 수행하도록 반복
    @Override
    public void run() {
        while (true) {
            // 기차가 오기까지 기다리는 임의의 시간 선택
            long sleepingTime = random.nextInt(5);

            // 임의의 시간만큼 멈춤 -> 임의의 기차 스케쥴이 있다고 가정
            try {
                Thread.sleep(sleepingTime);
            } catch (InterruptedException e) {
                log.warn("Thread Interrupted");
            }

            // 스레드가 다시 동작하면 기차가 roadA를 타고 교차로롤 지남
            interSection.takeRoadB();
        }
    }
}

