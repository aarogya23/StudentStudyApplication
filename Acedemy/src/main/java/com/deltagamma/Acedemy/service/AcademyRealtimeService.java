package com.deltagamma.Acedemy.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.deltagamma.Acedemy.dto.AcademyDtos.DashboardDto;
import com.deltagamma.Acedemy.dto.AcademyDtos.MessageDto;
import com.deltagamma.Acedemy.model.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AcademyRealtimeService {

    private final SimpMessagingTemplate messagingTemplate;

    public void pushDashboard(User user, DashboardDto dashboardDto) {
        messagingTemplate.convertAndSend("/topic/dashboard/" + user.getId(), dashboardDto);
    }

    public void pushCourseMessage(Long courseId, MessageDto messageDto) {
        messagingTemplate.convertAndSend("/topic/course-chat/" + courseId, messageDto);
    }

    public void pushLiveClassSignal(Long liveClassId, Object payload) {
        messagingTemplate.convertAndSend("/topic/live-class/" + liveClassId, payload);
    }
}
