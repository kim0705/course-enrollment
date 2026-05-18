package com.yujin.course_enrollment.mapper;

import com.yujin.course_enrollment.dto.req.ReqAdminCoursePageDto;
import com.yujin.course_enrollment.dto.req.ReqCourseSearchDto;
import com.yujin.course_enrollment.dto.req.ReqMyCoursePageDto;
import com.yujin.course_enrollment.dto.resp.RespCourseDetailDto;
import com.yujin.course_enrollment.dto.resp.RespCourseListDto;
import com.yujin.course_enrollment.entity.Course;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 강의 Mapper 인터페이스
 * CourseMapper.xml과 매핑되어 강의 관련 DB 접근을 담당
 */
@Mapper
public interface CourseMapper {
    /* 강의 등록 */
    void insertCourse(Course course);

    /* 강의 단건 조회 */
    Course selectCourseById(Long id);

    /* 강의 목록 조회 (상태 필터 가능) */
    List<RespCourseListDto> selectCourseList(ReqCourseSearchDto reqCourseSearchDto);

    /* 강의 목록 전체 수 조회 (페이징용) */
    int selectCourseListCount(ReqCourseSearchDto reqCourseSearchDto);

    /* 강의 상세 조회 */
    RespCourseDetailDto selectCourseDetailById(Long id);

    /* 강의 수정 */
    void updateCourse(Course course);

    /* 강의 상태 변경 */
    void updateCourseStatus(Course course);

    /* 수강 인원 증가 (정원 미만일 때만, 반환값: 업데이트된 행 수) */
    int updateCourseEnrolledCountPlus(Long courseId);

    /* 수강 인원 감소 */
    void updateCourseEnrolledCountMinus(Long courseId);

    /* 나의 강의 목록 조회 (CREATOR 전용) */
    List<RespCourseListDto> selectCourseListByCreatorId(ReqMyCoursePageDto reqMyCoursePageDto);

    /* 나의 강의 목록 전체 수 조회 (페이징용) */
    int selectCourseListByCreatorIdCount(Long creatorId);

    /* 관리자 전체 강의 목록 조회 */
    List<RespCourseListDto> selectAdminCourseList(ReqAdminCoursePageDto reqAdminCoursePageDto);

    /* 관리자 전체 강의 수 조회 */
    int selectAdminCourseListCount();
}