import { useEffect, useState } from 'react';
import { PieChart, Pie, Cell, BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, LabelList, Label } from 'recharts';
import { getAdminDashboard } from '../../api/admin';

/* 사용자 역할별 레이블 및 색상 설정 */
const USER_COLORS = ['#4F46E5', '#8B5CF6'];
const COURSE_COLORS = ['#10B981', '#9ca3af', '#F87171', '#DC2626'];

/* 도넛 차트 중앙 라벨 */
const DonutCenter = ({ viewBox, total }) => {
    const { cx, cy } = viewBox || {};
    if (cx == null || cy == null) return null;
    return (
        <g>
            <text x={cx} y={cy - 5} textAnchor="middle" fill="#111827" style={{ fontSize: 22, fontWeight: 700 }}>
                {total.toLocaleString()}
            </text>
            <text x={cx} y={cy + 15} textAnchor="middle" fill="#9ca3af" style={{ fontSize: 11 }}>
                명
            </text>
        </g>
    );
};

/* 카드 아이콘 */
const IconUsers = () => (
    <svg className="w-5 h-5 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2M9 7a4 4 0 100 8 4 4 0 000-8zM23 21v-2a4 4 0 00-3-3.87M16 3.13a4 4 0 010 7.75" />
    </svg>
);
const IconBook = () => (
    <svg className="w-5 h-5 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
    </svg>
);
const IconCheck = () => (
    <svg className="w-5 h-5 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
    </svg>
);

/* 관리자 대시보드 탭 */
const DashboardTab = () => {
    /* 통계 데이터 상태 */
    const [stats, setStats] = useState(null);
    /* 로딩 상태 */
    const [isLoading, setIsLoading] = useState(true);

    /* 컴포넌트 마운트 시 통계 데이터 조회 */
    useEffect(() => {
        getAdminDashboard()
            .then(res => setStats(res.data))
            .catch(() => alert('통계 조회에 실패했습니다.'))
            .finally(() => setIsLoading(false));
    }, []);

    /* 카드 및 차트 데이터 구성 */
    const cards = [
        { label: '전체 사용자', value: stats?.totalUsers ?? 0, unit: '명', icon: <IconUsers /> },
        { label: '전체 강의', value: stats?.totalCourses ?? 0, unit: '개', icon: <IconBook /> },
        { label: '수강 신청 현황', value: stats?.totalEnrollments ?? 0, unit: '건', icon: <IconCheck /> },
    ];

    /* 사용자 역할 분포 차트 데이터 */
    const userChartData = [
        { name: '수강생', value: stats?.studentCount ?? 0 },
        { name: '강사', value: stats?.creatorCount ?? 0 },
    ];

    /* 강의 상태 분포 차트 데이터 */
    const courseChartData = [
        { name: '모집 중', value: stats?.openCount ?? 0 },
        { name: '준비 중', value: stats?.draftCount ?? 0 },
        { name: '마감', value: stats?.closedCount ?? 0 },
        { name: '강제 폐강', value: stats?.forceClosedCount ?? 0 },
    ];

    /* 전체 사용자 수 계산 (도넛 차트 중앙 라벨용) */
    const totalUsers = (stats?.studentCount ?? 0) + (stats?.creatorCount ?? 0);

    if (isLoading) return <div className="text-center py-20 text-gray-400">로딩 중...</div>;

    return (
        <div className="space-y-8">
            {/* 요약 카드 */}
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-5">
                {cards.map(card => (
                    <div key={card.label} className="bg-white border border-gray-100 rounded-2xl p-6 shadow-sm">
                        <div className="flex items-start justify-between mb-1">
                            <p className="text-xs font-semibold text-gray-400 tracking-wide uppercase">{card.label}</p>
                            {card.icon}
                        </div>
                        <p className="text-4xl font-extrabold text-gray-900">
                            {card.value.toLocaleString()}
                            <span className="text-sm font-medium text-gray-400 ml-1">{card.unit}</span>
                        </p>
                    </div>
                ))}
            </div>

            {/* 차트 */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
                {/* 사용자 역할 분포 */}
                <div className="bg-white border border-gray-100 rounded-2xl p-6 shadow-sm">
                    <p className="text-sm font-bold text-gray-700 mb-1">사용자 역할 분포</p>
                    <ResponsiveContainer width="100%" height={200}>
                        <PieChart>
                            <Pie
                                data={userChartData}
                                cx="50%"
                                cy="50%"
                                innerRadius={58}
                                outerRadius={82}
                                paddingAngle={3}
                                dataKey="value"
                                startAngle={90}
                                endAngle={-270}
                            >
                                {userChartData.map((_, i) => (
                                    <Cell key={i} fill={USER_COLORS[i]} strokeWidth={0} />
                                ))}
                                <Label content={<DonutCenter total={totalUsers} />} position="center" />
                            </Pie>
                            <Tooltip formatter={(v) => `${v.toLocaleString()}명`} />
                        </PieChart>
                    </ResponsiveContainer>
                    <div className="flex justify-center gap-6 mt-1">
                        {userChartData.map((item, i) => (
                            <div key={item.name} className="flex items-center gap-1.5">
                                <span className="w-2.5 h-2.5 rounded-full" style={{ backgroundColor: USER_COLORS[i] }} />
                                <span className="text-xs text-gray-500">{item.name}</span>
                                <span className="text-xs font-bold text-gray-700">{item.value.toLocaleString()}</span>
                            </div>
                        ))}
                    </div>
                </div>

                {/* 강의 상태 분포 */}
                <div className="bg-white border border-gray-100 rounded-2xl p-6 shadow-sm">
                    <p className="text-sm font-bold text-gray-700 mb-1">강의 상태 분포</p>
                    <ResponsiveContainer width="100%" height={200}>
                        <BarChart data={courseChartData} barSize={44} barCategoryGap="30%">
                            <XAxis dataKey="name" tick={{ fontSize: 12, fill: '#6b7280' }} axisLine={false} tickLine={false} />
                            <YAxis allowDecimals={false} tick={{ fontSize: 11, fill: '#9ca3af' }} axisLine={false} tickLine={false} width={28} />
                            <Tooltip formatter={(v) => `${v.toLocaleString()}개`} cursor={{ fill: '#f9fafb' }} />
                            <Bar dataKey="value" radius={[6, 6, 0, 0]}>
                                {courseChartData.map((_, i) => (
                                    <Cell key={i} fill={COURSE_COLORS[i]} />
                                ))}
                                <LabelList dataKey="value" position="top" style={{ fontSize: 12, fontWeight: 700, fill: '#374151' }} />
                            </Bar>
                        </BarChart>
                    </ResponsiveContainer>
                    <div className="flex justify-center gap-6 mt-1">
                        {courseChartData.map((item, i) => (
                            <div key={item.name} className="flex items-center gap-1.5">
                                <span className="w-2.5 h-2.5 rounded-full" style={{ backgroundColor: COURSE_COLORS[i] }} />
                                <span className="text-xs text-gray-500">{item.name}</span>
                                <span className="text-xs font-bold text-gray-700">{item.value.toLocaleString()}</span>
                            </div>
                        ))}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default DashboardTab;
