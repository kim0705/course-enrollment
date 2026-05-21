import { useNavigate } from 'react-router-dom';
import { ENROLLMENT_STATUS_BADGE_STYLE, ENROLLMENT_STATUS_LABEL } from '../../utils/statusConfig';
import { useMyEnrollments } from '../../hooks/useMyEnrollments';
import { useEnrollmentActions } from '../../hooks/useEnrollmentActions';
import CancelModal from '../../components/CancelModal';

/* 나의 수강목록 탭 */
const EnrollmentTab = () => {
    /* 페이지 이동 훅 */
    const navigate = useNavigate();
    /* 수강목록 액션 훅 */
    const {
        enrollmentPage,
        setEnrollmentPage,
        isPaymentLoading,
        cancelModal,
        setCancelModal,
        selectedReason,
        setSelectedReason,
        customReason,
        setCustomReason,
        handlePayment,
        handleFreeConfirm,
        handleCancel,
        handleCancelClick,
        handleCancelConfirm,
    } = useEnrollmentActions();
    
    /* 수강목록 데이터 */
    const { data: enrollmentData = { content: [], totalCount: 0, totalPages: 0, last: false }, isLoading } = useMyEnrollments(enrollmentPage);

    if (isLoading) return <div className="text-center py-20 text-gray-400">로딩 중...</div>;

    if (enrollmentData.content.length === 0) return (
        <div className="text-center text-gray-400 py-32 border-2 border-dashed border-gray-100 rounded-2xl">
            수강 신청 내역이 없습니다.
        </div>
    );

    return (
        <>
            <div className="flex flex-col gap-4">
                    {enrollmentData.content.map(enrollment => (
                        <div key={enrollment.id}
                            className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm flex flex-col sm:flex-row sm:items-center justify-between gap-4">

                            {/* 강의 정보 */}
                            <div className="flex-1 min-w-0">
                                <div className="flex items-center gap-2 mb-1">
                                    <span
                                        onClick={() => enrollment.courseStatus !== 'FORCE_CLOSED' && navigate(`/courses/${enrollment.courseId}`, { state: { from: 'my-page', tab: 'enrollments' } })}
                                        className={`text-base font-bold text-gray-900 truncate transition-colors ${enrollment.courseStatus !== 'FORCE_CLOSED' ? 'cursor-pointer hover:text-blue-600' : 'cursor-default'}`}
                                    >
                                        {enrollment.courseTitle}
                                    </span>
                                    <span className={`text-xs font-bold px-2 py-0.5 rounded border ${ENROLLMENT_STATUS_BADGE_STYLE[enrollment.status]}`}>
                                        {ENROLLMENT_STATUS_LABEL[enrollment.status]}
                                    </span>
                                </div>

                                <div className="flex flex-wrap items-center gap-3 text-xs text-gray-500 mt-1">
                                    <span>{enrollment.price === 0 ? '무료' : `${enrollment.price.toLocaleString()}원`}</span>
                                    <span className="text-gray-300">|</span>
                                    <span>{enrollment.startDate} ~ {enrollment.endDate}</span>
                                    <span className="text-gray-300">|</span>
                                    <span>신청일 {enrollment.createdAt?.substring(0, 10)}</span>
                                </div>

                                {enrollment.status === 'CONFIRMED' && enrollment.confirmedAt && (
                                    <p className="text-xs text-green-600 mt-1">
                                        결제일 {enrollment.confirmedAt.substring(0, 10)}
                                    </p>
                                )}
                                {enrollment.status === 'CANCELLED' && enrollment.cancelledAt && (
                                    <p className="text-xs text-gray-400 mt-1">
                                        취소일 {enrollment.cancelledAt.substring(0, 10)}
                                    </p>
                                )}
                                {enrollment.status === 'FORCE_CLOSED' && enrollment.cancelledAt && (
                                    <p className="text-xs text-red-400 mt-1">
                                        폐강일 {enrollment.cancelledAt.substring(0, 10)}
                                    </p>
                                )}
                                {enrollment.status === 'WAITLIST' && enrollment.waitlistPosition && (
                                    <p className="text-xs text-purple-600 mt-1">
                                        대기 순번 {enrollment.waitlistPosition}번
                                    </p>
                                )}
                            </div>

                            {/* 액션 버튼 */}
                            <div className="flex gap-2 shrink-0">
                                {enrollment.status === 'PENDING' && (
                                    <>
                                        <button
                                            onClick={() => enrollment.price > 0 ? handlePayment(enrollment) : handleFreeConfirm(enrollment.id)}
                                            disabled={isPaymentLoading}
                                            className="px-4 py-2 bg-blue-600 text-white text-sm font-semibold rounded-md hover:bg-blue-700 transition-colors cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
                                        >
                                            {enrollment.price > 0 ? '결제하기' : '수강 확정하기'}
                                        </button>
                                        <button
                                            onClick={() => handleCancel(enrollment.id)}
                                            className="px-4 py-2 border border-gray-300 text-gray-600 text-sm font-semibold rounded-md hover:bg-gray-50 transition-colors cursor-pointer"
                                        >
                                            취소하기
                                        </button>
                                    </>
                                )}
                                {enrollment.status === 'CONFIRMED' &&
                                    enrollment.confirmedAt &&
                                    new Date(enrollment.confirmedAt) > new Date(Date.now() - 7 * 24 * 60 * 60 * 1000) && (
                                    <button
                                        onClick={() => handleCancelClick(enrollment.id)}
                                        className="px-4 py-2 border border-gray-300 text-gray-600 text-sm font-semibold rounded-md hover:bg-gray-50 transition-colors cursor-pointer"
                                    >
                                        수강 취소
                                    </button>
                                )}
                                {enrollment.status === 'WAITLIST' && (
                                    <button
                                        onClick={() => handleCancel(enrollment.id)}
                                        className="px-4 py-2 border border-gray-300 text-gray-600 text-sm font-semibold rounded-md hover:bg-gray-50 transition-colors cursor-pointer"
                                    >
                                        대기 취소
                                    </button>
                                )}
                            </div>
                        </div>
                    ))}
                </div>

            {/* 페이징 */}
            {(enrollmentData.totalPages ?? 0) > 1 && (
                <div className="flex justify-center items-center gap-4 mt-8">
                    <button disabled={enrollmentPage === 0}
                        onClick={() => setEnrollmentPage(prev => prev - 1)}
                        className="p-2 border rounded-full hover:bg-gray-50 disabled:opacity-30 cursor-pointer transition-colors">
                        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M15 19l-7-7 7-7" />
                        </svg>
                    </button>
                    <span className="text-sm font-medium text-gray-700">
                        <span className="text-blue-600">{enrollmentPage + 1}</span> / {enrollmentData.totalPages}
                    </span>
                    <button disabled={enrollmentData.last}
                        onClick={() => setEnrollmentPage(prev => prev + 1)}
                        className="p-2 border rounded-full hover:bg-gray-50 disabled:opacity-30 cursor-pointer transition-colors">
                        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M9 5l7 7-7 7" />
                        </svg>
                    </button>
                </div>
            )}

            {/* CONFIRMED 취소 모달 */}
            <CancelModal
                open={cancelModal.open}
                onClose={() => setCancelModal({ open: false, enrollmentId: null })}
                onConfirm={handleCancelConfirm}
                selectedReason={selectedReason}
                setSelectedReason={setSelectedReason}
                customReason={customReason}
                setCustomReason={setCustomReason}
            />
        </>
    );
};

export default EnrollmentTab;
