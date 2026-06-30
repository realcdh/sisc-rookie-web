package com.sisc_it.sisc_rookie_web.dashboard.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sisc_it.sisc_rookie_web.application.domain.ApplicationStatus;
import com.sisc_it.sisc_rookie_web.application.repository.ApplicationRepository;
import com.sisc_it.sisc_rookie_web.dashboard.dto.DashboardResponse;
import com.sisc_it.sisc_rookie_web.event.domain.EventStatus;
import com.sisc_it.sisc_rookie_web.event.repository.EventRepository;
import com.sisc_it.sisc_rookie_web.feedback.repository.FeedbackRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final EventRepository eventRepository;
    private final ApplicationRepository applicationRepository;
    private final FeedbackRepository feedbackRepository;

    /** 운영 현황 수치를 집계한다. */
    public DashboardResponse getDashboard() {
        return new DashboardResponse(
            eventRepository.count(),
            eventRepository.countByStatus(EventStatus.OPEN),
            applicationRepository.count(),
            applicationRepository.countByStatus(ApplicationStatus.APPROVED),
            applicationRepository.countByAttendedTrue(),
            feedbackRepository.count()
        );
    }
}
