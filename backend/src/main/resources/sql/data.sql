-- 사용자 (password: Test1234!)
INSERT INTO users (username, name, email, password, role)
VALUES
    ('creator_a', '강사A',  'creator_a@test.com', '$2a$10$N/1FjmE1Nxvz24gnJ4A.I.3zw7QlQdAgKn8TVvuUfkCRJq0gMUioK', 'CREATOR'),
    ('creator_b', '강사B',  'creator_b@test.com', '$2a$10$N/1FjmE1Nxvz24gnJ4A.I.3zw7QlQdAgKn8TVvuUfkCRJq0gMUioK', 'CREATOR'),
    ('creator_c', '강사C',  'creator_c@test.com', '$2a$10$N/1FjmE1Nxvz24gnJ4A.I.3zw7QlQdAgKn8TVvuUfkCRJq0gMUioK', 'CREATOR'),
    ('student_a', '수강생A', 'student_a@test.com', '$2a$10$N/1FjmE1Nxvz24gnJ4A.I.3zw7QlQdAgKn8TVvuUfkCRJq0gMUioK', 'STUDENT'),
    ('student_b', '수강생B', 'student_b@test.com', '$2a$10$N/1FjmE1Nxvz24gnJ4A.I.3zw7QlQdAgKn8TVvuUfkCRJq0gMUioK', 'STUDENT'),
    ('student_c', '수강생C', 'student_c@test.com', '$2a$10$N/1FjmE1Nxvz24gnJ4A.I.3zw7QlQdAgKn8TVvuUfkCRJq0gMUioK', 'STUDENT');



-- 강의 - OPEN
INSERT INTO courses (creator_id, title, description, price, capacity, enrolled_count, status, start_date, end_date, created_at)
VALUES
    (1, 'React 실전 마스터',           'Vite와 최신 React 라이브러리를 활용한 실무 프로젝트',   45000,  30, 2, 'OPEN', '2026-05-01', '2026-07-31', '2026-04-01 09:00:00'),
    (1, 'Spring Boot 완전 정복',       '스프링 부트로 RESTful API 서버 설계부터 배포까지',     49000,  50, 2, 'OPEN', '2026-06-01', '2026-08-31', '2026-04-05 10:00:00'),
    (1, '자바 최적화(GC의 이해)',       'JVM 성능을 극대화하는 GC 튜닝 비법',                  80000,   2, 2, 'OPEN', '2026-05-01', '2026-06-30', '2026-04-08 11:00:00'),
    (2, 'Next.js 14 완벽 가이드',      'App Router와 SSR의 핵심 원리 파헤치기',               60000,  40, 2, 'OPEN', '2026-05-15', '2026-08-15', '2026-04-03 09:30:00'),
    (2, 'AWS 클라우드 기초',            'EC2부터 S3까지 클라우드 인프라 시작하기',                  0,  50, 1, 'OPEN', '2026-05-01', '2026-08-01', '2026-04-10 14:00:00'),
    (3, 'TypeScript 핵심 문법',        '자바스크립트 개발자를 위한 타입 시스템 정복',          25000,  60, 1, 'OPEN', '2026-04-15', '2026-07-15', '2026-04-02 08:30:00'),
    (3, 'Redis로 구축하는 캐시 시스템',  '대규모 트래픽 처리를 위한 백엔드 최적화 기법',        35000,  20, 2, 'OPEN', '2026-05-10', '2026-06-10', '2026-04-07 13:00:00'),
    (3, '초보자를 위한 Python',         '코딩의 기초부터 데이터 분석까지 한 번에!',                 0, 100, 0, 'OPEN', '2026-05-01', '2026-09-30', '2026-04-12 10:00:00');



-- 강의 - CLOSED
INSERT INTO courses (creator_id, title, description, price, capacity, enrolled_count, status, start_date, end_date, created_at)
VALUES
    (1, '2025 상반기 코딩 테스트 대비', '주요 알고리즘 50문제 풀이 전략',         30000, 100, 1, 'CLOSED', '2025-01-01', '2025-03-31', '2024-12-01 09:00:00'),
    (2, 'Git & GitHub 협업 가이드',    '개발자의 필수 덕목, 버전 관리 완벽 정복', 10000,  50, 1, 'CLOSED', '2025-03-10', '2025-04-10', '2025-02-01 10:00:00'),
    (3, 'Deep Learning 기초 수학',     '인공지능 공부를 위한 필수 수학 개념',      20000,  30, 0, 'CLOSED', '2025-02-01', '2025-04-30', '2025-01-15 11:00:00');



-- 강의 - DRAFT
INSERT INTO courses (creator_id, title, description, price, capacity, enrolled_count, status, start_date, end_date, created_at)
VALUES
    (1, 'Spring Security & JWT',    '인증과 인가, 제대로 구현해보자',           48000, 40, 0, 'DRAFT', '2026-08-01', '2026-10-31', '2026-04-20 09:00:00'),
    (2, 'Go 언어 효율적인 백엔드',   '빠르고 가벼운 Go 언어로 API 만들기',       42000, 30, 0, 'DRAFT', '2026-09-01', '2026-11-30', '2026-04-18 14:00:00'),
    (3, 'Figma 디자인 시스템 구축',  '개발자와 협업하기 위한 디자이너의 가이드', 38000, 20, 0, 'DRAFT', '2026-08-15', '2026-09-15', '2026-04-15 11:30:00');



-- 강의 - OPEN 추가
INSERT INTO courses (creator_id, title, description, price, capacity, enrolled_count, status, start_date, end_date, created_at)
VALUES
    (1, 'Kubernetes 입문',             'Docker 컨테이너를 오케스트레이션으로 확장하기',         55000, 30, 2, 'OPEN', '2026-05-20', '2026-08-20', '2026-04-11 09:00:00'),
    (1, 'MySQL 성능 최적화',            '인덱스 설계부터 쿼리 튜닝까지 실전 노하우',             42000, 40, 1, 'OPEN', '2026-06-01', '2026-09-01', '2026-04-13 10:00:00'),
    (1, 'Clean Code와 리팩토링',         '읽기 좋은 코드를 작성하는 실전 기법',                  30000, 50, 1, 'OPEN', '2026-05-15', '2026-07-15', '2026-04-14 11:00:00'),
    (2, 'Vue.js 3 완벽 입문',           'Composition API로 시작하는 모던 프론트엔드',           38000, 35, 2, 'OPEN', '2026-06-10', '2026-09-10', '2026-04-09 09:00:00'),
    (2, 'gRPC와 Protocol Buffers',      '마이크로서비스 간 고성능 통신 구현하기',                65000, 20, 0, 'OPEN', '2026-07-01', '2026-09-30', '2026-04-16 14:00:00'),
    (2, 'Flutter 앱 개발 입문',         'Dart 언어부터 iOS/Android 앱 출시까지',                48000, 45, 1, 'OPEN', '2026-05-01', '2026-08-31', '2026-04-06 08:00:00'),
    (3, 'Linux 시스템 프로그래밍',       '커널과 시스템 콜을 이해하는 저수준 개발',              70000, 15, 0, 'OPEN', '2026-06-15', '2026-09-15', '2026-04-17 13:00:00'),
    (3, 'GraphQL API 설계',             'REST를 넘어 유연한 API 구조 만들기',                    32000, 40, 1, 'OPEN', '2026-05-10', '2026-07-31', '2026-04-04 10:00:00'),
    (3, 'CI/CD 파이프라인 구축',         'GitHub Actions와 Jenkins로 자동화 배포 완성',           45000, 30, 1, 'OPEN', '2026-06-01', '2026-08-01', '2026-04-19 09:30:00'),
    (1, 'Apache Kafka 메시징 시스템',    '대용량 실시간 데이터 스트리밍 아키텍처',               60000, 25, 0, 'OPEN', '2026-07-01', '2026-10-01', '2026-04-21 11:00:00'),
    (2, 'Elasticsearch 검색 엔진',       '전문 검색과 로그 분석 시스템 구축하기',                52000, 20, 1, 'OPEN', '2026-06-20', '2026-09-20', '2026-04-22 14:00:00'),
    (3, '알고리즘 코딩테스트 완성',      '코딩테스트 합격을 위한 핵심 유형 100제',               25000, 100, 3, 'OPEN', '2026-05-01', '2026-10-31', '2026-04-23 09:00:00'),
    (1, 'Spring Batch 실전',            '대용량 데이터 처리를 위한 배치 시스템 설계',            47000, 25, 0, 'OPEN', '2026-08-01', '2026-10-31', '2026-04-24 10:00:00'),
    (2, 'Svelte로 만드는 경량 웹앱',     '번들 없이 빠른 UI를 완성하는 새로운 접근',             28000, 30, 1, 'OPEN', '2026-07-15', '2026-09-15', '2026-04-25 08:30:00');



-- 수강 신청 - 수강생A (user_id=4)
INSERT INTO enrollments (user_id, course_id, status, confirmed_at, cancelled_at, created_at)
VALUES
    (4,  3, 'WAITLIST',  NULL,                  NULL,                  '2026-04-25 10:15:00'),
    (4,  4, 'PENDING',   NULL,                  NULL,                  '2026-04-24 11:20:00'),
    (4,  5, 'PENDING',   NULL,                  NULL,                  '2026-04-23 15:40:00'),
    (4,  2, 'CONFIRMED', '2026-04-22 14:30:00', NULL,                  '2026-04-20 10:00:00'),
    (4,  7, 'CONFIRMED', '2026-04-14 11:00:00', NULL,                  '2026-04-08 13:00:00'),
    (4,  6, 'CANCELLED', NULL,                  '2026-04-15 16:00:00', '2026-04-05 09:00:00'),
    (4,  1, 'CONFIRMED', '2026-04-05 14:00:00', NULL,                  '2026-04-01 09:30:00'),
    (4, 10, 'CONFIRMED', '2025-03-15 11:00:00', NULL,                  '2025-03-11 09:00:00'),
    (4, 15, 'CONFIRMED', '2026-04-20 10:00:00', NULL,                  '2026-04-18 09:00:00'),
    (4, 16, 'PENDING',   NULL,                  NULL,                  '2026-04-23 15:00:00'),
    (4, 20, 'CONFIRMED', '2026-04-22 09:00:00', NULL,                  '2026-04-20 14:00:00'),
    (4, 26, 'CONFIRMED', '2026-04-24 10:00:00', NULL,                  '2026-04-23 11:00:00');



-- 수강 신청 - 수강생B (user_id=5)
INSERT INTO enrollments (user_id, course_id, status, confirmed_at, cancelled_at, created_at)
VALUES
    (5,  3, 'CONFIRMED', '2026-04-20 16:00:00', NULL,                  '2026-04-18 14:00:00'),
    (5,  2, 'CANCELLED', NULL,                  '2026-04-21 09:00:00', '2026-04-19 10:00:00'),
    (5,  7, 'CONFIRMED', '2026-04-25 11:00:00', NULL,                  '2026-04-24 15:00:00'),
    (5,  6, 'CONFIRMED', '2026-04-08 11:00:00', NULL,                  '2026-04-05 09:30:00'),
    (5,  1, 'CONFIRMED', '2026-04-03 10:00:00', NULL,                  '2026-04-02 09:00:00'),
    (5,  9, 'CONFIRMED', '2025-01-10 10:00:00', NULL,                  '2025-01-05 09:00:00'),
    (5, 15, 'PENDING',   NULL,                  NULL,                  '2026-04-24 11:00:00'),
    (5, 18, 'CONFIRMED', '2026-04-22 10:00:00', NULL,                  '2026-04-20 11:00:00'),
    (5, 25, 'CONFIRMED', '2026-04-23 12:00:00', NULL,                  '2026-04-23 10:00:00'),
    (5, 26, 'CONFIRMED', '2026-04-24 11:00:00', NULL,                  '2026-04-23 14:00:00');



-- 수강 신청 - 수강생C (user_id=6)
INSERT INTO enrollments (user_id, course_id, status, confirmed_at, cancelled_at, created_at)
VALUES
    (6,  3, 'PENDING',   NULL,                  NULL,                  '2026-04-22 09:30:00'),
    (6,  2, 'CONFIRMED', '2026-04-24 10:00:00', NULL,                  '2026-04-23 09:00:00'),
    (6,  4, 'CONFIRMED', '2026-04-13 10:00:00', NULL,                  '2026-04-10 15:30:00'),
    (6,  6, 'CANCELLED', NULL,                  '2026-04-10 16:00:00', '2026-04-03 09:00:00'),
    (6, 17, 'PENDING',   NULL,                  NULL,                  '2026-04-24 10:00:00'),
    (6, 18, 'CONFIRMED', '2026-04-22 14:00:00', NULL,                  '2026-04-20 09:00:00'),
    (6, 22, 'CONFIRMED', '2026-04-17 11:00:00', NULL,                  '2026-04-15 10:00:00'),
    (6, 23, 'CONFIRMED', '2026-04-23 10:00:00', NULL,                  '2026-04-21 09:00:00'),
    (6, 28, 'PENDING',   NULL,                  NULL,                  '2026-04-25 11:00:00'),
    (6, 26, 'CONFIRMED', '2026-04-25 09:00:00', NULL,                  '2026-04-23 10:00:00');
