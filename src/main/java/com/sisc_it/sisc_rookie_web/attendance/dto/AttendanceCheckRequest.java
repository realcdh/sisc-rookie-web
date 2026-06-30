package com.sisc_it.sisc_rookie_web.attendance.dto;

import jakarta.validation.constraints.NotBlank;

public record AttendanceCheckRequest(
    @NotBlank(message = "출석 코드는 필수입니다.")
    String code
) {
}
