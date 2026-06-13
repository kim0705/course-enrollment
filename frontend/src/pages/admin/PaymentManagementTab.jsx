import { useEffect, useState } from 'react';
import { getAdminPayments } from '../../api/admin';

/* 결제 상태 배지 스타일 */
const PAYMENT_STATUS_BADGE_STYLE = {
    DONE: 'bg-green-50 text-green-700 border-green-200',
    CANCELLED: 'bg-gray-50 text-gray-500 border-gray-200',
};
/* 결제 상태 레이블 */
const PAYMENT_STATUS_LABEL = {
    DONE: '결제완료',
    CANCELLED: '환불완료',
};

/* 상태 필터 목록 */
const STATUS_FILTERS = [
    { key: '', label: '전체' },
    { key: 'DONE', label: '결제완료' },
    { key: 'CANCELLED', label: '환불완료' },
];

/* 관리자 결제 관리 탭 */
const PaymentManagementTab = () => {
    /* 결제 목록 상태 */
    const [payments, setPayments] = useState([]);
    /* 페이징 상태 */
    const [page, setPage] = useState(0);
    /* 전체 페이지 수 상태 */
    const [totalPages, setTotalPages] = useState(0);
    /* 마지막 페이지 여부 상태 */
    const [isLast, setIsLast] = useState(false);
    /* 상태 필터 */
    const [status, setStatus] = useState('');
    /* 로딩 상태 */
    const [isLoading, setIsLoading] = useState(true);

    /* 결제 목록 조회 */
    const fetchPayments = (p, s) => {
        setIsLoading(true);

        getAdminPayments(p, 10, s)
            .then(res => {
                setPayments(res.data.content);
                setTotalPages(res.data.totalPages);
                setIsLast(res.data.last);
            })
            .catch(() => alert('결제 내역 조회에 실패했습니다.'))
            .finally(() => setIsLoading(false));
    };

    /* 페이지 또는 상태 필터 변경 시 재조회 */
    useEffect(() => {
        fetchPayments(page, status);
    }, [page, status]);

    /* 상태 필터 변경 시 페이지 초기화 */
    const handleStatusChange = (newStatus) => {
        setStatus(newStatus);
        setPage(0);
    };

    return (
        <>
            {/* 상태 필터 */}
            <div className="flex gap-2 mb-6">
                {STATUS_FILTERS.map(filter => (
                    <button
                        key={filter.key}
                        onClick={() => handleStatusChange(filter.key)}
                        className={`px-4 py-1.5 text-sm font-semibold rounded-full border transition-colors cursor-pointer ${
                            status === filter.key
                                ? 'bg-blue-600 text-white border-blue-600'
                                : 'text-gray-500 border-gray-300 hover:border-gray-400'
                        }`}
                    >
                        {filter.label}
                    </button>
                ))}
            </div>

            {isLoading ? (
                <div className="text-center py-20 text-gray-400">로딩 중...</div>
            ) : payments.length === 0 ? (
                <div className="text-center text-gray-400 py-32 border-2 border-dashed border-gray-100 rounded-2xl">
                    결제 내역이 없습니다.
                </div>
            ) : (
                <>
                    <div className="overflow-x-auto">
                        <table className="w-full text-sm text-left">
                            {/* 테이블 헤더 */}
                            <thead>
                                <tr className="border-b border-gray-200 text-gray-500 text-xs uppercase">
                                    <th className="pb-3 pr-6 font-semibold">사용자명</th>
                                    <th className="pb-3 pr-6 font-semibold">강의명</th>
                                    <th className="pb-3 pr-6 font-semibold">금액</th>
                                    <th className="pb-3 pr-6 font-semibold">상태</th>
                                    <th className="pb-3 pr-6 font-semibold">결제일</th>
                                    <th className="pb-3 font-semibold">환불일</th>
                                </tr>
                            </thead>
                            {/* 테이블 바디 */}
                            <tbody className="divide-y divide-gray-100">
                                {payments.map(payment => (
                                    <tr key={payment.id} className="hover:bg-gray-50">
                                        <td className="py-4 pr-6 font-medium text-gray-900">{payment.userName}</td>
                                        <td className="py-4 pr-6 text-gray-700 max-w-xs truncate">{payment.courseName}</td>
                                        <td className="py-4 pr-6 font-semibold text-gray-900">{payment.amount.toLocaleString()}원</td>
                                        <td className="py-4 pr-6">
                                            <span className={`text-xs font-bold px-2 py-0.5 rounded border ${PAYMENT_STATUS_BADGE_STYLE[payment.status]}`}>
                                                {PAYMENT_STATUS_LABEL[payment.status]}
                                            </span>
                                        </td>
                                        <td className="py-4 pr-6 text-gray-500">{payment.paidAt?.substring(0, 10) ?? '-'}</td>
                                        <td className="py-4 text-gray-500">{payment.canceledAt?.substring(0, 10) ?? '-'}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>

                    {/* 페이징 */}
                    {totalPages > 1 && (
                        <div className="flex justify-center items-center gap-4 mt-8">
                            <button disabled={page === 0}
                                onClick={() => setPage(prev => prev - 1)}
                                className="p-2 border rounded-full hover:bg-gray-50 disabled:opacity-30 cursor-pointer transition-colors">
                                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M15 19l-7-7 7-7" />
                                </svg>
                            </button>
                            <span className="text-sm font-medium text-gray-700">
                                <span className="text-blue-600">{page + 1}</span> / {totalPages}
                            </span>
                            <button disabled={isLast}
                                onClick={() => setPage(prev => prev + 1)}
                                className="p-2 border rounded-full hover:bg-gray-50 disabled:opacity-30 cursor-pointer transition-colors">
                                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M9 5l7 7-7 7" />
                                </svg>
                            </button>
                        </div>
                    )}
                </>
            )}
        </>
    );
};

export default PaymentManagementTab;
