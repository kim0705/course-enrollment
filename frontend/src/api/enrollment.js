import instance from './axios';

/* 수강 신청 */
export const createEnrollment = (courseId) => {
    return instance.post('/api/enrollments', { courseId }).then(res => res.data);
};

/* 나의 수강 신청 목록 조회 */
export const getMyEnrollments = (page = 0, size = 10) => {
    return instance.get('/api/enrollments/me', { params: { page, size } }).then(res => res.data);
};

/* 결제 요청 (PENDING → CONFIRMED) */
export const confirmEnrollment = (enrollmentId) => {
    return instance.patch(`/api/enrollments/${enrollmentId}/confirm`).then(res => res.data);
};

/* 수강 취소 */
export const cancelEnrollment = (enrollmentId, cancelReason = null) => {
    return instance.patch(`/api/enrollments/${enrollmentId}/cancel`, cancelReason ? { cancelReason } : undefined).then(res => res.data);
};
