import { TrendingUp, Clock3 } from 'lucide-react'
import './InsightsPage.css'

// TODO: 인사이트 조회 API 연동 예정 — 현재는 시안 기준 샘플 데이터
// 기간은 30일 고정 (7일/전체 선택은 추후)

// 시간대별 완료율 (%)
const TIME_COMPLETION = [
  { label: '오전', rate: 38 },
  { label: '오후', rate: 97 },
  { label: '저녁', rate: 74 },
]

// 미루기 상위 이유
const DEFER_REASONS = [
  { rank: 1, label: '예상보다 오래 걸림', count: 9 },
  { rank: 2, label: '집중 안 됨', count: 5 },
  { rank: 3, label: '시간을 착각함', count: 1 },
]

// 예상 vs 실제 평균 (분)
const ESTIMATE = { estimated: 30, actual: 50 }

const Y_TICKS = [100, 50, 0]

function InsightsPage() {
  const bestTime = TIME_COMPLETION.reduce((a, b) => (b.rate > a.rate ? b : a))
  const maxCount = Math.max(...DEFER_REASONS.map((d) => d.count))
  const maxMin = Math.max(ESTIMATE.estimated, ESTIMATE.actual)
  const diff = ESTIMATE.actual - ESTIMATE.estimated

  return (
    <div className="insights-page">
      <header className="insights-header">
        <h1 className="insights-title">나의 인사이트</h1>
      </header>

      {/* 기간 (30일 고정) */}
      <div className="period-wrap">
        <span className="period-chip">최근 30일</span>
      </div>

      {/* 시간대별 완료율 */}
      <section className="insights-section">
        <h2 className="insights-heading">시간대별 완료율</h2>
        <div className="insights-card">
          <div className="chart">
            <div className="chart-grid">
              {Y_TICKS.map((t) => (
                <div className="grid-line" key={t}>
                  <span className="grid-label">{t}</span>
                </div>
              ))}
            </div>
            <div className="chart-bars">
              {TIME_COMPLETION.map((d) => (
                <div
                  className={`bar-col${d.label === bestTime.label ? ' best' : ''}`}
                  key={d.label}
                >
                  <div className="bar" style={{ height: `${d.rate}%` }}>
                    <span className="bar-value">{d.rate}%</span>
                  </div>
                </div>
              ))}
            </div>
          </div>
          <div className="chart-xlabels">
            {TIME_COMPLETION.map((d) => (
              <span className="chart-xlabel" key={d.label}>
                {d.label}
              </span>
            ))}
          </div>
        </div>
      </section>

      {/* 미루기 상위 이유 */}
      <section className="insights-section">
        <h2 className="insights-heading">미루기 상위 이유</h2>
        <div className="insights-card">
          <ul className="reason-list">
            {DEFER_REASONS.map((d) => (
              <li className="reason-row" key={d.rank}>
                <span className={`reason-rank rank-${d.rank}`}>{d.rank}</span>
                <div className="reason-main">
                  <div className="reason-top">
                    <span className="reason-label">{d.label}</span>
                    <span className="reason-count">{d.count}회</span>
                  </div>
                  <div className="reason-track">
                    <div
                      className={`reason-fill rank-${d.rank}`}
                      style={{ width: `${(d.count / maxCount) * 100}%` }}
                    />
                  </div>
                </div>
              </li>
            ))}
          </ul>
        </div>
      </section>

      {/* 예상 vs 실제 평균 */}
      <section className="insights-section">
        <h2 className="insights-heading">예상 vs 실제 평균</h2>
        <div className="insights-card">
          <div className="compare-row">
            <span className="compare-label">
              <Clock3 size={14} />
              예상
            </span>
            <div className="compare-track">
              <div
                className="compare-fill est"
                style={{ width: `${(ESTIMATE.estimated / maxMin) * 100}%` }}
              />
            </div>
            <span className="compare-val">{ESTIMATE.estimated}분</span>
          </div>
          <div className="compare-row">
            <span className="compare-label">
              <Clock3 size={14} />
              실제
            </span>
            <div className="compare-track">
              <div
                className="compare-fill act"
                style={{ width: `${(ESTIMATE.actual / maxMin) * 100}%` }}
              />
            </div>
            <span className="compare-val">{ESTIMATE.actual}분</span>
          </div>

          {diff > 0 && (
            <div className="compare-note">
              <TrendingUp size={15} />
              평균 <strong>+{diff}분</strong> 더 걸려요
            </div>
          )}
        </div>
      </section>
    </div>
  )
}

export default InsightsPage
