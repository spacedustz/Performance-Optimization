## 📚 Lock-Free Algorithm

Lock-Free 알고리즘은 여러개의 Thread에서 동시 호출 시 정해진 단위 시간마다 적어도 1개의 호출이 완료되는 알고리즘이며, 동시성 제어에서 중요한 개념중 하나입니다.

간단히 요약하면 **Lock을 사용하지 않고 Atomic 연산을 통해 구현하며, 하드웨어 레벨에서 연산의 원자성을 보장합니다.**

<br>

**유의할 점**

- CAS(Compare And Set)를 사용하는 Atomic 클래스는 **Thread 간 경합이 있을 때** 성능이 다소 낮아질 수 있십니다.
- 하지만 여전히 Lock을 사용하지 않기 때문에 Lock과 비교해 더 빠르고 데드락이 없는 Lock-Free 구현이 가능합니다.
- CAS에 

<br>

물론 Lock을 사용 하는게 더 좋다거나 사용 안한다고 더 좋다기 보다 상황에 맞게 선택할 수 있는 선택지를 늘리는 차원에서 공부해 보았습니다.

그럼 왜 Lock이 있는데 Lock을 사용하지 않는지, 왜 위험한지 공부할겸 이유를 다시 적어보겠습니다.

<br>

### Lock 사용 시 문제점

**Slow Critical Section**

- 같은 Lock 을 사용하는 멀티 스레드가 있는 경우
- 내용이 많거나 긴 임계영역은 처음 스레드에게 Lock을 주고 다른 스레드를 계속 대기하게 합니다.
- 그 스레드 수가 많다면 다른 모든 스레드는 작업을 하지 못할 겁니다.

<br>

**Priority Inversion**

- 리소스 하나와 그 리소스의 Lock을 공유하는 스레드가 2개일때 생기는 문제입니다.
- 예를 들어 백그라운드 문서 저장 스레드와 UI 스레드 중 UI가 우선순위가 높을 경우입니다.
- 만약 Priority가 낮은 스레드가 Lock을 얻고 OS는 우선순위가 높은 스레드를 계속 스케줄링 하려고 하지만,
- 우선순위가 낮은 스레드가 Lock을 Release 할 때까지 계속 대기하게 되서 우선순위가 뒤바뀌는 것처럼 보이는 현상입니다.
- 이 문제는 특히 실시간 시스템(예: 게임 UI나 사용자 인터페이스)에서 문제가 되는데, 사용자는 우선순위가 높은 스레드가 응답하지 않기 때문에 시스템이 멈춘 것처럼 느끼게 됩니다.

<br>

**Kill Tolerance**

- 우선순위 역전이나 느린 임계영역보다 훨씬 치명적인건 Lock을 가진 스레드가 그냥 죽거나 인터럽트 될 때 입니다.
- Unlock 하는걸 깜빡 하는 경우도 포함입니다.
- 그 결과는 모든 다른 스레드가 영원히 정체되고 데드락처럼 회복되지 않을겁니다.
- 그래서 항상 중요한 영역에 타임아웃을 가진 `tryLock()`을 사용해 복잡한 코드를 사용하도록 강제됩니다.

<br>

**Performance**

- Lock을 얻기위한 스레드 간 다툼이 발생하는 문제인데 이때 성능 오버헤드가 있습니다.
- 스레드 A,B가 있다고 할떄 A가 Lock을 획득하면 B가 계속 가지려 하고 Block 됩니다.
- 이떄 B에서 다른 스레드로의 컨텍스트 스위칭이 일어나고 Lock이 Release 될 떄 다시 B를 가져오는 오버헤드가 생깁니다.
- 이런 오버헤드는 대부분의 어플리케이션에서는 감지하지 못하지만, 밀리초보다 짧은 지연 시간으로 연산하는 고속 거래시스템에서는 고래해야 할 중요 요소입니다.

---

## 📚 Atomic Operation

Atomic Operation(AtomicInteger, AtomicLong, AtomicBoolean 등등)은 Java에서 제공하는 클래스 중 하나로, 원자적인 정수 연산을 지원하고 멀티스레드 환경에서 안전하게 정수 값을 관리할 수 있습니다.

이 클래스를 통해 락이나 동기화를 사용하지 않으니 Race Condition이나 Data Race를 걱정하지 않아도 됩니디.

<br>

### 장점

**원자성**

AtomicInteger는 내부적으로 synchronized 블록이나 다른 동기화 메커니즘 없이도 원자적인 연산을 제공합니다. 

이는 여러 스레드가 동시에 접근하더라도 데이터의 일관성을 유지할 수 있게 해줍니다.

<br>

**성능**

일반적인 동기화 방법보다 더 가볍고 빠르며, 락을 사용하지 않기 때문에 성능이 향상됩니다. 

이는 특히 높은 동시성이 요구되는 경우에 유리합니다.

<br>

**CAS (Compare-And-Swap)**

AtomicInteger는 CAS 알고리즘을 사용하여 값의 변경을 수행합니다. 

메모리의 특정 값이 예상 값과 일치할 때만 새로운 값으로 원자적으로 교체하는 연산입니다. 

예상한 값과 실제 값이 다르면 교체하지 않고 단순히 실패를 반환하므로, 여러 스레드가 동시에 같은 자원에 접근할 수 있습니다.

<br>

**주요 메서드**

- incrementAndGet(): 현재 값을 1 증가시키고, 증가된 값을 반환합니다.
- decrementAndGet(): 현재 값을 1 감소시키고, 감소된 값을 반환합니다.
- addAndGet(int delta): 지정된 값만큼 더하고, 결과를 반환합니다.
- compareAndSet(int expect, int update): 현재 값이 expect와 같으면 update로 변경하고, 성공 여부를 반환합니다.

<br>

### 단점

Atomic 관련 함수들은 원자적인 특성을 갖고 있지만, 다른 연산과 함께 원자적으로 실행할 수 없습니다.

예를 들어 아래 incrementAndGet 함수와 addAndGet 함수는 각각 원자적인 연산이지만,

**두 연산 간의 관계가 원자적이지 않기 때문에** Race Condition이 발생할 수 있습니다.

<br>

### Lock-Free 예제

기존에 사용하던 Lock이나 synchronized 키워드없이 AtomicInteger를 통해 InventoryCounter를 10000번씩 증가/감소 시키는 연산을 원자적으로 할 수 있습니다.

결과값은 10000번의 increment를 할떄까지 Decrementing Thread가 대기후 다시 10000번을 감소시켜 0이 나오게 됩니다.

**만약 싱글스레드 환경이라면** 일반 Integer를 사용하는 것보다 성능상 느리기 떄문에 원자적 연산이 중요한 로직에서만 사용할 것을 추천드립니다.

```java
@Slf4j
public class ECommerceInventoryCounter {
    public static void main(String[] args) throws InterruptedException {
        InventoryCounter counter = new InventoryCounter();
        IncrementingThread incrementingThread = new IncrementingThread(counter);
        DecrementingThread decrementingThread = new DecrementingThread(counter);

        incrementingThread.start();
        decrementingThread.start();

        incrementingThread.join();
        decrementingThread.join();

        log.info("현재 보유한 아이템 수 : {}", counter.getItems());
    }

    // 증가 스레드
    @AllArgsConstructor
    public static class IncrementingThread extends Thread {
        private InventoryCounter inventoryCounter;

        @Override
        public void run() {
            for (int i=0; i<10000; i++) {
                inventoryCounter.increment();
            }
        }
    }

    // 감소 스레드
    @AllArgsConstructor
    public static class DecrementingThread extends Thread {
        private InventoryCounter inventoryCounter;;

        @Override
        public void run() {
            for (int i=0; i<10000; i++) {
                inventoryCounter.decrement();
            }
        }
    }

    // 인벤토리 카운터
    public static class InventoryCounter {
        private AtomicInteger items = new AtomicInteger(0);

        public void increment() {
            items.incrementAndGet();
        }

        public void decrement() {
            items.decrementAndGet();
        }

        public int getItems() {
            return items.get();
        }
    }
}
```

```text
13:14:04.856 [main] INFO com.thread.lockfree.ECommerceInventoryCounter -- 현재 보유한 아이템 수 : 0
Process finished with exit code 0
```

<br>

다음 글에서는 Lock이 걸리지 않은 Thread-Safe한 데이터 구조를 구현할거고,

이때 유용한 원자적 클래스인 `AtomicReference<T>`와 `CAS(Compare And Set)`에 대해 디테일하게 다루면서,

**락이 걸리지 않은 Lock-Free 구조**와 동일한 데이터 구조의 **Lock을 사용하는 Blocking 구조**의 성능을 비교 해보겠습니다.