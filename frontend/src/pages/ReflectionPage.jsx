import { useState } from 'react'
import { format } from 'date-fns'
import { CheckCircle2 } from 'lucide-react'
import { useTodayReflection } from '../hooks/useTodayReflection'
import { useCreateReflection } from '../hooks/useCreateReflection'
import { useUpdateReflection } from '../hooks/useUpdateReflection'
import { getApiErrorMessage } from '../api/client'
import './ReflectionPage.css'

const MAX_LENGTH = 1000

function ReflectionPage() {
  const todayKey = format(new Date(), 'yyyy-MM-dd')

  // 오늘 회고 + 오늘 완료한 일정 조회
  const { data, isLoading, isError, error } = useTodayReflection()
  const reflection = data?.reflection ?? null
  const completed = data?.completedSchedules ?? []
  const hasReflection = Boolean(reflection)

  const [isEditing, setIsEditing] = useState(false)
  const [content, setContent] = useState('')
  // 작성 내역이 없으면 바로 작성, 있으면 조회(보기) → 수정하기 눌러야 편집
  const showEditor = !hasReflection || isEditing

  const createMutation = useCreateReflection()
  const updateMutation = useUpdateReflection()
  const submitMutation = hasReflection ? updateMutation : createMutation

  const startEditing = () => {
    setContent(reflection?.content ?? '')
    updateMutation.reset()
    setIsEditing(true)
  }

  const cancelEditing = () => {
    setIsEditing(false)
    updateMutation.reset()
  }

  const handleSubmit = () => {
    if (!content.trim() || submitMutation.isPending) return

    if (hasReflection) {
      updateMutation.mutate(
        { reflectionId: reflection.reflectionId, content: content.trim() },
        { onSuccess: () => setIsEditing(false) },
      )
    } else {
      createMutation.mutate(
        { reflectionDate: todayKey, content: content.trim() },
        {
          onSuccess: () => {
            setIsEditing(false)
            setContent('')
          },
        },
      )
    }
  }

  return (
    <div className="reflection-page">
      <header className="reflection-header">
        <h1 className="reflection-title">오늘의 회고</h1>
      </header>

      {/* 오늘 완료한 일정 */}
      <section className="reflection-section">
        <h2 className="reflection-heading">오늘 완료한 일정</h2>

        {isLoading ? (
          <ul className="completed-list" aria-hidden="true">
            {[0, 1].map((i) => (
              <li className="completed-item" key={i}>
                <span className="skeleton refl-skel-check" />
                <span className="skeleton refl-skel-title" />
              </li>
            ))}
          </ul>
        ) : completed.length === 0 ? (
          <p className="reflection-empty">완료한 일정이 없어요.</p>
        ) : (
          <ul className="completed-list">
            {completed.map((item) => (
              <li className="completed-item" key={item.scheduleId}>
                <CheckCircle2 size={20} fill="#3b82f6" color="#fff" />
                <span className="completed-title">{item.title}</span>
              </li>
            ))}
          </ul>
        )}
      </section>

      {/* 오늘의 회고 */}
      <section className="reflection-section">
        <h2 className="reflection-heading">오늘의 회고</h2>

        {isLoading ? (
          <div className="skeleton refl-skel-editor" />
        ) : isError ? (
          <p className="reflection-error">{getApiErrorMessage(error)}</p>
        ) : showEditor ? (
          <>
            {!hasReflection && (
              <p className="reflection-subtext">
                오늘의 회고를 작성해볼까요? 😎
              </p>
            )}

            <div className="reflection-textarea-wrap">
              <textarea
                className="reflection-textarea"
                placeholder="오늘 느낀 점이나 배운 점을 자유롭게 작성해보세요."
                value={content}
                maxLength={MAX_LENGTH}
                onChange={(event) => setContent(event.target.value)}
              />
              <span className="reflection-counter">
                {content.length} / {MAX_LENGTH}
              </span>
            </div>

            {submitMutation.isError && (
              <p className="reflection-error">
                {getApiErrorMessage(submitMutation.error)}
              </p>
            )}

            {hasReflection ? (
              <div className="reflection-btn-row">
                <button
                  type="button"
                  className="reflection-cancel"
                  onClick={cancelEditing}
                  disabled={submitMutation.isPending}
                >
                  취소
                </button>
                <button
                  type="button"
                  className="reflection-submit"
                  onClick={handleSubmit}
                  disabled={!content.trim() || submitMutation.isPending}
                >
                  {submitMutation.isPending ? '수정 중...' : '수정 완료'}
                </button>
              </div>
            ) : (
              <button
                type="button"
                className="reflection-submit"
                onClick={handleSubmit}
                disabled={!content.trim() || submitMutation.isPending}
              >
                {submitMutation.isPending ? '작성 중...' : '작성하기'}
              </button>
            )}
          </>
        ) : (
          /* 작성 내역이 있으면 내용만 보여주고 '수정하기' */
          <>
            <div className="reflection-view">{reflection.content}</div>
            <button
              type="button"
              className="reflection-submit"
              onClick={startEditing}
            >
              수정하기
            </button>
          </>
        )}
      </section>
    </div>
  )
}

export default ReflectionPage
