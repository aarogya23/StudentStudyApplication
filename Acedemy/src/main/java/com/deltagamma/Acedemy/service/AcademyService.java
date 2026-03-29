package com.deltagamma.Acedemy.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.deltagamma.Acedemy.dto.AcademyDtos.AssessmentDto;
import com.deltagamma.Acedemy.dto.AcademyDtos.AssessmentSubmissionRequest;
import com.deltagamma.Acedemy.dto.AcademyDtos.AttemptDto;
import com.deltagamma.Acedemy.dto.AcademyDtos.CourseCardDto;
import com.deltagamma.Acedemy.dto.AcademyDtos.CourseDetailDto;
import com.deltagamma.Acedemy.dto.AcademyDtos.DashboardDto;
import com.deltagamma.Acedemy.dto.AcademyDtos.DashboardStatDto;
import com.deltagamma.Acedemy.dto.AcademyDtos.LessonDto;
import com.deltagamma.Acedemy.dto.AcademyDtos.LiveClassDto;
import com.deltagamma.Acedemy.dto.AcademyDtos.MessageDto;
import com.deltagamma.Acedemy.dto.AcademyDtos.MessageRequest;
import com.deltagamma.Acedemy.model.Assessment;
import com.deltagamma.Acedemy.model.AssessmentAttempt;
import com.deltagamma.Acedemy.model.Course;
import com.deltagamma.Acedemy.model.CourseMessage;
import com.deltagamma.Acedemy.model.Enrollment;
import com.deltagamma.Acedemy.model.LiveClassSession;
import com.deltagamma.Acedemy.model.User;
import com.deltagamma.Acedemy.repository.AssessmentAttemptRepository;
import com.deltagamma.Acedemy.repository.AssessmentRepository;
import com.deltagamma.Acedemy.repository.CourseMessageRepository;
import com.deltagamma.Acedemy.repository.CourseRepository;
import com.deltagamma.Acedemy.repository.EnrollmentRepository;
import com.deltagamma.Acedemy.repository.LiveClassSessionRepository;
import com.deltagamma.Acedemy.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AcademyService {

    private final EnrollmentRepository enrollmentRepository;
    private final AssessmentRepository assessmentRepository;
    private final AssessmentAttemptRepository assessmentAttemptRepository;
    private final LiveClassSessionRepository liveClassSessionRepository;
    private final CourseMessageRepository courseMessageRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final AcademyRealtimeService academyRealtimeService;
    private final UserProfileMapper userProfileMapper;

    public DashboardDto getDashboard(User user) {
        List<Enrollment> enrollments = enrollmentRepository.findByUserId(user.getId());
        List<AssessmentAttempt> attempts = assessmentAttemptRepository.findByUserIdOrderBySubmittedAtDesc(user.getId());
        List<LiveClassSession> liveClasses = liveClassSessionRepository.findAllByOrderByScheduledForAsc();

        int completionRate = enrollments.isEmpty() ? 0
                : enrollments.stream().mapToInt(Enrollment::getProgressPercent).sum() / enrollments.size();
        int mastery = enrollments.isEmpty() ? 0
                : enrollments.stream().mapToInt(Enrollment::getMasteryPercent).sum() / enrollments.size();
        int consistencyScore = Math.min(100, user.getStreakDays() * 12 + attempts.size() * 5);

        return DashboardDto.builder()
                .user(userProfileMapper.toDto(user))
                .totalXp(user.getXp())
                .weeklyProgress(Math.min(100, completionRate + 9))
                .completionRate(completionRate)
                .consistencyScore(consistencyScore)
                .barSeries(List.of(
                        DashboardStatDto.builder().label("Course Progress").value(completionRate).color("#27c2ff").build(),
                        DashboardStatDto.builder().label("Mastery").value(mastery).color("#1d7bf2").build(),
                        DashboardStatDto.builder().label("Consistency").value(consistencyScore).color("#12b981").build()))
                .pieSeries(List.of(
                        DashboardStatDto.builder().label("Completed").value(completionRate).color("#27c2ff").build(),
                        DashboardStatDto.builder().label("Remaining").value(Math.max(0, 100 - completionRate)).color("#d8eeff").build()))
                .recentAttempts(attempts.stream().limit(5).map(this::toAttemptDto).toList())
                .courses(enrollments.stream().map(this::toCourseCardDto).toList())
                .liveClasses(liveClasses.stream().map(this::toLiveClassDto).toList())
                .build();
    }

    public List<CourseCardDto> getCoursesForUser(User user) {
        return enrollmentRepository.findByUserId(user.getId()).stream().map(this::toCourseCardDto).toList();
    }

    public CourseDetailDto getCourseDetail(User user, Long courseId) {
        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(user.getId(), courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found for user"));
        Course course = enrollment.getCourse();

        List<MessageDto> messages = courseMessageRepository.findTop20ByCourseIdOrderBySentAtDesc(courseId).stream()
                .sorted(Comparator.comparing(CourseMessage::getSentAt))
                .map(this::toMessageDto)
                .toList();

        return CourseDetailDto.builder()
                .course(toCourseCardDto(enrollment))
                .lessons(course.getLessons().stream()
                        .sorted(Comparator.comparingInt(lesson -> lesson.getSequenceOrder()))
                        .map(lesson -> LessonDto.builder()
                                .id(lesson.getId())
                                .title(lesson.getTitle())
                                .topic(lesson.getTopic())
                                .durationMinutes(lesson.getDurationMinutes())
                                .sequenceOrder(lesson.getSequenceOrder())
                                .build())
                        .toList())
                .assessments(course.getAssessments().stream()
                        .map(assessment -> AssessmentDto.builder()
                                .id(assessment.getId())
                                .title(assessment.getTitle())
                                .totalQuestions(assessment.getTotalQuestions())
                                .totalMarks(assessment.getTotalMarks())
                                .timeLimitMinutes(assessment.getTimeLimitMinutes())
                                .build())
                        .toList())
                .messages(messages)
                .build();
    }

    @Transactional
    public DashboardDto completeNextLesson(User user, Long courseId) {
        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(user.getId(), courseId)
                .orElseThrow(() -> new IllegalArgumentException("Enrollment not found"));

        int totalLessons = enrollment.getCourse().getLessons().size();
        enrollment.setCompletedLessons(Math.min(totalLessons, enrollment.getCompletedLessons() + 1));
        enrollment.setProgressPercent((int) Math.round((enrollment.getCompletedLessons() * 100.0) / Math.max(1, totalLessons)));
        enrollment.setPointsEarned(enrollment.getPointsEarned() + 35);

        user.setXp(user.getXp() + 45);
        user.setLevel(1 + (user.getXp() / 250));
        user.setStreakDays(user.getStreakDays() + 1);

        enrollmentRepository.save(enrollment);
        userRepository.save(user);

        DashboardDto dashboard = getDashboard(user);
        academyRealtimeService.pushDashboard(user, dashboard);
        return dashboard;
    }

    @Transactional
    public DashboardDto submitAssessment(User user, AssessmentSubmissionRequest request) {
        Assessment assessment = assessmentRepository.findById(request.assessmentId())
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found"));
        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(user.getId(), assessment.getCourse().getId())
                .orElseThrow(() -> new IllegalArgumentException("Enrollment not found"));

        int boundedScore = Math.max(0, Math.min(request.score(), assessment.getTotalMarks()));
        int percentage = (int) Math.round((boundedScore * 100.0) / Math.max(1, assessment.getTotalMarks()));

        assessmentAttemptRepository.save(AssessmentAttempt.builder()
                .user(user)
                .assessment(assessment)
                .score(boundedScore)
                .percentage(percentage)
                .submittedAt(LocalDateTime.now())
                .build());

        enrollment.setMasteryPercent(Math.max(enrollment.getMasteryPercent(), percentage));
        enrollment.setPointsEarned(enrollment.getPointsEarned() + Math.max(20, percentage / 2));
        user.setXp(user.getXp() + percentage);
        user.setLevel(1 + (user.getXp() / 250));

        enrollmentRepository.save(enrollment);
        userRepository.save(user);

        DashboardDto dashboard = getDashboard(user);
        academyRealtimeService.pushDashboard(user, dashboard);
        return dashboard;
    }

    @Transactional
    public MessageDto sendCourseMessage(User user, MessageRequest request) {
        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        MessageDto dto = toMessageDto(courseMessageRepository.save(CourseMessage.builder()
                .course(course)
                .sender(user)
                .senderRole(user.getRole().name())
                .message(request.message())
                .sentAt(LocalDateTime.now())
                .build()));

        academyRealtimeService.pushCourseMessage(course.getId(), dto);
        return dto;
    }

    public List<LiveClassDto> getLiveClasses() {
        return liveClassSessionRepository.findAllByOrderByScheduledForAsc().stream().map(this::toLiveClassDto).toList();
    }

    public void signalLivePresence(User user, Long liveClassId) {
        academyRealtimeService.pushLiveClassSignal(liveClassId, java.util.Map.of(
                "userId", user.getId(),
                "name", user.getFullName(),
                "role", user.getRole().name(),
                "joinedAt", LocalDateTime.now().toString()));
    }

    private CourseCardDto toCourseCardDto(Enrollment enrollment) {
        Course course = enrollment.getCourse();
        return CourseCardDto.builder()
                .id(course.getId())
                .slug(course.getSlug())
                .title(course.getTitle())
                .tagline(course.getTagline())
                .category(course.getCategory())
                .mentorName(course.getMentorName())
                .estimatedHours(course.getEstimatedHours())
                .creditWeight(course.getCreditWeight())
                .difficultyLevel(course.getDifficultyLevel().name())
                .accentColor(course.getAccentColor())
                .progressPercent(enrollment.getProgressPercent())
                .masteryPercent(enrollment.getMasteryPercent())
                .pointsEarned(enrollment.getPointsEarned())
                .lessonCount(course.getLessons().size())
                .assessmentCount(course.getAssessments().size())
                .build();
    }

    private LiveClassDto toLiveClassDto(LiveClassSession session) {
        return LiveClassDto.builder()
                .id(session.getId())
                .title(session.getTitle())
                .meetingCode(session.getMeetingCode())
                .scheduledFor(session.getScheduledFor())
                .durationMinutes(session.getDurationMinutes())
                .courseTitle(session.getCourse().getTitle())
                .instructorName(session.getInstructor().getFullName())
                .build();
    }

    private AttemptDto toAttemptDto(AssessmentAttempt attempt) {
        return AttemptDto.builder()
                .assessmentTitle(attempt.getAssessment().getTitle())
                .percentage(attempt.getPercentage())
                .submittedAt(attempt.getSubmittedAt())
                .build();
    }

    private MessageDto toMessageDto(CourseMessage message) {
        return MessageDto.builder()
                .id(message.getId())
                .courseId(message.getCourse().getId())
                .senderName(message.getSender().getFullName())
                .senderRole(message.getSenderRole())
                .message(message.getMessage())
                .sentAt(message.getSentAt())
                .build();
    }
}
