import { createContext, useContext, useState } from 'react';
import { login as loginApi, logout as logoutApi } from '../api/auth';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
    /* localStorage에서 초기 유저 상태 복원 */
    const [user, setUser] = useState(() => {
        const saved = localStorage.getItem('user');
        return saved ? JSON.parse(saved) : null;
    });

    /* 로그인: API 호출 후 응답에서 사용자 정보 저장 */
    const login = async (username, password) => {
        const res = await loginApi({ username, password });
        const userInfo = res.data;

        localStorage.setItem('user', JSON.stringify(userInfo));
        setUser(userInfo);
    };

    /* 로그아웃: API 호출 후 상태 및 localStorage 초기화 */
    const logout = async () => {
        await logoutApi();
        
        localStorage.removeItem('user');
        setUser(null);
    };

    return (
        <AuthContext.Provider value={{ user, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => useContext(AuthContext);
