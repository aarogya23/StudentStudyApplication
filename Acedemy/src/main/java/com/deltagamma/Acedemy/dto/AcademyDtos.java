package com.deltagamma.Acedemy.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;

public class AcademyDtos {

    @Builder
    public record CourseCardDto(
            Long id,
            String slug,
            String title,
            String tagline,
            String category,
            String mentorName,
            int estimatedHours,
            int creditWeight,
            String difficultyLevel,
            String accentColor,
            int progressPercent,
            int masteryPercent,
            int pointsEarned,
            int lessonCount,
            int assessmentCount) {
    }

    @Builder
    public record LessonDto(Long id, String title, String topic, int durationMinutes, int sequenceOrder) {
    }

    @Builder
    public record AssessmentDto(Long id, String title, int totalQuestions, int totalMarks, int timeLimitMinutes) {
    }

    @Builder
    public record CourseDetailDto(
            CourseCardDto course,
            List<LessonDto> lessons,
            List<AssessmentDto> assessments,
            List<MessageDto> messages) {
    }

    @Builder
    public record LiveClassDto(
            Long id,
            String title,
            String meetingCode,
            LocalDateTime scheduledFor,
            int durationMinutes,
            String courseTitle,
            String instructorName) {
    }

    @Builder
    public record DashboardStatDto(String label, int value, String color) {
    }

    @Builder
    public record AttemptDto(String assessmentTitle, int percentage, LocalDateTime submittedAt) {
    }

    @Builder
    public record DashboardDto(
            UserProfileDto user,
            int totalXp,
            int weeklyProgress,
            int completionRate,
            int consistencyScore,
            List<DashboardStatDto> barSeries,
            List<DashboardStatDto> pieSeries,
            List<AttemptDto> recentAttempts,
            List<CourseCardDto> courses,
            List<LiveClassDto> liveClasses) {
    }

    public record AssessmentSubmissionRequest(Long assessmentId, int score) {
    }

    public record MessageRequest(Long courseId, String message) {
    }

    @Builder
    public record MessageDto(
            Long id,
            Long courseId,
            String senderName,
            String senderRole,
            String message,
            LocalDateTime sentAt) {
    }
}
