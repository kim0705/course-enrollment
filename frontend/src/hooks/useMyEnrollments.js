import { useQuery } from '@tanstack/react-query';
import { getMyEnrollments } from '../api/enrollment';

/* 내 수강 신청 목록 조회를 위한 커스텀 훅 */
export const useMyEnrollments = (page) => {
    return useQuery({
        queryKey: ['myEnrollments', page],
        queryFn: () => getMyEnrollments(page).then(res => res.data),
    });
};
