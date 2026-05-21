import instance from './axios';

/* 강사 신청 */
export const requestCreator = (reason) => {
    return instance.post('/api/creator-requests', { reason }).then(res => res.data);
};

/* 강사 신청 목록 조회 (관리자) */
export const getCreatorRequestList = () => {
    return instance.get('/api/admin/creator-requests').then(res => res.data);
};

/* 강사 신청 승인 (관리자) */
export const approveCreatorRequest = (id) => {
    return instance.patch(`/api/admin/creator-requests/${id}/approve`).then(res => res.data);
};

/* 강사 신청 거절 (관리자) */
export const rejectCreatorRequest = (id, rejectReason) => {
    return instance.patch(`/api/admin/creator-requests/${id}/reject`, { rejectReason }).then(res => res.data);
};
