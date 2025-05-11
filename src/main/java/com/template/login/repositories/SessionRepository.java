package com.template.login.repositories;

import com.template.login.entities.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session,Long> {
    List<Session> findAllByUser_Email(String email);

    Optional<Session> findByRefreshToken(String refreshToken);
}
