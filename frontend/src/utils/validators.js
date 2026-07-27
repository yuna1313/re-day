// 실용적 수준의 이메일 형식 검사 (로컬@도메인.TLD).
// 완벽한 RFC 검증은 아니지만 실제 형식 오류는 대부분 걸러낸다.
export function isValidEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
}
