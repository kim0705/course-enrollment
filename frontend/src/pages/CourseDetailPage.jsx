import { useEffect, useState } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import { closeCourse, getCourseDetail, publishCourse } from '../api/course';
import { createEnrollment } from '../api/enrollment';
import { useAuth } from '../context/AuthContext';
import { COURSE_STATUS_BADGE_STYLE, COURSE_STATUS_LABEL } from '../utils/statusConfig';

const CourseDetailPage = () => {
    const { courseId } = useParams();
    const navigate = useNavigate();
    const location = useLocation();

    const [course, setCourse] = useState(null);
    const { user } = useAuth();

    /* 현재 로그인한 사용자가 강의 소유자인지 여부 */
    const isOwner = course?.creatorId === user?.id;

    useEffect(() => {
        /* 강의 상세 조회 */
        const fetchCourseDetail = async () => {
            try {
                const result = await getCourseDetail(courseId);
                setCourse(result.data);
            } catch (err) {
                console.error(err);
                alert("강의 정보를 불러오는데 실패했습니다.");
                navigate('/courses');
            }
        };

        fetchCourseDetail();
    }, [courseId, navigate]);

    /* 목록으로 돌아가기 */
    const handleBackToList = () => {
        const previousSearch = location.state?.fromSearch || '';
        navigate(`/courses${previousSearch}`);
    };

    /* 강의 공개 */
    const handlePublish = async () => {
        if (!window.confirm('강의를 공개하시겠습니까?')) return;

        try {
            const result = await publishCourse(courseId, user?.id);
            setCourse(result.data);
        } catch (err) {
            alert(err.response?.data?.message || '강의 공개에 실패했습니다.');
        }
    };

    /* 수강 신청 */
    const handleEnroll = async () => {
        const isFull = course.enrolledCount >= course.capacity;
        const confirmMsg = isFull ? '현재 정원이 초과되었습니다. 대기열에 등록하시겠습니까?' : '수강 신청하시겠습니까?';

        if (!window.confirm(confirmMsg)) return;

        try {
            const enrolled = await createEnrollment(Number(courseId));

            alert(enrolled.data?.status === 'WAITLIST' ? '정원이 초과되어 대기열에 등록되었습니다.\n마이페이지에서 대기 순번을 확인하세요.' : '수강 신청이 완료되었습니다.');
            
            const result = await getCourseDetail(courseId);
            setCourse(result.data);
        } catch (err) {
            alert(err.response?.data?.message || '수강 신청에 실패했습니다.');
        }
    };

    /* 강의 마감 */
    const handleClose = async () => {
        if (!window.confirm('강의를 마감하시겠습니까?')) return;

        try {
            const result = await closeCourse(courseId, user?.id);
            setCourse(result.data);
        } catch (err) {
            alert(err.response?.data?.message || '강의 마감에 실패했습니다.');
        }
    };

    if (!course) return <div className="text-center py-20 text-gray-400">로딩 중...</div>;

    return (
        <div className="max-w-7xl mx-auto mt-10 p-6">
            {/* 상단 헤더 섹션 */}
            <div className="flex justify-end gap-3 mb-8 border-b pb-6">
                {isOwner && course.status === 'DRAFT' && (
                    <button onClick={() => navigate(`/courses/${courseId}/edit`, { state: { fromSearch: location.state?.fromSearch } })}
                        className="px-4 py-2 bg-blue-600 text-white rounded-md font-semibold hover:bg-blue-700 cursor-pointer transition-all shadow-sm text-sm">
                        수정하기
                    </button>
                )}

                <button onClick={handleBackToList}
                    className="px-4 py-2 border border-gray-300 text-gray-600 rounded-md font-semibold hover:bg-gray-50 cursor-pointer transition-all shadow-sm text-sm"
                >
                    목록으로
                </button>
            </div>

            {/* 제목 및 강사 섹션 */}
            <div className="mb-10">
                <div className="flex items-center gap-3 mb-6">
                    <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight leading-tight">
                        {course.title}
                    </h1>
                    <span className={`text-xs font-bold px-3 py-1.5 rounded-md border ${COURSE_STATUS_BADGE_STYLE[course.status]}`}>
                        {COURSE_STATUS_LABEL[course.status]}
                    </span>
                </div>

                <div className="flex items-center gap-3">
                    <div className="w-10 h-10 bg-blue-100 rounded-md flex items-center justify-center text-blue-600 font-bold">
                        {course.creatorName?.charAt(0)}
                    </div>
                    <p className="text-sm font-bold text-gray-900">{course?.creatorName}</p>
                </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-8 items-start">
                {/* 왼쪽 - 강의 정보 섹션 */}
                <div className="md:col-span-2">
                    <div className="bg-white border border-gray-200 rounded-md p-8 shadow-sm min-h-[360px]">
                        <h2 className="text-xl font-bold mb-6 text-gray-900 flex items-center gap-2">
                            <span className="w-1.5 h-6 bg-blue-600 rounded-full"></span>
                            강의 소개
                        </h2>
                        <p className="text-gray-600 whitespace-pre-wrap leading-relaxed text-base">
                            {course.description || '강의 소개 내용이 없습니다.'}
                        </p>
                    </div>
                </div>

                {/* 오른쪽 - 수강 신청 카드(Sticky 카드) 섹션 */}
                <div className="relative">
                    <div className="sticky top-10 h-full">
                        <div className="bg-white border border-gray-200 rounded-md overflow-hidden shadow-md flex flex-col">
                            <div className="p-6">

                                {/* 상단 섹션: 가격, 인원, 일정 */}
                                <div className="space-y-8">
                                    <div className="mb-6">
                                        <p className="text-sm text-gray-500 mb-1 font-semibold">수강 신청가</p>
                                        <p className="text-3xl font-black text-gray-900">
                                            {course.price === 0 ? '무료' : `${course.price.toLocaleString()}원`}
                                        </p>
                                    </div>

                                    <div className="space-y-6 pt-8 border-t border-gray-100">
                                        <div>
                                            <div className="flex justify-between text-xs font-bold mb-2">
                                                <span className="text-gray-500">현재 수강 인원</span>
                                                <span className="text-blue-600">
                                                    {course.enrolledCount} / {course.capacity}명
                                                </span>
                                            </div>
                                            <div className="w-full bg-gray-100 h-2.5 rounded-full overflow-hidden">
                                                <div className="bg-blue-600 h-full transition-all duration-1000 ease-out"
                                                    style={{ width: `${Math.min((course.enrolledCount / course.capacity) * 100, 100)}%` }}
                                                />
                                            </div>
                                        </div>

                                        <div className="bg-gray-50 p-4 rounded-md">
                                            <div className="flex justify-between items-center text-xs">
                                                <span className="text-gray-500 font-medium">강의 일정</span>
                                                <span className="text-gray-900 font-semibold">
                                                    {course.startDate} ~ {course.endDate}
                                                </span>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                {isOwner ? (
                                    <>
                                        {course.status === 'DRAFT' && (
                                            <button onClick={handlePublish}
                                                className="w-full mt-8 py-4 font-bold rounded-md transition-all shadow-md active:scale-95 cursor-pointer bg-green-600 text-white hover:bg-green-700">
                                                강의 공개하기
                                            </button>
                                        )}
                                        {course.status === 'OPEN' && (
                                            <button onClick={handleClose}
                                                className="w-full mt-8 py-4 font-bold rounded-md transition-all shadow-md active:scale-95 cursor-pointer bg-red-500 text-white hover:bg-red-600">
                                                모집 마감하기
                                            </button>
                                        )}
                                        {course.status === 'CLOSED' && (
                                            <button disabled
                                                className="w-full mt-8 py-4 font-bold rounded-md transition-all shadow-md cursor-not-allowed bg-gray-200 text-gray-500">
                                                마감된 강의입니다
                                            </button>
                                        )}
                                    </>
                                ) : (
                                    <>
                                        {course.enrolled ? (
                                            <button disabled
                                                className="w-full mt-8 py-4 font-bold rounded-md transition-all shadow-md cursor-not-allowed bg-gray-200 text-gray-500">
                                                이미 신청한 강의입니다
                                            </button>
                                        ) : (
                                            <button
                                                onClick={course.status === 'OPEN' ? handleEnroll : undefined}
                                                className={`w-full mt-8 py-4 font-bold rounded-md transition-all shadow-md active:scale-95 cursor-pointer ${course.status === 'OPEN'
                                                        ? 'bg-blue-600 text-white hover:bg-blue-700'
                                                        : 'bg-gray-200 text-gray-500 cursor-not-allowed'
                                                    }`}
                                                disabled={course.status !== 'OPEN'}
                                            >
                                                {course.status === 'OPEN' ? '수강 신청하기' : '지금은 신청할 수 없습니다'}
                                            </button>
                                        )}
                                    </>
                                )}
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default CourseDetailPage;