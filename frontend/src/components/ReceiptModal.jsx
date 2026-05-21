/* 결제 상태에 따른 배지 스타일 */
const PAYMENT_STATUS_BADGE_STYLE = {
    DONE: 'bg-green-50 text-green-700 border-green-200',
    CANCELLED: 'bg-gray-50 text-gray-500 border-gray-200',
};
/* 결제 상태 라벨 */
const PAYMENT_STATUS_LABEL = {
    DONE: '결제완료',
    CANCELLED: '환불완료',
};

/* 결제 영수증 모달 */
const ReceiptModal = ({ payment, onClose, canCancel = false, onCancel }) => {
    if (!payment) return null;

    /* 영수증 출력 핸들러 */
    const handlePrint = () => {
        const cancelSection = payment.status === 'CANCELLED' ? `
            <hr style="margin:12px 0;border:none;border-top:1px solid #e5e7eb"/>
            <div class="row"><span class="label">환불일</span><span class="value">${payment.canceledAt?.substring(0, 10) || '-'}</span></div>
            ${payment.cancelReason ? `<div class="row"><span class="label">취소 사유</span><span class="value">${payment.cancelReason}</span></div>` : ''}
        ` : '';

        const win = window.open('', '_blank');
        win.document.write(`
            <html><head><title>영수증</title><style>
                body { font-family: sans-serif; padding: 24px; max-width: 360px; }
                h1 { font-size: 18px; font-weight: bold; margin-bottom: 16px; }
                .row { display: flex; justify-content: space-between; margin: 8px 0; font-size: 14px; }
                .label { color: #6b7280; }
                .value { font-weight: 600; text-align: right; }
                .badge { font-size: 11px; font-weight: bold; padding: 2px 8px; border-radius: 4px; border: 1px solid; }
            </style></head><body>
                <h1>영수증</h1>
                <div class="row"><span class="label">주문명</span><span class="value">${payment.orderName}</span></div>
                <div class="row"><span class="label">주문번호</span><span class="value">${payment.orderId}</span></div>
                <div class="row"><span class="label">결제 금액</span><span class="value">${payment.amount.toLocaleString()}원</span></div>
                <div class="row"><span class="label">결제 수단</span><span class="value">${payment.method || '-'}</span></div>
                <div class="row"><span class="label">결제일</span><span class="value">${payment.paidAt?.substring(0, 10) || '-'}</span></div>
                ${cancelSection}
            </body></html>
        `);
        win.document.close();
        win.print();
    };

    /* 영수증 행 데이터 */
    const rows = [
        { label: '주문명', value: payment.orderName },
        { label: '주문번호', value: payment.orderId },
        { label: '결제 금액', value: `${payment.amount.toLocaleString()}원` },
        { label: '결제 수단', value: payment.method || '-' },
        { label: '결제일', value: payment.paidAt?.substring(0, 10) || '-' },
    ];

    return (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
            <div className="bg-white rounded-xl shadow-xl p-6 w-full max-w-sm mx-4">
                <div className="flex items-center justify-between mb-5">
                    <h2 className="text-lg font-bold text-gray-900">영수증</h2>
                    <span className={`text-xs font-bold px-2 py-0.5 rounded border ${PAYMENT_STATUS_BADGE_STYLE[payment.status]}`}>
                        {PAYMENT_STATUS_LABEL[payment.status]}
                    </span>
                </div>
                {/* 영수증 정보 행 */}
                <dl className="flex flex-col gap-3">
                    {rows.map(({ label, value }) => (
                        <div key={label} className="flex justify-between text-sm">
                            <dt className="text-gray-500">{label}</dt>
                            <dd className="font-medium text-gray-900 text-right max-w-[60%] break-all">{value}</dd>
                        </div>
                    ))}
                </dl>

                {/* 환불 정보 행 (취소된 결제에 한함) */}
                {payment.status === 'CANCELLED' && (
                    <>
                        <div className="border-t border-gray-100 my-4" />
                        <dl className="flex flex-col gap-3">
                            <div className="flex justify-between text-sm">
                                <dt className="text-gray-500">환불일</dt>
                                <dd className="font-medium text-gray-900">{payment.canceledAt?.substring(0, 10) || '-'}</dd>
                            </div>
                            {payment.cancelReason && (
                                <div className="flex justify-between text-sm">
                                    <dt className="text-gray-500">취소 사유</dt>
                                    <dd className="font-medium text-gray-900 text-right max-w-[60%]">{payment.cancelReason}</dd>
                                </div>
                            )}
                        </dl>
                    </>
                )}

                {/* 액션 버튼 그룹 (취소 가능한 경우에만 취소 버튼 표시) */}
                <div className="flex gap-2 mt-6">
                    {canCancel && (
                        <button
                            onClick={onCancel}
                            className="flex-1 px-4 py-2 border border-red-300 text-red-500 text-sm font-semibold rounded-md hover:bg-red-50 transition-colors cursor-pointer"
                        >
                            결제 취소
                        </button>
                    )}
                    <button
                        onClick={handlePrint}
                        className="flex-1 px-4 py-2 border border-gray-300 text-gray-600 text-sm font-semibold rounded-md hover:bg-gray-50 transition-colors cursor-pointer"
                    >
                        영수증 출력
                    </button>
                    <button
                        onClick={onClose}
                        className="flex-1 px-4 py-2 bg-gray-100 text-gray-700 text-sm font-semibold rounded-md hover:bg-gray-200 transition-colors cursor-pointer"
                    >
                        닫기
                    </button>
                </div>
            </div>
        </div>
    );
};

export default ReceiptModal;
