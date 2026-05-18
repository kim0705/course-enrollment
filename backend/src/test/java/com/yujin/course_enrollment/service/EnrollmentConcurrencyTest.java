package com.yujin.course_enrollment.service;

import com.yujin.course_enrollment.dto.req.ReqEnrollmentCreateDto;
import com.yujin.course_enrollment.dto.resp.RespEnrollmentDto;
import com.yujin.course_enrollment.entity.Course;
import com.yujin.course_enrollment.global.CourseStatus;
import com.yujin.course_enrollment.global.EnrollmentStatus;
import com.yujin.course_enrollment.global.exception.BusinessException;
import com.yujin.course_enrollment.mapper.CourseMapper;
import org.springframework.dao.DuplicateKeyException;
import com.yujin.course_enrollment.mapper.EnrollmentMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 수강 신청 동시성 통합 테스트
 * 실제 MySQL의 UNIQUE INDEX와 조건부 UPDATE가 동시 요청에서 정합성을 보장하는지 검증
 */
@Testcontainers
@SpringBootTest(properties = "jwt.secret=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
class EnrollmentConcurrencyTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @Autowired
    private EnrollmentService enrollmentService;
    @Autowired
    private CourseMapper courseMapper;
    @Autowired
    private EnrollmentMapper enrollmentMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /** 테스트용 STUDENT 유저 n명을 DB에 삽입하고 생성된 ID 목록 반환 */
    private List<Long> insertStudents(int count) {
        List<Long> ids = new ArrayList<>();
        String prefix = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        for (int i = 0; i < count; i++) {
            final int idx = i;
            KeyHolder holder = new GeneratedKeyHolder();

            jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement("INSERT INTO users (username, name, email, password, role) VALUES (?, ?, ?, '$2a$10$N/1FjmE1Nxvz24gnJ4A.I.3zw7QlQdAgKn8TVvuUfkCRJq0gMUioK', 'STUDENT')", new String[]{"id"});
                ps.setString(1, prefix + "_" + idx);
                ps.setString(2, "동시성테스트유저" + idx);
                ps.setString(3, prefix + "_" + idx + "@test.com");

                return ps;
            }, holder);

            ids.add(Objects.requireNonNull(holder.getKey()).longValue());
        }
        return ids;
    }

    /** 테스트용 OPEN 강의 생성 */
    private Course createOpenCourse(Long creatorId, int capacity) {
        Course course = Course.builder()
                .creatorId(creatorId)
                .title("동시성 테스트 강의")
                .price(0)
                .capacity(capacity)
                .status(CourseStatus.OPEN)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusMonths(1))
                .build();
        courseMapper.insertCourse(course);

        return course;
    }

    /** userIds 목록이 courseId 강의에 동시에 수강 신청하고 결과 상태 목록 반환 */
    private List<String> runConcurrentEnrollments(List<Long> userIds, Long courseId) throws InterruptedException {
        CopyOnWriteArrayList<String> statuses = new CopyOnWriteArrayList<>();
        int count = userIds.size();
        ExecutorService executor = Executors.newFixedThreadPool(count);

        CountDownLatch readyLatch = new CountDownLatch(count);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch  = new CountDownLatch(count);

        for (Long userId : userIds) {

            executor.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();

                    RespEnrollmentDto result = enrollmentService.registerEnrollment(userId, new ReqEnrollmentCreateDto(courseId));
                    statuses.add(result.getStatus());
                } catch (Exception e) {
                    statuses.add("ERROR: " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });

        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        return statuses;
    }

    @Test
    @DisplayName("1석에 20명 동시 신청 - 1명만 PENDING, 19명은 WAITLIST")
    void registerEnrollment_lastSeat_20ConcurrentRequests() throws InterruptedException {
        // given
        List<Long> userIds = insertStudents(20);
        Long courseId = createOpenCourse(1L, 1).getId();

        // when
        List<String> statuses = runConcurrentEnrollments(userIds, courseId);

        // then
        long pendingCount  = statuses.stream().filter(EnrollmentStatus.PENDING::equals).count();
        long waitlistCount = statuses.stream().filter(EnrollmentStatus.WAITLIST::equals).count();
        long errorCount    = statuses.stream().filter(s -> s.startsWith("ERROR")).count();

        System.out.println("[결과] PENDING: " + pendingCount + " / WAITLIST: " + waitlistCount + " / ERROR: " + errorCount);

        assertThat(errorCount).isEqualTo(0);
        assertThat(pendingCount).isEqualTo(1);
        assertThat(waitlistCount).isEqualTo(19);

        Course updated = courseMapper.selectCourseById(courseId);
        System.out.println("[enrolled_count] " + updated.getEnrolledCount() + " / capacity: " + updated.getCapacity());

        assertThat(updated.getEnrolledCount()).isEqualTo(1);
        assertThat(updated.getEnrolledCount()).isLessThanOrEqualTo(updated.getCapacity());
    }

    @Test
    @DisplayName("5석에 30명 동시 신청 - 5명만 PENDING, 25명은 WAITLIST")
    void registerEnrollment_fiveSeats_30ConcurrentRequests() throws InterruptedException {
        // given
        List<Long> userIds = insertStudents(30);
        Long courseId = createOpenCourse(1L, 5).getId();

        // when
        List<String> statuses = runConcurrentEnrollments(userIds, courseId);

        // then
        long pendingCount  = statuses.stream().filter(EnrollmentStatus.PENDING::equals).count();
        long waitlistCount = statuses.stream().filter(EnrollmentStatus.WAITLIST::equals).count();
        long errorCount    = statuses.stream().filter(s -> s.startsWith("ERROR")).count();

        System.out.println("[결과] PENDING: " + pendingCount + " / WAITLIST: " + waitlistCount + " / ERROR: " + errorCount);

        assertThat(errorCount).isEqualTo(0);
        assertThat(pendingCount).isEqualTo(5);
        assertThat(waitlistCount).isEqualTo(25);

        Course updated = courseMapper.selectCourseById(courseId);
        System.out.println("[enrolled_count] " + updated.getEnrolledCount() + " / capacity: " + updated.getCapacity());

        assertThat(updated.getEnrolledCount()).isEqualTo(5);
        assertThat(updated.getEnrolledCount()).isLessThanOrEqualTo(updated.getCapacity());
    }

    /**
     * 동시 취소 시 WAITLIST 승격 정확성 테스트
     *
     * 시나리오: cap=5, 수강생 5명(PENDING) + 대기자 3명(WAITLIST)
     *   → 수강생 5명이 동시에 취소
     *   → 대기자 3명이 모두 PENDING으로 승격되어야 함
     *   → enrolled_count = 3 (승격 인원수, cap 초과 없음)
     *
     * updateEnrollmentStatusPromote가 AND status = 'WAITLIST' 조건으로 실행되어
     * 동일 대기자에 대한 중복 승격을 방지함
     */
    @Test
    @DisplayName("5명 동시 취소 - WAITLIST 3명 전원 PENDING 승격, enrolled_count 정합성 유지")
    void cancelEnrollment_concurrent_allWaitlistPromoted() throws InterruptedException {
        // given - cap=5 강의에 수강생 5명 + 대기자 3명 세팅
        List<Long> enrolledUsers = insertStudents(5);
        List<Long> waitlistUsers = insertStudents(3);
        Long courseId = createOpenCourse(1L, 5).getId();

        // 5명 순차 신청 → 모두 PENDING (enrolled_count = 5 = capacity)
        for (Long userId : enrolledUsers) {
            enrollmentService.registerEnrollment(userId, new ReqEnrollmentCreateDto(courseId));
        }
        // 3명 추가 신청 → 정원 꽉 참이므로 모두 WAITLIST
        for (Long userId : waitlistUsers) {
            enrollmentService.registerEnrollment(userId, new ReqEnrollmentCreateDto(courseId));
        }

        // 수강 중인 5명의 enrollmentId 수집
        List<Long[]> cancelTargets = new ArrayList<>();
        for (Long userId : enrolledUsers) {
            Long enrollmentId = enrollmentMapper.selectEnrollmentByUserIdAndCourseId(userId, courseId).getId();
            cancelTargets.add(new Long[]{userId, enrollmentId});
        }

        // when - 수강생 5명 동시 취소
        int count = cancelTargets.size();
        ExecutorService executor = Executors.newFixedThreadPool(count);
        CountDownLatch readyLatch = new CountDownLatch(count);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch  = new CountDownLatch(count);

        for (Long[] target : cancelTargets) {

            executor.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();
                    enrollmentService.cancelEnrollment(target[0], target[1], null);
                } catch (Exception ignored) {
                } finally {
                    doneLatch.countDown();
                }
            });

        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // then
        long promotedCount = waitlistUsers.stream()
                .map(uid -> enrollmentMapper.selectEnrollmentByUserIdAndCourseId(uid, courseId))
                .filter(e -> EnrollmentStatus.PENDING.equals(e.getStatus()))
                .count();

        Course updated = courseMapper.selectCourseById(courseId);


        System.out.println("[승격 결과] PENDING 승격: " + promotedCount + " / enrolled_count: " + updated.getEnrolledCount() + " / capacity: " + updated.getCapacity());

        assertThat(promotedCount).isEqualTo(3); // WAITLIST 3명 모두 PENDING 승격
        assertThat(updated.getEnrolledCount()).isEqualTo(3); // enrolled_count = 승격 인원수
        assertThat(updated.getEnrolledCount()).isLessThanOrEqualTo(updated.getCapacity()); // cap 초과 없음
    }

    /**
     * 동시 취소 시 WAITLIST 승격 정확성 테스트 (취소 수 < 대기자 수)
     *
     * 시나리오: cap=5, 수강생 5명(PENDING) + 대기자 5명(WAITLIST)
     *   → 수강생 2명만 동시 취소
     *   → 대기자 2명만 PENDING으로 승격되어야 함
     *   → enrolled_count = 5 유지 (2명 취소 + 2명 승격)
     */
    @Test
    @DisplayName("2명 동시 취소 - WAITLIST 5명 중 2명만 PENDING 승격, enrolled_count 불변")
    void cancelEnrollment_concurrent_cancelLessThanWaitlist() throws InterruptedException {
        // given - cap=5 강의에 수강생 5명 + 대기자 5명 세팅
        List<Long> enrolledUsers = insertStudents(5);
        List<Long> waitlistUsers = insertStudents(5);
        Long courseId = createOpenCourse(1L, 5).getId();

        for (Long userId : enrolledUsers) {
            enrollmentService.registerEnrollment(userId, new ReqEnrollmentCreateDto(courseId));
        }
        for (Long userId : waitlistUsers) {
            enrollmentService.registerEnrollment(userId, new ReqEnrollmentCreateDto(courseId));
        }

        // 수강 중인 5명 중 2명만 취소 대상으로 선정
        List<Long[]> cancelTargets = new ArrayList<>();
        for (Long userId : enrolledUsers.subList(0, 2)) {
            Long enrollmentId = enrollmentMapper.selectEnrollmentByUserIdAndCourseId(userId, courseId).getId();
            cancelTargets.add(new Long[]{userId, enrollmentId});
        }

        // when - 2명 동시 취소
        int count = cancelTargets.size();
        ExecutorService executor = Executors.newFixedThreadPool(count);
        CountDownLatch readyLatch = new CountDownLatch(count);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch  = new CountDownLatch(count);

        for (Long[] target : cancelTargets) {
            executor.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();
                    enrollmentService.cancelEnrollment(target[0], target[1], null);
                } catch (Exception ignored) {
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // then
        long promotedCount = waitlistUsers.stream()
                .map(uid -> enrollmentMapper.selectEnrollmentByUserIdAndCourseId(uid, courseId))
                .filter(e -> EnrollmentStatus.PENDING.equals(e.getStatus()))
                .count();

        Course updated = courseMapper.selectCourseById(courseId);


        System.out.println("[승격 결과] PENDING 승격: " + promotedCount + " / enrolled_count: " + updated.getEnrolledCount() + " / capacity: " + updated.getCapacity());

        assertThat(promotedCount).isEqualTo(2); // 취소 2명 → 승격 2명
        assertThat(updated.getEnrolledCount()).isEqualTo(5); // 2명 취소 + 2명 승격 = enrolled_count 불변
        assertThat(updated.getEnrolledCount()).isLessThanOrEqualTo(updated.getCapacity());
    }

    /**
     * 동시 취소 시 WAITLIST 없는 경우 enrolled_count 정합성 테스트
     *
     * 시나리오: cap=5, 수강생 3명(PENDING), WAITLIST 없음
     *   → 수강생 3명 동시 취소
     *   → enrolled_count = 0
     */
    @Test
    @DisplayName("3명 동시 취소 - WAITLIST 없음, enrolled_count 정확히 감소")
    void cancelEnrollment_concurrent_noWaitlist() throws InterruptedException {
        // given - cap=5 강의에 수강생 3명만 세팅
        List<Long> enrolledUsers = insertStudents(3);
        Long courseId = createOpenCourse(1L, 5).getId();

        for (Long userId : enrolledUsers) {
            enrollmentService.registerEnrollment(userId, new ReqEnrollmentCreateDto(courseId));
        }

        List<Long[]> cancelTargets = new ArrayList<>();
        for (Long userId : enrolledUsers) {
            Long enrollmentId = enrollmentMapper.selectEnrollmentByUserIdAndCourseId(userId, courseId).getId();
            cancelTargets.add(new Long[]{userId, enrollmentId});
        }

        // when - 3명 동시 취소
        int count = cancelTargets.size();
        ExecutorService executor = Executors.newFixedThreadPool(count);
        CountDownLatch readyLatch = new CountDownLatch(count);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch  = new CountDownLatch(count);

        for (Long[] target : cancelTargets) {
            executor.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();
                    enrollmentService.cancelEnrollment(target[0], target[1], null);
                } catch (Exception ignored) {
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // then
        Course updated = courseMapper.selectCourseById(courseId);


        System.out.println("[취소 결과] enrolled_count: " + updated.getEnrolledCount() + " / capacity: " + updated.getCapacity());

        assertThat(updated.getEnrolledCount()).isEqualTo(0);
        assertThat(updated.getEnrolledCount()).isLessThanOrEqualTo(updated.getCapacity());
    }

    @Test
    @DisplayName("같은 사용자 동시 신청 - 1건만 성공, 나머지는 500 없이 400 처리")
    void registerEnrollment_sameUser_concurrentRequests_noDuplicate() throws InterruptedException {
        // given
        Long userId = insertStudents(1).get(0);
        Long courseId = createOpenCourse(1L, 10).getId();

        int requestCount = 5;
        CopyOnWriteArrayList<String> statuses = new CopyOnWriteArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(requestCount);
        CountDownLatch readyLatch = new CountDownLatch(requestCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch  = new CountDownLatch(requestCount);

        for (int i = 0; i < requestCount; i++) {
            executor.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();
                    RespEnrollmentDto result = enrollmentService.registerEnrollment(userId, new ReqEnrollmentCreateDto(courseId));
                    statuses.add(result.getStatus());
                } catch (BusinessException | DuplicateKeyException e) {
                    statuses.add("400: " + e.getMessage());
                } catch (Exception e) {
                    statuses.add("ERROR: [" + e.getClass().getSimpleName() + "] " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // then
        long successCount = statuses.stream().filter(s -> EnrollmentStatus.PENDING.equals(s) || EnrollmentStatus.WAITLIST.equals(s)).count();
        long errorCount   = statuses.stream().filter(s -> s.startsWith("ERROR")).count();

        System.out.println("[결과] 성공: " + successCount + " / 400 처리: " + (requestCount - successCount - errorCount) + " / ERROR(500): " + errorCount);

        assertThat(errorCount).isEqualTo(0);
        assertThat(successCount).isEqualTo(1);

        Course updated = courseMapper.selectCourseById(courseId);
        System.out.println("[enrolled_count] " + updated.getEnrolledCount());
        assertThat(updated.getEnrolledCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("같은 사용자 동시 재신청 - 1건만 성공, 나머지는 500 없이 400 처리")
    void registerEnrollment_sameUser_concurrentReEnrollments_noDuplicate() throws InterruptedException {
        // given - 사용자가 신청 후 취소한 상태 세팅
        Long userId = insertStudents(1).get(0);
        Long courseId = createOpenCourse(1L, 10).getId();

        enrollmentService.registerEnrollment(userId, new ReqEnrollmentCreateDto(courseId));
        Long enrollmentId = enrollmentMapper.selectEnrollmentByUserIdAndCourseId(userId, courseId).getId();
        enrollmentService.cancelEnrollment(userId, enrollmentId, null);

        int requestCount = 5;
        CopyOnWriteArrayList<String> statuses = new CopyOnWriteArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(requestCount);
        CountDownLatch readyLatch = new CountDownLatch(requestCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch  = new CountDownLatch(requestCount);

        for (int i = 0; i < requestCount; i++) {
            executor.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();
                    RespEnrollmentDto result = enrollmentService.registerEnrollment(userId, new ReqEnrollmentCreateDto(courseId));
                    statuses.add(result.getStatus());
                } catch (BusinessException | DuplicateKeyException e) {
                    statuses.add("400: " + e.getMessage());
                } catch (Exception e) {
                    statuses.add("ERROR: [" + e.getClass().getSimpleName() + "] " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // then
        long successCount = statuses.stream().filter(s -> EnrollmentStatus.PENDING.equals(s) || EnrollmentStatus.WAITLIST.equals(s)).count();
        long errorCount   = statuses.stream().filter(s -> s.startsWith("ERROR")).count();

        System.out.println("[결과] 성공: " + successCount + " / 400 처리: " + (requestCount - successCount - errorCount) + " / ERROR(500): " + errorCount);

        assertThat(errorCount).isEqualTo(0);
        assertThat(successCount).isEqualTo(1);

        Course updated = courseMapper.selectCourseById(courseId);
        System.out.println("[enrolled_count] " + updated.getEnrolledCount());
        assertThat(updated.getEnrolledCount()).isEqualTo(1);
    }
}
