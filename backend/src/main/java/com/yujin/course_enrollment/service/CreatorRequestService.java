package com.yujin.course_enrollment.service;

import com.yujin.course_enrollment.dto.req.ReqCreatorRequestDto;
import com.yujin.course_enrollment.dto.resp.RespCreatorRequestDto;
import com.yujin.course_enrollment.entity.CreatorRequest;
import com.yujin.course_enrollment.entity.User;
import com.yujin.course_enrollment.global.CreatorRequestStatus;
import com.yujin.course_enrollment.global.exception.BusinessException;
import com.yujin.course_enrollment.mapper.CreatorRequestMapper;
import com.yujin.course_enrollment.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    /**
     * 강사 신청 목록 조회 (관리자)
     * @return 전체 강사 신청 목록
     */
    public List<RespCreatorRequestDto> findCreatorRequestList() {
        log.info("[CreatorRequestService] 강사 신청 목록 조회");

        return creatorRequestMapper.selectCreatorRequestList();
    }

    /**
     * 강사 신청 승인
     * @param requestId 신청 ID
     * @throws BusinessException 신청 없음(404), 이미 처리된 신청(409)
     */
    @Transactional
    public void approveCreatorRequest(Long requestId) {
        log.info("[CreatorRequestService] 강사 신청 승인 - requestId: {}", requestId);

        RespCreatorRequestDto request = creatorRequestMapper.selectCreatorRequestById(requestId);
        if (request == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "강사 신청을 찾을 수 없습니다.");
        }

        if (!CreatorRequestStatus.PENDING.equals(request.getStatus())) {
            throw new BusinessException(HttpStatus.CONFLICT, "처리 완료된 신청입니다.");
        }

        creatorRequestMapper.updateCreatorRequestStatus(CreatorRequest.ofApprove(requestId));
        userMapper.updateUserRole(request.getUserId(), "CREATOR");
    }

    /**
     * 강사 신청 거절
     * @param requestId 신청 ID
     * @param rejectReason 거절 사유
     * @throws BusinessException 신청 없음(404), 이미 처리된 신청(409)
     */
    @Transactional
    public void rejectCreatorRequest(Long requestId, String rejectReason) {
        log.info("[CreatorRequestService] 강사 신청 거절 - requestId: {}", requestId);

        RespCreatorRequestDto request = creatorRequestMapper.selectCreatorRequestById(requestId);
        if (request == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "강사 신청을 찾을 수 없습니다.");
        }

        if (!CreatorRequestStatus.PENDING.equals(request.getStatus())) {
            throw new BusinessException(HttpStatus.CONFLICT, "처리 완료된 신청입니다.");
        }

        creatorRequestMapper.updateCreatorRequestStatus(CreatorRequest.ofReject(requestId, rejectReason));
    }
}
