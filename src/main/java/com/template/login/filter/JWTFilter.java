package com.template.login.filter;


import com.template.login.entities.BlacklistedToken;
import com.template.login.entities.User;
import com.template.login.repositories.BlackListedTokenRepository;
import com.template.login.services.JWTService;
import com.template.login.services.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;


@Component
@Slf4j
public class JWTFilter extends OncePerRequestFilter {

    private final JWTService jwtService;
    private final UserService userService;
    private final BlackListedTokenRepository blackListedTokenRepository;

    public JWTFilter(JWTService jwtService, UserService userService, BlackListedTokenRepository blackListedTokenRepository) {
        this.jwtService = jwtService;
        this.userService = userService;
        this.blackListedTokenRepository = blackListedTokenRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        final  String requestTokenHeader = request.getHeader("Authorization");

        if (requestTokenHeader == null || !requestTokenHeader.startsWith("Bearer")) {
            filterChain.doFilter(request,response);
            return;
        } else {
            String token = requestTokenHeader.split(" ")[1];
            String userId = jwtService.getUserIdFromToken(token);
            Optional<BlacklistedToken> blacklistedToken = blackListedTokenRepository.findByAccessToken(token);

            if (blacklistedToken.isPresent()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token is blacklisted. Please login again.");
                return;
            }

            if (userId != null) {
                User user = userService.getUserById(userId);
                log.info("user int jwtFilter  is:{}",user);
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken( user,null,user.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
            filterChain.doFilter(request,response);
        }
    }


}

