package com.sisc_it.sisc_rookie_web.application.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sisc_it.sisc_rookie_web.application.domain.Application;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    boolean existsByEventIdAndMemberId(Long eventId, Long memberId);

    Optional<Application> findByEventIdAndMemberId(Long eventId, Long memberId);

    // 특정 행사의 신청자 목록을 신청한 순서대로 조회
    List<Application> findByEventIdOrderByAppliedAtAsc(Long eventId);
}
