# ChessMate
체스 입문자를 위한 체스 플랫폼입니다.
로그인 없이 바로 친구와 대전하고, 게임을 분석해 실력을 기를 수 있습니다.

**[서비스 바로가기]()** (연결예정)

---
## 기술 스택
| 분류 | 기술 |
|-----|-----|
| Backend | Java 25, Spring Boot 4.0, Spring Security, Spring WebSocket(STOMP) |
|Database| MySQL 8.0, Redis|
|External| Stockfish (체스엔진) |
|Infra| AWS EC2, Docker, Nginx, GitHub Actions |

<br>

## 주요 기능

**비로그인**
- 초대 코드로 친구와 바로 대전
- 랜덤 매칭
- 컴퓨터(Stockfish)와 대전

**로그인**
- 전적 저장 및 조회
- Stockfish 기반 게임 분석 (수 분류, 정확도, 최선의 수)

<br>

## 문서
- [시스템 아키텍처](docs/architecture.md)
- [핵심 구현](docs/implementation.md)
- [트러블슈팅](docs/troubleshooting.md)

<br>

## 로컬 실행 방법
**필요 환경**: Java 25, Redis, Stockfish

```bash
# 레포 클론
git clone https://github.com/Youngho-kr/chessmate.git
cd chessmate

# 로컬 설정 파일 생성
cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml

# application-local.yml 수정 (jwt.secret, stockfish.path 등)

# 실행
./gradlew bootRun
```

