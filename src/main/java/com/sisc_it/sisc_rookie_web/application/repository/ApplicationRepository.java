package com.sisc_it.sisc_rookie_web.application.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sisc_it.sisc_rookie_web.application.domain.Application;
import com.sisc_it.sisc_rookie_web.application.domain.ApplicationStatus;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    boolean existsByEventIdAndMemberId(Long eventId, Long memberId);

    Optional<Application> findByEventIdAndMemberId(Long eventId, Long memberId);

    // 출석 자격(승인 여부) 확인에 사용한다.
    boolean existsByEventIdAndMemberIdAndStatus(Long eventId, Long memberId, ApplicationStatus status);

    // 대시보드 집계용
    long countByStatus(ApplicationStatus status);

    long countByAttendedTrue();

    // 신청자 목록 조회 시 신청자(member)를 함께 가져와 N+1을 방지한다.
    @Query("select a from Application a join fetch a.member where a.event.id = :eventId order by a.appliedAt asc")
    List<Application> findByEventIdWithMember(@Param("eventId") Long eventId);
}
