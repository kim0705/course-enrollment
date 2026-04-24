import { createContext, useContext, useState } from 'react';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
    /* localStorage에서 초기 유저 상태 복원 */
    const [user, setUser] = useState(() => {
        const saved = localStorage.getItem('user');
        return saved ? JSON.parse(saved) : null;
    });

    /* 로그인: 선택한 유저를 전역 상태 및 localStorage에 저장 */
    const login = (selectedUser) => {
        localStorage.setItem('user', JSON.stringify(selectedUser));
        setUser(selectedUser);
    };

    /* 로그아웃: 전역 상태 및 localStorage 초기화 */
    const logout = () => {
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
