import instance from './axios';

/* 수강 신청 */
export const createEnrollment = (courseId) => {
    return instance.post('/api/enrollments', { courseId }).then(res => res.data);
};
