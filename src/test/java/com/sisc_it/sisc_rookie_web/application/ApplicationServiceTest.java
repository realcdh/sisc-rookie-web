package com.sisc_it.sisc_rookie_web.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.sisc_it.sisc_rookie_web.application.domain.ApplicationStatus;
import com.sisc_it.sisc_rookie_web.application.dto.ApplicationResponse;
import com.sisc_it.sisc_rookie_web.application.dto.ApplicationStatusUpdateRequest;
import com.sisc_it.sisc_rookie_web.application.service.ApplicationService;
import com.sisc_it.sisc_rookie_web.event.domain.Event;
import com.sisc_it.sisc_rookie_web.event.domain.EventStatus;
import com.sisc_it.sisc_rookie_web.event.repository.EventRepository;
import com.sisc_it.sisc_rookie_web.global.exception.BusinessException;
import com.sisc_it.sisc_rookie_web.global.exception.ErrorCode;
import com.sisc_it.sisc_rookie_web.member.domain.Member;
import com.sisc_it.sisc_rookie_web.member.domain.Role;
import com.sisc_it.sisc_rookie_web.member.repository.MemberRepository;

@SpringBootTest
@Transactional
class ApplicationServiceTest {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private MemberRepository memberRepository;

    private String memberEmail;
    private Long openEventId;
    private Long draftEventId;

    @BeforeEach
    void setUp() {
        Member admin = memberRepository.save(new Member("관리자", "app-admin@sisc.test", "hash", Role.ADMIN));
        Member member = memberRepository.save(new Member("부원", "app-member@sisc.test", "hash", Role.MEMBER));
        memberEmail = member.getEmail();

        openEventId = eventRepository.save(new Event("OPEN 세미나", "설명", EventStatus.OPEN, admin)).getId();
        draftEventId = eventRepository.save(new Event("DRAFT 세미나", "설명", EventStatus.DRAFT, admin)).getId();
    }

    @Test
    void applyCreatesPendingApplication() {
        ApplicationResponse response = applicationService.apply(openEventId, memberEmail);

        assertThat(response.applicationId()).isNotNull();
        assertThat(response.status()).isEqualTo(ApplicationStatus.PENDING);
        assertThat(response.memberName()).isEqualTo("부원");
    }

    @Test
    void applyRejectsNonOpenEvent() {
        assertThatThrownBy(() -> applicationService.apply(draftEventId, memberEmail))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.EVENT_NOT_OPEN);
    }

    @Test
    void applyRejectsDuplicateApplication() {
        applicationService.apply(openEventId, memberEmail);

        assertThatThrownBy(() -> applicationService.apply(openEventId, memberEmail))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.DUPLICATE_APPLICATION);
    }

    @Test
    void cancelMyApplicationSucceedsWhenPending() {
        applicationService.apply(openEventId, memberEmail);

        ApplicationResponse canceled = applicationService.cancelMyApplication(openEventId, memberEmail);

        assertThat(canceled.status()).isEqualTo(ApplicationStatus.CANCELED);
    }

    @Test
    void cancelMyApplicationRejectsNonPending() {
        ApplicationResponse applied = applicationService.apply(openEventId, memberEmail);
        // 운영진이 승인하면 더 이상 PENDING이 아니다.
        applicationService.review(openEventId, applied.applicationId(),
            new ApplicationStatusUpdateRequest(ApplicationStatus.APPROVED));

        assertThatThrownBy(() -> applicationService.cancelMyApplication(openEventId, memberEmail))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.CANNOT_CANCEL_APPLICATION);
    }

    @Test
    void reviewApprovesPendingApplication() {
        ApplicationResponse applied = applicationService.apply(openEventId, memberEmail);

        ApplicationResponse reviewed = applicationService.review(openEventId, applied.applicationId(),
            new ApplicationStatusUpdateRequest(ApplicationStatus.APPROVED));

        assertThat(reviewed.status()).isEqualTo(ApplicationStatus.APPROVED);
    }

    @Test
    void reviewRejectsNonPendingApplication() {
        ApplicationResponse applied = applicationService.apply(openEventId, memberEmail);
        applicationService.review(openEventId, applied.applicationId(),
            new ApplicationStatusUpdateRequest(ApplicationStatus.REJECTED));

        // 이미 반려된 신청을 다시 승인하려 하면 거부된다.
        assertThatThrownBy(() -> applicationService.review(openEventId, applied.applicationId(),
            new ApplicationStatusUpdateRequest(ApplicationStatus.APPROVED)))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.INVALID_APPLICATION_STATUS_CHANGE);
    }

    @Test
    void getApplicationsReturnsApplicantList() {
        applicationService.apply(openEventId, memberEmail);

        List<ApplicationResponse> applications = applicationService.getApplications(openEventId);

        assertThat(applications).hasSize(1);
        assertThat(applications.get(0).memberName()).isEqualTo("부원");
    }

    @Test
    void getMyApplicationThrowsWhenNotApplied() {
        assertThatThrownBy(() -> applicationService.getMyApplication(openEventId, memberEmail))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.APPLICATION_NOT_FOUND);
    }
}
