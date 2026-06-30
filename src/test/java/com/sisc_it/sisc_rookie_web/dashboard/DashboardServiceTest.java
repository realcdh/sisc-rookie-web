package com.sisc_it.sisc_rookie_web.dashboard;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.sisc_it.sisc_rookie_web.application.domain.Application;
import com.sisc_it.sisc_rookie_web.application.domain.ApplicationStatus;
import com.sisc_it.sisc_rookie_web.application.repository.ApplicationRepository;
import com.sisc_it.sisc_rookie_web.attendance.domain.Attendance;
import com.sisc_it.sisc_rookie_web.attendance.repository.AttendanceRepository;
import com.sisc_it.sisc_rookie_web.dashboard.dto.DashboardResponse;
import com.sisc_it.sisc_rookie_web.dashboard.service.DashboardService;
import com.sisc_it.sisc_rookie_web.event.domain.Event;
import com.sisc_it.sisc_rookie_web.event.domain.EventStatus;
import com.sisc_it.sisc_rookie_web.event.repository.EventRepository;
import com.sisc_it.sisc_rookie_web.feedback.domain.Feedback;
import com.sisc_it.sisc_rookie_web.feedback.repository.FeedbackRepository;
import com.sisc_it.sisc_rookie_web.member.domain.Member;
import com.sisc_it.sisc_rookie_web.member.domain.Role;
import com.sisc_it.sisc_rookie_web.member.repository.MemberRepository;
import com.sisc_it.sisc_rookie_web.team.domain.Team;
import com.sisc_it.sisc_rookie_web.team.repository.TeamRepository;

@SpringBootTest
@Transactional
class DashboardServiceTest {

    @Autowired
    private DashboardService dashboardService;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private FeedbackRepository feedbackRepository;
    @Autowired
    private AttendanceRepository attendanceRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TeamRepository teamRepository;

    @Test
    void dashboardAggregatesCountsCorrectly() {
        Team team = teamRepository.save(new Team("집계팀"));
        Member admin = memberRepository.save(new Member("관리자", "dash-admin@sisc.test", "hash", Role.ADMIN));
        Member m1 = memberRepository.save(new Member("부원1", "dash-m1@sisc.test", "hash", Role.MEMBER));
        Member m2 = memberRepository.save(new Member("부원2", "dash-m2@sisc.test", "hash", Role.MEMBER));

        Event open1 = eventRepository.save(new Event("OPEN1", "설명", EventStatus.OPEN, admin));
        Event open2 = eventRepository.save(new Event("OPEN2", "설명", EventStatus.OPEN, admin));
        eventRepository.save(new Event("DRAFT", "설명", EventStatus.DRAFT, admin));

        // 신청 3건: open1-m1(APPROVED), open1-m2(APPROVED), open2-m1(PENDING)
        applicationRepository.save(new Application(open1, m1, team, ApplicationStatus.APPROVED));
        applicationRepository.save(new Application(open1, m2, team, ApplicationStatus.APPROVED));
        applicationRepository.save(new Application(open2, m1, team, ApplicationStatus.PENDING));

        // 출석 1건(통합 환경에서는 Attendance 테이블로 출석 수를 집계)
        attendanceRepository.save(new Attendance(open1, m1));

        feedbackRepository.save(new Feedback(open1, m1, "좋았습니다."));

        DashboardResponse dashboard = dashboardService.getDashboard();

        assertThat(dashboard.totalEvents()).isEqualTo(3);
        assertThat(dashboard.openEvents()).isEqualTo(2);
        assertThat(dashboard.totalApplications()).isEqualTo(3);
        assertThat(dashboard.approvedApplications()).isEqualTo(2);
        assertThat(dashboard.attendanceCount()).isEqualTo(1);
        assertThat(dashboard.totalFeedbacks()).isEqualTo(1);
    }

    @Test
    void dashboardReturnsZeroWhenEmpty() {
        DashboardResponse dashboard = dashboardService.getDashboard();

        assertThat(dashboard.totalEvents()).isZero();
        assertThat(dashboard.openEvents()).isZero();
        assertThat(dashboard.totalApplications()).isZero();
        assertThat(dashboard.approvedApplications()).isZero();
        assertThat(dashboard.attendanceCount()).isZero();
        assertThat(dashboard.totalFeedbacks()).isZero();
    }
}
