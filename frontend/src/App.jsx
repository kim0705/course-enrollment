import { Navigate, Route, Routes } from 'react-router-dom'
import { AuthProvider, useAuth } from './context/AuthContext'
import Layout from './components/Layout'
import LoginPage from './pages/LoginPage'
import CourseRegisterPage from './pages/CourseRegisterPage'
import CourseListPage from './pages/CourseListPage'
import CourseDetailPage from './pages/CourseDetailPage'

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
                <Route path="/courses" element={<PrivateRoute><CourseListPage /></PrivateRoute>} />
                <Route path="/courses/:courseId" element={<PrivateRoute><CourseDetailPage /></PrivateRoute>} />
                <Route path="/courses/new" element={<CreatorRoute><CourseRegisterPage /></CreatorRoute>} />
                <Route path="/courses/:courseId/edit" element={<CreatorRoute><CourseRegisterPage /></CreatorRoute>} />
                <Route path="*" element={<Navigate to="/courses" replace />} />
            </Routes>
        </AuthProvider>
    )
}

export default App
