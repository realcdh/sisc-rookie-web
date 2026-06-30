package com.sisc_it.sisc_rookie_web.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.sisc_it.sisc_rookie_web.event.domain.EventStatus;
import com.sisc_it.sisc_rookie_web.event.dto.EventCreateRequest;
import com.sisc_it.sisc_rookie_web.event.dto.EventResponse;
import com.sisc_it.sisc_rookie_web.event.dto.EventStatusUpdateRequest;
import com.sisc_it.sisc_rookie_web.event.dto.EventUpdateRequest;
import com.sisc_it.sisc_rookie_web.event.service.EventService;
import com.sisc_it.sisc_rookie_web.global.exception.BusinessException;
import com.sisc_it.sisc_rookie_web.global.exception.ErrorCode;
import com.sisc_it.sisc_rookie_web.member.domain.Member;
import com.sisc_it.sisc_rookie_web.member.domain.Role;
import com.sisc_it.sisc_rookie_web.member.repository.MemberRepository;

@SpringBootTest
@Transactional
class EventServiceTest {

    @Autowired
    private EventService eventService;

    @Autowired
    private MemberRepository memberRepository;

    private String adminEmail;

    @BeforeEach
    void setUp() {
        Member admin = memberRepository.save(
            new Member("관리자", "event-admin@sisc.test", "hashed-password", Role.ADMIN));
        adminEmail = admin.getEmail();
    }

    @Test
    void createEventStartsAsDraft() {
        EventCreateRequest request = new EventCreateRequest(
            "기업분석 세미나", "삼성전자 분석", 30, "301호", LocalDateTime.now().plusDays(5));

        EventResponse response = eventService.createEvent(request, adminEmail);

        assertThat(response.eventId()).isNotNull();
        assertThat(response.status()).isEqualTo(EventStatus.DRAFT);
        assertThat(response.capacity()).isEqualTo(30);
        assertThat(response.createdByName()).isEqualTo("관리자");
    }

    @Test
    void updateStatusAllowsValidTransition() {
        EventResponse created = eventService.createEvent(
            new EventCreateRequest("OT", "신입 OT", null, null, null), adminEmail);

        EventResponse updated = eventService.updateStatus(
            created.eventId(), new EventStatusUpdateRequest(EventStatus.OPEN));

        assertThat(updated.status()).isEqualTo(EventStatus.OPEN);
    }

    @Test
    void updateStatusRejectsInvalidTransition() {
        EventResponse created = eventService.createEvent(
            new EventCreateRequest("OT", "신입 OT", null, null, null), adminEmail);

        // DRAFT -> COMPLETED 는 허용되지 않는 전이다.
        assertThatThrownBy(() -> eventService.updateStatus(
            created.eventId(), new EventStatusUpdateRequest(EventStatus.COMPLETED)))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.INVALID_EVENT_STATUS_TRANSITION);
    }

    @Test
    void getEventsFiltersByStatus() {
        eventService.createEvent(new EventCreateRequest("A", "draft 행사", null, null, null), adminEmail);
        EventResponse open = eventService.createEvent(
            new EventCreateRequest("B", "open 행사", null, null, null), adminEmail);
        eventService.updateStatus(open.eventId(), new EventStatusUpdateRequest(EventStatus.OPEN));

        List<EventResponse> openEvents = eventService.getEvents(EventStatus.OPEN);

        assertThat(openEvents).hasSize(1);
        assertThat(openEvents.get(0).status()).isEqualTo(EventStatus.OPEN);
    }

    @Test
    void updateEventChangesDetails() {
        EventResponse created = eventService.createEvent(
            new EventCreateRequest("초안 제목", "초안 설명", null, null, null), adminEmail);

        EventResponse updated = eventService.updateEvent(created.eventId(),
            new EventUpdateRequest("수정 제목", "수정 설명", 100, "대강당", null));

        assertThat(updated.title()).isEqualTo("수정 제목");
        assertThat(updated.capacity()).isEqualTo(100);
        assertThat(updated.location()).isEqualTo("대강당");
    }

    @Test
    void getEventThrowsWhenNotFound() {
        assertThatThrownBy(() -> eventService.getEvent(999_999L))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.EVENT_NOT_FOUND);
    }

    @Test
    void deleteEventRemovesEvent() {
        EventResponse created = eventService.createEvent(
            new EventCreateRequest("삭제 대상", "설명", null, null, null), adminEmail);

        eventService.deleteEvent(created.eventId());

        assertThatThrownBy(() -> eventService.getEvent(created.eventId()))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.EVENT_NOT_FOUND);
    }
}
