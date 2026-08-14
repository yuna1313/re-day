import { useNavigate } from 'react-router-dom'
import { TrendingUp, Clock3, Sparkles, Lightbulb } from 'lucide-react'
import { useInsights } from '../hooks/useInsights'
import { getApiErrorMessage } from '../api/client'
import './InsightsPage.css'

const Y_TICKS = [100, 50, 0]

// 미루기 상위 이유별 행동 제안
const DEFER_ACTION_TIP = {
  LONGER_THAN_EXPECTED: '예상 시간을 넉넉히 잡거나 작업을 잘게 나눠보세요.',
  TOO_BIG: '작업을 더 작은 단위로 쪼개면 시작이 쉬워져요.',
  NOT_STARTED: '가장 작은 첫 단계를 정해 바로 시작해보세요.',
  COULD_NOT_FOCUS: '25분 집중 → 5분 휴식처럼 짧게 나눠 집중해보세요.',
  NO_TIME: '하루에 딱 하나를 "오늘의 한 가지"로 정해보세요.',
}

// 응답 데이터로 행동 제안(액션 팁) 생성 — 숫자를 "그래서 뭘 할지"로 연결
function buildActionTips(timeSlots, reasons, diff) {
  const tips = []

  const best = timeSlots.reduce(
    (acc, slot) =>
      slot.completionRate > (acc?.completionRate ?? -1) ? slot : acc,
    null,
  )
  if (best && best.completionRate > 0) {
    tips.push(
      `${best.label}에 완료율이 높아요. 어려운 일은 ${best.label}에 배치해보세요.`,
    )
  }

  const topReason = reasons[0]
  if (topReason && DEFER_ACTION_TIP[topReason.deferReasonCode]) {
    tips.push(DEFER_ACTION_TIP[topReason.deferReasonCode])
  }

  if (diff > 0) {
    tips.push(
      `실제가 예상보다 평균 ${diff}분 더 걸려요. 예상 시간을 실제에 맞춰 잡아보세요.`,
    )
  }

  return tips
}

function InsightsPage() {
  const { data, isLoading, isError, error } = useInsights()

  return (
    <div className="insights-page">
      <header className="insights-header">
        <h1 className="insights-title">나의 인사이트</h1>
      </header>

      {/* 기간 (30일 고정) */}
      <div className="period-wrap">
        <span className="period-chip">최근 30일</span>
      </div>

      {isLoading ? (
        <InsightsSkeleton />
      ) : isError ? (
        <p className="insights-error">{getApiErrorMessage(error)}</p>
      ) : (
        <InsightsContent data={data} />
      )}
    </div>
  )
}

function InsightsContent({ data }) {
  const navigate = useNavigate()
  const timeSlots = data.timeSlotCompletionRates ?? []
  const reasons = data.topDeferReasons ?? []
  const deferred = data.topDeferredSchedules ?? []
  const est = data.estimatedVsActual ?? {}
  const feedback = data.feedbackMessages ?? []

  const bestRate = timeSlots.length
    ? Math.max(...timeSlots.map((t) => t.completionRate))
    : 0
  const maxCount = reasons.length ? Math.max(...reasons.map((r) => r.count)) : 0
  const maxDeferCount = deferred.length
    ? Math.max(...deferred.map((d) => d.deferCount))
    : 0
  const estMin = est.averageEstimatedMinutes ?? 0
  const actMin = est.averageActualMinutes ?? 0
  const diff = est.averageDiffMinutes ?? actMin - estMin
  const maxMin = Math.max(estMin, actMin) || 1
  const tips = buildActionTips(timeSlots, reasons, diff)

  return (
    <>
      {/* 피드백(인사이트 한 줄) */}
      {feedback.length > 0 && (
        <div className="insight-banner">
          <Sparkles size={16} />
          <div className="insight-lines">
            {feedback.map((msg, i) => (
              <span key={i}>{msg}</span>
            ))}
          </div>
        </div>
      )}

      {/* 시간대별 완료율 */}
      <section className="insights-section">
        <h2 className="insights-heading">시간대별 완료율</h2>
        <div className="insights-card">
          {timeSlots.length === 0 ? (
            <p className="insights-empty">표시할 데이터가 없어요.</p>
          ) : (
            <>
              <div className="chart">
                <div className="chart-grid">
                  {Y_TICKS.map((t) => (
                    <div className="grid-line" key={t}>
                      <span className="grid-label">{t}</span>
                    </div>
                  ))}
                </div>
                <div className="chart-bars">
                  {timeSlots.map((d) => (
                    <div
                      className={`bar-col${
                        d.completionRate === bestRate ? ' best' : ''
                      }`}
                      key={d.timeSlot}
                    >
                      <div
                        className="bar"
                        style={{ height: `${d.completionRate}%` }}
                      >
                        <span className="bar-value">{d.completionRate}%</span>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
              <div className="chart-xlabels">
                {timeSlots.map((d) => (
                  <span className="chart-xlabel" key={d.timeSlot}>
                    {d.label}
                  </span>
                ))}
              </div>
            </>
          )}
        </div>
      </section>

      {/* 미루기 상위 이유 */}
      <section className="insights-section">
        <h2 className="insights-heading">미루기 상위 이유</h2>
        <div className="insights-card">
          {reasons.length === 0 ? (
            <p className="insights-empty">미룬 일정이 없어요.</p>
          ) : (
            <ul className="reason-list">
              {reasons.map((d) => (
                <li className="reason-row" key={d.deferReasonCode}>
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
          )}
        </div>
      </section>

      {/* 자주 미루는 일정 */}
      <section className="insights-section">
        <h2 className="insights-heading">자주 미루는 일정</h2>
        <div className="insights-card">
          {deferred.length === 0 ? (
            <p className="insights-empty">반복해서 미룬 일정이 없어요.</p>
          ) : (
            <ul className="reason-list">
              {deferred.map((d) => (
                <li className="reason-row" key={d.scheduleId}>
                  <span className={`reason-rank rank-${d.rank}`}>{d.rank}</span>
                  <button
                    type="button"
                    className="reason-main deferred-main"
                    onClick={() => navigate(`/schedules/${d.scheduleId}`)}
                  >
                    <div className="reason-top">
                      <span className="reason-label">{d.title}</span>
                      <span className="reason-count">{d.deferCount}번</span>
                    </div>
                    <div className="reason-track">
                      <div
                        className={`reason-fill rank-${d.rank}`}
                        style={{
                          width: `${(d.deferCount / maxDeferCount) * 100}%`,
                        }}
                      />
                    </div>
                  </button>
                </li>
              ))}
            </ul>
          )}
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
                style={{ width: `${(estMin / maxMin) * 100}%` }}
              />
            </div>
            <span className="compare-val">{estMin}분</span>
          </div>
          <div className="compare-row">
            <span className="compare-label">
              <Clock3 size={14} />
              실제
            </span>
            <div className="compare-track">
              <div
                className="compare-fill act"
                style={{ width: `${(actMin / maxMin) * 100}%` }}
              />
            </div>
            <span className="compare-val">{actMin}분</span>
          </div>

          {diff > 0 && (
            <div className="compare-note">
              <TrendingUp size={15} />
              평균 <strong>+{diff}분</strong> 더 걸려요
            </div>
          )}
        </div>
      </section>

      {/* 이렇게 해보세요 (관찰 → 행동) */}
      {tips.length > 0 && (
        <section className="insights-section">
          <h2 className="insights-heading">이렇게 해보세요</h2>
          <div className="insights-card">
            <ul className="tip-list">
              {tips.map((tip, i) => (
                <li className="tip-item" key={i}>
                  <Lightbulb size={16} />
                  <span>{tip}</span>
                </li>
              ))}
            </ul>
          </div>
        </section>
      )}
    </>
  )
}

// 로딩 스켈레톤 (카드 구조에 맞춘 회색 블록 + shimmer)
function InsightsSkeleton() {
  return (
    <div aria-hidden="true">
      <section className="insights-section">
        <h2 className="insights-heading">시간대별 완료율</h2>
        <div className="insights-card">
          <div className="chart-skel">
            {[58, 92, 76].map((h, i) => (
              <span
                className="skeleton"
                key={i}
                style={{
                  width: 54,
                  height: `${h}%`,
                  borderRadius: '14px 14px 0 0',
                }}
              />
            ))}
          </div>
        </div>
      </section>

      <section className="insights-section">
        <h2 className="insights-heading">미루기 상위 이유</h2>
        <div className="insights-card">
          <ul className="reason-list">
            {[0, 1, 2].map((i) => (
              <li className="reason-row" key={i}>
                <span
                  className="skeleton"
                  style={{ width: 26, height: 26, borderRadius: 9 }}
                />
                <div className="reason-main">
                  <div className="reason-top">
                    <span
                      className="skeleton"
                      style={{ width: '45%', height: 14 }}
                    />
                  </div>
                  <span
                    className="skeleton"
                    style={{ display: 'block', height: 6, borderRadius: 4 }}
                  />
                </div>
              </li>
            ))}
          </ul>
        </div>
      </section>

      <section className="insights-section">
        <h2 className="insights-heading">예상 vs 실제 평균</h2>
        <div className="insights-card">
          {[0, 1].map((i) => (
            <div
              className="compare-row"
              key={i}
              style={i ? { marginTop: 14 } : undefined}
            >
              <span className="skeleton" style={{ width: 44, height: 14 }} />
              <span
                className="skeleton"
                style={{ flex: 1, height: 22, borderRadius: 11 }}
              />
            </div>
          ))}
        </div>
      </section>
    </div>
  )
}

export default InsightsPage
