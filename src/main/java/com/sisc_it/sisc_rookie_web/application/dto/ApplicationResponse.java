package com.sisc_it.sisc_rookie_web.application.dto;

import java.time.LocalDateTime;

import com.sisc_it.sisc_rookie_web.application.domain.Application;
import com.sisc_it.sisc_rookie_web.application.domain.ApplicationStatus;

public record ApplicationResponse(
    Long applicationId,
    Long eventId,
    String eventTitle,
    Long memberId,
    String memberName,
    ApplicationStatus status,
    LocalDateTime appliedAt,
    boolean attended
) {

    public static ApplicationResponse from(Application application) {
        return new ApplicationResponse(
            application.getId(),
            application.getEvent().getId(),
            application.getEvent().getTitle(),
            application.getMember().getId(),
            application.getMember().getName(),
            application.getStatus(),
            application.getAppliedAt(),
            application.isAttended()
        );
    }
}
