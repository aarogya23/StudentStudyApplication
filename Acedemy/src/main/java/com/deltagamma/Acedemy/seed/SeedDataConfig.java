package com.deltagamma.Acedemy.seed;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.deltagamma.Acedemy.model.Assessment;
import com.deltagamma.Acedemy.model.Course;
import com.deltagamma.Acedemy.model.CourseMessage;
import com.deltagamma.Acedemy.model.DifficultyLevel;
import com.deltagamma.Acedemy.model.Enrollment;
import com.deltagamma.Acedemy.model.Lesson;
import com.deltagamma.Acedemy.model.LiveClassSession;
import com.deltagamma.Acedemy.model.Role;
import com.deltagamma.Acedemy.model.User;
import com.deltagamma.Acedemy.repository.CourseMessageRepository;
import com.deltagamma.Acedemy.repository.CourseRepository;
import com.deltagamma.Acedemy.repository.EnrollmentRepository;
import com.deltagamma.Acedemy.repository.LiveClassSessionRepository;
import com.deltagamma.Acedemy.repository.UserRepository;

@Configuration
public class SeedDataConfig {

    @Bean
    @ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
    CommandLineRunner seedAcademyData(
            UserRepository userRepository,
            CourseRepository courseRepository,
            EnrollmentRepository enrollmentRepository,
            LiveClassSessionRepository liveClassSessionRepository,
            CourseMessageRepository courseMessageRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() > 0) {
                return;
            }

            User instructor = userRepository.save(User.builder()
                    .fullName("Dr. Naina Kapoor")
                    .email("instructor@deltagamma.edu")
                    .password(passwordEncoder.encode("mentor123"))
                    .role(Role.INSTRUCTOR)
                    .title("Academic Lead")
                    .level(8)
                    .xp(2100)
                    .streakDays(20)
                    .build());

            User student = userRepository.save(User.builder()
                    .fullName("Aarav Shrestha")
                    .email("student@deltagamma.edu")
                    .password(passwordEncoder.encode("student123"))
                    .role(Role.STUDENT)
                    .title("Capstone Contender")
                    .level(4)
                    .xp(620)
                    .streakDays(6)
                    .build());

            Course distributedSystems = Course.builder()
                    .slug("distributed-systems")
                    .title("Distributed Systems Studio")
                    .tagline("Design resilient architectures for modern products.")
                    .category("Computer Science")
                    .mentorName(instructor.getFullName())
                    .estimatedHours(42)
                    .creditWeight(4)
                    .difficultyLevel(DifficultyLevel.ADVANCED)
                    .accentColor("#27c2ff")
                    .build();

            distributedSystems.setLessons(List.of(
                    Lesson.builder().title("Consensus Patterns").topic("Raft vs Paxos").durationMinutes(48).sequenceOrder(1).course(distributedSystems).build(),
                    Lesson.builder().title("Event-Driven Modules").topic("Async choreography").durationMinutes(42).sequenceOrder(2).course(distributedSystems).build(),
                    Lesson.builder().title("Scaling Tactics").topic("Replication and caching").durationMinutes(55).sequenceOrder(3).course(distributedSystems).build()));
            distributedSystems.setAssessments(List.of(
                    Assessment.builder().title("Architecture Case Sprint").totalQuestions(20).totalMarks(100).timeLimitMinutes(45).course(distributedSystems).build(),
                    Assessment.builder().title("Fault Tolerance Challenge").totalQuestions(15).totalMarks(75).timeLimitMinutes(30).course(distributedSystems).build()));

            Course dataIntelligence = Course.builder()
                    .slug("data-intelligence")
                    .title("Data Intelligence Lab")
                    .tagline("Transform raw datasets into sharp decisions.")
                    .category("Analytics")
                    .mentorName(instructor.getFullName())
                    .estimatedHours(36)
                    .creditWeight(3)
                    .difficultyLevel(DifficultyLevel.INTERMEDIATE)
                    .accentColor("#12b981")
                    .build();
            dataIntelligence.setLessons(List.of(
                    Lesson.builder().title("Feature Thinking").topic("Signals and labels").durationMinutes(38).sequenceOrder(1).course(dataIntelligence).build(),
                    Lesson.builder().title("Dashboard Strategy").topic("Narrative metrics").durationMinutes(44).sequenceOrder(2).course(dataIntelligence).build(),
                    Lesson.builder().title("Model Review").topic("Precision and recall").durationMinutes(52).sequenceOrder(3).course(dataIntelligence).build()));
            dataIntelligence.setAssessments(List.of(
                    Assessment.builder().title("Insight Mining Drill").totalQuestions(25).totalMarks(100).timeLimitMinutes(40).course(dataIntelligence).build()));

            courseRepository.saveAll(List.of(distributedSystems, dataIntelligence));

            enrollmentRepository.saveAll(List.of(
                    Enrollment.builder().user(student).course(distributedSystems).completedLessons(1).progressPercent(34).masteryPercent(72).pointsEarned(220).build(),
                    Enrollment.builder().user(student).course(dataIntelligence).completedLessons(2).progressPercent(67).masteryPercent(81).pointsEarned(340).build()));

            liveClassSessionRepository.saveAll(List.of(
                    LiveClassSession.builder().title("Systems Design War Room").meetingCode("DG-LIVE-401").scheduledFor(LocalDateTime.now().plusHours(2)).durationMinutes(75).course(distributedSystems).instructor(instructor).build(),
                    LiveClassSession.builder().title("Analytics Review Clinic").meetingCode("DG-LIVE-221").scheduledFor(LocalDateTime.now().plusDays(1)).durationMinutes(60).course(dataIntelligence).instructor(instructor).build()));

            courseMessageRepository.saveAll(List.of(
                    CourseMessage.builder().course(distributedSystems).sender(instructor).senderRole("INSTRUCTOR").message("Share your trade-off notes before live class so we can critique them in depth.").sentAt(LocalDateTime.now().minusHours(6)).build(),
                    CourseMessage.builder().course(distributedSystems).sender(student).senderRole("STUDENT").message("I mapped latency vs consistency scenarios and want feedback on the quorum section.").sentAt(LocalDateTime.now().minusHours(5)).build()));
        };
    }
}
