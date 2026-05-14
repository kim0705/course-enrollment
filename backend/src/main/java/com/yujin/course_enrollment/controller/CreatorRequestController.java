package com.yujin.course_enrollment.controller;

import com.yujin.course_enrollment.dto.req.ReqCreatorRequestDto;
import com.yujin.course_enrollment.global.response.ApiResponse;
import com.yujin.course_enrollment.service.CreatorRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 강사 신청 컨트롤러
 * 강사 신청 및 관리자 승인/거절 HTTP 요청을 처리
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CreatorRequestController {

    private final CreatorRequestService creatorRequestService;

    /**
     * 강사 신청
     * POST /api/creator-requests
     * @param userId 신청자 ID (헤더로 전달)
     * @param reqCreatorRequestDto 신청 정보 (reason)
     * @return 201 Created
     */
    @PostMapping("/creator-requests")
    public ResponseEntity<ApiResponse<Void>> requestCreator(@RequestHeader("X-User-Id") Long userId, @Valid @RequestBody ReqCreatorRequestDto reqCreatorRequestDto) {
        log.debug("[CreatorRequestController] 강사 신청 요청 - userId: {}", userId);

        creatorRequestService.requestCreator(userId, reqCreatorRequestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created());
    }
}
