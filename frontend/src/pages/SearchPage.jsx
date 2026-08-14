import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { ChevronLeft, Search, X } from 'lucide-react'
import { useSearchSchedules } from '../hooks/useSearchSchedules'
import { getApiErrorMessage } from '../api/client'
import './SearchPage.css'

// 입력할 때마다 요청하지 않도록 잠시 기다렸다가 검색한다.
const DEBOUNCE_MS = 300

function SearchPage() {
  const navigate = useNavigate()
  const [keyword, setKeyword] = useState('')
  const [searchTerm, setSearchTerm] = useState('')

  useEffect(() => {
    const timer = setTimeout(() => setSearchTerm(keyword.trim()), DEBOUNCE_MS)
    return () => clearTimeout(timer)
  }, [keyword])

  const { data, isLoading, isError, error } = useSearchSchedules(searchTerm)
  const items = data?.items ?? []

  return (
    <div className="search-page">
      <header className="search-header">
        <button
          type="button"
          className="search-back"
          onClick={() => navigate(-1)}
          aria-label="뒤로"
        >
          <ChevronLeft size={26} />
        </button>

        <div className="search-field">
          <Search size={18} className="search-field-icon" />
          <input
            className="search-input"
            type="search"
            placeholder="일정 제목 검색"
            value={keyword}
            // 검색 화면은 입력이 유일한 목적이라 바로 포커스를 준다.
            autoFocus
            onChange={(event) => setKeyword(event.target.value)}
          />
          {keyword && (
            <button
              type="button"
              className="search-clear"
              onClick={() => setKeyword('')}
              aria-label="검색어 지우기"
            >
              <X size={16} />
            </button>
          )}
        </div>
      </header>

      {!searchTerm ? (
        <p className="search-guide">제목으로 지난 일정을 찾아볼 수 있어요.</p>
      ) : isLoading ? (
        <SearchSkeleton />
      ) : isError ? (
        <p className="search-error">{getApiErrorMessage(error)}</p>
      ) : items.length === 0 ? (
        <p className="search-guide">‘{searchTerm}’와 일치하는 일정이 없어요.</p>
      ) : (
        <>
          <p className="search-count">{items.length}개 찾았어요</p>
          <ul className="search-list">
            {items.map((item) => (
              <SearchResultItem
                key={item.id}
                item={item}
                onClick={() =>
                  navigate(`/schedules/${item.id}`, {
                    state: { schedule: item },
                  })
                }
              />
            ))}
          </ul>
          {data.hasMore && (
            <p className="search-more-note">
              최근 일정 50개까지만 보여드려요. 검색어를 더 자세히 적어보세요.
            </p>
          )}
        </>
      )}
    </div>
  )
}

// 'yyyy-MM-dd' → '2026년 1월 9일'
function dateLabel(dateStr) {
  const [year, month, day] = dateStr.split('-')
  return `${Number(year)}년 ${Number(month)}월 ${Number(day)}일`
}

function SearchResultItem({ item, onClick }) {
  return (
    <li>
      <button type="button" className="search-item" onClick={onClick}>
        <span className="search-item-title">{item.title}</span>
        <span className="search-item-meta">
          {dateLabel(item.date)} {item.period} {item.time}
        </span>
        <span className="search-item-badges">
          {item.completed && <span className="search-badge done">완료</span>}
          {item.deferCount > 0 && (
            <span className="search-badge deferred">
              {item.deferCount}번 미룸
            </span>
          )}
        </span>
      </button>
    </li>
  )
}

// 로딩 중 스켈레톤 (검색 결과 모양의 회색 블록 + shimmer)
function SearchSkeleton() {
  return (
    <ul className="search-list" aria-hidden="true">
      {[0, 1, 2].map((i) => (
        <li key={i}>
          <div className="search-item">
            <span className="skeleton search-skel-title" />
            <span className="skeleton search-skel-meta" />
          </div>
        </li>
      ))}
    </ul>
  )
}

export default SearchPage
