package com.sisc_it.sisc_rookie_web.attendance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.sisc_it.sisc_rookie_web.application.domain.Application;
import com.sisc_it.sisc_rookie_web.application.domain.ApplicationStatus;
import com.sisc_it.sisc_rookie_web.application.repository.ApplicationRepository;
import com.sisc_it.sisc_rookie_web.attendance.domain.AttendanceCode;
import com.sisc_it.sisc_rookie_web.attendance.dto.AttendanceCheckRequest;
import com.sisc_it.sisc_rookie_web.attendance.dto.AttendanceCodeCreateRequest;
import com.sisc_it.sisc_rookie_web.attendance.dto.AttendanceCodeResponse;
import com.sisc_it.sisc_rookie_web.attendance.dto.AttendanceResponse;
import com.sisc_it.sisc_rookie_web.attendance.repository.AttendanceCodeRepository;
import com.sisc_it.sisc_rookie_web.attendance.service.AttendanceCodeService;
import com.sisc_it.sisc_rookie_web.attendance.service.AttendanceService;
import com.sisc_it.sisc_rookie_web.event.domain.Event;
import com.sisc_it.sisc_rookie_web.event.domain.EventStatus;
import com.sisc_it.sisc_rookie_web.event.repository.EventRepository;
import com.sisc_it.sisc_rookie_web.global.exception.BusinessException;
import com.sisc_it.sisc_rookie_web.global.exception.ErrorCode;
import com.sisc_it.sisc_rookie_web.member.domain.Member;
import com.sisc_it.sisc_rookie_web.member.domain.Role;
import com.sisc_it.sisc_rookie_web.member.repository.MemberRepository;
import com.sisc_it.sisc_rookie_web.team.domain.Team;
import com.sisc_it.sisc_rookie_web.team.repository.TeamRepository;

@SpringBootTest
@Transactional
class AttendanceServiceTest {

    @Autowired
    private AttendanceService attendanceService;
    @Autowired
    private AttendanceCodeService attendanceCodeService;
    @Autowired
    private AttendanceCodeRepository attendanceCodeRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TeamRepository teamRepository;

    private Long eventId;
    private Event event;
    private String approvedEmail;
    private String notApprovedEmail;

    @BeforeEach
    void setUp() {
        Team team = teamRepository.save(new Team("알파팀"));
        Member admin = memberRepository.save(new Member("관리자", "att-admin@sisc.test", "hash", Role.ADMIN));
        Member approved = memberRepository.save(new Member("승인부원", "att-approved@sisc.test", "hash", Role.MEMBER));
        Member notApproved = memberRepository.save(new Member("미승인부원", "att-pending@sisc.test", "hash", Role.MEMBER));
        approvedEmail = approved.getEmail();
        notApprovedEmail = notApproved.getEmail();

        event = eventRepository.save(new Event("세미나", "설명", EventStatus.OPEN, admin));
        eventId = event.getId();

        // 승인된 신청 1건, 대기 중 신청 1건
        applicationRepository.save(new Application(event, approved, team, ApplicationStatus.APPROVED));
        applicationRepository.save(new Application(event, notApproved, team, ApplicationStatus.PENDING));
    }

    @Test
    void issueCreatesActiveSixDigitCode() {
        AttendanceCodeResponse response = attendanceCodeService.issue(eventId, new AttendanceCodeCreateRequest(null));

        assertThat(response.code()).hasSize(6);
        assertThat(response.active()).isTrue();
    }

    @Test
    void issueDeactivatesPreviousCode() {
        AttendanceCodeResponse first = attendanceCodeService.issue(eventId, new AttendanceCodeCreateRequest(null));
        attendanceCodeService.issue(eventId, new AttendanceCodeCreateRequest(null));

        // 새 코드 발급 후 이전 코드로는 출석할 수 없다.
        assertThatThrownBy(() -> attendanceService.checkIn(
            eventId, new AttendanceCheckRequest(first.code()), approvedEmail))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.INVALID_ATTENDANCE_CODE);
    }

    @Test
    void checkInSucceedsForApprovedMemberWithValidCode() {
        AttendanceCodeResponse code = attendanceCodeService.issue(eventId, new AttendanceCodeCreateRequest(null));

        AttendanceResponse response = attendanceService.checkIn(
            eventId, new AttendanceCheckRequest(code.code()), approvedEmail);

        assertThat(response.attendanceId()).isNotNull();
        assertThat(response.memberName()).isEqualTo("승인부원");
    }

    @Test
    void checkInRejectsNotApprovedMember() {
        AttendanceCodeResponse code = attendanceCodeService.issue(eventId, new AttendanceCodeCreateRequest(null));

        assertThatThrownBy(() -> attendanceService.checkIn(
            eventId, new AttendanceCheckRequest(code.code()), notApprovedEmail))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.NOT_APPROVED_FOR_ATTENDANCE);
    }

    @Test
    void checkInRejectsInvalidCode() {
        attendanceCodeService.issue(eventId, new AttendanceCodeCreateRequest(null));

        assertThatThrownBy(() -> attendanceService.checkIn(
            eventId, new AttendanceCheckRequest("000000"), approvedEmail))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.INVALID_ATTENDANCE_CODE);
    }

    @Test
    void checkInRejectsDuplicateAttendance() {
        AttendanceCodeResponse code = attendanceCodeService.issue(eventId, new AttendanceCodeCreateRequest(null));
        attendanceService.checkIn(eventId, new AttendanceCheckRequest(code.code()), approvedEmail);

        assertThatThrownBy(() -> attendanceService.checkIn(
            eventId, new AttendanceCheckRequest(code.code()), approvedEmail))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.ALREADY_ATTENDED);
    }

    @Test
    void checkInRejectsExpiredCode() {
        // 이미 만료된 코드를 직접 저장한다.
        AttendanceCode expired = attendanceCodeRepository.save(
            new AttendanceCode(event, "999999", LocalDateTime.now().minusMinutes(1)));

        assertThatThrownBy(() -> attendanceService.checkIn(
            eventId, new AttendanceCheckRequest(expired.getCode()), approvedEmail))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.INVALID_ATTENDANCE_CODE);
    }

    @Test
    void getAttendancesReturnsCheckedInMembers() {
        AttendanceCodeResponse code = attendanceCodeService.issue(eventId, new AttendanceCodeCreateRequest(null));
        attendanceService.checkIn(eventId, new AttendanceCheckRequest(code.code()), approvedEmail);

        List<AttendanceResponse> attendances = attendanceService.getAttendances(eventId);

        assertThat(attendances).hasSize(1);
        assertThat(attendances.get(0).memberName()).isEqualTo("승인부원");
    }
}
