import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useUserList } from '../hooks/useUserList';

const LoginPage = () => {
    /* 페이지 이동을 위한 네비게이트 함수 */
    const navigate = useNavigate();
    /* 현재 위치 정보 */
    const location = useLocation();
    /* 인증 관련 함수 */
    const { login } = useAuth();
    /* 유저 목록 조회 */
    const { data: users = [] } = useUserList();

    /* 로그인 후 리다이렉트할 경로 */
    const redirect = location.state?.redirect || '/courses';
    /* 역할에 따른 라벨 */
    const roleLabel = (role) => role === 'CREATOR' ? '강사' : '수강생';
    /* 역할에 따른 스타일 */
    const roleStyle = (role) => role === 'CREATOR' ? 'bg-blue-100 text-blue-700' : 'bg-green-100 text-green-700';

    /* 유저 선택 시 로그인 처리 */
    const handleSelectUser = (user) => {
        login(user);
        navigate(redirect, { replace: true });
    };

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

                <button
                    onClick={() => navigate('/courses')}
                    className="w-full mt-4 text-sm text-gray-400 hover:text-gray-600 transition-colors cursor-pointer py-2"
                >
                    비회원으로 계속 보기
                </button>

                <p className="text-center mt-2 text-sm text-gray-500">
                    계정이 없으신가요?{' '}
                    <Link to="/signup" className="text-blue-600 font-medium hover:underline">회원가입</Link>
                </p>
            </div>
        </div>
    );
};

export default LoginPage;
