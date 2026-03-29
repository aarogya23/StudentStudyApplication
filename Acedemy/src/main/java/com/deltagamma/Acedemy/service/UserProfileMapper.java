package com.deltagamma.Acedemy.service;

import org.springframework.stereotype.Component;

import com.deltagamma.Acedemy.dto.UserProfileDto;
import com.deltagamma.Acedemy.model.User;

@Component
public class UserProfileMapper {

    public UserProfileDto toDto(User user) {
        return UserProfileDto.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .level(user.getLevel())
                .xp(user.getXp())
                .streakDays(user.getStreakDays())
                .title(user.getTitle())
                .build();
    }
}
