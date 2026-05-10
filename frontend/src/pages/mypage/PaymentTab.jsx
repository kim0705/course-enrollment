import { useState } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { cancelEnrollment } from '../../api/enrollment';
import { useMyPayments } from '../../hooks/useMyPayments';
import CancelModal from '../../components/CancelModal';
import ReceiptModal from '../../components/ReceiptModal';

/* 결제 상태 배지 스타일 */
const PAYMENT_STATUS_BADGE_STYLE = {
    DONE: 'bg-green-50 text-green-700 border-green-200',
    CANCELLED: 'bg-gray-50 text-gray-500 border-gray-200',
};
/* 결제 상태 라벨 */
const PAYMENT_STATUS_LABEL = {
    DONE: '결제완료',
    CANCELLED: '환불완료',
};

/* 결제 취소 가능 여부 (결제일로부터 7일 이내) */
const isWithin7Days = (paidAt) =>
    paidAt && new Date(paidAt) > new Date(Date.now() - 7 * 24 * 60 * 60 * 1000);

/* 결제 내역 탭 */
const PaymentTab = () => {
    /* 결제 데이터 */
    const { data: myPayments = [] } = useMyPayments();
    /* 결제 영수증 모달 상태 */
    const [receiptPayment, setReceiptPayment] = useState(null);
    /* CONFIRMED 취소 모달 상태 */
    const [cancelModal, setCancelModal] = useState({ open: false, enrollmentId: null });
    /* 취소 사유 상태 */
    const [selectedReason, setSelectedReason] = useState('');
    /* 기타 사유 입력 상태 */
    const [customReason, setCustomReason] = useState('');
    /* React Query 클라이언트 */
    const queryClient = useQueryClient();

    /* 결제 취소 클릭 핸들러 */
    const handleCancelClick = (e, payment) => {
        e.stopPropagation();

        if (!window.confirm('결제를 취소하시겠습니까?')) return;

        setSelectedReason('');
        setCustomReason('');
        setCancelModal({ open: true, enrollmentId: payment.enrollmentId });
    };

    /* 영수증 모달에서 결제 취소 핸들러 */
    const handleCancelFromReceipt = () => {
        if (!window.confirm('결제를 취소하시겠습니까?')) return;
        
        setSelectedReason('');
        setCustomReason('');
        setCancelModal({ open: true, enrollmentId: receiptPayment.enrollmentId });
    };

    /* CONFIRMED 취소 모달 확인 핸들러 */
    const handleCancelConfirm = async () => {
        const reason = selectedReason === '기타' ? customReason.trim() : selectedReason;

        if (!reason) {
            alert('취소 사유를 선택하거나 입력해주세요.');
            return;
        }

        try {
            await cancelEnrollment(cancelModal.enrollmentId, reason);
            alert('결제가 취소되었습니다.');
            setCancelModal({ open: false, enrollmentId: null });
            setReceiptPayment(null);

            queryClient.invalidateQueries({ queryKey: ['myPayments'] });
            queryClient.invalidateQueries({ queryKey: ['myEnrollments'] });
        } catch (err) {
            alert(err.response?.data?.message || '결제 취소에 실패했습니다.');
        }
    };

    /* 결제 내역이 없는 경우 */
    if (myPayments.length === 0) {
        return (
            <div className="text-center text-gray-400 py-32 border-2 border-dashed border-gray-100 rounded-2xl">
                결제 내역이 없습니다.
            </div>
        );
    }

    return (
        <>
            <div className="flex flex-col gap-4">
                {myPayments.map(payment => (
                    <div key={payment.id}
                        onClick={() => setReceiptPayment(payment)}
                        className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm cursor-pointer hover:shadow-md transition-shadow">
                        <div className="flex items-start justify-between gap-4">
                            {/* 결제 정보 */}
                            <div className="flex-1 min-w-0">
                                <div className="flex items-center gap-2 mb-1">
                                    <span className="text-base font-bold text-gray-900 truncate">{payment.orderName}</span>
                                    <span className={`text-xs font-bold px-2 py-0.5 rounded border shrink-0 ${PAYMENT_STATUS_BADGE_STYLE[payment.status]}`}>
                                        {PAYMENT_STATUS_LABEL[payment.status]}
                                    </span>
                                </div>
                                <div className="flex flex-wrap items-center gap-3 text-xs text-gray-500 mt-1">
                                    <span className="font-semibold text-gray-700">{payment.amount.toLocaleString()}원</span>
                                    {payment.method && (
                                        <>
                                            <span className="text-gray-300">|</span>
                                            <span>{payment.method}</span>
                                        </>
                                    )}
                                    <span className="text-gray-300">|</span>
                                    <span>결제일 {payment.paidAt?.substring(0, 10)}</span>
                                </div>
                                {payment.status === 'CANCELLED' && (
                                    <p className="text-xs text-gray-400 mt-1">
                                        환불일 {payment.canceledAt?.substring(0, 10)}
                                        {payment.cancelReason && <span className="ml-2">· {payment.cancelReason}</span>}
                                    </p>
                                )}
                            </div>

                            {/* CONFIRMED 결제이면서 결제일로부터 7일 이내인 경우에만 취소 버튼 표시 */}
                            {payment.status === 'DONE' && isWithin7Days(payment.paidAt) && (
                                <button
                                    onClick={(e) => handleCancelClick(e, payment)}
                                    className="shrink-0 px-4 py-2 border border-red-200 text-red-400 text-sm font-semibold rounded-md hover:bg-red-50 transition-colors cursor-pointer"
                                >
                                    결제 취소
                                </button>
                            )}
                        </div>
                    </div>
                ))}
            </div>

            {/* 영수증 모달 */}
            <ReceiptModal
                payment={receiptPayment}
                onClose={() => setReceiptPayment(null)}
                canCancel={receiptPayment?.status === 'DONE' && isWithin7Days(receiptPayment?.paidAt)}
                onCancel={handleCancelFromReceipt}
            />

            {/* CONFIRMED 취소 모달 */}
            <CancelModal
                open={cancelModal.open}
                onClose={() => setCancelModal({ open: false, enrollmentId: null })}
                onConfirm={handleCancelConfirm}
                selectedReason={selectedReason}
                setSelectedReason={setSelectedReason}
                customReason={customReason}
                setCustomReason={setCustomReason}
            />
        </>
    );
};

export default PaymentTab;
