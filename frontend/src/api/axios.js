import axios from 'axios';

/* Axios 인스턴스 설정
 * - baseURL: API 서버 주소
 * - withCredentials: 쿠키 자동 포함
 * - headers: JSON 요청 기본값 */
const instance = axios.create({
    baseURL: 'http://localhost:8080',
    withCredentials: true,
    headers: {
        'Content-Type': 'application/json',
    },
});

/* 응답 인터셉터: 401 Unauthorized 응답 시 자동으로 토큰 갱신 시도
 * - refresh/login 요청 자체거나 이미 재시도한 경우는 그대로 reject
 * - refresh 성공 시 원래 요청 재시도
 * - refresh 실패 시 로컬 스토리지에서 사용자 정보 제거 후 로그인 페이지로 리다이렉트 */
instance.interceptors.response.use(
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

        original._retry = true;

        try {
            await instance.post('/api/auth/refresh');
            return instance(original);
        } catch {
            localStorage.removeItem('user');
            window.location.href = '/login';
            return Promise.reject(error);
        }
    }
);

/* 인터셉터 없는 인스턴스 — 앱 초기화 시 인증 상태 확인 전용 */
export const plainAxios = axios.create({
    baseURL: 'http://localhost:8080',
    withCredentials: true,
    headers: { 'Content-Type': 'application/json' },
    validateStatus: s => s < 500,
});

export default instance;
