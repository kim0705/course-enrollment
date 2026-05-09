import { useNavigate, useSearchParams } from 'react-router-dom';

/* 결제 실패 페이지 */
const PaymentFailPage = () => {
    /* 페이지 이동을 위한 네비게이트 함수 */
    const navigate = useNavigate();
    /* URL 검색 파라미터에서 결제 실패 정보 추출 */
    const [searchParams] = useSearchParams();
    /* 결제 실패 정보 (code, message, orderId) */
    const code = searchParams.get('code');
    /* 결제 실패 메시지, 주문번호 등은 URL 파라미터로 전달받음 */
    const message = searchParams.get('message');
    /* 주문번호는 결제 실패 시에도 URL 파라미터로 전달될 수 있음 */
    const orderId = searchParams.get('orderId');

    return (
        <div className="max-w-lg mx-auto mt-20 p-8 text-center">
            <div className="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-6">
                <svg className="w-8 h-8 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12" />
                </svg>
            </div>

            <h1 className="text-2xl font-extrabold text-gray-900 mb-3">결제가 취소되었습니다</h1>

            {message && (
                <p className="text-gray-500 mb-2">{message}</p>
            )}
            {code && (
                <p className="text-xs text-gray-400 mb-6">오류 코드: {code}</p>
            )}
            {orderId && (
                <p className="text-xs text-gray-400 mb-6">주문번호: {orderId}</p>
            )}

            <button
                onClick={() => navigate('/my-page', { state: { tab: 'enrollments' } })}
                className="px-6 py-3 bg-blue-600 text-white font-semibold rounded-md hover:bg-blue-700 transition-colors cursor-pointer"
            >
                마이페이지로 돌아가기
            </button>
        </div>
    );
};

export default PaymentFailPage;
