package com.thread.vault;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
public class VaultHackerThread {

    public static final int MAX_PASSWORD = 9999; // 비밀번호의 최대값

    // 금고 클래스
    private static class Vault {
        private int password;

        public Vault(int password) {
            this.password = password;
        }

        // 비밀 번호가 맞는지 확인하는 함수
        public boolean isCorrectPassword(int guess) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
            }

            return this.password == guess;
        }
    }

    // 해커 스레드
    private static abstract class Hacker extends Thread {
        protected Vault vault;

        public Hacker(Vault vault) {
            this.vault = vault;
            this.setName(this.getClass().getSimpleName());
            this.setPriority(Thread.MAX_PRIORITY);
        }

        @Override
        public void start() {
            log.info("Starting Thread : {}", this.getName());
            super.start();
        }
    }

    // 해커 스레드를 확장하는 비밀번호를 오름차순으로 추측하는 클래스
    // 모든 해커 스레드와 스레드 기능을 가져옵니다.
    private static class AscendingHackerThread extends Hacker {

        public AscendingHackerThread(Vault vault) {
            super(vault);
        }

        @Override
        public void run () {
            for (int guess = 0; guess < MAX_PASSWORD; guess++) {
                if (vault.isCorrectPassword(guess)) {
                    log.info("{} 스레드가 비밀번호 추측에 성공하였습니다. 입력한 비밀번호 : {}", this.getName(), guess);
                    log.info("프로그램을 종료합니다.");
                    System.exit(0);
                }
            }
        }
    }

    // 해커 스레드를 확장하며 비밀번호를 내림차순으로 추측하는 클래스
    // 모든 해커 스레드와 스레드 기능을 가져옵니다.
    private static class DescendingHackerThread extends Hacker {

        public DescendingHackerThread(Vault vault) {
            super(vault);
        }

        @Override
        public void run() {
            for (int guess = MAX_PASSWORD; guess >= 0; guess--) {
                if (vault.isCorrectPassword(guess)) {
                    log.info("{} 스레드가 비밀번호 추측에 성공하였습니다. 입력한 비밀번호 : {}", this.getName(), guess);
                    log.info("프로그램을 종료합니다.");
                    System.exit(0);
                }
            }
        }
    }

    // 경찰 스레드, Thread를 직접 확장 합니다.
    // 캡슐화된 모든 기능을 해커 스레드에 가져올 수 없습니다.
    private static class PoliceThread extends Thread {

        @Override
        public void run() {
            for (int i = 10; i > 0; i--) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                log.info(String.valueOf(i));
            }

            log.info("잡았다 요놈!");
            log.info("프로그램을 종료합니다.");
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        // Random 객체 생성
        Random random = new Random();

        // 0 ~ MAX_PASSWORD 사이의 임의 비밀번호 설정
        Vault vault = new Vault(random.nextInt(MAX_PASSWORD));

        // Thread List에 Thread 들을 넣고 각 스레드를 실행시킵니다.
        List<Thread> list = new ArrayList<>();
        list.add(new AscendingHackerThread(vault));
        list.add(new DescendingHackerThread(vault));
        list.add(new PoliceThread());

        for (Thread thread : list) {
            thread.start();
        }
    }
}
