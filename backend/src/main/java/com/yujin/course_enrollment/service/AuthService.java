package com.yujin.course_enrollment.service;

import com.yujin.course_enrollment.config.JwtUtil;
import com.yujin.course_enrollment.entity.User;
import com.yujin.course_enrollment.global.exception.BusinessException;
import com.yujin.course_enrollment.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * 인증 서비스
 * 로그인, 회원가입, 중복 확인 등 인증 관련 비즈니스 로직을 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * AccessToken 생성
     * @param user 인증된 사용자 엔티티
     * @return 서명된 JWT 문자열
     */
    public String generateToken(User user) {
        return jwtUtil.generateToken(user.getId(), user.getRole());
    }

    /**
     * 로그인
     * @param username 아이디
     * @param password 비밀번호
     * @return 인증된 사용자 엔티티
     * @throws BusinessException 아이디 없음 또는 비밀번호 불일치 (401)
     */
    public User login(String username, String password) {
        log.debug("[AuthService] 로그인 - username: {}", username);

        User user = userMapper.selectUserByUsername(username);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        return user;
    }

    /**
     * 아이디 사용 가능 여부 확인
     * @param username 확인할 아이디
     * @return 사용 가능 여부
     */
    public boolean isUsernameAvailable(String username) {
        log.debug("[AuthService] 아이디 중복 확인 - username: {}", username);

        return userMapper.selectUserByUsername(username) == null;
    }

    /**
     * 이메일 사용 가능 여부 확인
     * @param email 확인할 이메일
     * @return 사용 가능 여부
     */
    public boolean isEmailAvailable(String email) {
        log.debug("[AuthService] 이메일 중복 확인 - email: {}", email);

        return userMapper.selectUserByEmail(email) == null;
    }

    /**
     * 회원가입
     * @param user 회원가입 요청 (username, name, email, password)
     * @throws BusinessException 아이디 또는 이메일 중복 (409)
     */
    @Transactional
    public void signup(User user) {
        log.info("[AuthService] 회원가입 - username: {}, email: {}", user.getUsername(), user.getEmail());

        if (userMapper.selectUserByUsername(user.getUsername()) != null) {
            throw new BusinessException(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다.");
        }

        if (userMapper.selectUserByEmail(user.getEmail()) != null) {
            throw new BusinessException(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.");
        }

        userMapper.insertUser(User.ofSignup(user.getUsername(), user.getName(), user.getEmail(), passwordEncoder.encode(user.getPassword())));
    }
}
