import { useState } from 'react';
import CreatorRequestTab from './admin/CreatorRequestTab';
import { updateAdminPassword } from '../api/admin';

/* 관리자 페이지 */
const AdminPage = () => {
    /* 현재 활성화된 탭 상태 */
    const [activeTab, setActiveTab] = useState('creator-requests');
    /* 비밀번호 변경 모달 표시 상태 */
    const [showPasswordModal, setShowPasswordModal] = useState(false);
    /* 비밀번호 폼 */
    const [currentPassword, setCurrentPassword] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [passwordLoading, setPasswordLoading] = useState(false);

    /* 탭 목록 */
    const tabs = [
        { key: 'creator-requests', label: '강사 신청 관리' },
    ];

    /* 모달 닫기 */
    const handleCloseModal = () => {
        setShowPasswordModal(false);
        setCurrentPassword('');
        setNewPassword('');
    };

    /* 비밀번호 변경 */
    const handlePasswordSave = async () => {
        if (!currentPassword || !newPassword) {
            alert('비밀번호를 입력해주세요.');
            return;
        }

        if (!window.confirm('비밀번호를 변경하시겠습니까?')) return;

        setPasswordLoading(true);
        try {
            await updateAdminPassword(currentPassword, newPassword);
            alert('비밀번호가 변경되었습니다.');
            handleCloseModal();
        } catch (err) {
            alert(err.response?.data?.message || '비밀번호 변경에 실패했습니다.');
        } finally {
            setPasswordLoading(false);
        }
    };

    return (
        <div className="max-w-4xl mx-auto mt-10 p-6">
            <div className="flex items-center justify-between mb-8">
                <h1 className="text-3xl font-extrabold text-gray-900">관리자 페이지</h1>
                <button
                    onClick={() => setShowPasswordModal(true)}
                    className="px-4 py-2 text-sm font-semibold text-gray-600 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors cursor-pointer"
                >
                    비밀번호 변경
                </button>
            </div>

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

            {/* 비밀번호 변경 모달 */}
            {showPasswordModal && (
                <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
                    <div className="bg-white rounded-2xl shadow-xl w-full max-w-md p-6">
                        <h2 className="text-lg font-bold text-gray-900 mb-6">비밀번호 변경</h2>
                        <div className="space-y-4">
                            <div>
                                <label className="block text-xs font-semibold text-gray-500 mb-1">현재 비밀번호</label>
                                <input
                                    type="password"
                                    value={currentPassword}
                                    onChange={(e) => setCurrentPassword(e.target.value)}
                                    className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm text-gray-800 focus:outline-none focus:ring-2 focus:ring-blue-500"
                                />
                            </div>
                            <div>
                                <label className="block text-xs font-semibold text-gray-500 mb-1">새 비밀번호</label>
                                <input
                                    type="password"
                                    value={newPassword}
                                    onChange={(e) => setNewPassword(e.target.value)}
                                    placeholder="영문·숫자·특수문자 각 1개 이상, 8~16자"
                                    className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm text-gray-800 focus:outline-none focus:ring-2 focus:ring-blue-500 placeholder:text-gray-300"
                                />
                            </div>
                        </div>
                        <div className="flex justify-end gap-2 mt-6">
                            <button
                                onClick={handleCloseModal}
                                className="px-4 py-2 text-sm font-medium text-gray-600 border border-gray-300 rounded-lg hover:bg-gray-100 transition-colors cursor-pointer"
                            >
                                취소
                            </button>
                            <button
                                onClick={handlePasswordSave}
                                disabled={passwordLoading}
                                className="px-4 py-2 text-sm font-semibold bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors cursor-pointer disabled:opacity-50"
                            >
                                {passwordLoading ? '변경 중...' : '변경'}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default AdminPage;
