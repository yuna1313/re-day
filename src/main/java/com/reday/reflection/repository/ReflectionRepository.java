package com.reday.reflection.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reday.reflection.domain.Reflection;

public interface ReflectionRepository extends JpaRepository<Reflection, Long> {

	/**
	 * 특정 회원이 특정 날짜에 작성한 회고를 조회합니다.
	 *
	 * @param memberIdx 회고 작성 회원 식별자
	 * @param reflectionDate 회고 날짜
	 * @return 해당 날짜의 회고
	 */
	Optional<Reflection> findByMemberIdxAndReflectionDate(Integer memberIdx, LocalDate reflectionDate);

	/**
	 * 특정 회원이 특정 날짜에 작성한 회고가 존재하는지 확인합니다.
	 *
	 * @param memberIdx 회고 작성 회원 식별자
	 * @param reflectionDate 회고 날짜
	 * @return 해당 날짜 회고 존재 여부
	 */
	boolean existsByMemberIdxAndReflectionDate(Integer memberIdx, LocalDate reflectionDate);

	/**
	 * 특정 회원이 작성한 특정 회고를 조회합니다.
	 *
	 * @param reflectionIdx 회고 식별자
	 * @param memberIdx 회고 작성 회원 식별자
	 * @return 회원이 작성한 회고
	 */
	Optional<Reflection> findByReflectionIdxAndMemberIdx(Integer reflectionIdx, Integer memberIdx);
}
