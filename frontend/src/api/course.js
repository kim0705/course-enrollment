import instance from './axios';

/* 강의 등록 */
export const registerCourse = (creatorId, data) => {
    return instance.post('/api/courses', data, {
        headers: { 'X-User-Id': creatorId },
    }).then(res => res.data);
};

/* 강의 목록 조회 */
export const getCourseList = (params) => {
    return instance.get('/api/courses', { params }).then(res => res.data);
};

/* 강의 상세 조회 */
export const getCourseDetail = (courseId) => {
    return instance.get(`/api/courses/${courseId}`).then(res => res.data);
};

/* 강의 수정 */
export const updateCourse = (courseId, creatorId, data) => {
    return instance.put(`/api/courses/${courseId}`, data, {
        headers: { 'X-User-Id': creatorId },
    }).then(res => res.data);
};

/* 강의 공개 (DRAFT → OPEN) */
export const publishCourse = (courseId, creatorId) => {
    return instance.patch(`/api/courses/${courseId}/publish`, {}, {
        headers: { 'X-User-Id': creatorId },
    }).then(res => res.data);
};

/* 강의 마감 (OPEN → CLOSED) */
export const closeCourse = (courseId, creatorId) => {
    return instance.patch(`/api/courses/${courseId}/close`, {}, {
        headers: { 'X-User-Id': creatorId },
    }).then(res => res.data);
};

/* 나의 강의 목록 조회 (CREATOR 전용) */
export const getMyCourses = () => {
    return instance.get('/api/courses/my').then(res => res.data);
};

/* 강의별 수강생 목록 조회 (CREATOR 전용) */
export const getCourseEnrollments = (courseId) => {
    return instance.get(`/api/courses/${courseId}/enrollments`).then(res => res.data);
};
