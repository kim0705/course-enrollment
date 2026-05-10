import { useQuery } from '@tanstack/react-query';
import { getCourseList } from '../api/course';

/* 강의 목록 조회를 위한 커스텀 훅 */
export const useCourseList = (params) => {
    return useQuery({
        queryKey: ['courseList', params],
        queryFn: () => getCourseList(params).then(res => res.data),
    });
};
