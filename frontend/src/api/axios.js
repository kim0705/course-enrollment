import axios from 'axios';

const instance = axios.create({
    baseURL: 'http://localhost:8080',
    headers: {
        'Content-Type': 'application/json',
    },
});

/* 요청 인터셉터: localStorage에서 유저 정보를 읽어 X-User-Id 헤더 자동 추가 */
instance.interceptors.request.use(config => {
    const user = localStorage.getItem('user');

    if (user) {
        config.headers['X-User-Id'] = JSON.parse(user).id;
    }
    
    return config;
});

export default instance;
