package com.sisc_it.sisc_rookie_web.feedback.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sisc_it.sisc_rookie_web.event.domain.Event;
import com.sisc_it.sisc_rookie_web.event.repository.EventRepository;
import com.sisc_it.sisc_rookie_web.feedback.domain.Feedback;
import com.sisc_it.sisc_rookie_web.feedback.dto.FeedbackCreateRequest;
import com.sisc_it.sisc_rookie_web.feedback.dto.FeedbackResponse;
import com.sisc_it.sisc_rookie_web.feedback.repository.FeedbackRepository;
import com.sisc_it.sisc_rookie_web.global.exception.BusinessException;
import com.sisc_it.sisc_rookie_web.global.exception.ErrorCode;
import com.sisc_it.sisc_rookie_web.member.domain.Member;
import com.sisc_it.sisc_rookie_web.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final EventRepository eventRepository;
    private final MemberRepository memberRepository;

    /** 피드백 작성. 행사당 회원 1건만 작성할 수 있다. 빈 내용은 DTO 검증에서 차단된다. */
    @Transactional
    public FeedbackResponse createFeedback(Long eventId, FeedbackCreateRequest request, String memberEmail) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));
        Member member = memberRepository.findByEmail(memberEmail)
            .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (feedbackRepository.existsByEventIdAndMemberId(eventId, member.getId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_FEEDBACK);
        }

        Feedback feedback = feedbackRepository.save(new Feedback(event, member, request.content()));
        return FeedbackResponse.from(feedback);
    }

    /** [운영진] 특정 행사의 피드백 목록 조회. */
    public List<FeedbackResponse> getFeedbacks(Long eventId) {
        eventRepository.findById(eventId)
            .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));
        return feedbackRepository.findByEventIdOrderByCreatedAtDesc(eventId).stream()
            .map(FeedbackResponse::from)
            .toList();
    }

    /** [관리자] 전체 행사의 피드백 모아보기. */
    public List<FeedbackResponse> getAllFeedbacks() {
        return feedbackRepository.findAllByOrderByCreatedAtDesc().stream()
            .map(FeedbackResponse::from)
            .toList();
    }
}
