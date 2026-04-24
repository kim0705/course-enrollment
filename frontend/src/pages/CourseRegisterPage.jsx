import { useEffect, useState } from 'react';
import { registerCourse, getCourseDetail, updateCourse } from '../api/course';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

/* 강의 등록/수정 페이지 */
const CourseRegisterPage = () => {
    const { courseId } = useParams();
    const isEdit = !!courseId;
    const navigate = useNavigate();
    const location = useLocation();
    const { user } = useAuth();
    const previousSearch = location.state?.fromSearch || '';

    /* 오늘 날짜 */
    const today = new Date().toISOString().split('T')[0];

    /* 폼 상태 */
    const [form, setForm] = useState({
        title: '',
        description: '',
        price: 0,
        capacity: 1,
        startDate: '',
        endDate: '',
    });

    /* 수정 모드일 때 기존 데이터 불러오기 */
    useEffect(() => {

        if (isEdit) {
            const fetchCourseData = async () => {
                try {
                    const response = await getCourseDetail(courseId);
                    const data = response.data;
                    setForm({
                        ...data,
                        priceDisplay: data.price.toLocaleString()
                    });
                } catch (err) {
                    alert('강의 정보를 불러오는데 실패했습니다.');
                    navigate(`/courses${previousSearch}`);
                }
            };

            fetchCourseData();
        }
    }, [courseId, isEdit]);

    /* 입력값 변경 핸들러 */
    const handleChange = (e) => {
        const { name, value } = e.target;
        setForm(prev => ({ ...prev, [name]: value }));
    };

    /* 가격 입력 핸들러 */
    const handlePriceChange = (e) => {

        const raw = e.target.value.replace(/[^0-9]/g, '');

        if (raw === '') {
            setForm(prev => ({
                ...prev,
                price: 0,
                priceDisplay: ''
            }));
            return;
        }

        const numValue = parseInt(raw, 10);
        if (numValue < 0) return;

        setForm(prev => ({
            ...prev,
            price: Number(raw),
            priceDisplay: Number(raw).toLocaleString()
        }));
    };

    /* 강의 등록/수정 요청 */
    const handleSubmit = async (e) => {
        e.preventDefault();

        try {
            if (isEdit) {
                await updateCourse(courseId, user?.id, form);
                alert('강의 정보가 수정되었습니다!');
                navigate(`/courses/${courseId}`, { state: { fromSearch: previousSearch } });
            } else {
                const result = await registerCourse(user?.id, form);
                alert('강의가 등록되었습니다!');
                navigate(`/courses/${result.data.id}`, { state: { fromSearch: previousSearch } });
            }
        } catch (err) {
            alert(err.response?.data?.message || '저장에 실패했습니다.');
        }
    };

    /* 취소 버튼 핸들러 */
    const handleCancel = () => {
        if (isEdit) {
            navigate(`/courses/${courseId}`, { state: { fromSearch: previousSearch } });
        } else {
            navigate(`/courses${previousSearch}`);
        }
    };

    return (
        <div className="max-w-7xl mx-auto mt-10 p-6">
            {/* 헤더 섹션 */}
            <div className="flex justify-between items-center mb-8 border-b pb-4">
                <div className="flex flex-col justify-center">
                    <h1 className="text-3xl font-extrabold text-gray-900">
                        {isEdit ? '강의 수정' : '강의 등록'}
                    </h1>
                    <p className="text-gray-500 mt-5">강의의 상세 정보를 입력해주세요.</p>
                </div>

                <div className="flex items-center gap-3">
                    <button type="button" onClick={handleCancel}
                        className="px-5 py-2 border border-gray-300 text-gray-600 rounded-md font-semibold hover:bg-gray-50 cursor-pointer transition-all shadow-sm">
                        취소
                    </button>
                    <button form="course-form" type="submit"
                        className="px-5 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-md font-semibold cursor-pointer shadow-sm transition-all active:scale-95">
                        {isEdit ? '수정 사항 저장' : '강의 저장하기'}
                    </button>
                </div>
            </div>

            {/* 강의 정보 섹션 */}
            <form id="course-form" onSubmit={handleSubmit} className="grid grid-cols-1 md:grid-cols-3 gap-8 items-stretch">
                <div className="md:col-span-2">
                    <div className="bg-white p-6 rounded-xl shadow-sm border min-h-[640px] flex flex-col">
                        <h2 className="text-lg font-semibold mb-4 text-gray-700">강의 상세 정보</h2>
                        <div className="space-y-4 flex-1 flex flex-col">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">강의 제목</label>
                                <input name="title" value={form.title} required maxLength={100} placeholder="예: 초보자를 위한 Java Spring Boot 마스터" onChange={handleChange}
                                    className="w-full border-gray-300 border py-3 px-4 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none" />
                                <p className="text-right text-xs text-gray-400 mt-1">{form.title.length} / 100</p>
                            </div>

                            <div className="flex-1 flex flex-col">
                                <label className="block text-sm font-medium text-gray-700 mb-1">강의 설명</label>
                                <textarea name="description" value={form.description} rows="10" maxLength={3000} placeholder="강의 내용을 상세히 적어주세요." onChange={handleChange}
                                    className="w-full border-gray-300 border py-3 px-4 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none flex-1 resize-none" />
                                <p className="text-right text-xs text-gray-400 mt-1">{form.description.length} / 3000</p>
                            </div>
                        </div>
                    </div>
                </div>

                {/* 강의 가격/정원 섹션 */}
                <div className="space-y-6 h-full flex flex-col">
                    <div className="bg-gray-50 p-6 rounded-xl border border-dashed border-gray-300 flex-1">
                        <h2 className="text-lg font-semibold mb-4 text-gray-700">가격 및 정원</h2>
                        <div className="space-y-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">가격</label>
                                <div className="relative">
                                    <input name="price" type="text" required value={form.priceDisplay || ''} onChange={handlePriceChange}
                                        className="w-full border-gray-300 border py-3 px-4 rounded-lg text-right pr-10" />
                                    <span className="absolute right-3 top-3.5 text-gray-500">원</span>
                                </div>
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">수강 정원</label>
                                <input name="capacity" value={form.capacity} type="number" required min="1" onChange={handleChange}
                                    className="w-full border-gray-300 border py-3 px-4 rounded-lg" />
                            </div>
                        </div>
                    </div>

                    {/* 강의 일정 섹션 */}
                    <div className="bg-white p-6 rounded-xl shadow-sm border flex-1">
                        <h2 className="text-lg font-semibold mb-4 text-gray-700">강의 일정</h2>
                        <div className="space-y-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">시작일</label>
                                <input name="startDate" value={form.startDate} type="date" required min={today} onChange={handleChange} className="w-full border-gray-300 border py-3 px-4 rounded-lg" />
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">종료일</label>
                                <input name="endDate" value={form.endDate} type="date" required min={form.startDate || today} onChange={handleChange} className="w-full border-gray-300 border py-3 px-4 rounded-lg" />
                            </div>
                        </div>
                    </div>
                </div>
            </form>
        </div>
    );
};

export default CourseRegisterPage;