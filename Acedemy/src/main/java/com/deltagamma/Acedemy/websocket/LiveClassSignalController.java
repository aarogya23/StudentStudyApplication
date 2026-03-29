package com.deltagamma.Acedemy.websocket;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import com.deltagamma.Acedemy.model.User;
import com.deltagamma.Acedemy.service.AcademyService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class LiveClassSignalController {

    private final AcademyService academyService;

    @MessageMapping("/live-class.join")
    public void joinLiveClass(@Payload java.util.Map<String, Long> payload) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User user && payload.get("liveClassId") != null) {
            academyService.signalLivePresence(user, payload.get("liveClassId"));
        }
    }
}
