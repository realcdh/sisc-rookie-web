package com.sisc_it.sisc_rookie_web.feedback;

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

import com.sisc_it.sisc_rookie_web.event.domain.Event;
import com.sisc_it.sisc_rookie_web.event.domain.EventStatus;
import com.sisc_it.sisc_rookie_web.event.repository.EventRepository;
import com.sisc_it.sisc_rookie_web.member.domain.Member;
import com.sisc_it.sisc_rookie_web.member.domain.Role;
import com.sisc_it.sisc_rookie_web.member.repository.MemberRepository;

@SpringBootTest
@Transactional
class FeedbackControllerTest {

    private static final String MEMBER_EMAIL = "fb-ctrl-member@sisc.test";

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private MemberRepository memberRepository;

    private Long eventId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();

        Member admin = memberRepository.save(new Member("관리자", "fb-ctrl-admin@sisc.test", "hash", Role.ADMIN));
        memberRepository.save(new Member("부원", MEMBER_EMAIL, "hash", Role.MEMBER));
        eventId = eventRepository.save(new Event("세미나", "설명", EventStatus.COMPLETED, admin)).getId();
    }

    @Test
    @WithMockUser(username = MEMBER_EMAIL, roles = "MEMBER")
    void memberCanCreateFeedback() throws Exception {
        mockMvc.perform(post("/api/v1/events/{eventId}/feedbacks", eventId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"설명이 좋았습니다.\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value(201))
            .andExpect(jsonPath("$.data.content").value("설명이 좋았습니다."));
    }

    @Test
    @WithMockUser(username = MEMBER_EMAIL, roles = "MEMBER")
    void emptyFeedbackIsRejected() throws Exception {
        // PRD 필수 시나리오: 내용이 비어있는 피드백 저장 차단
        mockMvc.perform(post("/api/v1/events/{eventId}/feedbacks", eventId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"   \"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @WithMockUser(username = MEMBER_EMAIL, roles = "MEMBER")
    void memberCannotViewEventFeedbackList() throws Exception {
        mockMvc.perform(get("/api/v1/events/{eventId}/feedbacks", eventId))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(username = "fb-ctrl-admin@sisc.test", roles = "ADMIN")
    void adminCanViewAllFeedbacks() throws Exception {
        mockMvc.perform(get("/api/v1/admin/feedbacks"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data").isArray());
    }
}
