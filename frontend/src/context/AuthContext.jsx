import { createContext, useContext, useState, useEffect, useRef } from 'react';
import { login as loginApi, logout as logoutApi } from '../api/auth';
import { plainAxios } from '../api/axios';

/* 인증 컨텍스트 */
const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
    /* 현재 로그인한 사용자 정보 상태 */
    const [user, setUser] = useState(null);
    /* 앱 초기 구동 시 서버 인증 상태 확인 완료 여부 */
    const [isInitializing, setIsInitializing] = useState(true);
    /* StrictMode 이중 실행 방지 — refreshToken rotation 충돌 방어 */
    const initRef = useRef(false);

    /* 앱 마운트 시 서버에서 토큰 유효성 및 사용자 정보 검증
     * validateStatus로 인터셉터 개입 없이 직접 401/200 처리 */
    useEffect(() => {
        if (initRef.current) return;

        initRef.current = true;

        if (!localStorage.getItem('user')) {
            setIsInitializing(false);
            return;
        }

        const init = async () => {
            try {
                let res = await plainAxios.get('/api/auth/me');

                if (res.status === 401) {
                    const refreshRes = await plainAxios.post('/api/auth/refresh');

                    if (refreshRes.status !== 200) throw new Error('refresh failed');
                    
                    res = await plainAxios.get('/api/auth/me');
                }

                if (res.status === 200) {
                    setUser(res.data.data);
                    localStorage.setItem('user', JSON.stringify(res.data.data));
                } else {
                    throw new Error('me failed');
                }
            } catch {
                setUser(null);
                localStorage.removeItem('user');
            } finally {
                setIsInitializing(false);
            }
        };

        init();
    }, []);

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
        <AuthContext.Provider value={{ user, isInitializing, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => useContext(AuthContext);
