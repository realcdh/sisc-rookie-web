package com.sisc_it.sisc_rookie_web.attendance.domain;

import java.time.LocalDateTime;

import com.sisc_it.sisc_rookie_web.event.domain.Event;
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
import lombok.Getter;

/**
 * 행사 당일 출석을 위해 운영진이 발급하는 출석 코드.
 * 한 행사에 여러 번 발급될 수 있으며, 출석 검증에는 활성(active) 상태이고 만료되지 않은 코드만 사용된다.
 */
@Getter
@Entity
@Table(name = "attendance_codes")
public class AttendanceCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private boolean active;

    // null이면 만료되지 않는다.
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected AttendanceCode() {
    }

    public AttendanceCode(Event event, String code, LocalDateTime expiresAt) {
        this.event = event;
        this.code = code;
        this.expiresAt = expiresAt;
        this.active = true;
    }

    /** 새 코드 발급 등으로 더 이상 사용하지 않도록 비활성화한다. */
    public void deactivate() {
        this.active = false;
    }

    /** 주어진 시각 기준으로 사용 가능한 코드인지(활성 + 미만료) 여부. */
    public boolean isUsableAt(LocalDateTime now) {
        return active && (expiresAt == null || now.isBefore(expiresAt));
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
