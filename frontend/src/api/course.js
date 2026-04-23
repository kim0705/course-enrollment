import instance from './axios';

/* 강의 등록 */
export const registerCourse = (creatorId, data) => {
    return instance.post('/api/courses', data, {
        headers: { 'X-User-Id': creatorId },
    }).then(res => res.data);
};