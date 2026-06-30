package com.sisc_it.sisc_rookie_web.attendance.domain;

import java.time.LocalDateTime;

import com.sisc_it.sisc_rookie_web.event.domain.Event;
import com.sisc_it.sisc_rookie_web.member.domain.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

/**
 * 한 회원의 한 행사에 대한 출석 기록. 동일 행사에 중복 출석할 수 없다.
 */
@Getter
@Entity
@Table(
    name = "attendances",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_attendance_event_member", columnNames = {"event_id", "member_id"})
    }
)
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "checked_in_at", nullable = false)
    private LocalDateTime checkedInAt;

    protected Attendance() {
    }

    public Attendance(Event event, Member member) {
        this.event = event;
        this.member = member;
    }

    @PrePersist
    void prePersist() {
        if (checkedInAt == null) {
            checkedInAt = LocalDateTime.now();
        }
    }
}
