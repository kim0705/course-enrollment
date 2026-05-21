package com.yujin.course_enrollment.controller;

import com.yujin.course_enrollment.dto.req.ReqCreatorRequestDto;
import com.yujin.course_enrollment.dto.req.ReqCreatorRequestRejectDto;
import com.yujin.course_enrollment.dto.resp.RespCreatorRequestDto;
import com.yujin.course_enrollment.global.response.ApiResponse;
import com.yujin.course_enrollment.service.CreatorRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
    public ResponseEntity<ApiResponse<Void>> requestCreator(@AuthenticationPrincipal Long userId, @Valid @RequestBody ReqCreatorRequestDto reqCreatorRequestDto) {
        log.debug("[CreatorRequestController] 강사 신청 요청 - userId: {}", userId);

        creatorRequestService.requestCreator(userId, reqCreatorRequestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created());
    }

    /**
     * 강사 신청 목록 조회 (관리자)
     * GET /api/admin/creator-requests
     * @return 전체 강사 신청 목록
     */
    @GetMapping("/admin/creator-requests")
    public ResponseEntity<ApiResponse<List<RespCreatorRequestDto>>> getCreatorRequestList() {
        log.debug("[CreatorRequestController] 강사 신청 목록 조회");

        return ResponseEntity.ok(ApiResponse.success(creatorRequestService.findCreatorRequestList()));
    }

    /**
     * 강사 신청 승인 (관리자)
     * PATCH /api/admin/creator-requests/{id}/approve
     * @param id 신청 ID
     * @return 200 OK
     */
    @PatchMapping("/admin/creator-requests/{id}/approve")
    public ResponseEntity<ApiResponse<Void>> approveCreatorRequest(@PathVariable Long id) {
        log.debug("[CreatorRequestController] 강사 신청 승인 - requestId: {}", id);

        creatorRequestService.approveCreatorRequest(id);

        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 강사 신청 거절 (관리자)
     * PATCH /api/admin/creator-requests/{id}/reject
     * @param id 신청 ID
     * @param reqCreatorRequestRejectDto 거절 사유
     * @return 200 OK
     */
    @PatchMapping("/admin/creator-requests/{id}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectCreatorRequest(@PathVariable Long id, @Valid @RequestBody ReqCreatorRequestRejectDto reqCreatorRequestRejectDto) {
        log.debug("[CreatorRequestController] 강사 신청 거절 - requestId: {}", id);

        creatorRequestService.rejectCreatorRequest(id, reqCreatorRequestRejectDto.getRejectReason());

        return ResponseEntity.ok(ApiResponse.success());
    }
}
