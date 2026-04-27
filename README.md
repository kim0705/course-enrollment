# 📋 프로젝트 README

## 📌 프로젝트 개요
- **프로젝트 목적:** 강사(CREATOR)가 강의를 등록·관리하고, 수강생(STUDENT)이 강의를 검색·신청 할 수 있는 수강 신청 시스템
- **주요 기능:**
  - 강의 등록 / 수정 / 상태 관리 (DRAFT → OPEN → CLOSED)
  - 강의 목록 조회 (상태 필터, 키워드 검색, 페이징)
  - 수강 신청 / 결제 확정 / 취소
  - 정원 초과 시 자동 대기열(WAITLIST) 등록 및 취소 시 자동 승격
  - 마이페이지: 수강생 신청 목록 / 강사 강의 목록 · 수강생 조회
- **핵심 요구사항:**
  - 동시 수강 신청 시 정원 초과 방지 (조건부 UPDATE)
  - 강의 상태별 접근 제어 (DRAFT 강의는 작성자 외 접근 불가)
  - CONFIRMED 확정 후 7일 이내에만 취소 가능

---

## 🛠 기술 스택
- **Backend:** Java 17, Spring Boot 3.5.13
- **Frontend:** React + Vite
- **Database:** H2 (In-Memory, MySQL 호환 모드)
- **ORM / Mapper:** MyBatis
- **Security:** 별도 인증 없음 — `X-User-Id` 요청 헤더로 사용자 식별
- **Build Tool:** Gradle

---

## 🚀 실행 방법

### 1. 프로젝트 클론
```bash
git clone https://github.com/kim0705/course-enrollment.git
cd course-enrollment
```

### 2. 백엔드 실행
IntelliJ IDEA에서 `CourseEnrollmentApplication` 실행

### 3. 프론트엔드 실행
```bash
cd frontend
npm install
npm run dev
```

### 4. DB 및 환경 설정
- DB 설정 불필요 — 애플리케이션 시작 시 H2 인메모리 DB가 자동으로 스키마/샘플 데이터 초기화

### 5. 접속 URL
| 서비스 | URL |
|---|---|
| 프론트엔드 | http://localhost:5173 |
| 백엔드 API | http://localhost:8080 |
| H2 Console | http://localhost:8080/h2-console |

> H2 Console 접속 정보: JDBC URL `jdbc:h2:mem:course_enrollment`, 계정 `sa` / 비밀번호 없음

---

## 📡 주요 API 목록 및 예시

> 인증 방식: 요청 헤더 `X-User-Id: {userId}` 로 사용자 식별

### 1. 강의 등록
- **Method:** `POST`
- **URL:** `/api/courses`
- **Headers:** `X-User-Id: 1`
- **Request:**
```json
{
  "title": "Spring Boot 완전 정복",
  "description": "강의 설명",
  "price": 49000,
  "capacity": 30,
  "startDate": "2026-06-01",
  "endDate": "2026-08-31"
}
```
- **Response:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 29,
    "creatorId": 1,
    "title": "Spring Boot 완전 정복",
    "description": "강의 설명",
    "price": 49000,
    "capacity": 30,
    "status": "DRAFT",
    "startDate": "2026-06-01",
    "endDate": "2026-08-31",
    "createdAt": "2026-04-27T21:38:38.782161"
  }
}
```

---

### 2. 강의 목록 조회
- **Method:** `GET`
- **URL:** `/api/courses?status=OPEN&keyword=React&page=0&size=12`
- **Request:** Query Parameter (모두 선택사항)
  - `status`: `OPEN` | `CLOSED` (미입력 시 전체, DRAFT 제외)
  - `keyword`: 제목 검색어
  - `page`: 페이지 번호 (기본값 0)
  - `size`: 페이지 크기 (기본값 12)
- **Response:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "id": 1,
        "creatorId": 1,
        "creatorName": "강사A",
        "title": "React 실전 마스터",
        "price": 45000,
        "capacity": 30,
        "enrolledCount": 2,
        "status": "OPEN",
        "startDate": "2026-05-01",
        "endDate": "2026-07-31",
        "createdAt": "2026-04-01T09:00:00"
      }
    ],
    "page": 0,
    "size": 12,
    "totalCount": 1,
    "totalPages": 1,
    "last": true
  }
}
```

---

### 3. 강의 상세 조회
- **Method:** `GET`
- **URL:** `/api/courses/{courseId}`
- **Headers:** `X-User-Id: 4` (선택 — 미입력 시 수강 신청 여부 미포함)
- **Request:** 없음
- **Response:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "creatorId": 1,
    "creatorName": "강사A",
    "title": "React 실전 마스터",
    "description": "Vite와 최신 React 라이브러리를 활용한 실무 프로젝트",
    "price": 45000,
    "capacity": 30,
    "enrolledCount": 2,
    "status": "OPEN",
    "startDate": "2026-05-01",
    "endDate": "2026-07-31",
    "createdAt": "2026-04-01T09:00:00",
    "updatedAt": "2026-04-27T21:36:20.858123",
    "enrolled": true
  }
}
```

---

### 4. 수강 신청
- **Method:** `POST`
- **URL:** `/api/enrollments`
- **Headers:** `X-User-Id: 6`
- **Request:**
```json
{ "courseId": 1 }
```
- **Response:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 33,
    "userId": 6,
    "courseId": 1,
    "courseTitle": "React 실전 마스터",
    "status": "PENDING",
    "confirmedAt": null,
    "cancelledAt": null,
    "createdAt": "2026-04-27T21:44:41.331255"
  }
}
```
> 정원 초과 시 `status: "WAITLIST"` 로 응답

---

### 5. 결제 요청 (PENDING → CONFIRMED)
- **Method:** `PATCH`
- **URL:** `/api/enrollments/{enrollmentId}/confirm`
- **Headers:** `X-User-Id: 4`
- **Request:** 없음
- **Response:** 수강 신청 정보 (`status: "CONFIRMED"`, `confirmedAt` 포함)

---

### 6. 수강 취소
- **Method:** `PATCH`
- **URL:** `/api/enrollments/{enrollmentId}/cancel`
- **Headers:** `X-User-Id: 4`
- **Request:** 없음
- **Response:** 수강 신청 정보 (`status: "CANCELLED"`, `cancelledAt` 포함)
> CONFIRMED 상태는 확정 후 7일 이내에만 취소 가능
> PENDING/CONFIRMED 취소 시 WAITLIST 첫 번째 대기자 자동 PENDING 승격

---

## 🗂 데이터 모델 설명

### 1. ERD (Entity Relationship Diagram)


### 2. 테이블 상세 설명

- **users**
  - 설명: 사용자(강사/수강생) 정보를 관리하는 테이블

- **courses**
  - 설명: 강의 정보 및 현재 모집 상태를 관리하는 테이블

- **enrollments**
  - 설명: 수강생의 강의 신청 내역을 관리하는 테이블

### 3. 테이블 관계 및 제약 사항
- `users` : `courses` = 1:N (한 강사가 여러 강의 등록 가능)
- `users` : `enrollments` = 1:N (한 수강생이 여러 강의 신청 가능)
- `courses` : `enrollments` = 1:N (한 강의에 여러 수강 신청 존재)
- `enrollments(user_id, course_id)` UNIQUE 제약 — 동일 강의 중복 신청 불가
- `courses.enrolled_count` 조건부 UPDATE 적용 — 정원 초과 방지

---

## 📌 요구사항 해석 및 가정

### 구현 범위

**필수 구현** 
- 강의 등록 / 강의 상태 관리 (DRAFT → OPEN → CLOSED)
- 강의 목록 조회 (상태 필터), 강의 상세 조회 (현재 신청 인원 포함)
- 수강 신청 / 신청 상태 관리 / 수강 취소 / 내 수강 신청 목록 조회
- 정원 초과 신청 거부, 동시 신청 처리

**선택 구현**
- 수강 취소 기간 제한 (결제 확정 후 7일 이내)
- 대기열(WAITLIST) 기능
- 강의별 수강생 목록 조회 (크리에이터 전용)
- 신청 내역 페이지네이션

**요구사항 외 추가 구현**
- 강의 수정 — DRAFT 상태에서만 허용
- 개설 강의 목록 조회 — 강사가 본인 강의(DRAFT 포함) 확인용
- 강의 목록 키워드 검색 및 페이지네이션

---

### 요구사항 해석 및 가정

**정원 초과 신청 처리**
- 정원이 초과된 경우 수강 신청 상태를 `WAITLIST`로 처리
- 기본 상태(`PENDING → CONFIRMED → CANCELLED`)에 `WAITLIST` 상태 추가

**WAITLIST 자동 승격**
- PENDING / CONFIRMED 취소 발생 시 WAITLIST 첫 번째를 PENDING으로 승격
- 승격 순서는 생성 시점 기준

**취소 가능 기간 기준**
- 취소 가능 기간은 `CONFIRMED(결제 완료 시점)` 기준으로 적용
- PENDING은 아직 결제 전이므로 환불 개념이 없다고 보고 기간 제한 없이 취소 가능

**강의 수정 가능 시점**
- 강의 수정은 `DRAFT` 상태에서만 가능

**동시성 처리 방식**
- 조건부 UPDATE로 정원 초과 방지

**강사의 본인 강의 수강 신청 제한**
- 강사는 본인이 생성한 강의에 수강 신청할 수 없도록 제한

**DRAFT 강의 접근 제어**
- DRAFT는 준비 중인 강의이므로 일반 목록에서 제외하고 강사 본인만 상세 조회 가능하도록 처리

**X-User-Id 없는 경우 비회원 처리**
- `X-User-Id` 미포함 시 비회원으로 처리 (조회만 가능)

---

## ⚙️ 설계 결정과 이유

**조건부 UPDATE로 동시성 제어**
- `UPDATE ... WHERE enrolled_count < capacity` 방식으로 정원 초과 방지
- 별도 락 없이 단일 쿼리로 처리 가능해 구현이 단순하고 성능 부담이 적음

**enrolled_count 컬럼 직접 관리**
- `COUNT(*)` 집계 대신 수강 신청/취소 시 직접 증감
- 매 요청마다 집계 쿼리를 수행하지 않아 성능 저하를 방지

**WAITLIST 승격을 취소 트랜잭션에 동기 포함**
- 취소와 승격을 하나의 트랜잭션으로 처리
- 처리 중 일부만 반영되는 상황을 방지하기 위함

**`X-User-Id` 헤더 인증**
- `X-User-Id` 헤더로 사용자 식별
- 인증 구현을 생략하고 핵심 로직에 집중하기 위함

**DRAFT 공개 목록 제외**
- 일반 목록에서는 제외하고 작성자만 조회 가능
- 준비 중인 강의가 외부에 노출되는 것을 방지

---

## ⚡ 한계 및 트레이드오프

- **enrolled_count 정합성:** 수강 신청/취소 시 직접 증감하는 구조이기 때문에 예상치 못한 오류 발생 시 실제 신청 건수와 enrolled_count가 어긋날 수 있음
- **WAITLIST 승격 실패 시 취소 롤백:** 승격을 취소와 같은 트랜잭션으로 묶기 때문에 승격 중 예외가 생기면 취소도 함께 롤백됨
- **키워드 검색 성능:** `LIKE '%keyword%'` 방식이라 데이터가 많아지면 검색 속도가 느려질 수 있음

---

## 🧪 테스트 실행 방법

- **실행 방법:**
```bash
cd backend  
./gradlew test
```

- **주요 테스트:**
  - CourseServiceTest: 강의 등록/조회/상태 변경
  - EnrollmentServiceTest: 수강 신청/결제/취소/대기열 처리
  - EnrollmentConcurrencyTest: 동시 신청 시 정원 초과 방지 검증

---

## ⚠️ 미구현 / 제약사항

- **미구현 기능:**

  - 실제 인증/인가 (JWT, Spring Security)
    - 과제 요구사항에 따라 `X-User-Id` 헤더 기반으로 대체
  
  - 회원 가입 / 로그인 UI (현재 사용자 선택 방식)
  
  - 실제 결제 연동
    - 외부 시스템 없이 상태 변경(PENDING → CONFIRMED)으로 단순화

  - 강의 CLOSED 시 WAITLIST 처리
    - 현재는 마감 이후에도 WAITLIST가 유지됨

  - 강의 종료 시 상태 자동 변경
    - 현재는 수동으로 CLOSED 처리

- **제약사항:**
  - 인증을 `X-User-Id` 헤더 기반으로 단순화하여 사용자 검증 기능이 없음

---

## AI 활용 범위

- **활용 내용:** 백엔드/프론트엔드 코드 작성 및 단위 테스트 작성
- **활용 방식:** 설계 방향은 직접 결정하고 구현 및 검증 과정에서 AI를 활용
