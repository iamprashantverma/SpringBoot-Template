package com.template.login.controller;

import com.template.login.dto.LoginDTO;
import com.template.login.dto.LoginResponseDTO;
import com.template.login.dto.UserDTO;
import com.template.login.services.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/auth")
@RestController
@Slf4j
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<UserDTO> signUp(@Valid @RequestBody UserDTO user) {
        UserDTO savedUser = authService.signUp(user);
        return ResponseEntity.status(201).body(savedUser);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> logIn(@Valid @RequestBody LoginDTO login, HttpServletResponse servletResponse) {
        LoginResponseDTO resp = authService.logIn(login,servletResponse);
        return ResponseEntity.status(200).body(resp);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refreshToken(HttpServletRequest req){
        LoginResponseDTO loginResponseDTO = authService.refreshToken(req);
        return ResponseEntity.ok(loginResponseDTO);
    }

}
