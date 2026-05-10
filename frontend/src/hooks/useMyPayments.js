import { useQuery } from '@tanstack/react-query';
import { getMyPayments } from '../api/payment';

/* 내 결제 내역 조회를 위한 커스텀 훅 */
export const useMyPayments = (enabled = true) => {
    return useQuery({
        queryKey: ['myPayments'],
        queryFn: () => getMyPayments().then(res => res.data),
        enabled,
    });
};
