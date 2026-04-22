package com.yujin.course_enrollment.mapper;

import com.yujin.course_enrollment.entity.Course;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

/**
 * 강의 Mapper 인터페이스
 * CourseMapper.xml과 매핑되어 강의 관련 DB 접근을 담당
 */
@Mapper
public interface CourseMapper {
    /* 강의 등록 */
    void insertCourse(Course course);
}