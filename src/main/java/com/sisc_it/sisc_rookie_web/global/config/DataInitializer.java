package com.sisc_it.sisc_rookie_web.global.config;

import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sisc_it.sisc_rookie_web.event.domain.Event;
import com.sisc_it.sisc_rookie_web.event.domain.EventStatus;
import com.sisc_it.sisc_rookie_web.event.repository.EventRepository;
import com.sisc_it.sisc_rookie_web.member.domain.Member;
import com.sisc_it.sisc_rookie_web.member.domain.Role;
import com.sisc_it.sisc_rookie_web.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

/**
 * 로컬 시연용 초기 데이터. H2 인메모리는 재기동 시 초기화되므로 기동마다 시드한다.
 * local 프로파일에서만 동작하며(테스트는 default 프로파일), {@code bootRun} 시 자동으로 활성화된다.
 */
@Component
@Profile("local")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final String DEFAULT_PASSWORD = "password123";

    private final MemberRepository memberRepository;
    private final EventRepository eventRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (memberRepository.count() > 0) {
            return;
        }

        Member admin = saveMember("관리자", "admin@sisc.test", Role.ADMIN);
        saveMember("운영진", "staff@sisc.test", Role.STAFF);
        saveMember("부원", "member@sisc.test", Role.MEMBER);

        eventRepository.save(new Event(
            "기업분석 세미나 2회차",
            "삼성전자 사업보고서를 함께 분석합니다. 노트북을 지참해 주세요.",
            30, "동아리방 301호", LocalDateTime.now().plusDays(7),
            EventStatus.OPEN, admin
        ));
        eventRepository.save(new Event(
            "신입 OT",
            "세투연 신규 부원을 위한 오리엔테이션입니다.",
            50, "대강당", LocalDateTime.now().plusDays(3),
            EventStatus.OPEN, admin
        ));
        eventRepository.save(new Event(
            "모의투자대회 설명회",
            "이번 학기 모의투자대회 규칙과 일정을 안내합니다.",
            null, "온라인(Zoom)", LocalDateTime.now().plusDays(14),
            EventStatus.DRAFT, admin
        ));
    }

    private Member saveMember(String name, String email, Role role) {
        Member member = new Member(name, email, passwordEncoder.encode(DEFAULT_PASSWORD), role);
        return memberRepository.save(member);
    }
}
