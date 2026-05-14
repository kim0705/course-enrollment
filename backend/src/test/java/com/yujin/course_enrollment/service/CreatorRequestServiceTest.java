package com.yujin.course_enrollment.service;

import com.yujin.course_enrollment.dto.req.ReqCreatorRequestDto;
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
}
