import { useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { getCreatorRequestList, approveCreatorRequest, rejectCreatorRequest } from '../../api/creatorRequest';

/* 상태 라벨/스타일 */
const statusLabel = (status) => status === 'PENDING' ? '검토 중' : status === 'APPROVED' ? '승인' : '거절';
const statusStyle = (status) =>
    status === 'PENDING' ? 'bg-yellow-100 text-yellow-700' :
    status === 'APPROVED' ? 'bg-green-100 text-green-700' :
    'bg-red-100 text-red-700';

/* 강사 신청 관리 탭 */
const CreatorRequestTab = () => {
    /* React Query 클라이언트 */
    const queryClient = useQueryClient();
    /* 거절 모달 상태: { id, rejectReason } */
    const [rejectModal, setRejectModal] = useState(null);

    /* 강사 신청 목록 조회 */
    const { data: requests = [], isLoading } = useQuery({
        queryKey: ['creatorRequests'],
        queryFn: () => getCreatorRequestList().then(res => res.data),
    });

    /* 승인 */
    const handleApprove = async (id) => {
        if (!window.confirm('강사 신청을 승인하시겠습니까?')) return;

        try {
            await approveCreatorRequest(id);
            alert('승인되었습니다.');
            queryClient.invalidateQueries({ queryKey: ['creatorRequests'] });
        } catch (err) {
            alert(err.response?.data?.message || '승인에 실패했습니다.');
        }
    };

    /* 거절 */
    const handleReject = async () => {
        if (!rejectModal.rejectReason.trim()) {
            alert('거절 사유를 입력해주세요.');
            return;
        }

        try {
            await rejectCreatorRequest(rejectModal.id, rejectModal.rejectReason.trim());

            alert('거절되었습니다.');
            setRejectModal(null);
            queryClient.invalidateQueries({ queryKey: ['creatorRequests'] });
        } catch (err) {
            alert(err.response?.data?.message || '거절에 실패했습니다.');
        }
    };

    if (isLoading) return <div className="text-center py-20 text-gray-400">로딩 중...</div>;

    if (requests.length === 0) return (
        <div className="text-center text-gray-400 py-32 border-2 border-dashed border-gray-100 rounded-2xl">
            강사 신청 내역이 없습니다.
        </div>
    );

    return (
        <div className="flex flex-col gap-4">
            {requests.map(req => (
                <div key={req.id} className="border border-gray-200 rounded-xl p-5 bg-white shadow-sm">
                    <div className="flex items-start justify-between gap-4">

                        {/* 신청자 정보 및 사유 */}
                        <div className="flex-1 min-w-0">
                            <div className="flex items-center gap-2 mb-1">
                                <span className="font-semibold text-gray-900">{req.name}</span>
                                <span className="text-sm text-gray-400">@{req.username}</span>
                                <span className={`text-xs font-semibold px-2 py-0.5 rounded-full ${statusStyle(req.status)}`}>
                                    {statusLabel(req.status)}
                                </span>
                            </div>
                            <p className="text-sm text-gray-600 mt-2">
                                <span className="font-medium text-gray-700">신청 사유</span>
                                <span className="mx-2 text-gray-300">|</span>
                                {req.reason}
                            </p>
                            {req.rejectReason && (
                                <p className="text-sm text-red-500 mt-1">
                                    <span className="font-medium">거절 사유</span>
                                    <span className="mx-2 text-red-300">|</span>
                                    {req.rejectReason}
                                </p>
                            )}
                            <p className="text-xs text-gray-400 mt-2">
                                {new Date(req.requestedAt).toLocaleDateString('ko-KR', { year: 'numeric', month: 'long', day: 'numeric' })}
                            </p>
                        </div>

                        {/* 승인/거절 버튼 */}
                        {req.status === 'PENDING' && (
                            <div className="flex gap-2 shrink-0">
                                <button
                                    onClick={() => handleApprove(req.id)}
                                    className="px-4 py-1.5 text-sm font-semibold bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors cursor-pointer"
                                >
                                    승인
                                </button>
                                <button
                                    onClick={() => setRejectModal({ id: req.id, rejectReason: '' })}
                                    className="px-4 py-1.5 text-sm font-semibold bg-red-500 text-white rounded-lg hover:bg-red-600 transition-colors cursor-pointer"
                                >
                                    거절
                                </button>
                            </div>
                        )}
                    </div>

                    {/* 거절 사유 입력 */}
                    {rejectModal?.id === req.id && (
                        <div className="mt-4 pt-4 border-t border-gray-100">
                            <textarea
                                value={rejectModal.rejectReason}
                                onChange={(e) => setRejectModal(prev => ({ ...prev, rejectReason: e.target.value }))}
                                placeholder="거절 사유를 입력해주세요."
                                rows={2}
                                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm text-gray-800 focus:outline-none focus:ring-2 focus:ring-red-400 resize-none"
                            />
                            <div className="flex justify-end gap-2 mt-2">
                                <button
                                    onClick={() => setRejectModal(null)}
                                    className="px-4 py-1.5 text-sm font-medium text-gray-600 border border-gray-300 rounded-lg hover:bg-gray-100 transition-colors cursor-pointer"
                                >
                                    취소
                                </button>
                                <button
                                    onClick={handleReject}
                                    className="px-4 py-1.5 text-sm font-semibold bg-red-500 text-white rounded-lg hover:bg-red-600 transition-colors cursor-pointer"
                                >
                                    거절하기
                                </button>
                            </div>
                        </div>
                    )}
                </div>
            ))}
        </div>
    );
};

export default CreatorRequestTab;
