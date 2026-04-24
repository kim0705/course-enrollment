package com.yujin.course_enrollment.service;

import com.yujin.course_enrollment.dto.req.ReqEnrollmentCreateDto;
import com.yujin.course_enrollment.dto.resp.RespEnrollmentDto;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

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
        given(enrollmentMapper.selectEnrollmentByUserIdAndCourseId(userId, courseId)).willReturn(null);
        willDoNothing().given(enrollmentMapper).insertEnrollment(any());
        given(courseMapper.incrementEnrolledCount(courseId)).willReturn(1);
        given(enrollmentMapper.selectEnrollmentById(any())).willReturn(saved);

        // when
        RespEnrollmentDto result = enrollmentService.registerEnrollment(userId, new ReqEnrollmentCreateDto(courseId));

        // then
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getCourseId()).isEqualTo(courseId);
        assertThat(result.getStatus()).isEqualTo("PENDING");
        then(enrollmentMapper).should().insertEnrollment(any());
        then(courseMapper).should().incrementEnrolledCount(courseId);
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
    @DisplayName("동시 신청으로 정원 초과 - 수강 신청 실패")
    void registerEnrollment_capacityExceeded_concurrent() {
        // given
        Long userId = 4L;
        Long courseId = 1L;
        User student = new User(userId, "수강생A", "STUDENT");
        Course openCourse = Course.builder()
                .id(courseId)
                .creatorId(1L)
                .status("OPEN")
                .capacity(10)
                .enrolledCount(9)
                .build();
        ReqEnrollmentCreateDto req = new ReqEnrollmentCreateDto(courseId);

        given(userMapper.selectUserById(userId)).willReturn(student);
        given(courseMapper.selectCourseById(courseId)).willReturn(openCourse);
        given(enrollmentMapper.selectEnrollmentByUserIdAndCourseId(userId, courseId)).willReturn(null);
        willDoNothing().given(enrollmentMapper).insertEnrollment(any());
        given(courseMapper.incrementEnrolledCount(courseId)).willReturn(0);

        // when & then
        assertThatThrownBy(() -> enrollmentService.registerEnrollment(userId, req))
                .isInstanceOf(BusinessException.class)
                .hasMessage("수강 정원이 초과되었습니다.");
    }

    @Test
    @DisplayName("정원 초과 - 수강 신청 실패")
    void registerEnrollment_capacityExceeded() {
        // given
        Long userId = 4L;
        Long courseId = 1L;
        User student = new User(userId, "수강생A", "STUDENT");
        Course fullCourse = Course.builder()
                .id(courseId)
                .creatorId(1L)
                .status("OPEN")
                .capacity(10)
                .enrolledCount(10)
                .build();
        ReqEnrollmentCreateDto req = new ReqEnrollmentCreateDto(courseId);

        given(userMapper.selectUserById(userId)).willReturn(student);
        given(courseMapper.selectCourseById(courseId)).willReturn(fullCourse);
        given(enrollmentMapper.selectEnrollmentByUserIdAndCourseId(userId, courseId)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> enrollmentService.registerEnrollment(userId, req))
                .isInstanceOf(BusinessException.class)
                .hasMessage("수강 정원이 초과되었습니다.");
    }
}
