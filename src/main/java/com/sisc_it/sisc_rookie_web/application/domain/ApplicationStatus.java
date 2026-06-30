package com.sisc_it.sisc_rookie_web.application.domain;

public enum ApplicationStatus {
    /** 신청 직후의 기본 상태. 운영진의 검토를 기다린다. */
    PENDING,
    /** 운영진이 승인함. 출석 대상이 된다. */
    APPROVED,
    /** 운영진이 반려함. */
    REJECTED,
    /** 신청자가 직접 취소함. */
    CANCELED;

    /** 운영진이 승인/반려할 수 있는 상태인지(PENDING) 여부. */
    public boolean isReviewable() {
        return this == PENDING;
    }

    /** 신청자가 취소할 수 있는 상태인지(PENDING) 여부. */
    public boolean isCancelable() {
        return this == PENDING;
    }

    /** 운영진이 검토 결과로 지정할 수 있는 상태(APPROVED/REJECTED)인지 여부. */
    public boolean isReviewDecision() {
        return this == APPROVED || this == REJECTED;
    }
}
