package com.deltagamma.Acedemy.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.deltagamma.Acedemy.dto.UserProfileDto;
import com.deltagamma.Acedemy.model.User;
import com.deltagamma.Acedemy.service.UserProfileMapper;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserProfileMapper userProfileMapper;

    @GetMapping
    public UserProfileDto me(@AuthenticationPrincipal User user) {
        return userProfileMapper.toDto(user);
    }
}
