package com.yujin.course_enrollment.service;

import com.yujin.course_enrollment.dto.req.ReqCreatorRequestDto;
import com.yujin.course_enrollment.entity.CreatorRequest;
import com.yujin.course_enrollment.entity.User;
import com.yujin.course_enrollment.global.exception.BusinessException;
import com.yujin.course_enrollment.mapper.CreatorRequestMapper;
import com.yujin.course_enrollment.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 강사 신청 서비스
 * 강사 신청 관련 비즈니스 로직을 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CreatorRequestService {

    private final CreatorRequestMapper creatorRequestMapper;
    private final UserMapper userMapper;

    /**
     * 강사 신청
     * @param userId 신청자 ID
     * @param reqCreatorRequestDto 신청 정보 (reason)
     * @throws BusinessException 사용자 없음(404), 이미 강사(409), 신청 중복(409)
     */
    @Transactional
    public void requestCreator(Long userId, ReqCreatorRequestDto reqCreatorRequestDto) {
        log.info("[CreatorRequestService] 강사 신청 - userId: {}", userId);

        User user = userMapper.selectUserById(userId);
        if (user == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.");
        }

        if ("CREATOR".equals(user.getRole())) {
            throw new BusinessException(HttpStatus.CONFLICT, "이미 강사 계정입니다.");
        }

        if (creatorRequestMapper.selectPendingRequestByUserId(userId) != null) {
            throw new BusinessException(HttpStatus.CONFLICT, "이미 강사 신청 중입니다.");
        }

        creatorRequestMapper.insertCreatorRequest(CreatorRequest.ofCreate(userId, reqCreatorRequestDto.getReason()));
    }
}
