package com.template.login.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import static com.template.login.util.UserUtil.generateSessionId;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(nullable = false,name = "user-Id")
    private User user;

    @Column(nullable = false)
    private String refreshToken;

    @Column(nullable = false)
    private String accessToken;

    @Column(nullable = false)
    private LocalDateTime createdAt;

}
