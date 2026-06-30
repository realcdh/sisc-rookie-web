package com.sisc_it.sisc_rookie_web.application.dto;

import com.sisc_it.sisc_rookie_web.application.domain.ApplicationStatus;

import jakarta.validation.constraints.NotNull;

/**
 * 운영진의 신청 승인/반려 요청.
 * status에는 APPROVED 또는 REJECTED만 의미가 있으며, 그 외 값은 서비스에서 거부된다.
 */
public record ApplicationStatusUpdateRequest(
    @NotNull(message = "변경할 상태는 필수입니다.")
    ApplicationStatus status
) {
}
