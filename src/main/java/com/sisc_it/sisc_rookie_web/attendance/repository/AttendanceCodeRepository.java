package com.sisc_it.sisc_rookie_web.attendance.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sisc_it.sisc_rookie_web.attendance.domain.AttendanceCode;

public interface AttendanceCodeRepository extends JpaRepository<AttendanceCode, Long> {

    // 새 코드 발급 시 기존 활성 코드를 비활성화하기 위해 사용한다.
    List<AttendanceCode> findByEventIdAndActiveTrue(Long eventId);

    // 출석 코드 검증에 사용한다.
    Optional<AttendanceCode> findByEventIdAndCodeAndActiveTrue(Long eventId, String code);
}
