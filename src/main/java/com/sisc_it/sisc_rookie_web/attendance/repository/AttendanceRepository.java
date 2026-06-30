package com.sisc_it.sisc_rookie_web.attendance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sisc_it.sisc_rookie_web.attendance.domain.Attendance;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    boolean existsByEventIdAndMemberId(Long eventId, Long memberId);

    // 특정 행사의 출석 명단을 출석한 순서대로 조회
    List<Attendance> findByEventIdOrderByCheckedInAtAsc(Long eventId);
}
