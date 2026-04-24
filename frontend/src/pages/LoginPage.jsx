import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getUserList } from '../api/user';
import { useAuth } from '../context/AuthContext';

const LoginPage = () => {
    const navigate = useNavigate();
    const { login } = useAuth();

    /* 유저 목록 상태 */
    const [users, setUsers] = useState([]);

    /* 유저 목록 조회 */
    useEffect(() => {
        const fetchUserList = async () => {
            try {
                const result = await getUserList();
                setUsers(result.data);
            } catch (err) {
                console.error(err);
            }
        };

        fetchUserList();
    }, []);

    /* 유저 선택 시 로그인 처리 */
    const handleSelectUser = (user) => {
        login(user);
        navigate('/courses');
    };

    const roleLabel = (role) => role === 'CREATOR' ? '강사' : '수강생';
    const roleStyle = (role) => role === 'CREATOR'
        ? 'bg-blue-100 text-blue-700'
        : 'bg-green-100 text-green-700';

    return (
        <div className="min-h-screen bg-gray-50 flex items-center justify-center p-6">
            <div className="w-full max-w-md">
                <div className="text-center mb-8">
                    <h1 className="text-3xl font-extrabold text-gray-900 mb-2">로그인</h1>
                    <p className="text-sm text-gray-500">테스트할 계정을 선택하세요</p>
                </div>

                <div className="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden">
                    {users.map((user, index) => (
                        <button
                            key={user.id}
                            onClick={() => handleSelectUser(user)}
                            className={`w-full flex items-center justify-between px-5 py-4 hover:bg-gray-50 transition-colors cursor-pointer text-left ${index !== 0 ? 'border-t border-gray-100' : ''}`}
                        >
                            <div className="flex items-center gap-3">
                                <div className="w-9 h-9 rounded-full bg-gray-100 flex items-center justify-center text-sm font-bold text-gray-500">
                                    {user.name.charAt(0)}
                                </div>
                                <span className="font-medium text-gray-800">{user.name}</span>
                            </div>
                            <span className={`text-xs font-semibold px-2 py-1 rounded-full ${roleStyle(user.role)}`}>
                                {roleLabel(user.role)}
                            </span>
                        </button>
                    ))}
                </div>
            </div>
        </div>
    );
};

export default LoginPage;
