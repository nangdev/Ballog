package notfound.ballog.domain.auth.repository;

import notfound.ballog.domain.auth.entity.Auth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AuthRepository extends JpaRepository<Auth, Integer> {
    boolean existsByEmailAndIsActiveTrue(String email);

    Optional<Auth> findByEmail(String email);

    Optional<Auth> findByEmailAndIsActiveTrue(String email);

    Optional<Auth> findByAuthIdAndIsActiveTrue(Integer authId);

    Optional<Auth> findByUser_UserIdAndIsActiveTrue(UUID userId);
}
