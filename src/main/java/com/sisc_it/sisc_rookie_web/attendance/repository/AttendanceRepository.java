package com.sisc_it.sisc_rookie_web.attendance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sisc_it.sisc_rookie_web.attendance.domain.Attendance;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    boolean existsByEventIdAndMemberId(Long eventId, Long memberId);

    // 출석 명단 조회 시 회원(member)을 함께 가져와 N+1을 방지한다.
    @Query("select a from Attendance a join fetch a.member where a.event.id = :eventId order by a.checkedInAt asc")
    List<Attendance> findByEventIdWithMember(@Param("eventId") Long eventId);
}
