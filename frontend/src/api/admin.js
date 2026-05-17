import instance from './axios';

/* 관리자 비밀번호 변경 */
export const updateAdminPassword = (currentPassword, newPassword) => {
    return instance.patch('/api/admin/me/password', { currentPassword, newPassword }).then(res => res.data);
};
