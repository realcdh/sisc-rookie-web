package com.sisc_it.sisc_rookie_web.event.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record EventUpdateRequest(
    @NotBlank(message = "행사 제목은 필수입니다.")
    String title,

    @NotBlank(message = "행사 설명은 필수입니다.")
    String description,

    @Positive(message = "정원은 1 이상이어야 합니다.")
    Integer capacity,

    String location,

    LocalDateTime startAt
) {
}
