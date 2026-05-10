import { useQuery } from '@tanstack/react-query';
import { getMyCourses } from '../api/course';

/* 내 강의 목록 조회를 위한 커스텀 훅 */
export const useMyCourses = (enabled) => {
    return useQuery({
        queryKey: ['myCourses'],
        queryFn: () => getMyCourses().then(res => res.data),
        enabled,
    });
};
