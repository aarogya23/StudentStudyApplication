package com.deltagamma.Acedemy.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.deltagamma.Acedemy.dto.AuthDtos.LoginRequest;
import com.deltagamma.Acedemy.dto.AuthDtos.SignupRequest;
import com.deltagamma.Acedemy.model.Course;
import com.deltagamma.Acedemy.model.Enrollment;
import com.deltagamma.Acedemy.model.Role;
import com.deltagamma.Acedemy.model.User;
import com.deltagamma.Acedemy.repository.CourseRepository;
import com.deltagamma.Acedemy.repository.EnrollmentRepository;
import com.deltagamma.Acedemy.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public User signup(SignupRequest input) {
        if (userRepository.findByEmail(input.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = userRepository.save(User.builder()
                .fullName(input.getFullName())
                .email(input.getEmail())
                .password(passwordEncoder.encode(input.getPassword()))
                .role(Role.STUDENT)
                .title("Semester Strategist")
                .xp(180)
                .streakDays(3)
                .build());

        enrollStudentInStarterCourses(user);
        return user;
    }

    public User authenticate(LoginRequest input) {
        User user = userRepository.findByEmail(input.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (user.getOauthProvider() != null && (user.getPassword() == null || user.getPassword().isBlank())) {
            throw new IllegalArgumentException("This account uses " + user.getOauthProvider() + " sign-in.");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(input.getEmail(), input.getPassword()));

        return user;
    }

    public User findOrCreateOAuth2User(String email, String name) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User user = userRepository.save(User.builder()
                    .fullName(name)
                    .email(email)
                    .oauthProvider("google")
                    .role(Role.STUDENT)
                    .title("Connected Scholar")
                    .xp(200)
                    .streakDays(5)
                    .build());
            enrollStudentInStarterCourses(user);
            return user;
        });
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private void enrollStudentInStarterCourses(User user) {
        if (user.getRole() != Role.STUDENT) {
            return;
        }

        for (Course course : courseRepository.findAll()) {
            boolean alreadyEnrolled = enrollmentRepository.findByUserIdAndCourseId(user.getId(), course.getId()).isPresent();
            if (!alreadyEnrolled) {
                enrollmentRepository.save(Enrollment.builder()
                        .user(user)
                        .course(course)
                        .completedLessons(0)
                        .progressPercent(0)
                        .masteryPercent(0)
                        .pointsEarned(0)
                        .build());
            }
        }
    }
}
