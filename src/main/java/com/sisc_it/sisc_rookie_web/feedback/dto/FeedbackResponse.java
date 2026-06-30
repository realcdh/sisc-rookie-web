package com.sisc_it.sisc_rookie_web.feedback.dto;

import java.time.LocalDateTime;

import com.sisc_it.sisc_rookie_web.feedback.domain.Feedback;

public record FeedbackResponse(
    Long feedbackId,
    Long eventId,
    String eventTitle,
    Long memberId,
    String memberName,
    String content,
    LocalDateTime createdAt
) {

    public static FeedbackResponse from(Feedback feedback) {
        return new FeedbackResponse(
            feedback.getId(),
            feedback.getEvent().getId(),
            feedback.getEvent().getTitle(),
            feedback.getMember().getId(),
            feedback.getMember().getName(),
            feedback.getContent(),
            feedback.getCreatedAt()
        );
    }
}
