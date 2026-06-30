package com.sisc_it.sisc_rookie_web.application.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sisc_it.sisc_rookie_web.application.domain.Application;
import com.sisc_it.sisc_rookie_web.application.domain.ApplicationStatus;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    boolean existsByEventIdAndMemberId(Long eventId, Long memberId);

    // 출석 자격(승인 여부) 확인에 사용한다.
    boolean existsByEventIdAndMemberIdAndStatus(Long eventId, Long memberId, ApplicationStatus status);
}
