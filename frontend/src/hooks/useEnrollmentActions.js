import { useState } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { loadTossPayments } from '@tosspayments/tosspayments-sdk';
import { cancelEnrollment, confirmEnrollment } from '../api/enrollment';
import { useAuth } from '../context/AuthContext';

/* 수강목록 탭 액션 훅 (결제/취소 로직) */
export const useEnrollmentActions = () => {
    /* 인증 정보 */
    const { user } = useAuth();
    /* React Query 클라이언트 */
    const queryClient = useQueryClient();

    /* 수강목록 페이지네이션 상태 */
    const [enrollmentPage, setEnrollmentPage] = useState(0);
    /* 결제 로딩 상태 */
    const [isPaymentLoading, setIsPaymentLoading] = useState(false);
    /* CONFIRMED 취소 모달 상태 */
    const [cancelModal, setCancelModal] = useState({ open: false, enrollmentId: null });
    /* 취소 사유 상태 */
    const [selectedReason, setSelectedReason] = useState('');
    /* 기타 사유 입력 상태 */
    const [customReason, setCustomReason] = useState('');

    /* 유료 강의 결제 (토스페이먼츠) */
    const handlePayment = async (enrollment) => {
        setIsPaymentLoading(true);
        
        try {
            const tossPayments = await loadTossPayments(import.meta.env.VITE_TOSS_CLIENT_KEY);
            const payment = tossPayments.payment({ customerKey: `CUSTOMER-${user.id}` });

            await payment.requestPayment({
                method: 'CARD',
                amount: { currency: 'KRW', value: enrollment.price },
                orderId: `ORDER-${enrollment.id}-${Date.now()}`,
                orderName: enrollment.courseTitle,
                successUrl: `${window.location.origin}/payment/success?enrollmentId=${enrollment.id}&orderName=${encodeURIComponent(enrollment.courseTitle)}`,
                failUrl: `${window.location.origin}/payment/fail`,
                card: {
                    useEscrow: false,
                    flowMode: 'DEFAULT',
                    useCardPoint: false,
                    useAppCardOnly: false,
                },
            });
        } catch (err) {
            alert(err.message || '결제 요청에 실패했습니다.');
            setIsPaymentLoading(false);
        }
    };

    /* 무료 강의 수강 확정 */
    const handleFreeConfirm = async (enrollmentId) => {
        if (!window.confirm('수강을 확정하시겠습니까?')) return;

        try {
            await confirmEnrollment(enrollmentId);
            alert('수강이 확정되었습니다.');
            queryClient.invalidateQueries({ queryKey: ['myEnrollments', enrollmentPage] });
        } catch (err) {
            alert(err.response?.data?.message || '수강 확정에 실패했습니다.');
        }
    };

    /* PENDING/WAITLIST 취소 (사유 불필요) */
    const handleCancel = async (enrollmentId) => {
        if (!window.confirm('수강을 취소하시겠습니까?')) return;

        try {
            await cancelEnrollment(enrollmentId);
            alert('수강이 취소되었습니다.');
            queryClient.invalidateQueries({ queryKey: ['myEnrollments', enrollmentPage] });
        } catch (err) {
            alert(err.response?.data?.message || '수강 취소에 실패했습니다.');
        }
    };

    /* CONFIRMED 취소 모달 열기 */
    const handleCancelClick = (enrollmentId) => {
        setSelectedReason('');
        setCustomReason('');
        setCancelModal({ open: true, enrollmentId });
    };

    /* CONFIRMED 취소 모달 확인 */
    const handleCancelConfirm = async () => {
        const reason = selectedReason === '기타' ? customReason.trim() : selectedReason;

        if (!reason) {
            alert('취소 사유를 선택하거나 입력해주세요.');
            return;
        }

        try {
            await cancelEnrollment(cancelModal.enrollmentId, reason);
            alert('수강이 취소되었습니다.');
            setCancelModal({ open: false, enrollmentId: null });
            queryClient.invalidateQueries({ queryKey: ['myEnrollments', enrollmentPage] });
            queryClient.invalidateQueries({ queryKey: ['myPayments'] });
        } catch (err) {
            alert(err.response?.data?.message || '수강 취소에 실패했습니다.');
        }
    };

    return {
        enrollmentPage,
        setEnrollmentPage,
        isPaymentLoading,
        cancelModal,
        setCancelModal,
        selectedReason,
        setSelectedReason,
        customReason,
        setCustomReason,
        handlePayment,
        handleFreeConfirm,
        handleCancel,
        handleCancelClick,
        handleCancelConfirm,
    };
};
