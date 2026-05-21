import { useEffect, useState } from 'react';
import { ENROLLMENT_STATUS_BADGE_STYLE, ENROLLMENT_STATUS_LABEL } from '../../utils/statusConfig';
import { useMyCourses } from '../../hooks/useMyCourses';
import { useCourseEnrollments } from '../../hooks/useCourseEnrollments';

/* 강의별 수강생 목록 탭 (CREATOR 전용) */
const StudentTab = () => {
    /* 선택한 강의 ID */
    const [selectedCourseId, setSelectedCourseId] = useState('');
    /* 수강생 목록 페이지 */
    const [studentPage, setStudentPage] = useState(0);

    /* 내 강의 데이터 (드롭다운용 전체 목록) */
    const { data: myCourseData = { content: [] }, isLoading: isCoursesLoading } = useMyCourses(0, 100);
    /* 선택한 강의의 수강 신청 데이터 */
    const { data: courseEnrollmentData = { content: [], totalCount: 0, totalPages: 0, last: false }, isLoading: isEnrollmentsLoading } = useCourseEnrollments(selectedCourseId, studentPage);

    /* 강의가 변경될 때마다 수강생 페이지 초기화 */
    useEffect(() => {
        setSelectedCourseId('');
    }, []);

    /* 강의 선택 핸들러 */
    const handleCourseSelect = (courseId) => {
        setSelectedCourseId(courseId);
        setStudentPage(0);
    };

    if (isCoursesLoading || isEnrollmentsLoading) return <div className="text-center py-20 text-gray-400">로딩 중...</div>;

    return (
        <>
            {/* 강의 선택 */}
            <div className="mb-6">
                <select
                    value={selectedCourseId}
                    onChange={(e) => handleCourseSelect(e.target.value)}
                    className="w-full sm:w-80 border border-gray-300 rounded-md px-3 py-2 text-sm text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                    <option value="">강의를 선택하세요</option>
                    {myCourseData.content.filter(course => course.status !== 'FORCE_CLOSED').map(course => (
                        <option key={course.id} value={course.id}>{course.title}</option>
                    ))}
                </select>
            </div>

            {/* 수강생 목록 */}
            {!selectedCourseId ? (
                <div className="text-center text-gray-400 py-32 border-2 border-dashed border-gray-100 rounded-2xl">
                    강의를 선택하면 수강생 목록이 표시됩니다.
                </div>
            ) : courseEnrollmentData.content.length === 0 ? (
                <div className="text-center text-gray-400 py-32 border-2 border-dashed border-gray-100 rounded-2xl">
                    수강생이 없습니다.
                </div>
            ) : (
                <div className="overflow-x-auto">
                    <table className="w-full text-sm text-left">
                        <thead>
                            <tr className="border-b border-gray-200 text-gray-500 text-xs uppercase">
                                <th className="pb-3 pr-6 font-semibold">수강생</th>
                                <th className="pb-3 pr-6 font-semibold">상태</th>
                                <th className="pb-3 pr-6 font-semibold">신청일</th>
                                <th className="pb-3 font-semibold">결제일 / 취소일</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-gray-100">
                            {courseEnrollmentData.content.map(enrollment => (
                                <tr key={enrollment.id} className="hover:bg-gray-50">
                                    <td className="py-4 pr-6 font-medium text-gray-900">{enrollment.userName}</td>
                                    <td className="py-4 pr-6">
                                        <span className={`text-xs font-bold px-2 py-0.5 rounded border ${ENROLLMENT_STATUS_BADGE_STYLE[enrollment.status]}`}>
                                            {ENROLLMENT_STATUS_LABEL[enrollment.status]}
                                        </span>
                                    </td>
                                    <td className="py-4 pr-6 text-gray-500">{enrollment.createdAt?.substring(0, 10)}</td>
                                    <td className="py-4 text-gray-500">
                                        {enrollment.status === 'CONFIRMED' && enrollment.confirmedAt
                                            ? enrollment.confirmedAt.substring(0, 10)
                                            : enrollment.status === 'CANCELLED' && enrollment.cancelledAt
                                                ? enrollment.cancelledAt.substring(0, 10)
                                                : '-'}
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}

            {/* 페이징 */}
            {(courseEnrollmentData.totalPages ?? 0) > 1 && (
                <div className="flex justify-center items-center gap-4 mt-8">
                    <button disabled={studentPage === 0}
                        onClick={() => setStudentPage(prev => prev - 1)}
                        className="p-2 border rounded-full hover:bg-gray-50 disabled:opacity-30 cursor-pointer transition-colors">
                        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M15 19l-7-7 7-7" />
                        </svg>
                    </button>
                    <span className="text-sm font-medium text-gray-700">
                        <span className="text-blue-600">{studentPage + 1}</span> / {courseEnrollmentData.totalPages}
                    </span>
                    <button disabled={courseEnrollmentData.last}
                        onClick={() => setStudentPage(prev => prev + 1)}
                        className="p-2 border rounded-full hover:bg-gray-50 disabled:opacity-30 cursor-pointer transition-colors">
                        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M9 5l7 7-7 7" />
                        </svg>
                    </button>
                </div>
            )}
        </>
    );
};

export default StudentTab;
