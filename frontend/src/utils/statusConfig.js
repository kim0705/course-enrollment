/* 강의 상태 레이블 */
export const COURSE_STATUS_LABEL = {
    DRAFT: '준비 중',
    OPEN: '모집 중',
    CLOSED: '마감',
};

/* 강의 상태 뱃지 스타일 - 카드 썸네일용 (채워진 스타일) */
export const COURSE_STATUS_CARD_STYLE = {
    DRAFT: 'bg-gray-500 text-white',
    OPEN: 'bg-green-500 text-white',
    CLOSED: 'bg-gray-500 text-white',
};

/* 강의 상태 뱃지 스타일 - 인라인 뱃지용 (테두리 스타일) */
export const COURSE_STATUS_BADGE_STYLE = {
    DRAFT: 'bg-gray-50 text-gray-500 border-gray-200',
    OPEN: 'bg-green-50 text-green-600 border-green-200',
    CLOSED: 'bg-red-50 text-red-500 border-red-200',
};

/* 수강 신청 상태 레이블 */
export const ENROLLMENT_STATUS_LABEL = {
    PENDING: '결제 대기',
    CONFIRMED: '수강 확정',
    CANCELLED: '취소됨',
    WAITLIST: '대기 중',
};

/* 수강 신청 상태 뱃지 스타일 */
export const ENROLLMENT_STATUS_BADGE_STYLE = {
    PENDING: 'bg-amber-50 text-amber-600 border-amber-100',
    CONFIRMED: 'bg-green-50 text-green-600 border-green-100',
    CANCELLED: 'bg-gray-50 text-gray-400 border-gray-200',
    WAITLIST: 'bg-purple-50 text-purple-600 border-purple-100',
};
