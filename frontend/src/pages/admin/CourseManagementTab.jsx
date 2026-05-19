import { useEffect, useState } from 'react';
import { getAdminCourses, forceCloseCourse } from '../../api/admin';
import { COURSE_STATUS_LABEL, COURSE_STATUS_BADGE_STYLE } from '../../utils/statusConfig';

/* 관리자 강의 관리 탭 */
const CourseManagementTab = () => {
    /* 강의 목록 상태 */
    const [courses, setCourses] = useState([]);
    /* 페이징 상태 */
    const [page, setPage] = useState(0);
    /* 전체 페이지 수 상태 */
    const [totalPages, setTotalPages] = useState(0);
    /* 마지막 페이지 여부 상태 */
    const [isLast, setIsLast] = useState(false);
    /* 로딩 상태 */
    const [isLoading, setIsLoading] = useState(true);

    /* 강의 목록 조회 */
    const fetchCourses = (p) => {
        setIsLoading(true);

        getAdminCourses(p)
            .then(res => {
                setCourses(res.data.content);
                setTotalPages(res.data.totalPages);
                setIsLast(res.data.last);
            })
            .catch(() => alert('강의 목록 조회에 실패했습니다.'))
            .finally(() => setIsLoading(false));
    };

    /* 페이지 변경 시 강의 목록 재조회 */
    useEffect(() => {
        fetchCourses(page);
    }, [page]);

    /* 강제 폐강 */
    const handleForceClose = async (courseId, title) => {
        if (!window.confirm(`"${title}" 강의를 강제 폐강하시겠습니까?`)) return;

        try {
            await forceCloseCourse(courseId);
            setCourses(prev => prev.map(c => c.id === courseId ? { ...c, status: 'FORCE_CLOSED' } : c));
        } catch (err) {
            alert(err.response?.data?.message || '강제 폐강에 실패했습니다.');
        }
    };

    if (isLoading) return <div className="text-center py-20 text-gray-400">로딩 중...</div>;

    if (courses.length === 0) return (
        <div className="text-center text-gray-400 py-32 border-2 border-dashed border-gray-100 rounded-2xl">
            강의가 없습니다.
        </div>
    );

    return (
        <>
            <div className="overflow-x-auto">
                <table className="w-full text-sm text-left">
                    
                    {/* 테이블 헤더 */ }
                    <thead>
                        <tr className="border-b border-gray-200 text-gray-500 text-xs uppercase">
                            <th className="pb-3 pr-6 font-semibold">강의명</th>
                            <th className="pb-3 pr-6 font-semibold">강사</th>
                            <th className="pb-3 pr-6 font-semibold">상태</th>
                            <th className="pb-3 pr-6 font-semibold">수강인원</th>
                            <th className="pb-3 pr-6 font-semibold">등록일</th>
                            <th className="pb-3 font-semibold"></th>
                        </tr>
                    </thead>
                    {/* 테이블 바디 */ }
                    <tbody className="divide-y divide-gray-100">
                        {courses.map(course => (
                            <tr key={course.id} className="hover:bg-gray-50">
                                <td className="py-4 pr-6 font-medium text-gray-900 max-w-xs truncate">{course.title}</td>
                                <td className="py-4 pr-6 text-gray-700">{course.creatorName}</td>
                                <td className="py-4 pr-6">
                                    <span className={`text-xs font-bold px-2 py-0.5 rounded border ${COURSE_STATUS_BADGE_STYLE[course.status]}`}>
                                        {COURSE_STATUS_LABEL[course.status]}
                                    </span>
                                </td>
                                <td className="py-4 pr-6 text-gray-500">{course.enrolledCount} / {course.capacity}</td>
                                <td className="py-4 pr-6 text-gray-500">{course.createdAt?.substring(0, 10)}</td>
                                <td className="py-4">
                                    {course.status !== 'CLOSED' && course.status !== 'FORCE_CLOSED' && (
                                        <button
                                            onClick={() => handleForceClose(course.id, course.title)}
                                            className="px-3 py-1 text-xs font-semibold text-red-600 border border-red-200 rounded-lg hover:bg-red-50 transition-colors cursor-pointer"
                                        >
                                            강제 폐강
                                        </button>
                                    )}
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>

            {/* 페이징 */}
            {totalPages > 1 && (
                <div className="flex justify-center items-center gap-4 mt-8">
                    <button disabled={page === 0}
                        onClick={() => setPage(prev => prev - 1)}
                        className="p-2 border rounded-full hover:bg-gray-50 disabled:opacity-30 cursor-pointer transition-colors">
                        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M15 19l-7-7 7-7" />
                        </svg>
                    </button>
                    <span className="text-sm font-medium text-gray-700">
                        <span className="text-blue-600">{page + 1}</span> / {totalPages}
                    </span>
                    <button disabled={isLast}
                        onClick={() => setPage(prev => prev + 1)}
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

export default CourseManagementTab;
