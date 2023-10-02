## 📘 Daemon Thread

Daemon Thread는 백그라운드에서 실행되는 스레드로, 메인 스레드가 종료되도 어플리케이션 종료를 막지 않습니다.

특정 시나리오에서 스레드를 Daemon으로 생성하면 앱의 백그라운드 작업을 맡게 됩니다.

Daemon 스레드는 백그라운드 작업이기 때문에 **앱의 실행이나 종료를 방해하는 일이 없어야 합니다.**

<br>

위의 Thread.interrupt() 예시를 다시 가져와서 스레드를 데몬 스레드로 먼저 만들어 줍니다.

main 메서드에서 스레드를 start 하기 전 `thread.setDaemon(true)`를 작성하면 됩니다.

```java
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
        Thread thread = new Thread(new LongComputationTask(new BigInteger("200000"), new BigInteger("10000000")));  
        // 2의 10제곱 계산  
        thread.setDaemon(true);  
        thread.start();  
        thread.interrupt();  
    }
```

<br>

그 후 다시 프로그램을 실행했을때,

`Daemon으로 설정하기 전`에는 메인 스레드가 종료되어도 해당 스렏가 멈추지 않았지만,

`Daemon으로 설정한 후`는 main 스레드가 종료되면 전체 어플리케이션이 종료가 됩니다.

**Daemon Thread는 어플리케이션의 종료를 방해하면 안되기 때문입니다. (Background 작업)**