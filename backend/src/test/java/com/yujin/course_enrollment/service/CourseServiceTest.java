package com.yujin.course_enrollment.service;

import com.yujin.course_enrollment.dto.req.ReqCourseCreateDto;
import com.yujin.course_enrollment.dto.resp.RespCourseCreateDto;
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
                LocalDate.of(2025, 6, 1),
                LocalDate.of(2025, 8, 31)
        );
        Course savedCourse = Course.builder()
                .id(1L)
                .creatorId(creatorId)
                .title("Spring Boot 강의")
                .description("설명")
                .price(50000)
                .capacity(30)
                .startDate(LocalDate.of(2025, 6, 1))
                .endDate(LocalDate.of(2025, 8, 31))
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
                LocalDate.of(2025, 6, 1),
                LocalDate.of(2025, 8, 31)
        );

        // when & then
        assertThatThrownBy(() -> courseService.registerCourse(creatorId, req))
                .isInstanceOf(IllegalArgumentException.class)
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
                LocalDate.of(2025, 6, 1),
                LocalDate.of(2025, 8, 31)
        );

        // when & then
        assertThatThrownBy(() -> courseService.registerCourse(creatorId, req))
                .isInstanceOf(IllegalArgumentException.class)
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
                LocalDate.of(2025, 8, 31),
                LocalDate.of(2025, 6, 1)
        );

        // when & then
        assertThatThrownBy(() -> courseService.registerCourse(creatorId, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("종료일은 시작일보다 이전일 수 없습니다.");
    }

    @Test
    @DisplayName("날짜 경계값 - 시작일과 종료일이 같은 경우 허용")
    void registerCourse_sameDate_success() {
        // given
        Long creatorId = 1L;
        User creator = new User(1L, "강사A", "CREATOR");
        Course savedCourse = Course.builder()
                .id(1L)
                .creatorId(creatorId)
                .title("당일 클래스")
                .description("설명")
                .price(10000)
                .capacity(10)
                .startDate(LocalDate.of(2025, 6, 1))
                .endDate(LocalDate.of(2025, 6, 1))
                .createdAt(LocalDateTime.now())
                .build();

        given(userMapper.selectUserById(creatorId)).willReturn(creator);
        willDoNothing().given(courseMapper).insertCourse(any());
        given(courseMapper.selectCourseById(any())).willReturn(savedCourse);

        ReqCourseCreateDto req = new ReqCourseCreateDto(
                "당일 클래스", "설명", 10000, 10,
                LocalDate.of(2025, 6, 1),
                LocalDate.of(2025, 6, 1)
        );

        // when
        RespCourseCreateDto result = courseService.registerCourse(creatorId, req);

        // then
        assertThat(result.getTitle()).isEqualTo("당일 클래스");
        then(courseMapper).should().insertCourse(any());
        then(courseMapper).should().selectCourseById(any());
    }
}