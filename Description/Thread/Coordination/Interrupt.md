## 📘 Thread.interrupt()

모든 스레드는 `interrupt`라는 메서드를 가집니다.

만약 A,B 두개의 스레드가 실행 중이라고 가정하고, A에서 B.interrupt()를 실행하면 B 스레드가 멈춥니다.

<br>

> 📌 **어떤 상황일 때 interrupt()를 쓸 수 있을까요?**

**첫번째는**, 스레드가 InterruptedException을 발생시키는 메서드를 실행시키는 경우

**두번째는**, 스레드의 코드가 Interrupt Signal를 명시적으로 처리하는 경우

이유를 봐도 무슨 말인지 잘 모르니 예시를 봅시다.

<br>

**BlockingTimeThread**

이 코드는 main 스레드가 종료 되었음에도 BlockingTask 스레드는 sleep(500000) 동안 종료되지 않고 계속 실행됩니다.

그래서 메인 스레드에서 sleep()으로 5초 후 **orderStopThread** 스레드를 추가로 만들어서,

BlockingTask 스레드를 interrupt 시키는 동작을 추가로 수행 후에야 BlockingTask 스레드가 종료 되었습니다.

```java
package com.thread.coordination;  
  
import lombok.extern.slf4j.Slf4j;  
  
@Slf4j  
public class InterruptThread {  
  
    // Runnable을 구현하며 잘못된 시간을 차단하는 작업을 수행하는 스레드  
    private static class BlockingTask implements Runnable {  
  
        @Override  
        public void run() {  
            try {  
                Thread.sleep(500000);  
            } catch (InterruptedException e) {  
                log.info("Blocking Thread 종료");  
            }  
        }  
    }  
  
    public static void main(String[] args) {  
        Thread thread = new Thread(new BlockingTask());  
        thread.start();  
  
        try {  
            Thread.sleep(5000);  
        } catch (InterruptedException e) {  
            throw new RuntimeException(e);  
        }  
  
        Thread orderStopThread = new Thread(thread::interrupt);  
        orderStopThread.start();  
    }  
}
```

<br>

**아래 코드는 거듭 제곱을 계산하는 스레드를 실행하는 코드입니다.**

main 스레드에서는 작은 수인 2의 10제곱을 계산해서 계산결과가 바로 1024로 빠르게 나왔습니다.

만약 base, power에 엄청 큰 수(200000, 100000000)를 대입하게 되면 계산 시간이 엄청 오래 걸려서 스레드가 중지되지 않으며,

main 메서드에 interrupt() 메서드를 넣어도, 이를 처리할 메서드나 로직이 없기 때문에 interrupt 되지 않습니다.

```java
@Slf4j  
public class InterruptThread {  
  
    // Runnable을 구현하며 잘못된 시간을 차단하는 작업을 수행하는 스레드  
    private static class BlockingTask implements Runnable {  
  
        @Override  
        public void run() {  
            try {  
                Thread.sleep(500000);  
            } catch (InterruptedException e) {  
                log.info("Blocking Thread 종료");  
            }  
        }  
    }  

		// 거듭제곱을 수행하는 스레드
    private static class LongComputationTask implements Runnable {  
  
        private BigInteger base; // 밑수  
        private BigInteger power; // 제곱  
  
        public LongComputationTask(BigInteger base, BigInteger power) {  
            this.base = base;  
            this.power = power;  
        }  
  
        // 밑과 제곱을 올리는 함수  
        private BigInteger pow(BigInteger base, BigInteger power) {  
            // 결과만 선언하고 1초 초기화  
            BigInteger result = BigInteger.ONE;  
  
            // 그리고, 0부터 제곱의 값까지 반복  
            for (BigInteger i = BigInteger.ZERO; i.compareTo(power) != 0; i = i.add(BigInteger.ONE)) {  
                // 각각의 반복에서는 이전 반복에서 도출된 결과에 밑수를 곱해 새로운 결과를 계산합니다.  
                result = result.multiply(base);  
            }  
  
            // 결과 반환  
            return result;  
        }  
  
        // 밑수와 제곱을 계산해 결과를 반환하는 스레드 실행  
        @Override  
        public void run() {  
            log.info("{} * {} = {}", base, power, pow(base, power));  
        }  
    }  
  
    public static void main(String[] args) {  
        /* BlockingTask 실행 코드 *///        
//        Thread thread = new Thread(new BlockingTask());  
//        thread.start();  
//  
//        try {  
//            Thread.sleep(5000);  
//        } catch (InterruptedException e) {  
//            throw new RuntimeException(e);  
//        }  
//  
//        Thread orderStopThread = new Thread(thread::interrupt);  
//        orderStopThread.start();  
  
  
        /* LongComputationTask 실행 코드 */        
        Thread thread = new Thread(new LongComputationTask(new BigInteger("2"), new BigInteger("10")));  
        // 2의 10제곱 계산  
        thread.start();
        thread.interrupt();
    }  
}
```

<br>

이 문제를 해결하려면 거듭 제곱을 계산하는 코드 내에서 시간이 오래 걸리는 스팟을 찾아야 합니다.

이 경우는 for loop가 해당됩니다.

```java
for (BigInteger i = BigInteger.ZERO; i.compareTo(power) != 0; i = i.add(BigInteger.ONE)) {  
    // 각각의 반복에서는 이전 반복에서 도출된 결과에 밑수를 곱해 새로운 결과를 계산합니다.  
    result = result.multiply(base);  
}  
```

<br>

따라서 이 스레드가 외부에서 interrupt 당했는지 확인하는 로직을 반복이 돌떄마다 if 문을 추가합니다.

```java
for (BigInteger i = BigInteger.ZERO; i.compareTo(power) != 0; i = i.add(BigInteger.ONE)) {  
		// 조건문 추가 - Interrupt 시 계산 중지
		if (Thread.currentThread().isInterrupted()) {
			log.info("계산 중지");
			return BigInteger.ZERO;
		}

    // 각각의 반복에서는 이전 반복에서 도출된 결과에 밑수를 곱해 새로운 결과를 계산합니다.  
    result = result.multiply(base);  
}  
```