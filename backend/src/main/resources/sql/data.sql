-- users
INSERT INTO users (name, role) VALUES ('강사A', 'CREATOR');
INSERT INTO users (name, role) VALUES ('강사B', 'CREATOR');
INSERT INTO users (name, role) VALUES ('수강생A', 'STUDENT');
INSERT INTO users (name, role) VALUES ('수강생B', 'STUDENT');
INSERT INTO users (name, role) VALUES ('수강생C', 'STUDENT');

-- courses
INSERT INTO courses (creator_id, title, description, price, capacity, enrolled_count, status, start_date, end_date)
VALUES (1, 'Spring Boot 강의', 'Spring Boot 기초부터 심화까지', 50000, 30, 0, 'OPEN', '2025-06-01', '2025-08-31');

INSERT INTO courses (creator_id, title, description, price, capacity, enrolled_count, status, start_date, end_date)
VALUES (1, 'JPA 강의', 'JPA 완전 정복', 40000, 2, 0, 'OPEN', '2025-06-01', '2025-08-31');

INSERT INTO courses (creator_id, title, description, price, capacity, enrolled_count, status, start_date, end_date)
VALUES (2, 'MyBatis 강의', 'MyBatis 실전편', 30000, 10, 0, 'DRAFT', '2025-07-01', '2025-09-30');