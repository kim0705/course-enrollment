import { Route, Routes } from 'react-router-dom'
import CourseRegisterPage from './pages/CourseRegisterPage'
import CourseListPage from './pages/CourseListPage'
import CourseDetailPage from './pages/CourseDetailPage'

function App() {

    return (
        <Routes>
            <Route path="/courses" element={<CourseListPage />} />
            <Route path="/courses/:courseId" element={<CourseDetailPage />} />
            <Route path="/courses/new" element={<CourseRegisterPage />} />
        </Routes>
    )
}

export default App
