package com.sisc_it.sisc_rookie_web.attendance.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sisc_it.sisc_rookie_web.attendance.dto.AttendanceCheckRequest;
import com.sisc_it.sisc_rookie_web.attendance.dto.AttendanceCodeCreateRequest;
import com.sisc_it.sisc_rookie_web.attendance.dto.AttendanceCodeResponse;
import com.sisc_it.sisc_rookie_web.attendance.dto.AttendanceResponse;
import com.sisc_it.sisc_rookie_web.attendance.service.AttendanceCodeService;
import com.sisc_it.sisc_rookie_web.attendance.service.AttendanceService;
import com.sisc_it.sisc_rookie_web.global.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class AttendanceController {

    private final AttendanceCodeService attendanceCodeService;
    private final AttendanceService attendanceService;

    /** [운영진] 출석 코드 발급. 요청 본문(만료 시간)은 선택값이다. */
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @PostMapping("/admin/events/{eventId}/attendance-codes")
    public ResponseEntity<ApiResponse<AttendanceCodeResponse>> issueCode(
        @PathVariable Long eventId,
        @Valid @RequestBody(required = false) AttendanceCodeCreateRequest request
    ) {
        AttendanceCodeCreateRequest safeRequest =
            (request == null) ? new AttendanceCodeCreateRequest(null) : request;
        AttendanceCodeResponse response = attendanceCodeService.issue(eventId, safeRequest);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(HttpStatus.CREATED.value(), "출석 코드가 발급되었습니다.", response));
    }

    /** 출석 체크. 승인된 신청자가 유효한 코드를 입력해야 한다. */
    @PostMapping("/events/{eventId}/attendances")
    public ResponseEntity<ApiResponse<AttendanceResponse>> checkIn(
        @PathVariable Long eventId,
        @Valid @RequestBody AttendanceCheckRequest request,
        Principal principal
    ) {
        AttendanceResponse response = attendanceService.checkIn(eventId, request, principal.getName());
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(HttpStatus.CREATED.value(), "출석이 완료되었습니다.", response));
    }

    /** [운영진] 출석 현황 및 명단 조회. */
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @GetMapping("/events/{eventId}/attendances")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getAttendances(@PathVariable Long eventId) {
        List<AttendanceResponse> responses = attendanceService.getAttendances(eventId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "성공", responses));
    }
}
