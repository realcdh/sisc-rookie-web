package com.sisc_it.sisc_rookie_web.event.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sisc_it.sisc_rookie_web.event.domain.Event;
import com.sisc_it.sisc_rookie_web.event.domain.EventStatus;

public interface EventRepository extends JpaRepository<Event, Long> {

    // 목록/상세 조회 시 주최자(createdBy)를 함께 가져와 N+1을 방지한다.
    @Query("select e from Event e join fetch e.createdBy order by e.createdAt desc")
    List<Event> findAllWithCreator();

    @Query("select e from Event e join fetch e.createdBy where e.status = :status order by e.createdAt desc")
    List<Event> findAllByStatusWithCreator(@Param("status") EventStatus status);

    @Query("select e from Event e join fetch e.createdBy where e.id = :id")
    Optional<Event> findByIdWithCreator(@Param("id") Long id);
}
