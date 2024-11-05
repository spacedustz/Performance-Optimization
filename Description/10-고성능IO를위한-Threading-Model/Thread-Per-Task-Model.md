## 📚 Blocking I/O

블로킹 방식의 I/O를 사용하는 어플리케이션에서 응답 시간이 긴 블로킹 I/O 연산이 포함되어 있다면, 아무리 많은 스레드를 생성하더라도 근본적으로 성능 문제가 발생할 수 있습니다.

긴 블로킹 연산으로 인해, 스레드가 I/O 작업이 완료될 때까지 대기하면서 사용 가능한 CPU 코어가 점차 유휴 상태가 됩니다. 

결과적으로, 이러한 블로킹 호출이 계속 발생하면 서버의 전체 성능이 저하되고 모든 요청 처리에 영향을 미치게 됩니다.

이번 글에서는 블로킹 I/O 연산이 있어도 어플리케이션의 전체 성능을 최적화하기 위해 CPU 자원을 더 효과적으로 활용할 수 있는 다양한 방법들을 알아보았습니다.

---

## 📚 Thread-Per-Task Threading Model(작업 단위 스레딩 모델)

위에서 말했듯 코어 수만큼 스레드를 가지는 어플리케이션에서 블로킹 연산이 있는 경우 최적화된 성능을 얻지 못합니다.

예를 들면 아래의 블로킹 요청에서 `readFromDatabase()` 함수 에서 CPU Idle이 발생하고 스레드가 차단되면 그 동안 CPU는 아무것도 하지 못합니다.

```java
public void handleRequest(HttpExchange exchange) {
    Request request = parseUserRequest(exchange);
    Data data = readFromDatabase(request); // CPU Idle
    sendPageToUser(data, exchange);
}
```

<br>

위 문제를 해결하는 첫번째 방법은, 들어오는 네트워크 요청이나 작업 1개마다 스레드를 추가해주는 것입니다.

이 방밥을 사용하면 싱글 코어 CPU 환경에서도 멀티스레딩을 사용하여 요청이나 작업을 동시에 처리할 수 있고,

멀티 코어 CPU라면 단일 코어가 동시에 처리하는 것과 달리 여러개의 작업이 병렬적으로 수행될 수 있습니다.

<br>

만약 어플리케이션에서 어떤 블로킹 요청이 디스크에서 파일을 읽어 다른 네트워크 요청 or 서비스로 보내거나 최종 쿼리를 원격 데이터베이스에 전송합니다.

이떄 어플리케이션은 이런 블로킹 I/O를 수행하는 동안 아무 작업도 하지 않습니다.

이런 블로킹 호출이 예를 들어 1초가 걸린다고 가정하면, 별도의 스레드에서 각 작업을 수행하고 모든 연산을 이 1초 사이에 동시에 수행하게 만들겁니다.

<br>

### IOBoundApplication V1

이 클래스는 동적 스레드 풀을 사용하여 여러 개의 블로킹 I/O 작업을 동시에 실행함으로써 CPU의 Idle 상태를 최소화 합니다. 

NUMBER_OF_TASKS만큼의 블로킹 작업을 동적 스레드 풀에서 실행하여, 모든 작업이 완료될 때까지 걸린 시간을 측정하고 출력합니다. 

그리고 각 작업은 1초 동안 대기하며, 로그를 통해 각 작업의 시작과 전체 작업 시간을 기록합니다.

<br>

`Thread.sleep()`

- IO 연산이 아니고 Blocking 연산이지만 실제 IO 연산과 유사하게 동작하기 때문에 Blocking IO 연산 테스트로 사용하였습니다.

<br>

`performTasks()`

- ExecutorService는 AutoCloseable을 구현 하지 않기 떄문에, try-with-resource 구문에 Cloeable을 넣어서 Auto-Close를 시켜주면 메인 스레드는 `performTasks()`에서 수행하는 모든 작업이 완료될 때까지 대기하게 됩니다.
- 이 동적 스레드 풀은 각 스레드에 1000개의 작업을 할당하고, 작업을 완료하는데 필요한 스레드를 계속 생성하고 캐시로 저장해 필요할 떄 재사용 합니다.

```java
@Slf4j
public class IOBoundApplicationV1 {
    private static final int NUMBER_OF_TASKS = 1000; // 작업의 수

    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);

        System.out.println("Press Enter to Start");
        s.nextLine();
        log.info("실행 중인 작업 수 : {}", NUMBER_OF_TASKS);

        long start = System.currentTimeMillis();
        performTasks();
        log.info("작업 완료까지 소요 시간 : {}ms", System.currentTimeMillis() - start);
    }

    // Long Blocking IO 테스트
    private static void blockingIoOperation() {
        log.info("Blocking Task 실행 스레드 : {}", Thread.currentThread());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // 동적 스레드 풀 생성 / 작업 할당
    private static void performTasks() {
        ExecutorService executorService = Executors.newCachedThreadPool();

        try (Closeable close = executorService::shutdown) {

            // 새 작업이 실행될 때마다 스레드 풀에 있는 스레드로 실행
            for (int i = 0; i < NUMBER_OF_TASKS; i++) {
                executorService.submit(IOBoundApplicationV1::blockingIoOperation);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
```

<br>

### 실행 결과

1000개의 작업이 단 146ms 만에 처리됨을 볼 수 있습니다. 

1초의 실행 시간을 가지는 Blocking I/O 연산 1000개를 0.1초에 전부 처리했다는 의미입니다.

<br>

**동적 스레드 풀**

동적 스레드 풀을 사용한 이유는 실제 환경에서 동시에 얼마나 많은 작업을 처리해야 하는지 정확한 숫자를 가늠하기는 현실적으로 어렵고,

그 작업들을 수행하기 위해 몇개의 스레드를 미리 할당해야 하는지도 알기 어렵기 때문에 사용하였습니다.

하지만 동적으로 커지는 스레드 풀의 크기가 예를 들어 10000 이상 등등 너무 많이 생성 된다면 운영체제에서 스레드 할당을 거부해 `OutOfMemoryException`을 보낼것이므로,

**만약 작업의 수가 대충 봐도 엄청 많을것 같으면 `Executors.newFixedThreadPool()`을 이용해 스레드풀 의 크기를 static 하게 가져가는게 좋아 보입니다.**

그럼 만약 스레드 풀을 1000개로 고정해놓으면 10만개의 작업이 들어와도 1000개의 스레드만을 사용해 연산을 수행 할 것입니다.

```text
16:34:40.667 [main] INFO com.thread.blockingio.IOBoundApplicationV1 -- 실행 중인 작업 수 : 1000
16:34:40.680 [pool-1-thread-12] INFO com.thread.blockingio.IOBoundApplicationV1 -- Blocking Task 실행 스레드 : Thread[pool-1-thread-12,5,main]
16:34:40.680 [pool-1-thread-1] INFO com.thread.blockingio.IOBoundApplicationV1 -- Blocking Task 실행 스레드 : Thread[pool-1-thread-1,5,main]
16:34:40.681 [pool-1-thread-5] INFO com.thread.blockingio.IOBoundApplicationV1 -- Blocking Task 실행 스레드 : Thread[pool-1-thread-5,5,main]
16:34:40.681 [pool-1-thread-4] INFO com.thread.blockingio.IOBoundApplicationV1 -- Blocking Task 실행 스레드 : Thread[pool-1-thread-4,5,main]
16:34:40.681 [pool-1-thread-21] INFO com.thread.blockingio.IOBoundApplicationV1 -- Blocking Task 실행 스레드 : Thread[pool-1-thread-21,5,main]
16:34:40.680 [pool-1-thread-2] INFO com.thread.blockingio.IOBoundApplicationV1 -- Blocking Task 실행 스레드 : Thread[pool-1-thread-2,5,main]
16:34:40.681 [pool-1-thread-6] INFO com.thread.blockingio.IOBoundApplicationV1 -- Blocking Task 실행 스레드 : Thread[pool-1-thread-6,5,main]

...
...

16:34:40.820 [pool-1-thread-997] INFO com.thread.blockingio.IOBoundApplicationV1 -- Blocking Task 실행 스레드 : Thread[pool-1-thread-997,5,main]
16:34:40.820 [pool-1-thread-998] INFO com.thread.blockingio.IOBoundApplicationV1 -- Blocking Task 실행 스레드 : Thread[pool-1-thread-998,5,main]
16:34:40.820 [pool-1-thread-999] INFO com.thread.blockingio.IOBoundApplicationV1 -- Blocking Task 실행 스레드 : Thread[pool-1-thread-999,5,main]
16:34:40.820 [pool-1-thread-1000] INFO com.thread.blockingio.IOBoundApplicationV1 -- Blocking Task 실행 스레드 : Thread[pool-1-thread-1000,5,main]
16:34:40.820 [main] INFO com.thread.blockingio.IOBoundApplicationV1 -- 작업 완료까지 소요 시간 : 146ms

Process finished with exit code 0
```

<br>

### IOBoundApplicationV2

이번엔 로직을 좀 바꿔서 Blocking 시간을 1000ms 에서 10ms로 바꾸고 각 스레드는 이 Blocking 호출을 100번씩 실행하게 해보겠습니다.

이론적으로는 아무것도 바뀌지 않았고 각 작업의 총 Blocking 시간은 여전히 1000 밀리초 입니다.

```java
@Slf4j
public class IOBoundApplicationV2 {
    private static final int NUMBER_OF_TASKS = 1000; // 작업의 수

    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);

        System.out.println("Press Enter to Start");
        s.nextLine();
        log.info("실행 중인 작업 수 : {}", NUMBER_OF_TASKS);

        long start = System.currentTimeMillis();
        performTasks();
        log.info("작업 완료까지 소요 시간 : {}ms", System.currentTimeMillis() - start);
    }

    // Long Blocking IO 테스트
    private static void blockingIoOperation() {
        log.info("Blocking Task 실행 스레드 : {}", Thread.currentThread());

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static void performTasks() {
        ExecutorService executorService = Executors.newCachedThreadPool();

        try (Closeable close = executorService::shutdown) {

            // 새 작업이 실행될 때마다 스레드 풀에 있는 스레드로 실행
            for (int i = 0; i < NUMBER_OF_TASKS; i++) {
                executorService.submit(() -> {
                    for (int j = 0; j < 100; j++) {
                        blockingIoOperation();
                    }
                });
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
```

<br>

### 실행 결과

이론적으로 바뀐게 없더라도, 실행해보면 수행 시간은 기존 146ms보다 훨씬 느려진 2220ms 가 찍힙니다.

그 이유는 각 작업에서 100개의 블로킹 연산을 수행할 때 99번의 Context-Switching이 발생하기 떄문입니다.

이 스레드 스케쥴링을 취소하고 다시 배정하는 컨텍스트 스위칭에 발생하는 부하는 많은 시간을 소요하고,

실제 전체 블로킹 연산에 드는 시간보다 더 많은 시간을 소모합니다.

```text
17:02:54.523 [pool-1-thread-847] INFO com.thread.blockingio.IOBoundApplicationV2 -- Blocking Task 실행 스레드 : Thread[pool-1-thread-847,5,main]
17:02:54.538 [pool-1-thread-998] INFO com.thread.blockingio.IOBoundApplicationV2 -- Blocking Task 실행 스레드 : Thread[pool-1-thread-998,5,main]
17:02:54.538 [pool-1-thread-848] INFO com.thread.blockingio.IOBoundApplicationV2 -- Blocking Task 실행 스레드 : Thread[pool-1-thread-848,5,main]
17:02:54.554 [pool-1-thread-998] INFO com.thread.blockingio.IOBoundApplicationV2 -- Blocking Task 실행 스레드 : Thread[pool-1-thread-998,5,main]
17:02:54.570 [main] INFO com.thread.blockingio.IOBoundApplicationV2 -- 작업 완료까지 소요 시간 : 2220ms
```

---

## 요약 

**장점**

- 작업 단위 스레드 모델을 사용해 CPU 성능과 하드웨어 효율 향상
- 1코어=1스레드 일떄에 비해 모든 작업을 동시에 수행함에 따라 훨씬 빠른 작업 속도

<br>

**단점**

- 스레드는 비용이 큰 리소스이며 실행하는 OS와 설정에 따라 생성할 수 있는 스레드 수는 제한됨
- 이번 테스트에서 Blocking 호출 외에 다른 작업을 하지 않았음에도 Stack Memory와 다른 Resource들을 소모함
- 따라서 수천개의 스레드를 할당해야 하는 작업이 발생했을때, 스레드의 수를 제한하지 않으면 어플리케이션 충돌 위험이 있음
- 반대로, 필요한 것보다 적은 수로 스레드를 제한하면 또 원하는 만큼의 처리량이 나오지 않ㅎ을 수 있음 (CPU를 효율적으로 사용하지 못함)
- 즉 스레드 풀의 스레드 수와 처리량은 비례함
- 마지막으로, 블로킹 호출이 빈번히 일어나는 상황에서 **스레싱(Threshing)**이 발생할 수 있음

<br>

**다음에 배워볼 것**

- 다른 접근 방법을 이용해 블로킹 호출과 IO 블로킹 호출이 있는 작업을 관리하는 샘플 개발
- Core-Per Threading Model(코어 당 작업 스레딩 모델) 학습 예정