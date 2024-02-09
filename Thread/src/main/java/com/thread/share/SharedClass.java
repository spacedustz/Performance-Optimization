package com.thread.share;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SharedClass {
    public static void main(String[] args) {
        SubSharedClass sharedClass = new SubSharedClass();

        Thread t1 = new Thread(() -> {
            for (int i=0; i<Integer.MAX_VALUE; i++) {
                sharedClass.increment();
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i=0; i<Integer.MAX_VALUE; i++) {
                sharedClass.checkForDataRace();
            }
        });

        t1.start();
        t2.start();
    }

    public static class SubSharedClass {
        private volatile int x = 0;
        private volatile int y = 0;

        public void increment() {
            x++;
            y++;
        }

        /* 불변성 체크 함수 */
        public void checkForDataRace() {
            if (y > x) {
                log.error("y > x Data Race is Detected");
            }
        }
    }
}