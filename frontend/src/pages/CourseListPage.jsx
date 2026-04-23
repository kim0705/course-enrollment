import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getCourseList } from '../api/course';

const CourseListPage = () => {
    const navigate = useNavigate();

    /* 검색 조건 상태 */
    const [search, setSearch] = useState({
        status: '',
        searchType: '',
        keyword: '',
        page: 0,
        size: 12,
    });

    /* 목록 데이터 상태 */
    const [data, setData] = useState({
        content: [],
        totalCount: 0,
        totalPages: 0,
        last: false,
    });

    /* 강의 목록 조회 */
    const fetchCourseList = async () => {
        try {
            const result = await getCourseList(search);

            setData(result.data);
        } catch (err) {
            console.error(err);
        }
    };

    /* 페이지 사이즈 변경 핸들러 */
    const handleSizeChange = (e) => {
        const newSize = parseInt(e.target.value);

        setSearch(prev => ({
            ...prev,
            size: newSize,
            page: 0
        }));
    };

    useEffect(() => {
        fetchCourseList();
    }, [search.page, search.status, search.size]);

    /* 검색 입력값 변경 */
    const handleSearchChange = (e) => {
        const { name, value } = e.target;
        setSearch(prev => ({ ...prev, [name]: value }));
    };

    /* 검색 실행 */
    const handleSearch = (e) => {
        e.preventDefault();

        if (search.page === 0) {
            fetchCourseList();
        } else {
            setSearch(prev => ({ ...prev, page: 0 }));
        }
    };

    return (
        <div className="max-w-7xl mx-auto mt-10 p-6">
            <div className="flex justify-between items-center mb-8">
                <h1 className="text-3xl font-extrabold text-gray-900">전체 강의</h1>
                <button onClick={() => navigate('/courses/new')}
                    className="px-5 py-2 bg-blue-600 text-white rounded-md font-semibold hover:bg-blue-700 transition-all cursor-pointer shadow-sm">
                    강의 등록
                </button>
            </div>

            {/* 검색 섹션 */}
            <form onSubmit={handleSearch} className="flex flex-wrap gap-3 mb-10 bg-gray-50 p-4 rounded-xl border border-gray-100">
                <select name="status" onChange={handleSearchChange}
                    className="bg-white border border-gray-300 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
                    <option value="">전체 상태</option>
                    <option value="DRAFT">초안</option>
                    <option value="OPEN">모집 중</option>
                    <option value="CLOSED">마감</option>
                </select>
                <div className="flex flex-1 gap-2">
                    <select name="searchType" onChange={handleSearchChange}
                        className="bg-white border border-gray-300 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
                        <option value="">제목+내용</option>
                        <option value="title">제목</option>
                        <option value="creator">강사명</option>
                    </select>
                    <input name="keyword" placeholder="어떤 강의를 찾으시나요?" onChange={handleSearchChange}
                        className="bg-white border border-gray-300 rounded-md px-4 py-2 text-sm flex-1 focus:outline-none focus:ring-2 focus:ring-blue-500" />
                    <button type="submit"
                        className="px-6 py-2 bg-gray-800 text-white rounded-md text-sm font-bold hover:bg-black cursor-pointer transition-colors">
                        검색
                    </button>
                </div>
            </form>

            {/* 목록 갯수 선택 섹션 */}
            <div className="flex justify-between items-center mb-4 px-1">
                <div className="text-sm text-gray-500">
                    총 <span className="font-bold text-gray-900">{data.totalCount}</span>개의 강의
                </div>

                <div className="flex items-center gap-3">
                    <select
                        value={search.size}
                        onChange={handleSizeChange}
                        className="bg-transparent text-xs text-gray-600 border-none focus:ring-0 cursor-pointer"
                    >
                        <option value={12}>12개씩 보기</option>
                        <option value={24}>24개씩 보기</option>
                        <option value={48}>48개씩 보기</option>
                    </select>
                </div>
            </div>

            {/* 강의 목록 섹션 */}
            {data?.content?.length === 0 ? (
                <div className="text-center text-gray-400 py-32 border-2 border-dashed border-gray-100 rounded-2xl">
                    검색 결과와 일치하는 강의가 없습니다.
                </div>
            ) : (
                <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
                    {data?.content?.map(course => (
                        <div key={course.id} onClick={() => navigate(`/courses/${course.id}`)}
                            className="group flex flex-col bg-white border border-gray-200 rounded-lg overflow-hidden hover:shadow-xl transition-all duration-300 cursor-pointer">

                            {/* 카드 상단: 이미지 영역(썸네일 대신 제목 앞글자) */}
                            <div className="relative aspect-video bg-gray-100 flex items-center justify-center overflow-hidden">
                                <span className="text-gray-300 font-bold text-lg group-hover:scale-110 transition-transform">
                                    {course?.title?.substring(0, 2)}
                                </span>
                                {/* 상태 배지 */}
                                <div className="absolute top-2 left-2">
                                    <span className={`text-[10px] font-bold px-2 py-1 rounded-sm shadow-sm ${course.status === 'OPEN' ? 'bg-green-500 text-white' : 'bg-gray-500 text-white'
                                        }`}>
                                        {course.status === 'OPEN' ? '모집 중' : '준비 중'}
                                    </span>
                                </div>
                            </div>

                            {/* 카드 하단: 상세 정보 */}
                            <div className="p-4 flex flex-col flex-1">
                                <h2 className="text-sm font-bold text-gray-800 line-clamp-2 h-10 mb-1 group-hover:text-blue-600">
                                    {course?.title}
                                </h2>
                                <p className="text-xs text-gray-500 mb-3">{course.creatorName}</p>

                                <div className="mt-auto">
                                    {/* 가격 */}
                                    <p className="text-base font-extrabold text-gray-900">
                                        {course?.price === 0 ? '무료' : `${course.price.toLocaleString()}원`}
                                    </p>

                                    {/* 인원수 바 */}
                                    <div className="mt-2">
                                        <div className="flex justify-between text-[10px] text-gray-500 mb-1">
                                            <span>수강 신청 현황</span>
                                            <span>{course.enrolledCount}/{course.capacity}명</span>
                                        </div>
                                        <div className="w-full bg-gray-100 h-1 rounded-full overflow-hidden">
                                            <div
                                                className="bg-blue-500 h-full transition-all duration-500"
                                                style={{ width: `${(course.enrolledCount / course.capacity) * 100}%` }}
                                            />
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            )}

            {/* 페이징 섹션 */}
            {(data?.totalPages ?? 0) > 1 && (
                <div className="flex justify-center items-center gap-4 mt-12">
                    <button disabled={search.page === 0}
                        onClick={() => setSearch(prev => ({ ...prev, page: prev.page - 1 }))}
                        className="p-2 border rounded-full hover:bg-gray-50 disabled:opacity-30 cursor-pointer transition-colors">
                        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="15 19l-7-7 7-7" /></svg>
                    </button>
                    <span className="text-sm font-medium text-gray-700">
                        <span className="text-blue-600">{search.page + 1}</span> / {data?.totalPages}
                    </span>
                    <button disabled={data.last}
                        onClick={() => setSearch(prev => ({ ...prev, page: prev.page + 1 }))}
                        className="p-2 border rounded-full hover:bg-gray-50 disabled:opacity-30 cursor-pointer transition-colors">
                        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="9 5l7 7-7 7" /></svg>
                    </button>
                </div>
            )}
        </div>
    );
};

export default CourseListPage;