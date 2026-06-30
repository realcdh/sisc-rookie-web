package com.sisc_it.sisc_rookie_web.attendance.dto;

import jakarta.validation.constraints.Positive;

/**
 * 출석 코드 발급 요청. 코드는 서버가 자동 생성한다.
 * expireMinutes를 지정하면 발급 시점부터 해당 분(分) 이후 만료되며, null이면 만료되지 않는다.
 */
public record AttendanceCodeCreateRequest(
    @Positive(message = "만료 시간(분)은 1 이상이어야 합니다.")
    Integer expireMinutes
) {
}
