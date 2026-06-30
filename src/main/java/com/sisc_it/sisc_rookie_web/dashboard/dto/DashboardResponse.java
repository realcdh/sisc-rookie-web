package com.sisc_it.sisc_rookie_web.dashboard.dto;

/**
 * 관리자 대시보드 집계 수치.
 *
 * @param totalEvents           전체 행사 수
 * @param openEvents            현재 모집 중(OPEN)인 행사 수
 * @param totalApplications     전체 신청 수
 * @param approvedApplications  승인된 신청 수
 * @param attendanceCount       출석 수
 * @param totalFeedbacks        전체 피드백 수
 */
public record DashboardResponse(
    long totalEvents,
    long openEvents,
    long totalApplications,
    long approvedApplications,
    long attendanceCount,
    long totalFeedbacks
) {
}
