package com.sisc_it.sisc_rookie_web.feedback.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sisc_it.sisc_rookie_web.feedback.domain.Feedback;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    boolean existsByEventIdAndMemberId(Long eventId, Long memberId);

    // 특정 행사의 피드백을 최신순으로 조회
    List<Feedback> findByEventIdOrderByCreatedAtDesc(Long eventId);

    // 전체 피드백을 최신순으로 조회(관리자)
    List<Feedback> findAllByOrderByCreatedAtDesc();
}
