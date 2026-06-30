package com.sisc_it.sisc_rookie_web.application.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sisc_it.sisc_rookie_web.application.domain.Application;
import com.sisc_it.sisc_rookie_web.application.domain.ApplicationStatus;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    boolean existsByEventIdAndMemberId(Long eventId, Long memberId);

    // 대시보드 집계용
    long countByStatus(ApplicationStatus status);

    long countByAttendedTrue();
}
