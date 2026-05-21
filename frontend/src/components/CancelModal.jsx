/* CONFIRMED 수강 취소 사유 */
const CANCEL_REASONS = ['단순 변심', '일정 변경', '강의 내용이 기대와 다름', '기타'];

/* CONFIRMED 수강 취소 사유 모달 */
const CancelModal = ({ open, onClose, onConfirm, selectedReason, setSelectedReason, customReason, setCustomReason }) => {
    if (!open) return null;

    return (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
            <div className="bg-white rounded-xl shadow-xl p-6 w-full max-w-md mx-4">
                <h2 className="text-lg font-bold text-gray-900 mb-4">수강 취소 사유</h2>

                {/* 사유 선택 라디오 버튼 */}
                <div className="flex flex-col gap-2 mb-4">
                    {CANCEL_REASONS.map(reason => (
                        <label key={reason} className="flex items-center gap-2 cursor-pointer">
                            <input
                                type="radio"
                                name="cancelReason"
                                value={reason}
                                checked={selectedReason === reason}
                                onChange={(e) => setSelectedReason(e.target.value)}
                                className="accent-blue-600"
                            />
                            <span className="text-sm text-gray-700">{reason}</span>
                        </label>
                    ))}
                </div>

                {/* 기타 사유 입력 텍스트 영역 (기타 선택 시에만 표시) */}
                {selectedReason === '기타' && (
                    <textarea
                        value={customReason}
                        onChange={(e) => setCustomReason(e.target.value)}
                        placeholder="취소 사유를 입력해주세요."
                        rows={3}
                        className="w-full border border-gray-300 rounded-md px-3 py-2 text-sm text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500 mb-4 resize-none"
                    />
                )}

                {/* 액션 버튼 그룹 */}
                <div className="flex gap-2 justify-end">
                    <button
                        onClick={onClose}
                        className="px-4 py-2 border border-gray-300 text-gray-600 text-sm font-semibold rounded-md hover:bg-gray-50 transition-colors cursor-pointer"
                    >
                        닫기
                    </button>
                    <button
                        onClick={onConfirm}
                        className="px-4 py-2 bg-red-500 text-white text-sm font-semibold rounded-md hover:bg-red-600 transition-colors cursor-pointer"
                    >
                        취소 확인
                    </button>
                </div>
            </div>
        </div>
    );
};

export default CancelModal;
