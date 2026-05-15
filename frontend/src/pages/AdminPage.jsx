import { useState } from 'react';
import CreatorRequestTab from './admin/CreatorRequestTab';

/* 관리자 페이지 */
const AdminPage = () => {
    /* 현재 활성화된 탭 상태 */
    const [activeTab, setActiveTab] = useState('creator-requests');

    /* 탭 목록 */
    const tabs = [
        { key: 'creator-requests', label: '강사 신청 관리' },
    ];

    return (
        <div className="max-w-4xl mx-auto mt-10 p-6">
            <h1 className="text-3xl font-extrabold text-gray-900 mb-8">관리자 페이지</h1>

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
            </div>

            {/* 탭 콘텐츠 */}
            {activeTab === 'creator-requests' && <CreatorRequestTab />}
        </div>
    );
};

export default AdminPage;
