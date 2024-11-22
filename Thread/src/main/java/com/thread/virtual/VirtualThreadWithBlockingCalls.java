package com.thread.virtual;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class VirtualThreadWithBlockingCalls {
    private static final int NUMBER_OF_VIRTUAL_THREADS = 1000;

    public static void main(String[] args)throws InterruptedException {
        List<Thread> virtualThreads = new ArrayList<>();

        for (int i = 0; i < NUMBER_OF_VIRTUAL_THREADS; i++) {
            Thread virtualThread = Thread.ofVirtual().unstarted(new BlockingTask());
            virtualThreads.add(virtualThread);
        }

        for (Thread virtualThread : virtualThreads) {
            virtualThread.start();
        }

        for (Thread virtualThread : virtualThreads) {
            virtualThread.join();
        }
    }

    private static class BlockingTask implements Runnable {
        @Override
        public void run() {
            log.info("Inside Thread : {} Before Blocking Call", Thread.currentThread());

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            log.info("Inside Thread : {} After Blocking Call", Thread.currentThread());
        }
    }
}
