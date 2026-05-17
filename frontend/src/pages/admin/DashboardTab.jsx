import { useEffect, useState } from 'react';
import { getAdminDashboard } from '../../api/admin';

/* 관리자 대시보드 탭 */
const DashboardTab = () => {
    /* 통계 데이터 상태 */
    const [stats, setStats] = useState(null);
    /* 로딩 상태 */
    const [isLoading, setIsLoading] = useState(true);

    /* 컴포넌트 마운트 시 대시보드 통계 데이터 조회 */
    useEffect(() => {
        getAdminDashboard()
            .then(res => setStats(res.data))
            .catch(() => alert('통계 조회에 실패했습니다.'))
            .finally(() => setIsLoading(false));
    }, []);

    /* 카드 정보 배열 */
    const cards = [
        { label: '전체 사용자', value: stats?.totalUsers ?? 0, unit: '명' },
        { label: '전체 강의', value: stats?.totalCourses ?? 0, unit: '개' },
        { label: '확정 수강 신청', value: stats?.totalEnrollments ?? 0, unit: '건' },
    ];

    if (isLoading) return <div className="text-center py-20 text-gray-400">로딩 중...</div>;

    return (
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-6">
            {cards.map(card => (
                <div key={card.label} className="bg-white border border-gray-200 rounded-2xl p-6 shadow-sm">
                    <p className="text-xs font-semibold text-gray-400 mb-2">{card.label}</p>
                    <p className="text-3xl font-extrabold text-gray-900">
                        {card.value.toLocaleString()}
                        <span className="text-base font-semibold text-gray-400 ml-1">{card.unit}</span>
                    </p>
                </div>
            ))}
        </div>
    );
};

export default DashboardTab;
