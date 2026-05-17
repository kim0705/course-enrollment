import instance from './axios';

/* 전체 사용자 목록 조회 */
export const getUserList = () => {
    return instance.get('/api/users').then(res => res.data);
};

/* 프로필 수정 (이름·이메일) */
export const updateProfile = (name, email) => {
    return instance.patch('/api/users/me', { name, email }).then(res => res.data);
};

/* 비밀번호 변경 */
export const updatePassword = (currentPassword, newPassword) => {
    return instance.patch('/api/users/me/password', { currentPassword, newPassword }).then(res => res.data);
};
