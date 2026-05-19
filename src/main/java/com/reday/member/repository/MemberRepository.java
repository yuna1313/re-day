package com.reday.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reday.member.domain.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

	Optional<Member> findByEmail(String email);

	boolean existsByEmail(String email);
}
