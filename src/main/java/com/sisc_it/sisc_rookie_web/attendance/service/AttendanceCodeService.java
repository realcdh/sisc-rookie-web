package com.sisc_it.sisc_rookie_web.attendance.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sisc_it.sisc_rookie_web.attendance.domain.AttendanceCode;
import com.sisc_it.sisc_rookie_web.attendance.dto.AttendanceCodeCreateRequest;
import com.sisc_it.sisc_rookie_web.attendance.dto.AttendanceCodeResponse;
import com.sisc_it.sisc_rookie_web.attendance.repository.AttendanceCodeRepository;
import com.sisc_it.sisc_rookie_web.event.domain.Event;
import com.sisc_it.sisc_rookie_web.event.repository.EventRepository;
import com.sisc_it.sisc_rookie_web.global.exception.BusinessException;
import com.sisc_it.sisc_rookie_web.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceCodeService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int CODE_LENGTH = 6;

    private final AttendanceCodeRepository attendanceCodeRepository;
    private final EventRepository eventRepository;

    /**
     * [운영진] 출석 코드 발급. 같은 행사의 기존 활성 코드는 비활성화하고 새 코드를 발급한다.
     */
    @Transactional
    public AttendanceCodeResponse issue(Long eventId, AttendanceCodeCreateRequest request) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));

        // 기존 활성 코드를 비활성화한다(행사당 하나의 유효 코드 유지).
        attendanceCodeRepository.findByEventIdAndActiveTrue(eventId)
            .forEach(AttendanceCode::deactivate);

        LocalDateTime expiresAt = (request.expireMinutes() == null)
            ? null
            : LocalDateTime.now().plusMinutes(request.expireMinutes());

        AttendanceCode code = new AttendanceCode(event, generateCode(), expiresAt);
        return AttendanceCodeResponse.from(attendanceCodeRepository.save(code));
    }

    private String generateCode() {
        StringBuilder builder = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            builder.append(RANDOM.nextInt(10));
        }
        return builder.toString();
    }
}
