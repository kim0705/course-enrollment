import { useQuery } from '@tanstack/react-query';
import { getMyCourses } from '../api/course';

/* 내 강의 목록 조회를 위한 커스텀 훅 */
export const useMyCourses = (page = 0, size = 10, enabled = true) => {
    return useQuery({
        queryKey: ['myCourses', page, size],
        queryFn: () => getMyCourses(page, size).then(res => res.data),
        enabled,
    });
};
