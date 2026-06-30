package com.sisc_it.sisc_rookie_web.event.domain;

import java.util.Set;

/**
 * 행사의 생명주기 상태.
 *
 * <p>상태에 따라 가능한 행동이 달라진다는 것이 이 프로젝트의 핵심 설계 포인트다.
 * 어떤 상태에서 어떤 상태로 넘어갈 수 있는지는 {@link #canTransitionTo(EventStatus)}에 명시한다.
 */
public enum EventStatus {
    /** 작성 중. 외부에 공개되지 않으며 신청을 받을 수 없다. */
    DRAFT,
    /** 모집 중. 신청을 받을 수 있는 유일한 상태다. */
    OPEN,
    /** 모집 마감. 더 이상 신청을 받지 않지만 행사는 진행된다(출석 가능). */
    CLOSED,
    /** 종료. 결과만 조회 가능하며 피드백 작성 대상이 된다. */
    COMPLETED,
    /** 취소된 행사. 더 이상 어떤 전이도 허용하지 않는 종료 상태다. */
    CANCELED;

    private Set<EventStatus> allowedTransitions() {
        return switch (this) {
            case DRAFT -> Set.of(OPEN, CANCELED);
            case OPEN -> Set.of(CLOSED, CANCELED);
            case CLOSED -> Set.of(OPEN, COMPLETED, CANCELED);
            case COMPLETED, CANCELED -> Set.of();
        };
    }

    /**
     * 현재 상태에서 {@code target} 상태로 전이할 수 있는지 여부.
     * 같은 상태로의 전이는 변경이 아니므로 허용하지 않는다.
     */
    public boolean canTransitionTo(EventStatus target) {
        return allowedTransitions().contains(target);
    }

    /** 신청을 받을 수 있는 상태인지 여부. */
    public boolean isOpenForApplication() {
        return this == OPEN;
    }
}
