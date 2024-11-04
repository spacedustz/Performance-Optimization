## 📚 AtomicReference<T>를 이용한 Lock-Free Stack 구현

Lock-Free의 연장으로 멀티스레드 환경에서 Stack을 Lock-Free로 구현하는 방법을 작성합니다.

구현 흐름은, 초기 데이터로 100,000개의 무작위 정수를 스택에 추가한 후, 여러 스레드가 동시에 push와 pop 작업을 수행합니다. 

10초 후에 총 작업 수를 로그로 기록하여 스택의 동작 성능을 측정합니다. 

이러한 과정을 통해 Lock을 사용하지 않고도 안전하게 멀티스레드 환경에서 동작하는 스택을 구현하는 방법을 배울 수 있습니다.

<br>

### StandardStack 클래스

Lock을 사용하는 단일 스레드 환경에서 동작하는 스택으로, synchronized 키워드를 사용하여 push 메소드에 락을 걸어 안전한 접근을 보장합니다. 

새 노드를 생성하고 현재 헤드를 업데이트하며, 카운터를 증가시켜 현재 스택의 크기를 유지합니다.

이 테스트에서는 사용하지 않을 것이고 Lock-Free 구조와 비교를 위해 작성하였습니다.

```java
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
```

<br>


### StackNode 클래스

StackNode 클래스는 Lock을 사용 하지 않는 방식으로 구현했으며,

각 노드는 저장할 데이터와 다음 노드를 가리키는 참조를 포함하고 있고, Linked-List 구조를 형성하는 기본적인 단위를 제공합니다. 

생성자는 주어진 값을 노드에 저장하고, 다음 노드의 참조는 초기화하지 않으며, 이는 스택에서 연결될 때 설정됩니다.

```java
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
```

<br>

### LockFreeStack 클래스

LockFreeStack 클래스는 멀티스레드 환경에서 안전하게 데이터를 추가하거나 제거할 수 있는 스택을 구현한 클래스입니다.
이 클래스는 락을 사용하지 않으므로 성능이 뛰어나며, 동시 접근이 가능하도록 설계 되었습니다.

<br>

**필드**

- head: 스택의 최상단 노드를 참조하는 AtomicReference입니다. 이를 통해 여러 스레드가 동시에 접근해도 안전하게 업데이트할 수 있습니다.
- counter: 스택에 쌓인 요소의 개수를 추적하는 AtomicInteger로, 스레드 세이프하게 값을 증가시킬 수 있습니다.

<br>

**함수**

- push(T value): 새로운 값을 스택의 최상단에 추가합니다. 새로운 노드를 만들고 현재 head를 설정한 후, compareAndSet을 통해 head가 변경되지 않았는지 확인합니다. 변경되지 않았다면 새 노드를 head로 설정합니다. 변경되었다면 짧은 대기 시간을 두고 다시 시도합니다.
- pop(): 스택의 최상단 노드를 제거하고 그 값을 반환합니다. 현재 head가 null이 아닐 경우, compareAndSet을 통해 head를 새 노드로 교체합니다.
- getCounter(): 현재 스택에 쌓인 요소의 수를 반환합니다.

<br>

```java
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
```

<br>

### LockFreeStackNode 클래스

이 클래스를 메인으로 Lock-Free Stack을 이용해 100,000개의 무작위 정수를 스택에 추가한 후, 두 개의 push 스레드와 두 개의 pop 스레드를 생성하여 동시에 작업을 수행하게 합니다. 

10초 후에 로그를 통해 총 작업 수를 출력하여 스택의 성능을 평가합니다.

- 초기화: LockFreeStack 인스턴스와 무작위 정수를 생성할 Random 객체를 초기화합니다.
- 데이터 추가: 100,000개의 무작위 정수를 스택에 추가하여 초기 상태를 설정합니다.
- 스레드 생성: push 작업을 수행할 2개의 스레드와 pop 작업을 수행할 2개의 스레드를 생성합니다. 각 스레드는 무한 루프에서 계속 작업을 수행하며, setDaemon(true)로 설정하여 메인 스레드가 종료되면 자동으로 종료됩니다.
- 스레드 시작: 모든 스레드를 시작한 후, 메인 스레드는 10초 동안 대기합니다. 이 동안 생성된 스레드는 계속해서 push와 pop 작업을 수행합니다.
- 로그 출력: 10초 동안 수행된 총 작업 수를 로그에 기록합니다.

```java
@Slf4j
public class LockFreeStackNode {
    public static void main(String[] args) throws InterruptedException {
        LockFreeStack<Integer> stack = new LockFreeStack<>();
        Random random = new Random();

        // 초기 랜덤 데이터 값 추가
        for (int i = 0; i < 100000; i++) {
            stack.push(random.nextInt());
        }

        List<Thread> threads = new ArrayList<>();

        int pushingThreads = 2;
        int poppingThreads = 2;

        for (int i = 0; i < pushingThreads; i++) {
            Thread thread = new Thread(() -> {
                while (true) {
                    stack.push(random.nextInt());
                }
            });

            thread.setDaemon(true);
            threads.add(thread);
        }

        for (int i = 0; i < poppingThreads; i++) {
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

        Thread.sleep(10000);
        ;
        log.info("10초 동안의 Lock Free Stack의 Push/Pop 연산 횟수 : {}", stack.getCounter());
    }
}
```

<br>

### LockSupport.parkNanos() 를 사용한 이유 🧙

LockSupport.parkNanos(1)는 Java의 LockSupport 클래스를 사용해 현재 스레드를 잠시 대기 상태로 만드는 함수입니다. 

여기서 parkNanos(1)는 스레드를 1나노초 동안 대기시키는 역할을 합니다.

<br>

1나노초는 매우 짧은 시간이지만, compareAndSet이 실패할 때마다 바로 재시도하면 CPU 리소스를 많이 사용할 수 있습니다. 

parkNanos(1)로 잠깐 대기하게 해서, 과도한 CPU 사용을 피하고 시스템 성능을 개선하는 것입니다. 

이처럼 lock-free 알고리즘에서 다른 스레드와 충돌을 줄이기 위해 짧은 대기 시간을 삽입하는 기법을 `백오프(backoff)`라고 합니다.

하지만, 실질적으로 1 나노초 대기는 의미가 없을 수 있어 일반적으로 백오프 시간은 1, 10, 100 나노초 등으로 점진적으로 늘려가면서 설정하는 방식으로도 사용됩니다.

<br>

### 전체 코드

```java
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
```

<br>

**결과값**

```text
17:31:21.495 [main] INFO com.thread.lockfree.LockFreeStackNode -- 10초 동안의 Lock Free Stack의 Push/Pop 연산 횟수 : 410979831

Process finished with exit code 0
```