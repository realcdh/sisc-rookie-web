package com.sisc_it.sisc_rookie_web.attendance.dto;

import java.time.LocalDateTime;

import com.sisc_it.sisc_rookie_web.attendance.domain.AttendanceCode;

public record AttendanceCodeResponse(
    Long codeId,
    Long eventId,
    String code,
    boolean active,
    LocalDateTime expiresAt,
    LocalDateTime createdAt
) {

    public static AttendanceCodeResponse from(AttendanceCode attendanceCode) {
        return new AttendanceCodeResponse(
            attendanceCode.getId(),
            attendanceCode.getEvent().getId(),
            attendanceCode.getCode(),
            attendanceCode.isActive(),
            attendanceCode.getExpiresAt(),
            attendanceCode.getCreatedAt()
        );
    }
}
