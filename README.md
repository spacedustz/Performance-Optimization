## Performance-Optimization

성능을 위한 코딩을 연습하는 저장소

---

## 📘 Multi-Threading

😯 **Multi-Threading을 통한 동시성 & 응답성 향상 및 고성능 구현** 😯

**프로젝트 내 사용 Directory** : Thread

<br>

> 📕 **1. Process & Thread Scheduling 기본 개념**

**Intro - Process & Thread Scheduling 기본 개념**
- [Process & Thread Scheduling 개념](./Description/1-기본개념/Basic.md)

<br><br>

> 📕 **2. 스레드 기초 - A (생성, 상속)**

**Thread 생성 - Thread를 생성하는 2가지 방법**
- [Runnable Interface 구현](./Description/2-스레딩기초A/Create.md)

**Thread 상속 - 경찰과 도둑 예시**
- [Inheritance Thread](./Description/2-스레딩기초A/Inheritance.md)

<br><br>

> 📕 **3. 스레드 기초 - B (중지, 조인)**

**Thread 조정 - Thread Termination & Join**
- [Thread를 interrupt 해야하는 이유](./Description/3-스레딩기초B/Basic.md)
- [Thread.interrupt()를 이용한 Thread Interrupt](./Description/3-스레딩기초B/Interrupt.md)
- [Daemon Thread를 이용한 Thread Interrupt](./Description/3-스레딩기초B/Daemon.md)
- [Thread.join()을 이용한 실행 순서 제어](./Description/3-스레딩기초B/Join.md)

<br><br>

> 📕 **4. 성능 최적화 (지연시간, 처리량)**

**지연시간**
- [Perfomance Optimization - 성능의 정의](./Description/4-성능최적화/Optimization.md)
- [Image Processing - 이미지 픽셀 처리](./Description/4-성능최적화/ImageProcessing.md)

**처리량**
- [Throughput Optimization - 처리량의 정의](./Description/4-성능최적화/Throughput-Basic.md)
- [Throughput Optimization - 처리량 최적화 & 성능 테스트 (Apache Jmeter)](./Description/4-성능최적화/Throughput-Implementation.md)

<br><br>

> 📕 **5. Thread 간 데이터 공유 (Stack & Heap / 임계영역)**

**Thread 간 데이터 공유**
- [Share Resource - 스레드간 리소스 공유 시 발생할 수 있는 문제점 (Atomic Operation)](./Description/5-리소스공유/SharingResource.md)
- [Atomic Operation 판단기준 & volatile 키워드](./Description/5-리소스공유/Metrics-Capturing.md)

<br><br>

> 📕 **6. 동시성 문제와 해결 방법 (임계영역, 동기화, 경쟁상태, Thread Lock, DeadLock)**

**동시성 문제와 해결법**
- [Race Condition & Data Race](./Description/5-리소스공유/Race-Condition-Data-Race.md)
- [Critical Section & Synchronized with Lock](./Description/6-동시성문제/Critical-Section-Lock.md)
- [Coarse-Grained & Fine-Grained Lock & Dead Lock 방지](Description/6-동시성문제/Lock.md)

<br><br>

> 📕 **7. Thread Locking 심화 (Reentrant Lock)**

**Locking**
- [ReentrantLock - tryLock() & lockInterruptibly()](./Description/7-Locking심화/1.%20ReentrantLock-tryLock.md)
- [ReentrantReadWriteLock을 이용한 조회 성능 향상 (5배)](./Description/7-Locking심화/2.%20ReentrantReadWriteLock.md)

<br><br>

> 📕 **8. Thread 간 통신 (세마포어, CountDownLatch를 이용한 Thread Blocking)**

**Thread 간 통신**
- [Semaphore 란?](./Description/8-스레드간-통신/Semaphore/Semaphore.md)
  - [Binary Semaphore와 Mutex의 차이점](./Description/8-스레드간-통신/Semaphore/Binary-Semaphore.md) 
  - [Producer-Consumer 패턴을 이용한 Semaphore 구현](./Thread/src/main/java/com/thread/communicate/SemaphoreImpl.java)
  - [Semaphore Barrier - 멀티스레딩 작업 순서 제어](./Description/8-스레드간-통신/Semaphore/작업순서제어.md)
  - [Object를 이용한 동기화 및 통신](./Description/8-스레드간-통신/Object/Object.md)

<br><br>

> 📕 **9. Lock-Free 알고리즘, 데이터 구조 및 기술 (Non-Blocking & Lock-Free / 원자적 레퍼런스, CompareAndSet 고성능 데이터 구조)**

**Lock-Free 알고리즘, 데이터 구조 및 기술**
- [Lock-Free & Atomic Operation](./Description/9-Lock-Free-Algorithm/Lock-Free-Algorithm.md)
- [AtomicReference<T>를 이용한 Lock-Free Stack 구현](./Description/9-Lock-Free-Algorithm/Lock-Free-Stack.md)

<br><br>

> 📕 **10. 고성능 I/O를 위한 Threading Model**

**고성능 I/O를 위한 Threading Model**
- Not Studied Yet

<br><br>

> 📕 **11. 가상 스레드와 고성능 I/O**

**가상 Thread와 고성능 I/O**
- Not Studied Yet

<br><br>

> 📕 **12. 분산 시스템, 빅데이터, 성능**

**분산 시스템, 빅데이터, 성능**
- Not Studied Yet
