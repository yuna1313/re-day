import { Link } from 'react-router-dom'

function HomePage() {
  return (
    <section>
      <h1>RE:DAY</h1>
      <p>커밋 기록으로 하루를 돌아보는 회고 서비스입니다.</p>
      <Link to="/retrospectives">회고 목록 보러가기 →</Link>
    </section>
  )
}

export default HomePage
