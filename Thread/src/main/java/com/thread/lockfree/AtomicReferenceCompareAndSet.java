package com.thread.lockfree;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class AtomicReferenceCompareAndSet {
    public static void main(String[] args) {
        String oldName = "old name";
        String newName = "new name";

        AtomicReference<String> atomicReference = new AtomicReference<>(oldName);

        // 첫번쨰 파라미터 expected / 두번쨰 파라미터 update
        // 만약 첫번쨰 값이 일치하지 않으면 업데이트 수행 안됨
        if (atomicReference.compareAndSet(oldName, newName)) {
            log.info("new value is : {}", atomicReference.get());
        }
    }
}
