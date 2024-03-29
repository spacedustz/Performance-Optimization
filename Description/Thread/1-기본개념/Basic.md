## 📘 Process 구조

운영체제에서 모든 프로그램은 실행을 하면 메모리로 올라와서 프로세스로 올려집니다.

<br>

> 📌 **프로세스의 구조**

- MetaData (PID, Mode, Priority ... 등등)
- Data(Heap)
- Files
- Code
- MainThread (Stack, Instruction Pointer)
    - Stack은 메모리 영역으로 지역 변수가 저장되고 기능이 실행되는 영역입니다.
    - Instruction Pointer는 스레드가 실행할 다음 명령어 주소의 포인트 역할만 합니다.

<br>

프로세스의 Stack, Instruction 부분만 제외하면 나머지 모든 스레드들이 **MetaData, Data, Files, Code를 공유**합니다.

---

## 📘 Context Switching

각각의 어플리케이션과 프로세스는 독립적으로 실행됩니다.

운영체제에서는 일반적으로 코어보다 프로세스가 훨씬 많고, 각각의 프로세스는 1개 이상의 스레드를 가집니다.

그리고 모든 스레드는 CPU 실행을 두고 서로 경쟁합니다.

즉 CPU에서는 스레드 실행 - 멈춤 - 다른 스레드 실행 - 멈춤 을 반복합니다.

위처럼 스레드를 스케줄링 하면서 다시 실행 시키는 것이 Context Switch 입니다.

<br>

> 📌 **Context Switch를 이해해야 하는 중요한 이유**

동시에 많은 스레드를 다룰때는 효율성이 떨어지는데 이것이 동시성을 위한 대가입니다.

CPU에서 실행되는 **각 스레드는 CPU 내의 레지스터나 캐시 메모리 내부의 커널 리소스를 일정 부분 차지**합니다.

다른 스레드로 전활할때는 기존의 **모든 데이터를 저장**하고 또 다른 스레드으 리소스를 CPU와 메모리에 올려야합니다.

<br>

> 📌 **Threashing 이란?**

너무 많은 스레드를 가동하게 되면 **Thrashing**이라는 현상이 발생합니다.

**Thrashing**이란 운영체제가 Context Switching에 더 많은 시간을 할애하게 되는 현상입니다.

---

## 📘 Thread Scheduling

Thead Scheduling을 이해하기 위해 간단한 예시를 들어보죠.

예를 들어 Text 편집기, 음악 플레이어 2개의 프로세스가 실행되고 있다고 가정해 봅시다.

- Music Player
- Text Editor

<br>

음악 플레이어 프로세스에는 2개의 스레드가 실행되고 있습니다.

- Music : 파일에서 음악을 로딩해 스피커로 송출합니다.
- User Interface : 음악 트랙의 상황을 UI로 보여주고 재생 멈춤 버튼의 마우스 클릭에 반응합니다.

<br>

마찬가지로 문서 편집기에도 2개의 스레드가 실행되고 있습니다.

- File Server : 현재 작업을 2초마다 파일에 저장합니다.
- User Interface : 입력한 내용을 UI로 보여주고 키보드와 마우스 입력에 반응합니다.

<br>

> 📌 **위의 예시는 하나의 CPU 코어에 4개의 스레드가 실행되고 있습니다.**

이제 코어 하나로 스케줄을 짜야 합니다.

작업의 **도착 순서** & **실행 시간** 이 주어진다고 가정하면, 운영체재는 어떤 스레드를 가정 먼저 실행할까요?

<br>

## 📘 Scheduling 방식

> 📌 **First Come, First Serve**

- 단순하게 **선착순**으로 스케줄을 짜면 어떨까요?
- 우선 이 방식의 문제점은 실행시간이 긴 스레드가 먼저 도착하면 다른 스레드에 **기아 상태(Starvation)**가 발생합니다.
- 그럼 특히 UI 스레드에는 큰 문제일 겁니다. 어플리케이션의 응답성을 방해해 좋지 않은 UX를 발생시킵니다.
- 일반적으로 UI 스레드는 다른 실행 스레드보다 더 짧습니다.

<br>

그럼 반대로 짧은 스레드인 UI 스레드를 먼저 실행한다면 어떨까요?

> 📌 **Shortest Job First**

- 위의 상황과 정반대의 문제가 생깁니다.
- 사용자 관련 이벤트가 항상 시스템에 존재하게 되고, 계산에 들어간 긴 작업들은 영원히 실행될 수 없습니다.

<br>

> 📌 **일반적인 운영체제의 Thread Scheduling 방식** - Epochs

일반적인 운영체제는 **Epochs**에 맞춰 시간을 적당한 크기로 나누고, 스레드의 Time Slice를 종류 별로 Epochs에 할당합니다.

하지만 모든 스레드가 각 Epochs에서 실행되거나 완료되지는 않습니다.

그럼 스레드에 Epochs 시간을 할당하는 방법은 뭘까요?

<br>

> 📌 **Dynamic Priority = Static Priority + Bonus (Bonus can be Negative)**

스레드에 시간을 할당하는 방법은 운영체제가 각각의 스레드에 적용하는 Dynamic Priority에 달려 있습니다.

**Static Priority**는 개발자가 미리 설정 가능하며, **Bonus**는 운영체제가 각각의 Epochs마다 조절합니다.

이렇게 하면 운영체제는 즉각적인 반응이 필요한 실시간 스레드나 Interactive 스레드에게 우선권을 주게 됩니다.

<br>

이와 동시에 **기아 상태(Starvation**를 방지하기 위해 이전 Epochs에서 실행 시간이 부족했거나,

완료되지 않은 스레드도 놓치지 않습니다.

---

## 📘 Threads vs Processes

그럼 첫번째 멀티 스레드 어플리케이션 작업을 시작하기 전에 확인할 것은,

- 하나의 프로그램에 멀티 스레드를 사용하는 시점
- 새로 생성한 프로그램을 다른 프로세스에 실행하는 시점

입니다.

<br>

> 우선 어떤 방식을 쓸건지 아래 2가지 방식 중에서 미리 결정해야 합니다.

**멀티 스레드 접근법**

- 스레드는 많은 리소스를 공유합니다.
- 많은 데이터를 공유하는 다양한 작업을 실행하려면 멀티 스레드 어플리케이션 아키텍쳐가 좋을 겁니다.
- 또한 스레드는 생성과 파괴가 훨씬 빠릅니다.
- 같은 프로세스 내부에서 스레드끼리 전환하는 것은 프로세스간 스위칭보다 훨씬 빠릅니다.

<br>

**멀티 프로세스 접근법**

- 만약 독립적으로 실행할 프로그램이라면 독립 프로세스에 실행하는게 좋을 수 있습니다.
- 예를 들면 보안과 안정성이 중요한 어플리케이션이 이에 해당합니다.
- 또, 서로 관련이 없는 스레드들을  같은 프로세스에 넣는것도 아무 의미가 없습니다.