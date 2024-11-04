package com.thread.lockfree;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

@Slf4j
public class LockFreeStackNode {
    public static void main(String[] args) throws InterruptedException {
        LockFreeStack<Integer> stack = new LockFreeStack<>();
        Random random = new Random();

        // 초기 랜덤 데이터 값 추가
        for (int i=0; i<100000; i++) {
            stack.push(random.nextInt());
        }

        List<Thread> threads = new ArrayList<>();

        int pushingThreads = 2;
        int poppingThreads = 2;

        for (int i=0; i<pushingThreads; i++) {
            Thread thread = new Thread(() -> {
                while (true) {
                    stack.push(random.nextInt());
                }
            });

            thread.setDaemon(true);
            threads.add(thread);
        }

        for (int i=0; i<poppingThreads; i++) {
            Thread thread = new Thread(() -> {
                while (true) {
                    stack.pop();
                }
            });

            thread.setDaemon(true);
            threads.add(thread);
        }

        for (Thread thread : threads) {
            thread.start();
        }

        Thread.sleep(10000);;
        log.info("10초 동안의 Lock Free Stack의 Push/Pop 연산 횟수 : {}", stack.getCounter());
    }

    /**
     * 여러 스레드가 동시 접근해도 안전하게 동작할 수 있도록 설계한 Stack
     * Lock을 사용하지 않고 Lock-Free 방식으로 Stack에 데이터 추가/제거
     * @field head : 스택의 최상단 노드 참조, AtomicReference를 통해 동시 접근 가능, 여러 스레드가 안전하게 head 업데이트 가능
     * @field counter : 스택 내 요소의 개수를 추적하는 Atomic Integer, Thread-Safe 하게 값 증가 가능
     */
    public static class LockFreeStack<T> {
        private AtomicReference<StackNode<T>> head = new AtomicReference<>();
        private AtomicInteger counter = new AtomicInteger(0);

        public void push(T value) {
            StackNode<T> newHeadNode = new StackNode<>(value);

            while (true) {
                StackNode<T> currentHeadNode = head.get();
                newHeadNode.next = currentHeadNode;

                if (head.compareAndSet(currentHeadNode, newHeadNode)) {
                    break;
                } else {
                    LockSupport.parkNanos(1);
                }
            }
            counter.incrementAndGet();
        }

        public T pop() {
            StackNode<T> currentHeadNode = head.get();
            StackNode<T> newHeadNode;

            while (currentHeadNode != null) {
                newHeadNode = currentHeadNode.next;

                if (head.compareAndSet(currentHeadNode, newHeadNode)) {
                    break;
                } else {
                    LockSupport.parkNanos(1);
                    currentHeadNode = head.get();
                }
            }
            counter.incrementAndGet();
            return currentHeadNode != null ? currentHeadNode.value : null;
        }

        public int getCounter() {
            return counter.get();
        }
    }

    /**
     * 사용 X
     * 단일 스레드 환경에서 안전하게 동작하는 스택의 구현체, push()에 락을 걸어 여러 스레드가 접근해도 순차 실행, Lock을 사용함
     * @field head : 스택의 최상단 노드의 참조
     * @field : counter : 스택에 쌓인 요소의 개수
     */
    public static class StandardStack<T> {
        private StackNode<T> head;
        private int counter = 0;

        public synchronized void push(T value) {
            StackNode<T> newHead = new StackNode<>(value);
            newHead.next = head;
            head = newHead;
            counter++;
        }
    }

    /**
     * Lock을 사용하지 않는 Stack Node 클래스
     * @field value : 노드가 저장하고 있는 실제 데이터
     * @field next : 다음 노드를 가르키는 참조, 스택에서 Linked-List 구조를 형성하는데 사용
     * @constructor : 새 노드를 생성할 떄 노드에 데이터를 저장하고 next는 초기화 안함
     */
    private static class StackNode<T> {
        public T value;
        public StackNode<T> next;

        public StackNode(T value) {
            this.value = value;
            this.next = next;
        }
    }
}
