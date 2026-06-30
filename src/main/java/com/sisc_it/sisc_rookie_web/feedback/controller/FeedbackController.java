package com.sisc_it.sisc_rookie_web.feedback.controller;

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

import com.sisc_it.sisc_rookie_web.feedback.dto.FeedbackCreateRequest;
import com.sisc_it.sisc_rookie_web.feedback.dto.FeedbackResponse;
import com.sisc_it.sisc_rookie_web.feedback.service.FeedbackService;
import com.sisc_it.sisc_rookie_web.global.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class FeedbackController {

    private final FeedbackService feedbackService;

    /** 행사 종료 후 피드백 작성(MEMBER 이상). */
    @PostMapping("/events/{eventId}/feedbacks")
    public ResponseEntity<ApiResponse<FeedbackResponse>> createFeedback(
        @PathVariable Long eventId,
        @Valid @RequestBody FeedbackCreateRequest request,
        Principal principal
    ) {
        FeedbackResponse response = feedbackService.createFeedback(eventId, request, principal.getName());
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(HttpStatus.CREATED.value(), "피드백이 등록되었습니다.", response));
    }

    /** [운영진] 특정 행사의 피드백 목록 조회. */
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @GetMapping("/events/{eventId}/feedbacks")
    public ResponseEntity<ApiResponse<List<FeedbackResponse>>> getFeedbacks(@PathVariable Long eventId) {
        List<FeedbackResponse> responses = feedbackService.getFeedbacks(eventId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "성공", responses));
    }

    /** [관리자] 전체 피드백 모아보기. */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/feedbacks")
    public ResponseEntity<ApiResponse<List<FeedbackResponse>>> getAllFeedbacks() {
        List<FeedbackResponse> responses = feedbackService.getAllFeedbacks();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "성공", responses));
    }
}
