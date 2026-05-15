package com.yujin.course_enrollment.service;

import com.yujin.course_enrollment.entity.User;
import com.yujin.course_enrollment.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 사용자 서비스
 * 사용자 관련 비즈니스 로직을 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserMapper userMapper;

    /**
     * 전체 사용자 목록 조회
     * @return 전체 사용자 목록
     */
    public List<User> findUserList() {
        log.info("[UserService] 전체 사용자 목록 조회");

        return userMapper.selectUserList();
    }
}
