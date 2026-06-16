# 트러블 슈팅

## 1. Jackson 역직렬화 실패

실시간 대전 구현 중 게임 상태 유지를 위한 `GameState` 객체에 대한 정보를 조회할 때 실패했다.
`@Getter` 어노테이션이 `DEFAULT_FEN` 필드의 게터를 생성해 직렬화 했지만 세터가 없어 역직렬화에 실패했다.

Jackson에서 `no-args` 생성자가 있어도 파라미터 생성자를 creator로 자동 감지한다.
JSON에 없는 `timeLimit` 파라미터를 null로 처리하려다가 long 타입에 실패했다.

### 해결
static 필드는 Jackson이 직렬화 하지 않기 위해 static 변수로 수정했다.
```java
// 수정 전
private final String DEFAULT_FEN = "...";

// 수정 후
private static final String DEFAULT_FEN = "...";
```

파라미터 생성자를 제거하고 `no-args` 생성자와 정적 팩토리 메서드 방식으로 변경했다.
```java
// 수정 전
public GameState(String gameId, String whiteEmail,
                 String blackEmail, long timeLimit) {
    this.gameId = gameId;
    //...
}

// 수정 후
public GameState() {
    //...
}

public static GameState create(String gameId, String whiteEmail,
                               String blackEmail, long timeLimit) {
    GameState state = new GameState();
    state.gameId = gameId;
    //...
    return state;
}
```

<br>

## 2. `@Transactional` 자기 호출 문제
`GameService`에서 `@Transactinal` 어노테이션을 적용해 대전 종료 후 결과를 저장하는 작업에 대해 트랜잭션을 적용하려고 했다.
실제로는 트랜잭션이 적용되지 않는다는 경고 메시지가 발생했다.

> @Transactional 자동 호출(실질적으로 타깃 객체 내의 메서드가 타깃 객체 내의 다른 메서드를 호출)은
런타임 시 실제 트랜잭션을 발생시키지 않습니다

### 트랜잭션 흐름
Spring AOP는 프록시 기반으로 동작하기 때문에 같은 클래스내에서 메서드를 직접 호출하면 프록시를 거치지 않아 `@Transactional`이 동작하지 않는다.
외부에서 호출할 때는 프록시가 트랜잭션을 시작하고 종료하지만, 
같은 클래스 내에서 메서드를 호출하면 `this`를 통해 직접 호출되어 프록시를 거치지 않는다.
따라서 `@Transactional`이 선언되어 있어도 실제로 트랜잭션이 시작되지 않는다.

### 해결
트랜잭션이 필요한 DB 저장 로직을 별도 서비스 - `GameRecordService`로 분리했다.
이제 `GameService`에서 `GameRecordService`의 메서드를 호출하면 프록시를 거쳐 트랜잭션이 적용된다.

<br>

## 3. 컴퓨터 대전 첫 수 타이밍 문제
컴퓨터가 백일 때 첫 수를 클라이언트가 받지 못하는 문제가 발생했다.
게임 시작 후 컴퓨터의 첫 수를 브로드캐스트할 때 클라이언트의 WebSocket 구독이 완료되지 않아 메시지를 놓쳐 발생했다.

### 해결
클라이언트가 WebSocket을 구독완료한 경우 `/app/game/{gameId}/ready`를 전송하고, 
양 플레이어가 `ready`를 전송한 후에 수를 둘 수 있도록 했다.

<br>

## 4. Stockfish 평가치 관점 미반영으로 인한 수 분류 오류
`1. e4 e5` 같은 정상적인 수가 실수 또는 블런더로 잘못 분류되었습니다.
Stockfish의 평가치인 `score cp`는 항상 현재 수를 두는 플레이어 기준으로 반환된다.
매 수마다 플레이어가 바뀌므로 매 평가치의 관점이 반대로, 단순 변화값을 계산하면 의미없는 delta값을 얻게 된다.

### 해결
공식문서에 따라 평가치를 백 기준으로 정규화해 점수를 평가한다.
백이 둔 경우라면 점수의 부호를 반전해 백 기준으로 점수를 변환하여 평가치에 적용하거나 수 분류에 사용한다.

<br>