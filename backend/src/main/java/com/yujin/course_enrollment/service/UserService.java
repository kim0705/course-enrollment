package com.yujin.course_enrollment.service;

import com.yujin.course_enrollment.entity.User;
import com.yujin.course_enrollment.global.exception.BusinessException;
import com.yujin.course_enrollment.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 전체 사용자 목록 조회
     * @return 전체 사용자 목록
     */
    public List<User> findUserList() {
        log.info("[UserService] 전체 사용자 목록 조회");

        return userMapper.selectUserList();
    }

    /**
     * 아이디 사용 가능 여부 확인
     * @param username 확인할 아이디
     * @return 사용 가능 여부
     */
    public boolean isUsernameAvailable(String username) {
        log.debug("[UserService] 아이디 중복 확인 - username: {}", username);

        return userMapper.selectUserByUsername(username) == null;
    }

    /**
     * 이메일 사용 가능 여부 확인
     * @param email 확인할 이메일
     * @return 사용 가능 여부
     */
    public boolean isEmailAvailable(String email) {
        log.debug("[UserService] 이메일 중복 확인 - email: {}", email);

        return userMapper.selectUserByEmail(email) == null;
    }

    /**
     * 회원가입
     * @param user 회원가입 요청 (username, name, email, password)
     * @throws BusinessException 아이디 또는 이메일 중복 (409)
     */
    @Transactional
    public void signup(User user) {
        log.info("[UserService] 회원가입 - username: {}, email: {}", user.getUsername(), user.getEmail());

        if (userMapper.selectUserByUsername(user.getUsername()) != null) {
            throw new BusinessException(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다.");
        }

        if (userMapper.selectUserByEmail(user.getEmail()) != null) {
            throw new BusinessException(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.");
        }

        userMapper.insertUser(User.ofSignup(user.getUsername(), user.getName(), user.getEmail(), passwordEncoder.encode(user.getPassword())));
    }
}
