package com.yujin.course_enrollment.mapper;

import com.yujin.course_enrollment.entity.CreatorRequest;
import org.apache.ibatis.annotations.Mapper;

/**
 * 강사 신청 Mapper 인터페이스
 * CreatorRequestMapper.xml과 매핑되어 강사 신청 관련 DB 접근을 담당
 */
@Mapper
public interface CreatorRequestMapper {

    /* 강사 신청 등록 */
    void insertCreatorRequest(CreatorRequest creatorRequest);

    /* 사용자의 PENDING 상태 신청 조회 */
    CreatorRequest selectPendingRequestByUserId(Long userId);
}
