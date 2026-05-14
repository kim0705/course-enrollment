import { useState } from 'react';
import { useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { requestCreator } from '../api/creatorRequest';
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
    /* 강사 신청 폼 표시 상태 */
    const [showCreatorForm, setShowCreatorForm] = useState(false);
    /* 강사 신청 사유 */
    const [creatorReason, setCreatorReason] = useState('');

    /* 탭 목록 - CREATOR는 추가 탭 노출 */
    const tabs = [
        { key: 'enrollments', label: '나의 수강목록' },
        { key: 'payments', label: '결제 내역' },
        ...(user?.role === 'CREATOR' ? [
            { key: 'my-courses', label: '내 강의' },
            { key: 'students', label: '강의별 수강생 목록' },
        ] : []),
    ];

    /* 강사 신청 제출 */
    const handleCreatorRequest = async () => {
        if (!creatorReason.trim()) {
            alert('신청 사유를 입력해주세요.');
            return;
        }

        try {
            await requestCreator(creatorReason.trim());
            alert('강사 신청이 완료되었습니다. 관리자 승인 후 강사 계정으로 전환됩니다.');
            
            setShowCreatorForm(false);
            setCreatorReason('');
        } catch (err) {
            alert(err.response?.data?.message || '강사 신청에 실패했습니다.');
        }
    };

    return (
        <div className="max-w-4xl mx-auto mt-10 p-6">
            <h1 className="text-3xl font-extrabold text-gray-900 mb-8">마이페이지</h1>

            {/* 강사 신청 폼 */}
            {user?.role === 'STUDENT' && showCreatorForm && (
                <div className="bg-gray-50 rounded-xl border border-gray-200 p-5 mb-8">
                    <h2 className="text-sm font-semibold text-gray-800 mb-3">강사 신청</h2>
                    <textarea
                        value={creatorReason}
                        onChange={(e) => setCreatorReason(e.target.value)}
                        placeholder="강사 신청 사유를 입력해주세요."
                        rows={3}
                        className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm text-gray-800 focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
                    />
                    <div className="flex justify-end gap-2 mt-3">
                        <button
                            onClick={() => { setShowCreatorForm(false); setCreatorReason(''); }}
                            className="px-4 py-2 text-sm font-medium text-gray-600 border border-gray-300 rounded-lg hover:bg-gray-100 transition-colors cursor-pointer"
                        >
                            취소
                        </button>
                        <button
                            onClick={handleCreatorRequest}
                            className="px-4 py-2 text-sm font-semibold bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors cursor-pointer"
                        >
                            신청하기
                        </button>
                    </div>
                </div>
            )}

            {/* 탭 버튼들 */}
            <div className="flex items-center border-b border-gray-200 mb-8">
                <div className="flex gap-1 flex-1">
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
                {user?.role === 'STUDENT' && !showCreatorForm && (
                    <div className="flex items-center gap-3 mb-1">
                        <span className="text-xs text-gray-400">강의를 만들고 싶으신가요?</span>
                        <button
                            onClick={() => setShowCreatorForm(true)}
                            className="px-4 py-1.5 text-sm font-semibold bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition-colors cursor-pointer"
                        >
                            강사 신청
                        </button>
                    </div>
                )}
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
