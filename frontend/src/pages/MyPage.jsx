import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { cancelEnrollment, confirmEnrollment, getMyEnrollments } from '../api/enrollment';
import { getCourseEnrollments, getMyCourses } from '../api/course';
import { useAuth } from '../context/AuthContext';
import { ENROLLMENT_STATUS_BADGE_STYLE, ENROLLMENT_STATUS_LABEL } from '../utils/statusConfig';

const MyPage = () => {
    const navigate = useNavigate();
    const { user } = useAuth();
    const [activeTab, setActiveTab] = useState('enrollments');
    const [enrollmentData, setEnrollmentData] = useState({ content: [], totalCount: 0, totalPages: 0, last: false });
    const [enrollmentPage, setEnrollmentPage] = useState(0);
    const [myCourses, setMyCourses] = useState([]);
    const [selectedCourseId, setSelectedCourseId] = useState('');
    const [courseEnrollments, setCourseEnrollments] = useState([]);

    const fetchMyEnrollments = async (page = 0) => {
        try {
            const result = await getMyEnrollments(page);
            setEnrollmentData(result.data);
        } catch (err) {
            console.error(err);
            alert('수강 신청 목록을 불러오는데 실패했습니다.');
        }
    };

    useEffect(() => {
        fetchMyEnrollments(enrollmentPage);
    }, [enrollmentPage]);

    /* 강의별 수강생 목록 탭 진입 시 나의 강의 목록 조회 */
    useEffect(() => {
        if (activeTab !== 'students') return;

        const fetchMyCourses = async () => {
            try {
                const result = await getMyCourses();
                
                setMyCourses(result.data);
                setSelectedCourseId('');
                setCourseEnrollments([]);
            } catch (err) {
                console.error(err);
                alert('강의 목록을 불러오는데 실패했습니다.');
            }
        };

        fetchMyCourses();
    }, [activeTab]);

    /* 강의 선택 시 수강생 목록 조회 */
    const handleCourseSelect = async (courseId) => {
        setSelectedCourseId(courseId);
        
        if (!courseId) {
            setCourseEnrollments([]);
            return;
        }

        try {
            const result = await getCourseEnrollments(courseId);
            setCourseEnrollments(result.data);
        } catch (err) {
            alert(err.response?.data?.message || '수강생 목록을 불러오는데 실패했습니다.');
        }
    };

    /* 결제 요청 */
    const handleConfirm = async (enrollmentId) => {
        if (!window.confirm('결제하시겠습니까?')) return;

        try {
            await confirmEnrollment(enrollmentId);
            alert('결제가 완료되었습니다.');
            fetchMyEnrollments(enrollmentPage);
        } catch (err) {
            alert(err.response?.data?.message || '결제에 실패했습니다.');
        }
    };

    /* 수강 취소 */
    const handleCancel = async (enrollmentId) => {
        if (!window.confirm('수강을 취소하시겠습니까?')) return;

        try {
            await cancelEnrollment(enrollmentId);
            alert('수강이 취소되었습니다.');
            fetchMyEnrollments(enrollmentPage);
        } catch (err) {
            alert(err.response?.data?.message || '수강 취소에 실패했습니다.');
        }
    };

    const tabs = [
        { key: 'enrollments', label: '나의 수강목록' },
        ...(user?.role === 'CREATOR' ? [{ key: 'students', label: '강의별 수강생 목록' }] : []),
    ];

    return (
        <div className="max-w-4xl mx-auto mt-10 p-6">
            <h1 className="text-3xl font-extrabold text-gray-900 mb-8">마이페이지</h1>

            {/* 탭 */}
            <div className="flex gap-1 border-b border-gray-200 mb-8">
                {tabs.map(tab => (
                    <button
                        key={tab.key}
                        onClick={() => setActiveTab(tab.key)}
                        className={`px-5 py-2.5 text-sm font-semibold transition-colors cursor-pointer ${activeTab === tab.key
                                ? 'border-b-2 border-blue-600 text-blue-600'
                                : 'text-gray-500 hover:text-gray-700'
                            }`}
                    >
                        {tab.label}
                    </button>
                ))}
            </div>

            {/* 나의 수강목록 */}
            {activeTab === 'enrollments' && (
                <>
                    {enrollmentData.content.length === 0 ? (
                        <div className="text-center text-gray-400 py-32 border-2 border-dashed border-gray-100 rounded-2xl">
                            수강 신청 내역이 없습니다.
                        </div>
                    ) : (
                        <div className="flex flex-col gap-4">
                            {enrollmentData.content.map(enrollment => (
                                <div key={enrollment.id}
                                    className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm flex flex-col sm:flex-row sm:items-center justify-between gap-4">

                                    {/* 강의 정보 */}
                                    <div className="flex-1 min-w-0">
                                        <div className="flex items-center gap-2 mb-1">
                                            <span
                                                onClick={() => navigate(`/courses/${enrollment.courseId}`)}
                                                className="text-base font-bold text-gray-900 truncate cursor-pointer hover:text-blue-600 transition-colors"
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
                                                    onClick={() => handleConfirm(enrollment.id)}
                                                    className="px-4 py-2 bg-blue-600 text-white text-sm font-semibold rounded-md hover:bg-blue-700 transition-colors cursor-pointer"
                                                >
                                                    결제하기
                                                </button>
                                                <button
                                                    onClick={() => handleCancel(enrollment.id)}
                                                    className="px-4 py-2 border border-gray-300 text-gray-600 text-sm font-semibold rounded-md hover:bg-gray-50 transition-colors cursor-pointer"
                                                >
                                                    취소하기
                                                </button>
                                            </>
                                        )}
                                        {enrollment.status === 'CONFIRMED' && (
                                            <button
                                                onClick={() => handleCancel(enrollment.id)}
                                                className="px-4 py-2 border border-gray-300 text-gray-600 text-sm font-semibold rounded-md hover:bg-gray-50 transition-colors cursor-pointer"
                                            >
                                                취소하기
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
                    )}

                    {/* 페이징 섹션 */}
                    {(enrollmentData?.totalPages ?? 0) > 1 && (
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
                </>
            )}

            {/* 강의별 수강생 목록 (CREATOR 전용) */}
            {activeTab === 'students' && (
                <>
                    {/* 강의 선택 */}
                    <div className="mb-6">
                        <select
                            value={selectedCourseId}
                            onChange={(e) => handleCourseSelect(e.target.value)}
                            className="w-full sm:w-80 border border-gray-300 rounded-md px-3 py-2 text-sm text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500"
                        >
                            <option value="">강의를 선택하세요</option>
                            {myCourses.map(course => (
                                <option key={course.id} value={course.id}>
                                    {course.title}
                                </option>
                            ))}
                        </select>
                    </div>

                    {/* 수강생 목록 */}
                    {!selectedCourseId ? (
                        <div className="text-center text-gray-400 py-32 border-2 border-dashed border-gray-100 rounded-2xl">
                            강의를 선택하면 수강생 목록이 표시됩니다.
                        </div>
                    ) : courseEnrollments.length === 0 ? (
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
                                    {courseEnrollments.map(enrollment => (
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
                </>
            )}
        </div>
    );
};

export default MyPage;
