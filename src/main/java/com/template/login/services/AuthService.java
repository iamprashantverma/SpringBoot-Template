package com.template.login.services;

import com.template.login.dto.LoginDTO;
import com.template.login.dto.LoginResponseDTO;
import com.template.login.dto.UserDTO;
import com.template.login.entities.Session;
import com.template.login.entities.User;
import com.template.login.exceptions.ResourceAlreadyExists;
import com.template.login.exceptions.ResourceNotFound;
import com.template.login.exceptions.UnauthorizedAccess;
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

    @Value("${SESSION_MAX_SESSIONS_PER_USER}")
    private final Integer MAX_SESSIONS_PER_USER ;

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
        String email = user.getEmail();

        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent())
                throw  new ResourceAlreadyExists("Email is Already Registered !. Please Login ");

        User toBeCreated =  modelMapper.map(user,User.class);

        String hashPassword = passwordEncoder.encode(user.getPassword());
        toBeCreated.setPassword(hashPassword);

        User savedUser = userRepository.save(toBeCreated);

        return modelMapper.map(savedUser,UserDTO.class);

    }

    @Transactional
    public LoginResponseDTO logIn(@Valid LoginDTO login, HttpServletResponse servletResponse) {
        String email = login.getEmail();
        String password = login.getPassword();

        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty())
            throw new ResourceNotFound("Email is not registered !. Sign Up");

        // handling sessions
        List<Session> userSessions = sessionRepository.findAllByUser_Email(email);

        if (userSessions.size() == MAX_SESSIONS_PER_USER) {
            userSessions.sort((a, b) -> a.getExpiresAt().compareTo(b.getExpiresAt()));
            Session oldSession = userSessions.get(0);
            sessionRepository.delete(oldSession);
        }

        // matching the password
        String hashPassword = optionalUser.get().getPassword();

        if (!passwordEncoder.matches(password,hashPassword)){
            throw  new UnauthorizedAccess("wrong password");
        }

        // generate the tokens
        String accessToken = jwtService.generateAccessToken(optionalUser.get());
        String refreshToken = jwtService.generateRefreshToken(optionalUser.get());

        Cookie cookie = new Cookie("refresh_token",refreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24 * 30);
        servletResponse.addCookie(cookie);

        return LoginResponseDTO.builder()
                .accessToken(accessToken)
                .build();
    }

    public LoginResponseDTO refreshToken(HttpServletRequest servletRequest) {
        String refreshToken = extractRefreshTokenFromCookies(servletRequest);
        if (refreshToken == null) {
            throw new RuntimeException("Refresh token is missing in cookies");
        }

        String userId = jwtService.getUserIdFromToken(refreshToken);

        User user = userRepository.findById(userId).orElseThrow(()->new ResourceNotFound("user not found with this Id:"+userId));
        String accessToken = jwtService.generateAccessToken(user);

        return  LoginResponseDTO.builder()
                .accessToken(accessToken)
                .build();
    }



}
