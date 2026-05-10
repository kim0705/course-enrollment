import { useQuery } from '@tanstack/react-query';
import { getCourseDetail } from '../api/course';

/* 강의 상세 정보 조회를 위한 커스텀 훅 */
export const useCourseDetail = (courseId) => {
    return useQuery({
        queryKey: ['courseDetail', courseId],
        queryFn: () => getCourseDetail(courseId).then(res => res.data),
        enabled: !!courseId,
    });
};
