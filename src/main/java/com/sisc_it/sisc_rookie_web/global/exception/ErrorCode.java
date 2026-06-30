package com.sisc_it.sisc_rookie_web.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // 인증/회원
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    INVALID_LOGIN(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "요청 값이 올바르지 않습니다."),

    // 행사(Event)
    EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "행사를 찾을 수 없습니다."),
    INVALID_EVENT_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "허용되지 않는 행사 상태 변경입니다."),

    // 신청(Application)
    EVENT_NOT_OPEN(HttpStatus.BAD_REQUEST, "모집 중인 행사가 아니어서 신청할 수 없습니다."),
    DUPLICATE_APPLICATION(HttpStatus.CONFLICT, "이미 신청한 행사입니다."),
    APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "신청 내역을 찾을 수 없습니다."),
    CANNOT_CANCEL_APPLICATION(HttpStatus.BAD_REQUEST, "대기 중(PENDING)인 신청만 취소할 수 있습니다."),
    INVALID_APPLICATION_STATUS_CHANGE(HttpStatus.BAD_REQUEST, "대기 중(PENDING)인 신청만 승인/반려할 수 있습니다."),

    // 출석(Attendance)
    NOT_APPROVED_FOR_ATTENDANCE(HttpStatus.FORBIDDEN, "승인된 신청자만 출석할 수 있습니다."),
    INVALID_ATTENDANCE_CODE(HttpStatus.BAD_REQUEST, "유효하지 않은 출석 코드입니다."),
    ALREADY_ATTENDED(HttpStatus.CONFLICT, "이미 출석 처리되었습니다."),

    // 피드백(Feedback)
    DUPLICATE_FEEDBACK(HttpStatus.CONFLICT, "이미 이 행사에 피드백을 작성했습니다."),

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
