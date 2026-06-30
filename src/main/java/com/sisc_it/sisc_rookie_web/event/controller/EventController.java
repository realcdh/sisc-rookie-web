package com.sisc_it.sisc_rookie_web.event.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sisc_it.sisc_rookie_web.event.domain.EventStatus;
import com.sisc_it.sisc_rookie_web.event.dto.EventCreateRequest;
import com.sisc_it.sisc_rookie_web.event.dto.EventResponse;
import com.sisc_it.sisc_rookie_web.event.dto.EventStatusUpdateRequest;
import com.sisc_it.sisc_rookie_web.event.dto.EventUpdateRequest;
import com.sisc_it.sisc_rookie_web.event.service.EventService;
import com.sisc_it.sisc_rookie_web.global.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class EventController {

    private final EventService eventService;

    // ---------- 조회: 로그인한 모든 사용자(MEMBER 이상) ----------

    @GetMapping("/events")
    public ResponseEntity<ApiResponse<List<EventResponse>>> getEvents(
        @RequestParam(name = "status", required = false) EventStatus status
    ) {
        List<EventResponse> events = eventService.getEvents(status);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "성공", events));
    }

    @GetMapping("/events/{eventId}")
    public ResponseEntity<ApiResponse<EventResponse>> getEvent(@PathVariable Long eventId) {
        EventResponse event = eventService.getEvent(eventId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "성공", event));
    }

    // ---------- 관리: ADMIN 전용 ----------

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/events")
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(
        @Valid @RequestBody EventCreateRequest request,
        Principal principal
    ) {
        EventResponse event = eventService.createEvent(request, principal.getName());
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(HttpStatus.CREATED.value(), "행사가 생성되었습니다.", event));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/events/{eventId}")
    public ResponseEntity<ApiResponse<EventResponse>> updateEvent(
        @PathVariable Long eventId,
        @Valid @RequestBody EventUpdateRequest request
    ) {
        EventResponse event = eventService.updateEvent(eventId, request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "행사가 수정되었습니다.", event));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/admin/events/{eventId}/status")
    public ResponseEntity<ApiResponse<EventResponse>> updateStatus(
        @PathVariable Long eventId,
        @Valid @RequestBody EventStatusUpdateRequest request
    ) {
        EventResponse event = eventService.updateStatus(eventId, request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "행사 상태가 변경되었습니다.", event));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/events/{eventId}")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(@PathVariable Long eventId) {
        eventService.deleteEvent(eventId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "행사가 삭제되었습니다.", null));
    }
}
