import { useState } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import { useMutation } from '@tanstack/react-query'
import { CalendarClock, History, LineChart } from 'lucide-react'
import { useAuth } from '../contexts/AuthContext'
import { authApi } from '../api/auth'
import { getApiErrorMessage } from '../api/client'
import './LandingPage.css'

const POINTS = [
  {
    Icon: CalendarClock,
    title: '미룰 땐, 다시 약속해요',
    body: '그냥 내일로 넘기는 게 아니라 미루는 이유를 고르고 언제 다시 할지 정합니다.',
  },
  {
    Icon: History,
    title: '미룬 기록이 남아요',
    body: '언제 몇 번 미뤘는지 일정마다 쌓입니다. 지난 날짜에 남은 일정은 먼저 알려드려요.',
  },
  {
    Icon: LineChart,
    title: '미루는 패턴을 알려줘요',
    body: '시간대별 완료율, 자주 미루는 일정, 예상 대비 실제 소요 시간을 정리해 보여줍니다.',
  },
]

// public/screenshots/ 에 넣어두면 표시된다. (파일이 없으면 해당 장면은 자동으로 숨겨진다)
// 순서: 밀린 일정을 마주하고 → 다시 약속하고 → 패턴을 보고 → 하루를 돌아본다
const SCREENSHOTS = [
  {
    src: '/screenshots/home.png',
    alt: '월간 달력에서 완료·미룸·남음을 한눈에',
  },
  {
    src: '/screenshots/defer.png',
    alt: '미룰 때 이유를 고르고 다시 할 시간을 정하는 화면',
  },
  {
    src: '/screenshots/insights.png',
    alt: '미루기 패턴을 정리한 인사이트 화면',
  },
  { src: '/screenshots/reflection.png', alt: '하루를 돌아보는 회고 화면' },
]

function LandingPage() {
  const navigate = useNavigate()
  const { isAuthenticated, login } = useAuth()
  const guestMutation = useMutation({
    mutationFn: authApi.startGuestSession,
    onSuccess: (session) => {
      login(session)
      navigate('/', { replace: true })
    },
  })
  // 스크린샷 파일을 아직 안 넣었을 때 깨진 이미지가 보이지 않도록 걸러낸다.
  const [brokenShots, setBrokenShots] = useState([])
  const shots = SCREENSHOTS.filter((shot) => !brokenShots.includes(shot.src))

  if (isAuthenticated) {
    return <Navigate to="/" replace />
  }

  return (
    <div className="landing">
      {/* 로그인은 본문에서 반복하지 않고 상단 우측에 한 번만 둔다 */}
      <header className="landing-header">
        <h1 className="landing-logo">RE:DAY</h1>
        <button
          type="button"
          className="landing-login"
          onClick={() => navigate('/login')}
        >
          로그인
        </button>
      </header>

      <section className="landing-hero">
        <p className="landing-lead">
          적는 건 쉬운데,
          <br />
          <strong>못 한 일은 어디로 갔는지</strong>
          <br />
          모르겠더라고요.
        </p>
        <p className="landing-sub">
          체크를 안 하면 아무 기록도 남지 않으니까요. RE:DAY는 완료 대신{' '}
          <strong>미루는 것</strong>을 기록하는 일정 관리 서비스예요.
        </p>
      </section>

      {shots.length > 0 && (
        <section className="landing-shots">
          {shots.map((shot) => (
            <span className="landing-shot-wrap" key={shot.src}>
              <img
                className="landing-shot"
                src={shot.src}
                alt={shot.alt}
                loading="lazy"
                onError={() =>
                  setBrokenShots((prev) => [...new Set([...prev, shot.src])])
                }
              />
            </span>
          ))}
        </section>
      )}

      <section className="landing-points">
        {POINTS.map(({ Icon, title, body }) => (
          <div className="landing-point" key={title}>
            <span className="landing-point-icon">
              <Icon size={20} />
            </span>
            <div>
              <p className="landing-point-title">{title}</p>
              <p className="landing-point-body">{body}</p>
            </div>
          </div>
        ))}
      </section>

      <p className="landing-note">설치 없이 브라우저에서 바로 쓸 수 있어요.</p>

      <div className="landing-cta">
        {guestMutation.isError && (
          <p className="landing-error" role="alert">
            {getApiErrorMessage(
              guestMutation.error,
              '게스트 모드를 시작하지 못했어요. 다시 시도해주세요.',
            )}
          </p>
        )}
        <button
          type="button"
          className="landing-start"
          disabled={guestMutation.isPending}
          onClick={() => guestMutation.mutate()}
        >
          {guestMutation.isPending ? '시작하는 중…' : '로그인 없이 시작'}
        </button>
      </div>
    </div>
  )
}

export default LandingPage
