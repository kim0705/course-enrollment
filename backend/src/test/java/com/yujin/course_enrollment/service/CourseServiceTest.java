package com.yujin.course_enrollment.service;

import com.yujin.course_enrollment.dto.req.ReqCourseCreateDto;
import com.yujin.course_enrollment.global.exception.BusinessException;
import com.yujin.course_enrollment.dto.req.ReqCourseUpdateDto;
import com.yujin.course_enrollment.dto.resp.RespCourseCreateDto;
import com.yujin.course_enrollment.dto.resp.RespCourseDetailDto;
import com.yujin.course_enrollment.entity.Course;
import com.yujin.course_enrollment.entity.User;
import com.yujin.course_enrollment.mapper.CourseMapper;
import com.yujin.course_enrollment.mapper.UserMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

/**
 * 강의 서비스 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @InjectMocks
    private CourseService courseService;

    @Mock
    private CourseMapper courseMapper;

    @Mock
    private UserMapper userMapper;

    @Test
    @DisplayName("강의 등록 성공")
    void registerCourse_success() {
        // given
        Long creatorId = 1L;
        User creator = new User(1L, "강사A", "CREATOR");
        ReqCourseCreateDto req = new ReqCourseCreateDto(
                "Spring Boot 강의", "설명", 50000, 30,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusMonths(1)
        );
        Course savedCourse = Course.builder()
                .id(1L)
                .creatorId(creatorId)
                .title("Spring Boot 강의")
                .description("설명")
                .price(50000)
                .capacity(30)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusMonths(1))
                .createdAt(LocalDateTime.now())
                .build();

        given(userMapper.selectUserById(creatorId)).willReturn(creator);
        willDoNothing().given(courseMapper).insertCourse(any());
        given(courseMapper.selectCourseById(any())).willReturn(savedCourse);

        // when
        RespCourseCreateDto result = courseService.registerCourse(creatorId, req);

        // then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Spring Boot 강의");
        then(courseMapper).should().insertCourse(any());
        then(courseMapper).should().selectCourseById(any());
    }

    @Test
    @DisplayName("존재하지 않는 사용자 - 강의 등록 실패")
    void registerCourse_userNotFound() {
        // given
        Long creatorId = 999L;
        given(userMapper.selectUserById(creatorId)).willReturn(null);

        ReqCourseCreateDto req = new ReqCourseCreateDto(
                "Spring Boot 강의", "설명", 50000, 30,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(10)
        );

        // when & then
        assertThatThrownBy(() -> courseService.registerCourse(creatorId, req))
                .isInstanceOf(BusinessException.class)
                .hasMessage("존재하지 않는 사용자입니다.");
    }

    @Test
    @DisplayName("크리에이터 권한 없음 - 강의 등록 실패")
    void registerCourse_notCreator() {
        // given
        Long creatorId = 3L;
        User student = new User(3L, "수강생A", "STUDENT");
        given(userMapper.selectUserById(creatorId)).willReturn(student);

        ReqCourseCreateDto req = new ReqCourseCreateDto(
                "Spring Boot 강의", "설명", 50000, 30,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(10)
        );

        // when & then
        assertThatThrownBy(() -> courseService.registerCourse(creatorId, req))
                .isInstanceOf(BusinessException.class)
                .hasMessage("강의 등록 권한이 없습니다.");
    }

    @Test
    @DisplayName("날짜 유효성 오류 - 종료일이 시작일보다 이전")
    void registerCourse_invalidDate() {
        // given
        Long creatorId = 1L;
        User creator = new User(1L, "강사A", "CREATOR");
        given(userMapper.selectUserById(creatorId)).willReturn(creator);

        ReqCourseCreateDto req = new ReqCourseCreateDto(
                "Spring Boot 강의", "설명", 50000, 30,
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(1)
        );

        // when & then
        assertThatThrownBy(() -> courseService.registerCourse(creatorId, req))
                .isInstanceOf(BusinessException.class)
                .hasMessage("종료일은 시작일보다 이전일 수 없습니다.");
    }

    @Test
    @DisplayName("날짜 경계값 - 시작일과 종료일이 같은 경우 허용")
    void registerCourse_sameDate_success() {
        // given
        Long creatorId = 1L;
        User creator = new User(1L, "강사A", "CREATOR");
        LocalDate futureDate = LocalDate.now().plusDays(10);

        ReqCourseCreateDto req = new ReqCourseCreateDto(
                "당일 클래스", "설명", 10000, 10,
                futureDate,
                futureDate
        );

        Course savedCourse = Course.builder()
                .id(1L)
                .creatorId(creatorId)
                .title("당일 클래스")
                .description("설명")
                .price(10000)
                .capacity(10)
                .startDate(futureDate)
                .endDate(futureDate)
                .createdAt(LocalDateTime.now())
                .build();

        given(userMapper.selectUserById(creatorId)).willReturn(creator);
        willDoNothing().given(courseMapper).insertCourse(any());
        given(courseMapper.selectCourseById(any())).willReturn(savedCourse);

        // when
        RespCourseCreateDto result = courseService.registerCourse(creatorId, req);

        // then
        assertThat(result.getTitle()).isEqualTo("당일 클래스");
        then(courseMapper).should().insertCourse(any());
        then(courseMapper).should().selectCourseById(any());
    }

    @Test
    @DisplayName("시작일이 과거 - 강의 등록 실패")
    void registerCourse_pastStartDate() {
        // given
        Long creatorId = 1L;
        User creator = new User(1L, "강사A", "CREATOR");
        given(userMapper.selectUserById(creatorId)).willReturn(creator);

        ReqCourseCreateDto req = new ReqCourseCreateDto(
                "Spring Boot 강의", "설명", 50000, 30,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusMonths(1)
        );

        // when & then
        assertThatThrownBy(() -> courseService.registerCourse(creatorId, req))
                .isInstanceOf(BusinessException.class)
                .hasMessage("시작일은 오늘 이후여야 합니다.");
    }

    @Test
    @DisplayName("종료일이 과거 - 강의 등록 실패")
    void registerCourse_pastEndDate() {
        // given
        Long creatorId = 1L;
        User creator = new User(1L, "강사A", "CREATOR");
        given(userMapper.selectUserById(creatorId)).willReturn(creator);

        ReqCourseCreateDto req = new ReqCourseCreateDto(
                "Spring Boot 강의", "설명", 50000, 30,
                LocalDate.now(),
                LocalDate.now().minusDays(1)
        );

        // when & then
        assertThatThrownBy(() -> courseService.registerCourse(creatorId, req))
                .isInstanceOf(BusinessException.class)
                .hasMessage("종료일은 오늘 이후여야 합니다.");
    }

    @Test
    @DisplayName("강의 수정 성공 - DRAFT 상태에서 정상 수정")
    void modifyCourse_success() {
        // given
        Long courseId = 1L;
        Long creatorId = 1L;
        Course existingCourse = Course.builder()
                .id(courseId)
                .creatorId(creatorId)
                .status("DRAFT")
                .build();

        ReqCourseUpdateDto req = new ReqCourseUpdateDto(
                "수정 제목", "수정 설명", 20000, 50,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(10)
        );

        RespCourseDetailDto respDto = new RespCourseDetailDto();
        respDto.setId(courseId);
        respDto.setTitle("수정 제목");

        given(courseMapper.selectCourseById(courseId)).willReturn(existingCourse);
        willDoNothing().given(courseMapper).updateCourse(any(Course.class));
        given(courseMapper.selectCourseDetailById(courseId)).willReturn(respDto);

        // when
        RespCourseDetailDto result = courseService.modifyCourse(creatorId, courseId, req);

        // then
        assertThat(result.getTitle()).isEqualTo("수정 제목");
        then(courseMapper).should().updateCourse(any(Course.class));
        then(courseMapper).should().selectCourseDetailById(courseId);
    }

    @Test
    @DisplayName("강의 수정 실패 - 수정 권한 없음")
    void modifyCourse_fail_forbidden() {
        // given
        Long courseId = 1L;
        Long otherUserId = 99L;
        Course existingCourse = Course.builder()
                .id(courseId)
                .creatorId(1L)
                .status("DRAFT")
                .build();

        ReqCourseUpdateDto req = new ReqCourseUpdateDto(
                "수정 제목", "수정 설명", 20000, 50,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(10)
        );

        given(courseMapper.selectCourseById(courseId)).willReturn(existingCourse);

        // when & then
        assertThatThrownBy(() -> courseService.modifyCourse(otherUserId, courseId, req))
                .isInstanceOf(BusinessException.class)
                .hasMessage("강의 수정 권한이 없습니다.");
    }

    @Test
    @DisplayName("강의 수정 실패 - DRAFT 상태가 아님")
    void modifyCourse_fail_notDraft() {
        // given
        Long courseId = 1L;
        Long creatorId = 1L;
        Course openCourse = Course.builder()
                .id(courseId)
                .creatorId(creatorId)
                .status("OPEN")
                .build();

        ReqCourseUpdateDto resp = new ReqCourseUpdateDto(
                "제목 수정", "설명", 1000, 10,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5)
        );

        given(courseMapper.selectCourseById(courseId)).willReturn(openCourse);

        // when & then
        assertThatThrownBy(() -> courseService.modifyCourse(creatorId, courseId, resp))
                .isInstanceOf(BusinessException.class)
                .hasMessage("DRAFT 상태의 강의만 수정할 수 있습니다.");
    }

    @Test
    @DisplayName("강의 공개 성공 - DRAFT에서 OPEN으로 변경")
    void publishCourse_success() {
        // given
        Long courseId = 1L;
        Long creatorId = 1L;
        Course draftCourse = Course.builder()
                .id(courseId)
                .creatorId(creatorId)
                .status("DRAFT")
                .build();

        RespCourseDetailDto resp = new RespCourseDetailDto();
        resp.setId(courseId);
        resp.setStatus("OPEN");

        given(courseMapper.selectCourseById(courseId)).willReturn(draftCourse);
        willDoNothing().given(courseMapper).updateCourseStatus(any(Course.class));
        given(courseMapper.selectCourseDetailById(courseId)).willReturn(resp);

        // when
        RespCourseDetailDto result = courseService.publishCourse(creatorId, courseId);

        // then
        assertThat(result.getStatus()).isEqualTo("OPEN");
        then(courseMapper).should().updateCourseStatus(any(Course.class));
        then(courseMapper).should().selectCourseDetailById(courseId);
    }

    @Test
    @DisplayName("강의 공개 실패 - 공개 권한 없음")
    void publishCourse_fail_forbidden() {
        // given
        Long courseId = 1L;
        Long otherUserId = 99L;
        Course draftCourse = Course.builder()
                .id(courseId)
                .creatorId(1L)
                .status("DRAFT")
                .build();

        given(courseMapper.selectCourseById(courseId)).willReturn(draftCourse);

        // when & then
        assertThatThrownBy(() -> courseService.publishCourse(otherUserId, courseId))
                .isInstanceOf(BusinessException.class)
                .hasMessage("강의 공개 권한이 없습니다.");
    }

    @Test
    @DisplayName("강의 공개 실패 - DRAFT 상태가 아님")
    void publishCourse_fail_notDraft() {
        // given
        Long courseId = 1L;
        Long creatorId = 1L;
        Course openCourse = Course.builder()
                .id(courseId)
                .creatorId(creatorId)
                .status("OPEN")
                .build();

        given(courseMapper.selectCourseById(courseId)).willReturn(openCourse);

        // when & then
        assertThatThrownBy(() -> courseService.publishCourse(creatorId, courseId))
                .isInstanceOf(BusinessException.class)
                .hasMessage("DRAFT 상태의 강의만 공개할 수 있습니다.");
    }

    @Test
    @DisplayName("강의 마감 성공 - OPEN에서 CLOSED로 변경")
    void closeCourse_success() {
        // given
        Long courseId = 1L;
        Long creatorId = 1L;
        Course openCourse = Course.builder()
                .id(courseId)
                .creatorId(creatorId)
                .status("OPEN")
                .build();

        RespCourseDetailDto respDto = new RespCourseDetailDto();
        respDto.setId(courseId);
        respDto.setStatus("CLOSED");

        given(courseMapper.selectCourseById(courseId)).willReturn(openCourse);
        willDoNothing().given(courseMapper).updateCourseStatus(any(Course.class));
        given(courseMapper.selectCourseDetailById(courseId)).willReturn(respDto);

        // when
        RespCourseDetailDto result = courseService.closeCourse(creatorId, courseId);

        // then
        assertThat(result.getStatus()).isEqualTo("CLOSED");
        then(courseMapper).should().updateCourseStatus(any(Course.class));
        then(courseMapper).should().selectCourseDetailById(courseId);
    }

    @Test
    @DisplayName("강의 마감 권한 없음 - 실패")
    void closeCourse_forbidden() {
        // given
        Long courseId = 1L;
        Long wrongUserId = 99L;
        Course course = Course.builder()
                .id(courseId)
                .creatorId(1L)
                .status("OPEN")
                .build();

        given(courseMapper.selectCourseById(courseId)).willReturn(course);

        // when & then
        assertThatThrownBy(() -> courseService.closeCourse(wrongUserId, courseId))
                .isInstanceOf(BusinessException.class)
                .hasMessage("강의 마감 권한이 없습니다.");
    }

    @Test
    @DisplayName("강의 마감 실패 - OPEN 상태가 아님")
    void closeCourse_fail_notOpen() {
        // given
        Long courseId = 1L;
        Long creatorId = 1L;
        Course draftCourse = Course.builder()
                .id(courseId)
                .creatorId(creatorId)
                .status("DRAFT")
                .build();

        given(courseMapper.selectCourseById(courseId)).willReturn(draftCourse);

        // when & then
        assertThatThrownBy(() -> courseService.closeCourse(creatorId, courseId))
                .isInstanceOf(BusinessException.class)
                .hasMessage("OPEN 상태의 강의만 마감할 수 있습니다.");
    }
}