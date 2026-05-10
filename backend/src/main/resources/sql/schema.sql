DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS enrollments;
DROP TABLE IF EXISTS courses;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
                       id    BIGINT AUTO_INCREMENT PRIMARY KEY,
                       name  VARCHAR(50)  NOT NULL,
                       role  VARCHAR(20)  NOT NULL
);

CREATE TABLE courses (
                         id             BIGINT AUTO_INCREMENT PRIMARY KEY,
                         creator_id     BIGINT       NOT NULL,
                         title          VARCHAR(100) NOT NULL,
                         description    TEXT,
                         price          INT          NOT NULL DEFAULT 0,
                         capacity       INT          NOT NULL,
                         enrolled_count INT          NOT NULL DEFAULT 0,
                         status         VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
                         start_date     DATE         NOT NULL,
                         end_date       DATE         NOT NULL,
                         created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE TABLE enrollments (
                             id           BIGINT      AUTO_INCREMENT PRIMARY KEY,
                             user_id      BIGINT      NOT NULL,
                             course_id    BIGINT      NOT NULL,
                             status       VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                             confirmed_at DATETIME,
                             cancelled_at DATETIME,
                             created_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             updated_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             FOREIGN KEY (user_id)   REFERENCES users(id)   ON DELETE RESTRICT,
                             FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE RESTRICT
);

CREATE UNIQUE INDEX idx_enrollment_user_course ON enrollments(user_id, course_id);
CREATE INDEX idx_enrollment_course             ON enrollments(course_id);

CREATE TABLE payments (
                          id            BIGINT       AUTO_INCREMENT PRIMARY KEY,
                          enrollment_id BIGINT       NOT NULL,
                          payment_key   VARCHAR(200),
                          order_id      VARCHAR(64)  NOT NULL UNIQUE,
                          order_name    VARCHAR(200) NOT NULL,
                          amount        INT          NOT NULL,
                          method        VARCHAR(20),
                          status        VARCHAR(20)  NOT NULL,
                          paid_at       DATETIME,
                          canceled_at   DATETIME,
                          cancel_reason VARCHAR(200),
                          created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          FOREIGN KEY (enrollment_id) REFERENCES enrollments(id) ON DELETE RESTRICT
);