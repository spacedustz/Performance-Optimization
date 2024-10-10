## 📚 Object를 이용한 Thread 간 통신 / 동기화

지난 글에서 멀티스레딩 환경에서 여러 스레드 간 통신 및 동기화가 필요할 때 Semaphore / Binary Semaphore를 사용 했었습니다.

이번 글은 **로직의 복잡성이 낮고 간단하며 직관적인 락 제어가 필요한 경우** Object 객체의 `wait()`, `notify()`, `notifyAll()` 을 이용한 동기화/통신 방법에 대한 글을 작성합니다.

`wait()`, `notify()`, `notifyAll()` 함수를 사용하면 효율적으로 스레드를 제어할 수 있습니다.

---

## 📚 wait(), notify(), notifyAll()

wait(), notify(), notifyAll()은 Object 클래스에 정의된 함수들로 모든 객체에서 사용 가능합니다.

그러므로, 모든 객체는 하나의 `모니터 락(Monitor Lock)`을 가지고 있어서, 특정 조건을 만족할 때까지 스레드가 기다리거나 다른 스레드에게 작업을 전달할 수 있습니다.

이 함수들은 스레드가 어떤 특정한 조건이 만족될 때까지 대기하게 하거나, 대기 중인 스레드를 깨우기 위해 사용됩니다.

<br>

그리고 Object의 wait()는 모니터 락(Monitor Lock)을 일시적으로 해제합니다. 

wait()를 호출한 스레드는 해당 객체의 모니터 락을 가지고 있다가, wait()가 호출되면 그 락을 해제하고 대기 상태로 들어가고, 이 과정에서 다른 스레드가 그 모니터 락을 획득할 수 있게 됩니다.

하지만 중요한 건, 스레드는 여전히 그 객체의 모니터 락에 관련된 대기열에 속해 있고. wait() 상태에 있는 스레드는 notify()나 notifyAll()을 통해서 깨어나며, 다시 모니터 락을 획득해야만 실행을 이어나갈 수 있습니다.

<br>

### wait()

- wait() 메서드는 현재 스레드가 호출한 객체의 모니터 락을 해제하고 대기 상태로 전환시킵니다. 
- 이 스레드는 다른 스레드가 notify() 또는 notifyAll()을 호출해 자신을 깨워줄 때까지 대기하게 됩니다.
- 중요한 점은 `wait()는 반드시 synchronized 블록 안에서 호출해야 합니다.`

<br>

> 동기화 시 ReentrantLock을 사용하면 안되나?

ReentrantLock은 보다 세밀한 락 제어가 가능한 동기화 도구지만, 모니터 락을 제공하지 않기 때문에 Object의 wait(), notify(), notifyAll() 메서드와는 같이 쓸 수 없습니다.

wait()와 notify()는 모니터 락에 기반해서 동작하기 때문에 반드시 synchronized 블록 안에서만 호출해야 하고, 이 규칙을 어기면 `IllegalMonitorStateException`이 발생합니다.

<br>

### notify()

- notify()는 wait()에 의해 대기 상태에 있던 스레드 중 하나를 깨워서 다시 실행을 시작하게 합니다.
- 이때 어떤 스레드가 깨워질지는 JVM이 결정하는데, 이를 명확하게 제어할 수는 없습니다. 
- 마찬가지로, notify()도 synchronized 블록 안에서 호출해야 합니다.

<br>

### notifyAll()

- notifyAll()은 대기 중인 모든 스레드를 깨워서 실행 대기 상태로 전환시킵니다.
- 여러 스레드가 대기 중일 때, 하나만 깨우는 notify()와는 달리 모든 대기 중인 스레드를 깨우기 때문에 더 광범위하게 사용될 수 있습니다.

---

## 📚 Object를 이용한 동기화와 ReentrantLock과 Condition을 이용한 동기화

우선 서로 다른 방식으로 동기화 및 통신을 수행합니다.

주요 차이점은 아래에서 설명하겠습니다.

<br>

### Object를 이용한 동기화/통신

```java
@Slf4j
public class SyncObject {
    private String data;
    private boolean hasData = false;
    
    public synchronized void produce(String newData) throws InterruptedException {
        while (hasData) {
            wait();  // 이미 데이터가 있으면 소비될 때까지 대기
        }
        data = newData;
        hasData = true;
        log.info("생산 : {}", newData);
        notify();  // 소비자에게 데이터가 준비되었음을 알림
    }
    
    public synchronized String consume() throws InterruptedException {
        while (!hasData) {
            wait();  // 데이터가 없으면 생산될 때까지 대기
        }
        String consumedData = data;
        hasData = false;
        log.info("소비 : {}", consumedData);
        notify();  // 생산자에게 데이터를 소비했음을 알림
        return consumedData;
    }
    
    public static class ProducerConsumerExample {
        public static void main(String[] args) {
            SyncObject resource = new SyncObject();
            
            Thread producer = new Thread(() -> {
                try {
                    resource.produce("Hello World");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            
            Thread consumer = new Thread(() -> {
                try {
                    String data = resource.consume();
                    log.info("Received : {}", data);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            producer.start();
            consumer.start();
        }
    }
}
```

<br>

### ReentrantLock과 Condition을 이용한 동기화/통신

```java
@Slf4j
class SyncReentrantLock {
    private String data;
    private boolean hasData = false;
    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();

    public void produce(String newData) throws InterruptedException {
        lock.lock();
        try {
            while (hasData) {
                condition.await();  // 데이터가 소비될 때까지 대기
            }
            data = newData;
            hasData = true;
            log.info("생산 : {}", newData);
            condition.signal();  // 소비자에게 알림
        } finally {
            lock.unlock();
        }
    }

    public String consume() throws InterruptedException {
        lock.lock();
        try {
            while (!hasData) {
                condition.await();  // 데이터가 생산될 때까지 대기
            }
            String consumedData = data;
            hasData = false;
            log.info("소비 : {}", consumedData);
            condition.signal();  // 생산자에게 알림
            return consumedData;
        } finally {
            lock.unlock();
        }
    }

    public static class ProducerConsumerExample {
        public static void main(String[] args) {
            SyncReentrantLock resource = new SyncReentrantLock();

            Thread producer = new Thread(() -> {
                try {
                    resource.produce("Hello World");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            Thread consumer = new Thread(() -> {
                try {
                    String data = resource.consume();
                    log.info("Received : {}", data);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            producer.start();
            consumer.start();
        }
    }
}
```

---

## 📚 주요 차이점

### Lock 제어의 유연성

**Object**

- 모니터 락을 기반으로 동작하기 때문에, 락의 획득과 해제는 자동으로 처리됩니다. 
- synchronized 블록에서만 동작하며, 락의 획득과 해제가 자동화되어 있어 편리하지만, 그만큼 유연성은 떨어집니다.

<br>

**ReentrantLock**

- 락을 명시적으로 획득하고 해제해야 해. lock()으로 락을 획득하고, 작업이 끝나면 unlock()으로 해제합니다. 
- 이로 인해 더 세밀하게 제어할 수 있고, 같은 스레드가 락을 여러 번 획득할 수 있는 재진입 가능성(Reentrant)도 제공됩니다. 
- 또한, tryLock()을 사용해 락을 시도만 해볼 수 있고, 제한된 시간 동안만 락을 기다리게 할 수도 있습니다.

<br>

### 다중 조건 지원

**Object**

- wait()와 notify()는 하나의 객체에 대해 하나의 대기열을 제공합니다. 
- 한 객체에 대해 여러 조건을 독립적으로 다루기가 어려워. 모든 대기 중인 스레드는 같은 대기열에서 깨어날 가능성이 있어.

<br>

**ReentrantLock**

- ReentrantLock은 여러 개의 Condition 객체를 생성할 수 있습니다. 
- 이를 통해 서로 다른 조건에 대해 독립적으로 스레드를 대기시키거나 깨울 수 있습니다. 
- 예를 들어, 특정 조건을 만족하는 스레드들만 깨우거나, 다른 조건을 기다리는 스레드와는 별도로 관리할 수 있습니다.

<br>

### 공정성

**Object**

- wait()와 notify()는 대기 중인 스레드들 중에서 어떤 스레드가 먼저 깨어날지는 보장하지 않습니다. 
- notify()는 대기 중인 스레드 중 임의로 하나를 깨우며, notifyAll()은 모든 스레드를 깨우지만, 실행 순서는 불확실합니다.

<br>

**ReentrantLock**

- ReentrantLock은 공정성(Fairness) 정책을 설정할 수 있습니다.
- 락을 요청하는 스레드들이 공평하게 락을 획득할 수 있도록 설정할 수 있고, 이를 통해 먼저 대기한 스레드가 먼저 락을 획득할 수 있도록 제어할 수 있습니다.

<br>

### 타임아웃 지원

**Object**

- 타임아웃을 지원하긴 하지만, 설정할 수 있는 옵션이 제한적이야. 예를 들어, wait(long timeout)으로만 시간 제한을 줄 수 있습니다.

<br>

**ReentrantLock**

- Condition의 await()는 타임아웃을 더 유연하게 설정할 수 있습니다. 
- await(long time, TimeUnit unit) 메서드를 사용하면 보다 세밀하게 대기 시간을 제어할 수 있고, ReentrantLock의 tryLock(long time, TimeUnit unit)을 통해도 제한된 시간 동안만 락을 획득하려 시도할 수 있습니다.

<br>

### 재진입(Reentrant) 가능 여부

**Object**

- synchronized 블록 안에서만 작동하며, 기본적으로 재진입 락을 제공하지 않습니다. 
- synchronized로 한 번 락을 얻은 스레드는 다른 곳에서 다시 synchronized로 락을 획득하는 구조를 쉽게 지원하지 않습니다.

<br>

**ReentrantLock**

- ReentrantLock은 이름 그대로 재진입 가능한 락을 제공합니다. 
- 같은 스레드가 이미 락을 소유한 경우에도 다시 획득할 수 있기 때문에 더 복잡한 동기화 로직을 작성할 때 유용합니다.