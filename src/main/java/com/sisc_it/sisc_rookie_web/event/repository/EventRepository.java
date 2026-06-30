package com.sisc_it.sisc_rookie_web.event.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sisc_it.sisc_rookie_web.event.domain.Event;
import com.sisc_it.sisc_rookie_web.event.domain.EventStatus;

public interface EventRepository extends JpaRepository<Event, Long> {

    // 메서드 이름만으로 쿼리가 자동 생성된다(Spring Data JPA).
    // 전체 행사를 최신순으로 조회
    List<Event> findAllByOrderByCreatedAtDesc();

    // 특정 상태의 행사를 최신순으로 조회
    List<Event> findAllByStatusOrderByCreatedAtDesc(EventStatus status);
}
