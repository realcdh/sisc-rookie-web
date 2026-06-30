package com.sisc_it.sisc_rookie_web.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sisc_it.sisc_rookie_web.application.domain.Application;
import com.sisc_it.sisc_rookie_web.application.dto.ApplicationResponse;
import com.sisc_it.sisc_rookie_web.application.dto.ApplicationStatusUpdateRequest;
import com.sisc_it.sisc_rookie_web.application.repository.ApplicationRepository;
import com.sisc_it.sisc_rookie_web.event.domain.Event;
import com.sisc_it.sisc_rookie_web.event.domain.EventStatus;
import com.sisc_it.sisc_rookie_web.event.repository.EventRepository;
import com.sisc_it.sisc_rookie_web.global.exception.BusinessException;
import com.sisc_it.sisc_rookie_web.global.exception.ErrorCode;
import com.sisc_it.sisc_rookie_web.member.domain.Member;
import com.sisc_it.sisc_rookie_web.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final EventRepository eventRepository;
    private final MemberRepository memberRepository;

    /**
     * 행사 신청. OPEN 상태의 행사에만, 회원당 한 번만 신청할 수 있다.
     */
    @Transactional
    public ApplicationResponse apply(Long eventId, String memberEmail) {
        Member member = findMember(memberEmail);
        Event event = findEvent(eventId);

        if (event.getStatus() != EventStatus.OPEN) {
            throw new BusinessException(ErrorCode.EVENT_NOT_OPEN);
        }
        if (applicationRepository.existsByEventIdAndMemberId(eventId, member.getId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_APPLICATION);
        }

        Application application = applicationRepository.save(new Application(event, member));
        return ApplicationResponse.from(application);
    }

    /** 내 신청 상태 조회. */
    public ApplicationResponse getMyApplication(Long eventId, String memberEmail) {
        Member member = findMember(memberEmail);
        Application application = applicationRepository.findByEventIdAndMemberId(eventId, member.getId())
            .orElseThrow(() -> new BusinessException(ErrorCode.APPLICATION_NOT_FOUND));
        return ApplicationResponse.from(application);
    }

    /** 내 신청 취소. 대기(PENDING) 상태에서만 가능하다. */
    @Transactional
    public ApplicationResponse cancelMyApplication(Long eventId, String memberEmail) {
        Member member = findMember(memberEmail);
        Application application = applicationRepository.findByEventIdAndMemberId(eventId, member.getId())
            .orElseThrow(() -> new BusinessException(ErrorCode.APPLICATION_NOT_FOUND));
        application.cancel();
        return ApplicationResponse.from(application);
    }

    /** [운영진] 해당 행사의 신청자 전체 목록 조회. */
    public List<ApplicationResponse> getApplications(Long eventId) {
        findEvent(eventId); // 존재하지 않는 행사면 404
        return applicationRepository.findByEventIdWithMember(eventId).stream()
            .map(ApplicationResponse::from)
            .toList();
    }

    /** [운영진] 신청 승인/반려. 대기(PENDING) 상태에서만 APPROVED/REJECTED로 변경할 수 있다. */
    @Transactional
    public ApplicationResponse review(Long eventId, Long applicationId, ApplicationStatusUpdateRequest request) {
        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new BusinessException(ErrorCode.APPLICATION_NOT_FOUND));

        // 경로의 행사와 신청의 행사가 일치하지 않으면 잘못된 접근이다.
        if (!application.getEvent().getId().equals(eventId)) {
            throw new BusinessException(ErrorCode.APPLICATION_NOT_FOUND);
        }

        application.review(request.status());
        return ApplicationResponse.from(application);
    }

    private Event findEvent(Long eventId) {
        return eventRepository.findById(eventId)
            .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));
    }

    private Member findMember(String email) {
        return memberRepository.findByEmail(email)
            .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
