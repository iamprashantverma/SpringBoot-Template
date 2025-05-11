package com.template.login.repositories;

import com.template.login.entities.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BlackListedTokenRepository extends JpaRepository<BlacklistedToken,Long> {
    Optional<BlacklistedToken> findByRefreshToken(String refreshToken);

    Optional<BlacklistedToken> findByAccessToken(String token);
}
