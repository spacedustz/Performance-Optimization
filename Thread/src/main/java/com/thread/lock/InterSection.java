package com.thread.lock;

import lombok.extern.slf4j.Slf4j;

/**
 * Dead Lock 발생 테스트를 위한 임의의 철도 교통 시스템 구현
 */
@Slf4j
public class InterSection {
    // 공유 리소스 2개 생성
    private Object roadA = new Object();
    private Object roadB = new Object();

    public static void main(String[] args) {
        InterSection interSection = new InterSection();
        Thread trainAThread = new Thread(new TrainA(interSection));
        Thread trainBThread = new Thread(new TrainB(interSection));

        trainAThread.start();
        trainBThread.start();
    }

    // roadA의 Lock을 생성하는 Thread, 어느 Thread가 Lock을 걸었는지 로깅
    public void takeRoadA() {
        synchronized (roadA) {
            log.info("Read A is Locked by Thread {}", Thread.currentThread().getName());

            // roadA가 사용중이면 roadB에 기차가 지나가지 않도록 다른 기차 운행을 Suspend 시킴
            synchronized (roadB) {
                log.info("road A로 지나가는 중");

                // 1 밀리초 동안 기차 통과 중단
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    log.warn("Thread Interrupted");
                }
            }
        }
    }

    // roadB의 Lock을 생성하는 Thread, 어느 Thread가 Lock을 걸었는지 로깅
    public void takeRoadB() {
        synchronized (roadA) {
            log.info("Road B is Locked by Thread {}", Thread.currentThread().getName());

            // roadB가 사용중이면 roadA에 기차가 지나가지 않도록 다른 기차 운행을 Suspend 시킴
            synchronized (roadB) {
                log.info("road B로 지나가는 중");

                // 1 밀리초 동안 기차 통과 중단
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    log.warn("Thread Interrupted");
                }
            }
        }
    }
}
