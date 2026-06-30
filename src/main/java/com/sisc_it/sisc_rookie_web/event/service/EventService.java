package com.sisc_it.sisc_rookie_web.event.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sisc_it.sisc_rookie_web.event.domain.Event;
import com.sisc_it.sisc_rookie_web.event.domain.EventStatus;
import com.sisc_it.sisc_rookie_web.event.dto.EventCreateRequest;
import com.sisc_it.sisc_rookie_web.event.dto.EventResponse;
import com.sisc_it.sisc_rookie_web.event.dto.EventStatusUpdateRequest;
import com.sisc_it.sisc_rookie_web.event.dto.EventUpdateRequest;
import com.sisc_it.sisc_rookie_web.event.repository.EventRepository;
import com.sisc_it.sisc_rookie_web.global.exception.BusinessException;
import com.sisc_it.sisc_rookie_web.global.exception.ErrorCode;
import com.sisc_it.sisc_rookie_web.member.domain.Member;
import com.sisc_it.sisc_rookie_web.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;
    private final MemberRepository memberRepository;

    /** 행사 목록 조회. status가 null이면 전체, 지정 시 해당 상태만 반환한다. */
    public List<EventResponse> getEvents(EventStatus status) {
        List<Event> events = (status == null)
            ? eventRepository.findAllByOrderByCreatedAtDesc()
            : eventRepository.findAllByStatusOrderByCreatedAtDesc(status);

        return events.stream()
            .map(EventResponse::from)
            .toList();
    }

    /** 행사 상세 조회. */
    public EventResponse getEvent(Long eventId) {
        Event event = findEvent(eventId);
        return EventResponse.from(event);
    }

    /** 행사 생성. 생성자는 현재 로그인한 관리자다. 초기 상태는 DRAFT. */
    @Transactional
    public EventResponse createEvent(EventCreateRequest request, String creatorEmail) {
        Member creator = findMemberByEmail(creatorEmail);

        Event event = new Event(
            request.title(),
            request.description(),
            request.capacity(),
            request.location(),
            request.startAt(),
            EventStatus.DRAFT,
            creator
        );

        return EventResponse.from(eventRepository.save(event));
    }

    /** 행사 상세 내용 수정. */
    @Transactional
    public EventResponse updateEvent(Long eventId, EventUpdateRequest request) {
        Event event = findEvent(eventId);
        event.updateDetails(
            request.title(),
            request.description(),
            request.capacity(),
            request.location(),
            request.startAt()
        );
        return EventResponse.from(event);
    }

    /** 행사 상태 전이. 허용되지 않는 전이는 도메인에서 예외를 던진다. */
    @Transactional
    public EventResponse updateStatus(Long eventId, EventStatusUpdateRequest request) {
        Event event = findEvent(eventId);
        event.changeStatus(request.status());
        return EventResponse.from(event);
    }

    /** 행사 삭제. */
    @Transactional
    public void deleteEvent(Long eventId) {
        Event event = findEvent(eventId);
        eventRepository.delete(event);
    }

    private Event findEvent(Long eventId) {
        return eventRepository.findById(eventId)
            .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));
    }

    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
            .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
