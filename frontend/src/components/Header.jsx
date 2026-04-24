import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Header = () => {
    const navigate = useNavigate();
    const { user, logout } = useAuth();

    /* 로그아웃 처리 */
    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    const roleLabel = (role) => role === 'CREATOR' ? '강사' : '수강생';

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
                    <div className="flex items-center gap-2 text-sm text-gray-600">
                        <span className="font-medium">{user?.name}</span>
                        <span className="text-xs px-2 py-0.5 rounded-full bg-gray-100 text-gray-500">
                            {roleLabel(user?.role)}
                        </span>
                    </div>
                    <button
                        onClick={handleLogout}
                        className="text-sm px-3 py-1.5 rounded-md border border-gray-300 text-gray-600 hover:bg-gray-50 transition-colors cursor-pointer"
                    >
                        로그아웃
                    </button>
                </div>
            </div>
        </header>
    );
};

export default Header;
