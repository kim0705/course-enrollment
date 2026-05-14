import instance from './axios';

/* 강사 신청 */
export const requestCreator = (reason) => {
    return instance.post('/api/creator-requests', { reason }).then(res => res.data);
};
