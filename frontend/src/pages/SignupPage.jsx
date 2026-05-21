import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { signup, checkUsername, checkEmail } from '../api/auth';

/* 필드별 유효성 검사 함수 */
const validateField = (name, value) => {
    switch (name) {
        case 'username':
            if (!value) return '아이디를 입력해주세요.';
            if (value.length < 4 || value.length > 20) return '아이디는 4~20자여야 합니다.';
            if (!/^[a-zA-Z0-9_]+$/.test(value)) return '영문, 숫자, 밑줄(_)만 사용 가능합니다.';
            return '';

        case 'name':
            if (!value) return '이름을 입력해주세요.';
            return '';

        case 'email':
            if (!value) return '이메일을 입력해주세요.';
            if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) return '올바른 이메일 형식이 아닙니다.';
            return '';

        case 'password':
            if (!value) return '비밀번호를 입력해주세요.';
            if (/\s/.test(value)) return '비밀번호에 공백을 사용할 수 없습니다.';
            if (value.length < 8 || value.length > 16) return '비밀번호는 8~16자여야 합니다.';
            if (!/(?=.*[a-zA-Z])(?=.*\d)(?=.*[!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?])/.test(value))
                return '영문, 숫자, 특수문자를 각각 최소 1개 이상 포함해야 합니다.';
            return '';

        default:
            return '';
    }
};

/* 회원가입 페이지 */
const SignupPage = () => {
    /* 페이지 이동을 위한 navigate 함수 */
    const navigate = useNavigate();
    /* 폼 상태 */
    const [form, setForm] = useState({ username: '', name: '', email: '', password: '' });
    /* 필드별 에러 메시지 */
    const [errors, setErrors] = useState({});
    /* 필드 방문 여부 (blur 발생 기준) */
    const [touched, setTouched] = useState({});
    /* 서버 중복 확인 중 여부 */
    const [checking, setChecking] = useState({ username: false, email: false });
    /* 전체 폼 유효성 여부 */
    const isFormValid =
        Object.keys(form).every(name => !validateField(name, form[name])) &&
        !errors.username && !errors.email &&
        !checking.username && !checking.email;

    /* 회원가입 뮤테이션 */
    const { mutate: signupMutate, isPending } = useMutation({
        mutationFn: signup,
        onSuccess: () => {
            alert('회원가입이 완료되었습니다.');
            navigate('/login');
        },
        onError: (err) => {
            alert(err.response?.data?.message || '회원가입에 실패했습니다.');
        },
    });

    /* 입력값 변경 핸들러 - 방문한 필드만 실시간 검사 */
    const handleChange = (e) => {
        const { name, value } = e.target;
        setForm(prev => ({ ...prev, [name]: value }));

        if (touched[name]) {
            setErrors(prev => ({ ...prev, [name]: validateField(name, value) }));
        }
    };

    /* 포커스 아웃 핸들러 - 앞뒤 공백 제거, 유효성 검사 시작, 중복 확인 */
    const handleBlur = async (e) => {
        const { name, value } = e.target;
        const trimmed = value.trim();

        setForm(prev => ({ ...prev, [name]: trimmed }));
        setTouched(prev => ({ ...prev, [name]: true }));

        const localError = validateField(name, trimmed);
        setErrors(prev => ({ ...prev, [name]: localError }));

        if (!localError && (name === 'username' || name === 'email')) {
            setChecking(prev => ({ ...prev, [name]: true }));
            try {
                const checkFn = name === 'username' ? checkUsername : checkEmail;
                const result = await checkFn(trimmed);
                if (!result.data.available) {
                    setErrors(prev => ({ ...prev, [name]: name === 'username' ? '이미 사용 중인 아이디입니다.' : '이미 사용 중인 이메일입니다.' }));
                }
            } catch {
                /* 서버 오류 시 무시 */
            } finally {
                setChecking(prev => ({ ...prev, [name]: false }));
            }
        }
    };

    /* 회원가입 요청 */
    const handleSubmit = (e) => {
        e.preventDefault();

        const trimmedForm = Object.fromEntries(
            Object.entries(form).map(([k, v]) => [k, v.trim()])
        );
        const allTouched = { username: true, name: true, email: true, password: true };
        const newErrors = Object.fromEntries(
            Object.keys(trimmedForm).map(name => [name, validateField(name, trimmedForm[name])])
        );

        setForm(trimmedForm);
        setTouched(allTouched);
        setErrors(newErrors);

        if (Object.values(newErrors).some(msg => msg)) return;

        signupMutate(trimmedForm);
    };

    return (
        <div className="min-h-screen bg-gray-50 flex items-center justify-center p-6">
            <div className="w-full max-w-md">
                <div className="text-center mb-8">
                    <h1 className="text-3xl font-extrabold text-gray-900 mb-2">회원가입</h1>
                </div>

                {/* 회원가입 폼 */}
                <form onSubmit={handleSubmit} noValidate className="bg-white rounded-xl border border-gray-200 shadow-sm p-6 space-y-4">

                    {/* 아이디 입력 필드 */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">아이디</label>
                        <input
                            type="text"
                            name="username"
                            value={form.username}
                            onChange={handleChange}
                            onBlur={handleBlur}
                            placeholder="4~20자, 영문/숫자/밑줄(_)"
                            required
                            className={`w-full border py-2.5 px-3 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 outline-none ${errors.username && touched.username ? 'border-red-400' : 'border-gray-300'}`}
                        />
                        {checking.username && <p className="mt-1 text-xs text-gray-400">확인 중...</p>}
                        {!checking.username && errors.username && touched.username && (
                            <p className="mt-1 text-xs text-red-500">{errors.username}</p>
                        )}
                    </div>

                    {/* 이름 입력 필드 */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">이름</label>
                        <input
                            type="text"
                            name="name"
                            value={form.name}
                            onChange={handleChange}
                            onBlur={handleBlur}
                            placeholder="이름을 입력하세요"
                            required
                            className={`w-full border py-2.5 px-3 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 outline-none ${errors.name && touched.name ? 'border-red-400' : 'border-gray-300'}`}
                        />
                        {errors.name && touched.name && (
                            <p className="mt-1 text-xs text-red-500">{errors.name}</p>
                        )}
                    </div>

                    {/* 이메일 입력 필드 */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">이메일</label>
                        <input
                            type="email"
                            name="email"
                            value={form.email}
                            onChange={handleChange}
                            onBlur={handleBlur}
                            placeholder="example@email.com"
                            required
                            className={`w-full border py-2.5 px-3 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 outline-none ${errors.email && touched.email ? 'border-red-400' : 'border-gray-300'}`}
                        />
                        {checking.email && <p className="mt-1 text-xs text-gray-400">확인 중...</p>}
                        {!checking.email && errors.email && touched.email && (
                            <p className="mt-1 text-xs text-red-500">{errors.email}</p>
                        )}
                    </div>

                    {/* 비밀번호 입력 필드 */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">비밀번호</label>
                        <input
                            type="password"
                            name="password"
                            value={form.password}
                            onChange={handleChange}
                            onBlur={handleBlur}
                            placeholder="영문+숫자+특수문자 8~16자"
                            required
                            className={`w-full border py-2.5 px-3 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 outline-none ${errors.password && touched.password ? 'border-red-400' : 'border-gray-300'}`}
                        />
                        {errors.password && touched.password && (
                            <p className="mt-1 text-xs text-red-500">{errors.password}</p>
                        )}
                    </div>

                    {/* 제출 버튼 - 처리 중이거나 폼이 유효하지 않으면 비활성화 */}
                    <button
                        type="submit"
                        disabled={isPending || !isFormValid}
                        className="w-full py-2.5 bg-blue-600 text-white text-sm font-semibold rounded-lg hover:bg-blue-700 transition-colors cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                        {isPending ? '처리 중...' : '가입하기'}
                    </button>
                </form>

                {/* 로그인 페이지로 이동하는 링크 */}
                <p className="text-center mt-4 text-sm text-gray-500">
                    이미 계정이 있으신가요?{' '}
                    <Link to="/login" className="text-blue-600 font-medium hover:underline">로그인</Link>
                </p>
            </div>
        </div>
    );
};

export default SignupPage;
