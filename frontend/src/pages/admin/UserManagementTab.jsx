import { useEffect, useState } from 'react';
import { getAdminUsers, updateUserRole } from '../../api/admin';

/* 역할 레이블 */
const ROLE_LABEL = { STUDENT: '수강생', CREATOR: '강사', ADMIN: '관리자' };
/* 역할별 배지 스타일 */
const ROLE_BADGE = {
    STUDENT: 'bg-blue-50 text-blue-700 border-blue-200',
    CREATOR: 'bg-purple-50 text-purple-700 border-purple-200',
    ADMIN: 'bg-red-50 text-red-700 border-red-200',
};

/* 관리자 사용자 관리 탭 */
const UserManagementTab = () => {
    /* 사용자 목록 상태 */
    const [users, setUsers] = useState([]);
    /* 로딩 상태 */
    const [isLoading, setIsLoading] = useState(true);

    /* 컴포넌트 마운트 시 사용자 목록 조회 */
    useEffect(() => {
        getAdminUsers()
            .then(res => setUsers(res.data))
            .catch(() => alert('사용자 목록 조회에 실패했습니다.'))
            .finally(() => setIsLoading(false));
    }, []);

    /* 역할 변경 */
    const handleRoleChange = async (userId, newRole) => {
        if (!window.confirm(`역할을 ${ROLE_LABEL[newRole]}(으)로 변경하시겠습니까?`)) return;

        try {
            await updateUserRole(userId, newRole);
            setUsers(prev => prev.map(u => u.id === userId ? { ...u, role: newRole } : u));
        } catch (err) {
            alert(err.response?.data?.message || '역할 변경에 실패했습니다.');
        }
    };

    if (isLoading) return <div className="text-center py-20 text-gray-400">로딩 중...</div>;

    if (users.length === 0) return (
        <div className="text-center text-gray-400 py-32 border-2 border-dashed border-gray-100 rounded-2xl">
            사용자가 없습니다.
        </div>
    );

    return (
        <div className="overflow-x-auto">
            <table className="w-full text-sm text-left">
                <thead>
                    <tr className="border-b border-gray-200 text-gray-500 text-xs uppercase">
                        <th className="pb-3 pr-6 font-semibold">아이디</th>
                        <th className="pb-3 pr-6 font-semibold">이름</th>
                        <th className="pb-3 pr-6 font-semibold">이메일</th>
                        <th className="pb-3 pr-6 font-semibold">역할</th>
                        <th className="pb-3 font-semibold">가입일</th>
                    </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                    {users.map(user => (
                        <tr key={user.id} className="hover:bg-gray-50">
                            <td className="py-4 pr-6 font-medium text-gray-900">{user.username}</td>
                            <td className="py-4 pr-6 text-gray-700">{user.name}</td>
                            <td className="py-4 pr-6 text-gray-500">{user.email}</td>
                            <td className="py-4 pr-6">
                                {user.role === 'ADMIN' ? (
                                    <span className={`text-xs font-bold px-2 py-0.5 rounded border ${ROLE_BADGE[user.role]}`}>
                                        {ROLE_LABEL[user.role]}
                                    </span>
                                ) : (
                                    <select
                                        value={user.role}
                                        onChange={(e) => handleRoleChange(user.id, e.target.value)}
                                        className="text-xs border border-gray-300 rounded px-2 py-1 text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500 cursor-pointer"
                                    >
                                        <option value="STUDENT">수강생</option>
                                        <option value="CREATOR">강사</option>
                                    </select>
                                )}
                            </td>
                            <td className="py-4 text-gray-500">{user.createdAt?.substring(0, 10)}</td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
};

export default UserManagementTab;
