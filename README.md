## Performance-Optimization

성능을 위한 코딩을 연습하는 저장소

---

## 📘 Multi-Threading

😯 **Multi-Threading을 통한 동시성 & 응답성 향상 및 고성능 구현** 😯

**프로젝트 내 사용 Directory** : Thread

<br>

> 📕 **1. Process & Thread Scheduling 기본 개념**

**Intro - Process & Thread Scheduling 기본 개념**
- [Process & Thread Scheduling 개념](https://github.com/spacedustz/Performance-Optimization/blob/main/Description/Thread/1-기본개념/Basic.md)

<br><br>

> 📕 **2. 스레드 기초 - A (생성, 상속)**

**Thread 생성 - Thread를 생성하는 2가지 방법**
- [Runnable Interface 구현](https://github.com/spacedustz/Performance-Optimization/blob/main/Description/Thread/2-스레딩기초A/Create.md)

**Thread 상속 - 경찰과 도둑 예시**
- [Inheritance Thread](https://github.com/spacedustz/Performance-Optimization/blob/main/Description/Thread/2-스레딩기초A/Inheritance.md)

<br><br>

> 📕 **3. 스레드 기초 - B (중지, 조인)**

**Thread 조정 - Thread Termination & Join**
- [Thread를 interrupt 해야하는 이유](https://github.com/spacedustz/Performance-Optimization/blob/main/Description/Thread/3-스레딩기초B/Basic.md)
- [Thread.interrupt()를 이용한 Thread Interrupt](https://github.com/spacedustz/Performance-Optimization/blob/main/Description/Thread/3-스레딩기초B/Interrupt.md)
- [Daemon Thread를 이용한 Thread Interrupt](https://github.com/spacedustz/Performance-Optimization/blob/main/Description/Thread/3-스레딩기초B/Daemon.md)
- [Thread.join()을 이용한 실행 순서 제어](https://github.com/spacedustz/Performance-Optimization/blob/main/Description/Thread/3-스레딩기초B/Join.md)

<br><br>

> 📕 **4. 성능 최적화 (지연시간, 처리량)**

**지연시간**
- [Perfomance Optimization - 성능의 정의](https://github.com/spacedustz/Performance-Optimization/blob/main/Description/Thread/4-성능최적화/Optimization.md)
- [Image Processing - 이미지 픽셀 처리](https://github.com/spacedustz/Performance-Optimization/blob/main/Description/Thread/4-성능최적화/ImageProcessing.md)

**처리량**
- [Throughput Optimization - 처리량의 정의](https://github.com/spacedustz/Performance-Optimization/blob/main/Description/Thread/4-성능최적화/Throughput-Basic.md)
- [Throughput Optimization - 처리량 최적화 & 성능 테스트 (Apache Jmeter)](https://github.com/spacedustz/Performance-Optimization/blob/main/Description/Thread/4-성능최적화/Throughput-Implementation.md)

<br><br>

> 📕 **5. Thread 간 데이터 공유 (Stack & Heap / 임계영역)**

**Thread 간 데이터 공유**
- [Share Resource - 스레드간 리소스 공유 시 발생할 수 있는 문제점 (Atomic Operation)](https://github.com/spacedustz/Performance-Optimization/blob/main/Description/Thread/5-리소스공유/SharingResource.md)
- [Critical Section & Synchronized with Lock](https://github.com/spacedustz/Performance-Optimization/blob/main/Description/Thread/5-리소스공유/Critical-Section-Lock.md)
- [Atomic Operation 판단기준 & volatile 키워드](https://github.com/spacedustz/Performance-Optimization/blob/main/Description/Thread/5-리소스공유/Metrics-Capturing.md)

<br><br>

> 📕 **6. 동시성 문제와 해결 방법 (임계영역, 동기화, 경쟁상태, Thread Lock, DeadLock)**

**동시성 문제와 해결법**
- Not Studied Yet

<br><br>

> 📕 **7. Thread Locking 심화 (Reentrant Lock)**

**Locking**
- Not Studied Yet

<br><br>

> 📕 **8. Thread 간 통신 (세마포어, CountDownLatch를 이용한 Thread Blocking)**

**Thread 간 통신**
- Not Studied Yet

<br><br>

> 📕 **9. Lock-Free 알고리즘, 데이터 구조 및 기술 (Non-Blocking & Lock-Free / 원자적 레퍼런스, CompareAndSet 고성능 데이터 구조)**

**Lock-Free 알고리즘, 데이터 구조 및 기술**
- Not Studied Yet

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
