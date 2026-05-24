# 강의 커머스 플랫폼

강사가 강의를 등록하고 수강생이 신청·결제할 수 있는 커머스 플랫폼입니다.
동시 요청 상황에서도 정원 초과나 중복 처리 없이 동작하는 걸 목표로 설계했습니다.

**배포 URL**: https://d3vgazv4kqn9do.cloudfront.net

---

## 화면

| 수강 신청 | 대기열 상태 |
|---|---|
| <img width="1742" height="1013" alt="강의상세_정원마감" src="https://github.com/user-attachments/assets/36d3c24a-06fb-470c-ad55-f3ec056a1c49" /> | <img width="1706" height="934" alt="마이페이지_목록" src="https://github.com/user-attachments/assets/b6fbd1ff-5034-45cc-b800-23b52c32d671" />

---

## 핵심 기능

**수강 신청 흐름**
- 정원이 찼을 때 자동으로 대기열 등록
- 취소 발생 시 대기열 첫 번째 수강생 자동 승격
- 동시 신청 시에도 정원 초과 없이 처리 (DB 레벨 조건부 UPDATE)

**결제 및 환불**
- Toss Payments 연동 / 결제 확정 전 서버에서 금액 재검증 (위변조 방지)
- 결제 확정 후 7일 이내 환불
- 강제 폐강 시 전체 수강생 일괄 환불 처리

**권한 관리**
- 수강생 / 강사 / 관리자 역할별 API 접근 제어
- 수강생 → 강사 전환 신청 및 관리자 승인 흐름
- 로그아웃 시 accessToken Redis 블랙리스트 등록으로 즉시 무효화

---

## 기술 스택

| 구분 | 기술 |
|---|---|
| Backend | Java 17, Spring Boot, Spring Security, MyBatis |
| Frontend | React, Vite |
| Database | MySQL (AWS RDS / Testcontainers) |
| 인증 | JWT — accessToken / refreshToken 쿠키, Redis |
| 결제 | Toss Payments API |
| 테스트 | JUnit 5, Testcontainers |

---

## 실행 방법

환경 변수: `JWT_SECRET`, `TOSS_SECRET_KEY`, `TOSS_WEBHOOK_SECRET`, `DB_PASSWORD`

```bash
# MySQL + Redis 실행
docker compose up -d

# 백엔드
cd backend && ./gradlew bootRun

# 프론트엔드
cd frontend && npm install && npm run dev
```

---

## ERD

<img width="1135" height="1252" alt="course_enrollment-db" src="https://github.com/user-attachments/assets/52f28773-feed-4e43-860f-bc6d951f8bb9" />

```
수강 신청 상태 흐름
PENDING → CONFIRMED → CANCELLED
WAITLIST → PENDING (정원 공석 발생 시 자동 승격)
```

---

## 설계 결정

### 동시 신청 처리 — 조건부 UPDATE
```sql
UPDATE courses
   SET enrolled_count = enrolled_count + 1
 WHERE id = #{id}
   AND enrolled_count < capacity
```
애플리케이션 레벨에서 정원을 체크하면 동시 요청 시 타이밍 문제가 생깁니다. 단일 UPDATE 쿼리로 처리하면 DB가 원자적으로 처리하기 때문에 별도 락 없이 정원 초과를 방지할 수 있습니다.

### 대기열 승격 — FOR UPDATE SKIP LOCKED
취소가 동시에 발생하면 여러 트랜잭션이 같은 대기자를 조회해 중복 승격이 생길 수 있습니다. 일반 `FOR UPDATE`는 락이 걸린 행을 다른 트랜잭션이 대기하므로 동시 취소가 몰릴 경우 병목이 생깁니다. `SKIP LOCKED`는 이미 락이 걸린 행을 건너뛰고 다음 대기자를 선점하기 때문에 대기 없이 서로 다른 대기자를 처리할 수 있습니다.

### enrolled_count 직접 관리
`COUNT(*)` 집계 대신 수강 신청·취소 시 직접 증감합니다. 매 요청마다 집계 쿼리를 수행하지 않아 성능 부담을 줄이지만 예상치 못한 오류 발생 시 실제 신청 건수와 어긋날 수 있는 트레이드오프가 있습니다.

### AccessToken 즉시 무효화 — Redis 블랙리스트
JWT는 stateless 특성상 발급 후 서버에서 제어가 어렵습니다. 로그아웃 시 해당 accessToken을 Redis에 저장하고(`BL:{token}`, 잔여 TTL 기준), JwtFilter에서 요청마다 블랙리스트 여부를 확인해 등록된 토큰을 즉시 차단합니다. 만료된 키는 Redis TTL에 의해 자동 삭제됩니다.

---

## 한계 및 트레이드오프

- **외부 API와 트랜잭션 일관성**: 환불 API 성공 후 DB 롤백이 발생하면 환불은 됐지만 상태는 CONFIRMED로 남을 수 있음. Toss 웹훅(`PAYMENT_STATUS_CHANGED`)을 수신해 결제 상태를 동기화하는 방식으로 보완 - 인라인 업데이트 실패 시 웹훅이 안전망으로 동작
- **대기열 승격 알림**: 승격 시 사용자에게 별도 알림이 없어 결제 기한을 놓칠 수 있는 구조. 이메일 알림 연동으로 개선 가능
- **AccessToken 블랙리스트**: 모든 인증 요청마다 Redis 조회가 추가됨. Redis 장애 시 블랙리스트 체크가 불가능해 로그아웃된 토큰이 일시적으로 유효한 상태가 될 수 있음

---

## 테스트

단위 테스트로 수강 신청·결제·취소·대기열·인증 처리 흐름을 검증하고 동시성 테스트는 실제 락 동작 검증을 위해 Testcontainers로 격리된 MySQL 환경을 구성해 진행했습니다. Redis를 사용하는 RefreshToken·AccessToken 블랙리스트는 실제 Redis 연결 기반 통합 테스트로 검증합니다.
