package com.yujin.course_enrollment.mapper;

import com.yujin.course_enrollment.dto.req.ReqAdminPaymentPageDto;
import com.yujin.course_enrollment.dto.req.ReqMyPaymentPageDto;
import com.yujin.course_enrollment.dto.resp.RespAdminPaymentDto;
import com.yujin.course_enrollment.entity.Payment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

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

    /* 수강 신청 ID로 DONE 상태 결제 조회 */
    Payment selectPaymentByEnrollmentId(Long enrollmentId);

    /* 환불 실패 마킹 (REFUND_FAILED 업데이트) */
    void updatePaymentRefundFailed(Long enrollmentId);

    /* 결제 취소 상태 업데이트 */
    void updatePaymentCancelled(Payment payment);

    /* 사용자 결제 내역 조회 */
    List<Payment> selectPaymentListByUserId(ReqMyPaymentPageDto reqMyPaymentPageDto);

    /* 사용자 결제 내역 전체 수 조회 (페이징용) */
    int selectPaymentListByUserIdCount(Long userId);

    /* 관리자 전체 결제 내역 조회 */
    List<RespAdminPaymentDto> selectAdminPaymentList(ReqAdminPaymentPageDto reqAdminPaymentPageDto);

    /* 관리자 전체 결제 내역 수 조회 (페이징용) */
    int selectAdminPaymentListCount(ReqAdminPaymentPageDto reqAdminPaymentPageDto);

    /* 전체 결제 금액 (DONE) */
    Long selectTotalRevenue();

    /* 전체 환불 금액 (CANCELLED) */
    Long selectTotalRefund();

    /* 이번 달 매출 (DONE, paid_at 기준) */
    Long selectMonthlyRevenue();
}
