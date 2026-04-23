import { useEffect, useState } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import { getCourseDetail } from '../api/course';

const CourseDetailPage = () => {
    const { courseId } = useParams();
    const navigate = useNavigate();
    const location = useLocation();

    const [course, setCourse] = useState(null);

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

    if (!course) return <div className="text-center py-20 text-gray-400">로딩 중...</div>;

    return (
        <div className="max-w-7xl mx-auto mt-10 p-6">
            {/* 상단 헤더 섹션 */}
            <div className="flex justify-end mb-8 border-b pb-6">
                <button onClick={handleBackToList}
                    className="px-4 py-2 border border-gray-300 text-gray-600 rounded-md font-semibold hover:bg-gray-50 cursor-pointer transition-all shadow-sm text-sm"
                >
                    ← 목록으로
                </button>
            </div>

            {/* 제목 및 강사 섹션 */}
            <div className="mb-10">
                <div className="flex items-center gap-3 mb-6">
                    <h1 className="text-4xl font-extrabold text-gray-900 tracking-tight leading-tight">
                        {course.title}
                    </h1>
                    <span className={`text-xs font-bold px-3 py-1.5 rounded-md border ${course.status === 'OPEN'
                        ? 'bg-blue-50 text-blue-600 border-blue-100'
                        : 'bg-gray-50 text-gray-500 border-gray-200'
                        }`}>
                        {course.status === 'OPEN' ? '모집 중' : course.status === 'DRAFT' ? '준비 중' : '마감'}
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

                                <button
                                    className={`w-full mt-8 py-4 font-bold rounded-md transition-all shadow-md active:scale-95 cursor-pointer ${course.status === 'OPEN'
                                        ? 'bg-blue-600 text-white hover:bg-blue-700'
                                        : 'bg-gray-200 text-gray-500 cursor-not-allowed'
                                        }`}
                                    disabled={course.status !== 'OPEN'}
                                >
                                    {course.status === 'OPEN' ? '수강 신청하기' : '지금은 신청할 수 없습니다'}
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default CourseDetailPage;