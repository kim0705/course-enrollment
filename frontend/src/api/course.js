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