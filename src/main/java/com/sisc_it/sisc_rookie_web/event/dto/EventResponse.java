package com.sisc_it.sisc_rookie_web.event.dto;

import java.time.LocalDateTime;

import com.sisc_it.sisc_rookie_web.event.domain.Event;
import com.sisc_it.sisc_rookie_web.event.domain.EventStatus;

public record EventResponse(
    Long eventId,
    String title,
    String description,
    Integer capacity,
    String location,
    LocalDateTime startAt,
    EventStatus status,
    LocalDateTime createdAt,
    Long createdById,
    String createdByName
) {

    public static EventResponse from(Event event) {
        return new EventResponse(
            event.getId(),
            event.getTitle(),
            event.getDescription(),
            event.getCapacity(),
            event.getLocation(),
            event.getStartAt(),
            event.getStatus(),
            event.getCreatedAt(),
            event.getCreatedBy().getId(),
            event.getCreatedBy().getName()
        );
    }
}
