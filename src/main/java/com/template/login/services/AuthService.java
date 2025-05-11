package com.template.login.services;

import com.template.login.dto.LoginDTO;
import com.template.login.dto.LoginResponseDTO;
import com.template.login.dto.UserDTO;
import com.template.login.entities.BlacklistedToken;
import com.template.login.entities.Session;
import com.template.login.entities.User;
import com.template.login.exceptions.ResourceAlreadyExistsException;
import com.template.login.exceptions.ResourceNotFoundException;
import com.template.login.exceptions.UnauthorizedAccessException;
import com.template.login.repositories.BlackListedTokenRepository;
import com.template.login.repositories.SessionRepository;
import com.template.login.repositories.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;
    private final BlackListedTokenRepository blackListedTokenRepository;

    @Value("${SESSION_MAX_SESSIONS_PER_USER}")
    private int MAX_SESSIONS_PER_USER;

    private String extractRefreshTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refresh_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    @Transactional
    public UserDTO signUp(@Valid UserDTO user) {
        Optional<User> optionalUser = userRepository.findByEmail(user.getEmail());
        if (optionalUser.isPresent()) {
            throw new ResourceAlreadyExistsException("Email is already registered. Please login.");
        }

        User toBeCreated = modelMapper.map(user, User.class);
        toBeCreated.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(toBeCreated);

        return modelMapper.map(savedUser, UserDTO.class);
    }

    @Transactional
    public LoginResponseDTO logIn(@Valid LoginDTO login, HttpServletResponse servletResponse) {
        User user = userRepository.findByEmail(login.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Email is not registered. Please sign up."));

        if (!passwordEncoder.matches(login.getPassword(), user.getPassword())) {
            throw new UnauthorizedAccessException("Incorrect password");
        }

        List<Session> userSessions = sessionRepository.findAllByUser_Email(login.getEmail());
        if (userSessions.size() >= MAX_SESSIONS_PER_USER) {
            userSessions.sort(Comparator.comparing(Session::getCreatedAt));
            sessionRepository.delete(userSessions.get(0));
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        Session newSession = Session.builder()
                .createdAt(LocalDateTime.now())
                .refreshToken(refreshToken)
                .user(user)
                .build();
        sessionRepository.save(newSession);

        Cookie cookie = new Cookie("refresh_token", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24 * 30); // 30 days
        servletResponse.addCookie(cookie);

        return LoginResponseDTO.builder()
                .accessToken(accessToken)
                .build();
    }

    public LoginResponseDTO refreshToken(HttpServletRequest servletRequest) {
        String refreshToken = extractRefreshTokenFromCookies(servletRequest);

        if (refreshToken == null) {
            throw new UnauthorizedAccessException("Refresh token is missing");
        }

        // Check if the refresh token is blacklisted
        Optional<BlacklistedToken> blacklistedOpt = blackListedTokenRepository.findByRefreshToken(refreshToken);
        if (blacklistedOpt.isPresent()) {
            throw new UnauthorizedAccessException("Refresh token has been blacklisted. Please log in again.");
        }

        String userId = jwtService.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        String newAccessToken = jwtService.generateAccessToken(user);

        // only do this if Session has an accessToken field
        Session session = sessionRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found for refresh token"));

        // session.setAccessToken(newAccessToken);
        sessionRepository.save(session);

        return LoginResponseDTO.builder()
                .accessToken(newAccessToken)
                .build();
    }

    @Transactional
    public void logOut(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshTokenFromCookies(request);

        if (refreshToken == null) {
            throw new UnauthorizedAccessException("Refresh token is missing in request");
        }

        Session session = sessionRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found for the given token"));

        sessionRepository.delete(session);

        // Remove refresh token cookie
        Cookie cookie = new Cookie("refresh_token", "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);

        BlacklistedToken blacklistedToken = BlacklistedToken.builder()
                        .accessToken(session.getAccessToken())
                                .refreshToken(session.getRefreshToken())
                                        .build();

        blackListedTokenRepository.save(blacklistedToken);

    }

}
