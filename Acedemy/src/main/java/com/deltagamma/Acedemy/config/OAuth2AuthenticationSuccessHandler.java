package com.deltagamma.Acedemy.config;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.deltagamma.Acedemy.model.User;
import com.deltagamma.Acedemy.service.AuthService;
import com.deltagamma.Acedemy.service.JwtService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;
    private final JwtService jwtService;
    private final String frontendRedirectBase;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        User user = authService.findOrCreateOAuth2User(oauthUser.getAttribute("email"), oauthUser.getAttribute("name"));
        String token = jwtService.generateToken(user);
        response.sendRedirect(frontendRedirectBase + "/auth/callback?token="
                + URLEncoder.encode(token, StandardCharsets.UTF_8));
    }
}
