package com.sisc_it.sisc_rookie_web.event.dto;

import com.sisc_it.sisc_rookie_web.event.domain.EventStatus;

import jakarta.validation.constraints.NotNull;

public record EventStatusUpdateRequest(
    @NotNull(message = "변경할 상태는 필수입니다.")
    EventStatus status
) {
}
