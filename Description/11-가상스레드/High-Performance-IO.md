## 📚 Virtual Thread를 이용한 고성능 I/O

가상 스레드(Virtual Thread)가 긴 시간이 걸리는 Blocking I/O 연산을 처리할 때, **캐리어 스레드(Carrier Thread, 플랫폼 스레드)**와의 마운트(Mount) 및 마운트 해제(Unmount) 과정은 다음과 같이 동작합니다.

### 캐리어 스레드란?

캐리어 스레드는 OS 플랫폼 스레드를 의미하며, JVM이 관리하는 실제 물리적 스레드입니다.

가상 스레드는 캐리어 스레드에 마운트되어 실행됩니다.

한 번에 여러 가상 스레드가 캐리어 스레드에 스케줄링되어 실행됩니다.

<br>

### 마운트와 마운트 해제 과정

가상 스레드는 Blocking I/O 연산과 같은 작업이 발생할 때, 다음 단계를 거칩니다.

**Step 1: 마운트(Mount)**

- 가상 스레드가 시작되면 JVM은 해당 가상 스레드를 실행하기 위해 캐리어 스레드에 마운트합니다.
- 이때, 가상 스레드는 물리적 스레드 위에서 실행되기 시작합니다.

<br>

**Step 2: 차단 호출 발생**

예를 들어, 가상 스레드에서 다음과 같은 Blocking I/O 작업이 발생합니다

```java
InputStream input = socket.getInputStream();
int data = input.read(); // Blocking 호출
```

read()는 데이터가 도착할 때까지 **차단(blocking)**되며, 가상 스레드는 더 이상 실행되지 않습니다.

<br>

**Step 3: 마운트 해제(Unmount)**

가상 스레드가 차단 상태에 들어가면, JVM은 가상 스레드를 캐리어 스레드에서 분리(Unmount)합니다.
캐리어 스레드는 차단 상태에 있는 가상 스레드를 더 이상 점유하지 않고, 다른 가상 스레드로 전환하여 작업을 계속 처리합니다.

**핵심: 차단된 가상 스레드는 더 이상 캐리어 스레드 리소스를 점유하지 않습니다.**

<br>

**Step 4: 차단 해제 (Ready State)**

I/O 작업이 완료되면, JVM은 차단 상태에 있던 가상 스레드를 다시 캐리어 스레드에 마운트하여 작업을 재개합니다.

이때, 가상 스레드는 이전에 차단되었던 부분에서 다시 실행을 시작합니다.

<br>

### 가상 스레드와 캐리어 스레드의 효율성

가상 스레드는 경량화되어 있으며, 차단 호출이 발생해도 캐리어 스레드 리소스를 점유하지 않기 때문에, 물리적 스레드 자원이 효율적으로 사용됩니다.

수천, 수백만 개의 가상 스레드가 존재해도, 제한된 수의 캐리어 스레드가 이를 처리할 수 있습니다.

차단 상태의 가상 스레드는 메모리와 같은 저렴한 리소스만 점유.

---

## 📚 다른 Threading Model과 비교

아래 테이블은 저번에 배웠던 Thread-Per-Task 모델과 Thread-Per-Core 모델을 가상 스레드와 비교한 표 입니다.

가상 스레드를 사용하면, Blocking I/O를 사용하는 Thread-Per-Task 모델의 성능과,

Non-Blocking I/O를 사용하는 Thread-Per-Core 모델의 안전성을 둘 다 챙길 수 있습니다.

|| Blocking IO + Thread Per Task     | Non Blocking IO + Thread Per Core | Virtual Threads |
|---|-----------------------------------|-----------------------------------|-----------------|
|Performance| 높은 메모리 사용 & 컨텍스트 스위칭              | 최상                                | 최상              |
|Safety & Stability| 제어 역전(Inversion of Control / IoC) | 이슈 없음                             | 이슈 없음           |
|Code Writing| 쉬움                                | 어려움                               | 쉬움              |
|Code Reading| 쉬움                                |어려움                                   | 쉬움              |
|Testing| 쉬움                                |어려움                                   | 쉬움              |
|Debugging| 쉬움                                |어려움                                   | 쉬움              |

---

## 📚 Virtual Thread 간단 구현 예시

가상 스레드르 사용해 처리량 개선을 측정하기 위해 Virtual Thread를 이용해 10000개의 작업과 각 작업당 100개의 Blocking I/O Call을 수행하는 예시 코드를 구현 해보겠습니다.

<br>

**Blocking I/O 효율성**

- Thread.sleep(10)과 같은 Blocking Call이 발생하면, 가상 스레드는 캐리어 스레드에서 마운트 해제됩니다.
- 차단된 가상 스레드는 더 이상 물리적 자원을 점유하지 않고, 캐리어 스레드는 다른 가상 스레드를 처리할 수 있습니다.
- 이로 인해 Blocking 작업이 많은 경우에도 효율적으로 동작합니다.

<br>

아래 코드에서는 `newVirtualThreadPerTaskExecutor()`를 사용했지만 가상스레드가 아닌, Thread-Per-Task 모델을 사용하면서 `newCachedThreadPool()`을 사용하면, 어플리케이션이 충돌해 OutofMemoryError가 뜨게 됩니다.

이 현상을 스레싱(Thrashing)이라고 하며, 시스템이 Thread의 컨텍스트 스위칭에 과도한 시간을 소비하거나, OS가 플랫폼 스레드를 더 많이 할당하는 것을 거부했기 때문입니다. (플랫폼 스레드 수가 많아지면서 컨텍스트 전환 비용 증가, CPU 경쟁, 메모리 부족 발생)

```java
@Slf4j
public class SingleCoreOnlineStore {
    private static final int NUMBER_OF_TASKS = 10_000;

    public static void main(String[] args) {
        log.info("실행 작업 수 : {}", NUMBER_OF_TASKS);

        long start = System.currentTimeMillis();
        performTasks();
        log.info("작업 수행 완료 시간 : {}ms", System.currentTimeMillis() - start);
    }

    // newVirtualThreadPerTaskExecutor()를 사용해 작업이 생길때마다 가상 스레드를 동적으로 생성
    private static void performTasks() {
        try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {

            for (int i = 0; i < NUMBER_OF_TASKS; i++) {
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        for (int j = 0; j < 100; j++) {
                            blockingIoOperation();
                        }
                    }
                });
            }
        }
    }

    // Blocking I/O
    private static void blockingIoOperation() {
        log.info("Blocking I/O 수행 Thread : {}", Thread.currentThread());
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
```

<br>

**실행 결과**

어플리케이션 내부에서 에러 없이 가상 스레드 풀인 ForkJoinPool을 할당해 동적으로 캐리어 스레드와 가상 스레드의 마운트/마운트해제를 하며 작업이 잘 완료되었습니다.

이런 유형의 작업은 Thread-Per-Core 모델을 Non-Blocking I/O로 구현해도 비슷한 성능을 낼 수 있습니다.

결론은 기존 스레드의 컨텍스트 스위칭 부하보다 훨씬 적은 부하로 수 많은 작업을 처리할 수 있게 되었습니다.

```text
inPool-1-worker-12
10:56:32.557 [] INFO com.thread.virtual.SingleCoreOnlineStore -- Blocking I/O 수행 Thread : VirtualThread[#8061]/runnable@ForkJoinPool-1-worker-12
10:56:32.557 [] INFO com.thread.virtual.SingleCoreOnlineStore -- Blocking I/O 수행 Thread : VirtualThread[#6063]/runnable@ForkJoinPool-1-worker-12
10:56:32.557 [] INFO com.thread.virtual.SingleCoreOnlineStore -- Blocking I/O 수행 Thread : VirtualThread[#9178]/runnable@ForkJoinPool-1-worker-12
10:56:32.557 [] INFO com.thread.virtual.SingleCoreOnlineStore -- Blocking I/O 수행 Thread : VirtualThread[#296]/runnable@ForkJoinPool-1-worker-12

...
...


10:56:32.735 [] INFO com.thread.virtual.SingleCoreOnlineStore -- Blocking I/O 수행 Thread : VirtualThread[#3942]/runnable@ForkJoinPool-1-worker-16
10:56:32.735 [] INFO com.thread.virtual.SingleCoreOnlineStore -- Blocking I/O 수행 Thread : VirtualThread[#1668]/runnable@ForkJoinPool-1-worker-17
10:56:32.751 [main] INFO com.thread.virtual.SingleCoreOnlineStore -- 작업 수행 완료 시간 : 7123ms
```

---

## 📚 정리

### 스레드 안전성(Thread Safety)

경합 상태, 교착 상태, 데이터 경합 등을 방지하는 방법도 가상스레드에서 100% 동일하게 적용되며, Thread간 통신, Lock-Free 알고리즘 등과 관련 모든것도 동일하게 적용됩니다.

- Race Conditions
- Deadlocks
- Data Races
- Inter-Thread Communication
- Lock-Free Algorithms

<br>

### 성능(Performance)

첫번째로 중요한 개념은 **CPU 연산만 필요한 작업**인데, 가상 스레드에서는 어떠한 이점이 없습니다.

플랫폼 스레드 위에 있는 또 다른 수준의 간접 접근법일 뿐이며 성능에는 영향을 미치지 않습니다.

따라서 로직에 Blocking Call이 없다고 확신한다면 가상스레드 보다는 기존 플랫폼 스레드를 사용하는 것이 좋습니다.

위 이유때문에 가상스레드가 기존의 스레드를 완전히 대체하지 못하는 이유가 됩니다.

<br>

성능과 관련된 두번째 개념은 가상 스레드가 Latency 측면에서도 전혀 이점이 없다는 점입니다.

예를 들여 실행하는데 T의 시간이 걸리는 작업 시간의 90%는 DB의 응답을 기다리는데 소요된다면 가상 스레드를 사용하는지 여부는 관계없이 완료까지는 항상 T 만큼의 시간이 필요할겁니다.

가상 스레드를 사용해 얻는 유일한 장점은 `처리량의 증가`입니다.

컴퓨터의 외부 인터페이스의 응답을 기다리는 동안(CPU Idle) 가상 스레드로 다른 작업을 처리할 수 있습니다.

<br>

### 짧고 빈번한 Blocking Calls

이런 로직을 수행해야 한다면, 기존 스레드를 사용하기보다 가상 스레드를 사용하는게 더 나은 선택이 될 겁니다.

가상 스레드를 사용하면 위에서 말한바와 같이 컨텍스트 스위칭 비용이 아닌, JVM 내부에서 가상 스레드를 마운트/언마운트 하는 비용만 들어가기 떄문입니다.

<br>

### Thread Pool 크기 고정 불가능

일단 가상스레드는 고정된 크기로 만들 수 없습니다. 기존 스레드에서의 `Executgor.newFixedThreadPool()` 같이 풀의 크기를 정하지 못하며,

JVM이 내부적으로 자체 스케쥴링하므로 개발자가 수동으로 제한할 필요가 없습니다.

또, 가상 스레드는 플랫폼 스레드보다 경량화되어 있기 때문에, 대부분의 경우 풀 크기를 조정하지 않아도 성능상의 문제가 발생하지 않습니다.

<br>

### Daemon-Thread

또 하나 중요한것은 가상 스레드는 항상 `데몬 스레드`로 실행된다는 점입니다. 명시적으로 `virtualThread.setDaemon()` 을 호출하면 Exception이 날겁니다.

이 말은, 가상 스레드는 어플리케이션이 종료되는 것을 절대 막지 못한다는 의미이고,

그래서 스레드의 우선순위를 설정하는 것도 `virtualThreead.setPriority()` 아무 의미가 없으며, 설정을 해도 값은 무시됩니다.

<br>

### 가시성 및 디버깅

디버깅 도구를 사용할 떄 가상 스레드가 캐리어 스레드 위에서 실행되고 있다는 사실은 숨겨집니다.

그래서 BreakPoint를 설정하거나 가상 스레드의 상태를 보고 싶을때, 디버깅 도구를 활용해 플랫폼 스레드와 동일하게 만들면 됩니다.

일반적인 트러블슈팅 도구의 대부분은 가상 스레드를 다른 스레드처럼 다루도록 제공되므료 특별히 할 것이 없습니다.

하지만 가상 스레드의 수가 많아 수천,수만개의 스레드가 생길때에는 디버깅이 힘들 수 있습니다.

<br>

### Semaphore를 이용한 ForkJoinPool 동시 실행 크기 고정

그래도 혹시나 고정된 풀 크기가 필요한 경우 아래처럼 Semaphore를 활용하거나, Executor를 커스터마이징 하거나, Blocking Queue를 활용할 수 있습니다.

```java
@Slf4j
public class FixedVirtualThreadPool {
    private static final int MAX_CONCURRENT_THREADS = 100; // 최대 동시 실행 스레드 수
    private static final Semaphore semaphore = new Semaphore(MAX_CONCURRENT_THREADS);

    public static void main(String[] args) throws InterruptedException {
        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

        for (int i = 0; i < 1000; i++) {
            semaphore.acquire(); // 실행 전 세마포어 획득
            executorService.submit(() -> {
                try {
                    log.info("Thread : {}", Thread.currentThread());
                    Thread.sleep(1000); // 작업 수행
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    semaphore.release(); // 작업 완료 후 세마포어 해제
                }
            });
        }

        executorService.shutdown();
        executorService.close();
    }
}
```

<br>

**Semaphore 100개로 제한했을떄 작업 시간 - 10133ms**

```text
13:33:41.303 [] INFO com.thread.virtual.FixedVirtualThreadPool -- Thread : VirtualThread[#1037]/runnable@ForkJoinPool-1-worker-7
13:33:41.303 [] INFO com.thread.virtual.FixedVirtualThreadPool -- Thread : VirtualThread[#1045]/runnable@ForkJoinPool-1-worker-6
13:33:41.303 [] INFO com.thread.virtual.FixedVirtualThreadPool -- Thread : VirtualThread[#1046]/runnable@ForkJoinPool-1-worker-6
13:33:42.320 [main] INFO com.thread.virtual.FixedVirtualThreadPool -- 작업 완료 시간: 10133ms

Process finished with exit code 0
```

<br>

**Semaphore 1000개로 제한했을때 작업 시간 - 1048 ms**

```text
13:34:43.163 [] INFO com.thread.virtual.FixedVirtualThreadPool -- Thread : VirtualThread[#827]/runnable@ForkJoinPool-1-worker-16
13:34:43.162 [] INFO com.thread.virtual.FixedVirtualThreadPool -- Thread : VirtualThread[#816]/runnable@ForkJoinPool-1-worker-18
13:34:43.164 [] INFO com.thread.virtual.FixedVirtualThreadPool -- Thread : VirtualThread[#982]/runnable@ForkJoinPool-1-worker-8
13:34:44.186 [main] INFO com.thread.virtual.FixedVirtualThreadPool -- 작업 완료 시간: 1048ms

Process finished with exit code 0
```