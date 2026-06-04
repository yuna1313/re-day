package com.reday.reflection.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "reflection")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reflection {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "reflection_idx")
	private Integer reflectionIdx;

	@Column(name = "member_idx", nullable = false)
	private Integer memberIdx;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	@Column(name = "reflection_date", nullable = false)
	private LocalDate reflectionDate;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	private Reflection(Integer memberIdx, String content, LocalDate reflectionDate) {
		this.memberIdx = memberIdx;
		this.content = content;
		this.reflectionDate = reflectionDate;
		this.createdAt = LocalDateTime.now();
	}

	/**
	 * 회고 엔티티를 생성합니다.
	 *
	 * @param memberIdx 회고 작성 회원 식별자
	 * @param content 회고 내용
	 * @param reflectionDate 회고 날짜
	 * @return 회고 엔티티
	 */
	public static Reflection create(Integer memberIdx, String content, LocalDate reflectionDate) {
		return new Reflection(memberIdx, content, reflectionDate);
	}
}
