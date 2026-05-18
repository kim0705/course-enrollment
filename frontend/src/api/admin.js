import instance from './axios';

/* 대시보드 통계 조회 */
export const getAdminDashboard = () => {
    return instance.get('/api/admin/dashboard').then(res => res.data);
};

/* 전체 사용자 목록 조회 */
export const getAdminUsers = () => {
    return instance.get('/api/admin/users').then(res => res.data);
};

/* 사용자 역할 변경 */
export const updateUserRole = (userId, role) => {
    return instance.patch(`/api/admin/users/${userId}/role`, { role }).then(res => res.data);
};

/* 전체 강의 목록 조회 */
export const getAdminCourses = (page = 0, size = 10) => {
    return instance.get('/api/admin/courses', { params: { page, size } }).then(res => res.data);
};

/* 강의 강제 폐강 */
export const forceCloseCourse = (courseId) => {
    return instance.patch(`/api/admin/courses/${courseId}/close`).then(res => res.data);
};

/* 관리자 비밀번호 변경 */
export const updateAdminPassword = (currentPassword, newPassword) => {
    return instance.patch('/api/admin/me/password', { currentPassword, newPassword }).then(res => res.data);
};
