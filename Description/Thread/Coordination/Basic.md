## 📘 Thread Coordination

이번에 배워볼 건 스레드를 조정하는 방법입니다.

그 중 하나의 스레드를 다른 스레드에서 멈추게 하는 작업 (Thread Termination)이죠.

<br>

이 Thread Termination에는 몇가지 방법이 있습니다.

- **interrupt() 를 사용하는 방법**
- **Daemon Thread를 사용하는 방법**

<br>



> 📌 **스레드를 언제/왜 멈춰야 할까요?**

- **스레드는 아무 동작을 안해도 메모리와 일부 커널 리소스를 사용합니다.**
- 그리고 CPU 타임과 CPU 캐시 공간도 사용합니다.
- 따라서 생성한 스레드가 이미 작업을 완료했는데 어플리케이션이 동작중이라면 미사용 스레드가 잡아먹는 리소스를 정리해야 합니다.
- 또, **스레드가 오작동 할 시**에도 스레드를 중지해야 합니다.
- 예를 들어 응답이 없는 서버에 계속 요청을 보낸다거나 하는 등의 행위입니다.
- 그리고 마지막 이유는, **어플리케이션 전체를 중단하기 위해서** 입니다.
- 스레드가 하나라도 실행 중 이라면, 어플리케이션은 종료되기 않기 때문입니다.
- _(메인스레드가 이미 멈췄다고 하더라도 다른 스레드가 실행중이면 어플리케이션은 종료되지 않습니다.)_
- 따라서 어플리케이션을 종료하기 전, 모든 스레드를 중단할 기능이 필요합니다.