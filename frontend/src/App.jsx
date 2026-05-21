import { Navigate, Route, Routes } from 'react-router-dom'
import { AuthProvider, useAuth } from './context/AuthContext'
import Layout from './components/Layout'
import LoginPage from './pages/LoginPage'
import SignupPage from './pages/SignupPage'
import CourseRegisterPage from './pages/CourseRegisterPage'
import CourseListPage from './pages/CourseListPage'
import CourseDetailPage from './pages/CourseDetailPage'
import MyPage from './pages/MyPage'
import AdminPage from './pages/AdminPage'
import PaymentSuccessPage from './pages/PaymentSuccessPage'
import PaymentFailPage from './pages/PaymentFailPage'

/* 비회원도 접근 가능한 공개 라우트 */
const PublicRoute = ({ children }) => <Layout>{children}</Layout>;

/* 비인증 전용 라우트 - 로그인 상태면 /courses로 리다이렉트 */
const GuestRoute = ({ children }) => {
    const { user } = useAuth();

    return user ? <Navigate to="/courses" replace /> : children;
};

/* 인증된 사용자 전용 라우트 */
const PrivateRoute = ({ children }) => {
    const { user } = useAuth();

    return user ? <Layout>{children}</Layout> : <Navigate to="/login" replace />;
};

/* STUDENT/CREATOR 전용 라우트 - ADMIN은 /admin으로 리다이렉트 */
const StudentRoute = ({ children }) => {
    const { user } = useAuth();

    if (!user) return <Navigate to="/login" replace />;
    if (user.role === 'ADMIN') return <Navigate to="/admin" replace />;

    return <Layout>{children}</Layout>;
};

/* CREATOR 권한 전용 라우트 */
const CreatorRoute = ({ children }) => {
    const { user } = useAuth();

    if (!user) return <Navigate to="/login" replace />;
    if (user.role !== 'CREATOR') return <Navigate to="/courses" replace />;

    return <Layout>{children}</Layout>;
};

/* ADMIN 권한 전용 라우트 */
const AdminRoute = ({ children }) => {
    const { user } = useAuth();

    if (!user) return <Navigate to="/login" replace />;
    if (user.role !== 'ADMIN') return <Navigate to="/my-page" replace />;

    return <Layout>{children}</Layout>;
};

/* 인증 초기화 완료 후 라우트 렌더링 */
function AppRoutes() {
    const { isInitializing } = useAuth();

    if (isInitializing) return null;

    return (
        <Routes>
            <Route path="/login" element={<GuestRoute><LoginPage /></GuestRoute>} />
            <Route path="/signup" element={<GuestRoute><SignupPage /></GuestRoute>} />
            <Route path="/courses" element={<PublicRoute><CourseListPage /></PublicRoute>} />
            <Route path="/courses/:courseId" element={<PublicRoute><CourseDetailPage /></PublicRoute>} />
            <Route path="/courses/new" element={<CreatorRoute><CourseRegisterPage /></CreatorRoute>} />
            <Route path="/courses/:courseId/edit" element={<CreatorRoute><CourseRegisterPage /></CreatorRoute>} />
            <Route path="/my-page" element={<StudentRoute><MyPage /></StudentRoute>} />
            <Route path="/admin" element={<AdminRoute><AdminPage /></AdminRoute>} />
            <Route path="/payment/success" element={<PrivateRoute><PaymentSuccessPage /></PrivateRoute>} />
            <Route path="/payment/fail" element={<PrivateRoute><PaymentFailPage /></PrivateRoute>} />
            <Route path="*" element={<Navigate to="/courses" replace />} />
        </Routes>
    );
}

function App() {
    return (
        <AuthProvider>
            <AppRoutes />
        </AuthProvider>
    )
}

export default App
