import axios from 'axios';

/* Axios 인스턴스 설정
 * - baseURL: API 서버 주소
 * - withCredentials: 쿠키 자동 포함
 * - headers: JSON 요청 기본값 */
const instance = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
    withCredentials: true,
    headers: {
        'Content-Type': 'application/json',
    },
});

/* 인터셉터 없는 인스턴스 — 앱 초기화 시 인증 상태 확인 전용 */
export const plainAxios = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
    withCredentials: true,
    headers: { 'Content-Type': 'application/json' },
    validateStatus: s => s < 500,
});

export default instance;
