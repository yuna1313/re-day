// 미루기 이유 목록 (백엔드 ALLOWED_DEFER_REASON_CODES 와 일치해야 함).
export const DEFER_CUSTOM_CODE = 'CUSTOM'

export const DEFER_REASONS = [
  { code: 'LONGER_THAN_EXPECTED', label: '예상보다 오래 걸림' },
  { code: 'NOT_STARTED', label: '시작을 못 함' },
  { code: 'NO_TIME', label: '시간이 없었음' },
  { code: 'COULD_NOT_FOCUS', label: '집중 안 됨' },
  { code: 'TOO_BIG', label: '작업량이 너무 많음' },
]

// 사유 코드 → 표시 라벨. 모르는 코드(백엔드에 새 사유 추가 등)는 '기타'로 표시한다.
export function deferReasonLabel(code) {
  if (code === DEFER_CUSTOM_CODE) return '직접 입력'
  return DEFER_REASONS.find((reason) => reason.code === code)?.label ?? '기타'
}
