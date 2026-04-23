import { Route, Routes } from 'react-router-dom'
import CourseRegisterPage from './pages/CourseRegisterPage'

function App() {

    return (
        <Routes>
            <Route path="/courses/new" element={<CourseRegisterPage />} />
        </Routes>
    )
}

export default App
