import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { confirmPayment } from '../api/payment';

/* 결제 성공 처리 페이지 */
const PaymentSuccessPage = () => {
    /* 페이지 이동을 위한 네비게이트 함수 */
    const navigate = useNavigate();
    /* URL 검색 파라미터에서 결제 정보 추출 */
    const [searchParams] = useSearchParams();
    /* 결제 확인 상태 (loading / success / error) */
    const [status, setStatus] = useState('loading');
    /* 오류 시 표시할 주문번호 */
    const [orderId, setOrderId] = useState('');

    /* 마운트 시 URL 파라미터로 백엔드 결제 승인 요청 */
    useEffect(() => {
        const controller = new AbortController();
        const paymentKey = searchParams.get('paymentKey');
        const rawOrderId = searchParams.get('orderId');
        const amount = parseInt(searchParams.get('amount'), 10);
        const enrollmentId = parseInt(searchParams.get('enrollmentId'), 10);
        const orderName = searchParams.get('orderName');

        setOrderId(rawOrderId);

        confirmPayment({ enrollmentId, paymentKey, orderId: rawOrderId, orderName, amount }, controller.signal)
            .then(() => setStatus('success'))
            .catch((err) => {
                if (err?.code === 'ERR_CANCELED') return;
                setStatus('error');
            });

        return () => controller.abort();
    }, [searchParams]);

    /* 상태에 따른 UI 렌더링 */
    if (status === 'loading') {
        return (
            <div className="max-w-lg mx-auto mt-20 p-8 text-center">
                <div className="w-12 h-12 border-4 border-blue-600 border-t-transparent rounded-full animate-spin mx-auto mb-6" />
                <p className="text-gray-500 font-medium">결제를 확인하는 중입니다...</p>
            </div>
        );
    }

    if (status === 'error') {
        return (
            <div className="max-w-lg mx-auto mt-20 p-8 text-center">
                <div className="w-16 h-16 bg-amber-100 rounded-full flex items-center justify-center mx-auto mb-6">
                    <svg className="w-8 h-8 text-amber-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 9v2m0 4h.01M12 3a9 9 0 110 18A9 9 0 0112 3z" />
                    </svg>
                </div>
                <h1 className="text-2xl font-extrabold text-gray-900 mb-3">결제 확인 중 오류가 발생했습니다</h1>
                <p className="text-gray-500 mb-2">결제는 완료되었으나 시스템 오류가 발생했습니다.</p>
                <p className="text-sm text-gray-400 mb-6">주문번호 <span className="font-mono font-semibold">{orderId}</span>를 기록하여 관리자에게 문의해주세요.</p>
                <button
                    onClick={() => navigate('/my-page', { state: { tab: 'enrollments' } })}
                    className="px-6 py-3 bg-blue-600 text-white font-semibold rounded-md hover:bg-blue-700 transition-colors cursor-pointer"
                >
                    마이페이지로 이동
                </button>
            </div>
        );
    }

    return (
        <div className="max-w-lg mx-auto mt-20 p-8 text-center">
            <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-6">
                <svg className="w-8 h-8 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 13l4 4L19 7" />
                </svg>
            </div>
            <h1 className="text-2xl font-extrabold text-gray-900 mb-3">결제가 완료되었습니다</h1>
            <p className="text-gray-500 mb-8">수강 신청이 확정되었습니다. 마이페이지에서 확인하세요.</p>
            <button
                onClick={() => navigate('/my-page', { state: { tab: 'enrollments' } })}
                className="px-6 py-3 bg-blue-600 text-white font-semibold rounded-md hover:bg-blue-700 transition-colors cursor-pointer"
            >
                마이페이지에서 확인하기
            </button>
        </div>
    );
};

export default PaymentSuccessPage;
