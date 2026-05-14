package com.yujin.course_enrollment.service;

import com.yujin.course_enrollment.dto.req.ReqCreatorRequestDto;
import com.yujin.course_enrollment.dto.resp.RespCreatorRequestDto;
import com.yujin.course_enrollment.entity.CreatorRequest;
import com.yujin.course_enrollment.entity.User;
import com.yujin.course_enrollment.global.exception.BusinessException;
import com.yujin.course_enrollment.mapper.CreatorRequestMapper;
import com.yujin.course_enrollment.mapper.UserMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;

/**
 * 강사 신청 서비스 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class CreatorRequestServiceTest {

    @InjectMocks
    private CreatorRequestService creatorRequestService;

    @Mock
    private CreatorRequestMapper creatorRequestMapper;

    @Mock
    private UserMapper userMapper;

    @Test
    @DisplayName("강사 신청 성공")
    void requestCreator_success() {
        // given
        Long userId = 4L;
        User student = User.builder()
                .id(userId)
                .name("수강생A")
                .role("STUDENT")
                .build();
        ReqCreatorRequestDto dto = new ReqCreatorRequestDto("강의를 만들고 싶습니다.");

        given(userMapper.selectUserById(userId)).willReturn(student);
        given(creatorRequestMapper.selectPendingRequestByUserId(userId)).willReturn(null);
        willDoNothing().given(creatorRequestMapper).insertCreatorRequest(any());

        // when
        creatorRequestService.requestCreator(userId, dto);

        // then
        then(creatorRequestMapper).should().insertCreatorRequest(any());
    }

    @Test
    @DisplayName("존재하지 않는 사용자 - 강사 신청 실패")
    void requestCreator_userNotFound() {
        // given
        Long userId = 999L;
        ReqCreatorRequestDto dto = new ReqCreatorRequestDto("강의를 만들고 싶습니다.");

        given(userMapper.selectUserById(userId)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> creatorRequestService.requestCreator(userId, dto))
                .isInstanceOf(BusinessException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");
        then(creatorRequestMapper).should(never()).insertCreatorRequest(any());
    }

    @Test
    @DisplayName("이미 강사 계정 - 강사 신청 실패")
    void requestCreator_alreadyCreator() {
        // given
        Long userId = 1L;
        User creator = User.builder()
                .id(userId)
                .name("강사A")
                .role("CREATOR")
                .build();
        ReqCreatorRequestDto dto = new ReqCreatorRequestDto("더 많은 강의를 만들고 싶습니다.");

        given(userMapper.selectUserById(userId)).willReturn(creator);

        // when & then
        assertThatThrownBy(() -> creatorRequestService.requestCreator(userId, dto))
                .isInstanceOf(BusinessException.class)
                .hasMessage("이미 강사 계정입니다.");
        then(creatorRequestMapper).should(never()).insertCreatorRequest(any());
    }

    @Test
    @DisplayName("이미 신청 중 - 강사 신청 실패")
    void requestCreator_alreadyPending() {
        // given
        Long userId = 4L;
        User student = User.builder()
                .id(userId)
                .name("수강생A")
                .role("STUDENT")
                .build();
        CreatorRequest pending = CreatorRequest.builder()
                .id(1L)
                .userId(userId)
                .status("PENDING")
                .reason("강의를 만들고 싶습니다.")
                .build();
        ReqCreatorRequestDto dto = new ReqCreatorRequestDto("다시 신청합니다.");

        given(userMapper.selectUserById(userId)).willReturn(student);
        given(creatorRequestMapper.selectPendingRequestByUserId(userId)).willReturn(pending);

        // when & then
        assertThatThrownBy(() -> creatorRequestService.requestCreator(userId, dto))
                .isInstanceOf(BusinessException.class)
                .hasMessage("이미 강사 신청 중입니다.");
        then(creatorRequestMapper).should(never()).insertCreatorRequest(any());
    }

    @Test
    @DisplayName("강사 신청 승인 성공")
    void approveCreatorRequest_success() {
        // given
        Long requestId = 1L;
        Long userId = 4L;
        RespCreatorRequestDto request = new RespCreatorRequestDto(requestId, userId, "student_a", "수강생A", "PENDING", "강의를 만들고 싶습니다.", null, null, null);

        given(creatorRequestMapper.selectCreatorRequestById(requestId)).willReturn(request);
        willDoNothing().given(creatorRequestMapper).updateCreatorRequestStatus(any());
        willDoNothing().given(userMapper).updateUserRole(userId, "CREATOR");

        // when
        creatorRequestService.approveCreatorRequest(requestId);

        // then
        then(creatorRequestMapper).should().updateCreatorRequestStatus(any());
        then(userMapper).should().updateUserRole(userId, "CREATOR");
    }

    @Test
    @DisplayName("존재하지 않는 신청 - 승인 실패")
    void approveCreatorRequest_notFound() {
        // given
        Long requestId = 999L;
        given(creatorRequestMapper.selectCreatorRequestById(requestId)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> creatorRequestService.approveCreatorRequest(requestId))
                .isInstanceOf(BusinessException.class)
                .hasMessage("강사 신청을 찾을 수 없습니다.");
        then(userMapper).should(never()).updateUserRole(any(), any());
    }

    @Test
    @DisplayName("이미 처리된 신청 - 승인 실패")
    void approveCreatorRequest_alreadyProcessed() {
        // given
        Long requestId = 1L;
        RespCreatorRequestDto request = new RespCreatorRequestDto(requestId, 4L, "student_a", "수강생A", "APPROVED", "강의를 만들고 싶습니다.", null, null, null);

        given(creatorRequestMapper.selectCreatorRequestById(requestId)).willReturn(request);

        // when & then
        assertThatThrownBy(() -> creatorRequestService.approveCreatorRequest(requestId))
                .isInstanceOf(BusinessException.class)
                .hasMessage("처리 완료된 신청입니다.");
        then(userMapper).should(never()).updateUserRole(any(), any());
    }

    @Test
    @DisplayName("강사 신청 거절 성공")
    void rejectCreatorRequest_success() {
        // given
        Long requestId = 1L;
        String rejectReason = "경력 정보가 부족합니다.";
        RespCreatorRequestDto request = new RespCreatorRequestDto(requestId, 4L, "student_a", "수강생A", "PENDING", "강의를 만들고 싶습니다.", null, null, null);

        given(creatorRequestMapper.selectCreatorRequestById(requestId)).willReturn(request);
        willDoNothing().given(creatorRequestMapper).updateCreatorRequestStatus(any());

        // when
        creatorRequestService.rejectCreatorRequest(requestId, rejectReason);

        // then
        then(creatorRequestMapper).should().updateCreatorRequestStatus(any());
        then(userMapper).should(never()).updateUserRole(any(), any());
    }

    @Test
    @DisplayName("존재하지 않는 신청 - 거절 실패")
    void rejectCreatorRequest_notFound() {
        // given
        Long requestId = 999L;
        given(creatorRequestMapper.selectCreatorRequestById(requestId)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> creatorRequestService.rejectCreatorRequest(requestId, "사유"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("강사 신청을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("이미 처리된 신청 - 거절 실패")
    void rejectCreatorRequest_alreadyProcessed() {
        // given
        Long requestId = 1L;
        RespCreatorRequestDto request = new RespCreatorRequestDto(requestId, 4L, "student_a", "수강생A", "REJECTED", "강의를 만들고 싶습니다.", "경력 정보가 부족합니다.", null, null);

        given(creatorRequestMapper.selectCreatorRequestById(requestId)).willReturn(request);

        // when & then
        assertThatThrownBy(() -> creatorRequestService.rejectCreatorRequest(requestId, "다른 사유"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("처리 완료된 신청입니다.");
    }
}
