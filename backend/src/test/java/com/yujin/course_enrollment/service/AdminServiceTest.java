package com.yujin.course_enrollment.service;

import com.yujin.course_enrollment.entity.Course;
import com.yujin.course_enrollment.entity.Enrollment;
import com.yujin.course_enrollment.global.CourseStatus;
import com.yujin.course_enrollment.global.EnrollmentStatus;
import com.yujin.course_enrollment.global.exception.BusinessException;
import com.yujin.course_enrollment.mapper.CourseMapper;
import com.yujin.course_enrollment.mapper.EnrollmentMapper;
import com.yujin.course_enrollment.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.ArgumentMatchers.argThat;

/**
 * 관리자 서비스 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminServiceTest {

    @InjectMocks
    private AdminService adminService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private CourseMapper courseMapper;

    @Mock
    private EnrollmentMapper enrollmentMapper;

    @Mock
    private PaymentService paymentService;

    @Mock
    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUpTransactionTemplate() {
        given(transactionTemplate.execute(any())).willAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });

        willAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            Consumer<org.springframework.transaction.TransactionStatus> consumer = invocation.getArgument(0);
            consumer.accept(null);
            return null;
        }).given(transactionTemplate).executeWithoutResult(any());
    }

    @Test
    @DisplayName("강제 폐강 성공 - CONFIRMED 없음")
    void forceCloseCourse_noConfirmed_closesImmediately() {
        // given
        Long courseId = 1L;
        Course course = Course.builder().id(courseId).status(CourseStatus.OPEN).build();

        given(courseMapper.selectCourseById(courseId)).willReturn(course);
        given(enrollmentMapper.selectConfirmedEnrollmentIdsByCourseId(courseId)).willReturn(List.of());

        // when
        adminService.forceCloseCourse(courseId);

        // then
        then(enrollmentMapper).should().updatePendingWaitlistCancelledByCourseId(courseId);
        then(courseMapper).should().updateCourseEnrolledCountReset(courseId);
        then(courseMapper).should().updateCourseStatus(argThat(c ->
                courseId.equals(c.getId()) && CourseStatus.FORCE_CLOSED.equals(c.getStatus())));
        then(paymentService).should(never()).refund(any(), any());
    }

    @Test
    @DisplayName("강제 폐강 성공 - CONFIRMED 2건 환불 후 FORCE_CLOSED 처리")
    void forceCloseCourse_withConfirmed_refundsAndForceCloses() {
        // given
        Long courseId = 1L;
        Course course = Course.builder().id(courseId).status(CourseStatus.OPEN).build();
        List<Long> confirmedIds = List.of(10L, 11L);

        given(courseMapper.selectCourseById(courseId)).willReturn(course);
        given(enrollmentMapper.selectConfirmedEnrollmentIdsByCourseId(courseId)).willReturn(confirmedIds);

        // when
        adminService.forceCloseCourse(courseId);

        // then
        then(paymentService).should(times(2)).refund(any(), eq("강의 폐강"));
        then(enrollmentMapper).should().updateEnrollmentStatus(argThat(e ->
                Long.valueOf(10L).equals(e.getId()) && EnrollmentStatus.FORCE_CLOSED.equals(e.getStatus())));
        then(enrollmentMapper).should().updateEnrollmentStatus(argThat(e ->
                Long.valueOf(11L).equals(e.getId()) && EnrollmentStatus.FORCE_CLOSED.equals(e.getStatus())));
    }

    @Test
    @DisplayName("강제 폐강 - 환불 실패한 건은 로그 후 다음 건으로 진행")
    void forceCloseCourse_refundFails_continuesOtherEnrollments() {
        // given
        Long courseId = 1L;
        Course course = Course.builder().id(courseId).status(CourseStatus.OPEN).build();
        List<Long> confirmedIds = List.of(10L, 11L);

        given(courseMapper.selectCourseById(courseId)).willReturn(course);
        given(enrollmentMapper.selectConfirmedEnrollmentIdsByCourseId(courseId)).willReturn(confirmedIds);
        willThrow(new RuntimeException("토스 API 오류")).given(paymentService).refund(eq(10L), any());

        // when
        assertThatCode(() -> adminService.forceCloseCourse(courseId)).doesNotThrowAnyException();

        // then: 10L 실패해도 11L 처리됨
        then(paymentService).should().refund(eq(11L), eq("강의 폐강"));
        then(enrollmentMapper).should(never()).updateEnrollmentStatus(argThat(e ->
                Long.valueOf(10L).equals(e.getId()) && EnrollmentStatus.FORCE_CLOSED.equals(e.getStatus())));
        then(enrollmentMapper).should().updateEnrollmentStatus(argThat(e ->
                Long.valueOf(11L).equals(e.getId()) && EnrollmentStatus.FORCE_CLOSED.equals(e.getStatus())));
    }

    @Test
    @DisplayName("강제 폐강 실패 - 강의 없음 404")
    void forceCloseCourse_courseNotFound_throws404() {
        // given
        given(courseMapper.selectCourseById(any())).willReturn(null);

        // when & then
        assertThatThrownBy(() -> adminService.forceCloseCourse(99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("존재하지 않는 강의");
    }

    @Test
    @DisplayName("강제 폐강 실패 - 이미 마감된 강의 400")
    void forceCloseCourse_alreadyClosed_throws400() {
        // given
        Course course = Course.builder().id(1L).status(CourseStatus.CLOSED).build();
        given(courseMapper.selectCourseById(1L)).willReturn(course);

        // when & then
        assertThatThrownBy(() -> adminService.forceCloseCourse(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("이미 폐강");
    }

    @Test
    @DisplayName("강제 폐강 실패 - 이미 강제 폐강된 강의 400")
    void forceCloseCourse_alreadyForceClosed_throws400() {
        // given
        Course course = Course.builder().id(1L).status(CourseStatus.FORCE_CLOSED).build();
        given(courseMapper.selectCourseById(1L)).willReturn(course);

        // when & then
        assertThatThrownBy(() -> adminService.forceCloseCourse(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("이미 폐강");
    }
}
