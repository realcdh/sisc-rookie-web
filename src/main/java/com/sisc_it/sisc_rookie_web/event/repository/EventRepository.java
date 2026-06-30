package com.sisc_it.sisc_rookie_web.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sisc_it.sisc_rookie_web.event.domain.Event;
import com.sisc_it.sisc_rookie_web.event.domain.EventStatus;

public interface EventRepository extends JpaRepository<Event, Long> {

    // 대시보드 집계용(예: 모집 중인 행사 수)
    long countByStatus(EventStatus status);
}
