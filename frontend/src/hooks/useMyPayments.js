import { useQuery } from '@tanstack/react-query';
import { getMyPayments } from '../api/payment';

/* 내 결제 내역 조회를 위한 커스텀 훅 */
export const useMyPayments = (page = 0, enabled = true) => {
    return useQuery({
        queryKey: ['myPayments', page],
        queryFn: () => getMyPayments(page).then(res => res.data),
        enabled,
    });
};
