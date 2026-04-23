package com.yujin.course_enrollment.mapper;

import com.yujin.course_enrollment.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 사용자 Mapper 인터페이스
 * UserMapper.xml과 매핑되어 사용자 관련 DB 접근을 담당
 */
@Mapper
public interface UserMapper {
    /* 사용자 단건 조회 */
    User selectUserById(Long id);
}