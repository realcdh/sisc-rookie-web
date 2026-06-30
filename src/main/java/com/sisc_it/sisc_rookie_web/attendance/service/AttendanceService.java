package com.sisc_it.sisc_rookie_web.attendance.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sisc_it.sisc_rookie_web.application.domain.ApplicationStatus;
import com.sisc_it.sisc_rookie_web.application.repository.ApplicationRepository;
import com.sisc_it.sisc_rookie_web.attendance.domain.Attendance;
import com.sisc_it.sisc_rookie_web.attendance.domain.AttendanceCode;
import com.sisc_it.sisc_rookie_web.attendance.dto.AttendanceCheckRequest;
import com.sisc_it.sisc_rookie_web.attendance.dto.AttendanceResponse;
import com.sisc_it.sisc_rookie_web.attendance.repository.AttendanceCodeRepository;
import com.sisc_it.sisc_rookie_web.attendance.repository.AttendanceRepository;
import com.sisc_it.sisc_rookie_web.event.domain.Event;
import com.sisc_it.sisc_rookie_web.event.repository.EventRepository;
import com.sisc_it.sisc_rookie_web.global.exception.BusinessException;
import com.sisc_it.sisc_rookie_web.global.exception.ErrorCode;
import com.sisc_it.sisc_rookie_web.member.domain.Member;
import com.sisc_it.sisc_rookie_web.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final AttendanceCodeRepository attendanceCodeRepository;
    private final ApplicationRepository applicationRepository;
    private final EventRepository eventRepository;
    private final MemberRepository memberRepository;

    /**
     * 출석 체크. 승인된 신청자만, 유효한 출석 코드를 입력했을 때 출석 처리된다. 중복 출석은 불가하다.
     */
    @Transactional
    public AttendanceResponse checkIn(Long eventId, AttendanceCheckRequest request, String memberEmail) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));
        Member member = memberRepository.findByEmail(memberEmail)
            .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 1) 승인된 신청자인지 확인
        boolean approved = applicationRepository.existsByEventIdAndMemberIdAndStatus(
            eventId, member.getId(), ApplicationStatus.APPROVED);
        if (!approved) {
            throw new BusinessException(ErrorCode.NOT_APPROVED_FOR_ATTENDANCE);
        }

        // 2) 출석 코드 검증(존재 + 활성 + 미만료)
        AttendanceCode code = attendanceCodeRepository
            .findByEventIdAndCodeAndActiveTrue(eventId, request.code())
            .filter(c -> c.isUsableAt(LocalDateTime.now()))
            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_ATTENDANCE_CODE));

        // 3) 중복 출석 방지
        if (attendanceRepository.existsByEventIdAndMemberId(eventId, member.getId())) {
            throw new BusinessException(ErrorCode.ALREADY_ATTENDED);
        }

        Attendance attendance = attendanceRepository.save(new Attendance(code.getEvent(), member));
        return AttendanceResponse.from(attendance);
    }

    /** [운영진] 해당 행사의 출석 현황 및 명단 조회. */
    public List<AttendanceResponse> getAttendances(Long eventId) {
        eventRepository.findById(eventId)
            .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));
        return attendanceRepository.findByEventIdWithMember(eventId).stream()
            .map(AttendanceResponse::from)
            .toList();
    }
}
