import instance from './axios';

/* 토스페이먼츠 결제 승인 */
export const confirmPayment = ({ enrollmentId, paymentKey, orderId, orderName, amount }, signal) => {
    return instance.post('/api/payments/confirm', { enrollmentId, paymentKey, orderId, orderName, amount }, { signal }).then(res => res.data);
};

/* 나의 결제 내역 조회 */
export const getMyPayments = (page = 0, size = 10) => {
    return instance.get('/api/payments/my', { params: { page, size } }).then(res => res.data);
};
