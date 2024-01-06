package com.thread.share;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SharedClass {
    int x = 0;
    int y = 0;

    public void increment() {
        x++;
        y++;
    }

    /* 불변성 체크 함수 */
    public void checkForDataRace() {
        if (y > x) {
            log.error("불가능한 상황 발생");
        }
    }
}