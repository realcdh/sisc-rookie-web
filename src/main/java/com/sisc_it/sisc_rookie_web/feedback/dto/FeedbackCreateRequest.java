package com.sisc_it.sisc_rookie_web.feedback.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FeedbackCreateRequest(
    // 빈 피드백 저장 차단(PRD 필수 시나리오)
    @NotBlank(message = "피드백 내용은 비어 있을 수 없습니다.")
    @Size(max = 2000, message = "피드백은 2000자 이하여야 합니다.")
    String content
) {
}
