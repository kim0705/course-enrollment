import { createContext, useContext, useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { login as loginApi, logout as logoutApi } from '../api/auth';
import instance, { plainAxios } from '../api/axios';

/* 인증 컨텍스트 */
const AuthContext = createContext(null);

/* StrictMode 이중 실행 방지 - useRef는 remount 시 초기화되므로 모듈 레벨 변수 사용 */
let didAuthInit = false;

/* 토큰 갱신 상태 플래그 */
let isRefreshing = false;
/* 토큰 갱신 대기 중인 요청 큐 */
let failedQueue = [];

/* 토큰 갱신 완료 후 대기 중인 요청 처리 */
const processQueue = (error) => {
    failedQueue.forEach(({ resolve, reject }) => error ? reject(error) : resolve());
    failedQueue = [];
};

/* 사용자 정보 저장 및 상태 업데이트 헬퍼 */
const saveUser = (userInfo, setUser) => {
    localStorage.setItem('user', JSON.stringify(userInfo));
    setUser(userInfo);
};

/* 사용자 정보 제거 및 상태 초기화 헬퍼 */
const clearUser = (setUser) => {
    localStorage.removeItem('user');
    setUser(null);
};

export const AuthProvider = ({ children }) => {
    /* 네비게이션 훅 */
    const navigate = useNavigate();
    /* 현재 로그인한 사용자 정보 상태 */
    const [user, setUser] = useState(null);
    /* 앱 초기 구동 시 서버 인증 상태 확인 완료 여부 */
    const [isInitializing, setIsInitializing] = useState(true);

    /* 응답 인터셉터: 401 Unauthorized 응답 시 자동으로 토큰 갱신 시도
     * - refresh/login 요청 자체인 경우는 그대로 reject
     * - refresh 진행 중이면 대기 큐에 추가 후 완료 시 재시도
     * - refresh 성공 시 원래 요청 재시도
     * - refresh 실패 시 로컬 스토리지에서 사용자 정보 제거 후 로그인 페이지로 리다이렉트 */
    useEffect(() => {
        const id = instance.interceptors.response.use(
            res => res,
            async error => {
                const original = error.config;

                if (
                    error.response?.status !== 401 ||
                    original._retry ||
                    original.url === '/api/auth/refresh' ||
                    original.url === '/api/auth/login'
                ) {
                    return Promise.reject(error);
                }

                /* refresh 진행 중이면 큐에 대기 */
                if (isRefreshing) {
                    original._retry = true;
                    return new Promise((resolve, reject) => {
                        failedQueue.push({ resolve, reject });
                    }).then(() => instance(original))
                      .catch(err => Promise.reject(err));
                }

                original._retry = true;
                isRefreshing = true;

                try {
                    await instance.post('/api/auth/refresh');
                    processQueue(null);
                    return instance(original);
                } catch (err) {
                    processQueue(err);
                    clearUser(setUser);
                    navigate('/login', { replace: true });
                    return Promise.reject(err);
                } finally {
                    isRefreshing = false;
                }
            }
        );

        return () => instance.interceptors.response.eject(id);
    }, [navigate]);

    /* 앱 초기 구동 시 서버 인증 상태 확인
     * - localStorage에 사용자 정보가 없으면 바로 초기화 완료 처리
     * - 있으면 /api/auth/me로 인증 상태 확인 시도
     *   - 401 응답 시 토큰 갱신 시도 후 재확인
     *   - 성공 시 사용자 정보 상태 및 localStorage에 저장
     *   - 실패 시 사용자 정보 제거 */
    useEffect(() => {
        if (didAuthInit) return;

        didAuthInit = true;

        if (!localStorage.getItem('user')) {
            setIsInitializing(false);
            return;
        }

        const init = async () => {
            localStorage.removeItem('user');

            try {
                let res = await plainAxios.get('/api/auth/me', {
                    headers: { 'Cache-Control': 'no-cache' },
                });

                if (res.status === 401) {
                    const refreshRes = await plainAxios.post('/api/auth/refresh');

                    if (refreshRes.status === 200) {
                        res = await plainAxios.get('/api/auth/me', {
                            headers: { 'Cache-Control': 'no-cache' },
                        });
                    }
                }

                if (res.status === 200) {
                    saveUser(res.data.data, setUser);
                }
            } catch {
                /* network error — user stays null */
            } finally {
                setIsInitializing(false);
            }
        };

        init();
    }, []);

    /* 로그인: API 호출 후 응답에서 사용자 정보 저장 */
    const login = async (username, password) => {
        const res = await loginApi({ username, password });
        saveUser(res.data, setUser);
    };

    /* 로그아웃: API 호출 후 상태 및 localStorage 초기화 */
    const logout = async () => {
        await logoutApi();
        clearUser(setUser);
    };

    /* 프로필 수정 후 사용자 정보 갱신 */
    const updateUser = (newUserInfo) => {
        saveUser(newUserInfo, setUser);
    };

    return (
        <AuthContext.Provider value={{ user, isInitializing, login, logout, updateUser }}>
            {children}
        </AuthContext.Provider>
    );
};

/* 인증 컨텍스트 훅 */
export const useAuth = () => useContext(AuthContext);
