import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Header = () => {
    /* 페이지 이동을 위한 navigate 함수 */
    const navigate = useNavigate();
    /* 인증 정보에서 현재 사용자 정보와 로그아웃 함수 추출 */
    const { user, logout } = useAuth();

    /* 역할에 따른 라벨 */
    const roleLabel = (role) => role === 'CREATOR' ? '강사' : role === 'ADMIN' ? '관리자' : '수강생';

    /* 로그아웃 처리 */
    const handleLogout = () => {
        logout();
        navigate('/signup');
    };

    return (
        <header className="sticky top-0 z-50 bg-white border-b border-gray-200 shadow-sm">
            <div className="max-w-7xl mx-auto px-6 h-14 flex items-center justify-between">
                <span
                    onClick={() => navigate('/courses')}
                    className="font-extrabold text-gray-900 cursor-pointer hover:text-blue-600 transition-colors"
                >
                    강의 플랫폼
                </span>

                <div className="flex items-center gap-4">
                    {user ? (
                        <>
                            <div className="flex items-center gap-2 text-sm text-gray-600">
                                <span className="font-medium">{user.name}</span>
                                <span className="text-xs px-2 py-0.5 rounded-full bg-gray-100 text-gray-500">
                                    {roleLabel(user.role)}
                                </span>
                            </div>
                            <button
                                onClick={() => navigate('/my-page')}
                                className="text-sm px-3 py-1.5 rounded-md border border-gray-300 text-gray-600 hover:bg-gray-50 transition-colors cursor-pointer"
                            >
                                마이페이지
                            </button>
                            <button
                                onClick={handleLogout}
                                className="text-sm px-3 py-1.5 rounded-md border border-gray-300 text-gray-600 hover:bg-gray-50 transition-colors cursor-pointer"
                            >
                                로그아웃
                            </button>
                        </>
                    ) : (
                        <button
                            onClick={() => navigate('/signup')}
                            className="text-sm px-3 py-1.5 rounded-md bg-blue-600 text-white hover:bg-blue-700 transition-colors cursor-pointer"
                        >
                            회원가입
                        </button>
                    )}
                </div>
            </div>
        </header>
    );
};

export default Header;
