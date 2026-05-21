import { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { updateProfile, updatePassword } from '../../api/user';

/* 프로필 수정 탭 (STUDENT / CREATOR 공통) */
const ProfileTab = () => {
    /* 인증 정보 및 업데이트 함수 */
    const { user, updateUser } = useAuth();

    /* 프로필 폼 */
    const [name, setName] = useState(user?.name || '');
    const [email, setEmail] = useState(user?.email || '');
    const [profileLoading, setProfileLoading] = useState(false);

    /* 비밀번호 폼 */
    const [currentPassword, setCurrentPassword] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [passwordLoading, setPasswordLoading] = useState(false);

    /* 프로필 저장 */
    const handleProfileSave = async () => {
        if (!name.trim() || !email.trim()) {
            alert('이름과 이메일을 입력해주세요.');
            return;
        }

        if (!window.confirm('프로필을 수정하시겠습니까?')) return;

        setProfileLoading(true);
        try {
            await updateProfile(name.trim(), email.trim());
            updateUser({ ...user, name: name.trim(), email: email.trim() });
            alert('프로필이 수정되었습니다.');
        } catch (err) {
            alert(err.response?.data?.message || '프로필 수정에 실패했습니다.');
        } finally {
            setProfileLoading(false);
        }
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
            await updatePassword(currentPassword, newPassword);
            alert('비밀번호가 변경되었습니다.');
            setCurrentPassword('');
            setNewPassword('');
        } catch (err) {
            alert(err.response?.data?.message || '비밀번호 변경에 실패했습니다.');
        } finally {
            setPasswordLoading(false);
        }
    };

    return (
        <div className="space-y-10 max-w-lg">
            {/* 프로필 수정 */}
            <div>
                <h2 className="text-base font-bold text-gray-800 mb-4">프로필 수정</h2>
                <div className="space-y-4">
                    {/* 아이디는 수정 불가 - 회색으로 표시 */}
                    <div>
                        <label className="block text-xs font-semibold text-gray-500 mb-1">아이디</label>
                        <input
                            type="text"
                            value={user?.username || ''}
                            disabled
                            className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm text-gray-400 bg-gray-50 cursor-not-allowed"
                        />
                    </div>
                    {/* 이름과 이메일은 수정 가능 */ }
                    <div>
                        <label className="block text-xs font-semibold text-gray-500 mb-1">이름</label>
                        <input
                            type="text"
                            value={name}
                            onChange={(e) => setName(e.target.value)}
                            className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm text-gray-800 focus:outline-none focus:ring-2 focus:ring-blue-500"
                        />
                    </div>
                    <div>
                        <label className="block text-xs font-semibold text-gray-500 mb-1">이메일</label>
                        <input
                            type="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm text-gray-800 focus:outline-none focus:ring-2 focus:ring-blue-500"
                        />
                    </div>
                    {/* 저장 버튼 */ }
                    <div className="flex justify-end">
                        <button
                            onClick={handleProfileSave}
                            disabled={profileLoading}
                            className="px-5 py-2 text-sm font-semibold bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors cursor-pointer disabled:opacity-50"
                        >
                            {profileLoading ? '저장 중...' : '저장'}
                        </button>
                    </div>
                </div>
            </div>

            <hr className="border-gray-100" />

            {/* 비밀번호 변경 */}
            <div>
                <h2 className="text-base font-bold text-gray-800 mb-4">비밀번호 변경</h2>
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
                    <div className="flex justify-end">
                        <button
                            onClick={handlePasswordSave}
                            disabled={passwordLoading}
                            className="px-5 py-2 text-sm font-semibold bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors cursor-pointer disabled:opacity-50"
                        >
                            {passwordLoading ? '변경 중...' : '변경'}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ProfileTab;
