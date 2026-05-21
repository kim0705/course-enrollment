import instance from './axios';

/* 로그인 */
export const login = (data) => {
    return instance.post('/api/auth/login', data).then(res => res.data);
};

/* 로그아웃 */
export const logout = () => {
    return instance.post('/api/auth/logout').then(res => res.data);
};

/* 회원가입 */
export const signup = (data) => {
    return instance.post('/api/auth/signup', data).then(res => res.data);
};

/* 아이디 중복 확인 */
export const checkUsername = (value) => {
    return instance.get('/api/auth/check-username', { params: { value } }).then(res => res.data);
};

/* 이메일 중복 확인 */
export const checkEmail = (value) => {
    return instance.get('/api/auth/check-email', { params: { value } }).then(res => res.data);
};
