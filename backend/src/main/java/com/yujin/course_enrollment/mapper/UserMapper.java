package com.yujin.course_enrollment.mapper;

import com.yujin.course_enrollment.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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

    /* 사용자 역할 변경 */
    void updateUserRole(@Param("id") Long id, @Param("role") String role);

    /* 프로필 수정 (이름·이메일) */
    void updateUserInfo(User user);

    /* 비밀번호 변경 */
    void updateUserPassword(@Param("id") Long id, @Param("password") String password);

    /* 비밀번호 조회 (검증용) */
    String selectPasswordById(Long id);

    /* 전체 사용자 수 조회 */
    int selectUserCount();

    /* 역할별 사용자 수 조회 */
    int selectUserCountByRole(@Param("role") String role);
}
