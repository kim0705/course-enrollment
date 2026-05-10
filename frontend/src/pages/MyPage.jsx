import { useState } from 'react';
import { useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import EnrollmentTab from './mypage/EnrollmentTab';
import PaymentTab from './mypage/PaymentTab';
import MyCourseTab from './mypage/MyCourseTab';
import StudentTab from './mypage/StudentTab';

/* 마이페이지 */
const MyPage = () => {
    /* 라우터 location에서 탭 정보 가져오기 */
    const location = useLocation();
    /* 인증 정보 */
    const { user } = useAuth();
    /* 현재 활성화된 탭 상태 */
    const [activeTab, setActiveTab] = useState(location.state?.tab || 'enrollments');

    /* 탭 목록 - CREATOR는 추가 탭 노출 */
    const tabs = [
        { key: 'enrollments', label: '나의 수강목록' },
        { key: 'payments', label: '결제 내역' },
        ...(user?.role === 'CREATOR' ? [
            { key: 'my-courses', label: '내 강의' },
            { key: 'students', label: '강의별 수강생 목록' },
        ] : []),
    ];

    return (
        <div className="max-w-4xl mx-auto mt-10 p-6">
            <h1 className="text-3xl font-extrabold text-gray-900 mb-8">마이페이지</h1>

            {/* 탭 버튼들 */}
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

            {/* 탭 콘텐츠 */}
            {activeTab === 'enrollments' && <EnrollmentTab />}
            {activeTab === 'payments' && <PaymentTab />}
            {activeTab === 'my-courses' && <MyCourseTab />}
            {activeTab === 'students' && <StudentTab />}
        </div>
    );
};

export default MyPage;
