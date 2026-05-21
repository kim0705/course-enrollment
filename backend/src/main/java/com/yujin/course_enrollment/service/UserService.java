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
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * 전체 사용자 목록 조회
     * @return 전체 사용자 목록
     */
    public List<User> findUserList() {
        log.info("[UserService] 전체 사용자 목록 조회");

        return userMapper.selectUserList();
    }

    /**
     * 프로필 수정 (이름·이메일)
     * @param userId 사용자 ID
     * @param name 새 이름
     * @param email 새 이메일
     * @throws BusinessException 이메일 중복(409)
     */
    @Transactional
    public void updateProfile(Long userId, String name, String email) {
        log.info("[UserService] 프로필 수정 - userId: {}", userId);

        User existing = userMapper.selectUserByEmail(email);
        if (existing != null && !existing.getId().equals(userId)) {
            throw new BusinessException(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.");
        }

        userMapper.updateUserInfo(User.ofProfileUpdate(userId, name, email));
    }

    /**
     * 비밀번호 변경
     * @param userId 사용자 ID
     * @param currentPassword 현재 비밀번호
     * @param newPassword 새 비밀번호
     * @throws BusinessException 현재 비밀번호 불일치(401)
     */
    @Transactional
    public void updatePassword(Long userId, String currentPassword, String newPassword) {
        log.info("[UserService] 비밀번호 변경 - userId: {}", userId);

        String stored = userMapper.selectPasswordById(userId);
        if (!passwordEncoder.matches(currentPassword, stored)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "현재 비밀번호가 일치하지 않습니다.");
        }

        userMapper.updateUserPassword(userId, passwordEncoder.encode(newPassword));
    }
}
