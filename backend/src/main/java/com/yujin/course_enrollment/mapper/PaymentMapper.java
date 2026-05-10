package com.yujin.course_enrollment.mapper;

import com.yujin.course_enrollment.entity.Payment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 결제 Mapper 인터페이스
 * PaymentMapper.xml과 매핑되어 결제 관련 DB 접근을 담당
 */
@Mapper
public interface PaymentMapper {
    /* 결제 저장 (PENDING 상태로 선저장) */
    void insertPayment(Payment payment);

    /* 결제 승인 완료 (DONE 상태로 업데이트) */
    void updatePaymentDone(Payment payment);

    /* 결제 승인 실패 (FAILED 상태로 업데이트) */
    void updatePaymentFailed(Long id);

    /* 주문 ID로 결제 조회 */
    Payment selectPaymentByOrderId(String orderId);
}
