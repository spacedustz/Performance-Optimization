## 📚 Virtual Thread

JDK에 정식 도입된 Virtual 스레드는 기존의 KLT(kernel-level 스레드)와 ULT(user-level 스레드)를 1:1 매핑하여 사용하는 JVM의 스레드 모델을 개선한, 

여러 개의 가상 스레드를 하나의 네이티브 스레드에 할당하여 사용하는 모델이며, 경량 스레드로, JDK 19에서 미리보기의 일부로 처음 공개 되었습니다. 

가상 스레드는 OS 스레드에 비해 매우 적은 자원을 사용하여 수천 개의 스레드를 효율적으로 실행할 수 있도록 설계되었고,

이를 통해 고성능 서버 애플리케이션이 더 높은 동시성을 쉽게 구현할 수 있으며, 전통적인 스레드 풀의 복잡한 설정 없이도 많은 작업을 동시에 처리할 수 있습니다.

---

## 📚 Java Thread와 OS Thread와의 관계

**Java의 Thread 클래스**

Java에서 생성되는 스레드는 OS 스레드와 밀접하게 연결됩니다. 

Thread 클래스의 `start()` 메서드를 호출하면, JVM은 새로운 OS 스레드를 생성하도록 요청하며, 이 스레드에 특정 크기의 스택 공간이 할당되고, 이 공간은 스레드가 실행하는 코드와 로컬 변수를 저장하는 데 사용됩니다.

Java의 기본 스레드는 OS 스레드를 감싸고 있으며, JVM 내의 각 스레드는 OS 스레드와 1:1로 매핑됩니다. 

이러한 설계는 OS가 CPU 스케줄링과 스레드 실행을 직접 관리하게 하지만, OS 스레드 자원이 제한되어 있어 무거운 작업에 부담이 될 수 있습니다.

<br>

**Virtual Thread**

Java의 가상 스레드는 기존의 스레드처럼 start()나 run() 메서드를 통해 시작할 코드를 가지고 있지만, OS 스레드와는 달리 완전히 JVM에서 관리됩니다.

따라서 OS는 가상 스레드의 존재를 인지하지 못하며, 가상 스레드는 필요 없어지면 GC(Garbage Collection)에 의해 자동으로 정리됩니다.

이러한 구조 덕분에 가상 스레드는 생성과 관리에 드는 비용이 매우 적고, 대량으로 효율적으로 생성할 수 있습니다.

<br>

**가상 스레드의 실행 방식**

가상 스레드가 실행될 때, JVM은 내부에 작은 스레드 풀을 사용하여 가상 스레드를 마운트합니다. 이 스레드 풀에 포함된 실제 OS 스레드들은 캐리어 스레드(Carrier Thread)라고 불리며, 가상 스레드를 실행시키기 위한 자원으로 사용됩니다. 가상 스레드가 끝나면 캐리어 스레드에서 마운트가 해제되고, 해제된 캐리어 스레드는 다음 가상 스레드를 실행할 준비를 합니다. 마운트가 해제된 가상 스레드는 필요 시 GC에 의해 제거됩니다.

<br>

**일시 정지와 스냅샷 저장**

특정 상황에서 가상 스레드가 실행을 잠시 멈춰야 한다면, JVM은 캐리어 스레드에서 가상 스레드를 마운트 해제하고 그 상태를 힙 메모리에 저장합니다. 

이 저장된 상태에는 명령 포인터와 캐리어 스레드의 스택 상태가 포함되며, 이를 스냅샷이라고 합니다.

이후 가상 스레드가 재개될 때, JVM은 스냅샷을 기반으로 캐리어 스레드의 스택 메모리를 복원하여 중단된 시점에서 이어서 실행하게 됩니다.

<br>

**개발자와 JVM의 역할**

가상 스레드와 캐리어 스레드는 JVM이 자동으로 관리하므로, 개발자는 별도로 제어할 필요가 없습니다. 

이로 인해 가상 스레드는 복잡한 설정 없이도 대량의 스레드를 쉽게 활용할 수 있는 유연성을 제공합니다.

가상 스레드의 이러한 특성 덕분에 Java 애플리케이션은 높은 동시성을 가볍게 구현할 수 있게 되었습니다.


---

## 📚 Virtual Thread 생성

우선 Virtual Thread를 사용하기 위해 JDK21을 따로 설치했습니다.

테스트를 하기 전 미리 정의된 스레드풀을 만들지 않고 OS가 알아서 스레드풀의 개수를 지정하도록 아무런 설정도 하지 않은 상태로 수행합니다.

그럼 위에서 설명한것과 같이 가상 스레드의 스케줄링 과정을 보기위해 스레드의 진행을 방해하는 100개의 Blocking I/O 연산(Blocking Task 클래스)을 넣어 테스트 해보았습니다.

`Thread.ofVirtual()`

- 가상 스레드 팩토리를 생성합니다. 이 팩토리는 경량 스레드를 생성하며, 운영 체제 스레드(OS 스레드)가 아닌 JVM 수준에서 관리되는 스레드를 만듭니다.
- 가상 스레드는 일반 운영 체제 스레드와 달리, 작은 메모리 오버헤드와 높은 생성/소멸 속도를 가지고 있습니다. 
- 블로킹 작업을 수행할 때도 효율적이며, 특히 I/O 작업에서 유용합니다.

<br>

`unstarted`

- 스레드를 생성하되, 즉시 실행하지 않고 비활성 상태로 둡니다. 이후 start() 메서드를 호출해야 실행됩니다. 
- 이 메서드를 사용하면 생성과 실행 시점을 분리할 수 있어, 필요한 시점에만 스레드를 시작할 수 있습니다.

```java
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
```

<br>

### 실행 결과

실행 결과에 로그로 찍은 스레드의 정보를 보면 **고유 ID, Thread Pool Name, Worker(Thread) Name**이 보입니다. 

가상 스레드를 스케줄링 하기 위해 JVM이 캐리어 스레드로 구성된 스레드 풀을 만들고 각 캐리어 스레드에 가상 스레드를 마운팅하고 그 후 JVM 내부 스레드 풀인 ForkJoinPool을 만들게 됩니다.

로그를 보면 worker의 숫자가 최대 18까지만 있는것을 보니 캐리어 스레드풀은 18개의 플랫폼 스레드로 이루어져 있는것을 알 수 있고, 18개의 캐리어 스레드로 가상 스레드 100개를 스케줄링 합니다.

18개의 스레드풀이 생긴 이유는 따로 스레드 풀을 정의하지 않았고 현재 사용중인 노트북의 코어가 18개이기 떄문에 18개의 스레드로 이루어진 풀이 생겼습니다.

```text
14:35:53.755 [] INFO com.thread.virtual.VirtualThreadWithBlockingCalls -- Inside Thread : VirtualThread[#47]/runnable@ForkJoinPool-1-worker-18 Before Blocking Call
14:35:53.755 [] INFO com.thread.virtual.VirtualThreadWithBlockingCalls -- Inside Thread : VirtualThread[#43]/runnable@ForkJoinPool-1-worker-14 Before Blocking Call
14:35:53.755 [] INFO com.thread.virtual.VirtualThreadWithBlockingCalls -- Inside Thread : VirtualThread[#40]/runnable@ForkJoinPool-1-worker-11 Before Blocking Call
14:35:53.760 [] INFO com.thread.virtual.VirtualThreadWithBlockingCalls -- Inside Thread : VirtualThread[#48]/runnable@ForkJoinPool-1-worker-2 Before Blocking Call
14:35:53.760 [] INFO com.thread.virtual.VirtualThreadWithBlockingCalls -- Inside Thread : VirtualThread[#50]/runnable@ForkJoinPool-1-worker-10 Before Blocking Call
14:35:53.760 [] INFO com.thread.virtual.VirtualThreadWithBlockingCalls -- Inside Thread : VirtualThread[#51]/runnable@ForkJoinPool-1-worker-6 Before Blocking Call
14:35:53.760 [] INFO com.thread.virtual.VirtualThreadWithBlockingCalls -- Inside Thread : VirtualThread[#66]/runnable@ForkJoinPool-1-worker-16 Before Blocking Call

...

14:35:54.777 [] INFO com.thread.virtual.VirtualThreadWithBlockingCalls -- Inside Thread : VirtualThread[#105]/runnable@ForkJoinPool-1-worker-2 After Blocking Call
14:35:54.777 [] INFO com.thread.virtual.VirtualThreadWithBlockingCalls -- Inside Thread : VirtualThread[#108]/runnable@ForkJoinPool-1-worker-5 After Blocking Call
14:35:54.777 [] INFO com.thread.virtual.VirtualThreadWithBlockingCalls -- Inside Thread : VirtualThread[#98]/runnable@ForkJoinPool-1-worker-1 After Blocking Call
14:35:54.777 [] INFO com.thread.virtual.VirtualThreadWithBlockingCalls -- Inside Thread : VirtualThread[#114]/runnable@ForkJoinPool-1-worker-5 After Blocking Call
14:35:54.777 [] INFO com.thread.virtual.VirtualThreadWithBlockingCalls -- Inside Thread : VirtualThread[#120]/runnable@ForkJoinPool-1-worker-8 After Blocking Call
14:35:54.777 [] INFO com.thread.virtual.VirtualThreadWithBlockingCalls -- Inside Thread : VirtualThread[#129]/runnable@ForkJoinPool-1-worker-7 After Blocking Call
14:35:54.777 [] INFO com.thread.virtual.VirtualThreadWithBlockingCalls -- Inside Thread : VirtualThread[#126]/runnable@ForkJoinPool-1-worker-18 After Blocking Call

```

---

## 📚 결론

가상스레드를 사용하기 전 쓰던 스레드는 플랫폼 스레드라고 하며 가상스레드를 싣는 캐리어 스레드라고도 합니다.

많은 수의 가상 스레드가 이 캐리어 스레드 풀에서 처리되기 떄문에 기존에 사요하던 스레드에 비해 리소스의 사용이 최소화됩니다.

이 플랫폼 스레드는 고정된 크기의 스택을 포함하고 생성/해제 비용이 비싸며 제한된 리소스인 OS의 Thread와 1:1 매핑됩니다.

<br>

그에 비해 가상 스레드는 Heap 메모리를 할당받는 자바의 객체와 더 유사한 구조를 가지며,

가상 스레드는 플랫폼 스레드 중 하나에 마운트되어 실행되고, 블로킹 상태가 되면 플랫폼 스레드는 다른 가상 스레드에 할당됩니다.

또 기존 OS 스레드는 한정된 수를 가지는 반면 가상 스레드는 캐리어 스레드를 통해 소천개의 가상 스레드를 동시에 실행할 수 있고, 대기 상태일 때 리소스를 거의 사용하지 않습니다.

<br>

이 글에서 가상스레드의 기본적인 동자원리만 알아보았고 다음 글에서는 고성능 I/O를 요구하는 어플리케이션에서의 가상 스레드 사용 예제와 모범 사례등을 학습해서 작성해 보겠습니다.