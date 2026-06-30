package com.sisc_it.sisc_rookie_web.feedback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.sisc_it.sisc_rookie_web.event.domain.Event;
import com.sisc_it.sisc_rookie_web.event.domain.EventStatus;
import com.sisc_it.sisc_rookie_web.event.repository.EventRepository;
import com.sisc_it.sisc_rookie_web.feedback.dto.FeedbackCreateRequest;
import com.sisc_it.sisc_rookie_web.feedback.dto.FeedbackResponse;
import com.sisc_it.sisc_rookie_web.feedback.service.FeedbackService;
import com.sisc_it.sisc_rookie_web.global.exception.BusinessException;
import com.sisc_it.sisc_rookie_web.global.exception.ErrorCode;
import com.sisc_it.sisc_rookie_web.member.domain.Member;
import com.sisc_it.sisc_rookie_web.member.domain.Role;
import com.sisc_it.sisc_rookie_web.member.repository.MemberRepository;

@SpringBootTest
@Transactional
class FeedbackServiceTest {

    @Autowired
    private FeedbackService feedbackService;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private MemberRepository memberRepository;

    private Long eventId;
    private String memberEmail;

    @BeforeEach
    void setUp() {
        Member admin = memberRepository.save(new Member("관리자", "fb-admin@sisc.test", "hash", Role.ADMIN));
        Member member = memberRepository.save(new Member("부원", "fb-member@sisc.test", "hash", Role.MEMBER));
        memberEmail = member.getEmail();
        Event event = eventRepository.save(new Event("세미나", "설명", EventStatus.COMPLETED, admin));
        eventId = event.getId();
    }

    @Test
    void createFeedbackSavesContent() {
        FeedbackResponse response = feedbackService.createFeedback(
            eventId, new FeedbackCreateRequest("실습이 더 있었으면 좋겠습니다."), memberEmail);

        assertThat(response.feedbackId()).isNotNull();
        assertThat(response.content()).isEqualTo("실습이 더 있었으면 좋겠습니다.");
        assertThat(response.memberName()).isEqualTo("부원");
    }

    @Test
    void createFeedbackRejectsDuplicate() {
        feedbackService.createFeedback(eventId, new FeedbackCreateRequest("좋았습니다."), memberEmail);

        assertThatThrownBy(() -> feedbackService.createFeedback(
            eventId, new FeedbackCreateRequest("또 작성"), memberEmail))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.DUPLICATE_FEEDBACK);
    }

    @Test
    void createFeedbackThrowsWhenEventNotFound() {
        assertThatThrownBy(() -> feedbackService.createFeedback(
            999_999L, new FeedbackCreateRequest("내용"), memberEmail))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.EVENT_NOT_FOUND);
    }

    @Test
    void getFeedbacksReturnsEventFeedbacks() {
        feedbackService.createFeedback(eventId, new FeedbackCreateRequest("내용"), memberEmail);

        List<FeedbackResponse> feedbacks = feedbackService.getFeedbacks(eventId);

        assertThat(feedbacks).hasSize(1);
        assertThat(feedbacks.get(0).eventId()).isEqualTo(eventId);
    }

    @Test
    void getAllFeedbacksReturnsEveryFeedback() {
        feedbackService.createFeedback(eventId, new FeedbackCreateRequest("내용"), memberEmail);

        List<FeedbackResponse> all = feedbackService.getAllFeedbacks();

        assertThat(all).hasSize(1);
    }

    @Test
    void getFeedbacksThrowsWhenEventNotFound() {
        assertThatThrownBy(() -> feedbackService.getFeedbacks(999_999L))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.EVENT_NOT_FOUND);
    }
}
