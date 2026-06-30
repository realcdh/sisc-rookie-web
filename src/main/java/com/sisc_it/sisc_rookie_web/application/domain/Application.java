package com.sisc_it.sisc_rookie_web.application.domain;

import java.time.LocalDateTime;

import com.sisc_it.sisc_rookie_web.event.domain.Event;
import com.sisc_it.sisc_rookie_web.global.exception.BusinessException;
import com.sisc_it.sisc_rookie_web.global.exception.ErrorCode;
import com.sisc_it.sisc_rookie_web.member.domain.Member;
import com.sisc_it.sisc_rookie_web.team.domain.Team;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Getter
@Entity
@Table(
    name = "applications",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_application_event_member", columnNames = {"event_id", "member_id"})
    }
)
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Fixed at creation. Changing application ownership should go through a DTO and service validation.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 팀이 없는 신입 부원도 행사에 신청할 수 있으므로 nullable이다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    // Managed by the system, so no public setter.
    @Column(name = "applied_at", nullable = false)
    private LocalDateTime appliedAt;

    @Column(name = "is_attended", nullable = false)
    @Setter
    private boolean attended;

    protected Application() {
    }

    public Application(Event event, Member member) {
        this(event, member, member.getTeam(), ApplicationStatus.PENDING);
    }

    public Application(Event event, Member member, Team team, ApplicationStatus status) {
        this.event = event;
        this.member = member;
        this.team = team;
        this.status = status;
        this.attended = false;
    }

    /**
     * 운영진의 승인/반려 처리. 대기(PENDING) 상태에서만 가능하며,
     * 결과는 APPROVED 또는 REJECTED여야 한다.
     */
    public void review(ApplicationStatus decision) {
        if (!this.status.isReviewable() || !decision.isReviewDecision()) {
            throw new BusinessException(ErrorCode.INVALID_APPLICATION_STATUS_CHANGE);
        }
        this.status = decision;
    }

    /** 신청자 본인의 취소. 대기(PENDING) 상태에서만 가능하다. */
    public void cancel() {
        if (!this.status.isCancelable()) {
            throw new BusinessException(ErrorCode.CANNOT_CANCEL_APPLICATION);
        }
        this.status = ApplicationStatus.CANCELED;
    }

    @PrePersist
    void prePersist() {
        if (appliedAt == null) {
            appliedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = ApplicationStatus.PENDING;
        }
    }
}
