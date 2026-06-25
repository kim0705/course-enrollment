import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

/* 테스트 계정 목록 */
const TEST_ACCOUNTS = [
    { label: '관리자', username: 'admin',     password: 'Test1234!' },
    { label: '수강생', username: 'student_a', password: 'Test1234!' },
    { label: '강사',   username: 'creator_a', password: 'Test1234!' },
];

const LoginPage = () => {
    /* 페이지 이동을 위한 네비게이트 함수 */
    const navigate = useNavigate();
    /* 현재 위치 정보 */
    const location = useLocation();
    /* 인증 관련 함수 */
    const { login } = useAuth();
    /* 입력 폼 상태 */
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    /* 에러 메시지 */
    const [error, setError] = useState('');

    /* 로그인 후 리다이렉트할 경로 */
    const redirect = location.state?.redirect || '/courses';

    /* 로그인 처리 */
    const handleLogin = async (e) => {
        e.preventDefault();
        setError('');

        try {
            await login(username, password);
            navigate(redirect, { replace: true });
        } catch (err) {
            setError(err.response?.data?.message || '아이디 또는 비밀번호가 올바르지 않습니다.');
        }
    };

    /* 테스트 계정 빠른 로그인 */
    const handleQuickLogin = async (testUsername, testPassword) => {
        setError('');

        try {
            await login(testUsername, testPassword);
            navigate(redirect, { replace: true });
        } catch (err) {
            setError(err.response?.data?.message || '로그인에 실패했습니다.');
        }
    };

    return (
        <div className="min-h-screen bg-gray-50 flex items-center justify-center p-6">
            <div className="w-full max-w-md">
                <div className="text-center mb-8">
                    <h1 className="text-3xl font-extrabold text-gray-900 mb-2">로그인</h1>
                </div>

                <form onSubmit={handleLogin} className="bg-white rounded-xl border border-gray-200 shadow-sm p-8 flex flex-col gap-5">
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1.5">아이디</label>
                        <input
                            type="text"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            placeholder="아이디를 입력하세요"
                            className="w-full border border-gray-300 rounded-lg px-3 py-2.5 text-sm text-gray-800 focus:outline-none focus:ring-2 focus:ring-blue-500"
                            required
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1.5">비밀번호</label>
                        <input
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            placeholder="비밀번호를 입력하세요"
                            className="w-full border border-gray-300 rounded-lg px-3 py-2.5 text-sm text-gray-800 focus:outline-none focus:ring-2 focus:ring-blue-500"
                            required
                        />
                    </div>

                    {error && <p className="text-sm text-red-500">{error}</p>}

                    <button
                        type="submit"
                        className="w-full py-2.5 bg-blue-600 text-white text-sm font-semibold rounded-lg hover:bg-blue-700 transition-colors cursor-pointer mt-1"
                    >
                        로그인
                    </button>
                </form>

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

                {/* 테스트 계정 빠른 로그인 */}
                <div className="mt-8 border border-dashed border-gray-200 rounded-xl p-5">
                    <p className="text-xs font-semibold text-gray-400 text-center mb-3">테스트 계정으로 바로 로그인</p>
                    <div className="flex gap-2">
                        {TEST_ACCOUNTS.map(account => (
                            <button
                                key={account.username}
                                onClick={() => handleQuickLogin(account.username, account.password)}
                                className="flex-1 py-2 text-sm font-medium text-gray-600 bg-gray-50 border border-gray-200 rounded-lg hover:bg-gray-100 transition-colors cursor-pointer"
                            >
                                {account.label}
                            </button>
                        ))}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default LoginPage;
