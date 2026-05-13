package com.yujin.course_enrollment.mapper;

import com.yujin.course_enrollment.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 사용자 Mapper 인터페이스
 * UserMapper.xml과 매핑되어 사용자 관련 DB 접근을 담당
 */
@Mapper
public interface UserMapper {
    /* 사용자 단건 조회 */
    User selectUserById(Long id);

    /* 전체 사용자 목록 조회 */
    List<User> selectUserList();

    /* 이메일로 사용자 조회 */
    User selectUserByEmail(String email);

    /* 아이디로 사용자 조회 */
    User selectUserByUsername(String username);

    /* 사용자 등록 */
    void insertUser(User user);
}
