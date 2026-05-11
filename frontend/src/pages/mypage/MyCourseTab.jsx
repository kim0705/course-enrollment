import { useNavigate } from 'react-router-dom';
import { COURSE_STATUS_BADGE_STYLE, COURSE_STATUS_LABEL } from '../../utils/statusConfig';
import { useMyCourses } from '../../hooks/useMyCourses';

/* 내 강의 목록 탭 (CREATOR 전용) */
const MyCourseTab = () => {
    /* 페이지 이동 함수 */
    const navigate = useNavigate();
    /* 내 강의 데이터 */
    const { data: myCourses = [], isLoading } = useMyCourses(true);

    if (isLoading) return <div className="text-center py-20 text-gray-400">로딩 중...</div>;

    /* 등록한 강의가 없는 경우 */
    if (myCourses.length === 0) {
        return (
            <div className="text-center text-gray-400 py-32 border-2 border-dashed border-gray-100 rounded-2xl">
                등록한 강의가 없습니다.
            </div>
        );
    }

    return (
        <div className="flex flex-col gap-4">
            {myCourses.map(course => (
                <div key={course.id}
                    onClick={() => navigate(`/courses/${course.id}`, { state: { from: 'my-page' } })}
                    className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm flex flex-col sm:flex-row sm:items-center justify-between gap-4 cursor-pointer hover:shadow-md transition-shadow">
                    <div className="flex-1 min-w-0">
                        {/* 강의 제목과 상태 배지 */}
                        <div className="flex items-center gap-2 mb-1">
                            <span className="text-base font-bold text-gray-900 truncate">{course.title}</span>
                            <span className={`text-xs font-bold px-2 py-0.5 rounded border ${COURSE_STATUS_BADGE_STYLE[course.status]}`}>
                                {COURSE_STATUS_LABEL[course.status]}
                            </span>
                        </div>
                        
                        {/* 강의 정보 */}
                        <div className="flex flex-wrap items-center gap-3 text-xs text-gray-500 mt-1">
                            <span>{course.price === 0 ? '무료' : `${course.price.toLocaleString()}원`}</span>
                            <span className="text-gray-300">|</span>
                            <span>수강 {course.enrolledCount} / {course.capacity}명</span>
                            <span className="text-gray-300">|</span>
                            <span>{course.startDate} ~ {course.endDate}</span>
                        </div>
                    </div>
                </div>
            ))}
        </div>
    );
};

export default MyCourseTab;
