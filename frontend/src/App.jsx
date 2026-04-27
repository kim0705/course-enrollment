import { Navigate, Route, Routes } from 'react-router-dom'
import { AuthProvider, useAuth } from './context/AuthContext'
import Layout from './components/Layout'
import LoginPage from './pages/LoginPage'
import CourseRegisterPage from './pages/CourseRegisterPage'
import CourseListPage from './pages/CourseListPage'
import CourseDetailPage from './pages/CourseDetailPage'
import MyPage from './pages/MyPage'

/* 비회원도 접근 가능한 공개 라우트 */
const PublicRoute = ({ children }) => <Layout>{children}</Layout>;

/* 인증 여부에 따라 라우트 보호 및 공통 레이아웃 적용 */
const PrivateRoute = ({ children }) => {
    const { user } = useAuth();

    return user ? <Layout>{children}</Layout> : <Navigate to="/login" replace />;
};

/* CREATOR 권한 전용 라우트 */
const CreatorRoute = ({ children }) => {
    const { user } = useAuth();

    if (!user) return <Navigate to="/login" replace />;
    if (user.role !== 'CREATOR') return <Navigate to="/courses" replace />;

    return <Layout>{children}</Layout>;
};

function App() {
    return (
        <AuthProvider>
            <Routes>
                <Route path="/login" element={<LoginPage />} />
                <Route path="/courses" element={<PublicRoute><CourseListPage /></PublicRoute>} />
                <Route path="/courses/:courseId" element={<PublicRoute><CourseDetailPage /></PublicRoute>} />
                <Route path="/courses/new" element={<CreatorRoute><CourseRegisterPage /></CreatorRoute>} />
                <Route path="/courses/:courseId/edit" element={<CreatorRoute><CourseRegisterPage /></CreatorRoute>} />
                <Route path="/my-page" element={<PrivateRoute><MyPage /></PrivateRoute>} />
                <Route path="*" element={<Navigate to="/courses" replace />} />
            </Routes>
        </AuthProvider>
    )
}

export default App
