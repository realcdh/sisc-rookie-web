package com.sisc_it.sisc_rookie_web.event;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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

import com.sisc_it.sisc_rookie_web.event.domain.EventStatus;
import com.sisc_it.sisc_rookie_web.event.dto.EventCreateRequest;
import com.sisc_it.sisc_rookie_web.event.dto.EventResponse;
import com.sisc_it.sisc_rookie_web.event.service.EventService;
import com.sisc_it.sisc_rookie_web.member.domain.Member;
import com.sisc_it.sisc_rookie_web.member.domain.Role;
import com.sisc_it.sisc_rookie_web.member.repository.MemberRepository;

@SpringBootTest
@Transactional
class EventControllerTest {

    private static final String ADMIN_EMAIL = "event-ctrl-admin@sisc.test";

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EventService eventService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();
        memberRepository.save(new Member("관리자", ADMIN_EMAIL, "hashed-password", Role.ADMIN));
    }

    @Test
    @WithMockUser(username = ADMIN_EMAIL, roles = "ADMIN")
    void adminCanCreateEvent() throws Exception {
        String body = """
            {"title":"기업분석 세미나","description":"삼성전자 분석","capacity":30}
            """;

        mockMvc.perform(post("/api/v1/admin/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value(201))
            .andExpect(jsonPath("$.data.status").value("DRAFT"))
            .andExpect(jsonPath("$.data.capacity").value(30));
    }

    @Test
    @WithMockUser(username = "member@sisc.test", roles = "MEMBER")
    void memberCannotAccessAdminApi() throws Exception {
        String body = """
            {"title":"권한 없는 생성","description":"막혀야 한다"}
            """;

        // PRD 필수 시나리오: 관리자 권한 없는 사용자의 관리자 API 접근 차단
        mockMvc.perform(post("/api/v1/admin/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(username = "member@sisc.test", roles = "MEMBER")
    void anyAuthenticatedUserCanListEvents() throws Exception {
        eventService.createEvent(
            new EventCreateRequest("목록 노출 행사", "설명", null, null, null), ADMIN_EMAIL);

        mockMvc.perform(get("/api/v1/events"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data[0].createdByName").value("관리자"));
    }

    @Test
    @WithMockUser(username = ADMIN_EMAIL, roles = "ADMIN")
    void invalidStatusTransitionReturnsBadRequest() throws Exception {
        EventResponse created = eventService.createEvent(
            new EventCreateRequest("상태 전이 테스트", "설명", null, null, null), ADMIN_EMAIL);

        // DRAFT -> COMPLETED 는 허용되지 않는 전이 -> 400
        mockMvc.perform(patch("/api/v1/admin/events/{eventId}/status", created.eventId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"" + EventStatus.COMPLETED.name() + "\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));
    }
}
