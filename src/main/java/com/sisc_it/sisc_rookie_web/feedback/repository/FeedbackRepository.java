package com.sisc_it.sisc_rookie_web.feedback.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sisc_it.sisc_rookie_web.feedback.domain.Feedback;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    boolean existsByEventIdAndMemberId(Long eventId, Long memberId);

    // 특정 행사의 피드백 목록(작성자 함께 조회)
    @Query("select f from Feedback f join fetch f.member where f.event.id = :eventId order by f.createdAt desc")
    List<Feedback> findByEventIdWithMember(@Param("eventId") Long eventId);

    // 전체 피드백 모아보기(행사·작성자 함께 조회)
    @Query("select f from Feedback f join fetch f.member join fetch f.event order by f.createdAt desc")
    List<Feedback> findAllWithEventAndMember();
}
