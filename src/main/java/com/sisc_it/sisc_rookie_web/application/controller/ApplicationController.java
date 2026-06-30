package com.sisc_it.sisc_rookie_web.application.controller;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sisc_it.sisc_rookie_web.application.dto.ApplicationResponse;
import com.sisc_it.sisc_rookie_web.application.dto.ApplicationStatusUpdateRequest;
import com.sisc_it.sisc_rookie_web.application.service.ApplicationService;
import com.sisc_it.sisc_rookie_web.global.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/events/{eventId}/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    // ---------- 신청자 본인(MEMBER 이상, 인증된 사용자) ----------

    @PostMapping
    public ResponseEntity<ApiResponse<ApplicationResponse>> apply(
        @PathVariable Long eventId,
        Principal principal
    ) {
        ApplicationResponse response = applicationService.apply(eventId, principal.getName());
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(HttpStatus.CREATED.value(), "행사 신청이 완료되었습니다.", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<ApplicationResponse>> getMyApplication(
        @PathVariable Long eventId,
        Principal principal
    ) {
        ApplicationResponse response = applicationService.getMyApplication(eventId, principal.getName());
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "성공", response));
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<ApplicationResponse>> cancelMyApplication(
        @PathVariable Long eventId,
        Principal principal
    ) {
        ApplicationResponse response = applicationService.cancelMyApplication(eventId, principal.getName());
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "신청이 취소되었습니다.", response));
    }

    // ---------- 운영진(STAFF/ADMIN) ----------

    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getApplications(@PathVariable Long eventId) {
        List<ApplicationResponse> responses = applicationService.getApplications(eventId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "성공", responses));
    }

    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @PatchMapping("/{applicationId}/status")
    public ResponseEntity<ApiResponse<ApplicationResponse>> reviewApplication(
        @PathVariable Long eventId,
        @PathVariable Long applicationId,
        @Valid @RequestBody ApplicationStatusUpdateRequest request
    ) {
        ApplicationResponse response = applicationService.review(eventId, applicationId, request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "신청 상태가 변경되었습니다.", response));
    }
}
