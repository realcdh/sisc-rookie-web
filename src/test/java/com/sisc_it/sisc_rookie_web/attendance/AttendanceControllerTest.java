package com.sisc_it.sisc_rookie_web.attendance;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.sisc_it.sisc_rookie_web.application.domain.Application;
import com.sisc_it.sisc_rookie_web.application.domain.ApplicationStatus;
import com.sisc_it.sisc_rookie_web.application.repository.ApplicationRepository;
import com.sisc_it.sisc_rookie_web.attendance.dto.AttendanceCodeCreateRequest;
import com.sisc_it.sisc_rookie_web.attendance.service.AttendanceCodeService;
import com.sisc_it.sisc_rookie_web.event.domain.Event;
import com.sisc_it.sisc_rookie_web.event.domain.EventStatus;
import com.sisc_it.sisc_rookie_web.event.repository.EventRepository;
import com.sisc_it.sisc_rookie_web.member.domain.Member;
import com.sisc_it.sisc_rookie_web.member.domain.Role;
import com.sisc_it.sisc_rookie_web.member.repository.MemberRepository;
import com.sisc_it.sisc_rookie_web.team.domain.Team;
import com.sisc_it.sisc_rookie_web.team.repository.TeamRepository;

@SpringBootTest
@Transactional
class AttendanceControllerTest {

    private static final String STAFF_EMAIL = "att-ctrl-staff@sisc.test";
    private static final String APPROVED_EMAIL = "att-ctrl-approved@sisc.test";

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private AttendanceCodeService attendanceCodeService;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TeamRepository teamRepository;

    private Long eventId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();

        Team team = teamRepository.save(new Team("베타팀"));
        Member admin = memberRepository.save(new Member("관리자", "att-ctrl-admin@sisc.test", "hash", Role.ADMIN));
        memberRepository.save(new Member("운영진", STAFF_EMAIL, "hash", Role.STAFF));
        Member approved = memberRepository.save(new Member("승인부원", APPROVED_EMAIL, "hash", Role.MEMBER));

        Event event = eventRepository.save(new Event("세미나", "설명", EventStatus.OPEN, admin));
        eventId = event.getId();
        applicationRepository.save(new Application(event, approved, team, ApplicationStatus.APPROVED));
    }

    @Test
    @WithMockUser(username = STAFF_EMAIL, roles = "STAFF")
    void staffCanIssueAttendanceCode() throws Exception {
        mockMvc.perform(post("/api/v1/admin/events/{eventId}/attendance-codes", eventId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value(201))
            .andExpect(jsonPath("$.data.code").isNotEmpty());
    }

    @Test
    @WithMockUser(username = APPROVED_EMAIL, roles = "MEMBER")
    void memberCannotIssueAttendanceCode() throws Exception {
        mockMvc.perform(post("/api/v1/admin/events/{eventId}/attendance-codes", eventId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(username = APPROVED_EMAIL, roles = "MEMBER")
    void approvedMemberCanCheckInWithValidCode() throws Exception {
        String code = attendanceCodeService.issue(eventId, new AttendanceCodeCreateRequest(null)).code();

        mockMvc.perform(post("/api/v1/events/{eventId}/attendances", eventId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"" + code + "\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value(201))
            .andExpect(jsonPath("$.data.memberName").value("승인부원"));
    }

    @Test
    @WithMockUser(username = APPROVED_EMAIL, roles = "MEMBER")
    void memberCannotViewAttendanceList() throws Exception {
        mockMvc.perform(get("/api/v1/events/{eventId}/attendances", eventId))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.status").value(403));
    }
}
