package com.yujin.course_enrollment.service;

import com.yujin.course_enrollment.dto.req.ReqEnrollmentCreateDto;
import com.yujin.course_enrollment.dto.req.ReqEnrollmentPageDto;
import com.yujin.course_enrollment.dto.resp.RespEnrollmentDto;
import com.yujin.course_enrollment.dto.resp.RespEnrollmentStudentDto;
import com.yujin.course_enrollment.dto.resp.RespPageDto;
import com.yujin.course_enrollment.entity.Course;
import com.yujin.course_enrollment.entity.Enrollment;
import com.yujin.course_enrollment.entity.User;
import com.yujin.course_enrollment.global.exception.BusinessException;
import com.yujin.course_enrollment.mapper.CourseMapper;
import com.yujin.course_enrollment.mapper.EnrollmentMapper;
import com.yujin.course_enrollment.mapper.UserMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.times;

/**
 * 수강 신청 서비스 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @InjectMocks
    private EnrollmentService enrollmentService;

    @Mock
    private EnrollmentMapper enrollmentMapper;

    @Mock
    private CourseMapper courseMapper;

    @Mock
    private UserMapper userMapper;

    @Test
    @DisplayName("수강 신청 성공")
    void registerEnrollment_success() {
        // given
        Long userId = 4L;
        Long courseId = 1L;
        User student = new User(userId, "수강생A", "STUDENT");
        Course openCourse = Course.builder()
                .id(courseId)
                .creatorId(1L)
                .title("Spring Boot 강의")
                .status("OPEN")
                .capacity(30)
                .enrolledCount(10)
                .build();
        Enrollment saved = Enrollment.builder()
                .id(1L)
                .userId(userId)
                .courseId(courseId)
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        given(userMapper.selectUserById(userId)).willReturn(student);
        given(courseMapper.selectCourseById(courseId)).willReturn(openCourse);
        given(enrollmentMapper.selectEnrollmentByUserIdAndCourseId(userId, courseId)).willReturn(null).willReturn(saved);
        given(courseMapper.updateCourseEnrolledCountPlus(courseId)).willReturn(1);
        willDoNothing().given(enrollmentMapper).insertEnrollment(any());

        // when
        RespEnrollmentDto result = enrollmentService.registerEnrollment(userId, new ReqEnrollmentCreateDto(courseId));

        // then
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getCourseId()).isEqualTo(courseId);
        assertThat(result.getStatus()).isEqualTo("PENDING");
        then(enrollmentMapper).should().insertEnrollment(any());
        then(courseMapper).should().updateCourseEnrolledCountPlus(courseId);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 - 수강 신청 실패")
    void registerEnrollment_userNotFound() {
        // given
        Long userId = 999L;
        ReqEnrollmentCreateDto req = new ReqEnrollmentCreateDto(1L);
        given(userMapper.selectUserById(userId)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> enrollmentService.registerEnrollment(userId, req))
                .isInstanceOf(BusinessException.class)
                .hasMessage("존재하지 않는 사용자입니다.");
    }

    @Test
    @DisplayName("존재하지 않는 강의 - 수강 신청 실패")
    void registerEnrollment_courseNotFound() {
        // given
        Long userId = 4L;
        Long courseId = 999L;
        User student = new User(userId, "수강생A", "STUDENT");
        ReqEnrollmentCreateDto req = new ReqEnrollmentCreateDto(courseId);

        given(userMapper.selectUserById(userId)).willReturn(student);
        given(courseMapper.selectCourseById(courseId)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> enrollmentService.registerEnrollment(userId, req))
                .isInstanceOf(BusinessException.class)
                .hasMessage("존재하지 않는 강의입니다.");
    }

    @Test
    @DisplayName("본인 강의 신청 - 수강 신청 실패")
    void registerEnrollment_ownCourse() {
        // given
        Long userId = 1L;
        Long courseId = 1L;
        User creator = new User(userId, "강사A", "CREATOR");
        Course ownCourse = Course.builder()
                .id(courseId)
                .creatorId(userId)
                .status("OPEN")
                .build();
        ReqEnrollmentCreateDto req = new ReqEnrollmentCreateDto(courseId);

        given(userMapper.selectUserById(userId)).willReturn(creator);
        given(courseMapper.selectCourseById(courseId)).willReturn(ownCourse);

        // when & then
        assertThatThrownBy(() -> enrollmentService.registerEnrollment(userId, req))
                .isInstanceOf(BusinessException.class)
                .hasMessage("본인이 개설한 강의는 신청할 수 없습니다.");
    }

    @Test
    @DisplayName("모집 중이 아닌 강의 - 수강 신청 실패")
    void registerEnrollment_courseNotOpen() {
        // given
        Long userId = 4L;
        Long courseId = 1L;
        User student = new User(userId, "수강생A", "STUDENT");
        Course closedCourse = Course.builder()
                .id(courseId)
                .creatorId(1L)
                .status("CLOSED")
                .build();
        ReqEnrollmentCreateDto req = new ReqEnrollmentCreateDto(courseId);

        given(userMapper.selectUserById(userId)).willReturn(student);
        given(courseMapper.selectCourseById(courseId)).willReturn(closedCourse);

        // when & then
        assertThatThrownBy(() -> enrollmentService.registerEnrollment(userId, req))
                .isInstanceOf(BusinessException.class)
                .hasMessage("모집 중인 강의만 신청할 수 있습니다.");
    }

    @Test
    @DisplayName("중복 신청 - 수강 신청 실패")
    void registerEnrollment_duplicate() {
        // given
        Long userId = 4L;
        Long courseId = 1L;
        User student = new User(userId, "수강생A", "STUDENT");
        Course openCourse = Course.builder()
                .id(courseId)
                .creatorId(1L)
                .status("OPEN")
                .build();
        Enrollment existing = Enrollment.builder()
                .id(1L)
                .userId(userId)
                .courseId(courseId)
                .status("PENDING")
                .build();
        ReqEnrollmentCreateDto req = new ReqEnrollmentCreateDto(courseId);

        given(userMapper.selectUserById(userId)).willReturn(student);
        given(courseMapper.selectCourseById(courseId)).willReturn(openCourse);
        given(enrollmentMapper.selectEnrollmentByUserIdAndCourseId(userId, courseId)).willReturn(existing);

        // when & then
        assertThatThrownBy(() -> enrollmentService.registerEnrollment(userId, req))
                .isInstanceOf(BusinessException.class)
                .hasMessage("이미 신청한 강의입니다.");
    }

    @Test
    @DisplayName("취소 후 재신청 성공")
    void registerEnrollment_reEnrollAfterCancel() {
        // given
        Long userId = 4L;
        Long courseId = 1L;
        User student = new User(userId, "수강생A", "STUDENT");
        Course openCourse = Course.builder()
                .id(courseId)
                .creatorId(1L)
                .title("Spring Boot 강의")
                .status("OPEN")
                .capacity(30)
                .enrolledCount(10)
                .build();
        Enrollment cancelled = Enrollment.builder()
                .id(1L)
                .userId(userId)
                .courseId(courseId)
                .status("CANCELLED")
                .build();
        Enrollment reEnrolled = Enrollment.builder()
                .id(1L)
                .userId(userId)
                .courseId(courseId)
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();
        ReqEnrollmentCreateDto req = new ReqEnrollmentCreateDto(courseId);

        given(userMapper.selectUserById(userId)).willReturn(student);
        given(courseMapper.selectCourseById(courseId)).willReturn(openCourse);
        given(enrollmentMapper.selectEnrollmentByUserIdAndCourseId(userId, courseId)).willReturn(cancelled).willReturn(reEnrolled);
        willDoNothing().given(enrollmentMapper).insertEnrollment(any());
        given(courseMapper.updateCourseEnrolledCountPlus(courseId)).willReturn(1);

        // when
        RespEnrollmentDto result = enrollmentService.registerEnrollment(userId, req);

        // then
        assertThat(result.getStatus()).isEqualTo("PENDING");
        then(enrollmentMapper).should().insertEnrollment(any());
        then(courseMapper).should().updateCourseEnrolledCountPlus(courseId);
    }

    @Test
    @DisplayName("동시 신청으로 정원 초과 - 대기열 등록")
    void registerEnrollment_capacityExceeded_concurrent() {
        // given
        Long userId = 4L;
        Long courseId = 1L;
        User student = new User(userId, "수강생A", "STUDENT");
        Course openCourse = Course.builder()
                .id(courseId)
                .creatorId(1L)
                .title("Spring Boot 강의")
                .status("OPEN")
                .capacity(10)
                .enrolledCount(9)
                .build();
        Enrollment waitlisted = Enrollment.builder()
                .id(1L)
                .userId(userId)
                .courseId(courseId)
                .status("WAITLIST")
                .createdAt(LocalDateTime.now())
                .build();
        ReqEnrollmentCreateDto req = new ReqEnrollmentCreateDto(courseId);

        given(userMapper.selectUserById(userId)).willReturn(student);
        given(courseMapper.selectCourseById(courseId)).willReturn(openCourse);
        given(enrollmentMapper.selectEnrollmentByUserIdAndCourseId(userId, courseId)).willReturn(null).willReturn(waitlisted);
        willDoNothing().given(enrollmentMapper).insertEnrollment(any());
        given(courseMapper.updateCourseEnrolledCountPlus(courseId)).willReturn(0);

        // when
        RespEnrollmentDto result = enrollmentService.registerEnrollment(userId, req);

        // then
        assertThat(result.getStatus()).isEqualTo("WAITLIST");
        then(enrollmentMapper).should(times(1)).insertEnrollment(any());
        then(courseMapper).should().updateCourseEnrolledCountPlus(courseId);
    }

    @Test
    @DisplayName("정원 초과 - 대기열 등록")
    void registerEnrollment_capacityExceeded() {
        // given
        Long userId = 4L;
        Long courseId = 1L;
        User student = new User(userId, "수강생A", "STUDENT");
        Course fullCourse = Course.builder()
                .id(courseId)
                .creatorId(1L)
                .title("Spring Boot 강의")
                .status("OPEN")
                .capacity(10)
                .enrolledCount(10)
                .build();
        Enrollment waitlisted = Enrollment.builder()
                .id(1L)
                .userId(userId)
                .courseId(courseId)
                .status("WAITLIST")
                .createdAt(LocalDateTime.now())
                .build();
        ReqEnrollmentCreateDto req = new ReqEnrollmentCreateDto(courseId);

        given(userMapper.selectUserById(userId)).willReturn(student);
        given(courseMapper.selectCourseById(courseId)).willReturn(fullCourse);
        given(enrollmentMapper.selectEnrollmentByUserIdAndCourseId(userId, courseId)).willReturn(null).willReturn(waitlisted);
        willDoNothing().given(enrollmentMapper).insertEnrollment(any());

        // when
        RespEnrollmentDto result = enrollmentService.registerEnrollment(userId, req);

        // then
        assertThat(result.getStatus()).isEqualTo("WAITLIST");
        then(enrollmentMapper).should().insertEnrollment(any());
        then(courseMapper).should(never()).updateCourseEnrolledCountPlus(courseId);
    }

    @Test
    @DisplayName("수강 신청 목록 조회 성공")
    void findMyEnrollments_success() {
        // given
        Long userId = 4L;
        ReqEnrollmentPageDto pageDto = new ReqEnrollmentPageDto();
        List<RespEnrollmentStudentDto> list = List.of(new RespEnrollmentStudentDto(), new RespEnrollmentStudentDto());
        given(enrollmentMapper.selectEnrollmentListByUserId(pageDto)).willReturn(list);
        given(enrollmentMapper.selectEnrollmentListByUserIdCount(userId)).willReturn(2);

        // when
        RespPageDto<RespEnrollmentStudentDto> result = enrollmentService.findMyEnrollments(userId, pageDto);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalCount()).isEqualTo(2);
        then(enrollmentMapper).should().selectEnrollmentListByUserId(pageDto);
        then(enrollmentMapper).should().selectEnrollmentListByUserIdCount(userId);
    }

    @Test
    @DisplayName("결제 요청 성공 - PENDING → CONFIRMED")
    void confirmEnrollment_success() {
        // given
        Long userId = 4L;
        Long enrollmentId = 1L;
        Enrollment pending = Enrollment.builder()
                .id(enrollmentId)
                .userId(userId)
                .courseId(1L)
                .status("PENDING")
                .build();
        Course course = Course.builder().id(1L).title("Spring Boot 강의").build();
        Enrollment confirmed = Enrollment.builder()
                .id(enrollmentId)
                .userId(userId)
                .courseId(1L)
                .status("CONFIRMED")
                .confirmedAt(LocalDateTime.now())
                .build();

        given(enrollmentMapper.selectEnrollmentById(enrollmentId)).willReturn(pending).willReturn(confirmed);
        willDoNothing().given(enrollmentMapper).updateEnrollmentStatus(any());
        given(courseMapper.selectCourseById(1L)).willReturn(course);

        // when
        RespEnrollmentDto result = enrollmentService.confirmEnrollment(userId, enrollmentId);

        // then
        assertThat(result.getStatus()).isEqualTo("CONFIRMED");
        then(enrollmentMapper).should().updateEnrollmentStatus(any());
    }

    @Test
    @DisplayName("본인 신청 아님 - 결제 요청 실패")
    void confirmEnrollment_fail_notOwner() {
        // given
        Long userId = 99L;
        Long enrollmentId = 1L;
        Enrollment pending = Enrollment.builder()
                .id(enrollmentId)
                .userId(4L)
                .courseId(1L)
                .status("PENDING")
                .build();

        given(enrollmentMapper.selectEnrollmentById(enrollmentId)).willReturn(pending);

        // when & then
        assertThatThrownBy(() -> enrollmentService.confirmEnrollment(userId, enrollmentId))
                .isInstanceOf(BusinessException.class)
                .hasMessage("본인의 수강 신청만 결제할 수 있습니다.");
    }

    @Test
    @DisplayName("PENDING 상태 아님 - 결제 요청 실패")
    void confirmEnrollment_fail_notPending() {
        // given
        Long userId = 4L;
        Long enrollmentId = 1L;
        Enrollment confirmed = Enrollment.builder()
                .id(enrollmentId)
                .userId(userId)
                .courseId(1L)
                .status("CONFIRMED")
                .build();

        given(enrollmentMapper.selectEnrollmentById(enrollmentId)).willReturn(confirmed);

        // when & then
        assertThatThrownBy(() -> enrollmentService.confirmEnrollment(userId, enrollmentId))
                .isInstanceOf(BusinessException.class)
                .hasMessage("PENDING 상태의 수강 신청만 결제할 수 있습니다.");
    }

    @Test
    @DisplayName("수강 취소 성공 - PENDING → CANCELLED")
    void cancelEnrollment_success_pending() {
        // given
        Long userId = 4L;
        Long enrollmentId = 1L;
        Enrollment pending = Enrollment.builder()
                .id(enrollmentId)
                .userId(userId)
                .courseId(1L)
                .status("PENDING")
                .build();
        Course course = Course.builder().id(1L).title("Spring Boot 강의").build();
        Enrollment cancelled = Enrollment.builder()
                .id(enrollmentId)
                .userId(userId)
                .courseId(1L)
                .status("CANCELLED")
                .cancelledAt(LocalDateTime.now())
                .build();

        given(enrollmentMapper.selectEnrollmentById(enrollmentId)).willReturn(pending).willReturn(cancelled);
        willDoNothing().given(enrollmentMapper).updateEnrollmentStatus(any());
        willDoNothing().given(courseMapper).updateCourseEnrolledCountMinus(1L);
        given(courseMapper.selectCourseById(1L)).willReturn(course);

        // when
        RespEnrollmentDto result = enrollmentService.cancelEnrollment(userId, enrollmentId);

        // then
        assertThat(result.getStatus()).isEqualTo("CANCELLED");
        then(enrollmentMapper).should().updateEnrollmentStatus(any());
        then(courseMapper).should().updateCourseEnrolledCountMinus(1L);
    }

    @Test
    @DisplayName("이미 취소됨 - 수강 취소 실패")
    void cancelEnrollment_fail_alreadyCancelled() {
        // given
        Long userId = 4L;
        Long enrollmentId = 1L;
        Enrollment cancelled = Enrollment.builder()
                .id(enrollmentId)
                .userId(userId)
                .courseId(1L)
                .status("CANCELLED")
                .build();

        given(enrollmentMapper.selectEnrollmentById(enrollmentId)).willReturn(cancelled);

        // when & then
        assertThatThrownBy(() -> enrollmentService.cancelEnrollment(userId, enrollmentId))
                .isInstanceOf(BusinessException.class)
                .hasMessage("이미 취소된 수강 신청입니다.");
    }

    @Test
    @DisplayName("취소 기간 초과 - 수강 취소 실패")
    void cancelEnrollment_fail_expiredCancelPeriod() {
        // given
        Long userId = 4L;
        Long enrollmentId = 1L;
        Enrollment confirmed = Enrollment.builder()
                .id(enrollmentId)
                .userId(userId)
                .courseId(1L)
                .status("CONFIRMED")
                .confirmedAt(LocalDateTime.now().minusDays(8))
                .build();

        given(enrollmentMapper.selectEnrollmentById(enrollmentId)).willReturn(confirmed);

        // when & then
        assertThatThrownBy(() -> enrollmentService.cancelEnrollment(userId, enrollmentId))
                .isInstanceOf(BusinessException.class)
                .hasMessage("수강 확정 후 7일이 지나 취소할 수 없습니다.");
    }
}
