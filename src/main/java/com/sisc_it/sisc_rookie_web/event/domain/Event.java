package com.sisc_it.sisc_rookie_web.event.domain;

import java.time.LocalDateTime;

import com.sisc_it.sisc_rookie_web.global.exception.BusinessException;
import com.sisc_it.sisc_rookie_web.global.exception.ErrorCode;
import com.sisc_it.sisc_rookie_web.member.domain.Member;
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
import lombok.Getter;

@Getter
@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    // 정원. null이면 정원 제한 없음을 의미한다(표시/집계용).
    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "location")
    private String location;

    @Column(name = "start_at")
    private LocalDateTime startAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;

    // Managed by the system, so no public setter.
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Fixed at creation. Changing the creator should go through a DTO and permission check in service code.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private Member createdBy;

    protected Event() {
    }

    public Event(String title, String description, Member createdBy) {
        this(title, description, EventStatus.DRAFT, createdBy);
    }

    public Event(String title, String description, EventStatus status, Member createdBy) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.createdBy = createdBy;
    }

    public Event(String title, String description, Integer capacity, String location,
                 LocalDateTime startAt, EventStatus status, Member createdBy) {
        this(title, description, status, createdBy);
        this.capacity = capacity;
        this.location = location;
        this.startAt = startAt;
    }

    /** 행사 상세 내용을 수정한다. 상태 전이와는 무관하다. */
    public void updateDetails(String title, String description, Integer capacity,
                              String location, LocalDateTime startAt) {
        this.title = title;
        this.description = description;
        this.capacity = capacity;
        this.location = location;
        this.startAt = startAt;
    }

    /**
     * 행사 상태를 전이한다. 허용되지 않는 전이는 예외를 던진다.
     *
     * @throws BusinessException 전이 규칙({@link EventStatus#canTransitionTo})을 위반한 경우
     */
    public void changeStatus(EventStatus target) {
        if (this.status == target || !this.status.canTransitionTo(target)) {
            throw new BusinessException(ErrorCode.INVALID_EVENT_STATUS_TRANSITION);
        }
        this.status = target;
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = EventStatus.DRAFT;
        }
    }
}
