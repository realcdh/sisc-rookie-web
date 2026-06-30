package com.sisc_it.sisc_rookie_web.attendance.dto;

import java.time.LocalDateTime;

import com.sisc_it.sisc_rookie_web.attendance.domain.Attendance;

public record AttendanceResponse(
    Long attendanceId,
    Long eventId,
    Long memberId,
    String memberName,
    LocalDateTime checkedInAt
) {

    public static AttendanceResponse from(Attendance attendance) {
        return new AttendanceResponse(
            attendance.getId(),
            attendance.getEvent().getId(),
            attendance.getMember().getId(),
            attendance.getMember().getName(),
            attendance.getCheckedInAt()
        );
    }
}
