package com.deltagamma.Acedemy.dto;

import com.deltagamma.Acedemy.model.Role;

import lombok.Builder;

@Builder
public record UserProfileDto(
        Long id,
        String fullName,
        String email,
        Role role,
        int level,
        int xp,
        int streakDays,
        String title) {
}
