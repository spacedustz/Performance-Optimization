package com.thread.coordination;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/* 숫자 배열 - 각 숫자의 Factorial을 각각의 스레드로 병렬 계산하는 클래스 */
@Slf4j
@Getter
public class FactorialThread extends Thread {

    private long inputNums;
    private BigInteger result = BigInteger.ZERO;
    private boolean isFinished = false;

    public FactorialThread(long inputNums) {
        this.inputNums = inputNums;
    }

    @Override
    public void run() {
        this.result = factorial(inputNums);
        this.isFinished = true;
    }

    // 스레드가 스케줄링 되면 입력 숫자의 Factorial을 계산 후, result 변수에 저장합니다.
    public BigInteger factorial(long n) {
        BigInteger tempResult = BigInteger.ONE;

        for (long i = n; i > 0; i--) {
            tempResult = tempResult.multiply(new BigInteger(Long.toString(i)));
        }

        return tempResult;
    }

    public static void main(String[] args) {
        // 숫자 배열 생성
        List<Long> inputNums = Arrays.asList(0L, 3435L, 35435L, 2324L, 4656L, 23L, 2435L, 5566L);

        // 스레드 리스트 생성
        List<FactorialThread> list = new ArrayList<>();

        // 모든 입력 숫자에 대해 각각 스레드 객체 생성
        for (long inputNum : inputNums) {
            list.add(new FactorialThread(inputNum));
        }

        // 스레드 리스트의 모든 스레드 시작
        for (Thread thread : list) {
            thread.setDaemon(true);
            thread.start();
        }

        // join()을 통해 계산 스레드의 모든 작업이 완료 되었을때 아래 Loop를 실행하게 함.
        for (Thread thread : list) {
            try {
                thread.join(2000);
            } catch (InterruptedException e) {
            }
        }

        // 계산 스레드에서 결과값을 가져와 출력 - main 메서드의 역할
        for (int i = 0; i < inputNums.size(); i++) {
            // 각 스레드의 계산 완료 여부(isFinished)를 확인해 결과가 준비 됬는지 확인
            FactorialThread thread = list.get(i);

            // 계산이 완료됬다면, 입력값과 계산 결과값 출력
            if (thread.isFinished) {
                log.info("계산 완료. - {}의 Factorial은 {} 입니다.", inputNums.get(i), thread.getResult());
            } else {
                log.info("계산 중 입니다. - 입력값 : {}", inputNums.get(i));
            }
        }
    }
}
