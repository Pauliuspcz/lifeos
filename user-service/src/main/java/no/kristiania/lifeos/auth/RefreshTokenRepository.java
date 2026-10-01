package no.kristiania.lifeos.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    @Modifying
    @Query("update RefreshToken t set t.revoked = true where t.userId = :userId and t.revoked = false")
    void revokeAllByUserId(@Param("userId") Long userId);
}
