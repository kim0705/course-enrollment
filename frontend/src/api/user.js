import instance from './axios';

/* 전체 사용자 목록 조회 */
export const getUserList = () => {
    return instance.get('/api/users').then(res => res.data);
};
