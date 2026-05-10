import { useQuery } from '@tanstack/react-query';
import { getCourseEnrollments } from '../api/course';

/* 강의 수강생 목록 조회를 위한 커스텀 훅 */
export const useCourseEnrollments = (courseId, page) => {
    return useQuery({
        queryKey: ['courseEnrollments', courseId, page],
        queryFn: () => getCourseEnrollments(courseId, page).then(res => res.data),
        enabled: !!courseId,
    });
};
