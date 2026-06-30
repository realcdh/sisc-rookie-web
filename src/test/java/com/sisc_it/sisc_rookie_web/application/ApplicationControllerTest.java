package com.sisc_it.sisc_rookie_web.application;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.sisc_it.sisc_rookie_web.event.domain.Event;
import com.sisc_it.sisc_rookie_web.event.domain.EventStatus;
import com.sisc_it.sisc_rookie_web.event.repository.EventRepository;
import com.sisc_it.sisc_rookie_web.member.domain.Member;
import com.sisc_it.sisc_rookie_web.member.domain.Role;
import com.sisc_it.sisc_rookie_web.member.repository.MemberRepository;

@SpringBootTest
@Transactional
class ApplicationControllerTest {

    private static final String MEMBER_EMAIL = "app-ctrl-member@sisc.test";
    private static final String STAFF_EMAIL = "app-ctrl-staff@sisc.test";

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EventRepository eventRepository;

    private Long openEventId;
    private Long draftEventId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();

        Member admin = memberRepository.save(new Member("관리자", "app-ctrl-admin@sisc.test", "hash", Role.ADMIN));
        memberRepository.save(new Member("부원", MEMBER_EMAIL, "hash", Role.MEMBER));
        memberRepository.save(new Member("운영진", STAFF_EMAIL, "hash", Role.STAFF));

        openEventId = eventRepository.save(new Event("OPEN 세미나", "설명", EventStatus.OPEN, admin)).getId();
        draftEventId = eventRepository.save(new Event("DRAFT 세미나", "설명", EventStatus.DRAFT, admin)).getId();
    }

    @Test
    @WithMockUser(username = MEMBER_EMAIL, roles = "MEMBER")
    void memberCanApplyToOpenEvent() throws Exception {
        mockMvc.perform(post("/api/v1/events/{eventId}/applications", openEventId))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value(201))
            .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = MEMBER_EMAIL, roles = "MEMBER")
    void applyToNonOpenEventReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/events/{eventId}/applications", draftEventId))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @WithMockUser(username = MEMBER_EMAIL, roles = "MEMBER")
    void memberCannotViewApplicantList() throws Exception {
        // PRD 필수 시나리오: 운영진 전용 API에 대한 MEMBER 접근 차단
        mockMvc.perform(get("/api/v1/events/{eventId}/applications", openEventId))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(username = STAFF_EMAIL, roles = "STAFF")
    void staffCanViewApplicantList() throws Exception {
        mockMvc.perform(get("/api/v1/events/{eventId}/applications", openEventId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data").isArray());
    }
}
