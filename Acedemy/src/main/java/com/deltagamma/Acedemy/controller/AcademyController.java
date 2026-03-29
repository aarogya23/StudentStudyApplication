package com.deltagamma.Acedemy.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.deltagamma.Acedemy.dto.AcademyDtos.AssessmentSubmissionRequest;
import com.deltagamma.Acedemy.dto.AcademyDtos.CourseCardDto;
import com.deltagamma.Acedemy.dto.AcademyDtos.CourseDetailDto;
import com.deltagamma.Acedemy.dto.AcademyDtos.DashboardDto;
import com.deltagamma.Acedemy.dto.AcademyDtos.LiveClassDto;
import com.deltagamma.Acedemy.dto.AcademyDtos.MessageDto;
import com.deltagamma.Acedemy.dto.AcademyDtos.MessageRequest;
import com.deltagamma.Acedemy.model.User;
import com.deltagamma.Acedemy.service.AcademyService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AcademyController {

    private final AcademyService academyService;

    @GetMapping("/dashboard")
    public DashboardDto dashboard(@AuthenticationPrincipal User user) {
        return academyService.getDashboard(user);
    }

    @GetMapping("/courses")
    public List<CourseCardDto> courses(@AuthenticationPrincipal User user) {
        return academyService.getCoursesForUser(user);
    }

    @GetMapping("/courses/{courseId}")
    public CourseDetailDto course(@AuthenticationPrincipal User user, @PathVariable Long courseId) {
        return academyService.getCourseDetail(user, courseId);
    }

    @PostMapping("/courses/{courseId}/lessons/complete")
    public DashboardDto completeLesson(@AuthenticationPrincipal User user, @PathVariable Long courseId) {
        return academyService.completeNextLesson(user, courseId);
    }

    @PostMapping("/assessments/submit")
    public DashboardDto submitAssessment(
            @AuthenticationPrincipal User user,
            @RequestBody AssessmentSubmissionRequest request) {
        return academyService.submitAssessment(user, request);
    }

    @GetMapping("/live-classes")
    public List<LiveClassDto> liveClasses() {
        return academyService.getLiveClasses();
    }

    @PostMapping("/live-classes/{liveClassId}/join")
    public java.util.Map<String, String> joinLiveClass(@AuthenticationPrincipal User user, @PathVariable Long liveClassId) {
        academyService.signalLivePresence(user, liveClassId);
        return java.util.Map.of("status", "joined");
    }

    @PostMapping("/course-messages")
    public MessageDto sendMessage(@AuthenticationPrincipal User user, @RequestBody MessageRequest request) {
        return academyService.sendCourseMessage(user, request);
    }
}
