import instance from './axios';

/* 토스페이먼츠 결제 승인 */
export const confirmPayment = ({ enrollmentId, paymentKey, orderId, orderName, amount }, signal) => {
    return instance.post('/api/payments/confirm', { enrollmentId, paymentKey, orderId, orderName, amount }, { signal }).then(res => res.data);
};
